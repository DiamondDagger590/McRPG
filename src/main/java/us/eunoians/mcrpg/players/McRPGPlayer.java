package us.eunoians.mcrpg.players;

import com.cyr1en.flatdb.Database;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.archery.*;
import us.eunoians.mcrpg.abilities.axes.*;
import us.eunoians.mcrpg.abilities.excavation.*;
import us.eunoians.mcrpg.abilities.fishing.*;
import us.eunoians.mcrpg.abilities.fitness.*;
import us.eunoians.mcrpg.abilities.herbalism.*;
import us.eunoians.mcrpg.abilities.mining.*;
import us.eunoians.mcrpg.abilities.sorcery.*;
import us.eunoians.mcrpg.abilities.swords.*;
import us.eunoians.mcrpg.abilities.unarmed.*;
import us.eunoians.mcrpg.abilities.woodcutting.*;
import us.eunoians.mcrpg.api.events.mcrpg.axes.CripplingBlowEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.SmitingFistEvent;
import us.eunoians.mcrpg.api.leaderboards.PlayerRank;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RedeemBit;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
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

  @Getter private UUID uuid;

  @Getter private int powerLevel;
  @Getter @Setter private int abilityPoints;

  private ArrayList<Skill> skills = new ArrayList<>();

  @Getter private ArrayList<UnlockedAbilities> pendingUnlockAbilities = new ArrayList<>();
  private HashMap<UnlockedAbilities, Long> abilitiesOnCooldown = new HashMap<>();
  @Getter private ArrayList<UnlockedAbilities> abilityLoadout = new ArrayList<>();
  @Getter @Setter private long endTimeForReplaceCooldown;
  @Getter private ArrayList<UnlockedAbilities> activeAbilities = new ArrayList<>();

  @Getter @Setter
  private PlayerRank powerRank;
  @Getter
  private Map<Skills, PlayerRank> skillRanks = new HashMap<>();
  @Getter
  private boolean isLoadingRankData = false;

  //Ability data
  @Getter @Setter private boolean hasBleedImmunity = false;
  @Getter @Setter private boolean hasDazeImmunity = false;
  @Setter @Getter private boolean canSmite;
  @Getter @Setter private SmitingFistEvent smitingFistData;
  @Getter @Setter private CripplingBlowEvent cripplingBlowData;
  @Getter @Setter private boolean isLinkedToRemoteTransfer = false;
  @Getter @Setter private boolean canDenseImpact;
  @Getter @Setter private int armourDmg;
  @Getter @Setter private double divineEscapeExpDebuff;
  @Getter @Setter private double divineEscapeDamageDebuff;
  @Getter @Setter private long divineEscapeExpEnd;
  @Getter @Setter private long divineEscapeDamageEnd;
  @Getter @Setter private boolean isHandDigging = false;
  @Getter @Setter private Set<Material> handDiggingBlocks;

  //Ready variables
  @Setter private boolean isReadying = false;
  @Getter @Setter private PlayerReadyBit readyingAbilityBit = null;

  //Settings
  @Getter @Setter private MobHealthbarUtils.MobHealthbarType healthbarType = MobHealthbarUtils.MobHealthbarType.BAR;
  @Getter @Setter private boolean keepHandEmpty = false;
  @Getter @Setter private DisplayType displayType = DisplayType.SCOREBOARD;
  @Getter @Setter private boolean autoDeny = false;
  @Getter @Setter private boolean requireEmptyOffHand = false;
  @Getter @Setter private boolean ignoreTips;
  @Getter @Setter private int unarmedIgnoreSlot;

  @Getter private Set<TipType> usedTips = new HashSet<>();

  //Redeemable data
  @Getter @Setter private int redeemableExp;
  @Getter @Setter private int redeemableLevels;
  @Getter @Setter private boolean listenForCustomExpInput = false;
  @Getter @Setter private RedeemBit redeemBit;

  //Guardian Data
  @Getter @Setter private double guardianSummonChance;
  @Getter @Setter private Location lastFishCaughtLoc = null;

  //Fitness Data
  @Getter private List<Location> lastFallLocation = new ArrayList<>();
  
  //Artifact variables
  @Getter @Setter private long magnetArtifactCooldownTime = 0;
  @Getter @Setter private long cooldownResetArtifactCooldownTime = 0;

  //mcMMO conversion
  @Getter @Setter private int boostedExp;
  
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
        String query = "INSERT INTO mcrpg_" + type.getName() + "_data (uuid) VALUES ('" + uuid.toString() + "')";
        database.executeUpdate(query);
      }
      String query = "INSERT INTO MCRPG_PLAYER_SETTINGS (UUID) VALUES ('" + uuid.toString() + "')";
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
        long replaceCooldown = resultSet.getLong("replace_ability_cooldown_time");
        this.boostedExp = resultSet.getInt("boosted_exp");
        this.divineEscapeExpDebuff = resultSet.getDouble("divine_escape_exp_debuff");
        this.divineEscapeDamageDebuff = resultSet.getDouble("divine_escape_damage_debuff");
        this.divineEscapeExpEnd = resultSet.getInt("divine_escape_exp_end_time");
        this.divineEscapeDamageEnd = resultSet.getInt("divine_escape_damage_end_time");
        Calendar cal1 = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();
        cal1.setTimeInMillis(replaceCooldown);
        if(cal.getTimeInMillis() < cal1.getTimeInMillis()) {
          this.endTimeForReplaceCooldown = cal1.getTimeInMillis();
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
          this.requireEmptyOffHand = rs.getBoolean("require_empty_offhand");
          this.unarmedIgnoreSlot = rs.getInt("unarmed_ignore_slot");
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
          String query = "INSERT INTO mcrpg_" + skill.getName().toLowerCase() + "_data (uuid) VALUES ('" + uuid.toString() + "')";
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
            Bleed bleed = new Bleed(rs.getBoolean("is_bleed_toggled"));
            DeeperWound deeperWound = new DeeperWound(rs.getBoolean("is_deeper_wound_toggled"), rs.getInt("deeper_wound_tier"));
            BleedPlus bleedPlus = new BleedPlus(rs.getBoolean("is_bleed_plus_toggled"), rs.getInt("bleed_plus_tier"));
            Vampire vampire = new Vampire(rs.getBoolean("is_vampire_toggled"), rs.getInt("vampire_tier"));
            SerratedStrikes serratedStrikes = new SerratedStrikes(rs.getBoolean("is_serrated_strikes_toggled"), rs.getInt("serrated_strikes_tier"));
            RageSpike rageSpike = new RageSpike(rs.getBoolean("is_rage_spike_toggled"), rs.getInt("rage_spike_tier"));
            TaintedBlade taintedBlade = new TaintedBlade(rs.getBoolean("is_tainted_blade_toggled"), rs.getInt("tainted_blade_tier"));

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
            DoubleDrop doubleDrop = new DoubleDrop(rs.getBoolean("is_double_drop_toggled"));
            RicherOres richerOres = new RicherOres(rs.getBoolean("is_richer_ores_toggled"), rs.getInt("richer_ores_tier"));
            ItsATriple itsATriple = new ItsATriple(rs.getBoolean("is_its_a_triple_toggled"), rs.getInt("its_a_triple_tier"));

            RemoteTransfer remoteTransfer = new RemoteTransfer(uuid, rs.getBoolean("is_remote_transfer_toggled"), rs.getInt("remote_transfer_tier"));
            this.isLinkedToRemoteTransfer = remoteTransfer.isAbilityLinked();

            SuperBreaker superBreaker = new SuperBreaker(rs.getBoolean("is_super_breaker_toggled"), rs.getInt("super_breaker_tier"));
            BlastMining blastMining = new BlastMining(rs.getBoolean("is_blast_mining_toggled"), rs.getInt("blast_mining_tier"));
            OreScanner oreScanner = new OreScanner(rs.getBoolean("is_ore_scanner_toggled"), rs.getInt("ore_scanner_tier"));

            int superBreakerCooldown = rs.getInt("super_breaker_cooldown");
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
            StickyFingers stickyFingers = new StickyFingers(rs.getBoolean("is_sticky_fingers_toggled"));
            TighterGrip tighterGrip = new TighterGrip(rs.getBoolean("is_tighter_grip_toggled"), rs.getInt("tighter_grip_tier"));
            Disarm disarm = new Disarm(rs.getBoolean("is_disarm_toggled"), rs.getInt("disarm_tier"));
            IronArm ironArm = new IronArm(rs.getBoolean("is_iron_arm_toggled"), rs.getInt("iron_arm_tier"));
            Berserk berserk = new Berserk(rs.getBoolean("is_berserk_toggled"), rs.getInt("berserk_tier"));
            SmitingFist smitingFist = new SmitingFist(rs.getBoolean("is_smiting_fist_toggled"), rs.getInt("smiting_fist_tier"));
            DenseImpact denseImpact = new DenseImpact(rs.getBoolean("is_dense_impact_toggled"), rs.getInt("dense_impact_tier"));

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
            TooManyPlants tooManyPlants = new TooManyPlants(rs.getBoolean("is_too_many_plants_toggled"));
            Replanting replanting = new Replanting(rs.getBoolean("is_replanting_toggled"), rs.getInt("replanting_tier"));
            FarmersDiet farmersDiet = new FarmersDiet(rs.getBoolean("is_farmers_diet_toggled"), rs.getInt("farmers_diet_tier"));
            DiamondFlowers diamondFlowers = new DiamondFlowers(rs.getBoolean("is_diamond_flowers_toggled"), rs.getInt("diamond_flowers_tier"));
            MassHarvest massHarvest = new MassHarvest(rs.getBoolean("is_mass_harvest_toggled"), rs.getInt("mass_harvest_tier"));
            PansBlessing pansBlessing = new PansBlessing(rs.getBoolean("is_pans_blessing_toggled"), rs.getInt("pans_blessing_tier"));
            NaturesWrath naturesWrath = new NaturesWrath(rs.getBoolean("is_natures_wrath_toggled"), rs.getInt("natures_wrath_tier"));

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
            Daze daze = new Daze(rs.getBoolean("is_daze_toggled"));
            Combo combo = new Combo(rs.getBoolean("is_combo_toggled"), rs.getInt("combo_tier"));
            Puncture puncture = new Puncture(rs.getBoolean("is_puncture_toggled"), rs.getInt("puncture_tier"));
            TippedArrows tippedArrows = new TippedArrows(rs.getBoolean("is_tipped_arrows_toggled"), rs.getInt("tipped_arrows_tier"));
            BlessingOfApollo blessingOfApollo = new BlessingOfApollo(rs.getBoolean("is_blessing_of_apollo_toggled"), rs.getInt("blessing_of_apollo_tier"));
            BlessingOfArtemis blessingOfArtemis = new BlessingOfArtemis(rs.getBoolean("is_blessing_of_artemis_toggled"), rs.getInt("blessing_of_artemis_tier"));
            CurseOfHades curseOfHades = new CurseOfHades(rs.getBoolean("is_curse_of_hades_toggled"), rs.getInt("curse_of_hades_tier"));

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
          //init woodcutting
          else if(skill.equals(Skills.WOODCUTTING)) {
            ExtraLumber extraLumber = new ExtraLumber(rs.getBoolean("is_extra_lumber_toggled"));
            HeavySwing heavySwing = new HeavySwing(rs.getBoolean("is_heavy_swing_toggled"), rs.getInt("heavy_swing_tier"));
            NymphsVitality nymphsVitality = new NymphsVitality(rs.getBoolean("is_nymphs_vitality_toggled"), rs.getInt("nymphs_vitality_tier"));
            DryadsGift dryadsGift = new DryadsGift(rs.getBoolean("is_dryads_gift_toggled"), rs.getInt("dryads_gift_tier"));
            HesperidesApples hesperidesApples = new HesperidesApples(rs.getBoolean("is_hesperides_apples_toggled"), rs.getInt("hesperides_apples_tier"));
            TemporalHarvest temporalHarvest = new TemporalHarvest(rs.getBoolean("is_temporal_harvest_toggled"), rs.getInt("temporal_harvest_tier"));
            DemetersShrine demetersShrine = new DemetersShrine(rs.getBoolean("is_demeters_shrine_toggled"), rs.getInt("demeters_shrine_tier"));

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
          //Initialize Fitness
          else if(skill.equals(Skills.FITNESS)) {
            Roll roll = new Roll(rs.getBoolean("is_roll_toggled"));
            ThickSkin thickSkin = new ThickSkin(rs.getBoolean("is_thick_skin_toggled"), rs.getInt("thick_skin_tier"));
            BulletProof bulletProof = new BulletProof(rs.getBoolean("is_bullet_proof_toggled"), rs.getInt("bullet_proof_tier"));
            Dodge dodge = new Dodge(rs.getBoolean("is_dodge_toggled"), rs.getInt("dodge_tier"));
            IronMuscles ironMuscles = new IronMuscles(rs.getBoolean("is_iron_muscles_toggled"), rs.getInt("iron_muscles_tier"));
            RunnersDiet runnersDiet = new RunnersDiet(rs.getBoolean("is_runners_diet_toggled"), rs.getInt("runners_diet_tier"));
            DivineEscape divineEscape = new DivineEscape(rs.getBoolean("is_divine_escape_toggled"), rs.getInt("divine_escape_tier"));

            int divineEscapeCooldown = rs.getInt("divine_escape_cooldown");

            if(divineEscapeCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, divineEscapeCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.DIVINE_ESCAPE, cal.getTimeInMillis());
            }
            if(rs.getBoolean("is_thick_skin_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.THICK_SKIN);
            }
            if(rs.getBoolean("is_bullet_proof_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BULLET_PROOF);
            }
            if(rs.getBoolean("is_dodge_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DODGE);
            }
            if(rs.getBoolean("is_iron_muscles_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.IRON_MUSCLES);
            }
            if(rs.getBoolean("is_runners_diet_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.RUNNERS_DIET);
            }
            if(rs.getBoolean("is_divine_escape_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.DIVINE_ESCAPE);
            }

            abilityMap.put(DefaultAbilities.ROLL, roll);
            abilityMap.put(UnlockedAbilities.THICK_SKIN, thickSkin);
            abilityMap.put(UnlockedAbilities.BULLET_PROOF, bulletProof);
            abilityMap.put(UnlockedAbilities.DODGE, dodge);
            abilityMap.put(UnlockedAbilities.IRON_MUSCLES, ironMuscles);
            abilityMap.put(UnlockedAbilities.RUNNERS_DIET, runnersDiet);
            abilityMap.put(UnlockedAbilities.DIVINE_ESCAPE, divineEscape);
            //Create skill
            Fitness fitness = new Fitness(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(fitness);
          }
          //init excavation
          else if(skill.equals(Skills.EXCAVATION)) {
            Extraction extraction = new Extraction(rs.getBoolean("is_extraction_toggled"));
            BuriedTreasure buriedTreasure = new BuriedTreasure(rs.getBoolean("is_buried_treasure_toggled"), rs.getInt("buried_treasure_tier"));
            LargerSpade largerSpade = new LargerSpade(rs.getBoolean("is_larger_spade_toggled"), rs.getInt("larger_spade_tier"));
            ManaDeposit manaDeposit = new ManaDeposit(rs.getBoolean("is_mana_deposit_toggled"), rs.getInt("mana_deposit_tier"));
            HandDigging handDigging = new HandDigging(rs.getBoolean("is_hand_digging_toggled"), rs.getInt("hand_digging_tier"));
            PansShrine pansShrine = new PansShrine(rs.getBoolean("is_pans_shrine_toggled"), rs.getInt("pans_shrine_tier"));
            FrenzyDig frenzyDig = new FrenzyDig(rs.getBoolean("is_frenzy_dig_toggled"), rs.getInt("frenzy_dig_tier"));

            int handDiggingCooldown = rs.getInt("hand_digging_cooldown");
            int pansShrineCooldown = rs.getInt("pans_shrine_cooldown");
            int frenzyDigCooldown = rs.getInt("frenzy_dig_cooldown");

            if(handDiggingCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, handDiggingCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.HAND_DIGGING, cal.getTimeInMillis());
            }
            if(pansShrineCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, pansShrineCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.PANS_SHRINE, cal.getTimeInMillis());
            }
            if(frenzyDigCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, frenzyDigCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.FRENZY_DIG, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_buried_treasure_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BURIED_TREASURE);
            }
            if(rs.getBoolean("is_larger_spade_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.LARGER_SPADE);
            }
            if(rs.getBoolean("is_mana_deposit_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.MANA_DEPOSIT);
            }
            if(rs.getBoolean("is_hand_digging_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.HAND_DIGGING);
            }
            if(rs.getBoolean("is_frenzy_dig_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.FRENZY_DIG);
            }
            if(rs.getBoolean("is_pans_shrine_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.PANS_SHRINE);
            }
            abilityMap.put(DefaultAbilities.EXTRACTION, extraction);
            abilityMap.put(UnlockedAbilities.BURIED_TREASURE, buriedTreasure);
            abilityMap.put(UnlockedAbilities.LARGER_SPADE, largerSpade);
            abilityMap.put(UnlockedAbilities.MANA_DEPOSIT, manaDeposit);
            abilityMap.put(UnlockedAbilities.HAND_DIGGING, handDigging);
            abilityMap.put(UnlockedAbilities.FRENZY_DIG, frenzyDig);
            abilityMap.put(UnlockedAbilities.PANS_SHRINE, pansShrine);
            Excavation excavation = new Excavation(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(excavation);
          }
          //init axes
          else if(skill.equals(Skills.AXES)) {
            Shred shred = new Shred(rs.getBoolean("is_shred_toggled"));
            HeavyStrike heavyStrike = new HeavyStrike(rs.getBoolean("is_heavy_strike_toggled"), rs.getInt("heavy_strike_tier"));
            BloodFrenzy bloodFrenzy = new BloodFrenzy(rs.getBoolean("is_blood_frenzy_toggled"), rs.getInt("blood_frenzy_tier"));
            SharperAxe sharperAxe = new SharperAxe(rs.getBoolean("is_sharper_axe_toggled"), rs.getInt("sharper_axe_tier"));
            WhirlwindStrike whirlwindStrike = new WhirlwindStrike(rs.getBoolean("is_whirlwind_strike_toggled"), rs.getInt("whirlwind_strike_tier"));
            AresBlessing aresBlessing = new AresBlessing(rs.getBoolean("is_ares_blessing_toggled"), rs.getInt("ares_blessing_tier"));
            CripplingBlow cripplingBlow = new CripplingBlow(rs.getBoolean("is_crippling_blow_toggled"), rs.getInt("crippling_blow_tier"));

            int whirlwindStrikeCooldown = rs.getInt("whirlwind_strike_cooldown");
            int aresBlessingCooldown = rs.getInt("ares_blessing_cooldown");
            int cripplingBlowCooldown = rs.getInt("crippling_blow_cooldown");

            if(whirlwindStrikeCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, whirlwindStrikeCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.WHIRLWIND_STRIKE, cal.getTimeInMillis());
            }
            if(aresBlessingCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, aresBlessingCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.ARES_BLESSING, cal.getTimeInMillis());
            }
            if(cripplingBlowCooldown > 0) {
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.SECOND, cripplingBlowCooldown);
              abilitiesOnCooldown.put(UnlockedAbilities.CRIPPLING_BLOW, cal.getTimeInMillis());
            }

            if(rs.getBoolean("is_heavy_strike_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.HEAVY_STRIKE);
            }
            if(rs.getBoolean("is_blood_frenzy_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.BLOOD_FRENZY);
            }
            if(rs.getBoolean("is_sharper_axe_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SHARPER_AXE);
            }
            if(rs.getBoolean("is_whirlwind_strike_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.WHIRLWIND_STRIKE);
            }
            if(rs.getBoolean("is_ares_blessing_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.ARES_BLESSING);
            }
            if(rs.getBoolean("is_crippling_blow_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.CRIPPLING_BLOW);
            }
            abilityMap.put(DefaultAbilities.SHRED, shred);
            abilityMap.put(UnlockedAbilities.HEAVY_STRIKE, heavyStrike);
            abilityMap.put(UnlockedAbilities.BLOOD_FRENZY, bloodFrenzy);
            abilityMap.put(UnlockedAbilities.SHARPER_AXE, sharperAxe);
            abilityMap.put(UnlockedAbilities.WHIRLWIND_STRIKE, whirlwindStrike);
            abilityMap.put(UnlockedAbilities.ARES_BLESSING, aresBlessing);
            abilityMap.put(UnlockedAbilities.CRIPPLING_BLOW, cripplingBlow);
            Axes axes = new Axes(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(axes);
          }
          //init fishing
          else if(skill.equals(Skills.FISHING)) {
            GreatRod greatRod = new GreatRod(rs.getBoolean("is_great_rod_toggled"));
            PoseidonsFavor poseidonsFavor = new PoseidonsFavor(rs.getBoolean("is_poseidons_favor_toggled"), rs.getInt("poseidons_favor_tier"));
            MagicTouch magicTouch = new MagicTouch(rs.getBoolean("is_magic_touch_toggled"), rs.getInt("magic_touch_tier"));
            SeaGodsBlessing seaGodsBlessing = new SeaGodsBlessing(rs.getBoolean("is_sea_gods_blessing_toggled"), rs.getInt("sea_gods_blessing_tier"));
            SunkenArmory sunkenArmory = new SunkenArmory(rs.getBoolean("is_sunken_armory_toggled"), rs.getInt("sunken_armory_tier"));
            Shake shake = new Shake(rs.getBoolean("is_shake_toggled"), rs.getInt("shake_tier"));
            SuperRod superRod = new SuperRod(rs.getBoolean("is_super_rod_toggled"), rs.getInt("super_rod_tier"));

            if(rs.getBoolean("is_poseidons_favor_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.POSEIDONS_FAVOR);
            }
            if(rs.getBoolean("is_magic_touch_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.MAGIC_TOUCH);
            }
            if(rs.getBoolean("is_sea_gods_blessing_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SEA_GODS_BLESSING);
            }
            if(rs.getBoolean("is_sunken_armory_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SUNKEN_ARMORY);
            }
            if(rs.getBoolean("is_shake_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SHAKE);
            }
            if(rs.getBoolean("is_super_rod_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.SUPER_ROD);
            }
            abilityMap.put(DefaultAbilities.GREAT_ROD, greatRod);
            abilityMap.put(UnlockedAbilities.POSEIDONS_FAVOR, poseidonsFavor);
            abilityMap.put(UnlockedAbilities.MAGIC_TOUCH, magicTouch);
            abilityMap.put(UnlockedAbilities.SEA_GODS_BLESSING, seaGodsBlessing);
            abilityMap.put(UnlockedAbilities.SUNKEN_ARMORY, sunkenArmory);
            abilityMap.put(UnlockedAbilities.SHAKE, shake);
            abilityMap.put(UnlockedAbilities.SUPER_ROD, superRod);
            Fishing fishing = new Fishing(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(fishing);
          }
          //init sorcery
          else if(skill.equals(Skills.SORCERY)) {
            HastyBrew hastyBrew = new HastyBrew(rs.getBoolean("is_hasty_brew_toggled"));
            CircesRecipes circesRecipes = new CircesRecipes(rs.getBoolean("is_circes_recipes_toggled"), rs.getInt("circes_recipes_tier"));
            PotionAffinity potionAffinity = new PotionAffinity(rs.getBoolean("is_potion_affinity_toggled"), rs.getInt("potion_affinity_tier"));
            ManaAffinity manaAffinity = new ManaAffinity(rs.getBoolean("is_mana_affinity_toggled"), rs.getInt("mana_affinity_tier"));
            CircesProtection circesProtection = new CircesProtection(rs.getBoolean("is_circes_protection_toggled"), rs.getInt("circes_protection_tier"));
            HadesDomain hadesDomain = new HadesDomain(rs.getBoolean("is_hades_domain_toggled"), rs.getInt("hades_domain_tier"));
            CircesShrine circesShrine = new CircesShrine(rs.getBoolean("is_circes_shrine_toggled"), rs.getInt("circes_shrine_tier"));

            if(rs.getBoolean("is_circes_recipes_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.CIRCES_RECIPES);
            }
            if(rs.getBoolean("is_potion_affinity_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.POTION_AFFINITY);
            }
            if(rs.getBoolean("is_mana_affinity_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.MANA_AFFINITY);
            }
            if(rs.getBoolean("is_circes_protection_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.CIRCES_PROTECTION);
            }
            if(rs.getBoolean("is_hades_domain_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.HADES_DOMAIN);
            }
            if(rs.getBoolean("is_circes_shrine_pending")) {
              pendingUnlockAbilities.add(UnlockedAbilities.CIRCES_SHRINE);
            }
            abilityMap.put(DefaultAbilities.HASTY_BREW, hastyBrew);
            abilityMap.put(UnlockedAbilities.CIRCES_RECIPES, circesRecipes);
            abilityMap.put(UnlockedAbilities.POTION_AFFINITY, potionAffinity);
            abilityMap.put(UnlockedAbilities.MANA_AFFINITY, manaAffinity);
            abilityMap.put(UnlockedAbilities.CIRCES_PROTECTION, circesProtection);
            abilityMap.put(UnlockedAbilities.HADES_DOMAIN, hadesDomain);
            abilityMap.put(UnlockedAbilities.CIRCES_SHRINE, circesShrine);
            Sorcery sorcery = new Sorcery(rs.getInt("current_level"),
                    rs.getInt("current_exp"), abilityMap, this);
            skills.add(sorcery);
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
    List<UnlockedAbilities> toremove = new ArrayList<>();
    for(UnlockedAbilities a : abilityLoadout){
      BaseAbility ab = getBaseAbility(a);
      if(ab.getCurrentTier() < 1){
        ab.setUnlocked(false);
        toremove.add(a);
      }
    }
    for(UnlockedAbilities a : toremove){
      abilityLoadout.remove(a);
    }
  }

  public OfflinePlayer getOfflineMcMMOPlayer() {
    return Bukkit.getOfflinePlayer(uuid);
  }

  public boolean isReadying(){
    if(this.readyingAbilityBit == null || this.readyingAbilityBit.getAbilityReady() == null){
      this.isReadying = false;
    }
    return isReadying;
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
    return ability != null ? getSkill(ability.getSkill()).getAbility(ability) : null;
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

  public void removeAbilityOnCooldown(Skills skill){
    UnlockedAbilities remove = null;
    for(UnlockedAbilities ab : abilitiesOnCooldown.keySet()) {
      if(ab.getSkill().equalsIgnoreCase(skill.getName())) {
        remove = ab;
        break;
      }
    }
    if(remove != null){
      abilitiesOnCooldown.remove(remove);
    }
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
      if(!abilityLoadout.contains(ability)){
        toRemove.add(ability);
        continue;
      }
      long timeToEnd = abilitiesOnCooldown.get(ability);
      if(Calendar.getInstance().getTimeInMillis() >= timeToEnd) {
        if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
          this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                  McRPG.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
        }
        toRemove.add(ability);
      }
      else if(timeToEnd <= 0L){
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
    if(endTimeForReplaceCooldown != 0 && Calendar.getInstance().getTimeInMillis() >= endTimeForReplaceCooldown) {
      this.endTimeForReplaceCooldown = 0;
      if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
        this.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() +
                McRPG.getInstance().getLangFile().getString("Messages.Players.ReplaceCooldownExpire")));
      }
      database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown_time = 0 WHERE uuid = '" + uuid.toString() + "'");
    }
    if(divineEscapeExpEnd != 0 && divineEscapeExpEnd <= Calendar.getInstance().getTimeInMillis()){
      divineEscapeExpEnd = 0;
      divineEscapeExpDebuff = 0;
      if(Bukkit.getOfflinePlayer(uuid).isOnline()){
        getPlayer().sendMessage(Methods.color(getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DivineEscape.ExpDebuffExpire")));
        }
      }
    if(divineEscapeDamageEnd != 0 && divineEscapeDamageEnd <= Calendar.getInstance().getTimeInMillis()){
      divineEscapeDamageEnd = 0;
      divineEscapeDamageDebuff = 0;
      if(Bukkit.getOfflinePlayer(uuid).isOnline()){
        getPlayer().sendMessage(Methods.color(getPlayer(), McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DivineEscape.DamageDebuffExpire")));
      }
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
                + "_cooldown = 0 WHERE uuid = '" + uuid.toString() + "'");

      }
    }
    abilitiesOnCooldown.clear();
    endTimeForReplaceCooldown = 0;
    database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown_time = 0 WHERE uuid = `" + uuid.toString() + "`");
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
      String query = "UPDATE mcrpg_" + skill.getName().toLowerCase() + "_data SET current_level = " + skill.getCurrentLevel() + ", current_exp = " + skill.getCurrentExp();
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
      database.executeUpdate("UPDATE mcrpg_player_data SET replace_ability_cooldown_time = " + endTimeForReplaceCooldown + " WHERE uuid = '" + uuid.toString() + "'");
    }
    database.executeUpdate("UPDATE mcrpg_player_data SET ability_points = " + abilityPoints + ", power_level = " + powerLevel + ", redeemable_exp = " + redeemableExp + ", redeemable_levels = " + redeemableLevels + ", boosted_exp = " + boostedExp + ", divine_escape_exp_debuff = " + divineEscapeExpDebuff
            + ", divine_escape_damage_debuff = " + divineEscapeDamageDebuff + ", divine_escape_exp_end_time = " + divineEscapeExpEnd +
            ", divine_escape_damage_end_time = " + divineEscapeDamageEnd + " WHERE uuid = '" + uuid.toString() + "'");
    String query = "UPDATE mcrpg_player_settings SET require_empty_offhand = " + Methods.convertBool(requireEmptyOffHand) + ", keep_hand = " + Methods.convertBool(keepHandEmpty)
            + ", ignore_tips = " + Methods.convertBool(ignoreTips) + ", auto_deny = " + Methods.convertBool(autoDeny) + ", display_type = '" + displayType.getName() +
            "', health_type = '" + healthbarType.getName() + "', unarmed_ignore_slot = " + unarmedIgnoreSlot + " WHERE uuid = '" + uuid.toString() + "'";
    database.executeUpdate(query);
    for(UnlockedAbilities ability : pendingUnlockAbilities) {
      query = "UPDATE mcrpg_" + ability.getSkill().toLowerCase() + "_data SET is_" + Methods.convertNameToSQL(ability.getName().replace(" ", "").replace("_", "").replace("+", "Plus")) + "_pending = 1" +
              " WHERE uuid = '" + uuid.toString() + "'";
      database.executeUpdate(query);
    }
    String loadoutQuery = "UPDATE mcrpg_loadout SET";
    for(int i = 1; i <= McRPG.getInstance().getConfig().getInt("PlayerConfiguration.AmountOfTotalAbilities"); i++) {
      if(i != 1) {
        loadoutQuery += ",";
      }
      String toAdd = "null";
      loadoutQuery += " Slot" + i + " = '" + (abilityLoadout.size() >= i ? abilityLoadout.get(i - 1).getName() : toAdd) + "'";

    }
    loadoutQuery += " WHERE uuid = '" + uuid.toString() + "'";
    database.executeUpdate(loadoutQuery);

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

  public boolean isPlayerOnline(){
    return Bukkit.getOfflinePlayer(uuid).isOnline();
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