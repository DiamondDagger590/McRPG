package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;

import java.io.File;
import java.util.Arrays;

/**
 * An enum of every ability that can be unlocked
 */
public enum UnlockedAbilities implements GenericAbility {
  BERSERK("Berserk", "Unarmed", AbilityType.ACTIVE, FileManager.Files.UNARMED_CONFIG),
  BLAST_MINING("BlastMining", "Mining", AbilityType.ACTIVE, FileManager.Files.MINING_CONFIG),
  BLEED_PLUS("Bleed+", "Swords", AbilityType.PASSIVE, FileManager.Files.SWORDS_CONFIG),
  DEEPER_WOUND("DeeperWound", "Swords", AbilityType.PASSIVE, FileManager.Files.SWORDS_CONFIG),
  DENSE_IMPACT("DenseImpact", "Unarmed", AbilityType.ACTIVE, FileManager.Files.UNARMED_CONFIG),
  DIAMOND_FLOWERS("DiamondFlowers", "Herbalism", AbilityType.PASSIVE, FileManager.Files.HERBALISM_CONFIG),
  DISARM("Disarm", "Unarmed", AbilityType.PASSIVE, FileManager.Files.UNARMED_CONFIG),
  FARMERS_DIET("FarmersDiet", "Herbalism", AbilityType.PASSIVE, FileManager.Files.HERBALISM_CONFIG),
  IRON_ARM("IronArm", "Unarmed", AbilityType.PASSIVE, FileManager.Files.UNARMED_CONFIG),
  ITS_A_TRIPLE("ItsATriple", "Mining", AbilityType.PASSIVE, FileManager.Files.MINING_CONFIG),
  MASS_HARVEST("MassHarvest", "Herbalism", AbilityType.ACTIVE, FileManager.Files.HERBALISM_CONFIG),
  NATURES_WRATH("NaturesWrath", "Herbalism", AbilityType.ACTIVE, FileManager.Files.HERBALISM_CONFIG),
  ORE_SCANNER("OreScanner", "Mining", AbilityType.ACTIVE, FileManager.Files.MINING_CONFIG),
  PANS_BLESSING("PansBlessing", "Herbalism", AbilityType.ACTIVE, FileManager.Files.HERBALISM_CONFIG),
  RAGE_SPIKE("RageSpike", "Swords", AbilityType.ACTIVE, FileManager.Files.SWORDS_CONFIG),
  REMOTE_TRANSFER("RemoteTransfer", "Mining", AbilityType.PASSIVE, FileManager.Files.MINING_CONFIG),
  REPLANTING("Replanting", "Herbalism", AbilityType.PASSIVE, FileManager.Files.HERBALISM_CONFIG),
  RICHER_ORES("RicherOres", "Mining", AbilityType.PASSIVE, FileManager.Files.MINING_CONFIG),
  SERRATED_STRIKES("SerratedStrikes", "Swords", AbilityType.ACTIVE, FileManager.Files.SWORDS_CONFIG),
  SMITING_FIST("SmitingFist", "Unarmed", AbilityType.ACTIVE, FileManager.Files.UNARMED_CONFIG),
  SUPER_BREAKER("SuperBreaker", "Mining", AbilityType.ACTIVE, FileManager.Files.MINING_CONFIG),
  TAINTED_BLADE("TaintedBlade", "Swords", AbilityType.ACTIVE, FileManager.Files.SWORDS_CONFIG),
  TIGHTER_GRIP("TighterGrip", "Unarmed", AbilityType.PASSIVE, FileManager.Files.UNARMED_CONFIG),
  VAMPIRE("Vampire", "Swords", AbilityType.PASSIVE, FileManager.Files.SWORDS_CONFIG);

  @Getter
  private String name;
  @Getter
  private String skill;
  @Getter
  private AbilityType abilityType;
  private FileManager.Files file;

  /**
   * @param name
   * @param skill
   * @param type
   * @param config
   */
  UnlockedAbilities(String name, String skill, AbilityType type, FileManager.Files config){
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
	return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(),
		File.separator + "skills" + File.separator + this.skill.toLowerCase() + ".yml")).getBoolean("EnabledAbilities." + name);
  }

  /**
   * @param ability The name of the ability you want the instance of
   * @return The instance of the unlocked ability if a correct name is provided or null if the ability provided does not exist
   */
  public static UnlockedAbilities fromString(String ability){
	return Arrays.stream(UnlockedAbilities.values()).filter(ab -> ab.getName().equalsIgnoreCase(ability)).findAny().orElse(null);
  }

  /**
   * Get the level an ability is unlocked
   *
   * @return The integer representation of when the ability is unlocked
   */
  public int getUnlockLevel(){
	return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(),
		File.separator + "skills" + File.separator + this.skill.toLowerCase() + ".yml")).getInt("UnlockLevelForAbility." + name);
  }

  /**
   * Check if the ability should use the permission ability.use.* or ability.use.{ability_name}
   *
   * @return true if the permission system is meant to be used for this ability or false if not
   */
  public boolean usePerm(){
	return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(),
		File.separator + "skills" + File.separator + this.skill.toLowerCase() + ".yml")).getBoolean("UsePermsForAbility." + name);
  }

  /**
   * Get the max tier an ability can reach. This should not exceed 5 but the limit is not hardcoded
   *
   * @return The max tier an ability can reach
   */
  public int getMaxTier(){
	return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(),
		File.separator + "skills" + File.separator + this.skill.toLowerCase() + ".yml")).getInt(name + "Config.TierAmount");
  }

  /**
   * Check what level a skill should be to unlock the provided tier
   *
   * @param tier The tier you want to check
   * @return The level that the tier is unlocked or -1 if that tier doesnt exist
   */
  public int tierUnlockLevel(int tier){
	return YamlConfiguration.loadConfiguration(new File(McRPG.getInstance().getDataFolder(), File.separator + "skills" + File.separator
		+ this.skill.toLowerCase() + ".yml")).getInt(this.name + "Config.TierUpgrade.Tier" + Methods.convertToNumeral(tier));
  }

  public static boolean isAbility(String ability){
	return Arrays.stream(values()).anyMatch(ab -> ab.getName().equalsIgnoreCase(ability));
  }
}
