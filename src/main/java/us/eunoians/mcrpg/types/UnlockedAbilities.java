package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;

import java.io.File;
import java.util.Arrays;

import static us.eunoians.mcrpg.api.util.FileManager.Files;

/**
 * An enum of every ability that can be unlocked
 */
public enum UnlockedAbilities implements GenericAbility {

  BERSERK("Berserk", "Unarmed", AbilityType.ACTIVE, Files.UNARMED_CONFIG),
  BLAST_MINING("BlastMining", "Mining", AbilityType.ACTIVE, Files.MINING_CONFIG),
  BLEED_PLUS("Bleed+", "Swords", AbilityType.PASSIVE, Files.SWORDS_CONFIG),
  BLESSING_OF_APOLLO("BlessingOfApollo", "Archery", AbilityType.ACTIVE, Files.ARCHERY_CONFIG),
  BLESSING_OF_ARTEMIS("BlessingOfArtemis", "Archery", AbilityType.ACTIVE, Files.ARCHERY_CONFIG),
  COMBO("Combo", "Archery", AbilityType.PASSIVE, Files.ARCHERY_CONFIG),
  CURSE_OF_HADES("CurseOfHades", "Archery", AbilityType.ACTIVE, Files.ARCHERY_CONFIG),
  DEEPER_WOUND("DeeperWound", "Swords", AbilityType.PASSIVE, Files.SWORDS_CONFIG),
  DEMETERS_SHRINE("DemetersShrine", "Woodcutting", AbilityType.ACTIVE, Files.WOODCUTTING_CONFIG),
  DENSE_IMPACT("DenseImpact", "Unarmed", AbilityType.ACTIVE, Files.UNARMED_CONFIG),
  DIAMOND_FLOWERS("DiamondFlowers", "Herbalism", AbilityType.PASSIVE, Files.HERBALISM_CONFIG),
  DISARM("Disarm", "Unarmed", AbilityType.PASSIVE, Files.UNARMED_CONFIG),
  DRYADS_GIFT("DryadsGift", "Woodcutting", AbilityType.PASSIVE, Files.WOODCUTTING_CONFIG),
  FARMERS_DIET("FarmersDiet", "Herbalism", AbilityType.PASSIVE, Files.HERBALISM_CONFIG),
  HEAVY_SWING("HeavySwing", "Woodcutting", AbilityType.PASSIVE, Files.WOODCUTTING_CONFIG),
  HESPERIDES_APPLES("HesperidesApples", "Woodcutting", AbilityType.ACTIVE, Files.WOODCUTTING_CONFIG),
  IRON_ARM("IronArm", "Unarmed", AbilityType.PASSIVE, Files.UNARMED_CONFIG),
  ITS_A_TRIPLE("ItsATriple", "Mining", AbilityType.PASSIVE, Files.MINING_CONFIG),
  MASS_HARVEST("MassHarvest", "Herbalism", AbilityType.ACTIVE, Files.HERBALISM_CONFIG),
  NATURES_WRATH("NaturesWrath", "Herbalism", AbilityType.ACTIVE, Files.HERBALISM_CONFIG),
  NYMPHS_VITALITY("NymphsVitality", "Woodcutting", AbilityType.PASSIVE, Files.WOODCUTTING_CONFIG),
  ORE_SCANNER("OreScanner", "Mining", AbilityType.ACTIVE, Files.MINING_CONFIG),
  PANS_BLESSING("PansBlessing", "Herbalism", AbilityType.ACTIVE, Files.HERBALISM_CONFIG),
  PUNCTURE("Puncture", "Archery", AbilityType.PASSIVE, Files.ARCHERY_CONFIG),
  RAGE_SPIKE("RageSpike", "Swords", AbilityType.ACTIVE, Files.SWORDS_CONFIG),
  REMOTE_TRANSFER("RemoteTransfer", "Mining", AbilityType.PASSIVE, Files.MINING_CONFIG),
  REPLANTING("Replanting", "Herbalism", AbilityType.PASSIVE, Files.HERBALISM_CONFIG),
  RICHER_ORES("RicherOres", "Mining", AbilityType.PASSIVE, Files.MINING_CONFIG),
  SERRATED_STRIKES("SerratedStrikes", "Swords", AbilityType.ACTIVE, Files.SWORDS_CONFIG),
  SMITING_FIST("SmitingFist", "Unarmed", AbilityType.ACTIVE, Files.UNARMED_CONFIG),
  SUPER_BREAKER("SuperBreaker", "Mining", AbilityType.ACTIVE, Files.MINING_CONFIG),
  TAINTED_BLADE("TaintedBlade", "Swords", AbilityType.ACTIVE, Files.SWORDS_CONFIG),
  TEMPORAL_HARVEST("TemporalHarvest", "Woodcutting", AbilityType.ACTIVE, Files.WOODCUTTING_CONFIG),
  TIGHTER_GRIP("TighterGrip", "Unarmed", AbilityType.PASSIVE, Files.UNARMED_CONFIG),
  TIPPED_ARROWS("TippedArrows", "Archery", AbilityType.PASSIVE, Files.ARCHERY_CONFIG),
  VAMPIRE("Vampire", "Swords", AbilityType.PASSIVE, Files.SWORDS_CONFIG);

