package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.quest.chain.QuestChainState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * DAO for the {@code mcrpg_quest_chain_state} table which persists per-player chain lifecycle
 * state. Uses upsert semantics so both insert and update paths go through
 * {@link #saveChainState}.
 * <p>
 * Write methods return {@link List} of un-executed {@link PreparedStatement}s for use with
 * {@link com.diamonddagger590.mccore.database.transaction.FailSafeTransaction}. Read methods
 * propagate {@link SQLException} to callers.
 */
public class QuestChainStateDAO {

    public static final String TABLE_NAME = "mcrpg_quest_chain_state";
    private static final int CURRENT_TABLE_VERSION = 2;

    /**
     * Attempts to create the chain state table if it does not already exist.
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
                        "`current_quest` VARCHAR(255), " +
                        "`state` VARCHAR(32) NOT NULL, " +
                        "`completion_count` INTEGER NOT NULL DEFAULT 0, " +
                        "`last_completed_at` BIGINT, " +
                        "PRIMARY KEY (`player_uuid`, `chain_key`)" +
                        ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.SEVERE,
                    "[QuestChainStateDAO] Failed to create table " + TABLE_NAME, e);
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
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
        }
        if (lastStoredVersion < 2) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "ALTER TABLE " + TABLE_NAME + " ADD COLUMN conditions_pending BOOLEAN NOT NULL DEFAULT FALSE")) {
                ps.executeUpdate();
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "[QuestChainStateDAO] Failed to add conditions_pending column during migration", e);
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 2);
        }
    }

    /**
     * Loads all chain states for a player.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return all chain states (may be empty)
     * @throws SQLException if a database error occurs
     */
    @NotNull
    public static List<QuestChainPlayerState> loadAllChainStates(@NotNull Connection connection,
                                                                  @NotNull UUID playerUUID) throws SQLException {
        List<QuestChainPlayerState> states = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT chain_key, current_quest, state, completion_count, last_completed_at, conditions_pending " +
                        "FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String chainKeyStr = rs.getString("chain_key");
                    String currentQuestStr = rs.getString("current_quest");
                    String stateStr = rs.getString("state");
                    int completionCount = rs.getInt("completion_count");
                    long lastCompletedAtRaw = rs.getLong("last_completed_at");
                    Instant lastCompletedAt = rs.wasNull() ? null : Instant.ofEpochMilli(lastCompletedAtRaw);

                    NamespacedKey chainKey = parseNamespacedKey(chainKeyStr);
                    if (chainKey == null) {
                        McRPG.getInstance().getLogger().log(Level.WARNING,
                                "[QuestChainStateDAO] Invalid chain key '" + chainKeyStr + "' for player " + playerUUID + ", skipping");
                        continue;
                    }

                    NamespacedKey currentQuest = currentQuestStr != null ? parseNamespacedKey(currentQuestStr) : null;
                    QuestChainState chainState;
                    try {
                        chainState = QuestChainState.valueOf(stateStr);
                    } catch (IllegalArgumentException e) {
                        McRPG.getInstance().getLogger().log(Level.WARNING,
                                "[QuestChainStateDAO] Unknown chain state '" + stateStr + "' for chain '" + chainKeyStr + "', defaulting to ACTIVE");
                        chainState = QuestChainState.ACTIVE;
                    }

                    QuestChainPlayerState playerState = new QuestChainPlayerState(chainKey, currentQuest, chainState, completionCount, lastCompletedAt);
                    boolean conditionsPending = rs.getBoolean("conditions_pending");
                    if (conditionsPending) {
                        playerState.setConditionsPending(true);
                        playerState.clearDirty();
                    }
                    states.add(playerState);
                }
            }
        }
        return states;
    }

    /**
     * Returns an un-executed {@link PreparedStatement} list that upserts a single chain state
     * for a player. Execute via {@link com.diamonddagger590.mccore.database.transaction.FailSafeTransaction}.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @param state      the chain state to save
     * @return list containing the upsert statement (un-executed)
     */
    @NotNull
    public static List<PreparedStatement> saveChainState(@NotNull Connection connection,
                                                         @NotNull UUID playerUUID,
                                                         @NotNull QuestChainPlayerState state) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR REPLACE INTO " + TABLE_NAME +
                            " (player_uuid, chain_key, current_quest, state, completion_count, last_completed_at, conditions_pending) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, playerUUID.toString());
            statement.setString(2, state.getChainKey().toString());
            var currentQuestOpt = state.getCurrentQuestKey();
            if (currentQuestOpt.isPresent()) {
                statement.setString(3, currentQuestOpt.get().toString());
            } else {
                statement.setNull(3, Types.VARCHAR);
            }
            statement.setString(4, state.getState().name());
            statement.setInt(5, state.getCompletionCount());
            var lastCompletedOpt = state.getLastCompletedAt();
            if (lastCompletedOpt.isPresent()) {
                statement.setLong(6, lastCompletedOpt.get().toEpochMilli());
            } else {
                statement.setNull(6, Types.BIGINT);
            }
            statement.setBoolean(7, state.isConditionsPending());
            statements.add(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statements;
    }

    /**
     * Returns an un-executed {@link PreparedStatement} list that deletes a specific chain state
     * for a player. Execute via {@link com.diamonddagger590.mccore.database.transaction.FailSafeTransaction}.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @param chainKey   the chain key
     * @return list containing the delete statement (un-executed)
     */
    @NotNull
    public static List<PreparedStatement> deleteChainState(@NotNull Connection connection,
                                                           @NotNull UUID playerUUID,
                                                           @NotNull NamespacedKey chainKey) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + TABLE_NAME + " WHERE player_uuid = ? AND chain_key = ?");
            statement.setString(1, playerUUID.toString());
            statement.setString(2, chainKey.toString());
            statements.add(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statements;
    }

    /**
     * Deletes all chain states for a player.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return number of deleted rows
     */
    public static int deleteAllForPlayer(@NotNull Connection connection, @NotNull UUID playerUUID) {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM " + TABLE_NAME + " WHERE player_uuid = ?")) {
            statement.setString(1, playerUUID.toString());
            return statement.executeUpdate();
        } catch (SQLException e) {
            McRPG.getInstance().getLogger().log(Level.WARNING,
                    "[QuestChainStateDAO] Failed to delete all chain states for player " + playerUUID, e);
        }
        return 0;
    }

    /**
     * Parses a {@link NamespacedKey} from its string form ({@code "namespace:key"}).
     * Returns {@code null} if the string is malformed.
     *
     * @param value the key string to parse
     * @return the parsed key, or null if invalid
     */
    private static NamespacedKey parseNamespacedKey(@NotNull String value) {
        int colon = value.indexOf(':');
        if (colon < 0 || colon == 0 || colon == value.length() - 1) {
            return null;
        }
        try {
            return new NamespacedKey(value.substring(0, colon), value.substring(colon + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
