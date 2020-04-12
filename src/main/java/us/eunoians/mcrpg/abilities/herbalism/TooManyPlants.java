package us.eunoians.mcrpg.abilities.herbalism;

import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class TooManyPlants extends BaseAbility {

  public TooManyPlants(boolean isToggled) {
    super(DefaultAbilities.TOO_MANY_PLANTS, isToggled, 0, true);
  }
}
