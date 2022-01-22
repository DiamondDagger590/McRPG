package us.eunoians.mcrpg.database.tables.skills;

import org.jetbrains.annotations.NotNull;
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
 * A DAO used to store data regarding a player's {@link us.eunoians.mcrpg.skills.Herbalism} skill
 *
 * @deprecated Instead working on implementing {@link us.eunoians.mcrpg.database.tables.SkillDAO} as a general solution
 * @author DiamondDagger590
 */
@Deprecated
public class HerbalismDAO extends SkillDAO {

    private static final String TABLE_NAME = "mcrpg_herbalism_data";
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
    @NotNull
    public static CompletableFuture<Boolean> attemptCreateTable(@NotNull Connection connection, @NotNull DatabaseManager databaseManager) {

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
             ** Contains player data for the herbalism skill
             *
             *
             * uuid is the {@link java.util.UUID} of the player being stored
             * current_exp is the amount of exp a player currently has in this skill
             * current_level is the level a player currently has in this skill
             * is_too_many_plants_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.herbalism.TooManyPlants} ability toggled
             * is_farmers_diet_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.herbalism.FarmersDiet} ability toggled
             * is_diamond_flowers_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.herbalism.DiamondFlowers} ability toggled
             * is_replanting_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.herbalism.Replanting} ability toggled
             * is_mass_harvest_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.herbalism.MassHarvest} ability toggled
             * is_natures_wrath_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.herbalism.NaturesWrath} ability toggled
             * is_pans_blessing_toggled represents if the player ahs the {@link us.eunoians.mcrpg.abilities.herbalism.PansBlessing} ability toggled
             * farmers_diet_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.herbalism.FarmersDiet} ability
             * diamond_flowers_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.herbalism.DiamondFlowers} ability
             * replanting_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.herbalism.Replanting} ability
             * mass_harvest_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.herbalism.MassHarvest} ability
             * natures_wrath_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.herbalism.NaturesWrath} ability
             * pans_blessing_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.herbalism.PansBlessing} ability
             * is_farmers_diet_pending represents if the player has {@link us.eunoians.mcrpg.abilities.herbalism.FarmersDiet} pending to be accepted
             * is_diamond_flowers_pending represents if the player has {@link us.eunoians.mcrpg.abilities.herbalism.DiamondFlowers} pending to be accepted
             * is_replanting_pending represents if the player has {@link us.eunoians.mcrpg.abilities.herbalism.Replanting} pending to be accepted
             * is_mass_harvest_pending represents if the player has {@link us.eunoians.mcrpg.abilities.herbalism.MassHarvest} pending to be accepted
             * is_natures_wrath_pending represents if the player has {@link us.eunoians.mcrpg.abilities.herbalism.NaturesWrath} pending to be accepted
             * is_pans_blessing_pending represents if the player has {@link us.eunoians.mcrpg.abilities.herbalism.PansBlessing} pending to be accepted
             * mass_harvest_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.herbalism.MassHarvest} ability
             * natures_wrath_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.herbalism.NaturesWrath} ability
             * pans_blessing_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.herbalism.PansBlessing} ability
             **
             ** Reasoning for structure:
             ** PK is the `uuid` field, as each player only has one uuid
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                                                                           "(" +
                                                                           "`uuid` varchar(36) NOT NULL," +
                                                                           "`current_exp` int(11) NOT NULL DEFAULT 0," +
                                                                           "`current_level` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_too_many_plants_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_farmers_diet_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_diamond_flowers_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_replanting_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_mass_harvest_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_natures_wrath_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_pans_blessing_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`farmers_diet_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`diamond_flowers_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`replanting_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`mass_harvest_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`natures_wrath_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`pans_blessing_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_farmers_diet_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_diamond_flowers_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_replanting_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_mass_harvest_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_natures_wrath_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_pans_blessing_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`mass_harvest_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`natures_wrath_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`pans_blessing_cooldown` int(11) NOT NULL DEFAULT 0," +
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
    @NotNull
    public static CompletableFuture<Void> updateTable(@NotNull Connection connection) {

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
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for {@link us.eunoians.mcrpg.skills.Herbalism}. If
     * the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's {@link us.eunoians.mcrpg.skills.Herbalism} skill
     * data. If the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    @NotNull
    public static CompletableFuture<SkillDataSnapshot> getPlayerHerbalismData(@NotNull Connection connection, @NotNull UUID uuid) {
        return getSkillData(TABLE_NAME, connection, uuid, Skills.HERBALISM);
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerHerbalismData(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {

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
