package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.BoardOffering;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_board_offering} table.
 */
public class BoardOfferingDAO {

    static final String TABLE_NAME = "mcrpg_board_offering";
    private static final int CURRENT_TABLE_VERSION = 1;

    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`offering_id` TEXT NOT NULL," +
                "`rotation_id` TEXT NOT NULL," +
                "`category_key` TEXT NOT NULL," +
                "`slot_index` INTEGER NOT NULL," +
                "`quest_definition_key` TEXT," +
                "`rarity_key` TEXT NOT NULL," +
                "`scope_target_id` TEXT," +
                "`state` TEXT NOT NULL," +
                "`accepted_at` BIGINT," +
                "`quest_instance_uuid` TEXT," +
                "`completion_time_ms` BIGINT NOT NULL," +
                "PRIMARY KEY (`offering_id`)" +
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
    public static List<PreparedStatement> saveOffering(@NotNull Connection connection,
                                                        @NotNull BoardOffering offering) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (offering_id, rotation_id, category_key, slot_index, quest_definition_key," +
                            " rarity_key, scope_target_id, state, accepted_at, quest_instance_uuid, completion_time_ms)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(offering_id) DO UPDATE SET" +
                            " state = excluded.state," +
                            " accepted_at = excluded.accepted_at," +
                            " quest_instance_uuid = excluded.quest_instance_uuid");
            setOfferingParams(ps, offering);
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    @NotNull
    public static List<PreparedStatement> saveOfferings(@NotNull Connection connection,
                                                         @NotNull List<BoardOffering> offerings) {
        List<PreparedStatement> statements = new ArrayList<>();
        for (BoardOffering offering : offerings) {
            statements.addAll(saveOffering(connection, offering));
        }
        return statements;
    }

    @NotNull
    public static List<BoardOffering> loadOfferingsForRotation(@NotNull Connection connection,
                                                                @NotNull UUID rotationId) {
        List<BoardOffering> offerings = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE rotation_id = ? ORDER BY slot_index")) {
            ps.setString(1, rotationId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    offerings.add(buildOffering(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offerings;
    }

    @NotNull
    public static Optional<BoardOffering> loadOfferingById(@NotNull Connection connection,
                                                            @NotNull UUID offeringId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM " + TABLE_NAME + " WHERE offering_id = ?")) {
            ps.setString(1, offeringId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildOffering(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @NotNull
    public static List<PreparedStatement> updateOfferingState(@NotNull Connection connection,
                                                               @NotNull UUID offeringId,
                                                               @NotNull BoardOffering.State state,
                                                               @Nullable Long acceptedAt,
                                                               @Nullable UUID questInstanceUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE " + TABLE_NAME +
                            " SET state = ?, accepted_at = ?, quest_instance_uuid = ?" +
                            " WHERE offering_id = ?");
            ps.setString(1, state.name());
            if (acceptedAt != null) {
                ps.setLong(2, acceptedAt);
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            if (questInstanceUUID != null) {
                ps.setString(3, questInstanceUUID.toString());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, offeringId.toString());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    @NotNull
    public static List<PreparedStatement> expireOfferingsForRotation(@NotNull Connection connection,
                                                                      @NotNull UUID rotationId) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE " + TABLE_NAME + " SET state = ? WHERE rotation_id = ? AND state = ?");
            ps.setString(1, BoardOffering.State.EXPIRED.name());
            ps.setString(2, rotationId.toString());
            ps.setString(3, BoardOffering.State.VISIBLE.name());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    private static void setOfferingParams(@NotNull PreparedStatement ps,
                                           @NotNull BoardOffering offering) throws SQLException {
        ps.setString(1, offering.getOfferingId().toString());
        ps.setString(2, offering.getRotationId().toString());
        ps.setString(3, offering.getCategoryKey().toString());
        ps.setInt(4, offering.getSlotIndex());
        ps.setString(5, offering.getQuestDefinitionKey().toString());
        ps.setString(6, offering.getRarityKey().toString());
        if (offering.getScopeTargetId().isPresent()) {
            ps.setString(7, offering.getScopeTargetId().get());
        } else {
            ps.setNull(7, Types.VARCHAR);
        }
        ps.setString(8, offering.getState().name());
        if (offering.getAcceptedAt().isPresent()) {
            ps.setLong(9, offering.getAcceptedAt().get());
        } else {
            ps.setNull(9, Types.BIGINT);
        }
        if (offering.getQuestInstanceUUID().isPresent()) {
            ps.setString(10, offering.getQuestInstanceUUID().get().toString());
        } else {
            ps.setNull(10, Types.VARCHAR);
        }
        ps.setLong(11, offering.getCompletionTime().toMillis());
    }

    @NotNull
    private static BoardOffering buildOffering(@NotNull ResultSet rs) throws SQLException {
        String defKeyStr = rs.getString("quest_definition_key");
        String scopeTargetId = rs.getString("scope_target_id");
        Long acceptedAt = rs.getLong("accepted_at");
        if (rs.wasNull()) acceptedAt = null;
        String questInstStr = rs.getString("quest_instance_uuid");

        return new BoardOffering(
                UUID.fromString(rs.getString("offering_id")),
                UUID.fromString(rs.getString("rotation_id")),
                NamespacedKey.fromString(rs.getString("category_key")),
                rs.getInt("slot_index"),
                defKeyStr != null ? NamespacedKey.fromString(defKeyStr) : null,
                NamespacedKey.fromString(rs.getString("rarity_key")),
                scopeTargetId,
                Duration.ofMillis(rs.getLong("completion_time_ms")),
                BoardOffering.State.valueOf(rs.getString("state")),
                acceptedAt,
                questInstStr != null ? UUID.fromString(questInstStr) : null
        );
    }
}
