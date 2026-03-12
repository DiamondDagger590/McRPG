package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_player_board_state} table.
 */
public class PlayerBoardStateDAO {

    static final String TABLE_NAME = "mcrpg_player_board_state";
    private static final int CURRENT_TABLE_VERSION = 1;

    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`player_uuid` VARCHAR(36) NOT NULL," +
                "`board_key` VARCHAR(256) NOT NULL," +
                "`offering_id` VARCHAR(36) NOT NULL," +
                "`state` VARCHAR(32) NOT NULL," +
                "`accepted_at` BIGINT," +
                "`quest_instance_uuid` VARCHAR(36)," +
                "PRIMARY KEY (`player_uuid`, `board_key`, `offering_id`)" +
                ");")) {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion == 0) {
            String[] indexes = {
                    "CREATE INDEX IF NOT EXISTS idx_pbs_player_board_state ON " + TABLE_NAME + " (player_uuid, board_key, state)",
                    "CREATE INDEX IF NOT EXISTS idx_pbs_offering ON " + TABLE_NAME + " (offering_id)"
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
     * Generates prepared statements to save (upsert) a player's board state for a specific offering.
     * If a row with the same composite key ({@code playerUUID}, {@code boardKey}, {@code offeringId})
     * already exists, its state, accepted timestamp, and quest instance UUID are updated.
     *
     * @param connection       the database connection
     * @param playerUUID       the player's UUID
     * @param boardKey         the board this offering belongs to
     * @param offeringId       the unique offering identifier
     * @param state            the offering state (e.g., "VISIBLE", "ACCEPTED", "ABANDONED")
     * @param acceptedAt       the epoch millis timestamp when the offering was accepted, or {@code null} if not yet accepted
     * @param questInstanceUUID the quest instance UUID tied to this acceptance, or {@code null} if none
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveState(@NotNull Connection connection,
                                                     @NotNull UUID playerUUID,
                                                     @NotNull NamespacedKey boardKey,
                                                     @NotNull UUID offeringId,
                                                     @NotNull String state,
                                                     @Nullable Long acceptedAt,
                                                     @Nullable UUID questInstanceUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (player_uuid, board_key, offering_id, state, accepted_at, quest_instance_uuid)" +
                            " VALUES (?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(player_uuid, board_key, offering_id) DO UPDATE SET" +
                            " state = excluded.state," +
                            " accepted_at = excluded.accepted_at," +
                            " quest_instance_uuid = excluded.quest_instance_uuid");
            ps.setString(1, playerUUID.toString());
            ps.setString(2, boardKey.toString());
            ps.setString(3, offeringId.toString());
            ps.setString(4, state);
            if (acceptedAt != null) {
                ps.setLong(5, acceptedAt);
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            if (questInstanceUUID != null) {
                ps.setString(6, questInstanceUUID.toString());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Deletes all player board state records for the given player.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return the number of deleted rows
     */
    public static int deleteForPlayer(@NotNull Connection connection, @NotNull UUID playerUUID) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            ps.setString(1, playerUUID.toString());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Loads all ACCEPTED personal board state records for the given player (to find quests to cancel).
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return list of offering IDs and quest instance UUIDs in ACCEPTED state
     */
    @NotNull
    public static List<AcceptedBoardEntry> loadAcceptedForPlayer(@NotNull Connection connection,
                                                                  @NotNull UUID playerUUID) {
        List<AcceptedBoardEntry> entries = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT offering_id, quest_instance_uuid FROM " + TABLE_NAME +
                        " WHERE player_uuid = ? AND state = 'ACCEPTED'")) {
            ps.setString(1, playerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String questStr = rs.getString("quest_instance_uuid");
                    entries.add(new AcceptedBoardEntry(
                            UUID.fromString(rs.getString("offering_id")),
                            questStr != null ? UUID.fromString(questStr) : null
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Immutable record representing an accepted personal board offering.
     *
     * @param offeringId        the unique offering identifier
     * @param questInstanceUUID the quest instance UUID associated with this acceptance, or {@code null} if none
     */
    public record AcceptedBoardEntry(@NotNull UUID offeringId, @Nullable UUID questInstanceUUID) {}

    /**
     * Updates the state of a player board entry identified by the quest instance UUID.
     * Used when a quest is completed or cancelled to release the board slot.
     *
     * @param connection        the database connection
     * @param questInstanceUUID the quest instance UUID to match
     * @param newState          the new state (e.g., "COMPLETED", "CANCELLED")
     * @return the number of rows updated
     */
    public static int updateStateByQuestInstanceUUID(@NotNull Connection connection,
                                                     @NotNull UUID questInstanceUUID,
                                                     @NotNull String newState) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE " + TABLE_NAME + " SET state = ? WHERE quest_instance_uuid = ?")) {
            ps.setString(1, newState);
            ps.setString(2, questInstanceUUID.toString());
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Bulk-updates all {@code ACCEPTED} board state rows whose linked quest instance has already
     * been cancelled in the quest instances table (e.g. by {@code bulkExpireStaleQuests}).
     * This keeps the board count consistent for quests that expired while unloaded.
     *
     * @param connection the database connection
     * @return the number of rows updated
     */
    public static int bulkCancelExpiredBoardStates(@NotNull Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE " + TABLE_NAME + " SET state = 'CANCELLED'" +
                        " WHERE state = 'ACCEPTED'" +
                        " AND quest_instance_uuid IN (" +
                        "   SELECT quest_uuid FROM mcrpg_quest_instances WHERE state = 'CANCELLED'" +
                        " )")) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Counts the number of offerings currently in the {@code ACCEPTED} state for a given player
     * on a specific board. Used to enforce maximum active quest limits.
     *
     * @param connection the database connection
     * @param playerUUID the player's UUID
     * @param boardKey   the board to count accepted offerings for
     * @return the number of active (accepted) offerings
     */
    public static int countActiveQuestsFromBoard(@NotNull Connection connection,
                                                  @NotNull UUID playerUUID,
                                                  @NotNull NamespacedKey boardKey) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + TABLE_NAME +
                        " WHERE player_uuid = ? AND board_key = ? AND state = 'ACCEPTED'")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, boardKey.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
