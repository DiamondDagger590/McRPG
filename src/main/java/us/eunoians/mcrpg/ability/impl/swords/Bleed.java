package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.*;
import us.eunoians.mcrpg.api.event.taming.GoreActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.SkillType;
import us.eunoians.mcrpg.util.Parser;

import java.util.Optional;

/**
 * This ability is an {@link DefaultAbility} that activates when a {@link org.bukkit.entity.LivingEntity} attacks
 * another {@link org.bukkit.entity.LivingEntity}. This {@link Ability} will deal damage over time with a few modifiers
 */
public class Bleed extends AbilityConstructor implements DefaultAbility, ToggleableAbility {

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
     * @param mcRPGPlayer The {@link McRPGPlayer} that owns this {@link Ability}
     */
    public Bleed(McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    /**
     * Gets the {@link AbilityType} enum that represents this ability
     *
     * @return The {@link AbilityType} enum that represents this ability
     */
    @Override
    public @NotNull AbilityType getAbilityType() {
        return AbilityType.BLEED;
    }

    /**
     * Gets the {@link SkillType} that this {@link Ability} belongs to
     *
     * @return The {@link SkillType} that this {@link Ability} belongs to
     */
    @Override
    public @NotNull SkillType getSkill() {
        return SkillType.SWORDS;
    }

    /**
     * Attempts to handle the {@link Event} and activate the ability based on the event
     *
     * @param event The {@link Event} to handle
     */
    @Override
    public void handleEvent(Event event) {

        //The ability can activate from Gore so we want to handle that
        if (event instanceof GoreActivateEvent) {

        }
        else {

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event;

            LivingEntity damager = (LivingEntity) entityDamageByEntityEvent.getDamager();
            LivingEntity target = (LivingEntity) entityDamageByEntityEvent.getEntity();

            if(damager instanceof Player){

                Optional<McRPGPlayer> mcRPGPlayerOptional = McRPG.getInstance().getPlayerContainer().getPlayer(damager.getUniqueId());

                if(mcRPGPlayerOptional.isPresent()){

                    McRPGPlayer mcRPGPlayer = mcRPGPlayerOptional.get();

                    //TODO handle checking odds
                    if(true){

                    }

                }
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
        return this.isToggled() && (event instanceof GoreActivateEvent ||
                (event instanceof EntityDamageByEntityEvent &&
                        ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity
                        && ((LivingEntity) ((EntityDamageByEntityEvent) event).getDamager()).getEquipment() != null
                        && ((LivingEntity) ((EntityDamageByEntityEvent) event).getDamager()).getEquipment().getItemInMainHand().getType().toString().contains("_SWORD")
                        && ((EntityDamageByEntityEvent) event).getEntity() instanceof LivingEntity));
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

    /**
     * Get the {@link EventPriority} that this {@link Ability} should be ran on
     *
     * @return The {@link EventPriority} that this {@link Ability} should be ran on
     */
    @Override
    public @NotNull EventPriority getListenPriority() {
        return EventPriority.MONITOR;
    }
}
