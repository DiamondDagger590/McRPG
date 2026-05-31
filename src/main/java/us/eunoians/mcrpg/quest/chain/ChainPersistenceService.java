package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.database.table.quest.QuestChainStateDAO;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Owns all asynchronous and synchronous persistence operations for quest chain state.
 * Serializes per-player writes to prevent concurrent DB mutations to the same player's
 * chain rows.
 * <p>
 * Async writes ({@link #saveChainStateAsync}) are optimistic: they fire and forget,
 * relying on the write-generation gate to skip stale writes when a logout flush
 * supersedes them. Dirty flags are only cleared by the authoritative synchronous
 * flush at logout ({@link #flushChainStatesSync}).
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
     * Persists a chain state and any pending advancement log entries asynchronously.
     * Snapshots the current state values and pending advancements before submitting
     * to avoid read/write races. Writes are serialized per-player via
     * {@code pendingSaves}: each new submission chains onto the previous future so that
     * concurrent saves for the same player always execute in submission order.
     * <p>
     * Both the state upsert and any completion-log entries are written in a single
     * {@link FailSafeTransaction} — they succeed together or both roll back.
     * Dirty flags are not cleared here — the authoritative flush at logout
     * ({@link #flushChainStatesSync}) handles that.
     * <p>
     * If the write generation has been incremented since this save was enqueued (e.g. by a
     * logout flush), the JDBC call is skipped entirely.
     *
     * @param playerUUID the player UUID
     * @param state      the chain state to persist
     */
    public void saveChainStateAsync(@NotNull UUID playerUUID, @NotNull QuestChainPlayerState state) {
        NamespacedKey chainKey = state.getChainKey();
        int generation = writeGenerations.computeIfAbsent(playerUUID, k -> new AtomicInteger()).get();

        QuestChainPlayerState snapshot = new QuestChainPlayerState(
                chainKey,
                state.getCurrentQuestKey().orElse(null),
                state.getState(),
                state.getCompletionCount(),
                state.getLastCompletedAt().orElse(null));
        List<QuestChainPlayerState.PendingAdvancement> advancementSnapshot = state.drainPendingAdvancements();

        Database database = getDatabase();
        CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> {
            AtomicInteger gen = writeGenerations.get(playerUUID);
            if (gen == null || gen.get() != generation) {
                return;
            }
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = new ArrayList<>();
                statements.addAll(QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot));
                for (QuestChainPlayerState.PendingAdvancement adv : advancementSnapshot) {
                    statements.addAll(QuestChainCompletionLogDAO.logCompletion(connection, playerUUID,
                            chainKey.toString(), adv.questKey().toString(), adv.completedAt(), adv.completionNumber()));
                }
                new FailSafeTransaction(connection, statements).executeTransaction();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to save chain state for player " + playerUUID
                                + ", chain " + chainKey, e);
            }
        }, database.getDatabaseExecutorService());

        CompletableFuture<Void> guarded = saveTask.exceptionally(ex -> {
            if (ex instanceof CancellationException) {
                return null;
            }
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
     * Synchronously flushes all dirty chain states for a player on the database executor
     * thread. Each dirty state is saved via its own {@link FailSafeTransaction} so that a
     * failure on one state does not prevent others from being written. On success, clears
     * the dirty flag immediately.
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
        for (QuestChainPlayerState state : chainData.getDirtyStates()) {
            try {
                QuestChainPlayerState snapshot = new QuestChainPlayerState(
                        state.getChainKey(),
                        state.getCurrentQuestKey().orElse(null),
                        state.getState(),
                        state.getCompletionCount(),
                        state.getLastCompletedAt().orElse(null));
                List<PreparedStatement> statements = new ArrayList<>();
                statements.addAll(QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot));
                NamespacedKey chainKey = state.getChainKey();
                List<QuestChainPlayerState.PendingAdvancement> advancementSnapshot =
                        List.copyOf(state.getPendingAdvancements());
                for (QuestChainPlayerState.PendingAdvancement adv : advancementSnapshot) {
                    statements.addAll(QuestChainCompletionLogDAO.logCompletion(connection, playerUUID,
                            chainKey.toString(), adv.questKey().toString(), adv.completedAt(), adv.completionNumber()));
                }
                new FailSafeTransaction(connection, statements).executeTransaction();
                state.clearPendingAdvancements();
                state.clearDirty();
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
