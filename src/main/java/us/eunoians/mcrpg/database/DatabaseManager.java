package us.eunoians.mcrpg.database;

import com.cyr1en.flatdb.Database;
import com.cyr1en.flatdb.DatabaseBuilder;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.tables.TableVersionHistoryDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private McRPG instance;
    private Database database;

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

        attemptCreateTables();

    }

    /**
     * Gets the {@link Database} that is being used
     *
     * @return The {@link Database} that is being used
     */
    public Database getDatabase() {
        return database;
    }

    private void attemptCreateTables(){

        Connection connection = database.getConnection();
        Logger logger = McRPG.getInstance().getLogger();

        //We need to create this first to ensure that versioning is setup
        TableVersionHistoryDAO.attemptCreateTable(connection, this)
                .thenAccept(bool -> logger.log(Level.INFO, "Database Creation - Table Version History DAO "
                                                           + (bool ? "created" : "did not create") + " a new table."));
    }

    private void updateTables(){

    }
}
