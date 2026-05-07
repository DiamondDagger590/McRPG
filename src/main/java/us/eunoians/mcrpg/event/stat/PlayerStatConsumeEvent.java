package us.eunoians.mcrpg.event.stat;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * A cancellable Bukkit event fired before every {@link us.eunoians.mcrpg.stat.instance.PlayerStatInstance#consume(double)}
 * call that originates from ability activation.
 * <p>
 * Allows third-party plugins to implement mana-drain, mana-shield, cost reduction, or
 * consumption logging. Cancelling this event prevents the consumption from occurring and
 * suppresses the ability activation entirely (the activation caller is expected to skip
 * activation when this event is cancelled).
 * <p>
 * Listeners may also adjust the effective amount via {@link #setEffectiveAmount(double)}
 * without cancelling — the adjusted value will be consumed instead of the requested amount.
 * Setting it to {@code 0} grants a free cast; raising it applies a mana-drain effect.
 * <p>
 * This event fires at exactly two sites:
 * <ol>
 *   <li>The combo path — before consumption inside
 *       {@link us.eunoians.mcrpg.listener.ability.OnComboCompleteListener}.</li>
 *   <li>The passive/event-driven path — before passive mana consumption inside
 *       {@link us.eunoians.mcrpg.listener.ability.AbilityListener#activateAbilities}.</li>
 * </ol>
 * It does NOT fire during regen ticking or direct
 * {@link us.eunoians.mcrpg.stat.instance.PlayerStatInstance#setCurrent(double)} calls.
 */
public class PlayerStatConsumeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final AbilityHolder holder;
    private final NamespacedKey statKey;
    private final double requestedAmount;
    private double effectiveAmount;
    private boolean cancelled;

    /**
     * @param holder          The entity attempting to consume the stat.
     * @param statKey         The key of the player stat being consumed (e.g., mana).
     * @param requestedAmount The original amount requested for consumption.
     */
    public PlayerStatConsumeEvent(@NotNull AbilityHolder holder,
                                  @NotNull NamespacedKey statKey,
                                  double requestedAmount) {
        this.holder = holder;
        this.statKey = statKey;
        this.requestedAmount = requestedAmount;
        this.effectiveAmount = requestedAmount;
    }

    /**
     * @return The {@link AbilityHolder} attempting to consume the stat.
     */
    @NotNull
    public AbilityHolder getHolder() {
        return holder;
    }

    /**
     * @return The key of the player stat being consumed (e.g., {@code mcrpg:mana}).
     */
    @NotNull
    public NamespacedKey getStatKey() {
        return statKey;
    }

    /**
     * @return The original consumption amount before any listener modifications.
     */
    public double getRequestedAmount() {
        return requestedAmount;
    }

    /**
     * @return The adjusted consumption amount that will actually be deducted.
     */
    public double getEffectiveAmount() {
        return effectiveAmount;
    }

    /**
     * Adjusts the actual amount consumed without cancelling the activation.
     * Set to {@code 0} for a free cast; set higher than the requested amount for a mana-drain effect.
     *
     * @param effectiveAmount The adjusted consumption amount (must be non-negative).
     * @throws IllegalArgumentException If {@code effectiveAmount} is negative.
     */
    public void setEffectiveAmount(double effectiveAmount) {
        if (effectiveAmount < 0) {
            throw new IllegalArgumentException("effectiveAmount must be >= 0, got: " + effectiveAmount);
        }
        this.effectiveAmount = effectiveAmount;
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
        return HANDLER_LIST;
    }

    /**
     * @return The static {@link HandlerList} for this event type.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
