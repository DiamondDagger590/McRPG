package us.eunoians.mcrpg.database.table.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.quest.chain.QuestChainState;

import java.sql.Connection;
import java.time.Instant;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link QuestChainStateDAO}.
 */
public class QuestChainStateDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");

    @Test
    @DisplayName("Given a valid ResultSet with one row, When loadAllChainStates is called, Then it returns one QuestChainPlayerState")
    void loadAllChainStates_returnsOne_whenResultSetHasOneRow() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("chain_key")).thenReturn(CHAIN_KEY.toString());
        when(mockResultSet.getString("current_quest")).thenReturn(QUEST_KEY.toString());
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getInt("completion_count")).thenReturn(0);
        when(mockResultSet.getLong("last_completed_at")).thenReturn(0L);
        when(mockResultSet.wasNull()).thenReturn(true);

        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(mockConnection, PLAYER_UUID);

        assertEquals(1, states.size());
        QuestChainPlayerState state = states.get(0);
        assertEquals(CHAIN_KEY, state.getChainKey());
        assertEquals(QuestChainState.ACTIVE, state.getState());
        assertTrue(state.getCurrentQuestKey().isPresent());
        assertEquals(QUEST_KEY, state.getCurrentQuestKey().get());
    }

    @Test
    @DisplayName("Given an empty ResultSet, When loadAllChainStates is called, Then it returns an empty list")
    void loadAllChainStates_returnsEmpty_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(mockConnection, PLAYER_UUID);

        assertNotNull(states);
        assertTrue(states.isEmpty());
    }

    @Test
    @DisplayName("Given a ResultSet with null last_completed_at, When loadAllChainStates is called, Then lastCompletedAt is empty")
    void loadAllChainStates_setsLastCompletedAtToEmpty_whenNull() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("chain_key")).thenReturn(CHAIN_KEY.toString());
        when(mockResultSet.getString("current_quest")).thenReturn(null);
        when(mockResultSet.getString("state")).thenReturn("COMPLETED");
        when(mockResultSet.getInt("completion_count")).thenReturn(2);
        when(mockResultSet.getLong("last_completed_at")).thenReturn(0L);
        when(mockResultSet.wasNull()).thenReturn(true);

        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(mockConnection, PLAYER_UUID);

        assertEquals(1, states.size());
        assertTrue(states.get(0).getLastCompletedAt().isEmpty());
    }

    @Test
    @DisplayName("Given a ResultSet with a non-null last_completed_at, When loadAllChainStates is called, Then lastCompletedAt is present")
    void loadAllChainStates_setsLastCompletedAt_whenNonNull() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        long expectedTimestamp = 1_700_000_000_000L;
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("chain_key")).thenReturn(CHAIN_KEY.toString());
        when(mockResultSet.getString("current_quest")).thenReturn(null);
        when(mockResultSet.getString("state")).thenReturn("COMPLETED");
        when(mockResultSet.getInt("completion_count")).thenReturn(1);
        when(mockResultSet.getLong("last_completed_at")).thenReturn(expectedTimestamp);
        when(mockResultSet.wasNull()).thenReturn(false);

        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(mockConnection, PLAYER_UUID);

        assertEquals(1, states.size());
        assertTrue(states.get(0).getLastCompletedAt().isPresent());
        assertEquals(Instant.ofEpochMilli(expectedTimestamp), states.get(0).getLastCompletedAt().get());
    }

    @Test
    @DisplayName("Given a ResultSet row with an invalid chain key, When loadAllChainStates is called, Then the row is skipped")
    void loadAllChainStates_skipsRow_whenChainKeyIsInvalid() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("chain_key")).thenReturn("not-a-valid-key");
        when(mockResultSet.getString("current_quest")).thenReturn(null);
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getInt("completion_count")).thenReturn(0);
        when(mockResultSet.getLong("last_completed_at")).thenReturn(0L);
        when(mockResultSet.wasNull()).thenReturn(true);

        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(mockConnection, PLAYER_UUID);

        assertTrue(states.isEmpty());
    }

    @Test
    @DisplayName("Given a valid chain state, When saveChainState is called, Then it returns a non-empty statement list without executing")
    void saveChainState_returnsUnexecutedStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        var state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        List<PreparedStatement> statements = QuestChainStateDAO.saveChainState(mockConnection, PLAYER_UUID, state);

        assertFalse(statements.isEmpty());
        assertTrue(statements.contains(mockStatement));
        verify(mockStatement, never()).executeUpdate();
    }

    @Test
    @DisplayName("Given a player UUID and chain key, When deleteChainState is called, Then it returns a non-empty statement list without executing")
    void deleteChainState_returnsUnexecutedStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = QuestChainStateDAO.deleteChainState(mockConnection, PLAYER_UUID, CHAIN_KEY);

        assertFalse(statements.isEmpty());
        assertTrue(statements.contains(mockStatement));
        verify(mockStatement, never()).executeUpdate();
    }

    @Test
    @DisplayName("Given a player UUID, When deleteAllForPlayer is called, Then it executes a delete statement and returns the count")
    void deleteAllForPlayer_executesDeleteAndReturnsCount() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(3);

        int result = QuestChainStateDAO.deleteAllForPlayer(mockConnection, PLAYER_UUID);

        assertEquals(3, result);
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("Given a ResultSet with an unknown state string, When loadAllChainStates is called, Then state defaults to ACTIVE")
    void loadAllChainStates_defaultsToActive_whenStateIsUnrecognized() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("chain_key")).thenReturn(CHAIN_KEY.toString());
        when(mockResultSet.getString("current_quest")).thenReturn(null);
        when(mockResultSet.getString("state")).thenReturn("UNKNOWN_LEGACY_STATE");
        when(mockResultSet.getInt("completion_count")).thenReturn(0);
        when(mockResultSet.getLong("last_completed_at")).thenReturn(0L);
        when(mockResultSet.wasNull()).thenReturn(true);

        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(mockConnection, PLAYER_UUID);

        assertFalse(states.isEmpty());
        assertEquals(QuestChainState.ACTIVE, states.get(0).getState());
    }
}
