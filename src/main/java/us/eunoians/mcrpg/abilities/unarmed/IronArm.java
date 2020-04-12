package us.eunoians.mcrpg.abilities.unarmed;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class IronArm extends BaseAbility {

  public IronArm(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.IRON_ARM, isToggled, currentTier);
  }
}
