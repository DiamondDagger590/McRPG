package us.eunoians.mcrpg.database.table;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;

import java.sql.Connection;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link PlayerExperienceExtrasDAO}.
 */
class PlayerExperienceExtrasDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Test
    @DisplayName("getPlayerExperienceExtras returns default values when no row exists")
    void getPlayerExperienceExtras_returnsDefaults_whenNoRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        PlayerExperienceExtras result = PlayerExperienceExtrasDAO.getPlayerExperienceExtras(mockConnection, PLAYER_UUID);

        assertNotNull(result);
        assertEquals(0, result.getRedeemableExperience());
        assertEquals(0, result.getRedeemableLevels());
        assertEquals(0, result.getBoostedExperience());
        assertEquals(0.0f, result.getRestedExperience());
    }

    @Test
    @DisplayName("getPlayerExperienceExtras binds UUID parameter")
    void getPlayerExperienceExtras_bindsUuidParameter() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        PlayerExperienceExtrasDAO.getPlayerExperienceExtras(mockConnection, PLAYER_UUID);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
    }

    @Test
    @DisplayName("getPlayerExperienceExtras populates all fields from result set")
    void getPlayerExperienceExtras_populatesAllFields_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("redeemable_experience")).thenReturn(100);
        when(mockResultSet.getInt("redeemable_levels")).thenReturn(5);
        when(mockResultSet.getInt("boosted_experience")).thenReturn(200);
        when(mockResultSet.getFloat("rested_experience")).thenReturn(1.5f);

        PlayerExperienceExtras result = PlayerExperienceExtrasDAO.getPlayerExperienceExtras(mockConnection, PLAYER_UUID);

        assertEquals(100, result.getRedeemableExperience());
        assertEquals(5, result.getRedeemableLevels());
        assertEquals(200, result.getBoostedExperience());
        assertEquals(1.5f, result.getRestedExperience());
    }

    @Test
    @DisplayName("savePlayerExperienceExtras uses REPLACE INTO and binds all five parameters")
    void savePlayerExperienceExtras_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        PlayerExperienceExtras extras = new PlayerExperienceExtras(150, 10, 300, 2.5f);
        List<PreparedStatement> result = PlayerExperienceExtrasDAO.savePlayerExperienceExtras(mockConnection, PLAYER_UUID, extras);

        assertFalse(result.isEmpty());
        verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("REPLACE INTO"));
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, 150);
        verify(mockStatement).setInt(3, 10);
        verify(mockStatement).setInt(4, 300);
        verify(mockStatement).setFloat(5, 2.5f);
    }

    @Test
    @DisplayName("savePlayerExperienceExtras returns list with one prepared statement")
    void savePlayerExperienceExtras_returnsSingleStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        PlayerExperienceExtras extras = new PlayerExperienceExtras();
        List<PreparedStatement> result = PlayerExperienceExtrasDAO.savePlayerExperienceExtras(mockConnection, PLAYER_UUID, extras);

        assertEquals(1, result.size());
        assertTrue(result.contains(mockStatement));
    }

    @Test
    @DisplayName("getPlayerExperienceExtras returns defaults on SQL exception")
    void getPlayerExperienceExtras_returnsDefaults_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        PlayerExperienceExtras result = PlayerExperienceExtrasDAO.getPlayerExperienceExtras(mockConnection, PLAYER_UUID);

        assertNotNull(result);
        assertEquals(0, result.getRedeemableExperience());
        assertEquals(0, result.getRedeemableLevels());
        assertEquals(0, result.getBoostedExperience());
        assertEquals(0.0f, result.getRestedExperience());
    }

    @Test
    @DisplayName("savePlayerExperienceExtras returns empty list on SQL exception")
    void savePlayerExperienceExtras_returnsEmptyList_whenSqlExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

        PlayerExperienceExtras extras = new PlayerExperienceExtras(100, 5, 200, 1.5f);
        List<PreparedStatement> result = PlayerExperienceExtrasDAO.savePlayerExperienceExtras(mockConnection, PLAYER_UUID, extras);

        assertTrue(result.isEmpty());
    }
}
