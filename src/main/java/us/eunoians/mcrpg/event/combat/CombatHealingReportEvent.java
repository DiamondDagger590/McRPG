package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when healing is attributed to a healer via
 * {@link us.eunoians.mcrpg.combat.CombatTrackerManager#reportHealing(UUID, UUID, double)}, before the
 * {@code healing_dealt} statistic is credited. Cancellable — cancelling suppresses the attribution
 * without affecting the heal itself, which has already been applied by the caller.
 * <p>
 * This is the interception point for healing attribution: an arena ruleset that discounts healing,
 * a plugin that scales credited healing, or a leaderboard that wants per-target attribution can all
 * observe it. {@link #getAmount()} is modifiable, so a listener can adjust the credited amount
 * without cancelling.
 * <p>
 * Note the asymmetry with {@code healing_received}: this event carries the attributed <em>dealt</em>
 * side only. The target's {@code healing_received} is credited separately by
 * {@link us.eunoians.mcrpg.listener.combat.OnCombatHealingStatListener} from Bukkit's
 * {@link org.bukkit.event.entity.EntityRegainHealthEvent}, which is cancellable in its own right —
 * cancelling this event does not suppress that.
 */
public class CombatHealingReportEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID healerUUID;
    private final UUID targetUUID;
    private double amount;
    private boolean cancelled;

    /**
     * Constructs a new {@link CombatHealingReportEvent}.
     *
     * @param healerUUID The UUID of the entity that performed the healing.
     * @param targetUUID The UUID of the entity that was healed.
     * @param amount     The amount of healing being attributed.
     */
    public CombatHealingReportEvent(@NotNull UUID healerUUID, @NotNull UUID targetUUID, double amount) {
        this.healerUUID = healerUUID;
        this.targetUUID = targetUUID;
        this.amount = amount;
    }

    /**
     * Gets the UUID of the entity that performed the healing.
     *
     * @return The healer's UUID.
     */
    @NotNull
    public UUID getHealerUUID() {
        return healerUUID;
    }

    /**
     * Gets the UUID of the entity that was healed.
     *
     * @return The target's UUID.
     */
    @NotNull
    public UUID getTargetUUID() {
        return targetUUID;
    }

    /**
     * Gets the amount of healing being attributed to the healer.
     *
     * @return The healing amount.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount of healing to credit to the healer. Lets a listener scale the attributed
     * amount without cancelling the attribution outright.
     *
     * @param amount The healing amount to credit.
     */
    public void setAmount(double amount) {
        this.amount = amount;
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

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
