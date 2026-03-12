package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CoreTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.PendingRewardDAO;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.reward.PendingReward;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Starts the {@link McRPGPlayerLoadTask} to load in the player and grants any
 * pending quest rewards that were queued while the player was offline.
 */
public class PlayerJoinListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void handleJoin(@NotNull PlayerJoinEvent playerJoinEvent) {

        McRPG mcRPG = McRPG.getInstance();
        Player player = playerJoinEvent.getPlayer();
        McRPGPlayer mcRPGPlayer = new McRPGPlayer(player, mcRPG);
        new McRPGPlayerLoadTask(mcRPG, mcRPGPlayer).runTask();

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);

        grantPendingRewards(mcRPG, player);

        mcRPG.getServer().getScheduler().runTaskLater(mcRPG, () -> {
            McRPGPlayerManager playerManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.PLAYER);
            playerManager.getPlayer(player.getUniqueId()).ifPresent(
                    questManager::sanityCheckUpgradeQuests);
        }, 40L);
    }

    /**
     * Loads and grants any pending quest rewards for the joining player.
     * Expired rewards are cleaned up and granted rewards are deleted from the database.
     * Reward granting happens on the main thread after the async database load completes.
     *
     * @param mcRPG  the plugin instance
     * @param player the player who joined
     */
    private void grantPendingRewards(@NotNull McRPG mcRPG, @NotNull Player player) {
        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();

        database.getDatabaseExecutorService().submit(() -> {
            List<PendingReward> pendingRewards;
            try (Connection connection = database.getConnection()) {
                pendingRewards = PendingRewardDAO.loadAndCleanPendingRewards(connection, player.getUniqueId());
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            if (pendingRewards.isEmpty()) {
                return;
            }

            new CoreTask(mcRPG) {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        return;
                    }
                    QuestRewardTypeRegistry rewardTypeRegistry = RegistryAccess.registryAccess()
                            .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);

                    for (PendingReward pending : pendingRewards) {
                        Optional<QuestRewardType> baseType = rewardTypeRegistry.get(pending.getRewardTypeKey());
                        if (baseType.isEmpty()) {
                            continue;
                        }
                        QuestRewardType configured = baseType.get().fromSerializedConfig(pending.getSerializedConfig());
                        configured.grant(player);
                    }

                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            for (PendingReward pending : pendingRewards) {
                                PendingRewardDAO.deletePendingReward(connection, pending.getId());
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }.runTask();
        });
    }
}
