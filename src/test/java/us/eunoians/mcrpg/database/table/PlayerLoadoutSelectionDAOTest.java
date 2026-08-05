package us.eunoians.mcrpg.database.table;

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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link PlayerLoadoutSelectionDAO}.
 */
class PlayerLoadoutSelectionDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME)).thenReturn(true);

            boolean result = PlayerLoadoutSelectionDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true and executes CREATE TABLE when table does not exist")
        void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = PlayerLoadoutSelectionDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("returns false when SQLException is thrown during creation")
        void attemptCreateTable_returnsFalse_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = PlayerLoadoutSelectionDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("CREATE TABLE SQL references the loadout info table")
        void attemptCreateTable_referencesLoadoutInfoTable() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            PlayerLoadoutSelectionDAO.attemptCreateTable(mockConnection, mockDatabase);

            verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("CREATE TABLE"));
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("skips migration when version is current")
        void updateTable_skips_whenVersionIsCurrent() {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME))
                        .thenReturn(1);

                PlayerLoadoutSelectionDAO.updateTable(mockConnection);

                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME, 1),
                        org.mockito.Mockito.never());
            }
        }

        @Test
        @DisplayName("sets version to 1 when at version 0")
        void updateTable_setsVersionToOne_whenAtVersionZero() {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME))
                        .thenReturn(0);

                PlayerLoadoutSelectionDAO.updateTable(mockConnection);

                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, PlayerLoadoutSelectionDAO.TABLE_NAME, 1));
            }
        }
    }

    @Nested
    @DisplayName("getActiveLoadout")
    class GetActiveLoadout {

        @Test
        @DisplayName("returns stored loadout id")
        void getActiveLoadout_returnsStoredId() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("active_loadout_id")).thenReturn(3);

            int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

            assertEquals(3, result);
        }

        @Test
        @DisplayName("binds UUID parameter")
        void getActiveLoadout_bindsUuidParameter() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("active_loadout_id")).thenReturn(1);

            PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

            verify(mockStatement).setString(1, PLAYER_UUID.toString());
        }

        @Test
        @DisplayName("returns minimum of 1 when stored value is zero")
        void getActiveLoadout_returnsMinimumOne_whenStoredValueIsZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("active_loadout_id")).thenReturn(0);

            int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

            assertEquals(1, result);
        }

        @Test
        @DisplayName("returns minimum of 1 when stored value is negative")
        void getActiveLoadout_returnsMinimumOne_whenStoredValueIsNegative() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt("active_loadout_id")).thenReturn(-5);

            int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

            assertEquals(1, result);
        }

        @Test
        @DisplayName("defaults to 1 on SQL exception")
        void getActiveLoadout_defaultsToOne_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

            assertEquals(1, result);
        }

        @Test
        @DisplayName("returns 1 when no row exists and getInt throws after empty result set")
        void getActiveLoadout_returnsOne_whenNoRowExists() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);
            when(mockResultSet.getInt("active_loadout_id"))
                    .thenThrow(new SQLException("ResultSet is empty"));

            int result = PlayerLoadoutSelectionDAO.getActiveLoadout(mockConnection, PLAYER_UUID);

            assertEquals(1, result);
        }
    }

    @Nested
    @DisplayName("setActiveLoadout")
    class SetActiveLoadout {

        @Test
        @DisplayName("uses REPLACE INTO and binds UUID and loadout id")
        void setActiveLoadout_bindsAllParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> result = PlayerLoadoutSelectionDAO.setActiveLoadout(mockConnection, PLAYER_UUID, 2);

            assertFalse(result.isEmpty());
            verify(mockConnection).prepareStatement(org.mockito.ArgumentMatchers.contains("REPLACE INTO"));
            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setInt(2, 2);
        }

        @Test
        @DisplayName("returns list with one prepared statement")
        void setActiveLoadout_returnsSingleStatement() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> result = PlayerLoadoutSelectionDAO.setActiveLoadout(mockConnection, PLAYER_UUID, 1);

            assertEquals(1, result.size());
            assertTrue(result.contains(mockStatement));
        }

        @Test
        @DisplayName("returns empty list on SQL exception")
        void setActiveLoadout_returnsEmptyList_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            List<PreparedStatement> result = PlayerLoadoutSelectionDAO.setActiveLoadout(mockConnection, PLAYER_UUID, 2);

            assertTrue(result.isEmpty());
        }
    }
}
