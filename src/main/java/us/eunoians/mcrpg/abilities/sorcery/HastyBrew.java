package us.eunoians.mcrpg.abilities.sorcery;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class HastyBrew extends BaseAbility {

  public HastyBrew(boolean isToggled) {
    super(DefaultAbilities.HASTY_BREW, isToggled, 0, true);
  }
}
