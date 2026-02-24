package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import us.eunoians.mcrpg.McRPG;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.reward.PendingReward;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for persisting and loading {@link PendingReward} data.
 * Pending rewards are queued for offline players and granted on their next login.
 */
public class PendingRewardDAO {

    public static final String TABLE_NAME = "mcrpg_pending_quest_rewards";
    private static final int CURRENT_TABLE_VERSION = 1;
    private static final Gson GSON = new Gson();
    private static final Type CONFIG_MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    /**
     * Attempts to create the table if it does not already exist.
     *
     * @param connection the database connection
     * @param database   the database instance
     * @return {@code true} if a new table was created
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`id` VARCHAR(36) NOT NULL, " +
                "`player_uuid` VARCHAR(36) NOT NULL, " +
                "`reward_type_key` VARCHAR(255) NOT NULL, " +
                "`serialized_config` TEXT NOT NULL, " +
                "`quest_key` VARCHAR(255) NOT NULL, " +
                "`created_at` BIGINT NOT NULL, " +
                "`expires_at` BIGINT NOT NULL, " +
                "PRIMARY KEY (`id`)" +
                ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Applies any pending schema migrations.
     *
     * @param connection the database connection
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion == 0) {
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
    }

    /**
     * Generates the prepared statements to save a pending reward.
     *
     * @param connection    the database connection
     * @param pendingReward the pending reward to save
     * @return the list of prepared statements
     */
    @NotNull
    public static List<PreparedStatement> savePendingReward(@NotNull Connection connection, @NotNull PendingReward pendingReward) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (id, player_uuid, reward_type_key, serialized_config, quest_key, created_at, expires_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT (id) DO NOTHING");
            statement.setString(1, pendingReward.getId().toString());
            statement.setString(2, pendingReward.getPlayerUUID().toString());
            statement.setString(3, pendingReward.getRewardTypeKey().toString());
            statement.setString(4, GSON.toJson(pendingReward.getSerializedConfig()));
            statement.setString(5, pendingReward.getQuestKey().toString());
            statement.setLong(6, pendingReward.getCreatedAt());
            statement.setLong(7, pendingReward.getExpiresAt());
            statements.add(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Loads all non-expired pending rewards for a player and deletes expired ones
     * in the same operation.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID to load rewards for
     * @return the list of valid pending rewards
     */
    @NotNull
    public static List<PendingReward> loadAndCleanPendingRewards(@NotNull Connection connection, @NotNull UUID playerUUID) {
        long now = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        List<PendingReward> rewards = new ArrayList<>();

        try (PreparedStatement deleteExpired = connection.prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE player_uuid = ? AND expires_at <= ?")) {
            deleteExpired.setString(1, playerUUID.toString());
            deleteExpired.setLong(2, now);
            deleteExpired.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement select = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            select.setString(1, playerUUID.toString());
            try (ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> config = GSON.fromJson(rs.getString("serialized_config"), CONFIG_MAP_TYPE);
                    String rewardTypeKeyStr = rs.getString("reward_type_key");
                    String questKeyStr = rs.getString("quest_key");
                    rewards.add(new PendingReward(
                            UUID.fromString(rs.getString("id")),
                            playerUUID,
                            NamespacedKey.fromString(rewardTypeKeyStr),
                            config,
                            NamespacedKey.fromString(questKeyStr),
                            rs.getLong("created_at"),
                            rs.getLong("expires_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rewards;
    }

    /**
     * Deletes a specific pending reward by its ID after it has been granted.
     *
     * @param connection the database connection
     * @param rewardId   the pending reward ID to delete
     */
    public static void deletePendingReward(@NotNull Connection connection, @NotNull UUID rewardId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE id = ?")) {
            statement.setString(1, rewardId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
