package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardRotation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_quest_board_rotation} table.
 */
public class BoardRotationDAO {

    static final String TABLE_NAME = "mcrpg_quest_board_rotation";
    private static final int CURRENT_TABLE_VERSION = 1;

    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`rotation_id` VARCHAR(36) NOT NULL," +
                "`board_key` VARCHAR(256) NOT NULL," +
                "`refresh_type_key` VARCHAR(256) NOT NULL," +
                "`rotation_epoch` BIGINT NOT NULL," +
                "`started_at` BIGINT NOT NULL," +
                "`expires_at` BIGINT NOT NULL," +
                "PRIMARY KEY (`rotation_id`)" +
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
                    "CREATE INDEX IF NOT EXISTS idx_rotation_board_key ON " + TABLE_NAME + " (board_key)",
                    "CREATE INDEX IF NOT EXISTS idx_rotation_expires ON " + TABLE_NAME + " (expires_at)"
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

    @NotNull
    public static List<PreparedStatement> saveRotation(@NotNull Connection connection,
                                                        @NotNull BoardRotation rotation) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (rotation_id, board_key, refresh_type_key, rotation_epoch, started_at, expires_at)" +
                            " VALUES (?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(rotation_id) DO UPDATE SET" +
                            " rotation_epoch = excluded.rotation_epoch," +
                            " started_at = excluded.started_at," +
                            " expires_at = excluded.expires_at");
            ps.setString(1, rotation.getRotationId().toString());
            ps.setString(2, rotation.getBoardKey().toString());
            ps.setString(3, rotation.getRefreshTypeKey().toString());
            ps.setLong(4, rotation.getRotationEpoch());
            ps.setLong(5, rotation.getStartedAt());
            ps.setLong(6, rotation.getExpiresAt());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    @NotNull
    public static Optional<BoardRotation> loadCurrentRotation(@NotNull Connection connection,
                                                               @NotNull NamespacedKey boardKey,
                                                               @NotNull NamespacedKey refreshTypeKey) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT rotation_id, rotation_epoch, started_at, expires_at FROM " + TABLE_NAME +
                        " WHERE board_key = ? AND refresh_type_key = ? ORDER BY started_at DESC LIMIT 1")) {
            ps.setString(1, boardKey.toString());
            ps.setString(2, refreshTypeKey.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new BoardRotation(
                            UUID.fromString(rs.getString("rotation_id")),
                            boardKey,
                            refreshTypeKey,
                            rs.getLong("rotation_epoch"),
                            rs.getLong("started_at"),
                            rs.getLong("expires_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @NotNull
    public static Optional<BoardRotation> loadRotationById(@NotNull Connection connection,
                                                            @NotNull UUID rotationId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT board_key, refresh_type_key, rotation_epoch, started_at, expires_at FROM " + TABLE_NAME +
                        " WHERE rotation_id = ?")) {
            ps.setString(1, rotationId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new BoardRotation(
                            rotationId,
                            NamespacedKey.fromString(rs.getString("board_key")),
                            NamespacedKey.fromString(rs.getString("refresh_type_key")),
                            rs.getLong("rotation_epoch"),
                            rs.getLong("started_at"),
                            rs.getLong("expires_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
