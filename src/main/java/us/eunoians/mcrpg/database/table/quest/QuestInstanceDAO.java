package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.QuestSourceRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for the {@code mcrpg_quest_instances} table, storing the top-level quest instance data.
 * <p>
 * Scope data is stored in separate per-scope-type tables managed by their respective scope DAOs.
 * This table stores the {@code scope_type} key so the correct scope provider can be identified
 * during loading.
 */
public class QuestInstanceDAO {

    static final String TABLE_NAME = "mcrpg_quest_instances";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create the quest instances table if it does not already exist.
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
                "`quest_uuid` varchar(36) NOT NULL," +
                "`definition_key` varchar(256) NOT NULL," +
                "`state` varchar(32) NOT NULL," +
                "`scope_type` varchar(256) NOT NULL," +
                "`start_time` BIGINT," +
                "`end_time` BIGINT," +
                "`expiration_time` BIGINT," +
                "`quest_source` varchar(256) NOT NULL," +
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
            String[] indexes = {
                    "CREATE INDEX IF NOT EXISTS idx_quest_instances_state ON " + TABLE_NAME + " (state)",
                    "CREATE INDEX IF NOT EXISTS idx_qi_definition_key ON " + TABLE_NAME + " (definition_key)"
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
     * Saves a quest instance to the database, inserting or updating on conflict.
     * <p>
     * The scope type is derived from the quest's definition key via the provided {@code scopeType}.
     *
     * @param connection the database connection
     * @param quest      the quest instance to save
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> saveQuestInstance(@NotNull Connection connection,
                                                            @NotNull QuestInstance quest) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO " + TABLE_NAME +
                            " (quest_uuid, definition_key, state, scope_type, start_time, end_time, expiration_time, quest_source)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(quest_uuid) DO UPDATE SET" +
                            " state = excluded.state," +
                            " start_time = excluded.start_time," +
                            " end_time = excluded.end_time," +
                            " expiration_time = excluded.expiration_time");
            ps.setString(1, quest.getQuestUUID().toString());
            ps.setString(2, quest.getQuestKey().toString());
            ps.setString(3, quest.getQuestState().name());
            ps.setString(4, quest.getScopeType().toString());
            setNullableLong(ps, 5, quest.getStartTime().orElse(null));
            setNullableLong(ps, 6, quest.getEndTime().orElse(null));
            setNullableLong(ps, 7, quest.getExpirationTime().orElse(null));
            ps.setString(8, quest.getQuestSource().getKey().toString());
            statements.add(ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Loads a quest instance by its UUID. The returned instance will have a {@code null} scope;
     * scope loading is handled separately by the appropriate scope provider.
     *
     * @param connection the database connection
     * @param questUUID  the quest instance UUID
     * @return the loaded quest instance, or empty if not found
     */
    @NotNull
    public static Optional<QuestInstance> loadQuestInstance(@NotNull Connection connection, @NotNull UUID questUUID) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT definition_key, scope_type, state, start_time, end_time, expiration_time, quest_source FROM " + TABLE_NAME +
                        " WHERE quest_uuid = ?")) {
            ps.setString(1, questUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildQuestInstance(questUUID, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Loads the scope type key stored for the given quest instance.
     *
     * @param connection the database connection
     * @param questUUID  the quest instance UUID
     * @return the scope type namespaced key, or empty if the quest was not found
     */
    @NotNull
    public static Optional<NamespacedKey> loadScopeType(@NotNull Connection connection, @NotNull UUID questUUID) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT scope_type FROM " + TABLE_NAME + " WHERE quest_uuid = ?")) {
            ps.setString(1, questUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(NamespacedKey.fromString(rs.getString("scope_type")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Loads all quest instances that match one of the provided states. Useful for finding
     * all active quests ({@code NOT_STARTED}, {@code IN_PROGRESS}) at startup.
     *
     * @param connection the database connection
     * @param states     the quest states to match
     * @return a list of quest instances (scopes will be {@code null})
     */
    @NotNull
    public static List<QuestInstance> loadQuestInstancesByState(@NotNull Connection connection,
                                                                @NotNull QuestState... states) {
        List<QuestInstance> results = new ArrayList<>();
        if (states.length == 0) {
            return results;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < states.length; i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT quest_uuid, definition_key, scope_type, state, start_time, end_time, expiration_time, quest_source FROM " +
                        TABLE_NAME + " WHERE state IN (" + placeholders + ")")) {
            for (int i = 0; i < states.length; i++) {
                ps.setString(i + 1, states[i].name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID questUUID = UUID.fromString(rs.getString("quest_uuid"));
                    results.add(buildQuestInstance(questUUID, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Deletes a quest instance row from the database.
     *
     * @param connection the database connection
     * @param questUUID  the quest instance UUID to delete
     * @return a list of prepared statements to execute
     */
    @NotNull
    public static List<PreparedStatement> deleteQuestInstance(@NotNull Connection connection, @NotNull UUID questUUID) {
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

    /**
     * Bulk-expires all quest instances in the database that are still in an active state
     * ({@code NOT_STARTED} or {@code IN_PROGRESS}) but whose {@code expiration_time} has
     * passed. Sets their state to {@code CANCELLED} and records the current time as
     * {@code end_time}.
     * <p>
     * This is intended for database-only cleanup of quests that expired while no players
     * were online or while the quest was not loaded in memory. In-memory quests should be
     * expired through {@link QuestInstance#expire()} to ensure events fire properly.
     *
     * @param connection  the database connection
     * @param currentTime the current epoch millis to compare against expiration times
     * @return the number of rows updated
     */
    public static int bulkExpireStaleQuests(@NotNull Connection connection, long currentTime) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE " + TABLE_NAME +
                        " SET state = ?, end_time = ?" +
                        " WHERE state IN (?, ?)" +
                        " AND expiration_time IS NOT NULL" +
                        " AND expiration_time < ?")) {
            ps.setString(1, QuestState.CANCELLED.name());
            ps.setLong(2, currentTime);
            ps.setString(3, QuestState.NOT_STARTED.name());
            ps.setString(4, QuestState.IN_PROGRESS.name());
            ps.setLong(5, currentTime);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @NotNull
    private static QuestInstance buildQuestInstance(@NotNull UUID questUUID, @NotNull ResultSet rs) throws SQLException {
        NamespacedKey definitionKey = NamespacedKey.fromString(rs.getString("definition_key"));
        NamespacedKey scopeType = NamespacedKey.fromString(rs.getString("scope_type"));
        QuestState state = QuestState.valueOf(rs.getString("state"));
        Long startTime = getNullableLong(rs, "start_time");
        Long endTime = getNullableLong(rs, "end_time");
        Long expirationTime = getNullableLong(rs, "expiration_time");

        NamespacedKey sourceKey = NamespacedKey.fromString(rs.getString("quest_source"));
        QuestSourceRegistry sourceRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_SOURCE);
        QuestSource questSource = sourceRegistry.get(sourceKey)
                .orElseThrow(() -> new IllegalStateException("Unknown quest source: " + sourceKey + " for quest " + questUUID));

        return new QuestInstance(definitionKey, questUUID, scopeType, state, null, startTime, endTime, expirationTime, questSource, null);
    }

    /**
     * Loads a complete quest tree: the quest instance, all its stages, all objectives per stage,
     * and all per-player contributions per objective. This reconstructs the full in-memory
     * hierarchy in a single call, suitable for loading quests at startup or on-demand from SQL.
     * <p>
     * The returned quest will have a {@code null} scope; scope loading must be handled separately
     * by the appropriate scope provider.
     *
     * @param connection the database connection
     * @param questUUID  the quest instance UUID
     * @return the fully loaded quest instance, or empty if the quest was not found
     */
    @NotNull
    public static Optional<QuestInstance> loadFullQuestTree(@NotNull Connection connection, @NotNull UUID questUUID) {
        Optional<QuestInstance> optionalQuest = loadQuestInstance(connection, questUUID);
        if (optionalQuest.isEmpty()) {
            return Optional.empty();
        }

        QuestInstance quest = optionalQuest.get();
        List<QuestStageInstance> stages = QuestStageInstanceDAO.loadStageInstances(connection, questUUID, quest);
        for (QuestStageInstance stage : stages) {
            List<QuestObjectiveInstance> objectives = QuestObjectiveInstanceDAO.loadObjectiveInstancesWithContributions(
                    connection, stage.getQuestStageUUID(), stage);
            for (QuestObjectiveInstance objective : objectives) {
                stage.addQuestObjective(objective);
            }
            quest.addQuestStage(stage);
        }
        return Optional.of(quest);
    }

    /**
     * Saves an entire quest tree: the quest instance, all stages, all objectives, and all
     * per-player contributions. Suitable for bulk-saving a quest and all its children.
     *
     * @param connection the database connection
     * @param quest      the quest instance to save
     * @return a list of all prepared statements needed to persist the full tree
     */
    /**
     * Saves an entire quest tree: the quest instance, its scope, all stages, all objectives,
     * and all per-player contributions. Suitable for bulk-saving a quest and all its children.
     *
     * @param connection the database connection
     * @param quest      the quest instance to save
     * @return a list of all prepared statements needed to persist the full tree
     */
    @NotNull
    public static List<PreparedStatement> saveFullQuestTree(@NotNull Connection connection,
                                                            @NotNull QuestInstance quest) {
        List<PreparedStatement> statements = new ArrayList<>();
        statements.addAll(saveQuestInstance(connection, quest));
        quest.getQuestScope().ifPresent(scope -> {
            if (scope.isScopeValid()) {
                statements.addAll(scope.saveScope(connection));
            }
        });
        for (QuestStageInstance stage : quest.getQuestStageInstances()) {
            statements.addAll(QuestStageInstanceDAO.saveStageInstance(connection, stage));
            for (QuestObjectiveInstance objective : stage.getQuestObjectives()) {
                statements.addAll(QuestObjectiveInstanceDAO.saveObjectiveInstance(connection, objective));
                statements.addAll(QuestObjectiveContributionDAO.saveContributions(connection, objective));
            }
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
