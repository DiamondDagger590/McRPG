package us.eunoians.mcrpg.database.table;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link PlayerLoginTimeDAO}.
 */
class PlayerLoginTimeDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final Instant TEST_INSTANT = Instant.parse("2024-06-15T10:30:00Z");

    @Test
    @DisplayName("hasPlayerLoggedInBefore returns true when row exists")
    void hasPlayerLoggedInBefore_returnsTrue_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean result = PlayerLoginTimeDAO.hasPlayerLoggedInBefore(mockConnection, PLAYER_UUID);

        assertTrue(result);
    }

    @Test
    @DisplayName("hasPlayerLoggedInBefore returns false when no row exists")
    void hasPlayerLoggedInBefore_returnsFalse_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = PlayerLoginTimeDAO.hasPlayerLoggedInBefore(mockConnection, PLAYER_UUID);

        assertFalse(result);
    }

    @Test
    @DisplayName("hasPlayerLoggedInBefore binds UUID parameter")
    void hasPlayerLoggedInBefore_bindsUuidParameter() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        PlayerLoginTimeDAO.hasPlayerLoggedInBefore(mockConnection, PLAYER_UUID);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
    }

    @Test
    @DisplayName("getFirstLoginTime returns empty when no row exists")
    void getFirstLoginTime_returnsEmpty_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Instant> result = PlayerLoginTimeDAO.getFirstLoginTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getFirstLoginTime returns instant when row exists")
    void getFirstLoginTime_returnsInstant_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getTimestamp("first_login_time")).thenReturn(Timestamp.from(TEST_INSTANT));

        Optional<Instant> result = PlayerLoginTimeDAO.getFirstLoginTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isPresent());
        assertEquals(TEST_INSTANT, result.get());
    }

    @Test
    @DisplayName("getLastLogoutTime returns empty when no row exists")
    void getLastLogoutTime_returnsEmpty_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Instant> result = PlayerLoginTimeDAO.getLastLogoutTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLastLogoutTime returns instant when row exists")
    void getLastLogoutTime_returnsInstant_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getTimestamp("last_logout_time")).thenReturn(Timestamp.from(TEST_INSTANT));

        Optional<Instant> result = PlayerLoginTimeDAO.getLastLogoutTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isPresent());
        assertEquals(TEST_INSTANT, result.get());
    }

    @Test
    @DisplayName("getLastLoginTime returns empty when no row exists")
    void getLastLoginTime_returnsEmpty_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Instant> result = PlayerLoginTimeDAO.getLastLoginTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLastLoginTime returns instant when row exists")
    void getLastLoginTime_returnsInstant_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getTimestamp("last_login_time")).thenReturn(Timestamp.from(TEST_INSTANT));

        Optional<Instant> result = PlayerLoginTimeDAO.getLastLoginTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isPresent());
        assertEquals(TEST_INSTANT, result.get());
    }

    @Test
    @DisplayName("getLastSeenTime returns empty when no row exists")
    void getLastSeenTime_returnsEmpty_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Instant> result = PlayerLoginTimeDAO.getLastSeenTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLastSeenTime returns instant when row exists")
    void getLastSeenTime_returnsInstant_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getTimestamp("last_seen_time")).thenReturn(Timestamp.from(TEST_INSTANT));

        Optional<Instant> result = PlayerLoginTimeDAO.getLastSeenTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isPresent());
        assertEquals(TEST_INSTANT, result.get());
    }

    @Test
    @DisplayName("didPlayerLogoutInSafeZone returns false when no row exists")
    void didPlayerLogoutInSafeZone_returnsFalse_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = PlayerLoginTimeDAO.didPlayerLogoutInSafeZone(mockConnection, PLAYER_UUID);

        assertFalse(result);
    }

    @Test
    @DisplayName("didPlayerLogoutInSafeZone returns true when stored value is true")
    void didPlayerLogoutInSafeZone_returnsTrue_whenStoredValueIsTrue() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getBoolean("logged_out_in_safezone")).thenReturn(true);

        boolean result = PlayerLoginTimeDAO.didPlayerLogoutInSafeZone(mockConnection, PLAYER_UUID);

        assertTrue(result);
    }

    @Test
    @DisplayName("saveFirstLoginTime binds UUID and timestamp parameters")
    void saveFirstLoginTime_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveFirstLoginTime(mockConnection, PLAYER_UUID, TEST_INSTANT);

        assertFalse(result.isEmpty());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setTimestamp(2, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(3, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(4, Timestamp.from(TEST_INSTANT));
    }

    @Test
    @DisplayName("saveFirstLoginTime returns list with one prepared statement")
    void saveFirstLoginTime_returnsSingleStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveFirstLoginTime(mockConnection, PLAYER_UUID, TEST_INSTANT);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("saveLastLogoutTime binds UUID and timestamp parameters")
    void saveLastLogoutTime_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveLastLogoutTime(mockConnection, PLAYER_UUID, TEST_INSTANT);

        assertFalse(result.isEmpty());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setTimestamp(2, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(3, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(4, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(5, Timestamp.from(TEST_INSTANT));
    }

    @Test
    @DisplayName("saveLastLoginTime binds UUID and timestamp parameters")
    void saveLastLoginTime_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveLastLoginTime(mockConnection, PLAYER_UUID, TEST_INSTANT);

        assertFalse(result.isEmpty());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setTimestamp(2, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(3, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(4, Timestamp.from(TEST_INSTANT));
    }

    @Test
    @DisplayName("saveLastSeenTime binds UUID and timestamp parameters")
    void saveLastSeenTime_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveLastSeenTime(mockConnection, PLAYER_UUID, TEST_INSTANT);

        assertFalse(result.isEmpty());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setTimestamp(2, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(3, Timestamp.from(TEST_INSTANT));
        verify(mockStatement).setTimestamp(4, Timestamp.from(TEST_INSTANT));
    }

    @Test
    @DisplayName("saveLoggedOutInSafeZone binds UUID, timestamps from TimeProvider, and boolean true")
    void saveLoggedOutInSafeZone_bindsAllParameters_whenTrue() throws SQLException {
        Instant fixedNow = Instant.parse("2024-07-01T12:00:00Z");
        when(mcRPG.getTimeProvider().now()).thenReturn(fixedNow);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveLoggedOutInSafeZone(mockConnection, PLAYER_UUID, true);

        assertFalse(result.isEmpty());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setTimestamp(2, Timestamp.from(fixedNow));
        verify(mockStatement).setTimestamp(3, Timestamp.from(fixedNow));
        verify(mockStatement).setTimestamp(4, Timestamp.from(fixedNow));
        verify(mockStatement).setBoolean(5, true);
    }

    @Test
    @DisplayName("saveLoggedOutInSafeZone binds boolean false correctly")
    void saveLoggedOutInSafeZone_bindsBoolean_whenFalse() throws SQLException {
        Instant fixedNow = Instant.parse("2024-07-01T12:00:00Z");
        when(mcRPG.getTimeProvider().now()).thenReturn(fixedNow);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = PlayerLoginTimeDAO.saveLoggedOutInSafeZone(mockConnection, PLAYER_UUID, false);

        assertEquals(1, result.size());
        verify(mockStatement).setBoolean(5, false);
    }

    @Test
    @DisplayName("hasPlayerLoggedInBefore returns false on SQL exception")
    void hasPlayerLoggedInBefore_returnsFalse_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        boolean result = PlayerLoginTimeDAO.hasPlayerLoggedInBefore(mockConnection, PLAYER_UUID);

        assertFalse(result);
    }

    @Test
    @DisplayName("getFirstLoginTime returns empty on SQL exception")
    void getFirstLoginTime_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        Optional<Instant> result = PlayerLoginTimeDAO.getFirstLoginTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLastLogoutTime returns empty on SQL exception")
    void getLastLogoutTime_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        Optional<Instant> result = PlayerLoginTimeDAO.getLastLogoutTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLastLoginTime returns empty on SQL exception")
    void getLastLoginTime_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        Optional<Instant> result = PlayerLoginTimeDAO.getLastLoginTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLastSeenTime returns empty on SQL exception")
    void getLastSeenTime_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        Optional<Instant> result = PlayerLoginTimeDAO.getLastSeenTime(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("didPlayerLogoutInSafeZone returns false on SQL exception")
    void didPlayerLogoutInSafeZone_returnsFalse_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        boolean result = PlayerLoginTimeDAO.didPlayerLogoutInSafeZone(mockConnection, PLAYER_UUID);

        assertFalse(result);
    }

    @Test
    @DisplayName("didPlayerLogoutInSafeZone returns false when stored value is false")
    void didPlayerLogoutInSafeZone_returnsFalse_whenStoredValueIsFalse() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getBoolean("logged_out_in_safezone")).thenReturn(false);

        boolean result = PlayerLoginTimeDAO.didPlayerLogoutInSafeZone(mockConnection, PLAYER_UUID);

        assertFalse(result);
    }
}
