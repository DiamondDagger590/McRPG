package us.eunoians.mcrpg.abilities.woodcutting;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class TemporalHarvest extends BaseAbility {

  public TemporalHarvest(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.TEMPORAL_HARVEST, isToggled, currentTier);
  }
}
