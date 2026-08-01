package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link CombatPersistentStateDAO}. Follows the pattern established by
 * {@link PlayerStatDAOTest}: the JDBC layer is fully mocked so the tests run without a real database.
 */
class CombatPersistentStateDAOTest extends McRPGBaseTest {

    private static final UUID ENTITY_UUID = UUID.randomUUID();
    private static final String STATE_KEY = "mcrpg:combats_today";

    @DisplayName("attemptCreateTable creates the table when it does not exist")
    @Test
    void attemptCreateTable_createsTable_whenAbsent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, CombatPersistentStateDAO.TABLE_NAME)).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean created = CombatPersistentStateDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(created);
        verify(mockConnection).prepareStatement(contains("CREATE TABLE"));
        verify(mockStatement).executeUpdate();
    }

    @DisplayName("attemptCreateTable returns false when the table already exists")
    @Test
    void attemptCreateTable_returnsFalse_whenAlreadyExists() {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, CombatPersistentStateDAO.TABLE_NAME)).thenReturn(true);

        boolean created = CombatPersistentStateDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(created);
    }

    @DisplayName("savePersistentState upserts with entity_uuid, state_key, and serialized_value bound")
    @Test
    void savePersistentState_bindsAllParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = CombatPersistentStateDAO.savePersistentState(
                mockConnection, ENTITY_UUID, STATE_KEY, "4");

        assertEquals(1, statements.size());
        verify(mockConnection).prepareStatement(contains("ON CONFLICT"));
        verify(mockStatement).setString(1, ENTITY_UUID.toString());
        verify(mockStatement).setString(2, STATE_KEY);
        verify(mockStatement).setString(3, "4");
    }

    @DisplayName("loadPersistentState returns an empty map for an entity with no persisted state")
    @Test
    void loadPersistentState_returnsEmptyMap_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Map<String, String> result = CombatPersistentStateDAO.loadPersistentState(mockConnection, ENTITY_UUID);

        assertTrue(result.isEmpty());
    }

    @DisplayName("loadPersistentState returns all persisted rows keyed by state_key")
    @Test
    void loadPersistentState_returnsAllRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("state_key")).thenReturn(STATE_KEY);
        when(mockResultSet.getString("serialized_value")).thenReturn("7");

        Map<String, String> result = CombatPersistentStateDAO.loadPersistentState(mockConnection, ENTITY_UUID);

        assertEquals(1, result.size());
        assertEquals("7", result.get(STATE_KEY));
    }

    @DisplayName("deleteAllForEntity binds the entity UUID and deletes all rows for it")
    @Test
    void deleteAllForEntity_bindsEntityUUID() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> statements = CombatPersistentStateDAO.deleteAllForEntity(mockConnection, ENTITY_UUID);

        assertEquals(1, statements.size());
        verify(mockConnection).prepareStatement(contains("DELETE FROM"));
        verify(mockStatement).setString(1, ENTITY_UUID.toString());
    }
}
