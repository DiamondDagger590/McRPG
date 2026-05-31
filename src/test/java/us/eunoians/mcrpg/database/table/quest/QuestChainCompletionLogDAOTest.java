package us.eunoians.mcrpg.database.table.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO.ChainStepRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * Tests for {@link QuestChainCompletionLogDAO}.
 */
public class QuestChainCompletionLogDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY_A = new NamespacedKey("mcrpg", "quest_a");
    private static final NamespacedKey QUEST_KEY_B = new NamespacedKey("mcrpg", "quest_b");

    @Test
    @DisplayName("Given valid parameters, When logCompletion is called, Then it returns a non-empty statement list without executing")
    void logCompletion_returnsUnexecutedStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = QuestChainCompletionLogDAO.logCompletion(
                mockConnection,
                PLAYER_UUID,
                CHAIN_KEY.toString(),
                QUEST_KEY_A.toString(),
                System.currentTimeMillis(),
                1
        );

        assertFalse(statements.isEmpty());
        assertTrue(statements.contains(mockStatement));
        verify(mockStatement, never()).executeUpdate();
    }

    @Test
    @DisplayName("Given a ResultSet with two quest keys, When getCompletedQuestKeys is called, Then both keys are returned")
    void getCompletedQuestKeys_returnsAllDistinctKeys() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("quest_key"))
                .thenReturn(QUEST_KEY_A.toString(), QUEST_KEY_B.toString());

        Set<String> keys = QuestChainCompletionLogDAO.getCompletedQuestKeys(
                mockConnection, PLAYER_UUID, CHAIN_KEY.toString());

        assertEquals(2, keys.size());
        assertTrue(keys.contains(QUEST_KEY_A.toString()));
        assertTrue(keys.contains(QUEST_KEY_B.toString()));
    }

    @Test
    @DisplayName("Given an empty ResultSet, When getCompletedQuestKeys is called, Then an empty set is returned")
    void getCompletedQuestKeys_returnsEmptySet_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Set<String> keys = QuestChainCompletionLogDAO.getCompletedQuestKeys(
                mockConnection, PLAYER_UUID, CHAIN_KEY.toString());

        assertNotNull(keys);
        assertTrue(keys.isEmpty());
    }

    @Test
    @DisplayName("Given a ResultSet with two chain entries, When getAllCompletedQuestKeysByChain is called, Then the map groups quest keys by chain")
    void getAllCompletedQuestKeysByChain_groupsKeysByChain() throws SQLException {
        NamespacedKey chainKeyB = new NamespacedKey("mcrpg", "chain_b");
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("chain_key"))
                .thenReturn(CHAIN_KEY.toString(), chainKeyB.toString());
        when(mockResultSet.getString("quest_key"))
                .thenReturn(QUEST_KEY_A.toString(), QUEST_KEY_B.toString());

        Map<NamespacedKey, Set<NamespacedKey>> result =
                QuestChainCompletionLogDAO.getAllCompletedQuestKeysByChain(mockConnection, PLAYER_UUID);

        assertEquals(2, result.size());
        assertTrue(result.containsKey(CHAIN_KEY));
        assertTrue(result.get(CHAIN_KEY).contains(QUEST_KEY_A));
        assertTrue(result.containsKey(chainKeyB));
        assertTrue(result.get(chainKeyB).contains(QUEST_KEY_B));
    }

    @Test
    @DisplayName("Given an empty ResultSet, When getAllCompletedQuestKeysByChain is called, Then an empty map is returned")
    void getAllCompletedQuestKeysByChain_returnsEmptyMap_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Map<NamespacedKey, Set<NamespacedKey>> result =
                QuestChainCompletionLogDAO.getAllCompletedQuestKeysByChain(mockConnection, PLAYER_UUID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given a player UUID and chain key, When deleteForChain is called, Then it returns a non-empty statement list without executing")
    void deleteForChain_returnsUnexecutedStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = QuestChainCompletionLogDAO.deleteForChain(
                mockConnection, PLAYER_UUID, CHAIN_KEY.toString());

        assertFalse(statements.isEmpty());
        assertTrue(statements.contains(mockStatement));
        verify(mockStatement, never()).executeUpdate();
    }

    @Test
    @DisplayName("Given a player UUID, When deleteForPlayer is called, Then a delete is executed and the count returned")
    void deleteForPlayer_executesDeleteAndReturnsCount() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(5);

        int result = QuestChainCompletionLogDAO.deleteForPlayer(mockConnection, PLAYER_UUID);

        assertEquals(5, result);
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("Given a ResultSet with two completion runs, When getChainCompletionRuns is called, Then both runs are returned")
    void getChainCompletionRuns_returnsTwoRuns_whenTwoRows() throws SQLException {
        NamespacedKey chainKeyB = new NamespacedKey("mcrpg", "chain_b");
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("chain_key"))
                .thenReturn(CHAIN_KEY.toString(), chainKeyB.toString());
        when(mockResultSet.getInt("completion_number")).thenReturn(1, 2);
        when(mockResultSet.getLong("last_step_at")).thenReturn(2000L, 1000L);
        when(mockResultSet.getInt("step_count")).thenReturn(3, 2);

        List<ChainCompletionRun> runs =
                QuestChainCompletionLogDAO.getChainCompletionRuns(mockConnection, PLAYER_UUID);

        assertEquals(2, runs.size());
        assertEquals(CHAIN_KEY, runs.get(0).chainKey());
        assertEquals(1, runs.get(0).completionNumber());
        assertEquals(2000L, runs.get(0).completedAt());
        assertEquals(3, runs.get(0).stepCount());
    }

    @Test
    @DisplayName("Given an empty ResultSet, When getChainCompletionRuns is called, Then an empty list is returned")
    void getChainCompletionRuns_returnsEmpty_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<ChainCompletionRun> runs =
                QuestChainCompletionLogDAO.getChainCompletionRuns(mockConnection, PLAYER_UUID);

        assertNotNull(runs);
        assertTrue(runs.isEmpty());
    }

    @Test
    @DisplayName("Given a ResultSet with step records, When getStepsForRun is called, Then steps are returned in order")
    void getStepsForRun_returnsStepsInOrder() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("quest_key"))
                .thenReturn(QUEST_KEY_A.toString(), QUEST_KEY_B.toString());
        when(mockResultSet.getLong("completed_at")).thenReturn(1000L, 2000L);

        List<ChainStepRecord> steps =
                QuestChainCompletionLogDAO.getStepsForRun(
                        mockConnection, PLAYER_UUID, CHAIN_KEY.toString(), 1);

        assertEquals(2, steps.size());
        assertEquals(QUEST_KEY_A.toString(), steps.get(0).questKey());
        assertEquals(1000L, steps.get(0).completedAt());
        assertEquals(QUEST_KEY_B.toString(), steps.get(1).questKey());
        assertEquals(2000L, steps.get(1).completedAt());
    }

    @Test
    @DisplayName("Given a ResultSet with no steps, When getStepsForRun is called, Then an empty list is returned")
    void getStepsForRun_returnsEmpty_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<ChainStepRecord> steps =
                QuestChainCompletionLogDAO.getStepsForRun(
                        mockConnection, PLAYER_UUID, CHAIN_KEY.toString(), 1);

        assertNotNull(steps);
        assertTrue(steps.isEmpty());
    }

    @Test
    @DisplayName("Given a ResultSet with participant quest keys, When getChainParticipantQuestKeys is called, Then all are returned")
    void getChainParticipantQuestKeys_returnsAllQuestKeys() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("quest_key"))
                .thenReturn(QUEST_KEY_A.toString(), QUEST_KEY_B.toString());

        Set<String> keys = QuestChainCompletionLogDAO.getChainParticipantQuestKeys(
                mockConnection, PLAYER_UUID);

        assertEquals(2, keys.size());
        assertTrue(keys.contains(QUEST_KEY_A.toString()));
        assertTrue(keys.contains(QUEST_KEY_B.toString()));
    }

    @Test
    @DisplayName("Given an empty ResultSet, When getChainParticipantQuestKeys is called, Then an empty set is returned")
    void getChainParticipantQuestKeys_returnsEmpty_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Set<String> keys = QuestChainCompletionLogDAO.getChainParticipantQuestKeys(
                mockConnection, PLAYER_UUID);

        assertNotNull(keys);
        assertTrue(keys.isEmpty());
    }
}
