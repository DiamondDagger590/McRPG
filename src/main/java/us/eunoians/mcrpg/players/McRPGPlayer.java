package us.eunoians.mcrpg.players;

import com.cyr1en.flatdb.Database;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Language;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.archery.*;
import us.eunoians.mcrpg.abilities.herbalism.*;
import us.eunoians.mcrpg.abilities.mining.*;
import us.eunoians.mcrpg.abilities.swords.*;
import us.eunoians.mcrpg.abilities.unarmed.*;
import us.eunoians.mcrpg.abilities.woodcutting.*;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.SmitingFistEvent;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.skills.*;
import us.eunoians.mcrpg.types.*;
import us.eunoians.mcrpg.util.mcmmo.MobHealthbarUtils;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class McRPGPlayer {

  /**
   * The UUID of the player
   */
  @Getter
  private UUID uuid;
  /**
   * The power level of a player. The total sum of all of the players skill levels
   */
  @Getter
  private int powerLevel;
  /**
   * The amount of ability points a player has for upgrading their abilities.
   */
  @Getter
  @Setter
  private int abilityPoints;
  /**
   * The array of skills for the player
   */
  private ArrayList<Skill> skills = new ArrayList<>();
  /**
   * The abilities a player has unlocked and has not yet accepted or denied. Whenever a player next opens the mcrpg main gui they should be forced to go through these
   */
  @Getter
  private ArrayList<UnlockedAbilities> pendingUnlockAbilities = new ArrayList<>();

  /**
   * Map of the abilities on cooldown
   */
  private HashMap<UnlockedAbilities, Long> abilitiesOnCooldown = new HashMap<>();

  /**
   * Boolean of if the player should be immune from bleed
   */
  @Getter
  @Setter
  private boolean hasBleedImmunity = false;

  @Getter
  @Setter
  private boolean hasDazeImmunity = false;

  /**
   * The players current ability loadout
   */
  @Getter
  private ArrayList<UnlockedAbilities> abilityLoadout = new ArrayList<>();

  /**
   * The kind of display the player currently has
   */
  @Getter
  @Setter
  private DisplayType displayType = DisplayType.SCOREBOARD;

  /**
   * If the player is readying for an active ability
   */
  @Getter
  @Setter
  private boolean isReadying = false;

  /**
   * Contains info about what ability is being readied
   */
  @Getter
  @Setter
  private PlayerReadyBit readyingAbilityBit = null;

  /**
   * Boolean to check if the player is linked to remote transfer
   */
  @Getter
  @Setter
  private boolean isLinkedToRemoteTransfer = false;

  /**
   * Boolean to check if the player can smite
   */
  @Getter
  @Setter
  private boolean canSmite;

  /**
   * Object containing info about SmitingFist
   */
  @Getter
  @Setter
  private SmitingFistEvent smitingFistData;

  /**
   * Boolean if the player can use dense impact
   */
  @Getter
  @Setter
  private boolean canDenseImpact;

  /**
   * How much dmg Dense Impact should do
   */
  @Getter
  @Setter
  private int armourDmg;

  /**
   * The type of health display when a player hits a mob
   */
  @Getter
  @Setter
  private MobHealthbarUtils.MobHealthbarType healthbarType = MobHealthbarUtils.MobHealthbarType.BAR;

  /**
   * The end time for when a players replace ability cooldown should expire
   */
  @Getter
  @Setter
  private long endTimeForReplaceCooldown;

  /**
   * If an empty hand should be kept as such
   */
  @Getter
  @Setter
  private boolean keepHandEmpty = false;

  /**
   * If abilities should be auto denied
   */
  @Getter
  @Setter
  private boolean autoDeny = false;
  /**
   * If tips shouldnt be sent to player
   */
  @Getter
  @Setter
  private boolean ignoreTips;

  /**
   * Current active abilities
   */
  @Getter
  private ArrayList<UnlockedAbilities> activeAbilities = new ArrayList<>();

  /**
   * Tips that a player has already had displayed to them
   */
  @Getter
  private HashSet<TipType> usedTips = new HashSet<>();

  @Getter
  @Setter
  private int redeemableExp;

  @Getter
  @Setter
  private int redeemableLevels;

  @Getter
  @Setter
  private boolean listenForCustomExpInput = false;

  @Getter
  @Setter
  private RedeemBit redeemBit;

  @Getter
  @Setter
  private double guardianSummonChance;

  @Getter
  @Setter
  private Location lastFishCaughtLoc = null;

  @Getter
  @Setter
  private double divineEscapeExpDebuff;

  @Getter
  @Setter
  private double divineEscapeDamageDebuff;



  public McRPGPlayer(UUID uuid) {
    this.uuid = uuid;
    this.guardianSummonChance = McRPG.getInstance().getConfig().getDouble("PlayerConfiguration.PoseidonsGuardian.DefaultSummonChance");
    Database database = McRPG.getInstance().getMcRPGDb().getDatabase();
    Optional<ResultSet> playerDataSet = database.executeQuery("SELECT * FROM mcrpg_player_data WHERE uuid = '" + uuid.toString() + "'");

    boolean isNew = false;
    try {
      if(playerDataSet.isPresent()) {
        isNew = !playerDataSet.get().next();
      }
      else {
        isNew = true;
      }
    } catch(SQLException e) {
      e.printStackTrace();
    }
    if(isNew) {
      for(Skills type : Skills.values()) {
        @Language("SQL") String query = "INSERT INTO mcrpg_" + type.getName() + "_data (uuid) VALUES ('" + uuid.toString() + "')";
        database.executeUpdate(query);
      }
      @Language("SQL") String query = "INSERT INTO MCRPG_PLAYER_SETTINGS (UUID) VALUES ('" + uuid.toString() + "')";
      database.executeUpdate(query);
      query = "INSERT INTO MCRPG_PLAYER_DATA (UUID) VALUES ('" + uuid.toString() + "')";
      database.executeUpdate(query);
      query = "INSERT INTO MCRPG_LOADOUT (UUID) VALUES ('" + uuid.toString() + "')";
      database.executeUpdate(query);
      playerDataSet = database.executeQuery("SELECT * FROM mcrpg_player_data WHERE uuid = '" + uuid.toString() + "'");
      try {
        playerDataSet.get().next();
      } catch(SQLException e) {
        e.printStackTrace();
      }
    }
    playerDataSet.ifPresent(resultSet -> {
      try {
        //if(resultSet.next()) {
          this.abilityPoints = resultSet.getInt("ability_points");
          this.redeemableExp = resultSet.getInt("redeemable_exp");
          this.redeemableLevels = resultSet.getInt("redeemable_levels");
          int replaceCooldown = resultSet.getInt("replace_ability_cooldown");
          if(replaceCooldown > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, replaceCooldown);
            this.endTimeForReplaceCooldown = cal.getTimeInMillis();
          //}
        }
      } catch(SQLException e) {
        e.printStackTrace();
      }
    });

    final Optional<ResultSet> settingsSet = database.executeQuery("SELECT * FROM mcrpg_player_settings WHERE uuid = '" + uuid.toString() + "'");
    settingsSet.ifPresent(rs -> {
      try {
        if(rs.next()) {
          this.healthbarType = MobHealthbarUtils.MobHealthbarType.fromString(rs.getString("health_type"));
          this.keepHandEmpty = rs.getBoolean("keep_hand");
          this.displayType = DisplayType.fromString(rs.getString("display_type"));
          this.autoDeny = rs.getBoolean("auto_deny");
          this.ignoreTips = rs.getBoolean("ignore_tips");
        }
      } catch(SQLException e) {
        e.printStackTrace();
      }
    });

    //Initialize skills
    Arrays.stream(Skills.values()).forEach(skill -> {
      HashMap<GenericAbility, BaseAbility> abilityMap = new HashMap<>();
      Optional<ResultSet> skillSet = database.executeQuery("SELECT * FROM mcrpg_" + skill.getName().toLowerCase() + "_data WHERE uuid = '" + uuid.toString() + "'");
      try {
        if(!skillSet.isPresent() || !skillSet.get().next()) {
          @Language("SQL") String query = "INSERT INTO mcrpg_" + skill.getName().toLowerCase() + "_data (uuid) VALUES ('" + uuid.toString() + "')";
          database.executeUpdate(query);
          skillSet = database.executeQuery("SELECT * FROM mcrpg_" + skill.getName().toLowerCase() + "_data WHERE uuid = '" + uuid.toString() + "'");
          skillSet.get().next();
        }
      } catch(SQLException e) {
        e.printStackTrace();
      }
      skillSet.ifPresent(rs -> {
        try {
          //if(rs.next()) {
          if(skill.equals(Skills.SWORDS)) {
            //Initialize bleed
            Bleed bleed = new Bleed();
            bleed.setToggled(rs.getBoolean("is_bleed_toggled"));
            //Initialize Deeper Wound
            DeeperWound deeperWound = new DeeperWound();
            deeperWound.setToggled(rs.getBoolean("is_deeper_wound_toggled"));
            deeperWound.setCurrentTier(rs.getInt("deeper_wound_tier"));
            if(deeperWound.getCurrentTier() != 0) {
              deeperWound.setUnlocked(true);
            }
            //Initialize Bleed+
            BleedPlus bleedPlus = new BleedPlus();
            bleedPlus.setToggled(rs.getBoolean("is_bleed_plus_toggled"));
            bleedPlus.setCurrentTier(rs.getInt("bleed_plus_tier"));
            if(bleedPlus.getCurrentTier() != 0) {
              bleedPlus.setUnlocked(true);
            }
            //Initialize Vampire
            Vampire vampire = new Vampire();
            vampire.setToggled(rs.getBoolean("is_vampire_toggled"));
            vampire.setCurrentTier(rs.getInt("vampire_tier"));
            if(vampire.getCurrentTier() != 0) {
              vampire.setUnlocked(true);
            }
            //Initialize Serrated Strikes
            SerratedStrikes serratedStrikes = new SerratedStrikes();
            serratedStrikes.setToggled(rs.getBoolean("is_serrated_strikes_toggled"));
            serratedStrikes.setCurrentTier(rs.getInt("serrated_strikes_tier"));
            if(serratedStrikes.getCurrentTier() != 0) {
              serratedStrikes.setUnlocked(true);
            }
            //Initialize Rage Spike
            RageSpike rageSpike = new RageSpike();
            rageSpike.setToggled(rs.getBoolean("is_rage_spike_toggled"));
            rageSpike.setCurrentTier(rs.getInt("rage_spike_tier"));
            if(rageSpike.getCurrentTier() != 0) {
              rageSpike.setUnlocked(true);
            }
            //Initialize Tainted Blade
            TaintedBlade taintedBlade = new TaintedBlade();
            taintedBlade.setToggled(rs.getBoolean("is_tainted_blade_toggled"));
            taintedBlade.setCurrentTier(rs.getInt("tainted_blade_tier"));
            if(taintedBlade.getCurrentTier() != 0) {
              taintedBlade.setUnlocked(true);
            }

            int serratedStrikesCooldown = rs.getInt("serrated_strikes_cooldown");
            int rageSpikeCooldown = rs.getInt("rage_spike_cooldown");
            int taintedBladeCooldown = rs.getInt("tainted_blade_cooldown");
            if(serratedStrikesCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, serratedStrikesCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.SERRATED_STRIKES, cal.getTimeInMillis());
            }
            if(rageSpikeCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, rageSpikeCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.RAGE_SPIKE, cal.getTimeInMillis());
            }
            if(taintedBladeCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, taintedBladeCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.TAINTED_BLADE, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_deeper_wound_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DEEPER_WOUND);
            }
            if(rs.getBoolean("is_bleed_plus_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BLEED_PLUS);
            }
            if(rs.getBoolean("is_vampire_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.VAMPIRE);
            }
            if(rs.getBoolean("is_serrated_strikes_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SERRATED_STRIKES);
            }
            if(rs.getBoolean("is_rage_spike_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.RAGE_SPIKE);
            }
            if(rs.getBoolean("is_tainted_blade_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.TAINTED_BLADE);
            }

            abilityMap.put(DefaultAbilities.BLEED, bleed);
            abilityMap.put(UnlockedAbilities.DEEPER_WOUND, deeperWound);
            abilityMap.put(UnlockedAbilities.BLEED_PLUS, bleedPlus);
            abilityMap.put(UnlockedAbilities.VAMPIRE, vampire);
            abilityMap.put(UnlockedAbilities.SERRATED_STRIKES, serratedStrikes);
            abilityMap.put(UnlockedAbilities.RAGE_SPIKE, rageSpike);
            abilityMap.put(UnlockedAbilities.TAINTED_BLADE, taintedBlade);
            //Create skill
            Swords swords = new Swords(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(swords);
          }
          //Init mining
          else if(skill.equals(Skills.MINING)) {
            //Initialize DoubleDrops
            DoubleDrop doubleDrop = new DoubleDrop();
            doubleDrop.setToggled(rs.getBoolean("is_double_drop_toggled"));

            //Initialize RicherOres
            RicherOres richerOres = new RicherOres();
            richerOres.setToggled(rs.getBoolean("is_richer_ores_toggled"));
            richerOres.setCurrentTier(rs.getInt("richer_ores_tier"));
            if(richerOres.getCurrentTier() != 0) {
              richerOres.setUnlocked(true);
            }

            //Initialize ItsATriple
            ItsATriple itsATriple = new ItsATriple();
            itsATriple.setToggled(rs.getBoolean("is_its_a_triple_toggled"));
            itsATriple.setCurrentTier(rs.getInt("its_a_triple_tier"));
            if(itsATriple.getCurrentTier() != 0) {
              itsATriple.setUnlocked(true);
            }

            //Initialize RemoteTransfer
            RemoteTransfer remoteTransfer = new RemoteTransfer();
            remoteTransfer.setToggled(rs.getBoolean("is_remote_transfer_toggled"));
            remoteTransfer.setCurrentTier(rs.getInt("remote_transfer_tier"));
            if(remoteTransfer.getCurrentTier() != 0) {
              remoteTransfer.setUnlocked(true);
              remoteTransfer.updateBlocks();
            }
            if(RemoteTransferTracker.isTracked(uuid)) {
              remoteTransfer.setLinkedChestLocation(RemoteTransferTracker.getLocation(uuid));
            }
            File file = new File(McRPG.getInstance().getDataFolder(), File.separator + "remote_transfer_data" + File.separator + uuid.toString() + ".yml");
            FileConfiguration remoteTransferFile = YamlConfiguration.loadConfiguration(file);
            if(remoteTransferFile.contains("RemoteTransferBlocks")) {
              for(String s : remoteTransferFile.getConfigurationSection("RemoteTransferBlocks").getKeys(false)) {
                remoteTransfer.getItemsToSync().put(Material.getMaterial(s), remoteTransferFile.getBoolean("RemoteTransferBlocks." + s));
              }
            }


            //Initialize SuperBreaker
            SuperBreaker superBreaker = new SuperBreaker();
            superBreaker.setToggled(rs.getBoolean("is_super_breaker_toggled"));
            superBreaker.setCurrentTier(rs.getInt("super_breaker_tier"));
            if(superBreaker.getCurrentTier() != 0) {
              superBreaker.setUnlocked(true);
            }

            //Initialize BlastMining
            BlastMining blastMining = new BlastMining();
            blastMining.setToggled(rs.getBoolean("is_blast_mining_toggled"));
            blastMining.setCurrentTier(rs.getInt("blast_mining_tier"));
            if(blastMining.getCurrentTier() != 0) {
              blastMining.setUnlocked(true);
            }

            //Initilize OreScanner
            OreScanner oreScanner = new OreScanner();
            oreScanner.setToggled(rs.getBoolean("is_ore_scanner_toggled"));
            oreScanner.setCurrentTier(rs.getInt("ore_scanner_tier"));
            if(oreScanner.getCurrentTier() != 0) {
              oreScanner.setUnlocked(true);
            }

            int superBreakerCooldown = rs.getInt("super_break_cooldown");
            int blastMiningCooldown = rs.getInt("blast_mining_cooldown");
            int oreScannerCooldown = rs.getInt("ore_scanner_cooldown");
            if(superBreakerCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, superBreakerCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.SUPER_BREAKER, cal.getTimeInMillis());
            }
            if(blastMiningCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, blastMiningCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.BLAST_MINING, cal.getTimeInMillis());
            }
            if(oreScannerCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, oreScannerCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.ORE_SCANNER, cal.getTimeInMillis());
            }
            abilityMap.put(DefaultAbilities.DOUBLE_DROP, doubleDrop);
            abilityMap.put(UnlockedAbilities.RICHER_ORES, richerOres);
            abilityMap.put(UnlockedAbilities.ITS_A_TRIPLE, itsATriple);
            abilityMap.put(UnlockedAbilities.REMOTE_TRANSFER, remoteTransfer);
            abilityMap.put(UnlockedAbilities.SUPER_BREAKER, superBreaker);
            abilityMap.put(UnlockedAbilities.BLAST_MINING, blastMining);
            abilityMap.put(UnlockedAbilities.ORE_SCANNER, oreScanner);

            if(rs.getBoolean("is_richer_ores_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.RICHER_ORES);
            }
            if(rs.getBoolean("is_its_a_triple_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.ITS_A_TRIPLE);
            }
            if(rs.getBoolean("is_remote_transfer_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.REMOTE_TRANSFER);
            }
            if(rs.getBoolean("is_super_breaker_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SUPER_BREAKER);
            }
            if(rs.getBoolean("is_blast_mining_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BLAST_MINING);
            }
            if(rs.getBoolean("is_ore_scanner_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.ORE_SCANNER);
            }
            Mining mining = new Mining(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(mining);
          }
          //Init unarmed
          else if(skill.equals(Skills.UNARMED)) {
            //Initialize Sticky Fingers
            StickyFingers stickyFingers = new StickyFingers();
            stickyFingers.setToggled(rs.getBoolean("is_sticky_fingers_toggled"));

            //Initialize Tighter Grip
            TighterGrip tighterGrip = new TighterGrip();
            tighterGrip.setToggled(rs.getBoolean("is_tighter_grip_toggled"));
            tighterGrip.setCurrentTier(rs.getInt("tighter_grip_tier"));
            if(tighterGrip.getCurrentTier() != 0) {
              tighterGrip.setUnlocked(true);
            }

            //Initialize Disarm
            Disarm disarm = new Disarm();
            disarm.setToggled(rs.getBoolean("is_disarm_toggled"));
            disarm.setCurrentTier(rs.getInt("disarm_tier"));
            if(disarm.getCurrentTier() != 0) {
              disarm.setUnlocked(true);
            }
            //Initialize Iron Arm
            IronArm ironArm = new IronArm();
            ironArm.setToggled(rs.getBoolean("is_iron_arm_toggled"));
            ironArm.setCurrentTier(rs.getInt("iron_arm_tier"));
            if(ironArm.getCurrentTier() != 0) {
              ironArm.setUnlocked(true);
            }
            //Initialize Berserk
            Berserk berserk = new Berserk();
            berserk.setToggled(rs.getBoolean("is_berserk_toggled"));
            berserk.setCurrentTier(rs.getInt("berserk_tier"));
            if(berserk.getCurrentTier() != 0) {
              berserk.setUnlocked(true);
            }
            //Initialize Smiting Fist
            SmitingFist smitingFist = new SmitingFist();
            smitingFist.setToggled(rs.getBoolean("is_smiting_fist_toggled"));
            smitingFist.setCurrentTier(rs.getInt("smiting_fist_tier"));
            if(smitingFist.getCurrentTier() != 0) {
              smitingFist.setUnlocked(true);
            }
            //Initialize Dense Impact
            DenseImpact denseImpact = new DenseImpact();
            denseImpact.setToggled(rs.getBoolean("is_dense_impact_toggled"));
            denseImpact.setCurrentTier(rs.getInt("dense_impact_tier"));
            if(denseImpact.getCurrentTier() != 0) {
              denseImpact.setUnlocked(true);
            }
            int berserkCooldown = rs.getInt("berserk_cooldown");
            int smitingFistCooldown = rs.getInt("smiting_fist_cooldown");
            int denseImpactCooldown = rs.getInt("dense_impact_cooldown");

            if(berserkCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, berserkCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.BERSERK, cal.getTimeInMillis());
            }
            if(smitingFistCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, smitingFistCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.SMITING_FIST, cal.getTimeInMillis());
            }
            if(denseImpactCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, denseImpactCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.DENSE_IMPACT, cal.getTimeInMillis());
            }
            abilityMap.put(DefaultAbilities.STICKY_FINGERS, stickyFingers);
            abilityMap.put(UnlockedAbilities.TIGHTER_GRIP, tighterGrip);
            abilityMap.put(UnlockedAbilities.DISARM, disarm);
            abilityMap.put(UnlockedAbilities.IRON_ARM, ironArm);
            abilityMap.put(UnlockedAbilities.BERSERK, berserk);
            abilityMap.put(UnlockedAbilities.SMITING_FIST, smitingFist);
            abilityMap.put(UnlockedAbilities.DENSE_IMPACT, denseImpact);

            if(rs.getBoolean("is_tighter_grip_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.TIGHTER_GRIP);
            }
            if(rs.getBoolean("is_disarm_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DISARM);
            }
            if(rs.getBoolean("is_iron_arm_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.IRON_ARM);
            }
            if(rs.getBoolean("is_berserk_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BERSERK);
            }
            if(rs.getBoolean("is_smiting_fist_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SMITING_FIST);
            }
            if(rs.getBoolean("is_dense_impact_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DENSE_IMPACT);
            }
            //Create skill
            Unarmed unarmed = new Unarmed(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(unarmed);
          }
          //Add herbalism
          else if(skill.equals(Skills.HERBALISM)) {
            //Initialize Too Many Plants
            TooManyPlants tooManyPlants = new TooManyPlants();
            tooManyPlants.setToggled(rs.getBoolean("is_too_many_plants_toggled"));
            //Initialize Replanting
            Replanting replanting = new Replanting();
            replanting.setToggled(rs.getBoolean("is_replanting_toggled"));
            replanting.setCurrentTier(rs.getInt("replanting_tier"));
            if(replanting.getCurrentTier() != 0) {
              replanting.setUnlocked(true);
            }
            //Initialize Farmers Diet
            FarmersDiet farmersDiet = new FarmersDiet();
            farmersDiet.setToggled(rs.getBoolean("is_farmers_diet_toggled"));
            farmersDiet.setCurrentTier(rs.getInt("farmers_diet_tier"));
            if(farmersDiet.getCurrentTier() != 0) {
              farmersDiet.setUnlocked(true);
            }
            //Initialize Diamond Flowers
            DiamondFlowers diamondFlowers = new DiamondFlowers();
            diamondFlowers.setToggled(rs.getBoolean("is_diamond_flowers_toggled"));
            diamondFlowers.setCurrentTier(rs.getInt("diamond_flowers_tier"));
            if(diamondFlowers.getCurrentTier() != 0) {
              diamondFlowers.setUnlocked(true);
            }
            //Initialize Mass Harvest
            MassHarvest massHarvest = new MassHarvest();
            massHarvest.setToggled(rs.getBoolean("is_mass_harvest_toggled"));
            massHarvest.setCurrentTier(rs.getInt("mass_harvest_tier"));
            if(massHarvest.getCurrentTier() != 0) {
              massHarvest.setUnlocked(true);
            }
            //Initialize Pans Blessing
            PansBlessing pansBlessing = new PansBlessing();
            pansBlessing.setToggled(rs.getBoolean("is_pans_blessing_toggled"));
            pansBlessing.setCurrentTier(rs.getInt("pans_blessing_tier"));
            if(pansBlessing.getCurrentTier() != 0) {
              pansBlessing.setUnlocked(true);
            }
            //Initialize Natures Wrath
            NaturesWrath naturesWrath = new NaturesWrath();
            naturesWrath.setToggled(rs.getBoolean("is_natures_wrath_toggled"));
            naturesWrath.setCurrentTier(rs.getInt("natures_wrath_tier"));
            if(naturesWrath.getCurrentTier() != 0) {
              naturesWrath.setUnlocked(true);
            }

            int massHarvestCooldown = rs.getInt("mass_harvest_cooldown");
            int pansBlessingCooldown = rs.getInt("pans_blessing_cooldown");
            //We dont need to care about natures wrath cooldown since its an instantaneous ability. Leaving supporting code in just in case
            if(massHarvestCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, massHarvestCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.MASS_HARVEST, cal.getTimeInMillis());
            }
            if(pansBlessingCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, pansBlessingCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.PANS_BLESSING, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_replanting_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.REPLANTING);
            }
            if(rs.getBoolean("is_farmers_diet_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.FARMERS_DIET);
            }
            if(rs.getBoolean("is_diamond_flowers_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DIAMOND_FLOWERS);
            }
            if(rs.getBoolean("is_mass_harvest_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.MASS_HARVEST);
            }
            if(rs.getBoolean("is_pans_blessing_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.PANS_BLESSING);
            }
            if(rs.getBoolean("is_natures_wrath_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.NATURES_WRATH);
            }
            abilityMap.put(DefaultAbilities.TOO_MANY_PLANTS, tooManyPlants);
            abilityMap.put(UnlockedAbilities.REPLANTING, replanting);
            abilityMap.put(UnlockedAbilities.FARMERS_DIET, farmersDiet);
            abilityMap.put(UnlockedAbilities.DIAMOND_FLOWERS, diamondFlowers);
            abilityMap.put(UnlockedAbilities.MASS_HARVEST, massHarvest);
            abilityMap.put(UnlockedAbilities.PANS_BLESSING, pansBlessing);
            abilityMap.put(UnlockedAbilities.NATURES_WRATH, naturesWrath);
            //Create skill
            Herbalism herbalism = new Herbalism(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(herbalism);
          }
          //init archery
          else if(skill.equals(Skills.ARCHERY)) {
            //Initialize Daze
            Daze daze = new Daze();
            daze.setToggled(rs.getBoolean("is_daze_toggled"));
            //Initialize Combo
            Combo combo = new Combo();
            combo.setToggled(rs.getBoolean("is_combo_toggled"));
            combo.setCurrentTier(rs.getInt("combo_tier"));
            if(combo.getCurrentTier() != 0) {
              combo.setUnlocked(true);
            }
            //Initialize Puncture
            Puncture puncture = new Puncture();
            puncture.setToggled(rs.getBoolean("is_puncture_toggled"));
            puncture.setCurrentTier(rs.getInt("puncture_tier"));
            if(puncture.getCurrentTier() != 0) {
              puncture.setUnlocked(true);
            }
            //Initialize Tipped Arrows
            TippedArrows tippedArrows = new TippedArrows();
            tippedArrows.setToggled(rs.getBoolean("is_tipped_arrows_toggled"));
            tippedArrows.setCurrentTier(rs.getInt("tipped_arrows_tier"));
            if(tippedArrows.getCurrentTier() != 0) {
              tippedArrows.setUnlocked(true);
            }
            //Initialize Blessing of Apollo
            BlessingOfApollo blessingOfApollo = new BlessingOfApollo();
            blessingOfApollo.setToggled(rs.getBoolean("is_blessing_of_apollo_toggled"));
            blessingOfApollo.setCurrentTier(rs.getInt("blessing_of_apollo_tier"));
            if(blessingOfApollo.getCurrentTier() != 0) {
              blessingOfApollo.setUnlocked(true);
            }
            //Initialize Blessing of Artemis
            BlessingOfArtemis blessingOfArtemis = new BlessingOfArtemis();
            blessingOfArtemis.setToggled(rs.getBoolean("is_blessing_of_artemis_toggled"));
            blessingOfArtemis.setCurrentTier(rs.getInt("blessing_of_artemis_tier"));
            if(blessingOfArtemis.getCurrentTier() != 0) {
              blessingOfArtemis.setUnlocked(true);
            }
            //Initialize Curse of Hades
            CurseOfHades curseOfHades = new CurseOfHades();
            curseOfHades.setToggled(rs.getBoolean("is_curse_of_hades_toggled"));
            curseOfHades.setCurrentTier(rs.getInt("curse_of_hades_tier"));
            if(curseOfHades.getCurrentTier() != 0) {
              curseOfHades.setUnlocked(true);
            }

            int blessingOfApolloCooldown = rs.getInt("blessing_of_apollo_cooldown");
            int blessingOfArtemisCooldown = rs.getInt("blessing_of_artemis_cooldown");
            int curseOfHadesCooldown = rs.getInt("curse_of_hades_cooldown");

            if(blessingOfApolloCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, blessingOfApolloCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.BLESSING_OF_APOLLO, cal.getTimeInMillis());
            }
            if(blessingOfArtemisCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, blessingOfArtemisCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.BLESSING_OF_ARTEMIS, cal.getTimeInMillis());
            }
            if(curseOfHadesCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, curseOfHadesCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.CURSE_OF_HADES, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_combo_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.COMBO);
            }
            if(rs.getBoolean("is_puncture_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.PUNCTURE);
            }
            if(rs.getBoolean("is_tipped_arrows_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.TIPPED_ARROWS);
            }
            if(rs.getBoolean("is_blessing_of_apollo_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BLESSING_OF_APOLLO);
            }
            if(rs.getBoolean("is_blessing_of_artemis_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BLESSING_OF_ARTEMIS);
            }
            if(rs.getBoolean("is_curse_of_hades_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.CURSE_OF_HADES);
            }
            abilityMap.put(DefaultAbilities.DAZE, daze);
            abilityMap.put(UnlockedAbilities.COMBO, combo);
            abilityMap.put(UnlockedAbilities.PUNCTURE, puncture);
            abilityMap.put(UnlockedAbilities.TIPPED_ARROWS, tippedArrows);
            abilityMap.put(UnlockedAbilities.BLESSING_OF_APOLLO, blessingOfApollo);
            abilityMap.put(UnlockedAbilities.BLESSING_OF_ARTEMIS, blessingOfArtemis);
            abilityMap.put(UnlockedAbilities.CURSE_OF_HADES, curseOfHades);
            Archery archery = new Archery(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(archery);
          }
          //}
          //init woodcutting
          else if(skill.equals(Skills.WOODCUTTING)) {
            //Initialize Extra Lumber
            ExtraLumber extraLumber = new ExtraLumber();
            extraLumber.setToggled(rs.getBoolean("is_extra_lumber_toggled"));
            //Initialize Heavy Swing
            HeavySwing heavySwing = new HeavySwing();
            heavySwing.setToggled(rs.getBoolean("is_heavy_swing_toggled"));
            heavySwing.setCurrentTier(rs.getInt("heavy_swing_tier"));
            if(heavySwing.getCurrentTier() != 0) {
              heavySwing.setUnlocked(true);
            }
            //Initialize Nymphs Vitality
            NymphsVitality nymphsVitality = new NymphsVitality();
            nymphsVitality.setToggled(rs.getBoolean("is_nymphs_vitality_toggled"));
            nymphsVitality.setCurrentTier(rs.getInt("nymphs_vitality_tier"));
            if(nymphsVitality.getCurrentTier() != 0) {
              nymphsVitality.setUnlocked(true);
            }
            //Initialize Dryads Gift
            DryadsGift dryadsGift = new DryadsGift();
            dryadsGift.setToggled(rs.getBoolean("is_dryads_gift_toggled"));
            dryadsGift.setCurrentTier(rs.getInt("dryads_gift_tier"));
            if(dryadsGift.getCurrentTier() != 0) {
              dryadsGift.setUnlocked(true);
            }
            //Initialize Hesperides Apples
            HesperidesApples hesperidesApples = new HesperidesApples();
            hesperidesApples.setToggled(rs.getBoolean("is_hesperides_apples_toggled"));
            hesperidesApples.setCurrentTier(rs.getInt("hesperides_apples_tier"));
            if(hesperidesApples.getCurrentTier() != 0) {
              hesperidesApples.setUnlocked(true);
            }
            //Initialize Temporal Harvest
            TemporalHarvest temporalHarvest = new TemporalHarvest();
            temporalHarvest.setToggled(rs.getBoolean("is_temporal_harvest_toggled"));
            temporalHarvest.setCurrentTier(rs.getInt("temporal_harvest_tier"));
            if(temporalHarvest.getCurrentTier() != 0) {
              temporalHarvest.setUnlocked(true);
            }
            //Initialize Demeters Shrine
            DemetersShrine demetersShrine = new DemetersShrine();
            demetersShrine.setToggled(rs.getBoolean("is_demeters_shrine_toggled"));
            demetersShrine.setCurrentTier(rs.getInt("demeters_shrine_tier"));
            if(demetersShrine.getCurrentTier() != 0) {
              demetersShrine.setUnlocked(true);
            }

            int hesperidesApplesCooldown = rs.getInt("hesperides_apples_cooldown");
            int temporalHarvestCooldown = rs.getInt("temporal_harvest_cooldown");
            int demetersShrineCooldown = rs.getInt("demeters_shrine_cooldown");

            if(hesperidesApplesCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, hesperidesApplesCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.HESPERIDES_APPLES, cal.getTimeInMillis());
            }
            if(temporalHarvestCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, temporalHarvestCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.TEMPORAL_HARVEST, cal.getTimeInMillis());
            }
            if(demetersShrineCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, demetersShrineCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.DEMETERS_SHRINE, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_dryads_gift_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DRYADS_GIFT);
            }
            if(rs.getBoolean("is_heavy_swing_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.HEAVY_SWING);
            }
            if(rs.getBoolean("is_nymphs_vitality_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.NYMPHS_VITALITY);
            }
            if(rs.getBoolean("is_hesperides_apples_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.HESPERIDES_APPLES);
            }
            if(rs.getBoolean("is_temporal_harvest_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.TEMPORAL_HARVEST);
            }
            if(rs.getBoolean("is_demeters_shrine_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DEMETERS_SHRINE);
            }
            abilityMap.put(DefaultAbilities.EXTRA_LUMBER, extraLumber);
            abilityMap.put(UnlockedAbilities.HEAVY_SWING, heavySwing);
            abilityMap.put(UnlockedAbilities.NYMPHS_VITALITY, nymphsVitality);
            abilityMap.put(UnlockedAbilities.DRYADS_GIFT, dryadsGift);
            abilityMap.put(UnlockedAbilities.HESPERIDES_APPLES, hesperidesApples);
            abilityMap.put(UnlockedAbilities.TEMPORAL_HARVEST, temporalHarvest);
            abilityMap.put(UnlockedAbilities.DEMETERS_SHRINE, demetersShrine);
            Woodcutting woodcutting = new Woodcutting(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(woodcutting);
          }
          else if(skill.equals(Skills.FITNESS)) {
            //Initialize bleed
            Bleed bleed = new Bleed();
            bleed.setToggled(rs.getBoolean("is_bleed_toggled"));
            //Initialize Deeper Wound
            DeeperWound deeperWound = new DeeperWound();
            deeperWound.setToggled(rs.getBoolean("is_deeper_wound_toggled"));
            deeperWound.setCurrentTier(rs.getInt("deeper_wound_tier"));
            if(deeperWound.getCurrentTier() != 0) {
              deeperWound.setUnlocked(true);
            }
            //Initialize Bleed+
            BleedPlus bleedPlus = new BleedPlus();
            bleedPlus.setToggled(rs.getBoolean("is_bleed_plus_toggled"));
            bleedPlus.setCurrentTier(rs.getInt("bleed_plus_tier"));
            if(bleedPlus.getCurrentTier() != 0) {
              bleedPlus.setUnlocked(true);
            }
            //Initialize Vampire
            Vampire vampire = new Vampire();
            vampire.setToggled(rs.getBoolean("is_vampire_toggled"));
            vampire.setCurrentTier(rs.getInt("vampire_tier"));
            if(vampire.getCurrentTier() != 0) {
              vampire.setUnlocked(true);
            }
            //Initialize Serrated Strikes
            SerratedStrikes serratedStrikes = new SerratedStrikes();
            serratedStrikes.setToggled(rs.getBoolean("is_serrated_strikes_toggled"));
            serratedStrikes.setCurrentTier(rs.getInt("serrated_strikes_tier"));
            if(serratedStrikes.getCurrentTier() != 0) {
              serratedStrikes.setUnlocked(true);
            }
            //Initialize Rage Spike
            RageSpike rageSpike = new RageSpike();
            rageSpike.setToggled(rs.getBoolean("is_rage_spike_toggled"));
            rageSpike.setCurrentTier(rs.getInt("rage_spike_tier"));
            if(rageSpike.getCurrentTier() != 0) {
              rageSpike.setUnlocked(true);
            }
            //Initialize Tainted Blade
            TaintedBlade taintedBlade = new TaintedBlade();
            taintedBlade.setToggled(rs.getBoolean("is_tainted_blade_toggled"));
            taintedBlade.setCurrentTier(rs.getInt("tainted_blade_tier"));
            if(taintedBlade.getCurrentTier() != 0) {
              taintedBlade.setUnlocked(true);
            }

            int serratedStrikesCooldown = rs.getInt("serrated_strikes_cooldown");
            int rageSpikeCooldown = rs.getInt("rage_spike_cooldown");
            int taintedBladeCooldown = rs.getInt("tainted_blade_cooldown");
            if(serratedStrikesCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, serratedStrikesCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.SERRATED_STRIKES, cal.getTimeInMillis());
            }
            if(rageSpikeCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, rageSpikeCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.RAGE_SPIKE, cal.getTimeInMillis());
            }
            if(taintedBladeCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, taintedBladeCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.TAINTED_BLADE, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_deeper_wound_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DEEPER_WOUND);
            }
            if(rs.getBoolean("is_bleed_plus_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BLEED_PLUS);
            }
            if(rs.getBoolean("is_vampire_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.VAMPIRE);
            }
            if(rs.getBoolean("is_serrated_strikes_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SERRATED_STRIKES);
            }
            if(rs.getBoolean("is_rage_spike_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.RAGE_SPIKE);
            }
            if(rs.getBoolean("is_tainted_blade_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.TAINTED_BLADE);
            }

            abilityMap.put(DefaultAbilities.BLEED, bleed);
            abilityMap.put(UnlockedAbilities.DEEPER_WOUND, deeperWound);
            abilityMap.put(UnlockedAbilities.BLEED_PLUS, bleedPlus);
            abilityMap.put(UnlockedAbilities.VAMPIRE, vampire);
            abilityMap.put(UnlockedAbilities.SERRATED_STRIKES, serratedStrikes);
            abilityMap.put(UnlockedAbilities.RAGE_SPIKE, rageSpike);
            abilityMap.put(UnlockedAbilities.TAINTED_BLADE, taintedBlade);
            //Create skill
            Swords swords = new Swords(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(swords);
          }
        } catch(SQLException e) {
          e.printStackTrace();
        }
      });
    });


    final Optional<ResultSet> loadoutSet = database.executeQuery("SELECT * FROM mcrpg_loadout WHERE uuid = '" + uuid.toString() + "'");
    loadoutSet.ifPresent(rs -> {
      try {
        if(rs.next()) {
          for(int i = 1; i <= McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities"); i++) {
            //It has to be an unlocked ability since default ones cant be in the loadout
            String s = rs.getString("Slot" + i);
            if(s == null || s.equalsIgnoreCase("null")) {
              continue;
            }
            UnlockedAbilities ability = UnlockedAbilities.fromString(s);
            abilityLoadout.add(ability);
          }
        }
      } catch(SQLException e) {
        e.printStackTrace();
      }
    });
    updatePowerLevel();
    for(Skill s : skills) {
      s.updateExpToLevel();
    }
  }

  public OfflinePlayer getOfflineMcMMOPlayer() {
    return Bukkit.getOfflinePlayer(uuid);
  }

  /**
   * Updates the power level of the player by adding together all of the levels of each skill
   *
   * @return The power level of the player
   */
  public int updatePowerLevel() {
    if(skills.isEmpty()) {
      powerLevel = 0;
    }
    else {
      final AtomicInteger powerLevelUpdater = new AtomicInteger(0);
      skills.forEach(skill -> powerLevelUpdater.addAndGet(skill.getCurrentLevel()));
      this.powerLevel = powerLevelUpdater.get();
    }
    return powerLevel;
  }

  /**
   * Get the instance of the players skill
   *
   * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist
   * @return The instance of the players skill of the type provided
   */
  public Skill getSkill(String skill) {
    return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill)).findAny().orElse(null);
  }

  /**
   * Get the instance of the players skill
   *
   * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist. Good lucky getting null out of this xD
   * @return The instance of the players skill of the type provided
   */
  public Skill getSkill(Skills skill) {
    return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill.getName())).findFirst().orElse(null);
  }

  /**
   *
   * @param ability The GenericAbility enum value you are searching for
   * @return The BaseAbility of the provided enum value
   */
  public BaseAbility getBaseAbility(GenericAbility ability) {
    return getSkill(ability.getSkill()).getAbility(ability);
  }

  public void giveExp(Skills skill, int exp, GainReason reason) {
    getSkill(skill).giveExp(this, exp, reason);
  }

  /**
   * Get the cooldown of an ability.
   *
   * @param ability The ability type you want to check the cooldown for
   * @return The endtime of the cooldown in milis. If the cooldown doesnt exist return -1
   */
  public long getCooldown(GenericAbility ability) {

    if(abilitiesOnCooldown.containsKey(ability)) {
      return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ability) - Calendar.getInstance().getTimeInMillis());
    }
    else {
      return -1;
    }
  }

  /**
   * Get the cooldown of an ability (this works since a skill can only have one active ability unlocked
   *
   * @param skill The skill to check
   * @return The time to end in millis or -1 if it doesnt exist
   */
  public long getCooldown(Skills skill) {
    for(UnlockedAbilities ab : abilitiesOnCooldown.keySet()) {
      if(ab.getSkill().equalsIgnoreCase(skill.getName())) {
        return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ab) - Calendar.getInstance().getTimeInMillis());
      }
    }
    return -1;
  }

  /**
   * @param ability   Ability to add on cooldown
   * @param timeToEnd The end time in milis
   */
  public void addAbilityOnCooldown(UnlockedAbilities ability, long timeToEnd) {
    abilitiesOnCooldown.put(ability, timeToEnd);
  }

  /**
   * @param ability Ability to remove from cooldows
   */
  public void removeAbilityOnCooldown(UnlockedAbilities ability) {
    abilitiesOnCooldown.replace(ability, 0L);
  }

  /**
   * Update all the cooldowns and verify if they are valid
   */
  public void updateCooldowns() {
    ArrayList<UnlockedAbilities> toRemove = new ArrayList<>();
    if(abilitiesOnCooldown.isEmpty() && endTimeForReplaceCooldown == 0) {
      return;
    }
    for(UnlockedAbilities ability : abilitiesOnCooldown.keySet()) {
      long timeToEnd = abilitiesOnCooldown.get(ability);
      if(Calendar.getInstance().getTimeInMillis() >= timeToEnd) {
        if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
          this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
        }
        toRemove.add(ability);
      }
      else if(timeToEnd == 0L){
        toRemove.add(ability);
      }
    }
    Database database = McRPG.getInstance().getMcRPGDb().getDatabase();
    if(!toRemove.isEmpty()) {
      for(UnlockedAbilities ab : toRemove) {
        database.executeUpdate("UPDATE mcrpg_" + ab.getSkill().toLowerCase() + "_data SET "
                + Methods.convertNameToSQL(ab.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_cooldown = 0 WHERE uuid = '" + uuid.toString() + "'");
        abilitiesOnCooldown.remove(ab);
      }
    }
    long timeToEnd = this.endTimeForReplaceCooldown;
    if(timeToEnd != 0 && Calendar.getInstance().getTimeInMillis() >= timeToEnd) {
      this.endTimeForReplaceCooldown = 0;
      if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
        this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.Players.ReplaceCooldownExpire")));
      }
      database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown = 0 WHERE uuid = '" + uuid.toString() + "'");
    }
  }

  /**
   * Reset all cooldowns to be 0
   */
  public void resetCooldowns() {
    Database database = McRPG.getInstance().getMcRPGDb().getDatabase();
    for(UnlockedAbilities ability : abilitiesOnCooldown.keySet()) {
      long timeToEnd = abilitiesOnCooldown.get(ability);
      if(Calendar.getInstance().getTimeInMillis() >= timeToEnd) {
        if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
          this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
        }
        database.executeUpdate("UPDATE mcrpg_" + ability.getSkill().toLowerCase() + "_data SET " + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus"))
                + "_cooldown = 0 WHERE uuid = `" + uuid.toString() + "`");

      }
    }
    abilitiesOnCooldown.clear();
    endTimeForReplaceCooldown = 0;
    database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown = 0 WHERE uuid = `" + uuid.toString() + "`");
    if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
      this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
              McRPG.getInstance().getLangFile().getString("Messages.Players.ReplaceCooldownExpire")));
    }
  }

  /**
   * Save players data
   */
  public void saveData() {
    Database database = McRPG.getInstance().getMcRPGDb().getDatabase();
    for(Skills type : Skills.values()) {
      Skill skill = getSkill(type);
      @Language("SQL") String query = "UPDATE mcrpg_" + skill.getName().toLowerCase() + "_data SET current_level = " + skill.getCurrentLevel() + ", current_exp = " + skill.getCurrentExp();
      for(GenericAbility ability : skill.getAbilityKeys()) {
        if(ability instanceof DefaultAbilities) {
          query += ", is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_toggled = " + Methods.convertBool(skill.getAbility(ability).isToggled());
        }
        if(ability instanceof UnlockedAbilities) {
          query += ", is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_toggled = " + Methods.convertBool(skill.getAbility(ability).isToggled());
          query += ", is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_pending = " + Methods.convertBool(pendingUnlockAbilities.contains(ability));
          query += ", " + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_tier = " + skill.getAbility(ability).getCurrentTier();
        }
        Calendar cal = Calendar.getInstance();
        if(abilitiesOnCooldown.containsKey(ability)) {
          Calendar temp = Calendar.getInstance();
          temp.setTimeInMillis(abilitiesOnCooldown.get(ability));
          int seconds = (int) (temp.getTimeInMillis() - cal.getTimeInMillis()) / 1000;
          query += ", " + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_cooldown = " + seconds;
        }
      }
      query += " WHERE uuid = '" + this.uuid.toString() + "'";
      database.executeUpdate(query);
    }
    if(endTimeForReplaceCooldown != 0) {
      Calendar temp = Calendar.getInstance();
      temp.setTimeInMillis(endTimeForReplaceCooldown);
      int seconds = (int) (temp.getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 1000;
      database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown = " + seconds + " WHERE uuid = '" + uuid.toString() + "'");
    }
    database.executeUpdate("UPDATE mcrpg_player_data SET ability_points = " + abilityPoints + ", redeemable_exp = " + redeemableExp + ", redeemable_levels = " + redeemableLevels + " WHERE uuid = '" + uuid.toString() + "'");
    @Language("SQL") String query = "UPDATE mcrpg_player_settings SET keep_hand = " + Methods.convertBool(keepHandEmpty)
            + ", ignore_tips = " + Methods.convertBool(ignoreTips) + ", auto_deny = " + Methods.convertBool(autoDeny) + ", display_type = '" + displayType.getName() +
            "', health_type = '" + healthbarType.getName() + "' WHERE uuid = '" + uuid.toString() + "'";
    database.executeUpdate(query);
    for(UnlockedAbilities ability : pendingUnlockAbilities) {
      query = "UPDATE mcrpg_" + ability.getSkill().toLowerCase() + "_data SET is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_pending = 1" +
              " WHERE uuid = '" + uuid.toString() + "'";
      database.executeUpdate(query);
    }
    @Language("SQL") String loadoutQuery = "UPDATE mcrpg_loadout SET";
    for(int i = 1; i <= abilityLoadout.size(); i++) {
      if(i != 1) {
        loadoutQuery += ",";
      }
      loadoutQuery += " Slot" + i + " = '" + abilityLoadout.get(i - 1).getName() + "'";

    }
    loadoutQuery += " WHERE uuid = '" + uuid.toString() + "'";
    if(abilityLoadout.size()>0) {
      database.executeUpdate(loadoutQuery);
    }

    RemoteTransfer transfer = (RemoteTransfer) getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
    if(transfer.isUnlocked()) {
      File remoteTransferFile = new File(McRPG.getInstance().getDataFolder(), File.separator + "remote_transfer_data" + File.separator + uuid.toString() + ".yml");
      FileConfiguration data = YamlConfiguration.loadConfiguration(remoteTransferFile);
      for(Material mat : transfer.getItemsToSync().keySet()) {
        data.set("RemoteTransferBlocks." + mat.toString(), transfer.getItemsToSync().get(mat));
      }
      try {
        data.save(remoteTransferFile);
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @param abilities The ability to add to the pending list
   */
  public void addPendingAbilityUnlock(UnlockedAbilities abilities) {
    this.pendingUnlockAbilities.add(abilities);
  }

  /**
   * @param abilities The ability to remove from the pending list
   */
  public void removePendingAbilityUnlock(UnlockedAbilities abilities) {
    this.pendingUnlockAbilities.remove(abilities);
  }

  /**
   * @return true if the player has a pending ability and false if not
   */
  public boolean hasPendingAbility() {
    return !this.pendingUnlockAbilities.isEmpty();
  }

  /**
   * @return true if player is online false if not
   */
  public boolean isOnline() {
    return Bukkit.getOfflinePlayer(uuid).isOnline();
  }

  /**
   * @return Player instance of the mcrpg player. We dont safe check if they are online here
   */
  public Player getPlayer() {
    return (Player) Bukkit.getOfflinePlayer(uuid);
  }

  /**
   * @param ability Ability to add to the loadout
   */
  public void addAbilityToLoadout(UnlockedAbilities ability) {
    abilityLoadout.add(ability);
    saveData();
  }

  /**
   * @param ability Ability to check for
   * @return true if the player has the ability in their loadout, false if not
   */
  public boolean doesPlayerHaveAbilityInLoadout(UnlockedAbilities ability) {
    return abilityLoadout.stream().filter(ability1 -> ability1.getName().equalsIgnoreCase(ability.getName())).findFirst().orElse(null) != null;
  }

  /**
   * @param skill The skill to check if they have an active ability for
   * @return true if the player has an active ability, false if not
   */
  public boolean doesPlayerHaveActiveAbilityFromSkill(Skills skill) {
    return abilityLoadout.stream().filter(ability -> ability.getSkill().equalsIgnoreCase(skill.getName()))
            .filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null) != null;
  }

  /**
   * @param skill Skill to get the ability for
   * @return The UnlockedAbilities instance of the active ability belonging to the provided skill a player has, or null if they dont have any
   */
  public UnlockedAbilities getActiveAbilityForSkill(Skills skill) {
    return abilityLoadout.stream().filter(ability -> ability.getSkill().equals(skill.getName()))
            .filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null);
  }

  /**
   *
   * @param oldAbility Old ability to be replaced
   * @param newAbility Ability to replace with
   */
  public void replaceAbility(UnlockedAbilities oldAbility, UnlockedAbilities newAbility) {
    for(int i = 0; i < abilityLoadout.size(); i++) {
      if(abilityLoadout.get(i).equals(oldAbility)) {
        abilityLoadout.set(i, newAbility);
        return;
      }
    }
  }

  public void giveRedeemableExp(int exp){
    this.redeemableExp += exp;
  }

  public void giveRedeemableLevels(int levels){
    this.redeemableLevels += levels;
  }

  public void sendConstantUpdate(Skill skill, int expGained){
    getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(Methods.color(getPlayer(), McRPG.getInstance().getConfig()
            .getString("DisplayConfig.ActionBar." + skill.getName() + ".Message").replace("%Current_Level%", Integer.toString(skill.getCurrentLevel())).replace("%Skill%", skill.getType().getDisplayName())
                    .replace("%Exp_Gained%", Integer.toString(expGained)).replace("%Exp_To_Level%", Integer.toString(skill.getExpToLevel() - skill.getCurrentExp())))));
  }

  @Override
  public boolean equals(Object object) {
    if(object instanceof McRPGPlayer) {
      return uuid.equals(((McRPGPlayer) object).getUuid());
    }
    else if(object instanceof Player) {
      return uuid.equals(((Player) object).getUniqueId());
    }
    else if(object instanceof UUID) {
      return uuid.equals((object));
    }
    return false;
  }
}