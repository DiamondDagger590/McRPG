package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A DAO used to store a player's loadout information such as the loadout id and uuid
 */
public class LoadoutInfoDAO {

    static final String TABLE_NAME = "mcrpg_loadout_info";
    private static final int CURRENT_TABLE_VERSION = 1;

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
         ** Contains player loadout information
         *
         *
         * holder_uuid is the {@link UUID} of the {@link LoadoutHolder} being stored
         * loadout_id is the id of the loadout. Players down the line might be able to have multiple loadouts, so this is an integer representing what loadout this is for them to make lookups easier
         *
         **
         ** Reasoning for structure:
         ** PK is the `holder_uuid`, `loadout_id` fields, as each loadout id can only exist once, and
         * each loadout + player UUID can only exist once
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`holder_uuid` varchar(36) NOT NULL," +
                "`loadout_id` int(11) NOT NULL DEFAULT 1," +
                "PRIMARY KEY (`loadout_id`, `holder_uuid`)" +
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
        if (lastStoredVersion < CURRENT_TABLE_VERSION) {
            //Adds table to our tracking
            if (lastStoredVersion == 0) {
                // Create an index to group by UUIDs
                try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE INDEX holder_uuid_index_loadout_info ON " + TABLE_NAME + " (holder_uuid)")) {
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                lastStoredVersion = 1;
            }
        }
    }

    /**
     * Saves all information about {@link Loadout}s for the given {@link LoadoutHolder}.
     *
     * @param connection    The {@link Connection} to save data on.
     * @param loadoutHolder The {@link LoadoutHolder} to save the data of.
     * @return A {@link List} of {@link PreparedStatement}s to be run for all the updates required for the provided {@link LoadoutHolder}.
     */
    @NotNull
    public static List<PreparedStatement> saveAllLoadoutInfo(@NotNull Connection connection, @NotNull LoadoutHolder loadoutHolder) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        UUID uuid = loadoutHolder.getUUID();
        int loadoutAmount = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
        for (int i = 1; i <= loadoutAmount; i++) {
            preparedStatements.addAll(saveLoadoutInfo(connection, uuid, loadoutHolder.getLoadout(i)));
        }
        return preparedStatements;
    }

    /**
     * Saves the {@link Loadout} for the given loadout holder {@link UUID}.
     *
     * @param connection        The {@link Connection} to save data to.
     * @param loadoutHolderUUID The {@link UUID} of the {@link LoadoutHolder} to save data for.
     * @param loadout           The {@link Loadout} being saved.
     * @return A {@link List} of {@link PreparedStatement}s to be run for all the updates required for the provided {@link Loadout}.
     */
    @NotNull
    public static List<PreparedStatement> saveLoadoutInfo(@NotNull Connection connection, @NotNull UUID loadoutHolderUUID, @NotNull Loadout loadout) {
        List<PreparedStatement> preparedStatements = new ArrayList<>(deleteLoadoutInfo(connection, loadoutHolderUUID, loadout.getLoadoutSlot()));
        AbilityRegistry abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        // If it's empty, don't bother saving
        if (loadout.getAbilities().isEmpty()) {
            return preparedStatements;
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (holder_uuid, loadout_id) VALUES (?, ?)");
            preparedStatement.setString(1, loadoutHolderUUID.toString());
            preparedStatement.setInt(2, loadout.getLoadoutSlot());
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    /**
     * Deletes all the information about the {@link Loadout} with the provided id belonging to the provided {@link UUID}.
     *
     * @param connection        The {@link Connection} to delete data from.
     * @param lodaoutHolderUUID The {@link UUID} of the {@link LoadoutHolder} to delete data for.
     * @param loadoutId         The id of the {@link Loadout} being deleted.
     * @return A {@link List} of {@link PreparedStatement}s to be run for all the updates required for the {@link Loadout} belonging
     * to the provided {@link UUID}.
     */
    @NotNull
    public static List<PreparedStatement> deleteLoadoutInfo(@NotNull Connection connection, @NotNull UUID lodaoutHolderUUID, int loadoutId) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE holder_uuid = ? AND loadout_id = ?");
            preparedStatement.setString(1, lodaoutHolderUUID.toString());
            preparedStatement.setInt(2, loadoutId);
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }
}
