package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

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
                mcRPG.getLogger().log(Level.SEVERE, "Failed to load pending rewards for player " + player.getUniqueId(), e);
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

                    Set<UUID> grantedRewardIds = grantRewards(mcRPG, player, pendingRewards, rewardTypeRegistry);

                    if (grantedRewardIds.isEmpty()) {
                        return;
                    }

                    deleteGrantedRewards(mcRPG, database, player.getUniqueId(), grantedRewardIds);
                }
            }.runTask();
        });
    }

    /**
     * Grants each loaded pending reward in isolation and reports which rows were actually granted.
     * <p>
     * A reward whose type key is not registered is retained (never granted, never marked for deletion)
     * so it survives until the owning expansion returns or the row expires. A reward whose
     * {@code fromSerializedConfig}/{@code grant} throws is logged and skipped without aborting the loop,
     * and is left in the database so it is not silently lost.
     *
     * @param mcRPG              the plugin instance
     * @param player            the player to grant rewards to
     * @param pendingRewards    the loaded pending rewards
     * @param rewardTypeRegistry the reward type registry used to resolve reward keys
     * @return the IDs of the rewards that were successfully granted and may now be deleted
     */
    @NotNull
    private Set<UUID> grantRewards(@NotNull McRPG mcRPG, @NotNull Player player,
                                   @NotNull List<PendingReward> pendingRewards,
                                   @NotNull QuestRewardTypeRegistry rewardTypeRegistry) {
        Set<UUID> grantedRewardIds = new HashSet<>();
        for (PendingReward pending : pendingRewards) {
            Optional<QuestRewardType> baseType = rewardTypeRegistry.get(pending.getRewardTypeKey());
            if (baseType.isEmpty()) {
                mcRPG.getLogger().log(Level.WARNING, "Retaining pending reward " + pending.getId() + " for player "
                        + player.getUniqueId() + ": reward type '" + pending.getRewardTypeKey()
                        + "' is not registered (expansion not enabled?). The reward will be granted once the type is available.");
                continue;
            }
            try {
                QuestRewardType configured = baseType.get().fromSerializedConfig(pending.getSerializedConfig());
                configured.grant(player);
                grantedRewardIds.add(pending.getId());
            } catch (RuntimeException e) {
                mcRPG.getLogger().log(Level.SEVERE, "Failed to grant pending reward " + pending.getId() + " (type '"
                        + pending.getRewardTypeKey() + "') for player " + player.getUniqueId()
                        + ". The reward is retained and will be retried on next login.", e);
            }
        }
        return grantedRewardIds;
    }

    /**
     * Deletes exactly the rows that were granted, atomically, on the database executor thread.
     *
     * @param mcRPG            the plugin instance
     * @param database         the database instance
     * @param playerUUID       the player whose rewards were granted
     * @param grantedRewardIds the IDs of the rewards to delete
     */
    private void deleteGrantedRewards(@NotNull McRPG mcRPG, @NotNull Database database,
                                      @NotNull UUID playerUUID, @NotNull Set<UUID> grantedRewardIds) {
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                new FailSafeTransaction(connection,
                        PendingRewardDAO.deletePendingRewards(connection, grantedRewardIds)).executeTransaction();
            } catch (SQLException e) {
                mcRPG.getLogger().log(Level.SEVERE, "Failed to delete granted pending rewards for player " + playerUUID
                        + "; they may be re-granted on next login.", e);
            }
        });
    }
}
