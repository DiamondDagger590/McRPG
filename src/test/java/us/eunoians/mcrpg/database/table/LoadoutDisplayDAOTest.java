package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutDisplay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link LoadoutDisplayDAO}.
 * <p>
 * The JDBC layer is fully mocked so the tests run without a real database.
 */
class LoadoutDisplayDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Test
    @DisplayName("attemptCreateTable returns false when table already exists")
    void attemptCreateTable_returnsFalse_whenTableExists() {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, LoadoutDisplayDAO.TABLE_NAME)).thenReturn(true);

        boolean result = LoadoutDisplayDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("attemptCreateTable returns true when table does not exist")
    void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockDatabase.tableExists(mockConnection, LoadoutDisplayDAO.TABLE_NAME)).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        boolean result = LoadoutDisplayDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertTrue(result);
        verify(mockStatement).executeUpdate();
    }

    @Test
    @DisplayName("attemptCreateTable returns false when SQLException is thrown")
    void attemptCreateTable_returnsFalse_whenSQLExceptionThrown() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabase.tableExists(mockConnection, LoadoutDisplayDAO.TABLE_NAME)).thenReturn(false);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

        boolean result = LoadoutDisplayDAO.attemptCreateTable(mockConnection, mockDatabase);

        assertFalse(result);
    }

    @Test
    @DisplayName("saveLoadoutDisplay binds correct parameters")
    void saveLoadoutDisplay_bindsCorrectParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        CustomItemWrapper mockItem = mock(CustomItemWrapper.class);
        when(mockItem.customItem()).thenReturn(Optional.of("DIAMOND_SWORD"));
        when(mockItem.material()).thenReturn(Optional.of(Material.DIAMOND_SWORD));

        LoadoutDisplay mockDisplay = mock(LoadoutDisplay.class);
        when(mockDisplay.getDisplayItem()).thenReturn(mockItem);
        when(mockDisplay.getDisplayName()).thenReturn(Optional.of("My Loadout"));

        int loadoutSlot = 2;

        LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, loadoutSlot, mockDisplay);

        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, loadoutSlot);
        verify(mockStatement).setString(3, "DIAMOND_SWORD");
        verify(mockStatement).setString(4, "My Loadout");
    }

    @Test
    @DisplayName("saveLoadoutDisplay binds null display name when absent")
    void saveLoadoutDisplay_bindsNullDisplayName_whenDisplayNameAbsent() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        CustomItemWrapper mockItem = mock(CustomItemWrapper.class);
        when(mockItem.customItem()).thenReturn(Optional.of("STONE"));
        when(mockItem.material()).thenReturn(Optional.of(Material.STONE));

        LoadoutDisplay mockDisplay = mock(LoadoutDisplay.class);
        when(mockDisplay.getDisplayItem()).thenReturn(mockItem);
        when(mockDisplay.getDisplayName()).thenReturn(Optional.empty());

        LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, 1, mockDisplay);

        verify(mockStatement).setString(4, null);
    }

    @Test
    @DisplayName("saveLoadoutDisplay with Loadout returns empty list when display should not be saved")
    void saveLoadoutDisplay_returnsEmptyList_whenShouldNotSaveDisplay() {
        Connection mockConnection = mock(Connection.class);

        Loadout mockLoadout = mock(Loadout.class);
        when(mockLoadout.shouldSaveDisplay()).thenReturn(false);

        List<PreparedStatement> result = LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, mockLoadout);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("saveLoadoutDisplay with Loadout delegates to overload when display should be saved")
    void saveLoadoutDisplay_delegatesToOverload_whenShouldSaveDisplay() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        CustomItemWrapper mockItem = mock(CustomItemWrapper.class);
        when(mockItem.customItem()).thenReturn(Optional.of("CHERRY_SIGN"));
        when(mockItem.material()).thenReturn(Optional.of(Material.CHERRY_SIGN));

        LoadoutDisplay mockDisplay = mock(LoadoutDisplay.class);
        when(mockDisplay.getDisplayItem()).thenReturn(mockItem);
        when(mockDisplay.getDisplayName()).thenReturn(Optional.of("Custom Name"));

        Loadout mockLoadout = mock(Loadout.class);
        when(mockLoadout.shouldSaveDisplay()).thenReturn(true);
        when(mockLoadout.getLoadoutSlot()).thenReturn(3);
        when(mockLoadout.getDisplay()).thenReturn(mockDisplay);

        List<PreparedStatement> result = LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, mockLoadout);

        assertEquals(1, result.size());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, 3);
    }

    @Test
    @DisplayName("deleteLoadoutDisplay binds correct parameters")
    void deleteLoadoutDisplay_bindsCorrectParameters() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        int loadoutSlot = 5;

        List<PreparedStatement> result = LoadoutDisplayDAO.deleteLoadoutDisplay(mockConnection, PLAYER_UUID, loadoutSlot);

        assertEquals(1, result.size());
        verify(mockStatement).setString(1, PLAYER_UUID.toString());
        verify(mockStatement).setInt(2, loadoutSlot);
    }

    @Test
    @DisplayName("getLoadoutDisplay returns empty when no row exists")
    void getLoadoutDisplay_returnsEmpty_whenNoRow() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<LoadoutDisplay> result = LoadoutDisplayDAO.getLoadoutDisplay(mockConnection, PLAYER_UUID, 1);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getLoadoutDisplay returns display when row exists")
    void getLoadoutDisplay_returnsDisplay_whenRowExists() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("display_item")).thenReturn("DIAMOND_SWORD");
        when(mockResultSet.getString("display_name")).thenReturn("Battle Loadout");

        Optional<LoadoutDisplay> result = LoadoutDisplayDAO.getLoadoutDisplay(mockConnection, PLAYER_UUID, 1);

        assertTrue(result.isPresent());
    }
}
