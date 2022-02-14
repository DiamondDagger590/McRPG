package us.eunoians.mcrpg.database.tables;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.database.builder.DatabaseDriver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to track the versions for different tables which can be used
 * to run updates for those tables as needed.
 *
 * @author DiamondDagger590
 */
public class TableVersionHistoryDAO {

    private static final String TABLE_NAME = "table_history";
    private static final int CURRENT_TABLE_VERSION = 1;

    private static boolean isAcceptingQueries = true;

    /**
     * Gets a {@link CompletableFuture} containing an {@link Integer} that contains the latest version the
     * provided table name was updated against. This is used to track table updates over time and to handle updating
     * tables as needed.
     *
     * @param connection The databased {@link Connection} to use to save the query to
     * @param tableName  The name of the table we are checking
     * @return The {@link Integer} version of the table, {@code 0} if the table doesn't have any version saved or {@code -1}
     * if {@link #isAcceptingQueries()} returns {@code false}.
     */
    public static CompletableFuture<Integer> getLatestVersion(Connection connection, String tableName) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (!isAcceptingQueries()) {
                completableFuture.complete(-1);
                return;
            }

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
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(lastVersion);
        });

        return completableFuture;
    }

    /**
     * Sets the version for a table to mark an update having been processed
     *
     * @param connection The {@link Connection} to use for this update
     * @param tableName  The name of the table having its version updated
     * @param version    The new version of the table to store
     * @return A {@link CompletableFuture} that is being used to run this change which returns {@code true}
     * if ran successfully or {@code false} otherwise.
     */
    @NotNull
    public static CompletableFuture<Boolean> setTableVersion(@NotNull Connection connection, @NotNull String tableName, int version) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        DatabaseDriver databaseDriver = databaseManager.getDriver();
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (!isAcceptingQueries()) {
                completableFuture.complete(false);
                return;
            }

            //Update table to contain new table version
            try (PreparedStatement statement = databaseDriver == DatabaseDriver.H2 ? connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (table_name, updated_time, table_version) " +
                                                                                                                 "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE updated_time = VALUES(updated_time), " +
                                                                                                                 "table_version = VALUES(table_version);")
                                                                                   : connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (table_name, updated_time, table_version) " +
                                                                                                                 "VALUES (?, ?, ?);")) {
                statement.setString(1, tableName);
                statement.setTime(2, new Time(Calendar.getInstance().getTimeInMillis()));
                statement.setInt(3, version); //We know the version needs to be 1, so we are hard coding it here rather than incrementing the variable, as we can't confirm that this query works so it's unsafe to assume so

                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            completableFuture.complete(true);
        });

        return completableFuture;
    }

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

            if (databaseManager.getDatabase().tableExists(TABLE_NAME)) {
                completableFuture.complete(false);
                return;
            }

            isAcceptingQueries = false;

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
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
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
    @NotNull
    public static CompletableFuture<Void> updateTable(@NotNull Connection connection) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            getLatestVersion(connection, TABLE_NAME).thenAccept(latestStoredVersion -> {

                if (latestStoredVersion >= CURRENT_TABLE_VERSION) {
                    completableFuture.complete(null);
                    return;
                }

                isAcceptingQueries = false;

                //We need multiple if statements since we are going to be incrementing any values by one each time we find an absent version. This will allow us to update from 0 to a version like 2 in one go.

                //Table version 0 (doesn't exist or value isn't present)
                if (latestStoredVersion == 0) {
                    setTableVersion(connection, TABLE_NAME, 1);
                    latestStoredVersion = 1;
                }

                isAcceptingQueries = true;

            });

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Checks to see if this table is accepting queries at the moment. A reason it could be false is either the table is
     * in creation or the table is being updated and for some reason a query is attempting to be ran.
     *
     * @return {@code true} if this table is accepting queries
     */
    public static boolean isAcceptingQueries() {
        return isAcceptingQueries;
    }
}
