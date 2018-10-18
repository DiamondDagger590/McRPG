package us.eunoians.mcmmox.skills;

import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.GenericAbility;
import us.eunoians.mcmmox.types.Skills;

import java.util.HashMap;

public class Mining extends Skill {

  /**
   * @param currentLevel The current level of the players swords skill
   * @param currentExp   The current exp amount of the players swords skill
   */
  public Mining(int currentLevel, int currentExp, HashMap<GenericAbility, BaseAbility> map, McMMOPlayer player) {
	super(Skills.MINING, map, currentLevel, currentExp, player);
  }
}
