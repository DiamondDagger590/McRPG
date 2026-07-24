package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestInstanceDAOTest extends McRPGBaseTest {

    private static final UUID QUEST_UUID = UUID.randomUUID();
    private static final NamespacedKey DEFINITION_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final NamespacedKey SCOPE_TYPE = new NamespacedKey("mcrpg", "single_player");

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, "mcrpg_quest_instances")).thenReturn(true);

            boolean result = QuestInstanceDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true when table does not exist")
        void attemptCreateTable_returnsTrue_whenTableMissing() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(db.tableExists(conn, "mcrpg_quest_instances")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = QuestInstanceDAO.attemptCreateTable(conn, db);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @Test
        @DisplayName("returns false when SQL exception is thrown during creation")
        void attemptCreateTable_returnsFalse_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, "mcrpg_quest_instances")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = QuestInstanceDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("skips migration when version is current")
        void updateTable_skips_whenVersionCurrent() {
            Connection conn = mock(Connection.class);
            try (MockedStatic<TableVersionHistoryDAO> tvh = mockStatic(TableVersionHistoryDAO.class)) {
                tvh.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, "mcrpg_quest_instances")).thenReturn(1);

                QuestInstanceDAO.updateTable(conn);

                tvh.verify(() -> TableVersionHistoryDAO.setTableVersion(eq(conn), anyString(), anyInt()), never());
            }
        }

        @Test
        @DisplayName("creates indexes when version is 0")
        void updateTable_createsIndexes_whenVersion0() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            try (MockedStatic<TableVersionHistoryDAO> tvh = mockStatic(TableVersionHistoryDAO.class)) {
                tvh.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, "mcrpg_quest_instances")).thenReturn(0);

                QuestInstanceDAO.updateTable(conn);

                verify(ps, org.mockito.Mockito.times(2)).executeUpdate();
                tvh.verify(() -> TableVersionHistoryDAO.setTableVersion(conn, "mcrpg_quest_instances", 1));
            }
        }
    }

    @Nested
    @DisplayName("saveQuestInstance")
    class SaveQuestInstance {

        @Test
        @DisplayName("returns a prepared statement for a quest with all fields")
        void saveQuestInstance_returnsPreparedStatement_whenAllFields() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance quest = new QuestInstance(
                    DEFINITION_KEY, QUEST_UUID, SCOPE_TYPE,
                    QuestState.IN_PROGRESS, null,
                    Instant.ofEpochMilli(1000L), null, Instant.ofEpochMilli(5000L),
                    new ManualQuestSource(), null
            );

            List<PreparedStatement> statements = QuestInstanceDAO.saveQuestInstance(conn, quest);

            assertEquals(1, statements.size());
            verify(ps).setString(1, QUEST_UUID.toString());
            verify(ps).setString(2, DEFINITION_KEY.toString());
            verify(ps).setString(3, "IN_PROGRESS");
            verify(ps).setString(4, SCOPE_TYPE.toString());
            verify(ps).setLong(5, 1000L);
            verify(ps).setNull(6, Types.BIGINT);
            verify(ps).setLong(7, 5000L);
        }

        @Test
        @DisplayName("sets board_rarity_key when present")
        void saveQuestInstance_setsBoardRarityKey_whenPresent() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance quest = new QuestInstance(
                    DEFINITION_KEY, QUEST_UUID, SCOPE_TYPE,
                    QuestState.COMPLETED, null,
                    Instant.ofEpochMilli(1000L), Instant.ofEpochMilli(2000L), null,
                    new ManualQuestSource(), null
            );
            quest.setBoardRarityKey(new NamespacedKey("mcrpg", "rare"));

            QuestInstanceDAO.saveQuestInstance(conn, quest);

            verify(ps).setString(9, "mcrpg:rare");
        }

        @Test
        @DisplayName("sets board_rarity_key to null when absent")
        void saveQuestInstance_setsNullBoardRarityKey_whenAbsent() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance quest = new QuestInstance(
                    DEFINITION_KEY, QUEST_UUID, SCOPE_TYPE,
                    QuestState.IN_PROGRESS, null,
                    null, null, null,
                    new ManualQuestSource(), null
            );

            QuestInstanceDAO.saveQuestInstance(conn, quest);

            verify(ps).setNull(9, Types.VARCHAR);
        }

        @Test
        @DisplayName("returns empty list when prepare throws")
        void saveQuestInstance_returnsEmpty_whenPrepareThrows() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            QuestInstance quest = new QuestInstance(
                    DEFINITION_KEY, QUEST_UUID, SCOPE_TYPE,
                    QuestState.IN_PROGRESS, null,
                    null, null, null,
                    new ManualQuestSource(), null
            );

            List<PreparedStatement> statements = QuestInstanceDAO.saveQuestInstance(conn, quest);

            assertTrue(statements.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadQuestInstance")
    class LoadQuestInstance {

        @Test
        @DisplayName("returns empty when no row exists")
        void loadQuestInstance_returnsEmpty_whenNoRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, QUEST_UUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns quest instance when row exists with valid data")
        void loadQuestInstance_returnsInstance_whenRowExists() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("definition_key")).thenReturn("mcrpg:test_quest");
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");
            when(rs.getString("state")).thenReturn("IN_PROGRESS");
            when(rs.getString("quest_source")).thenReturn("mcrpg:manual");
            when(rs.getLong("start_time")).thenReturn(1000L);
            when(rs.wasNull()).thenReturn(false, true, true);
            when(rs.getLong("end_time")).thenReturn(0L);
            when(rs.getLong("expiration_time")).thenReturn(0L);
            when(rs.getString("board_rarity_key")).thenReturn(null);

            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, QUEST_UUID);

            assertTrue(result.isPresent());
            QuestInstance quest = result.get();
            assertEquals(QUEST_UUID, quest.getQuestUUID());
            assertEquals("mcrpg:test_quest", quest.getQuestKey().toString());
            assertEquals(QuestState.IN_PROGRESS, quest.getQuestState());
        }

        @Test
        @DisplayName("returns quest with board rarity key when present in DB")
        void loadQuestInstance_setsBoardRarityKey_whenPresentInDb() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("definition_key")).thenReturn("mcrpg:test_quest");
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");
            when(rs.getString("state")).thenReturn("COMPLETED");
            when(rs.getString("quest_source")).thenReturn("mcrpg:manual");
            when(rs.getLong("start_time")).thenReturn(0L);
            when(rs.wasNull()).thenReturn(true, true, true);
            when(rs.getLong("end_time")).thenReturn(0L);
            when(rs.getLong("expiration_time")).thenReturn(0L);
            when(rs.getString("board_rarity_key")).thenReturn("mcrpg:legendary");

            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, QUEST_UUID);

            assertTrue(result.isPresent());
            assertTrue(result.get().getBoardRarityKey().isPresent());
            assertEquals("mcrpg:legendary", result.get().getBoardRarityKey().get().toString());
        }

        @Test
        @DisplayName("returns empty when SQL exception is thrown")
        void loadQuestInstance_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, QUEST_UUID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadScopeType")
    class LoadScopeType {

        @Test
        @DisplayName("returns key when valid scope_type exists")
        void loadScopeType_returnsKey_whenValid() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, QUEST_UUID);

            assertTrue(result.isPresent());
            assertEquals("mcrpg:single_player", result.get().toString());
        }

        @Test
        @DisplayName("returns empty when no row exists")
        void loadScopeType_returnsEmpty_whenNoRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, QUEST_UUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when scope_type is malformed")
        void loadScopeType_returnsEmpty_whenMalformed() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("scope_type")).thenReturn("!!!invalid!!!");

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, QUEST_UUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when SQL exception is thrown")
        void loadScopeType_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, QUEST_UUID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadQuestInstancesByState")
    class LoadQuestInstancesByState {

        @Test
        @DisplayName("returns empty list when no states provided")
        void loadQuestInstancesByState_returnsEmpty_whenNoStates() {
            Connection conn = mock(Connection.class);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty list when no rows match")
        void loadQuestInstancesByState_returnsEmpty_whenNoMatchingRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("binds all state parameters")
        void loadQuestInstancesByState_bindsAllStates() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.NOT_STARTED, QuestState.IN_PROGRESS);

            verify(ps).setString(1, "NOT_STARTED");
            verify(ps).setString(2, "IN_PROGRESS");
        }

        @Test
        @DisplayName("returns loaded instances from result set")
        void loadQuestInstancesByState_returnsInstances_whenRowsExist() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            UUID questUuid = UUID.randomUUID();
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("quest_uuid")).thenReturn(questUuid.toString());
            when(rs.getString("definition_key")).thenReturn("mcrpg:daily_mining");
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");
            when(rs.getString("state")).thenReturn("IN_PROGRESS");
            when(rs.getString("quest_source")).thenReturn("mcrpg:manual");
            when(rs.getLong("start_time")).thenReturn(0L);
            when(rs.wasNull()).thenReturn(true, true, true);
            when(rs.getLong("end_time")).thenReturn(0L);
            when(rs.getLong("expiration_time")).thenReturn(0L);
            when(rs.getString("board_rarity_key")).thenReturn(null);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertEquals(1, result.size());
            assertEquals(questUuid, result.get(0).getQuestUUID());
        }

        @Test
        @DisplayName("skips corrupt rows and continues loading")
        void loadQuestInstancesByState_skipsCorruptRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            UUID goodUuid = UUID.randomUUID();
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("quest_uuid")).thenReturn(UUID.randomUUID().toString(), goodUuid.toString());
            when(rs.getString("definition_key")).thenReturn("!!!INVALID!!!", "mcrpg:test");
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");
            when(rs.getString("state")).thenReturn("IN_PROGRESS");
            when(rs.getString("quest_source")).thenReturn("mcrpg:manual");
            when(rs.getLong("start_time")).thenReturn(0L);
            when(rs.wasNull()).thenReturn(true, true, true);
            when(rs.getLong("end_time")).thenReturn(0L);
            when(rs.getLong("expiration_time")).thenReturn(0L);
            when(rs.getString("board_rarity_key")).thenReturn(null);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertEquals(1, result.size());
            assertEquals(goodUuid, result.get(0).getQuestUUID());
        }

        @Test
        @DisplayName("returns empty list when SQL exception is thrown")
        void loadQuestInstancesByState_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteQuestInstance")
    class DeleteQuestInstance {

        @Test
        @DisplayName("returns a delete prepared statement")
        void deleteQuestInstance_returnsPreparedStatement() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(ps);

            List<PreparedStatement> statements = QuestInstanceDAO.deleteQuestInstance(conn, QUEST_UUID);

            assertEquals(1, statements.size());
            verify(ps).setString(1, QUEST_UUID.toString());
        }

        @Test
        @DisplayName("returns empty list when prepare throws")
        void deleteQuestInstance_returnsEmpty_whenPrepareThrows() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            List<PreparedStatement> statements = QuestInstanceDAO.deleteQuestInstance(conn, QUEST_UUID);

            assertTrue(statements.isEmpty());
        }
    }

    @Nested
    @DisplayName("bulkExpireStaleQuests")
    class BulkExpireStaleQuests {

        @Test
        @DisplayName("binds correct state parameters and returns update count")
        void bulkExpireStaleQuests_bindsParamsAndReturnsCount() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(5);

            long now = 999999L;
            int result = QuestInstanceDAO.bulkExpireStaleQuests(conn, now);

            assertEquals(5, result);
            verify(ps).setString(1, "CANCELLED");
            verify(ps).setLong(2, now);
            verify(ps).setString(3, "NOT_STARTED");
            verify(ps).setString(4, "IN_PROGRESS");
            verify(ps).setLong(5, now);
        }

        @Test
        @DisplayName("returns 0 when SQL exception is thrown")
        void bulkExpireStaleQuests_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            int result = QuestInstanceDAO.bulkExpireStaleQuests(conn, 999999L);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("saveFullQuestTree")
    class SaveFullQuestTree {

        @Test
        @DisplayName("returns prepared statements for quest instance")
        void saveFullQuestTree_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance instance = new QuestInstance(
                    DEFINITION_KEY, QUEST_UUID, SCOPE_TYPE,
                    QuestState.IN_PROGRESS, null,
                    Instant.now(), null, null,
                    new ManualQuestSource(), null
            );

            List<PreparedStatement> statements = QuestInstanceDAO.saveFullQuestTree(conn, instance);

            assertNotNull(statements);
            assertFalse(statements.isEmpty());
        }
    }
}
