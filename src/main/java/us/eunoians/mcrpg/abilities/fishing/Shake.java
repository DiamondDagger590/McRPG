package us.eunoians.mcrpg.abilities.fishing;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Shake extends BaseAbility {

  public Shake(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.SHAKE, isToggled, currentTier);
  }
}
