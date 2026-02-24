package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutDisplay;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This DAO is used to store and access a {@link us.eunoians.mcrpg.loadout.Loadout}'s
 * {@link us.eunoians.mcrpg.loadout.LoadoutDisplay}.
 */
public class LoadoutDisplayDAO {

    static final String TABLE_NAME = "mcrpg_loadout_display";
    private static final int CURRENT_TABLE_VERSION = 2;
    private static final String DISPLAY_ITEM_COLUMN = "display_item";

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection The {@link Connection} to use to attempt the creation
     * @param database   The {@link Database} being used to attempt to create the table
     * @return {@code true} if a new table was made or {@code false} otherwise.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        //Check to see if the table already exists
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }

        /*****
         ** Table Description:
         ** Contains player loadout slots
         *
         *
         * loadout_id is the slot of the player's loadout the data belongs to
         **
         ** Reasoning for structure:
         ** PK is the composite of `loadout_id` field, `slot_number` and `uuid`, as a loadout id will be present once for each ability in the loadout,
         * so the combination of that and the slot number will be used to look up individual abilities and each player uuid is unique.
         *
         * The foreign key requires the player's uuid to be present in the loadout table as that's where the player's loadout info is stored
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`holder_uuid` varchar(36) NOT NULL," +
                "`loadout_id` int(11) NOT NULL DEFAULT 1," +
                "`display_item` varchar(96) NOT NULL," +
                "`display_name` varchar(32) NULL," +
                "PRIMARY KEY (`holder_uuid`, `loadout_id`), " +
                // Ensure that the loadout is stored in the info table, also if it ever gets removed from that table, ensure it's deleted here
                "CONSTRAINT FK_loadout FOREIGN KEY (`holder_uuid`, `loadout_id`) REFERENCES " + LoadoutInfoDAO.TABLE_NAME + " (`holder_uuid`, `loadout_id`) ON DELETE CASCADE" +
                ");")) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }

        // Version 1: initial tracking + index
        if (lastStoredVersion == 0) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE INDEX holder_uuid_index_loadout_display ON " + TABLE_NAME + " (holder_uuid)")) {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }

        // Version 2: add display_item column for older installs that created the table without it
        if (lastStoredVersion == 1) {
            ensureColumnExists(connection, DISPLAY_ITEM_COLUMN, "VARCHAR(96) NOT NULL DEFAULT 'STONE'");
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 2);
        }
    }

    private static void ensureColumnExists(@NotNull Connection connection, @NotNull String columnName, @NotNull String addColumnSqlFragment) {
        if (columnExists(connection, TABLE_NAME, columnName)) {
            return;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + addColumnSqlFragment)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean columnExists(@NotNull Connection connection, @NotNull String tableName, @NotNull String columnName) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
                    return true;
                }
            }
            // SQLite metadata lookups can be finicky with case; fallback to a pragma check.
            try (PreparedStatement ps = connection.prepareStatement("PRAGMA table_info(" + tableName + ")");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                        return true;
                    }
                }
            }
        } catch (SQLException ignored) {
            // If we can't determine it, we'll assume it doesn't exist and let the ALTER attempt decide.
        }
        return false;
    }

    /**
     * Gets a {@link List} of {@link PreparedStatement}s to be run in order to save all the {@link LoadoutDisplay}s for the provided {@link LoadoutHolder}.
     *
     * @param connection    The {@link Connection} to save on.
     * @param loadoutHolder The {@link LoadoutHolder} to save {@link LoadoutDisplay}s for.
     * @return A {@link List} of {@link PreparedStatement}s to be run in order to save all the {@link LoadoutDisplay}s for the provided {@link LoadoutHolder}.
     */
    @NotNull
    public static List<PreparedStatement> saveAllLoadoutDisplays(@NotNull Connection connection, @NotNull LoadoutHolder loadoutHolder) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        for (int i = 1; i <= loadoutHolder.getMaxLoadoutAmount(); i++) {
            preparedStatements.addAll(deleteLoadoutDisplay(connection, loadoutHolder.getUUID(), i));
            preparedStatements.addAll(saveLoadoutDisplay(connection, loadoutHolder.getUUID(), loadoutHolder.getLoadout(i)));
        }
        return preparedStatements;
    }

    /**
     * Gets a {@link List} of {@link PreparedStatement}s to be run in order to save the provided {@link Loadout}'s {@link LoadoutDisplay}.
     *
     * @param connection        The connection to save on.
     * @param loadoutHolderUUID The {@link UUID} of the holder of the loadout.
     * @param loadout           The {@link Loadout} to save.
     * @return A {@link List} of {@link PreparedStatement}s to be run in order to save the provided {@link Loadout}'s {@link LoadoutDisplay}.
     */
    @NotNull
    public static List<PreparedStatement> saveLoadoutDisplay(@NotNull Connection connection, @NotNull UUID loadoutHolderUUID, @NotNull Loadout loadout) {
        return loadout.shouldSaveDisplay() ? saveLoadoutDisplay(connection, loadoutHolderUUID, loadout.getLoadoutSlot(), loadout.getDisplay()) : List.of();
    }

    /**
     * Gets a {@link List} of {@link PreparedStatement}s to be run in order to save the provided {@link LoadoutDisplay}.
     *
     * @param connection        The connection to save on.
     * @param loadoutHolderUUID The {@link UUID} of the holder of the loadout.
     * @param loadoutSlot       The slot of the {@link Loadout} for the holder.
     * @param loadoutDisplay    The {@link LoadoutDisplay} to be displayed.
     * @return A {@link List} of {@link PreparedStatement}s to be run in order to save the provided {@link LoadoutDisplay}.
     */
    @NotNull
    public static List<PreparedStatement> saveLoadoutDisplay(@NotNull Connection connection, @NotNull UUID loadoutHolderUUID, int loadoutSlot, @NotNull LoadoutDisplay loadoutDisplay) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (holder_uuid, loadout_id, display_item, display_name) VALUES (?, ?, ?, ?)");
            preparedStatement.setString(1, loadoutHolderUUID.toString());
            preparedStatement.setInt(2, loadoutSlot);
            preparedStatement.setString(3, loadoutDisplay.getDisplayItem().customItem().orElse(loadoutDisplay.getDisplayItem().material().get().toString()));
            preparedStatement.setString(4, loadoutDisplay.getDisplayName().isPresent() ? loadoutDisplay.getDisplayName().get() : null);
            statements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Gets a {@link List} of {@link PreparedStatement}s to be run in order to delete the {@link LoadoutDisplay}
     * belonging to the provided {@link UUID} and {@link Loadout} slot.
     *
     * @param connection        The connection to delete on.
     * @param loadoutHolderUUID The {@link UUID} of the holder of the loadout.
     * @param loadoutSlot       The slot of the {@link Loadout} for the holder.
     * @return A {@link List} of {@link PreparedStatement}s to be run in order to delete the {@link LoadoutDisplay}
     * belonging to the provided {@link UUID} and {@link Loadout} slot.
     */
    @NotNull
    public static List<PreparedStatement> deleteLoadoutDisplay(@NotNull Connection connection, @NotNull UUID loadoutHolderUUID, int loadoutSlot) {
        List<PreparedStatement> statements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE holder_uuid = ? AND loadout_id = ?");
            preparedStatement.setString(1, loadoutHolderUUID.toString());
            preparedStatement.setInt(2, loadoutSlot);
            statements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statements;
    }

    /**
     * Gets the {@link LoadoutDisplay} belonging to the provided {@link UUID}.
     *
     * @param connection        The {@link Connection} to read from.
     * @param loadoutHolderUUID The {@link UUID} of the holder of the loadout.
     * @param loadoutSlot       The slot of the {@link Loadout} for the holder.
     * @return An {@link Optional} containing the {@link LoadoutDisplay} belonging to the provided {@link UUID}
     * and {@link Loadout} slot. If there is not a saved display, the optional will be empty.
     */
    @NotNull
    public static Optional<LoadoutDisplay> getLoadoutDisplay(@NotNull Connection connection, @NotNull UUID loadoutHolderUUID, int loadoutSlot) {
        Optional<LoadoutDisplay> loadoutDisplayOptional = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT display_item, display_name FROM " + TABLE_NAME + " WHERE holder_uuid = ? AND loadout_id = ?");) {
            preparedStatement.setString(1, loadoutHolderUUID.toString());
            preparedStatement.setInt(2, loadoutSlot);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String displayItem = resultSet.getString("display_item");
                String displayName = resultSet.getString("display_name");
                loadoutDisplayOptional = Optional.of(new LoadoutDisplay(new CustomItemWrapper(displayItem), displayName));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loadoutDisplayOptional;
    }
}
