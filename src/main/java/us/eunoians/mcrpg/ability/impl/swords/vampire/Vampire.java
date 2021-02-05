package us.eunoians.mcrpg.ability.impl.swords.vampire;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.TierableAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.ability.UnlockableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.event.ability.swords.VampireActivateEvent;

import java.util.Collections;
import java.util.List;

/**
 * This ability restores health each bleed cycle to the {@link AbilityHolder} that caused the {@link us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed}
 *
 * @author DiamondDagger590
 */
@AbilityIdentifier(id = "vampire")
public class Vampire extends BaseAbility implements UnlockableAbility, ToggleableAbility, TierableAbility {

    private int tier = 0;
    private boolean toggled = false;
    private boolean unlocked = false;

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public Vampire(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if(abilityCreationData instanceof VampireCreationData){

            VampireCreationData vampireCreationData = (VampireCreationData) abilityCreationData;

            this.tier = vampireCreationData.getTier();
            this.unlocked = vampireCreationData.isUnlocked();
            this.toggled = vampireCreationData.isToggled();
        }
    }

    /**
     * Gets the {@link NamespacedKey} that this {@link Ability} belongs to
     *
     * @return The {@link NamespacedKey} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull NamespacedKey getSkill() {
        return McRPG.getNamespacedKey("swords");
    }

    /**
     * @param activator    The {@link AbilityHolder} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder activator, Object... optionalData) {

        if(optionalData.length > 0 && optionalData[0] instanceof BleedActivateEvent) {
            BleedActivateEvent bleedActivateEvent = (BleedActivateEvent) optionalData[0];
            ConfigurationSection configurationSection = getTierConfigSection(getTier());

            int amountToHeal = configurationSection.getInt("amount-to-heal", 3);

            VampireActivateEvent vampireActivateEvent = new VampireActivateEvent(bleedActivateEvent.getAbilityHolder(), this, amountToHeal, bleedActivateEvent);
            Bukkit.getPluginManager().callEvent(vampireActivateEvent);

            if(!vampireActivateEvent.isCancelled()){
                bleedActivateEvent.setHealthToRestore(vampireActivateEvent.getAmountToHeal());
            }
        }
    }

    /**
     * Abstract method that can be used to create listeners for this specific ability.
     * Note: This should only return a {@link List} of {@link Listener} objects. These shouldn't be registered yet!
     * This will be done automatically.
     *
     * @return a list of listeners for this {@link Ability}
     */
    @Override
    protected List<Listener> createListeners() {
        return Collections.singletonList(new VampireListener());
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
     * Gets the {@link ConfigurationSection} that belongs to this ability
     *
     * @param tier The tier at which to get the {@link ConfigurationSection} for
     * @return Either the {@link ConfigurationSection} mapped to the provided tier or {@code null} if invalid
     */
    @Override
    public @Nullable ConfigurationSection getTierConfigSection(int tier) {
        return null;
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
