package us.eunoians.mcrpg.abilities.sorcery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class CircesProtection extends BaseAbility {

  public CircesProtection(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.CIRCES_PROTECTION, isToggled, currentTier);
  }
}
