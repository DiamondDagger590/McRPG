package us.eunoians.mcrpg.database;

import com.cyr1en.flatdb.Database;
import com.cyr1en.flatdb.DatabaseBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Language;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.database.tables.LoadOutTableGenerator;
import us.eunoians.mcrpg.database.tables.PlayerData;
import us.eunoians.mcrpg.database.tables.PlayerSetting;
import us.eunoians.mcrpg.database.tables.skills.*;

import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.UUID;

public class McRPGDb {

  private McRPG instance;
  private Database database;

  public McRPGDb(McRPG plugin) {
    this.instance = plugin;
    DatabaseBuilder dbBuilder = new DatabaseBuilder();
    dbBuilder.setDatabasePrefix("mcrpg_");
    dbBuilder.setPath(plugin.getDataFolder().getAbsolutePath() + "/database/mcrpg");
    dbBuilder.appendTable(new LoadOutTableGenerator(9).asClass());
    dbBuilder.appendTable(PlayerData.class);
    dbBuilder.appendTable(PlayerSetting.class);
    dbBuilder.appendTable(ArcheryTable.class);
    dbBuilder.appendTable(HerbalismTable.class);
    dbBuilder.appendTable(MiningTable.class);
    dbBuilder.appendTable(SwordsTable.class);
    dbBuilder.appendTable(UnarmedTable.class);
    try {
      database = dbBuilder.build();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  public void convertLegacyToFlatDB(){
    File playerFolder = new File(instance.getDataFolder(), File.separator + "PlayerData");
    if(!playerFolder.exists()){
      Bukkit.getConsoleSender().sendMessage(Methods.color(instance.getPluginPrefix() + instance.getLangFile().getString("Messages.Utility.PlayerFolderDoesntExist")));
      return;
    }
    File[] playerFiles = playerFolder.listFiles();
    int playersProccessed = 0;
    Calendar cal = Calendar.getInstance();
    long beginTime = cal.getTimeInMillis();
    if(playerFiles != null) {
      Bukkit.getConsoleSender().sendMessage(Methods.color(instance.getPluginPrefix() + instance.getLangFile().getString("Messages.Utility.BeginningConversion")
              .replace("%FileAmount%", Integer.toString(playerFiles.length))));
      for(File f : playerFiles) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(f);
        UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
        int abilityPoints = config.getInt("AbilityPoints");
        String remoteTransferLocation = config.getString("Mining.RemoteTransfer.LinkedLocation");
        int redeemableExp = config.getInt("RedeemableExp");
        int redeemableLevels = config.getInt("RedeemableLevels");
        @Language("SQL") String query = "INSERT INTO mcrpg_player_data (uuid, ability_points, remote_transfer_location, redeemable_exp, redeemable_levels)" +
                "VALUES (" + uuid.toString() + ", " + abilityPoints + ", '" + remoteTransferLocation + "', " + redeemableExp + ", " + redeemableLevels+ " )";
        database.executeQuery(query);

        boolean ignoreTips = config.getBoolean("IgnoreTips");
        boolean keepHandEmpty = config.getBoolean("KeepHandEmpty");
        String healthBarType = config.getString("HealthType");
        String displayType = config.getString("DisplayType");
        boolean autoDeny = config.getBoolean("AutoDeny");
        query = "INSERT INTO mcrpg_player_settings (uuid, keep_hand, ignore_tips, auto_deny, display_type, health_type)" +
                "VALUES (" + uuid.toString() + ", " + keepHandEmpty + ", " + ignoreTips + ", " + autoDeny + ", '" + displayType + "', '" + healthBarType + "')";
        database.executeQuery(query);

        playersProccessed++;
      }
    }
  }
}
