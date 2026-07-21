package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
import us.eunoians.mcrpg.combat.condition.CombatConditionTask;
import us.eunoians.mcrpg.combat.state.CombatStateSnapshot;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;
import us.eunoians.mcrpg.combat.task.CombatSessionTimeoutTask;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.database.table.CombatPersistentStateDAO;
import us.eunoians.mcrpg.event.combat.CombatParticipantAddEvent;
import us.eunoians.mcrpg.event.combat.CombatParticipantRemoveEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionStartEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Central manager for the combat tracker system. Owns the session map, condition task lifecycle,
 * and the public API for combat interactions. Registered via {@link McRPGManagerKey#COMBAT_TRACKER}.
 * <p>
 * The {@link CombatConditionRegistry} is an independent registry accessed via
 * {@link McRPGRegistryKey#COMBAT_CONDITION}. The manager reads from it and coordinates task
 * lifecycle, but does not own it.
 * <p>
 * Only {@link Player} entities receive automatic combat sessions. Mobs are tracked as participants
 * in player sessions but do not get their own. The session map, participant model, and event
 * signatures are entity-type-agnostic — the player-only restriction is enforced solely in
 * {@link #handleCombatInteraction(UUID, UUID, CustomEntityWrapper, CustomEntityWrapper)} and
 * {@link #reportCombatActivity(UUID, UUID)}.
 * <p>
 * <b>Threading:</b> this manager is not thread-safe. Its session map, per-session collections, and
 * timestamp fields are plain (non-concurrent) structures and it dispatches Bukkit events, so every
 * public mutating method must be invoked on the main server thread. The mutating entry points guard
 * this with a {@link org.bukkit.Bukkit#isPrimaryThread()} check and throw
 * {@link IllegalStateException} if called from another thread. The sole exception is
 * {@code pendingPersistentWrites}, which is completed from the database executor thread and is
 * therefore concurrent.
 */
public class CombatTrackerManager extends Manager<McRPG> {

    /**
     * How long {@link #shutdown()} waits for outstanding persistent-state writes before abandoning
     * them. Bounded so a wedged database executor cannot hang server shutdown indefinitely.
     */
    private static final long PENDING_WRITE_DRAIN_TIMEOUT_SECONDS = 10L;

    private final Map<UUID, CombatSession> activeSessions;
    private final Map<NamespacedKey, CombatConditionTask> conditionTasks;
    @Nullable
    private CombatSessionTimeoutTask timeoutTask;
    private final Map<UUID, Map<String, String>> persistentStateCache;
    private final Set<NamespacedKey> registeredDoubleStatKeys;
    private final Set<NamespacedKey> registeredLongStatKeys;
    /**
     * The most recently submitted persistent-state write per entity. Each new write for an entity is
     * chained onto its predecessor so writes for the same row execute in submission order, and the
     * map lets {@link #shutdown()} drain outstanding writes before the database closes and lets
     * {@link #clearPersistentStateCacheWhenWritesSettle(UUID)} defer the cache clear until the
     * logout write has landed.
     * <p>
     * Entries are added on the main thread and removed by the write's own completion callback,
     * which runs on the database executor thread — hence the concurrent map.
     */
    private final Map<UUID, CompletableFuture<Void>> pendingPersistentWrites;
    /**
     * Keys encountered in a session's state store that resolve to no registered
     * {@link CombatStateType}. Tracked purely so {@link #collectPersistentEntries(CombatSession)}
     * warns once per key instead of on every session end.
     */
    private final Set<NamespacedKey> warnedUnregisteredStateKeys;
    /**
     * Set by {@link #shutdown()} and exists solely to let {@link #endSession(UUID, CombatSessionEndReason)}
     * distinguish an in-progress shutdown from every other end-session path, so it can skip a redundant
     * persistent-state save (already flushed synchronously by {@link #saveAllPersistentStateSync()}).
     * All access is on the main thread (enforced by {@link #requireMainThread()}), so no synchronization
     * is needed. A new {@link CombatTrackerManager} instance is created on every plugin enable, so the
     * field always starts {@code false} for a fresh run.
     */
    private boolean shuttingDown;

    /**
     * Constructs a new {@link CombatTrackerManager}.
     *
     * @param mcRPG The {@link McRPG} plugin instance.
     */
    public CombatTrackerManager(@NotNull McRPG mcRPG) {
        super(mcRPG);
        this.activeSessions = new HashMap<>();
        this.conditionTasks = new HashMap<>();
        this.persistentStateCache = new HashMap<>();
        this.registeredDoubleStatKeys = new HashSet<>();
        this.registeredLongStatKeys = new HashSet<>();
        this.pendingPersistentWrites = new ConcurrentHashMap<>();
        this.warnedUnregisteredStateKeys = new HashSet<>();
        this.shuttingDown = false;
    }

    /**
     * Gets the active combat session for the given entity, if one exists.
     *
     * @param entityUUID The UUID of the entity.
     * @return An {@link Optional} containing the session, or empty if the entity is not in combat.
     */
    @NotNull
    public Optional<CombatSession> getSession(@NotNull UUID entityUUID) {
        return Optional.ofNullable(activeSessions.get(entityUUID));
    }

    /**
     * Checks whether the given entity has an active combat session.
     *
     * @param entityUUID The UUID of the entity.
     * @return {@code true} if the entity has an active session.
     */
    public boolean hasActiveSession(@NotNull UUID entityUUID) {
        return activeSessions.containsKey(entityUUID);
    }

    /**
     * Handles a combat interaction between two entities. Creates or updates sessions for the
     * source and target entities (if they are players) and manages participant rosters. This is the
     * primary entry point called by the damage listener on damage events.
     * <p>
     * Only creates sessions for players. Mobs are tracked as participants in player sessions
     * but do not get their own sessions.
     * <p>
     * For each side (source and target), the following logic is applied:
     * <ol>
     *   <li>If the entity is not a player, skip session management for that side.</li>
     *   <li>If no session exists, fire {@link CombatSessionStartEvent} (cancellable). If not
     *       cancelled, create a new {@link CombatSession}, add the other entity as a participant,
     *       and record the interaction.</li>
     *   <li>If a session exists but the other entity is not a participant, fire
     *       {@link CombatParticipantAddEvent} (cancellable). If not cancelled, add the participant.
     *       If the addition causes a mob FIFO eviction, fire {@link CombatParticipantRemoveEvent}
     *       with {@link ParticipantRemovalReason#EVICTION}.</li>
     *   <li>If the other entity is already a participant, record the interaction to refresh
     *       timestamps.</li>
     * </ol>
     *
     * @param sourceUUID          The UUID of the entity dealing damage.
     * @param targetUUID          The UUID of the entity taking damage.
     * @param sourceEntityWrapper The {@link CustomEntityWrapper} of the source entity.
     * @param targetEntityWrapper The {@link CustomEntityWrapper} of the target entity.
     */
    public void handleCombatInteraction(@NotNull UUID sourceUUID, @NotNull UUID targetUUID,
                                        @NotNull CustomEntityWrapper sourceEntityWrapper,
                                        @NotNull CustomEntityWrapper targetEntityWrapper) {
        requireMainThread();
        handleInteraction(sourceUUID, targetUUID, () -> sourceEntityWrapper, () -> targetEntityWrapper);
    }

    /**
     * Entity-based variant of
     * {@link #handleCombatInteraction(UUID, UUID, CustomEntityWrapper, CustomEntityWrapper)} for hot
     * callers such as the damage listener. The {@link CustomEntityWrapper}s are built lazily and only
     * when a session or participant is actually created, so the dominant steady-state case (both
     * entities already tracked) constructs no wrappers at all. Prefer this overload on the damage
     * path; the wrapper overload exists for callers that already hold wrappers.
     *
     * @param sourceUUID   The UUID of the entity dealing damage.
     * @param targetUUID   The UUID of the entity taking damage.
     * @param sourceEntity The source entity.
     * @param targetEntity The target entity.
     */
    public void handleCombatInteraction(@NotNull UUID sourceUUID, @NotNull UUID targetUUID,
                                        @NotNull Entity sourceEntity, @NotNull Entity targetEntity) {
        requireMainThread();
        handleInteraction(sourceUUID, targetUUID,
                () -> new CustomEntityWrapper(sourceEntity), () -> new CustomEntityWrapper(targetEntity));
    }

    /**
     * Shared implementation of combat-interaction handling. Dispatches to
     * {@link #handleSideInteraction(UUID, UUID, ParticipantType, Supplier)} for each player side. The
     * wrapper suppliers are resolved lazily by the side handler, so they are only invoked when a
     * session or participant is created — never on the steady-state re-hit path.
     *
     * @param sourceUUID            The UUID of the source entity.
     * @param targetUUID            The UUID of the target entity.
     * @param sourceWrapperSupplier Supplier of the source entity's {@link CustomEntityWrapper}.
     * @param targetWrapperSupplier Supplier of the target entity's {@link CustomEntityWrapper}.
     */
    private void handleInteraction(@NotNull UUID sourceUUID, @NotNull UUID targetUUID,
                                   @NotNull Supplier<CustomEntityWrapper> sourceWrapperSupplier,
                                   @NotNull Supplier<CustomEntityWrapper> targetWrapperSupplier) {
        ParticipantType sourceType = resolveParticipantType(sourceUUID);
        ParticipantType targetType = resolveParticipantType(targetUUID);

        // Handle the source side (if source is a player)
        if (sourceType == ParticipantType.PLAYER) {
            handleSideInteraction(sourceUUID, targetUUID, targetType, targetWrapperSupplier);
        }

        // Handle the target side (if target is a player)
        if (targetType == ParticipantType.PLAYER) {
            handleSideInteraction(targetUUID, sourceUUID, sourceType, sourceWrapperSupplier);
        }
    }

    /**
     * Reports combat activity between two entities without a corresponding Bukkit damage event.
     * Used by DOT effects, AoE splash, and third-party event-based triggers that operate
     * outside of {@link org.bukkit.event.entity.EntityDamageByEntityEvent}.
     * <p>
     * Follows the same session creation and participant management logic as
     * {@link #handleCombatInteraction(UUID, UUID, CustomEntityWrapper, CustomEntityWrapper)},
     * but resolves {@link CustomEntityWrapper}s from the live entity objects. If either entity
     * is not currently loaded, only the loaded entity's session is updated.
     *
     * @param sourceUUID The UUID of the source entity.
     * @param targetUUID The UUID of the target entity.
     */
    public void reportCombatActivity(@NotNull UUID sourceUUID, @NotNull UUID targetUUID) {
        requireMainThread();
        Entity sourceEntity = Bukkit.getEntity(sourceUUID);
        Entity targetEntity = Bukkit.getEntity(targetUUID);

        if (sourceEntity != null && targetEntity != null) {
            handleCombatInteraction(sourceUUID, targetUUID, sourceEntity, targetEntity);
            return;
        }

        // If only source is resolvable and source is a player with a session, record interaction
        if (sourceEntity != null && sourceEntity instanceof Player) {
            CombatSession sourceSession = activeSessions.get(sourceUUID);
            if (sourceSession != null) {
                sourceSession.recordParticipantInteraction(targetUUID);
            }
        }

        // If only target is resolvable and target is a player with a session, record interaction
        if (targetEntity != null && targetEntity instanceof Player) {
            CombatSession targetSession = activeSessions.get(targetUUID);
            if (targetSession != null) {
                targetSession.recordParticipantInteraction(sourceUUID);
            }
        }
    }

    /**
     * Reports that a combat condition is actively holding an entity in combat, without
     * specifying a specific participant. Creates a session if one does not exist, or
     * refreshes the session's activity timestamp if one does.
     * <p>
     * Used by {@link CombatConditionTask} when a condition returns {@code true} for an entity
     * but provides no implied participants (proximity-based conditions).
     * <p>
     * When creating a new session, {@link CombatSessionStartEvent} is fired with the entity's own
     * UUID as both the entity and trigger participant (since there is no specific opponent) and with
     * {@code conditionKey} as the triggering condition, so listeners can distinguish condition-driven
     * starts from damage-driven ones via {@link CombatSessionStartEvent#getTriggeringConditionKey()}.
     *
     * @param entityUUID   The UUID of the entity held in combat.
     * @param conditionKey The {@link NamespacedKey} of the condition holding the entity.
     */
    public void reportConditionActivity(@NotNull UUID entityUUID, @NotNull NamespacedKey conditionKey) {
        requireMainThread();
        CombatSession existingSession = activeSessions.get(entityUUID);
        if (existingSession != null) {
            existingSession.recordActivity();
            return;
        }

        // No session exists — attempt to create one
        Player player = Bukkit.getPlayer(entityUUID);
        if (player == null) {
            return;
        }

        CustomEntityWrapper entityWrapper = new CustomEntityWrapper(player);
        CombatSessionStartEvent startEvent = new CombatSessionStartEvent(
                entityUUID, entityUUID, ParticipantType.PLAYER, entityWrapper, conditionKey);
        Bukkit.getPluginManager().callEvent(startEvent);

        if (startEvent.isCancelled()) {
            return;
        }

        CombatSession session = new CombatSession(entityUUID, getMaxMobParticipants(), getSessionTimeoutMillis());
        activeSessions.put(entityUUID, session);
        initializeNewSession(session);
    }

    /**
     * Ends the combat session for the given entity, if one exists. Fires
     * {@link CombatSessionEndEvent} with the session's final state, per-session statistics
     * snapshot, and combat state snapshot. Saves any {@code PERSISTENT}-scoped state
     * asynchronously (skipped during shutdown — {@link #saveAllPersistentStateSync()} already
     * flushed it synchronously) and clears session-scoped state before firing the event.
     *
     * @param entityUUID The UUID of the entity whose session to end.
     * @param reason     The {@link CombatSessionEndReason} for ending the session.
     */
    public void endSession(@NotNull UUID entityUUID, @NotNull CombatSessionEndReason reason) {
        requireMainThread();
        CombatSession session = activeSessions.get(entityUUID);
        if (session == null) {
            return;
        }

        CombatStateTypeRegistry stateTypeRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        CombatSessionStatisticsSnapshot statisticsSnapshot = session.createStatisticsSnapshot();
        CombatStateSnapshot stateSnapshot = session.createStateSnapshot(stateTypeRegistry);

        if (!shuttingDown) {
            savePersistentStateAsync(session);
        }

        activeSessions.remove(entityUUID);

        CombatSessionEndEvent endEvent = new CombatSessionEndEvent(
                entityUUID,
                reason,
                session.getParticipants(),
                session.getCombatType(),
                session.getDurationMillis(),
                statisticsSnapshot,
                stateSnapshot);

        session.clearSessionState();

        Bukkit.getPluginManager().callEvent(endEvent);
    }

    /**
     * Removes a participant from all active sessions that reference it. Used when an entity
     * dies, despawns, or logs out. For each session that contained the participant, fires
     * {@link CombatParticipantRemoveEvent}. If removing the participant empties a session's
     * roster, the session is ended with {@link CombatSessionEndReason#ALL_PARTICIPANTS_GONE}.
     *
     * @param participantUUID The UUID of the participant to remove.
     * @param reason          The {@link ParticipantRemovalReason} for removal.
     */
    public void removeParticipantFromAllSessions(@NotNull UUID participantUUID,
                                                  @NotNull ParticipantRemovalReason reason) {
        requireMainThread();
        // Snapshot to avoid ConcurrentModificationException (removeParticipant may end sessions, mutating activeSessions)
        List<Map.Entry<UUID, CombatSession>> snapshot = new ArrayList<>(activeSessions.entrySet());

        for (Map.Entry<UUID, CombatSession> entry : snapshot) {
            CombatSession session = entry.getValue();
            if (session.hasParticipant(participantUUID)) {
                removeParticipant(session, participantUUID, reason);
            }
        }
    }

    /**
     * Removes a specific participant from a specific entity's session, the supported entry point for
     * third-party plugins (arena, duel, AFK, etc.) that need to drop a single participant. Fires
     * {@link CombatParticipantRemoveEvent} and ends the session with
     * {@link CombatSessionEndReason#ALL_PARTICIPANTS_GONE} if its roster empties (unless a condition
     * holds it open). Must be called from the main server thread.
     *
     * @param ownerUUID       The UUID of the session owner to remove the participant from.
     * @param participantUUID The UUID of the participant to remove.
     * @param reason          The {@link ParticipantRemovalReason} for the removal.
     * @return An {@link Optional} containing the removed {@link CombatParticipant}, or empty if the
     *         owner has no session or the session did not contain the participant.
     */
    @NotNull
    public Optional<CombatParticipant> removeParticipantFromSession(@NotNull UUID ownerUUID,
                                                                    @NotNull UUID participantUUID,
                                                                    @NotNull ParticipantRemovalReason reason) {
        requireMainThread();
        CombatSession session = activeSessions.get(ownerUUID);
        if (session == null || !session.hasParticipant(participantUUID)) {
            return Optional.empty();
        }
        return removeParticipant(session, participantUUID, reason);
    }

    /**
     * Removes a participant from a single session, firing {@link CombatParticipantRemoveEvent} with
     * the session's combat-type transition and ending the session with
     * {@link CombatSessionEndReason#ALL_PARTICIPANTS_GONE} if its roster empties as a result.
     * <p>
     * This is the sole owner of participant-removal lifecycle. Every removal path — the death /
     * quit / despawn sweep, the per-participant timeout scan, and
     * {@link #removeParticipantFromSession(UUID, UUID, ParticipantRemovalReason)} — routes through it
     * so event firing and empty-session handling stay consistent in one place.
     *
     * @param session         The session to remove the participant from.
     * @param participantUUID The UUID of the participant to remove.
     * @param reason          The {@link ParticipantRemovalReason} for the removal.
     * @return An {@link Optional} containing the removed {@link CombatParticipant}, or empty if the
     *         session did not contain the participant.
     */
    @NotNull
    private Optional<CombatParticipant> removeParticipant(@NotNull CombatSession session,
                                                          @NotNull UUID participantUUID,
                                                          @NotNull ParticipantRemovalReason reason) {
        CombatType previousType = session.getCombatType();
        Optional<CombatParticipant> removedParticipant = session.removeParticipant(participantUUID);
        if (removedParticipant.isEmpty()) {
            return Optional.empty();
        }
        CombatType newType = session.getCombatType();

        CombatParticipantRemoveEvent removeEvent = new CombatParticipantRemoveEvent(
                session, removedParticipant.get(), reason, previousType, newType);
        Bukkit.getPluginManager().callEvent(removeEvent);

        // An emptied roster ends the session — unless a registered condition still holds the owner
        // in combat (e.g. a proximity/region condition), in which case the session persists.
        if (session.isEmpty() && !isHeldOpenByCondition(session)) {
            endSession(session.getEntityUUID(), CombatSessionEndReason.ALL_PARTICIPANTS_GONE);
        }
        return removedParticipant;
    }

    /**
     * Scans all active sessions for per-participant and session-level timeouts on the main thread.
     * For each session it removes participants whose per-participant inactivity timer has expired
     * (firing {@link CombatParticipantRemoveEvent} with {@link ParticipantRemovalReason#TIMEOUT} and
     * ending the session if its roster empties), then ends the session with
     * {@link CombatSessionEndReason#TIMEOUT} if the session's own inactivity timer has expired —
     * unless a registered {@link CombatCondition} holds it open.
     * <p>
     * Invoked by {@link CombatSessionTimeoutTask} at the configured scan cadence. The active session
     * keys are snapshotted before iterating because removal and session-ending mutate the session map.
     */
    public void scanSessionsForTimeout() {
        List<UUID> sessionKeys = new ArrayList<>(activeSessions.keySet());

        for (UUID ownerUUID : sessionKeys) {
            CombatSession session = activeSessions.get(ownerUUID);
            if (session == null) {
                continue;
            }

            for (CombatParticipant participant : session.getTimedOutParticipants()) {
                removeParticipant(session, participant.getUUID(), ParticipantRemovalReason.TIMEOUT);
            }

            // removeParticipant ends and unmaps the session if its roster emptied.
            if (!activeSessions.containsKey(ownerUUID)) {
                continue;
            }

            if (session.isTimedOut() && !isHeldOpenByCondition(session)) {
                endSession(ownerUUID, CombatSessionEndReason.TIMEOUT);
            }
        }
    }

    /**
     * Checks whether any registered {@link CombatCondition} currently holds the given session's
     * owner in combat. When a condition matches, the session's activity timer is refreshed via
     * {@link CombatSession#recordActivity()} so it is not ended on this scan pass.
     * <p>
     * Each condition is evaluated defensively: a third-party condition that throws is logged and
     * skipped so one faulty condition cannot abort the whole timeout scan.
     *
     * @param session The session whose owner to evaluate.
     * @return {@code true} if a condition holds the session open (and its timer was refreshed).
     */
    private boolean isHeldOpenByCondition(@NotNull CombatSession session) {
        Player player = Bukkit.getPlayer(session.getEntityUUID());
        if (player == null) {
            return false;
        }
        CombatConditionRegistry conditionRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);
        for (CombatCondition condition : conditionRegistry.getAll()) {
            try {
                if (condition.isInCombat(player)) {
                    session.recordActivity();
                    return true;
                }
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "Combat condition " + condition.getKey()
                        + " threw while evaluating combat hold-open for player " + player.getUniqueId()
                        + "; skipping this condition", e);
            }
        }
        return false;
    }

    /**
     * Starts periodic tasks for all conditions currently in the {@link CombatConditionRegistry}.
     * Called once during bootstrap after content expansion processing has populated the registry.
     */
    public void startConditionTasks() {
        CombatConditionRegistry conditionRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);
        for (CombatCondition condition : conditionRegistry.getAll()) {
            startConditionTask(condition);
        }
    }

    /**
     * Starts the periodic evaluation task for a single {@link CombatCondition}. The condition
     * must already be registered in the {@link CombatConditionRegistry}.
     * <p>
     * For standalone plugins registering conditions at runtime, call
     * {@code conditionRegistry.register(condition)} first, then this method.
     * <p>
     * If a task is already running for this condition's key, it is cancelled before the new one is
     * started, so calling this twice for the same key cannot leak an orphaned, uncancellable task.
     *
     * @param condition The condition whose task to start.
     */
    public void startConditionTask(@NotNull CombatCondition condition) {
        stopConditionTask(condition.getKey());
        CombatConditionTask conditionTask = condition.createTask(plugin(), this);
        conditionTask.runTask();
        conditionTasks.put(condition.getKey(), conditionTask);
    }

    /**
     * Stops and removes the periodic evaluation task for a {@link CombatCondition}. Does not
     * unregister the condition from the {@link CombatConditionRegistry} — the caller is
     * responsible for calling {@code conditionRegistry.unregister(key)} separately.
     *
     * @param conditionKey The key of the condition whose task to stop.
     */
    public void stopConditionTask(@NotNull NamespacedKey conditionKey) {
        CombatConditionTask removedTask = conditionTasks.remove(conditionKey);
        if (removedTask != null) {
            removedTask.cancelTask();
        }
    }

    /**
     * Starts the global session timeout scan task. Called during plugin enable.
     * If a timeout task is already running, it is cancelled before starting the new one.
     */
    public void startTimeoutTask() {
        stopTimeoutTask();
        double scanIntervalSeconds = getScanIntervalSeconds();
        this.timeoutTask = new CombatSessionTimeoutTask(plugin(), this, scanIntervalSeconds);
        this.timeoutTask.runTask();
    }

    /**
     * Stops the global session timeout scan task. Called during plugin disable.
     */
    public void stopTimeoutTask() {
        if (timeoutTask != null) {
            timeoutTask.cancelTask();
            timeoutTask = null;
        }
    }

    /**
     * Shuts down the combat tracker. Drains outstanding async persistent-state writes, synchronously
     * flushes all dirty persistent state while sessions are still active, ends all active sessions
     * with {@link CombatSessionEndReason#PLUGIN}, cancels all condition tasks, and stops the timeout
     * task.
     * <p>
     * The ordering is what makes "no persistent state is lost" true, so do not reorder these steps:
     * <ol>
     *   <li>{@link #awaitPendingPersistentWrites()} drains writes submitted before shutdown began
     *       (a player who quit moments before {@code /stop}). {@code McRPGBootstrap.stop()} closes
     *       the database shortly after this method returns, and a write still in flight then would
     *       hit a closed connection pool and be lost with only a logged warning.</li>
     *   <li>{@link #saveAllPersistentStateSync()} runs while all sessions are still in the active
     *       session map, so it can see their state.</li>
     *   <li>Sessions are ended with {@link #shuttingDown} {@code true}, which suppresses
     *       {@link #endSession(UUID, CombatSessionEndReason)}'s own async save — otherwise every
     *       session would re-submit a duplicate write for state just flushed synchronously, racing
     *       the database executor's own shutdown.</li>
     * </ol>
     */
    public void shutdown() {
        awaitPendingPersistentWrites();
        shuttingDown = true;
        saveAllPersistentStateSync();

        // End all active sessions with reason PLUGIN
        List<UUID> sessionOwners = new ArrayList<>(activeSessions.keySet());
        for (UUID ownerUUID : sessionOwners) {
            endSession(ownerUUID, CombatSessionEndReason.PLUGIN);
        }

        // Cancel all condition tasks
        for (CombatConditionTask conditionTask : conditionTasks.values()) {
            conditionTask.cancelTask();
        }
        conditionTasks.clear();

        // Stop timeout task
        stopTimeoutTask();
    }

    /**
     * Blocks until every outstanding persistent-state write submitted by
     * {@link #savePersistentStateAsync(CombatSession)} has completed, or until
     * {@link #PENDING_WRITE_DRAIN_TIMEOUT_SECONDS} elapses. Called at the top of
     * {@link #shutdown()} so no write is still in flight when the database closes its connection
     * pool moments later.
     * <p>
     * The timeout is bounded so a wedged database executor cannot hang server shutdown; on timeout
     * the remaining writes are abandoned with a warning rather than waited on indefinitely. Blocking
     * the main thread here is safe — these writes only need the database executor to make progress.
     */
    private void awaitPendingPersistentWrites() {
        if (pendingPersistentWrites.isEmpty()) {
            return;
        }
        CompletableFuture<?>[] outstandingWrites = pendingPersistentWrites.values().toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(outstandingWrites).get(PENDING_WRITE_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            plugin().getLogger().log(Level.WARNING, "Timed out after " + PENDING_WRITE_DRAIN_TIMEOUT_SECONDS
                    + " seconds waiting for " + outstandingWrites.length
                    + " persistent combat state write(s) to finish; abandoning them so shutdown can continue", e);
        } catch (ExecutionException e) {
            plugin().getLogger().log(Level.WARNING,
                    "A persistent combat state write failed while draining writes during shutdown", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin().getLogger().log(Level.WARNING,
                    "Interrupted while draining persistent combat state writes during shutdown", e);
        }
    }

    /**
     * Gets an immutable snapshot of all active sessions, keyed by session-owner UUID. The returned
     * map is a copy taken at call time, so it is safe to iterate while mutating combat state (for
     * example calling {@link #endSession(UUID, CombatSessionEndReason)} during the iteration). It
     * does not reflect sessions started or ended after the call.
     *
     * @return An immutable snapshot {@link Map} of entity UUID to {@link CombatSession}.
     */
    @NotNull
    public Map<UUID, CombatSession> getActiveSessions() {
        return Map.copyOf(activeSessions);
    }

    /**
     * Convenience wrapper over {@code registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE).register(stateType)}
     * for standalone plugins that would rather not reach for the registry directly. The manager does
     * not own {@link CombatStateTypeRegistry} — unlike combat conditions, state types need no
     * per-type task for the manager to coordinate, so this delegates and nothing more. Registering
     * on the registry directly is equally valid.
     * <p>
     * For plugins using the expansion system, registration happens via
     * {@link us.eunoians.mcrpg.expansion.content.CombatStateTypeContentPack} and the
     * {@code ContentHandlerType.COMBAT_STATE_TYPE} processor — calling this method directly is not
     * needed.
     *
     * @param stateType The state type to register.
     */
    public void registerStateType(@NotNull CombatStateType<?> stateType) {
        requireMainThread();
        CombatStateTypeRegistry registry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        registry.register(stateType);
    }

    /**
     * Registers a double-valued custom per-session statistic key so it appears with its default
     * value ({@code 0.0}) in every new session's statistics container.
     *
     * @param key The custom statistic key.
     */
    public void registerDoubleSessionStatisticKey(@NotNull NamespacedKey key) {
        requireMainThread();
        registeredDoubleStatKeys.add(key);
    }

    /**
     * Registers a long-valued custom per-session statistic key so it appears with its default
     * value ({@code 0}) in every new session's statistics container.
     *
     * @param key The custom statistic key.
     */
    public void registerLongSessionStatisticKey(@NotNull NamespacedKey key) {
        requireMainThread();
        registeredLongStatKeys.add(key);
    }

    /**
     * Reports an attributed healing interaction, incrementing {@code healing_dealt} on the healer's
     * active session. Does not create sessions or add participants.
     * <p>
     * McRPG heal abilities and third-party plugins call this to attribute healing — Bukkit's
     * {@link org.bukkit.event.entity.EntityRegainHealthEvent} carries no healer source, so
     * attribution requires explicit reporting.
     * <p>
     * This method deliberately does <b>not</b> write {@code healing_received}. Applying a heal
     * through the Bukkit API fires {@link org.bukkit.event.entity.EntityRegainHealthEvent}, which
     * {@link us.eunoians.mcrpg.listener.combat.OnCombatHealingStatListener} already credits to the
     * healed entity's session — writing it here as well would double-count every attributed heal.
     * A heal that bypasses that event (absorption hearts, direct health mutation) is therefore not
     * counted as received, which matches what the statistic measures.
     *
     * @param healerUUID The UUID of the entity that performed the healing.
     * @param targetUUID The UUID of the entity that was healed. Retained for a stable signature and
     *                   so listeners of future attribution events can identify the target; the
     *                   target's own {@code healing_received} is credited by the heal event listener.
     * @param amount     The amount of healing applied.
     */
    public void reportHealing(@NotNull UUID healerUUID, @NotNull UUID targetUUID, double amount) {
        requireMainThread();
        getSession(healerUUID).ifPresent(session ->
                session.getStatistics().incrementDouble(CombatSessionStatisticKey.HEALING_DEALT, amount));
    }

    /**
     * Caches pre-loaded persistent combat state for a player. Called by
     * {@link us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask} after the DB load completes, during
     * main-thread finalization — before the player is registered in
     * {@link us.eunoians.mcrpg.entity.McRPGPlayerManager} and before any combat session can start.
     * <p>
     * Values already in the cache win over the loaded ones. A cache entry can survive a logout when
     * the player rejoins before their logout write reaches the database (see
     * {@link #clearPersistentStateCacheWhenWritesSettle(UUID)}); in that window the retained
     * in-memory value is the fresher of the two and the DB read is stale by definition.
     *
     * @param entityUUID      The UUID of the entity.
     * @param persistentState The loaded persistent state map (keyed by stringified NamespacedKey).
     */
    public void cachePersistentState(@NotNull UUID entityUUID, @NotNull Map<String, String> persistentState) {
        Map<String, String> cacheEntry = persistentStateCache.computeIfAbsent(entityUUID, uuid -> new HashMap<>());
        for (Map.Entry<String, String> entry : persistentState.entrySet()) {
            cacheEntry.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Clears the persistent state cache for an entity immediately.
     *
     * @param entityUUID The UUID of the entity whose cache to clear.
     */
    public void clearPersistentStateCache(@NotNull UUID entityUUID) {
        persistentStateCache.remove(entityUUID);
    }

    /**
     * Clears an entity's persistent state cache once its outstanding database write has landed.
     * Called by {@link us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener} during combat
     * teardown on logout, after {@link #endSession(UUID, CombatSessionEndReason)} has submitted the
     * write.
     * <p>
     * Clearing eagerly would open a fast-relog hole: the write is queued on the database executor
     * while the reconnect path reads the same row on the main thread, so a player who rejoins
     * quickly could be seeded from a pre-logout row and then write that stale state back at their
     * next session end. Holding the cache entry until the write completes keeps
     * {@link #cachePersistentState(UUID, Map)} authoritative for that window. If the entity is back
     * online by the time the write finishes, the clear is skipped so a live session's cache is not
     * pulled out from under it.
     *
     * @param entityUUID The UUID of the entity whose cache to clear.
     */
    public void clearPersistentStateCacheWhenWritesSettle(@NotNull UUID entityUUID) {
        requireMainThread();
        CompletableFuture<Void> pendingWrite = pendingPersistentWrites.get(entityUUID);
        if (pendingWrite == null || pendingWrite.isDone()) {
            clearPersistentStateCache(entityUUID);
            return;
        }
        pendingWrite.whenComplete((ignored, throwable) -> {
            // The write completes on the database executor thread, so hop back to the main thread
            // before touching the cache — unless the plugin is already disabling, in which case the
            // scheduler rejects new tasks and this manager (cache included) is about to be discarded.
            if (!plugin().isEnabled()) {
                return;
            }
            Bukkit.getScheduler().runTask(plugin(), () -> {
                // Skip the clear if the entity is back online: their cache now belongs to a live
                // session that was seeded from it.
                if (Bukkit.getPlayer(entityUUID) == null) {
                    clearPersistentStateCache(entityUUID);
                }
            });
        });
    }

    /**
     * Saves dirty persistent state from a session to the database asynchronously. Called
     * during session end for sessions that contain persistent state. No-ops (and never touches
     * the database) when the session has no raw state matching a registered {@code PERSISTENT}
     * type.
     * <p>
     * Writes for the same entity are chained so they reach the database in submission order — two
     * session ends in quick succession (death then immediate re-engagement, rapid timeout cycling)
     * would otherwise be free to land in either order and let a stale write overwrite a fresher one.
     * The returned future also lets {@link #shutdown()} drain in-flight writes before the database
     * closes and lets {@link #clearPersistentStateCacheWhenWritesSettle(UUID)} time the cache clear.
     *
     * @param session The session whose persistent state to save.
     * @return A {@link CompletableFuture} completing when the write has been attempted; an
     *         already-completed future when the session has no persistent state to write.
     */
    @NotNull
    public CompletableFuture<Void> savePersistentStateAsync(@NotNull CombatSession session) {
        requireMainThread();
        Map<CombatStateType<?>, String> serializedEntries = collectPersistentEntries(session);
        if (serializedEntries.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        UUID entityUUID = session.getEntityUUID();
        updatePersistentStateCache(entityUUID, serializedEntries);

        Database database = plugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        CompletableFuture<Void> previousWrite = pendingPersistentWrites.getOrDefault(
                entityUUID, CompletableFuture.completedFuture(null));
        // exceptionally() first: a failed predecessor must not cancel this entity's next write.
        CompletableFuture<Void> write = previousWrite
                .exceptionally(throwable -> null)
                .thenRunAsync(() -> writePersistentEntries(database, entityUUID, serializedEntries),
                        database.getDatabaseExecutorService());
        pendingPersistentWrites.put(entityUUID, write);
        write.whenComplete((ignored, throwable) -> pendingPersistentWrites.remove(entityUUID, write));
        return write;
    }

    /**
     * Writes one entity's serialized persistent state to the database in a single batch. Runs on the
     * database executor thread.
     *
     * @param database          The database to write through.
     * @param entityUUID        The UUID of the entity whose state is being written.
     * @param serializedEntries The serialized state entries to write.
     */
    private void writePersistentEntries(@NotNull Database database, @NotNull UUID entityUUID,
                                        @NotNull Map<CombatStateType<?>, String> serializedEntries) {
        try (Connection connection = database.getConnection()) {
            BatchTransaction batch = new BatchTransaction(connection);
            for (Map.Entry<CombatStateType<?>, String> entry : serializedEntries.entrySet()) {
                batch.addAll(CombatPersistentStateDAO.savePersistentState(
                        connection, entityUUID, entry.getKey().getKey().toString(), entry.getValue()));
            }
            batch.executeTransaction();
        } catch (SQLException e) {
            plugin().getLogger().log(Level.WARNING,
                    "Failed to save persistent combat state for entity " + entityUUID, e);
        }
    }

    /**
     * Saves all dirty persistent state synchronously. Called during plugin shutdown to ensure
     * no persistent state is lost. No-ops (and never touches the database) when no active session
     * has any raw state matching a registered {@code PERSISTENT} type.
     */
    public void saveAllPersistentStateSync() {
        requireMainThread();
        Map<UUID, Map<CombatStateType<?>, String>> perEntityEntries = new HashMap<>();
        for (CombatSession session : activeSessions.values()) {
            Map<CombatStateType<?>, String> entries = collectPersistentEntries(session);
            if (!entries.isEmpty()) {
                perEntityEntries.put(session.getEntityUUID(), entries);
            }
        }
        if (perEntityEntries.isEmpty()) {
            return;
        }

        for (Map.Entry<UUID, Map<CombatStateType<?>, String>> entityEntry : perEntityEntries.entrySet()) {
            updatePersistentStateCache(entityEntry.getKey(), entityEntry.getValue());
        }

        Database database = plugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        try (Connection connection = database.getConnection()) {
            BatchTransaction batch = new BatchTransaction(connection);
            for (Map.Entry<UUID, Map<CombatStateType<?>, String>> entityEntry : perEntityEntries.entrySet()) {
                UUID entityUUID = entityEntry.getKey();
                for (Map.Entry<CombatStateType<?>, String> stateEntry : entityEntry.getValue().entrySet()) {
                    batch.addAll(CombatPersistentStateDAO.savePersistentState(
                            connection, entityUUID, stateEntry.getKey().getKey().toString(), stateEntry.getValue()));
                }
            }
            batch.executeTransaction();
        } catch (SQLException e) {
            plugin().getLogger().log(Level.SEVERE,
                    "Failed to synchronously save persistent combat state during shutdown", e);
        }
    }

    /**
     * Gets the configured session timeout in milliseconds. Reads the
     * {@link CombatConfigFile#SESSION_TIMEOUT_SECONDS} value from the combat configuration file and
     * converts it to milliseconds. The value is floored at 1 second: a zero or negative timeout would
     * make every session expire on the next scan pass, silently disabling combat tracking.
     *
     * @return The session timeout in milliseconds (always at least 1000).
     */
    public long getSessionTimeoutMillis() {
        YamlDocument config = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);
        double seconds = config.getDouble(CombatConfigFile.SESSION_TIMEOUT_SECONDS);
        if (seconds < 1.0) {
            plugin().getLogger().log(Level.WARNING, "Combat config {0} is {1}, which is invalid; using 1.0 instead.",
                    new Object[]{CombatConfigFile.SESSION_TIMEOUT_SECONDS.toString(), seconds});
            seconds = 1.0;
        }
        return (long) (seconds * 1000);
    }

    /**
     * Gets the configured maximum mob participant count from the combat configuration file. The
     * value is floored at 1: a configured value below 1 would make FIFO eviction attempt to evict
     * from an empty roster on the first mob add, so it is clamped and a warning is logged.
     *
     * @return The max mob participant count (always at least 1).
     */
    public int getMaxMobParticipants() {
        YamlDocument config = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);
        int configured = config.getInt(CombatConfigFile.MAX_MOB_PARTICIPANTS);
        if (configured < 1) {
            plugin().getLogger().log(Level.WARNING, "Combat config {0} is {1}, which is invalid; using 1 instead.",
                    new Object[]{CombatConfigFile.MAX_MOB_PARTICIPANTS.toString(), configured});
            return 1;
        }
        return configured;
    }

    /**
     * Gets the configured scan interval for the timeout task in seconds from the combat configuration
     * file. The value is floored at 0.25 seconds: a zero or negative interval would degrade the scan
     * task to running every tick (or break it entirely), hurting tick time.
     *
     * @return The scan interval in seconds (always at least 0.25).
     */
    private double getScanIntervalSeconds() {
        YamlDocument config = plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);
        double interval = config.getDouble(CombatConfigFile.TIMEOUT_SCAN_INTERVAL_SECONDS);
        if (interval < 0.25) {
            plugin().getLogger().log(Level.WARNING, "Combat config {0} is {1}, which is invalid; using 0.25 instead.",
                    new Object[]{CombatConfigFile.TIMEOUT_SCAN_INTERVAL_SECONDS.toString(), interval});
            interval = 0.25;
        }
        return interval;
    }

    /**
     * Initializes a newly-created session with cached persistent state (if any) and the default
     * values of any registered custom per-session statistic keys. Called by both session-creation
     * paths ({@link #createSessionForInteraction(UUID, UUID, ParticipantType, CustomEntityWrapper)}
     * and {@link #reportConditionActivity(UUID, NamespacedKey)}) immediately after the session is
     * put into {@link #activeSessions}.
     *
     * @param session The newly-created session to initialize.
     */
    private void initializeNewSession(@NotNull CombatSession session) {
        applyCachedPersistentState(session);
        initializeRegisteredStatKeys(session);
    }

    /**
     * Applies any cached persistent combat state for the session's owner, deserializing each
     * cached entry via its {@link CombatStateType}'s registered deserializer and writing it into
     * the session's raw state store without firing {@link us.eunoians.mcrpg.event.combat.CombatStateChangeEvent}.
     * Entries whose key does not resolve to a registered state type, or whose key string is not a
     * valid {@link NamespacedKey}, are skipped.
     *
     * @param session The session to apply cached persistent state to.
     */
    private void applyCachedPersistentState(@NotNull CombatSession session) {
        Map<String, String> cached = persistentStateCache.get(session.getEntityUUID());
        if (cached == null || cached.isEmpty()) {
            return;
        }

        CombatStateTypeRegistry stateTypeRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        for (Map.Entry<String, String> entry : cached.entrySet()) {
            NamespacedKey key = NamespacedKey.fromString(entry.getKey());
            if (key == null) {
                continue;
            }
            stateTypeRegistry.get(key).ifPresent(stateType -> applyDeserializedState(session, stateType, entry.getValue()));
        }
    }

    /**
     * Deserializes a single cached persistent state value and writes it into the session's raw
     * state store. No-ops if the state type declares no deserializer.
     * <p>
     * The deserializer belongs to whoever registered the state type. If it throws — a corrupt or
     * hand-edited database row, a format change between versions — the entry is skipped so the
     * session starts that type at its default, and the failure is logged. Letting it propagate
     * would break session creation for that entity on every subsequent hit until restart, since
     * the offending cached value is never cleared.
     *
     * @param session         The session to write the deserialized value into.
     * @param stateType       The state type owning the value.
     * @param serializedValue The serialized value loaded from the database.
     * @param <T>             The state value type.
     */
    private <T> void applyDeserializedState(@NotNull CombatSession session, @NotNull CombatStateType<T> stateType,
                                            @NotNull String serializedValue) {
        stateType.getDeserializer().ifPresent(deserializer -> {
            try {
                session.setRawState(stateType.getKey(), deserializer.apply(serializedValue));
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "Combat state type " + stateType.getKey()
                        + " deserializer threw for stored value \"" + serializedValue + "\" belonging to entity "
                        + session.getEntityUUID() + "; leaving the state at its default value", e);
            }
        });
    }

    /**
     * Seeds a new session's statistics container with the default value ({@code 0.0}/{@code 0}) of
     * every custom key registered via {@link #registerDoubleSessionStatisticKey(NamespacedKey)} or
     * {@link #registerLongSessionStatisticKey(NamespacedKey)}, so those keys appear in statistics
     * snapshots even if never incremented during the session.
     *
     * @param session The newly-created session to seed.
     */
    private void initializeRegisteredStatKeys(@NotNull CombatSession session) {
        for (NamespacedKey key : registeredDoubleStatKeys) {
            session.getStatistics().setDouble(key, 0.0);
        }
        for (NamespacedKey key : registeredLongStatKeys) {
            session.getStatistics().setLong(key, 0L);
        }
    }

    /**
     * Collects and serializes every entry in the session's raw state store whose key resolves to a
     * registered {@link us.eunoians.mcrpg.combat.state.CombatStateLifecycle#PERSISTENT} {@link CombatStateType}.
     * Entries whose type is unregistered, {@code SESSION}-scoped, or declares no serializer are skipped.
     * <p>
     * A key with no registered type is logged once (per key, per manager instance). Session-scoped
     * state works fine without registration, but persistence and end-of-session resolution do not —
     * so an unregistered key here is either intentional or a persistent type whose registration was
     * forgotten, and the latter would otherwise vanish silently at session end.
     *
     * @param session The session to collect persistent entries from.
     * @return A map of persistent state type to its serialized value; empty if the session has none.
     */
    @NotNull
    private Map<CombatStateType<?>, String> collectPersistentEntries(@NotNull CombatSession session) {
        CombatStateTypeRegistry stateTypeRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        Map<CombatStateType<?>, String> serialized = new HashMap<>();
        for (Map.Entry<NamespacedKey, Object> entry : session.getRawStateMap().entrySet()) {
            Optional<CombatStateType<?>> typeOpt = stateTypeRegistry.get(entry.getKey());
            if (typeOpt.isEmpty()) {
                warnUnregisteredStateKey(entry.getKey(), session.getEntityUUID());
                continue;
            }
            if (!typeOpt.get().isPersistent()) {
                continue;
            }
            CombatStateType<?> stateType = typeOpt.get();
            serializeEntry(stateType, entry.getValue()).ifPresent(serializedValue -> serialized.put(stateType, serializedValue));
        }
        return serialized;
    }

    /**
     * Logs a one-time warning for a state key held by a session but absent from
     * {@link CombatStateTypeRegistry}, so a forgotten registration is debuggable instead of silent.
     * Subsequent encounters of the same key are ignored to keep a legitimately-unregistered
     * session-scoped type from spamming the log on every session end.
     *
     * @param stateKey   The unregistered state key.
     * @param entityUUID The UUID of the session owner holding the state.
     */
    private void warnUnregisteredStateKey(@NotNull NamespacedKey stateKey, @NotNull UUID entityUUID) {
        if (!warnedUnregisteredStateKeys.add(stateKey)) {
            return;
        }
        plugin().getLogger().log(Level.WARNING, "Session for entity {0} holds combat state {1}, which has no "
                        + "registered CombatStateType. Session-scoped state works without registration, but the value "
                        + "will not be persisted or resolved at session boundaries — register the type via "
                        + "CombatStateTypeContentPack or CombatTrackerManager#registerStateType if that was intended. "
                        + "This warning is logged once per state key.",
                new Object[]{entityUUID, stateKey});
    }

    /**
     * Serializes a single raw state value using its type's registered serializer.
     * <p>
     * The serializer belongs to whoever registered the state type. If it throws, that one entry is
     * dropped rather than aborting the enclosing flush — a single faulty type must not take down
     * the whole session-end save or the shutdown flush behind it (and, in
     * {@link #endSession(UUID, CombatSessionEndReason)}, leak the session by unwinding before it is
     * removed from the active map).
     *
     * @param type     The state type owning the value.
     * @param rawValue The raw stored value.
     * @param <T>      The state value type.
     * @return An {@link Optional} containing the serialized value, or empty if no serializer is
     *         declared or the serializer threw.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private <T> Optional<String> serializeEntry(@NotNull CombatStateType<T> type, @NotNull Object rawValue) {
        try {
            return type.getSerializer().map(serializer -> serializer.apply((T) rawValue));
        } catch (Exception e) {
            plugin().getLogger().log(Level.WARNING, "Combat state type " + type.getKey()
                    + " serializer threw while persisting state; skipping this entry", e);
            return Optional.empty();
        }
    }

    /**
     * Merges newly-serialized persistent state entries into the in-memory cache, keyed by the
     * state type's key string (matching {@link CombatPersistentStateDAO}'s string-keyed map shape).
     *
     * @param entityUUID The UUID of the entity whose cache entry to update.
     * @param entries    The newly-serialized entries to merge in.
     */
    private void updatePersistentStateCache(@NotNull UUID entityUUID, @NotNull Map<CombatStateType<?>, String> entries) {
        Map<String, String> cacheEntry = persistentStateCache.computeIfAbsent(entityUUID, uuid -> new HashMap<>());
        for (Map.Entry<CombatStateType<?>, String> entry : entries.entrySet()) {
            cacheEntry.put(entry.getKey().getKey().toString(), entry.getValue());
        }
    }

    /**
     * Guards a public entry point against being called off the main server thread. The session map,
     * per-session collections, and timestamp fields are not thread-safe and this manager fires Bukkit
     * events, so all mutating calls must run on the main thread.
     *
     * @throws IllegalStateException if called from any thread other than the main server thread.
     */
    private void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("CombatTrackerManager must be called from the main server thread, "
                    + "but was called from thread: " + Thread.currentThread().getName());
        }
    }

    /**
     * Resolves the {@link ParticipantType} for the given entity UUID. Returns
     * {@link ParticipantType#PLAYER} if the entity is an online player, otherwise
     * {@link ParticipantType#MOB}.
     *
     * @param entityUUID The UUID of the entity to classify.
     * @return The resolved {@link ParticipantType}.
     */
    @NotNull
    private ParticipantType resolveParticipantType(@NotNull UUID entityUUID) {
        return Bukkit.getPlayer(entityUUID) != null ? ParticipantType.PLAYER : ParticipantType.MOB;
    }

    /**
     * Handles one side of a combat interaction. Ensures the session owner (identified by
     * {@code ownerUUID}) has an active session and that the other entity (identified by
     * {@code otherUUID}) is tracked as a participant in that session. Fires the appropriate
     * lifecycle events for session creation, participant addition, and mob FIFO eviction.
     * <p>
     * This method is called once per player side of a combat interaction — a PvP hit calls
     * it twice (once for each player), while a PvE hit calls it once (for the player only).
     * <p>
     * The other entity's {@link CustomEntityWrapper} is supplied lazily and resolved only when a
     * session or participant is actually created; the steady-state re-hit path never resolves it.
     *
     * @param ownerUUID            The UUID of the session owner (always a player).
     * @param otherUUID            The UUID of the other entity in the interaction.
     * @param otherType            The {@link ParticipantType} of the other entity.
     * @param otherWrapperSupplier Supplier of the other entity's {@link CustomEntityWrapper}.
     */
    private void handleSideInteraction(@NotNull UUID ownerUUID, @NotNull UUID otherUUID,
                                       @NotNull ParticipantType otherType,
                                       @NotNull Supplier<CustomEntityWrapper> otherWrapperSupplier) {
        CombatSession session = activeSessions.get(ownerUUID);

        if (session == null) {
            createSessionForInteraction(ownerUUID, otherUUID, otherType, otherWrapperSupplier.get());
            return;
        }

        if (!session.hasParticipant(otherUUID)) {
            addNewParticipant(session, otherUUID, otherType, otherWrapperSupplier.get());
            return;
        }

        // Participant already exists — just record the interaction
        session.recordParticipantInteraction(otherUUID);
    }

    /**
     * Creates a new session for {@code ownerUUID} after firing and confirming a non-cancelled
     * {@link CombatSessionStartEvent}, then adds the other entity as its first participant.
     *
     * @param ownerUUID          The UUID of the session owner (always a player).
     * @param otherUUID          The UUID of the other entity in the interaction.
     * @param otherType          The {@link ParticipantType} of the other entity.
     * @param otherEntityWrapper The {@link CustomEntityWrapper} of the other entity.
     */
    private void createSessionForInteraction(@NotNull UUID ownerUUID, @NotNull UUID otherUUID,
                                             @NotNull ParticipantType otherType,
                                             @NotNull CustomEntityWrapper otherEntityWrapper) {
        CombatSessionStartEvent startEvent = new CombatSessionStartEvent(
                ownerUUID, otherUUID, otherType, otherEntityWrapper);
        Bukkit.getPluginManager().callEvent(startEvent);

        if (startEvent.isCancelled()) {
            return;
        }

        CombatSession session = new CombatSession(ownerUUID, getMaxMobParticipants(), getSessionTimeoutMillis());
        activeSessions.put(ownerUUID, session);
        initializeNewSession(session);

        long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        CombatParticipant participant = new CombatParticipant(otherUUID, otherType, otherEntityWrapper, nowMillis);
        session.addParticipant(participant);
        session.recordParticipantInteraction(otherUUID);
    }

    /**
     * Adds a new participant to an existing session after firing and confirming a non-cancelled
     * {@link CombatParticipantAddEvent}. Handles FIFO mob eviction, firing
     * {@link CombatParticipantRemoveEvent} with {@link ParticipantRemovalReason#EVICTION} when a mob
     * is displaced. If the add event is cancelled, the entity is not added and the session's activity
     * timer is left untouched (a rejected participant should not keep the session alive).
     *
     * @param session            The existing session to add the participant to.
     * @param otherUUID          The UUID of the entity to add.
     * @param otherType          The {@link ParticipantType} of the entity.
     * @param otherEntityWrapper The {@link CustomEntityWrapper} of the entity.
     */
    private void addNewParticipant(@NotNull CombatSession session, @NotNull UUID otherUUID,
                                   @NotNull ParticipantType otherType,
                                   @NotNull CustomEntityWrapper otherEntityWrapper) {
        CombatType previousCombatType = session.getCombatType();

        long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        CombatParticipant newParticipant = new CombatParticipant(otherUUID, otherType, otherEntityWrapper, nowMillis);

        // Compute what the combat type would be after adding the new participant
        CombatType newCombatType = otherType == ParticipantType.PLAYER
                ? CombatType.PVP
                : previousCombatType;

        CombatParticipantAddEvent addEvent = new CombatParticipantAddEvent(
                session, newParticipant, previousCombatType, newCombatType);
        Bukkit.getPluginManager().callEvent(addEvent);

        if (addEvent.isCancelled()) {
            // The participant was rejected — do not refresh the session's activity timer, otherwise a
            // listener that persistently cancels adds would keep the owner's session alive forever.
            return;
        }

        Optional<CombatParticipant> evictedParticipant = session.addParticipant(newParticipant);

        // Handle FIFO eviction if a mob was evicted. The eviction event's "previous" type is the
        // session's type before this add/evict operation; the "new" type is the type afterward.
        if (evictedParticipant.isPresent()) {
            CombatParticipant evicted = evictedParticipant.get();
            CombatType typeAfterEviction = session.getCombatType();

            CombatParticipantRemoveEvent evictionEvent = new CombatParticipantRemoveEvent(
                    session, evicted, ParticipantRemovalReason.EVICTION,
                    previousCombatType, typeAfterEviction);
            Bukkit.getPluginManager().callEvent(evictionEvent);
        }

        session.recordParticipantInteraction(otherUUID);
    }
}
