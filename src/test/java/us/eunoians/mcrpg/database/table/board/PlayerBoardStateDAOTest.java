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

public class PlayerBoardStateDAOTest extends McRPGBaseTest {

    @DisplayName("saveState returns prepared statements")
    @Test
    void saveState_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        UUID playerUUID = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        UUID questInstanceUUID = UUID.randomUUID();
        NamespacedKey boardKey = new NamespacedKey("mcrpg", "main_board");
        long acceptedAt = System.currentTimeMillis();

        List<PreparedStatement> statements = PlayerBoardStateDAO.saveState(
                mockConnection,
                playerUUID,
                boardKey,
                offeringId,
                "ACCEPTED",
                acceptedAt,
                questInstanceUUID
        );

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("saveState handles null optional fields")
    @Test
    void saveState_handlesNullOptionalFields() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        UUID playerUUID = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        NamespacedKey boardKey = new NamespacedKey("mcrpg", "main_board");

        PlayerBoardStateDAO.saveState(mockConnection, playerUUID, boardKey, offeringId, "VISIBLE", null, null);

        verify(mockStatement).setNull(eq(5), eq(java.sql.Types.BIGINT));
        verify(mockStatement).setNull(eq(6), eq(java.sql.Types.VARCHAR));
    }

    @DisplayName("countActiveQuestsFromBoard returns zero when no results")
    @Test
    void countActiveQuestsFromBoard_returnsZeroWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(
                mockConnection,
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "main_board")
        );

        assertEquals(0, result);
    }

    @DisplayName("countActiveQuestsFromBoard returns count when present")
    @Test
    void countActiveQuestsFromBoard_returnsCountWhenPresent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt(1)).thenReturn(3);

        int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(
                mockConnection,
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "main_board")
        );

        assertEquals(3, result);
    }
}
