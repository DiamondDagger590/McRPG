package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs a {@link Level#WARNING} about a {@link CombatStateType} at most once per key.
 * <p>
 * A misbehaving registrant callback — a resolver, serializer, or deserializer that throws, returns
 * {@code null}, or returns the wrong type — fails deterministically: the resolver on every read, a
 * corrupt database row on every session start. Logging each occurrence would flood the log with the
 * same stack trace, so callers funnel their warnings through here and the first for a given key wins.
 * <p>
 * <b>Eviction:</b> never — insert-only for this object's lifetime. That is bounded and intentional:
 * keys are {@link NamespacedKey}s of state types, effectively static constants, so the set holds at
 * most one entry per state type the server's plugins define. A new instance is constructed with its
 * owner (a {@link CombatStateCodec}, a {@link us.eunoians.mcrpg.combat.CombatSession}) and discarded
 * with it. One instance tracks one callback kind, so a faulty serializer does not suppress the
 * warning for the same type's faulty deserializer — the reason an owner holds a separate instance
 * per callback rather than sharing one.
 * <p>
 * Internal to the combat tracker. It is {@code public} only because its owners span the
 * {@code combat} and {@code combat.state} packages; it is not part of McRPG's extension API.
 */
public final class StateTypeWarningLog {

    private final Logger logger;
    private final Set<NamespacedKey> warnedKeys;

    /**
     * Constructs a new {@link StateTypeWarningLog}.
     *
     * @param logger The logger to write warnings to.
     */
    public StateTypeWarningLog(@NotNull Logger logger) {
        this.logger = logger;
        this.warnedKeys = new HashSet<>();
    }

    /**
     * Logs a warning for the given state type key, unless one has already been logged for it.
     *
     * @param key     The state type key the warning is about.
     * @param message The warning message.
     * @param cause   The throwable that triggered the warning, or {@code null} if there was none.
     */
    public void warnOnce(@NotNull NamespacedKey key, @NotNull String message, @Nullable Throwable cause) {
        if (warnedKeys.add(key)) {
            logger.log(Level.WARNING, message, cause);
        }
    }
}
