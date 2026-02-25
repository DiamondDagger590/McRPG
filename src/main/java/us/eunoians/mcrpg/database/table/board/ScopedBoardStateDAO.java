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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for persisting scoped board state -- tracks the lifecycle of scoped offerings
 * per scope entity (e.g., per land, per faction).
 * <p>
 * Generic and not land-specific. Uses {@code scope_entity_id} as the entity identifier
 * (land name, faction name, party ID, etc.).
 */
public class ScopedBoardStateDAO {

    public static final String TABLE_NAME = "mcrpg_scoped_board_state";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Immutable record representing one row from the scoped board state table.
     *
     * @param scopeEntityId    the identifier for the scoped entity (e.g., land name)
     * @param scopeProviderKey the {@link NamespacedKey} of the scope provider that owns this entity (e.g., Lands adapter key)
     * @param boardKey         the {@link NamespacedKey} of the board this offering belongs to
     * @param offeringId       the unique offering identifier
     * @param state            the offering state (e.g., "VISIBLE", "ACCEPTED", "COMPLETED", "EXPIRED")
     * @param acceptedAt       the epoch millis timestamp when the offering was accepted, or {@code null} if not yet accepted
     * @param acceptedBy       the UUID of the player who accepted this offering, or {@code null} if not yet accepted
     * @param questInstanceUUID the quest instance UUID tied to this acceptance, or {@code null} if none
     */
    public record ScopedBoardStateRecord(
            @NotNull String scopeEntityId,
            @NotNull NamespacedKey scopeProviderKey,
            @NotNull NamespacedKey boardKey,
            @NotNull UUID offeringId,
            @NotNull String state,
            @Nullable Long acceptedAt,
            @Nullable UUID acceptedBy,
            @Nullable UUID questInstanceUUID
    ) {}

    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`scope_entity_id` VARCHAR(255) NOT NULL, " +
                "`scope_provider_key` VARCHAR(256) NOT NULL, " +
                "`board_key` VARCHAR(256) NOT NULL, " +
                "`offering_id` VARCHAR(36) NOT NULL, " +
                "`state` VARCHAR(32) NOT NULL, " +
                "`accepted_at` BIGINT, " +
                "`accepted_by` VARCHAR(36), " +
                "`quest_instance_uuid` VARCHAR(36), " +
                "PRIMARY KEY (`scope_entity_id`, `board_key`, `offering_id`)" +
                ");")) {
            statement.executeUpdate();
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
                    "CREATE INDEX IF NOT EXISTS idx_scoped_board_state_entity ON " + TABLE_NAME + " (scope_entity_id, board_key)",
                    "CREATE INDEX IF NOT EXISTS idx_scoped_board_state_quest ON " + TABLE_NAME + " (quest_instance_uuid)"
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
     * Saves or updates a scoped board state record.
     *
     * @return list of prepared statements for batching
     */
    @NotNull
    public static List<PreparedStatement> saveState(@NotNull Connection connection,
                                                    @NotNull String entityId,
                                                    @NotNull NamespacedKey scopeProviderKey,
                                                    @NotNull NamespacedKey boardKey,
                                                    @NotNull UUID offeringId,
                                                    @NotNull String state,
                                                    @Nullable Long acceptedAt,
                                                    @Nullable UUID acceptedBy,
                                                    @Nullable UUID questInstanceUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (scope_entity_id, scope_provider_key, board_key, offering_id, state, accepted_at, accepted_by, quest_instance_uuid) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT (scope_entity_id, board_key, offering_id) DO UPDATE SET " +
                            "state = excluded.state, accepted_at = excluded.accepted_at, " +
                            "accepted_by = excluded.accepted_by, quest_instance_uuid = excluded.quest_instance_uuid");
            ps.setString(1, entityId);
            ps.setString(2, scopeProviderKey.toString());
            ps.setString(3, boardKey.toString());
            ps.setString(4, offeringId.toString());
            ps.setString(5, state);
            if (acceptedAt != null) {
                ps.setLong(6, acceptedAt);
            } else {
                ps.setNull(6, java.sql.Types.BIGINT);
            }
            ps.setString(7, acceptedBy != null ? acceptedBy.toString() : null);
            ps.setString(8, questInstanceUUID != null ? questInstanceUUID.toString() : null);
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Loads all scoped board state records for a given entity and board.
     */
    @NotNull
    public static List<ScopedBoardStateRecord> loadStatesForEntity(@NotNull Connection connection,
                                                                    @NotNull String entityId,
                                                                    @NotNull NamespacedKey boardKey) {
        List<ScopedBoardStateRecord> records = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE scope_entity_id = ? AND board_key = ?")) {
            ps.setString(1, entityId);
            ps.setString(2, boardKey.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Counts the number of active (ACCEPTED) scoped quests for a given entity and board.
     */
    public static int countActiveQuestsForEntity(@NotNull Connection connection,
                                                  @NotNull String entityId,
                                                  @NotNull NamespacedKey boardKey) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE scope_entity_id = ? AND board_key = ? AND state = 'ACCEPTED'")) {
            ps.setString(1, entityId);
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

    /**
     * Updates the state of a specific scoped board entry.
     */
    @NotNull
    public static PreparedStatement updateState(@NotNull Connection connection,
                                                @NotNull String entityId,
                                                @NotNull NamespacedKey boardKey,
                                                @NotNull UUID offeringId,
                                                @NotNull String state) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE " + TABLE_NAME + " SET state = ? WHERE scope_entity_id = ? AND board_key = ? AND offering_id = ?");
        ps.setString(1, state);
        ps.setString(2, entityId);
        ps.setString(3, boardKey.toString());
        ps.setString(4, offeringId.toString());
        return ps;
    }

    /**
     * Deletes all scoped board state records for a given entity (used on entity removal).
     */
    @NotNull
    public static List<PreparedStatement> deleteStatesForEntity(@NotNull Connection connection,
                                                                 @NotNull String entityId) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE scope_entity_id = ?");
            ps.setString(1, entityId);
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Loads all ACCEPTED states for a given entity (across all boards).
     * Used during entity removal to identify quests that need cancellation.
     */
    @NotNull
    public static List<ScopedBoardStateRecord> loadAcceptedStatesForEntity(@NotNull Connection connection,
                                                                           @NotNull String entityId) {
        List<ScopedBoardStateRecord> records = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE scope_entity_id = ? AND state = 'ACCEPTED'")) {
            ps.setString(1, entityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    records.add(fromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @NotNull
    private static ScopedBoardStateRecord fromResultSet(@NotNull ResultSet rs) throws SQLException {
        String acceptedByStr = rs.getString("accepted_by");
        String questUuidStr = rs.getString("quest_instance_uuid");
        long acceptedAtLong = rs.getLong("accepted_at");

        return new ScopedBoardStateRecord(
                rs.getString("scope_entity_id"),
                NamespacedKey.fromString(rs.getString("scope_provider_key")),
                NamespacedKey.fromString(rs.getString("board_key")),
                UUID.fromString(rs.getString("offering_id")),
                rs.getString("state"),
                rs.wasNull() ? null : acceptedAtLong,
                acceptedByStr != null ? UUID.fromString(acceptedByStr) : null,
                questUuidStr != null ? UUID.fromString(questUuidStr) : null
        );
    }
}
