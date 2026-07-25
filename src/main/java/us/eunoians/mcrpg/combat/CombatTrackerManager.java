package us.eunoians.mcrpg.combat;

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
import us.eunoians.mcrpg.combat.state.CombatStateCodec;
import us.eunoians.mcrpg.combat.state.CombatStateSnapshot;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticKey;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;
import us.eunoians.mcrpg.combat.task.CombatSessionTimeoutTask;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.event.combat.CombatHealingReportEvent;
import us.eunoians.mcrpg.event.combat.CombatParticipantAddEvent;
import us.eunoians.mcrpg.event.combat.CombatParticipantRemoveEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionStartEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
 * {@link IllegalStateException} if called from another thread.
 * <p>
 * Persistent combat state — its cache and the ordering and lifetime of its async database writes —
 * is delegated wholesale to {@link PersistentCombatStateStore}; the methods here are thin
 * pass-throughs that keep it reachable from the manager's public API.
 */
public class CombatTrackerManager extends Manager<McRPG> {

    private final Map<UUID, CombatSession> activeSessions;
    private final Map<NamespacedKey, CombatConditionTask> conditionTasks;
    @Nullable
    private CombatSessionTimeoutTask timeoutTask;
    private final PersistentCombatStateStore stateStore;
    private final Set<NamespacedKey> registeredDoubleStatKeys;
    private final Set<NamespacedKey> registeredLongStatKeys;
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
        this.stateStore = new PersistentCombatStateStore(mcRPG, new CombatStateCodec(mcRPG.getLogger()));
        this.registeredDoubleStatKeys = new HashSet<>();
        this.registeredLongStatKeys = new HashSet<>();
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
        CombatSession session = activeSessions.remove(entityUUID);
        if (session == null) {
            return;
        }

        // Unmapped first, deliberately: everything below can throw (third-party resolvers and
        // serializers, a database executor rejecting the write once it is shut down), and an
        // exception escaping while the session was still mapped would strand it in activeSessions
        // forever — its end event never fired, and every subsequent timeout scan re-throwing on it.
        // Nothing below reads activeSessions, so removing up front costs nothing.
        CombatStateTypeRegistry stateTypeRegistry = plugin().registryAccess()
                .registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        CombatSessionStatisticsSnapshot statisticsSnapshot = session.createStatisticsSnapshot();
        CombatStateSnapshot stateSnapshot = session.createStateSnapshot(stateTypeRegistry);

