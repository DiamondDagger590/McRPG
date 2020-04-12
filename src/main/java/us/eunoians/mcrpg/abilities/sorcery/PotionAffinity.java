package us.eunoians.mcrpg.abilities.sorcery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class PotionAffinity extends BaseAbility {

  public PotionAffinity(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.POTION_AFFINITY, isToggled, currentTier);
  }
}
