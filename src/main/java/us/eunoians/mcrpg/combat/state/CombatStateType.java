package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;
import java.util.function.Function;

/**
 * Typed, keyed definition of a combat state that can be attached to sessions. Immutable once
 * constructed. Three factory methods correspond to the three use cases:
 * <ul>
 *     <li>{@link #of(NamespacedKey, Class, Object, NamespacedKey)} — simple session-scoped state,
 *     raw value returned directly</li>
 *     <li>{@link #resolved(NamespacedKey, Class, Object, CombatStateResolver, NamespacedKey)} —
 *     session-scoped state with a resolver that computes the effective value on read</li>
 *     <li>{@link #persistent(NamespacedKey, Class, Object, Function, Function, NamespacedKey)} —
 *     state that survives session boundaries, backed by a DAO with registrant-provided
 *     serialization</li>
 * </ul>
 * <p>
 * Implements {@link McRPGContent} for {@code ContentPack} registration — required by the
 * {@code McRPGContentPack<T extends McRPGContent>} bound that
 * {@link us.eunoians.mcrpg.expansion.content.CombatStateTypeContentPack} is built on. Since
 * {@link CombatStateType} is instantiated through static factories rather than subclassed per
 * state type, each factory takes a {@code @Nullable NamespacedKey expansionKey} parameter
 * (mirroring the {@code StatisticContent} wrapper's constructor pattern) so callers can identify
 * which {@link us.eunoians.mcrpg.expansion.ContentExpansion} — if any — owns the state type;
 * {@link #getExpansionKey()} wraps it in an {@link Optional}. Third-party callers pass their
 * expansion's key; a {@code null} expansion key is valid for state types not tied to a specific
 * expansion.
 *
 * @param <T> The state value type.
 */
public final class CombatStateType<T> implements McRPGContent {

    private final NamespacedKey key;
    private final Class<T> type;
    private final T defaultValue;
    private final CombatStateLifecycle lifecycle;
    @Nullable
    private final CombatStateResolver<T> resolver;
    @Nullable
    private final Function<T, String> serializer;
    @Nullable
    private final Function<String, T> deserializer;
    @Nullable
    private final NamespacedKey expansionKey;

