package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("QuestStageInstanceDAO")
public class QuestStageInstanceDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveStageInstance")
    class SaveStageInstance {

        @DisplayName("Given a stage instance, when saving, then prepared statements are returned")
        @Test
        void saveStageInstance_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_test");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            List<PreparedStatement> result = QuestStageInstanceDAO.saveStageInstance(conn, stage);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }

        @DisplayName("Given a stage instance, when saving, then connection prepareStatement is called")
        @Test
        void saveStageInstance_usesConnection() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_conn");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            QuestStageInstanceDAO.saveStageInstance(conn, stage);
            verify(conn).prepareStatement(anyString());
        }

        @DisplayName("Given a stage instance, when saving, then parameters are bound correctly")
        @Test
        void saveStageInstance_bindsParameters() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_params");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            QuestStageInstanceDAO.saveStageInstance(conn, stage);

            verify(mockPs).setString(eq(1), eq(stage.getQuestStageUUID().toString()));
            verify(mockPs).setString(eq(2), eq(quest.getQuestUUID().toString()));
            verify(mockPs).setString(eq(3), eq(stage.getStageKey().toString()));
            verify(mockPs).setInt(eq(4), eq(stage.getPhaseIndex()));
            verify(mockPs).setString(eq(5), eq(stage.getQuestStageState().name()));
        }

        @DisplayName("Given a started stage with no end time, when saving, then end_time is set to null")
        @Test
        void saveStageInstance_nullEndTime_setsNullForEndTime() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_null_time");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            QuestStageInstanceDAO.saveStageInstance(conn, stage);

            verify(mockPs).setNull(eq(7), eq(Types.BIGINT));
        }

        @DisplayName("Given an unstarted stage, when saving, then both time columns are set to null")
        @Test
        void saveStageInstance_unstartedStage_setsNullForBothTimes() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_unstarted");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            NamespacedKey stageKey = new NamespacedKey("mcrpg", "test_stage_unstarted");
            QuestStageInstance stage = new QuestStageInstance(stageKey, UUID.randomUUID(), 0, quest, QuestStageState.NOT_STARTED, null, null);

            QuestStageInstanceDAO.saveStageInstance(conn, stage);

            verify(mockPs).setNull(eq(6), eq(Types.BIGINT));
            verify(mockPs).setNull(eq(7), eq(Types.BIGINT));
        }

        @DisplayName("Given a stage with non-null timestamps, when saving, then times are set as longs")
        @Test
        void saveStageInstance_nonNullTimestamps_setsLongs() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_with_times");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            NamespacedKey stageKey = new NamespacedKey("mcrpg", "test_stage_timed");
            QuestStageInstance stage = new QuestStageInstance(stageKey, UUID.randomUUID(), 0, quest, QuestStageState.COMPLETED, 5000L, 9000L);

            QuestStageInstanceDAO.saveStageInstance(conn, stage);

            verify(mockPs).setLong(eq(6), eq(5000L));
            verify(mockPs).setLong(eq(7), eq(9000L));
        }

        @DisplayName("Given a SQLException from prepareStatement, when saving, then empty list is returned")
        @Test
        void saveStageInstance_sqlException_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_error");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            List<PreparedStatement> result = QuestStageInstanceDAO.saveStageInstance(conn, stage);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveAllStageInstances")
    class SaveAllStageInstances {

        @DisplayName("Given a quest with stages, when saving all, then prepared statements are returned")
        @Test
        void saveAllStageInstances_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_all_stages");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<PreparedStatement> result = QuestStageInstanceDAO.saveAllStageInstances(conn, quest);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(quest.getQuestStageInstances().size(), result.size());
        }
    }

    @Nested
    @DisplayName("loadStageInstances")
    class LoadStageInstances {

        @DisplayName("Given a valid stage row, when loading, then stage is returned with correct state")
        @Test
        void loadStageInstances_validRow_returnsStage() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);

            UUID stageUUID = UUID.randomUUID();
            when(mockRs.getString("stage_uuid")).thenReturn(stageUUID.toString());
            when(mockRs.getString("definition_key")).thenReturn("mcrpg:test_stage");
            when(mockRs.getInt("phase_index")).thenReturn(0);
            when(mockRs.getString("state")).thenReturn("IN_PROGRESS");
            when(mockRs.getLong("start_time")).thenReturn(1000L);
            when(mockRs.wasNull()).thenReturn(false, false);
            when(mockRs.getLong("end_time")).thenReturn(0L);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_stage");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            UUID questUUID = quest.getQuestUUID();

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, questUUID, quest);

            assertNotNull(result);
            assertEquals(1, result.size());
            QuestStageInstance loaded = result.get(0);
            assertEquals(stageUUID, loaded.getQuestStageUUID());
            assertEquals(QuestStageState.IN_PROGRESS, loaded.getQuestStageState());
            assertEquals(0, loaded.getPhaseIndex());
        }

        @DisplayName("Given no rows, when loading, then empty list is returned")
        @Test
        void loadStageInstances_noRows_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_empty");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given a malformed definition_key, when loading, then that row is skipped")
        @Test
        void loadStageInstances_malformedKey_skipsRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, true, false);

            UUID badStageUUID = UUID.randomUUID();
            UUID goodStageUUID = UUID.randomUUID();

            when(mockRs.getString("stage_uuid")).thenReturn(badStageUUID.toString(), goodStageUUID.toString());
            when(mockRs.getString("definition_key")).thenReturn(":::invalid:::", "mcrpg:valid_stage");
            when(mockRs.getInt("phase_index")).thenReturn(0, 1);
            when(mockRs.getString("state")).thenReturn("NOT_STARTED", "IN_PROGRESS");
            when(mockRs.getLong("start_time")).thenReturn(0L);
            when(mockRs.wasNull()).thenReturn(true, true);
            when(mockRs.getLong("end_time")).thenReturn(0L);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_malformed");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertEquals(1, result.size());
            assertEquals(goodStageUUID, result.get(0).getQuestStageUUID());
        }

        @DisplayName("Given nullable timestamps, when loading, then timestamps are correctly handled")
        @Test
        void loadStageInstances_nullableTimestamps_handledCorrectly() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, false);

            when(mockRs.getString("stage_uuid")).thenReturn(UUID.randomUUID().toString());
            when(mockRs.getString("definition_key")).thenReturn("mcrpg:nullable_test");
            when(mockRs.getInt("phase_index")).thenReturn(0);
            when(mockRs.getString("state")).thenReturn("NOT_STARTED");
            when(mockRs.getLong("start_time")).thenReturn(0L);
            when(mockRs.getLong("end_time")).thenReturn(0L);
            when(mockRs.wasNull()).thenReturn(true, true);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_nullable");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertEquals(1, result.size());
            QuestStageInstance loaded = result.get(0);
            assertTrue(loaded.getStartTime().isEmpty());
            assertTrue(loaded.getEndTime().isEmpty());
        }

        @DisplayName("Given multiple valid rows, when loading, then all stages are returned")
        @Test
        void loadStageInstances_multipleRows_returnsAll() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, true, true, false);

            UUID uuid1 = UUID.randomUUID();
            UUID uuid2 = UUID.randomUUID();
            UUID uuid3 = UUID.randomUUID();

            when(mockRs.getString("stage_uuid"))
                    .thenReturn(uuid1.toString(), uuid2.toString(), uuid3.toString());
            when(mockRs.getString("definition_key"))
                    .thenReturn("mcrpg:stage_a", "mcrpg:stage_b", "mcrpg:stage_c");
            when(mockRs.getInt("phase_index"))
                    .thenReturn(0, 0, 1);
            when(mockRs.getString("state"))
                    .thenReturn("COMPLETED", "IN_PROGRESS", "NOT_STARTED");
            when(mockRs.getLong("start_time")).thenReturn(5000L, 6000L, 0L);
            when(mockRs.getLong("end_time")).thenReturn(7000L, 0L, 0L);
            when(mockRs.wasNull()).thenReturn(false, false, false, true, true, true);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_multi");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertEquals(3, result.size());
            assertEquals(QuestStageState.COMPLETED, result.get(0).getQuestStageState());
            assertEquals(QuestStageState.IN_PROGRESS, result.get(1).getQuestStageState());
            assertEquals(QuestStageState.NOT_STARTED, result.get(2).getQuestStageState());
        }

        @DisplayName("Given a quest UUID, when loading, then the quest UUID is bound as parameter")
        @Test
        void loadStageInstances_bindsQuestUUID() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_bind");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            UUID questUUID = quest.getQuestUUID();

            QuestStageInstanceDAO.loadStageInstances(conn, questUUID, quest);

            verify(mockPs).setString(eq(1), eq(questUUID.toString()));
        }

        @DisplayName("Given a SQLException from executeQuery, when loading, then empty list is returned")
        @Test
        void loadStageInstances_sqlException_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenThrow(new SQLException("Test exception"));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_error");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteStageInstances")
    class DeleteStageInstances {

        @DisplayName("Given a quest UUID, when deleting stages, then prepared statements are returned")
        @Test
        void deleteStageInstances_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            List<PreparedStatement> result = QuestStageInstanceDAO.deleteStageInstances(conn, UUID.randomUUID());
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
        }

        @DisplayName("Given a quest UUID, when deleting, then UUID is bound as parameter")
        @Test
        void deleteStageInstances_bindsUUID() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            UUID questUUID = UUID.randomUUID();
            QuestStageInstanceDAO.deleteStageInstances(conn, questUUID);

            verify(mockPs).setString(eq(1), eq(questUUID.toString()));
        }

        @DisplayName("Given a SQLException from prepareStatement, when deleting, then empty list is returned")
        @Test
        void deleteStageInstances_sqlException_returnsEmptyList() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            List<PreparedStatement> result = QuestStageInstanceDAO.deleteStageInstances(conn, UUID.randomUUID());
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
            when(database.tableExists(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(true);

            boolean result = QuestStageInstanceDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }

        @DisplayName("Given the table does not exist, when creating successfully, then returns true")
        @Test
        void attemptCreateTable_newTable_returnsTrue() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(database.tableExists(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            boolean result = QuestStageInstanceDAO.attemptCreateTable(conn, database);

            assertTrue(result);
            verify(mockPs).executeUpdate();
        }

        @DisplayName("Given a SQLException during creation, when creating, then returns false")
        @Test
        void attemptCreateTable_sqlException_returnsFalse() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            boolean result = QuestStageInstanceDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @DisplayName("Given the table is at current version, when updating, then no migration runs")
        @Test
        void updateTable_currentVersion_noMigration() throws SQLException {
            Connection conn = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(1);

                QuestStageInstanceDAO.updateTable(conn);

                verify(conn, never()).prepareStatement(anyString());
            }
        }

        @DisplayName("Given version 0, when updating, then index is created and version is set")
        @Test
        void updateTable_version0_createsIndexAndSetsVersion() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(0);

                QuestStageInstanceDAO.updateTable(conn);

                verify(conn).prepareStatement(anyString());
                verify(mockPs).executeUpdate();
                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(conn, QuestStageInstanceDAO.TABLE_NAME, 1));
            }
        }
    }
}
