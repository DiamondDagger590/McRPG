package us.eunoians.mcmmox.abilities.unarmed;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.types.DefaultAbilities;

public class StickyFingers extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance = 0.0;

  public StickyFingers(){
    super(DefaultAbilities.STICKY_FINGERS, true, true );
  }
}
