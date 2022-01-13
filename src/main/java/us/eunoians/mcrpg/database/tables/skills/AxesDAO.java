package us.eunoians.mcrpg.database.tables.skills;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.database.tables.TableVersionHistoryDAO;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.Skills;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to store data regarding a player's {@link us.eunoians.mcrpg.skills.Axes} skill
 *
 * @author DiamondDagger590
 */
public class AxesDAO extends SkillDAO {

    private static final String TABLE_NAME = "mcrpg_axes_data";
    private static final int CURRENT_TABLE_VERSION = 1;

    private static boolean isAcceptingQueries = true;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param databaseManager The {@link DatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    public static CompletableFuture<Boolean> attemptCreateTable(Connection connection, DatabaseManager databaseManager) {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //Check to see if the table already exists
            if (databaseManager.getDatabase().tableExists(TABLE_NAME)) {
                completableFuture.complete(false);
                return;
            }

            isAcceptingQueries = false;

            /*****
             ** Table Description:
             ** Contains player data for the archery skill
             *
             *
             * id is the id of the entry which auto increments but doesn't really serve a large purpose since it isn't
             * guaranteed to be the same for players across the board
             * uuid is the {@link java.util.UUID} of the player being stored
             * current_exp is the amount of exp a player currently has in this skill
             * current_level is the level a player currently has in this skill
             * is_shred_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.axes.Shred} ability toggled
             * is_heavy_strike_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.axes.HeavyStrike} ability toggled
             * is_blood_frenzy_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.axes.BloodFrenzy} ability toggled
             * is_sharper_axe_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.axes.SharperAxe} ability toggled
             * is_whirlwind_strike_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.axes.WhirlwindStrike} ability toggled
             * is_ares_blessing_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.axes.AresBlessing} ability toggled
             * is_crippling_blow_toggled represents if the player ahs the {@link us.eunoians.mcrpg.abilities.axes.CripplingBlow} ability toggled
             * heavy_strike_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.axes.HeavyStrike} ability
             * blood_frenzy_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.axes.BloodFrenzy} ability
             * sharper_axe_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.axes.SharperAxe} ability
             * whirlwind_strike_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.axes.WhirlwindStrike} ability
             * ares_blessing_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.axes.AresBlessing} ability
             * crippling_blow_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.axes.CripplingBlow} ability
             * is_heavy_strike_pending represents if the player has {@link us.eunoians.mcrpg.abilities.axes.HeavyStrike} pending to be accepted
             * is_blood_frenzy_pending represents if the player has {@link us.eunoians.mcrpg.abilities.axes.BloodFrenzy} pending to be accepted
             * is_sharper_axe_pending represents if the player has {@link us.eunoians.mcrpg.abilities.axes.SharperAxe} pending to be accepted
             * is_whirlwind_strike_pending represents if the player has {@link us.eunoians.mcrpg.abilities.axes.WhirlwindStrike} pending to be accepted
             * is_ares_blessing_pending represents if the player has {@link us.eunoians.mcrpg.abilities.axes.AresBlessing} pending to be accepted
             * is_crippling_blow_pending represents if the player has {@link us.eunoians.mcrpg.abilities.axes.CripplingBlow} pending to be accepted
             * whirlwind_strike_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.axes.WhirlwindStrike} ability
             * ares_blessing_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.axes.AresBlessing} ability
             * crippling_blow_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.axes.CripplingBlow} ability
             **
             ** Reasoning for structure:
             ** PK is the `uuid` field, as each player only has one uuid
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                                                                           "(" +
                                                                           "`id` int(11) NOT NULL AUTO_INCREMENT," +
                                                                           "`uuid` varchar(32) NOT NULL," +
                                                                           "`current_exp` int(11) NOT NULL DEFAULT 0," +
                                                                           "`current_level` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_shred_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_heavy_strike_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_blood_frenzy_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_sharper_axe_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_whirlwind_strike_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_ares_blessing_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_crippling_blow_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`heavy_strike_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`blood_frenzy_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`sharper_axe_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`whirlwind_strike_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`ares_blessing_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`crippling_blow_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_heavy_strike_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_blood_frenzy_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_sharper_axe_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_whirlwind_strike_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_ares_blessing_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_crippling_blow_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`whirlwind_strike_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`ares_blessing_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`crippling_blow_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "PRIMARY KEY (`uuid`)" +
                                                                           ");")) {
                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            isAcceptingQueries = true;

            completableFuture.complete(true);
        });

        return completableFuture;
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     * @return The {@link  CompletableFuture} that is running these changes.
     */
    public static CompletableFuture<Void> updateTable(Connection connection) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (TableVersionHistoryDAO.isAcceptingQueries()) {

                TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME).thenAccept(lastStoredVersion -> {

                    if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                        completableFuture.complete(null);
                        return;
                    }

                    isAcceptingQueries = false;

                    //Adds table to our tracking
                    if (lastStoredVersion == 0) {
                        TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
                        lastStoredVersion = 1;
                    }

                    isAcceptingQueries = true;
                });

            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for {@link us.eunoians.mcrpg.skills.Axes}. If
     * the provided {@link UUID} doesn't have any data, any empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player who's data is being obtained
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's {@link us.eunoians.mcrpg.skills.Axes} skill
     * data. If the provided {@link UUID} doesn't have any data, any empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    public static CompletableFuture<SkillDataSnapshot> getPlayerAxesData(Connection connection, UUID uuid) {
        return getSkillData(TABLE_NAME, connection, uuid, Skills.AXES);
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerAxesData(Connection connection, McRPGPlayer mcRPGPlayer) {

    }

    /**
     * Checks to see if this table is accepting queries at the moment. A reason it could be false is either the table is
     * in creation or the table is being updated and for some reason a query is attempting to be run.
     *
     * @return {@code true} if this table is accepting queries
     */
    public static boolean isAcceptingQueries() {
        return isAcceptingQueries;
    }
}
