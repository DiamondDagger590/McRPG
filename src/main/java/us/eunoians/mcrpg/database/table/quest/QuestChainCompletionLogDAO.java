package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import org.bukkit.NamespacedKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * DAO for the {@code mcrpg_quest_chain_completion_log} table, which records which quest
 * definitions a player has completed as part of a specific chain. Used during restart
 * re-resolution (skipping already-completed steps) and login re-resolution.
 * <p>
 * The table uses {@code (player_uuid, chain_key, quest_key, completion_number)} as
 * its primary key to support multiple chain completions with the same step history.
 * <p>
 * Write methods return {@link List} of un-executed {@link PreparedStatement}s for use with
 * {@link com.diamonddagger590.mccore.database.transaction.FailSafeTransaction}. Read methods
 * propagate {@link SQLException} to callers.
 */
public class QuestChainCompletionLogDAO {

    public static final String TABLE_NAME = "mcrpg_quest_chain_completion_log";
    private static final int CURRENT_TABLE_VERSION = 2;

    /**
     * Attempts to create the chain completion log table if it does not already exist.
     *
     * @param connection the database connection
     * @param database   the database instance
     * @return {@code true} if a new table was created
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "CREATE TABLE `" + TABLE_NAME + "` (" +
                        "`player_uuid` VARCHAR(36) NOT NULL, " +
                        "`chain_key` VARCHAR(255) NOT NULL, " +
                        "`quest_key` VARCHAR(255) NOT NULL, " +
                        "`completed_at` BIGINT NOT NULL, " +
                        "`completion_number` INTEGER NOT NULL, " +
                        "PRIMARY KEY (`player_uuid`, `chain_key`, `quest_key`, `completion_number`)" +
                        ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE,
                    "[QuestChainCompletionLogDAO] Failed to create table " + TABLE_NAME, e);
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
                    "CREATE INDEX IF NOT EXISTS idx_chain_log_player_chain ON " +
                            TABLE_NAME + " (player_uuid, chain_key)")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "[QuestChainCompletionLogDAO] Failed to create index during migration", e);
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
        if (lastStoredVersion < 2) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN skipped BOOLEAN NOT NULL DEFAULT FALSE")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "[QuestChainCompletionLogDAO] Failed to add skipped column during migration", e);
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 2);
        }
    }

    /**
     * Returns an un-executed {@link PreparedStatement} list that records a chain step completion.
     * Execute via {@link com.diamonddagger590.mccore.database.transaction.FailSafeTransaction}.
     *
     * @param connection       the database connection
     * @param playerUUID       the player UUID
     * @param chainKey         the chain key (string form)
     * @param questKey         the completed quest key (string form)
     * @param completedAt      the completion timestamp
     * @param completionNumber which chain completion this belongs to (1-based)
     * @return list containing the insert statement (un-executed)
     */
    @NotNull
    public static List<PreparedStatement> logCompletion(@NotNull Connection connection,
                                                        @NotNull UUID playerUUID,
                                                        @NotNull String chainKey,
                                                        @NotNull String questKey,
                                                        @NotNull Instant completedAt,
                                                        int completionNumber) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR REPLACE INTO " + TABLE_NAME +
                            " (player_uuid, chain_key, quest_key, completed_at, completion_number) " +
                            "VALUES (?, ?, ?, ?, ?)");
            statement.setString(1, playerUUID.toString());
            statement.setString(2, chainKey);
            statement.setString(3, questKey);
            statement.setLong(4, completedAt.toEpochMilli());
            statement.setInt(5, completionNumber);
            statements.add(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statements;
    }

    /**
     * Returns an un-executed {@link PreparedStatement} list that records a chain step as
     * skipped (quest expired with "skip" behavior). Logged with {@code skipped = true}.
     *
     * @param connection       the database connection
     * @param playerUUID       the player UUID
     * @param chainKey         the chain key (string form)
     * @param questKey         the skipped quest key (string form)
     * @param skippedAt        the skip timestamp
     * @param completionNumber which chain run this belongs to
     * @return list containing the insert statement (un-executed)
     */
    @NotNull
    public static List<PreparedStatement> logSkip(@NotNull Connection connection,
                                                   @NotNull UUID playerUUID,
                                                   @NotNull String chainKey,
                                                   @NotNull String questKey,
                                                   @NotNull Instant skippedAt,
                                                   int completionNumber) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR REPLACE INTO " + TABLE_NAME +
                            " (player_uuid, chain_key, quest_key, completed_at, completion_number, skipped) " +
                            "VALUES (?, ?, ?, ?, ?, TRUE)");
            statement.setString(1, playerUUID.toString());
            statement.setString(2, chainKey);
            statement.setString(3, questKey);
            statement.setLong(4, skippedAt.toEpochMilli());
            statement.setInt(5, completionNumber);
            statements.add(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statements;
    }

    /**
     * Returns the set of quest definition keys a player has completed (not skipped) within
     * a specific chain (across all completion numbers). Skipped entries are excluded so
     * that admin restart replays skipped steps.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @param chainKey   the chain key (string form)
     * @return set of non-skipped completed quest key strings
     * @throws SQLException if a database error occurs
     */
    @NotNull
    public static Set<String> getNonSkippedCompletedQuestKeys(@NotNull Connection connection,
                                                               @NotNull UUID playerUUID,
                                                               @NotNull String chainKey) throws SQLException {
        Set<String> keys = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT quest_key FROM " + TABLE_NAME +
                        " WHERE player_uuid = ? AND chain_key = ? AND skipped = FALSE")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, chainKey);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("quest_key"));
                }
            }
        }
        return keys;
    }

    /**
     * Returns all completed quest keys for a player grouped by chain key in a single query.
     * Used during login re-resolution to avoid N separate per-chain queries.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return map of chain key → set of completed quest key strings for that chain
     * @throws SQLException if a database error occurs
     */
    @NotNull
    public static Map<NamespacedKey, Set<NamespacedKey>> getAllCompletedQuestKeysByChain(
            @NotNull Connection connection, @NotNull UUID playerUUID) throws SQLException {
        Map<NamespacedKey, Set<NamespacedKey>> result = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT chain_key, quest_key FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    NamespacedKey chainKey = NamespacedKey.fromString(rs.getString("chain_key"));
                    NamespacedKey questKey = NamespacedKey.fromString(rs.getString("quest_key"));
                    if (chainKey != null && questKey != null) {
                        result.computeIfAbsent(chainKey, k -> new HashSet<>()).add(questKey);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns an un-executed {@link PreparedStatement} list that deletes all completion log
     * entries for a specific chain for a player. Execute via
     * {@link com.diamonddagger590.mccore.database.transaction.FailSafeTransaction}.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @param chainKey   the chain key (string form)
     * @return list containing the delete statement (un-executed)
     */
    @NotNull
    public static List<PreparedStatement> deleteForChain(@NotNull Connection connection,
                                                         @NotNull UUID playerUUID,
                                                         @NotNull String chainKey) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE player_uuid = ? AND chain_key = ?");
            statement.setString(1, playerUUID.toString());
            statement.setString(2, chainKey);
            statements.add(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statements;
    }

    /**
     * Deletes all chain completion log entries for a player.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return number of deleted rows
     */
    public static int deleteForPlayer(@NotNull Connection connection, @NotNull UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            return statement.executeUpdate();
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[QuestChainCompletionLogDAO] Failed to delete all log entries for player " + playerUUID, e);
        }
        return 0;
    }

    /**
     * Returns all completed chain runs for a player, ordered newest-first.
     * <p>
     * Each row in the result represents a distinct {@code (chain_key, completion_number)} pair
     * with the timestamp of the last step that completed in that run.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return list of chain completion run summaries, newest-first
     */
    @NotNull
    public static List<ChainCompletionRun> getChainCompletionRuns(@NotNull Connection connection,
                                                                   @NotNull UUID playerUUID) {
        List<ChainCompletionRun> runs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT chain_key, completion_number, MAX(completed_at) AS last_step_at, COUNT(DISTINCT quest_key) AS step_count"
                        + " FROM " + TABLE_NAME
                        + " WHERE player_uuid = ?"
                        + " GROUP BY chain_key, completion_number"
                        + " ORDER BY last_step_at DESC")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String chainKeyStr = rs.getString("chain_key");
                    NamespacedKey chainKey = NamespacedKey.fromString(chainKeyStr);
                    if (chainKey == null) {
                        McRPG.getInstance().getLogger().warning(
                                "[QuestChainCompletionLogDAO] Invalid chain key in completion log: " + chainKeyStr);
                        continue;
                    }
                    runs.add(new ChainCompletionRun(
                            chainKey,
                            rs.getInt("completion_number"),
                            Instant.ofEpochMilli(rs.getLong("last_step_at")),
                            rs.getInt("step_count")
                    ));
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[QuestChainCompletionLogDAO] Failed to load chain completion runs for player " + playerUUID, e);
        }
        return runs;
    }

    /**
     * Returns the set of all quest definition keys (as strings) that appear in any chain
     * completion log entry for this player. Used to filter chain-managed quests out of the
     * individual quest history view.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return set of quest definition key strings belonging to chain completions
     */
    @NotNull
    public static Set<String> getChainParticipantQuestKeys(@NotNull Connection connection,
                                                            @NotNull UUID playerUUID) {
        Set<String> keys = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT DISTINCT quest_key FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("quest_key"));
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[QuestChainCompletionLogDAO] Failed to load chain participant keys for player " + playerUUID, e);
        }
        return keys;
    }

    /**
     * Returns step-level completion entries for a specific chain run, ordered by step position
     * (ascending completion timestamp as a proxy for step order).
     *
     * @param connection       the database connection
     * @param playerUUID       the player UUID
     * @param chainKey         the chain definition key (string form)
     * @param completionNumber the specific chain completion run number
     * @return list of (quest_key, completed_at) records for the run, in step order
     */
    @NotNull
    public static List<ChainStepRecord> getStepsForRun(@NotNull Connection connection,
                                                        @NotNull UUID playerUUID,
                                                        @NotNull String chainKey,
                                                        int completionNumber) {
        List<ChainStepRecord> steps = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT quest_key, completed_at FROM " + TABLE_NAME
                        + " WHERE player_uuid = ? AND chain_key = ? AND completion_number = ?"
                        + " ORDER BY completed_at ASC")) {
            statement.setString(1, playerUUID.toString());
            statement.setString(2, chainKey);
            statement.setInt(3, completionNumber);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    steps.add(new ChainStepRecord(rs.getString("quest_key"), Instant.ofEpochMilli(rs.getLong("completed_at"))));
                }
            }
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[QuestChainCompletionLogDAO] Failed to load steps for chain run " + chainKey
                            + " completion #" + completionNumber + " for player " + playerUUID, e);
        }
        return steps;
    }

    /**
     * A single step's completion within a chain run.
     *
     * @param questKey    the quest definition key (string form)
     * @param completedAt the completion timestamp
     */
    public record ChainStepRecord(@NotNull String questKey, @NotNull Instant completedAt) {
    }
}