  @Getter
  private String name;
  @Getter
  private String skill;
  @Getter
  private AbilityType abilityType;
  private Files file;

  /**
   * @param name
   * @param skill
   * @param type
   * @param config
   */
  UnlockedAbilities(String name, String skill, AbilityType type, Files config) {
    this.name = name;
    this.skill = skill;
    this.abilityType = type;
    this.file = config;
  }
//TODO FIX FILES HERE BOI

  /**
   * Check if the ability is passive or not
   *
   * @return true if the ability if passive and false if it is active
   */
  public boolean isPassiveAbility() {
    return abilityType.equals(AbilityType.PASSIVE);
  }

  /**
   * Check if the ability is enabled or disabled
   *
   * @return ture if the ability is enabled or false if the ability is disabled
   */
  @Override
  public boolean isEnabled() {
    return file.getFile().getBoolean("EnabledAbilities." + name);
  }

  /**
   * @param ability The name of the ability you want the instance of
   * @return The instance of the unlocked ability if a correct name is provided or null if the ability provided does not exist
   */
  public static UnlockedAbilities fromString(String ability) {
    return Arrays.stream(UnlockedAbilities.values()).filter(ab -> ab.getName().equalsIgnoreCase(ability)).findAny().orElse(null);
  }

  /**
   * Get the level an ability is unlocked
   *
   * @return The integer representation of when the ability is unlocked
   */
  public int getUnlockLevel() {
    return file.getFile().getInt("UnlockLevelForAbility." + name);
  }

  /**
   * Check if the ability should use the permission ability.use.* or ability.use.{ability_name}
   *
   * @return true if the permission system is meant to be used for this ability or false if not
   */
  public boolean usePerm() {
    return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(),
            File.separator + "skills" + File.separator + this.skill.toLowerCase() + ".yml")).getBoolean("UsePermsForAbility." + name);
  }

  /**
   * Get the max tier an ability can reach. This should not exceed 5 but the limit is not hardcoded
   *
   * @return The max tier an ability can reach
   */
  public int getMaxTier() {
    return file.getFile().getInt(name + "Config.TierAmount");
  }

  /**
   * Check what level a skill should be to unlock the provided tier
   *
   * @param tier The tier you want to check
   * @return The level that the tier is unlocked or -1 if that tier doesnt exist
   */
  public int tierUnlockLevel(int tier) {
    return file.getFile().getInt(this.name + "Config.TierUpgrade.Tier" + Methods.convertToNumeral(tier));
  }

  public static boolean isAbility(String ability) {
    return Arrays.stream(values()).anyMatch(ab -> ab.getName().equalsIgnoreCase(ability));
  }

  public String getDisplayName() {
    char[] chars = name.toCharArray();
    StringBuilder string = new StringBuilder();
    boolean first = true;
    for(char s : chars) {
      if(!first) {
        if(Character.isUpperCase(s)) {
          string.append(" ");
        }
      }
      else {
        first = false;
      }
      string.append(s);
    }
    return string.toString();
  }
}
