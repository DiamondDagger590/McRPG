package us.eunoians.mcrpg.abilities.woodcutting;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class HeavySwing extends BaseAbility {

  public HeavySwing(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.HEAVY_SWING, isToggled, currentTier);
  }
}
