package us.eunoians.mcrpg.combat.state;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.combat.CombatSession;

/**
 * Functional interface for computing the effective value of a combat state from its raw stored value
 * plus external context. Resolvers must be pure and side-effect-free — reads should never mutate
 * state. The resolver runs on every {@code getState()} call, so it should be lightweight.
 *
 * @param <T> The state value type.
 */
@FunctionalInterface
public interface CombatStateResolver<T> {

    /**
     * Computes the effective value of a combat state from the raw stored value and the
     * current session context.
     * <p>
     * This method must be <b>pure</b> — it must not mutate the session, the entity, or
     * any external state. It runs on every {@code getState()} call and must be lightweight
     * (O(1) operations only — checking active potion effects is O(1) in Paper).
     *
     * @param session  The combat session the state belongs to.
     * @param rawValue The raw stored value (last written via {@code setState}).
     * @return The effective value after resolution.
     */
    @NotNull
    T resolve(@NotNull CombatSession session, @NotNull T rawValue);
}
