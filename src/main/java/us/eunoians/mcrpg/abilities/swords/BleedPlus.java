package us.eunoians.mcrpg.abilities.swords;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class BleedPlus extends BaseAbility {

  public BleedPlus(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.BLEED_PLUS, isToggled, currentTier);
  }
}
