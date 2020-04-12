package us.eunoians.mcrpg.abilities.fishing;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class MagicTouch extends BaseAbility {

  public MagicTouch(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.MAGIC_TOUCH, isToggled, currentTier);
  }
}
