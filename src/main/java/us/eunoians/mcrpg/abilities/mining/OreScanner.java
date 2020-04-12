package us.eunoians.mcrpg.abilities.mining;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class OreScanner extends BaseAbility {

  public OreScanner(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.ORE_SCANNER, isToggled, currentTier);
  }
}
