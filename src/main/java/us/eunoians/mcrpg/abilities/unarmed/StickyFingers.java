package us.eunoians.mcrpg.abilities.unarmed;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class StickyFingers extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance = 0.0;

  public StickyFingers(){
    super(DefaultAbilities.STICKY_FINGERS, true, true );
  }
}
