package us.eunoians.mcrpg.skills;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.GenericAbility;
import us.eunoians.mcrpg.types.Skills;

import java.util.Map;

public class Herbalism extends Skill {

  public Herbalism(int currentLevel, int currentExp, Map<GenericAbility, BaseAbility> map, McRPGPlayer player) {
	super(Skills.HERBALISM, map, currentLevel, currentExp, player);
  }
}
