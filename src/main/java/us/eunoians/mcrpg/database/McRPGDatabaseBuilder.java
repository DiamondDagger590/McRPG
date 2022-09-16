package us.eunoians.mcrpg.database;

import com.diamonddagger590.mccore.database.builder.DatabaseBuilder;
import com.diamonddagger590.mccore.database.builder.DatabaseDriver;
import org.jetbrains.annotations.NotNull;

public class McRPGDatabaseBuilder extends DatabaseBuilder {

    public McRPGDatabaseBuilder(DatabaseDriver databaseDriver) {
        super(databaseDriver);
    }

    @Override
    @NotNull
    public McRPGDatabaseBuilder setPath(@NotNull String path) {
        return (McRPGDatabaseBuilder) super.setPath(path);
    }
}
