package us.eunoians.mcrpg.database;

import com.diamonddagger590.mccore.database.builder.Database;
import com.diamonddagger590.mccore.database.builder.DatabaseBuilder;
import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import com.diamonddagger590.mccore.database.builder.FastStrings;
import com.diamonddagger590.mccore.database.builder.FlatDatabase;
import lombok.Getter;

import java.sql.SQLException;

public class McRPGDatabaseBuilder extends DatabaseBuilder {

    @Getter
    private String path;
    @Getter
    private String driverName;

    private String connectionURL;

    public McRPGDatabaseBuilder(DatabaseDriver databaseDriver) {
        super(databaseDriver);
        path = "";
        connectionURL = databaseDriver.getConnectionURL();

        tryDriverName(databaseDriver.getDatabaseDriverClass());
    }

    public McRPGDatabaseBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    private void tryDriverName(String driverName) {

        try {
            Class.forName(driverName).newInstance();
            this.driverName = driverName.split("\\.")[1];
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public String getConnectionURL() {
        return String.format(connectionURL, driverName, path);
    }

    public Database build() throws SQLException {
        if (FastStrings.isBlank(driverName)) {
            throw new SQLException("The driver name was left empty!");
        }
        connectionURL = getConnectionURL();

        return new FlatDatabase(this);
    }
}
