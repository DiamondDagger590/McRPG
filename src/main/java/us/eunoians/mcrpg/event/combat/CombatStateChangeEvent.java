package us.eunoians.mcrpg.event.combat;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.state.CombatStateType;

import java.util.Map;

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

    /**
     * Maps each primitive {@link Class} token to its boxed equivalent, so a state type declared with
     * {@code int.class} accepts an {@link Integer}. See {@link #isAssignableToStateType}.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_BOX_TYPES = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class);

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
     * <p>
     * The value must be an instance of {@link #getStateType()}'s
     * {@link CombatStateType#getType() class token}. This event cannot be generic — Bukkit's event
     * system does not support generic events — so the check happens here, at the point of the call,
     * rather than at the eventual store. Validating here means Bukkit's event bus attributes the
     * resulting exception to <em>your</em> plugin; a wrong-typed value that slipped through to the
     * store would instead surface as a {@link ClassCastException} in whichever unrelated plugin read
     * the state next.
     *
     * @param newValue The modified new value.
     * @throws IllegalArgumentException if {@code newValue} is not an instance of the state type's class.
     */
    public void setNewValue(@NotNull Object newValue) {
        if (!isAssignableToStateType(stateType, newValue)) {
            throw new IllegalArgumentException("Combat state type " + stateType.getKey() + " expects a "
                    + stateType.getType().getName() + " but setNewValue was called with "
                    + (newValue == null ? "null" : "a " + newValue.getClass().getName()));
        }
        this.newValue = newValue;
    }

    /**
     * Checks whether a value can be stored under a state type, treating a primitive class token as
     * its boxed equivalent.
     * <p>
     * {@code Class.isInstance} always returns {@code false} for a primitive {@link Class} object, and
     * {@code CombatStateType.of(key, int.class, 0, null)} compiles cleanly — {@code int.class} has
     * static type {@code Class<Integer>} and the default value autoboxes — so a naive
     * {@code isInstance} check would reject every write to such a type.
     *
     * @param stateType The state type the value would be stored under.
     * @param value     The candidate value.
     * @return {@code true} if the value is a non-null instance of the state type's class.
     */
    public static boolean isAssignableToStateType(@NotNull CombatStateType<?> stateType, @Nullable Object value) {
        if (value == null) {
            return false;
        }
        Class<?> expectedType = stateType.getType();
        if (expectedType.isPrimitive()) {
            expectedType = PRIMITIVE_BOX_TYPES.getOrDefault(expectedType, expectedType);
        }
        return expectedType.isInstance(value);
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
