package us.eunoians.mcmmox.abilities.mining;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.types.DefaultAbilities;

public class DoubleDrop extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance = 0.0;

  public DoubleDrop(){
    super(DefaultAbilities.DOUBLE_DROP, true, true);
  }
}
