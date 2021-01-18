package us.eunoians.mcrpg.ability;

/**
 * This interface represents an {@link Ability} that can be unlocked.
 *
 * @author DiamondDagger590
 */
public interface UnlockableAbility extends Ability {

    /**
     * Checks to see if the {@link UnlockableAbility} is currently unlocked or not.
     *
     * @return {@code true} if this {@link UnlockableAbility} is currently unlocked.
     */
    public boolean isUnlocked();

    /**
     * Sets if this {@link UnlockableAbility} is currently unlocked or not.
     *
     * @param unlocked If this {@link UnlockableAbility} is currently unlocked or not.
     */
    public void setUnlocked(boolean unlocked);
}
