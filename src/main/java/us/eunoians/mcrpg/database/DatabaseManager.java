package us.eunoians.mcrpg.database;

import com.cyr1en.flatdb.Database;
import com.cyr1en.flatdb.DatabaseBuilder;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.tables.PlayerDataDAO;
import us.eunoians.mcrpg.database.tables.PlayerSettingsDAO;
import us.eunoians.mcrpg.database.tables.TableVersionHistoryDAO;
import us.eunoians.mcrpg.database.tables.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.tables.skills.ArcheryDAO;
import us.eunoians.mcrpg.database.tables.skills.AxesDAO;
import us.eunoians.mcrpg.database.tables.skills.ExcavationDAO;
import us.eunoians.mcrpg.database.tables.skills.FishingDAO;
import us.eunoians.mcrpg.database.tables.skills.FitnessDAO;
import us.eunoians.mcrpg.database.tables.skills.HerbalismDAO;
import us.eunoians.mcrpg.database.tables.skills.MiningDAO;
import us.eunoians.mcrpg.database.tables.skills.SorceryDAO;
import us.eunoians.mcrpg.database.tables.skills.SwordsDAO;
import us.eunoians.mcrpg.database.tables.skills.TamingDAO;
import us.eunoians.mcrpg.database.tables.skills.UnarmedDAO;
import us.eunoians.mcrpg.database.tables.skills.WoodcuttingDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private McRPG instance;
    private Database database;
    private final ThreadPoolExecutor databaseExecutorService =
            new ThreadPoolExecutor(1, 4, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());


    public DatabaseManager(McRPG plugin) {
        this.instance = plugin;

        DatabaseBuilder dbBuilder = new DatabaseBuilder();
        dbBuilder.setDatabasePrefix("mcrpg_");
        dbBuilder.setPath(plugin.getDataFolder().getAbsolutePath() + "/database/mcrpg");

        try {
            database = dbBuilder.build();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        Logger logger = plugin.getLogger();

        //Create any missing tables
        attemptCreateTables().thenAccept(tableCreationNull -> {

            logger.log(Level.INFO, "Any missing database tables have been created! Now starting the updating process...");
            //Update all tables and alert console
            updateTables().thenAccept(tableUpdateNull -> {
                logger.log(Level.INFO, "All database tables are now updated to their latest versions!");
            });

        }).exceptionally(throwable -> {
            logger.log(Level.WARNING, "There was an error creating any missing database tables... please alert the developer of this plugin.");
            return null;
        });
        //TODO Fire events to allow custom DAO's to be created from 3rd party plugins?

    }

    /**
     * Gets the {@link Database} that is being used
     *
     * @return The {@link Database} that is being used
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Gets the {@link ThreadPoolExecutor} used to run database queries
     *
     * @return The {@link ThreadPoolExecutor} used to run database queries
     */
    public ThreadPoolExecutor getDatabaseExecutorService() {
        return databaseExecutorService;
    }

    private CompletableFuture<Void> attemptCreateTables() {

        Connection connection = database.getConnection();
        Logger logger = McRPG.getInstance().getLogger();

        CompletableFuture<Void> tableCreationFuture = new CompletableFuture<>();

        getDatabaseExecutorService().submit(() -> {

            //We need to create this first to ensure that versioning is setup
            TableVersionHistoryDAO.attemptCreateTable(connection, this).thenAccept(tableVersionHistoryTableCreated -> {

                logger.log(Level.INFO, "Database Creation - Table Version History DAO "
                                       + (tableVersionHistoryTableCreated ? "created a new table." : "already existed so skipping creation."));

                PlayerLoadoutDAO.attemptCreateTable(connection, this)
                        .thenAccept(playerLoadoutTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Player Loadout DAO "
                                                       + (playerLoadoutTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Player Loadout DAO had an error when creating.");
                            return null;
                        });

                PlayerDataDAO.attemptCreateTable(connection, this)
                        .thenAccept(playerDataTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Player Data DAO "
                                                       + (playerDataTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Player Data DAO had an error when creating.");
                            return null;
                        });

                PlayerSettingsDAO.attemptCreateTable(connection, this)
                        .thenAccept(playerSettingsTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Player Settings DAO "
                                                       + (playerSettingsTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Player Settings DAO had an error when creating.");
                            return null;
                        });

                //Can now start creating skill tables since the table version has already been created
                ArcheryDAO.attemptCreateTable(connection, this)
                        .thenAccept(archeryTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Archery DAO "
                                                       + (archeryTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Archery DAO had an error when creating.");
                            return null;
                        });

                AxesDAO.attemptCreateTable(connection, this)
                        .thenAccept(axesTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Axes DAO "
                                                       + (axesTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Axes DAO had an error when creating.");
                            return null;
                        });

                ExcavationDAO.attemptCreateTable(connection, this)
                        .thenAccept(excavationTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Excavation DAO "
                                                       + (excavationTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Excavation DAO had an error when creating.");
                            return null;
                        });

                FishingDAO.attemptCreateTable(connection, this)
                        .thenAccept(fishingTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Fishing DAO "
                                                       + (fishingTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                                    logger.log(Level.WARNING, "Database Creation - Fishing DAO had an error when creating.");
                                    return null;
                                }
                        );

                FitnessDAO.attemptCreateTable(connection, this)
                        .thenAccept(fitnessTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Fitness DAO "
                                                       + (fitnessTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Fitness DAO had an error when creating.");
                            return null;
                        });

                HerbalismDAO.attemptCreateTable(connection, this)
                        .thenAccept(herbalismTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Herbalism DAO "
                                                       + (herbalismTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Herbalism DAO had an error when creating.");
                            return null;
                        });

                MiningDAO.attemptCreateTable(connection, this)
                        .thenAccept(miningTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Mining DAO "
                                                       + (miningTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Mining DAO had an error when creating.");
                            return null;
                        });

                SorceryDAO.attemptCreateTable(connection, this)
                        .thenAccept(sorceryTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Sorcery DAO "
                                                       + (sorceryTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Sorcery DAO had an error when creating.");
                            return null;
                        });

                SwordsDAO.attemptCreateTable(connection, this)
                        .thenAccept(swordsTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Swords DAO "
                                                       + (swordsTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Swords DAO had an error when creating.");
                            return null;
                        });

                TamingDAO.attemptCreateTable(connection, this)
                        .thenAccept(tamingTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Taming DAO "
                                                       + (tamingTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Taming DAO had an error when creating.");
                            return null;
                        });

                UnarmedDAO.attemptCreateTable(connection, this)
                        .thenAccept(unarmedTabledCreated ->
                                logger.log(Level.INFO, "Database Creation - Unarmed DAO "
                                                       + (unarmedTabledCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Unarmed DAO had an error when creating.");
                            return null;
                        });

                WoodcuttingDAO.attemptCreateTable(connection, this)
                        .thenAccept(woodcuttingTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Woodcutting DAO "
                                                       + (woodcuttingTableCreated ? "created a new table." : "already existed so skipping creation.")))
                        .exceptionally(throwable -> {
                            logger.log(Level.WARNING, "Database Creation - Woodcutting DAO had an error when creating.");
                            return null;
                        });

                tableCreationFuture.complete(null);

            }).exceptionally(throwable -> {
                tableCreationFuture.completeExceptionally(throwable);
                return null;
            });
        });

        return tableCreationFuture;

    }

    private CompletableFuture<Void> updateTables() {

        Connection connection = database.getConnection();
        Logger logger = McRPG.getInstance().getLogger();

        CompletableFuture<Void> tableUpdateFuture = new CompletableFuture<>();

        logger.log(Level.INFO, "Starting the Database Update process... This will check existing tables for updates and update them as needed.");

        getDatabaseExecutorService().submit(() -> {

            //We need to first update the table version history, and then we can update other tables
            TableVersionHistoryDAO.updateTable(connection).thenAccept(thisIsNullThouLol -> {

                logger.log(Level.INFO, "Database Update - Table Version History DAO has undergone any applicable updates.");

                PlayerLoadoutDAO.updateTable(connection).thenAccept(playerLoadoutNull ->
                        logger.log(Level.INFO, "Database Update - Player Loadout DAO has undergone any applicable updates."));

                PlayerDataDAO.updateTable(connection).thenAccept(playerDataNull ->
                        logger.log(Level.INFO, "Database Update - Player Data DAO has undergone any applicable updates."));

                PlayerSettingsDAO.updateTable(connection).thenAccept(playerSettingsNull ->
                        logger.log(Level.INFO, "Database Update - Player Settings DAO has undergone any applicable updates."));

                ArcheryDAO.updateTable(connection).thenAccept(archeryNull ->
                        logger.log(Level.INFO, "Database Update - Archery DAO has undergone any applicable updates."));
                AxesDAO.updateTable(connection).thenAccept(axesNull ->
                        logger.log(Level.INFO, "Database Update - Axes DAO has undergone any applicable updates."));
                ExcavationDAO.updateTable(connection).thenAccept(excavationNull ->
                        logger.log(Level.INFO, "Database Update - Excavation DAO has undergone any applicable updates."));
                FishingDAO.updateTable(connection).thenAccept(fishingNull ->
                        logger.log(Level.INFO, "Database Update - Fishing DAO has undergone any applicable updates."));
                FitnessDAO.updateTable(connection).thenAccept(fitnessNull ->
                        logger.log(Level.INFO, "Database Update - Fitness DAO has undergone any applicable updates."));
                HerbalismDAO.updateTable(connection).thenAccept(herbalismNull ->
                        logger.log(Level.INFO, "Database Update - Herbalism DAO has undergone any applicable updates."));
                MiningDAO.updateTable(connection).thenAccept(miningNull ->
                        logger.log(Level.INFO, "Database Update - Mining DAO has undergone any applicable updates."));
                SorceryDAO.updateTable(connection).thenAccept(sorceryNull ->
                        logger.log(Level.INFO, "Database Update - Sorcery DAO has undergone any applicable updates."));
                SwordsDAO.updateTable(connection).thenAccept(swordsNull ->
                        logger.log(Level.INFO, "Database Update - Swords DAO has undergone any applicable updates."));
                TamingDAO.updateTable(connection).thenAccept(tamingNull ->
                        logger.log(Level.INFO, "Database Update - Taming DAO has undergone any applicable updates."));
                UnarmedDAO.updateTable(connection).thenAccept(unarmedNull ->
                        logger.log(Level.INFO, "Database Update - Unarmed DAO has undergone any applicable updates."));
                WoodcuttingDAO.updateTable(connection).thenAccept(woodcuttingNull ->
                        logger.log(Level.INFO, "Database Update - Woodcutting DAO has undergone any applicable updates."));

                tableUpdateFuture.complete(null);
            });
        });

        return tableUpdateFuture;

    }
}
