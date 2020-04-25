package us.eunoians.mcrpg.abilities.axes;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class Shred extends BaseAbility {

  public Shred(boolean isToggled) {
    super(DefaultAbilities.SHRED, isToggled, 0, true);
  }
}
