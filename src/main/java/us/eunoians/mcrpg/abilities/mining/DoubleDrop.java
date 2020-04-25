package us.eunoians.mcrpg.abilities.mining;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class DoubleDrop extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance = 0.0;

  public DoubleDrop(boolean isToggled) {
    super(DefaultAbilities.DOUBLE_DROP, isToggled, 0, true);
  }
}
