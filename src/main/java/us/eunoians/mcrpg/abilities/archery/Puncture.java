package us.eunoians.mcrpg.abilities.archery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Puncture extends BaseAbility {

  public Puncture(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.PUNCTURE, isToggled, currentTier);
  }
}
