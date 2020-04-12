package us.eunoians.mcrpg.abilities.unarmed;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class SmitingFist extends BaseAbility {

  public SmitingFist(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.SMITING_FIST, isToggled, currentTier);
  }
}