    /**
     * Private constructor — use the static factory methods.
     */
    private CombatStateType(@NotNull NamespacedKey key, @NotNull Class<T> type,
                            @NotNull T defaultValue, @NotNull CombatStateLifecycle lifecycle,
                            @Nullable CombatStateResolver<T> resolver,
                            @Nullable Function<T, String> serializer,
                            @Nullable Function<String, T> deserializer,
                            @Nullable NamespacedKey expansionKey) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.lifecycle = lifecycle;
        this.resolver = resolver;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.expansionKey = expansionKey;
    }

    /**
     * Creates a simple session-scoped state type with no resolver. {@code getState()} returns the
     * raw value directly. State is cleared when the session ends.
     * <p>
     * Register the type even though session-scoped reads and writes work without it: the combat
     * tracker logs a one-time warning for any state key it finds on a session with no registered
     * type, since it cannot tell a deliberately-unregistered session type from a persistent type
     * whose registration was forgotten (which would silently discard player data at every session
     * end).
     *
     * @param key          The unique key identifying this state type.
     * @param type         The class of the state value.
     * @param defaultValue The initial value for new sessions.
     * @param expansionKey The {@link NamespacedKey} of the owning {@link us.eunoians.mcrpg.expansion.ContentExpansion},
     *                     or {@code null} if this state type is not tied to a specific expansion.
     * @param <T>          The state value type.
     * @return A new session-scoped {@link CombatStateType}.
     */
    @NotNull
    public static <T> CombatStateType<T> of(@NotNull NamespacedKey key,
                                             @NotNull Class<T> type,
                                             @NotNull T defaultValue,
                                             @Nullable NamespacedKey expansionKey) {
        return new CombatStateType<>(key, type, defaultValue, CombatStateLifecycle.SESSION,
                null, null, null, expansionKey);
    }

    /**
     * Creates a session-scoped state type with a resolver. {@code getState()} returns the
     * resolver's output; {@code getRawState()} returns the stored value. State is cleared
     * when the session ends.
     * <p>
     * The resolver is only applied at session boundaries — in the {@code CombatStateSnapshot}
     * carried by {@link us.eunoians.mcrpg.event.combat.CombatSessionEndEvent} — if this type is
     * registered, via {@link us.eunoians.mcrpg.expansion.content.CombatStateTypeContentPack} or
     * {@code CombatTrackerManager#registerStateType(CombatStateType)}. Live {@code getState} /
     * {@code setState} work without registration; end-of-session resolution does not, and an
     * unregistered type's snapshot reports its raw value instead.
     *
     * @param key          The unique key identifying this state type.
     * @param type         The class of the state value.
     * @param defaultValue The initial value for new sessions.
     * @param resolver     The resolver that computes the effective value on every read.
     * @param expansionKey The {@link NamespacedKey} of the owning {@link us.eunoians.mcrpg.expansion.ContentExpansion},
     *                     or {@code null} if this state type is not tied to a specific expansion.
     * @param <T>          The state value type.
     * @return A new resolved session-scoped {@link CombatStateType}.
     */
    @NotNull
    public static <T> CombatStateType<T> resolved(@NotNull NamespacedKey key,
                                                    @NotNull Class<T> type,
                                                    @NotNull T defaultValue,
                                                    @NotNull CombatStateResolver<T> resolver,
                                                    @Nullable NamespacedKey expansionKey) {
        return new CombatStateType<>(key, type, defaultValue, CombatStateLifecycle.SESSION,
                resolver, null, null, expansionKey);
    }

    /**
     * Creates a persistent state type. The value survives session boundaries — saved to the DB
     * on session end and re-loaded on the next session start. The registrant provides a
     * serializer/deserializer pair for DB round-tripping. The registrant is responsible for
     * cleanup policy (TTL, daily reset); the tracker only stores and loads.
     * <p>
     * Persistence only happens if this type is registered, via
     * {@link us.eunoians.mcrpg.expansion.content.CombatStateTypeContentPack} or
     * {@code CombatTrackerManager#registerStateType(CombatStateType)}. Session-scoped
     * {@code getState} / {@code setState} work without registration, so an unregistered type
     * behaves normally for the life of a session and then does not survive it. The combat tracker
     * logs a warning the first time it encounters an unregistered state key at save time.
     *
     * @param key          The unique key identifying this state type.
     * @param type         The class of the state value.
     * @param defaultValue The initial value when no persisted value exists.
     * @param serializer   Converts the value to a string for DB storage.
     * @param deserializer Converts a DB string back to the value.
     * @param expansionKey The {@link NamespacedKey} of the owning {@link us.eunoians.mcrpg.expansion.ContentExpansion},
     *                     or {@code null} if this state type is not tied to a specific expansion.
     * @param <T>          The state value type.
     * @return A new persistent {@link CombatStateType}.
     */
    @NotNull
    public static <T> CombatStateType<T> persistent(@NotNull NamespacedKey key,
                                                      @NotNull Class<T> type,
                                                      @NotNull T defaultValue,
                                                      @NotNull Function<T, String> serializer,
                                                      @NotNull Function<String, T> deserializer,
                                                      @Nullable NamespacedKey expansionKey) {
        return new CombatStateType<>(key, type, defaultValue, CombatStateLifecycle.PERSISTENT,
                null, serializer, deserializer, expansionKey);
    }

    /**
     * Gets the unique key identifying this state type.
     *
     * @return The {@link NamespacedKey}.
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Gets the class of the state value.
     *
     * @return The value {@link Class}.
     */
    @NotNull
    public Class<T> getType() {
        return type;
    }

    /**
     * Gets the default value used for new sessions when no stored value exists.
     *
     * @return The default value.
     */
    @NotNull
    public T getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the lifecycle scope of this state type.
     *
     * @return The {@link CombatStateLifecycle}.
     */
    @NotNull
    public CombatStateLifecycle getLifecycle() {
        return lifecycle;
    }

    /**
     * Gets the resolver for this state type, if one is declared.
     *
     * @return An {@link Optional} containing the resolver, or empty for simple types.
     */
    @NotNull
    public Optional<CombatStateResolver<T>> getResolver() {
        return Optional.ofNullable(resolver);
    }

    /**
     * Gets the serializer for this state type, if one is declared.
     *
     * @return An {@link Optional} containing the serializer, or empty for non-persistent types.
     */
    @NotNull
    public Optional<Function<T, String>> getSerializer() {
        return Optional.ofNullable(serializer);
    }

    /**
     * Gets the deserializer for this state type, if one is declared.
     *
     * @return An {@link Optional} containing the deserializer, or empty for non-persistent types.
     */
    @NotNull
    public Optional<Function<String, T>> getDeserializer() {
        return Optional.ofNullable(deserializer);
    }

    /**
     * Checks whether this state type has persistent lifecycle.
     *
     * @return {@code true} if the lifecycle is {@link CombatStateLifecycle#PERSISTENT}.
     */
    public boolean isPersistent() {
        return lifecycle == CombatStateLifecycle.PERSISTENT;
    }

    /**
     * Checks whether this state type declares a resolver.
     *
     * @return {@code true} if a resolver is present.
     */
    public boolean hasResolver() {
        return resolver != null;
    }

    /**
     * Gets the {@link us.eunoians.mcrpg.expansion.ContentExpansion} key that owns this state type, if any.
     *
     * @return An {@link Optional} containing the {@link NamespacedKey} of the owning
     * {@link us.eunoians.mcrpg.expansion.ContentExpansion}, or empty if this state type
     * is not tied to a specific expansion.
     */
    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.ofNullable(expansionKey);
    }
}
