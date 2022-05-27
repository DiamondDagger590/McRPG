package us.eunoians.mcrpg.database;

import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.builder.Database;
import com.diamonddagger590.mccore.database.builder.DatabaseBuilder;
import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import com.diamonddagger590.mccore.database.function.DatabaseInitializationFunction;
import com.diamonddagger590.mccore.exception.CoreDatabaseInitializationException;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.files.MainConfigurationFile;
import us.eunoians.mcrpg.database.table.PlayerDataDAO;
import us.eunoians.mcrpg.database.table.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.table.PlayerSettingsDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class McRPGDatabaseManager extends DatabaseManager {

    private final DatabaseDriver driver;
    private final DatabaseInitializationFunction databaseInitializationFunction = (driver) -> {

        DatabaseBuilder dbBuilder = new McRPGDatabaseBuilder(driver);

        dbBuilder.setPath(plugin.getDataFolder().getAbsolutePath() + "/database/mcrpg");

        Optional<Database> initializedDatabase = Optional.empty();

        try {
            initializedDatabase = Optional.of(dbBuilder.build());
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return initializedDatabase;
    };

    public McRPGDatabaseManager(@NotNull McRPG plugin) {
        super(plugin);
        Optional<DatabaseDriver> databaseDriver = DatabaseDriver.getFromString(McRPG.getInstance().getFileManager()
                                                                                   .getFileConfiguration(FileType.MAIN_CONFIG).getString(MainConfigurationFile.DATABASE_DRIVER.getPath(), (String) MainConfigurationFile.DATABASE_DRIVER.getDefaultValue()));

        if (databaseDriver.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "The configured database driver in the config.yml is invalid and is being defaulted to SQLite.");
            databaseDriver = Optional.of(DatabaseDriver.SQLITE);
        }
        this.driver = databaseDriver.get();
        populateCreateFunctions();
        populateUpdateFunctions();
    }

    /**
     * Adds the required {@link com.diamonddagger590.mccore.database.function.CreateTableFunction CreateTableFunctions} for McRPG to properly run.
     */
    private void populateCreateFunctions() {

        addCreateTableFunction((databaseManager) -> {

            CompletableFuture<Void> tableCreationFuture = new CompletableFuture<>();
            Database database = databaseManager.getDatabase();

            if (database == null) {
                tableCreationFuture.completeExceptionally(new CoreDatabaseInitializationException("The database for McRPG is null, please report this to the plugin developer."));
                return tableCreationFuture;
            }

            Connection connection = databaseManager.getDatabase().getConnection();

            getDatabaseExecutorService().submit(() -> {

                CompletableFuture.allOf(SkillDAO.attemptCreateTable(connection, this),
                        PlayerLoadoutDAO.attemptCreateTable(connection, this), PlayerDataDAO.attemptCreateTable(connection, this),
                        PlayerSettingsDAO.attemptCreateTable(connection, this))
                    .thenAccept(tableCreationFuture::complete)
                    .exceptionally(throwable -> {
                        tableCreationFuture.completeExceptionally(throwable);
                        return null;
                    });

            });

            return tableCreationFuture;
        });
    }

    /**
     * Adds the required {@link com.diamonddagger590.mccore.database.function.UpdateTableFunction UpdateTableFunctions} for McRPG to properly run.
     */
    private void populateUpdateFunctions() {

        addUpdateTableFunction((databaseManager -> {

            CompletableFuture<Void> tableUpdateFuture = new CompletableFuture<>();
            Database database = databaseManager.getDatabase();

            if (database == null) {
                tableUpdateFuture.completeExceptionally(new CoreDatabaseInitializationException("The database for McRPG is null, please report this to the plugin developer."));
                return tableUpdateFuture;
            }

            Connection connection = databaseManager.getDatabase().getConnection();

            getDatabaseExecutorService().submit(() -> {
                CompletableFuture.allOf(SkillDAO.updateTable(connection), PlayerLoadoutDAO.updateTable(connection),
                        PlayerDataDAO.updateTable(connection), PlayerSettingsDAO.updateTable(connection))
                    .thenAccept(tableUpdateFuture::complete);
            });

            return tableUpdateFuture;
        }));
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public DatabaseInitializationFunction getDatabaseInitializationFunction() {
        return databaseInitializationFunction;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public DatabaseDriver getDatabaseDriver() {
        return driver;
    }

    @NotNull
    public DatabaseDriver getDriver() {
        return this.driver;
    }
}
