package us.eunoians.mcmmox.Abilities;

import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.util.Parser;

public class Bleed extends BaseAbility {

  private Parser bleedChanceEquation;

  public Bleed() {
    super(DefaultAbilities.BLEED, true);
  }


}
