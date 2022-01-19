package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * A DAO used to store a player's loadout
 *
 * @author DiamondDagger590
 */
public class PlayerLoadoutDAO {

    private static final String LEGACY_LOADOUT_TABLE_NAME = "mcrpg_loadout";
    private static final String LOADOUT_TABLE_NAME = "mcrpg_loadout_info";
    private static final String LOADOUT_SLOTS_TABLE_NAME = "mcrpg_loadout_slots";
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

            boolean loadoutTableExists = databaseManager.getDatabase().tableExists(LOADOUT_TABLE_NAME);
            boolean loadoutSlotsTableExists = databaseManager.getDatabase().tableExists(LOADOUT_SLOTS_TABLE_NAME);

            //Check to see if the table already exists
            if (loadoutTableExists && loadoutSlotsTableExists) {
                completableFuture.complete(false);
                return;
            }

            isAcceptingQueries = false;

            if (!loadoutTableExists) {
                /*****
                 ** Table Description:
                 ** Contains player loadout information
                 *
                 *
                 * loadout_id is the {@link UUID} of the loadout which can be used to lookup specific information about that loadout's contents
                 * player_uuid is the {@link UUID} of the player being stored
                 * player_loadout_id is the id of the loadout. Players down the line might be able to have multiple loadouts, so this is an integer representing what loadout this is for them to make lookups easier
                 *
                 **
                 ** Reasoning for structure:
                 ** PK is the `loadout_id` field, as each loadout id can only exist once
                 *****/
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + LOADOUT_TABLE_NAME + "`" +
                                                                               "(" +
                                                                               "`loadout_id` varchar(36) NOT NULL," +
                                                                               "`player_uuid` varchar(36) NOT NULL," +
                                                                               "`player_loadout_id` int(11) NOT NULL DEFAULT 1," +
                                                                               "PRIMARY KEY (`loadout_id`)" +
                                                                               ");")) {
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.completeExceptionally(e);
                }
            }

            if (!loadoutSlotsTableExists) {

                /*****
                 ** Table Description:
                 ** Contains player loadout slots
                 *
                 *
                 * loadout_id is the {@link java.util.UUID} of the loadout the data belongs to
                 * slot_number is an integer representing the slot in the loadout that the ability is stored in
                 * ability_id is the ability id that is used to find the corresponding {@link UnlockedAbilities} value
                 **
                 ** Reasoning for structure:
                 ** PK is the composite of `loadout_id` field and `slot_number`, as a loadout id will be present once for each ability in the loadout, so the combination of that and the slot number will be used to look up individual abilities
                 *****/
                //TODO update javadoc
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + LOADOUT_SLOTS_TABLE_NAME + "`" +
                                                                               "(" +
                                                                               "`loadout_id` varchar(36) NOT NULL," +
                                                                               "`slot_number` int(11) NOT NULL," +
                                                                               "`ability_id` varchar(32) NOT NULL," +
                                                                               "PRIMARY KEY (`loadout_id`, `slot_number`)" +
                                                                               ");")) {
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                    completableFuture.completeExceptionally(e);
                }
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

        boolean updateFromLegacy = databaseManager.getDatabase().tableExists(LEGACY_LOADOUT_TABLE_NAME);

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (TableVersionHistoryDAO.isAcceptingQueries()) {

                TableVersionHistoryDAO.getLatestVersion(connection, LOADOUT_TABLE_NAME)
                        //Update the mcrpg_loadout_info first
                        .thenAccept(lastStoredVersion -> {

                            if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                                completableFuture.complete(null);
                                return;
                            }

                            isAcceptingQueries = false;

                            //Adds table to our tracking
                            if (lastStoredVersion == 0) {

                                if (updateFromLegacy) {

                                    try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOADOUT_TABLE_NAME + " SELECT RANDOM_UUID() AS loadout_id, uuid AS player_uuid, 1 AS player_loadout_id  FROM " + LEGACY_LOADOUT_TABLE_NAME)) {
                                        preparedStatement.executeUpdate();
                                    }
                                    catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }

                                TableVersionHistoryDAO.setTableVersion(connection, LOADOUT_TABLE_NAME, 1);
                                lastStoredVersion = 1;
                            }

                            isAcceptingQueries = true;

                        })
                        //Update the mcrpg_loadout_slots table after
                        .thenAccept(unused -> {
                            TableVersionHistoryDAO.getLatestVersion(connection, LOADOUT_SLOTS_TABLE_NAME).thenAccept(lastStoredVersion -> {

                                if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                                    completableFuture.complete(null);
                                    return;
                                }

                                isAcceptingQueries = false;

                                //Adds table to our tracking
                                if (lastStoredVersion == 0) {

                                    if (updateFromLegacy) {

                                        try {

                                            //Get the amount of slots in the legacy table
                                            int slotAmount = 0;
                                            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = N'" + LEGACY_LOADOUT_TABLE_NAME.toUpperCase(Locale.ROOT) + "' ")) {

                                                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                                                    //Check the last column to get the highest amount of slots
                                                    resultSet.last();

                                                    //Get the slot amount
                                                    slotAmount = Integer.parseInt(resultSet.getString("column_name").toLowerCase(Locale.ROOT).replace("slot", ""));
                                                }
                                            }

                                            //Go through all legacy slot columns and convert them to new
                                            for (int i = 1; i <= slotAmount; i++) {

                                                try (PreparedStatement preparedStatement = connection.prepareStatement("MERGE INTO " + LOADOUT_SLOTS_TABLE_NAME + " SELECT loadout.loadout_id AS loadout_id, " + i + " AS slot_number, legacy.slot" + i + " AS ability_id FROM " + LOADOUT_TABLE_NAME + " AS loadout JOIN " + LEGACY_LOADOUT_TABLE_NAME + " AS legacy ON legacy.uuid = loadout.player_uuid WHERE legacy.slot" + i + " IS NOT NULL AND legacy.slot" + i + " <> 'null';")) {
                                                    preparedStatement.executeUpdate();
                                                }
                                            }
                                        }
                                        catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    TableVersionHistoryDAO.setTableVersion(connection, LOADOUT_SLOTS_TABLE_NAME, 1);
                                    lastStoredVersion = 1;
                                }

                                isAcceptingQueries = true;

                                completableFuture.complete(null);
                            });
                        });
            }
        });

        return completableFuture;
    }

    /**
     * Gets a {@link List} of {@link UnlockedAbilities} that the provided player {@link UUID} has equipped.
     * <p>
     * This list is ordered by the slots that the player put them in, so this will persist order for player experience.
     *
     * @param connection The {@link Connection} that will be running the query
     * @param playerUUID The {@link UUID} of the player to get the loadout for
     * @return A {@link CompletableFuture} that has a {@link List} of {@link UnlockedAbilities} that the
     * provided player {@link UUID} has equipped.
     */
    @NotNull
    public static CompletableFuture<List<UnlockedAbilities>> getPlayerLoadout(@NotNull Connection connection, @NotNull UUID playerUUID) { //TODO make this return multiple or make an overloaded method to take in an int for a specific loadout number

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<List<UnlockedAbilities>> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //We are getting the first loadout since we only support single loadouts right now
            getPlayerLoadoutUUID(connection, playerUUID).thenAccept(optional -> {

                //If loadout exists, return it. Otherwise return an empty list
                if (optional.isPresent()) {
                    UUID loadoutUUID = optional.get();
                    getLoadoutByUUID(connection, loadoutUUID).thenAccept(completableFuture::complete);
                }
                else {
                    initializeNewPlayerLoadout(connection, playerUUID).thenAccept(unused -> completableFuture.complete(new ArrayList<>())).exceptionally(throwable -> {
                        McRPG.getInstance().getLogger().log(Level.WARNING, "Player with UUID of " + playerUUID + " attempted to generate a new loadout but failed. Please report the following error to the McRPG developer.");
                        throwable.printStackTrace();
                        completableFuture.complete(new ArrayList<>());
                        return null;
                    }); //Create a new loadout for the player for the next time we query. Only complete once it's done so that way we can't make multiple calls and have weird edge cases
                }
            });
        });

        return completableFuture;
    }

    /**
     * Gets a {@link List} of {@link UnlockedAbilities} that the provided loadout {@link UUID} has stored in it.
     * <p>
     * Keep in mind a player must have a loadout but a loadout doesn't always have a player. This method is private as
     * we don't really want to expose the internals of how the loadout system works. So a developer should access {@link #getPlayerLoadout(Connection, UUID)}
     * to get the loadout of a player. That method will use {@link #getPlayerLoadoutUUID(Connection, UUID)} to get the {@link UUID} for the loadout belonging
     * to that player, then {@link #getLoadoutByUUID(Connection, UUID)} will be called using that loadout {@link UUID} in one seamless call for the developer.
     *
     * @param connection  The {@link Connection} that will be running the query
     * @param loadoutUUID The {@link UUID} of the loadout
     * @return A {@link CompletableFuture} that has a {@link List} of {@link UnlockedAbilities} that the provided loadout {@link UUID} has stored in it.
     */
    @NotNull
    private static CompletableFuture<List<UnlockedAbilities>> getLoadoutByUUID(@NotNull Connection connection, @NotNull UUID loadoutUUID) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<List<UnlockedAbilities>> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            List<UnlockedAbilities> unlockedAbilities = new ArrayList<>();

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ABILITY_ID, SLOT_NUMBER FROM " + LOADOUT_SLOTS_TABLE_NAME + " WHERE LOADOUT_ID = ? ORDER BY SLOT_NUMBER;")) {

                preparedStatement.setString(1, loadoutUUID.toString());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                        UnlockedAbilities unlockedAbility = UnlockedAbilities.fromString(resultSet.getString("ability_id"));
                        unlockedAbilities.add(unlockedAbility);
                    }
                }
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(unlockedAbilities);
        });

        return completableFuture;
    }

    /**
     * Gets the loadout {@link UUID} that belongs to the provided player {@link UUID}
     *
     * @param connection The {@link Connection} that will be running the query
     * @param uuid       The {@link UUID} of the player to get the loadout {@link UUID} of
     * @return A {@link CompletableFuture} containing an {@link Optional} which will either be empty or contain the {@link UUID}
     * of the loadout belonging to the provided player {@link UUID}
     */
    @NotNull
    private static CompletableFuture<Optional<UUID>> getPlayerLoadoutUUID(@NotNull Connection connection, @NotNull UUID uuid) { //TODO make this return multiple or make an overloaded method to take in an int for a specific loadout number

        int loadoutNumber = 1;//Same as above TODO
        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Optional<UUID>> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            Optional<UUID> loadoutUUIDOptional = Optional.empty();

            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + LOADOUT_TABLE_NAME + " WHERE player_uuid = ? AND player_loadout_id = ?;")) {

                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setInt(2, loadoutNumber);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {

                    while (resultSet.next()) {

                        UUID loadoutUUID = UUID.fromString(resultSet.getString("loadout_id"));
                        loadoutUUIDOptional = Optional.of(loadoutUUID);
                    }

                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(loadoutUUIDOptional);

        });

        return completableFuture;
    }

    /**
     * Creates a new loadout for the player. This assumes that a loadout doesn't exist when called and as such, will do no handling of that edge case.
     * <p>
     * This updates the {@link #LOADOUT_TABLE_NAME} with the loadout information but doesn't populate {@link #LOADOUT_SLOTS_TABLE_NAME}.
     *
     * @param connection The {@link Connection} that is used to execute the update
     * @param uuid       The player {@link UUID} to generate a new loadout for
     * @return A {@link CompletableFuture} that returns the new {@link UUID} of a created loadout whenever the table has been updated, or completes with an
     * exception provided something goes wrong with the update.
     */
    @NotNull
    private static CompletableFuture<UUID> initializeNewPlayerLoadout(@NotNull Connection connection, @NotNull UUID uuid) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<UUID> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            UUID loadoutUUID = UUID.randomUUID();
            int loadoutNumber = 1;

            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOADOUT_TABLE_NAME + " (LOADOUT_ID, PLAYER_UUID, PLAYER_LOADOUT_ID) VALUES (?, ?, ?);")) {

                preparedStatement.setString(1, loadoutUUID.toString());
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.setInt(3, loadoutNumber);

                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(loadoutUUID);
        });
        return completableFuture;
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerLoadout(@NotNull Connection connection, @NotNull McRPGPlayer mcRPGPlayer) {
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
