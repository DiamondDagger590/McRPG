package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
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
import us.eunoians.mcrpg.abilities.taming.Comradery;
import us.eunoians.mcrpg.abilities.taming.DivineFur;
import us.eunoians.mcrpg.abilities.taming.FuryOfCerberus;
import us.eunoians.mcrpg.abilities.taming.LinkedFangs;
import us.eunoians.mcrpg.abilities.taming.PETAsWrath;
import us.eunoians.mcrpg.abilities.taming.SharpenedFangs;
import us.eunoians.mcrpg.abilities.unarmed.*;
import us.eunoians.mcrpg.abilities.woodcutting.*;
import us.eunoians.mcrpg.api.util.Methods;

import java.io.File;
import java.util.Arrays;

import static us.eunoians.mcrpg.api.util.FileManager.Files;

/**
 * An enum of every ability that can be unlocked
 */
public enum UnlockedAbilities implements GenericAbility{
  
  ARES_BLESSING("AresBlessing", AresBlessing.class, AbilityType.ACTIVE, Files.AXES_CONFIG, true),
  BERSERK("Berserk", Berserk.class, AbilityType.ACTIVE, Files.UNARMED_CONFIG, true),
  BLAST_MINING("BlastMining", BlastMining.class, AbilityType.ACTIVE, Files.MINING_CONFIG, true),
  BLEED_PLUS("Bleed+", BleedPlus.class, AbilityType.PASSIVE, Files.SWORDS_CONFIG, false),
  BLESSING_OF_APOLLO("BlessingOfApollo", BlessingOfApollo.class, AbilityType.ACTIVE, Files.ARCHERY_CONFIG, true),
  BLESSING_OF_ARTEMIS("BlessingOfArtemis", BlessingOfArtemis.class, AbilityType.ACTIVE, Files.ARCHERY_CONFIG, true),
  BLOOD_FRENZY("BloodFrenzy", BloodFrenzy.class, AbilityType.PASSIVE, Files.AXES_CONFIG, false),
  BULLET_PROOF("BulletProof", BulletProof.class, AbilityType.PASSIVE, Files.FITNESS_CONFIG, false),
  BURIED_TREASURE("BuriedTreasure", BuriedTreasure.class, AbilityType.PASSIVE, Files.EXCAVATION_CONFIG, false),
  CIRCES_PROTECTION("CircesProtection", CircesProtection.class, AbilityType.PASSIVE, Files.SORCERY_CONFIG, false),
  CIRCES_RECIPES("CircesRecipes", CircesRecipes.class, AbilityType.PASSIVE, Files.SORCERY_CONFIG, false),
  CIRCES_SHRINE("CircesShrine", CircesShrine.class, AbilityType.ACTIVE, Files.SORCERY_CONFIG, false),
  COMBO("Combo", Combo.class, AbilityType.PASSIVE, Files.ARCHERY_CONFIG, false),
  COMRADERY("Comradery", Comradery.class, AbilityType.PASSIVE, Files.TAMING_CONFIG, false),
  CRIPPLING_BLOW("CripplingBlow", CripplingBlow.class, AbilityType.ACTIVE, Files.AXES_CONFIG, true),
  CURSE_OF_HADES("CurseOfHades", CurseOfHades.class, AbilityType.ACTIVE, Files.ARCHERY_CONFIG, true),
  DEEPER_WOUND("DeeperWound", DeeperWound.class, AbilityType.PASSIVE, Files.SWORDS_CONFIG, false),
  DEMETERS_SHRINE("DemetersShrine", DemetersShrine.class, AbilityType.ACTIVE, Files.WOODCUTTING_CONFIG, true),
  DENSE_IMPACT("DenseImpact", DenseImpact.class, AbilityType.ACTIVE, Files.UNARMED_CONFIG, true),
  DIAMOND_FLOWERS("DiamondFlowers", DiamondFlowers.class, AbilityType.PASSIVE, Files.HERBALISM_CONFIG, false),
  DISARM("Disarm", Disarm.class, AbilityType.PASSIVE, Files.UNARMED_CONFIG, false),
  DIVINE_ESCAPE("DivineEscape", DivineEscape.class, AbilityType.PASSIVE, Files.FITNESS_CONFIG, true),
  DIVINE_FUR("DivineFur", DivineFur.class, AbilityType.PASSIVE, Files.TAMING_CONFIG, false),
  DODGE("Dodge", Dodge.class, AbilityType.PASSIVE, Files.FITNESS_CONFIG, false),
  DRYADS_GIFT("DryadsGift", DryadsGift.class, AbilityType.PASSIVE, Files.WOODCUTTING_CONFIG, false),
  FARMERS_DIET("FarmersDiet", FarmersDiet.class, AbilityType.PASSIVE, Files.HERBALISM_CONFIG, false),
  FRENZY_DIG("FrenzyDig", FrenzyDig.class, AbilityType.ACTIVE, Files.EXCAVATION_CONFIG, true),
  FURY_OF_CERBERUS("FuryOfCerberus", FuryOfCerberus.class, AbilityType.ACTIVE, Files.TAMING_CONFIG, false),
  HADES_DOMAIN("HadesDomain", HadesDomain.class, AbilityType.PASSIVE, Files.SORCERY_CONFIG, false),
  HAND_DIGGING("HandDigging", HandDigging.class, AbilityType.ACTIVE, Files.EXCAVATION_CONFIG, true),
  HEAVY_STRIKE("HeavyStrike", HeavyStrike.class, AbilityType.PASSIVE, Files.AXES_CONFIG, false),
  HEAVY_SWING("HeavySwing", HeavySwing.class, AbilityType.PASSIVE, Files.WOODCUTTING_CONFIG, false),
  HESPERIDES_APPLES("HesperidesApples", HesperidesApples.class, AbilityType.ACTIVE, Files.WOODCUTTING_CONFIG, true),
  IRON_ARM("IronArm", IronArm.class, AbilityType.PASSIVE, Files.UNARMED_CONFIG, false),
  IRON_MUSCLES("IronMuscles", IronMuscles.class, AbilityType.PASSIVE, Files.FITNESS_CONFIG, false),
  ITS_A_TRIPLE("ItsATriple", ItsATriple.class, AbilityType.PASSIVE, Files.MINING_CONFIG, false),
  LARGER_SPADE("LargerSpade", LargerSpade.class, AbilityType.PASSIVE, Files.EXCAVATION_CONFIG, false),
  LINKED_FANGS("LinkedFangs", LinkedFangs.class, AbilityType.PASSIVE, Files.TAMING_CONFIG, false),
  MAGIC_TOUCH("MagicTouch", MagicTouch.class, AbilityType.PASSIVE, Files.FISHING_CONFIG, false),
  MANA_AFFINITY("ManaAffinity", ManaAffinity.class, AbilityType.PASSIVE, Files.SORCERY_CONFIG, false),
  MANA_DEPOSIT("ManaDeposit", ManaDeposit.class, AbilityType.PASSIVE, Files.EXCAVATION_CONFIG, false),
  MASS_HARVEST("MassHarvest", MassHarvest.class, AbilityType.ACTIVE, Files.HERBALISM_CONFIG, true),
  NATURES_WRATH("NaturesWrath", NaturesWrath.class, AbilityType.ACTIVE, Files.HERBALISM_CONFIG, false),
  NYMPHS_VITALITY("NymphsVitality", NymphsVitality.class, AbilityType.PASSIVE, Files.WOODCUTTING_CONFIG, false),
  ORE_SCANNER("OreScanner", OreScanner.class, AbilityType.ACTIVE, Files.MINING_CONFIG, true),
  PANS_BLESSING("PansBlessing", PansBlessing.class, AbilityType.ACTIVE, Files.HERBALISM_CONFIG, true),
  PANS_SHRINE("PansShrine", PansShrine.class, AbilityType.ACTIVE, Files.EXCAVATION_CONFIG, true),
  PETAS_WRATH("PETAsWrath", PETAsWrath.class, AbilityType.PASSIVE, Files.TAMING_CONFIG, false),
  POSEIDONS_FAVOR("PoseidonsFavor", PoseidonsFavor.class, AbilityType.PASSIVE, Files.FISHING_CONFIG, false),
  POTION_AFFINITY("PotionAffinity", PotionAffinity.class, AbilityType.PASSIVE, Files.SORCERY_CONFIG, false),
  PUNCTURE("Puncture", Puncture.class, AbilityType.PASSIVE, Files.ARCHERY_CONFIG, false),
  RAGE_SPIKE("RageSpike", RageSpike.class, AbilityType.ACTIVE, Files.SWORDS_CONFIG, true),
  REMOTE_TRANSFER("RemoteTransfer", RemoteTransfer.class, AbilityType.PASSIVE, Files.MINING_CONFIG, false),
  REPLANTING("Replanting", Replanting.class, AbilityType.PASSIVE, Files.HERBALISM_CONFIG, false),
  RICHER_ORES("RicherOres", RicherOres.class, AbilityType.PASSIVE, Files.MINING_CONFIG, false),
  RUNNERS_DIET("RunnersDiet", RunnersDiet.class, AbilityType.PASSIVE, Files.FITNESS_CONFIG, false),
  SEA_GODS_BLESSING("SeaGodsBlessing", SeaGodsBlessing.class, AbilityType.PASSIVE, Files.FISHING_CONFIG, false),
  SERRATED_STRIKES("SerratedStrikes", SerratedStrikes.class, AbilityType.ACTIVE, Files.SWORDS_CONFIG, true),
  SHAKE("Shake", Shake.class, AbilityType.PASSIVE, Files.FISHING_CONFIG, false),
  SHARPENED_FANGS("SharpenedFangs", SharpenedFangs.class, AbilityType.PASSIVE, Files.TAMING_CONFIG, false),
  SHARPER_AXE("SharperAxe", SharperAxe.class, AbilityType.PASSIVE, Files.AXES_CONFIG, false),
  SMITING_FIST("SmitingFist", SmitingFist.class, AbilityType.ACTIVE, Files.UNARMED_CONFIG, true),
  SUNKEN_ARMORY("SunkenArmory", SunkenArmory.class, AbilityType.PASSIVE, Files.FISHING_CONFIG, false),
  SUPER_BREAKER("SuperBreaker", SuperBreaker.class, AbilityType.ACTIVE, Files.MINING_CONFIG, true),
  SUPER_ROD("SuperRod", SuperRod.class, AbilityType.PASSIVE, Files.FISHING_CONFIG, false),
  TAINTED_BLADE("TaintedBlade", TaintedBlade.class, AbilityType.ACTIVE, Files.SWORDS_CONFIG, true),
  TEMPORAL_HARVEST("TemporalHarvest", TemporalHarvest.class, AbilityType.ACTIVE, Files.WOODCUTTING_CONFIG, true),
  THICK_SKIN("ThickSkin", ThickSkin.class, AbilityType.PASSIVE, Files.FITNESS_CONFIG, false),
  TIGHTER_GRIP("TighterGrip", TighterGrip.class, AbilityType.PASSIVE, Files.UNARMED_CONFIG, false),
  TIPPED_ARROWS("TippedArrows", TippedArrows.class, AbilityType.PASSIVE, Files.ARCHERY_CONFIG, false),
  VAMPIRE("Vampire", Vampire.class, AbilityType.PASSIVE, Files.SWORDS_CONFIG, false),
  WHIRLWIND_STRIKE("WhirlwindStrike", WhirlwindStrike.class, AbilityType.ACTIVE, Files.AXES_CONFIG, true);
  
