package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This DAO is responsible for storing the active loadout ID for a player.
 */
public class PlayerLoadoutSelectionDAO {

    static final String TABLE_NAME = "mcrpg_player_loadout_selection";
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
         ** Contains player loadout slots
         *
         * holder_uuid is the {@link UUID} of the {@link us.eunoians.mcrpg.entity.holder.LoadoutHolder} being stored
         * active_loadout_id is the id of the loadout that the holder is currently using
         **
         ** Reasoning for structure:
         ** PK is the `holder_uuid` as each holder can only have one active loadout.
         *
         * The foreign key requires the player's uuid to be present in the loadout table as that's where the player's loadout info is stored
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`holder_uuid` varchar(36) NOT NULL," +
                "`active_loadout_id` int(11) NOT NULL DEFAULT 1," +
                "PRIMARY KEY (`holder_uuid`), " +
                // Ensure that the loadout is stored in the info table, also if it ever gets removed from that table, ensure it's deleted here
                "CONSTRAINT FK_loadout FOREIGN KEY (`holder_uuid`, `active_loadout_id`) REFERENCES " + LoadoutInfoDAO.TABLE_NAME + " (`holder_uuid`, `loadout_id`) ON DELETE CASCADE" +
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
                TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                lastStoredVersion = 1;
            }
        }
    }

    /**
     * Updates the active loadout ID for a specific holder in the database.
     *
     * @param connection The SQL database connection to be used for executing the query. Must not be null.
     * @param holderUUID The UUID of the holder whose active loadout is being updated. Must not be null.
     * @param loadoutID  The ID of the loadout to set as active.
     * @return A list of {@link PreparedStatement} objects representing the SQL statement(s) created.
     */
    @NotNull
    public static List<PreparedStatement> setActiveLoadout(@NotNull Connection connection, @NotNull UUID holderUUID, int loadoutID) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (holder_uuid, active_loadout_id) " +
                    "VALUES (?, ?) ON CONFLICT (holder_uuid) DO UPDATE SET active_loadout_id = ?");
            preparedStatement.setString(1, holderUUID.toString());
            preparedStatement.setInt(2, loadoutID);
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    /**
     * Retrieves the active loadout ID for the specified holder from the database.
     *
     * @param connection The SQL database connection to be used for executing the query. Must not be null.
     * @param holderUUID The UUID of the holder whose active loadout ID is being retrieved. Must not be null.
     * @return The active loadout ID associated with the specified holder.
     */
    public static int getActiveLoadout(@NotNull Connection connection, @NotNull UUID holderUUID) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT `active_loadout_id` FROM `" + TABLE_NAME + "` WHERE `holder_uuid` = ?")) {
            preparedStatement.setString(1, holderUUID.toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return Math.max(1, resultSet.getInt("active_loadout_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
