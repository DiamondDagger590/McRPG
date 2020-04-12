package us.eunoians.mcrpg.abilities.excavation;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class BuriedTreasure extends BaseAbility {

  public BuriedTreasure(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.BURIED_TREASURE, isToggled, currentTier);
  }
}
