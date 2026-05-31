package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.database.table.quest.QuestChainStateDAO;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.ChainCompletionSource;
import us.eunoians.mcrpg.event.quest.QuestChainAbandonEvent;
import us.eunoians.mcrpg.event.quest.QuestChainCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestChainFailEvent;
import us.eunoians.mcrpg.event.quest.QuestChainStartEvent;
import us.eunoians.mcrpg.event.quest.QuestChainStepAdvanceEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages quest chain lifecycle: starting, advancing, completing, restarting, resetting,
 * and persisting chain state. Delegates individual quest starts to
 * {@link QuestManager} and never creates {@link QuestInstance} objects directly.
 * <p>
 * All methods that mutate {@link QuestChainPlayerState} or fire events run on the main
 * Bukkit thread. All DAO read operations run on the database executor thread, with results
 * delivered back to the main thread via {@code Bukkit.getScheduler().runTask()}.
 */
public class QuestChainManager extends Manager<McRPG> {

    private final ChainPersistenceService persistenceService;
    private final ChainQuestStarter chainQuestStarter;

    /**
     * Creates a new chain manager.
     *
     * @param plugin the McRPG plugin instance
     */
    public QuestChainManager(@NotNull McRPG plugin) {
        super(plugin);
        this.persistenceService = new ChainPersistenceService(plugin);
        this.chainQuestStarter = new ChainQuestStarter(plugin);
    }

    /**
     * Returns the persistence service used by this manager. Exposed for unload tasks
     * that need to flush dirty states synchronously at logout.
     *
     * @return the chain persistence service
     */
    @NotNull
    public ChainPersistenceService getPersistenceService() {
        return persistenceService;
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
        var chainRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);
        Optional<QuestChainDefinition> definitionOpt = chainRegistry.get(chainKey);
        if (definitionOpt.isEmpty()) {
            plugin().getLogger().warning("[QuestChainManager] Attempted to start unknown chain '"
                    + chainKey + "' for player " + player.getUniqueId());
            return false;
        }
        QuestChainDefinition definition = definitionOpt.get();

        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            plugin().getLogger().warning("[QuestChainManager] Player " + player.getUniqueId()
                    + " not loaded, cannot start chain '" + chainKey + "'");
            return false;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();

        Optional<QuestChainPlayerState> existingState = chainData.getChainState(chainKey);
        if (existingState.isPresent()) {
            QuestChainPlayerState state = existingState.get();
            if (state.getState() == QuestChainState.ACTIVE) {
                return false;
            }
            if (state.getState().isTerminal() && definition.getRepeatMode() == QuestChainRepeatMode.ONCE) {
                return false;
            }
            if (state.getState().isTerminal() && state.getState().isRepeatEligible()) {
                return false;
            }
        }

        QuestChainStep firstStep = definition.getSteps().get(0);
        QuestChainPlayerState newState = QuestChainPlayerState.newActive(chainKey, firstStep.questKey());
        chainData.putChainState(newState);

        if (!chainQuestStarter.startStepQuest(player.getUniqueId(), definition, firstStep)) {
            chainData.removeChainState(chainKey);
            return false;
        }

