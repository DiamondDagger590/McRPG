package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a {@link us.eunoians.mcrpg.ability.impl.guardian.PhaseShift} critical damage
 * strike lands on a target.
 */
public class PhaseShiftCritDamageEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player caster;
    private final LivingEntity target;
    private final double originalDamage;
    private double critDamage;
    private double multiplier;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link PhaseShiftCritDamageEvent}.
     *
     * @param caster         The {@link Player} who cast the phase shift
     * @param target         The {@link LivingEntity} receiving the critical damage
     * @param originalDamage The original damage before the crit multiplier
     * @param critDamage     The damage after applying the crit multiplier
     * @param multiplier     The crit damage multiplier applied
     */
    public PhaseShiftCritDamageEvent(@NotNull Player caster, @NotNull LivingEntity target, double originalDamage, double critDamage, double multiplier) {
        this.caster = caster;
        this.target = target;
        this.originalDamage = originalDamage;
        this.critDamage = critDamage;
        this.multiplier = multiplier;
    }

    /**
     * Gets the {@link Player} who cast the phase shift.
     *
     * @return The {@link Player} who cast the phase shift
     */
    @NotNull
    public Player getCaster() {
        return caster;
    }

    /**
     * Gets the {@link LivingEntity} receiving the critical damage.
     *
     * @return The {@link LivingEntity} receiving the critical damage
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the original damage before the crit multiplier was applied.
     *
     * @return The original damage before the crit multiplier
     */
    public double getOriginalDamage() {
        return originalDamage;
    }

    /**
     * Gets the damage after applying the crit multiplier.
     *
     * @return The damage after the crit multiplier
     */
    public double getCritDamage() {
        return critDamage;
    }

    /**
     * Sets the damage after applying the crit multiplier.
     *
     * @param critDamage The new crit damage value
     */
    public void setCritDamage(double critDamage) {
        this.critDamage = critDamage;
    }

    /**
     * Gets the crit damage multiplier applied.
     *
     * @return The crit damage multiplier
     */
    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Sets the crit damage multiplier.
     *
     * @param multiplier The new crit damage multiplier
     */
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit to locate the handler list for this event class.
     *
     * @return The {@link HandlerList} for this event
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
