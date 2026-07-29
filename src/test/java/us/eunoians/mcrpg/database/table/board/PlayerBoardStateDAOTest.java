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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("PlayerBoardStateDAO")
class PlayerBoardStateDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();
    private static final NamespacedKey BOARD_KEY = new NamespacedKey("mcrpg", "main_board");

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, PlayerBoardStateDAO.TABLE_NAME)).thenReturn(true);

            boolean result = PlayerBoardStateDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Returns true when table is created successfully")
        void attemptCreateTable_returnsTrue_whenCreatedSuccessfully() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, PlayerBoardStateDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = PlayerBoardStateDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Returns false when SQLException is thrown during creation")
        void attemptCreateTable_returnsFalse_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, PlayerBoardStateDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("create failed"));

            boolean result = PlayerBoardStateDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("No-ops when table is already at current version")
        void updateTable_noOps_whenAlreadyAtCurrentVersion() throws SQLException {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, PlayerBoardStateDAO.TABLE_NAME))
                        .thenReturn(1);

                PlayerBoardStateDAO.updateTable(mockConnection);

                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(eq(mockConnection), eq(PlayerBoardStateDAO.TABLE_NAME), eq(1)),
                        org.mockito.Mockito.never());
            }
        }

        @Test
        @DisplayName("Creates indexes when upgrading from version 0")
        void updateTable_createsIndexes_whenUpgradingFromVersionZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, PlayerBoardStateDAO.TABLE_NAME))
                        .thenReturn(0);

                PlayerBoardStateDAO.updateTable(mockConnection);

                verify(mockStatement, org.mockito.Mockito.times(2)).executeUpdate();
                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, PlayerBoardStateDAO.TABLE_NAME, 1));
            }
        }
    }

    @Nested
    @DisplayName("saveState")
    class SaveState {

        @Test
        @DisplayName("Returns prepared statements with correct parameter bindings")
        void saveState_bindsAllParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            UUID offeringId = UUID.randomUUID();
            UUID questInstanceUUID = UUID.randomUUID();
            long acceptedAt = 1000L;

            List<PreparedStatement> statements = PlayerBoardStateDAO.saveState(
                    mockConnection, PLAYER_UUID, BOARD_KEY, offeringId, "ACCEPTED", acceptedAt, questInstanceUUID);

            assertEquals(1, statements.size());
            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setString(2, BOARD_KEY.toString());
            verify(mockStatement).setString(3, offeringId.toString());
            verify(mockStatement).setString(4, "ACCEPTED");
            verify(mockStatement).setLong(5, acceptedAt);
            verify(mockStatement).setString(6, questInstanceUUID.toString());
        }

        @Test
        @DisplayName("Handles null optional fields by setting SQL NULL")
        void saveState_setsNulls_whenOptionalFieldsAreNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            PlayerBoardStateDAO.saveState(mockConnection, PLAYER_UUID, BOARD_KEY, UUID.randomUUID(), "VISIBLE", null, null);

            verify(mockStatement).setNull(eq(5), eq(Types.BIGINT));
            verify(mockStatement).setNull(eq(6), eq(Types.VARCHAR));
        }

        @Test
        @DisplayName("Returns empty list when SQLException is thrown")
        void saveState_returnsEmptyList_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            List<PreparedStatement> result = PlayerBoardStateDAO.saveState(
                    mockConnection, PLAYER_UUID, BOARD_KEY, UUID.randomUUID(), "ACCEPTED", 1000L, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteForPlayer")
    class DeleteForPlayer {

        @Test
        @DisplayName("Binds UUID parameter and returns deleted row count")
        void deleteForPlayer_bindsAndReturnsRowCount() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(5);

            int result = PlayerBoardStateDAO.deleteForPlayer(mockConnection, PLAYER_UUID);

            assertEquals(5, result);
            verify(mockStatement).setString(1, PLAYER_UUID.toString());
        }

        @Test
        @DisplayName("Returns 0 when no rows are deleted")
        void deleteForPlayer_returnsZero_whenNoRowsDeleted() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int result = PlayerBoardStateDAO.deleteForPlayer(mockConnection, PLAYER_UUID);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Returns 0 when SQLException is thrown")
        void deleteForPlayer_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("error"));

            int result = PlayerBoardStateDAO.deleteForPlayer(mockConnection, PLAYER_UUID);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("loadAcceptedForPlayer")
    class LoadAcceptedForPlayer {

        @Test
        @DisplayName("Returns entries with offering and quest instance UUIDs")
        void loadAcceptedForPlayer_returnsEntries() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            UUID offeringId = UUID.randomUUID();
            UUID questUUID = UUID.randomUUID();
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString("offering_id")).thenReturn(offeringId.toString());
            when(mockResultSet.getString("quest_instance_uuid")).thenReturn(questUUID.toString());

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, PLAYER_UUID);

            assertEquals(1, entries.size());
            assertEquals(offeringId, entries.get(0).offeringId());
            assertEquals(questUUID, entries.get(0).questInstanceUUID());
            verify(mockStatement).setString(1, PLAYER_UUID.toString());
        }

        @Test
        @DisplayName("Handles null quest_instance_uuid")
        void loadAcceptedForPlayer_handlesNullQuestUUID() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString("offering_id")).thenReturn(UUID.randomUUID().toString());
            when(mockResultSet.getString("quest_instance_uuid")).thenReturn(null);

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, PLAYER_UUID);

            assertEquals(1, entries.size());
            assertNull(entries.get(0).questInstanceUUID());
        }

        @Test
        @DisplayName("Returns empty list when no accepted entries exist")
        void loadAcceptedForPlayer_returnsEmptyList_whenNoEntries() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, PLAYER_UUID);

            assertTrue(entries.isEmpty());
        }

        @Test
        @DisplayName("Returns empty list when SQLException is thrown")
        void loadAcceptedForPlayer_returnsEmptyList_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("error"));

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, PLAYER_UUID);

            assertTrue(entries.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateStateByQuestInstanceUUID")
    class UpdateStateByQuestInstanceUUID {

        @Test
        @DisplayName("Binds parameters and returns updated row count")
        void updateStateByQuestInstanceUUID_bindsAndReturnsCount() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);

            UUID questUUID = UUID.randomUUID();
            int result = PlayerBoardStateDAO.updateStateByQuestInstanceUUID(mockConnection, questUUID, "COMPLETED");

            assertEquals(1, result);
            verify(mockStatement).setString(1, "COMPLETED");
            verify(mockStatement).setString(2, questUUID.toString());
        }

        @Test
        @DisplayName("Returns 0 when no rows match")
        void updateStateByQuestInstanceUUID_returnsZero_whenNoRowsMatch() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int result = PlayerBoardStateDAO.updateStateByQuestInstanceUUID(mockConnection, UUID.randomUUID(), "CANCELLED");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Returns 0 when SQLException is thrown")
        void updateStateByQuestInstanceUUID_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("error"));

            int result = PlayerBoardStateDAO.updateStateByQuestInstanceUUID(mockConnection, UUID.randomUUID(), "COMPLETED");

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("bulkCancelExpiredBoardStates")
    class BulkCancelExpiredBoardStates {

        @Test
        @DisplayName("Returns number of updated rows")
        void bulkCancelExpiredBoardStates_returnsUpdatedCount() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(7);

            int result = PlayerBoardStateDAO.bulkCancelExpiredBoardStates(mockConnection);

            assertEquals(7, result);
        }

        @Test
        @DisplayName("Returns 0 when no rows match")
        void bulkCancelExpiredBoardStates_returnsZero_whenNoRowsMatch() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int result = PlayerBoardStateDAO.bulkCancelExpiredBoardStates(mockConnection);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Returns 0 when SQLException is thrown")
        void bulkCancelExpiredBoardStates_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("error"));

            int result = PlayerBoardStateDAO.bulkCancelExpiredBoardStates(mockConnection);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("countActiveQuestsFromBoard")
    class CountActiveQuestsFromBoard {

        @Test
        @DisplayName("Returns count when row exists")
        void countActiveQuestsFromBoard_returnsCount_whenRowExists() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(3);

            int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(mockConnection, PLAYER_UUID, BOARD_KEY);

            assertEquals(3, result);
        }

        @Test
        @DisplayName("Binds player UUID and board key parameters")
        void countActiveQuestsFromBoard_bindsParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(0);

            PlayerBoardStateDAO.countActiveQuestsFromBoard(mockConnection, PLAYER_UUID, BOARD_KEY);

            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setString(2, BOARD_KEY.toString());
        }

        @Test
        @DisplayName("Returns 0 when no result row exists")
        void countActiveQuestsFromBoard_returnsZero_whenNoResultRow() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(mockConnection, PLAYER_UUID, BOARD_KEY);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Returns 0 when SQLException is thrown")
        void countActiveQuestsFromBoard_returnsZero_whenSqlExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("error"));

            int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(mockConnection, PLAYER_UUID, BOARD_KEY);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("AcceptedBoardEntry")
    class AcceptedBoardEntryTest {

        @Test
        @DisplayName("Record accessors return constructor values")
        void acceptedBoardEntry_accessorsReturnValues() {
            UUID offeringId = UUID.randomUUID();
            UUID questUUID = UUID.randomUUID();

            PlayerBoardStateDAO.AcceptedBoardEntry entry = new PlayerBoardStateDAO.AcceptedBoardEntry(offeringId, questUUID);

            assertEquals(offeringId, entry.offeringId());
            assertEquals(questUUID, entry.questInstanceUUID());
        }

        @Test
        @DisplayName("Record supports null quest instance UUID")
        void acceptedBoardEntry_supportsNullQuestUUID() {
            UUID offeringId = UUID.randomUUID();

            PlayerBoardStateDAO.AcceptedBoardEntry entry = new PlayerBoardStateDAO.AcceptedBoardEntry(offeringId, null);

            assertNotNull(entry.offeringId());
            assertNull(entry.questInstanceUUID());
        }
    }
}