        Bukkit.getPluginManager().callEvent(new QuestChainStartEvent(definition, player, firstStep));
        plugin().getLogger().fine("[QuestChainManager] Started chain '" + chainKey
                + "' for player " + player.getUniqueId() + " at step '" + firstStep.questKey() + "'");
        persistenceService.saveChainStateAsync(player.getUniqueId(), newState);
        return true;
    }

    /**
     * Advances a player's chain to the next step after the specified quest key was completed.
     * Uses the O(1) reverse index in {@link QuestChainPlayerData} to locate the chain.
     * No-op (returns {@code false}) if the completed quest is not managed by any active chain.
     *
     * @param playerUUID        the player UUID
     * @param completedQuestKey the quest definition key that was just completed
     * @return {@code true} if the chain advanced or completed; {@code false} if it was a no-op
     */
    public boolean advanceChain(@NotNull UUID playerUUID, @NotNull NamespacedKey completedQuestKey) {
        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            plugin().getLogger().warning("[QuestChainManager] Player " + playerUUID
                    + " not loaded, cannot advance chain for quest '" + completedQuestKey + "'");
            return false;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();

        Optional<NamespacedKey> chainKeyOpt = chainData.getChainKeyForCurrentQuest(completedQuestKey);
        if (chainKeyOpt.isEmpty()) {
            return false;
        }
        NamespacedKey chainKey = chainKeyOpt.get();

        Optional<QuestChainDefinition> definitionOpt = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN).get(chainKey);
        if (definitionOpt.isEmpty()) {
            plugin().getLogger().warning("[QuestChainManager] Chain '" + chainKey
                    + "' definition missing during advancement for player " + playerUUID
                    + " — state left as ACTIVE (inert)");
            return false;
        }
        QuestChainDefinition definition = definitionOpt.get();
        QuestChainPlayerState state = chainData.getChainState(chainKey).orElse(null);
        if (state == null) {
            plugin().getLogger().warning("[QuestChainManager] Chain '" + chainKey
                    + "' reverse index hit but state is null for player " + playerUUID
                    + " — possible index/state desync");
            return false;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        Optional<QuestChainStep> nextStepOpt = definition.getNextStep(completedQuestKey);

        if (nextStepOpt.isPresent()) {
            QuestChainStep nextStep = nextStepOpt.get();
            if (!chainQuestStarter.startStepQuest(playerUUID, definition, nextStep)) {
                // Record that the current step completed even though the next could not start.
                // Re-resolution on next login detects this via the completion log and retries.
                int failedCompletionNumber = state.getCompletionCount() + 1;
                long failedCompletedAt = plugin().getTimeProvider().now().toEpochMilli();
                state.recordAdvancement(completedQuestKey, failedCompletedAt, failedCompletionNumber);
                persistenceService.saveChainStateAsync(playerUUID, state);
                plugin().getLogger().warning("[QuestChainManager] Failed to start next step '" + nextStep.questKey()
                        + "' in chain '" + chainKey + "' for player " + playerUUID
                        + " — completed step recorded; re-resolution will retry on next login");
                return false;
            }

            int completionNumber = state.getCompletionCount() + 1;
            long completedAt = plugin().getTimeProvider().now().toEpochMilli();
            QuestChainStep completedStep = definition.getStep(completedQuestKey).orElse(
                    QuestChainStep.simple(completedQuestKey));
            state.advance(nextStep.questKey());
            state.recordAdvancement(completedQuestKey, completedAt, completionNumber);
            chainData.updateQuestKeyIndex(state);
            Bukkit.getPluginManager().callEvent(
                    new QuestChainStepAdvanceEvent(definition, player, playerUUID, completedStep, nextStep));
            plugin().getLogger().fine("[QuestChainManager] Advanced chain '" + chainKey
                    + "' for player " + playerUUID + " to step '" + nextStep.questKey() + "'");
            persistenceService.saveChainStateAsync(playerUUID, state);
            return true;

        } else {
            int completionNumber = state.getCompletionCount() + 1;
            long completedAt = plugin().getTimeProvider().now().toEpochMilli();
            state.recordAdvancement(completedQuestKey, completedAt, completionNumber);
            state.complete(completedAt);
            chainData.updateQuestKeyIndex(state);
            Bukkit.getPluginManager().callEvent(
                    new QuestChainCompleteEvent(definition, player, playerUUID, state.getCompletionCount(), ChainCompletionSource.ADVANCEMENT));
            plugin().getLogger().fine("[QuestChainManager] Completed chain '" + chainKey
                    + "' for player " + playerUUID + " (completion #" + state.getCompletionCount() + ")");
            persistenceService.saveChainStateAsync(playerUUID, state);
            return true;
        }
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
        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();
        Optional<QuestChainPlayerState> stateOpt = chainData.getChainState(chainKey);
        if (stateOpt.isEmpty() || stateOpt.get().getState() != QuestChainState.ACTIVE) {
            return false;
        }
        QuestChainPlayerState state = stateOpt.get();
        Optional<NamespacedKey> currentQuestKeyOpt = state.getCurrentQuestKey();
        if (currentQuestKeyOpt.isEmpty()) {
            return false;
        }
        return advanceChain(playerUUID, currentQuestKeyOpt.get());
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
        Optional<QuestChainDefinition> definitionOpt = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN).get(chainKey);
        if (definitionOpt.isEmpty()) {
            callback.accept(false);
            return;
        }
        QuestChainDefinition definition = definitionOpt.get();

        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            callback.accept(false);
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();
        Optional<QuestChainPlayerState> stateOpt = chainData.getChainState(chainKey);
        if (stateOpt.isEmpty()) {
            callback.accept(false);
            return;
        }

        persistenceService.prepareForFlush(playerUUID);
        Optional<NamespacedKey> oldQuestKey = stateOpt.get().getCurrentQuestKey();

        if (force) {
            QuestChainStep firstStep = definition.getSteps().get(0);
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && startStepForPlayer(definition, firstStep, player, stateOpt.get(), chainData)) {
                oldQuestKey.ifPresent(key -> cancelQuestByKey(playerUUID, key));
                persistenceService.saveChainStateAsync(playerUUID, stateOpt.get());
                callback.accept(true);
            } else {
                callback.accept(false);
            }
            return;
        }

        QuestChainPlayerState state = stateOpt.get();
        var database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            Set<NamespacedKey> completedKeys;
            try (Connection connection = database.getConnection()) {
                Set<String> rawKeys = QuestChainCompletionLogDAO.getCompletedQuestKeys(
                        connection, playerUUID, chainKey.toString());
                completedKeys = rawKeys.stream()
                        .map(NamespacedKey::fromString)
                        .filter(k -> k != null)
                        .collect(Collectors.toSet());
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE,
                        "[QuestChainManager] Failed to read completion log for restartChain, player "
                                + playerUUID + ", chain " + chainKey, e);
                Bukkit.getScheduler().runTask(plugin(), () -> callback.accept(false));
                return;
            }
            Set<NamespacedKey> finalCompletedKeys = completedKeys;
            Bukkit.getScheduler().runTask(plugin(), () -> {
                Optional<QuestChainStep> firstUncompleted = findFirstUncompletedStep(definition, finalCompletedKeys);
                Player player = Bukkit.getPlayer(playerUUID);
                if (firstUncompleted.isPresent()) {
                    boolean started = player != null && startStepForPlayer(definition, firstUncompleted.get(), player, state, chainData);
                    if (started) {
                        oldQuestKey.ifPresent(key -> cancelQuestByKey(playerUUID, key));
                        persistenceService.saveChainStateAsync(playerUUID, state);
                    }
                    callback.accept(started);
                } else {
                    oldQuestKey.ifPresent(key -> cancelQuestByKey(playerUUID, key));
                    state.complete(plugin().getTimeProvider().now().toEpochMilli());
                    chainData.updateQuestKeyIndex(state);
                    Bukkit.getPluginManager().callEvent(
                            new QuestChainCompleteEvent(definition, player, playerUUID, state.getCompletionCount(), ChainCompletionSource.RESTART));
                    persistenceService.saveChainStateAsync(playerUUID, state);
                    callback.accept(true);
                }
            });
        });
    }

    /**
     * Hard-resets a player's chain state — clears chain state and completion log entries.
     * The player experiences the chain as if they have never started it.
     * The result is delivered asynchronously via {@code callback} on the main thread
     * once the DB delete has completed.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain definition key
     * @param callback   invoked on the main thread with {@code true} if the reset succeeded
     */
    public void resetChain(@NotNull UUID playerUUID, @NotNull NamespacedKey chainKey,
                           @NotNull Consumer<Boolean> callback) {
        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            callback.accept(false);
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();
        Optional<QuestChainPlayerState> stateOpt = chainData.getChainState(chainKey);
        if (stateOpt.isEmpty()) {
            callback.accept(false);
            return;
        }
        cancelActiveChainQuestIfExists(playerUUID, stateOpt.get());
        // Do not remove in-memory state yet — wait for DB delete to succeed
        // so a failed delete doesn't leave the player with missing state.

        persistenceService.prepareForFlush(playerUUID);

        var database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            boolean success = false;
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = new ArrayList<>();
                statements.addAll(QuestChainStateDAO.deleteChainState(connection, playerUUID, chainKey));
                statements.addAll(QuestChainCompletionLogDAO.deleteForChain(connection, playerUUID, chainKey.toString()));
                new FailSafeTransaction(connection, statements).executeTransaction();
                success = true;
            } catch (Exception e) {
                plugin().getLogger().log(Level.SEVERE,
                        "[QuestChainManager] Failed to reset chain '" + chainKey
                                + "' for player " + playerUUID, e);
            }
            boolean finalSuccess = success;
            Bukkit.getScheduler().runTask(plugin(), () -> {
                if (finalSuccess) {
                    chainData.removeChainState(chainKey);
                    plugin().getLogger().fine("[QuestChainManager] Reset chain '" + chainKey
                            + "' for player " + playerUUID);
                }
                callback.accept(finalSuccess);
            });
        });
    }

    /**
     * Handles a cancelled quest that may belong to an active chain. If the cancelled quest is
     * the current step of an ACTIVE chain, transitions the chain to ABANDONED.
     *
     * @param playerUUID        the player UUID
     * @param cancelledQuestKey the cancelled quest's definition key
     */
    public void handleQuestCancelled(@NotNull UUID playerUUID, @NotNull NamespacedKey cancelledQuestKey) {
        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        QuestChainPlayerData chainData = mcRPGPlayerOpt.get().getChainData();
        Optional<NamespacedKey> chainKeyOpt = chainData.getChainKeyForCurrentQuest(cancelledQuestKey);
        if (chainKeyOpt.isEmpty()) {
            return;
        }
        NamespacedKey chainKey = chainKeyOpt.get();
        Optional<QuestChainDefinition> definitionOpt = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN).get(chainKey);
        chainData.getChainState(chainKey).ifPresent(state -> {
            state.abandon();
            chainData.updateQuestKeyIndex(state);
            plugin().getLogger().fine("[QuestChainManager] Chain '" + chainKey
                    + "' abandoned for player " + playerUUID
                    + " — quest '" + cancelledQuestKey + "' was cancelled");
            persistenceService.saveChainStateAsync(playerUUID, state);
            definitionOpt.ifPresent(definition -> Bukkit.getPluginManager().callEvent(
                    new QuestChainAbandonEvent(definition, Bukkit.getPlayer(playerUUID), playerUUID)));
        });
    }

    /**
     * Handles an expired quest that may belong to an active chain. Applies the chain's
     * configured {@code on-quest-expire} behavior.
     *
     * @param playerUUID      the player UUID
     * @param expiredQuestKey the expired quest's definition key
     */
    public void handleQuestExpired(@NotNull UUID playerUUID, @NotNull NamespacedKey expiredQuestKey) {
        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        QuestChainPlayerData chainData = mcRPGPlayerOpt.get().getChainData();
        Optional<NamespacedKey> chainKeyOpt = chainData.getChainKeyForCurrentQuest(expiredQuestKey);
        if (chainKeyOpt.isEmpty()) {
            return;
        }
        NamespacedKey chainKey = chainKeyOpt.get();
        Optional<QuestChainDefinition> definitionOpt = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN).get(chainKey);
        if (definitionOpt.isEmpty()) {
            plugin().getLogger().warning("[QuestChainManager] Chain '" + chainKey
                    + "' definition missing during expiry handling for player " + playerUUID);
            return;
        }
        QuestChainDefinition definition = definitionOpt.get();
        chainData.getChainState(chainKey).ifPresent(state -> {
            String expireAction = definition.getStep(expiredQuestKey)
                    .map(QuestChainStep::onQuestExpire)
                    .orElse("fail-chain");
            if (!"fail-chain".equals(expireAction)) {
                plugin().getLogger().warning("[QuestChainManager] Chain '" + chainKey
                        + "' step '" + expiredQuestKey + "' has unsupported on-quest-expire value '"
                        + expireAction + "' — defaulting to fail-chain");
            }
            state.fail();
            chainData.updateQuestKeyIndex(state);
            plugin().getLogger().fine("[QuestChainManager] Chain '" + chainKey
                    + "' failed for player " + playerUUID + " — quest '" + expiredQuestKey
                    + "' expired (on-quest-expire: fail-chain)");
            persistenceService.saveChainStateAsync(playerUUID, state);
            Bukkit.getPluginManager().callEvent(
                    new QuestChainFailEvent(definition, Bukkit.getPlayer(playerUUID), playerUUID));
        });
    }

    /**
     * Re-resolves chain state for a player on login. Handles any definition changes that
     * occurred while the player was offline (removed steps, renamed quest keys, etc.).
     * Runs a single batched DB read for all chains, then returns to the main thread for
     * state mutations. Once resolution completes, {@code onComplete} is invoked on the
     * main thread so callers can sequence login-triggered chain starts after resolution.
     *
     * @param playerUUID the player UUID
     * @param onComplete callback invoked on the main thread after re-resolution finishes
     *                   (including the fast path when no resolution is needed)
     */
    public void reResolveOnLogin(@NotNull UUID playerUUID, @NotNull Runnable onComplete) {
        Optional<McRPGPlayer> mcRPGPlayerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            onComplete.run();
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        QuestChainPlayerData chainData = mcRPGPlayer.getChainData();

        // Include all active chains — applyReResolution will skip ones that are genuinely
        // still in-progress (current step in definition and not yet completed). Including
        // all of them requires a completion-log DB read on every login with an active chain,
        // but that is necessary to detect stuck states where startStepQuest failed after
        // the previous step completed.
        List<QuestChainPlayerState> chainsNeedingReResolution = chainData.getAllStates().stream()
                .filter(state -> state.getState() == QuestChainState.ACTIVE)
                .toList();

        if (chainsNeedingReResolution.isEmpty()) {
            onComplete.run();
            return;
        }

        var database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            Map<NamespacedKey, Set<NamespacedKey>> completionsByChain;
            try (Connection connection = database.getConnection()) {
                completionsByChain = QuestChainCompletionLogDAO.getAllCompletedQuestKeysByChain(connection, playerUUID);
            } catch (SQLException e) {
                plugin().getLogger().log(Level.SEVERE,
                        "[QuestChainManager] Failed to read completion logs for reResolveOnLogin, player "
                                + playerUUID, e);
                Bukkit.getScheduler().runTask(plugin(), onComplete);
                return;
            }
            Map<NamespacedKey, Set<NamespacedKey>> finalCompletionsByChain = completionsByChain;
            Bukkit.getScheduler().runTask(plugin(), () -> {
                // Guard against fast disconnects: if the player unloaded while the DB read was
                // in-flight, skip re-resolution but still invoke onComplete so callers can
                // sequence downstream work (e.g. auto-start triggers).
                Optional<McRPGPlayer> stillLoadedOpt = RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
                if (stillLoadedOpt.isEmpty()) {
                    onComplete.run();
                    return;
                }
                applyReResolution(playerUUID, stillLoadedOpt.get().getChainData(), chainsNeedingReResolution, finalCompletionsByChain);
                onComplete.run();
            });
        });
    }

    /**
     * Re-resolves chain state for a player on login without a completion callback.
     * Equivalent to {@link #reResolveOnLogin(UUID, Runnable)} with a no-op callback.
     * Used by {@link #reResolveOnReload()} where trigger evaluation is not needed.
     *
     * @param playerUUID the player UUID
     */
    public void reResolveOnLogin(@NotNull UUID playerUUID) {
        reResolveOnLogin(playerUUID, () -> {
        });
    }

    /**
     * Applies the result of a batched completion-log read back to chain states on the main thread.
     * Called both from {@link #reResolveOnLogin(UUID)} and {@link #reResolveOnReload()}.
     *
     * @param playerUUID               the player UUID
     * @param chainData                the player's chain data container
     * @param chainsNeedingReResolution active chains whose state needs re-resolution
     * @param completionsByChain       map of chain key → set of completed quest keys
     */
    private void applyReResolution(@NotNull UUID playerUUID,
                                    @NotNull QuestChainPlayerData chainData,
                                    @NotNull List<QuestChainPlayerState> chainsNeedingReResolution,
                                    @NotNull Map<NamespacedKey, Set<NamespacedKey>> completionsByChain) {
        for (QuestChainPlayerState state : chainsNeedingReResolution) {
            NamespacedKey chainKey = state.getChainKey();
            if (state.getState() != QuestChainState.ACTIVE) {
                plugin().getLogger().fine("[QuestChainManager] Skipping re-resolution for chain '"
                        + chainKey + "' for player " + playerUUID + ": state is now " + state.getState());
                continue;
            }
            Optional<QuestChainDefinition> definitionOpt = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_CHAIN).get(chainKey);
            if (definitionOpt.isEmpty()) {
                plugin().getLogger().warning("[QuestChainManager] Chain '" + chainKey
                        + "' definition not found during login re-resolution for player "
                        + playerUUID + " — state left as ACTIVE (inert)");
                continue;
            }
            QuestChainDefinition definition = definitionOpt.get();
            Optional<NamespacedKey> currentQuestOpt = state.getCurrentQuestKey();
            Set<NamespacedKey> completedKeys = completionsByChain.getOrDefault(chainKey, Set.of());
            // Skip only if the current step still exists in the definition AND has not been
            // completed yet. If the step is completed but the next couldn't start (stuck state),
            // fall through to findFirstUncompletedStep so it can retry or complete the chain.
            if (currentQuestOpt.isPresent() && definition.getStep(currentQuestOpt.get()).isPresent()
                    && !completedKeys.contains(currentQuestOpt.get())) {
                continue;
            }
            Optional<QuestChainStep> uncompletedStep = findFirstUncompletedStep(definition, completedKeys);

            if (uncompletedStep.isPresent()) {
                QuestChainStep step = uncompletedStep.get();
                if (!chainQuestStarter.startStepQuest(playerUUID, definition, step)) {
                    plugin().getLogger().warning("[QuestChainManager] Failed to start re-resolved step '"
                            + step.questKey() + "' in chain '" + chainKey + "' for player " + playerUUID);
                    continue;
                }
                state.resetToStep(step.questKey());
                plugin().getLogger().fine("[QuestChainManager] Re-resolved chain '" + chainKey
                        + "' for player " + playerUUID + ": advanced to step '" + step.questKey()
                        + "' (previous step removed)");
                persistenceService.saveChainStateAsync(playerUUID, state);
            } else {
                state.complete(plugin().getTimeProvider().now().toEpochMilli());
                plugin().getLogger().fine("[QuestChainManager] Re-resolved chain '" + chainKey
                        + "' for player " + playerUUID + ": all steps completed, marking COMPLETED");
                Bukkit.getPluginManager().callEvent(
                        new QuestChainCompleteEvent(definition, Bukkit.getPlayer(playerUUID), playerUUID, state.getCompletionCount(), ChainCompletionSource.RE_RESOLUTION));
                persistenceService.saveChainStateAsync(playerUUID, state);
            }
        }
        chainData.rebuildQuestKeyIndex();
    }

    /**
     * Re-resolves chain state for all online players after a reload.
     * Uses the no-callback overload since reload doesn't need to sequence trigger evaluation.
     * Delegates to {@link #reResolveOnLogin(UUID)} for each online player.
     */
    public void reResolveOnReload() {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getAllPlayers()
                .forEach(p -> reResolveOnLogin(p.getUUID()));
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
     * Persists a chain state asynchronously. Delegates to {@link ChainPersistenceService}
     * which serializes writes per-player and clears the dirty flag on the main thread after
     * a successful write.
     *
     * @param playerUUID the player UUID
     * @param state      the chain state to persist
     */
    public void saveChainStateAsync(@NotNull UUID playerUUID, @NotNull QuestChainPlayerState state) {
        persistenceService.saveChainStateAsync(playerUUID, state);
    }

    /**
     * Flushes all dirty chain states for a player synchronously and then cleans up all
     * per-player persistence tracking. Must be called from the database executor thread.
     * <p>
     * Call sequence: {@link ChainPersistenceService#prepareForFlush(UUID)} → flush → cleanup.
     *
     * @param playerUUID the player UUID to unload
     * @param connection the open database connection to use for the flush
     * @param chainData  the player's chain data container
     */
    public void unloadPlayer(@NotNull UUID playerUUID,
                             @NotNull Connection connection,
                             @NotNull QuestChainPlayerData chainData) {
        persistenceService.prepareForFlush(playerUUID);
        persistenceService.flushChainStatesSync(connection, playerUUID, chainData);
        persistenceService.cleanupPlayer(playerUUID);
    }

    /**
     * Loads all chain states for a player from the database. This method runs on the database
     * executor thread and must not be called from the main thread.
     *
     * @param connection the database connection
     * @param playerUUID the player UUID
     * @return the list of loaded chain states
     * @throws SQLException if a database error occurs
     */
    @NotNull
    public List<QuestChainPlayerState> loadChainStates(@NotNull Connection connection,
                                                        @NotNull UUID playerUUID) throws SQLException {
        List<QuestChainPlayerState> states = QuestChainStateDAO.loadAllChainStates(connection, playerUUID);
        if (states.isEmpty()) {
            plugin().getLogger().fine("[QuestChainManager] No chain states found for player " + playerUUID);
        }
        return states;
    }

    /**
     * Finds the first step in the chain whose quest key is not in the completed set.
     *
     * @param definition         the chain definition
     * @param completedQuestKeys quest keys already completed by this player for this chain
     * @return the first uncompleted step, or empty if all steps are completed
     */
    @NotNull
    private Optional<QuestChainStep> findFirstUncompletedStep(@NotNull QuestChainDefinition definition,
                                                               @NotNull Set<NamespacedKey> completedQuestKeys) {
        return definition.getSteps().stream()
                .filter(step -> !completedQuestKeys.contains(step.questKey()))
                .findFirst();
    }

    /**
     * Cancels the active quest instance for a chain state's current quest, if one exists in
     * the {@link QuestManager}. Used before restarting or resetting a chain.
     *
     * @param playerUUID the player UUID
     * @param state      the chain state whose current quest may need cancellation
     */
    /**
     * Cancels the active quest instance for a specific quest key, if one exists for the player.
     *
     * @param playerUUID the player UUID
     * @param questKey   the quest key to cancel
     */
    private void cancelQuestByKey(@NotNull UUID playerUUID, @NotNull NamespacedKey questKey) {
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        questManager.getActiveQuestsForPlayer(playerUUID).stream()
                .filter(instance -> instance.getQuestKey().equals(questKey))
                .findFirst()
                .ifPresent(QuestInstance::cancel);
    }

    /**
     * Cancels the active quest instance for the chain's current step, if one exists.
     * Used by hard reset operations where the old quest must be cancelled unconditionally.
     *
     * @param playerUUID the player UUID
     * @param state      the chain state whose current quest key to cancel
     */
    private void cancelActiveChainQuestIfExists(@NotNull UUID playerUUID,
                                                 @NotNull QuestChainPlayerState state) {
        state.getCurrentQuestKey().ifPresent(questKey -> cancelQuestByKey(playerUUID, questKey));
    }

    /**
     * Starts the given step's quest for the player and resets the chain state to that step.
     * Delegates quest resolution and start to {@link ChainQuestStarter}.
     *
     * @param definition the chain definition
     * @param step       the step to start
     * @param player     the player
     * @param state      the chain state to reset on success
     * @return {@code true} if the quest started successfully
     */
    /**
     * Starts the quest for the given chain step. On success, resets the state to the step's
     * quest key and updates the reverse quest-key index so O(1) lookup from quest→chain works.
     *
     * @param definition the chain definition
     * @param step       the step to start
     * @param player     the online player
     * @param state      the chain state to update
     * @param chainData  the player's chain data (for index update)
     * @return {@code true} if the quest was started successfully
     */
    private boolean startStepForPlayer(@NotNull QuestChainDefinition definition,
                                        @NotNull QuestChainStep step,
                                        @NotNull Player player,
                                        @NotNull QuestChainPlayerState state,
                                        @NotNull QuestChainPlayerData chainData) {
        if (chainQuestStarter.startStepQuest(player.getUniqueId(), definition, step)) {
            state.resetToStep(step.questKey());
            chainData.updateQuestKeyIndex(state);
            return true;
        }
        return false;
    }

}
