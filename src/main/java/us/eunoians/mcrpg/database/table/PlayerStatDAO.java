package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import us.eunoians.mcrpg.McRPG;

import java.util.logging.Level;

/**
 * DAO for persisting per-player resource pool stat values (e.g. mana, health).
 * <p>
 * Stores a single {@code current_value} per {@code (player_uuid, stat_key)} pair.
 * The DAO is stat-agnostic — callers decide which stats to persist; this class does not
 * hardcode mana or any other specific stat. On first login (no rows) the player's pool
 * initialises to the stat definition's base value.
 * <p>
 * All methods are static and must be called from an async thread. The connection is
 * never closed by this DAO — callers own the connection lifecycle.
 *
 * <p>Table schema ({@code mcrpg_player_stat}):
 * <pre>
 *   player_uuid   TEXT  NOT NULL
 *   stat_key      TEXT  NOT NULL   -- NamespacedKey as string, e.g. "mcrpg:mana"
 *   current_value REAL  NOT NULL
 *   PRIMARY KEY (player_uuid, stat_key)
 * </pre>
 */
public final class PlayerStatDAO {

    private static final String TABLE_NAME = "mcrpg_player_stat";

    private PlayerStatDAO() {}

    /**
     * Creates the player stat table if it does not already exist.
     *
     * @param connection The {@link Connection} to use.
     * @param database   The {@link Database} for dialect selection.
     * @return {@code true} if the table was newly created, {@code false} if it already existed.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE `" + TABLE_NAME + "` (" +
                        "`player_uuid` varchar(36) NOT NULL," +
                        "`stat_key` varchar(128) NOT NULL," +
                        "`current_value` REAL NOT NULL DEFAULT 0," +
                        "PRIMARY KEY (`player_uuid`, `stat_key`)" +
                        ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE, "Failed to create table " + TABLE_NAME, e);
            return false;
        }
    }

    /**
     * No-op for schema version 1. Future migrations go here.
     *
     * @param connection The {@link Connection} to use.
     * @param database   The {@link Database} for dialect selection.
     */
    public static void updateTable(@NotNull Connection connection, @NotNull Database database) {
        // v1 — no migrations yet
    }

    /**
     * Returns a {@link PreparedStatement} that upserts the current value for a single stat.
     * Uses {@code REPLACE INTO} semantics so subsequent calls overwrite prior rows.
     * <p>
     * The caller is responsible for closing the statement after execution.
     *
     * @param connection   The {@link Connection} to use.
     * @param playerUUID   The player's {@link UUID}.
     * @param statKey      The stat key (e.g. {@code McRPGPlayerStat.MANA.getKey()}).
     * @param currentValue The current value to persist.
     * @return The prepared statement ready for execution.
     * @throws SQLException if the statement cannot be prepared.
     */
    @NotNull
    public static PreparedStatement saveStat(@NotNull Connection connection,
                                             @NotNull UUID playerUUID,
                                             @NotNull NamespacedKey statKey,
                                             double currentValue) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "REPLACE INTO `" + TABLE_NAME + "` (player_uuid, stat_key, current_value) VALUES (?, ?, ?);");
        statement.setString(1, playerUUID.toString());
        statement.setString(2, statKey.toString());
        statement.setDouble(3, currentValue);
        return statement;
    }

    /**
     * Batch-saves multiple stats for a player.
     * Returns one {@link PreparedStatement} per entry; callers must execute and close each.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The player's {@link UUID}.
     * @param stats      Map of stat key → current value.
     * @return The list of prepared statements (one per stat entry).
     * @throws SQLException if any statement cannot be prepared.
     */
    @NotNull
    public static List<PreparedStatement> saveStats(@NotNull Connection connection,
                                                    @NotNull UUID playerUUID,
                                                    @NotNull Map<NamespacedKey, Double> stats) throws SQLException {
        List<PreparedStatement> statements = new ArrayList<>(stats.size());
        for (Map.Entry<NamespacedKey, Double> entry : stats.entrySet()) {
            statements.add(saveStat(connection, playerUUID, entry.getKey(), entry.getValue()));
        }
        return statements;
    }

    /**
     * Loads the persisted current value for a single stat key.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The player's {@link UUID}.
     * @param statKey    The stat key to load.
     * @return The persisted value, or empty if no row exists.
     */
    @NotNull
    public static Optional<Double> loadStat(@NotNull Connection connection,
                                            @NotNull UUID playerUUID,
                                            @NotNull NamespacedKey statKey) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT current_value FROM `" + TABLE_NAME + "` WHERE player_uuid = ? AND stat_key = ?;")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, statKey.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(resultSet.getDouble("current_value"));
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE, "Failed to load stat " + statKey + " for " + playerUUID, e);
        }
        return Optional.empty();
    }

    /**
     * Loads all persisted stat values for a player.
     *
     * @param connection The {@link Connection} to use.
     * @param playerUUID The player's {@link UUID}.
     * @return A map of stat key → current value. Empty if no rows exist.
     */
    @NotNull
    public static Map<NamespacedKey, Double> loadAllStats(@NotNull Connection connection,
                                                          @NotNull UUID playerUUID) {
        Map<NamespacedKey, Double> result = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT stat_key, current_value FROM `" + TABLE_NAME + "` WHERE player_uuid = ?;")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String keyString = resultSet.getString("stat_key");
                    NamespacedKey key = NamespacedKey.fromString(keyString);
                    if (key != null) {
                        result.put(key, resultSet.getDouble("current_value"));
                    }
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE, "Failed to load stats for " + playerUUID, e);
        }
        return result;
    }
}
