package us.eunoians.mcrpg.abilities;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.types.GenericAbility;

public abstract class BaseAbility {

  @Getter
  private GenericAbility genericAbility;
  @Getter
  @Setter
  private boolean isToggled;

  @Getter @Setter
  private int currentTier;

  @Getter @Setter
  private boolean isUnlocked;

  /**
   *
   * @param genericAbility The enum value of what ability this instance represents
   * @param isToggled If the player has this ability toggled
   * @param isUnlocked If the player has this ability unlocked
   */
  public BaseAbility(GenericAbility genericAbility, boolean isToggled, boolean isUnlocked) {
    this.genericAbility = genericAbility;
    this.isToggled = isToggled;
    this.isUnlocked = isUnlocked;
  }
}