package us.eunoians.mcrpg.combat;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatistics;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;
import us.eunoians.mcrpg.combat.state.CombatStateResolver;
import us.eunoians.mcrpg.combat.state.CombatStateSnapshot;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.combat.state.StateTypeWarningLog;
import us.eunoians.mcrpg.event.combat.CombatStateChangeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

/**
 * Represents an active combat session for a single entity (the "session owner"). A session
 * tracks every opponent the owner has interacted with as a {@link CombatParticipant} and
 * maintains timing state for both session-level and per-participant inactivity timeouts.
 * <p>
 * Participants are stored in two collections:
 * <ul>
 *   <li><b>Player participants</b> — stored in an unbounded {@link HashMap} keyed by UUID.
 *       There is no cap on the number of player opponents tracked.</li>
 *   <li><b>Mob participants</b> — stored in a bounded {@link LinkedHashMap} keyed by UUID, whose
 *       insertion order provides FIFO eviction. When adding a new mob would exceed
 *       {@code maxMobParticipants}, the oldest mob is evicted and returned from
 *       {@link #addParticipant(CombatParticipant)} so the caller can fire the appropriate removal
 *       event. Keying by UUID makes lookup, containment, and removal O(1) on the combat hot path and
 *       prevents duplicate entries for the same mob.</li>
 * </ul>
 * <p>
 * The {@link CombatType} (PVP vs PVE) is derived dynamically from the participant roster
 * rather than stored — a session is PVP if at least one player participant is present,
 * PVE otherwise. This means the combat type can change mid-session as participants are
 * added or removed.
 * <p>
 * Sessions are created, queried, and ended through the {@link CombatTrackerManager}, which
 * owns the session map and fires the corresponding lifecycle events
 * ({@link us.eunoians.mcrpg.event.combat.CombatSessionStartEvent},
 * {@link us.eunoians.mcrpg.event.combat.CombatSessionEndEvent}, etc.). This class is a
 * plain data container with no direct Bukkit event interaction.
 *
 * @see CombatTrackerManager
 * @see CombatParticipant
 * @see CombatType
 */
public class CombatSession {

    private final UUID entityUUID;
    private final Map<UUID, CombatParticipant> playerParticipants;
    private final LinkedHashMap<UUID, CombatParticipant> mobParticipants;
    private final int maxMobParticipants;
    private final long startTimeMillis;
    private final long timeoutMillis;
    private long lastActivityMillis;
    private final Map<NamespacedKey, Object> stateStore;
    private final CombatSessionStatistics statistics;
    /**
     * Deduplicates warnings about a throwing resolver, so a resolver that fails on the hot read path
     * is reported once per state type rather than once per read. Session-scoped, so it re-surfaces a
     * persistent problem once each time the entity re-enters combat.
     */
    private final StateTypeWarningLog resolverWarnings;

    /**
     * Constructs a new {@link CombatSession}.
     *
     * @param entityUUID         The UUID of the entity that owns this session.
     * @param maxMobParticipants The maximum number of mob participants before FIFO eviction.
     * @param timeoutMillis      The inactivity timeout in milliseconds.
     */
    public CombatSession(@NotNull UUID entityUUID, int maxMobParticipants, long timeoutMillis) {
        this.entityUUID = entityUUID;
        this.playerParticipants = new HashMap<>();
        this.mobParticipants = new LinkedHashMap<>();
        this.maxMobParticipants = maxMobParticipants;
        long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        this.startTimeMillis = nowMillis;
        this.lastActivityMillis = nowMillis;
        this.timeoutMillis = timeoutMillis;
        this.stateStore = new HashMap<>();
        this.statistics = new CombatSessionStatistics();
        this.resolverWarnings = new StateTypeWarningLog(McRPG.getInstance().getLogger());
    }

    /**
     * Gets the UUID of the entity that owns this session.
     *
     * @return The UUID of the session owner.
     */
    @NotNull
    public UUID getEntityUUID() {
        return entityUUID;
    }

    /**
     * Derives the current {@link CombatType} from the participant roster.
     * Returns {@link CombatType#PVP} if the roster contains at least one player,
     * {@link CombatType#PVE} otherwise.
     *
     * @return The derived {@link CombatType}.
     */
    @NotNull
    public CombatType getCombatType() {
        return playerParticipants.isEmpty() ? CombatType.PVE : CombatType.PVP;
    }

