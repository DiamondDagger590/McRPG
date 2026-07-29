package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutDisplay;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock-based unit tests for {@link LoadoutDisplayDAO}.
 * <p>
 * The JDBC layer is fully mocked so the tests run without a real database.
 */
class LoadoutDisplayDAOTest extends McRPGBaseTest {

    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("returns false when table already exists")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, LoadoutDisplayDAO.TABLE_NAME)).thenReturn(true);

            boolean result = LoadoutDisplayDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("returns true when table does not exist")
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
        @DisplayName("returns false when SQLException is thrown")
        void attemptCreateTable_returnsFalse_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, LoadoutDisplayDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = LoadoutDisplayDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("updateTable")
    class UpdateTable {

        @Test
        @DisplayName("skips all migrations when version is current")
        void updateTable_skipsAll_whenVersionIsCurrent() throws SQLException {
            Connection mockConnection = mock(Connection.class);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME))
                        .thenReturn(2);

                LoadoutDisplayDAO.updateTable(mockConnection);

                verify(mockConnection, never()).prepareStatement(anyString());
            }
        }

        @Test
        @DisplayName("creates index and sets version when at version 0")
        void updateTable_createsIndex_whenAtVersionZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockIndexStatement = mock(PreparedStatement.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
            ResultSet mockMetaResultSet = mock(ResultSet.class);
            PreparedStatement mockPragmaStatement = mock(PreparedStatement.class);
            ResultSet mockPragmaResultSet = mock(ResultSet.class);

            when(mockConnection.prepareStatement(org.mockito.ArgumentMatchers.contains("CREATE INDEX")))
                    .thenReturn(mockIndexStatement);
            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumns(isNull(), isNull(), anyString(), anyString()))
                    .thenReturn(mockMetaResultSet);
            when(mockMetaResultSet.next()).thenReturn(true);
            when(mockConnection.prepareStatement(org.mockito.ArgumentMatchers.contains("PRAGMA")))
                    .thenReturn(mockPragmaStatement);
            when(mockPragmaStatement.executeQuery()).thenReturn(mockPragmaResultSet);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME))
                        .thenReturn(0);

                LoadoutDisplayDAO.updateTable(mockConnection);

                verify(mockIndexStatement).executeUpdate();
                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME, 1));
                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME, 2));
            }
        }

        @Test
        @DisplayName("skips index creation and only runs version 2 migration when at version 1")
        void updateTable_skipsIndex_whenAtVersionOne() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
            ResultSet mockMetaResultSet = mock(ResultSet.class);

            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumns(isNull(), isNull(), anyString(), anyString()))
                    .thenReturn(mockMetaResultSet);
            when(mockMetaResultSet.next()).thenReturn(true);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME))
                        .thenReturn(1);

                LoadoutDisplayDAO.updateTable(mockConnection);

                verify(mockConnection, never()).prepareStatement(org.mockito.ArgumentMatchers.contains("CREATE INDEX"));
                mockedHistory.verify(() -> TableVersionHistoryDAO.setTableVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME, 2));
            }
        }

        @Test
        @DisplayName("version 2 adds column when column does not exist via metadata")
        void updateTable_addsColumn_whenColumnMissing() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
            ResultSet mockMetaResultSet = mock(ResultSet.class);
            PreparedStatement mockPragmaStatement = mock(PreparedStatement.class);
            ResultSet mockPragmaResultSet = mock(ResultSet.class);
            PreparedStatement mockAlterStatement = mock(PreparedStatement.class);

            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumns(isNull(), isNull(), anyString(), anyString()))
                    .thenReturn(mockMetaResultSet);
            when(mockMetaResultSet.next()).thenReturn(false);
            when(mockConnection.prepareStatement(org.mockito.ArgumentMatchers.contains("PRAGMA")))
                    .thenReturn(mockPragmaStatement);
            when(mockPragmaStatement.executeQuery()).thenReturn(mockPragmaResultSet);
            when(mockPragmaResultSet.next()).thenReturn(false);
            when(mockConnection.prepareStatement(org.mockito.ArgumentMatchers.contains("ALTER TABLE")))
                    .thenReturn(mockAlterStatement);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME))
                        .thenReturn(1);

                LoadoutDisplayDAO.updateTable(mockConnection);

                verify(mockAlterStatement).executeUpdate();
            }
        }

        @Test
        @DisplayName("version 2 skips alter when column already exists via metadata")
        void updateTable_skipsAlter_whenColumnExists() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);
            ResultSet mockMetaResultSet = mock(ResultSet.class);

            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getColumns(isNull(), isNull(), anyString(), anyString()))
                    .thenReturn(mockMetaResultSet);
            when(mockMetaResultSet.next()).thenReturn(true);

            try (MockedStatic<TableVersionHistoryDAO> mockedHistory = mockStatic(TableVersionHistoryDAO.class)) {
                mockedHistory.when(() -> TableVersionHistoryDAO.getLatestVersion(mockConnection, LoadoutDisplayDAO.TABLE_NAME))
                        .thenReturn(1);

                LoadoutDisplayDAO.updateTable(mockConnection);

                verify(mockConnection, never()).prepareStatement(org.mockito.ArgumentMatchers.contains("ALTER TABLE"));
            }
        }
    }

    @Nested
    @DisplayName("saveLoadoutDisplay (direct)")
    class SaveLoadoutDisplayDirect {

        @Test
        @DisplayName("binds correct parameters")
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
        @DisplayName("binds null display name when absent")
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
        @DisplayName("falls back to material name when custom item is absent")
        void saveLoadoutDisplay_fallsBackToMaterial_whenCustomItemAbsent() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            CustomItemWrapper mockItem = mock(CustomItemWrapper.class);
            when(mockItem.customItem()).thenReturn(Optional.empty());
            when(mockItem.material()).thenReturn(Optional.of(Material.OAK_LOG));

            LoadoutDisplay mockDisplay = mock(LoadoutDisplay.class);
            when(mockDisplay.getDisplayItem()).thenReturn(mockItem);
            when(mockDisplay.getDisplayName()).thenReturn(Optional.empty());

            LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, 1, mockDisplay);

            verify(mockStatement).setString(3, "OAK_LOG");
        }

        @Test
        @DisplayName("returns empty list when SQLException is thrown")
        void saveLoadoutDisplay_returnsEmptyList_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            CustomItemWrapper mockItem = mock(CustomItemWrapper.class);
            when(mockItem.customItem()).thenReturn(Optional.of("STONE"));
            when(mockItem.material()).thenReturn(Optional.of(Material.STONE));

            LoadoutDisplay mockDisplay = mock(LoadoutDisplay.class);
            when(mockDisplay.getDisplayItem()).thenReturn(mockItem);
            when(mockDisplay.getDisplayName()).thenReturn(Optional.empty());

            List<PreparedStatement> result = LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, 1, mockDisplay);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveLoadoutDisplay (Loadout overload)")
    class SaveLoadoutDisplayLoadout {

        @Test
        @DisplayName("returns empty list when display should not be saved")
        void saveLoadoutDisplay_returnsEmptyList_whenShouldNotSaveDisplay() {
            Connection mockConnection = mock(Connection.class);

            Loadout mockLoadout = mock(Loadout.class);
            when(mockLoadout.shouldSaveDisplay()).thenReturn(false);

            List<PreparedStatement> result = LoadoutDisplayDAO.saveLoadoutDisplay(mockConnection, PLAYER_UUID, mockLoadout);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("delegates to direct overload when display should be saved")
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
    }

    @Nested
    @DisplayName("saveAllLoadoutDisplays")
    class SaveAllLoadoutDisplays {

        @Test
        @DisplayName("iterates over all loadout slots and collects statements")
        void saveAllLoadoutDisplays_iteratesAllSlots() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            PreparedStatement mockInsertStatement = mock(PreparedStatement.class);

            when(mockConnection.prepareStatement(org.mockito.ArgumentMatchers.contains("DELETE")))
                    .thenReturn(mockDeleteStatement);
            when(mockConnection.prepareStatement(org.mockito.ArgumentMatchers.contains("INSERT")))
                    .thenReturn(mockInsertStatement);

            CustomItemWrapper mockItem = mock(CustomItemWrapper.class);
            when(mockItem.customItem()).thenReturn(Optional.of("STONE"));
            when(mockItem.material()).thenReturn(Optional.of(Material.STONE));

            LoadoutDisplay mockDisplay = mock(LoadoutDisplay.class);
            when(mockDisplay.getDisplayItem()).thenReturn(mockItem);
            when(mockDisplay.getDisplayName()).thenReturn(Optional.empty());

            Loadout mockLoadout = mock(Loadout.class);
            when(mockLoadout.shouldSaveDisplay()).thenReturn(true);
            when(mockLoadout.getLoadoutSlot()).thenReturn(1);
            when(mockLoadout.getDisplay()).thenReturn(mockDisplay);

            LoadoutHolder mockHolder = mock(LoadoutHolder.class);
            when(mockHolder.getMaxLoadoutAmount()).thenReturn(2);
            when(mockHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockHolder.getLoadout(1)).thenReturn(mockLoadout);
            when(mockHolder.getLoadout(2)).thenReturn(mockLoadout);

            List<PreparedStatement> result = LoadoutDisplayDAO.saveAllLoadoutDisplays(mockConnection, mockHolder);

            assertEquals(4, result.size());
        }

        @Test
        @DisplayName("returns empty list when holder has zero loadout slots")
        void saveAllLoadoutDisplays_returnsEmpty_whenNoSlots() {
            Connection mockConnection = mock(Connection.class);

            LoadoutHolder mockHolder = mock(LoadoutHolder.class);
            when(mockHolder.getMaxLoadoutAmount()).thenReturn(0);
            when(mockHolder.getUUID()).thenReturn(PLAYER_UUID);

            List<PreparedStatement> result = LoadoutDisplayDAO.saveAllLoadoutDisplays(mockConnection, mockHolder);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteLoadoutDisplay")
    class DeleteLoadoutDisplay {

        @Test
        @DisplayName("binds correct parameters")
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
        @DisplayName("returns empty list when SQLException is thrown")
        void deleteLoadoutDisplay_returnsEmptyList_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            List<PreparedStatement> result = LoadoutDisplayDAO.deleteLoadoutDisplay(mockConnection, PLAYER_UUID, 1);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getLoadoutDisplay")
    class GetLoadoutDisplay {

        @Test
        @DisplayName("returns empty when no row exists")
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
        @DisplayName("returns display when row exists")
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

        @Test
        @DisplayName("returns display with null display name")
        void getLoadoutDisplay_returnsDisplay_whenDisplayNameIsNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getString("display_item")).thenReturn("STONE");
            when(mockResultSet.getString("display_name")).thenReturn(null);

            Optional<LoadoutDisplay> result = LoadoutDisplayDAO.getLoadoutDisplay(mockConnection, PLAYER_UUID, 1);

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("binds UUID and loadout slot parameters")
        void getLoadoutDisplay_bindsParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            LoadoutDisplayDAO.getLoadoutDisplay(mockConnection, PLAYER_UUID, 4);

            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setInt(2, 4);
        }

        @Test
        @DisplayName("returns empty on SQLException")
        void getLoadoutDisplay_returnsEmpty_whenSQLExceptionThrown() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("connection error"));

            Optional<LoadoutDisplay> result = LoadoutDisplayDAO.getLoadoutDisplay(mockConnection, PLAYER_UUID, 1);

            assertTrue(result.isEmpty());
        }
    }
}
