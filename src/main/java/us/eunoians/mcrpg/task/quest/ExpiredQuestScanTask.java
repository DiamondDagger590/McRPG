package us.eunoians.mcrpg.task.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.QuestInstanceDAO;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Periodic background task that scans for and expires stale quests.
 * <p>
 * Two-phase sweep on each interval:
 * <ol>
 *     <li><b>In-memory pass</b> – Iterates all Tier 1 active quests on the main thread
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

        // Phase 1: expire in-memory active quests on the main thread
        QuestManager questManager = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);

        List<QuestInstance> toExpire = new ArrayList<>();
        for (QuestInstance quest : questManager.getActiveQuests()) {
            if (quest.isExpired()) {
                toExpire.add(quest);
            }
        }

        if (!toExpire.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (QuestInstance quest : toExpire) {
                    quest.expire();
                }
                plugin.getLogger().info("[ExpiredQuestScan] Expired " + toExpire.size()
                        + " in-memory quest(s).");
            });
        }

        // Phase 2: bulk-expire database-only stale quests asynchronously
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase()
                .getDatabaseExecutorService().submit(() -> {
                    try (Connection connection = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase().getConnection()) {
                        long now = plugin.getTimeProvider().now().toEpochMilli();
                        int updated = QuestInstanceDAO.bulkExpireStaleQuests(connection, now);
                        if (updated > 0) {
                            plugin.getLogger().info("[ExpiredQuestScan] Bulk-expired " + updated
                                    + " stale quest(s) in database.");
                        }
                    } catch (SQLException e) {
                        plugin.getLogger().log(Level.SEVERE,
                                "[ExpiredQuestScan] Failed to bulk-expire stale quests", e);
                    }
                });
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
