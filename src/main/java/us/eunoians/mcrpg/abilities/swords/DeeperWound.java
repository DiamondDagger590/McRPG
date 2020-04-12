package us.eunoians.mcrpg.abilities.swords;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class DeeperWound extends BaseAbility {

  public DeeperWound(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.DEEPER_WOUND, isToggled, currentTier);
  }
}
