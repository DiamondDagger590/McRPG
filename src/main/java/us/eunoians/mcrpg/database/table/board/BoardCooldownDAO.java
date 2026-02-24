package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import us.eunoians.mcrpg.McRPG;
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

/**
 * DAO for the {@code mcrpg_board_cooldown} table.
 */
public class BoardCooldownDAO {

    static final String TABLE_NAME = "mcrpg_board_cooldown";
    private static final int CURRENT_TABLE_VERSION = 1;

    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`cooldown_id` VARCHAR(36) NOT NULL," +
                "`cooldown_type` VARCHAR(64) NOT NULL," +
                "`scope_type` VARCHAR(64) NOT NULL," +
                "`scope_identifier` VARCHAR(255) NOT NULL," +
                "`quest_definition_key` VARCHAR(256)," +
                "`category_key` VARCHAR(256)," +
                "`expires_at` BIGINT NOT NULL," +
                "PRIMARY KEY (`cooldown_id`)" +
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
                    "CREATE INDEX IF NOT EXISTS idx_cooldown_scope ON " + TABLE_NAME + " (scope_type, scope_identifier)",
                    "CREATE INDEX IF NOT EXISTS idx_cooldown_expires ON " + TABLE_NAME + " (expires_at)"
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
    public static List<PreparedStatement> saveCooldown(@NotNull Connection connection,
                                                        @NotNull String cooldownId,
                                                        @NotNull String cooldownType,
                                                        @NotNull String scopeType,
                                                        @NotNull String scopeIdentifier,
                                                        @Nullable NamespacedKey questDefinitionKey,
                                                        @Nullable NamespacedKey categoryKey,
                                                        long expiresAt) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (cooldown_id, cooldown_type, scope_type, scope_identifier," +
                            " quest_definition_key, category_key, expires_at)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(cooldown_id) DO UPDATE SET" +
                            " expires_at = excluded.expires_at");
            ps.setString(1, cooldownId);
            ps.setString(2, cooldownType);
            ps.setString(3, scopeType);
            ps.setString(4, scopeIdentifier);
            if (questDefinitionKey != null) {
                ps.setString(5, questDefinitionKey.toString());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }
            if (categoryKey != null) {
                ps.setString(6, categoryKey.toString());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            ps.setLong(7, expiresAt);
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    public static boolean isOnCooldown(@NotNull Connection connection,
                                        @NotNull String cooldownType,
                                        @NotNull String scopeType,
                                        @NotNull String scopeIdentifier,
                                        @Nullable NamespacedKey questDefinitionKey,
                                        @Nullable NamespacedKey categoryKey) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM " + TABLE_NAME +
                " WHERE cooldown_type = ? AND scope_type = ? AND scope_identifier = ? AND expires_at > ?");
        if (questDefinitionKey != null) {
            sql.append(" AND quest_definition_key = ?");
        }
        if (categoryKey != null) {
            sql.append(" AND category_key = ?");
        }
        sql.append(" LIMIT 1");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setString(idx++, cooldownType);
            ps.setString(idx++, scopeType);
            ps.setString(idx++, scopeIdentifier);
            ps.setLong(idx++, McRPG.getInstance().getTimeProvider().now().toEpochMilli());
            if (questDefinitionKey != null) {
                ps.setString(idx++, questDefinitionKey.toString());
            }
            if (categoryKey != null) {
                ps.setString(idx, categoryKey.toString());
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @NotNull
    public static List<PreparedStatement> pruneExpiredCooldowns(@NotNull Connection connection) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE expires_at <= ?");
            ps.setLong(1, McRPG.getInstance().getTimeProvider().now().toEpochMilli());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }
}
