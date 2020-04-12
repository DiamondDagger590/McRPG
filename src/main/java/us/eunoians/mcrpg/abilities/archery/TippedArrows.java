package us.eunoians.mcrpg.abilities.archery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class TippedArrows extends BaseAbility {

  public TippedArrows(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.TIPPED_ARROWS, isToggled, currentTier);
  }
}
