package us.eunoians.mcrpg.abilities.fitness;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class Roll extends BaseAbility {

  public Roll(boolean isToggled) {
    super(DefaultAbilities.ROLL, true, 0, true);
  }
}
