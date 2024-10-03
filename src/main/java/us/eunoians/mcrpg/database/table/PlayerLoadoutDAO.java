package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.loadout.Loadout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * A DAO used to store a player's loadout
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
     * @param databaseManager The {@link McRPGDatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    @NotNull
    public static CompletableFuture<Boolean> attemptCreateTable(@NotNull Connection connection, @NotNull McRPGDatabaseManager databaseManager) {

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
                 * player_uuid is the {@link UUID} of the player being stored
                 * loadout_uuid is the {@link UUID} of the loadout which can be used to lookup specific information about that loadout's contents
                 * loadout_id is the id of the loadout. Players down the line might be able to have multiple loadouts, so this is an integer representing what loadout this is for them to make lookups easier
                 *
                 **
                 ** Reasoning for structure:
                 ** PK is the `player_uuid`, `loadout_id`, `loadout_uuid` fields, as each loadout id can only exist once, and
                 * each loadout + player UUID can only exist once
                 *****/
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + LOADOUT_TABLE_NAME + "`" +
                        "(" +
                        "`player_uuid` varchar(36) NOT NULL," +
                        "`loadout_uuid` varchar(36) NOT NULL," +
                        "`loadout_id` int(11) NOT NULL DEFAULT 1," +
                        "PRIMARY KEY (`loadout_id`, `player_uuid`, `loadout_uuid`)" +
                        ");")) {
                    statement.executeUpdate();
                } catch (SQLException e) {
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
                 * loadout_id is the slot of the player's loadout the data belongs to
                 * player_uuid is an integer representing the slot in the loadout that the ability is stored in
                 * ability_id is the ability id that is used to find the corresponding {@link UnlockedAbilities} value
                 **
                 ** Reasoning for structure:
                 ** PK is the composite of `loadout_id` field, `slot_number` and `player_uuid`, as a loadout id will be present once for each ability in the loadout,
                 * so the combination of that and the slot number will be used to look up individual abilities and each player uuid is unique.
                 *
                 * The foreign key requires the player's uuid to be present in the loadout table as that's where the player's loadout info is stored
                 *****/
                try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + LOADOUT_SLOTS_TABLE_NAME + "`" +
                        "(" +
                        "`loadout_id` int(11) NOT NULL," +
                        "`player_uuid` varchar(36) NOT NULL," +
                        "`ability_id` varchar(32) NOT NULL," +
                        "PRIMARY KEY (`loadout_id`, `ability_id`, `player_uuid`), " +
                        "CONSTRAINT FK_loadout FOREIGN KEY (`player_uuid`) REFERENCES " + LOADOUT_TABLE_NAME + "(`player_uuid`)" +
                        ");")) {
                    statement.executeUpdate();
                } catch (SQLException e) {
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

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        databaseManager.getDatabaseExecutorService().submit(() -> {

            CompletableFuture<Void> loadoutTableFuture = new CompletableFuture<>();

            TableVersionHistoryDAO.getLatestVersion(connection, LOADOUT_TABLE_NAME)
                    //Update the mcrpg_loadout_info first
                    .thenAccept(lastStoredVersion -> {

                        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                            loadoutTableFuture.complete(null);
                            return;
                        }

                        isAcceptingQueries = false;

                        //Adds table to our tracking
                        if (lastStoredVersion == 0) {
                            TableVersionHistoryDAO.setTableVersion(connection, LOADOUT_TABLE_NAME, 1);
                            lastStoredVersion = 1;
                        }

                        isAcceptingQueries = true;

                        loadoutTableFuture.complete(null);

                    });

            loadoutTableFuture.thenAccept(unused -> {

                TableVersionHistoryDAO.getLatestVersion(connection, LOADOUT_SLOTS_TABLE_NAME).thenAccept(lastStoredSlotsVersion -> {

                    if (lastStoredSlotsVersion >= CURRENT_TABLE_VERSION) {
                        completableFuture.complete(null);
                        return;
                    }

                    isAcceptingQueries = false;

                    //Adds table to our tracking
                    if (lastStoredSlotsVersion == 0) {
                        TableVersionHistoryDAO.setTableVersion(connection, LOADOUT_SLOTS_TABLE_NAME, 1);
                        lastStoredSlotsVersion = 1;
                    }

                    isAcceptingQueries = true;

                    completableFuture.complete(null);
                });

            });

        });

        return completableFuture;
    }

    /**
     * Gets a {@link java.util.List} of {@link us.eunoians.mcrpg.ability.impl.UnlockableAbility} that the provided player {@link UUID} has equipped.
     * <p>
     * This list is ordered by the slots that the player put them in, so this will persist order for player experience.
     *
     * @param connection The {@link Connection} that will be running the query
     * @param playerUUID The {@link UUID} of the player to get the loadout for
     * @return A {@link CompletableFuture} that has a {@link List} of {@link us.eunoians.mcrpg.ability.impl.Ability} that the
     * provided player {@link UUID} has equipped.
     */
    @NotNull
    public static CompletableFuture<Loadout> getPlayerLoadout(@NotNull Connection connection, @NotNull UUID playerUUID, int loadoutNumber) {

        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        CompletableFuture<Loadout> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            Loadout loadout = new Loadout(playerUUID, loadoutNumber);
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ability_id FROM " + LOADOUT_SLOTS_TABLE_NAME + " WHERE player_uuid = ? AND loadout_id = ?;")) {

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
                completableFuture.completeExceptionally(e);
            }
            completableFuture.complete(loadout);
        });

        return completableFuture;
    }

    public static CompletableFuture<Void> saveAllPlayerLoadouts(@NotNull Connection connection, @NotNull LoadoutHolder loadoutHolder) {
        UUID uuid = loadoutHolder.getUUID();
        int loadoutAmount = McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
        CompletableFuture[] futures = new CompletableFuture[loadoutAmount];
        for (int i = 1; i <= loadoutAmount; i++) {
            futures[i-1] = savePlayerLoadout(connection, uuid, loadoutHolder.getLoadout(i));
        }
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures);
        return allFuture;
    }

    @NotNull
    public static CompletableFuture<Void> savePlayerLoadout(@NotNull Connection connection, @NotNull UUID playerUUID, @NotNull Loadout loadout) {
        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        try {
            deletePlayerLoadout(connection, playerUUID, loadout.getLoadoutSlot()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        // If it's empty, don't bother saving
        if (loadout.getAbilities().isEmpty()) {
            completableFuture.complete(null);
            return completableFuture;
        }
        databaseManager.getDatabaseExecutorService().submit(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOADOUT_TABLE_NAME + " (player_uuid, loadout_id) VALUES (?, ?)")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, loadout.getLoadoutSlot());
                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOADOUT_SLOTS_TABLE_NAME + " (player_uuid, loadout_id, ability_id) VALUES (?, ?, ?);")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, loadout.getLoadoutSlot());

                for (NamespacedKey namespacedKey : loadout.getAbilities()) {
                    preparedStatement.setString(3, abilityRegistry.getRegisteredAbility(namespacedKey).getDatabaseName().get());
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
            completableFuture.complete(null);
        });

        return completableFuture;
    }

    public static CompletableFuture<Void> deletePlayerLoadout(@NotNull Connection connection, @NotNull UUID playerUUID, int loadoutId) {
        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + LOADOUT_SLOTS_TABLE_NAME + " WHERE player_uuid = ? AND loadout_id = ?;")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, loadoutId);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + LOADOUT_TABLE_NAME + " WHERE player_uuid = ? AND loadout_id = ?")) {
                preparedStatement.setString(1, playerUUID.toString());
                preparedStatement.setInt(2, loadoutId);
                preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            }
            completableFuture.complete(null);
        });
        return completableFuture;
    }
