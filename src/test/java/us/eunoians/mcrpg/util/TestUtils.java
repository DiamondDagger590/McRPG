package us.eunoians.mcrpg.util;

import com.diamonddagger590.mccore.database.builder.Database;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;

import java.sql.Connection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {


    public static void mockDatabaseConnection(McRPG mcRPG) {
        McRPGDatabaseManager mcRPGDatabaseManager = mock(McRPGDatabaseManager.class);
        Database database = mock(Database.class);
        Connection connection = mock(Connection.class);

        when(mcRPG.getDatabaseManager()).thenReturn(mcRPGDatabaseManager);
        when(mcRPGDatabaseManager.getDatabase()).thenReturn(database);
        when(database.getConnection()).thenReturn(connection);
    }
}
