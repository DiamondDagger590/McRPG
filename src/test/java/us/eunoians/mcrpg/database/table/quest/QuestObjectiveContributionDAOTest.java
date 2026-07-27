package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("QuestObjectiveContributionDAO")
public class QuestObjectiveContributionDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveContributions")
    class SaveContributions {

        @DisplayName("Given an objective with contributions, when saving, then prepared statements are returned")
        @Test
        void saveContributions_withContributions_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            UUID playerUUID = UUID.randomUUID();
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_test");
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);
            QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);
            obj.progress(5, playerUUID);

            List<PreparedStatement> result = QuestObjectiveContributionDAO.saveContributions(conn, obj);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @DisplayName("Given an objective without contributions, when saving, then empty list is returned")
        @Test
        void saveContributions_noContributions_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_empty");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);
            QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);

            List<PreparedStatement> result = QuestObjectiveContributionDAO.saveContributions(conn, obj);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given an objective with multiple contributors, when saving, then one statement per contributor")
        @Test
        void saveContributions_multipleContributors_oneStatementEach() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID player1 = UUID.randomUUID();
            UUID player2 = UUID.randomUUID();
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_multi");
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, player1);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);
            QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);
            obj.progress(3, player1);
            obj.progress(7, player2);

            List<PreparedStatement> result = QuestObjectiveContributionDAO.saveContributions(conn, obj);
            assertEquals(2, result.size());
        }

        @DisplayName("Given a contribution, when saving, then parameters are bound correctly")
        @Test
        void saveContributions_bindsParameters() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID playerUUID = UUID.randomUUID();
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_bind");
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);
            QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);
            obj.progress(10, playerUUID);

            QuestObjectiveContributionDAO.saveContributions(conn, obj);

            verify(mockPs).setString(eq(1), eq(obj.getQuestObjectiveUUID().toString()));
            verify(mockPs).setString(eq(2), eq(playerUUID.toString()));
            verify(mockPs).setLong(eq(3), eq(10L));
        }

        @DisplayName("Given a SQLException from prepareStatement, when saving, then empty list is returned")
        @Test
        void saveContributions_sqlException_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            UUID playerUUID = UUID.randomUUID();
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_contrib_error");
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);
            QuestObjectiveInstance obj = stage.getQuestObjectives().get(0);
            obj.progress(5, playerUUID);

            List<PreparedStatement> result = QuestObjectiveContributionDAO.saveContributions(conn, obj);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadContributions")
    class LoadContributions {

        @DisplayName("Given contributions exist, when loading, then map is returned with correct entries")
        @Test
        void loadContributions_withRows_returnsMap() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);

            UUID player1 = UUID.randomUUID();
            UUID player2 = UUID.randomUUID();
            when(mockRs.next()).thenReturn(true, true, false);
            when(mockRs.getString("player_uuid")).thenReturn(player1.toString(), player2.toString());
            when(mockRs.getLong("amount")).thenReturn(15L, 25L);

            UUID objectiveUUID = UUID.randomUUID();
            Map<UUID, Long> result = QuestObjectiveContributionDAO.loadContributions(conn, objectiveUUID);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(15L, result.get(player1));
            assertEquals(25L, result.get(player2));
        }

        @DisplayName("Given no contributions exist, when loading, then empty map is returned")
        @Test
        void loadContributions_noRows_returnsEmptyMap() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            Map<UUID, Long> result = QuestObjectiveContributionDAO.loadContributions(conn, UUID.randomUUID());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given an objective UUID, when loading, then the UUID is bound as parameter")
        @Test
        void loadContributions_bindsObjectiveUUID() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            UUID objectiveUUID = UUID.randomUUID();
            QuestObjectiveContributionDAO.loadContributions(conn, objectiveUUID);

            verify(mockPs).setString(eq(1), eq(objectiveUUID.toString()));
        }

        @DisplayName("Given a SQLException from executeQuery, when loading, then empty map is returned")
        @Test
        void loadContributions_sqlException_returnsEmptyMap() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenThrow(new SQLException("Test exception"));

            Map<UUID, Long> result = QuestObjectiveContributionDAO.loadContributions(conn, UUID.randomUUID());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given a single contributor, when loading, then map contains one entry")
        @Test
        void loadContributions_singleContributor_returnsOneEntry() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);

            UUID playerUUID = UUID.randomUUID();
            when(mockRs.next()).thenReturn(true, false);
            when(mockRs.getString("player_uuid")).thenReturn(playerUUID.toString());
            when(mockRs.getLong("amount")).thenReturn(42L);

            Map<UUID, Long> result = QuestObjectiveContributionDAO.loadContributions(conn, UUID.randomUUID());

            assertEquals(1, result.size());
            assertEquals(42L, result.get(playerUUID));
        }
    }

    @Nested
    @DisplayName("deleteContributions")
    class DeleteContributions {

        @DisplayName("Given an objective UUID, when deleting contributions, then prepared statements are returned")
        @Test
        void deleteContributions_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            List<PreparedStatement> result = QuestObjectiveContributionDAO.deleteContributions(conn, UUID.randomUUID());
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }

        @DisplayName("Given an objective UUID, when deleting, then UUID is bound as parameter")
        @Test
        void deleteContributions_bindsUUID() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID objectiveUUID = UUID.randomUUID();
            QuestObjectiveContributionDAO.deleteContributions(conn, objectiveUUID);

            verify(mockPs).setString(eq(1), eq(objectiveUUID.toString()));
        }

        @DisplayName("Given a SQLException from prepareStatement, when deleting, then empty list is returned")
        @Test
        void deleteContributions_sqlException_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            List<PreparedStatement> result = QuestObjectiveContributionDAO.deleteContributions(conn, UUID.randomUUID());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @DisplayName("Given the table already exists, when creating, then returns false")
        @Test
        void attemptCreateTable_tableExists_returnsFalse() {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, QuestObjectiveContributionDAO.TABLE_NAME)).thenReturn(true);

            boolean result = QuestObjectiveContributionDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }

        @DisplayName("Given the table does not exist, when creating successfully, then returns true")
        @Test
        void attemptCreateTable_newTable_returnsTrue() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(database.tableExists(conn, QuestObjectiveContributionDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            boolean result = QuestObjectiveContributionDAO.attemptCreateTable(conn, database);

            assertTrue(result);
            verify(mockPs).executeUpdate();
        }

        @DisplayName("Given a SQLException during creation, when creating, then returns false")
        @Test
        void attemptCreateTable_sqlException_returnsFalse() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, QuestObjectiveContributionDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            boolean result = QuestObjectiveContributionDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @DisplayName("Given the table is at current version, when updating, then no migration runs")
        @Test
        void updateTable_currentVersion_noMigration() {
            Connection conn = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, QuestObjectiveContributionDAO.TABLE_NAME)).thenReturn(1);

                QuestObjectiveContributionDAO.updateTable(conn);

                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(eq(conn), anyString(), anyInt()), never());
            }
        }

        @DisplayName("Given version 0, when updating, then version is set to 1")
        @Test
        void updateTable_version0_setsVersion() {
            Connection conn = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, QuestObjectiveContributionDAO.TABLE_NAME)).thenReturn(0);

                QuestObjectiveContributionDAO.updateTable(conn);

                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(conn, QuestObjectiveContributionDAO.TABLE_NAME, 1));
            }
        }
    }
}
