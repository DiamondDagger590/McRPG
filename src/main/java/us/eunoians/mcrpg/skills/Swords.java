package us.eunoians.mcrpg.skills;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;

import java.util.HashMap;

/*
A class representation of the swords skill
 */
public class Swords extends Skill {

  /**
   * @param currentLevel The current level of the players swords skill
   * @param currentExp   The current exp amount of the players swords skill
   */
  public Swords(int currentLevel, int currentExp, HashMap<GenericAbility, BaseAbility> map, McRPGPlayer player) {
    super(Skills.SWORDS, map, currentLevel, currentExp, player);
  }
}