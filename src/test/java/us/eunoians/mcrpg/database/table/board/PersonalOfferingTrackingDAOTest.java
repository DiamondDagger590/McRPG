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
import static org.mockito.Mockito.*;

public class PersonalOfferingTrackingDAOTest extends McRPGBaseTest {

    @DisplayName("markGenerated returns prepared statements")
    @Test
    void markGenerated_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = PersonalOfferingTrackingDAO.markGenerated(
                mockConnection,
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "default"),
                UUID.randomUUID()
        );

        assertNotNull(statements);
        assertEquals(1, statements.size());
    }

    @DisplayName("hasGenerated returns false when no record exists")
    @Test
    void hasGenerated_returnsFalse_whenNoRecord() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        boolean result = PersonalOfferingTrackingDAO.hasGenerated(
                mockConnection,
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "default"),
                UUID.randomUUID()
        );

        assertFalse(result);
    }

    @DisplayName("hasGenerated returns true when record exists")
    @Test
    void hasGenerated_returnsTrue_whenRecordExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        boolean result = PersonalOfferingTrackingDAO.hasGenerated(
                mockConnection,
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "default"),
                UUID.randomUUID()
        );

        assertTrue(result);
    }

    @DisplayName("pruneForExpiredRotations returns one statement per rotation")
    @Test
    void pruneForExpiredRotations_returnsStatementsPerRotation() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<UUID> expiredIds = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        List<PreparedStatement> statements = PersonalOfferingTrackingDAO.pruneForExpiredRotations(
                mockConnection, expiredIds);

        assertNotNull(statements);
        assertEquals(3, statements.size());
    }

    @DisplayName("pruneForExpiredRotations returns empty for empty input")
    @Test
    void pruneForExpiredRotations_returnsEmpty_whenNoRotations() {
        Connection mockConnection = mock(Connection.class);

        List<PreparedStatement> statements = PersonalOfferingTrackingDAO.pruneForExpiredRotations(
                mockConnection, List.of());

        assertNotNull(statements);
        assertTrue(statements.isEmpty());
    }
}
