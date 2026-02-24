package us.eunoians.mcrpg.database.table.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestCompletionLogDAOTest extends McRPGBaseTest {

    @DisplayName("Given valid parameters, when logging completion, then it prepares and executes an insert")
    @Test
    public void logCompletion_preparesAndExecutesInsert() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        QuestCompletionLogDAO.logCompletion(
                mockConnection,
                UUID.randomUUID(),
                "mcrpg:test_quest",
                UUID.randomUUID(),
                System.currentTimeMillis()
        );

        verify(mockStatement).executeUpdate();
    }

    @DisplayName("Given a mocked ResultSet with count, when getting completion count, then it returns the count")
    @Test
    public void getCompletionCount_returnsCountFromResultSet() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(3);

        int count = QuestCompletionLogDAO.getCompletionCount(
                mockConnection, UUID.randomUUID(), "mcrpg:test_quest");
        assertEquals(3, count);
    }

    @DisplayName("Given a mocked ResultSet with timestamp, when getting last completion time, then it returns the value")
    @Test
    public void getLastCompletionTime_returnsTimestamp() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(123456789L);
        when(mockResultSet.wasNull()).thenReturn(false);

        assertTrue(QuestCompletionLogDAO.getLastCompletionTime(
                mockConnection, UUID.randomUUID(), "mcrpg:test_quest").isPresent());
    }

    @DisplayName("Given a mocked ResultSet with null value, when getting last completion time, then it returns empty")
    @Test
    public void getLastCompletionTime_returnsEmpty_whenNull() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(0L);
        when(mockResultSet.wasNull()).thenReturn(true);

        assertTrue(QuestCompletionLogDAO.getLastCompletionTime(
                mockConnection, UUID.randomUUID(), "mcrpg:test_quest").isEmpty());
    }

    @DisplayName("Given a mocked ResultSet with rows, when getting completion history, then it returns records in order")
    @Test
    public void getCompletionHistory_returnsRecords() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        UUID questUUID1 = UUID.randomUUID();
        UUID questUUID2 = UUID.randomUUID();
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("definition_key")).thenReturn("mcrpg:quest_a", "mcrpg:quest_b");
        when(mockResultSet.getString("quest_uuid")).thenReturn(questUUID1.toString(), questUUID2.toString());
        when(mockResultSet.getLong("completed_at")).thenReturn(1000L, 2000L);

        List<CompletionRecord> records = QuestCompletionLogDAO.getCompletionHistory(
                mockConnection, UUID.randomUUID(), true);
        assertEquals(2, records.size());
        assertEquals("mcrpg:quest_a", records.get(0).definitionKey());
        assertEquals(questUUID1, records.get(0).questUUID());
        assertEquals(1000L, records.get(0).completedAt());
        assertEquals("mcrpg:quest_b", records.get(1).definitionKey());
        assertEquals(2000L, records.get(1).completedAt());
    }

    @DisplayName("Given no rows, when getting completion history, then it returns empty list")
    @Test
    public void getCompletionHistory_returnsEmptyList_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<CompletionRecord> records = QuestCompletionLogDAO.getCompletionHistory(
                mockConnection, UUID.randomUUID(), true);
        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @DisplayName("Given ascending=true, when getting completion history, then SQL contains ASC")
    @Test
    public void getCompletionHistory_usesAscOrder_whenAscendingTrue() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("ASC"))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        QuestCompletionLogDAO.getCompletionHistory(mockConnection, UUID.randomUUID(), true);
        verify(mockConnection).prepareStatement(contains("ASC"));
    }

    @DisplayName("Given ascending=false, when getting completion history, then SQL contains DESC")
    @Test
    public void getCompletionHistory_usesDescOrder_whenAscendingFalse() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(contains("DESC"))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        QuestCompletionLogDAO.getCompletionHistory(mockConnection, UUID.randomUUID(), false);
        verify(mockConnection).prepareStatement(contains("DESC"));
    }
}
