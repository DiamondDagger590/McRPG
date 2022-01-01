package us.eunoians.mcrpg.database.tables;

import us.eunoians.mcrpg.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class TableVersionHistoryDAO {

    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Gets a {@link CompletableFuture} containing an {@link Integer} that contains the latest version the
     * provided table name was updated against. This is used to track table updates over time and to handle updating
     * tables as needed.
     *
     * @param connection The databased {@link Connection} to use to save the query to
     * @param tableName  The name of the table we are checking
     * @return The {@link Integer} version of the table or {@code 0} if the table doesn't have any version saved
     */
    public static CompletableFuture<Integer> getLatestVersion(Connection connection, String tableName) {

        return CompletableFuture.supplyAsync(() -> {

            int lastVersion = 0;
            try (PreparedStatement statement = connection.prepareStatement("SELECT table_version FROM table_history WHERE table_name = ?;")) {
                statement.setString(1, tableName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        lastVersion = resultSet.getInt("table_version");
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

            return lastVersion;
        });
    }

    public static void setTableVersion(Connection connection, String tableName, int version) {

    }

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param databaseManager The {@link DatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    public static CompletableFuture<Boolean> attemptCreateTable(Connection connection, DatabaseManager databaseManager) {

        return CompletableFuture.supplyAsync(() -> {

            if (databaseManager.getDatabase().tableExists("table_history")) {
                return false;
            }

            /*****
             ** Table Description:
             ** Contains the versions a table was last updated
             **
             ** table_name is the name of the sql table we are storing the version of
             ** updated_time is the time stamp the table was last updated
             ** table_version is the latest version of the table
             **
             ** Reasoning for structure:
             ** PK is the `table_name` field, as each table has one version assigned to it
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `table_history`" +
                                                                           "(" +
                                                                           "`table_name` varchar(32) NOT NULL," +
                                                                           "`updated_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                                                                           "`table_version` int(11) NOT NULL DEFAULT '0'," +
                                                                           "PRIMARY KEY (`table_name`)" +
                                                                           ");")) {
                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        });
    }

    public static void updateTable(Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement("")) {

        }
    }
}
