package us.eunoians.mcrpg.database.table.quest.scope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SinglePlayerQuestScopeDAOTest extends McRPGBaseTest {

    @DisplayName("Given a valid single player scope, when saving, then prepared statements are returned")
    @Test
    public void saveScope_validScope_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());
        scope.setPlayerInScope(UUID.randomUUID());

        List<PreparedStatement> result = SinglePlayerQuestScopeDAO.saveScope(conn, scope);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given an invalid scope without player, when saving, then exception is thrown")
    @Test
    public void saveScope_invalidScope_throwsException() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());

        assertThrows(QuestScopeInvalidStateException.class,
                () -> SinglePlayerQuestScopeDAO.saveScope(conn, scope));
    }

    @DisplayName("Given a valid single player scope, when saving, then connection is used")
    @Test
    public void saveScope_usesConnection() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(mockPs);

        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());
        scope.setPlayerInScope(UUID.randomUUID());

        SinglePlayerQuestScopeDAO.saveScope(conn, scope);
        verify(conn).prepareStatement(anyString());
    }
}
