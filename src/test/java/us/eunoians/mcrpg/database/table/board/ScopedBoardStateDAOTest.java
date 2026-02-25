package us.eunoians.mcrpg.database.table.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScopedBoardStateDAOTest extends McRPGBaseTest {

    private static final NamespacedKey SCOPE_KEY = new NamespacedKey("mcrpg", "land_scope");
    private static final NamespacedKey BOARD_KEY = new NamespacedKey("mcrpg", "default");

    @DisplayName("saveState returns prepared statements with all fields populated")
    @Test
    void saveState_returnsPreparedStatements() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);

        UUID offeringId = UUID.randomUUID();
        UUID acceptedBy = UUID.randomUUID();
        UUID questUuid = UUID.randomUUID();

        List<PreparedStatement> result = ScopedBoardStateDAO.saveState(
                mockConn, "kingdom_alpha", SCOPE_KEY, BOARD_KEY,
                offeringId, "ACCEPTED", 1000L, acceptedBy, questUuid);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(mockPs).setString(1, "kingdom_alpha");
        verify(mockPs).setString(2, SCOPE_KEY.toString());
        verify(mockPs).setString(3, BOARD_KEY.toString());
        verify(mockPs).setString(4, offeringId.toString());
        verify(mockPs).setString(5, "ACCEPTED");
        verify(mockPs).setLong(6, 1000L);
        verify(mockPs).setString(7, acceptedBy.toString());
        verify(mockPs).setString(8, questUuid.toString());
    }

    @DisplayName("saveState with null optional fields sets null correctly")
    @Test
    void saveState_nullOptionalFields_setsNullCorrectly() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);

        List<PreparedStatement> result = ScopedBoardStateDAO.saveState(
                mockConn, "kingdom_beta", SCOPE_KEY, BOARD_KEY,
                UUID.randomUUID(), "VISIBLE", null, null, null);

        assertFalse(result.isEmpty());
        verify(mockPs).setNull(eq(6), eq(java.sql.Types.BIGINT));
        verify(mockPs).setString(7, null);
        verify(mockPs).setString(8, null);
    }

    @DisplayName("loadStatesForEntity returns deserialized records")
    @Test
    void loadStatesForEntity_returnsRecords() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        UUID offeringId = UUID.randomUUID();
        UUID acceptedBy = UUID.randomUUID();
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getString("scope_entity_id")).thenReturn("kingdom_alpha");
        when(mockRs.getString("scope_provider_key")).thenReturn(SCOPE_KEY.toString());
        when(mockRs.getString("board_key")).thenReturn(BOARD_KEY.toString());
        when(mockRs.getString("offering_id")).thenReturn(offeringId.toString());
        when(mockRs.getString("state")).thenReturn("ACCEPTED");
        when(mockRs.getLong("accepted_at")).thenReturn(5000L);
        when(mockRs.wasNull()).thenReturn(false);
        when(mockRs.getString("accepted_by")).thenReturn(acceptedBy.toString());
        when(mockRs.getString("quest_instance_uuid")).thenReturn(null);

        List<ScopedBoardStateDAO.ScopedBoardStateRecord> records =
                ScopedBoardStateDAO.loadStatesForEntity(mockConn, "kingdom_alpha", BOARD_KEY);

        assertEquals(1, records.size());
        ScopedBoardStateDAO.ScopedBoardStateRecord record = records.get(0);
        assertEquals("kingdom_alpha", record.scopeEntityId());
        assertEquals(SCOPE_KEY, record.scopeProviderKey());
        assertEquals(BOARD_KEY, record.boardKey());
        assertEquals(offeringId, record.offeringId());
        assertEquals("ACCEPTED", record.state());
        assertEquals(5000L, record.acceptedAt());
        assertEquals(acceptedBy, record.acceptedBy());
        assertEquals(null, record.questInstanceUUID());
    }

    @DisplayName("loadStatesForEntity with empty result returns empty list")
    @Test
    void loadStatesForEntity_emptyResult_returnsEmptyList() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(false);

        List<ScopedBoardStateDAO.ScopedBoardStateRecord> records =
                ScopedBoardStateDAO.loadStatesForEntity(mockConn, "nonexistent", BOARD_KEY);

        assertTrue(records.isEmpty());
    }

    @DisplayName("countActiveQuestsForEntity returns count from result set")
    @Test
    void countActiveQuestsForEntity_returnsCount() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getInt(1)).thenReturn(3);

        int count = ScopedBoardStateDAO.countActiveQuestsForEntity(mockConn, "kingdom_alpha", BOARD_KEY);

        assertEquals(3, count);
    }

    @DisplayName("updateState returns prepared statement with correct bindings")
    @Test
    void updateState_returnsPreparedStatement() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);

        UUID offeringId = UUID.randomUUID();
        PreparedStatement result = ScopedBoardStateDAO.updateState(
                mockConn, "kingdom_alpha", BOARD_KEY, offeringId, "COMPLETED");

        assertNotNull(result);
        verify(mockPs).setString(1, "COMPLETED");
        verify(mockPs).setString(2, "kingdom_alpha");
        verify(mockPs).setString(3, BOARD_KEY.toString());
        verify(mockPs).setString(4, offeringId.toString());
    }

    @DisplayName("deleteStatesForEntity returns prepared statements")
    @Test
    void deleteStatesForEntity_returnsPreparedStatements() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);

        List<PreparedStatement> result = ScopedBoardStateDAO.deleteStatesForEntity(mockConn, "kingdom_alpha");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(mockPs).setString(1, "kingdom_alpha");
    }

    @DisplayName("loadAcceptedStatesForEntity returns only ACCEPTED records")
    @Test
    void loadAcceptedStatesForEntity_returnsOnlyAccepted() throws SQLException {
        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPs = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPs);
        when(mockPs.executeQuery()).thenReturn(mockRs);

        UUID offeringId = UUID.randomUUID();
        UUID questUuid = UUID.randomUUID();
        when(mockRs.next()).thenReturn(true, false);
        when(mockRs.getString("scope_entity_id")).thenReturn("kingdom_alpha");
        when(mockRs.getString("scope_provider_key")).thenReturn(SCOPE_KEY.toString());
        when(mockRs.getString("board_key")).thenReturn(BOARD_KEY.toString());
        when(mockRs.getString("offering_id")).thenReturn(offeringId.toString());
        when(mockRs.getString("state")).thenReturn("ACCEPTED");
        when(mockRs.getLong("accepted_at")).thenReturn(9000L);
        when(mockRs.wasNull()).thenReturn(false);
        when(mockRs.getString("accepted_by")).thenReturn(null);
        when(mockRs.getString("quest_instance_uuid")).thenReturn(questUuid.toString());

        List<ScopedBoardStateDAO.ScopedBoardStateRecord> records =
                ScopedBoardStateDAO.loadAcceptedStatesForEntity(mockConn, "kingdom_alpha");

        assertEquals(1, records.size());
        assertEquals("ACCEPTED", records.get(0).state());
        assertEquals(questUuid, records.get(0).questInstanceUUID());
    }
}
