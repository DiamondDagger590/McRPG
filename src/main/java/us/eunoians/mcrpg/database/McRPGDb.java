package us.eunoians.mcrpg.database;

import com.cyr1en.flatdb.Database;
import com.cyr1en.flatdb.DatabaseBuilder;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.tables.LoadOutTableGenerator;

import java.sql.SQLException;

public class McRPGDb {

  private McRPG instance;
  private Database database;

  public McRPGDb(McRPG plugin) {
    this.instance = plugin;
    DatabaseBuilder dbBuilder = new DatabaseBuilder();
    dbBuilder.setDatabasePrefix("mcrpg_");
    dbBuilder.setPath(plugin.getDataFolder().getAbsolutePath() + "/database/mcrpg");
    dbBuilder.appendTable(new LoadOutTableGenerator(9).asClass());
    try {
      database = dbBuilder.build();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  
}
