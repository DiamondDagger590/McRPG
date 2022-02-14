package us.eunoians.mcrpg.database.builder;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

/**
 * An enum representing the different types of database drivers that are supported.
 *
 * @author DiamondDagger590
 */
public enum DatabaseDriver {

    H2("h2", "org.h2.Driver", "jdbc:%s:%s;mode=MySQL;PAGE_SIZE=2048"),
    SQLITE("sqlite", "org.sqlite.JDBC", "jdbc:%s:%s.db");

    private final String databaseDriverName;
    private final String databaseDriverClass;
    private final String connectionURL;

    DatabaseDriver(@NotNull String databaseDriverName, @NotNull String databaseDriverClass, @NotNull String connectionURL) {
        this.databaseDriverName = databaseDriverName;
        this.databaseDriverClass = databaseDriverClass;
        this.connectionURL = connectionURL;
    }

    /**
     * Gets the name of this driver, such as "h2" for {@link #H2}.
     *
     * @return The name of this driver, such as "h2" for {@link #H2}
     */
    @NotNull
    public String getDatabaseDriverName() {
        return databaseDriverName;
    }

    /**
     * Gets the class path for the driver to be instantiated
     *
     * @return The class path for the driver to be instantiated
     */
    @NotNull
    public String getDatabaseDriverClass() {
        return databaseDriverClass;
    }

    /**
     * Gets the URL to use for connecting to a database using this specific driver
     *
     * @return The URL to use for connecting to a database using this specific driver
     */
    @NotNull
    public String getConnectionURL() {
        return connectionURL;
    }

    /**
     * Gets an {@link Optional} containing the {@link DatabaseDriver} that matches the provided database driver name,
     * or an empty {@link Optional} if no matches are found
     *
     * @param databaseDriverName The database driver name to find a match for
     * @return An {@link Optional} containing the {@link DatabaseDriver} that matches the provided database driver name,
     * or an empty {@link Optional} if no matches are found
     */
    @NotNull
    public static Optional<DatabaseDriver> getFromString(@NotNull String databaseDriverName) {
        return Arrays.stream(values()).filter(databaseDriver -> databaseDriver.getDatabaseDriverName().equalsIgnoreCase(databaseDriverName)).findFirst();
    }
}
