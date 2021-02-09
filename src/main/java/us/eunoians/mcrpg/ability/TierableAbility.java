package us.eunoians.mcrpg.ability;

import us.eunoians.mcrpg.api.error.AbilityConfigurationNotFoundException;

/**
 * This interface represents an {@link Ability} that has a tier associated with it
 *
 * @author DiamondDagger590
 */
public interface TierableAbility extends Ability {

    /**
     * Gets the tier of this {@link Ability}.
     * <p>
     * A tier of 0 represents an ability that is currently not unlocked but this should be checked by
     * {@link UnlockableAbility#isUnlocked()}.
     *
     * @return A positive zero inclusive number representing the current tier of this {@link TierableAbility}.
     */
    public int getTier();

    /**
     * Sets the tier of this {@link TierableAbility}.
     * <p>
     * This should only accept positive zero inclusive numbers and should sanitize for them.
     *
     * @param tier A positive zero inclusive number representing the new tier of this {@link TierableAbility}
     */
    public void setTier(int tier);

    /**
     * Gets the level at which the provided tier is unlocked at
     *
     * @param tier A positive zero exclusive tier at which to get the unlock level for
     * @return The level at which the tier becomes available or {@code -1} if missing from config.
     * @throws AbilityConfigurationNotFoundException if this is an instance of {@link us.eunoians.mcrpg.ability.configurable.ConfigurableTierableAbility}
     *                                               and there is an issue
     */
    public int getTierUnlockLevel(int tier) throws AbilityConfigurationNotFoundException;
}
