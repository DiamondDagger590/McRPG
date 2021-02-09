package us.eunoians.mcrpg.ability.configurable;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.*;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;

/**
 * This class implements a lot of the generic {@link us.eunoians.mcrpg.ability.ConfigurableAbility} interfaces that most of
 * McRPG's abilities use. All non-default abilities utilize all the interfaces present in this class (at the time of writing),
 * so this serves as a way for these to all be consolidated into one place and better create an abstraction pyramid.
 * <p>
 * This class offers no additional functionality beyond just combining these interfaces.
 *
 * @author DiamondDagger590
 */
public abstract class ConfigurableBaseAbility extends BaseAbility implements ConfigurableTierableAbility, ConfigurableEnableableAbility,
        ConfigurableAbilityDisplayItem, ConfigurableUnlockableAbility, ToggleableAbility {

    protected int tier;
    protected boolean unlocked;
    protected boolean toggled;

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public ConfigurableBaseAbility(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);
    }

    /**
     * Gets the tier of this {@link Ability}.
     * <p>
     * A tier of 0 represents an ability that is currently not unlocked but this should be checked by
     * {@link UnlockableAbility#isUnlocked()}.
     *
     * @return A positive zero inclusive number representing the current tier of this {@link TierableAbility}.
     */
    @Override
    public int getTier() {
        return this.tier;
    }

    /**
     * Sets the tier of this {@link TierableAbility}.
     * <p>
     * This should only accept positive zero inclusive numbers and should sanitize for them.
     *
     * @param tier A positive zero inclusive number representing the new tier of this {@link TierableAbility}
     */
    @Override
    public void setTier(int tier) {
        this.tier = Math.max(0, tier);
    }

    /**
     * This method checks to see if the {@link ToggleableAbility} is currently toggled on
     *
     * @return True if the {@link ToggleableAbility} is currently toggled on
     */
    @Override
    public boolean isToggled() {
        return this.toggled;
    }

    /**
     * This method inverts the current toggled state of the ability and returns the result.
     * <p>
     * This is more of a lazy way of calling {@link #setToggled(boolean)} without also needing to call
     * {@link #isToggled()} to invert
     *
     * @return The stored result of the inverted version of {@link #isToggled()}
     */
    @Override
    public boolean toggle() {
        this.toggled = !this.toggled;
        return this.toggled;
    }

    /**
     * This method sets the toggled status of the ability
     *
     * @param toggled True if the ability should be toggled on
     */
    @Override
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    /**
     * Checks to see if the {@link UnlockableAbility} is currently unlocked or not.
     *
     * @return {@code true} if this {@link UnlockableAbility} is currently unlocked.
     */
    @Override
    public boolean isUnlocked() {
        return this.unlocked;
    }

    /**
     * Sets if this {@link UnlockableAbility} is currently unlocked or not.
     *
     * @param unlocked If this {@link UnlockableAbility} is currently unlocked or not.
     */
    @Override
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    /**
     * Gets the level at which this {@link UnlockableAbility} is automatically unlocked
     *
     * @return A positive zero-exclusive that is the level at which this {@link UnlockableAbility} is unlocked
     */
    @Override
    public int getUnlockLevel() {
        return 0;
    }
}
