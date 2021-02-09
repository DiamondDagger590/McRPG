package us.eunoians.mcrpg.api.event.ability.swords.ragespike;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.ragespike.RageSpike;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.CooldownableAbilityActivateEvent;

/**
 * This event is called whenever a {@link us.eunoians.mcrpg.player.McRPGPlayer} launches due to
 * ability {@link RageSpike}
 *
 * @author DiamondDagger590
 */
public class RageSpikeLaunchEvent extends CooldownableAbilityActivateEvent implements Cancellable {

    private boolean cancelled;
    private double vectorMultiplier;
    private double damage;
    private double damageRadius;
    private double targetVectorMultiplier;

    public RageSpikeLaunchEvent(@NotNull AbilityHolder abilityHolder, @NotNull RageSpike rageSpike, double vectorMultiplier, double damage, double damageRadius, double targetVectorMultiplier, int cooldownSeconds) {
        super(abilityHolder, rageSpike, AbilityEventType.COMBAT, cooldownSeconds);
        this.vectorMultiplier = vectorMultiplier;
        this.damage = Math.max(0, damage);
        this.damageRadius = Math.max(0, damageRadius);
        this.targetVectorMultiplier = targetVectorMultiplier;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull RageSpike getAbility() {
        return (RageSpike) super.getAbility();
    }

    /**
     * Gets the multiplier that the player should have their {@link org.bukkit.util.Vector} multiplied by when being launched
     *
     * @return The multiplier that the player should have their {@link org.bukkit.util.Vector} multiplied by when being launched
     */
    public double getVectorMultiplier() {
        return vectorMultiplier;
    }

    /**
     * Sets the multiplier that the player should have their {@link org.bukkit.util.Vector} multiplied by when being launched
     *
     * @param vectorMultiplier The multiplier that the player should have their {@link org.bukkit.util.Vector} multiplied by when being launched
     */
    public void setVectorMultiplier(double vectorMultiplier) {
        this.vectorMultiplier = vectorMultiplier;
    }

    /**
     * Gets the amount of damage that will be dealt to any enemy within the {@link #getDamageRadius()} as the user flies
     *
     * @return A non-negative zero inclusive amount of damage that will be dealt to
     * any enemy within the {@link #getDamageRadius()} as the user flies.
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage that will be dealt to any enemy within the {@link #getDamageRadius()} as the user flies
     *
     * @param damage A non-negative zero inclusive amount of damage that will be dealt
     *               to any enemy within the {@link #getDamageRadius()} as the user flies.
     */
    public void setDamage(double damage) {
        this.damage = Math.max(0, damage);
    }

    /**
     * Gets the radius around the user to check for entities to damage using {@link org.bukkit.entity.LivingEntity#getNearbyEntities(double, double, double)}
     * as the user flies.
     * <p>
     * This radius is checked every tick for 1 second
     *
     * @return A positive zero inclusive radius around the user to check for entities to damage as the user flies.
     */
    public double getDamageRadius() {
        return damageRadius;
    }

    /**
     * Sets the radius around the user to check for entities to damage using {@link org.bukkit.entity.LivingEntity#getNearbyEntities(double, double, double)}
     * as the user flies.
     * <p>
     * This radius is checked every tick for 1 second
     *
     * @param damageRadius A positive zero inclusive radius around the user to check for entities to damage as the user flies.
     */
    public void setDamageRadius(double damageRadius) {
        this.damageRadius = Math.max(0, damageRadius);
    }

    /**
     * Gets the velocity multiplier to apply to any {@link org.bukkit.entity.LivingEntity} that gets damaged by this ability.
     *
     * @return The velocity multiplier to apply to any {@link org.bukkit.entity.LivingEntity} that gets damaged by this ability
     */
    public double getTargetVectorMultiplier() {
        return targetVectorMultiplier;
    }

    /**
     * Sets the velocity multiplier to apply to any {@link org.bukkit.entity.LivingEntity} that gets damaged by this ability.
     *
     * @param targetVectorMultiplier The velocity multiplier to apply to any {@link org.bukkit.entity.LivingEntity} that gets damaged by this ability
     */
    public void setTargetVectorMultiplier(double targetVectorMultiplier) {
        this.targetVectorMultiplier = targetVectorMultiplier;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
