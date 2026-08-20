package us.eunoians.mcrpg.database.table.quest.scope;

import com.diamonddagger590.mccore.database.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.exception.quest.QuestScopeInvalidStateException;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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

public class SinglePlayerQuestScopeDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveScope")
    class SaveScope {

        @DisplayName("Given a valid single player scope, when saving, then prepared statements are returned")
        @Test
        void saveScope_returnsPreparedStatements_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());
            scope.setPlayerInScope(UUID.randomUUID());

            List<PreparedStatement> result = SinglePlayerQuestScopeDAO.saveScope(conn, scope);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @DisplayName("Given an invalid scope without player, when saving, then QuestScopeInvalidStateException is thrown")
        @Test
        void saveScope_throwsException_whenScopeIsInvalid() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());

            assertThrows(QuestScopeInvalidStateException.class,
                    () -> SinglePlayerQuestScopeDAO.saveScope(conn, scope));
        }

        @DisplayName("Given a valid scope, when saving, then connection is used to prepare a statement")
        @Test
        void saveScope_usesConnection_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());
            scope.setPlayerInScope(UUID.randomUUID());

            SinglePlayerQuestScopeDAO.saveScope(conn, scope);
            verify(conn).prepareStatement(anyString());
        }

        @DisplayName("Given a valid scope, when saving, then quest UUID and player UUID are bound to the statement")
        @Test
        void saveScope_bindsParameters_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID questUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();
            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(questUUID);
            scope.setPlayerInScope(playerUUID);

            SinglePlayerQuestScopeDAO.saveScope(conn, scope);

            verify(mockPs).setString(1, questUUID.toString());
            verify(mockPs).setString(2, playerUUID.toString());
        }

        @DisplayName("Given a valid scope, when saving, then exactly one prepared statement is returned")
        @Test
        void saveScope_returnsExactlyOneStatement_whenScopeIsValid() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());
            scope.setPlayerInScope(UUID.randomUUID());

            List<PreparedStatement> result = SinglePlayerQuestScopeDAO.saveScope(conn, scope);
            assertEquals(1, result.size());
        }

        @DisplayName("Given a SQL exception on prepareStatement, when saving a valid scope, then an empty list is returned")
        @Test
        void saveScope_returnsEmptyList_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());
            scope.setPlayerInScope(UUID.randomUUID());

            List<PreparedStatement> result = SinglePlayerQuestScopeDAO.saveScope(conn, scope);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findPlayerUuidForQuest")
    class FindPlayerUuidForQuest {

        @DisplayName("Given a matching row in the database, when finding player UUID, then the UUID is returned")
        @Test
        void findPlayerUuidForQuest_returnsUuid_whenRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            UUID playerUUID = UUID.randomUUID();
            when(rs.getString("player_uuid")).thenReturn(playerUUID.toString());

            UUID questUUID = UUID.randomUUID();
            Optional<UUID> result = SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(conn, questUUID);

            assertTrue(result.isPresent());
            assertEquals(playerUUID, result.orElseThrow());
        }

        @DisplayName("Given no matching row, when finding player UUID, then empty Optional is returned")
        @Test
        void findPlayerUuidForQuest_returnsEmpty_whenNoRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            UUID questUUID = UUID.randomUUID();
            Optional<UUID> result = SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(conn, questUUID);

            assertFalse(result.isPresent());
        }

        @DisplayName("Given a matching row, when finding player UUID, then quest UUID is bound to the query")
        @Test
        void findPlayerUuidForQuest_bindsQuestUuid_whenQuerying() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("player_uuid")).thenReturn(UUID.randomUUID().toString());

            UUID questUUID = UUID.randomUUID();
            SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(conn, questUUID);

            verify(ps).setString(1, questUUID.toString());
        }

        @DisplayName("Given a SQL exception, when finding player UUID, then RuntimeException is thrown")
        @Test
        void findPlayerUuidForQuest_throwsRuntimeException_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            UUID questUUID = UUID.randomUUID();

            assertThrows(RuntimeException.class,
                    () -> SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(conn, questUUID));
        }
    }

    @Nested
    @DisplayName("getPlayerInScope")
    class GetPlayerInScope {

        @DisplayName("Given a matching row, when getting player in scope, then the UUID is returned")
        @Test
        void getPlayerInScope_returnsUuid_whenRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            UUID playerUUID = UUID.randomUUID();
            when(rs.getString("player_uuid")).thenReturn(playerUUID.toString());

            UUID questUUID = UUID.randomUUID();
            UUID result = SinglePlayerQuestScopeDAO.getPlayerInScope(conn, questUUID);

            assertEquals(playerUUID, result);
        }

        @DisplayName("Given no matching row, when getting player in scope, then IllegalStateException is thrown")
        @Test
        void getPlayerInScope_throwsIllegalState_whenNoRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            UUID questUUID = UUID.randomUUID();

            assertThrows(IllegalStateException.class,
                    () -> SinglePlayerQuestScopeDAO.getPlayerInScope(conn, questUUID));
        }
    }

    @Nested
    @DisplayName("findActiveQuestsForPlayer")
    class FindActiveQuestsForPlayer {

        @DisplayName("Given active quests for a player, when finding all, then a list of quest UUIDs is returned")
        @Test
        void findActiveQuestsForPlayer_returnsList_whenRowsExist() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("quest_uuid")).thenReturn(questUUID1.toString(), questUUID2.toString());

            UUID playerUUID = UUID.randomUUID();
            List<UUID> result = SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(conn, playerUUID);

            assertEquals(2, result.size());
            assertEquals(questUUID1, result.get(0));
            assertEquals(questUUID2, result.get(1));
        }

        @DisplayName("Given no active quests for a player, when finding all, then an empty list is returned")
        @Test
        void findActiveQuestsForPlayer_returnsEmptyList_whenNoRowsExist() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            UUID playerUUID = UUID.randomUUID();
            List<UUID> result = SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(conn, playerUUID);

            assertTrue(result.isEmpty());
        }

        @DisplayName("Given active quests, when finding all, then player UUID is bound to the query")
        @Test
        void findActiveQuestsForPlayer_bindsPlayerUuid_whenQuerying() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            UUID playerUUID = UUID.randomUUID();
            SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(conn, playerUUID);

            verify(ps).setString(1, playerUUID.toString());
        }

        @DisplayName("Given a SQL exception, when finding active quests, then an empty list is returned")
        @Test
        void findActiveQuestsForPlayer_returnsEmptyList_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            UUID playerUUID = UUID.randomUUID();
            List<UUID> result = SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(conn, playerUUID);

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
            when(database.tableExists(conn, "mcrpg_single_player_quest_scope")).thenReturn(true);

            boolean result = SinglePlayerQuestScopeDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }

        @DisplayName("Given the table does not exist, when attempting to create, then a CREATE TABLE statement is executed")
        @Test
        void attemptCreateTable_executesCreateStatement_whenTableDoesNotExist() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(database.tableExists(conn, "mcrpg_single_player_quest_scope")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = SinglePlayerQuestScopeDAO.attemptCreateTable(conn, database);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @DisplayName("Given a SQL exception during creation, when attempting to create, then false is returned")
        @Test
        void attemptCreateTable_returnsFalse_whenSqlExceptionOccurs() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, "mcrpg_single_player_quest_scope")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = SinglePlayerQuestScopeDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }
    }
}
