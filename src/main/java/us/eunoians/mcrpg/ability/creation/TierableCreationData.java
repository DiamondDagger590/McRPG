package us.eunoians.mcrpg.ability.creation;

/**
 * This interface represents something that an implementation of {@link AbilityCreationData} can implement
 * to pass in extra data properly.
 * <p>
 * This should be used for abilities that have tiers to have their data passed in for construction
 *
 * @author DiamondDagger590
 */
public interface TierableCreationData {

    /**
     * Gets the tier of the {@link us.eunoians.mcrpg.ability.Ability} being created
     *
     * @return The tier of the {@link us.eunoians.mcrpg.ability.Ability} being created
     */
    public int getTier();
}
