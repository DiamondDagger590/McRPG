package us.eunoians.mcrpg.abilities.excavation;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class ManaDeposit extends BaseAbility {

  public ManaDeposit(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.MANA_DEPOSIT, isToggled, currentTier);
  }
}
