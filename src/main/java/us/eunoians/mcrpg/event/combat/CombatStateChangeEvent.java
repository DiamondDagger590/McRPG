package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.state.CombatStateType;

/**
 * Fired when combat state is modified via {@code setState} or {@code modifyState}. Cancellable —
 * cancelling prevents the state change from being applied. The new value is modifiable by listeners
 * (e.g., a buff ability that doubles stack gains).
 * <p>
 * Bukkit's event system does not support generic events, so the old/new values are typed as
 * {@link Object}. Listeners check the state type and cast:
 * <pre>{@code
 * if (event.getStateType().equals(MY_TYPE)) {
 *     Integer oldVal = (Integer) event.getOldValue();
 *     Integer newVal = (Integer) event.getNewValue();
 * }
 * }</pre>
 * <p>
 * <b>Not fired for:</b> persistent state re-attached at session start (restored raw from the
 * database) and session-scoped state cleared at session end. Consumers mirroring combat state
 * externally — a scoreboard, a boss bar — should also observe
 * {@link CombatSessionStartEvent} and {@link CombatSessionEndEvent}, whose
 * {@link CombatSessionEndEvent#getCombatState()} snapshot carries the end-of-session values.
 */
public class CombatStateChangeEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CombatSession session;
    private final CombatStateType<?> stateType;
    private final Object oldValue;
    private Object newValue;
    private boolean cancelled;

    /**
     * Constructs a new {@link CombatStateChangeEvent}.
     *
     * @param session   The session whose state is changing.
     * @param stateType The state type being modified.
     * @param oldValue  The value before the change.
     * @param newValue  The proposed new value.
     */
    public CombatStateChangeEvent(@NotNull CombatSession session,
                                   @NotNull CombatStateType<?> stateType,
                                   @NotNull Object oldValue,
                                   @NotNull Object newValue) {
        this.session = session;
        this.stateType = stateType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Gets the session whose state is changing.
     *
     * @return The {@link CombatSession}.
     */
    @NotNull
    public CombatSession getSession() {
        return session;
    }

    /**
     * Gets the state type being modified.
     *
     * @return The {@link CombatStateType}.
     */
    @NotNull
    public CombatStateType<?> getStateType() {
        return stateType;
    }

    /**
     * Gets the value before the change.
     *
     * @return The old value.
     */
    @NotNull
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Gets the proposed new value. May be modified by listeners via {@link #setNewValue(Object)}.
     *
     * @return The new value.
     */
    @NotNull
    public Object getNewValue() {
        return newValue;
    }

    /**
     * Sets the new value. Listeners can modify the incoming value without cancelling the event
     * (e.g., a buff ability that doubles stack gains).
     *
     * @param newValue The modified new value.
     */
    public void setNewValue(@NotNull Object newValue) {
        this.newValue = newValue;
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
     * a listener registers for {@link CombatStateChangeEvent}.
     *
     * @return The shared {@link HandlerList} for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
