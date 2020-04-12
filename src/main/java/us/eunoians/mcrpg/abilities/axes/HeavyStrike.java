package us.eunoians.mcrpg.abilities.axes;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class HeavyStrike extends BaseAbility {

  public HeavyStrike(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.HEAVY_STRIKE, isToggled, currentTier);
  }
}