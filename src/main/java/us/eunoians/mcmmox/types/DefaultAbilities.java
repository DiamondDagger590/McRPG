package us.eunoians.mcmmox.types;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcmmox.Mcmmox;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All abilities that come default with a skill should be stored in this enum
 */
public enum DefaultAbilities implements GenericAbility {
  BLEED("Bleed", "Swords", AbilityType.PASSIVE) {
  };

  @Getter
  private String name;
  @Getter
  private String skill;
  @Getter
  private AbilityType abilityType;

  DefaultAbilities(String name, String skill, AbilityType type) {
    this.name = name;
    this.skill = skill;
    this.abilityType = type;

  }

  /**
   * Check if the ability is enabled in the config
   *
   * @return A boolean if the ability is disabled or enabled.
   */
  @Override
  public boolean isEnabled() {
    return YamlConfiguration.loadConfiguration(new File(Mcmmox.getInstance().getDataFolder(),
            File.separator + "Skills" + File.separator + this.skill)).getBoolean("EnabledAbilities." + name);
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

}
