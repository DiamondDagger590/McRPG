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
import java.util.List;
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
        //Convert the player data table
        UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
        Integer abilityPoints = config.getInt("AbilityPoints");
        String remoteTransferLocation = config.getString("Mining.RemoteTransfer.LinkedLocation");
        Integer redeemableExp = config.getInt("RedeemableExp");
        Integer redeemableLevels = config.getInt("RedeemableLevels");
        @Language("SQL") String query = "INSERT INTO mcrpg_player_data (uuid, ability_points, remote_transfer_location, redeemable_exp, redeemable_levels) " +
                "VALUES (%s, %s, %s, %s, %s)";
        database.executeQuery(query, uuid.toString(), abilityPoints.toString(), remoteTransferLocation, redeemableExp.toString(), redeemableLevels.toString());

        //Convert player settings table
        Boolean ignoreTips = config.getBoolean("IgnoreTips");
        Boolean keepHandEmpty = config.getBoolean("KeepHandEmpty");
        String healthBarType = config.getString("HealthType");
        String displayType = config.getString("DisplayType");
        Boolean autoDeny = config.getBoolean("AutoDeny");
        query = "INSERT INTO mcrpg_player_settings (uuid, keep_hand, ignore_tips, auto_deny, display_type, health_type) " +
                "VALUES (%s, %s, %s, %s, %s, %s)";
        database.executeQuery(query, uuid.toString(), keepHandEmpty.toString(), ignoreTips.toString(), autoDeny.toString(), displayType, healthBarType);

        //Convert ability loadout table
        List<String> abilityLoadout = config.getStringList("AbilityLoadout");
        query = "INSERT INTO mcrpg_player_loadout (uuid";
        for(int i = 1; i <= abilityLoadout.size(); i++){
          query += ", " + i;
        }
        query += ") VALUES (";
        for(String s : abilityLoadout) {
          query += ", " + s;
        }
        query += ")";
        database.executeQuery(query);

        //Convert Swords table
        Integer currentExp = config.getInt("Swords.CurrentExp");
        Integer level = config.getInt("Swords.Level");
        Boolean isBleedToggled = config.getBoolean("Swords.Bleed.IsToggled");
        Boolean isBleedPlusToggled = config.getBoolean("Swords.Bleed+.IsToggled");
        Boolean isDeeperWoundToggled = config.getBoolean("Swords.DeeperWound.IsToggled");
        Boolean isVampireToggled = config.getBoolean("Swords.Vampire.IsToggled");
        Boolean isRageSpikeToggled = config.getBoolean("Swords.RageSpike.IsToggled");
        Boolean isSerratedStrikesToggled = config.getBoolean("Swords.SerratedStrikes.IsToggled");
        Boolean isTaintedBladeToggled = config.getBoolean("Swords.TaintedBlade.IsToggled");
        Integer bleedPlusTier = config.getInt("Swords.Bleed+.Tier");
        Integer deeperWoundTier = config.getInt("Swords.DeeperWound.Tier");
        Integer vampireTier = config.getInt("Swords.Vampire.Tier");
        Integer rageSpikeTier = config.getInt("Swords.RageSpike.Tier");
        Integer serratedStrikesTier = config.getInt("Swords.SerratedStrikes.Tier");
        Integer taintedBladeTier = config.getInt("Swords.TaintedBlade.Tier");
        Long rageSpikeCooldown = 0L;
        if(config.contains("Cooldowns.RageSpike")){
          rageSpikeCooldown = config.getLong("Cooldowns.RageSpike");
        }
        Long serratedStrikesCooldown = 0L;
        if(config.contains("Cooldowns.SerratedStrikes")){
          serratedStrikesCooldown = config.getLong("Cooldowns.SerratedStrikes");
        }
        Long taintedBladeCooldown = 0L;
        if(config.contains("Cooldowns.TaintedBlade")){
          taintedBladeCooldown = config.getLong("Cooldowns.TaintedBlade");
        }
        query = "INSERT INTO mcrpg_swords_data (uuid, current_exp, level, is_bleed_toggled, is_bleed_plus_toggled, is_deeper_wound_toggled, " +
                "is_vampire_toggled, is_rage_spike_toggled, is_serrated_strikes_toggled, is_tainted_blade_toggled, bleed_plus_tier, " +
                "deeper_wound_tier, vampire_tier, rage_spike_tier, serrated_strikes_tier, tainted_blade_tier, rage_spike_cooldown, serrated_strikes_cooldown, tainted_blade_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";
        database.executeQuery(query, uuid.toString(), currentExp.toString(), level.toString(), isBleedToggled.toString(), isBleedPlusToggled.toString(), isDeeperWoundToggled.toString(),
                isVampireToggled.toString(), isRageSpikeToggled.toString(), isSerratedStrikesToggled.toString(), isTaintedBladeToggled.toString(), bleedPlusTier.toString(), deeperWoundTier.toString(),
                vampireTier.toString(), rageSpikeTier.toString(), serratedStrikesTier.toString(), taintedBladeTier.toString(), rageSpikeCooldown.toString(), serratedStrikesCooldown.toString(), taintedBladeCooldown.toString());


        playersProccessed++;
      }
    }
  }
}
