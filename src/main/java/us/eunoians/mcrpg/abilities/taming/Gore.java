package us.eunoians.mcrpg.abilities.taming;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class Gore extends BaseAbility{
  
  public Gore(boolean isToggled) {
    super(DefaultAbilities.GORE, isToggled, 0, true);
  }
}
