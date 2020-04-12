package us.eunoians.mcrpg.types;

import us.eunoians.mcrpg.abilities.BaseAbility;

public interface GenericAbility {
  //Blank class only used for hierarchy
  String getName();

  Class<? extends BaseAbility> getClazz();

  Skills getSkill();

  AbilityType getAbilityType();

  boolean isEnabled();

  boolean isCooldown();

  /**
   * enum name
   */
  String name();
}