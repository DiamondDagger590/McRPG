package us.eunoians.mcrpg.database.table.quest.scope;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.scope.impl.PermissionQuestScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PermissionQuestScopeDAOTest extends McRPGBaseTest {

    @DisplayName("Given a valid permission scope, when saving, then prepared statements are returned")
    @Test
    public void saveScope_validScope_returnsPreparedStatements() throws SQLException {
        Connection conn = mock(Connection.class);
        when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        UUID questUUID = UUID.randomUUID();
        PermissionQuestScope scope = new PermissionQuestScope(questUUID, "mcrpg.some.permission");

        List<PreparedStatement> result = PermissionQuestScopeDAO.saveScope(conn, scope);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @DisplayName("Given a valid permission scope, when saving, then connection is used")
    @Test
    public void saveScope_usesConnection() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(mockPs);

        UUID questUUID = UUID.randomUUID();
        PermissionQuestScope scope = new PermissionQuestScope(questUUID, "mcrpg.some.permission");

        PermissionQuestScopeDAO.saveScope(conn, scope);
        verify(conn).prepareStatement(anyString());
    }
}
