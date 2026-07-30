package us.eunoians.mcrpg.database.table.quest.scope;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SinglePlayerQuestScopeDAO")
class SinglePlayerQuestScopeDAOTest extends McRPGBaseTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private Database mockDatabase;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockDatabase = mock(Database.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given table already exists, when attempting to create, then returns false")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            when(mockDatabase.tableExists(mockConnection, "mcrpg_single_player_quest_scope")).thenReturn(true);

            boolean result = SinglePlayerQuestScopeDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given table does not exist, when attempting to create, then creates table and returns true")
        void attemptCreateTable_returnsTrue_whenTableCreated() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "mcrpg_single_player_quest_scope")).thenReturn(false);
            when(mockStatement.executeUpdate()).thenReturn(0);

            boolean result = SinglePlayerQuestScopeDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockConnection).prepareStatement(anyString());
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given SQL exception during create, when attempting to create, then returns false")
        void attemptCreateTable_returnsFalse_whenSqlException() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "mcrpg_single_player_quest_scope")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("create failed"));

            boolean result = SinglePlayerQuestScopeDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("Given table is already at current version, when updating, then no version update is performed")
        void updateTable_noOp_whenAlreadyCurrentVersion() {
            try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class)) {
                tvhMock.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_single_player_quest_scope"))
                        .thenReturn(1);

                SinglePlayerQuestScopeDAO.updateTable(mockConnection);

                tvhMock.verify(() -> TableVersionHistoryDAO.setTableVersion(
                        mockConnection, "mcrpg_single_player_quest_scope", 1), org.mockito.Mockito.never());
            }
        }

        @Test
        @DisplayName("Given table is at version 0, when updating, then sets version to 1")
        void updateTable_setsVersion_whenAtVersion0() {
            try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class)) {
                tvhMock.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_single_player_quest_scope"))
                        .thenReturn(0);

                SinglePlayerQuestScopeDAO.updateTable(mockConnection);

                tvhMock.verify(() -> TableVersionHistoryDAO.setTableVersion(
                        mockConnection, "mcrpg_single_player_quest_scope", 1));
            }
        }
    }

    @Nested
    @DisplayName("findPlayerUuidForQuest")
    class FindPlayerUuidForQuest {

        @Test
        @DisplayName("Given a row exists, when finding player UUID, then returns the player UUID")
        void findPlayerUuidForQuest_returnsUuid_whenRowExists() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("player_uuid")).thenReturn(playerUUID.toString());

            Optional<UUID> result = SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(mockConnection, questUUID);

            assertTrue(result.isPresent());
            assertEquals(playerUUID, result.get());
            verify(mockStatement).setString(1, questUUID.toString());
        }

        @Test
        @DisplayName("Given no row exists, when finding player UUID, then returns empty Optional")
        void findPlayerUuidForQuest_returnsEmpty_whenNoRowFound() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(false);

            Optional<UUID> result = SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(mockConnection, questUUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given SQL exception, when finding player UUID, then throws RuntimeException")
        void findPlayerUuidForQuest_throwsRuntimeException_whenSqlException() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            assertThrows(RuntimeException.class,
                    () -> SinglePlayerQuestScopeDAO.findPlayerUuidForQuest(mockConnection, questUUID));
        }
    }

    @Nested
    @DisplayName("getPlayerInScope")
    class GetPlayerInScope {

        @Test
        @DisplayName("Given a player exists for quest, when getting player in scope, then returns the UUID")
        void getPlayerInScope_returnsUuid_whenPlayerExists() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("player_uuid")).thenReturn(playerUUID.toString());

            UUID result = SinglePlayerQuestScopeDAO.getPlayerInScope(mockConnection, questUUID);

            assertEquals(playerUUID, result);
        }

        @Test
        @DisplayName("Given no player exists for quest, when getting player in scope, then throws IllegalStateException")
        void getPlayerInScope_throwsIllegalState_whenNoPlayerFound() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(false);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> SinglePlayerQuestScopeDAO.getPlayerInScope(mockConnection, questUUID));
            assertTrue(exception.getMessage().contains(questUUID.toString()));
        }
    }

    @Nested
    @DisplayName("findActiveQuestsForPlayer")
    class FindActiveQuestsForPlayer {

        @Test
        @DisplayName("Given multiple active quests, when finding for player, then returns all quest UUIDs")
        void findActiveQuestsForPlayer_returnsAllUuids_whenMultipleRows() throws SQLException {
            UUID playerUUID = UUID.randomUUID();
            UUID quest1 = UUID.randomUUID();
            UUID quest2 = UUID.randomUUID();
            UUID quest3 = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true, true, true, false);
            when(mockResultSet.getString("quest_uuid")).thenReturn(
                    quest1.toString(), quest2.toString(), quest3.toString());

            List<UUID> result = SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(mockConnection, playerUUID);

            assertEquals(3, result.size());
            assertTrue(result.contains(quest1));
            assertTrue(result.contains(quest2));
            assertTrue(result.contains(quest3));
            verify(mockStatement).setString(1, playerUUID.toString());
        }

        @Test
        @DisplayName("Given no active quests, when finding for player, then returns empty list")
        void findActiveQuestsForPlayer_returnsEmptyList_whenNoRows() throws SQLException {
            UUID playerUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(false);

            List<UUID> result = SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(mockConnection, playerUUID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given SQL exception, when finding for player, then returns empty list")
        void findActiveQuestsForPlayer_returnsEmptyList_whenSqlException() throws SQLException {
            UUID playerUUID = UUID.randomUUID();
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            List<UUID> result = SinglePlayerQuestScopeDAO.findActiveQuestsForPlayer(mockConnection, playerUUID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveScope")
    class SaveScope {

        @Test
        @DisplayName("Given a valid scope with player, when saving, then returns prepared statements with correct bindings")
        void saveScope_returnsStatements_whenValidScope() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            UUID playerUUID = UUID.randomUUID();
            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(questUUID);
            scope.setPlayerInScope(playerUUID);

            List<PreparedStatement> result = SinglePlayerQuestScopeDAO.saveScope(mockConnection, scope);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(mockStatement).setString(1, questUUID.toString());
            verify(mockStatement).setString(2, playerUUID.toString());
        }

        @Test
        @DisplayName("Given an invalid scope without player, when saving, then throws QuestScopeInvalidStateException")
        void saveScope_throwsException_whenNoPlayer() {
            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(UUID.randomUUID());

            assertThrows(QuestScopeInvalidStateException.class,
                    () -> SinglePlayerQuestScopeDAO.saveScope(mockConnection, scope));
        }

        @Test
        @DisplayName("Given SQL exception during prepare, when saving, then returns empty list")
        void saveScope_returnsEmptyList_whenSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));

            UUID questUUID = UUID.randomUUID();
            SinglePlayerQuestScope scope = new SinglePlayerQuestScope(questUUID);
            scope.setPlayerInScope(UUID.randomUUID());

            List<PreparedStatement> result = SinglePlayerQuestScopeDAO.saveScope(mockConnection, scope);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
