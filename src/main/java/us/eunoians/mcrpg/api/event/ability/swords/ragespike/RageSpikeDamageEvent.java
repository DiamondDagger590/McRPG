package us.eunoians.mcrpg.api.event.ability.swords.ragespike;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.swords.ragespike.RageSpike;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This event is called when an {@link us.eunoians.mcrpg.player.McRPGPlayer} activates {@link RageSpike}
 * and causes damage to a {@link LivingEntity}.
 * <p>
 * If this event is uncancelled, then the {@link LivingEntity} will be exempt from future checks for the current activation
 * of {@link RageSpike}.
 * <p>
 * If cancelled, then the {@link LivingEntity} will mostly likely have this event called again multiple times which any developer
 * modifying this event should be aware of.
 *
 * @author DiamondDagger590
 */
public class RageSpikeDamageEvent extends RageSpikeNonActivationEvent implements Cancellable {

    private final @NotNull LivingEntity target;
    private boolean cancelled;
    private double damage;
    private double targetVectorMultiplier;

    public RageSpikeDamageEvent(@NotNull AbilityHolder abilityHolder, @NotNull RageSpike rageSpike, @NotNull LivingEntity target, double damage, double targetVectorMultiplier) {
        super(abilityHolder, rageSpike);
        this.target = target;
        this.damage = Math.max(0, damage);
        this.targetVectorMultiplier = targetVectorMultiplier;
    }

    /**
     * Gets the {@link LivingEntity} being damaged by this event
     *
     * @return The {@link LivingEntity} being damaged by this event
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the amount of damage being dealt to the {@link #getTarget()} by this event
     *
     * @return A positive zero inclusive amount of damage to be dealt to the {@link #getTarget()} by this event
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the amount of damage being dealt to the {@link #getTarget()} by this event
     *
     * @param damage A positive zero inclusive amount of damage to be dealt to the {@link #getTarget()} by this event
     */
    public void setDamage(double damage) {
        this.damage = Math.max(0, damage);
    }

    /**
     * Gets the amount that the {@link #getTarget()} will have their vector multiplied by in order to launch them
     *
     * @return The amount that the {@link #getTarget()} will have their vector multiplied by in order to launch them
     */
    public double getTargetVectorMultiplier() {
        return targetVectorMultiplier;
    }

    /**
     * Sets the amount that the {@link #getTarget()} will have their vector multiplied by in order to launch them
     *
     * @param targetVectorMultiplier The new amount that the {@link #getTarget()} will have their vector multiplied by
     *                               in order to launch them
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
