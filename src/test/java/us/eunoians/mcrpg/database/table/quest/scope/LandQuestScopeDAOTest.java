package us.eunoians.mcrpg.database.table.quest.scope;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.scope.impl.LandQuestScope;

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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LandQuestScopeDAO")
class LandQuestScopeDAOTest extends McRPGBaseTest {

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
            when(mockDatabase.tableExists(mockConnection, "mcrpg_land_quest_scope")).thenReturn(true);

            boolean result = LandQuestScopeDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given table does not exist, when attempting to create, then creates table and returns true")
        void attemptCreateTable_returnsTrue_whenTableCreated() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "mcrpg_land_quest_scope")).thenReturn(false);
            when(mockStatement.executeUpdate()).thenReturn(0);

            boolean result = LandQuestScopeDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockConnection).prepareStatement(anyString());
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Given SQL exception during create, when attempting to create, then returns false")
        void attemptCreateTable_returnsFalse_whenSqlException() throws SQLException {
            when(mockDatabase.tableExists(mockConnection, "mcrpg_land_quest_scope")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("create failed"));

            boolean result = LandQuestScopeDAO.attemptCreateTable(mockConnection, mockDatabase);

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
                tvhMock.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_land_quest_scope"))
                        .thenReturn(1);

                LandQuestScopeDAO.updateTable(mockConnection);

                tvhMock.verify(() -> TableVersionHistoryDAO.setTableVersion(
                        mockConnection, "mcrpg_land_quest_scope", 1), org.mockito.Mockito.never());
            }
        }

        @Test
        @DisplayName("Given table is at version 0, when updating, then sets version to 1")
        void updateTable_setsVersion_whenAtVersion0() {
            try (MockedStatic<TableVersionHistoryDAO> tvhMock = mockStatic(TableVersionHistoryDAO.class)) {
                tvhMock.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, "mcrpg_land_quest_scope"))
                        .thenReturn(0);

                LandQuestScopeDAO.updateTable(mockConnection);

                tvhMock.verify(() -> TableVersionHistoryDAO.setTableVersion(
                        mockConnection, "mcrpg_land_quest_scope", 1));
            }
        }
    }

    @Nested
    @DisplayName("getLandName")
    class GetLandName {

        @Test
        @DisplayName("Given a row exists, when getting land name, then returns the land name string")
        void getLandName_returnsLandName_whenRowExists() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("land_name")).thenReturn("TestLand");

            String result = LandQuestScopeDAO.getLandName(mockConnection, questUUID);

            assertEquals("TestLand", result);
            verify(mockStatement).setString(1, questUUID.toString());
        }

        @Test
        @DisplayName("Given no row exists, when getting land name, then throws IllegalStateException")
        void getLandName_throwsIllegalState_whenNoRowFound() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(false);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> LandQuestScopeDAO.getLandName(mockConnection, questUUID));
            assertTrue(exception.getMessage().contains(questUUID.toString()));
        }

        @Test
        @DisplayName("Given SQL exception, when getting land name, then throws RuntimeException wrapping SQLException")
        void getLandName_throwsRuntimeException_whenSqlException() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            assertThrows(RuntimeException.class,
                    () -> LandQuestScopeDAO.getLandName(mockConnection, questUUID));
        }
    }

    @Nested
    @DisplayName("findAllActiveLandQuests")
    class FindAllActiveLandQuests {

        @Test
        @DisplayName("Given multiple active quests, when finding all, then returns all entries")
        void findAllActiveLandQuests_returnsAllEntries_whenMultipleRows() throws SQLException {
            UUID quest1 = UUID.randomUUID();
            UUID quest2 = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getString("quest_uuid")).thenReturn(quest1.toString(), quest2.toString());
            when(mockResultSet.getString("land_name")).thenReturn("LandA", "LandB");

            Map<UUID, String> result = LandQuestScopeDAO.findAllActiveLandQuests(mockConnection);

            assertEquals(2, result.size());
            assertEquals("LandA", result.get(quest1));
            assertEquals("LandB", result.get(quest2));
        }

        @Test
        @DisplayName("Given no active quests, when finding all, then returns empty map")
        void findAllActiveLandQuests_returnsEmptyMap_whenNoRows() throws SQLException {
            when(mockResultSet.next()).thenReturn(false);

            Map<UUID, String> result = LandQuestScopeDAO.findAllActiveLandQuests(mockConnection);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a single active quest, when finding all, then returns single entry with correct binding")
        void findAllActiveLandQuests_returnsSingleEntry_whenOneRow() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString("quest_uuid")).thenReturn(questUUID.toString());
            when(mockResultSet.getString("land_name")).thenReturn("MySingleLand");

            Map<UUID, String> result = LandQuestScopeDAO.findAllActiveLandQuests(mockConnection);

            assertEquals(1, result.size());
            assertEquals("MySingleLand", result.get(questUUID));
        }

        @Test
        @DisplayName("Given SQL exception, when finding all, then returns empty map")
        void findAllActiveLandQuests_returnsEmptyMap_whenSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            Map<UUID, String> result = LandQuestScopeDAO.findAllActiveLandQuests(mockConnection);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveScope")
    class SaveScope {

        @Test
        @DisplayName("Given a valid land scope, when saving, then returns prepared statements with correct bindings")
        void saveScope_returnsStatements_whenValidScope() throws SQLException {
            UUID questUUID = UUID.randomUUID();
            LandQuestScope scope = new LandQuestScope(questUUID, "my_land");

            List<PreparedStatement> result = LandQuestScopeDAO.saveScope(mockConnection, scope);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(mockStatement).setString(1, questUUID.toString());
            verify(mockStatement).setString(2, "my_land");
        }

        @Test
        @DisplayName("Given SQL exception during prepare, when saving, then returns empty list")
        void saveScope_returnsEmptyList_whenSqlException() throws SQLException {
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));

            UUID questUUID = UUID.randomUUID();
            LandQuestScope scope = new LandQuestScope(questUUID, "my_land");

            List<PreparedStatement> result = LandQuestScopeDAO.saveScope(mockConnection, scope);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
