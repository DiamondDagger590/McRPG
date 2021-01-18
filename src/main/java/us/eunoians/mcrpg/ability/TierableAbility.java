package us.eunoians.mcrpg.ability;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

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
     * Gets the {@link ConfigurationSection} that belongs to this ability
     *
     * @param tier The tier at which to get the {@link ConfigurationSection} for
     * @return Either the {@link ConfigurationSection} mapped to the provided tier or {@code null} if invalid
     */
    @Nullable
    public ConfigurationSection getTierConfigSection(int tier);
}
