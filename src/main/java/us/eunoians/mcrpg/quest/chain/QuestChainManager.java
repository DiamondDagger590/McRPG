package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.QuestChainStateDAO;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Manages quest chain lifecycle: starting, advancing, completing, restarting, resetting,
 * and persisting chain state. Delegates individual quest starts to
 * {@link us.eunoians.mcrpg.quest.QuestManager} and never creates
 * {@link us.eunoians.mcrpg.quest.impl.QuestInstance} objects directly.
 * <p>
 * All methods that mutate {@link QuestChainPlayerState} or fire events run on the main
 * Bukkit thread. All DAO read operations run on the database executor thread, with results
 * delivered back to the main thread via {@code Bukkit.getScheduler().runTask()}.
 */
public class QuestChainManager extends Manager<McRPG> {

    public QuestChainManager(@NotNull McRPG plugin) {
        super(plugin);
    }

    /**
     * Attempts to start a chain for a player. If the chain is already ACTIVE or the player
     * is blocked by the repeat mode, returns false silently.
     *
     * @param player   the player
     * @param chainKey the chain definition key
     * @return {@code true} if the chain was started
     */
    public boolean tryStartChain(@NotNull Player player, @NotNull NamespacedKey chainKey) {
        plugin().getLogger().log(Level.INFO,
                "[QuestChainManager] tryStartChain called for player " + player.getUniqueId() + ", chain " + chainKey);
        return false;
    }

    /**
     * Advances a player's chain to the next step after the specified quest key was completed.
     * Uses the O(1) reverse index in {@link QuestChainPlayerData} to locate the chain.
     * No-op if the completed quest is not managed by any active chain for this player.
     *
     * @param playerUUID        the player UUID
     * @param completedQuestKey the quest definition key that was just completed
     */
    public void advanceChain(@NotNull UUID playerUUID, @NotNull NamespacedKey completedQuestKey) {
    }

    /**
     * Force-advances a player's chain by completing the current step and starting the next,
     * bypassing normal quest completion flow. Intended for admin use only.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain definition key
     * @return {@code true} if the advancement succeeded
     */
    public boolean forceAdvanceChain(@NotNull UUID playerUUID, @NotNull NamespacedKey chainKey) {
        return false;
    }

    /**
     * Restarts a player's chain from an appropriate step. If {@code force} is true, restarts
     * from step 1 regardless of completion history. If false, reads the completion log
     * asynchronously and skips already-completed steps, starting from the first incomplete one.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain definition key
     * @param force      if true, replay all steps regardless of the completion log
     * @param callback   invoked on the main thread with {@code true} if restart succeeded
     */
    public void restartChain(@NotNull UUID playerUUID,
                             @NotNull NamespacedKey chainKey,
                             boolean force,
                             @NotNull Consumer<Boolean> callback) {
        callback.accept(false);
    }

    /**
     * Hard-resets a player's chain state — clears chain state and completion log entries.
     * The player experiences the chain as if they have never started it.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain definition key
     * @return {@code true} if the reset succeeded
     */
    public boolean resetChain(@NotNull UUID playerUUID, @NotNull NamespacedKey chainKey) {
        return false;
    }

    /**
     * Handles a cancelled quest that may belong to an active chain. If the cancelled quest is
     * the current step of an ACTIVE chain, transitions the chain to ABANDONED.
     *
     * @param playerUUID        the player UUID
     * @param cancelledQuestKey the cancelled quest's definition key
     */
    public void handleQuestCancelled(@NotNull UUID playerUUID, @NotNull NamespacedKey cancelledQuestKey) {
    }

    /**
     * Handles an expired quest that may belong to an active chain. Applies the chain's
     * configured {@code on-quest-expire} behavior.
     *
     * @param playerUUID      the player UUID
     * @param expiredQuestKey the expired quest's definition key
     */
    public void handleQuestExpired(@NotNull UUID playerUUID, @NotNull NamespacedKey expiredQuestKey) {
    }

    /**
     * Re-resolves chain state for a player on login. Handles any definition changes that
     * occurred while the player was offline (removed steps, renamed quest keys, etc.).
     *
     * @param playerUUID the player UUID
     */
    public void reResolveOnLogin(@NotNull UUID playerUUID) {
    }

    /**
     * Re-resolves chain state for all online players after a reload.
     * Delegates to {@link #reResolveOnLogin(UUID)} for each online player.
     */
    public void reResolveOnReload() {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getAllPlayers().forEach(p -> reResolveOnLogin(p.getUUID()));
    }

    /**
     * Gets the chain state for a player, or empty if no state exists.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain definition key
     * @return the player's chain state, or empty
     */
    @NotNull
    public Optional<QuestChainPlayerState> getChainStatus(@NotNull UUID playerUUID, @NotNull NamespacedKey chainKey) {
        return RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID)
                .flatMap(p -> p.getChainData().getChainState(chainKey));
    }

    /**
     * Persists a chain state asynchronously via the database executor. Snapshots the current
     * state values before submitting to avoid read/write races. Only clears the dirty flag
     * on successful write.
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

        var database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                QuestChainStateDAO.saveChainState(connection, playerUUID, snapshot);
                state.clearDirty();
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE,
                        "[QuestChainManager] Failed to save chain state for player " + playerUUID +
                                ", chain " + chainKey, e);
            }
        });
    }

    /**
     * Loads all chain states for a player from the database. This method runs on the database
     * executor thread and must not be called from the main thread.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return the list of loaded chain states
     */
    /**
     * Loads all chain states for a player from the database. This method runs on the database
     * executor thread and must not be called from the main thread.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return the list of loaded chain states
     */
    @NotNull
    public List<QuestChainPlayerState> loadChainStates(@NotNull Connection connection,
                                                        @NotNull UUID playerUUID) {
        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(connection, playerUUID);
        if (states.isEmpty()) {
            plugin().getLogger().log(Level.FINE,
                    "[QuestChainManager] No chain states found for player " + playerUUID);
        }
        return states;
    }
}
