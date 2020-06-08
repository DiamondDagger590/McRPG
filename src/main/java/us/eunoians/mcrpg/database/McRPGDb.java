package us.eunoians.mcrpg.database;

import com.cyr1en.flatdb.Database;
import com.cyr1en.flatdb.DatabaseBuilder;
import com.cyr1en.flatdb.util.FastStrings;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.database.tables.LoadoutInstrumentation;
import us.eunoians.mcrpg.database.tables.PlayerData;
import us.eunoians.mcrpg.database.tables.PlayerSetting;
import us.eunoians.mcrpg.database.tables.skills.*;

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class McRPGDb {

  private McRPG instance;
  @Getter private Database database;

  public McRPGDb(McRPG plugin) {
    this.instance = plugin;

    Class generated = new LoadoutInstrumentation(instance,
            plugin.getFileManager().getFile(FileManager.Files.CONFIG).getInt("PlayerConfiguration.AmountOfTotalAbilities")).instrument();
    //printClass(generated);
    DatabaseBuilder dbBuilder = new DatabaseBuilder();
    dbBuilder.setDatabasePrefix("mcrpg_");
    dbBuilder.setPath(plugin.getDataFolder().getAbsolutePath() + "/database/mcrpg");
    dbBuilder.appendTable(PlayerData.class);
    dbBuilder.appendTable(PlayerSetting.class);
    dbBuilder.appendTable(ArcheryTable.class);
    dbBuilder.appendTable(HerbalismTable.class);
    dbBuilder.appendTable(WoodcuttingTable.class);
    dbBuilder.appendTable(MiningTable.class);
    dbBuilder.appendTable(SwordsTable.class);
    dbBuilder.appendTable(UnarmedTable.class);
    dbBuilder.appendTable(FitnessTable.class);
    dbBuilder.appendTable(ExcavationTable.class);
    dbBuilder.appendTable(AxesTable.class);
    dbBuilder.appendTable(FishingTable.class);
    dbBuilder.appendTable(SorceryTable.class);
    dbBuilder.appendTable(TamingTable.class);

    dbBuilder.appendTable(generated);
    try {
      database = dbBuilder.build();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    //listAllTable();
    //Bukkit.getLogger().info("Does generated table exist?: " + database.tableExists("mcrpg_loadout"));
  }

  private void listAllTable() {
    try {
      DatabaseMetaData meta = database.getMetaData().orElse(null);
      if (meta == null) return;
      ResultSet rs = meta.getTables(null, null, null, new String[]{"TABLE"});
      while (rs.next())
        Bukkit.getLogger().info(rs.getString("TABLE_NAME"));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void convertLegacyToFlatDB() {
    File playerFolder = new File(instance.getDataFolder(), File.separator + "PlayerData");
    if (!playerFolder.exists()) {
      Bukkit.getConsoleSender().sendMessage(Methods.color(instance.getPluginPrefix() + instance.getLangFile().getString("Messages.Utility.PlayerFolderDoesntExist")));
      return;
    }
    File[] playerFiles = playerFolder.listFiles();
    int playersProccessed = 0;
    Instant start = Instant.now();
    if (playerFiles != null) {
      Bukkit.getConsoleSender().sendMessage(Methods.color(instance.getPluginPrefix() + instance.getLangFile().getString("Messages.Commands.Utility.BeginningConversion")
              .replace("%FileAmount%", Integer.toString(playerFiles.length))));
      LegacyDataConverter converter = new LegacyDataConverter();
      for (File f : playerFiles) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(f);

        UUID uuid = UUID.fromString(f.getName().replace(".yml", ""));
        //Convert the player data table
        ImmutableMap.Builder<String, String> dataBuilder = new ImmutableMap.Builder<>();
        dataBuilder.put("uuid", uuid.toString());
        dataBuilder.put("ability_points", Integer.toString(config.getInt("AbilityPoints")));
        dataBuilder.put("redeemable_exp", Integer.toString(config.getInt("RedeemableExp")));
        dataBuilder.put("redeemable_levels", Integer.toString(config.getInt("RedeemableLevels")));
        converter.convert("mcrpg_player_data", dataBuilder.build());

        //Convert player settings table
        dataBuilder = new ImmutableMap.Builder<>();
        dataBuilder.put("uuid", uuid.toString());
        dataBuilder.put("keep_hand", String.valueOf(config.getBoolean("KeepHandEmpty")));
        dataBuilder.put("ignore_tips", String.valueOf(config.getBoolean("IgnoreTips")));
        dataBuilder.put("auto_deny", String.valueOf(config.getBoolean("AutoDeny")));
        dataBuilder.put("display_type", config.getString("DisplayType"));
        dataBuilder.put("health_type", config.getString("HealthType"));
        converter.convert("mcrpg_player_settings", dataBuilder.build());

        //Convert ability load out table
        List<String> abilityLoadOut = config.getStringList("AbilityLoadout");
        dataBuilder = new ImmutableMap.Builder<>();
        for (int i = 1; i <= abilityLoadOut.size(); i++) {
          dataBuilder.put("slot" + i, abilityLoadOut.get(i - 1));
        }
        converter.convert("mcrpg_loadout", dataBuilder.build());

        //Convert Swords table
        Integer currentExp = config.getInt("Swords.CurrentExp");
        Integer level = config.getInt("Swords.current_level");
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
        if (config.contains("Cooldowns.RageSpike")) {
          rageSpikeCooldown = config.getLong("Cooldowns.RageSpike");
        }
        Long serratedStrikesCooldown = 0L;
        if (config.contains("Cooldowns.SerratedStrikes")) {
          serratedStrikesCooldown = config.getLong("Cooldowns.SerratedStrikes");
        }
        Long taintedBladeCooldown = 0L;
        if (config.contains("Cooldowns.TaintedBlade")) {
          taintedBladeCooldown = config.getLong("Cooldowns.TaintedBlade");
        }
        String query = "INSERT INTO mcrpg_swords_data (uuid, current_exp, current_level, is_bleed_toggled, is_bleed_plus_toggled, is_deeper_wound_toggled, " +
                "is_vampire_toggled, is_rage_spike_toggled, is_serrated_strikes_toggled, is_tainted_blade_toggled, bleed_plus_tier, " +
                "deeper_wound_tier, vampire_tier, rage_spike_tier, serrated_strikes_tier, tainted_blade_tier, rage_spike_cooldown, serrated_strikes_cooldown, tainted_blade_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";
        try {
          database.getConnection().createStatement().execute(String.format(query, "'" + uuid.toString() + "'", currentExp.toString(), level.toString(), isBleedToggled.toString(), isBleedPlusToggled.toString(), isDeeperWoundToggled.toString(),
                  isVampireToggled.toString(), isRageSpikeToggled.toString(), isSerratedStrikesToggled.toString(), isTaintedBladeToggled.toString(), bleedPlusTier.toString(), deeperWoundTier.toString(),
                  vampireTier.toString(), rageSpikeTier.toString(), serratedStrikesTier.toString(), taintedBladeTier.toString(), rageSpikeCooldown.toString(), serratedStrikesCooldown.toString(), taintedBladeCooldown.toString()));
        } catch (SQLException e) {
          e.printStackTrace();
        }

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
        if (config.contains("Cooldowns.SuperBreaker")) {
          superBreakerCooldown = config.getLong("Cooldowns.SuperBreaker");
        }
        Long blastMiningCooldown = 0L;
        if (config.contains("Cooldowns.BlastMining")) {
          blastMiningCooldown = config.getLong("Cooldowns.BlastMining");
        }
        Long oreScannerCooldown = 0L;
        if (config.contains("Cooldowns.OreScanner")) {
          oreScannerCooldown = config.getLong("Cooldowns.OreScanner");
        }
        query = "INSERT INTO mcrpg_mining_data (uuid, current_exp, current_level, is_double_drop_toggled, is_richer_ores_toggled, " +
                "is_remote_transfer_toggled, is_its_a_triple_toggled, is_super_breaker_toggled, is_blast_mining_toggled, " +
                "is_ore_scanner_toggled, richer_ores_tier, remote_transfer_tier, its_a_triple_tier, super_breaker_tier, " +
                "blast_mining_tier, ore_scanner_tier, super_break_cooldown, blast_mining_cooldown, ore_scanner_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        try {
          database.getConnection().createStatement().execute(String.format(query, "'" + uuid.toString() + "'", currentExp.toString(), level.toString(), isDoubleDropToggled.toString(), isRicherOresToggled.toString(), isRemoteTransferToggled.toString(),
                  isITsATripleToggled.toString(), isSuperBreakerToggled.toString(), isBlastMiningToggled.toString(), isOreScannerToggled.toString(), richerOresTier.toString(), remoteTransferTier.toString(),
                  itsATripleTier.toString(), superBreakerTier.toString(), blastMiningTier.toString(), oreScannerTier.toString(), superBreakerCooldown.toString(), blastMiningCooldown.toString(), oreScannerCooldown.toString()));
        } catch (SQLException e) {
          e.printStackTrace();
        }

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
        if (config.contains("Cooldowns.Berserk")) {
          berserkCooldown = config.getLong("Cooldowns.Berserk");
        }
        Long smitingFistCooldown = 0L;
        if (config.contains("Cooldowns.SmitingFist")) {
          smitingFistCooldown = config.getLong("Cooldowns.SmitingFist");
        }
        Long denseImpactCooldown = 0L;
        if (config.contains("Cooldowns.DenseImpact")) {
          denseImpactCooldown = config.getLong("Cooldowns.DenseImpact");
        }

        query = "INSERT INTO mcrpg_unarmed_data (uuid, current_exp, current_level, is_sticky_fingers_toggled, is_tighter_grip_toggled, " +
                "is_disarm_toggled, is_iron_arm_toggled, is_berserk_toggled, is_smiting_fist_toggled, " +
                "is_dense_impact_toggled, tighter_grip_tier, disarm_tier, iron_arm_tier, berserk_tier, " +
                "smiting_fist_tier, dense_impact_tier, berserk_cooldown, smiting_fist_cooldown, dense_impact_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        try {
          database.getConnection().createStatement().execute(String.format(query, "'" + uuid.toString() + "'", currentExp.toString(), level.toString(), isStickyFingersToggled.toString(), isTighterGripToggled.toString(), isDisarmToggled.toString(),
                  isIronArmToggled.toString(), isBerserkToggled.toString(), isSmitingFistToggled.toString(), isDenseImpactToggled.toString(), tighterGripTier.toString(), disarmTier.toString(),
                  ironArmTier.toString(), berserkTier.toString(), smitingFistTier.toString(), denseImpactTier.toString(), berserkCooldown.toString(), smitingFistCooldown.toString(), denseImpactCooldown.toString()));
        } catch (SQLException e) {
          e.printStackTrace();
        }

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
        if (config.contains("Cooldowns.MassHarvest")) {
          massHarvestCooldown = config.getLong("Cooldowns.MassHarvest");
        }
        Long naturesWrathCooldown = 0L;
        if (config.contains("Cooldowns.NaturesWrath")) {
          naturesWrathCooldown = config.getLong("Cooldowns.NaturesWrath");
        }
        Long pansBlessingCooldown = 0L;
        if (config.contains("Cooldowns.PansBlessing")) {
          pansBlessingCooldown = config.getLong("Cooldowns.PansBlessing");
        }

        query = "INSERT INTO mcrpg_herbalism_data (uuid, current_exp, current_level, is_too_many_plants_toggled, is_farmers_diet_toggled, " +
                "is_diamond_flowers_toggled, is_replanting_toggled, is_mass_harvest_toggled, is_natures_wrath_toggled, " +
                "is_pans_blessing_toggled, farmers_diet_tier, diamond_flowers_tier, replanting_tier, mass_harvest_tier, " +
                "natures_wrath_tier, pans_blessing_tier, mass_harvest_cooldown, natures_wrath_cooldown, pans_blessing_cooldown) " +
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)";

        try {
          database.getConnection().createStatement().execute(String.format(query, "'" + uuid.toString() + "'", currentExp.toString(), level.toString(), isTooManyPlantsToggled.toString(), isFarmersDietToggled.toString(), isDiamondFlowersToggled.toString(),
                  isReplantingToggled.toString(), isMassHarvestToggled.toString(), isNaturesWrathToggled.toString(), isPansBlessingToggled.toString(), farmersDietTier.toString(), diamondFlowersTier.toString(),
                  replantingTier.toString(), massHarvestTier.toString(), naturesWrathTier.toString(), pansBlessingTier.toString(), massHarvestCooldown.toString(), naturesWrathCooldown.toString(), pansBlessingCooldown.toString()));
        } catch (SQLException e) {
          e.printStackTrace();
        }

        //Convert Archery table
        dataBuilder = new ImmutableMap.Builder<>();
        dataBuilder.put("uuid", uuid.toString());
        dataBuilder.put("current_exp", Integer.toString(config.getInt("Archery.CurrentExp")));
        dataBuilder.put("current_level", Integer.toString(config.getInt("Archery.Level")));
        dataBuilder.put("is_daze_toggled", Boolean.toString(config.getBoolean("Archery.Daze.IsToggled")));
        dataBuilder.put("is_puncture_toggled", Boolean.toString(config.getBoolean("Archery.Puncture.IsToggled")));
        dataBuilder.put("is_tipped_arrows_toggled", Boolean.toString(config.getBoolean("Archery.TippedArrows.IsToggled")));
        dataBuilder.put("is_blessing_of_artemis_toggled", Boolean.toString(config.getBoolean("Archery.BlessingOfArtemis.IsToggled")));
        dataBuilder.put("is_blessing_of_apollo_toggled", Boolean.toString(config.getBoolean("Archery.BlessingOfApollo.IsToggled")));
        dataBuilder.put("is_curse_of_hades_toggled", Boolean.toString(config.getBoolean("Archery.CurseOfHades.IsToggled")));

        dataBuilder.put("puncture_tier", Integer.toString(config.getInt("Archery.Puncture.Tier")));
        dataBuilder.put("tipped_arrows_tier", Integer.toString(config.getInt("Archery.TippedArrows.Tier")));
        dataBuilder.put("combo_tier", Integer.toString(config.getInt("Archery.Combo.Tier")));
        dataBuilder.put("blessing_of_artemis_tier", Integer.toString(config.getInt("Archery.BlessingOfArtemis.Tier")));
        dataBuilder.put("blessing_of_apollo_tier", Integer.toString(config.getInt("Archery.BlessingOfApollo.Tier")));
        dataBuilder.put("curse_of_hades_tier", Integer.toString(config.getInt("Archery.CurseOfHades.Tier")));

        Long blessingOfArtemisCooldown = config.contains("Cooldowns.BlessingOfArtemis") ? config.getLong("Cooldowns.BlessingOfArtemis") : 0L;
        Long blessingOfApolloCooldown = config.contains("Cooldowns.BlessingOfApollo") ? config.getLong("Cooldowns.BlessingOfApollo") : 0L;
        Long curseOfHadesCooldown = config.contains("Cooldowns.CurseOfHades") ? config.getLong("Cooldowns.CurseOfHades") : 0L;
        dataBuilder.put("blessing_of_artemis_cooldown", Long.toString(blessingOfArtemisCooldown));
        dataBuilder.put("blessing_of_apollo_cooldown", Long.toString(blessingOfApolloCooldown));
        dataBuilder.put("curse_of_hades_cooldown", Long.toString(curseOfHadesCooldown));
        converter.convert("mcrpg_archery_data", dataBuilder.build());

        f.delete();
        playersProccessed++;
      }
    }
    long diffInSec = Duration.between(start, Instant.now()).getSeconds();
    playerFolder.delete();
    Bukkit.getConsoleSender().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.ConversionComplete").replace("%Amount%", Integer.toString(playersProccessed))
            .replace("%Seconds%", Long.toString(diffInSec))));
  }

  private class LegacyDataConverter {

    private Function<String, String> prepareValuesFunc;

    public LegacyDataConverter() {
      prepareValuesFunc = s -> {
        if (s.equals("true") || s.equals("false"))
          return Boolean.getBoolean(s) ? "1" : "0";
        if (FastStrings.isNumeric(s))
          return s;
        return "'" + s + "'";
      };
    }

    private String buildSql(String tableName, Map<String, String> data) {
      Set<String> keys = data.keySet();
      String joinedKeys = FastStrings.join(keys.toArray(), ", ");
      String joinedValues = FastStrings.join(data.values().stream().map(prepareValuesFunc).toArray(), ", ");
      return String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, joinedKeys, joinedValues);
    }

    public void convert(String tableName, Map<String, String> data) {
      String sql = buildSql(tableName, data);
      database.executeUpdate(sql);
    }
  }
}
