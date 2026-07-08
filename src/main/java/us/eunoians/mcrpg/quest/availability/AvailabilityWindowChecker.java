package us.eunoians.mcrpg.quest.availability;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.chain.QuestChainExpireEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerData;
import us.eunoians.mcrpg.quest.chain.QuestChainPlayerState;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.availability.AvailabilityConfig;
import us.eunoians.mcrpg.quest.chain.availability.WindowClosePolicy;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scheduled task that periodically checks availability windows for quest chains and
 * standalone quest definitions. When a window transitions from available to unavailable,
 * the configured {@link WindowClosePolicy} is applied to any active instances.
 * <p>
 * This task runs on Bukkit's async scheduler thread. Each interval tick follows a
 * two-phase pattern: availability transitions are computed on the async thread (pure
 * time-based comparisons against immutable config), then side effects (event firing,
 * state mutation, grace period scheduling) are applied on the main thread via
 * {@code Bukkit.getScheduler().runTask()}.
 * <p>
 * The checker maintains per-key previous-availability snapshots to detect open-to-close
 * transitions. Only transitions trigger policy enforcement; steady-state checks are no-ops.
 * The snapshot maps are published via {@code volatile} writes on the main thread so the
 * async compute phase can read them safely.
 * <p>
 * Grace period tasks are tracked by Bukkit task ID so they can be cancelled if the window
 * re-opens before the grace period expires (e.g., overlapping windows or config reload).
 * All {@code activeGraceTasks} access is confined to the main thread.
 * <p>
 * Eviction: {@code previousChainAvailability}, {@code previousQuestAvailability}, and
 * {@code activeGraceTasks} are bounded by the number of registered definitions with
 * availability configs. Entries are rebuilt from the registry on each check interval
 * via {@link #snapshotCurrentAvailability()}, so stale keys from unregistered definitions
 * are naturally evicted.
 */
public final class AvailabilityWindowChecker extends CancelableCoreTask {

    private final McRPG plugin;
    private volatile Map<NamespacedKey, Boolean> previousChainAvailability;
    private volatile Map<NamespacedKey, Boolean> previousQuestAvailability;
    private final Map<NamespacedKey, DelayableCoreTask> activeGraceTasks;

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
        return true;
    }

    /**
     * Called when the initial delay completes. Computes which chains are currently
     * unavailable on the async thread, then hops to the main thread to apply
     * close policies and build the initial availability snapshot.
     */
    @Override
    protected void onDelayComplete() {
        List<PendingTransition> startupTransitions = computeStartupReconciliation();
        Bukkit.getScheduler().runTask(plugin, () -> applyStartupReconciliation(startupTransitions));
    }

    /**
     * No-op. Interval start does not require any action.
     */
    @Override
    protected void onIntervalStart() {
        // No-op
    }

    /**
     * Called each time a full check interval completes on the async thread. Computes
     * availability transitions, then schedules a main-thread task to apply side effects
     * (event firing, state mutation, grace period management) and refresh the snapshot.
     */
    @Override
    protected void onIntervalComplete() {
        List<PendingTransition> chainTransitions = computeChainTransitions();
        List<PendingTransition> questTransitions = computeQuestTransitions();

        Bukkit.getScheduler().runTask(plugin, () -> {
            chainTransitions.forEach(this::applyChainTransition);
            questTransitions.forEach(this::applyQuestTransition);
            snapshotCurrentAvailability();
        });
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
     * Cancels all pending grace period tasks when this checker is cancelled.
     */
    @Override
    protected void onCancel() {
        for (DelayableCoreTask task : activeGraceTasks.values()) {
            Bukkit.getScheduler().cancelTask(task.getBukkitTaskId());
        }
        activeGraceTasks.clear();
    }

    /**
     * Computes which chains are currently unavailable at server startup. Called on the
     * async thread during {@link #onDelayComplete()}. Only performs pure time-based
     * comparisons against immutable config — no side effects.
     * <p>
     * <b>Limitation:</b> Only online players are affected by startup reconciliation.
     * Offline players with ACTIVE chains during a closed window retain their ACTIVE
     * state until next login, when the chain login listener re-evaluates availability.
     *
     * @return transitions for chains that need close policy applied
     */
    @NotNull
    private List<PendingTransition> computeStartupReconciliation() {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);

        List<PendingTransition> transitions = new ArrayList<>();
        for (QuestChainDefinition definition : chainRegistry.allChains()) {
            Optional<AvailabilityConfig> configOpt = definition.getAvailabilityConfig();
            if (configOpt.isEmpty()) {
                continue;
            }
            AvailabilityConfig config = configOpt.get();
            boolean available = config.isCurrentlyAvailable(currentTimeInZone(config));
            if (!available) {
                transitions.add(new PendingTransition(definition.getChainKey(), config, true, false));
            }
        }
        return transitions;
    }

    /**
     * Applies startup reconciliation on the main thread. Applies close policies for
     * chains that were found to be unavailable, then builds the initial availability
     * snapshot for subsequent interval checks.
     *
     * @param transitions the transitions computed by {@link #computeStartupReconciliation()}
     */
    private void applyStartupReconciliation(@NotNull List<PendingTransition> transitions) {
        for (PendingTransition transition : transitions) {
            applyChainClosePolicy(transition.key(), transition.config());
        }
        snapshotCurrentAvailability();

        plugin.getLogger().info("[AvailabilityWindowChecker] Startup reconciliation complete. "
                + "Tracking " + previousChainAvailability.size() + " chain availability window(s).");
    }

    /**
     * Computes chain availability transitions by comparing current window state against
     * the previous snapshot. Called on the async thread. Reads the volatile snapshot
     * reference once for a consistent view.
     *
     * @return transitions that need to be applied on the main thread
     */
    @NotNull
    private List<PendingTransition> computeChainTransitions() {
        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        Map<NamespacedKey, Boolean> chainAvailability = this.previousChainAvailability;

        List<PendingTransition> transitions = new ArrayList<>();
        for (QuestChainDefinition definition : chainRegistry.allChains()) {
            Optional<AvailabilityConfig> configOpt = definition.getAvailabilityConfig();
            if (configOpt.isEmpty()) {
                continue;
            }
            AvailabilityConfig config = configOpt.get();
            NamespacedKey chainKey = definition.getChainKey();
            boolean nowAvailable = config.isCurrentlyAvailable(currentTimeInZone(config));
            boolean wasAvailable = chainAvailability.getOrDefault(chainKey, true);

            if (wasAvailable && !nowAvailable) {
                transitions.add(new PendingTransition(chainKey, config, true, false));
            } else if (!wasAvailable && nowAvailable) {
                transitions.add(new PendingTransition(chainKey, config, false, true));
            }
        }
        return transitions;
    }

    /**
     * Computes quest availability transitions. Currently returns an empty list because
     * {@link QuestDefinition} does not yet carry an {@link AvailabilityConfig}.
     *
     * @return transitions that need to be applied on the main thread
     */
    @NotNull
    private List<PendingTransition> computeQuestTransitions() {
        return List.of();
    }

    /**
     * Applies a single chain availability transition on the main thread. Handles
     * both window-close (apply close policy) and window-reopen (cancel grace task).
     *
     * @param transition the transition to apply
     */
    private void applyChainTransition(@NotNull PendingTransition transition) {
        if (transition.windowReopened()) {
            cancelGraceTask(transition.key());
            plugin.getLogger().info("[AvailabilityWindowChecker] Chain '" + transition.key()
                    + "' window opened.");
        }
        if (transition.windowClosed()) {
            plugin.getLogger().info("[AvailabilityWindowChecker] Chain '" + transition.key()
                    + "' window closed. Applying close policy: " + transition.config().onWindowClose());
            applyChainClosePolicy(transition.key(), transition.config());
        }
    }

    /**
     * Applies a single quest availability transition on the main thread. Handles
     * both window-close (apply close policy) and window-reopen (cancel grace task).
     *
     * @param transition the transition to apply
     */
    private void applyQuestTransition(@NotNull PendingTransition transition) {
        if (transition.windowReopened()) {
            cancelGraceTask(transition.key());
            plugin.getLogger().info("[AvailabilityWindowChecker] Quest '" + transition.key()
                    + "' window opened.");
        }
        if (transition.windowClosed()) {
            plugin.getLogger().info("[AvailabilityWindowChecker] Quest '" + transition.key()
                    + "' window closed. Applying close policy: " + transition.config().onWindowClose());
            applyQuestClosePolicy(transition.key(), transition.config());
        }
    }

    /**
     * Rebuilds the current availability snapshots from the registry. Called on the main
     * thread after applying transitions so that stale keys from unregistered definitions
     * are evicted. The resulting maps are published via volatile write for safe reads
     * by the async compute phase.
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
        previousChainAvailability = newChainSnapshot;

        // Quest snapshot will be added when QuestDefinition gains AvailabilityConfig.
    }

    /**
     * Applies the configured close policy for a chain whose availability window has closed.
     * Must be called on the main thread.
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
     * Must be called on the main thread.
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
     * given chain key and is currently active. Fires a cancellable {@link QuestChainExpireEvent}
     * per player before transitioning the state. Must be called on the main thread.
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
     * {@link QuestInstance#cancel()} on matching instances. Must be called on the main thread.
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
     * task ID is tracked so it can be cancelled if the window re-opens. Must be called
     * on the main thread.
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

        DelayableCoreTask graceTask = new DelayableCoreTask(plugin, config.gracePeriod().getSeconds()) {
            @Override
            public void run() {
                activeGraceTasks.remove(chainKey);
                expireActiveChainInstances(chainKey);
                plugin.getLogger().info("[AvailabilityWindowChecker] Grace period expired for chain '"
                        + chainKey + "'. Active instances expired.");
            }
        };
        graceTask.runTask();
        activeGraceTasks.put(chainKey, graceTask);
        plugin.getLogger().info("[AvailabilityWindowChecker] Grace period started for chain '"
                + chainKey + "' (" + config.gracePeriod().getSeconds() + "s).");
    }

    /**
     * Schedules a delayed task that will cancel all active quest instances for the given
     * quest key after the grace period defined in the availability config. The scheduled
     * task ID is tracked so it can be cancelled if the window re-opens. Must be called
     * on the main thread.
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

        DelayableCoreTask graceTask = new DelayableCoreTask(plugin, config.gracePeriod().getSeconds()) {
            @Override
            public void run() {
                activeGraceTasks.remove(questKey);
                cancelActiveQuestInstances(questKey);
                plugin.getLogger().info("[AvailabilityWindowChecker] Grace period expired for quest '"
                        + questKey + "'. Active instances cancelled.");
            }
        };
        graceTask.runTask();
        activeGraceTasks.put(questKey, graceTask);
        plugin.getLogger().info("[AvailabilityWindowChecker] Grace period started for quest '"
                + questKey + "' (" + config.gracePeriod().getSeconds() + "s).");
    }

    /**
     * Cancels an active grace period task for the given key, if one exists. Used when a
     * window re-opens before the grace period expires. Must be called on the main thread.
     *
     * @param key the chain or quest definition key
     */
    private void cancelGraceTask(@NotNull NamespacedKey key) {
        DelayableCoreTask existingTask = activeGraceTasks.remove(key);
        if (existingTask != null) {
            Bukkit.getScheduler().cancelTask(existingTask.getBukkitTaskId());
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

    /**
     * Immutable carrier for an availability window transition detected during the async
     * compute phase. Consumed on the main thread to apply the corresponding side effects.
     *
     * @param key            the chain or quest definition key
     * @param config         the availability config for the definition
     * @param windowClosed   {@code true} if the window transitioned from available to unavailable
     * @param windowReopened {@code true} if the window transitioned from unavailable to available
     */
    private record PendingTransition(
            @NotNull NamespacedKey key,
            @NotNull AvailabilityConfig config,
            boolean windowClosed,
            boolean windowReopened
    ) {}
}
