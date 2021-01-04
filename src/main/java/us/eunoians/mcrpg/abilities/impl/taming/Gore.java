package us.eunoians.mcrpg.abilities.impl.taming;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.abilities.Ability;
import us.eunoians.mcrpg.abilities.AbilityConstructor;
import us.eunoians.mcrpg.abilities.AbilityType;
import us.eunoians.mcrpg.abilities.DefaultAbility;
import us.eunoians.mcrpg.abilities.ToggleableAbility;
import us.eunoians.mcrpg.api.events.taming.GoreActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.skills.SkillType;
import us.eunoians.mcrpg.skills.impl.Taming;
import us.eunoians.mcrpg.util.Parser;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This is a Taming ability that activates whenever a {@link org.bukkit.entity.Wolf} attacks a
 * {@link org.bukkit.entity.LivingEntity}. The {@link org.bukkit.entity.LivingEntity} will be afflicted with
 * the Bleed status using all Bleed modifiers from the {@link McRPGPlayer}'s ability loadout.
 *
 * @author DiamondDagger590
 */
public class Gore extends AbilityConstructor implements ToggleableAbility, DefaultAbility {

    /**
     * Represents whether the ability is toggled on or off
     */
    private boolean isToggled;
    
    /**
     * The equation representing the chance at which this {@link us.eunoians.mcrpg.abilities.Ability}
     * can activate
     */
    private Parser activationEquation;

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} that owns this {@link Ability}
     */
    public Gore(McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        this.isToggled = true;

        //TODO load activation equation
    }

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} that owns this {@link Ability}
     */
    public Gore(McRPGPlayer mcRPGPlayer, boolean isToggled) {
        super(mcRPGPlayer);
        this.isToggled = isToggled;

        //TODO load activation equation
    }

    /**
     * If an ability has been modified and needs saving in some sort of manner, this method will return
     * true, indicating that it should be processed and stored to update the database.
     *
     * @return True if the ability has some dirty data in it that needs stored
     */
    @Override
    public boolean isDirty() {
        return isDirty;
    }

    /**
     * Sets if this ability has dirty data that needs stored or not
     *
     * @param dirty True if the ability should be marked as dirty for storage
     */
    @Override
    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
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

        Taming taming = (Taming) this.getPlayer().getSkill(this.getSkill());

        this.getActivationEquation().setVariable("taming_level", taming.getCurrentLevel());

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
        return event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Wolf
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
        return this.isToggled;
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
        this.isToggled = !this.isToggled;
        return isToggled();
    }

    /**
     * This method sets the toggled status of the ability
     *
     * @param toggled True if the ability should be toggled on
     */
    @Override
    public void setToggled(boolean toggled) {
        this.isToggled = toggled;
    }
}
