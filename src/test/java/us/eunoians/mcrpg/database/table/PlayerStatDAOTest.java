package us.eunoians.mcrpg.database.table;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link PlayerStatDAO}.
 * <p>
 * Follows the pattern established by
 * {@link us.eunoians.mcrpg.database.table.board.BoardRotationDAOTest}: the JDBC layer
 * is fully mocked so the tests run without a real database.
 */
class PlayerStatDAOTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = new NamespacedKey("mcrpg", "mana");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    // saveStat

    @DisplayName("saveStat uses REPLACE INTO and binds all three parameters")
    @Test
    void saveStat_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        PlayerStatDAO.saveStat(mockConnection, PLAYER_UUID, MANA_KEY, 75.0);

        verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("REPLACE INTO"));
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setString(2, MANA_KEY.toString());
        verify(mockStatement).setDouble(3, 75.0);
    }

    @DisplayName("saveStat returns the prepared statement")
    @Test
    void saveStat_returnsPreparedStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        PreparedStatement result = PlayerStatDAO.saveStat(mockConnection, PLAYER_UUID, MANA_KEY, 50.0);

        assertEquals(mockStatement, result);
    }

    // loadStat

    @DisplayName("loadStat returns empty when no row exists")
    @Test
    void loadStat_returnsEmptyWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<Double> result = PlayerStatDAO.loadStat(mockConnection, PLAYER_UUID, MANA_KEY);

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadStat returns the persisted value when a row exists")
    @Test
    void loadStat_returnsValueWhenPresent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getDouble("current_value")).thenReturn(42.0);

        Optional<Double> result = PlayerStatDAO.loadStat(mockConnection, PLAYER_UUID, MANA_KEY);

        assertTrue(result.isPresent());
        assertEquals(42.0, result.get());
    }

    // loadAllStats

    @DisplayName("loadAllStats returns empty map when no rows exist")
    @Test
    void loadAllStats_returnsEmptyWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadAllStats silently skips rows whose stat_key cannot be parsed as a NamespacedKey")
    @Test
    void loadAllStats_skipsNullKey() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        // Row 1: invalid key (spaces → NamespacedKey.fromString returns null) — getDouble NOT called
        // Row 2: valid key — getDouble IS called (first and only call, returns 88.0)
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("stat_key"))
                .thenReturn("not a valid key", MANA_KEY.toString());
        when(mockResultSet.getDouble("current_value"))
                .thenReturn(88.0);

        Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

        assertFalse(result.containsKey(null));
        assertEquals(1, result.size());
        assertEquals(88.0, result.get(MANA_KEY));
    }

    @DisplayName("loadAllStats returns all valid rows")
    @Test
    void loadAllStats_returnsAllValidRows() throws SQLException {
        NamespacedKey healthKey = new NamespacedKey("mcrpg", "health");
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("stat_key"))
                .thenReturn(MANA_KEY.toString(), healthKey.toString());
        when(mockResultSet.getDouble("current_value"))
                .thenReturn(60.0, 100.0);

        Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

        assertEquals(2, result.size());
        assertEquals(60.0, result.get(MANA_KEY));
        assertEquals(100.0, result.get(healthKey));
    }
}
