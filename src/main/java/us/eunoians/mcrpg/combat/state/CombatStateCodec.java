package us.eunoians.mcrpg.combat.state;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.event.combat.CombatStateChangeEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Translates combat state values to and from their stored string form, using the serializer and
 * deserializer supplied by whoever registered the {@link CombatStateType}.
 * <p>
 * Those functions are third-party code, and the deserializer's input is a database string —
 * hand-editable, and liable to drift across plugin versions. Every call is therefore treated as
 * untrusted: a throw, a {@code null} return, and (on decode) a wrong-typed return are all handled
 * the same way, by reporting an empty result so the caller can fall back. Nothing propagates,
 * because these run inside session start and session end, where an escaping exception would strand
 * the session in the tracker's active map.
 * <p>
 * The type check on decode is not redundant with the one in
 * {@link us.eunoians.mcrpg.combat.CombatSession}: the decoded value is written into the raw state
 * store directly, bypassing {@code CombatSession#setState}'s validation, so a raw-typed deserializer
 * could otherwise seed a value that only fails much later in an unrelated reader.
 * <p>
 * Warnings are deduplicated per state type via {@link StateTypeWarningLog}. A serializer or
 * deserializer that fails does so deterministically — the bad database row is retried on every
 * session start for that entity, which for a player cycling in and out of combat is several times a
 * minute — so an unbounded warning would be a stack trace per attempt. The dedup state is bounded by
 * the number of distinct state types the server's plugins define, and is discarded with this object
 * on disable.
 */
public class CombatStateCodec {

    private final StateTypeWarningLog serializerWarnings;
    private final StateTypeWarningLog deserializerWarnings;

    /**
     * Constructs a new {@link CombatStateCodec}.
     *
     * @param logger The logger to report faulty registrant callbacks to.
     */
    public CombatStateCodec(@NotNull Logger logger) {
        this.serializerWarnings = new StateTypeWarningLog(logger);
        this.deserializerWarnings = new StateTypeWarningLog(logger);
    }

    /**
     * Encodes a raw state value using its type's registered serializer.
     * <p>
     * An empty result means the value could not be encoded, and the caller should skip persisting
     * that entry. Skipping is not free, and the log line says so: the previous database row survives
     * untouched, so the entity silently reverts to older state and reads it back next session as if
     * current. That is still preferable to failing the whole flush, but it is the one outcome here
     * that yields <em>wrong</em> data rather than degraded data, so it is never silent — which is
     * why a {@code null} return is reported rather than folded into the "no serializer declared"
     * case that {@link Optional#map} would make it indistinguishable from.
     *
     * @param type       The state type owning the value.
     * @param rawValue   The raw stored value.
     * @param entityUUID The UUID of the entity whose state is being encoded, for the log message.
     * @param <T>        The state value type.
     * @return The encoded value, or empty if no serializer is declared, the serializer threw, or it
     *         returned {@code null}.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> Optional<String> encode(@NotNull CombatStateType<T> type, @NotNull Object rawValue,
                                       @NotNull UUID entityUUID) {
        Optional<Function<T, String>> serializer = type.getSerializer();
        if (serializer.isEmpty()) {
            return Optional.empty();
        }
        try {
            String encoded = serializer.get().apply((T) rawValue);
            if (encoded == null) {
                warnFaultySerializer(type, entityUUID, "returned null", null);
                return Optional.empty();
            }
            return Optional.of(encoded);
        } catch (Exception e) {
            warnFaultySerializer(type, entityUUID, "threw", e);
            return Optional.empty();
        }
    }

    /**
     * Decodes a stored value using its type's registered deserializer.
     * <p>
     * An empty result means the caller should leave the state at its default rather than
     * re-attaching it. Reporting instead of propagating matters here: a value that fails to decode
     * stays in the cache and is retried on every session start for that entity, so an escaping
     * exception would break session creation for them on every subsequent hit until restart.
     *
     * @param type            The state type owning the value.
     * @param serializedValue The stored value loaded from the database.
     * @param entityUUID      The UUID of the entity the value belongs to, for the log message.
     * @param <T>             The state value type.
     * @return The decoded value, or empty if no deserializer is declared, the deserializer threw,
     *         returned {@code null}, or returned a value of the wrong type.
     */
    @NotNull
    public <T> Optional<T> decode(@NotNull CombatStateType<T> type, @NotNull String serializedValue,
                                  @NotNull UUID entityUUID) {
        Optional<Function<String, T>> deserializer = type.getDeserializer();
        if (deserializer.isEmpty()) {
            return Optional.empty();
        }
        T decoded;
        try {
            decoded = deserializer.get().apply(serializedValue);
        } catch (Exception e) {
            warnFaultyDeserializer(type, entityUUID, serializedValue, "threw", e);
            return Optional.empty();
        }
        if (!CombatStateChangeEvent.isAssignableToStateType(type, decoded)) {
            warnFaultyDeserializer(type, entityUUID, serializedValue,
                    "returned " + (decoded == null
                            ? "null" : "a " + decoded.getClass().getName()
                            + " rather than a " + type.getType().getName()), null);
            return Optional.empty();
        }
        return Optional.of(decoded);
    }

    /**
     * Logs a serializer misbehaviour once per state type, naming the entity whose state is now stale.
     *
     * @param type       The state type whose serializer misbehaved.
     * @param entityUUID The UUID of the entity whose state was not persisted.
     * @param problem    A short description of what the serializer did.
     * @param cause      The exception it threw, or {@code null} if it returned null instead.
     */
    private void warnFaultySerializer(@NotNull CombatStateType<?> type, @NotNull UUID entityUUID,
                                      @NotNull String problem, @Nullable Throwable cause) {
        serializerWarnings.warnOnce(type.getKey(), "Combat state type " + type.getKey() + " serializer "
                + problem + " while persisting state for entity " + entityUUID + "; this entry was not "
                + "written, so the previously stored value stays in the database and will be loaded back "
                + "as current on the next session. Further occurrences for this state type are not logged.",
                cause);
    }

    /**
     * Logs a deserializer misbehaviour once per state type.
     *
     * @param type            The state type whose deserializer misbehaved.
     * @param entityUUID      The UUID of the entity whose stored value triggered it.
     * @param serializedValue The stored value that could not be decoded.
     * @param problem         A short description of what the deserializer did.
     * @param cause           The exception it threw, or {@code null} if it returned a bad value.
     */
    private void warnFaultyDeserializer(@NotNull CombatStateType<?> type, @NotNull UUID entityUUID,
                                        @NotNull String serializedValue, @NotNull String problem,
                                        @Nullable Throwable cause) {
        deserializerWarnings.warnOnce(type.getKey(), "Combat state type " + type.getKey() + " deserializer "
                + problem + " for stored value \"" + serializedValue + "\" belonging to entity " + entityUUID
                + "; leaving the state at its default value. Further occurrences for this state type are "
                + "not logged.", cause);
    }
}
