package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.database.table.board.BoardCooldownDAO;
import us.eunoians.mcrpg.database.table.board.BoardOfferingDAO;
import us.eunoians.mcrpg.database.table.board.BoardRotationDAO;
import us.eunoians.mcrpg.database.table.board.PersonalOfferingTrackingDAO;
import us.eunoians.mcrpg.database.table.board.PlayerBoardStateDAO;
import us.eunoians.mcrpg.database.table.board.ScopedBoardStateDAO;
import us.eunoians.mcrpg.database.table.quest.QuestCompletionLogDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent;
import us.eunoians.mcrpg.event.board.BoardOfferingExpireEvent;
import us.eunoians.mcrpg.event.board.BoardOfferingGenerateEvent;
import us.eunoians.mcrpg.event.board.BoardRotationEvent;
import us.eunoians.mcrpg.event.board.PersonalOfferingGenerateEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategoryRegistry;
import us.eunoians.mcrpg.quest.board.configuration.ReloadableCategoryConfig;
import us.eunoians.mcrpg.quest.board.configuration.ReloadableRarityConfig;
import us.eunoians.mcrpg.quest.board.configuration.ReloadableTemplateConfig;
import us.eunoians.mcrpg.quest.board.generation.PersonalOfferingGenerator;
import us.eunoians.mcrpg.quest.board.generation.QuestPool;
import us.eunoians.mcrpg.quest.board.generation.SlotGenerationLogic;
import us.eunoians.mcrpg.quest.board.generation.SlotSelection;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestDefinitionCodec;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.WeightedObjectiveSelector;
import us.eunoians.mcrpg.quest.board.template.condition.QuestCompletionHistory;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.QuestSourceRegistry;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;
import us.eunoians.mcrpg.util.PermissionNumberParser;

