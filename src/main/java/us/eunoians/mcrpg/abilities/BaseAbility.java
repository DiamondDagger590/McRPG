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
   * Defines an ability that unlocks when currentTier > 0
   * If the ability needs to be unlocked with currentTier <= 0, see {@link #BaseAbility(GenericAbility, boolean, int, boolean)} instead
   *
   * @param genericAbility The enum value of what ability this instance represents
   * @param isToggled If the player has this ability toggled
   * @param currentTier The current tier of this ability for the player
   */
  public BaseAbility(GenericAbility genericAbility, boolean isToggled, int currentTier) {
    this(genericAbility, isToggled, currentTier, currentTier > 0);
  }

  /**
   * Defines an ability
   *
   * @param genericAbility The enum value of what ability this instance represents
   * @param isToggled If the player has this ability toggled
   * @param currentTier The current tier of this ability for the player
   * @param isUnlocked If the player has this ability unlocked
   */
  public BaseAbility(GenericAbility genericAbility, boolean isToggled, int currentTier, boolean isUnlocked) {
    this.genericAbility = genericAbility;
    this.isToggled = isToggled;
    this.currentTier = currentTier;
    this.isUnlocked = isUnlocked;
  }

}