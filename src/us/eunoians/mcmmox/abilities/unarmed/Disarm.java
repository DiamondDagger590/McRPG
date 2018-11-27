package us.eunoians.mcmmox.abilities.unarmed;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class Disarm extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance;

  public Disarm(){
    super(UnlockedAbilities.DISARM, true, false);
  }
}
