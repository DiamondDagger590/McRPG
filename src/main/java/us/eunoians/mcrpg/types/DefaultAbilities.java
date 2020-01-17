package us.eunoians.mcrpg.types;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.util.Parser;

import java.util.Arrays;

/**
 * All abilities that come default with a skill should be stored in this enum
 */
public enum DefaultAbilities implements GenericAbility {
  BLEED("Bleed", "Swords", AbilityType.PASSIVE, FileManager.Files.SWORDS_CONFIG),
  DAZE("Daze", "Archery", AbilityType.PASSIVE, FileManager.Files.ARCHERY_CONFIG),
  DOUBLE_DROP("Double Drop", "Mining", AbilityType.PASSIVE, FileManager.Files.MINING_CONFIG),
  STICKY_FINGERS("Sticky Fingers", "Unarmed", AbilityType.PASSIVE, FileManager.Files.UNARMED_CONFIG),
  TOO_MANY_PLANTS("Too Many Plants", "Herbalism", AbilityType.PASSIVE, FileManager.Files.HERBALISM_CONFIG),
  EXTRA_LUMBER("Extra Lumber", "Woodcutting", AbilityType.PASSIVE, FileManager.Files.WOODCUTTING_CONFIG),
  SHRED("Shred", "Axes", AbilityType.PASSIVE, FileManager.Files.AXES_CONFIG),
  ROLL("Roll", "Fitness", AbilityType.PASSIVE, FileManager.Files.FITNESS_CONFIG),
  EXTRACTION("Extraction", "Excavation", AbilityType.PASSIVE, FileManager.Files.EXCAVATION_CONFIG),
  GREAT_ROD("Great Rod", "Fishing", AbilityType.PASSIVE, FileManager.Files.FISHING_CONFIG),
  HASTY_BREW("Hasty Brew", "Sorcery", AbilityType.PASSIVE, FileManager.Files.SORCERY_CONFIG);

  @Getter
  private String name;
  //TODO fix this lmao
  @Getter
  private String skill;
  @Getter
  private AbilityType abilityType;
  @Getter
  private FileManager.Files file;

  DefaultAbilities(String name, String skill, AbilityType type, FileManager.Files file) {
    this.name = name;
    this.skill = skill;
    this.abilityType = type;
    this.file = file;
  }

  /**
   * Check if the ability is enabled in the config
   *
   * @return A boolean if the ability is disabled or enabled.
   */
  @Override
  public boolean isEnabled() {
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(Skills.fromString(skill))).getBoolean("EnabledAbilities." + name.replace(" ", "").replace("_", ""));
  }

  /**
   * Get a skills default ability
   *
   * @param skill The skill you want the default ability of
   * @return The enum value of the default ability the skill owns
   */
  public static DefaultAbilities getSkillsDefaultAbility(String skill) {
    return Arrays.stream(DefaultAbilities.values()).filter(n -> n.getSkill().equalsIgnoreCase(skill)).findFirst().orElse(null);
  }

  public Parser getActivationEquation(){
    return new Parser(McRPG.getInstance().getFileManager().getFile(file).getString(name.replaceAll(" ", "") + "Config." + name.replaceAll(" ", "") + "ChanceEquation"));
  }

}
