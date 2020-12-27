package us.eunoians.mcrpg.abilities;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skills.SkillType;

/**
 * This enum holds all existing abilities
 *
 * @author DiamondDagger590
 */
public enum AbilityType {
  
  //Taming Abilities
  GORE(SkillType.TAMING);
  
  private SkillType skillType;
  
  AbilityType(SkillType skillType){
    this.skillType = skillType;
  }
  
  /**
   * Gets the {@link SkillType} that this {@link AbilityType} belongs to.
   *
   * @return The {@link SkillType} that this {@link AbilityType} belongs to
   */
  @NotNull
  public SkillType getSkillType(){
    return this.skillType;
  }
}
