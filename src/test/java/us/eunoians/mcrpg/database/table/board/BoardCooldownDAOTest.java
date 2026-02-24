package us.eunoians.mcrpg.database.table.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BoardCooldownDAOTest extends McRPGBaseTest {

    @DisplayName("saveCooldown returns prepared statements")
    @Test
    void saveCooldown_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = BoardCooldownDAO.saveCooldown(
                mockConnection,
                "cooldown-1",
                "rotation",
                "player",
                UUID.randomUUID().toString(),
                new NamespacedKey("mcrpg", "mine_stone"),
                new NamespacedKey("mcrpg", "daily_personal"),
                1000000L
        );

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("saveCooldown handles null keys")
    @Test
    void saveCooldown_handlesNullKeys() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        BoardCooldownDAO.saveCooldown(
                mockConnection,
                "cooldown-2",
                "rotation",
                "land",
                "land-uuid-123",
                null,
                null,
                2000000L
        );

        verify(mockStatement).setNull(eq(5), eq(java.sql.Types.VARCHAR));
        verify(mockStatement).setNull(eq(6), eq(java.sql.Types.VARCHAR));
    }

    @DisplayName("isOnCooldown returns false when no results")
    @Test
    void isOnCooldown_returnsFalseWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = BoardCooldownDAO.isOnCooldown(
                mockConnection,
                "rotation",
                "player",
                UUID.randomUUID().toString(),
                null,
                null
        );

        assertFalse(result);
    }

    @DisplayName("isOnCooldown returns true when present")
    @Test
    void isOnCooldown_returnsTrueWhenPresent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean result = BoardCooldownDAO.isOnCooldown(
                mockConnection,
                "rotation",
                "player",
                UUID.randomUUID().toString(),
                null,
                null
        );

        assertTrue(result);
    }

    @DisplayName("pruneExpiredCooldowns returns prepared statements")
    @Test
    void pruneExpiredCooldowns_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }
}
