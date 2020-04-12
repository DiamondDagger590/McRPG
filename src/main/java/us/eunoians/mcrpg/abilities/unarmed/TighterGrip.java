package us.eunoians.mcrpg.abilities.unarmed;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class TighterGrip extends BaseAbility {

  public TighterGrip(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.TIGHTER_GRIP, isToggled, currentTier);
  }
}
