package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_quest_stage_instances} table, storing stage-level quest state.
 * <p>
 * Each stage belongs to a parent quest instance and a specific phase (identified by index).
 */
public class QuestStageInstanceDAO {

    static final String TABLE_NAME = "mcrpg_quest_stage_instances";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create the stage instances table if it does not already exist.
     *
     * @param connection the database connection
     * @param database   the database instance
     * @return {@code true} if a new table was created, {@code false} if it already existed
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "` (" +
                "`stage_uuid` varchar(36) NOT NULL," +
                "`quest_uuid` varchar(36) NOT NULL," +
                "`definition_key` varchar(256) NOT NULL," +
                "`phase_index` INTEGER NOT NULL," +
                "`state` varchar(32) NOT NULL," +
                "`start_time` BIGINT," +
                "`end_time` BIGINT," +
                "PRIMARY KEY (`stage_uuid`)," +
                "FOREIGN KEY (`quest_uuid`) REFERENCES `" + QuestInstanceDAO.TABLE_NAME + "`(`quest_uuid`) ON DELETE CASCADE" +
                ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Applies any pending schema migrations for this table.
     *
     * @param connection the database connection
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }
        if (lastStoredVersion == 0) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE INDEX idx_stage_instances_quest ON " + TABLE_NAME + " (quest_uuid)")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
    }

    /**
     * Saves a stage instance, inserting or updating on conflict.
     *
     * @param connection the database connection
     * @param stage      the stage instance to save
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveStageInstance(@NotNull Connection connection,
                                                            @NotNull QuestStageInstance stage) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (stage_uuid, quest_uuid, definition_key, phase_index, state, start_time, end_time)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(stage_uuid) DO UPDATE SET" +
                            " state = excluded.state," +
                            " start_time = excluded.start_time," +
                            " end_time = excluded.end_time");
            ps.setString(1, stage.getQuestStageUUID().toString());
            ps.setString(2, stage.getQuestInstance().getQuestUUID().toString());
            ps.setString(3, stage.getStageKey().toString());
            ps.setInt(4, stage.getPhaseIndex());
            ps.setString(5, stage.getQuestStageState().name());
            setNullableLong(ps, 6, stage.getStartTime().orElse(null));
            setNullableLong(ps, 7, stage.getEndTime().orElse(null));
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Saves all stage instances belonging to a quest.
     *
     * @param connection the database connection
     * @param quest      the quest whose stages should be saved
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveAllStageInstances(@NotNull Connection connection,
                                                                @NotNull QuestInstance quest) {
        List<PreparedStatement> statements = new ArrayList<>();
        for (QuestStageInstance stage : quest.getQuestStageInstances()) {
            statements.addAll(saveStageInstance(connection, stage));
        }
        return statements;
    }

    /**
     * Loads all stage instances for a given quest. The stages are created with the provided
     * parent quest reference and their objectives list will be empty (loaded separately).
     *
     * @param connection the database connection
     * @param questUUID  the parent quest UUID
     * @param quest      the parent quest instance (for back-reference)
     * @return a list of loaded stage instances
     */
    @NotNull
    public static List<QuestStageInstance> loadStageInstances(@NotNull Connection connection,
                                                              @NotNull UUID questUUID,
                                                              @NotNull QuestInstance quest) {
        List<QuestStageInstance> stages = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT stage_uuid, definition_key, phase_index, state, start_time, end_time FROM " +
                        TABLE_NAME + " WHERE quest_uuid = ? ORDER BY phase_index")) {
            ps.setString(1, questUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID stageUUID = UUID.fromString(rs.getString("stage_uuid"));
                    NamespacedKey defKey = NamespacedKey.fromString(rs.getString("definition_key"));
                    int phaseIndex = rs.getInt("phase_index");
                    QuestStageState state = QuestStageState.valueOf(rs.getString("state"));
                    Long startTime = getNullableLong(rs, "start_time");
                    Long endTime = getNullableLong(rs, "end_time");
                    stages.add(new QuestStageInstance(defKey, stageUUID, phaseIndex, quest, state, startTime, endTime));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stages;
    }

    /**
     * Deletes all stage instance rows for a given quest.
     *
     * @param connection the database connection
     * @param questUUID  the parent quest UUID
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> deleteStageInstances(@NotNull Connection connection,
                                                               @NotNull UUID questUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE quest_uuid = ?");
            ps.setString(1, questUUID.toString());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    private static void setNullableLong(@NotNull PreparedStatement ps, int index, Long value) throws SQLException {
        if (value != null) {
            ps.setLong(index, value);
        } else {
            ps.setNull(index, java.sql.Types.BIGINT);
        }
    }

    private static Long getNullableLong(@NotNull ResultSet rs, @NotNull String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
