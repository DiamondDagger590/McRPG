package us.eunoians.mcrpg.database;

import com.diamonddagger590.mccore.database.ConnectionDetails;
import com.diamonddagger590.mccore.database.Credentials;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.LoadoutAbilityDAO;
import us.eunoians.mcrpg.database.table.LoadoutDisplayDAO;
import us.eunoians.mcrpg.database.table.LoadoutInfoDAO;
import us.eunoians.mcrpg.database.table.PlayerExperienceExtrasDAO;
import us.eunoians.mcrpg.database.table.PlayerLoadoutSelectionDAO;
import us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a database for McRPG
 */
public class McRPGDatabase extends Database {

    public McRPGDatabase(@NotNull McRPG mcRPG, @NotNull DatabaseDriverType driverType) {
        super(mcRPG, driverType);
        populateCreateFunctions();
        populateUpdateFunctions();
        initializeDatabase();
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @NotNull
    @Override
    protected Credentials getCredentials() {
        switch (getDatabaseDriverType()) {
            case SQLITE -> {
                return new Credentials("", -1, "", "", "");
            }
            default ->
                    throw new IllegalArgumentException("Unknown database driver type found " + getDatabaseDriverType().getDriverName());
        }
    }

    @NotNull
    @Override
    protected ConnectionDetails getConnectionDetails() {
        switch (getDatabaseDriverType()) {
            case SQLITE -> {
                return new ConnectionDetails(15 * 1000, 60 * 1000, 15 * 60 * 1000, 3, 8, 6 * 1000);
            }
            default ->
                    throw new IllegalArgumentException("Unknown database driver type found " + getDatabaseDriverType().getDriverName());
        }
    }

    private void populateCreateFunctions() {
        addCreateTableFunction(database -> {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            Logger logger = getPlugin().getLogger();
            database.getDatabaseExecutorService().submit(() -> {
                try (Connection connection = database.getConnection()) {
                    logger.log(Level.INFO, "Database Creation - Skill DAO "
                            + (SkillDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    logger.log(Level.INFO, "Database Creation - Loadout Info DAO "
                            + (LoadoutInfoDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    logger.log(Level.INFO, "Database Creation - Loadout Ability DAO "
                            + (LoadoutAbilityDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    logger.log(Level.INFO, "Database Creation - Loadout Display DAO "
                            + (LoadoutDisplayDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    logger.log(Level.INFO, "Database Creation - Player Data DAO "
                            + (PlayerLoginTimeDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    logger.log(Level.INFO, "Database Creation - Player Experience Extras DAO "
                            + (PlayerExperienceExtrasDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    logger.log(Level.INFO, "Database Creation - Player Loadout Selection DAO "
                            + (PlayerLoadoutSelectionDAO.attemptCreateTable(connection, database) ? "created a new table." : "already existed so skipping creation."));
                    completableFuture.complete(null);
                }
                catch (SQLException e) {
                    completableFuture.completeExceptionally(e);
                }
            });
            return completableFuture;
        });
    }

    private void populateUpdateFunctions() {
        addUpdateTableFunction(database -> {
            CompletableFuture<Void> completableFuture = new CompletableFuture<>();
            Logger logger = getPlugin().getLogger();
            database.getDatabaseExecutorService().submit(() -> {
                try (Connection connection = database.getConnection()) {
                    SkillDAO.updateTable(connection);
                    LoadoutInfoDAO.updateTable(connection);
                    LoadoutAbilityDAO.updateTable(connection);
                    LoadoutDisplayDAO.updateTable(connection);
                    PlayerLoginTimeDAO.updateTable(connection);
                    PlayerExperienceExtrasDAO.updateTable(connection);
                    PlayerLoadoutSelectionDAO.updateTable(connection);
                    completableFuture.complete(null);
                }
                catch (SQLException e) {
                    completableFuture.completeExceptionally(e);
                }
            });
            return completableFuture;
        });
    }
}
