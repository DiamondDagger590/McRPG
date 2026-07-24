package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

class QuestCompletionLogDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final String DEFINITION_KEY = "mcrpg:test_quest";

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, QuestCompletionLogDAO.TABLE_NAME)).thenReturn(true);

            boolean result = QuestCompletionLogDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true when table does not exist")
        void attemptCreateTable_returnsTrue_whenTableMissing() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(db.tableExists(conn, QuestCompletionLogDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = QuestCompletionLogDAO.attemptCreateTable(conn, db);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @Test
        @DisplayName("returns false when SQL exception is thrown during creation")
        void attemptCreateTable_returnsFalse_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, QuestCompletionLogDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = QuestCompletionLogDAO.attemptCreateTable(conn, db);

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
                tvh.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, QuestCompletionLogDAO.TABLE_NAME)).thenReturn(1);

                QuestCompletionLogDAO.updateTable(conn);

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
                tvh.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, QuestCompletionLogDAO.TABLE_NAME)).thenReturn(0);

                QuestCompletionLogDAO.updateTable(conn);

                verify(ps, org.mockito.Mockito.times(2)).executeUpdate();
                tvh.verify(() -> TableVersionHistoryDAO.setTableVersion(conn, QuestCompletionLogDAO.TABLE_NAME, 1));
            }
        }
    }

    @Nested
    @DisplayName("logCompletion")
    class LogCompletion {

        @Test
        @DisplayName("prepares and executes an insert")
        void logCompletion_preparesAndExecutesInsert() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestCompletionLogDAO.logCompletion(
                    conn, PLAYER_UUID, DEFINITION_KEY, UUID.randomUUID(), Instant.now());

            verify(ps).executeUpdate();
        }

        @Test
        @DisplayName("binds player UUID and definition key")
        void logCompletion_bindsCorrectParams() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            UUID questUUID = UUID.randomUUID();
            Instant completedAt = Instant.ofEpochMilli(5000L);

            QuestCompletionLogDAO.logCompletion(conn, PLAYER_UUID, DEFINITION_KEY, questUUID, completedAt);

            verify(ps).setString(2, PLAYER_UUID.toString());
            verify(ps).setString(3, DEFINITION_KEY);
            verify(ps).setString(4, questUUID.toString());
            verify(ps).setLong(5, 5000L);
        }

        @Test
        @DisplayName("does not throw when SQL exception occurs")
        void logCompletion_handlesException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            assertDoesNotThrow(() -> QuestCompletionLogDAO.logCompletion(
                    conn, PLAYER_UUID, DEFINITION_KEY, UUID.randomUUID(), Instant.now()));
        }
    }

    @Nested
    @DisplayName("getCompletionCount")
    class GetCompletionCount {

        @Test
        @DisplayName("returns count from result set")
        void getCompletionCount_returnsCount() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(3);

            int count = QuestCompletionLogDAO.getCompletionCount(conn, PLAYER_UUID, DEFINITION_KEY);

            assertEquals(3, count);
        }

        @Test
        @DisplayName("returns 0 when no rows")
        void getCompletionCount_returnsZero_whenNoRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            int count = QuestCompletionLogDAO.getCompletionCount(conn, PLAYER_UUID, DEFINITION_KEY);

            assertEquals(0, count);
        }

        @Test
        @DisplayName("returns 0 when SQL exception is thrown")
        void getCompletionCount_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            int count = QuestCompletionLogDAO.getCompletionCount(conn, PLAYER_UUID, DEFINITION_KEY);

            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("getLastCompletionTime")
    class GetLastCompletionTime {

        @Test
        @DisplayName("returns timestamp when present")
        void getLastCompletionTime_returnsTimestamp() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getLong(1)).thenReturn(123456789L);
            when(rs.wasNull()).thenReturn(false);

            OptionalLong result = QuestCompletionLogDAO.getLastCompletionTime(conn, PLAYER_UUID, DEFINITION_KEY);

            assertTrue(result.isPresent());
            assertEquals(123456789L, result.getAsLong());
        }

        @Test
        @DisplayName("returns empty when value is null")
        void getLastCompletionTime_returnsEmpty_whenNull() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getLong(1)).thenReturn(0L);
            when(rs.wasNull()).thenReturn(true);

            OptionalLong result = QuestCompletionLogDAO.getLastCompletionTime(conn, PLAYER_UUID, DEFINITION_KEY);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when SQL exception is thrown")
        void getLastCompletionTime_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            OptionalLong result = QuestCompletionLogDAO.getLastCompletionTime(conn, PLAYER_UUID, DEFINITION_KEY);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("hasCompleted")
    class HasCompleted {

        @Test
        @DisplayName("returns true when completion count is greater than 0")
        void hasCompleted_returnsTrue_whenCountPositive() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(2);

            boolean result = QuestCompletionLogDAO.hasCompleted(conn, PLAYER_UUID, DEFINITION_KEY);

            assertTrue(result);
        }

        @Test
        @DisplayName("returns false when completion count is 0")
        void hasCompleted_returnsFalse_whenCountZero() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(0);

            boolean result = QuestCompletionLogDAO.hasCompleted(conn, PLAYER_UUID, DEFINITION_KEY);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("deleteForPlayer")
    class DeleteForPlayer {

        @Test
        @DisplayName("returns deleted row count")
        void deleteForPlayer_returnsDeletedCount() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(contains("DELETE"))).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(5);

            int result = QuestCompletionLogDAO.deleteForPlayer(conn, PLAYER_UUID);

            assertEquals(5, result);
            verify(ps).setString(1, PLAYER_UUID.toString());
        }

        @Test
        @DisplayName("returns 0 when SQL exception is thrown")
        void deleteForPlayer_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            int result = QuestCompletionLogDAO.deleteForPlayer(conn, PLAYER_UUID);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("getCompletionHistory")
    class GetCompletionHistory {

        @Test
        @DisplayName("returns records in order")
        void getCompletionHistory_returnsRecords() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            UUID questUUID1 = UUID.randomUUID();
            UUID questUUID2 = UUID.randomUUID();
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("definition_key")).thenReturn("mcrpg:quest_a", "mcrpg:quest_b");
            when(rs.getString("quest_uuid")).thenReturn(questUUID1.toString(), questUUID2.toString());
            when(rs.getLong("completed_at")).thenReturn(1000L, 2000L);

            List<CompletionRecord> records = QuestCompletionLogDAO.getCompletionHistory(conn, PLAYER_UUID, true);

            assertEquals(2, records.size());
            assertEquals("mcrpg:quest_a", records.get(0).definitionKey());
            assertEquals(questUUID1, records.get(0).questUUID());
            assertEquals(Instant.ofEpochMilli(1000L), records.get(0).completedAt());
            assertEquals("mcrpg:quest_b", records.get(1).definitionKey());
            assertEquals(Instant.ofEpochMilli(2000L), records.get(1).completedAt());
        }

        @Test
        @DisplayName("returns empty list when no rows")
        void getCompletionHistory_returnsEmptyList_whenNoRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<CompletionRecord> records = QuestCompletionLogDAO.getCompletionHistory(conn, PLAYER_UUID, true);

            assertNotNull(records);
            assertTrue(records.isEmpty());
        }

        @Test
        @DisplayName("uses ASC order when ascending is true")
        void getCompletionHistory_usesAscOrder_whenAscendingTrue() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(contains("ASC"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            QuestCompletionLogDAO.getCompletionHistory(conn, PLAYER_UUID, true);

            verify(conn).prepareStatement(contains("ASC"));
        }

        @Test
        @DisplayName("uses DESC order when ascending is false")
        void getCompletionHistory_usesDescOrder_whenAscendingFalse() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(contains("DESC"))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            QuestCompletionLogDAO.getCompletionHistory(conn, PLAYER_UUID, false);

            verify(conn).prepareStatement(contains("DESC"));
        }

        @Test
        @DisplayName("returns empty list when SQL exception is thrown")
        void getCompletionHistory_returnsEmpty_whenSqlExceptionThrown() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test"));

            List<CompletionRecord> records = QuestCompletionLogDAO.getCompletionHistory(conn, PLAYER_UUID, true);

            assertNotNull(records);
            assertTrue(records.isEmpty());
        }
    }
}
