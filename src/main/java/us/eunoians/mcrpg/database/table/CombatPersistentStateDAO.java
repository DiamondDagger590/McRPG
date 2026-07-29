package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Static DAO for the generic key-value table that persists {@code PERSISTENT}-scoped combat state
 * across session boundaries. Follows the repo DAO pattern: static methods, {@link Connection} as
 * first argument, {@link #attemptCreateTable(Connection, Database)} for initialization,
 * {@link #updateTable(Connection)} for version-gated migrations via {@link TableVersionHistoryDAO},
 * {@link List}{@code <}{@link PreparedStatement}{@code >} returns for batch support.
 */
public final class CombatPersistentStateDAO {

    public static final String TABLE_NAME = "mcrpg_combat_persistent_state";
    private static final int CURRENT_TABLE_VERSION = 1;

    private CombatPersistentStateDAO() { }

    /**
     * Creates the table if it does not already exist.
     *
     * @param connection The database connection.
     * @param database   The database instance for existence checks.
     * @return {@code true} if the table was newly created, {@code false} if it already existed.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection,
                                              @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE `" + TABLE_NAME + "` (" +
                        "`entity_uuid` VARCHAR(36) NOT NULL," +
                        "`state_key` VARCHAR(256) NOT NULL," +
                        "`serialized_value` TEXT NOT NULL," +
                        "PRIMARY KEY (`entity_uuid`, `state_key`)" +
                        ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE,
                    "[CombatPersistentStateDAO] Failed to create table " + TABLE_NAME, e);
            return false;
        }
    }

    /**
     * Checks the live table version against {@link #CURRENT_TABLE_VERSION} and applies any
     * outstanding migrations, recording progress in {@link TableVersionHistoryDAO}. Called from
     * {@code McRPGDatabase}'s update-table pipeline, after {@link #attemptCreateTable} has run
     * for every DAO.
     *
     * @param connection The database connection.
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion < CURRENT_TABLE_VERSION) {
            if (lastStoredVersion == 0) {
                TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            }
        }
    }

    /**
     * Loads all persisted state for an entity.
     *
     * @param connection The database connection.
     * @param entityUUID The entity's UUID.
     * @return A map of state key (namespaced key string) to serialized value.
     */
    @NotNull
    public static Map<String, String> loadPersistentState(@NotNull Connection connection,
                                                           @NotNull UUID entityUUID) {
        Map<String, String> persistentState = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT state_key, serialized_value FROM " + TABLE_NAME + " WHERE entity_uuid = ?;")) {
            statement.setString(1, entityUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    persistentState.put(resultSet.getString("state_key"), resultSet.getString("serialized_value"));
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[CombatPersistentStateDAO] Failed to load persistent state for entity " + entityUUID, e);
        }
        return persistentState;
    }

    /**
     * Saves a single persistent state entry. Uses upsert semantics (INSERT ... ON CONFLICT
     * DO UPDATE) so it handles both creation and updates.
     *
     * @param connection      The database connection.
     * @param entityUUID      The entity's UUID.
     * @param stateKey        The namespaced key string.
     * @param serializedValue The serialized value.
     * @return A list containing the prepared statement for batch execution.
     */
    @NotNull
    public static List<PreparedStatement> savePersistentState(@NotNull Connection connection,
                                                               @NotNull UUID entityUUID,
                                                               @NotNull String stateKey,
                                                               @NotNull String serializedValue) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME + " (entity_uuid, state_key, serialized_value)" +
                            " VALUES (?, ?, ?)" +
                            " ON CONFLICT(entity_uuid, state_key) DO UPDATE SET serialized_value = excluded.serialized_value;");
            statement.setString(1, entityUUID.toString());
            statement.setString(2, stateKey);
            statement.setString(3, serializedValue);
            statements.add(statement);
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[CombatPersistentStateDAO] Failed to prepare savePersistentState statement for entity "
                            + entityUUID + " and key " + stateKey, e);
        }
        return statements;
    }

    /**
     * Deletes all persistent state for an entity. Used during cleanup.
     *
     * @param connection The database connection.
     * @param entityUUID The entity's UUID.
     * @return A list containing the prepared statement for batch execution.
     */
    @NotNull
    public static List<PreparedStatement> deleteAllForEntity(@NotNull Connection connection,
                                                              @NotNull UUID entityUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE entity_uuid = ?;");
            statement.setString(1, entityUUID.toString());
            statements.add(statement);
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[CombatPersistentStateDAO] Failed to prepare deleteAllForEntity statement for entity "
                            + entityUUID, e);
        }
        return statements;
    }
}
