package us.eunoians.mcrpg.types;

public interface GenericAbility {
  //Blank class only used for hierarchy
  String getName();

  String getSkill();

  AbilityType getAbilityType();

  boolean isEnabled();
}