package us.eunoians.mcrpg.abilities.taming;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Comradery extends BaseAbility{
  
  public Comradery(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.COMRADERY, isToggled, currentTier);
  }
}
