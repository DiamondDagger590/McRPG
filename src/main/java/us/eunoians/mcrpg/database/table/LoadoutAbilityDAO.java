package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This DAO is used to store and access a {@link Loadout}'s ability contents
 */
public class LoadoutAbilityDAO {

    static final String TABLE_NAME = "mcrpg_loadout";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param database The {@link Database} being used to attempt to create the table
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
         * uuid is an integer representing the slot in the loadout that the ability is stored in
         * ability_id is the ability id that is used to find the corresponding {@link UnlockedAbilities} value
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
                "`loadout_id` int(11) NOT NULL," +
                "`ability_id` varchar(32) NOT NULL," +
                "PRIMARY KEY (`loadout_id`, `ability_id`, `holder_uuid`), " +
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

        //Adds table to our tracking
        if (lastStoredVersion == 0) {
            // Create an index to group by UUIDs
            try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE INDEX holder_uuid_index_loadout_ability ON " + TABLE_NAME + " (holder_uuid)")) {
                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            // Create an index to group by UUIDs and loadout ids
            try (PreparedStatement preparedStatement = connection.prepareStatement("CREATE INDEX holder_uuid_and_loadout_index ON " + TABLE_NAME + " (holder_uuid, loadout_id)")) {
                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    @NotNull
    public static Loadout getLoadout(@NotNull Connection connection, @NotNull UUID playerUUID, int loadoutNumber) {
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        Loadout loadout = new Loadout(playerUUID, loadoutNumber);
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ability_id FROM " + TABLE_NAME + " WHERE holder_uuid = ? AND loadout_id = ?;")) {
            preparedStatement.setString(1, playerUUID.toString());
            preparedStatement.setInt(2, loadoutNumber);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String abilityId = resultSet.getString("ability_id");
                    NamespacedKey namespacedKey = new NamespacedKey(McRPG.getInstance(), abilityId);
                    if (abilityRegistry.isAbilityRegistered(namespacedKey)) {
                        loadout.addAbility(namespacedKey);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loadout;
    }

    @NotNull
    public static List<PreparedStatement> saveAllLoadouts(@NotNull Connection connection, @NotNull LoadoutHolder loadoutHolder) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        UUID uuid = loadoutHolder.getUUID();
        int loadoutAmount = McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
        for (int i = 1; i <= loadoutAmount; i++) {
            preparedStatements.addAll(saveLoadout(connection, uuid, loadoutHolder.getLoadout(i)));
        }
        return preparedStatements;
    }

    @NotNull
    public static List<PreparedStatement> saveLoadout(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Loadout loadout) {
        List<PreparedStatement> preparedStatements = new ArrayList<>(deleteLoadout(connection, uuid, loadout.getLoadoutSlot()));
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        // If it's empty, don't bother saving
        if (loadout.getAbilities().isEmpty()) {
            return preparedStatements;
        }
        try {
            for (NamespacedKey namespacedKey : loadout.getAbilities()) {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (holder_uuid, loadout_id, ability_id) VALUES (?, ?, ?);");
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setInt(2, loadout.getLoadoutSlot());
                preparedStatement.setString(3, abilityRegistry.getRegisteredAbility(namespacedKey).getDatabaseName());
                preparedStatements.add(preparedStatement);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    @NotNull
    public static List<PreparedStatement> deleteLoadout(@NotNull Connection connection, @NotNull UUID uuid, int loadoutId) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement deleteSlotsStatement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE holder_uuid = ? AND loadout_id = ?;");
            deleteSlotsStatement.setString(1, uuid.toString());
            deleteSlotsStatement.setInt(2, loadoutId);
            preparedStatements.add(deleteSlotsStatement);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }
}
