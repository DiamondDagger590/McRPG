package us.eunoians.mcrpg.types;

import lombok.Getter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.abilities.archery.Daze;
import us.eunoians.mcrpg.abilities.axes.Shred;
import us.eunoians.mcrpg.abilities.excavation.Extraction;
import us.eunoians.mcrpg.abilities.fishing.GreatRod;
import us.eunoians.mcrpg.abilities.fitness.Roll;
import us.eunoians.mcrpg.abilities.herbalism.TooManyPlants;
import us.eunoians.mcrpg.abilities.mining.DoubleDrop;
import us.eunoians.mcrpg.abilities.sorcery.HastyBrew;
import us.eunoians.mcrpg.abilities.swords.Bleed;
import us.eunoians.mcrpg.abilities.taming.Gore;
import us.eunoians.mcrpg.abilities.unarmed.StickyFingers;
import us.eunoians.mcrpg.abilities.woodcutting.ExtraLumber;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.util.Parser;

import java.util.Arrays;

/**
 * All abilities that come default with a skill should be stored in this enum
 */
public enum DefaultAbilities implements GenericAbility{
  BLEED("Bleed", Bleed.class, AbilityType.PASSIVE, FileManager.Files.SWORDS_CONFIG),
  DAZE("Daze", Daze.class, AbilityType.PASSIVE, FileManager.Files.ARCHERY_CONFIG),
  DOUBLE_DROP("Double Drop", DoubleDrop.class, AbilityType.PASSIVE, FileManager.Files.MINING_CONFIG),
  EXTRACTION("Extraction", Extraction.class, AbilityType.PASSIVE, FileManager.Files.EXCAVATION_CONFIG),
  EXTRA_LUMBER("Extra Lumber", ExtraLumber.class, AbilityType.PASSIVE, FileManager.Files.WOODCUTTING_CONFIG),
  GREAT_ROD("Great Rod", GreatRod.class, AbilityType.PASSIVE, FileManager.Files.FISHING_CONFIG),
  GORE("Gore", Gore.class, AbilityType.PASSIVE, FileManager.Files.TAMING_CONFIG),
  HASTY_BREW("Hasty Brew", HastyBrew.class, AbilityType.PASSIVE, FileManager.Files.SORCERY_CONFIG),
  ROLL("Roll", Roll.class, AbilityType.PASSIVE, FileManager.Files.FITNESS_CONFIG),
  SHRED("Shred", Shred.class, AbilityType.PASSIVE, FileManager.Files.AXES_CONFIG),
  STICKY_FINGERS("Sticky Fingers", StickyFingers.class, AbilityType.PASSIVE, FileManager.Files.UNARMED_CONFIG),
  TOO_MANY_PLANTS("Too Many Plants", TooManyPlants.class, AbilityType.PASSIVE, FileManager.Files.HERBALISM_CONFIG);
  
  @Getter
  private String name;
  @Getter
  private Class<? extends BaseAbility> clazz;
  @Getter
  private AbilityType abilityType;
  @Getter
  private FileManager.Files file;
  @Getter
  private boolean cooldown = false; // no default abilities have cooldowns
  
  DefaultAbilities(String name, Class<? extends BaseAbility> clazz, AbilityType type, FileManager.Files file){
    this.name = name;
    this.clazz = clazz;
    this.abilityType = type;
    this.file = file;
  }
  
  public Skills getSkill(){
    return Arrays.stream(Skills.values()).filter(skill -> skill.getDefaultAbility().equals(this)).findFirst().orElse(null);
  }
  
  /**
   * Check if the ability is enabled in the config
   *
   * @return A boolean if the ability is disabled or enabled.
   */
  @Override
  public boolean isEnabled(){
    return McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(getSkill())).getBoolean("EnabledAbilities." + name.replace(" ", "").replace("_", ""));
  }
  
  /**
   * Get a skills default ability
   *
   * @param skill The skill you want the default ability of
   * @return The enum value of the default ability the skill owns
   */
  public static DefaultAbilities getSkillsDefaultAbility(Skills skill){
    return Arrays.stream(DefaultAbilities.values()).filter(n -> n.getSkill().equals(skill)).findFirst().orElse(null);
  }
  
  /**
   * Gets the default ability from a string id
   * @param id The string id of the default ability
   * @return The enum representation of the string id or null if it doesnt exist
   */
  public static DefaultAbilities getFromID(String id){
    return Arrays.stream(values()).filter(defaultAbilities -> defaultAbilities.getName().equalsIgnoreCase(id) ||
                                                                defaultAbilities.getName().replace(" ","").equalsIgnoreCase(id))
             .findFirst().orElse(null);
  }

  public Parser getActivationEquation(){
    return new Parser(McRPG.getInstance().getFileManager().getFile(file).getString(name.replaceAll(" ", "") + "Config." + name.replaceAll(" ", "") + "ChanceEquation"));
  }
}
