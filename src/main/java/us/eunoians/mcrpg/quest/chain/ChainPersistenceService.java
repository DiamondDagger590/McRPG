package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.database.table.quest.QuestChainStateDAO;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Owns all asynchronous and synchronous persistence operations for quest chain state.
 * Serializes per-player writes to prevent concurrent DB mutations to the same player's
 * chain rows, and moves {@code clearDirtyIfCurrent} back to the main thread after a
 * successful write.
 * <p>
 * Eviction: {@code pendingSaves} entries are removed via {@code whenComplete} after each
 * future completes. {@code writeGenerations} entries are removed by {@link #cleanupPlayer(UUID)}
 * at player logout after the synchronous flush completes.
 */
public class ChainPersistenceService {

    /**
     * Per-player future chain for serializing writes. Each new save for a player chains
     * onto the previous one so that concurrent DB submissions for the same player always
     * execute in submission order. Entries are evicted via {@code whenComplete}.
     */
    private final ConcurrentHashMap<UUID, CompletableFuture<Void>> pendingSaves = new ConcurrentHashMap<>();

    /**
     * Per-player monotonic write generation counter. Incremented by {@link #prepareForFlush(UUID)}
     * before a synchronous flush. Async writes capture the generation at snapshot time and skip
     * their JDBC if a newer generation is observed at execution time, preventing stale writes from
     * overwriting a flush or a delete. Entries are removed by {@link #cleanupPlayer(UUID)}.
     */
    private final ConcurrentHashMap<UUID, AtomicInteger> writeGenerations = new ConcurrentHashMap<>();

    private final McRPG plugin;

    /**
     * Creates a new persistence service.
     *
     * @param plugin the McRPG plugin instance used to access the DB executor and scheduler
     */
    public ChainPersistenceService(@NotNull McRPG plugin) {
        this.plugin = plugin;
    }

    /**
     * Persists a chain state asynchronously. Snapshots the current state values before
     * submitting to avoid read/write races. Writes are serialized per-player via
     * {@code pendingSaves}: each new submission chains onto the previous future so that
     * concurrent saves for the same player always execute in submission order.
     * <p>
     * After a successful write, {@link QuestChainPlayerState#clearDirtyIfCurrent(int)} is
     * scheduled back on the main thread using the version captured at snapshot time so that
     * a mutation occurring between snapshot and write correctly retains dirty.
     * <p>
     * If the write generation has been incremented since this save was enqueued (e.g. by a
     * logout flush), the JDBC call is skipped entirely.
     *
     * @param playerUUID the player UUID
     * @param state      the chain state to persist
     */
    public void saveChainStateAsync(@NotNull UUID playerUUID, @NotNull QuestChainPlayerState state) {
        NamespacedKey chainKey = state.getChainKey();
        int dirtySnapshot = state.getDirtyVersion();
        int generation = writeGenerations.computeIfAbsent(playerUUID, k -> new AtomicInteger()).get();

        QuestChainPlayerState snapshot = new QuestChainPlayerState(
                chainKey,
                state.getCurrentQuestKey().orElse(null),
                state.getState(),
                state.getCompletionCount(),
                state.getLastCompletedAt().orElse(null));

        Database database = getDatabase();
        CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> {
            AtomicInteger gen = writeGenerations.get(playerUUID);
            if (gen == null || gen.get() != generation) {
                return;
            }
            try (Connection connection = database.getConnection()) {
                boolean saved = QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot);
                if (saved) {
                    Bukkit.getScheduler().runTask(plugin, () -> state.clearDirtyIfCurrent(dirtySnapshot));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to save chain state for player " + playerUUID
                                + ", chain " + chainKey, e);
            }
        }, database.getDatabaseExecutorService());

        CompletableFuture<Void> guarded = saveTask.exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE,
                    "[ChainPersistenceService] Unhandled exception in save chain for player " + playerUUID, ex);
            return null;
        });

        pendingSaves.compute(playerUUID, (uuid, prev) -> {
            CompletableFuture<Void> chained = prev == null ? guarded : prev.thenCompose(v -> guarded);
            chained.whenComplete((v, ex) -> pendingSaves.remove(playerUUID, chained));
            return chained;
        });
    }

    /**
     * Persists both a step completion log entry and the updated chain state in a single
     * database connection. Coalesces two separate async writes into one to halve the
     * connection overhead per step advance. Clears the dirty flag on the main thread after
     * a successful write.
     * <p>
     * The write generation gate applies identically to {@link #saveChainStateAsync}.
     *
     * @param playerUUID        the player UUID
     * @param state             the chain state to persist
     * @param chainKey          the chain key (for the completion log entry)
     * @param completedQuestKey the quest key that was just completed
     * @param completionNumber  which run this is (1-based)
     */
    public void persistAdvancementAsync(@NotNull UUID playerUUID,
                                        @NotNull QuestChainPlayerState state,
                                        @NotNull NamespacedKey chainKey,
                                        @NotNull NamespacedKey completedQuestKey,
                                        int completionNumber) {
        int dirtySnapshot = state.getDirtyVersion();
        int generation = writeGenerations.computeIfAbsent(playerUUID, k -> new AtomicInteger()).get();

        QuestChainPlayerState snapshot = new QuestChainPlayerState(
                state.getChainKey(),
                state.getCurrentQuestKey().orElse(null),
                state.getState(),
                state.getCompletionCount(),
                state.getLastCompletedAt().orElse(null));

        long completedAt = plugin.getTimeProvider().now().toEpochMilli();
        Database database = getDatabase();
        CompletableFuture<Void> advanceTask = CompletableFuture.runAsync(() -> {
            AtomicInteger gen = writeGenerations.get(playerUUID);
            if (gen == null || gen.get() != generation) {
                return;
            }
            try (Connection connection = database.getConnection()) {
                QuestChainCompletionLogDAO.logCompletion(connection, playerUUID,
                        chainKey.toString(), completedQuestKey.toString(), completedAt, completionNumber);
                boolean saved = QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot);
                if (saved) {
                    Bukkit.getScheduler().runTask(plugin, () -> state.clearDirtyIfCurrent(dirtySnapshot));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to persist advancement for player " + playerUUID
                                + ", chain " + chainKey + ", quest " + completedQuestKey, e);
            }
        }, database.getDatabaseExecutorService());

        CompletableFuture<Void> guarded = advanceTask.exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE,
                    "[ChainPersistenceService] Unhandled exception in persist advancement for player " + playerUUID, ex);
            return null;
        });

        pendingSaves.compute(playerUUID, (uuid, prev) -> {
            CompletableFuture<Void> chained = prev == null ? guarded : prev.thenCompose(v -> guarded);
            chained.whenComplete((v, ex) -> pendingSaves.remove(playerUUID, chained));
            return chained;
        });
    }

    /**
     * Synchronously flushes all dirty chain states for a player on the database executor
     * thread. Snapshots each state before writing to avoid read/write races. Only clears the
     * dirty flag if the DAO write succeeds.
     * <p>
     * This method must only be called from the database executor thread (e.g. inside
     * {@code McRPGPlayerUnloadTask.unloadPlayer()}), and only after {@link #prepareForFlush(UUID)}
     * has been called to gate any in-flight async writes.
     *
     * @param connection the open database connection to use
     * @param playerUUID the player UUID
     * @param chainData  the player's chain data container
     */
    public void flushChainStatesSync(@NotNull Connection connection,
                                     @NotNull UUID playerUUID,
                                     @NotNull QuestChainPlayerData chainData) {
        List<QuestChainPlayerState> dirty = chainData.getDirtyStates();
        for (QuestChainPlayerState state : dirty) {
            try {
                QuestChainPlayerState snapshot = new QuestChainPlayerState(
                        state.getChainKey(),
                        state.getCurrentQuestKey().orElse(null),
                        state.getState(),
                        state.getCompletionCount(),
                        state.getLastCompletedAt().orElse(null));
                boolean saved = QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot);
                if (saved) {
                    state.clearDirty();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to flush dirty chain state for player " + playerUUID
                                + ", chain " + state.getChainKey(), e);
            }
        }
    }

    /**
     * Cancels and removes any pending in-flight save for the given player. Called before
     * async delete operations (e.g. chain reset) to prevent a queued save from racing with
     * the subsequent delete and restoring stale data.
     *
     * @param playerUUID the player UUID whose pending save should be cancelled
     */
    public void cancelPendingSave(@NotNull UUID playerUUID) {
        CompletableFuture<Void> pending = pendingSaves.remove(playerUUID);
        if (pending != null) {
            pending.cancel(true);
        }
    }

    /**
     * Prepares for a synchronous flush by incrementing the write generation (so any
     * in-flight async writes will see a stale generation and skip their JDBC) and
     * cancelling the pending future chain. Called from {@code McRPGPlayerUnloadTask}
     * before {@link #flushChainStatesSync}.
     *
     * @param playerUUID the player UUID to prepare for flush
     */
    public void prepareForFlush(@NotNull UUID playerUUID) {
        writeGenerations.computeIfAbsent(playerUUID, k -> new AtomicInteger()).incrementAndGet();
        cancelPendingSave(playerUUID);
    }

    /**
     * Removes all per-player tracking state (pending saves, write generations).
     * Called after a synchronous flush has completed and the player object is
     * about to be discarded — no more async writes can arrive for this player
     * since the generation gate already caused any in-flight ones to skip.
     *
     * @param playerUUID the player UUID to clean up
     */
    public void cleanupPlayer(@NotNull UUID playerUUID) {
        pendingSaves.remove(playerUUID);
        writeGenerations.remove(playerUUID);
    }

    /**
     * Returns the database instance from the registry.
     *
     * @return the database
     */
    @NotNull
    private Database getDatabase() {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
    }
}
