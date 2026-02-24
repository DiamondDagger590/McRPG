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
                "`player_uuid` TEXT NOT NULL," +
                "`board_key` TEXT NOT NULL," +
                "`offering_id` TEXT NOT NULL," +
                "`state` TEXT NOT NULL," +
                "`accepted_at` BIGINT," +
                "`quest_instance_uuid` TEXT," +
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
        TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, CURRENT_TABLE_VERSION);
    }

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
