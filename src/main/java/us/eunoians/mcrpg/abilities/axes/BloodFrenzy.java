package us.eunoians.mcrpg.abilities.axes;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class BloodFrenzy extends BaseAbility {

  public BloodFrenzy(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.BLOOD_FRENZY, isToggled, currentTier);
  }
}