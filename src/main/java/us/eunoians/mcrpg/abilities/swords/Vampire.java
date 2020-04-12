package us.eunoians.mcrpg.abilities.swords;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Vampire extends BaseAbility {

  public Vampire(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.VAMPIRE, isToggled, currentTier);
  }
}
