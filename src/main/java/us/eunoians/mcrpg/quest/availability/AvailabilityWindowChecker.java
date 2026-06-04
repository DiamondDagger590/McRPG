package us.eunoians.mcrpg.quest.availability;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.RepeatableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestChainExpireEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerData;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.QuestChainState;
import us.eunoians.mcrpg.quest.chain.availability.AvailabilityConfig;
import us.eunoians.mcrpg.quest.chain.availability.WindowClosePolicy;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Scheduled task that periodically checks availability windows for quest chains and
 * standalone quest definitions. When a window transitions from available to unavailable,
 * the configured {@link WindowClosePolicy} is applied to any active instances.
 * <p>
 * The checker maintains per-key previous-availability snapshots to detect open-to-close
 * transitions. Only transitions trigger policy enforcement; steady-state checks are no-ops.
 * <p>
 * Grace period tasks are tracked by Bukkit task ID so they can be cancelled if the window
 * re-opens before the grace period expires (e.g., overlapping windows or config reload).
 * <p>
 * Eviction: {@code previousChainAvailability}, {@code previousQuestAvailability}, and
 * {@code activeGraceTasks} are bounded by the number of registered definitions with
 * availability configs. Entries are rebuilt from the registry on each check interval
 * via {@link #snapshotCurrentAvailability()}, so stale keys from unregistered definitions
 * are naturally evicted.
 */
public final class AvailabilityWindowChecker extends RepeatableCoreTask {

    private final McRPG plugin;
    private final Map<NamespacedKey, Boolean> previousChainAvailability;
    private final Map<NamespacedKey, Boolean> previousQuestAvailability;
    private final Map<NamespacedKey, Integer> activeGraceTasks;

    /**
     * Constructs a new availability window checker.
     *
     * @param plugin               the McRPG plugin instance
     * @param checkIntervalSeconds the interval in seconds between availability checks
     */
    public AvailabilityWindowChecker(@NotNull McRPG plugin, double checkIntervalSeconds) {
        super(plugin, checkIntervalSeconds, checkIntervalSeconds);
        this.plugin = plugin;
        this.previousChainAvailability = new HashMap<>();
        this.previousQuestAvailability = new HashMap<>();
        this.activeGraceTasks = new HashMap<>();
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    /**
     * Checks whether the given chain definition is currently within an active availability
     * window. If the chain has no availability config, it is always considered available.
     *
     * @param chainKey the chain definition key to check
     * @return {@code true} if the chain is currently available or has no availability restrictions
     */
    public boolean isChainAvailable(@NotNull NamespacedKey chainKey) {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        Optional<QuestChainDefinition> definitionOpt = chainRegistry.get(chainKey);
        if (definitionOpt.isEmpty()) {
            return false;
        }
        QuestChainDefinition definition = definitionOpt.get();
        return definition.getAvailabilityConfig()
                .map(config -> config.isCurrentlyAvailable(currentTimeInZone(config)))
                .orElse(true);
    }

    /**
     * Checks whether the given quest definition is currently within an active availability
     * window. If the quest has no availability config, it is always considered available.
     * <p>
     * Note: standalone quest availability is a forward-looking extension point. Currently,
     * {@link QuestDefinition} does not carry an {@link AvailabilityConfig}. This method
     * always returns {@code true} until that field is added.
     *
     * @param questKey the quest definition key to check
     * @return {@code true} if the quest is currently available or has no availability restrictions
     */
    public boolean isQuestAvailable(@NotNull NamespacedKey questKey) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        Optional<QuestDefinition> definitionOpt = definitionRegistry.get(questKey);
        if (definitionOpt.isEmpty()) {
            return false;
        }
        // QuestDefinition does not yet carry AvailabilityConfig; always available.
        return true;
    }

    /**
     * Called when the initial delay completes. Reconciles the current availability state
     * against any active chain or quest instances that may have become unavailable while
     * the server was offline.
     */
    @Override
    protected void onDelayComplete() {
        reconcileOnStartup();
    }

    /**
     * No-op. Interval start does not require any action.
     */
    @Override
    protected void onIntervalStart() {
        // No-op
    }

    /**
     * Called each time a full check interval completes. Checks all chains and quests
     * for availability window transitions and applies close policies as needed.
     */
    @Override
    protected void onIntervalComplete() {
        checkAllChains();
        checkAllQuests();
        snapshotCurrentAvailability();
    }

    /**
     * No-op. Pausing does not require cleanup.
     */
    @Override
    protected void onIntervalPause() {
        // No-op
    }

    /**
     * No-op. Resuming does not require re-initialization.
     */
    @Override
    protected void onIntervalResume() {
        // No-op
    }

    /**
     * Reconciles availability state on first startup. For any chain or quest definition
     * that has an availability config and is currently unavailable, applies the close
     * policy to expire or cancel active instances that may have been started before the
     * window closed (e.g., server was offline during the transition).
     */
    private void reconcileOnStartup() {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);

        for (QuestChainDefinition definition : chainRegistry.allChains()) {
            Optional<AvailabilityConfig> configOpt = definition.getAvailabilityConfig();
            if (configOpt.isEmpty()) {
                continue;
            }
            AvailabilityConfig config = configOpt.get();
            boolean available = config.isCurrentlyAvailable(currentTimeInZone(config));
            previousChainAvailability.put(definition.getChainKey(), available);

            if (!available) {
                applyChainClosePolicy(definition.getChainKey(), config);
            }
        }

        // Quest definitions do not yet have AvailabilityConfig, so no reconciliation needed.

        plugin.getLogger().info("[AvailabilityWindowChecker] Startup reconciliation complete. "
                + "Tracking " + previousChainAvailability.size() + " chain availability window(s).");
    }

    /**
     * Iterates all registered chain definitions that have an {@link AvailabilityConfig} and
     * detects transitions from available to unavailable. When a close transition is detected,
     * the configured {@link WindowClosePolicy} is applied.
     */
    private void checkAllChains() {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);

        for (QuestChainDefinition definition : chainRegistry.allChains()) {
            Optional<AvailabilityConfig> configOpt = definition.getAvailabilityConfig();
            if (configOpt.isEmpty()) {
                continue;
            }
            AvailabilityConfig config = configOpt.get();
            NamespacedKey chainKey = definition.getChainKey();
            boolean nowAvailable = config.isCurrentlyAvailable(currentTimeInZone(config));
            boolean wasAvailable = previousChainAvailability.getOrDefault(chainKey, true);

            if (wasAvailable && !nowAvailable) {
                plugin.getLogger().info("[AvailabilityWindowChecker] Chain '" + chainKey
                        + "' window closed. Applying close policy: " + config.onWindowClose());
                applyChainClosePolicy(chainKey, config);
            } else if (!wasAvailable && nowAvailable) {
                cancelGraceTask(chainKey);
                plugin.getLogger().info("[AvailabilityWindowChecker] Chain '" + chainKey
                        + "' window opened.");
            }

            previousChainAvailability.put(chainKey, nowAvailable);
        }
    }

    /**
     * Iterates all registered quest definitions that have an {@link AvailabilityConfig} and
     * detects transitions from available to unavailable. When a close transition is detected,
     * the configured {@link WindowClosePolicy} is applied.
     * <p>
     * Currently a no-op because {@link QuestDefinition} does not yet carry an
     * {@link AvailabilityConfig}.
     */
    private void checkAllQuests() {
        // QuestDefinition does not yet carry AvailabilityConfig.
        // When it does, this method will mirror checkAllChains() but iterate
        // QuestDefinitionRegistry and call applyQuestClosePolicy().
    }

    /**
     * Rebuilds the current availability snapshots from the registry. Called after each
     * check interval so that stale keys from unregistered definitions are evicted.
     */
    private void snapshotCurrentAvailability() {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);

        Map<NamespacedKey, Boolean> newChainSnapshot = new HashMap<>();
        for (QuestChainDefinition definition : chainRegistry.allChains()) {
            definition.getAvailabilityConfig().ifPresent(config -> {
                boolean available = config.isCurrentlyAvailable(currentTimeInZone(config));
                newChainSnapshot.put(definition.getChainKey(), available);
            });
        }
        previousChainAvailability.clear();
        previousChainAvailability.putAll(newChainSnapshot);

        // Quest snapshot will be added when QuestDefinition gains AvailabilityConfig.
    }

    /**
     * Applies the configured close policy for a chain whose availability window has closed.
     * Switches on the policy:
     * <ul>
     *     <li>{@link WindowClosePolicy#EXPIRE_ACTIVE} — immediately expires all active instances</li>
     *     <li>{@link WindowClosePolicy#ALLOW_FINISH} — no-op; players are allowed to finish</li>
     *     <li>{@link WindowClosePolicy#EXPIRE_WITH_GRACE} — schedules a delayed expiration</li>
     * </ul>
     *
     * @param chainKey the chain definition key
     * @param config   the availability config containing the close policy
     */
    private void applyChainClosePolicy(@NotNull NamespacedKey chainKey,
                                       @NotNull AvailabilityConfig config) {
        switch (config.onWindowClose()) {
            case EXPIRE_ACTIVE -> expireActiveChainInstances(chainKey);
            case ALLOW_FINISH -> {
                // Intentional no-op: active instances are allowed to complete naturally.
            }
            case EXPIRE_WITH_GRACE -> startChainGracePeriod(chainKey, config);
        }
    }

    /**
     * Applies the configured close policy for a quest whose availability window has closed.
     * Switches on the policy:
     * <ul>
     *     <li>{@link WindowClosePolicy#EXPIRE_ACTIVE} — immediately cancels all active instances</li>
     *     <li>{@link WindowClosePolicy#ALLOW_FINISH} — no-op; players are allowed to finish</li>
     *     <li>{@link WindowClosePolicy#EXPIRE_WITH_GRACE} — schedules a delayed cancellation</li>
     * </ul>
     *
     * @param questKey the quest definition key
     * @param config   the availability config containing the close policy
     */
    private void applyQuestClosePolicy(@NotNull NamespacedKey questKey,
                                       @NotNull AvailabilityConfig config) {
        switch (config.onWindowClose()) {
            case EXPIRE_ACTIVE -> cancelActiveQuestInstances(questKey);
            case ALLOW_FINISH -> {
                // Intentional no-op: active instances are allowed to complete naturally.
            }
            case EXPIRE_WITH_GRACE -> startQuestGracePeriod(questKey, config);
        }
    }

    /**
     * Expires all active chain instances for online players whose chain state matches the
     * given chain key and is currently {@link QuestChainState#ACTIVE}. Fires a cancellable
     * {@link QuestChainExpireEvent} per player before transitioning the state.
     *
     * @param chainKey the chain definition key to expire
     */
    private void expireActiveChainInstances(@NotNull NamespacedKey chainKey) {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        Optional<QuestChainDefinition> definitionOpt = chainRegistry.get(chainKey);
        if (definitionOpt.isEmpty()) {
            return;
        }
        QuestChainDefinition definition = definitionOpt.get();

        McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);

        int expired = 0;
        for (McRPGPlayer mcRPGPlayer : playerManager.getAllPlayers()) {
            QuestChainPlayerData chainData = mcRPGPlayer.getChainData();
            Optional<QuestChainPlayerState> stateOpt = chainData.getChainState(chainKey);
            if (stateOpt.isEmpty() || !stateOpt.get().isActive()) {
                continue;
            }
            QuestChainPlayerState chainState = stateOpt.get();

            Player bukkitPlayer = Bukkit.getPlayer(mcRPGPlayer.getUUID());
            QuestChainExpireEvent event = new QuestChainExpireEvent(definition, mcRPGPlayer.getUUID(), bukkitPlayer);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                continue;
            }

            chainState.expire();
            chainData.updateQuestKeyIndex(chainState);
            expired++;
        }

        if (expired > 0) {
            plugin.getLogger().info("[AvailabilityWindowChecker] Expired " + expired
                    + " active chain instance(s) for '" + chainKey + "'.");
        }
    }

    /**
     * Cancels all active quest instances whose definition key matches the given quest key.
     * Iterates the {@link QuestManager}'s active quest pool and calls
     * {@link QuestInstance#cancel()} on matching instances.
     *
     * @param questKey the quest definition key to cancel instances for
     */
    private void cancelActiveQuestInstances(@NotNull NamespacedKey questKey) {
        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);

        int cancelled = 0;
        for (QuestInstance quest : questManager.getActiveQuests()) {
            if (quest.getQuestKey().equals(questKey)) {
                quest.cancel();
                cancelled++;
            }
        }

        if (cancelled > 0) {
            plugin.getLogger().info("[AvailabilityWindowChecker] Cancelled " + cancelled
                    + " active quest instance(s) for '" + questKey + "'.");
        }
    }

    /**
     * Schedules a delayed task that will expire all active chain instances for the given
     * chain key after the grace period defined in the availability config. The scheduled
     * task ID is tracked so it can be cancelled if the window re-opens.
     *
     * @param chainKey the chain definition key
     * @param config   the availability config containing the grace period duration
     */
    private void startChainGracePeriod(@NotNull NamespacedKey chainKey,
                                       @NotNull AvailabilityConfig config) {
        if (config.gracePeriod() == null) {
            plugin.getLogger().warning("[AvailabilityWindowChecker] Chain '" + chainKey
                    + "' has EXPIRE_WITH_GRACE policy but no grace period configured. "
                    + "Expiring immediately.");
            expireActiveChainInstances(chainKey);
            return;
        }

        cancelGraceTask(chainKey);

        long graceTicks = config.gracePeriod().getSeconds() * 20L;
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeGraceTasks.remove(chainKey);
            expireActiveChainInstances(chainKey);
            plugin.getLogger().info("[AvailabilityWindowChecker] Grace period expired for chain '"
                    + chainKey + "'. Active instances expired.");
        }, graceTicks).getTaskId();

        activeGraceTasks.put(chainKey, taskId);
        plugin.getLogger().info("[AvailabilityWindowChecker] Grace period started for chain '"
                + chainKey + "' (" + config.gracePeriod().getSeconds() + "s).");
    }

    /**
     * Schedules a delayed task that will cancel all active quest instances for the given
     * quest key after the grace period defined in the availability config. The scheduled
     * task ID is tracked so it can be cancelled if the window re-opens.
     *
     * @param questKey the quest definition key
     * @param config   the availability config containing the grace period duration
     */
    private void startQuestGracePeriod(@NotNull NamespacedKey questKey,
                                       @NotNull AvailabilityConfig config) {
        if (config.gracePeriod() == null) {
            plugin.getLogger().warning("[AvailabilityWindowChecker] Quest '" + questKey
                    + "' has EXPIRE_WITH_GRACE policy but no grace period configured. "
                    + "Cancelling immediately.");
            cancelActiveQuestInstances(questKey);
            return;
        }

        cancelGraceTask(questKey);

        long graceTicks = config.gracePeriod().getSeconds() * 20L;
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeGraceTasks.remove(questKey);
            cancelActiveQuestInstances(questKey);
            plugin.getLogger().info("[AvailabilityWindowChecker] Grace period expired for quest '"
                    + questKey + "'. Active instances cancelled.");
        }, graceTicks).getTaskId();

        activeGraceTasks.put(questKey, taskId);
        plugin.getLogger().info("[AvailabilityWindowChecker] Grace period started for quest '"
                + questKey + "' (" + config.gracePeriod().getSeconds() + "s).");
    }

    /**
     * Cancels an active grace period task for the given key, if one exists. Used when a
     * window re-opens before the grace period expires.
     *
     * @param key the chain or quest definition key
     */
    private void cancelGraceTask(@NotNull NamespacedKey key) {
        Integer existingTaskId = activeGraceTasks.remove(key);
        if (existingTaskId != null) {
            Bukkit.getScheduler().cancelTask(existingTaskId);
            plugin.getLogger().info("[AvailabilityWindowChecker] Cancelled grace period task for '"
                    + key + "' (window re-opened).");
        }
    }

    /**
     * Resolves the current time in the timezone defined by the given availability config.
     * Uses the plugin's {@link com.diamonddagger590.mccore.util.TimeProvider} for testability.
     *
     * @param config the availability config whose timezone to use
     * @return the current time in the config's timezone
     */
    @NotNull
    private ZonedDateTime currentTimeInZone(@NotNull AvailabilityConfig config) {
        return plugin.getTimeProvider().now().atZone(config.timezone());
    }
}
