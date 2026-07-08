package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.table.quest.chain.QuestChainCompletionLogDAO;
import us.eunoians.mcrpg.event.quest.chain.ChainCompletionSource;
import us.eunoians.mcrpg.event.quest.chain.QuestChainCompleteEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainFailEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainRestartEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainStepAdvanceEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainStepRetryEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles quest-step expiration behaviors for quest chains. Owns the logic for
 * fail, retry, restart-chain, and skip expiration actions, plus async completion
 * log operations used by those actions.
 * <p>
 * Collaborator of {@link QuestChainManager}; injected at construction. Shares
 * the same {@link java.util.concurrent.ConcurrentHashMap} reference for retry
 * counters so mutations are visible to both objects.
 */
class ChainExpirationHandler {

    /**
     * Composite key for tracking per-step retry counts. In-memory only — server
     * restart resets counters to prevent permanent lockout from misconfigured durations.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain key
     * @param questKey   the step quest key
     */
    record RetryKey(@NotNull UUID playerUUID,
                    @NotNull NamespacedKey chainKey,
                    @NotNull NamespacedKey questKey) {
    }

    private final McRPG plugin;
    private final ChainPersistenceService persistenceService;
    private final ChainQuestStarter chainQuestStarter;
    private final Map<RetryKey, Integer> retryCounters;

    /**
     * Creates a new expiration handler collaborator.
     *
     * @param plugin             the McRPG plugin instance
     * @param persistenceService the persistence service for async state saves
     * @param chainQuestStarter  the quest starter for re-launching step quests
     * @param retryCounters      the shared retry counter map (same reference held by the manager)
     */
    ChainExpirationHandler(@NotNull McRPG plugin,
                           @NotNull ChainPersistenceService persistenceService,
                           @NotNull ChainQuestStarter chainQuestStarter,
                           @NotNull Map<RetryKey, Integer> retryCounters) {
        this.plugin = plugin;
        this.persistenceService = persistenceService;
        this.chainQuestStarter = chainQuestStarter;
        this.retryCounters = retryCounters;
    }

    /**
     * Handles the {@code fail-chain} expiration behavior. Sets the chain to FAILED
     * and fires {@link QuestChainFailEvent}.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain key
     * @param definition the chain definition
     * @param state      the player's chain state
     * @param chainData  the player's chain data container
     */
    void handleExpireFail(@NotNull UUID playerUUID,
                          @NotNull NamespacedKey chainKey,
                          @NotNull QuestChainDefinition definition,
                          @NotNull QuestChainPlayerState state,
                          @NotNull QuestChainPlayerData chainData) {
        state.fail();
        chainData.updateQuestKeyIndex(state);
        clearRetryCountersForChain(playerUUID, chainKey);
        plugin.getLogger().fine("[QuestChainManager] Chain '" + chainKey
                + "' failed for player " + playerUUID + " (on-quest-expire: fail-chain)");
        persistenceService.saveChainStateAsync(playerUUID, state);
        Bukkit.getPluginManager().callEvent(
                new QuestChainFailEvent(definition, Bukkit.getPlayer(playerUUID), playerUUID));
    }

