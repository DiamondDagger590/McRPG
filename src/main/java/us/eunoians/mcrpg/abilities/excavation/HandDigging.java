package us.eunoians.mcrpg.abilities.excavation;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class HandDigging extends BaseAbility {

  public HandDigging(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.HAND_DIGGING, isToggled, currentTier);
  }
}
