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

        //TODO convert pending abilities but waiting on Ethan
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

        //Convert Herbalism table
        currentExp = config.getInt("Herbalism.CurrentExp");
        level = config.getInt("Herbalism.Level");
        Boolean isTooManyPlantsToggled = config.getBoolean("Herbalism.TooManyPlants.IsToggled");
        Boolean isFarmersDietToggled = config.getBoolean("Herbalism.FarmersDiet.IsToggled");
        Boolean isDiamondFlowersToggled = config.getBoolean("Herbalism.DiamondFlowers.IsToggled");
        Boolean isReplantingToggled = config.getBoolean("Herbalism.Replanting.IsToggled");
        Boolean isMassHarvestToggled = config.getBoolean("Herbalism.MassHarvest.IsToggled");
        Boolean isNaturesWrathToggled = config.getBoolean("Herbalism.NaturesWrath.IsToggled");
        Boolean isPansBlessingToggled = config.getBoolean("Herbalism.PansBlessing.IsToggled");
        Integer farmersDietTier = config.getInt("Herbalism.FarmersDiet.Tier");
        Integer diamondFlowersTier = config.getInt("Herbalism.DiamondFlowers.Tier");
        Integer replantingTier = config.getInt("Herbalism.Replanting.Tier");
        Integer massHarvestTier = config.getInt("Herbalism.MassHarvest.Tier");
        Integer naturesWrathTier = config.getInt("Herbalism.NaturesWrath.Tier");
        Integer pansBlessingTier = config.getInt("Herbalism.DenseImpact.Tier");
        Long massHarvestCooldown = 0L;
        if(config.contains("Cooldowns.MassHarvest")){
          massHarvestCooldown = config.getLong("Cooldowns.MassHarvest");
        }
        Long naturesWrathCooldown = 0L;
        if(config.contains("Cooldowns.NaturesWrath")){
          naturesWrathCooldown = config.getLong("Cooldowns.NaturesWrath");
        }
        Long pansBlessingCooldown = 0L;
        if(config.contains("Cooldowns.PansBlessing")){
          pansBlessingCooldown = config.getLong("Cooldowns.PansBlessing");
        }

        query = "INSERT INTO mcrpg_herbalism_data (uuid, current_exp, level, is_too_many_plants_toggled, is_farmers_diet_toggled, " +
                "is_diamond_flowers_toggled, is_replanting_toggled, is_mass_harvest_toggled, is_natures_wrath_toggled, " +
                "is_pans_blessing_toggled, famers_diet_tier, diamond_flowers_tier, replanting_tier, mass_harvest_tier, " +
                "natures_wrath_tier, pans_blessing_tier, mass_harvest_cooldown, natures_wrath_cooldown, pans_blessing_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        database.executeQuery(query, uuid.toString(), currentExp.toString(), level.toString(), isTooManyPlantsToggled.toString(), isFarmersDietToggled.toString(), isDiamondFlowersToggled.toString(),
                isReplantingToggled.toString(), isMassHarvestToggled.toString(), isNaturesWrathToggled.toString(), isPansBlessingToggled.toString(), farmersDietTier.toString(), diamondFlowersTier.toString(),
                replantingTier.toString(), massHarvestTier.toString(), naturesWrathTier.toString(), pansBlessingTier.toString(), massHarvestCooldown.toString(), naturesWrathCooldown.toString(), pansBlessingCooldown.toString());

        //Convert Archery table
        currentExp = config.getInt("Archery.CurrentExp");
        level = config.getInt("Archery.Level");
        Boolean isDazedToggled = config.getBoolean("Archery.Daze.IsToggled");
        Boolean isPunctureToggled = config.getBoolean("Archery.Puncture.IsToggled");
        Boolean isTippedArrowsToggled = config.getBoolean("Archery.TippedArrows.IsToggled");
        Boolean isComboToggled = config.getBoolean("Archery.Combo.IsToggled");
        Boolean isBlessingOfArtemisToggled = config.getBoolean("Archery.BlessingOfArtemis.IsToggled");
        Boolean isBlessingOfApolloToggled = config.getBoolean("Archery.BlessingOfApollo.IsToggled");
        Boolean isCurseOfHadesToggled = config.getBoolean("Archery.CurseOfHades.IsToggled");
        Integer punctureTier = config.getInt("Archery.Puncture.Tier");
        Integer tippedArrowsTier = config.getInt("Archery.TippedArrows.Tier");
        Integer comboTier = config.getInt("Archery.Combo.Tier");
        Integer blessingOfArtemisTier = config.getInt("Archery.BlessingOfArtemis.Tier");
        Integer blessingOfApolloTier = config.getInt("Archery.BlessingOfApollo.Tier");
        Integer curseOfHadesTier = config.getInt("Archery.CurseOfHades.Tier");
        Long blessingOfArtemisCooldown = 0L;
        if(config.contains("Cooldowns.BlessingOfArtemis")){
          blessingOfArtemisCooldown = config.getLong("Cooldowns.BlessingOfArtemis");
        }
        Long blessingOfApolloCooldown = 0L;
        if(config.contains("Cooldowns.BlessingOfApollo")){
          blessingOfApolloCooldown = config.getLong("Cooldowns.BlessingOfApollo");
        }
        Long curseOfHadesCooldown = 0L;
        if(config.contains("Cooldowns.CurseOfHades")){
          curseOfHadesCooldown = config.getLong("Cooldowns.CurseOfHades");
        }

        query = "INSERT INTO mcrpg_archery_data (uuid, current_exp, level, is_daze_toggled, is_puncture_toggled, " +
                "is_tipped_arrows_toggled, is_combo_toggled, is_blessing_of_artemis_toggled, is_blessing_of_apollo_toggled, " +
                "is_curse_of_hades_toggled, puncture_tier, tipped_arrows_tier, combo_tier, blessing_of_artemis_tier, " +
                "blessing_of_apollo_tier, pans_blessing_tier, mass_harvest_cooldown, natures_wrath_cooldown, pans_blessing_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        database.executeQuery(query, uuid.toString(), currentExp.toString(), level.toString(), isDazedToggled.toString(), isPunctureToggled.toString(), isTippedArrowsToggled.toString(),
                isComboToggled.toString(), isBlessingOfArtemisToggled.toString(), isBlessingOfApolloToggled.toString(), isCurseOfHadesToggled.toString(), punctureTier.toString(), tippedArrowsTier.toString(),
                comboTier.toString(), blessingOfArtemisTier.toString(), blessingOfApolloTier.toString(), curseOfHadesTier.toString(), blessingOfArtemisCooldown.toString(), blessingOfApolloCooldown.toString(), curseOfHadesCooldown.toString());
        playersProccessed++;
      }
    }
    long diff = cal.getTimeInMillis() - beginTime;
    int sec = (int) diff/1000;
    Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.ConversionComplete").replace("%Amount%", Integer.toString(playersProccessed)
    .replace("%Seconds%", Integer.toString(sec)))));
  }
}
