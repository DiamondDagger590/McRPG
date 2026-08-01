package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.state.CombatStateCodec;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.database.table.CombatPersistentStateDAO;
import us.eunoians.mcrpg.event.combat.CombatStateChangeEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Owns where {@code PERSISTENT}-scoped combat state lives between sessions: the in-memory cache that
 * spans a player's login, and the ordering and lifetime of the database writes that back it.
 * {@link CombatTrackerManager} delegates its persistence API here and keeps ownership of sessions,
 * conditions, and statistic keys.
 * <p>
 * Three collaborators, three concerns: {@link us.eunoians.mcrpg.database.table.CombatPersistentStateDAO}
 * owns the SQL, {@link CombatStateCodec} owns translating values to and from their stored form, and
 * this class owns the lifecycle tying them together.
 * <p>
 * <b>Write ordering.</b> Writes for one entity are chained so they reach the database in submission
 * order; writes for different entities stay fully parallel. Two session ends in quick succession for
 * the same entity — death then immediate re-engagement, rapid timeout cycling — would otherwise be
 * free to land in either order and let a stale write overwrite a fresher one.
 * <p>
 * <b>Cache lifetime.</b> An entity's cache entry is seeded at login, refreshed on every save, and
 * dropped on logout — but only once that logout's write has actually landed
 * ({@link #clearCacheWhenWritesSettle(UUID)}). The window between "write queued" and "write
 * committed" is exactly when a fast relog would otherwise read a stale row, so the cache is the
 * authority for that window.
 * <p>
 * <b>Faulty registrant callbacks.</b> Serializers, deserializers, and resolvers come from whoever
 * registered the state type, and their inputs include hand-editable database rows. A callback that
 * throws, returns {@code null}, or returns the wrong type is logged once per state type and its
 * entry skipped — never propagated, because these run inside session end, where an escaping
 * exception would strand the session in the manager's active map.
 * <p>
 * <b>Threading.</b> Every method must be called from the main server thread, enforced by
 * {@link #requireMainThread()}. The single exception is {@code pendingWrites}, which is completed
 * from the database executor thread and is therefore concurrent.
 */
class PersistentCombatStateStore {

    /**
     * How long {@link #awaitPendingWrites()} waits for outstanding writes before abandoning them.
     * Bounded so a wedged database executor cannot hang server shutdown indefinitely.
     */
    private static final long PENDING_WRITE_DRAIN_TIMEOUT_SECONDS = 10L;

    private final McRPG plugin;
    private final CombatStateCodec codec;
    private final Map<UUID, Map<String, String>> stateCache;
    /**
     * The most recently submitted write per entity. Each new write chains onto its predecessor, and
     * the map lets {@link #awaitPendingWrites()} drain outstanding writes before the database closes
     * and lets {@link #clearCacheWhenWritesSettle(UUID)} defer the cache clear.
     * <p>
     * Entries are added on the main thread and removed by the write's own completion callback, which
     * runs on the database executor thread — hence the concurrent map.
     */
    private final Map<UUID, CompletableFuture<Void>> pendingWrites;
    /**
     * State keys already reported by {@link #warnUnregisteredStateKey(NamespacedKey, UUID)}, so a
     * forgotten registration is logged once rather than on every session end.
     * <p>
     * <b>Eviction:</b> never — insert-only for this object's lifetime. That is bounded and
     * intentional: entries are {@link NamespacedKey}s of state types, which are effectively static
     * constants, so the set holds at most one entry per state type the server's plugins define. A
     * new instance is constructed on every plugin enable, so it is discarded on disable.
     */
    private final Set<NamespacedKey> warnedUnregisteredKeys;

    /**
     * Constructs a new {@link PersistentCombatStateStore}.
     *
     * @param plugin The {@link McRPG} plugin instance.
     * @param codec  The codec used to translate state values to and from their stored form.
     */
    PersistentCombatStateStore(@NotNull McRPG plugin, @NotNull CombatStateCodec codec) {
        this.plugin = plugin;
        this.codec = codec;
        this.stateCache = new HashMap<>();
        this.pendingWrites = new ConcurrentHashMap<>();
        this.warnedUnregisteredKeys = new HashSet<>();
    }

    /**
     * Caches pre-loaded persistent combat state for an entity, merging it into any existing entry
     * with the existing values winning.
     * <p>
     * Existing values win because a cache entry can outlive a logout when the player rejoins before
     * their logout write reaches the database (see {@link #clearCacheWhenWritesSettle(UUID)}). In
     * that window the retained in-memory value is fresher than the row this load just read.
     *
     * @param entityUUID      The UUID of the entity.
     * @param persistentState The loaded state map, keyed by stringified {@link NamespacedKey}.
     */
    void cache(@NotNull UUID entityUUID, @NotNull Map<String, String> persistentState) {
        requireMainThread();
        Map<String, String> cacheEntry = stateCache.computeIfAbsent(entityUUID, uuid -> new HashMap<>());
        for (Map.Entry<String, String> entry : persistentState.entrySet()) {
            cacheEntry.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Drops an entity's cache entry immediately.
     *
     * @param entityUUID The UUID of the entity whose cache to clear.
     */
    void clearCache(@NotNull UUID entityUUID) {
        requireMainThread();
        stateCache.remove(entityUUID);
    }

    /**
     * Drops an entity's cache entry once its outstanding write has landed, keeping it if the write
     * failed or if the entity has come back online in the meantime.
     * <p>
     * Clearing eagerly would open a fast-relog hole: the write sits on the database executor while
     * the reconnect path reads the same row on the main thread, so a player who rejoins quickly could
     * be seeded from their pre-logout row and then write that stale state back at their next session
     * end.
     *
     * @param entityUUID The UUID of the entity whose cache to clear.
     */
    void clearCacheWhenWritesSettle(@NotNull UUID entityUUID) {
        requireMainThread();
        CompletableFuture<Void> pendingWrite = pendingWrites.get(entityUUID);
        if (pendingWrite == null || pendingWrite.isDone()) {
            clearCache(entityUUID);
            return;
        }
        pendingWrite.whenComplete((ignored, throwable) -> {
            // A failed write means the database still holds the pre-logout row, so the cache entry is
            // the only surviving copy of the newer state — dropping it here would lose exactly what
            // this deferral exists to protect. Keep it; it is evicted on the entity's next clean save.
            if (throwable != null) {
                return;
            }
            // The write completes on the database executor thread, so hop back to the main thread
            // before touching the cache — unless the plugin is already disabling, in which case the
            // scheduler rejects new tasks and this object (cache included) is about to be discarded.
            if (!plugin.isEnabled()) {
                return;
            }
            try {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Skip the clear if the entity is back online: their cache now belongs to a live
                    // session that was seeded from it.
                    if (Bukkit.getPlayer(entityUUID) == null) {
                        clearCache(entityUUID);
                    }
                });
            } catch (IllegalPluginAccessException e) {
                // Lost the race against plugin disable between the check above and this call. This
                // object is being discarded anyway, so there is nothing left to clean up.
                plugin.getLogger().log(Level.FINE,
                        "Skipped the deferred combat state cache clear for entity " + entityUUID
                                + " because the plugin disabled while its write was in flight", e);
            }
        });
    }

    /**
     * Serializes a session's persistent state and writes it asynchronously, chaining behind any write
     * already outstanding for the same entity. No-ops (and never touches the database) when the
     * session holds no state matching a registered {@code PERSISTENT} type.
     * <p>
     * <b>Failure signalling:</b> the returned future completes <em>exceptionally</em> when the write
     * could not be performed at all — the database executor rejecting it once shut down, or the
     * connection pool refusing a connection. It completes <em>normally</em> when the statements
     * themselves fail, because McCore's {@code BatchTransaction} logs and swallows per-statement
     * {@link SQLException}s internally. A normally-completed future therefore means "the write ran",
     * not "the data is persisted"; the server log is the authority on statement-level failures.
     *
     * @param session The session whose persistent state to save.
     * @return A {@link CompletableFuture} completing when the write has been attempted; an
     *         already-completed future when there was nothing to write.
     */
    @NotNull
    CompletableFuture<Void> saveAsync(@NotNull CombatSession session) {
        requireMainThread();
        Map<CombatStateType<?>, String> serializedEntries = collectPersistentEntries(session);
        if (serializedEntries.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        UUID entityUUID = session.getEntityUUID();
        updateCache(entityUUID, serializedEntries);

        Database database = plugin.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        CompletableFuture<Void> previousWrite = pendingWrites.getOrDefault(
                entityUUID, CompletableFuture.completedFuture(null));
        // exceptionally() first: a failed predecessor must not cancel this entity's next write.
        CompletableFuture<Void> write = previousWrite
                .exceptionally(throwable -> null)
                .thenRunAsync(() -> writeEntries(database, entityUUID, serializedEntries),
                        database.getDatabaseExecutorService());
        // put before whenComplete, not after: with a same-thread executor the write is already
        // complete by now, so the callback fires inline — if the entry were not yet in the map it
        // would never be removed. Do not reorder these two lines.
        pendingWrites.put(entityUUID, write);
        write.whenComplete((ignored, throwable) -> {
            // Two-arg remove: a successor write may already have replaced this entry, in which case
            // this completion must leave it alone.
            pendingWrites.remove(entityUUID, write);
            if (throwable != null) {
                plugin.getLogger().log(Level.WARNING, "Persistent combat state write failed for entity "
                        + entityUUID + "; its state was not saved", throwable);
            }
        });
        return write;
    }

    /**
     * Serializes and writes every given session's persistent state on the calling thread, in one
     * batch. Used at shutdown, where the async path cannot be relied on to complete before the
     * database closes. No-ops (and never touches the database) when no session holds persistent
     * state.
     *
     * @param sessions The sessions to flush.
     */
    void saveAllSync(@NotNull Collection<CombatSession> sessions) {
        requireMainThread();
        Map<UUID, Map<CombatStateType<?>, String>> perEntityEntries = new HashMap<>();
        for (CombatSession session : sessions) {
            Map<CombatStateType<?>, String> entries = collectPersistentEntries(session);
            if (!entries.isEmpty()) {
                perEntityEntries.put(session.getEntityUUID(), entries);
            }
        }
        if (perEntityEntries.isEmpty()) {
            return;
        }

        for (Map.Entry<UUID, Map<CombatStateType<?>, String>> entityEntry : perEntityEntries.entrySet()) {
            updateCache(entityEntry.getKey(), entityEntry.getValue());
        }

        Database database = plugin.registryAccess().registry(RegistryKey.MANAGER)
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
            plugin.getLogger().log(Level.SEVERE,
                    "Failed to synchronously save persistent combat state during shutdown", e);
        }
    }

    /**
     * Blocks until every outstanding write has completed, or until
     * {@link #PENDING_WRITE_DRAIN_TIMEOUT_SECONDS} elapses.
     * <p>
     * The timeout is bounded so a wedged database executor cannot hang server shutdown; on timeout
     * the remaining writes are abandoned with a warning rather than waited on indefinitely. Blocking
     * the main thread here is safe because of a property of these specific futures, not of the
     * executor: every future in the map is rooted at a completed future and chains only
     * {@link #writeEntries}, which never touches the main thread. Do not store a future in this map
     * that depends on main-thread work — that would deadlock here.
     */
    void awaitPendingWrites() {
        if (pendingWrites.isEmpty()) {
            return;
        }
        CompletableFuture<?>[] outstandingWrites = pendingWrites.values().toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(outstandingWrites).get(PENDING_WRITE_DRAIN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            plugin.getLogger().log(Level.WARNING, "Timed out after " + PENDING_WRITE_DRAIN_TIMEOUT_SECONDS
                    + " seconds waiting for " + outstandingWrites.length
                    + " persistent combat state write(s) to finish; abandoning them so shutdown can continue", e);
        } catch (ExecutionException e) {
            plugin.getLogger().log(Level.WARNING,
                    "A persistent combat state write failed while draining writes during shutdown", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            plugin.getLogger().log(Level.WARNING,
                    "Interrupted while draining persistent combat state writes during shutdown", e);
        }
    }

    /**
     * Applies any cached persistent state for the session's owner, decoding each entry through
     * {@link CombatStateCodec} and writing it into the session's raw state store without firing
     * {@link CombatStateChangeEvent}. Entries whose key is not a valid {@link NamespacedKey}, does
     * not resolve to a registered state type, or fails to decode are skipped — the last of those
     * leaves the state at its default and is reported by the codec.
     *
     * @param session The newly-created session to seed.
     */
    void applyCachedState(@NotNull CombatSession session) {
        Map<String, String> cached = stateCache.get(session.getEntityUUID());
        if (cached == null || cached.isEmpty()) {
            return;
        }

        CombatStateTypeRegistry stateTypeRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
        for (Map.Entry<String, String> entry : cached.entrySet()) {
            NamespacedKey key = NamespacedKey.fromString(entry.getKey());
            if (key == null) {
                continue;
            }
            stateTypeRegistry.get(key).ifPresent(stateType ->
                    codec.decode(stateType, entry.getValue(), session.getEntityUUID())
                            .ifPresent(decoded -> session.setRawState(stateType.getKey(), decoded)));
        }
    }

    /**
     * Collects every entry in the session's raw state store whose key resolves to a registered
     * {@link us.eunoians.mcrpg.combat.state.CombatStateLifecycle#PERSISTENT} {@link CombatStateType},
     * encoding each through {@link CombatStateCodec}. Entries whose type is unregistered,
     * {@code SESSION}-scoped, or that fail to encode are skipped.
     *
     * @param session The session to collect from.
     * @return A map of persistent state type to serialized value; empty if the session has none.
     */
    @NotNull
    private Map<CombatStateType<?>, String> collectPersistentEntries(@NotNull CombatSession session) {
        CombatStateTypeRegistry stateTypeRegistry = plugin.registryAccess()
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
            codec.encode(stateType, entry.getValue(), session.getEntityUUID())
                    .ifPresent(encoded -> serialized.put(stateType, encoded));
        }
        return serialized;
    }

    /**
     * Writes one entity's serialized state to the database in a single batch. Runs on the database
     * executor thread.
     * <p>
     * A failure to obtain a connection is deliberately <em>not</em> caught here. McCore's
     * {@code Database#getConnection()} wraps its {@link SQLException} in a {@link RuntimeException},
     * which propagates out and completes the enclosing write future exceptionally — that is what lets
     * {@link #saveAsync(CombatSession)} log the failure and {@link #clearCacheWhenWritesSettle(UUID)}
     * retain the cache entry that is now the only copy of the unsaved state. Catching it here would
     * report a phantom success instead.
     *
     * @param database          The database to write through.
     * @param entityUUID        The UUID of the entity whose state is being written.
     * @param serializedEntries The serialized state entries to write.
     */
    private void writeEntries(@NotNull Database database, @NotNull UUID entityUUID,
                              @NotNull Map<CombatStateType<?>, String> serializedEntries) {
        try (Connection connection = database.getConnection()) {
            BatchTransaction batch = new BatchTransaction(connection);
            for (Map.Entry<CombatStateType<?>, String> entry : serializedEntries.entrySet()) {
                batch.addAll(CombatPersistentStateDAO.savePersistentState(
                        connection, entityUUID, entry.getKey().getKey().toString(), entry.getValue()));
            }
            batch.executeTransaction();
        } catch (SQLException e) {
            // Only reachable from Connection#close() — BatchTransaction swallows statement failures.
            plugin.getLogger().log(Level.WARNING,
                    "Failed to close the connection after saving persistent combat state for entity "
                            + entityUUID, e);
        }
    }

    /**
     * Merges newly-serialized entries into the in-memory cache, keyed by the state type's key string
     * (matching {@link CombatPersistentStateDAO}'s string-keyed map shape).
     *
     * @param entityUUID The UUID of the entity whose cache entry to update.
     * @param entries    The newly-serialized entries to merge in.
     */
    private void updateCache(@NotNull UUID entityUUID, @NotNull Map<CombatStateType<?>, String> entries) {
        Map<String, String> cacheEntry = stateCache.computeIfAbsent(entityUUID, uuid -> new HashMap<>());
        for (Map.Entry<CombatStateType<?>, String> entry : entries.entrySet()) {
            cacheEntry.put(entry.getKey().getKey().toString(), entry.getValue());
        }
    }

    /**
     * Logs a one-time warning for a state key held by a session but absent from
     * {@link CombatStateTypeRegistry}, so a forgotten registration is debuggable instead of silent.
     *
     * @param stateKey   The unregistered state key.
     * @param entityUUID The UUID of the session owner holding the state.
     */
    private void warnUnregisteredStateKey(@NotNull NamespacedKey stateKey, @NotNull UUID entityUUID) {
        if (!warnedUnregisteredKeys.add(stateKey)) {
            return;
        }
        plugin.getLogger().log(Level.WARNING, "Session for entity {0} holds combat state {1}, which has no "
                        + "registered CombatStateType. Session-scoped state works without registration, but the value "
                        + "will not be persisted or resolved at session boundaries — register the type via "
                        + "CombatStateTypeContentPack or CombatTrackerManager#registerStateType if that was intended. "
                        + "This warning is logged once per state key.",
                new Object[]{entityUUID, stateKey});
    }

    /**
     * Guards an entry point against being called off the main server thread. The cache and the
     * per-session state stores are plain, non-concurrent structures.
     *
     * @throws IllegalStateException if called from any thread other than the main server thread.
     */
    private void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("PersistentCombatStateStore must be called from the main server "
                    + "thread, but was called from thread: " + Thread.currentThread().getName());
        }
    }
}
