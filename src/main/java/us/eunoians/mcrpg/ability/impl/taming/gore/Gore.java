package us.eunoians.mcrpg.ability.impl.taming.gore;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.DefaultAbility;
import us.eunoians.mcrpg.ability.PlayerAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.util.parser.Parser;

import java.util.Collections;
import java.util.List;

/**
 * This is a Taming ability that activates whenever a {@link org.bukkit.entity.Wolf} attacks a
 * {@link org.bukkit.entity.LivingEntity}. The {@link org.bukkit.entity.LivingEntity} will be afflicted with
 * the {@link Bleed} status
 * using all {@link Bleed} modifiers from the {@link McRPGPlayer}'s ability loadout.
 *
 * @author DiamondDagger590
 */
@AbilityIdentifier(id = "gore", abilityCreationData = GoreCreationData.class)
public class Gore extends BaseAbility implements ToggleableAbility, DefaultAbility, PlayerAbility {

    /**
     * Represents whether the ability is toggled on or off
     */
    private boolean toggled;
    
    /**
     * The equation representing the chance at which this {@link us.eunoians.mcrpg.ability.Ability}
     * can activate
     */
    private Parser activationEquation;

    /**
     * This assumes that you will be passing in {@link GoreCreationData} and will attempt sanitization.
     *
     * @param abilityCreationData The {@link GoreCreationData} is used to create this {@link Ability}
     */
    public Gore(@NotNull AbilityCreationData abilityCreationData) {
        super(abilityCreationData);

        if(abilityCreationData instanceof GoreCreationData) {
            this.toggled = ((GoreCreationData) abilityCreationData).isToggled();
        }

        //TODO load activation equation
    }

    /**
     * Gets the {@link GoreCreationData} that creates this {@link Ability}.
     *
     * @return The {@link GoreCreationData} that creates this {@link Ability}
     */
    @Override
    public @NotNull GoreCreationData getAbilityCreationData() {
        return (GoreCreationData) super.getAbilityCreationData();
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
        return Collections.singletonList(new GoreListener());
    }

    /**
     * Gets the {@link NamespacedKey} that this {@link Ability} belongs to
     *
     * @return The {@link NamespacedKey} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull NamespacedKey getSkill() {
        return McRPG.getNamespacedKey("taming");
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
        return this.activationEquation;
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

    /**
     * @param abilityHolder    The {@link AbilityHolder} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(AbilityHolder abilityHolder, Object... optionalData) {

    }

    /**
     * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
     *
     * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull McRPGPlayer getPlayer() {
        return (McRPGPlayer) getAbilityCreationData().getAbilityHolder();
    }
}
