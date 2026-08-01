package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CoreTask;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.database.table.quest.PendingRewardDAO;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.reward.PendingReward;
import us.eunoians.mcrpg.quest.reward.QuestRewardGranter;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.reward.RewardGrantContext;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Starts the {@link McRPGPlayerLoadTask} to load in the player and grants any
 * pending quest rewards that were queued while the player was offline.
 */
public class PlayerJoinListener implements Listener {

    private final CombatTrackerManager combatTrackerManager;

    /**
     * Constructs a new {@link PlayerJoinListener}.
     *
     * @param combatTrackerManager The {@link CombatTrackerManager} used to pre-load and cache
     *                              persistent combat state for the joining player.
     */
    public PlayerJoinListener(@NotNull CombatTrackerManager combatTrackerManager) {
        this.combatTrackerManager = combatTrackerManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void handleJoin(@NotNull PlayerJoinEvent playerJoinEvent) {

        McRPG mcRPG = McRPG.getInstance();
        Player player = playerJoinEvent.getPlayer();
        McRPGPlayer mcRPGPlayer = new McRPGPlayer(player, mcRPG);
        new McRPGPlayerLoadTask(mcRPG, mcRPGPlayer, combatTrackerManager).runTask();

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
     * Rewards are resolved and grouped per quest key, then handed to {@link QuestRewardGranter} which
     * fires the {@link us.eunoians.mcrpg.event.quest.QuestRewardGrantEvent} interception point (context
     * {@link RewardGrantContext#PENDING}) before granting.
     * <p>
     * A reward whose type key is not registered is retained (never granted, never marked for deletion)
     * so it survives until the owning expansion returns or the row expires. A reward whose
     * {@code fromSerializedConfig} throws, whose {@code grant} throws, or which a listener removes or
     * cancels is not marked for deletion, so it is left in the database and retried on the next login
     * rather than being silently lost.
     *
     * @param mcRPG              the plugin instance
     * @param player            the player to grant rewards to
     * @param pendingRewards    the loaded pending rewards
     * @param rewardTypeRegistry the reward type registry used to resolve reward keys
     * @return the IDs of the rewards that were successfully granted and may now be deleted
     */
    @NotNull
    Set<UUID> grantRewards(@NotNull McRPG mcRPG, @NotNull Player player,
                           @NotNull List<PendingReward> pendingRewards,
                           @NotNull QuestRewardTypeRegistry rewardTypeRegistry) {
        // Resolve each pending row to a configured reward, grouped by quest key so the grant event
        // fires per-quest batch. An identity map ties each configured reward instance back to a queue of
        // its row ids: multiple rows can reconstruct to the same reward instance (a type whose
        // fromSerializedConfig returns a shared/cached instance), and each successful grant consumes one
        // id so every granted row is deleted exactly once. A reward a listener removes/replaces (or a
        // cancelled batch) leaves its rows retained for a later retry.
        Map<NamespacedKey, List<QuestRewardType>> rewardsByQuest = new LinkedHashMap<>();
        Map<QuestRewardType, Deque<UUID>> rewardToPendingIds = new IdentityHashMap<>();
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
                rewardsByQuest.computeIfAbsent(pending.getQuestKey(), key -> new ArrayList<>()).add(configured);
                rewardToPendingIds.computeIfAbsent(configured, key -> new ArrayDeque<>()).add(pending.getId());
            } catch (RuntimeException e) {
                mcRPG.getLogger().log(Level.WARNING, "Failed to reconstruct pending reward " + pending.getId() + " (type '"
                        + pending.getRewardTypeKey() + "') for player " + player.getUniqueId()
                        + ". The reward is retained and will be retried on next login.", e);
            }
        }

        Set<UUID> grantedRewardIds = new HashSet<>();
        QuestRewardGranter granter = new QuestRewardGranter(mcRPG);
        for (Map.Entry<NamespacedKey, List<QuestRewardType>> entry : rewardsByQuest.entrySet()) {
            List<QuestRewardType> granted = granter.grantToOnlinePlayer(player, entry.getValue(), entry.getKey(),
                    null, RewardGrantContext.PENDING);
            for (QuestRewardType grantedReward : granted) {
                Deque<UUID> pendingIds = rewardToPendingIds.get(grantedReward);
                if (pendingIds != null && !pendingIds.isEmpty()) {
                    grantedRewardIds.add(pendingIds.poll());
                }
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
