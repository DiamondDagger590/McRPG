package us.eunoians.mcrpg.database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.files.MainConfigurationFile;
import us.eunoians.mcrpg.database.builder.Database;
import us.eunoians.mcrpg.database.builder.DatabaseBuilder;
import us.eunoians.mcrpg.database.builder.DatabaseDriver;
import us.eunoians.mcrpg.database.tables.PlayerDataDAO;
import us.eunoians.mcrpg.database.tables.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.tables.PlayerSettingsDAO;
import us.eunoians.mcrpg.database.tables.SkillDAO;
import us.eunoians.mcrpg.database.tables.TableVersionHistoryDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private McRPG plugin;
    private Database database;
    private final ThreadPoolExecutor databaseExecutorService =
            new ThreadPoolExecutor(1, 4, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    private final DatabaseDriver driver;

    public DatabaseManager(@NotNull McRPG plugin) {
        this.plugin = plugin;
        Optional<DatabaseDriver> databaseDriver = DatabaseDriver.getFromString(McRPG.getInstance().getFileManager()
            .getFileConfiguration(FileType.MAIN_CONFIG).getString(MainConfigurationFile.DATABASE_DRIVER.getPath(), (String) MainConfigurationFile.DATABASE_DRIVER.getDefaultValue()));

        if (databaseDriver.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "The configured database driver in the config.yml is invalid and is being defaulted to SQLite.");
            databaseDriver = Optional.of(DatabaseDriver.SQLITE);
        }
        this.driver = databaseDriver.get();
    }

    public CompletableFuture<Void> initialize() {

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        DatabaseBuilder dbBuilder = new DatabaseBuilder(driver);

        dbBuilder.setPath(plugin.getDataFolder().getAbsolutePath() + "/database/mcrpg");

        try {
            database = dbBuilder.build();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        Logger logger = plugin.getLogger();

        //Create any missing tables
        attemptCreateTables().thenAccept(unused -> {

            logger.log(Level.INFO, "Any missing database tables have been created! Now starting the updating process...");
            //Update all tables and alert console
            updateTables()
                    .thenAccept(unused1 -> {
                        logger.log(Level.INFO, "All database tables are now updated to their latest versions!");
                        completableFuture.complete(null);
                    }).exceptionally(throwable -> {
                        logger.log(Level.WARNING, "There was an error updating database tables... please alert the developer of this plugin.");
                        completableFuture.completeExceptionally(throwable);
                        return null;
                    });

        }).exceptionally(throwable -> {
            logger.log(Level.WARNING, "There was an error creating any missing database tables... please alert the developer of this plugin.");
            completableFuture.completeExceptionally(throwable);
            return null;
        });
        //TODO Fire events to allow custom DAO's to be created from 3rd party plugins?

        return completableFuture;
    }

    /**
     * Gets the {@link Database} that is being used
     *
     * @return The {@link Database} that is being used
     */
    @Nullable
    public Database getDatabase() {
        return database;
    }

    /**
     * Gets the {@link ThreadPoolExecutor} used to run database queries
     *
     * @return The {@link ThreadPoolExecutor} used to run database queries
     */
    @NotNull
    public ThreadPoolExecutor getDatabaseExecutorService() {
        return databaseExecutorService;
    }

    @NotNull
    public DatabaseDriver getDriver() {
        return this.driver;
    }

    @NotNull
    private CompletableFuture<Void> attemptCreateTables() {

        Connection connection = database.getConnection();
        Logger logger = McRPG.getInstance().getLogger();

        CompletableFuture<Void> tableCreationFuture = new CompletableFuture<>();

        getDatabaseExecutorService().submit(() -> {

            //We need to create this first to ensure that versioning is setup
            TableVersionHistoryDAO.attemptCreateTable(connection, this).thenAccept(tableVersionHistoryTableCreated -> {

                logger.log(Level.INFO, "Database Creation - Table Version History DAO "
                                       + (tableVersionHistoryTableCreated ? "created a new table." : "already existed so skipping creation."));

                CompletableFuture.allOf(SkillDAO.attemptCreateTable(connection, this),
                                PlayerLoadoutDAO.attemptCreateTable(connection, this), PlayerDataDAO.attemptCreateTable(connection, this),
                                PlayerSettingsDAO.attemptCreateTable(connection, this))
                        .thenAccept(tableCreationFuture::complete)
                        .exceptionally(throwable -> {
                            tableCreationFuture.completeExceptionally(throwable);
                            return null;
                        });

            }).exceptionally(throwable -> {
                tableCreationFuture.completeExceptionally(throwable);
                return null;
            });
        });

        return tableCreationFuture;

    }

    @NotNull
    private CompletableFuture<Void> updateTables() {

        Connection connection = database.getConnection();
        Logger logger = McRPG.getInstance().getLogger();

        CompletableFuture<Void> tableUpdateFuture = new CompletableFuture<>();

        logger.log(Level.INFO, "Starting the Database Update process... This will check existing tables for updates and update them as needed.");

        getDatabaseExecutorService().submit(() -> {


            //We need to first update the table version history, and then we can update other tables
            TableVersionHistoryDAO.updateTable(connection).thenAccept(unused -> {

                logger.log(Level.INFO, "Database Update - Table Version History DAO has undergone any applicable updates.");

                CompletableFuture.allOf(SkillDAO.updateTable(connection), PlayerLoadoutDAO.updateTable(connection),
                                PlayerDataDAO.updateTable(connection), PlayerSettingsDAO.updateTable(connection))
                        .thenAccept(tableUpdateFuture::complete);
            });
        });

        return tableUpdateFuture;

    }
}
