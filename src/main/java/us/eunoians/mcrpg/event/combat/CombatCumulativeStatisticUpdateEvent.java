package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;

import java.util.UUID;

/**
 * Fired before per-session statistics are applied to cumulative McCore statistics at session end.
 * Cancellable — cancelling prevents the entire update. This gives third-party plugins the
 * cancellation surface the HLD requires ("observable and cancellable by third parties") that
 * {@link CombatSessionEndEvent} (not cancellable) cannot provide.
 */
public class CombatCumulativeStatisticUpdateEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID entityUUID;
    private final CombatSessionStatisticsSnapshot statistics;
    private boolean cancelled;

    /**
     * Constructs a new {@link CombatCumulativeStatisticUpdateEvent}.
     *
     * @param entityUUID The UUID of the entity whose session ended.
     * @param statistics The per-session statistics snapshot to be applied.
     */
    public CombatCumulativeStatisticUpdateEvent(@NotNull UUID entityUUID,
                                                @NotNull CombatSessionStatisticsSnapshot statistics) {
        this.entityUUID = entityUUID;
        this.statistics = statistics;
    }

    /**
     * Gets the UUID of the entity whose session-end statistics are about to be applied
     * to cumulative totals.
     *
     * @return The entity UUID.
     */
    @NotNull
    public UUID getEntityUUID() {
        return entityUUID;
    }

    /**
     * Gets the per-session statistics snapshot that will be applied to cumulative statistics.
     *
     * @return The {@link CombatSessionStatisticsSnapshot}.
     */
    @NotNull
    public CombatSessionStatisticsSnapshot getStatistics() {
        return statistics;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit to locate the handler list for this event class when
     * a listener registers for {@link CombatCumulativeStatisticUpdateEvent}.
     *
     * @return The shared {@link HandlerList} for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
