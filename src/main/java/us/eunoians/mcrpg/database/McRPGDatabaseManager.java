package us.eunoians.mcrpg.database;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.DatabaseManager;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

public class McRPGDatabaseManager extends DatabaseManager<McRPG> {

    private final Database database;

    public McRPGDatabaseManager(@NotNull McRPG plugin) {
        super(plugin);
        this.database = new McRPGDatabase(plugin, DatabaseDriverType.SQLITE);
    }

    @NotNull
    @Override
    public Database getDatabase() {
        return database;
    }
}
