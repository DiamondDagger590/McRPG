package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Immutable snapshot of all combat state data on a session, created at session end for inclusion in
 * {@link us.eunoians.mcrpg.event.combat.CombatSessionEndEvent}. Captures both raw and resolved
 * values at the moment of snapshot creation (while the session still exists and resolvers can run).
 */
public final class CombatStateSnapshot {

    private final Map<NamespacedKey, Object> rawValues;
    private final Map<NamespacedKey, Object> resolvedValues;

    /**
     * Constructs a new {@link CombatStateSnapshot}. Public because the sole caller,
     * {@code CombatSession.createStateSnapshot(CombatStateTypeRegistry)}, lives in
     * {@code us.eunoians.mcrpg.combat} — a different package than this class. This mirrors
     * the constructor visibility of other cross-package-constructed immutable holders such as
     * {@link us.eunoians.mcrpg.event.combat.CombatStateChangeEvent} (also built by {@code CombatSession}),
     * {@code SkillDataSnapshot}, and {@code AbilityData}. {@code CombatSessionStatisticsSnapshot} is the
     * exception, not the rule: its constructor stays package-private only because its sole caller,
     * {@code CombatSessionStatistics.snapshot()}, shares its package.
     *
     * @param rawValues      Immutable map of raw stored values keyed by state type key.
     * @param resolvedValues Immutable map of resolved values at snapshot time.
     */
    public CombatStateSnapshot(@NotNull Map<NamespacedKey, Object> rawValues,
                               @NotNull Map<NamespacedKey, Object> resolvedValues) {
        this.rawValues = rawValues;
        this.resolvedValues = resolvedValues;
    }

    /**
     * Gets the resolved value for a state type at the time the snapshot was taken. Returns
     * the type's default value if the state was not present. Uses an unchecked cast — callers
     * must ensure the type parameter matches the type used during {@code setState}.
     *
     * @param type The state type to query.
     * @param <T>  The state value type.
     * @return The resolved value, or the type's default value if absent.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getState(@NotNull CombatStateType<T> type) {
        Object value = resolvedValues.get(type.getKey());
        return value != null ? (T) value : type.getDefaultValue();
    }

    /**
     * Gets the raw stored value for a state type at the time the snapshot was taken. Returns
     * the type's default value if the state was not present.
     *
     * @param type The state type to query.
     * @param <T>  The state value type.
     * @return The raw value, or the type's default value if absent.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getRawState(@NotNull CombatStateType<T> type) {
        Object value = rawValues.get(type.getKey());
        return value != null ? (T) value : type.getDefaultValue();
    }

    /**
     * Checks whether the snapshot contains a value for the given key.
     *
     * @param key The key to check.
     * @return {@code true} if a value exists.
     */
    public boolean hasState(@NotNull NamespacedKey key) {
        return rawValues.containsKey(key);
    }

    /**
     * Gets the set of all state type keys present in the snapshot.
     *
     * @return An unmodifiable {@link Set} of keys.
     */
    @NotNull
    public Set<NamespacedKey> getStateKeys() {
        return Collections.unmodifiableSet(rawValues.keySet());
    }
}
