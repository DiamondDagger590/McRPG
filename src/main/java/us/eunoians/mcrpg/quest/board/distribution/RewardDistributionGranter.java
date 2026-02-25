package us.eunoians.mcrpg.quest.board.distribution;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
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
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Bridge between the pure {@link QuestRewardDistributionResolver} output and the Bukkit
 * reward granting pipeline. This is the only class in the distribution package that
 * interacts with Bukkit. Handles online/offline player detection and delegates offline
 * rewards to {@link PendingRewardDAO}.
 */
public final class RewardDistributionGranter {

    private RewardDistributionGranter() {
    }

    /**
     * Grants resolved distribution rewards to qualifying players. Online players
     * receive rewards immediately; offline players have them queued via
     * {@link PendingRewardDAO}.
     *
     * @param resolvedRewards map of player UUID to rewards from the resolver
     * @param questKey        the quest definition key (for pending reward tracking)
     */
    public static void grant(@NotNull Map<UUID, List<QuestRewardType>> resolvedRewards,
                             @NotNull NamespacedKey questKey) {
        for (Map.Entry<UUID, List<QuestRewardType>> entry : resolvedRewards.entrySet()) {
            UUID playerUUID = entry.getKey();
            List<QuestRewardType> rewards = entry.getValue();
            if (rewards.isEmpty()) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                for (QuestRewardType reward : rewards) {
                    reward.grant(player);
                }
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
    private static void queueForOffline(@NotNull UUID playerUUID,
                                        @NotNull List<QuestRewardType> rewards,
                                        @NotNull NamespacedKey questKey) {
        int expiryDays = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG)
                .getInt(MainConfigFile.QUEST_PENDING_REWARDS_EXPIRY_DAYS, 30);
        long now = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        long expiresAt = now + TimeUnit.DAYS.toMillis(expiryDays);

        Database database = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                for (QuestRewardType reward : rewards) {
                    PendingReward pending = new PendingReward(
                            UUID.randomUUID(),
                            playerUUID,
                            reward.getKey(),
                            reward.serializeConfig(),
                            questKey,
                            now,
                            expiresAt
                    );
                    for (PreparedStatement stmt : PendingRewardDAO.savePendingReward(connection, pending)) {
                        stmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
