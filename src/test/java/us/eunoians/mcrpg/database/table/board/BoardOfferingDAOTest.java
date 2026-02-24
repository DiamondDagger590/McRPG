package us.eunoians.mcrpg.database.table.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.BoardOffering;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BoardOfferingDAOTest extends McRPGBaseTest {

    @DisplayName("saveOffering returns prepared statements")
    @Test
    void saveOffering_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "daily_personal"),
                0,
                new NamespacedKey("mcrpg", "mine_stone"),
                new NamespacedKey("mcrpg", "common"),
                null,
                Duration.ofDays(1)
        );

        List<PreparedStatement> statements = BoardOfferingDAO.saveOffering(mockConnection, offering);

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("saveOfferings returns prepared statements for each")
    @Test
    void saveOfferings_returnsPreparedStatementsForEach() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<BoardOffering> offerings = List.of(
                new BoardOffering(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new NamespacedKey("mcrpg", "daily_personal"),
                        0,
                        new NamespacedKey("mcrpg", "mine_stone"),
                        new NamespacedKey("mcrpg", "common"),
                        null,
                        Duration.ofDays(1)
                ),
                new BoardOffering(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new NamespacedKey("mcrpg", "daily_personal"),
                        1,
                        new NamespacedKey("mcrpg", "chop_wood"),
                        new NamespacedKey("mcrpg", "rare"),
                        null,
                        Duration.ofDays(1)
                )
        );

        List<PreparedStatement> statements = BoardOfferingDAO.saveOfferings(mockConnection, offerings);

        assertNotNull(statements);
        assertEquals(2, statements.size());
    }

    @DisplayName("loadOfferingsForRotation returns empty when no results")
    @Test
    void loadOfferingsForRotation_returnsEmptyWhenNoResults() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<BoardOffering> result = BoardOfferingDAO.loadOfferingsForRotation(mockConnection, UUID.randomUUID());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @DisplayName("loadOfferingsForRotation returns offerings when present")
    @Test
    void loadOfferingsForRotation_returnsOfferingsWhenPresent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID rotId = UUID.randomUUID();

        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("offering_id")).thenReturn(id1.toString(), id2.toString());
        when(mockResultSet.getString("rotation_id")).thenReturn(rotId.toString(), rotId.toString());
        when(mockResultSet.getString("category_key")).thenReturn("mcrpg:daily_personal", "mcrpg:daily_personal");
        when(mockResultSet.getInt("slot_index")).thenReturn(0, 1);
        when(mockResultSet.getString("quest_definition_key")).thenReturn("mcrpg:mine_stone", "mcrpg:chop_wood");
        when(mockResultSet.getString("rarity_key")).thenReturn("mcrpg:common", "mcrpg:rare");
        when(mockResultSet.getString("scope_target_id")).thenReturn(null, (String) null);
        when(mockResultSet.getString("state")).thenReturn("VISIBLE", "VISIBLE");
        when(mockResultSet.getLong("accepted_at")).thenReturn(0L, 0L);
        when(mockResultSet.wasNull()).thenReturn(true, true);
        when(mockResultSet.getString("quest_instance_uuid")).thenReturn(null, (String) null);
        when(mockResultSet.getLong("completion_time_ms")).thenReturn(86400000L, 86400000L);

        List<BoardOffering> result = BoardOfferingDAO.loadOfferingsForRotation(mockConnection, rotId);

        assertNotNull(result);
        assertEquals(2, result.size());
        BoardOffering first = result.get(0);
        assertEquals(id1, first.getOfferingId());
        assertEquals(rotId, first.getRotationId());
        assertEquals(new NamespacedKey("mcrpg", "daily_personal"), first.getCategoryKey());
        assertEquals(0, first.getSlotIndex());
        assertEquals(new NamespacedKey("mcrpg", "mine_stone"), first.getQuestDefinitionKey());
        assertEquals(new NamespacedKey("mcrpg", "common"), first.getRarityKey());
        assertEquals(BoardOffering.State.VISIBLE, first.getState());
        assertEquals(Duration.ofDays(1), first.getCompletionTime());
    }

    @DisplayName("updateOfferingState returns prepared statements")
    @Test
    void updateOfferingState_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        UUID offeringId = UUID.randomUUID();
        UUID questInstanceId = UUID.randomUUID();
        long acceptedAt = System.currentTimeMillis();

        List<PreparedStatement> statements = BoardOfferingDAO.updateOfferingState(
                mockConnection,
                offeringId,
                BoardOffering.State.ACCEPTED,
                acceptedAt,
                questInstanceId
        );

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }

    @DisplayName("expireOfferingsForRotation returns prepared statements")
    @Test
    void expireOfferingsForRotation_returnsPreparedStatements() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        UUID rotationId = UUID.randomUUID();

        List<PreparedStatement> statements = BoardOfferingDAO.expireOfferingsForRotation(mockConnection, rotationId);

        assertNotNull(statements);
        assertFalse(statements.isEmpty());
    }
}
