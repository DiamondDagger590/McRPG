package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.log.CombatLogEntry;
import us.eunoians.mcrpg.combat.log.CombatLogPunishmentType;
import us.eunoians.mcrpg.combat.log.CombatLogPunishmentTypeRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Data Access Object for combat log audit trail persistence. Each record stores
 * who combat logged, when, where, the session's combat type, participants, and
 * which punishments were applied.
 */
public final class CombatLogDAO {

    public static final String TABLE_NAME = "combat_log";
    private static final int CURRENT_TABLE_VERSION = 2;

    private CombatLogDAO() {
    }

    /**
     * Creates the combat log table if it does not exist.
     *
     * @param connection The database connection.
     * @param database   The database instance for existence checks.
     * @return {@code true} if the table was newly created, {@code false} if it already existed.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE `" + TABLE_NAME + "` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "`player_uuid` VARCHAR(36) NOT NULL, " +
                        "`timestamp` BIGINT NOT NULL, " +
                        "`world` VARCHAR(64) NOT NULL, " +
                        "`x` DOUBLE NOT NULL, " +
                        "`y` DOUBLE NOT NULL, " +
                        "`z` DOUBLE NOT NULL, " +
                        "`combat_type` VARCHAR(16) NOT NULL, " +
                        "`participant_uuids` TEXT NOT NULL, " +
                        "`punishments_applied` TEXT NOT NULL" +
                        ");")) {
            statement.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE,
                    "[CombatLogDAO] Failed to create table " + TABLE_NAME, e);
            return false;
        }
    }

    /**
     * Applies any pending schema migrations for this table.
     *
     * @param connection The database connection.
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion < 1) {
            createIndex(connection, "idx_combat_log_player_time", "(player_uuid, timestamp)");
        }
        if (lastStoredVersion < 2) {
            // Serves deleteOlderThan's `WHERE timestamp < ?` retention sweep — the composite
            // index above can't be used for a range scan that doesn't lead with player_uuid.
            createIndex(connection, "idx_combat_log_timestamp", "(timestamp)");
        }
        TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, CURRENT_TABLE_VERSION);
    }

    /**
     * Creates an index on {@link #TABLE_NAME} if it does not already exist.
     *
     * @param connection  The database connection.
     * @param indexName   The name of the index to create.
     * @param columnsExpr The parenthesized column list for the index, e.g. {@code "(timestamp)"}.
     */
    private static void createIndex(@NotNull Connection connection, @NotNull String indexName,
                                    @NotNull String columnsExpr) {
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE INDEX IF NOT EXISTS " + indexName + " ON " + TABLE_NAME + " " + columnsExpr)) {
            statement.executeUpdate();
        }
        catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE,
                    "[CombatLogDAO] Failed to create index " + indexName + " on " + TABLE_NAME, e);
        }
    }

    /**
     * Creates a prepared statement for inserting a combat log entry.
     *
     * @param connection The database connection.
     * @param entry      The combat log entry to insert.
     * @return A list containing the prepared insert statement.
     * @throws SQLException If statement creation fails.
     */
    @NotNull
    public static List<PreparedStatement> insertCombatLog(@NotNull Connection connection,
                                                          @NotNull CombatLogEntry entry) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO " + TABLE_NAME
                        + " (player_uuid, timestamp, world, x, y, z, combat_type, participant_uuids, punishments_applied)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, entry.playerUUID().toString());
        statement.setLong(2, entry.timestamp().toEpochMilli());
        statement.setString(3, entry.world());
        statement.setDouble(4, entry.x());
        statement.setDouble(5, entry.y());
        statement.setDouble(6, entry.z());
        statement.setString(7, entry.combatType().name());
        statement.setString(8, entry.participantUUIDs().stream()
                .map(UUID::toString)
                .collect(Collectors.joining(",")));
        statement.setString(9, entry.punishmentsApplied().stream()
                .map(type -> type.getKey().toString())
                .collect(Collectors.joining(",")));
        return List.of(statement);
    }

    /**
     * Queries paginated combat log history for a player.
     *
     * @param connection The database connection.
     * @param playerUUID The UUID of the player to query.
     * @param page       The page number (1-indexed).
     * @param pageSize   The number of entries per page.
     * @return A list of combat log entries, newest first.
     */
    @NotNull
    public static List<CombatLogEntry> getCombatLogHistory(@NotNull Connection connection,
                                                           @NotNull UUID playerUUID,
                                                           int page, int pageSize) {
        String sql = "SELECT id, player_uuid, timestamp, world, x, y, z, combat_type, "
                + "participant_uuids, punishments_applied FROM " + TABLE_NAME
                + " WHERE player_uuid = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        List<CombatLogEntry> entries = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            statement.setInt(2, pageSize);
            statement.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // Isolated per row: a single corrupted/unparsable row (bad enum literal, malformed
                    // UUID) must not blank out the rest of an otherwise-valid page for the admin.
                    try {
                        entries.add(parseEntry(rs));
                    }
                    catch (RuntimeException e) {
                        McRPG.getInstance().getLogger().log(Level.WARNING,
                                "[CombatLogDAO] Skipping unparsable combat log row for " + playerUUID, e);
                    }
                }
            }
        }
        catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[CombatLogDAO] Failed to query combat log history for " + playerUUID, e);
        }
        return entries;
    }

    /**
     * Counts the total number of combat log entries for a player.
     *
     * @param connection The database connection.
     * @param playerUUID The UUID of the player to count.
     * @return The total entry count.
     */
    public static int getCombatLogCount(@NotNull Connection connection, @NotNull UUID playerUUID) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE player_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[CombatLogDAO] Failed to count combat log entries for " + playerUUID, e);
        }
        return 0;
    }

    /**
     * Deletes combat log entries older than the given cutoff timestamp.
     *
     * @param connection The database connection.
     * @param cutoff     Entries with a timestamp before this instant are deleted.
     * @return The number of rows deleted.
     */
    public static int deleteOlderThan(@NotNull Connection connection, @NotNull Instant cutoff) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE timestamp < ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cutoff.toEpochMilli());
            return statement.executeUpdate();
        }
        catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[CombatLogDAO] Failed to delete expired combat log entries", e);
            return 0;
        }
    }

    /**
     * Parses a {@link CombatLogEntry} from a result set row.
     *
     * @param rs The result set positioned at the current row.
     * @return The parsed entry.
     * @throws SQLException If column reading fails.
     */
    @NotNull
    private static CombatLogEntry parseEntry(@NotNull ResultSet rs) throws SQLException {
        String participantString = rs.getString("participant_uuids");
        List<UUID> participantUUIDs = participantString.isEmpty()
                ? Collections.emptyList()
                : Arrays.stream(participantString.split(","))
                        .map(UUID::fromString)
                        .toList();

        String punishmentString = rs.getString("punishments_applied");
        CombatLogPunishmentTypeRegistry punishmentRegistry = McRPG.getInstance().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_LOG_PUNISHMENT_TYPE);
        List<CombatLogPunishmentType> punishments = punishmentString.isEmpty()
                ? Collections.emptyList()
                : Arrays.stream(punishmentString.split(","))
                        .map(NamespacedKey::fromString)
                        .filter(Objects::nonNull)
                        .map(punishmentRegistry::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

        return new CombatLogEntry(
                rs.getLong("id"),
                UUID.fromString(rs.getString("player_uuid")),
                Instant.ofEpochMilli(rs.getLong("timestamp")),
                rs.getString("world"),
                rs.getDouble("x"),
                rs.getDouble("y"),
                rs.getDouble("z"),
                CombatType.valueOf(rs.getString("combat_type")),
                participantUUIDs,
                punishments
        );
    }
}