  @Getter
  private String name;
  @Getter
  private Class<? extends BaseAbility> clazz;
  @Getter
  private AbilityType abilityType;
  private Files file;
  @Getter
  private boolean cooldown;
  
  /**
   * @param name
   * @param type
   * @param config
   */
  UnlockedAbilities(String name, Class<? extends BaseAbility> clazz, AbilityType type, Files config, boolean cooldown){
    this.name = name;
    this.clazz = clazz;
    this.abilityType = type;
    this.file = config;
    this.cooldown = cooldown;
  }
//TODO FIX FILES HERE BOI
  
  public Skills getSkill(){
    return Arrays.stream(Skills.values()).filter(skill -> Arrays.asList(skill.getUnlockedAbilities()).contains(this)).findFirst().orElse(null);
  }
  
  /**
   * Check if the ability is passive or not
   *
   * @return true if the ability if passive and false if it is active
   */
  public boolean isPassiveAbility(){
    return abilityType.equals(AbilityType.PASSIVE);
  }
  
  /**
   * Check if the ability is enabled or disabled
   *
   * @return ture if the ability is enabled or false if the ability is disabled
   */
  @Override
  public boolean isEnabled(){
    return file.getFile().getBoolean("EnabledAbilities." + name);
  }
  
  /**
   * @param ability The name of the ability you want the instance of
   * @return The instance of the unlocked ability if a correct name is provided or null if the ability provided does not exist
   */
  public static UnlockedAbilities fromString(String ability){
    if(ability.contains("bleed") && ability.contains("plus")){
      return BLEED_PLUS;
    }
    return Arrays.stream(UnlockedAbilities.values()).filter(ab -> ab.getName().equalsIgnoreCase(ability)).findAny().orElse(null);
  }
  
