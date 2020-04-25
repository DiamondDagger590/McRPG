package us.eunoians.mcrpg.abilities.mining;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class RicherOres extends BaseAbility {

  public RicherOres(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.RICHER_ORES, isToggled, currentTier);
  }
}