//
//    /**
//     * Gets a {@link List} of {@link UnlockedAbilities} that the provided loadout {@link UUID} has stored in it.
//     * <p>
//     * Keep in mind a player must have a loadout but a loadout doesn't always have a player. This method is private as
//     * we don't really want to expose the internals of how the loadout system works. So a developer should access {@link #getPlayerLoadout(Connection, UUID)}
//     * to get the loadout of a player. That method will use {@link #getPlayerLoadoutUUID(Connection, UUID)} to get the {@link UUID} for the loadout belonging
//     * to that player, then {@link #getLoadoutByUUID(Connection, UUID)} will be called using that loadout {@link UUID} in one seamless call for the developer.
//     *
//     * @param connection  The {@link Connection} that will be running the query
//     * @param loadoutUUID The {@link UUID} of the loadout
//     * @return A {@link CompletableFuture} that has a {@link List} of {@link UnlockedAbilities} that the provided loadout {@link UUID} has stored in it.
//     */
//    @NotNull
//    private static CompletableFuture<List<UnlockedAbilities>> getLoadoutByUUID(@NotNull Connection connection, @NotNull UUID loadoutUUID) {
//
//        McRPGDatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
//        CompletableFuture<List<UnlockedAbilities>> completableFuture = new CompletableFuture<>();
//
//        databaseManager.getDatabaseExecutorService().submit(() -> {
//
//            List<UnlockedAbilities> unlockedAbilities = new ArrayList<>();
//
//            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ABILITY_ID, SLOT_NUMBER FROM " + LOADOUT_SLOTS_TABLE_NAME + " WHERE LOADOUT_ID = ? ORDER BY SLOT_NUMBER;")) {
//
//                preparedStatement.setString(1, loadoutUUID.toString());
//
//                try (ResultSet resultSet = preparedStatement.executeQuery()) {
//
//                    while (resultSet.next()) {
//
//                        UnlockedAbilities unlockedAbility = UnlockedAbilities.fromString(resultSet.getString("ability_id"));
//
//                        //Handle duplicates from any existing tables that didn't have constraints
//                        if (!unlockedAbilities.contains(unlockedAbility)) {
//                            unlockedAbilities.add(unlockedAbility);
//                        }
//                    }
//                }
//            }
//            catch (SQLException e) {
//                completableFuture.completeExceptionally(e);
//            }
//
//            completableFuture.complete(unlockedAbilities);
//        });
//
//        return completableFuture;
//    }

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
