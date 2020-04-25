package us.eunoians.mcrpg.abilities.fitness;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Dodge extends BaseAbility {

  public Dodge(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.DODGE, isToggled, currentTier);
  }
}