  /**
   * Get the level an ability is unlocked
   *
   * @return The integer representation of when the ability is unlocked
   */
  public int getUnlockLevel(){
    return file.getFile().getInt("UnlockLevelForAbility." + name);
  }
  
  /**
   * Check if the ability should use the permission ability.use.* or ability.use.{ability_name}
   *
   * @return true if the permission system is meant to be used for this ability or false if not
   */
  public boolean usePerm(){
    return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(),
      File.separator + "skills" + File.separator + this.getSkill().getName() + ".yml")).getBoolean("UsePermsForAbility." + name);
  }
  
  /**
   * Get the max tier an ability can reach. This should not exceed 5 but the limit is not hardcoded
   *
   * @return The max tier an ability can reach
   */
  public int getMaxTier(){
    return file.getFile().getInt(name + "Config.TierAmount");
  }
  
  /**
   * Check what level a skill should be to unlock the provided tier
   *
   * @param tier The tier you want to check
   * @return The level that the tier is unlocked or -1 if that tier doesnt exist
   */
  public int tierUnlockLevel(int tier){
    return file.getFile().getInt(this.name + "Config.TierUpgrade.Tier" + Methods.convertToNumeral(tier));
  }
  
  public static boolean isAbility(String ability){
    return Arrays.stream(values()).anyMatch(ab -> ab.getName().equalsIgnoreCase(ability));
  }
  
  public String getDisplayName(){
    char[] chars = name.toCharArray();
    StringBuilder string = new StringBuilder();
    boolean first = true;
    for(char s : chars){
      if(!first){
        if(Character.isUpperCase(s)){
          string.append(" ");
        }
      }
      else{
        first = false;
      }
      string.append(s);
    }
    return string.toString();
  }
  
  public String getLocalizedName(){
    return "";
  }
}
