package us.eunoians.mcrpg.ability;

/**
 * This interface represents an ability that can be toggled on and off
 *
 * @author DiamondDagger590
 */
public interface ToggleableAbility extends Ability {

    /**
     * This method checks to see if the {@link ToggleableAbility} is currently toggled on
     *
     * @return {@code true} if the {@link ToggleableAbility} is currently toggled on
     */
    public boolean isToggled();

    /**
     * This method inverts the current toggled state of the ability and returns the result.
     * <p>
     * This is more of a lazy way of calling {@link #setToggled(boolean)} without also needing to call
     * {@link #isToggled()} to invert
     *
     * @return The stored result of the inverted version of {@link #isToggled()}
     */
    public boolean toggle();

    /**
     * This method sets the toggled status of the ability
     *
     * @param toggled {@code true} if the ability should be toggled on
     */
    public void setToggled(boolean toggled);
}
