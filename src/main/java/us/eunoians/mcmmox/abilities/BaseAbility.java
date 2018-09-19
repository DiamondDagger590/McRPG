package us.eunoians.mcmmox.abilities;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.types.GenericAbility;

public abstract class BaseAbility {

  @Getter
  private GenericAbility genericAbility;
  //Transfer
  @Getter
  @Setter
  private boolean isToggled;

  @Getter @Setter
  private int currentTier;

  /**
   * @param genericAbility Accepts either a DefaultAbilities or UnlockedAbilities enum type
   * @param isToggled      If the ability is toggled for the player.
   */
  public BaseAbility(GenericAbility genericAbility, boolean isToggled) {
    this.genericAbility = genericAbility;
    this.isToggled = isToggled;

  }
}