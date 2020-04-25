package us.eunoians.mcrpg.abilities.woodcutting;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class ExtraLumber extends BaseAbility {

  public ExtraLumber(boolean isToggled) {
    super(DefaultAbilities.EXTRA_LUMBER, isToggled, 0, true);
  }
}
