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
import us.eunoians.mcrpg.configuration.BoardCategoryConfigLoader;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent;
import us.eunoians.mcrpg.event.board.BoardOfferingExpireEvent;
import us.eunoians.mcrpg.event.board.BoardOfferingGenerateEvent;
import us.eunoians.mcrpg.event.board.BoardRotationEvent;
import us.eunoians.mcrpg.database.table.board.BoardCooldownDAO;
import us.eunoians.mcrpg.database.table.board.BoardOfferingDAO;
import us.eunoians.mcrpg.database.table.board.BoardRotationDAO;
import us.eunoians.mcrpg.database.table.board.PlayerBoardStateDAO;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategoryRegistry;
import us.eunoians.mcrpg.quest.board.configuration.ReloadableCategoryConfig;
import us.eunoians.mcrpg.quest.board.configuration.ReloadableRarityConfig;
import us.eunoians.mcrpg.quest.board.generation.QuestPool;
import us.eunoians.mcrpg.quest.board.generation.SlotGenerationLogic;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.refresh.RefreshType;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;
import us.eunoians.mcrpg.quest.board.refresh.builtin.WeeklyRefreshType;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
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
    private static final NamespacedKey DEFAULT_BOARD_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "default_board");

    private final Map<NamespacedKey, QuestBoard> boards = new HashMap<>();
    private final Map<NamespacedKey, List<BoardOffering>> offeringCache = new ConcurrentHashMap<>();
    private QuestPool questPool;
    private ReloadableRarityConfig rarityConfig;
    private ReloadableCategoryConfig categoryConfig;

    public QuestBoardManager(@NotNull McRPG plugin) {
        super(plugin);
    }

    public void initialize(@NotNull McRPG plugin) {
        // 1. Load board.yml
        YamlDocument boardConfig = plugin.registryAccess()
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

        // 6. Create quest pool
        QuestDefinitionRegistry definitionRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        this.questPool = new QuestPool(definitionRegistry);

        // 7. Load current rotations from DB
        Database database = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                BoardRotationDAO.loadCurrentRotation(connection, DEFAULT_BOARD_KEY, DailyRefreshType.KEY)
                        .ifPresent(defaultBoard::setCurrentDailyRotation);
                BoardRotationDAO.loadCurrentRotation(connection, DEFAULT_BOARD_KEY, WeeklyRefreshType.KEY)
                        .ifPresent(defaultBoard::setCurrentWeeklyRotation);
            } catch (Exception e) {
                LOGGER.warning("[QuestBoard] Failed to load current rotations: " + e.getMessage());
            }
        });

        LOGGER.info("[QuestBoard] Initialized with " + rarityRegistry.getAll().size() + " rarities, "
                + categoryRegistry.getAll().size() + " categories");
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

        database.getDatabaseExecutorService().submit(() -> {
            Optional<BoardRotation> previousRotation;
            List<BoardOffering> offerings;

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

                offerings = generateSharedOfferings(board, rotation, new Random());
            } catch (Exception e) {
                LOGGER.severe("[QuestBoard] Rotation failed for " + refreshTypeKey + ": " + e.getMessage());
                e.printStackTrace();
                return;
            }

            final Optional<BoardRotation> prevRef = previousRotation;
            final List<BoardOffering> generatedOfferings = offerings;

            Bukkit.getScheduler().runTask(plugin(), () -> {
                prevRef.ifPresent(prev ->
                        Bukkit.getPluginManager().callEvent(new BoardOfferingExpireEvent(board, prev)));

                BoardOfferingGenerateEvent generateEvent = new BoardOfferingGenerateEvent(board, rotation, generatedOfferings);
                Bukkit.getPluginManager().callEvent(generateEvent);
                List<BoardOffering> finalOfferings = generateEvent.getOfferings();

                if (refreshTypeKey.equals(DailyRefreshType.KEY)) {
                    board.setCurrentDailyRotation(rotation);
                } else {
                    board.setCurrentWeeklyRotation(rotation);
                }
                offeringCache.remove(board.getBoardKey());

                Bukkit.getPluginManager().callEvent(new BoardRotationEvent(board, rotation, finalOfferings));

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
     * Generates shared offerings for a board rotation.
     */
    @NotNull
    public List<BoardOffering> generateSharedOfferings(@NotNull QuestBoard board,
                                                        @NotNull BoardRotation rotation,
                                                        @NotNull Random random) {
        BoardSlotCategoryRegistry categoryRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.BOARD_SLOT_CATEGORY);
        QuestRarityRegistry rarityRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.QUEST_RARITY);

        List<BoardSlotCategory> categories = categoryRegistry.getByVisibility(BoardSlotCategory.Visibility.SHARED);
        categories = categories.stream()
                .filter(c -> c.getRefreshTypeKey().equals(rotation.getRefreshTypeKey()))
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .toList();

        Map<NamespacedKey, Integer> slotCounts = SlotGenerationLogic.computeSlotCounts(
                categories, board.getMinimumTotalOfferings(), random, key -> false);

        List<BoardOffering> offerings = new ArrayList<>();
        int slotIndex = 0;

        for (BoardSlotCategory category : categories) {
            int count = slotCounts.getOrDefault(category.getKey(), 0);
            for (int i = 0; i < count; i++) {
                QuestRarity rarity = rarityRegistry.rollRarity(random);
                List<NamespacedKey> eligible = questPool.getEligibleDefinitions(rarity.getKey());

                Optional<NamespacedKey> selected = SlotGenerationLogic.selectQuestForSlot(eligible, random);

                if (selected.isEmpty()) {
                    // Backfill: try all eligible
                    eligible = questPool.getAllBoardEligibleDefinitions();
                    selected = SlotGenerationLogic.selectQuestForSlot(eligible, random);
                }

                if (selected.isPresent()) {
                    offerings.add(new BoardOffering(
                            UUID.randomUUID(),
                            rotation.getRotationId(),
                            category.getKey(),
                            slotIndex++,
                            selected.get(),
                            rarity.getKey(),
                            null,
                            category.getCompletionTime()
                    ));
                }
            }
        }

        return offerings;
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
        return offeringCache.computeIfAbsent(boardKey, key -> {
            QuestBoard board = boards.get(key);
            if (board == null) return List.of();

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
            } catch (Exception e) {
                LOGGER.warning("[QuestBoard] Failed to load offerings for board " + key + ": " + e.getMessage());
            }
            return all;
        });
    }

    /**
     * Attempts to accept an offering for a player.
     *
     * @param player     the player
     * @param offeringId the offering UUID
     * @return {@code true} if the offering was accepted
     */
    public boolean acceptOffering(@NotNull Player player, @NotNull UUID offeringId) {
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

        offering.accept(plugin().getTimeProvider().now().toEpochMilli(), UUID.randomUUID());

        return true;
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
}
