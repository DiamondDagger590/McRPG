package us.eunoians.mcrpg.abilities.herbalism;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Replanting extends BaseAbility {

  public Replanting(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.REPLANTING, isToggled, currentTier);
  }
}
