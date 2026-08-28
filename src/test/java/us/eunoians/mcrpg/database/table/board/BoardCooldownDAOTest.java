package us.eunoians.mcrpg.database.table.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;

public class BoardCooldownDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveCooldown")
    class SaveCooldownTests {

        @DisplayName("Returns prepared statements with all fields set")
        @Test
        void saveCooldown_returnsPreparedStatements() throws SQLException {
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
            assertFalse(statements.isEmpty());
        }

        @DisplayName("Sets null for absent quest definition and category keys")
        @Test
        void saveCooldown_handlesNullKeys() throws SQLException {
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
    }

    @Nested
    @DisplayName("isOnCooldown")
    class IsOnCooldownTests {

        @DisplayName("Returns false when no matching rows exist")
        @Test
        void isOnCooldown_returnsFalse_whenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    null,
                    null
            );

            assertFalse(result);
        }

        @DisplayName("Returns true when a matching row exists")
        @Test
        void isOnCooldown_returnsTrue_whenPresent() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    null,
                    null
            );

            assertTrue(result);
        }

        @DisplayName("Binds questDefinitionKey parameter when provided")
        @Test
        void isOnCooldown_bindsQuestDefinitionKey() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey questKey = new NamespacedKey("mcrpg", "mine_stone");
            BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "quest_repeat",
                    "player",
                    UUID.randomUUID().toString(),
                    questKey,
                    null
            );

            verify(mockStatement).setString(eq(5), eq(questKey.toString()));
        }

        @DisplayName("Binds categoryKey parameter when provided")
        @Test
        void isOnCooldown_bindsCategoryKey() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily_personal");
            BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "category_rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    null,
                    categoryKey
            );

            verify(mockStatement).setString(eq(5), eq(categoryKey.toString()));
        }

        @DisplayName("Binds both keys at correct indices when both provided")
        @Test
        void isOnCooldown_bindsBothKeys() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey questKey = new NamespacedKey("mcrpg", "mine_stone");
            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily_personal");
            BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "quest_repeat",
                    "player",
                    UUID.randomUUID().toString(),
                    questKey,
                    categoryKey
            );

            verify(mockStatement).setString(eq(5), eq(questKey.toString()));
            verify(mockStatement).setString(eq(6), eq(categoryKey.toString()));
        }
    }

    @Nested
    @DisplayName("listCooldowns")
    class ListCooldownsTests {

        @DisplayName("Returns empty list when no cooldowns exist")
        @Test
        void listCooldowns_returnsEmptyList_whenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<BoardCooldownDAO.CooldownRecord> results = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString());

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @DisplayName("Returns populated records when cooldowns exist")
        @Test
        void listCooldowns_returnsRecords_whenResultsExist() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getString("cooldown_type")).thenReturn("quest_repeat", "category_rotation");
            when(mockResultSet.getString("quest_definition_key")).thenReturn("mcrpg:mine_stone", (String) null);
            when(mockResultSet.getString("category_key")).thenReturn(null, "mcrpg:daily_personal");
            when(mockResultSet.getLong("expires_at")).thenReturn(5000000L, 6000000L);

            List<BoardCooldownDAO.CooldownRecord> results = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString());

            assertEquals(2, results.size());

            BoardCooldownDAO.CooldownRecord first = results.get(0);
            assertEquals("quest_repeat", first.cooldownType());
            assertEquals("mcrpg:mine_stone", first.questDefinitionKey());
            assertNull(first.categoryKey());
            assertEquals(5000000L, first.expiresAt());

            BoardCooldownDAO.CooldownRecord second = results.get(1);
            assertEquals("category_rotation", second.cooldownType());
            assertNull(second.questDefinitionKey());
            assertEquals("mcrpg:daily_personal", second.categoryKey());
            assertEquals(6000000L, second.expiresAt());
        }
    }

    @Nested
    @DisplayName("deleteCooldowns")
    class DeleteCooldownsTests {

        @DisplayName("Deletes all cooldowns for scope when no category key")
        @Test
        void deleteCooldowns_deletesAll_whenNoCategoryKey() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(3);

            int deleted = BoardCooldownDAO.deleteCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString(), null);

            assertEquals(3, deleted);
            verify(mockStatement).setString(eq(1), eq("player"));
            verify(mockStatement, never()).setString(eq(3), anyString());
        }

        @DisplayName("Filters by category key when provided")
        @Test
        void deleteCooldowns_filtersByCategoryKey_whenProvided() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);

            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily_personal");
            int deleted = BoardCooldownDAO.deleteCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString(), categoryKey);

            assertEquals(1, deleted);
            verify(mockStatement).setString(eq(3), eq(categoryKey.toString()));
        }

        @DisplayName("Returns zero when no rows deleted")
        @Test
        void deleteCooldowns_returnsZero_whenNoRowsDeleted() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int deleted = BoardCooldownDAO.deleteCooldowns(
                    mockConnection, "player", UUID.randomUUID().toString(), null);

            assertEquals(0, deleted);
        }
    }

    @Nested
    @DisplayName("pruneExpiredCooldowns")
    class PruneExpiredCooldownsTests {

        @DisplayName("Returns prepared statements")
        @Test
        void pruneExpiredCooldowns_returnsPreparedStatements() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> statements = BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

            assertNotNull(statements);
            assertFalse(statements.isEmpty());
        }
    }

    @Nested
    @DisplayName("CooldownRecord")
    class CooldownRecordTests {

        @DisplayName("Accessors return constructor values")
        @Test
        void cooldownRecord_accessorsReturnCorrectValues() {
            BoardCooldownDAO.CooldownRecord record = new BoardCooldownDAO.CooldownRecord(
                    "quest_repeat", "mcrpg:mine_stone", "mcrpg:daily_personal", 5000000L);

            assertEquals("quest_repeat", record.cooldownType());
            assertEquals("mcrpg:mine_stone", record.questDefinitionKey());
            assertEquals("mcrpg:daily_personal", record.categoryKey());
            assertEquals(5000000L, record.expiresAt());
        }

        @DisplayName("Nullable fields accept null")
        @Test
        void cooldownRecord_nullableFieldsAcceptNull() {
            BoardCooldownDAO.CooldownRecord record = new BoardCooldownDAO.CooldownRecord(
                    "category_rotation", null, null, 1000L);

            assertNull(record.questDefinitionKey());
            assertNull(record.categoryKey());
        }

        @DisplayName("Records with same values are equal")
        @Test
        void cooldownRecord_equalRecordsAreEqual() {
            BoardCooldownDAO.CooldownRecord a = new BoardCooldownDAO.CooldownRecord(
                    "quest_repeat", "mcrpg:mine_stone", null, 5000000L);
            BoardCooldownDAO.CooldownRecord b = new BoardCooldownDAO.CooldownRecord(
                    "quest_repeat", "mcrpg:mine_stone", null, 5000000L);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }
    }
}
