package us.eunoians.mcrpg.abilities.archery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Combo extends BaseAbility {

  public Combo(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.COMBO, isToggled, currentTier);
  }
}
