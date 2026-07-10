package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.loadout.Loadout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link LoadoutInfoDAO}.
 * <p>
 * The JDBC layer is fully mocked so the tests run without a real database.
 */
class LoadoutInfoDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Test
    @DisplayName("attemptCreateTable returns false when table exists")
    void attemptCreateTable_returnsFalse_whenTableExists() {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, LoadoutInfoDAO.TABLE_NAME)).thenReturn(true);

        boolean result = LoadoutInfoDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("attemptCreateTable returns true when table does not exist")
    void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, LoadoutInfoDAO.TABLE_NAME)).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean result = LoadoutInfoDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(result);
    }

    @Test
    @DisplayName("attemptCreateTable returns false when SQLException thrown")
    void attemptCreateTable_returnsFalse_whenSQLExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, LoadoutInfoDAO.TABLE_NAME)).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

        boolean result = LoadoutInfoDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("saveLoadoutInfo deletes and inserts when loadout not empty")
    void saveLoadoutInfo_deletesAndInserts_whenLoadoutNotEmpty() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Loadout mockLoadout = mock(Loadout.class);
        when(mockLoadout.getLoadoutSlot()).thenReturn(1);
        when(mockLoadout.getAbilities()).thenReturn(Set.of(new NamespacedKey("mcrpg", "bleed")));

        List<PreparedStatement> result = LoadoutInfoDAO.saveLoadoutInfo(mockConnection, PLAYER_UUID, mockLoadout);

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("saveLoadoutInfo produces only delete when loadout empty")
    void saveLoadoutInfo_producesOnlyDelete_whenLoadoutEmpty() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        Loadout mockLoadout = mock(Loadout.class);
        when(mockLoadout.getLoadoutSlot()).thenReturn(1);
        when(mockLoadout.getAbilities()).thenReturn(Set.of());

        List<PreparedStatement> result = LoadoutInfoDAO.saveLoadoutInfo(mockConnection, PLAYER_UUID, mockLoadout);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("saveLoadoutInfo binds correct parameters on insert")
    void saveLoadoutInfo_bindsCorrectParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
        PreparedStatement mockInsertStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockDeleteStatement, mockInsertStatement);

        Loadout mockLoadout = mock(Loadout.class);
        when(mockLoadout.getLoadoutSlot()).thenReturn(3);
        when(mockLoadout.getAbilities()).thenReturn(Set.of(new NamespacedKey("mcrpg", "bleed")));

        LoadoutInfoDAO.saveLoadoutInfo(mockConnection, PLAYER_UUID, mockLoadout);

        verify(mockInsertStatement).setString(1, PLAYER_UUID.toString());
        verify(mockInsertStatement).setInt(2, 3);
    }

    @Test
    @DisplayName("deleteLoadoutInfo binds correct parameters")
    void deleteLoadoutInfo_bindsCorrectParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        LoadoutInfoDAO.deleteLoadoutInfo(mockConnection, PLAYER_UUID, 2);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, 2);
    }

    @Test
    @DisplayName("deleteLoadoutInfo returns one statement")
    void deleteLoadoutInfo_returnsOneStatement() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        List<PreparedStatement> result = LoadoutInfoDAO.deleteLoadoutInfo(mockConnection, PLAYER_UUID, 1);

        assertEquals(1, result.size());
    }
}
