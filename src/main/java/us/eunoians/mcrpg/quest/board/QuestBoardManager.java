package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent;
import us.eunoians.mcrpg.event.board.BoardOfferingExpireEvent;
import us.eunoians.mcrpg.event.board.BoardOfferingGenerateEvent;
import us.eunoians.mcrpg.event.board.BoardRotationEvent;
import us.eunoians.mcrpg.event.board.PersonalOfferingGenerateEvent;
import us.eunoians.mcrpg.database.table.board.BoardCooldownDAO;
import us.eunoians.mcrpg.database.table.board.BoardOfferingDAO;
import us.eunoians.mcrpg.database.table.board.BoardRotationDAO;
import us.eunoians.mcrpg.database.table.board.PersonalOfferingTrackingDAO;
import us.eunoians.mcrpg.database.table.board.PlayerBoardStateDAO;
import us.eunoians.mcrpg.database.table.board.ScopedBoardStateDAO;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapter;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
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
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestDefinitionSerializer;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Central manager for the quest board system.
 * <p>
 * Owns the board entities, the offering cache, and orchestrates rotation, generation,
 * acceptance, and abandonment. Registered in {@link McRPGManagerKey#QUEST_BOARD}.
 */
public class QuestBoardManager extends Manager<McRPG> {

    private static final Logger LOGGER = McRPG.getInstance().getLogger();
    private static final String EXTRA_SLOTS_PERMISSION_PREFIX = "mcrpg.board.extra-slots.";
    private static final String EXTRA_OFFERINGS_PERMISSION_PREFIX = "mcrpg.extra-offerings.";
    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");

    private final Map<NamespacedKey, QuestBoard> boards = new HashMap<>();
    private final Map<NamespacedKey, List<BoardOffering>> offeringCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Object> offeringLocks = new ConcurrentHashMap<>();
    private YamlDocument boardConfig;
    private QuestPool questPool;
    private QuestTemplateEngine templateEngine;
    private ReloadableRarityConfig rarityConfig;
    private ReloadableCategoryConfig categoryConfig;
    private ReloadableTemplateConfig templateConfig;

    public QuestBoardManager(@NotNull McRPG plugin) {
        super(plugin);
    }

    private static final String[] DEFAULT_CATEGORY_RESOURCES = {
            "quest-board/categories/personal-daily.yml",
            "quest-board/categories/personal-weekly.yml"
    };

    private static final String[] DEFAULT_TEMPLATE_RESOURCES = {
            "quest-board/templates/daily_mining_templates.yml",
            "quest-board/templates/daily_combat_templates.yml",
            "quest-board/templates/mixed_templates.yml"
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

        this.templateEngine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry);
        File primaryTemplatesDir = new File(plugin.getDataFolder(), "quest-board/templates");
        this.templateConfig = new ReloadableTemplateConfig(boardConfig, templateRegistry, primaryTemplatesDir);
        this.templateConfig.getContent();

        this.questPool = new QuestPool(definitionRegistry);

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
                    LOGGER.info("[QuestBoard] No daily rotation found. Triggering initial rotation.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(DailyRefreshType.KEY));
                } else if (now > dailyRotation.get().getExpiresAt()) {
                    LOGGER.info("[QuestBoard] Detected missed daily rotation (expired: "
                            + dailyRotation.get().getExpiresAt() + ", now: " + now + "). Triggering catch-up.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(DailyRefreshType.KEY));
                }

                Optional<BoardRotation> weeklyRotation =
                        BoardRotationDAO.loadCurrentRotation(connection, DEFAULT_BOARD_KEY, WeeklyRefreshType.KEY);
                weeklyRotation.ifPresent(defaultBoard::setCurrentWeeklyRotation);
                if (weeklyRotation.isEmpty()) {
                    LOGGER.info("[QuestBoard] No weekly rotation found. Triggering initial rotation.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(WeeklyRefreshType.KEY));
                } else if (now > weeklyRotation.get().getExpiresAt()) {
                    LOGGER.info("[QuestBoard] Detected missed weekly rotation (expired: "
                            + weeklyRotation.get().getExpiresAt() + ", now: " + now + "). Triggering catch-up.");
                    Bukkit.getScheduler().runTask(plugin, () -> triggerRotation(WeeklyRefreshType.KEY));
                }
            } catch (Exception e) {
                LOGGER.warning("[QuestBoard] Failed to load current rotations: " + e.getMessage());
            }
        });

        LOGGER.info("[QuestBoard] Initialized with " + rarityRegistry.getAll().size() + " rarities, "
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
        File templatesDir = new File(plugin.getDataFolder(), "quest-board/templates");
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
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

        ZonedDateTime zonedNow = plugin().getTimeProvider().now().atZone(java.time.ZoneId.systemDefault());
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
                    try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                });

                previousRotation = refreshTypeKey.equals(DailyRefreshType.KEY)
                        ? board.getCurrentDailyRotation()
                        : board.getCurrentWeeklyRotation();
                previousRotation.ifPresent(prev ->
                    BoardOfferingDAO.expireOfferingsForRotation(connection, prev.getRotationId())
                            .forEach(ps -> {
                                try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                            }));
            } catch (Exception e) {
                LOGGER.severe("[QuestBoard] Rotation failed for " + refreshTypeKey + ": " + e.getMessage());
                e.printStackTrace();
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
                    List<BoardOffering> existing = offeringCache.getOrDefault(board.getBoardKey(), List.of());
                    existing.stream()
                            .filter(o -> o.getRotationId().equals(otherId))
                            .forEach(merged::add);
                });
                offeringCache.put(board.getBoardKey(), List.copyOf(merged));
                LOGGER.info("[QuestBoard] Cache updated for " + board.getBoardKey() + " — "
                        + finalOfferings.size() + " new + " + (merged.size() - finalOfferings.size())
                        + " kept from other rotation = " + merged.size() + " total cached");
                offeringLocks.clear();

                Bukkit.getPluginManager().callEvent(new BoardRotationEvent(board, rotation, finalOfferings));

                // Phase 3 (DB executor): persist generated offerings
                database.getDatabaseExecutorService().submit(() -> {
                    try (Connection connection = database.getConnection()) {
                        BoardOfferingDAO.saveOfferings(connection, finalOfferings).forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                        });
                        BoardCooldownDAO.pruneExpiredCooldowns(connection).forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                        });
                        LOGGER.info("[QuestBoard] Rotation complete for " + refreshTypeKey + " — generated " + finalOfferings.size() + " offerings");
                    } catch (Exception e) {
                        LOGGER.severe("[QuestBoard] Offering persistence failed for " + refreshTypeKey + ": " + e.getMessage());
                        e.printStackTrace();
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

        Map<NamespacedKey, Integer> slotCounts = SlotGenerationLogic.computeSlotCounts(
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
                        refreshType, usedKeys);

                selection.ifPresent(sel -> {
                    NamespacedKey defKey = switch (sel) {
                        case SlotSelection.HandCrafted hc -> hc.definitionKey();
                        case SlotSelection.TemplateGenerated tmpl -> tmpl.result().definition().getQuestKey();
                    };
                    usedKeys.add(defKey);
                    offerings.add(toOffering(sel, rotation, category, offerings.size()));
                });
            }
        }

        return offerings;
    }

    /**
     * Converts a {@link SlotSelection} into a {@link BoardOffering}.
     */
    @NotNull
    private BoardOffering toOffering(@NotNull SlotSelection selection,
                                     @NotNull BoardRotation rotation,
                                     @NotNull BoardSlotCategory category,
                                     int slotIndex) {
        return switch (selection) {
            case SlotSelection.HandCrafted hc -> new BoardOffering(
                    UUID.randomUUID(),
                    rotation.getRotationId(),
                    category.getKey(),
                    slotIndex,
                    hc.definitionKey(),
                    hc.rarityKey(),
                    null,
                    category.getCompletionTime()
            );
            case SlotSelection.TemplateGenerated tmpl -> new BoardOffering(
                    UUID.randomUUID(),
                    rotation.getRotationId(),
                    category.getKey(),
                    slotIndex,
                    tmpl.result().definition().getQuestKey(),
                    tmpl.rarityKey(),
                    null,
                    category.getCompletionTime(),
                    tmpl.result().templateKey(),
                    tmpl.result().serializedDefinition()
            );
        };
    }

    /**
     * Returns the cached shared offerings for a board, loading from the database
     * on first access.
     *
     * @param boardKey the board to get offerings for
     * @return the list of shared offerings
     */
    @NotNull
    public List<BoardOffering> getSharedOfferingsForBoard(@NotNull NamespacedKey boardKey) {
        boolean cacheHit = offeringCache.containsKey(boardKey);
        List<BoardOffering> result = offeringCache.computeIfAbsent(boardKey, key -> {
            QuestBoard board = boards.get(key);
            if (board == null) {
                LOGGER.warning("[QuestBoard] getSharedOfferings: board not found for key " + key);
                return List.of();
            }

            List<BoardOffering> all = new ArrayList<>();
            Database database = plugin().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.DATABASE)
                    .getDatabase();
            try (Connection connection = database.getConnection()) {
                board.getCurrentDailyRotation().ifPresent(daily ->
                        all.addAll(BoardOfferingDAO.loadOfferingsForRotation(connection, daily.getRotationId())));
                board.getCurrentWeeklyRotation().ifPresent(weekly ->
                        all.addAll(BoardOfferingDAO.loadOfferingsForRotation(connection, weekly.getRotationId())));
                validateOfferingStates(connection, all);
            } catch (Exception e) {
                LOGGER.warning("[QuestBoard] Failed to load offerings for board " + key + ": " + e.getMessage());
            }
            LOGGER.info("[QuestBoard] getSharedOfferings: loaded " + all.size() + " from DB for " + key);
            return all;
        });
        LOGGER.info("[QuestBoard] getSharedOfferings(" + boardKey + "): cacheHit=" + cacheHit
                + ", returning " + result.size() + " offerings");
        return result;
    }

    /**
     * Attempts to accept an offering for a player.
     *
     * @param player     the player
     * @param offeringId the offering UUID
     * @return {@code true} if the offering was accepted
     */
    public boolean acceptOffering(@NotNull Player player, @NotNull UUID offeringId) {
        Object lock = offeringLocks.computeIfAbsent(offeringId, k -> new Object());
        synchronized (lock) {
            QuestBoard board = getDefaultBoard();
            List<BoardOffering> offerings = getSharedOfferingsForBoard(board.getBoardKey());

            Optional<BoardOffering> optOffering = offerings.stream()
                    .filter(o -> o.getOfferingId().equals(offeringId))
                    .findFirst();

            if (optOffering.isEmpty() || !optOffering.get().canTransitionTo(BoardOffering.State.ACCEPTED)) {
                return false;
            }

            int activeCount = getActiveBoardQuestCount(player.getUniqueId());
            int maxQuests = getEffectiveMaxQuests(player, board);
            if (activeCount >= maxQuests) {
                return false;
            }

            BoardOffering offering = optOffering.get();

            BoardOfferingAcceptEvent acceptEvent = new BoardOfferingAcceptEvent(board, player, offering);
            Bukkit.getPluginManager().callEvent(acceptEvent);
            if (acceptEvent.isCancelled()) {
                return false;
            }

            QuestDefinition definition = resolveDefinitionForOffering(offering);
            if (definition == null) {
                LOGGER.warning("[QuestBoard] Could not resolve definition for offering "
                        + offering.getQuestDefinitionKey() + " — skipping acceptance");
                return false;
            }

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
                LOGGER.warning("[QuestBoard] Failed to start quest " + definition.getQuestKey()
                        + " for player " + player.getName());
                return false;
            }

            QuestInstance questInstance = instanceOpt.get();
            long acceptedAt = plugin().getTimeProvider().now().toEpochMilli();
            offering.accept(acceptedAt, questInstance.getQuestUUID());

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
                                try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                            });
                    PlayerBoardStateDAO.saveState(connection, playerUUID, boardKey,
                            offering.getOfferingId(), "ACCEPTED", acceptedAt, questInstance.getQuestUUID())
                            .forEach(ps -> {
                                try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                            });
                } catch (Exception e) {
                    LOGGER.warning("[QuestBoard] Failed to persist offering acceptance: " + e.getMessage());
                }
            });

            return true;
        }
    }

    /**
     * Resolves the {@link QuestDefinition} for an offering, either from the registry
     * (for hand-crafted quests) or by deserializing the generated definition JSON
     * (for template-generated quests).
     *
     * @param offering the offering to resolve
     * @return the quest definition, or {@code null} if it could not be resolved
     */
    @org.jetbrains.annotations.Nullable
    public QuestDefinition resolveDefinitionForOffering(@NotNull BoardOffering offering) {
        QuestDefinitionRegistry defRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);

        if (offering.isTemplateGenerated() && offering.getGeneratedDefinition().isPresent()) {
            try {
                QuestObjectiveTypeRegistry objTypeRegistry = plugin().registryAccess()
                        .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
                QuestRewardTypeRegistry rewardTypeRegistry = plugin().registryAccess()
                        .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
                return GeneratedQuestDefinitionSerializer.deserialize(
                        offering.getGeneratedDefinition().get(), objTypeRegistry, rewardTypeRegistry);
            } catch (Exception e) {
                LOGGER.warning("[QuestBoard] Failed to deserialize generated definition for "
                        + offering.getQuestDefinitionKey() + ": " + e.getMessage());
                return null;
            }
        }

        return defRegistry.get(offering.getQuestDefinitionKey()).orElse(null);
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
     * Counts the number of active board quests for a player from the database.
     *
     * @param playerUUID the player's UUID
     * @return the count of active board quests
     */
    public int getActiveBoardQuestCount(@NotNull UUID playerUUID) {
        Database database = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();
        try (Connection connection = database.getConnection()) {
            return PlayerBoardStateDAO.countActiveQuestsFromBoard(connection, playerUUID, DEFAULT_BOARD_KEY);
        } catch (Exception e) {
            LOGGER.warning("[QuestBoard] Failed to count active quests: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generates personal offerings for a player using deterministic seeding.
     * Offerings are lazily generated on first board open per rotation period and
     * persisted to the database.
     *
     * @param playerUUID the player's UUID
     * @param boardKey   the board key
     * @param rotation   the current rotation
     * @return the list of personal offerings
     */
    @NotNull
    public List<BoardOffering> generatePersonalOfferings(
            @NotNull UUID playerUUID,
            @NotNull NamespacedKey boardKey,
            @NotNull BoardRotation rotation) {
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
        return PersonalOfferingGenerator.generatePersonalOfferings(
                playerUUID, rotation, personalCategories, minOfferings,
                questPool, rarityRegistry, templateEngine, hcWeight, tmplWeight);
    }

    /**
     * Gets all offerings for a player: shared (cached) + personal (lazily generated
     * and persisted).
     *
     * @param playerUUID the player's UUID
     * @param boardKey   the board key
     * @return combined list of shared and personal offerings
     */
    @NotNull
    public List<BoardOffering> getOfferingsForPlayer(@NotNull UUID playerUUID,
                                                      @NotNull NamespacedKey boardKey) {
        List<BoardOffering> all = new ArrayList<>(getSharedOfferingsForBoard(boardKey));

        QuestBoard board = boards.get(boardKey);
        if (board == null) return all;

        board.getCurrentDailyRotation().ifPresent(rotation ->
                all.addAll(getOrGeneratePersonalOfferings(playerUUID, boardKey, rotation)));
        board.getCurrentWeeklyRotation().ifPresent(rotation ->
                all.addAll(getOrGeneratePersonalOfferings(playerUUID, boardKey, rotation)));

        return all;
    }

    /**
     * Returns or generates personal offerings for a player and rotation. On first
     * access the offerings are generated, persisted, and cached. Subsequent calls
     * load from the database.
     */
    @NotNull
    private List<BoardOffering> getOrGeneratePersonalOfferings(@NotNull UUID playerUUID,
                                                                @NotNull NamespacedKey boardKey,
                                                                @NotNull BoardRotation rotation) {
        Database database = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();
        try (Connection connection = database.getConnection()) {
            if (PersonalOfferingTrackingDAO
                    .hasGenerated(connection, playerUUID, boardKey, rotation.getRotationId())) {
                return BoardOfferingDAO.loadPersonalOfferingsForRotation(
                        connection, rotation.getRotationId(), playerUUID);
            }

            List<BoardOffering> personal = generatePersonalOfferings(playerUUID, boardKey, rotation);

            QuestBoard board = boards.get(boardKey);
            if (board != null) {
                PersonalOfferingGenerateEvent event = new PersonalOfferingGenerateEvent(
                        board, playerUUID, rotation, personal);
                Bukkit.getPluginManager().callEvent(event);
                personal = event.getOfferings();
            }

            BoardOfferingDAO.saveOfferings(connection, personal).forEach(ps -> {
                try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
            });
            PersonalOfferingTrackingDAO
                    .markGenerated(connection, playerUUID, boardKey, rotation.getRotationId());
            return personal;
        } catch (Exception e) {
            LOGGER.warning("[QuestBoard] Failed to get personal offerings for " + playerUUID + ": " + e.getMessage());
            return List.of();
        }
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
                Map<NamespacedKey, Integer> slotCounts = SlotGenerationLogic.computeSlotCounts(
                        List.of(category), 0, random, key -> false);

                int count = slotCounts.getOrDefault(category.getKey(), 0);
                for (int i = 0; i < count; i++) {
                    QuestRarity rarity = rarityRegistry.rollRarity(random);
                    Optional<SlotSelection> selection = questPool.selectForSlot(
                            rarity.getKey(), random, templateEngine, hcWeight, tmplWeight);
                    selection.ifPresent(sel -> offerings.add(
                            toScopedOffering(sel, rotation, category, offerings.size(), entityId)));
                }
            }
        }
        return offerings;
    }

    /**
     * Converts a {@link SlotSelection} into a scoped {@link BoardOffering} with the
     * scope entity identifier set. Mirrors the shared {@code toOffering()} helper but
     * populates {@link BoardOffering#getScopeTargetId()}.
     *
     * @param selection the slot selection (hand-crafted or template-generated)
     * @param rotation  the current board rotation
     * @param category  the scoped category the offering belongs to
     * @param slotIndex the positional index within the offering list
     * @param entityId  the scope entity identifier (e.g., land name)
     * @return the constructed scoped board offering
     */
    @NotNull
    private BoardOffering toScopedOffering(@NotNull SlotSelection selection,
                                            @NotNull BoardRotation rotation,
                                            @NotNull BoardSlotCategory category,
                                            int slotIndex,
                                            @NotNull String entityId) {
        return switch (selection) {
            case SlotSelection.HandCrafted hc -> new BoardOffering(
                    UUID.randomUUID(),
                    rotation.getRotationId(),
                    category.getKey(),
                    slotIndex,
                    hc.definitionKey(),
                    hc.rarityKey(),
                    entityId,
                    category.getCompletionTime()
            );
            case SlotSelection.TemplateGenerated tmpl -> new BoardOffering(
                    UUID.randomUUID(),
                    rotation.getRotationId(),
                    category.getKey(),
                    slotIndex,
                    tmpl.result().definition().getQuestKey(),
                    tmpl.rarityKey(),
                    entityId,
                    category.getCompletionTime(),
                    tmpl.result().templateKey(),
                    tmpl.result().serializedDefinition()
            );
        };
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
        List<BoardOffering> offerings = getSharedOfferingsForBoard(board.getBoardKey());

        Optional<BoardOffering> optOffering = offerings.stream()
                .filter(o -> o.getOfferingId().equals(offeringId))
                .filter(o -> o.getScopeTargetId().map(entityId::equals).orElse(false))
                .findFirst();

        if (optOffering.isEmpty() || !optOffering.get().canTransitionTo(BoardOffering.State.ACCEPTED)) {
            return CompletableFuture.completedFuture(false);
        }

        BoardSlotCategoryRegistry categoryRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        BoardOffering offering = optOffering.get();
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
            try (Connection connection = database.getConnection()) {
                int activeCount = ScopedBoardStateDAO.countActiveQuestsForEntity(connection, entityId, boardKey);
                if (activeCount >= effectiveLimit) {
                    future.complete(false);
                    return;
                }
                offering.accept(plugin().getTimeProvider().now().toEpochMilli(), UUID.randomUUID());
                future.complete(true);
            } catch (Exception e) {
                LOGGER.warning("[QuestBoard] Failed to accept scoped offering for entity "
                        + entityId + ": " + e.getMessage());
                future.complete(false);
            }
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
        List<BoardOffering> allOfferings = getSharedOfferingsForBoard(board.getBoardKey());

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
            try (Connection connection = database.getConnection()) {
                List<ScopedBoardStateDAO.ScopedBoardStateRecord> accepted =
                        ScopedBoardStateDAO.loadAcceptedStatesForEntity(connection, entityId);

                QuestManager questManager = plugin().registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.QUEST);

                for (ScopedBoardStateDAO.ScopedBoardStateRecord record : accepted) {
                    if (record.questInstanceUUID() != null) {
                        questManager.abandonQuest(record.questInstanceUUID());
                    }
                }

                ScopedBoardStateDAO.deleteStatesForEntity(connection, entityId)
                        .forEach(ps -> {
                            try { ps.executeUpdate(); ps.close(); } catch (Exception e) { e.printStackTrace(); }
                        });

                LOGGER.info("[QuestBoard] Cleaned up " + accepted.size() + " scoped quests for removed entity '"
                        + entityId + "' (scope: " + scopeProviderKey + ")");
            } catch (Exception e) {
                LOGGER.severe("[QuestBoard] Failed to clean up entity removal for '" + entityId + "': " + e.getMessage());
                e.printStackTrace();
            }
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
                    LOGGER.warning("[QuestBoard] Offering " + offering.getOfferingId()
                            + " is ACCEPTED but quest instance " + instanceUUID
                            + " not found in active quests. Transitioning to EXPIRED.");
                    offering.transitionTo(BoardOffering.State.EXPIRED);
                    BoardOfferingDAO.updateOfferingState(connection, offering.getOfferingId(),
                            BoardOffering.State.EXPIRED, null, null).forEach(ps -> {
                        try {
                            ps.executeUpdate();
                            ps.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }
}
