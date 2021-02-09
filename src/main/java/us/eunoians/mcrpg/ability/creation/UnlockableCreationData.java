package us.eunoians.mcrpg.ability.creation;

/**
 * This interface represents something that an implementation of {@link AbilityCreationData} can implement
 * to pass in extra data properly.
 * <p>
 * This should be used for abilities that can be unlocked to have their data passed in for construction
 *
 * @author DiamondDagger590
 */
public interface UnlockableCreationData {

    /**
     * Gets if the {@link us.eunoians.mcrpg.ability.Ability} being created
     * is unlocked.
     *
     * @return {@code true} if the {@link us.eunoians.mcrpg.ability.Ability} being created
     * is unlocked.
     */
    public boolean isUnlocked();
}