    /**
     * Gets an unmodifiable view of all participants (both players and mobs).
     *
     * @return An unmodifiable {@link Collection} of all {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getParticipants() {
        List<CombatParticipant> allParticipants = new ArrayList<>(playerParticipants.size() + mobParticipants.size());
        allParticipants.addAll(playerParticipants.values());
        allParticipants.addAll(mobParticipants.values());
        return Collections.unmodifiableList(allParticipants);
    }

    /**
     * Gets an unmodifiable view of player participants.
     *
     * @return An unmodifiable {@link Collection} of player {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getPlayerParticipants() {
        return Collections.unmodifiableCollection(playerParticipants.values());
    }

    /**
     * Gets an unmodifiable view of mob participants.
     *
     * @return An unmodifiable {@link Collection} of mob {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getMobParticipants() {
        return Collections.unmodifiableCollection(mobParticipants.values());
    }

    /**
     * Gets a specific participant by UUID.
     *
     * @param participantUUID The UUID of the participant to find.
     * @return An {@link Optional} containing the participant, or empty if not found.
     */
    @NotNull
    public Optional<CombatParticipant> getParticipant(@NotNull UUID participantUUID) {
        CombatParticipant playerParticipant = playerParticipants.get(participantUUID);
        if (playerParticipant != null) {
            return Optional.of(playerParticipant);
        }
        return Optional.ofNullable(mobParticipants.get(participantUUID));
    }

    /**
     * Adds a participant to the roster. Player participants go into the unlimited player map. Mob
     * participants go into the bounded FIFO map — re-adding an existing mob updates it in place
     * without evicting or changing its FIFO position, and adding a new mob when the map is full
     * evicts and returns the oldest.
     *
     * @param participant The participant to add.
     * @return An {@link Optional} containing the evicted {@link CombatParticipant} if a mob was
     *         evicted from the FIFO map, or empty if no eviction occurred.
     */
    @NotNull
    Optional<CombatParticipant> addParticipant(@NotNull CombatParticipant participant) {
        if (participant.getParticipantType() == ParticipantType.PLAYER) {
            playerParticipants.put(participant.getUUID(), participant);
            return Optional.empty();
        }

        UUID mobUUID = participant.getUUID();
        // Re-adding an existing mob updates it in place — no eviction, FIFO position preserved.
        if (mobParticipants.containsKey(mobUUID)) {
            mobParticipants.put(mobUUID, participant);
            return Optional.empty();
        }

        Optional<CombatParticipant> evictedParticipant = Optional.empty();
        if (!mobParticipants.isEmpty() && mobParticipants.size() >= maxMobParticipants) {
            Iterator<CombatParticipant> mobIterator = mobParticipants.values().iterator();
            evictedParticipant = Optional.of(mobIterator.next());
            mobIterator.remove();
        }
        mobParticipants.put(mobUUID, participant);
        return evictedParticipant;
    }

    /**
     * Removes a participant from the roster by UUID.
     *
     * @param participantUUID The UUID of the participant to remove.
     * @return An {@link Optional} containing the removed participant, or empty if not found.
     */
    @NotNull
    Optional<CombatParticipant> removeParticipant(@NotNull UUID participantUUID) {
        CombatParticipant removedPlayer = playerParticipants.remove(participantUUID);
        if (removedPlayer != null) {
            return Optional.of(removedPlayer);
        }
        return Optional.ofNullable(mobParticipants.remove(participantUUID));
    }

    /**
     * Checks if the roster contains a participant with the given UUID.
     *
     * @param participantUUID The UUID to check.
     * @return {@code true} if the participant is in the roster.
     */
    public boolean hasParticipant(@NotNull UUID participantUUID) {
        return playerParticipants.containsKey(participantUUID) || mobParticipants.containsKey(participantUUID);
    }

    /**
     * Records combat activity on this session, resetting the inactivity timeout.
     */
    void recordActivity() {
        this.lastActivityMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
    }

