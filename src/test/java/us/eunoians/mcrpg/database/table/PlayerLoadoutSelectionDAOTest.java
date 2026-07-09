package us.eunoians.mcrpg.database.table;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link PlayerLoadoutSelectionDAO}.
 */
class PlayerLoadoutSelectionDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Test
    @DisplayName("getActiveLoadout returns stored loadout id")
    void getActiveLoadout_returnsStoredId() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("active_loadout_id")).thenReturn(3);

        int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

        assertEquals(3, result);
    }

    @Test
    @DisplayName("getActiveLoadout binds UUID parameter")
    void getActiveLoadout_bindsUuidParameter() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("active_loadout_id")).thenReturn(1);

        PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
    }

    @Test
    @DisplayName("getActiveLoadout returns minimum of 1 when stored value is zero")
    void getActiveLoadout_returnsMinimumOne_whenStoredValueIsZero() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("active_loadout_id")).thenReturn(0);

        int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("getActiveLoadout returns minimum of 1 when stored value is negative")
    void getActiveLoadout_returnsMinimumOne_whenStoredValueIsNegative() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("active_loadout_id")).thenReturn(-5);

        int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("getActiveLoadout defaults to 1 on SQL exception")
    void getActiveLoadout_defaultsToOne_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("setActiveLoadout uses REPLACE INTO and binds UUID and loadout id")
    void setActiveLoadout_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoadoutSelectionDAO.setActiveLoadout(mockConnection, PLAYER_UUID, 2);

        assertFalse(result.isEmpty());
        verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("REPLACE INTO"));
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, 2);
    }

    @Test
    @DisplayName("setActiveLoadout returns list with one prepared statement")
    void setActiveLoadout_returnsSingleStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoadoutSelectionDAO.setActiveLoadout(mockConnection, PLAYER_UUID, 1);

        assertEquals(1, result.size());
        assertTrue(result.contains(mockStatement));
    }

    @Test
    @DisplayName("getActiveLoadout returns 1 when no row exists")
    void getActiveLoadout_returnsOne_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        when(mockResultSet.getInt("active_loadout_id")).thenReturn(0);

        int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

        assertEquals(1, result);
    }

    @Test
    @DisplayName("setActiveLoadout returns empty list on SQL exception")
    void setActiveLoadout_returnsEmptyList_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        List<PreparedStatement> result = PlayerLoadoutSelectionDAO.setActiveLoadout(mockConnection, PLAYER_UUID, 2);

        assertTrue(result.isEmpty());
    }
}
