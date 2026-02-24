package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_quest_completion_log} table, which records each player's
 * quest completions. Used to enforce repeat mode restrictions ({@code ONCE}, {@code LIMITED},
 * {@code COOLDOWN}).
 */
public class QuestCompletionLogDAO {

    public static final String TABLE_NAME = "mcrpg_quest_completion_log";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create the completion log table if it does not already exist.
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
                "`definition_key` VARCHAR(255) NOT NULL, " +
                "`quest_uuid` VARCHAR(36) NOT NULL, " +
                "`completed_at` BIGINT NOT NULL, " +
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
     * Applies any pending schema migrations for this table.
     *
     * @param connection the database connection
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion == 0) {
            String[] indexes = {
                    "CREATE INDEX IF NOT EXISTS idx_completion_player_def ON " + TABLE_NAME + " (player_uuid, definition_key)",
                    "CREATE INDEX IF NOT EXISTS idx_qcl_player_time ON " + TABLE_NAME + " (player_uuid, completed_at)"
            };
            for (String sql : indexes) {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    /**
     * Records a quest completion for a player.
     *
     * @param connection    the database connection
     * @param playerUUID    the player who completed the quest
     * @param definitionKey the quest definition key (e.g. {@code "mcrpg:daily_mining"})
     * @param questUUID     the specific quest instance UUID
     * @param completedAt   the completion timestamp in epoch millis
     */
    public static void logCompletion(@NotNull Connection connection,
                                     @NotNull UUID playerUUID,
                                     @NotNull String definitionKey,
                                     @NotNull UUID questUUID,
                                     long completedAt) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + TABLE_NAME +
                        " (id, player_uuid, definition_key, quest_uuid, completed_at) " +
                        "VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, playerUUID.toString());
            statement.setString(3, definitionKey);
            statement.setString(4, questUUID.toString());
            statement.setLong(5, completedAt);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of times a player has completed a specific quest definition.
     *
     * @param connection    the database connection
     * @param playerUUID    the player UUID
     * @param definitionKey the quest definition key
     * @return the completion count
     */
    public static int getCompletionCount(@NotNull Connection connection,
                                         @NotNull UUID playerUUID,
                                         @NotNull String definitionKey) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + TABLE_NAME +
                        " WHERE player_uuid = ? AND definition_key = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, definitionKey);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Returns the timestamp (epoch millis) of the player's most recent completion
     * of the specified quest definition.
     *
     * @param connection    the database connection
     * @param playerUUID    the player UUID
     * @param definitionKey the quest definition key
     * @return an {@link OptionalLong} containing the last completion time, or empty if never completed
     */
    @NotNull
    public static OptionalLong getLastCompletionTime(@NotNull Connection connection,
                                                     @NotNull UUID playerUUID,
                                                     @NotNull String definitionKey) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT MAX(completed_at) FROM " + TABLE_NAME +
                        " WHERE player_uuid = ? AND definition_key = ?")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, definitionKey);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    long value = rs.getLong(1);
                    if (!rs.wasNull()) {
                        return OptionalLong.of(value);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return OptionalLong.empty();
    }

    /**
     * Convenience method to check if a player has ever completed a specific quest definition.
     *
     * @param connection    the database connection
     * @param playerUUID    the player UUID
     * @param definitionKey the quest definition key
     * @return {@code true} if the player has completed this quest at least once
     */
    public static boolean hasCompleted(@NotNull Connection connection,
                                       @NotNull UUID playerUUID,
                                       @NotNull String definitionKey) {
        return getCompletionCount(connection, playerUUID, definitionKey) > 0;
    }

    /**
     * Retrieves the full completion history for a player, ordered by completion date.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @param ascending  {@code true} for oldest-first, {@code false} for newest-first
     * @return an ordered list of completion records
     */
    @NotNull
    public static List<CompletionRecord> getCompletionHistory(@NotNull Connection connection,
                                                              @NotNull UUID playerUUID,
                                                              boolean ascending) {
        List<CompletionRecord> records = new ArrayList<>();
        String order = ascending ? "ASC" : "DESC";
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT definition_key, quest_uuid, completed_at FROM " + TABLE_NAME
                        + " WHERE player_uuid = ? ORDER BY completed_at " + order)) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    records.add(new CompletionRecord(
                            rs.getString("definition_key"),
                            UUID.fromString(rs.getString("quest_uuid")),
                            rs.getLong("completed_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}
