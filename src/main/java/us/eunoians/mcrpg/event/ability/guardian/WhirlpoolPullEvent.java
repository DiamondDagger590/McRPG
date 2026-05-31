package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a {@link us.eunoians.mcrpg.ability.impl.guardian.Whirlpool} pulls
 * a target entity toward its center.
 */
public class WhirlpoolPullEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player caster;
    private final LivingEntity target;
    private final Location center;
    private Vector pullVector;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link WhirlpoolPullEvent}.
     *
     * @param caster     The {@link Player} who created the whirlpool
     * @param target     The {@link LivingEntity} being pulled
     * @param center     The {@link Location} center of the whirlpool
     * @param pullVector The {@link Vector} representing the pull direction and magnitude
     */
    public WhirlpoolPullEvent(@NotNull Player caster, @NotNull LivingEntity target, @NotNull Location center, @NotNull Vector pullVector) {
        this.caster = caster;
        this.target = target;
        this.center = center;
        this.pullVector = pullVector;
    }

    /**
     * Gets the {@link Player} who created the whirlpool.
     *
     * @return The {@link Player} who created the whirlpool
     */
    @NotNull
    public Player getCaster() {
        return caster;
    }

    /**
     * Gets the {@link LivingEntity} being pulled by the whirlpool.
     *
     * @return The {@link LivingEntity} being pulled
     */
    @NotNull
    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Gets the center {@link Location} of the whirlpool.
     *
     * @return The center {@link Location} of the whirlpool
     */
    @NotNull
    public Location getCenter() {
        return center;
    }

    /**
     * Gets the {@link Vector} representing the pull direction and magnitude.
     *
     * @return The pull {@link Vector}
     */
    @NotNull
    public Vector getPullVector() {
        return pullVector;
    }

    /**
     * Sets the {@link Vector} representing the pull direction and magnitude.
     *
     * @param pullVector The new pull {@link Vector}
     */
    public void setPullVector(@NotNull Vector pullVector) {
        this.pullVector = pullVector;
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
