package us.eunoians.mcrpg.database.table.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.BoardRotation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BoardRotationDAOTest extends McRPGBaseTest {

    @DisplayName("saveRotation returns prepared statements")
    @Test
    void saveRotation_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        BoardRotation rotation = new BoardRotation(
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "main_board"),
                new NamespacedKey("mcrpg", "daily"),
                1L,
                1000L,
                86400000L
        );

        var statements = BoardRotationDAO.saveRotation(mockConnection, rotation);

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("saveRotation binds all parameters")
    @Test
    void saveRotation_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        UUID rotationId = UUID.randomUUID();
        BoardRotation rotation = new BoardRotation(
                rotationId,
                new NamespacedKey("mcrpg", "main_board"),
                new NamespacedKey("mcrpg", "daily"),
                1L,
                1000L,
                86400000L
        );

        BoardRotationDAO.saveRotation(mockConnection, rotation);

        verify(mockStatement).setString(1, rotationId.toString());
        verify(mockStatement).setString(2, "mcrpg:main_board");
        verify(mockStatement).setString(3, "mcrpg:daily");
        verify(mockStatement).setLong(4, 1L);
        verify(mockStatement).setLong(5, 1000L);
        verify(mockStatement).setLong(6, 86400000L);
    }

    @DisplayName("loadCurrentRotation returns empty when no results")
    @Test
    void loadCurrentRotation_returnsEmptyWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<BoardRotation> result = BoardRotationDAO.loadCurrentRotation(
                mockConnection,
                new NamespacedKey("mcrpg", "main_board"),
                new NamespacedKey("mcrpg", "daily")
        );

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadCurrentRotation returns rotation when present")
    @Test
    void loadCurrentRotation_returnsRotationWhenPresent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        UUID rotationId = UUID.randomUUID();
        when(mockResultSet.getString("rotation_id")).thenReturn(rotationId.toString());
        when(mockResultSet.getLong("rotation_epoch")).thenReturn(2L);
        when(mockResultSet.getLong("started_at")).thenReturn(2000L);
        when(mockResultSet.getLong("expires_at")).thenReturn(90000000L);

        NamespacedKey boardKey = new NamespacedKey("mcrpg", "main_board");
        NamespacedKey refreshTypeKey = new NamespacedKey("mcrpg", "daily");
        Optional<BoardRotation> result = BoardRotationDAO.loadCurrentRotation(mockConnection, boardKey, refreshTypeKey);

        assertTrue(result.isPresent());
        BoardRotation rotation = result.get();
        assertEquals(rotationId, rotation.getRotationId());
        assertEquals(boardKey, rotation.getBoardKey());
        assertEquals(refreshTypeKey, rotation.getRefreshTypeKey());
        assertEquals(2L, rotation.getRotationEpoch());
        assertEquals(2000L, rotation.getStartedAt());
        assertEquals(90000000L, rotation.getExpiresAt());
    }

    @DisplayName("loadRotationById returns empty when no results")
    @Test
    void loadRotationById_returnsEmptyWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<BoardRotation> result = BoardRotationDAO.loadRotationById(mockConnection, UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadRotationById returns rotation when present")
    @Test
    void loadRotationById_returnsRotationWhenPresent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        UUID rotationId = UUID.randomUUID();
        when(mockResultSet.getString("board_key")).thenReturn("mcrpg:main_board");
        when(mockResultSet.getString("refresh_type_key")).thenReturn("mcrpg:weekly");
        when(mockResultSet.getLong("rotation_epoch")).thenReturn(3L);
        when(mockResultSet.getLong("started_at")).thenReturn(3000L);
        when(mockResultSet.getLong("expires_at")).thenReturn(95000000L);

        Optional<BoardRotation> result = BoardRotationDAO.loadRotationById(mockConnection, rotationId);

        assertTrue(result.isPresent());
        BoardRotation rotation = result.get();
        assertEquals(rotationId, rotation.getRotationId());
        assertEquals(new NamespacedKey("mcrpg", "main_board"), rotation.getBoardKey());
        assertEquals(new NamespacedKey("mcrpg", "weekly"), rotation.getRefreshTypeKey());
        assertEquals(3L, rotation.getRotationEpoch());
        assertEquals(3000L, rotation.getStartedAt());
        assertEquals(95000000L, rotation.getExpiresAt());
    }
}