    /**
     * Records an interaction with a specific participant, updating both the session's
     * last activity timestamp and the participant's last interaction timestamp.
     *
     * @param participantUUID The UUID of the participant involved in the interaction.
     */
    void recordParticipantInteraction(@NotNull UUID participantUUID) {
        long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        this.lastActivityMillis = nowMillis;
        getParticipant(participantUUID).ifPresent(participant -> participant.setLastInteractionMillis(nowMillis));
    }

    /**
     * Checks whether this session has exceeded its inactivity timeout.
     *
     * @return {@code true} if the session has been inactive longer than its timeout.
     */
    public boolean isTimedOut() {
        long currentTimeMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        return (currentTimeMillis - lastActivityMillis) >= timeoutMillis;
    }

    /**
     * Finds all participants whose per-participant inactivity timer has expired. Used by
     * {@link us.eunoians.mcrpg.combat.task.CombatSessionTimeoutTask} to identify participants
     * that should be removed from the roster.
     *
     * @return A {@link List} of participants that have timed out.
     */
    @NotNull
    public List<CombatParticipant> getTimedOutParticipants() {
        List<CombatParticipant> timedOutParticipants = new ArrayList<>();
        for (CombatParticipant participant : getParticipants()) {
            if (participant.isTimedOut(timeoutMillis)) {
                timedOutParticipants.add(participant);
            }
        }
        return timedOutParticipants;
    }

    /**
     * Gets the epoch milliseconds when this session started.
     *
     * @return The session start timestamp.
     */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    /**
     * Gets the epoch milliseconds of the most recent combat activity.
     *
     * @return The last activity timestamp.
     */
    public long getLastActivityMillis() {
        return lastActivityMillis;
    }

    /**
     * Computes the session duration in milliseconds, based on the current time.
     *
     * @return The duration in milliseconds.
     */
    public long getDurationMillis() {
        return McRPG.getInstance().getTimeProvider().now().toEpochMilli() - startTimeMillis;
    }

    /**
     * Gets the configured timeout for this session in milliseconds.
     *
     * @return The timeout in milliseconds.
     */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    /**
     * Checks whether the participant roster is empty.
     *
     * @return {@code true} if no participants remain in the roster.
     */
    public boolean isEmpty() {
        return playerParticipants.isEmpty() && mobParticipants.isEmpty();
    }

    /**
     * Gets the resolved value of a combat state. If the state type declares a
     * {@link us.eunoians.mcrpg.combat.state.CombatStateResolver}, the resolver is invoked with the
     * raw stored value and the current session to produce the effective value. If no resolver is
     * declared, returns the raw stored value. Returns the type's default value if no value has been
     * stored.
     * <p>
     * Resolvers are supplied by whoever registered the state type, so a faulty one is logged and the
     * raw value returned rather than letting the exception escape into an unrelated caller.
     *
     * @param type The state type to query.
     * @param <T>  The state value type.
     * @return The effective (resolved) value.
     */
    @NotNull
    public <T> T getState(@NotNull CombatStateType<T> type) {
        T rawValue = getRawState(type);
        return resolveDefensively(type, rawValue);
    }

    /**
     * Applies a state type's resolver to a raw value, falling back to the raw value if the resolver
     * throws or returns {@code null}. Third-party resolvers run on the combat read path, so a faulty
     * one must not propagate into an unrelated caller (a snapshot build, a session end, another
     * plugin's read), and one that fails on a hot read is warned about once per state type —
     * {@link CombatStateResolver} documents itself as running on every {@code getState()} call — so
     * the log is not flooded.
     * <p>
     * The {@code null} return is guarded even though {@link CombatStateResolver#resolve} is
     * {@code @NotNull}: that annotation is a contract, not a runtime guarantee, and a resolver that
     * violates it (a raw-typed or otherwise unchecked lambda) would send a {@code null} into
     * {@code Map.copyOf} at {@link #createStateSnapshot(CombatStateTypeRegistry)} time, which rejects
     * null values — throwing out of session end before the manager can unmap the session. The blast
     * radius (a leaked session that then floods the timeout scan with the same exception) is worth
     * defending against beyond the contract.
     *
     * @param type     The state type owning the value.
     * @param rawValue The raw stored value.
     * @param <T>      The state value type.
     * @return The resolved value, or {@code rawValue} if no resolver is declared, the resolver threw,
     *         or the resolver returned {@code null}.
     */
    @NotNull
    private <T> T resolveDefensively(@NotNull CombatStateType<T> type, @NotNull T rawValue) {
        Optional<CombatStateResolver<T>> resolver = type.getResolver();
        if (resolver.isEmpty()) {
            return rawValue;
        }
        try {
            T resolvedValue = resolver.get().resolve(this, rawValue);
            // resolve is @NotNull, but a misbehaving third party can still return null; see the
            // method Javadoc for why that is defended rather than trusted.
            //noinspection ConstantConditions
            if (resolvedValue == null) {
                warnFaultyResolve(type, "returned null", null);
                return rawValue;
            }
            return resolvedValue;
        } catch (Exception e) {
            warnFaultyResolve(type, "threw", e);
            return rawValue;
        }
    }

