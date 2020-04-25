package us.eunoians.mcrpg.abilities.unarmed;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Berserk extends BaseAbility {

  public Berserk(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.BERSERK, isToggled, currentTier);
  }
}
