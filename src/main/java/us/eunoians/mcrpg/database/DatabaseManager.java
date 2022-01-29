package us.eunoians.mcrpg.database;

import com.cyr1en.flatdb.Database;
import com.cyr1en.flatdb.DatabaseBuilder;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.tables.PlayerDataDAO;
import us.eunoians.mcrpg.database.tables.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.tables.PlayerSettingsDAO;
import us.eunoians.mcrpg.database.tables.SkillDAO;
import us.eunoians.mcrpg.database.tables.TableVersionHistoryDAO;

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

                SkillDAO.attemptCreateTable(connection, this)
                        .thenAccept(skillTableCreated ->
                                logger.log(Level.INFO, "Database Creation - Skill DAO "
                                                       + (skillTableCreated ? "created a new table." : "already existed so skipping creation.")));

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

                SkillDAO.updateTable(connection).thenAccept(skillNull ->
                        logger.log(Level.INFO, "Database Update - Skill DAO has undergone any applicable updates."));

                PlayerLoadoutDAO.updateTable(connection).thenAccept(playerLoadoutNull ->
                        logger.log(Level.INFO, "Database Update - Player Loadout DAO has undergone any applicable updates."));

                PlayerDataDAO.updateTable(connection).thenAccept(playerDataNull ->
                        logger.log(Level.INFO, "Database Update - Player Data DAO has undergone any applicable updates."));

                PlayerSettingsDAO.updateTable(connection).thenAccept(playerSettingsNull ->
                        logger.log(Level.INFO, "Database Update - Player Settings DAO has undergone any applicable updates."));

                tableUpdateFuture.complete(null);
            });
        });

        return tableUpdateFuture;

    }
}