    /**
     * Logs a resolver misbehaviour once per state type via {@link #resolverWarnings}.
     *
     * @param type    The state type whose resolver misbehaved.
     * @param problem A short description of what the resolver did, e.g. {@code "threw"}.
     * @param cause   The exception the resolver threw, or {@code null} if it returned null instead.
     */
    private void warnFaultyResolve(@NotNull CombatStateType<?> type, @NotNull String problem,
                                   @Nullable Throwable cause) {
        resolverWarnings.warnOnce(type.getKey(), "Combat state type " + type.getKey() + " resolver "
                + problem + " while resolving state for session owner " + entityUUID + "; falling back "
                + "to the raw stored value. Further occurrences for this state type are not logged for "
                + "this session.", cause);
    }

    /**
     * Gets the raw stored value of a combat state, bypassing any resolver. Returns the
     * type's default value if no value has been stored.
     *
     * @param type The state type to query.
     * @param <T>  The state value type.
     * @return The raw stored value, or the default value if absent.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getRawState(@NotNull CombatStateType<T> type) {
        Object value = stateStore.get(type.getKey());
        return value != null ? (T) value : type.getDefaultValue();
    }

    /**
     * Sets the raw value of a combat state. Fires {@link CombatStateChangeEvent} — if the
     * event is cancelled, the value is not changed. If the event's new value is modified by
     * a listener, the modified value is stored instead.
     * <p>
     * Resolvers are not involved in writes — the raw value is stored directly. Reads via
     * {@link #getState(CombatStateType)} apply the resolver afterward.
     * <p>
     * Type safety of a listener-substituted value is enforced by
     * {@link CombatStateChangeEvent#setNewValue(Object)}, which rejects a value that does not match
     * the state type's class token at the point the offending listener calls it — so Bukkit's event
     * bus attributes the failure to that listener rather than to this caller. This method re-checks
     * as a backstop and, if a wrong-typed value somehow reaches it, logs and stores the caller's
     * original {@code value} (which is type-safe by generics) rather than corrupting the store or
     * throwing into an unrelated caller's stack.
     *
     * @param type  The state type to write.
     * @param value The new raw value.
     * @param <T>   The state value type.
     */
    public <T> void setState(@NotNull CombatStateType<T> type, @NotNull T value) {
        T oldValue = getRawState(type);

        CombatStateChangeEvent event = new CombatStateChangeEvent(this, type, oldValue, value);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        Object newValue = event.getNewValue();
        if (newValue != value && !CombatStateChangeEvent.isAssignableToStateType(type, newValue)) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "Combat state type " + type.getKey()
                    + " expects a " + type.getType().getName() + " but a CombatStateChangeEvent listener "
                    + "substituted " + describeForLog(newValue) + "; ignoring the substitution and storing "
                    + "the original value for session owner " + entityUUID);
            newValue = value;
        }
        stateStore.put(type.getKey(), newValue);
    }

    /**
     * Renders a value for a log message without dereferencing a possible {@code null}.
     *
     * @param value The value to describe.
     * @return {@code "null"} or the value's class name.
     */
    @NotNull
    private static String describeForLog(@Nullable Object value) {
        return value == null ? "null" : "a " + value.getClass().getName();
    }

    /**
     * Atomically reads the current raw value, applies a modifier function, and writes the result.
     * Fires {@link CombatStateChangeEvent} with the old and new values — cancellation or value
     * modification by listeners is handled identically to {@link #setState(CombatStateType, Object)}.
     * <p>
     * The modifier belongs to the caller, so an exception it throws propagates to the caller
     * unchanged rather than being swallowed here.
     *
     * @param type     The state type to modify.
     * @param modifier The function to apply to the current raw value.
     * @param <T>      The state value type.
     */
    public <T> void modifyState(@NotNull CombatStateType<T> type, @NotNull UnaryOperator<T> modifier) {
        T oldValue = getRawState(type);
        T newValue = modifier.apply(oldValue);
        setState(type, newValue);
    }

    /**
     * Gets the per-session statistics container. Stat-tracking listeners use this to
     * increment damage, healing, hit, and kill counts during combat.
     *
     * @return The mutable {@link CombatSessionStatistics} for this session.
     */
    @NotNull
    public CombatSessionStatistics getStatistics() {
        return statistics;
    }

    /**
     * Creates an immutable snapshot of the per-session statistics. Computes and writes
     * {@code SESSION_DURATION} into the statistics container before snapshotting, so the
     * snapshot includes duration alongside all other stats uniformly.
     *
     * @return A new {@link CombatSessionStatisticsSnapshot}.
     */
    @NotNull
    public CombatSessionStatisticsSnapshot createStatisticsSnapshot() {
        double durationSeconds = getDurationMillis() / 1000.0;
        statistics.setDouble(CombatSessionStatisticKey.SESSION_DURATION, durationSeconds);
        return statistics.snapshot();
    }

    /**
     * Creates an immutable snapshot of all combat state data. Captures both raw and resolved
     * values for every key in the state store. Resolved values are computed at snapshot time
     * while the session still exists and resolvers can run.
     * <p>
     * A key whose {@link CombatStateType} is not registered in {@code stateTypeRegistry} has no
     * resolver to apply, so its raw value is used as its resolved value.
     *
     * @param stateTypeRegistry The registry used to look up state types for resolver resolution.
     * @return A new {@link CombatStateSnapshot}.
     */
    @NotNull
    public CombatStateSnapshot createStateSnapshot(@NotNull CombatStateTypeRegistry stateTypeRegistry) {
        Map<NamespacedKey, Object> rawValues = Map.copyOf(stateStore);
        Map<NamespacedKey, Object> resolvedValues = new HashMap<>();
        for (Map.Entry<NamespacedKey, Object> entry : stateStore.entrySet()) {
            Optional<CombatStateType<?>> typeOpt = stateTypeRegistry.get(entry.getKey());
            if (typeOpt.isPresent()) {
                resolvedValues.put(entry.getKey(), resolveForSnapshot(typeOpt.get(), entry.getValue()));
            } else {
                resolvedValues.put(entry.getKey(), entry.getValue());
            }
        }
        return new CombatStateSnapshot(rawValues, Map.copyOf(resolvedValues));
    }

    /**
     * Resolves a single state entry for {@link #createStateSnapshot(CombatStateTypeRegistry)},
     * applying the type's resolver (if any) to the raw stored value. A resolver that throws is
     * logged and the raw value used, so one faulty state type cannot abort the whole snapshot —
     * and with it the session-end flow that builds it.
     *
     * @param type     The state type owning the value.
     * @param rawValue The raw stored value.
     * @param <T>      The state value type.
     * @return The resolved value, or the raw value if no resolver is declared or the resolver threw.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T> Object resolveForSnapshot(@NotNull CombatStateType<T> type, @NotNull Object rawValue) {
        return resolveDefensively(type, (T) rawValue);
    }

    /**
     * Sets a raw value in the state store without firing events. Used by the manager during
     * persistent state re-attachment on session start.
     *
     * @param key   The state type key.
     * @param value The value to store.
     */
    void setRawState(@NotNull NamespacedKey key, @NotNull Object value) {
        stateStore.put(key, value);
    }

    /**
     * Gets the raw state store map. Used by the manager for persistent state save on session end.
     *
     * @return The mutable state store map.
     */
    @NotNull
    Map<NamespacedKey, Object> getRawStateMap() {
        return stateStore;
    }

    /**
     * Clears all session-scoped state. Called by the manager during session end cleanup.
     * Persistent state is saved before this call.
     */
    void clearSessionState() {
        stateStore.clear();
    }
}
