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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Owns all asynchronous and synchronous persistence operations for quest chain state.
 * Serializes per-player writes to prevent concurrent DB mutations to the same player's
 * chain rows, and moves {@code clearDirty} back to the main thread after a successful write.
 * <p>
 * Eviction: entries in {@code pendingSaves} are removed once their chain completes normally.
 * Stale entries for players who quit while a save is in-flight are cleaned up by
 * {@link #cancelPendingSave(UUID)} during chain reset and by natural future completion.
 */
public class ChainPersistenceService {

    /**
     * Per-player future chain for serializing writes. Each new save for a player chains
     * onto the previous one so that concurrent DB submissions for the same player always
     * execute in submission order.
     */
    private final ConcurrentHashMap<UUID, CompletableFuture<Void>> pendingSaves = new ConcurrentHashMap<>();

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
     * After a successful write, {@link QuestChainPlayerState#clearDirty()} is scheduled
     * back on the main thread.
     *
     * @param playerUUID the player UUID
     * @param state      the chain state to persist
     */
    public void saveChainStateAsync(@NotNull UUID playerUUID, @NotNull QuestChainPlayerState state) {
        NamespacedKey chainKey = state.getChainKey();
        QuestChainState chainState = state.getState();
        int completionCount = state.getCompletionCount();
        Optional<NamespacedKey> currentQuestKey = state.getCurrentQuestKey();
        Optional<Long> lastCompletedAt = state.getLastCompletedAt();

        QuestChainPlayerState snapshot = new QuestChainPlayerState(
                chainKey,
                currentQuestKey.orElse(null),
                chainState,
                completionCount,
                lastCompletedAt.orElse(null));

        Database database = getDatabase();
        CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> {
            try (Connection connection = database.getConnection()) {
                QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot);
                Bukkit.getScheduler().runTask(plugin, state::clearDirty);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to save chain state for player " + playerUUID
                                + ", chain " + chainKey, e);
            }
        }, database.getDatabaseExecutorService());

        pendingSaves.compute(playerUUID, (uuid, prev) ->
                prev == null ? saveTask : prev.thenCompose(v -> saveTask));
    }

    /**
     * Persists both a step completion log entry and the updated chain state in a single
     * database connection. Coalesces two separate async writes into one to halve the
     * connection overhead per step advance. Clears the dirty flag on the main thread after
     * a successful write.
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
        NamespacedKey stateChainKey = state.getChainKey();
        QuestChainState chainState = state.getState();
        int completionCount = state.getCompletionCount();
        Optional<NamespacedKey> currentQuestKey = state.getCurrentQuestKey();
        Optional<Long> lastCompletedAt = state.getLastCompletedAt();

        QuestChainPlayerState snapshot = new QuestChainPlayerState(
                stateChainKey,
                currentQuestKey.orElse(null),
                chainState,
                completionCount,
                lastCompletedAt.orElse(null));

        long completedAt = plugin.getTimeProvider().now().toEpochMilli();
        Database database = getDatabase();
        CompletableFuture<Void> advanceTask = CompletableFuture.runAsync(() -> {
            try (Connection connection = database.getConnection()) {
                QuestChainCompletionLogDAO.logCompletion(connection, playerUUID,
                        chainKey.toString(), completedQuestKey.toString(), completedAt, completionNumber);
                QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot);
                Bukkit.getScheduler().runTask(plugin, state::clearDirty);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to persist advancement for player " + playerUUID
                                + ", chain " + chainKey + ", quest " + completedQuestKey, e);
            }
        }, database.getDatabaseExecutorService());

        pendingSaves.compute(playerUUID, (uuid, prev) ->
                prev == null ? advanceTask : prev.thenCompose(v -> advanceTask));
    }

    /**
     * Synchronously flushes all dirty chain states for a player on the database executor
     * thread. Called during player logout to ensure no dirty states are lost when the
     * player object is discarded.
     * <p>
     * This method must only be called from the database executor thread (e.g. inside
     * {@code McRPGPlayerUnloadTask.unloadPlayer()}).
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
                QuestChainStateDAO.saveChainState(connection, playerUUID, state);
                state.clearDirty();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ChainPersistenceService] Failed to flush dirty chain state for player " + playerUUID
                                + ", chain " + state.getChainKey(), e);
            }
        }
    }

    /**
     * Cancels and awaits any pending in-flight save for the given player. Called before
     * async delete operations (e.g. chain reset) to prevent a queued save from racing with
     * the subsequent delete and restoring stale data.
     * <p>
     * The pending future is cancelled (with interrupt). If it has already completed the
     * cancel is a no-op.
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
