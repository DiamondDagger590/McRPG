package us.eunoians.mcrpg.abilities.swords;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class TaintedBlade extends BaseAbility {

  public TaintedBlade(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.TAINTED_BLADE, isToggled, currentTier);
  }
}
