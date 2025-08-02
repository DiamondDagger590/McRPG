package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.TableVersionHistoryDAO;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This DAO manages tracking various {@link Instant}s in time
 * related to a player's login data.
 */
public class PlayerLoginTimeDAO {

    static final String TABLE_NAME = "mcrpg_player_data";
    private static final int CURRENT_TABLE_VERSION = 1;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection The {@link Connection} to use to attempt the creation
     * @param database   The {@link Database} being used to attempt to create the table
     * @return {@code true} if a new table was made or {@code false} otherwise.
     */
    public static boolean attemptCreateTable(@NotNull Connection connection, @NotNull Database database) {
        //Check to see if the table already exists
        if (database.tableExists(connection, TABLE_NAME)) {
            return false;
        }

        /*****
         ** Table Description:
         ** Contains player login information
         *
         *
         * uuid is the {@link java.util.UUID} of the player being stored
         * first_login_time is the time that the player first logged in while McRPG was running
         * last_login_time is the time that the player last logged in while McRPG was running
         * last_seen_time is the time that the player was last seen while McRPG was running (logged in, logged out or data saved while online)
         * logged_out_in_safezone checks if the player last logged out in a "safe zone"
         * last_logout_time is the time that the player last logged out while McRPG was running
         *
         **
         ** Reasoning for structure:
         ** PK is the `uuid` field, as each player only has one uuid
         *****/
        try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                "(" +
                "`uuid` varchar(36) NOT NULL," +
                "`first_login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`last_login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "`last_seen_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`logged_out_in_safezone` INTEGER NOT NULL DEFAULT 0," +
                "`last_logout_time` DATETIME," +
                "PRIMARY KEY (`uuid`)" +
                ");")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     */
    public static void updateTable(@NotNull Connection connection) {
        int lastStoredVersion = TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME);
        if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
            return;
        }

        //Adds table to our tracking
        if (lastStoredVersion == 0) {
            TableVersionHistoryDAO.setTableVersion(connection, TABLE_NAME, 1);
            lastStoredVersion = 1;
        }
    }

    /**
     * Checks to see if a player has logged into the server while McRPG before was running.
     *
     * @param connection The {@link Connection} to check on.
     * @param uuid       The {@link UUID} of the player to check.
     * @return {@code true} if a player matching the provided {@link UUID} has logged into
     * the server before while McRPG was running.
     */
    public static boolean hasPlayerLoggedInBefore(@NotNull Connection connection, @NotNull UUID uuid) {
        boolean found = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT first_login_time FROM " + TABLE_NAME + " WHERE uuid = ?;")) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    found = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return found;
    }

    /**
     * Gets the {@link Instant} a player first logged into the server while McRPG was running.
     *
     * @param connection The {@link Connection} to check on.
     * @param uuid       The {@link UUID} of the player to check.
     * @return An {@link Optional} that contains {@link Instant} a player first logged into the server while
     * McRPG was running, or an empty optional if there is no data.
     */
    @NotNull
    public static Optional<Instant> getFirstLoginTime(@NotNull Connection connection, @NotNull UUID uuid) {
        Optional<Instant> firstLoginTime = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT first_login_time FROM " + TABLE_NAME + " WHERE uuid = ?;")) {
            preparedStatement.setString(1, uuid.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    firstLoginTime = Optional.of(resultSet.getTimestamp("first_login_time").toInstant());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return firstLoginTime;
    }

    /**
     * Gets the {@link Instant} a player last logged out of the server while McRPG was running.
     *
     * @param connection The {@link Connection} to check on.
     * @param uuid       The {@link UUID} of the player to check.
     * @return An {@link Optional} that contains {@link Instant} a player last logged out of the server while
     * McRPG was running, or an empty optional if there is no data.
     */
    @NotNull
    public static Optional<Instant> getLastLogoutTime(@NotNull Connection connection, @NotNull UUID uuid) {
        Optional<Instant> lastLogoutTime = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT last_logout_time FROM " + TABLE_NAME + " WHERE uuid = ? AND last_logout_time IS NOT NULL;")) {
            preparedStatement.setString(1, uuid.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    lastLogoutTime = Optional.of(resultSet.getTimestamp("last_logout_time").toInstant());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastLogoutTime;
    }

    /**
     * Gets the {@link Instant} a player last logged into the server while McRPG was running.
     *
     * @param connection The {@link Connection} to check on.
     * @param uuid       The {@link UUID} of the player to check.
     * @return An {@link Optional} that contains {@link Instant} a player last logged into the server while
     * McRPG was running, or an empty optional if there is no data.
     */
    @NotNull
    public static Optional<Instant> getLastLoginTime(@NotNull Connection connection, @NotNull UUID uuid) {
        Optional<Instant> lastLoginTime = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT last_login_time FROM " + TABLE_NAME + " WHERE uuid = ?;")) {
            preparedStatement.setString(1, uuid.toString());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    lastLoginTime = Optional.of(resultSet.getTimestamp("last_login_time").toInstant());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastLoginTime;
    }

    /**
     * Gets the {@link Instant} a player was last seen on the server while McRPG was running. This
     * data is updated when a player logs in, logs out and on a period basis while they are connected to the server.
     *
     * @param connection The {@link Connection} to check on.
     * @param uuid       The {@link UUID} of the player to check.
     * @return An {@link Optional} that contains {@link Instant} a player was last seen into the server while
     * McRPG was running, or an empty optional if there is no data.
     */
    @NotNull
    public static Optional<Instant> getLastSeenTime(@NotNull Connection connection, @NotNull UUID uuid) {
        Optional<Instant> lastSeenTime = Optional.empty();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT last_seen_time FROM " + TABLE_NAME + " WHERE uuid = ?;")) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    lastSeenTime = Optional.of(resultSet.getTimestamp("last_seen_time").toInstant());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastSeenTime;
    }

    /**
     * Checks to see if the player last logged out in a safe zone.
     *
     * @param connection The {@link Connection} to check on.
     * @param uuid       The {@link UUID} of the player to check.
     * @return {@code true} if the player last logged out in a safe zone.
     */
    public static boolean didPlayerLogoutInSafeZone(@NotNull Connection connection, @NotNull UUID uuid) {
        boolean loggedOutInSafeZone = false;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT logged_out_in_safezone FROM " + TABLE_NAME + " WHERE uuid = ?;")) {
            preparedStatement.setString(1, uuid.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    loggedOutInSafeZone = resultSet.getBoolean("logged_out_in_safezone");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loggedOutInSafeZone;
    }

    /**
     * Saves the {@link Instant} a player first logged into the server while McRPG was running.
     *
     * @param connection     The {@link Connection} to save on.
     * @param uuid           The {@link UUID} of the player to save login data for.
     * @param firstLoginTime The {@link Instant} that the player first logged in.
     * @return A {@link List} of {@link PreparedStatement}s needed to save the first login time for a player.
     */
    @NotNull
    public static List<PreparedStatement> saveFirstLoginTime(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Instant firstLoginTime) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, first_login_time) VALUES (?, ?);");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setTimestamp(2, Timestamp.from(firstLoginTime));
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    /**
     * Saves the {@link Instant} a player last logged out of the server while McRPG was running.
     *
     * @param connection     The {@link Connection} to save on.
     * @param uuid           The {@link UUID} of the player to save login data for.
     * @param lastLogoutTime The {@link Instant} that the player last logged out.
     * @return A {@link List} of {@link PreparedStatement}s needed to save the last logout time for a player.
     */
    @NotNull
    public static List<PreparedStatement> saveLastLogoutTime(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Instant lastLogoutTime) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, last_logout_time) VALUES (?, ?);");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setTimestamp(2, Timestamp.from(lastLogoutTime));
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    /**
     * Saves the {@link Instant} a player last logged into the server while McRPG was running.
     *
     * @param connection    The {@link Connection} to save on.
     * @param uuid          The {@link UUID} of the player to save login data for.
     * @param lastLoginTime The {@link Instant} that the player last logged in.
     * @return A {@link List} of {@link PreparedStatement}s needed to save the last login time for a player.
     */
    @NotNull
    public static List<PreparedStatement> saveLastLoginTime(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Instant lastLoginTime) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, last_login_time) VALUES (?, ?);");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setTimestamp(2, Timestamp.from(lastLoginTime));
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    /**
     * Saves the {@link Instant} a player was last seen on the server while McRPG was running.
     *
     * @param connection   The {@link Connection} to save on.
     * @param uuid         The {@link UUID} of the player to save login data for.
     * @param lastSeenTime The {@link Instant} that the player was last seen.
     * @return A {@link List} of {@link PreparedStatement}s needed to save the last seen time for a player.
     */
    @NotNull
    public static List<PreparedStatement> saveLastSeenTime(@NotNull Connection connection, @NotNull UUID uuid, @NotNull Instant lastSeenTime) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, last_seen_time) VALUES (?, ?);");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setTimestamp(2, Timestamp.from(lastSeenTime));
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }

    /**
     * Saves if the player logged out in a safe zone.
     *
     * @param connection          The {@link Connection} to save on.
     * @param uuid                The {@link UUID} of the player to save login data for.
     * @param loggedOutInSafeZone If the player logged out in a safe zone or not.
     * @return A {@link List} of {@link PreparedStatement}s needed to save if the player logged out
     * in a safe zone.
     */
    @NotNull
    public static List<PreparedStatement> saveLoggedOutInSafeZone(@NotNull Connection connection, @NotNull UUID uuid, boolean loggedOutInSafeZone) {
        List<PreparedStatement> preparedStatements = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("REPLACE INTO " + TABLE_NAME + " (uuid, logged_out_in_safezone) VALUES (?, ?);");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.setBoolean(2, loggedOutInSafeZone);
            preparedStatements.add(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatements;
    }
}