        if (!shuttingDown) {
            savePersistentStateAsync(session);
        }

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
            // Isolated per condition: createTask is an overridable third-party factory, and one
            // throwing override must not silently cost every condition after it in iteration order.
            try {
                startConditionTask(condition);
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING, "Combat condition " + condition.getKey()
                        + " failed to start its evaluation task; it will not hold entities in combat. "
                        + "Remaining conditions are unaffected.", e);
            }
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
     *   <li>{@link PersistentCombatStateStore#awaitPendingWrites()} drains writes submitted before shutdown began
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
        stateStore.awaitPendingWrites();
        shuttingDown = true;
        saveAllPersistentStateSync();

        // End all active sessions with reason PLUGIN. Each is isolated: shutdown is the last chance
        // to flush combat state, so one session failing to end must not cost every session after it.
        List<UUID> sessionOwners = new ArrayList<>(activeSessions.keySet());
        for (UUID ownerUUID : sessionOwners) {
            try {
                endSession(ownerUUID, CombatSessionEndReason.PLUGIN);
            } catch (Exception e) {
                plugin().getLogger().log(Level.WARNING,
                        "Failed to end the combat session for entity " + ownerUUID
                                + " during shutdown; continuing with the remaining sessions", e);
            }
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
     * per-type task for the manager to coordinate, so this adds no lifecycle work.
     * <p>
     * It does add one thing over the direct call: a main-thread assertion. The registry is backed by
     * a plain map, so registering off the main thread corrupts it silently; this method throws
     * instead. Prefer it over reaching for the registry when you are not certain of your thread.
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
     * <p>
     * Only <em>new</em> sessions are seeded, so register during plugin enable: a key registered
     * while players are already in combat is absent from those sessions' snapshots. Registration is
     * optional in the first place — {@code session.getStatistics().incrementDouble(myKey, 1.0)}
     * works on any key — it only guarantees the key appears even when never incremented.
     *
     * @param key The custom statistic key.
     * @throws IllegalArgumentException if the key is already registered as long-valued.
     */
    public void registerDoubleSessionStatisticKey(@NotNull NamespacedKey key) {
        requireMainThread();
        requireUnregisteredStatisticKey(key, registeredLongStatKeys, "long");
        registeredDoubleStatKeys.add(key);
    }

    /**
     * Registers a long-valued custom per-session statistic key so it appears with its default
     * value ({@code 0}) in every new session's statistics container.
     * <p>
     * Only <em>new</em> sessions are seeded, so register during plugin enable — see
     * {@link #registerDoubleSessionStatisticKey(NamespacedKey)} for the full contract.
     *
     * @param key The custom statistic key.
     * @throws IllegalArgumentException if the key is already registered as double-valued.
     */
    public void registerLongSessionStatisticKey(@NotNull NamespacedKey key) {
        requireMainThread();
        requireUnregisteredStatisticKey(key, registeredDoubleStatKeys, "double");
        registeredLongStatKeys.add(key);
    }

    /**
     * Rejects a statistic key that is already registered under the other value type.
     * {@link CombatSessionStatistics} keeps double and long statistics in separate maps, so the same
     * key registered to both would be seeded into both — and a {@code getLong} on a key written as a
     * double silently returns {@code 0} rather than failing.
     *
     * @param key             The key being registered.
     * @param otherTypeKeys   The registered keys of the opposite value type.
     * @param otherTypeName   The opposite value type's name, for the error message.
     * @throws IllegalArgumentException if the key is already registered under the other value type.
     */
    private void requireUnregisteredStatisticKey(@NotNull NamespacedKey key,
                                                 @NotNull Set<NamespacedKey> otherTypeKeys,
                                                 @NotNull String otherTypeName) {
        if (otherTypeKeys.contains(key)) {
            throw new IllegalArgumentException("Session statistic key " + key + " is already registered as "
                    + otherTypeName + "-valued; a key must have exactly one value type");
        }
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
     * Fires {@link CombatHealingReportEvent} before crediting, so third parties can scale or suppress
     * the attribution — {@code targetUUID} is carried on that event for listeners that need to know
     * who was healed.
     *
     * @param healerUUID The UUID of the entity that performed the healing.
     * @param targetUUID The UUID of the entity that was healed. Not used to credit the target's own
     *                   {@code healing_received} — that is the heal event listener's job — but
     *                   published on {@link CombatHealingReportEvent} for attribution listeners.
     * @param amount     The amount of healing applied.
     */
    public void reportHealing(@NotNull UUID healerUUID, @NotNull UUID targetUUID, double amount) {
        requireMainThread();
        CombatHealingReportEvent reportEvent = new CombatHealingReportEvent(healerUUID, targetUUID, amount);
        Bukkit.getPluginManager().callEvent(reportEvent);
        if (reportEvent.isCancelled()) {
            return;
        }
        getSession(healerUUID).ifPresent(session -> session.getStatistics()
                .incrementDouble(CombatSessionStatisticKey.HEALING_DEALT, reportEvent.getAmount()));
    }

    /**
     * Caches pre-loaded persistent combat state for a player. Called by
     * {@link us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask} after the DB load completes, during
     * main-thread finalization — before the player is registered in
     * {@link us.eunoians.mcrpg.entity.McRPGPlayerManager} and before any combat session can start.
     * <p>
     * Values already in the cache win over the loaded ones; see
     * {@link PersistentCombatStateStore#cache(UUID, Map)}.
     *
     * @param entityUUID      The UUID of the entity.
     * @param persistentState The loaded persistent state map (keyed by stringified NamespacedKey).
     */
    public void cachePersistentState(@NotNull UUID entityUUID, @NotNull Map<String, String> persistentState) {
        stateStore.cache(entityUUID, persistentState);
    }

    /**
     * Clears the persistent state cache for an entity immediately.
     * <p>
     * Prefer {@link #clearPersistentStateCacheWhenWritesSettle(UUID)} on any teardown path that has
     * just ended a session: this method drops the cache entry even when the corresponding database
     * write is still queued, which reopens the fast-relog window that the deferred variant exists to
     * close. Use this one only when no write can be outstanding for the entity.
     *
     * @param entityUUID The UUID of the entity whose cache to clear.
     */
    public void clearPersistentStateCache(@NotNull UUID entityUUID) {
        stateStore.clearCache(entityUUID);
    }

    /**
     * Clears an entity's persistent state cache once its outstanding database write has landed.
     * Called by {@link us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener} during combat
     * teardown on logout, after {@link #endSession(UUID, CombatSessionEndReason)} has submitted the
     * write — the ordering matters, since this method keys off that write.
     *
     * @param entityUUID The UUID of the entity whose cache to clear.
     * @see PersistentCombatStateStore#clearCacheWhenWritesSettle(UUID)
     */
    public void clearPersistentStateCacheWhenWritesSettle(@NotNull UUID entityUUID) {
        stateStore.clearCacheWhenWritesSettle(entityUUID);
    }

    /**
     * Saves dirty persistent state from a session to the database asynchronously. Called during
     * session end for sessions that contain persistent state. No-ops (and never touches the
     * database) when the session has no raw state matching a registered {@code PERSISTENT} type.
     * <p>
     * Writes for one entity are chained so they land in submission order, and the returned future
     * lets {@link #shutdown()} drain in-flight writes before the database closes. Note that it
     * completes <em>normally</em> even when the underlying statements failed — see
     * {@link PersistentCombatStateStore#saveAsync(CombatSession)} for the full failure contract.
     *
     * @param session The session whose persistent state to save.
     * @return A {@link CompletableFuture} completing when the write has been attempted; an
     *         already-completed future when the session has no persistent state to write.
     */
    @NotNull
    public CompletableFuture<Void> savePersistentStateAsync(@NotNull CombatSession session) {
        return stateStore.saveAsync(session);
    }

    /**
     * Saves all active sessions' dirty persistent state synchronously. Called during plugin shutdown
     * to ensure no persistent state is lost. No-ops (and never touches the database) when no active
     * session has any raw state matching a registered {@code PERSISTENT} type.
     */
    public void saveAllPersistentStateSync() {
        stateStore.saveAllSync(activeSessions.values());
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
        stateStore.applyCachedState(session);
        initializeRegisteredStatKeys(session);
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
