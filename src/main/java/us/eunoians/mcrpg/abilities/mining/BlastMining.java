package us.eunoians.mcrpg.abilities.mining;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class BlastMining extends BaseAbility {

  public BlastMining(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.BLAST_MINING, isToggled, currentTier);
  }
}
