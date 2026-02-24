package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.response.GetItemRequest;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Ticker;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.ability.impl.type.configurable.ConfigurableTierableAbility;
import us.eunoians.mcrpg.database.table.quest.QuestInstanceDAO;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.QuestConfigLoader;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Central manager for the quest system. Owns the type registries, loads quest definitions
 * from the {@code quests/} directory, and manages a three-tier memory model:
 * <ul>
 *     <li><b>Tier 1 (Active)</b> – In-memory map of all quests in {@link QuestState#NOT_STARTED}
 *     or {@link QuestState#IN_PROGRESS} state, indexed by a reverse player-to-quest map for O(1)
 *     lookups from event handlers.</li>
 *     <li><b>Tier 2 (Cache)</b> – Caffeine cache of recently finished (completed/cancelled/expired)
 *     quests, kept alive while any scope player is online and evicted after a configurable TTL.</li>
 *     <li><b>Tier 3 (Database)</b> – All quests in SQL, loaded on-demand via DAO when not found in
 *     Tier 1 or Tier 2.</li>
 * </ul>
 */
public class QuestManager extends Manager<McRPG> {

    private static final String QUESTS_DIRECTORY = "quests";
    private static final String DEFAULT_QUEST_RESOURCE = "quests/example_quest.yml";
    private static final String DEFAULT_UPGRADE_QUEST_RESOURCE = "quests/upgrades/swords_upgrades.yml";
    private static final String DEFAULT_GENERIC_UPGRADE_QUEST_RESOURCE = "quests/upgrades/generic_ability_upgrades.yml";
    private static final String DEFAULT_TIER_OVERRIDE_UPGRADE_QUEST_RESOURCE = "quests/upgrades/tier_override_ability_upgrades.yml";

    private final long finishedQuestKeepAliveNanos;
    private final long finishedQuestOfflineTtlNanos;

    private final QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private final QuestRewardTypeRegistry rewardTypeRegistry;
    private final QuestScopeProviderRegistry scopeProviderRegistry;
    private final QuestConfigLoader configLoader;
    private final QuestDefinitionRegistry questDefinitionRegistry;

    /** Tier 1: active quests keyed by quest UUID. */
    private final Map<UUID, QuestInstance> activeQuests;

    /** Reverse index: playerUUID -> set of active quest UUIDs the player contributes to. */
    private final Map<UUID, Set<UUID>> playerToQuestIndex;

    /** Tier 2: recently finished quests, evicted based on scope-aware TTL. */
    private final Cache<UUID, QuestInstance> cachedFinishedQuests;

    /** In-flight async load requests keyed by quest UUID (deduplication). */
    private final Map<UUID, GetItemRequest<QuestInstance>> pendingRequests;

    public QuestManager(@NonNull McRPG plugin) {
        this(plugin, Ticker.systemTicker());
    }

    /**
     * Creates a new quest manager with a custom {@link Ticker} for the Caffeine cache.
     * The ticker controls cache TTL timing and can be replaced in tests for deterministic
     * expiry behaviour.
     *
     * @param plugin the McRPG plugin instance
     * @param ticker the ticker to use for cache time tracking
     */
    public QuestManager(@NonNull McRPG plugin, @NotNull Ticker ticker) {
        super(plugin);

        RegistryAccess registryAccess = plugin.registryAccess();
        this.objectiveTypeRegistry = registryAccess.registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        this.rewardTypeRegistry = registryAccess.registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        this.scopeProviderRegistry = registryAccess.registry(McRPGRegistryKey.QUEST_SCOPE_PROVIDER);
        this.questDefinitionRegistry = registryAccess.registry(McRPGRegistryKey.QUEST_DEFINITION);

        this.configLoader = new QuestConfigLoader();

        YamlDocument mainConfig = registryAccess
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG);
        if (mainConfig != null) {
            this.finishedQuestKeepAliveNanos = Duration.ofMinutes(
                    mainConfig.getInt(MainConfigFile.QUEST_CACHE_FINISHED_KEEP_ALIVE_MINUTES, 15)).toNanos();
            this.finishedQuestOfflineTtlNanos = Duration.ofMinutes(
                    mainConfig.getInt(MainConfigFile.QUEST_CACHE_FINISHED_OFFLINE_TTL_MINUTES, 5)).toNanos();
        } else {
            this.finishedQuestKeepAliveNanos = Duration.ofMinutes(15).toNanos();
            this.finishedQuestOfflineTtlNanos = Duration.ofMinutes(5).toNanos();
        }

        extractDefaultQuestResources(plugin);

        this.activeQuests = new ConcurrentHashMap<>();
        this.playerToQuestIndex = new ConcurrentHashMap<>();
        this.cachedFinishedQuests = Caffeine.newBuilder().ticker(ticker).expireAfter(new Expiry<UUID, QuestInstance>() {
            @Override
            public long expireAfterCreate(UUID uuid, QuestInstance questInstance, long currentTime) {
                return computeNextExpiryNanos(questInstance);
            }

            @Override
            public long expireAfterUpdate(UUID uuid, QuestInstance questInstance, long currentTime,
                                          @NonNegative long currentDuration) {
                return computeNextExpiryNanos(questInstance);
            }

            @Override
            public long expireAfterRead(UUID uuid, QuestInstance questInstance, long currentTime,
                                        @NonNegative long currentDuration) {
                return currentDuration;
            }

            private long computeNextExpiryNanos(@NotNull QuestInstance questInstance) {
                int playersOnlineInScope = questInstance.getQuestScope()
                        .map(scope -> (int) scope.getCurrentPlayersInScope().stream()
                                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                                .count())
                        .orElse(0);
                return playersOnlineInScope > 0 ? finishedQuestKeepAliveNanos : finishedQuestOfflineTtlNanos;
            }
        }).build();
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    /**
     * Gets an immutable copy of all currently active quest instances.
     *
     * @return an immutable list of all Tier 1 quests
     */
    @NotNull
    public List<QuestInstance> getActiveQuests() {
        return List.copyOf(activeQuests.values());
    }

    /**
     * Gets all active quests where the given player is in scope. Uses the
     * player contribution index for O(1) lookup instead of iterating all
     * active quests.
     *
     * @param playerUUID the player to find quests for
     * @return an immutable list of active quests the player is part of
     */
    @NotNull
    public List<QuestInstance> getActiveQuestsForPlayer(@NotNull UUID playerUUID) {
        Set<UUID> questUUIDs = playerToQuestIndex.get(playerUUID);
        if (questUUIDs == null || questUUIDs.isEmpty()) {
            return List.of();
        }
        List<QuestInstance> result = new ArrayList<>(questUUIDs.size());
        for (UUID questUUID : questUUIDs) {
            QuestInstance quest = activeQuests.get(questUUID);
            if (quest != null) {
                result.add(quest);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Checks whether a quest instance UUID is currently tracked in Tier 1 (active quests).
     *
     * @param questUUID the quest instance UUID
     * @return {@code true} if the quest is currently active in Tier 1
     */
    public boolean isQuestActive(@NotNull UUID questUUID) {
        return activeQuests.containsKey(questUUID);
    }

    /**
     * Checks whether a player is allowed to start a new instance of the given quest
     * definition, based on the quest's repeat mode and the player's completion history.
     * <p>
     * This method performs a synchronous database query and must be called from an
     * async context (e.g., the database executor service).
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @param definition the quest definition to check
     * @return {@code true} if the player may start the quest, {@code false} if blocked by repeat rules
     */
    public boolean canPlayerStartQuest(@NotNull Connection connection,
                                       @NotNull UUID playerUUID,
                                       @NotNull QuestDefinition definition) {
        if (hasActiveInstanceOfDefinition(playerUUID, definition.getQuestKey())) {
            return false;
        }

        String defKey = definition.getQuestKey().toString();
        QuestRepeatMode mode = definition.getRepeatMode();

        switch (mode) {
            case ONCE:
                return !QuestCompletionLogDAO.hasCompleted(connection, playerUUID, defKey);
            case LIMITED:
                int limit = definition.getRepeatLimit().orElse(1);
                return QuestCompletionLogDAO.getCompletionCount(connection, playerUUID, defKey) < limit;
            case COOLDOWN:
                OptionalLong lastTime = QuestCompletionLogDAO.getLastCompletionTime(connection, playerUUID, defKey);
                if (lastTime.isEmpty()) {
                    return true;
                }
                Duration cooldown = definition.getRepeatCooldown().orElse(Duration.ZERO);
                return McRPG.getInstance().getTimeProvider().now().toEpochMilli() >= lastTime.getAsLong() + cooldown.toMillis();
            case REPEATABLE:
                return true;
            default:
                return true;
        }
    }

    /**
     * Checks whether a player already has an active (Tier 1) quest instance for the
     * given definition key. Used to prevent duplicate concurrent quest instances.
     *
     * @param playerUUID    the player UUID
     * @param definitionKey the quest definition's namespaced key
     * @return {@code true} if the player has an active instance of this definition
     */
    public boolean hasActiveInstanceOfDefinition(@NotNull UUID playerUUID, @NotNull NamespacedKey definitionKey) {
        Set<UUID> questUUIDs = playerToQuestIndex.get(playerUUID);
        if (questUUIDs == null || questUUIDs.isEmpty()) {
            return false;
        }
        for (UUID questUUID : questUUIDs) {
            QuestInstance quest = activeQuests.get(questUUID);
            if (quest != null && quest.getQuestKey().equals(definitionKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates, scopes, starts, and tracks a new quest instance from the given definition.
     * Resolves the scope provider from the definition's scope type, creates a new scope,
     * assigns the initial player, and moves the quest into Tier 1.
     *
     * @param definition         the quest definition to instantiate
     * @param initialPlayerUUID  the UUID of the player who initiates or is assigned to the quest
     * @param questSource        the source that originated this quest
     * @return the started quest instance, or empty if the scope provider could not be resolved
     */
    @NotNull
    public Optional<QuestInstance> startQuest(@NotNull QuestDefinition definition,
                                             @NotNull UUID initialPlayerUUID,
                                             @NotNull QuestSource questSource) {
        return startQuest(definition, initialPlayerUUID, Map.of(), questSource);
    }

    /**
     * Creates, scopes, starts, and tracks a new quest instance from the given definition,
     * supplying variables used for resolving expression-based configuration (e.g. objective
     * {@code required-progress}).
     *
     * @param definition         the quest definition to instantiate
     * @param initialPlayerUUID  the UUID of the player who initiates or is assigned to the quest
     * @param variables          variables available when instantiating this quest (e.g. {@code tier})
     * @param questSource        the source that originated this quest
     * @return the started quest instance, or empty if the scope provider could not be resolved
     */
    @NotNull
    public Optional<QuestInstance> startQuest(@NotNull QuestDefinition definition,
                                              @NotNull UUID initialPlayerUUID,
                                              @NotNull Map<String, Object> variables,
                                              @NotNull QuestSource questSource) {
        NamespacedKey scopeKey = definition.getScopeType();
        Optional<QuestScopeProvider<?>> providerOpt = scopeProviderRegistry.get(scopeKey);
        if (providerOpt.isEmpty()) {
            NamespacedKey alias = resolveScopeProviderAlias(scopeKey);
            if (alias != null) {
                providerOpt = scopeProviderRegistry.get(alias);
            }
        }
        if (providerOpt.isEmpty()) {
            plugin().getLogger().warning("No scope provider registered for type: " + scopeKey);
            return Optional.empty();
        }

        QuestInstance instance;
        try {
            instance = new QuestInstance(definition, null, variables, questSource, null);
        } catch (RuntimeException e) {
            plugin().getLogger().log(Level.SEVERE,
                    "Failed to instantiate quest " + definition.getQuestKey()
                            + " (variables=" + variables + "): " + e.getMessage(),
                    e);
            return Optional.empty();
        }
        var scope = providerOpt.get().createNewScope(instance.getQuestUUID());
        if (scope instanceof SinglePlayerQuestScope singleScope) {
            singleScope.setPlayerInScope(initialPlayerUUID);
        }
        instance.setQuestScope(scope);
        instance.start(definition);
        trackActiveQuest(instance);
        return Optional.of(instance);
    }

    /**
     * Attempts to resolve a compatibility alias for a scope key.
     * <p>
     * Historical configs often use {@code <type>} while scope providers use {@code <type>_scope}.
     * This method bridges the gap in either direction without requiring duplicate registrations.
     */
    private static NamespacedKey resolveScopeProviderAlias(@NotNull NamespacedKey scopeKey) {
        String key = scopeKey.getKey();
        if (key.endsWith("_scope")) {
            String trimmed = key.substring(0, key.length() - "_scope".length());
            return trimmed.isEmpty() ? null : new NamespacedKey(scopeKey.getNamespace(), trimmed);
        }
        return new NamespacedKey(scopeKey.getNamespace(), key + "_scope");
    }

    /**
     * Performs a sanity check across all {@link TierableAbility TierableAbilities} for
     * the given player. For each ability:
     * <ul>
     *     <li>If the player has a stale {@link AbilityUpgradeQuestAttribute} (quest no longer
     *         active), it is cleared.</li>
     *     <li>If the player has no active upgrade quest but is eligible for the next tier
     *         (meets the level requirement), the quest is validated and started asynchronously.</li>
     * </ul>
     *
     * @param mcRPGPlayer the player to check
     */
    public void sanityCheckUpgradeQuests(@NotNull McRPGPlayer mcRPGPlayer) {
        AbilityHolder abilityHolder = mcRPGPlayer.asSkillHolder();
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
            Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (!(ability instanceof TierableAbility tierableAbility)) {
                continue;
            }

            Optional<AbilityData> abilityDataOpt = abilityHolder.getAbilityData(ability);
            if (abilityDataOpt.isEmpty()) {
                continue;
            }

            AbilityData abilityData = abilityDataOpt.get();
            int currentTier = tierableAbility.getCurrentAbilityTier(abilityHolder);
            int nextTier = currentTier + 1;

            if (nextTier > tierableAbility.getMaxTier()) {
                continue;
            }

            var questAttrOpt = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
            if (questAttrOpt.isPresent() && questAttrOpt.get() instanceof AbilityUpgradeQuestAttribute questAttr
                    && questAttr.shouldContentBeSaved()) {
                if (isQuestActive(questAttr.getContent())) {
                    continue;
                }
                abilityData.addAttribute(new AbilityUpgradeQuestAttribute(AbilityUpgradeQuestAttribute.defaultUUID()));
            }

            if (tierableAbility instanceof SkillAbility skillAbility) {
                int requiredLevel = tierableAbility.getUnlockLevelForTier(nextTier);
                Optional<Integer> currentLevel = mcRPGPlayer.asSkillHolder()
                        .getSkillHolderData(skillAbility.getSkillKey())
                        .map(data -> data.getCurrentLevel());
                if (currentLevel.isEmpty() || currentLevel.get() < requiredLevel) {
                    continue;
                }
            }

            Optional<QuestDefinition> defOpt = resolveUpgradeQuestDefinition(tierableAbility, nextTier);
            if (defOpt.isEmpty()) {
                continue;
            }

            QuestDefinition definition = defOpt.get();
            UUID playerUUID = mcRPGPlayer.getUUID();

            Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.DATABASE).getDatabase();
            database.getDatabaseExecutorService().submit(() -> {
                try (Connection connection = database.getConnection()) {
                    if (!canPlayerStartQuest(connection, playerUUID, definition)) {
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin(), () -> {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player == null || !player.isOnline()) {
                            return;
                        }

                        var currentAttr = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
                        if (currentAttr.isPresent()
                                && currentAttr.get() instanceof AbilityUpgradeQuestAttribute existing
                                && existing.shouldContentBeSaved()
                                && isQuestActive(existing.getContent())) {
                            return;
                        }

                        startQuest(definition, playerUUID, Map.of("tier", nextTier), new AbilityUpgradeQuestSource()).ifPresent(instance ->
                                abilityData.addAttribute(new AbilityUpgradeQuestAttribute(instance.getQuestUUID())));
                    });
                } catch (SQLException e) {
                    plugin().getLogger().log(Level.SEVERE,
                            "Failed sanity check for upgrade quest eligibility, player " + playerUUID, e);
                }
            });
        }
    }

    /**
     * Retrieves a quest instance using the three-tier lookup model:
     * Tier 1 (active) -> Tier 2 (cache) -> Tier 3 (database). The result is
     * wrapped in a {@link GetItemRequest} whose future completes when the quest
     * is available. Duplicate in-flight requests for the same UUID are coalesced.
     *
     * @param questUUID the quest instance UUID
     * @return a request handle whose future resolves to the quest instance
     */
    @NotNull
    public GetItemRequest<QuestInstance> getQuestInstance(@NotNull UUID questUUID) {
        // Tier 1: active quests
        QuestInstance active = activeQuests.get(questUUID);
        if (active != null) {
            CompletableFuture<QuestInstance> immediate = CompletableFuture.completedFuture(active);
            return new GetItemRequest<>(immediate);
        }

        // Tier 2: cached finished quests
        QuestInstance cached = cachedFinishedQuests.getIfPresent(questUUID);
        if (cached != null) {
            CompletableFuture<QuestInstance> immediate = CompletableFuture.completedFuture(cached);
            return new GetItemRequest<>(immediate);
        }

        // Deduplicate in-flight requests
        GetItemRequest<QuestInstance> existing = pendingRequests.get(questUUID);
        if (existing != null) {
            return existing;
        }

        // Tier 3: load from database
        CompletableFuture<QuestInstance> future = new CompletableFuture<>();
        GetItemRequest<QuestInstance> request = new GetItemRequest<>(future);
        pendingRequests.put(questUUID, request);

        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                Optional<QuestInstance> loaded = QuestInstanceDAO.loadFullQuestTree(connection, questUUID);
                if (loaded.isPresent()) {
                    QuestInstance quest = loaded.get();
                    placeInCorrectTier(quest);
                    future.complete(quest);
                } else {
                    future.complete(null);
                }
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE, "Failed to load quest " + questUUID + " from database", e);
                future.completeExceptionally(e);
            } finally {
                pendingRequests.remove(questUUID);
            }
        });

        return request;
    }

    /**
     * Adds a quest to the Tier 1 active map and indexes all current in-scope
     * players in the player contribution index. Call this when a quest is started
     * or loaded from the database in an active state.
     *
     * @param quest the quest instance to track
     */
    public void trackActiveQuest(@NotNull QuestInstance quest) {
        activeQuests.put(quest.getQuestUUID(), quest);
        indexQuestForAllScopePlayers(quest);
    }

    /**
     * Moves a quest from Tier 1 (active) to Tier 2 (cache) and removes it from
     * the player contribution index. Call this when a quest completes, is cancelled,
     * or expires.
     *
     * @param quest the quest instance to retire
     */
    public void retireQuest(@NotNull QuestInstance quest) {
        activeQuests.remove(quest.getQuestUUID());
        deindexQuest(quest);
        cachedFinishedQuests.put(quest.getQuestUUID(), quest);
    }

    /**
     * Places a quest instance into the correct tier based on its current state.
     * Active quests go to Tier 1 with full indexing; finished quests go to Tier 2.
     *
     * @param quest the quest instance to place
     */
    private void placeInCorrectTier(@NotNull QuestInstance quest) {
        if (quest.getQuestState() == QuestState.IN_PROGRESS || quest.getQuestState() == QuestState.NOT_STARTED) {
            trackActiveQuest(quest);
        } else {
            cachedFinishedQuests.put(quest.getQuestUUID(), quest);
        }
    }

    /**
     * Adds a single player -> quest mapping to the contribution index.
     *
     * @param questUUID  the quest UUID
     * @param playerUUID the player UUID
     */
    public void indexQuestForPlayer(@NotNull UUID questUUID, @NotNull UUID playerUUID) {
        playerToQuestIndex.computeIfAbsent(playerUUID, k -> ConcurrentHashMap.newKeySet()).add(questUUID);
    }

    /**
     * Removes a single player -> quest mapping from the contribution index.
     *
     * @param questUUID  the quest UUID
     * @param playerUUID the player UUID
     */
    public void deindexQuestForPlayer(@NotNull UUID questUUID, @NotNull UUID playerUUID) {
        Set<UUID> quests = playerToQuestIndex.get(playerUUID);
        if (quests != null) {
            quests.remove(questUUID);
            if (quests.isEmpty()) {
                playerToQuestIndex.remove(playerUUID);
            }
        }
    }

    /**
     * Indexes a quest for every player currently in its scope. If the quest has no
     * scope assigned yet, this is a no-op.
     *
     * @param quest the quest instance to index
     */
    private void indexQuestForAllScopePlayers(@NotNull QuestInstance quest) {
        quest.getQuestScope().ifPresent(scope -> {
            for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                indexQuestForPlayer(quest.getQuestUUID(), playerUUID);
            }
        });
    }

    /**
     * Removes a quest from every player's index entry. Used when a quest leaves
     * Tier 1 (retirement, cancellation, etc.).
     *
     * @param quest the quest instance to deindex
     */
    private void deindexQuest(@NotNull QuestInstance quest) {
        UUID questUUID = quest.getQuestUUID();
        for (Map.Entry<UUID, Set<UUID>> entry : playerToQuestIndex.entrySet()) {
            entry.getValue().remove(questUUID);
        }
        playerToQuestIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Removes a player entirely from the contribution index. Call this when a player
     * disconnects and their quest associations are no longer needed in the index.
     *
     * @param playerUUID the player to remove from the index
     */
    public void deindexPlayer(@NotNull UUID playerUUID) {
        playerToQuestIndex.remove(playerUUID);
    }

    /**
     * Gets an immutable snapshot of the quest UUIDs indexed for a given player.
     *
     * @param playerUUID the player UUID
     * @return the set of quest UUIDs, or an empty set
     */
    @NotNull
    public Set<UUID> getIndexedQuestUUIDs(@NotNull UUID playerUUID) {
        Set<UUID> quests = playerToQuestIndex.get(playerUUID);
        return quests != null ? Set.copyOf(quests) : Set.of();
    }

    /**
     * Rescopes a player across all registered scope providers. For each provider, queries
     * the database for active quests where the player is in scope, loads any quests that are
     * not already in Tier 1, and adds them to the player's contribution index.
     * <p>
     * Typically called on player login to ensure all relevant quests are loaded and indexed.
     *
     * @param playerUUID the player to rescope
     */
    public void rescopePlayer(@NotNull UUID playerUUID) {
        for (QuestScopeProvider<?> provider : scopeProviderRegistry.getRegisteredProviders()) {
            rescopePlayer(playerUUID, provider);
        }
    }

    /**
     * Rescopes a player for a single scope provider type. Queries the database for active
     * quests matching this provider where the player is in scope, loads any missing quests
     * into Tier 1, and updates the player contribution index.
     * <p>
     * Called when a scope-change event occurs (e.g., joining a land) or at player login.
     *
     * @param playerUUID the player to rescope
     * @param provider   the scope provider to check
     */
    public void rescopePlayer(@NotNull UUID playerUUID, @NotNull QuestScopeProvider<?> provider) {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<UUID> questUUIDs = provider.resolveActiveQuestUUIDs(playerUUID, connection);
                for (UUID questUUID : questUUIDs) {
                    if (activeQuests.containsKey(questUUID)) {
                        indexQuestForPlayer(questUUID, playerUUID);
                    } else {
                        Optional<QuestInstance> loaded = QuestInstanceDAO.loadFullQuestTree(connection, questUUID);
                        loaded.ifPresent(quest -> {
                            trackActiveQuest(quest);
                            indexQuestForPlayer(questUUID, playerUUID);
                        });
                    }
                }
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE, "Failed to rescope player " + playerUUID + " for provider " + provider.getKey(), e);
            }
        });
    }

    /**
     * Abandons a quest if its source allows it.
     *
     * @param questUUID the UUID of the quest instance to abandon
     * @return {@code true} if the quest was successfully abandoned
     */
    public boolean abandonQuest(@NotNull UUID questUUID) {
        QuestInstance quest = activeQuests.get(questUUID);
        if (quest == null) {
            return false;
        }

        if (!quest.getQuestSource().isAbandonable()) {
            return false;
        }

        quest.cancel();
        return true;
    }

    /**
     * Asynchronously saves the full quest tree (quest, stages, objectives, contributions)
     * to the database using a {@link FailSafeTransaction} on the database executor thread.
     * Clears the quest's dirty flag on success.
     *
     * @param quest the quest instance to save
     */
    public void saveQuestAsync(@NotNull QuestInstance quest) {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                new FailSafeTransaction(connection, QuestInstanceDAO.saveFullQuestTree(connection, quest))
                        .executeTransaction();
                quest.clearDirty();
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE, "Failed to save quest " + quest.getQuestUUID(), e);
            }
        });
    }

    /**
     * Iterates all Tier 1 active quests and saves any that have been marked dirty
     * since the last save. Intended to be called periodically (e.g., every x minutes)
     * by the plugin's scheduled task.
     */
    public void saveDirtyQuests() {
        for (QuestInstance quest : activeQuests.values()) {
            if (quest.isDirty()) {
                saveQuestAsync(quest);
            }
        }
    }

    /**
     * Saves all Tier 1 active quests regardless of dirty state. Used during
     * server shutdown to ensure no data is lost.
     */
    public void saveAllActiveQuests() {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        try (Connection connection = database.getConnection()) {
            for (QuestInstance quest : activeQuests.values()) {
                new FailSafeTransaction(connection, QuestInstanceDAO.saveFullQuestTree(connection, quest))
                        .executeTransaction();
                quest.clearDirty();
            }
        } catch (SQLException e) {
            plugin().getLogger().log(Level.SEVERE, "Failed to save active quests during shutdown", e);
        }
    }

    /**
     * Loads all active quests ({@code NOT_STARTED} and {@code IN_PROGRESS}) from the
     * database into Tier 1. This should be called once during plugin startup after
     * tables have been created and scope providers registered.
     */
    public void loadActiveQuestsFromDatabase() {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<QuestInstance> quests = QuestInstanceDAO.loadQuestInstancesByState(
                        connection, QuestState.NOT_STARTED, QuestState.IN_PROGRESS);
                for (QuestInstance shell : quests) {
                    Optional<QuestInstance> fullTree = QuestInstanceDAO.loadFullQuestTree(connection, shell.getQuestUUID());
                    fullTree.ifPresent(this::trackActiveQuest);
                }
                plugin().getLogger().info("Loaded " + activeQuests.size() + " active quests from database.");
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE, "Failed to load active quests from database", e);
            }
        });
    }

    /**
     * Gets the objective type registry used by this quest manager.
     * External plugins can register custom objective types here.
     *
     * @return the objective type registry
     */
    @NotNull
    public QuestObjectiveTypeRegistry getObjectiveTypeRegistry() {
        return objectiveTypeRegistry;
    }

    /**
     * Gets the reward type registry used by this quest manager.
     * External plugins can register custom reward types here.
     *
     * @return the reward type registry
     */
    @NotNull
    public QuestRewardTypeRegistry getRewardTypeRegistry() {
        return rewardTypeRegistry;
    }

    /**
     * Gets the scope provider registry used by this quest manager.
     * External plugins can register custom scope providers here.
     *
     * @return the scope provider registry
     */
    @NotNull
    public QuestScopeProviderRegistry getScopeProviderRegistry() {
        return scopeProviderRegistry;
    }

    /**
     * Gets the quest definition registry. This is the global registry that holds all
     * loaded quest definitions. External plugins can also register definitions here.
     *
     * @return the quest definition registry
     */
    @NotNull
    public QuestDefinitionRegistry getQuestDefinitionRegistry() {
        return questDefinitionRegistry;
    }

    /**
     * Convenience method to get a quest definition by its key. Delegates to the
     * {@link QuestDefinitionRegistry}.
     *
     * @param key the namespaced key of the quest definition
     * @return the definition, or empty if not registered
     */
    @NotNull
    public Optional<QuestDefinition> getQuestDefinition(@NotNull NamespacedKey key) {
        return questDefinitionRegistry.get(key);
    }

    /**
     * Resolves an upgrade quest definition for the given tierable ability and target tier.
     * <p>
     * This supports "tier override" configurations that may not have a corresponding quest
     * definition present. In that case, it attempts fallback resolution in this order:
     * <ul>
     *     <li>the ability's configured tier-specific upgrade quest key</li>
     *     <li>the ability's configured all-tiers upgrade quest key (for configurable abilities)</li>
     *     <li>the inferred generic key {@code &lt;ability_key&gt;_upgrade}</li>
     * </ul>
     *
     * @param ability     the tierable ability
     * @param targetTier  the tier being upgraded to
     * @return the resolved quest definition, or empty if none exist
     */
    @NotNull
    public Optional<QuestDefinition> resolveUpgradeQuestDefinition(@NotNull TierableAbility ability, int targetTier) {
        Optional<NamespacedKey> primaryKeyOpt = ability.getUpgradeQuestKey(targetTier);
        if (primaryKeyOpt.isEmpty()) {
            return Optional.empty();
        }

        NamespacedKey primaryKey = primaryKeyOpt.get();
        Optional<QuestDefinition> primaryDef = getQuestDefinition(primaryKey);
        if (primaryDef.isPresent()) {
            return primaryDef;
        }

        // Fallback 1: configurable abilities may have a valid all-tiers quest key even if a tier override is missing.
        if (ability instanceof ConfigurableTierableAbility configurable) {
            YamlDocument doc = configurable.getYamlDocument();
            Route allTiersUpgradeQuestRoute = Route.addTo(configurable.getRouteForAllTiers(), "upgrade-quest");
            if (doc.contains(allTiersUpgradeQuestRoute)) {
                String allTiersKeyStr = doc.getString(allTiersUpgradeQuestRoute);
                if (allTiersKeyStr != null && !allTiersKeyStr.isEmpty()) {
                    allTiersKeyStr = allTiersKeyStr.replace("{tier}", String.valueOf(targetTier));
                    NamespacedKey allTiersKey = NamespacedKey.fromString(allTiersKeyStr);
                    if (allTiersKey != null && !allTiersKey.equals(primaryKey)) {
                        Optional<QuestDefinition> allTiersDef = getQuestDefinition(allTiersKey);
                        if (allTiersDef.isPresent()) {
                            return allTiersDef;
                        }
                    }
                }
            }
        }

        // Fallback 2: inferred generic quest key (<ability>_upgrade)
        NamespacedKey abilityKey = ability.getAbilityKey();
        NamespacedKey inferred = new NamespacedKey(abilityKey.getNamespace(), abilityKey.getKey() + "_upgrade");
        if (!inferred.equals(primaryKey)) {
            return getQuestDefinition(inferred);
        }

        return Optional.empty();
    }

    /**
     * Loads (or reloads) quest definitions from the {@code quests/} directory.
     * Replaces all previously loaded definitions in the registry with the newly
     * parsed ones.
     */
    public void loadQuestDefinitions() {
        File questsDir = new File(plugin().getDataFolder(), QUESTS_DIRECTORY);
        if (!questsDir.exists()) {
            questsDir.mkdirs();
        }
        questDefinitionRegistry.replaceConfigDefinitions(configLoader.loadQuestsFromDirectory(questsDir));
        enforceTierableAbilityUpgradeQuestConfiguration();
    }

    /**
     * Validates that all tierable abilities can resolve a usable upgrade quest definition.
     * If not, the ability is unregistered to prevent broken upgrade flows at runtime.
     * <p>
     * This runs after quest definitions are (re)loaded, so both configured quest keys and
     * inferred defaults can be checked against the registry.
     */
    private void enforceTierableAbilityUpgradeQuestConfiguration() {
        AbilityRegistry abilityRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        for (NamespacedKey abilityKey : abilityRegistry.getAllAbilities()) {
            Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
            if (!(ability instanceof TierableAbility tierableAbility)) {
                continue;
            }

            if (tierableAbility.getMaxTier() < 2) {
                continue;
            }

            Optional<QuestDefinition> defOpt = resolveUpgradeQuestDefinition(tierableAbility, 2);
            if (defOpt.isEmpty()) {
                Optional<NamespacedKey> questKeyOpt = tierableAbility.getUpgradeQuestKey(2);
                NamespacedKey resolvedKey = questKeyOpt.orElse(null);
                plugin().getLogger().severe("[TierableAbility] " + abilityKey
                        + " resolves upgrade quest key '" + resolvedKey + "' for tier 2, but no usable quest definition exists."
                        + " Unregistering ability.");
                abilityRegistry.unregisterAbility(abilityKey);
            }
        }
    }

    /**
     * Extracts default quest resources on first encounter only. Uses a marker file
     * ({@code .extracted-defaults}) in the quests directory to track which resources
     * have already been offered. A resource is extracted exactly once: on the first
     * startup where it appears in the defaults list. If a user later deletes the
     * extracted file, it will not be recreated. New defaults added in plugin updates
     * are extracted on the first startup after the update.
     *
     * @param plugin the McRPG plugin instance
     */
    private void extractDefaultQuestResources(@NotNull McRPG plugin) {
        File questsDir = new File(plugin.getDataFolder(), QUESTS_DIRECTORY);
        if (!questsDir.exists()) {
            questsDir.mkdirs();
        }
        File upgradesDir = new File(questsDir, "upgrades");
        if (!upgradesDir.exists()) {
            upgradesDir.mkdirs();
        }

        File markerFile = new File(questsDir, ".extracted-defaults");
        Set<String> alreadyExtracted = loadExtractedMarker(markerFile);

        List<String> defaultResources = List.of(
                DEFAULT_QUEST_RESOURCE,
                DEFAULT_UPGRADE_QUEST_RESOURCE,
                DEFAULT_GENERIC_UPGRADE_QUEST_RESOURCE,
                DEFAULT_TIER_OVERRIDE_UPGRADE_QUEST_RESOURCE
        );

        boolean markerDirty = false;
        for (String resource : defaultResources) {
            if (alreadyExtracted.contains(resource)) {
                continue;
            }
            plugin.saveResource(resource, false);
            alreadyExtracted.add(resource);
            markerDirty = true;
        }

        if (markerDirty) {
            saveExtractedMarker(markerFile, alreadyExtracted);
        }
    }

    @NotNull
    private static Set<String> loadExtractedMarker(@NotNull File markerFile) {
        Set<String> entries = new LinkedHashSet<>();
        if (!markerFile.exists()) {
            return entries;
        }
        try {
            for (String line : Files.readAllLines(markerFile.toPath())) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    entries.add(trimmed);
                }
            }
        } catch (IOException e) {
            // If we can't read the marker, treat as empty so defaults are re-offered
        }
        return entries;
    }

    private static void saveExtractedMarker(@NotNull File markerFile, @NotNull Set<String> entries) {
        try {
            Files.write(markerFile.toPath(), entries);
        } catch (IOException e) {
            // Non-fatal; worst case defaults are re-offered on next startup
        }
    }

}
