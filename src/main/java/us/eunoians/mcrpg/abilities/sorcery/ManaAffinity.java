package us.eunoians.mcrpg.abilities.sorcery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class ManaAffinity extends BaseAbility {

  public ManaAffinity(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.MANA_AFFINITY, isToggled, currentTier);
  }
}
