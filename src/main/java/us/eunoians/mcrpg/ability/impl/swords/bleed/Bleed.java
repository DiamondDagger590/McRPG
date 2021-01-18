package us.eunoians.mcrpg.ability.impl.swords.bleed;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.DefaultAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.swords.BleedActivateEvent;
import us.eunoians.mcrpg.api.manager.BleedManager;
import us.eunoians.mcrpg.util.parser.Parser;

import java.util.Collections;
import java.util.List;

/**
 * This ability is an {@link DefaultAbility} that activates when a {@link org.bukkit.entity.LivingEntity} attacks
 * another {@link org.bukkit.entity.LivingEntity}. This {@link Ability} will deal damage over time with a few modifiers
 */
@AbilityIdentifier(id = "bleed")
public class Bleed extends BaseAbility implements DefaultAbility, ToggleableAbility {

    private static final NamespacedKey NAMESPACED_KEY = McRPG.getNamespacedKey("bleed");

    /**
     * Represents whether the ability is toggled on or off
     */
    private boolean toggled = false;

    /**
     * The equation representing the chance at which this {@link us.eunoians.mcrpg.ability.Ability}
     * can activate
     */
    private Parser activationEquation;

    /**
     * This assumes that you will be passing in {@link BleedCreationData} and will attempt sanitization.
     *
     * @param abilityCreationData The {@link BleedCreationData} is used to create this {@link Ability}
     */
    public Bleed(AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if(abilityCreationData instanceof BleedCreationData) {
            this.toggled = ((BleedCreationData) abilityCreationData).isToggled();
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
    public List<Listener> createListeners() {
        return Collections.singletonList(new BleedListener());
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
     * Gets the {@link BleedCreationData} that creates this {@link Ability}.
     *
     * @return The {@link BleedCreationData} that creates this {@link Ability}
     */
    @Override
    public @NotNull BleedCreationData getAbilityCreationData() {
        return (BleedCreationData) super.getAbilityCreationData();
    }

    /**
     * @param activator    The {@link LivingEntity} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder activator, Object... optionalData) {

        if(optionalData.length > 0 && optionalData[0] instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) optionalData[0];
            //TODO load from config
            int frequencyInTicks = 20;
            int cycles = 3;
            int damagePerCycle = 2;

            BleedManager bleedManager = McRPG.getInstance().getBleedManager();

            BleedActivateEvent bleedActivateEvent = new BleedActivateEvent(activator, this, target,
                    frequencyInTicks, damagePerCycle, cycles, false, 0);

            Bukkit.getPluginManager().callEvent(bleedActivateEvent);
            if (!bleedActivateEvent.isCancelled()) {
                bleedManager.startBleed(activator, target, bleedActivateEvent.getCycleFrequencyInTicks(), bleedActivateEvent.getDamagePerCycle(),
                        bleedActivateEvent.getAmountOfCycles(), bleedActivateEvent.isRestoreHealth(), bleedActivateEvent.getHealthToRestore());
            }
        }
    }

    /**
     * Gets the {@link Parser} that represents the equation needed to activate this ability.
     * <p>
     * Provided that there is an invalid equation offered in the configuration file, the equation will
     * always result in 0.
     *
     * @return The {@link Parser} that represents the equation needed to activate this ability
     */
    @Override
    public @NotNull Parser getActivationEquation() {
        return activationEquation;
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
        return isToggled();
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
}