import java.io.File;
import java.sql.Connection;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Central manager for the quest board system.
 * <p>
 * Owns the board entities, the offering cache, and orchestrates rotation, generation,
 * acceptance, and abandonment. Registered in {@link McRPGManagerKey#QUEST_BOARD}.
 */
public class QuestBoardManager extends Manager<McRPG> {

    private static final String DEFAULT_ROTATION_TIMEZONE = "UTC";
    private static final String EXTRA_SLOTS_PERMISSION_PREFIX = "mcrpg.board.extra-slots.";
    private static final String EXTRA_OFFERINGS_PERMISSION_PREFIX = "mcrpg.extra-offerings.";
    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");

    private final Map<NamespacedKey, QuestBoard> boards = new HashMap<>();
    private final Map<NamespacedKey, Map<UUID, BoardOffering>> offeringCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> offeringLocks = new ConcurrentHashMap<>();
    private YamlDocument boardConfig;
    private QuestPool questPool;
    private QuestTemplateEngine templateEngine;
    private PersonalOfferingGenerator personalOfferingGenerator;
    private SlotGenerationLogic slotGenerationLogic;
    private GeneratedQuestDefinitionCodec codec;
    private ReloadableRarityConfig rarityConfig;
    private ReloadableCategoryConfig categoryConfig;
    private ReloadableTemplateConfig templateConfig;

    public QuestBoardManager(@NotNull McRPG plugin) {
        super(plugin);
    }

    private static final String[] DEFAULT_CATEGORY_RESOURCES = {
            "quest-board/categories/shared-daily.yml",
            "quest-board/categories/shared-weekly.yml",
            "quest-board/categories/personal-daily.yml",
            "quest-board/categories/personal-weekly.yml",
            "quest-board/categories/land-daily.yml",
            "quest-board/categories/land-weekly.yml"
    };

    private static final String[] DEFAULT_TEMPLATE_RESOURCES = {
            "quest-board/templates/combat/mob_slayer.yml",
            "quest-board/templates/combat/nether_combat.yml",
            "quest-board/templates/mining/ore_mining.yml",
            "quest-board/templates/mining/precision_mining.yml",
            "quest-board/templates/woodcutting/lumber.yml",
            "quest-board/templates/herbalism/farming.yml",
            "quest-board/templates/mixed/multi_skill.yml",
            "quest-board/templates/mixed/advanced.yml",
            "quest-board/templates/mixed/weekly_expeditions.yml",
            "quest-board/templates/legendary/legendary_personal.yml",
            "quest-board/templates/land/land_cooperative.yml"
    };

    public void initialize(@NotNull McRPG plugin) {
        extractDefaultBoardResources(plugin);

        // 1. Load board.yml
        this.boardConfig = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.BOARD_CONFIG);

        // 2. Set up rarity registry via reloadable config
        QuestRarityRegistry rarityRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);
        this.rarityConfig = new ReloadableRarityConfig(boardConfig, rarityRegistry);
        // Trigger initial load
        this.rarityConfig.getContent();

        // 3. Set up category registry via reloadable config
        BoardSlotCategoryRegistry categoryRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        File categoriesDir = new File(plugin.getDataFolder(), "quest-board/categories");
        this.categoryConfig = new ReloadableCategoryConfig(boardConfig, categoryRegistry, categoriesDir);
        this.categoryConfig.getContent();

        // 4. Register built-in refresh types
        RefreshTypeRegistry refreshTypeRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.REFRESH_TYPE);
        DayOfWeek resetDay = DayOfWeek.valueOf(
                boardConfig.getString(BoardConfigFile.ROTATION_WEEKLY_RESET_DAY).toUpperCase());
        refreshTypeRegistry.register(new DailyRefreshType());
        refreshTypeRegistry.register(new WeeklyRefreshType(resetDay));

        // 5. Create default board
        QuestBoard defaultBoard = new QuestBoard(DEFAULT_BOARD_KEY, boardConfig);
        boards.put(DEFAULT_BOARD_KEY, defaultBoard);

        // 6. Set up template engine + registry + quest pool
        QuestDefinitionRegistry definitionRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        QuestObjectiveTypeRegistry objectiveTypeRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        QuestRewardTypeRegistry rewardTypeRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        QuestTemplateRegistry templateRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.QUEST_TEMPLATE);
        var conditionRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.TEMPLATE_CONDITION);

        this.codec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, rewardTypeRegistry, conditionRegistry);
        this.templateEngine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry, plugin, new WeightedObjectiveSelector(), codec);
        this.slotGenerationLogic = new SlotGenerationLogic();
        this.personalOfferingGenerator = new PersonalOfferingGenerator(slotGenerationLogic);
        File primaryTemplatesDir = new File(plugin.getDataFolder(), "quest-board/templates");
        this.templateConfig = new ReloadableTemplateConfig(boardConfig, templateRegistry, conditionRegistry, primaryTemplatesDir);
        this.templateConfig.getContent();

        this.questPool = new QuestPool(definitionRegistry, templateRegistry, plugin().getLogger(), plugin.getTimeProvider());

        // 7. Load current rotations from DB and check for missed rotations
        Database database = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                long now = plugin.getTimeProvider().now().toEpochMilli();

                Optional<BoardRotation> dailyRotation =
                        BoardRotationDAO.loadCurrentRotation(connection, DEFAULT_BOARD_KEY, DailyRefreshType.KEY);
                dailyRotation.ifPresent(defaultBoard::setCurrentDailyRotation);
                if (dailyRotation.isEmpty()) {
                    plugin().getLogger().info("[QuestBoard] No daily rotation found. Triggering initial rotation.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(DailyRefreshType.KEY));
                } else if (now > dailyRotation.get().getExpiresAt()) {
                    plugin().getLogger().info("[QuestBoard] Detected missed daily rotation (expired: "
                            + dailyRotation.get().getExpiresAt() + ", now: " + now + "). Triggering catch-up.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(DailyRefreshType.KEY));
                }

                Optional<BoardRotation> weeklyRotation =
                        BoardRotationDAO.loadCurrentRotation(connection, DEFAULT_BOARD_KEY, WeeklyRefreshType.KEY);
                weeklyRotation.ifPresent(defaultBoard::setCurrentWeeklyRotation);
                if (weeklyRotation.isEmpty()) {
                    plugin().getLogger().info("[QuestBoard] No weekly rotation found. Triggering initial rotation.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(WeeklyRefreshType.KEY));
                } else if (now > weeklyRotation.get().getExpiresAt()) {
                    plugin().getLogger().info("[QuestBoard] Detected missed weekly rotation (expired: "
                            + weeklyRotation.get().getExpiresAt() + ", now: " + now + "). Triggering catch-up.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(WeeklyRefreshType.KEY));
                }
                // Warm the offering cache so getSharedOfferingsForBoard() never hits DB on main thread
                List<BoardOffering> warmOfferings = new ArrayList<>();
                // Load only shared (unscoped) offerings — scoped/personal rows must never enter the
                // shared cache, or they would render on every player's board and be acceptable cross-scope.
                dailyRotation.ifPresent(daily ->
                        warmOfferings.addAll(BoardOfferingDAO.loadSharedOfferingsForRotation(connection, daily.getRotationId())));
                weeklyRotation.ifPresent(weekly ->
                        warmOfferings.addAll(BoardOfferingDAO.loadSharedOfferingsForRotation(connection, weekly.getRotationId())));
                validateOfferingStates(connection, warmOfferings);
                offeringCache.put(DEFAULT_BOARD_KEY, toOfferingMap(warmOfferings));

                // Re-register generated definitions from all accepted offerings
                List<BoardOffering> acceptedGenerated =
                        BoardOfferingDAO.loadAcceptedGeneratedOfferings(connection);
                if (!acceptedGenerated.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () -> reRegisterGeneratedDefinitions(acceptedGenerated));
                }
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to load current rotations", e);
            }
        });

        plugin().getLogger().info("[QuestBoard] Initialized with " + rarityRegistry.getAll().size() + " rarities, "
                + categoryRegistry.getAll().size() + " categories");
    }

    /**
     * Extracts default board configuration resources (categories, templates) from the
     * plugin JAR to the data folder if they don't already exist.
     */
    private void extractDefaultBoardResources(@NotNull McRPG plugin) {
        File categoriesDir = new File(plugin.getDataFolder(), "quest-board/categories");
        if (!categoriesDir.exists()) {
            categoriesDir.mkdirs();
        }
        File templatesBaseDir = new File(plugin.getDataFolder(), "quest-board/templates");
        for (String sub : new String[]{"combat", "mining", "woodcutting", "herbalism", "mixed", "legendary", "land"}) {
            File dir = new File(templatesBaseDir, sub);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        for (String resource : DEFAULT_CATEGORY_RESOURCES) {
            File target = new File(plugin.getDataFolder(), resource);
            if (!target.exists()) {
                plugin.saveResource(resource, false);
            }
        }
        for (String resource : DEFAULT_TEMPLATE_RESOURCES) {
            File target = new File(plugin.getDataFolder(), resource);
            if (!target.exists()) {
                plugin.saveResource(resource, false);
            }
        }
    }

    /**
     * Registers a board instance with this manager.
     *
     * @param board the board to register
     */
    public void registerBoard(@NotNull QuestBoard board) {
        boards.put(board.getBoardKey(), board);
    }

    /**
     * Looks up a board by its key.
     *
     * @param key the board key
     * @return the board, or empty if not registered
     */
    @NotNull
    public Optional<QuestBoard> getBoard(@NotNull NamespacedKey key) {
        return Optional.ofNullable(boards.get(key));
    }

    /**
     * Returns the default global board ({@code mcrpg:default_board}).
     *
     * @return the default board
     */
    @NotNull
    public QuestBoard getDefaultBoard() {
        return boards.get(DEFAULT_BOARD_KEY);
    }

    /**
     * Triggers a rotation for the given refresh type. Creates a new rotation, expires old
     * offerings, generates new shared offerings, and persists everything.
     *
     * @param refreshTypeKey the refresh type that triggered this rotation
     */
    public void triggerRotation(@NotNull NamespacedKey refreshTypeKey) {
        QuestBoard board = getDefaultBoard();
        long now = plugin().getTimeProvider().now().toEpochMilli();

        // Determine epoch based on refresh type
        RefreshTypeRegistry refreshRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.REFRESH_TYPE);
        RefreshType refreshType = refreshRegistry.get(refreshTypeKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown refresh type: " + refreshTypeKey));

        ZonedDateTime zonedNow = plugin().getTimeProvider().now().atZone(getConfiguredRotationZone());
        long epoch = refreshType instanceof DailyRefreshType
                ? zonedNow.toLocalDate().toEpochDay()
                : WeeklyRefreshType.computeEpoch(zonedNow);

        BoardRotation rotation = new BoardRotation(
                UUID.randomUUID(), board.getBoardKey(), refreshTypeKey, epoch, now, now + Duration.ofDays(1).toMillis());

        Database database = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();

        // Phase 1 (DB executor): save rotation and expire old offerings
        database.getDatabaseExecutorService().submit(() -> {
            Optional<BoardRotation> previousRotation;

            try (Connection connection = database.getConnection()) {
                BoardRotationDAO.saveRotation(connection, rotation).forEach(ps -> {
                    try { ps.executeUpdate(); ps.close(); } catch (Exception e) {
                        plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to execute rotation save statement", e);
                    }
                });

                previousRotation = refreshTypeKey.equals(DailyRefreshType.KEY)
                        ? board.getCurrentDailyRotation()
                        : board.getCurrentWeeklyRotation();
                previousRotation.ifPresent(prev ->
                    BoardOfferingDAO.expireOfferingsForRotation(connection, prev.getRotationId())
                            .forEach(ps -> {
                                try { ps.executeUpdate(); ps.close(); } catch (Exception e) {
                                    plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to expire offering during rotation", e);
                                }
                            }));
            } catch (Exception e) {
                plugin().getLogger().log(Level.SEVERE, "[QuestBoard] Rotation failed for " + refreshTypeKey, e);
                return;
            }

            final Optional<BoardRotation> prevRef = previousRotation;

            // Phase 2 (main thread): generate offerings and fire events
            Bukkit.getScheduler().runTask(plugin(), () -> {
                prevRef.ifPresent(prev ->
                        Bukkit.getPluginManager().callEvent(new BoardOfferingExpireEvent(board, prev)));

                List<BoardOffering> offerings = generateSharedOfferings(board, rotation, new Random());
                offerings.addAll(generateScopedOfferings(board, rotation, new Random()));

                BoardOfferingGenerateEvent generateEvent = new BoardOfferingGenerateEvent(board, rotation, offerings);
                Bukkit.getPluginManager().callEvent(generateEvent);
                List<BoardOffering> finalOfferings = generateEvent.getOfferings();

                if (refreshTypeKey.equals(DailyRefreshType.KEY)) {
                    board.setCurrentDailyRotation(rotation);
                } else {
                    board.setCurrentWeeklyRotation(rotation);
                }
                List<BoardOffering> merged = new ArrayList<>(finalOfferings);
                Optional<UUID> otherRotationId = refreshTypeKey.equals(DailyRefreshType.KEY)
                        ? board.getCurrentWeeklyRotation().map(BoardRotation::getRotationId)
                        : board.getCurrentDailyRotation().map(BoardRotation::getRotationId);
                otherRotationId.ifPresent(otherId -> {
                    Map<UUID, BoardOffering> existing = offeringCache.getOrDefault(board.getBoardKey(), Map.of());
                    existing.values().stream()
                            .filter(o -> o.getRotationId().equals(otherId))
                            .forEach(merged::add);
                });
                offeringCache.put(board.getBoardKey(), toOfferingMap(merged));
                plugin().getLogger().info("[QuestBoard] Cache updated for " + board.getBoardKey() + " — "
                        + finalOfferings.size() + " new + " + (merged.size() - finalOfferings.size())
                        + " kept from other rotation = " + merged.size() + " total cached");
                offeringLocks.clear();

                Bukkit.getPluginManager().callEvent(new BoardRotationEvent(board, rotation, finalOfferings));

                // Phase 3 (DB executor): persist generated offerings
                database.getDatabaseExecutorService().submit(() -> {
                    try (Connection connection = database.getConnection()) {
                        BoardOfferingDAO.saveOfferings(connection, finalOfferings).forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); }
                            catch (Exception e) {
                                plugin().getLogger().log(Level.SEVERE,
                                        "[QuestBoard] Failed to persist offering statement during rotation for "
                                                + refreshTypeKey, e);
                            }
                        });
                        BoardCooldownDAO.pruneExpiredCooldowns(connection).forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); }
                            catch (Exception e) {
                                plugin().getLogger().log(Level.SEVERE,
                                        "[QuestBoard] Failed to prune cooldown during rotation for "
                                                + refreshTypeKey, e);
                            }
                        });
                        plugin().getLogger().info("[QuestBoard] Rotation complete for " + refreshTypeKey + " — generated " + finalOfferings.size() + " offerings");
                    } catch (Exception e) {
                        plugin().getLogger().log(Level.SEVERE,
                                "[QuestBoard] Offering persistence failed for " + refreshTypeKey, e);
                    }
                });
            });
        });
    }

    /**
     * Generates shared offerings for a board rotation. Uses configurable source weights
     * to choose between hand-crafted definitions and template-generated quests.
     */
    @NotNull
    public List<BoardOffering> generateSharedOfferings(@NotNull QuestBoard board,
                                                        @NotNull BoardRotation rotation,
                                                        @NotNull Random random) {
        BoardSlotCategoryRegistry categoryRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        QuestRarityRegistry rarityRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);

        int hcWeight = boardConfig.getInt(BoardConfigFile.SOURCE_WEIGHT_HAND_CRAFTED, 50);
        int tmplWeight = boardConfig.getInt(BoardConfigFile.SOURCE_WEIGHT_TEMPLATE, 50);

        List<BoardSlotCategory> categories = categoryRegistry.getByVisibility(BoardSlotCategory.Visibility.SHARED);
        categories = categories.stream()
                .filter(c -> c.getRefreshTypeKey().equals(rotation.getRefreshTypeKey()))
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .toList();

        Map<NamespacedKey, Integer> slotCounts = slotGenerationLogic.computeSlotCounts(
                categories, board.getMinimumTotalOfferings(), random, key -> false);

        String refreshType = rotation.getRefreshTypeKey().getKey().toUpperCase();
        Set<NamespacedKey> usedKeys = new HashSet<>();
        List<BoardOffering> offerings = new ArrayList<>();

        for (BoardSlotCategory category : categories) {
            int count = slotCounts.getOrDefault(category.getKey(), 0);
            for (int i = 0; i < count; i++) {
                QuestRarity rarity = rarityRegistry.rollRarity(random);

                Optional<SlotSelection> selection = questPool.selectForSlot(
                        rarity.getKey(), random, templateEngine, hcWeight, tmplWeight,
                        refreshType, usedKeys, category.getScopeProviderKey());

                selection.ifPresent(sel -> {
                    NamespacedKey defKey = switch (sel) {
                        case SlotSelection.HandCrafted hc -> hc.definitionKey();
                        case SlotSelection.TemplateGenerated tmpl -> tmpl.result().definition().getQuestKey();
                    };
                    usedKeys.add(defKey);
                    offerings.add(sel.toOffering(rotation, category, offerings.size(), null));
                });
            }
        }

        return offerings;
    }


    /**
     * Returns the cached shared offerings for a board. The cache is populated
     * during {@link #initialize} and after each rotation; this method never
     * performs database I/O on the calling thread.
     *
     * @param boardKey the board to get offerings for
     * @return the list of shared offerings (empty if the cache has not been warmed yet)
     */
    @NotNull
    public List<BoardOffering> getSharedOfferingsForBoard(@NotNull NamespacedKey boardKey) {
        Map<UUID, BoardOffering> cached = offeringCache.get(boardKey);
        if (cached != null) {
            // Only unscoped offerings belong on the shared board. Filtering here (rather than in the GUI)
            // guarantees every consumer of the shared view inherits the scope guarantee.
            return filterSharedOfferings(cached.values());
        }
        plugin().getLogger().warning("[QuestBoard] Offering cache miss for " + boardKey
                + " — cache should have been warmed during initialization or rotation");
        return List.of();
    }

    /**
     * Returns every cached offering for a board regardless of scope, including scoped (group)
     * offerings. Intended only for internal consumers that must see scope-targeted entries (e.g.
     * {@link #getScopedOfferingsForPlayer}); shared-board-facing views must use
     * {@link #getSharedOfferingsForBoard}, which strips scoped/personal offerings.
     *
     * @param boardKey the board key
     * @return an immutable list of all cached offerings, or empty if the cache is not warmed
     */
    @NotNull
    private List<BoardOffering> getAllCachedOfferings(@NotNull NamespacedKey boardKey) {
        Map<UUID, BoardOffering> cached = offeringCache.get(boardKey);
        return cached != null ? List.copyOf(cached.values()) : List.of();
    }

    /**
     * Filters a collection of offerings down to the shared (unscoped) ones — those whose
     * {@code scopeTargetId} is empty. Scoped (group) and personal offerings are excluded so
     * they never render on the shared board.
     *
     * @param offerings the offerings to filter
     * @return an immutable list containing only the unscoped offerings
     */
    @NotNull
    static List<BoardOffering> filterSharedOfferings(@NotNull Collection<BoardOffering> offerings) {
        return offerings.stream()
                .filter(offering -> offering.getScopeTargetId().isEmpty())
                .toList();
    }

    /**
     * Determines whether an offering may be accepted through the shared-board acceptance path by the
     * given player. Unscoped offerings are always acceptable; a scope-targeted offering is only
     * acceptable if its target is the clicking player (a personal offering). Scoped (group) offerings —
     * whose target is a group entity id — are rejected here and handled by the scoped-acceptance path.
     *
     * @param offering   the offering being accepted
     * @param playerUUID the UUID of the accepting player
     * @return {@code true} if acceptance through the shared-board path is permitted
     */
    static boolean canAcceptThroughSharedBoard(@NotNull BoardOffering offering, @NotNull UUID playerUUID) {
        Optional<String> scopeTargetId = offering.getScopeTargetId();
        return scopeTargetId.isEmpty() || scopeTargetId.get().equals(playerUUID.toString());
    }

    /**
     * Attempts to accept an offering for a player.
     *
     * @param player     the player
     * @param offeringId the offering UUID
     * @return an {@link OfferingAcceptResult} describing whether acceptance succeeded and,
     *         if not, the reason for failure
     */
    @NotNull
    public OfferingAcceptResult acceptOffering(@NotNull Player player, @NotNull UUID offeringId) {
        Object lock = offeringLocks.computeIfAbsent(offeringId, k -> new Object());
        synchronized (lock) {
            QuestBoard board = getDefaultBoard();
            Map<UUID, BoardOffering> offeringIndex = offeringCache.get(board.getBoardKey());

            BoardOffering offering = offeringIndex != null ? offeringIndex.get(offeringId) : null;

            if (offering == null || !offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
                return OfferingAcceptResult.NOT_AVAILABLE;
            }

            // Guard against cross-scope acceptance: this shared-board path may only accept unscoped
            // offerings (or, defensively, a personal offering belonging to the clicking player). Scoped
            // (group) offerings are accepted through acceptScopedOffering, never here.
            if (!canAcceptThroughSharedBoard(offering, player.getUniqueId())) {
                return OfferingAcceptResult.WRONG_SCOPE;
            }

            int activeCount = getActiveBoardQuestCount(player.getUniqueId());
            int maxQuests = getEffectiveMaxQuests(player, board);
            if (activeCount >= maxQuests) {
                return OfferingAcceptResult.SLOTS_FULL;
            }

            BoardOfferingAcceptEvent acceptEvent = new BoardOfferingAcceptEvent(board, player, offering);
            Bukkit.getPluginManager().callEvent(acceptEvent);
            if (acceptEvent.isCancelled()) {
                return OfferingAcceptResult.CANCELLED_BY_EVENT;
            }

            Optional<QuestDefinition> definitionOpt = resolveDefinitionForOffering(offering);
            if (definitionOpt.isEmpty()) {
                plugin().getLogger().warning("[QuestBoard] Could not resolve definition for offering "
                        + offering.getQuestDefinitionKey() + " — skipping acceptance");
                return OfferingAcceptResult.DEFINITION_NOT_FOUND;
            }
            QuestDefinition definition = definitionOpt.get();

            QuestDefinitionRegistry defRegistry = plugin().registryAccess()
                    .registry(McRPGRegistryKey.QUEST_DEFINITION);
            if (!defRegistry.registered(definition)) {
                defRegistry.register(definition);
            }

            QuestManager questManager = plugin().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.QUEST);
            QuestSourceRegistry sourceRegistry = plugin().registryAccess()
                    .registry(McRPGRegistryKey.QUEST_SOURCE);
            QuestSource questSource = sourceRegistry.get(BoardPersonalQuestSource.KEY)
                    .orElseThrow(() -> new IllegalStateException("BoardPersonalQuestSource not registered"));

            Optional<QuestInstance> instanceOpt = questManager.startQuest(
                    definition, player.getUniqueId(), questSource);
            if (instanceOpt.isEmpty()) {
                plugin().getLogger().warning("[QuestBoard] Failed to start quest " + definition.getQuestKey()
                        + " for player " + player.getName());
                return OfferingAcceptResult.QUEST_START_FAILED;
            }

            QuestInstance questInstance = instanceOpt.get();
            long acceptedAt = plugin().getTimeProvider().now().toEpochMilli();
            offering.accept(acceptedAt, questInstance.getQuestUUID());

            plugin().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.PLAYER)
                    .getPlayer(player.getUniqueId())
                    .ifPresent(p -> p.asQuestHolder().incrementBoardQuestCount());

            Database database = plugin().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.DATABASE)
                    .getDatabase();
            UUID playerUUID = player.getUniqueId();
            NamespacedKey boardKey = board.getBoardKey();
            database.getDatabaseExecutorService().submit(() -> {
                try (Connection connection = database.getConnection()) {
                    BoardOfferingDAO.updateOfferingState(connection, offering.getOfferingId(),
                            BoardOffering.State.ACCEPTED, acceptedAt, questInstance.getQuestUUID())
                            .forEach(ps -> {
                                try { ps.executeUpdate(); ps.close(); } catch (Exception e) {
                                    plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to update offering state to ACCEPTED", e);
                                }
                            });
                    PlayerBoardStateDAO.saveState(connection, playerUUID, boardKey,
                            offering.getOfferingId(), "ACCEPTED", acceptedAt, questInstance.getQuestUUID())
                            .forEach(ps -> {
                                try { ps.executeUpdate(); ps.close(); } catch (Exception e) {
                                    plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to persist player board state for accepted offering", e);
                                }
                            });
                } catch (Exception e) {
                    plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to persist offering acceptance", e);
                }
            });

            return OfferingAcceptResult.ACCEPTED;
        }
    }

    /**
     * Resolves the {@link QuestDefinition} for an offering, either from the registry
     * (for hand-crafted quests) or by deserializing the generated definition JSON
     * (for template-generated quests).
     *
     * @param offering the offering to resolve
     * @return the quest definition, or an empty Optional if it could not be resolved
     */
    @NotNull
    public Optional<QuestDefinition> resolveDefinitionForOffering(@NotNull BoardOffering offering) {
        QuestDefinitionRegistry defRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);

        if (offering.isTemplateGenerated() && offering.getGeneratedDefinition().isPresent()) {
            try {
                return Optional.of(codec.deserialize(offering.getGeneratedDefinition().get()));
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to deserialize generated definition for "
                        + offering.getQuestDefinitionKey(), e);
                return Optional.empty();
            }
        }

        return defRegistry.get(offering.getQuestDefinitionKey());
    }

    /**
     * Resolves the player-facing display name for a board offering.
     * Uses the quest definition display name when possible and falls back
     * to the raw quest definition key when resolution fails.
     *
     * @param mcRPGPlayer the player viewing the offering
     * @param offering the offering being displayed
     * @return the display name shown in board-related GUIs
     */
    @NotNull
    public String getOfferingDisplayName(@NotNull McRPGPlayer mcRPGPlayer,
                                         @NotNull BoardOffering offering) {
        return resolveDefinitionForOffering(offering)
                .map(def -> def.getDisplayName(mcRPGPlayer))
                .orElse(offering.getQuestDefinitionKey().getKey());
    }

    /**
     * Re-registers generated quest definitions from accepted offerings into the
     * {@link QuestDefinitionRegistry} so they survive server restarts.
     */
    private void reRegisterGeneratedDefinitions(@NotNull List<BoardOffering> offerings) {
        QuestDefinitionRegistry defRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);

        int registered = 0;
        for (BoardOffering offering : offerings) {
            if (!offering.isTemplateGenerated() || offering.getGeneratedDefinition().isEmpty()) {
                continue;
            }
            if (defRegistry.get(offering.getQuestDefinitionKey()).isPresent()) {
                continue;
            }
            try {
                QuestDefinition def = codec.deserialize(offering.getGeneratedDefinition().get());
                defRegistry.register(def);
                registered++;
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to re-register generated definition for "
                        + offering.getQuestDefinitionKey(), e);
            }
        }
        if (registered > 0) {
            plugin().getLogger().info("[QuestBoard] Re-registered " + registered + " generated quest definition(s) from offerings");
        }
    }

    /**
     * Computes the effective maximum board quests for a player, combining the board's
     * base limit with any permission-based bonus slots.
     *
     * @param player the player to compute for
     * @param board  the board to check against
     * @return the effective max accepted quests
     */
    public int getEffectiveMaxQuests(@NotNull Player player, @NotNull QuestBoard board) {
        int base = board.getMaxAcceptedQuests();
        Set<String> permissions = player.getEffectivePermissions().stream()
                .map(info -> info.getPermission())
                .collect(Collectors.toSet());
        int bonus = PermissionNumberParser.getHighestNumericSuffix(permissions, EXTRA_SLOTS_PERMISSION_PREFIX)
                .orElse(0);
        return base + bonus;
    }

    /**
     * Returns the number of active board quests for a player from the in-memory
     * counter on {@link us.eunoians.mcrpg.entity.holder.QuestHolder}.
     * Falls back to 0 if the player is not loaded (offline).
     *
     * @param playerUUID the player's UUID
     * @return the count of active board quests
     */
    public int getActiveBoardQuestCount(@NotNull UUID playerUUID) {
        return plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(playerUUID)
                .map(mcRPGPlayer -> mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount())
                .orElse(0);
    }

    /**
     * Generates personal offerings for a player using deterministic seeding.
     * Offerings are lazily generated on first board open per rotation period and
     * persisted to the database.
     *
     * @param playerUUID        the player's UUID
     * @param boardKey          the board key
     * @param rotation          the current rotation
     * @param completionHistory pre-loaded completion history for prerequisite evaluation, or {@code null}
     * @return the list of personal offerings
     */
    @NotNull
    public List<BoardOffering> generatePersonalOfferings(
            @NotNull UUID playerUUID,
            @NotNull NamespacedKey boardKey,
            @NotNull BoardRotation rotation,
            @Nullable QuestCompletionHistory completionHistory) {
        BoardSlotCategoryRegistry categoryRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        QuestRarityRegistry rarityRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);

        List<BoardSlotCategory> personalCategories = categoryRegistry
                .getByVisibility(BoardSlotCategory.Visibility.PERSONAL)
                .stream()
                .filter(c -> c.getRefreshTypeKey().equals(rotation.getRefreshTypeKey()))
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .toList();

        int hcWeight = boardConfig.getInt(BoardConfigFile.SOURCE_WEIGHT_HAND_CRAFTED, 50);
        int tmplWeight = boardConfig.getInt(BoardConfigFile.SOURCE_WEIGHT_TEMPLATE, 50);

        int minOfferings = 0;
        return personalOfferingGenerator.generatePersonalOfferings(
                playerUUID, rotation, personalCategories, minOfferings,
                questPool, rarityRegistry, templateEngine, hcWeight, tmplWeight, completionHistory);
    }

    /**
     * Asynchronously gets all offerings for a player: shared (cached) + personal
     * (lazily generated and persisted). Personal offerings are generated using the
     * 3-phase async pattern to keep DB I/O off the main thread.
     *
     * @param playerUUID the player's UUID
     * @param boardKey   the board key
     * @return a future that completes with the combined list of shared and personal offerings
     */
    @NotNull
    public CompletableFuture<List<BoardOffering>> getOfferingsForPlayer(@NotNull UUID playerUUID,
                                                                        @NotNull NamespacedKey boardKey) {
        List<BoardOffering> shared = new ArrayList<>(getSharedOfferingsForBoard(boardKey));

        QuestBoard board = boards.get(boardKey);
        if (board == null) {
            return CompletableFuture.completedFuture(shared);
        }

        List<CompletableFuture<List<BoardOffering>>> personalFutures = new ArrayList<>();
        board.getCurrentDailyRotation().ifPresent(rotation ->
                personalFutures.add(getOrGeneratePersonalOfferingsAsync(playerUUID, boardKey, rotation)));
        board.getCurrentWeeklyRotation().ifPresent(rotation ->
                personalFutures.add(getOrGeneratePersonalOfferingsAsync(playerUUID, boardKey, rotation)));

        if (personalFutures.isEmpty()) {
            return CompletableFuture.completedFuture(shared);
        }

        return CompletableFuture.allOf(personalFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    for (CompletableFuture<List<BoardOffering>> f : personalFutures) {
                        shared.addAll(f.join());
                    }
                    return shared;
                });
    }

    /**
     * Asynchronously returns or generates personal offerings for a player and rotation.
     * Uses the 3-phase pattern:
     * <ol>
     *     <li><b>Phase 1 (DB executor):</b> Check if already generated; if so, load and return.</li>
     *     <li><b>Phase 2 (main thread):</b> Generate offerings and fire {@link PersonalOfferingGenerateEvent}.</li>
     *     <li><b>Phase 3 (DB executor):</b> Persist offerings and mark as generated.</li>
     * </ol>
     */
    @NotNull
    private CompletableFuture<List<BoardOffering>> getOrGeneratePersonalOfferingsAsync(
            @NotNull UUID playerUUID,
            @NotNull NamespacedKey boardKey,
            @NotNull BoardRotation rotation) {

        Database database = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();
        CompletableFuture<List<BoardOffering>> future = new CompletableFuture<>();

        // Phase 1: Check DB if already generated
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                if (PersonalOfferingTrackingDAO
                        .hasGenerated(connection, playerUUID, boardKey, rotation.getRotationId())) {
                    List<BoardOffering> existing = BoardOfferingDAO.loadPersonalOfferingsForRotation(
                            connection, rotation.getRotationId(), playerUUID);
                    future.complete(existing);
                    return;
                }
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "[QuestBoard] Phase 1 failed for personal offerings "
                        + playerUUID, e);
                future.complete(List.of());
                return;
            }

            // Pre-load completion count on DB thread for prerequisite evaluation
            int completionCount = 0;
            try (Connection histConn = database.getConnection()) {
                completionCount = QuestCompletionLogDAO.getCompletionHistory(histConn, playerUUID, true).size();
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING,
                        "[QuestBoard] Failed to load completion history for " + playerUUID
                        + "; prerequisite conditions will evaluate as zero completions", e);
            }
            final int preloadedCount = completionCount;

            // Phase 2: Generate on main thread (fires event synchronously)
            Bukkit.getScheduler().runTask(plugin(), () -> {
                QuestCompletionHistory history = (uuid, cat, rar) -> preloadedCount;
                List<BoardOffering> personal = generatePersonalOfferings(playerUUID, boardKey, rotation, history);

                QuestBoard board = boards.get(boardKey);
                if (board != null) {
                    PersonalOfferingGenerateEvent event = new PersonalOfferingGenerateEvent(
                            board, playerUUID, rotation, personal);
                    Bukkit.getPluginManager().callEvent(event);
                    personal = event.getOfferings();
                }

                List<BoardOffering> finalPersonal = personal;

                // Phase 3: Persist on DB executor
                database.getDatabaseExecutorService().submit(() -> {
                    try (Connection connection = database.getConnection()) {
                        BoardOfferingDAO.saveOfferings(connection, finalPersonal).forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); } catch (Exception e) {
                                plugin().getLogger().log(Level.WARNING, "[QuestBoard] Failed to persist personal offering statement", e);
                            }
                        });
                        PersonalOfferingTrackingDAO
                                .markGenerated(connection, playerUUID, boardKey, rotation.getRotationId());
                    } catch (Exception e) {
                        plugin().getLogger().log(Level.WARNING, "[QuestBoard] Phase 3 failed for personal offerings "
                                + playerUUID, e);
                    }
                    future.complete(finalPersonal);
                });
            });
        });

        return future;
    }

    /**
     * Returns the effective minimum personal offerings for a player, combining the base
     * with permission-based bonuses.
     *
     * @param player the player
     * @param board  the board
     * @return the effective minimum personal offerings
     */
    public int getEffectiveMinimumOfferings(@NotNull Player player, @NotNull QuestBoard board) {
        int base = 0;
        Set<String> permissions = player.getEffectivePermissions().stream()
                .map(info -> info.getPermission())
                .collect(Collectors.toSet());
        return base + PermissionNumberParser.getHighestNumericSuffix(
                permissions, EXTRA_OFFERINGS_PERMISSION_PREFIX).orElse(0);
    }

    /**
     * Returns the quest pool used for generating board offerings.
     *
     * @return the quest pool
     */
    @NotNull
    public QuestPool getQuestPool() {
        return questPool;
    }

    /**
     * Returns the reloadable rarity configuration.
     *
     * @return the rarity config
     */
    @NotNull
    public ReloadableRarityConfig getRarityConfig() {
        return rarityConfig;
    }

    /**
     * Returns the reloadable category configuration.
     *
     * @return the category config
     */
    @NotNull
    public ReloadableCategoryConfig getCategoryConfig() {
        return categoryConfig;
    }

    /**
     * Returns the reloadable template configuration.
     *
     * @return the template config
     */
    @NotNull
    public ReloadableTemplateConfig getTemplateConfig() {
        return templateConfig;
    }

    /**
     * Returns the template engine used for generating quests from templates.
     *
     * @return the template engine
     */
    @NotNull
    public QuestTemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
     * Generates scoped offerings for all active scope entities during rotation.
     * Iterates every registered {@link ScopedBoardAdapter}, resolves active entities,
     * and rolls offerings for each SCOPED category matching the refresh type.
     *
     * @param board    the board to generate for
     * @param rotation the current rotation
     * @param random   the random source
     * @return the generated scoped offerings
     */
    @NotNull
    public List<BoardOffering> generateScopedOfferings(@NotNull QuestBoard board,
                                                        @NotNull BoardRotation rotation,
                                                        @NotNull Random random) {
        BoardSlotCategoryRegistry categoryRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        QuestRarityRegistry rarityRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);
        ScopedBoardAdapterRegistry adapterRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);

        List<BoardSlotCategory> scopedCategories = categoryRegistry
                .getByVisibility(BoardSlotCategory.Visibility.SCOPED).stream()
                .filter(c -> c.getRefreshTypeKey().equals(rotation.getRefreshTypeKey()))
                .toList();

        if (scopedCategories.isEmpty()) return List.of();

        int hcWeight = boardConfig.getInt(BoardConfigFile.SOURCE_WEIGHT_HAND_CRAFTED, 50);
        int tmplWeight = boardConfig.getInt(BoardConfigFile.SOURCE_WEIGHT_TEMPLATE, 50);

        List<BoardOffering> offerings = new ArrayList<>();
        for (BoardSlotCategory category : scopedCategories) {
            Optional<ScopedBoardAdapter> adapter = adapterRegistry.get(category.getScopeProviderKey());
            if (adapter.isEmpty()) continue;

            Set<String> activeEntities = adapter.get().getAllActiveEntities();
            for (String entityId : activeEntities) {
                Map<NamespacedKey, Integer> slotCounts = slotGenerationLogic.computeSlotCounts(
                        List.of(category), 0, random, key -> false);

                int count = slotCounts.getOrDefault(category.getKey(), 0);
                for (int i = 0; i < count; i++) {
                    QuestRarity rarity = rarityRegistry.rollRarity(random);
                    Optional<SlotSelection> selection = questPool.selectForSlot(
                            rarity.getKey(), random, templateEngine, hcWeight, tmplWeight);
                    selection.ifPresent(sel -> offerings.add(
                            sel.toOffering(rotation, category, offerings.size(), entityId)));
                }
            }
        }
        return offerings;
    }


    /**
     * Attempts to accept a scoped offering for a player. Performs synchronous
     * pre-flight checks (adapter lookup, permission, offering state), then
     * runs the active-count query on the database executor thread.
     *
     * @param player           the accepting player
     * @param offeringId       the offering UUID
     * @param entityId         the scope entity identifier
     * @param scopeProviderKey the scope provider key
     * @return future resolving to {@code true} if the offering was accepted
     */
    @NotNull
    public CompletableFuture<Boolean> acceptScopedOffering(@NotNull Player player,
                                                            @NotNull UUID offeringId,
                                                            @NotNull String entityId,
                                                            @NotNull NamespacedKey scopeProviderKey) {
        ScopedBoardAdapterRegistry adapterRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);
        Optional<ScopedBoardAdapter> optAdapter = adapterRegistry.get(scopeProviderKey);
        if (optAdapter.isEmpty()) return CompletableFuture.completedFuture(false);

        ScopedBoardAdapter adapter = optAdapter.get();
        if (!adapter.canManageQuests(player.getUniqueId(), entityId)) {
            return CompletableFuture.completedFuture(false);
        }

        QuestBoard board = getDefaultBoard();
        Map<UUID, BoardOffering> offeringIndex = offeringCache.get(board.getBoardKey());
        BoardOffering offering = offeringIndex != null ? offeringIndex.get(offeringId) : null;

        if (offering == null
                || !offering.getScopeTargetId().map(entityId::equals).orElse(false)
                || !offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
            return CompletableFuture.completedFuture(false);
        }

        BoardSlotCategoryRegistry categoryRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        Optional<BoardSlotCategory> optCategory = categoryRegistry.get(offering.getCategoryKey());

        int effectiveLimit = optCategory
                .map(cat -> cat.getMaxActivePerEntity().orElse(board.getMaxScopedQuestsPerEntity()))
                .orElse(board.getMaxScopedQuestsPerEntity());

        NamespacedKey boardKey = board.getBoardKey();
        Database database = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        database.getDatabaseExecutorService().submit(() -> {
            int activeCount;
            try (Connection connection = database.getConnection()) {
                activeCount = ScopedBoardStateDAO.countActiveQuestsForEntity(connection, entityId, boardKey);
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING,
                        "[QuestBoard] Failed to query active scoped quest count for entity '" + entityId + "'", e);
                future.complete(false);
                return;
            }
            if (activeCount >= effectiveLimit) {
                future.complete(false);
                return;
            }
            // Phase 2: hop to main thread so that the state-check + accept happen on the same
            // thread that holds the offering lock — mirrors the offeringLocks pattern in acceptOffering().
            Bukkit.getScheduler().runTask(plugin(), () -> {
                Object lock = offeringLocks.computeIfAbsent(offeringId, k -> new Object());
                synchronized (lock) {
                    if (!offering.canTransitionTo(BoardOffering.State.ACCEPTED)) {
                        future.complete(false);
                        return;
                    }
                    offering.accept(plugin().getTimeProvider().now().toEpochMilli(), UUID.randomUUID());
                    future.complete(true);
                }
            });
        });
        return future;
    }

    /**
     * Abandons a scoped quest. Validates management permission via the adapter.
     *
     * @param player            the abandoning player
     * @param questInstanceUUID the quest instance UUID
     * @param entityId          the scope entity identifier
     * @param scopeProviderKey  the scope provider key
     * @return {@code true} if the quest was successfully abandoned
     */
    public boolean abandonScopedQuest(@NotNull Player player,
                                      @NotNull UUID questInstanceUUID,
                                      @NotNull String entityId,
                                      @NotNull NamespacedKey scopeProviderKey) {
        ScopedBoardAdapterRegistry adapterRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);
        Optional<ScopedBoardAdapter> optAdapter = adapterRegistry.get(scopeProviderKey);
        if (optAdapter.isEmpty()) return false;

        if (!optAdapter.get().canManageQuests(player.getUniqueId(), entityId)) return false;

        QuestManager questManager = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        return questManager.abandonQuest(questInstanceUUID);
    }

    /**
     * Gets scoped offerings for all entities the player is a member of,
     * across all registered scope adapters.
     *
     * @param playerUUID the player UUID
     * @return map of entity ID to list of offerings for that entity
     */
    @NotNull
    public Map<String, List<BoardOffering>> getScopedOfferingsForPlayer(@NotNull UUID playerUUID) {
        ScopedBoardAdapterRegistry adapterRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.SCOPED_BOARD_ADAPTER);
        QuestBoard board = getDefaultBoard();
        // Scoped offerings are stripped from the shared view, so read the full cache here and filter by
        // the caller's entity scope below.
        List<BoardOffering> allOfferings = getAllCachedOfferings(board.getBoardKey());

        Map<String, List<BoardOffering>> result = new HashMap<>();

        for (ScopedBoardAdapter adapter : adapterRegistry.getAll()) {
            Set<String> memberEntities = adapter.getMemberEntities(playerUUID);
            for (String entityId : memberEntities) {
                List<BoardOffering> entityOfferings = allOfferings.stream()
                        .filter(o -> o.getScopeTargetId().map(entityId::equals).orElse(false))
                        .filter(o -> o.getState() == BoardOffering.State.VISIBLE)
                        .toList();
                if (!entityOfferings.isEmpty()) {
                    result.put(entityId, entityOfferings);
                }
            }
        }
        return result;
    }

    /**
     * Handles scope entity removal: cancels active quests, expires offerings,
     * cleans up state records. Called by scope-specific event listeners
     * (e.g., {@link us.eunoians.mcrpg.listener.lands.LandDeleteListener}).
     *
     * @param scopeProviderKey the scope provider key
     * @param entityId         the entity being removed
     */
    public void handleScopeEntityRemoval(@NotNull NamespacedKey scopeProviderKey, @NotNull String entityId) {
        Database database = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();

        database.getDatabaseExecutorService().submit(() -> {
            List<ScopedBoardStateDAO.ScopedBoardStateRecord> accepted;
            try (Connection connection = database.getConnection()) {
                accepted = ScopedBoardStateDAO.loadAcceptedStatesForEntity(connection, entityId);

                ScopedBoardStateDAO.deleteStatesForEntity(connection, entityId)
                        .forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); } catch (Exception e) {
                                plugin().getLogger().log(Level.WARNING,
                                        "[QuestBoard] Failed to delete scoped board state record for entity '"
                                                + entityId + "'", e);
                            }
                        });
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING,
                        "[QuestBoard] Failed to clean up entity removal for '" + entityId + "'", e);
                return;
            }

            int cleanedUp = accepted.size();
            // abandonQuest() calls quest.cancel() which fires a synchronous Bukkit event —
            // always schedule this loop on the main thread to prevent async Bukkit API calls.
            Bukkit.getScheduler().runTask(plugin(), () -> {
                QuestManager questManager = plugin().registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.QUEST);

                for (ScopedBoardStateDAO.ScopedBoardStateRecord record : accepted) {
                    if (record.questInstanceUUID() != null) {
                        questManager.abandonQuest(record.questInstanceUUID());
                    }
                }

                plugin().getLogger().info("[QuestBoard] Cleaned up " + cleanedUp + " scoped quests for removed entity '"
                        + entityId + "' (scope: " + scopeProviderKey + ")");
            });
        });
    }

    /**
     * Validates offering states loaded from cache/database and repairs orphaned states.
     * An offering marked ACCEPTED but whose quest instance no longer exists is transitioned
     * to EXPIRED as a consistency repair.
     *
     * @param connection the database connection for state updates
     * @param offerings  the offerings to validate
     */
    private void validateOfferingStates(@NotNull Connection connection,
                                        @NotNull List<BoardOffering> offerings) {
        QuestManager questManager = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);

        for (BoardOffering offering : offerings) {
            if (offering.getState() == BoardOffering.State.ACCEPTED
                    && offering.getQuestInstanceUUID().isPresent()) {
                UUID instanceUUID = offering.getQuestInstanceUUID().get();
                if (!questManager.isQuestActive(instanceUUID)) {
                    plugin().getLogger().warning("[QuestBoard] Offering " + offering.getOfferingId()
                            + " is ACCEPTED but quest instance " + instanceUUID
                            + " not found in active quests. Transitioning to EXPIRED.");
                    offering.transitionTo(BoardOffering.State.EXPIRED);
                    BoardOfferingDAO.updateOfferingState(connection, offering.getOfferingId(),
                            BoardOffering.State.EXPIRED, null, null).forEach(ps -> {
                        try {
                            ps.executeUpdate();
                            ps.close();
                        } catch (Exception e) {
                            plugin().getLogger().log(Level.SEVERE,
                                    "[QuestBoard] Failed to persist EXPIRED state for orphaned offering "
                                            + offering.getOfferingId(), e);
                        }
                    });
                }
            }
        }
    }

    @NotNull
    private ZoneId getConfiguredRotationZone() {
        String configuredTimezone = boardConfig.getString(BoardConfigFile.ROTATION_TIMEZONE, DEFAULT_ROTATION_TIMEZONE);
        try {
            return ZoneId.of(configuredTimezone);
        } catch (Exception exception) {
            plugin().getLogger().warning("[QuestBoard] Invalid rotation timezone '" + configuredTimezone
                    + "' configured in board.yml. Falling back to " + DEFAULT_ROTATION_TIMEZONE + ".");
            return ZoneId.of(DEFAULT_ROTATION_TIMEZONE);
        }
    }

    /**
     * Converts a list of {@link BoardOffering} into a {@link Map} keyed by offering UUID,
     * suitable for O(1) lookup in {@link #acceptOffering}.
     *
     * @param offerings the offerings to index
     * @return an unmodifiable map from offering UUID to offering
     */
    @NotNull
    private static Map<UUID, BoardOffering> toOfferingMap(@NotNull List<BoardOffering> offerings) {
        Map<UUID, BoardOffering> map = new HashMap<>();
        for (BoardOffering offering : offerings) {
            map.put(offering.getOfferingId(), offering);
        }
        return Map.copyOf(map);
    }
}
