package us.eunoians.mcrpg.quest.board.distribution;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.table.quest.PendingRewardDAO;
import us.eunoians.mcrpg.quest.reward.PendingReward;
import us.eunoians.mcrpg.quest.reward.QuestRewardGranter;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.RewardGrantContext;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Bridge between the pure {@link QuestRewardDistributionResolver} output and the Bukkit
 * reward granting pipeline. This is the only class in the distribution package that
 * interacts with Bukkit. Handles online/offline player detection and delegates offline
 * rewards to {@link PendingRewardDAO}.
 */
public final class RewardDistributionGranter {

    private final McRPG plugin;
    private final QuestRewardGranter rewardGranter;

    public RewardDistributionGranter(@NotNull McRPG plugin) {
        this.plugin = plugin;
        this.rewardGranter = new QuestRewardGranter(plugin);
    }

    /**
     * Grants resolved distribution rewards to qualifying players. Online players
     * receive rewards immediately; offline players have them queued via
     * {@link PendingRewardDAO}.
     *
     * @param resolvedRewards map of player UUID to rewards from the resolver
     * @param questKey        the quest definition key (for pending reward tracking)
     */
    public void grant(@NotNull Map<UUID, List<QuestRewardType>> resolvedRewards,
                      @NotNull NamespacedKey questKey) {
        for (Map.Entry<UUID, List<QuestRewardType>> entry : resolvedRewards.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<QuestRewardType> rewards = entry.getValue();
            if (rewards.isEmpty()) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                rewardGranter.grantToOnlinePlayer(player, rewards, questKey, null, RewardGrantContext.DISTRIBUTION);
            } else {
                queueForOffline(playerUUID, rewards, questKey);
            }
        }
    }

    /**
     * Persists rewards for an offline player to the pending rewards table. Each
     * reward is serialized and stored with an expiry timestamp derived from
     * the server's configured {@code pending-rewards-expiry-days} setting.
     * The database write is submitted asynchronously on the database executor.
     *
     * @param playerUUID the UUID of the offline player
     * @param rewards    the rewards to queue for later granting
     * @param questKey   the quest definition key (stored for audit/tracking)
     */
    private void queueForOffline(@NotNull UUID playerUUID,
                                 @NotNull List<QuestRewardType> rewards,
                                 @NotNull NamespacedKey questKey) {
        int expiryDays = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getInt(MainConfigFile.QUEST_PENDING_REWARDS_EXPIRY_DAYS, 30);
        long now = plugin.getTimeProvider().now().toEpochMilli();
        long expiresAt = now + TimeUnit.DAYS.toMillis(expiryDays);

        Database database = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE)
                .getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = new ArrayList<>();
                for (QuestRewardType reward : rewards) {
                    try {
                        PendingReward pending = new PendingReward(
                                UUID.randomUUID(),
                                playerUUID,
                                reward.getKey(),
                                reward.serializeConfig(),
                                questKey,
                                now,
                                expiresAt
                        );
                        statements.addAll(PendingRewardDAO.savePendingReward(connection, pending));
                    } catch (RuntimeException e) {
                        plugin.getLogger().log(Level.SEVERE, "Failed to build pending distribution reward '" + reward.getKey()
                                + "' for offline player " + playerUUID + " (quest: " + questKey + "); skipping this reward.", e);
                    }
                }
                new FailSafeTransaction(connection, statements).executeTransaction();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "Failed to persist pending rewards for offline player (quest: " + questKey + ")", e);
            }
        });
    }
}
