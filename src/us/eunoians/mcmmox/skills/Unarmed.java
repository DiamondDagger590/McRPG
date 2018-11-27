package us.eunoians.mcmmox.skills;

import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.GenericAbility;
import us.eunoians.mcmmox.types.Skills;

import java.util.HashMap;

public class Unarmed extends Skill {

  public Unarmed(int currentLevel, int currentExp, HashMap<GenericAbility, BaseAbility> map, McMMOPlayer player) {
	super(Skills.UNARMED, map, currentLevel, currentExp, player);
  }
}
