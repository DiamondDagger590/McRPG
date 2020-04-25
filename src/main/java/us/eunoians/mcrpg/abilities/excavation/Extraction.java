package us.eunoians.mcrpg.abilities.excavation;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.types.DefaultAbilities;

public class Extraction extends BaseAbility {

  @Getter
  @Setter
  private double bonusChance = 0;

  public Extraction(boolean isToggled) {
    super(DefaultAbilities.EXTRACTION, isToggled, 0, true);
  }
}
