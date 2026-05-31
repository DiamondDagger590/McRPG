package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a {@link us.eunoians.mcrpg.ability.impl.guardian.TsunamiWall} contacts
 * a target entity, applying knockback and slowness.
 */
public class TsunamiWallContactEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player caster;
    private final LivingEntity target;
    private Vector knockbackVector;
    private int slownessAmplifier;
    private int slownessDurationTicks;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link TsunamiWallContactEvent}.
     *
     * @param caster               The {@link Player} who created the tsunami wall
     * @param target               The {@link LivingEntity} contacted by the wall
     * @param knockbackVector      The {@link Vector} representing the knockback applied
     * @param slownessAmplifier    The amplifier of the slowness effect applied
     * @param slownessDurationTicks The duration of the slowness effect in ticks
     */
    public TsunamiWallContactEvent(@NotNull Player caster, @NotNull LivingEntity target, @NotNull Vector knockbackVector, int slownessAmplifier, int slownessDurationTicks) {
        this.caster = caster;
        this.target = target;
        this.knockbackVector = knockbackVector;
        this.slownessAmplifier = slownessAmplifier;
        this.slownessDurationTicks = slownessDurationTicks;
    }

    /**
     * Gets the {@link Player} who created the tsunami wall.
     *
     * @return The {@link Player} who created the tsunami wall
     */
    @NotNull
    public Player getCaster() {
        return caster;
    }

    /**
     * Gets the {@link LivingEntity} contacted by the tsunami wall.
     *
     * @return The {@link LivingEntity} contacted by the wall
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the {@link Vector} representing the knockback applied to the target.
     *
     * @return The knockback {@link Vector}
     */
    @NotNull
    public Vector getKnockbackVector() {
        return knockbackVector;
    }

    /**
     * Sets the {@link Vector} representing the knockback applied to the target.
     *
     * @param knockbackVector The new knockback {@link Vector}
     */
    public void setKnockbackVector(@NotNull Vector knockbackVector) {
        this.knockbackVector = knockbackVector;
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
