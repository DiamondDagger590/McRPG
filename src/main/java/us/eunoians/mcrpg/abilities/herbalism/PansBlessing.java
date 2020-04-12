package us.eunoians.mcrpg.abilities.herbalism;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class PansBlessing extends BaseAbility {

  public PansBlessing(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.PANS_BLESSING, isToggled, currentTier);
  }
}
