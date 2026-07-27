package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@DisplayName("BoardCooldownDAO")
public class BoardCooldownDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveCooldown")
    class SaveCooldown {

        @DisplayName("Given all non-null keys, when saving, then prepared statements are returned")
        @Test
        void saveCooldown_allKeys_returnsPreparedStatements() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> statements = BoardCooldownDAO.saveCooldown(
                    mockConnection,
                    "cooldown-1",
                    "rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    new NamespacedKey("mcrpg", "mine_stone"),
                    new NamespacedKey("mcrpg", "daily_personal"),
                    1000000L
            );

            assertNotNull(statements);
            assertEquals(1, statements.size());
        }

        @DisplayName("Given null keys, when saving, then setNull is called for both nullable columns")
        @Test
        void saveCooldown_nullKeys_setsNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            BoardCooldownDAO.saveCooldown(
                    mockConnection,
                    "cooldown-2",
                    "rotation",
                    "land",
                    "land-uuid-123",
                    null,
                    null,
                    2000000L
            );

            verify(mockStatement).setNull(eq(5), eq(Types.VARCHAR));
            verify(mockStatement).setNull(eq(6), eq(Types.VARCHAR));
        }

        @DisplayName("Given all parameters, when saving, then all parameters are bound correctly")
        @Test
        void saveCooldown_bindsAllParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            String cooldownId = "cd-123";
            String cooldownType = "quest_repeat";
            String scopeType = "player";
            String scopeId = UUID.randomUUID().toString();
            NamespacedKey questKey = new NamespacedKey("mcrpg", "mine_stone");
            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily");
            long expiresAt = 9999999L;

            BoardCooldownDAO.saveCooldown(mockConnection, cooldownId, cooldownType, scopeType, scopeId, questKey, categoryKey, expiresAt);

            verify(mockStatement).setString(eq(1), eq(cooldownId));
            verify(mockStatement).setString(eq(2), eq(cooldownType));
            verify(mockStatement).setString(eq(3), eq(scopeType));
            verify(mockStatement).setString(eq(4), eq(scopeId));
            verify(mockStatement).setString(eq(5), eq(questKey.toString()));
            verify(mockStatement).setString(eq(6), eq(categoryKey.toString()));
            verify(mockStatement).setLong(eq(7), eq(expiresAt));
        }

        @DisplayName("Given a SQLException from prepareStatement, when saving, then empty list is returned")
        @Test
        void saveCooldown_sqlException_returnsEmptyList() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            List<PreparedStatement> result = BoardCooldownDAO.saveCooldown(
                    mockConnection, "cd-err", "type", "scope", "id", null, null, 0L
            );

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isOnCooldown")
    class IsOnCooldown {

        @DisplayName("Given no matching rows, when checking cooldown, then returns false")
        @Test
        void isOnCooldown_noResults_returnsFalse() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection, "rotation", "player", UUID.randomUUID().toString(), null, null
            );

            assertFalse(result);
        }

        @DisplayName("Given a matching row, when checking cooldown, then returns true")
        @Test
        void isOnCooldown_hasResult_returnsTrue() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection, "rotation", "player", UUID.randomUUID().toString(), null, null
            );

            assertTrue(result);
        }

        @DisplayName("Given both optional keys are provided, when checking, then all parameters are bound")
        @Test
        void isOnCooldown_withBothKeys_bindsAllParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            String scopeId = UUID.randomUUID().toString();
            NamespacedKey questKey = new NamespacedKey("mcrpg", "mine_stone");
            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily");

            BoardCooldownDAO.isOnCooldown(mockConnection, "quest_repeat", "player", scopeId, questKey, categoryKey);

            verify(mockStatement).setString(eq(1), eq("quest_repeat"));
            verify(mockStatement).setString(eq(2), eq("player"));
            verify(mockStatement).setString(eq(3), eq(scopeId));
            verify(mockStatement).setString(eq(5), eq(questKey.toString()));
            verify(mockStatement).setString(eq(6), eq(categoryKey.toString()));
        }

        @DisplayName("Given only questDefinitionKey is provided, when checking, then questKey is bound at index 5")
        @Test
        void isOnCooldown_withQuestKeyOnly_bindsCorrectly() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey questKey = new NamespacedKey("mcrpg", "mine_stone");

            BoardCooldownDAO.isOnCooldown(mockConnection, "quest_repeat", "player", "scope-id", questKey, null);

            verify(mockStatement).setString(eq(5), eq(questKey.toString()));
            verify(mockStatement, never()).setString(eq(6), anyString());
        }

        @DisplayName("Given only categoryKey is provided, when checking, then categoryKey is bound at index 5")
        @Test
        void isOnCooldown_withCategoryKeyOnly_bindsCategoryAtIndex5() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily");

            BoardCooldownDAO.isOnCooldown(mockConnection, "rotation", "player", "scope-id", null, categoryKey);

            verify(mockStatement).setString(eq(5), eq(categoryKey.toString()));
            verify(mockStatement, never()).setString(eq(6), anyString());
        }

        @DisplayName("Given a SQLException from executeQuery, when checking, then returns false")
        @Test
        void isOnCooldown_sqlException_returnsFalse() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenThrow(new SQLException("Test exception"));

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection, "rotation", "player", "scope-id", null, null
            );

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("listCooldowns")
    class ListCooldowns {

        @DisplayName("Given cooldown rows exist, when listing, then records are returned")
        @Test
        void listCooldowns_withRows_returnsRecords() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(true, true, false);

            when(mockRs.getString("cooldown_type")).thenReturn("quest_repeat", "category_rotation");
            when(mockRs.getString("quest_definition_key")).thenReturn("mcrpg:mine_stone", null);
            when(mockRs.getString("category_key")).thenReturn(null, "mcrpg:daily");
            when(mockRs.getLong("expires_at")).thenReturn(5000L, 9000L);

            List<BoardCooldownDAO.CooldownRecord> result = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString()
            );

            assertNotNull(result);
            assertEquals(2, result.size());

            BoardCooldownDAO.CooldownRecord first = result.get(0);
            assertEquals("quest_repeat", first.cooldownType());
            assertEquals("mcrpg:mine_stone", first.questDefinitionKey());
            assertNull(first.categoryKey());
            assertEquals(5000L, first.expiresAt());

            BoardCooldownDAO.CooldownRecord second = result.get(1);
            assertEquals("category_rotation", second.cooldownType());
            assertNull(second.questDefinitionKey());
            assertEquals("mcrpg:daily", second.categoryKey());
            assertEquals(9000L, second.expiresAt());
        }

        @DisplayName("Given no cooldown rows, when listing, then empty list is returned")
        @Test
        void listCooldowns_noRows_returnsEmptyList() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            List<BoardCooldownDAO.CooldownRecord> result = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString()
            );

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @DisplayName("Given scope parameters, when listing, then parameters are bound correctly")
        @Test
        void listCooldowns_bindsParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            String scopeType = "land";
            String scopeId = "my-land";
            BoardCooldownDAO.listCooldowns(mockConnection, scopeType, scopeId);

            verify(mockPs).setString(eq(1), eq(scopeType));
            verify(mockPs).setString(eq(2), eq(scopeId));
        }

        @DisplayName("Given a SQLException from executeQuery, when listing, then empty list is returned")
        @Test
        void listCooldowns_sqlException_returnsEmptyList() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenThrow(new SQLException("Test exception"));

            List<BoardCooldownDAO.CooldownRecord> result = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", "scope-id"
            );

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteCooldowns")
    class DeleteCooldowns {

        @DisplayName("Given no categoryKey, when deleting, then all scope cooldowns are targeted")
        @Test
        void deleteCooldowns_noCategoryKey_deletesAll() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeUpdate()).thenReturn(3);

            String scopeType = "player";
            String scopeId = UUID.randomUUID().toString();
            int deleted = BoardCooldownDAO.deleteCooldowns(mockConnection, scopeType, scopeId, null);

            assertEquals(3, deleted);
            verify(mockPs).setString(eq(1), eq(scopeType));
            verify(mockPs).setString(eq(2), eq(scopeId));
            verify(mockPs, never()).setString(eq(3), anyString());
        }

        @DisplayName("Given a categoryKey, when deleting, then category filter is applied")
        @Test
        void deleteCooldowns_withCategoryKey_appliesFilter() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeUpdate()).thenReturn(1);

            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily");
            int deleted = BoardCooldownDAO.deleteCooldowns(mockConnection, "player", "scope-id", categoryKey);

            assertEquals(1, deleted);
            verify(mockPs).setString(eq(3), eq(categoryKey.toString()));
        }

        @DisplayName("Given a SQLException from executeUpdate, when deleting, then returns 0")
        @Test
        void deleteCooldowns_sqlException_returnsZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeUpdate()).thenThrow(new SQLException("Test exception"));

            int deleted = BoardCooldownDAO.deleteCooldowns(mockConnection, "player", "scope-id", null);

            assertEquals(0, deleted);
        }

        @DisplayName("Given no matching rows, when deleting, then returns 0")
        @Test
        void deleteCooldowns_noMatchingRows_returnsZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeUpdate()).thenReturn(0);

            int deleted = BoardCooldownDAO.deleteCooldowns(mockConnection, "player", "scope-id", null);

            assertEquals(0, deleted);
        }
    }

    @Nested
    @DisplayName("pruneExpiredCooldowns")
    class PruneExpiredCooldowns {

        @DisplayName("Given a valid connection, when pruning, then prepared statements are returned")
        @Test
        void pruneExpiredCooldowns_returnsPreparedStatements() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> statements = BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

            assertNotNull(statements);
            assertEquals(1, statements.size());
        }

        @DisplayName("Given a SQLException from prepareStatement, when pruning, then empty list is returned")
        @Test
        void pruneExpiredCooldowns_sqlException_returnsEmptyList() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            List<PreparedStatement> statements = BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

            assertNotNull(statements);
            assertTrue(statements.isEmpty());
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
            when(database.tableExists(conn, BoardCooldownDAO.TABLE_NAME)).thenReturn(true);

            boolean result = BoardCooldownDAO.attemptCreateTable(conn, database);

            assertFalse(result);
        }

        @DisplayName("Given the table does not exist, when creating successfully, then returns true")
        @Test
        void attemptCreateTable_newTable_returnsTrue() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(database.tableExists(conn, BoardCooldownDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            boolean result = BoardCooldownDAO.attemptCreateTable(conn, database);

            assertTrue(result);
            verify(mockPs).executeUpdate();
        }

        @DisplayName("Given a SQLException during creation, when creating, then returns false")
        @Test
        void attemptCreateTable_sqlException_returnsFalse() throws SQLException {
            Connection conn = mock(Connection.class);
            Database database = mock(Database.class);
            when(database.tableExists(conn, BoardCooldownDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("Test exception"));

            boolean result = BoardCooldownDAO.attemptCreateTable(conn, database);

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
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, BoardCooldownDAO.TABLE_NAME)).thenReturn(1);

                BoardCooldownDAO.updateTable(conn);

                verify(conn, never()).prepareStatement(anyString());
            }
        }

        @DisplayName("Given version 0, when updating, then indexes are created and version is set")
        @Test
        void updateTable_version0_createsIndexesAndSetsVersion() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            try (MockedStatic<TableVersionHistoryDAO> mockedVersionDAO = mockStatic(TableVersionHistoryDAO.class)) {
                mockedVersionDAO.when(() -> TableVersionHistoryDAO.getLatestVersion(conn, BoardCooldownDAO.TABLE_NAME)).thenReturn(0);

                BoardCooldownDAO.updateTable(conn);

                verify(conn, times(2)).prepareStatement(anyString());
                verify(mockPs, times(2)).executeUpdate();
                mockedVersionDAO.verify(() -> TableVersionHistoryDAO.setTableVersion(conn, BoardCooldownDAO.TABLE_NAME, 1));
            }
        }
    }

    @Nested
    @DisplayName("CooldownRecord")
    class CooldownRecordTests {

        @DisplayName("Given record parameters, when created, then all fields are accessible")
        @Test
        void cooldownRecord_fieldsAccessible() {
            BoardCooldownDAO.CooldownRecord record = new BoardCooldownDAO.CooldownRecord(
                    "quest_repeat", "mcrpg:mine_stone", "mcrpg:daily", 12345L
            );

            assertEquals("quest_repeat", record.cooldownType());
            assertEquals("mcrpg:mine_stone", record.questDefinitionKey());
            assertEquals("mcrpg:daily", record.categoryKey());
            assertEquals(12345L, record.expiresAt());
        }

        @DisplayName("Given nullable fields set to null, when created, then null fields return null")
        @Test
        void cooldownRecord_nullableFields() {
            BoardCooldownDAO.CooldownRecord record = new BoardCooldownDAO.CooldownRecord(
                    "rotation", null, null, 0L
            );

            assertEquals("rotation", record.cooldownType());
            assertNull(record.questDefinitionKey());
            assertNull(record.categoryKey());
        }

        @DisplayName("Given two equal records, when compared, then they are equal")
        @Test
        void cooldownRecord_equality() {
            BoardCooldownDAO.CooldownRecord a = new BoardCooldownDAO.CooldownRecord("t", "q", "c", 1L);
            BoardCooldownDAO.CooldownRecord b = new BoardCooldownDAO.CooldownRecord("t", "q", "c", 1L);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }
    }
}
