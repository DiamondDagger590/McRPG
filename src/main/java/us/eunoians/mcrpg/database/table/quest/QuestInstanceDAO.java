package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import us.eunoians.mcrpg.McRPG;
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
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

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
                "`board_rarity_key` varchar(256)," +
                "PRIMARY KEY (`quest_uuid`)" +
                ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE, "[QuestInstanceDAO] Failed to create table " + TABLE_NAME, e);
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
                    McRPG.getInstance().getLogger().log(Level.SEVERE, "[QuestInstanceDAO] Failed to create index during migration", e);
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
                            " (quest_uuid, definition_key, state, scope_type, start_time, end_time, expiration_time, quest_source, board_rarity_key)" +
                            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                            " ON CONFLICT(quest_uuid) DO UPDATE SET" +
                            " state = excluded.state," +
                            " start_time = excluded.start_time," +
                            " end_time = excluded.end_time," +
                            " expiration_time = excluded.expiration_time," +
                            " board_rarity_key = excluded.board_rarity_key");
            ps.setString(1, quest.getQuestUUID().toString());
            ps.setString(2, quest.getQuestKey().toString());
            ps.setString(3, quest.getQuestState().name());
            ps.setString(4, quest.getScopeType().toString());
            setNullableInstant(ps, 5, quest.getStartTime().orElse(null));
            setNullableInstant(ps, 6, quest.getEndTime().orElse(null));
            setNullableInstant(ps, 7, quest.getExpirationTime().orElse(null));
            ps.setString(8, quest.getQuestSource().getKey().toString());
            String boardRarityKey = quest.getBoardRarityKey().map(NamespacedKey::toString).orElse(null);
            if (boardRarityKey != null) {
                ps.setString(9, boardRarityKey);
            } else {
                ps.setNull(9, Types.VARCHAR);
            }
            statements.add(ps);
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Failed to prepare saveQuestInstance statement for quest " + quest.getQuestUUID(), e);
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
                "SELECT definition_key, scope_type, state, start_time, end_time, expiration_time, quest_source, board_rarity_key FROM " + TABLE_NAME +
                        " WHERE quest_uuid = ?")) {
            ps.setString(1, questUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildQuestInstance(questUUID, rs));
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Failed to load quest instance " + questUUID, e);
        } catch (IllegalStateException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Skipping corrupt quest instance " + questUUID, e);
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
                    String rawScopeType = rs.getString("scope_type");
                    NamespacedKey scopeType = NamespacedKey.fromString(rawScopeType);
                    if (scopeType == null) {
                        McRPG.getInstance().getLogger().warning("[QuestInstanceDAO] Malformed scope_type '" + rawScopeType + "' for quest " + questUUID);
                        return Optional.empty();
                    }
                    return Optional.of(scopeType);
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Failed to load scope type for quest " + questUUID, e);
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
                "SELECT quest_uuid, definition_key, scope_type, state, start_time, end_time, expiration_time, quest_source, board_rarity_key FROM " +
                        TABLE_NAME + " WHERE state IN (" + placeholders + ")")) {
            for (int i = 0; i < states.length; i++) {
                ps.setString(i + 1, states[i].name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID questUUID = UUID.fromString(rs.getString("quest_uuid"));
                    try {
                        results.add(buildQuestInstance(questUUID, rs));
                    } catch (IllegalStateException e) {
                        McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Skipping corrupt quest instance " + questUUID + " during bulk load", e);
                    }
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Failed to load quest instances by state", e);
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
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Failed to prepare deleteQuestInstance statement for quest " + questUUID, e);
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
            McRPG.getInstance().getLogger().log(Level.WARNING, "[QuestInstanceDAO] Failed to bulk-expire stale quests", e);
            return 0;
        }
    }

    @NotNull
    private static QuestInstance buildQuestInstance(@NotNull UUID questUUID, @NotNull ResultSet rs) throws SQLException {
        String rawDefKey = rs.getString("definition_key");
        NamespacedKey definitionKey = NamespacedKey.fromString(rawDefKey);
        if (definitionKey == null) {
            throw new IllegalStateException("Malformed definition_key '" + rawDefKey + "' for quest " + questUUID);
        }

        String rawScopeType = rs.getString("scope_type");
        NamespacedKey scopeType = NamespacedKey.fromString(rawScopeType);
        if (scopeType == null) {
            throw new IllegalStateException("Malformed scope_type '" + rawScopeType + "' for quest " + questUUID);
        }

        QuestState state = QuestState.valueOf(rs.getString("state"));
        Instant startTime = getNullableInstant(rs, "start_time");
        Instant endTime = getNullableInstant(rs, "end_time");
        Instant expirationTime = getNullableInstant(rs, "expiration_time");

        String rawSourceKey = rs.getString("quest_source");
        NamespacedKey sourceKey = NamespacedKey.fromString(rawSourceKey);
        if (sourceKey == null) {
            throw new IllegalStateException("Malformed quest_source '" + rawSourceKey + "' for quest " + questUUID);
        }
        QuestSourceRegistry sourceRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_SOURCE);
        QuestSource questSource = sourceRegistry.get(sourceKey)
                .orElseThrow(() -> new IllegalStateException("Unknown quest source: " + sourceKey + " for quest " + questUUID));

        QuestInstance questInstance = new QuestInstance(definitionKey, questUUID, scopeType, state, null, startTime, endTime, expirationTime, questSource, null);
        String boardRarityKeyStr = rs.getString("board_rarity_key");
        if (boardRarityKeyStr != null) {
            NamespacedKey boardRarityKey = NamespacedKey.fromString(boardRarityKeyStr);
            if (boardRarityKey != null) {
                questInstance.setBoardRarityKey(boardRarityKey);
            } else {
                McRPG.getInstance().getLogger().warning("[QuestInstanceDAO] Malformed board_rarity_key '" + boardRarityKeyStr + "' for quest " + questUUID + " — rarity will be absent");
            }
        }
        return questInstance;
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
            ps.setNull(index, Types.BIGINT);
        }
    }

    private static Long getNullableLong(@NotNull ResultSet rs, @NotNull String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    /**
     * Reads a BIGINT column and converts to {@link Instant}, or {@code null} if the column is SQL NULL.
     *
     * @param rs     the result set
     * @param column the column name
     * @return the instant, or {@code null}
     */
    @Nullable
    private static Instant getNullableInstant(@NotNull ResultSet rs, @NotNull String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : Instant.ofEpochMilli(value);
    }

    /**
     * Writes an {@link Instant} as a BIGINT epoch millis, or SQL NULL if the value is {@code null}.
     *
     * @param ps    the prepared statement
     * @param index the parameter index
     * @param value the instant, or {@code null}
     */
    private static void setNullableInstant(@NotNull PreparedStatement ps, int index, @Nullable Instant value) throws SQLException {
        if (value != null) {
            ps.setLong(index, value.toEpochMilli());
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }
}
