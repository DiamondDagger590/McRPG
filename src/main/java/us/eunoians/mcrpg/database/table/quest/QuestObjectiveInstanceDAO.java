package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_quest_objective_instances} table, storing objective-level quest state
 * including progress counters and timestamps.
 */
public class QuestObjectiveInstanceDAO {

    static final String TABLE_NAME = "mcrpg_quest_objective_instances";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create the objective instances table if it does not already exist.
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
                "`objective_uuid` varchar(36) NOT NULL," +
                "`stage_uuid` varchar(36) NOT NULL," +
                "`definition_key` varchar(256) NOT NULL," +
                "`state` varchar(32) NOT NULL," +
                "`required_progress` BIGINT NOT NULL," +
                "`current_progress` BIGINT NOT NULL DEFAULT 0," +
                "`start_time` BIGINT," +
                "`end_time` BIGINT," +
                "PRIMARY KEY (`objective_uuid`)," +
                "FOREIGN KEY (`stage_uuid`) REFERENCES `" + QuestStageInstanceDAO.TABLE_NAME + "`(`stage_uuid`) ON DELETE CASCADE" +
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
                    "CREATE INDEX idx_objective_instances_stage ON " + TABLE_NAME + " (stage_uuid)")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
    }

    /**
     * Saves an objective instance, inserting or updating on conflict.
     *
     * @param connection the database connection
     * @param objective  the objective instance to save
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveObjectiveInstance(@NotNull Connection connection,
                                                                @NotNull QuestObjectiveInstance objective) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (objective_uuid, stage_uuid, definition_key, state, required_progress, current_progress, start_time, end_time)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(objective_uuid) DO UPDATE SET" +
                            " state = excluded.state," +
                            " current_progress = excluded.current_progress," +
                            " start_time = excluded.start_time," +
                            " end_time = excluded.end_time");
            ps.setString(1, objective.getQuestObjectiveUUID().toString());
            ps.setString(2, objective.getQuestStage().getQuestStageUUID().toString());
            ps.setString(3, objective.getQuestObjectiveKey().toString());
            ps.setString(4, objective.getQuestObjectiveState().name());
            ps.setLong(5, objective.getRequiredProgression());
            ps.setLong(6, objective.getCurrentProgression());
            setNullableLong(ps, 7, objective.getStartTime().orElse(null));
            setNullableLong(ps, 8, objective.getEndTime().orElse(null));
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Saves all objective instances belonging to a stage.
     *
     * @param connection the database connection
     * @param stage      the stage whose objectives should be saved
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveAllObjectiveInstances(@NotNull Connection connection,
                                                                    @NotNull QuestStageInstance stage) {
        List<PreparedStatement> statements = new ArrayList<>();
        for (QuestObjectiveInstance objective : stage.getQuestObjectives()) {
            statements.addAll(saveObjectiveInstance(connection, objective));
        }
        return statements;
    }

    /**
     * Loads all objective instances for a given stage. The returned objectives have empty
     * contribution maps; contributions are loaded separately via
     * {@link QuestObjectiveContributionDAO}.
     *
     * @param connection the database connection
     * @param stageUUID  the parent stage UUID
     * @param stage      the parent stage instance (for back-reference)
     * @return a list of loaded objective instances
     */
    @NotNull
    public static List<QuestObjectiveInstance> loadObjectiveInstances(@NotNull Connection connection,
                                                                      @NotNull UUID stageUUID,
                                                                      @NotNull QuestStageInstance stage) {
        List<QuestObjectiveInstance> objectives = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT objective_uuid, definition_key, state, required_progress, current_progress, start_time, end_time" +
                        " FROM " + TABLE_NAME + " WHERE stage_uuid = ?")) {
            ps.setString(1, stageUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID objectiveUUID = UUID.fromString(rs.getString("objective_uuid"));
                    NamespacedKey defKey = NamespacedKey.fromString(rs.getString("definition_key"));
                    QuestObjectiveState state = QuestObjectiveState.valueOf(rs.getString("state"));
                    long requiredProgress = rs.getLong("required_progress");
                    long currentProgress = rs.getLong("current_progress");
                    Long startTime = getNullableLong(rs, "start_time");
                    Long endTime = getNullableLong(rs, "end_time");
                    Map<UUID, Long> emptyContributions = new HashMap<>();
                    objectives.add(new QuestObjectiveInstance(
                            defKey, objectiveUUID, stage, state, startTime, endTime,
                            requiredProgress, currentProgress, emptyContributions));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return objectives;
    }

    /**
     * Loads all objective instances for a given stage, including their per-player contributions.
     * This is a convenience method that combines {@link #loadObjectiveInstances} with
     * {@link QuestObjectiveContributionDAO#loadContributions} for each objective.
     *
     * @param connection the database connection
     * @param stageUUID  the parent stage UUID
     * @param stage      the parent stage instance (for back-reference)
     * @return a list of fully loaded objective instances with contributions populated
     */
    @NotNull
    public static List<QuestObjectiveInstance> loadObjectiveInstancesWithContributions(
            @NotNull Connection connection, @NotNull UUID stageUUID, @NotNull QuestStageInstance stage) {
        List<QuestObjectiveInstance> objectives = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT objective_uuid, definition_key, state, required_progress, current_progress, start_time, end_time" +
                        " FROM " + TABLE_NAME + " WHERE stage_uuid = ?")) {
            ps.setString(1, stageUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID objectiveUUID = UUID.fromString(rs.getString("objective_uuid"));
                    NamespacedKey defKey = NamespacedKey.fromString(rs.getString("definition_key"));
                    QuestObjectiveState state = QuestObjectiveState.valueOf(rs.getString("state"));
                    long requiredProgress = rs.getLong("required_progress");
                    long currentProgress = rs.getLong("current_progress");
                    Long startTime = getNullableLong(rs, "start_time");
                    Long endTime = getNullableLong(rs, "end_time");
                    Map<UUID, Long> contributions = QuestObjectiveContributionDAO.loadContributions(
                            connection, objectiveUUID);
                    objectives.add(new QuestObjectiveInstance(
                            defKey, objectiveUUID, stage, state, startTime, endTime,
                            requiredProgress, currentProgress, contributions));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return objectives;
    }

    /**
     * Deletes all objective instance rows for a given stage.
     *
     * @param connection the database connection
     * @param stageUUID  the parent stage UUID
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> deleteObjectiveInstances(@NotNull Connection connection,
                                                                   @NotNull UUID stageUUID) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE stage_uuid = ?");
            ps.setString(1, stageUUID.toString());
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
