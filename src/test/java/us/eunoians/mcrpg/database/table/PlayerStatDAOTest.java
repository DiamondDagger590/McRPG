package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link PlayerStatDAO}.
 * <p>
 * Follows the pattern established by
 * {@link us.eunoians.mcrpg.database.table.board.BoardRotationDAOTest}: the JDBC layer
 * is fully mocked so the tests run without a real database.
 */
class PlayerStatDAOTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = new NamespacedKey("mcrpg", "mana");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_player_stat")).thenReturn(true);

            boolean result = PlayerStatDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true and executes CREATE TABLE when table does not exist")
        void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_player_stat")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = PlayerStatDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("returns false when SQLException is thrown during creation")
        void attemptCreateTable_returnsFalse_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_player_stat")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = PlayerStatDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("CREATE TABLE SQL includes composite primary key")
        void attemptCreateTable_includesCompositePrimaryKey() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_player_stat")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            PlayerStatDAO.attemptCreateTable(mockConnection, mockDatabase);

            verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("PRIMARY KEY"));
        }
    }

    @Nested
    @DisplayName("saveStat")
    class SaveStat {

        @Test
        @DisplayName("uses REPLACE INTO and binds all three parameters")
        void saveStat_bindsAllParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            PlayerStatDAO.saveStat(mockConnection, PLAYER_UUID, MANA_KEY, 75.0);

            verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("REPLACE INTO"));
            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setString(2, MANA_KEY.toString());
            verify(mockStatement).setDouble(3, 75.0);
        }

        @Test
        @DisplayName("returns the prepared statement")
        void saveStat_returnsPreparedStatement() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            PreparedStatement result = PlayerStatDAO.saveStat(mockConnection, PLAYER_UUID, MANA_KEY, 50.0);

            assertEquals(mockStatement, result);
        }

        @Test
        @DisplayName("propagates SQLException to caller")
        void saveStat_propagatesSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            assertThrows(SQLException.class, () -> PlayerStatDAO.saveStat(mockConnection, PLAYER_UUID, MANA_KEY, 50.0));
        }
    }

    @Nested
    @DisplayName("saveStats")
    class SaveStats {

        @Test
        @DisplayName("returns one statement per map entry")
        void saveStats_returnsOneStatementPerEntry() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            NamespacedKey healthKey = new NamespacedKey("mcrpg", "health");
            Map<NamespacedKey, Double> stats = new LinkedHashMap<>();
            stats.put(MANA_KEY, 50.0);
            stats.put(healthKey, 100.0);

            List<PreparedStatement> result = PlayerStatDAO.saveStats(mockConnection, PLAYER_UUID, stats);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("returns empty list for empty map")
        void saveStats_returnsEmptyList_whenMapIsEmpty() throws SQLException {
            Connection mockConnection = mock(Connection.class);

            List<PreparedStatement> result = PlayerStatDAO.saveStats(mockConnection, PLAYER_UUID, Map.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("propagates SQLException from saveStat")
        void saveStats_propagatesSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            Map<NamespacedKey, Double> stats = Map.of(MANA_KEY, 50.0);

            assertThrows(SQLException.class, () -> PlayerStatDAO.saveStats(mockConnection, PLAYER_UUID, stats));
        }

        @Test
        @DisplayName("binds correct values for each stat entry")
        void saveStats_bindsCorrectValuesForEachEntry() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement1 = mock(PreparedStatement.class);
            PreparedStatement mockStatement2 = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString()))
                    .thenReturn(mockStatement1)
                    .thenReturn(mockStatement2);

            NamespacedKey healthKey = new NamespacedKey("mcrpg", "health");
            Map<NamespacedKey, Double> stats = new LinkedHashMap<>();
            stats.put(MANA_KEY, 50.0);
            stats.put(healthKey, 100.0);

            List<PreparedStatement> result = PlayerStatDAO.saveStats(mockConnection, PLAYER_UUID, stats);

            assertEquals(2, result.size());
            verify(mockStatement1).setString(2, MANA_KEY.toString());
            verify(mockStatement1).setDouble(3, 50.0);
            verify(mockStatement2).setString(2, healthKey.toString());
            verify(mockStatement2).setDouble(3, 100.0);
        }
    }

    @Nested
    @DisplayName("loadStat")
    class LoadStat {

        @Test
        @DisplayName("returns empty when no row exists")
        void loadStat_returnsEmptyWhenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Optional<Double> result = PlayerStatDAO.loadStat(mockConnection, PLAYER_UUID, MANA_KEY);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns the persisted value when a row exists")
        void loadStat_returnsValueWhenPresent() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getDouble("current_value")).thenReturn(42.0);

            Optional<Double> result = PlayerStatDAO.loadStat(mockConnection, PLAYER_UUID, MANA_KEY);

            assertTrue(result.isPresent());
            assertEquals(42.0, result.get());
        }

        @Test
        @DisplayName("binds UUID and stat key parameters")
        void loadStat_bindsParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            PlayerStatDAO.loadStat(mockConnection, PLAYER_UUID, MANA_KEY);

            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setString(2, MANA_KEY.toString());
        }

        @Test
        @DisplayName("returns empty on SQLException")
        void loadStat_returnsEmpty_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            Optional<Double> result = PlayerStatDAO.loadStat(mockConnection, PLAYER_UUID, MANA_KEY);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadAllStats")
    class LoadAllStats {

        @Test
        @DisplayName("returns empty map when no rows exist")
        void loadAllStats_returnsEmptyWhenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("silently skips rows whose stat_key cannot be parsed as a NamespacedKey")
        void loadAllStats_skipsNullKey() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getString("stat_key"))
                    .thenReturn("not a valid key", MANA_KEY.toString());
            when(mockResultSet.getDouble("current_value"))
                    .thenReturn(88.0);

            Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

            assertFalse(result.containsKey(null));
            assertEquals(1, result.size());
            assertEquals(88.0, result.get(MANA_KEY));
        }

        @Test
        @DisplayName("returns all valid rows")
        void loadAllStats_returnsAllValidRows() throws SQLException {
            NamespacedKey healthKey = new NamespacedKey("mcrpg", "health");
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getString("stat_key"))
                    .thenReturn(MANA_KEY.toString(), healthKey.toString());
            when(mockResultSet.getDouble("current_value"))
                    .thenReturn(60.0, 100.0);

            Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

            assertEquals(2, result.size());
            assertEquals(60.0, result.get(MANA_KEY));
            assertEquals(100.0, result.get(healthKey));
        }

        @Test
        @DisplayName("binds UUID parameter")
        void loadAllStats_bindsUuidParameter() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

            verify(mockStatement).setString(1, PLAYER_UUID.toString());
        }

        @Test
        @DisplayName("returns empty map on SQLException")
        void loadAllStats_returnsEmptyMap_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            Map<NamespacedKey, Double> result = PlayerStatDAO.loadAllStats(mockConnection, PLAYER_UUID);

            assertTrue(result.isEmpty());
        }
    }
}
