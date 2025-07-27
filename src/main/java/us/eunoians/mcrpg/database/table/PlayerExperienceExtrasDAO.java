package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This DAO is responsible for saving {@link PlayerExperienceExtras}.
 */
public class PlayerExperienceExtrasDAO {

    static final String TABLE_NAME = "mcrpg_player_experience_extras";
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
         ** Contains data about a player's experience that isn't tied to something like a skill's experience.
         **
         ** Reasoning for structure:
         ** PK is the `uuid` field, as each player only has one uuid
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`uuid` varchar(36) NOT NULL," +
                "`redeemable_experience` int(11) NOT NULL DEFAULT 0, " +
                "`redeemable_levels` int(11) NOT NULL DEFAULT 0, " +
                "`boosted_experience` int(11) NOT NULL DEFAULT 0, " +
                "`rested_experience` float(11) NOT NULL DEFAULT 0, " +
                "PRIMARY KEY (`uuid`)" +
                ");")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    /**
     * Gets the {@link PlayerExperienceExtras} for a given player.
     *
     * @param connection The {@link Connection} to get data from.
     * @param uuid       The {@link UUID} of the player to get data for.
     * @return The {@link PlayerExperienceExtras} for the given player.
     */
    @NotNull
    public static PlayerExperienceExtras getPlayerExperienceExtras(@NotNull Connection connection, @NotNull UUID uuid) {
        PlayerExperienceExtras playerExperienceExtras = new PlayerExperienceExtras();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT redeemable_experience, redeemable_levels, boosted_experience FROM " + TABLE_NAME + " WHERE uuid = ?")) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    playerExperienceExtras.setRedeemableExperience(resultSet.getInt("redeemable_experience"));
                    playerExperienceExtras.setRedeemableLevels(resultSet.getInt("redeemable_levels"));
                    playerExperienceExtras.setBoostedExperience(resultSet.getInt("boosted_experience"));
                    playerExperienceExtras.setRestedExperience(resultSet.getFloat("rested_experience"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerExperienceExtras;
    }

    /**
     * Saves a player's {@link PlayerExperienceExtras}.
     *
     * @param connection             The {@link Connection} to save on.
     * @param uuid                   The player to save data for.
     * @param playerExperienceExtras The {@link PlayerExperienceExtras} to save.
     * @return A {@link List} of {@link PreparedStatement}s to be run to save the player's {@link PlayerExperienceExtras}.
     */
    @NotNull
    public static List<PreparedStatement> savePlayerExperienceExtras(@NotNull Connection connection, @NotNull UUID uuid, @NotNull PlayerExperienceExtras playerExperienceExtras) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, redeemable_experience, redeemable_levels, boosted_experience, rested_experience) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setInt(2, playerExperienceExtras.getRedeemableExperience());
            preparedStatement.setInt(3, playerExperienceExtras.getRedeemableLevels());
            preparedStatement.setInt(4, playerExperienceExtras.getBoostedExperience());
            preparedStatement.setFloat(5, playerExperienceExtras.getRestedExperience());
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }
}
