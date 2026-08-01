package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.log.CombatLogEntry;
import us.eunoians.mcrpg.combat.log.KillOnLogoutPunishment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link CombatLogDAO}. Follows the pattern established by
 * {@link CombatPersistentStateDAOTest}: the JDBC layer is fully mocked so the tests run without a
 * real database.
 */
class CombatLogDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    private CombatLogEntry entry(long id, Instant timestamp) {
        return new CombatLogEntry(id, PLAYER_UUID, timestamp, "world", 1.0, 2.0, 3.0,
                CombatType.PVP, List.of(UUID.randomUUID()), List.of(new KillOnLogoutPunishment((NamespacedKey) null)));
    }

    @Test
    @DisplayName("attemptCreateTable creates the table when it does not exist")
    void attemptCreateTable_createsTable_whenAbsent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, CombatLogDAO.TABLE_NAME)).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean created = CombatLogDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(created);
        verify(mockConnection).prepareStatement(contains("CREATE TABLE"));
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("attemptCreateTable returns false when the table already exists")
    void attemptCreateTable_returnsFalse_whenAlreadyExists() {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, CombatLogDAO.TABLE_NAME)).thenReturn(true);

        boolean created = CombatLogDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(!created);
    }

    @Test
    @DisplayName("insertCombatLog binds all fields including comma-joined participants and punishments")
    void insertCombatLog_bindsAllFields() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Instant timestamp = Instant.now();
        CombatLogEntry entry = entry(0, timestamp);

        List<PreparedStatement> statements = CombatLogDAO.insertCombatLog(mockConnection, entry);

        assertEquals(1, statements.size());
        verify(mockConnection).prepareStatement(contains("INSERT INTO"));
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setLong(2, timestamp.toEpochMilli());
        verify(mockStatement).setString(3, "world");
        verify(mockStatement).setString(7, "PVP");
    }

    @Test
    @DisplayName("getCombatLogHistory returns an empty list for a player with no entries")
    void getCombatLogHistory_returnsEmptyList_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<CombatLogEntry> result = CombatLogDAO.getCombatLogHistory(mockConnection, PLAYER_UUID, 1, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getCombatLogHistory parses rows into entries, resolving punishment types from the registry")
    void getCombatLogHistory_parsesRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        UUID participantUUID = UUID.randomUUID();
        long timestampMillis = Instant.now().toEpochMilli();
        when(mockResultSet.getLong("id")).thenReturn(1L);
        when(mockResultSet.getString("player_uuid")).thenReturn(PLAYER_UUID.toString());
        when(mockResultSet.getLong("timestamp")).thenReturn(timestampMillis);
        when(mockResultSet.getString("world")).thenReturn("world");
        when(mockResultSet.getDouble("x")).thenReturn(1.0);
        when(mockResultSet.getDouble("y")).thenReturn(2.0);
        when(mockResultSet.getDouble("z")).thenReturn(3.0);
        when(mockResultSet.getString("combat_type")).thenReturn("PVP");
        when(mockResultSet.getString("participant_uuids")).thenReturn(participantUUID.toString());
        when(mockResultSet.getString("punishments_applied"))
                .thenReturn(KillOnLogoutPunishment.KEY.toString());

        List<CombatLogEntry> result = CombatLogDAO.getCombatLogHistory(mockConnection, PLAYER_UUID, 1, 10);

        assertEquals(1, result.size());
        CombatLogEntry parsed = result.get(0);
        assertEquals(1L, parsed.id());
        assertEquals(PLAYER_UUID, parsed.playerUUID());
        assertEquals(CombatType.PVP, parsed.combatType());
        assertEquals(List.of(participantUUID), parsed.participantUUIDs());
        assertEquals(List.of(new KillOnLogoutPunishment((NamespacedKey) null)), parsed.punishmentsApplied());
    }

    @Test
    @DisplayName("getCombatLogHistory paginates using LIMIT and OFFSET")
    void getCombatLogHistory_paginatesCorrectly() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        CombatLogDAO.getCombatLogHistory(mockConnection, PLAYER_UUID, 2, 10);

        verify(mockStatement).setInt(2, 10);
        verify(mockStatement).setInt(3, 10);
    }

    @Test
    @DisplayName("getCombatLogCount returns the correct total for a player with entries")
    void getCombatLogCount_returnsCorrectTotal() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(15);

        OptionalInt count = CombatLogDAO.getCombatLogCount(mockConnection, PLAYER_UUID);

        assertEquals(15, count.orElseThrow());
    }

    @Test
    @DisplayName("getCombatLogCount returns 0 for a player with no entries")
    void getCombatLogCount_returnsZero_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        OptionalInt count = CombatLogDAO.getCombatLogCount(mockConnection, PLAYER_UUID);

        assertEquals(0, count.orElseThrow());
    }

    @Test
    @DisplayName("getCombatLogCount returns empty when the query fails")
    void getCombatLogCount_returnsEmpty_whenQueryFails() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenThrow(new SQLException("boom"));

        OptionalInt count = CombatLogDAO.getCombatLogCount(mockConnection, PLAYER_UUID);

        assertTrue(count.isEmpty());
    }

    @Test
    @DisplayName("deleteOlderThan sums across batches until a batch comes back short")
    void deleteOlderThan_sumsAcrossBatches() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        // A full batch means there may be more; the short second batch ends the sweep.
        when(mockStatement.executeUpdate()).thenReturn(500, 200, 0);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);
        int deleted = CombatLogDAO.deleteOlderThan(mockConnection, cutoff);

        assertEquals(700, deleted);
        verify(mockConnection).prepareStatement(contains("DELETE FROM"));
        verify(mockStatement, atLeastOnce()).setLong(1, cutoff.toEpochMilli());
    }

    @Test
    @DisplayName("deleteOlderThan returns 0 when no entries are older than the cutoff")
    void deleteOlderThan_returnsZero_whenNoneOlder() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0);

        int deleted = CombatLogDAO.deleteOlderThan(mockConnection, Instant.now());

        assertEquals(0, deleted);
    }

    @Test
    @DisplayName("getCombatLogHistory skips unparsable rows and returns remaining valid entries")
    void getCombatLogHistory_skipsUnparsableRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        UUID participantUUID = UUID.randomUUID();
        long timestampMillis = Instant.now().toEpochMilli();

        when(mockResultSet.getLong("id")).thenReturn(1L, 2L);
        when(mockResultSet.getString("player_uuid")).thenReturn(PLAYER_UUID.toString(), PLAYER_UUID.toString());
        when(mockResultSet.getLong("timestamp")).thenReturn(timestampMillis, timestampMillis);
        when(mockResultSet.getString("world")).thenReturn("world", "world");
        when(mockResultSet.getDouble("x")).thenReturn(1.0, 1.0);
        when(mockResultSet.getDouble("y")).thenReturn(2.0, 2.0);
        when(mockResultSet.getDouble("z")).thenReturn(3.0, 3.0);
        when(mockResultSet.getString("participant_uuids")).thenReturn(participantUUID.toString(), participantUUID.toString());
        when(mockResultSet.getString("punishments_applied"))
                .thenReturn(KillOnLogoutPunishment.KEY.toString(), KillOnLogoutPunishment.KEY.toString());
        when(mockResultSet.getString("combat_type")).thenReturn("INVALID_ENUM", "PVP");

        List<CombatLogEntry> result = CombatLogDAO.getCombatLogHistory(mockConnection, PLAYER_UUID, 1, 10);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).id());
        assertEquals(CombatType.PVP, result.get(0).combatType());
    }
}
