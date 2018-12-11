package us.eunoians.mcrpg.skills;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;

import java.util.HashMap;

public class Mining extends Skill {

  /**
   * @param currentLevel The current level of the players swords skill
   * @param currentExp   The current exp amount of the players swords skill
   */
  public Mining(int currentLevel, int currentExp, HashMap<GenericAbility, BaseAbility> map, McRPGPlayer player) {
	super(Skills.MINING, map, currentLevel, currentExp, player);
  }
}
