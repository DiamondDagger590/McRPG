package us.eunoians.mcrpg.abilities.herbalism;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class MassHarvest extends BaseAbility {

  public MassHarvest(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.MASS_HARVEST, isToggled, currentTier);
  }
}
