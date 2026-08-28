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

public class PlayerBoardStateDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("saveState")
    class SaveStateTests {

        @DisplayName("Returns prepared statements with all fields set")
        @Test
        void saveState_returnsPreparedStatements() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            UUID playerUUID = UUID.randomUUID();
            UUID offeringId = UUID.randomUUID();
            UUID questInstanceUUID = UUID.randomUUID();
            NamespacedKey boardKey = new NamespacedKey("mcrpg", "main_board");
            long acceptedAt = System.currentTimeMillis();

            List<PreparedStatement> statements = PlayerBoardStateDAO.saveState(
                    mockConnection,
                    playerUUID,
                    boardKey,
                    offeringId,
                    "ACCEPTED",
                    acceptedAt,
                    questInstanceUUID
            );

            assertNotNull(statements);
            assertFalse(statements.isEmpty());
        }

        @DisplayName("Sets null for absent optional fields")
        @Test
        void saveState_handlesNullOptionalFields() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            UUID playerUUID = UUID.randomUUID();
            UUID offeringId = UUID.randomUUID();
            NamespacedKey boardKey = new NamespacedKey("mcrpg", "main_board");

            PlayerBoardStateDAO.saveState(mockConnection, playerUUID, boardKey, offeringId, "VISIBLE", null, null);

            verify(mockStatement).setNull(eq(5), eq(Types.BIGINT));
            verify(mockStatement).setNull(eq(6), eq(Types.VARCHAR));
        }
    }

    @Nested
    @DisplayName("countActiveQuestsFromBoard")
    class CountActiveQuestsTests {

        @DisplayName("Returns zero when ResultSet is empty")
        @Test
        void countActiveQuestsFromBoard_returnsZero_whenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(
                    mockConnection,
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "main_board")
            );

            assertEquals(0, result);
        }

        @DisplayName("Returns count from ResultSet when present")
        @Test
        void countActiveQuestsFromBoard_returnsCount_whenPresent() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt(1)).thenReturn(3);

            int result = PlayerBoardStateDAO.countActiveQuestsFromBoard(
                    mockConnection,
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "main_board")
            );

            assertEquals(3, result);
        }
    }

    @Nested
    @DisplayName("deleteForPlayer")
    class DeleteForPlayerTests {

        @DisplayName("Returns deleted row count")
        @Test
        void deleteForPlayer_returnsDeletedCount() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(5);

            UUID playerUUID = UUID.randomUUID();
            int deleted = PlayerBoardStateDAO.deleteForPlayer(mockConnection, playerUUID);

            assertEquals(5, deleted);
            verify(mockStatement).setString(eq(1), eq(playerUUID.toString()));
        }

        @DisplayName("Returns zero when no rows exist for player")
        @Test
        void deleteForPlayer_returnsZero_whenNoRows() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int deleted = PlayerBoardStateDAO.deleteForPlayer(mockConnection, UUID.randomUUID());

            assertEquals(0, deleted);
        }
    }

    @Nested
    @DisplayName("loadAcceptedForPlayer")
    class LoadAcceptedForPlayerTests {

        @DisplayName("Returns empty list when no accepted entries")
        @Test
        void loadAcceptedForPlayer_returnsEmptyList_whenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, UUID.randomUUID());

            assertNotNull(entries);
            assertTrue(entries.isEmpty());
        }

        @DisplayName("Returns entries with quest instance UUIDs")
        @Test
        void loadAcceptedForPlayer_returnsEntries_withQuestUUID() throws SQLException {
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
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, UUID.randomUUID());

            assertEquals(1, entries.size());
            assertEquals(offeringId, entries.get(0).offeringId());
            assertEquals(questUUID, entries.get(0).questInstanceUUID());
        }

        @DisplayName("Handles null quest instance UUID in entries")
        @Test
        void loadAcceptedForPlayer_handlesNullQuestUUID() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            UUID offeringId = UUID.randomUUID();

            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getString("offering_id")).thenReturn(offeringId.toString());
            when(mockResultSet.getString("quest_instance_uuid")).thenReturn(null);

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, UUID.randomUUID());

            assertEquals(1, entries.size());
            assertEquals(offeringId, entries.get(0).offeringId());
            assertNull(entries.get(0).questInstanceUUID());
        }

        @DisplayName("Returns multiple entries across iterations")
        @Test
        void loadAcceptedForPlayer_returnsMultipleEntries() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);

            UUID offering1 = UUID.randomUUID();
            UUID offering2 = UUID.randomUUID();
            UUID quest1 = UUID.randomUUID();

            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getString("offering_id"))
                    .thenReturn(offering1.toString(), offering2.toString());
            when(mockResultSet.getString("quest_instance_uuid"))
                    .thenReturn(quest1.toString(), (String) null);

            List<PlayerBoardStateDAO.AcceptedBoardEntry> entries =
                    PlayerBoardStateDAO.loadAcceptedForPlayer(mockConnection, UUID.randomUUID());

            assertEquals(2, entries.size());
            assertEquals(offering1, entries.get(0).offeringId());
            assertEquals(quest1, entries.get(0).questInstanceUUID());
            assertEquals(offering2, entries.get(1).offeringId());
            assertNull(entries.get(1).questInstanceUUID());
        }
    }

    @Nested
    @DisplayName("updateStateByQuestInstanceUUID")
    class UpdateStateByQuestInstanceUUIDTests {

        @DisplayName("Returns updated row count")
        @Test
        void updateStateByQuestInstanceUUID_returnsUpdatedCount() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);

            UUID questUUID = UUID.randomUUID();
            int updated = PlayerBoardStateDAO.updateStateByQuestInstanceUUID(
                    mockConnection, questUUID, "COMPLETED");

            assertEquals(1, updated);
            verify(mockStatement).setString(eq(1), eq("COMPLETED"));
            verify(mockStatement).setString(eq(2), eq(questUUID.toString()));
        }

        @DisplayName("Returns zero when no matching quest instance")
        @Test
        void updateStateByQuestInstanceUUID_returnsZero_whenNoMatch() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int updated = PlayerBoardStateDAO.updateStateByQuestInstanceUUID(
                    mockConnection, UUID.randomUUID(), "CANCELLED");

            assertEquals(0, updated);
        }
    }

    @Nested
    @DisplayName("bulkCancelExpiredBoardStates")
    class BulkCancelExpiredBoardStatesTests {

        @DisplayName("Returns count of updated rows")
        @Test
        void bulkCancelExpiredBoardStates_returnsUpdatedCount() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(4);

            int updated = PlayerBoardStateDAO.bulkCancelExpiredBoardStates(mockConnection);

            assertEquals(4, updated);
        }

        @DisplayName("Returns zero when no expired board states")
        @Test
        void bulkCancelExpiredBoardStates_returnsZero_whenNoneExpired() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(0);

            int updated = PlayerBoardStateDAO.bulkCancelExpiredBoardStates(mockConnection);

            assertEquals(0, updated);
        }
    }

    @Nested
    @DisplayName("AcceptedBoardEntry")
    class AcceptedBoardEntryTests {

        @DisplayName("Accessors return constructor values")
        @Test
        void acceptedBoardEntry_accessorsReturnCorrectValues() {
            UUID offeringId = UUID.randomUUID();
            UUID questUUID = UUID.randomUUID();

            PlayerBoardStateDAO.AcceptedBoardEntry entry =
                    new PlayerBoardStateDAO.AcceptedBoardEntry(offeringId, questUUID);

            assertEquals(offeringId, entry.offeringId());
            assertEquals(questUUID, entry.questInstanceUUID());
        }

        @DisplayName("Nullable questInstanceUUID accepts null")
        @Test
        void acceptedBoardEntry_nullableQuestUUID() {
            UUID offeringId = UUID.randomUUID();

            PlayerBoardStateDAO.AcceptedBoardEntry entry =
                    new PlayerBoardStateDAO.AcceptedBoardEntry(offeringId, null);

            assertEquals(offeringId, entry.offeringId());
            assertNull(entry.questInstanceUUID());
        }

        @DisplayName("Records with same values are equal")
        @Test
        void acceptedBoardEntry_equalRecordsAreEqual() {
            UUID offeringId = UUID.randomUUID();
            UUID questUUID = UUID.randomUUID();

            PlayerBoardStateDAO.AcceptedBoardEntry a =
                    new PlayerBoardStateDAO.AcceptedBoardEntry(offeringId, questUUID);
            PlayerBoardStateDAO.AcceptedBoardEntry b =
                    new PlayerBoardStateDAO.AcceptedBoardEntry(offeringId, questUUID);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }
    }
}
