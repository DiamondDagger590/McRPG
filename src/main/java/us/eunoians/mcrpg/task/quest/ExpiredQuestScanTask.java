package us.eunoians.mcrpg.task.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.board.PlayerBoardStateDAO;
import us.eunoians.mcrpg.database.table.quest.QuestInstanceDAO;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Periodic background task that scans for and expires stale quests.
 * <p>
 * Three-phase sweep on each interval:
 * <ol>
 *     <li><b>Near-expiry pass</b> – Checks for quests approaching expiry and sends
 *     batched near-expiry notifications to affected online players. Quests within
 *     the configured threshold fire as triggers; quests within 1.5× the threshold
 *     are bundled into the same message to reduce noise. The {@link QuestInstance#isNearExpiryNotified()}
 *     flag prevents duplicate notifications within the same online session.</li>
 *     <li><b>In-memory expiry pass</b> – Iterates all Tier 1 active quests on the main thread
 *     and calls {@link QuestInstance#expire()} on any that have passed their expiration time.
 *     This ensures events fire and listeners (e.g. {@code QuestCancelListener}) properly
 *     retire the quest from memory.</li>
 *     <li><b>Database sweep</b> – Runs an async bulk UPDATE via
 *     {@link QuestInstanceDAO#bulkExpireStaleQuests} to catch quests that were never loaded
 *     into memory (e.g. they expired while all scope players were offline).</li>
 * </ol>
 */
public final class ExpiredQuestScanTask extends CancelableCoreTask {

    public ExpiredQuestScanTask(@NotNull McRPG plugin, double taskDelay, double taskFrequency) {
        super(plugin, taskDelay, taskFrequency);
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @Override
    protected void onIntervalComplete() {
        McRPG plugin = getPlugin();
        QuestManager questManager = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        long now = plugin.getTimeProvider().now().toEpochMilli();
        long thresholdMs = loadNearExpiryThresholdMs(plugin);
        long lookaheadMs = (long) (thresholdMs * 1.5);

        // Phase 0: near-expiry notifications
        Map<UUID, Set<QuestInstance>> toNotify = buildNearExpiryNotifications(questManager, now, thresholdMs, lookaheadMs);
        if (!toNotify.isEmpty()) {
            scheduleNearExpiryDelivery(Map.copyOf(toNotify), plugin);
        }

        // Phase 1: expire in-memory active quests on the main thread
        List<QuestInstance> toExpire = collectExpiredQuests(questManager);
        if (!toExpire.isEmpty()) {
            scheduleInMemoryExpiry(toExpire, plugin);
        }

        // Phase 2: bulk-expire database-only stale quests asynchronously
        submitDatabaseExpirySweep(plugin, now);
    }

    /**
     * Collects per-player near-expiry notification sets from the active quest pool.
     * Quests within {@code thresholdMs} are trigger quests; quests within {@code lookaheadMs}
     * are pulled into the same message for players who already have a trigger quest.
     *
     * @param questManager the quest manager
     * @param now          current epoch millis
     * @param thresholdMs  near-expiry threshold in milliseconds
     * @param lookaheadMs  1.5× threshold used for batching
     * @return map from player UUID to the set of quests to include in their notification
     */
    @NotNull
    private Map<UUID, Set<QuestInstance>> buildNearExpiryNotifications(@NotNull QuestManager questManager,
                                                                        long now,
                                                                        long thresholdMs,
                                                                        long lookaheadMs) {
        Map<UUID, Set<QuestInstance>> triggerQuests = new HashMap<>();
        Map<UUID, Set<QuestInstance>> lookaheadQuests = new HashMap<>();

        for (QuestInstance quest : questManager.getActiveQuests()) {
            if (quest.isNearExpiryNotified()) {
                continue;
            }
            Optional<Long> expOpt = quest.getExpirationTime();
            if (expOpt.isEmpty()) {
                continue;
            }
            long timeUntilExpiry = expOpt.get() - now;
            if (timeUntilExpiry <= 0) {
                continue; // handled by the expire pass
            }

            quest.getQuestScope().ifPresent(scope -> {
                for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                    if (timeUntilExpiry <= thresholdMs) {
                        triggerQuests.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(quest);
                    } else if (timeUntilExpiry <= lookaheadMs) {
                        lookaheadQuests.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(quest);
                    }
                }
            });
        }

        // Merge lookahead into each player's trigger set so batched messages are grouped
        Map<UUID, Set<QuestInstance>> toNotify = new HashMap<>(triggerQuests);
        for (UUID playerUUID : triggerQuests.keySet()) {
            Set<QuestInstance> extra = lookaheadQuests.get(playerUUID);
            if (extra != null) {
                toNotify.get(playerUUID).addAll(extra);
            }
        }
        return toNotify;
    }

    /**
     * Schedules main-thread delivery of near-expiry notifications.
     * Bukkit API calls are deferred here because this method runs on the scheduler thread.
     *
     * @param notifySnapshot immutable snapshot of the per-player notification map
     * @param plugin         the McRPG plugin instance
     */
    private void scheduleNearExpiryDelivery(@NotNull Map<UUID, Set<QuestInstance>> notifySnapshot,
                                            @NotNull McRPG plugin) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION);
            McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.PLAYER);
            long nowMain = plugin.getTimeProvider().now().toEpochMilli();

            for (Map.Entry<UUID, Set<QuestInstance>> entry : notifySnapshot.entrySet()) {
                UUID playerUUID = entry.getKey();
                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline()) {
                    continue;
                }
                Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(playerUUID);
                if (mcRPGPlayerOpt.isEmpty()) {
                    continue;
                }
                McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
                Set<QuestInstance> quests = entry.getValue();

                sendNearExpiryNotification(player, mcRPGPlayer, quests, nowMain, localizationManager, plugin);
                quests.forEach(q -> q.setNearExpiryNotified(true));
            }
        });
    }

    /**
     * Returns the list of in-memory active quests that have passed their expiration time.
     *
     * @param questManager the quest manager
     * @return quests ready to be expired
     */
    @NotNull
    private List<QuestInstance> collectExpiredQuests(@NotNull QuestManager questManager) {
        List<QuestInstance> toExpire = new ArrayList<>();
        for (QuestInstance quest : questManager.getActiveQuests()) {
            if (quest.isExpired()) {
                toExpire.add(quest);
            }
        }
        return toExpire;
    }

    /**
     * Schedules main-thread expiry for a list of quests that have passed their expiration time.
     *
     * @param toExpire quests to expire
     * @param plugin   the McRPG plugin instance
     */
    private void scheduleInMemoryExpiry(@NotNull List<QuestInstance> toExpire, @NotNull McRPG plugin) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (QuestInstance quest : toExpire) {
                quest.expire();
            }
            plugin.getLogger().info("[ExpiredQuestScan] Expired " + toExpire.size() + " in-memory quest(s).");
        });
    }

    /**
     * Submits an async database sweep to bulk-expire quests that were never loaded into memory.
     *
     * @param plugin the McRPG plugin instance
     * @param now    epoch millis used as the expiry cutoff
     */
    private void submitDatabaseExpirySweep(@NotNull McRPG plugin, long now) {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase()
                .getDatabaseExecutorService().submit(() -> {
                    try (Connection connection = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase().getConnection()) {
                        int updated = QuestInstanceDAO.bulkExpireStaleQuests(connection, now);
                        if (updated > 0) {
                            int boardReleased = PlayerBoardStateDAO.bulkCancelExpiredBoardStates(connection);
                            plugin.getLogger().info("[ExpiredQuestScan] Bulk-expired " + updated
                                    + " stale quest(s) in database, released " + boardReleased + " board slot(s).");
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE,
                                "[ExpiredQuestScan] Failed to bulk-expire stale quests", e);
                    }
                });
    }

    /**
     * Sends a near-expiry notification to a player. Uses a single-quest or batched
     * format depending on how many quests are included.
     *
     * @param player              the Bukkit player to notify
     * @param mcRPGPlayer         the McRPG player wrapper for locale resolution
     * @param quests              the set of quests to include in the notification
     * @param now                 current epoch millis for time-remaining calculation
     * @param localizationManager the localization manager instance
     * @param plugin              the McRPG plugin instance
     */
    private void sendNearExpiryNotification(@NotNull Player player,
                                            @NotNull McRPGPlayer mcRPGPlayer,
                                            @NotNull Set<QuestInstance> quests,
                                            long now,
                                            @NotNull McRPGLocalizationManager localizationManager,
                                            @NotNull McRPG plugin) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);

        if (quests.size() == 1) {
            QuestInstance quest = quests.iterator().next();
            String questName = resolveQuestName(quest, mcRPGPlayer, definitionRegistry);
            String timeRemaining = formatTimeRemaining(quest.getExpirationTime().orElse(now) - now);

            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.QUEST_NEAR_EXPIRY_SINGLE_NOTIFICATION,
                    Map.of("quest_name", questName, "time_remaining", timeRemaining)));
        } else {
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer, LocalizationKey.QUEST_NEAR_EXPIRY_BATCH_HEADER,
                    Map.of("count", String.valueOf(quests.size()))));

            for (QuestInstance quest : quests) {
                String questName = resolveQuestName(quest, mcRPGPlayer, definitionRegistry);
                String timeRemaining = formatTimeRemaining(quest.getExpirationTime().orElse(now) - now);
                player.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                        mcRPGPlayer, LocalizationKey.QUEST_NEAR_EXPIRY_BATCH_ENTRY,
                        Map.of("quest_name", questName, "time_remaining", timeRemaining)));
            }
        }
    }

    /**
     * Resolves the display name for a quest, with fallback to the quest key.
     *
     * @param quest              the quest instance
     * @param player             the player whose locale to use
     * @param definitionRegistry the quest definition registry
     * @return the display name string
     */
    @NotNull
    private String resolveQuestName(@NotNull QuestInstance quest,
                                    @NotNull McRPGPlayer player,
                                    @NotNull QuestDefinitionRegistry definitionRegistry) {
        return definitionRegistry.get(quest.getQuestKey())
                .map(def -> def.getDisplayName(player))
                .orElse(quest.getQuestKey().getKey());
    }

    /**
     * Formats a millisecond duration as "Xh Ym" for display.
     *
     * @param millisRemaining time remaining in milliseconds (clamped to 0 if negative)
     * @return formatted string, e.g. "1h 30m"
     */
    @NotNull
    @org.jetbrains.annotations.VisibleForTesting
    static String formatTimeRemaining(long millisRemaining) {
        if (millisRemaining <= 0) {
            return "0h 0m";
        }
        Duration d = Duration.ofMillis(millisRemaining);
        return d.toHours() + "h " + d.toMinutesPart() + "m";
    }

    /**
     * Reads the near-expiry threshold from config and returns it in milliseconds.
     *
     * @param plugin the McRPG plugin instance
     * @return threshold duration in milliseconds
     */
    private long loadNearExpiryThresholdMs(@NotNull McRPG plugin) {
        YamlDocument boardConfig = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.BOARD_CONFIG);
        int minutes = boardConfig.getInt(BoardConfigFile.NEAR_EXPIRY_THRESHOLD_MINUTES, 60);
        return (long) minutes * 60_000L;
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected void onDelayComplete() {
    }

    @Override
    protected void onIntervalStart() {
    }

    @Override
    protected void onIntervalPause() {
    }

    @Override
    protected void onIntervalResume() {
    }
}
