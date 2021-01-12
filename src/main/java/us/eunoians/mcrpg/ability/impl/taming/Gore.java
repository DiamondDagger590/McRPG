package us.eunoians.mcrpg.ability.impl.taming;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.ability.BaseAbility;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.ability.DefaultAbility;
import us.eunoians.mcrpg.ability.ToggleableAbility;
import us.eunoians.mcrpg.ability.PlayerAbility;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.api.event.taming.GoreActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.SkillType;
import us.eunoians.mcrpg.util.parser.Parser;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a Taming ability that activates whenever a {@link org.bukkit.entity.Wolf} attacks a
 * {@link org.bukkit.entity.LivingEntity}. The {@link org.bukkit.entity.LivingEntity} will be afflicted with
 * the {@link Bleed} status
 * using all {@link Bleed} modifiers from the {@link McRPGPlayer}'s ability loadout.
 *
 * @author DiamondDagger590
 */
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
     * @param abilityHolder The {@link AbilityHolder} that owns this {@link Ability}
     */
    public Gore(AbilityHolder abilityHolder) {
        super(abilityHolder);
        this.toggled = true;

        //TODO load activation equation
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
        return null;
    }

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} that owns this {@link Ability}
     */
    public Gore(McRPGPlayer mcRPGPlayer, boolean isToggled) {
        super(mcRPGPlayer);
        this.toggled = isToggled;

        //TODO load activation equation
    }

    /**
     * Gets the {@link AbilityType} enum that represents this ability
     *
     * @return The {@link AbilityType} enum that represents this ability
     */
    @Override
    public @NotNull AbilityType getAbilityType() {
        return AbilityType.GORE;
    }

    /**
     * Gets the {@link SkillType} that this {@link Ability} belongs to
     *
     * @return The {@link SkillType} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull SkillType getSkill() {
        return SkillType.TAMING;
    }

    /**
     * Attempts to handle the {@link Event} and activate the ability based on the event
     *
     * @param event The {@link Event} to handle
     */
    @Override
    public void handleEvent(Event event) {

        EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;

        //Taming taming = (Taming) this.getPlayer().getSkill(this.getSkill());

        //this.getActivationEquation().setVariable("taming_level", taming.getCurrentLevel());

        if (this.getActivationEquation().getValue() * 100000 >= ThreadLocalRandom.current().nextInt(100000)) {

            GoreActivateEvent goreActivateEvent = new GoreActivateEvent(this.getPlayer(), this);
            Bukkit.getPluginManager().callEvent(goreActivateEvent);

            if (!goreActivateEvent.isCancelled()) {
                //TODO activate
            }
        }
    }

    /**
     * Checks to see if the provided {@link Event} is valid for handling
     *
     * @param event The {@link Event} to validate
     * @return True if the event can be passed for testing
     */
    @Override
    public boolean isValidEvent(Event event) {
        return this.isToggled() && event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Wolf
                && ((Wolf) ((EntityDamageByEntityEvent) event).getDamager()).getOwner() != null
                && ((Wolf) ((EntityDamageByEntityEvent) event).getDamager()).getOwner().getUniqueId().equals(this.getPlayer().getUniqueId())
                && ((EntityDamageByEntityEvent) event).getEntity() instanceof LivingEntity;
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
     * Handles activation of the ability outside of the {@link #handleEvent(Event)}
     * as to allow for future proofing additions with a custom mob AI
     *
     * @param activator    The {@link LivingEntity} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     */
    @Override
    public void activate(LivingEntity activator, Object... optionalData) {

    }

    /**
     * Get the {@link EventPriority} that this {@link Ability} should be ran on
     *
     * @return The {@link EventPriority} that this {@link Ability} should be ran on
     */
    @Override
    public @NotNull EventPriority getListenPriority() {
        return EventPriority.MONITOR;
    }

    /**
     * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
     *
     * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull McRPGPlayer getPlayer() {
        return (McRPGPlayer) getAbilityHolder();
    }
}
