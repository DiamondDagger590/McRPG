package us.eunoians.mcrpg.abilities.axes;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class CripplingBlow extends BaseAbility {

  public CripplingBlow(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.CRIPPLING_BLOW, isToggled, currentTier);
  }
}
