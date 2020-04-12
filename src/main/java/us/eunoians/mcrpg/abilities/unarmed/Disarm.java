package us.eunoians.mcrpg.abilities.unarmed;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class Disarm extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance;

  public Disarm(boolean isToggled, int currentTier) {
    super(UnlockedAbilities.DISARM, isToggled, currentTier);
  }
}