    /**
     * Handles the {@code retry} expiration behavior. Re-starts the same step quest if
     * the retry limit has not been exhausted. Falls back to fail-chain on limit exhaustion.
     *
     * @param playerUUID      the player UUID
     * @param chainKey        the chain key
     * @param definition      the chain definition
     * @param state           the player's chain state
     * @param chainData       the player's chain data container
     * @param expiredQuestKey the expired quest's definition key
     */
    void handleExpireRetry(@NotNull UUID playerUUID,
                           @NotNull NamespacedKey chainKey,
                           @NotNull QuestChainDefinition definition,
                           @NotNull QuestChainPlayerState state,
                           @NotNull QuestChainPlayerData chainData,
                           @NotNull NamespacedKey expiredQuestKey) {
        QuestChainStep step = definition.getStep(expiredQuestKey).orElse(null);
        if (step == null) {
            handleExpireFail(playerUUID, chainKey, definition, state, chainData);
            return;
        }
        RetryKey retryKey = new RetryKey(playerUUID, chainKey, expiredQuestKey);
        int maxRetries = step.maxRetries();
        int used = retryCounters.getOrDefault(retryKey, 0);

        if (maxRetries >= 0 && used >= maxRetries) {
            retryCounters.remove(retryKey);
            handleExpireFail(playerUUID, chainKey, definition, state, chainData);
            return;
        }

        retryCounters.put(retryKey, used + 1);

        if (!chainQuestStarter.startStepQuest(playerUUID, definition, step)) {
            handleExpireFail(playerUUID, chainKey, definition, state, chainData);
            return;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        Bukkit.getPluginManager().callEvent(
                new QuestChainStepRetryEvent(definition, player, playerUUID, step, used + 1, maxRetries));
        plugin.getLogger().info("[QuestChainManager] Retrying step '"
                + expiredQuestKey + "' for chain '" + chainKey
                + "' (attempt " + (used + 2) + "/" + (maxRetries < 0 ? "unlimited" : maxRetries + 1) + ")");
    }

    /**
     * Handles the {@code restart-chain} expiration behavior. Resets the chain to step 1,
     * clears the completion log, and starts the first step quest.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain key
     * @param definition the chain definition
     * @param state      the player's chain state
     * @param chainData  the player's chain data container
     */
    void handleExpireRestartChain(@NotNull UUID playerUUID,
                                  @NotNull NamespacedKey chainKey,
                                  @NotNull QuestChainDefinition definition,
                                  @NotNull QuestChainPlayerState state,
                                  @NotNull QuestChainPlayerData chainData) {
        clearRetryCountersForChain(playerUUID, chainKey);
        QuestChainStep firstStep = definition.getSteps().getFirst();
        state.resetToStep(firstStep.questKey());
        chainData.updateQuestKeyIndex(state);

        if (!chainQuestStarter.startStepQuest(playerUUID, definition, firstStep)) {
            state.fail();
            chainData.updateQuestKeyIndex(state);
            persistenceService.saveChainStateAsync(playerUUID, state);
            return;
        }

        Player player = Bukkit.getPlayer(playerUUID);
        Bukkit.getPluginManager().callEvent(
                new QuestChainRestartEvent(definition, player, playerUUID, QuestChainRestartEvent.RestartReason.QUEST_EXPIRE_RESTART_CHAIN));
        persistenceService.saveChainStateAsync(playerUUID, state);
        clearCompletionLogAsync(playerUUID, chainKey);
        plugin.getLogger().fine("[QuestChainManager] Restarted chain '" + chainKey
                + "' from step 1 for player " + playerUUID + " (on-quest-expire: restart-chain)");
    }

    /**
     * Handles the {@code skip} expiration behavior. Logs the skipped step in the completion
     * log and advances to the next step. If this was the last step, completes the chain.
     *
     * @param playerUUID      the player UUID
     * @param chainKey        the chain key
     * @param definition      the chain definition
     * @param state           the player's chain state
     * @param chainData       the player's chain data container
     * @param skippedQuestKey the skipped quest's definition key
     */
    void handleExpireSkip(@NotNull UUID playerUUID,
                          @NotNull NamespacedKey chainKey,
                          @NotNull QuestChainDefinition definition,
                          @NotNull QuestChainPlayerState state,
                          @NotNull QuestChainPlayerData chainData,
                          @NotNull NamespacedKey skippedQuestKey) {
        logStepSkippedAsync(playerUUID, chainKey, skippedQuestKey, state.getCompletionCount() + 1);

        Optional<QuestChainStep> nextStep = definition.getNextStep(skippedQuestKey);
        Player player = Bukkit.getPlayer(playerUUID);

        if (nextStep.isEmpty()) {
            state.complete(plugin.getTimeProvider().now());
            chainData.updateQuestKeyIndex(state);
            Bukkit.getPluginManager().callEvent(
                    new QuestChainCompleteEvent(definition, player, playerUUID,
                            state.getCompletionCount(), ChainCompletionSource.ADVANCEMENT));
            persistenceService.saveChainStateAsync(playerUUID, state);
            plugin.getLogger().fine("[QuestChainManager] Chain '" + chainKey
                    + "' completed for player " + playerUUID + " (last step skipped)");
            return;
        }

        if (!chainQuestStarter.startStepQuest(playerUUID, definition, nextStep.get())) {
            state.fail();
            chainData.updateQuestKeyIndex(state);
            persistenceService.saveChainStateAsync(playerUUID, state);
            return;
        }

        state.advance(nextStep.get().questKey());
        chainData.updateQuestKeyIndex(state);
        if (player != null) {
            QuestChainStep completedStep = definition.getStep(skippedQuestKey)
                    .orElse(QuestChainStep.simple(skippedQuestKey));
            Bukkit.getPluginManager().callEvent(
                    new QuestChainStepAdvanceEvent(definition, player, playerUUID, completedStep, nextStep.get()));
        }
        persistenceService.saveChainStateAsync(playerUUID, state);
        plugin.getLogger().fine("[QuestChainManager] Skipped step '" + skippedQuestKey
                + "' in chain '" + chainKey + "' for player " + playerUUID + ", advanced to '"
                + nextStep.get().questKey() + "'");
    }

    /**
     * Clears retry counters for all steps in a chain for a player.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain key
     */
    void clearRetryCountersForChain(@NotNull UUID playerUUID, @NotNull NamespacedKey chainKey) {
        retryCounters.entrySet().removeIf(e ->
                e.getKey().playerUUID().equals(playerUUID) && e.getKey().chainKey().equals(chainKey));
    }

    /**
     * Asynchronously deletes all completion log entries for a chain.
     *
     * @param playerUUID the player UUID
     * @param chainKey   the chain key
     */
    private void clearCompletionLogAsync(@NotNull UUID playerUUID, @NotNull NamespacedKey chainKey) {
        var database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = QuestChainCompletionLogDAO.deleteForChain(
                        connection, playerUUID, chainKey.toString());
                new FailSafeTransaction(connection, statements).executeTransaction();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[QuestChainManager] Failed to clear completion log for chain '"
                                + chainKey + "', player " + playerUUID, e);
            }
        });
    }

    /**
     * Asynchronously logs a skipped step in the completion log.
     *
     * @param playerUUID       the player UUID
     * @param chainKey         the chain key
     * @param questKey         the skipped quest key
     * @param completionNumber the current completion number
     */
    private void logStepSkippedAsync(@NotNull UUID playerUUID,
                                     @NotNull NamespacedKey chainKey,
                                     @NotNull NamespacedKey questKey,
                                     int completionNumber) {
        var database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                List<PreparedStatement> statements = QuestChainCompletionLogDAO.logSkip(
                        connection, playerUUID, chainKey.toString(), questKey.toString(),
                        plugin.getTimeProvider().now(), completionNumber);
                new FailSafeTransaction(connection, statements).executeTransaction();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[QuestChainManager] Failed to log skipped step '" + questKey
                                + "' for chain '" + chainKey + "', player " + playerUUID, e);
            }
        });
    }
}
