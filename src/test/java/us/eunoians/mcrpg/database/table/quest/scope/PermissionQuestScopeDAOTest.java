package us.eunoians.mcrpg.database.table.quest.scope;

import com.diamonddagger590.mccore.database.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.scope.impl.PermissionQuestScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PermissionQuestScopeDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveScope")
    class SaveScope {

        @DisplayName("Given a valid permission scope, when saving, then prepared statements are returned")
        @Test
        void saveScope_returnsPreparedStatements_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            UUID questUUID = UUID.randomUUID();
            PermissionQuestScope scope = new PermissionQuestScope(questUUID, "mcrpg.some.permission");

            List<PreparedStatement> result = PermissionQuestScopeDAO.saveScope(conn, scope);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @DisplayName("Given a valid permission scope, when saving, then connection is used to prepare a statement")
        @Test
        void saveScope_usesConnection_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID questUUID = UUID.randomUUID();
            PermissionQuestScope scope = new PermissionQuestScope(questUUID, "mcrpg.some.permission");

            PermissionQuestScopeDAO.saveScope(conn, scope);
            verify(conn).prepareStatement(anyString());
        }

        @DisplayName("Given a valid scope, when saving, then quest UUID and permission node are bound to the statement")
        @Test
        void saveScope_bindsParameters_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID questUUID = UUID.randomUUID();
            String permissionNode = "mcrpg.quest.vip";
            PermissionQuestScope scope = new PermissionQuestScope(questUUID, permissionNode);

            PermissionQuestScopeDAO.saveScope(conn, scope);

            verify(mockPs).setString(1, questUUID.toString());
            verify(mockPs).setString(2, permissionNode);
        }

        @DisplayName("Given a SQL exception on prepareStatement, when saving, then an empty list is returned")
        @Test
        void saveScope_returnsEmptyList_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            UUID questUUID = UUID.randomUUID();
            PermissionQuestScope scope = new PermissionQuestScope(questUUID, "mcrpg.quest.test");

            List<PreparedStatement> result = PermissionQuestScopeDAO.saveScope(conn, scope);
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given a valid scope, when saving, then exactly one prepared statement is returned")
        @Test
        void saveScope_returnsExactlyOneStatement_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            UUID questUUID = UUID.randomUUID();
            PermissionQuestScope scope = new PermissionQuestScope(questUUID, "mcrpg.quest.test");

            List<PreparedStatement> result = PermissionQuestScopeDAO.saveScope(conn, scope);
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getPermissionNode")
    class GetPermissionNode {

        @DisplayName("Given a matching row in the database, when getting permission node, then the node is returned")
        @Test
        void getPermissionNode_returnsNode_whenRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.elite");

            UUID questUUID = UUID.randomUUID();
            String result = PermissionQuestScopeDAO.getPermissionNode(conn, questUUID);

            assertEquals("mcrpg.quest.elite", result);
        }

        @DisplayName("Given a matching row, when getting permission node, then the quest UUID is bound to the query")
        @Test
        void getPermissionNode_bindsQuestUUID_whenQuerying() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.test");

            UUID questUUID = UUID.randomUUID();
            PermissionQuestScopeDAO.getPermissionNode(conn, questUUID);

            verify(ps).setString(1, questUUID.toString());
        }

        @DisplayName("Given no matching row, when getting permission node, then IllegalStateException is thrown")
        @Test
        void getPermissionNode_throwsIllegalState_whenNoRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            UUID questUUID = UUID.randomUUID();

            assertThrows(IllegalStateException.class,
                    () -> PermissionQuestScopeDAO.getPermissionNode(conn, questUUID));
        }
    }

    @Nested
    @DisplayName("findAllActivePermissionQuests")
    class FindAllActivePermissionQuests {

        @DisplayName("Given active permission quests, when finding all, then a map of UUID to permission node is returned")
        @Test
        void findAllActivePermissionQuests_returnsMap_whenRowsExist() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("quest_uuid")).thenReturn(questUUID1.toString(), questUUID2.toString());
            when(rs.getString("permission_node")).thenReturn("mcrpg.quest.vip", "mcrpg.quest.admin");

            Map<UUID, String> result = PermissionQuestScopeDAO.findAllActivePermissionQuests(conn);

            assertEquals(2, result.size());
            assertEquals("mcrpg.quest.vip", result.get(questUUID1));
            assertEquals("mcrpg.quest.admin", result.get(questUUID2));
        }

        @DisplayName("Given no active permission quests, when finding all, then an empty map is returned")
        @Test
        void findAllActivePermissionQuests_returnsEmptyMap_whenNoRowsExist() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Map<UUID, String> result = PermissionQuestScopeDAO.findAllActivePermissionQuests(conn);

            assertTrue(result.isEmpty());
        }

        @DisplayName("Given a SQL exception, when finding all active quests, then an empty map is returned")
        @Test
        void findAllActivePermissionQuests_returnsEmptyMap_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            Map<UUID, String> result = PermissionQuestScopeDAO.findAllActivePermissionQuests(conn);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @DisplayName("Given the table already exists, when attempting to create, then false is returned")
        @Test
        void attemptCreateTable_returnsFalse_whenTableAlreadyExists() {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, "mcrpg_permission_quest_scope")).thenReturn(true);

            boolean result = PermissionQuestScopeDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }

        @DisplayName("Given the table does not exist, when attempting to create, then a CREATE TABLE statement is executed")
        @Test
        void attemptCreateTable_executesCreateStatement_whenTableDoesNotExist() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(database.tableExists(conn, "mcrpg_permission_quest_scope")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = PermissionQuestScopeDAO.attemptCreateTable(conn, database);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @DisplayName("Given a SQL exception during creation, when attempting to create, then false is returned")
        @Test
        void attemptCreateTable_returnsFalse_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, "mcrpg_permission_quest_scope")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = PermissionQuestScopeDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }
    }
}
