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

        //Convert Mining table
        currentExp = config.getInt("Mining.CurrentExp");
        level = config.getInt("Mining.Level");
        Boolean isDoubleDropToggled = config.getBoolean("Mining.DoubleDrop.IsToggled");
        Boolean isRicherOresToggled = config.getBoolean("Mining.RicherOres.IsToggled");
        Boolean isRemoteTransferToggled = config.getBoolean("Mining.RemoteTransfer.IsToggled");
        Boolean isITsATripleToggled = config.getBoolean("Mining.ItsATriple.IsToggled");
        Boolean isSuperBreakerToggled = config.getBoolean("Mining.SuperBreaker.IsToggled");
        Boolean isBlastMiningToggled = config.getBoolean("Mining.BlastMining.IsToggled");
        Boolean isOreScannerToggled = config.getBoolean("Mining.OreScanner.IsToggled");
        Integer richerOresTier = config.getInt("Mining.RicherOres.Tier");
        Integer remoteTransferTier = config.getInt("Mining.RemoteTransfer.Tier");
        Integer itsATripleTier = config.getInt("Mining.ItsATriple.Tier");
        Integer superBreakerTier = config.getInt("Mining.SuperBreaker.Tier");
        Integer blastMiningTier = config.getInt("Mining.BlastMining.Tier");
        Integer oreScannerTier = config.getInt("Mining.OreScanner.Tier");
        Long superBreakerCooldown = 0L;
        if(config.contains("Cooldowns.SuperBreaker")){
          superBreakerCooldown = config.getLong("Cooldowns.SuperBreaker");
        }
        Long blastMiningCooldown = 0L;
        if(config.contains("Cooldowns.BlastMining")){
          blastMiningCooldown = config.getLong("Cooldowns.BlastMining");
        }
        Long oreScannerCooldown = 0L;
        if(config.contains("Cooldowns.OreScanner")){
          oreScannerCooldown = config.getLong("Cooldowns.OreScanner");
        }

        query = "INSERT INTO mcrpg_mining_data (uuid, current_exp, level, is_double_drop_toggled, is_richer_ores_toggled, " +
                "is_remote_transfer_toggled, is_its_a_triple_toggled, is_super_breaker_toggled, is_blast_mining_toggled, " +
                "is_ore_scanner_toggled, richer_ores_tier, remote_transfer_tier, its_a_triple_tier, super_breaker_tier, " +
                "blast_mining_tier, ore_scanner_tier, super_break_cooldown, blast_mining_cooldown, ore_scanner_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        database.executeQuery(query, uuid.toString(), currentExp.toString(), level.toString(), isDoubleDropToggled.toString(), isRicherOresToggled.toString(), isRemoteTransferToggled.toString(),
                isITsATripleToggled.toString(), isSuperBreakerToggled.toString(), isBlastMiningToggled.toString(), isOreScannerToggled.toString(), richerOresTier.toString(), remoteTransferTier.toString(),
                itsATripleTier.toString(), superBreakerTier.toString(), blastMiningTier.toString(), oreScannerTier.toString(), superBreakerCooldown.toString(), blastMiningCooldown.toString(), oreScannerCooldown.toString());

        //Convert Unarmed table
        currentExp = config.getInt("Unarmed.CurrentExp");
        level = config.getInt("Unarmed.Level");
        Boolean isStickyFingersToggled = config.getBoolean("Unarmed.StickyFingers.IsToggled");
        Boolean isTighterGripToggled = config.getBoolean("Unarmed.TighterGrip.IsToggled");
        Boolean isDisarmToggled = config.getBoolean("Unarmed.Disarm.IsToggled");
        Boolean isIronArmToggled = config.getBoolean("Unarmed.IronArm.IsToggled");
        Boolean isBerserkToggled = config.getBoolean("Unarmed.Berserk.IsToggled");
        Boolean isSmitingFistToggled = config.getBoolean("Unarmed.SmitingFist.IsToggled");
        Boolean isDenseImpactToggled = config.getBoolean("Unarmed.DenseImpact.IsToggled");
        Integer tighterGripTier = config.getInt("Unarmed.TighterGrip.Tier");
        Integer disarmTier = config.getInt("Unarmed.Disarm.Tier");
        Integer ironArmTier = config.getInt("Unarmed.IronArm.Tier");
        Integer berserkTier = config.getInt("Unarmed.Berserk.Tier");
        Integer smitingFistTier = config.getInt("Unarmed.SmitingFist.Tier");
        Integer denseImpactTier = config.getInt("Unarmed.DenseImpact.Tier");
        Long berserkCooldown = 0L;
        if(config.contains("Cooldowns.Berserk")){
          berserkCooldown = config.getLong("Cooldowns.Berserk");
        }
        Long smitingFistCooldown = 0L;
        if(config.contains("Cooldowns.SmitingFist")){
          smitingFistCooldown = config.getLong("Cooldowns.SmitingFist");
        }
        Long denseImpactCooldown = 0L;
        if(config.contains("Cooldowns.DenseImpact")){
          denseImpactCooldown = config.getLong("Cooldowns.DenseImpact");
        }

        query = "INSERT INTO mcrpg_unarmed_data (uuid, current_exp, level, is_sticky_fingers_toggled, is_tighter_grip_toggled, " +
                "is_disarm_toggled, is_iron_arm_toggled, is_berserk_toggled, is_smiting_fist_toggled, " +
                "is_dense_impact_toggled, tighter_grip_tier, disarm_tier, iron_arm_tier, berserk_tier, " +
                "smiting_fist_tier, dense_impact_tier, berserk_cooldown, smiting_fist_cooldown, dense_impact_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        database.executeQuery(query, uuid.toString(), currentExp.toString(), level.toString(), isStickyFingersToggled.toString(), isTighterGripToggled.toString(), isDisarmToggled.toString(),
                isIronArmToggled.toString(), isBerserkToggled.toString(), isSmitingFistToggled.toString(), isDenseImpactToggled.toString(), tighterGripTier.toString(), disarmTier.toString(),
                ironArmTier.toString(), berserkTier.toString(), smitingFistTier.toString(), denseImpactTier.toString(), berserkCooldown.toString(), smitingFistCooldown.toString(), denseImpactCooldown.toString());


        playersProccessed++;
      }
    }
  }
}
