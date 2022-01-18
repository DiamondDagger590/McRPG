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
 * A DAO used to store data regarding a player's {@link us.eunoians.mcrpg.skills.Excavation} skill
 *
 * @author DiamondDagger590
 */
public class ExcavationDAO extends SkillDAO {

    private static final String TABLE_NAME = "mcrpg_excavation_data";
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
             * is_extraction_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.excavation.Extraction} ability toggled
             * is_buried_treasure_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.excavation.BuriedTreasure} ability toggled
             * is_larger_spade_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.excavation.LargerSpade} ability toggled
             * is_mana_deposit_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.excavation.ManaDeposit} ability toggled
             * is_hand_digging_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.excavation.HandDigging} ability toggled
             * is_frenzy_dig_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.excavation.FrenzyDig} ability toggled
             * is_pans_shrine_toggled represents if the player ahs the {@link us.eunoians.mcrpg.abilities.excavation.PansShrine} ability toggled
             * buried_treasure_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.excavation.BuriedTreasure} ability
             * larger_spade_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.excavation.LargerSpade} ability
             * mana_deposit_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.excavation.ManaDeposit} ability
             * hand_digging_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.excavation.HandDigging} ability
             * frenzy_dig_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.excavation.FrenzyDig} ability
             * pans_shrine_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.excavation.PansShrine} ability
             * is_buried_treasure_pending represents if the player has {@link us.eunoians.mcrpg.abilities.excavation.BuriedTreasure} pending to be accepted
             * is_larger_spade_pending represents if the player has {@link us.eunoians.mcrpg.abilities.excavation.LargerSpade} pending to be accepted
             * is_mana_deposit_pending represents if the player has {@link us.eunoians.mcrpg.abilities.excavation.ManaDeposit} pending to be accepted
             * is_hand_digging_pending represents if the player has {@link us.eunoians.mcrpg.abilities.excavation.HandDigging} pending to be accepted
             * is_frenzy_dig_pending represents if the player has {@link us.eunoians.mcrpg.abilities.excavation.FrenzyDig} pending to be accepted
             * is_pans_shrine_pending represents if the player has {@link us.eunoians.mcrpg.abilities.excavation.PansShrine} pending to be accepted
             * hand_digging_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.excavation.HandDigging} ability
             * frenzy_dig_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.excavation.FrenzyDig} ability
             * pans_shrine_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.excavation.PansShrine} ability
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
                                                                           "`is_extraction_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_buried_treasure_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_larger_spade_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_mana_deposit_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_hand_digging_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_frenzy_dig_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_pans_shrine_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`buried_treasure_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`larger_spade_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`mana_deposit_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`hand_digging_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`frenzy_dig_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`pans_shrine_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_buried_treasure_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_larger_spade_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_mana_deposit_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_hand_digging_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_frenzy_dig_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_pans_shrine_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`hand_digging_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`frenzy_dig_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`pans_shrine_cooldown` int(11) NOT NULL DEFAULT 0," +
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
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for {@link us.eunoians.mcrpg.skills.Excavation}. If
     * the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's {@link us.eunoians.mcrpg.skills.Excavation} skill
     * data. If the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    public static CompletableFuture<SkillDataSnapshot> getPlayerExcavationData(Connection connection, UUID uuid) {
        return getSkillData(TABLE_NAME, connection, uuid, Skills.EXCAVATION);
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerExcavationData(Connection connection, McRPGPlayer mcRPGPlayer) {

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
