package us.eunoians.mcrpg.types;

import us.eunoians.mcrpg.abilities.BaseAbility;

import java.util.Locale;

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

  default String getDatabaseName() {

    char[] chars = getName().toCharArray();
    StringBuilder string = new StringBuilder();
    boolean first = true;

    for (char letter : chars) {

      //On the first letter, we don't need to add a `_`, so we skip
      if (!first) {

        //Check here for uppercase except on the first letter
        if (Character.isUpperCase(letter)) {
          string.append("_");
        }
      }
      else {
        first = false;
      }

      string.append(letter == '+' ? "plus" : letter);//Hardcode to handle Bleed+
    }
    return string.toString().toLowerCase(Locale.ROOT); //Lowercase it all
  }
}