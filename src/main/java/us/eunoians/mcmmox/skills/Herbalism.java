package us.eunoians.mcmmox.skills;

import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.GenericAbility;
import us.eunoians.mcmmox.types.Skills;

import java.util.HashMap;

public class Herbalism extends Skill {

  public Herbalism(int currentLevel, int currentExp, HashMap<GenericAbility, BaseAbility> map, McMMOPlayer player) {
	super(Skills.HERBALISM, map, currentLevel, currentExp, player);
  }
}
