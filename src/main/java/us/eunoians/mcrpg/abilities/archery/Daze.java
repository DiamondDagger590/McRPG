package us.eunoians.mcrpg.abilities.archery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class Daze extends BaseAbility {

  public Daze(boolean isToggled) {
    super(DefaultAbilities.DAZE, isToggled, 0, true);
  }
}
