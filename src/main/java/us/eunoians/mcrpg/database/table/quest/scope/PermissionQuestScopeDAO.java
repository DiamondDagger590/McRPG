package us.eunoians.mcrpg.database.table.quest.scope;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.scope.impl.PermissionQuestScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for persisting {@link PermissionQuestScope} data.
 * Stores the mapping between a quest instance and its required permission node.
 */
public class PermissionQuestScopeDAO {

    private static final String TABLE_NAME = "mcrpg_permission_quest_scope";
    private static final int CURRENT_TABLE_VERSION = 1;

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
                "`quest_uuid` VARCHAR(36) NOT NULL, " +
                "`permission_node` VARCHAR(255) NOT NULL, " +
                "PRIMARY KEY (`quest_uuid`)" +
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
     * Gets the permission node for a quest scope.
     *
     * @param connection the database connection
     * @param questUUID  the quest instance UUID
     * @return the permission node string
     * @throws IllegalStateException if no permission scope is found for the quest
     */
    @NotNull
    public static String getPermissionNode(@NotNull Connection connection, @NotNull UUID questUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT permission_node FROM " + TABLE_NAME + " WHERE quest_uuid = ?")) {
            statement.setString(1, questUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("permission_node");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalStateException("No permission scope found for quest " + questUUID);
    }

    /**
     * Loads all active permission-scoped quests, returning a map of quest UUID to permission node.
     * Active means the quest state is {@code NOT_STARTED} or {@code IN_PROGRESS}.
     * Callers filter the results by checking permission against the target player.
     *
     * @param connection the database connection
     * @return a map of quest UUID to permission node for all active permission-scoped quests
     */
    @NotNull
    public static Map<UUID, String> findAllActivePermissionQuests(@NotNull Connection connection) {
        Map<UUID, String> results = new LinkedHashMap<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT pqs.quest_uuid, pqs.permission_node FROM " + TABLE_NAME + " pqs " +
                        "JOIN mcrpg_quest_instances qi ON pqs.quest_uuid = qi.quest_uuid " +
                        "WHERE qi.state IN ('NOT_STARTED', 'IN_PROGRESS')")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.put(
                            UUID.fromString(rs.getString("quest_uuid")),
                            rs.getString("permission_node")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Generates the prepared statements to save a permission quest scope.
     *
     * @param connection the database connection
     * @param scope      the permission quest scope to save
     * @return the list of prepared statements
     */
    @NotNull
    public static List<PreparedStatement> saveScope(@NotNull Connection connection, @NotNull PermissionQuestScope scope) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME + " (quest_uuid, permission_node) VALUES (?, ?) " +
                            "ON CONFLICT (quest_uuid) DO UPDATE SET permission_node = excluded.permission_node");
            statement.setString(1, scope.getQuestUUID().toString());
            statement.setString(2, scope.getPermissionNode().orElseThrow());
            statements.add(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }
}
