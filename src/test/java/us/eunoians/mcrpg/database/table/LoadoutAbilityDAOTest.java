package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link LoadoutAbilityDAO}.
 * <p>
 * The JDBC layer is fully mocked so the tests run without a real database.
 * Tests that call {@link LoadoutAbilityDAO#getLoadout(Connection, UUID, int)} rely on
 * MockBukkit-loaded McRPG for {@link us.eunoians.mcrpg.ability.AbilityRegistry} access.
 */
class LoadoutAbilityDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        if (registryAccess.registry(McRPGRegistryKey.ABILITY) == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
        }
    }

    @Test
    @DisplayName("attemptCreateTable returns false when table already exists")
    void attemptCreateTable_returnsFalse_whenTableExists() {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_loadout")).thenReturn(true);

        boolean result = LoadoutAbilityDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("attemptCreateTable returns true when table does not exist")
    void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_loadout")).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean result = LoadoutAbilityDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(result);
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("attemptCreateTable returns false when SQLException is thrown")
    void attemptCreateTable_returnsFalse_whenSQLExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, "mcrpg_loadout")).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

        boolean result = LoadoutAbilityDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("deleteLoadout returns a list with one statement")
    void deleteLoadout_returnsOneStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = LoadoutAbilityDAO.deleteLoadout(mockConnection, PLAYER_UUID, 1);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("deleteLoadout binds correct parameters")
    void deleteLoadout_bindsCorrectParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        int loadoutId = 3;

        LoadoutAbilityDAO.deleteLoadout(mockConnection, PLAYER_UUID, loadoutId);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, loadoutId);
    }

    @Test
    @DisplayName("getLoadout returns empty loadout when no rows exist")
    void getLoadout_returnsEmptyLoadout_whenNoRows() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Loadout loadout = LoadoutAbilityDAO.getLoadout(mockConnection, PLAYER_UUID, 1);

        assertTrue(loadout.getAbilities().isEmpty());
    }

    @Test
    @DisplayName("getLoadout skips unregistered abilities")
    void getLoadout_skipsUnregisteredAbilities() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("ability_id")).thenReturn("nonexistent_ability_xyz");

        Loadout loadout = LoadoutAbilityDAO.getLoadout(mockConnection, PLAYER_UUID, 1);

        assertTrue(loadout.getAbilities().isEmpty());
    }
}
