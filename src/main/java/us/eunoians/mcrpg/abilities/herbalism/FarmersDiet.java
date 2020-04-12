package us.eunoians.mcrpg.abilities.herbalism;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class FarmersDiet extends BaseAbility {

  public FarmersDiet(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.FARMERS_DIET, isToggled, currentTier);
  }
}
