package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_personal_offering_tracking} table.
 * <p>
 * Tracks whether personal offerings have been generated for a player/rotation
 * combination to avoid regeneration on subsequent board opens.
 */
public class PersonalOfferingTrackingDAO {

    static final String TABLE_NAME = "mcrpg_personal_offering_tracking";
    private static final int CURRENT_TABLE_VERSION = 1;

    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`player_uuid` VARCHAR(36) NOT NULL," +
                "`board_key` VARCHAR(256) NOT NULL," +
                "`rotation_id` VARCHAR(36) NOT NULL," +
                "`generated_at` BIGINT NOT NULL," +
                "PRIMARY KEY (`player_uuid`, `board_key`, `rotation_id`)" +
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
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE INDEX IF NOT EXISTS idx_tracking_rotation ON " + TABLE_NAME + " (rotation_id)")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    @NotNull
    public static List<PreparedStatement> markGenerated(@NotNull Connection connection,
                                                         @NotNull UUID playerUUID,
                                                         @NotNull NamespacedKey boardKey,
                                                         @NotNull UUID rotationId) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (player_uuid, board_key, rotation_id, generated_at)" +
                            " VALUES (?, ?, ?, ?)" +
                            " ON CONFLICT(player_uuid, board_key, rotation_id) DO NOTHING");
            ps.setString(1, playerUUID.toString());
            ps.setString(2, boardKey.toString());
            ps.setString(3, rotationId.toString());
            ps.setLong(4, System.currentTimeMillis());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    public static boolean hasGenerated(@NotNull Connection connection,
                                       @NotNull UUID playerUUID,
                                       @NotNull NamespacedKey boardKey,
                                       @NotNull UUID rotationId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM " + TABLE_NAME +
                        " WHERE player_uuid = ? AND board_key = ? AND rotation_id = ?")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, boardKey.toString());
            ps.setString(3, rotationId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @NotNull
    public static List<PreparedStatement> pruneForExpiredRotations(@NotNull Connection connection,
                                                                    @NotNull List<UUID> expiredRotationIds) {
        List<PreparedStatement> statements = new ArrayList<>();
        for (UUID rotationId : expiredRotationIds) {
            try {
                PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM " + TABLE_NAME + " WHERE rotation_id = ?");
                ps.setString(1, rotationId.toString());
                statements.add(ps);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return statements;
    }
}
