package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a {@link us.eunoians.mcrpg.ability.impl.guardian.WaterloggedStrike} impacts
 * a target entity, applying damage and slowness.
 */
public class WaterloggedStrikeImpactEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final LivingEntity caster;
    private final LivingEntity target;
    private double damage;
    private int slownessAmplifier;
    private int slownessDurationTicks;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link WaterloggedStrikeImpactEvent}.
     *
     * @param caster               The {@link LivingEntity} who cast the waterlogged strike
     * @param target               The {@link LivingEntity} being impacted
     * @param damage               The damage dealt to the target
     * @param slownessAmplifier    The amplifier of the slowness effect applied
     * @param slownessDurationTicks The duration of the slowness effect in ticks
     */
    public WaterloggedStrikeImpactEvent(@NotNull LivingEntity caster, @NotNull LivingEntity target, double damage, int slownessAmplifier, int slownessDurationTicks) {
        this.caster = caster;
        this.target = target;
        this.damage = damage;
        this.slownessAmplifier = slownessAmplifier;
        this.slownessDurationTicks = slownessDurationTicks;
    }

    /**
     * Gets the {@link LivingEntity} who cast the waterlogged strike.
     *
     * @return The {@link LivingEntity} who cast the waterlogged strike
     */
    @NotNull
    public LivingEntity getCaster() {
        return caster;
    }

    /**
     * Gets the {@link LivingEntity} being impacted.
     *
     * @return The {@link LivingEntity} being impacted
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the damage dealt to the target.
     *
     * @return The damage dealt to the target
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the damage dealt to the target.
     *
     * @param damage The new damage value
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    /**
     * Gets the amplifier of the slowness effect applied to the target.
     *
     * @return The slowness amplifier
     */
    public int getSlownessAmplifier() {
        return slownessAmplifier;
    }

    /**
     * Sets the amplifier of the slowness effect applied to the target.
     *
     * @param slownessAmplifier The new slowness amplifier
     */
    public void setSlownessAmplifier(int slownessAmplifier) {
        this.slownessAmplifier = slownessAmplifier;
    }

    /**
     * Gets the duration of the slowness effect in ticks.
     *
     * @return The slowness duration in ticks
     */
    public int getSlownessDurationTicks() {
        return slownessDurationTicks;
    }

    /**
     * Sets the duration of the slowness effect in ticks.
     *
     * @param slownessDurationTicks The new slowness duration in ticks
     */
    public void setSlownessDurationTicks(int slownessDurationTicks) {
        this.slownessDurationTicks = slownessDurationTicks;
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
