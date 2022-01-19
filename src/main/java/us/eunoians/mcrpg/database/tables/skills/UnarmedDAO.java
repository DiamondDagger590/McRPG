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
 * A DAO used to store data regarding a player's {@link us.eunoians.mcrpg.skills.Unarmed} skill
 *
 * @author DiamondDagger590
 */
public class UnarmedDAO extends SkillDAO {

    private static final String TABLE_NAME = "mcrpg_unarmed_data";
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
             ** Contains player data for the unarmed skill
             *
             *
             * id is the id of the entry which auto increments but doesn't really serve a large purpose since it isn't
             * guaranteed to be the same for players across the board
             * uuid is the {@link java.util.UUID} of the player being stored
             * current_exp is the amount of exp a player currently has in this skill
             * current_level is the level a player currently has in this skill
             * is_sticky_fingers_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.unarmed.StickyFingers} ability toggled
             * is_tighter_grip_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.unarmed.TighterGrip} ability toggled
             * is_disarm_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.unarmed.Disarm} ability toggled
             * is_iron_arm_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.unarmed.IronArm} ability toggled
             * is_berserk_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.unarmed.Berserk} ability toggled
             * is_smiting_fist_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.unarmed.SmitingFist} ability toggled
             * is_dense_impact_toggled represents if the player ahs the {@link us.eunoians.mcrpg.abilities.unarmed.DenseImpact} ability toggled
             * tighter_grip_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.unarmed.TighterGrip} ability
             * disarm_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.unarmed.Disarm} ability
             * iron_arm_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.unarmed.IronArm} ability
             * berserk_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.unarmed.Berserk} ability
             * smiting_fist_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.unarmed.SmitingFist} ability
             * dense_impact_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.unarmed.DenseImpact} ability
             * is_tighter_grip_pending represents if the player has {@link us.eunoians.mcrpg.abilities.unarmed.TighterGrip} pending to be accepted
             * is_disarm_pending represents if the player has {@link us.eunoians.mcrpg.abilities.unarmed.Disarm} pending to be accepted
             * is_iron_arm_pending represents if the player has {@link us.eunoians.mcrpg.abilities.unarmed.IronArm} pending to be accepted
             * is_berserk_pending represents if the player has {@link us.eunoians.mcrpg.abilities.unarmed.Berserk} pending to be accepted
             * is_smiting_fist_pending represents if the player has {@link us.eunoians.mcrpg.abilities.unarmed.SmitingFist} pending to be accepted
             * is_dense_impact_pending represents if the player has {@link us.eunoians.mcrpg.abilities.unarmed.DenseImpact} pending to be accepted
             * berserk_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.unarmed.Berserk} ability
             * smiting_fist_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.unarmed.SmitingFist} ability
             * dense_impact_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.unarmed.DenseImpact} ability
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
                                                                           "`is_sticky_fingers_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_tighter_grip_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_disarm_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_iron_arm_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_berserk_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_smiting_fist_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_dense_impact_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`tighter_grip_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`disarm_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`iron_arm_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`berserk_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`smiting_fist_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`dense_impact_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_tighter_grip_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_disarm_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_iron_arm_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_berserk_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_smiting_fist_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_dense_impact_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`berserk_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`smiting_fist_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`dense_impact_cooldown` int(11) NOT NULL DEFAULT 0," +
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
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for {@link us.eunoians.mcrpg.skills.Unarmed}. If
     * the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player whose data is being obtained
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's {@link us.eunoians.mcrpg.skills.Unarmed} skill
     * data. If the provided {@link UUID} doesn't have any data, an empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    public static CompletableFuture<SkillDataSnapshot> getPlayerUnarmedData(Connection connection, UUID uuid) {
        return getSkillData(TABLE_NAME, connection, uuid, Skills.UNARMED);
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerUnarmedData(Connection connection, McRPGPlayer mcRPGPlayer) {

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
