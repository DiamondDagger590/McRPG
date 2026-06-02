package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.CascadeCompletedStep;
import us.eunoians.mcrpg.event.quest.CascadeFinalizeEvent;
import us.eunoians.mcrpg.event.quest.CascadeOutcome;
import us.eunoians.mcrpg.event.quest.CascadeStartEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.message.QuestMessageDeliverer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages cascade lifecycle around chain start/advance operations. A cascade occurs
 * when a chain step auto-completes immediately upon starting (because the player
 * already satisfies the objective), triggering a recursive advance that starts and
 * potentially auto-completes the next step, and so on within a single tick.
 * <p>
 * This class owns the {@code activeCascades} map and provides the public API that
 * listeners and commands call. It delegates actual chain state progression to
 * {@link QuestChainManager}, keeping cascade bookkeeping separate from core
 * chain logic.
 * <p>
 * When a cascade occurs:
 * <ol>
 *   <li>On-start messages for auto-completed steps are deferred to the
 *       {@link CascadeContext} (via {@link us.eunoians.mcrpg.listener.quest.QuestStartMessageListener}
 *       intercepting the {@link us.eunoians.mcrpg.event.quest.QuestStartEvent})</li>
 *   <li>After the root call returns, {@link #finalizeCascade} delivers only the final
 *       (non-auto-completed) step's deferred messages</li>
 *   <li>A generic batch summary listing all auto-completed steps is sent to the player</li>
 * </ol>
 * <p>
 * The cascade depth limit (50) guards against infinite loops from misconfigured chains.
 * When the limit is reached the cascade stops, the chain remains ACTIVE at the last
 * successfully started step, and a WARNING is logged directing the admin to report
 * the configuration error.
 * <p>
 * Not thread-safe — all access is on the main Bukkit thread.
 */
public class CascadeOrchestrator {

    private static final int CASCADE_DEPTH_LIMIT = 50;

    private final QuestChainManager chainManager;
    private final McRPG plugin;
    private final QuestMessageDeliverer messageDeliverer;
    private final Map<UUID, CascadeContext> activeCascades = new HashMap<>();

    /**
     * Creates a new cascade orchestrator.
     *
     * @param plugin       the McRPG plugin instance
     * @param chainManager the chain manager to delegate core progression to
     */
    public CascadeOrchestrator(@NotNull McRPG plugin, @NotNull QuestChainManager chainManager) {
        this.plugin = plugin;
        this.chainManager = chainManager;
        McRPGLocalizationManager locManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        this.messageDeliverer = new QuestMessageDeliverer(locManager, plugin.getMiniMessage(), plugin.getLogger());
    }

    /**
     * Attempts to start a chain for a player, wrapping the operation in a cascade
     * context. If the first quest auto-completes and triggers recursive
     * {@link #advanceChain} calls, all auto-completed steps are batched into a
     * single summary message delivered after the root call returns.
     *
     * @param player   the player
     * @param chainKey the chain definition key
     * @return {@code true} if the chain was started
     */
    public boolean tryStartChain(@NotNull Player player, @NotNull NamespacedKey chainKey) {
        UUID playerUUID = player.getUniqueId();
        boolean isRoot = !activeCascades.containsKey(playerUUID);
        if (isRoot) {
            activeCascades.put(playerUUID, new CascadeContext(chainKey));
            Bukkit.getPluginManager().callEvent(new CascadeStartEvent(chainKey, playerUUID, player));
        }

        CascadeOutcome outcome = CascadeOutcome.SUCCESS;
        try {
            boolean result = chainManager.tryStartChain(player, chainKey);

            if (!result && isRoot) {
                return false;
            }

            return result;
        } catch (RuntimeException ex) {
            outcome = CascadeOutcome.ERROR;
            throw ex;
        } finally {
            if (isRoot) {
                finalizeCascade(playerUUID, player, outcome);
            }
        }
    }

    /**
     * Advances a player's chain after a quest completes, wrapping the operation
     * in a cascade context. Records the completed step in the context and enforces
     * the cascade depth limit.
     *
     * @param playerUUID        the player UUID
     * @param completedQuestKey the quest definition key that was just completed
     * @return {@code true} if the chain advanced or completed
     */
    public boolean advanceChain(@NotNull UUID playerUUID, @NotNull NamespacedKey completedQuestKey) {
        boolean isRoot = !activeCascades.containsKey(playerUUID);
        if (isRoot) {
            NamespacedKey chainKey = resolveChainKey(playerUUID, completedQuestKey);
            activeCascades.put(playerUUID, new CascadeContext(chainKey));
            Bukkit.getPluginManager().callEvent(new CascadeStartEvent(chainKey, playerUUID,
                    Bukkit.getPlayer(playerUUID)));
        }

        CascadeContext cascadeContext = activeCascades.get(playerUUID);

        if (cascadeContext.getAutoCompletedSteps().size() >= CASCADE_DEPTH_LIMIT) {
            plugin.getLogger().warning("[CascadeOrchestrator] Cascade depth limit ("
                    + CASCADE_DEPTH_LIMIT + ") reached for chain '" + cascadeContext.getChainKey()
                    + "' player " + playerUUID + ". This likely indicates a chain configuration "
                    + "error — please report to the McRPG developers with your chain YAML.");
            if (isRoot) {
                finalizeCascade(playerUUID, Bukkit.getPlayer(playerUUID), CascadeOutcome.DEPTH_LIMIT_REACHED);
            }
            return false;
        }

        CascadeOutcome outcome = CascadeOutcome.SUCCESS;
        try {
            boolean result = chainManager.advanceChain(playerUUID, completedQuestKey);

            if (result && !isRoot) {
                String completedDisplayName = resolveQuestDisplayName(completedQuestKey, playerUUID);
                cascadeContext.recordAutoCompletedStep(completedQuestKey, completedDisplayName);
            }

            return result;
        } catch (RuntimeException ex) {
            outcome = CascadeOutcome.ERROR;
            throw ex;
        } finally {
            if (isRoot) {
                finalizeCascade(playerUUID, Bukkit.getPlayer(playerUUID), outcome);
            }
        }
    }

    /**
     * Returns {@code true} if the given player is currently in a chain cascade
     * (auto-completing steps within a single tick).
     *
     * @param playerUUID the player UUID
     * @return {@code true} if a cascade is active
     */
    public boolean isInCascade(@NotNull UUID playerUUID) {
        return activeCascades.containsKey(playerUUID);
    }

    /**
     * Returns the active cascade context for the player, if one exists.
     *
     * @param playerUUID the player UUID
     * @return the cascade context, or empty if no cascade is active
     */
    @NotNull
    public Optional<CascadeContext> getCascadeContext(@NotNull UUID playerUUID) {
        return Optional.ofNullable(activeCascades.get(playerUUID));
    }

    /**
     * Notifies the orchestrator that a new step quest has been started during
     * the current cascade. Called by the chain manager (or listener) after
     * successfully starting a step quest, so that {@code finalizeCascade} knows
     * which step's deferred messages to deliver.
     *
     * @param playerUUID the player UUID
     * @param questKey   the quest key that was started
     */
    public void notifyStepStarted(@NotNull UUID playerUUID, @NotNull NamespacedKey questKey) {
        CascadeContext context = activeCascades.get(playerUUID);
        if (context != null) {
            context.setLastStartedQuestKey(questKey);
        }
    }

    /**
     * Finalizes a cascade after the root call returns. If auto-completed steps
     * were recorded, sends a batch summary and delivers only the final step's
     * deferred messages. If no steps auto-completed, delivers all deferred messages
     * for the (only) started step normally.
     * <p>
     * Message delivery and batch summary sending are wrapped in a try/catch so that
     * the {@link CascadeFinalizeEvent} always fires even if delivery throws.
     *
     * @param playerUUID the player UUID
     * @param player     the Bukkit player (may be null if disconnected mid-cascade)
     * @param outcome    the outcome determined by the caller (may be overridden to
     *                   {@link CascadeOutcome#ERROR} if delivery fails)
     */
    void finalizeCascade(@NotNull UUID playerUUID, @Nullable Player player, @NotNull CascadeOutcome outcome) {
        CascadeContext context = activeCascades.remove(playerUUID);
        if (context == null) {
            return;
        }

        CascadeOutcome effectiveOutcome = outcome;

        if (player != null && player.isOnline()) {
            try {
                if (!context.hasAutoCompletedSteps()) {
                    context.getLastStartedQuestKey()
                            .ifPresent(key -> deliverDeferredMessages(player, playerUUID, context, key));
                } else {
                    context.getLastStartedQuestKey()
                            .ifPresent(key -> deliverDeferredMessages(player, playerUUID, context, key));
                    sendCascadeBatchSummary(player, playerUUID, context);
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE,
                        "[CascadeOrchestrator] Error delivering cascade messages for player "
                                + playerUUID + " chain '" + context.getChainKey() + "'", ex);
                effectiveOutcome = CascadeOutcome.ERROR;
            }
        }

        Bukkit.getPluginManager().callEvent(new CascadeFinalizeEvent(
                context.getChainKey(),
                playerUUID,
                player,
                context.getAutoCompletedSteps(),
                context.getLastStartedQuestKey().orElse(null),
                effectiveOutcome));
    }

    /**
     * Delivers deferred on-start messages for a specific quest key using the
     * shared {@link QuestMessageDeliverer} — the same delivery path used by
     * {@link us.eunoians.mcrpg.listener.quest.QuestStartMessageListener} for
     * immediate delivery.
     *
     * @param player     the player to deliver to
     * @param playerUUID the player UUID
     * @param context    the cascade context holding deferred messages
     * @param questKey   the quest key whose messages to deliver
     */
    private void deliverDeferredMessages(@NotNull Player player, @NotNull UUID playerUUID,
                                         @NotNull CascadeContext context, @NotNull NamespacedKey questKey) {
        List<OnStartMessage> messages = context.getDeferredMessagesFor(questKey);
        if (messages.isEmpty()) {
            return;
        }
        Optional<McRPGPlayer> mcRPGPlayerOpt = plugin.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        for (OnStartMessage msg : messages) {
            Route route = msg.localeKey().map(Route::fromString).orElse(null);
            messageDeliverer.deliver(player, mcRPGPlayerOpt.orElse(null), route, msg.inlineMessages());
        }
    }

    /**
     * Sends a localized batch summary message listing all auto-completed steps.
     * Uses generic locale keys (under {@code quest-chain.cascade}) so this works for
     * any chain, not just the tutorial.
     *
     * @param player     the player to send the summary to
     * @param playerUUID the player UUID
     * @param context    the cascade context with completed steps
     */
    private void sendCascadeBatchSummary(@NotNull Player player, @NotNull UUID playerUUID,
                                          @NotNull CascadeContext context) {
        McRPGLocalizationManager locManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Optional<McRPGPlayer> mcRPGPlayerOpt = plugin.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();

        String chainDisplayName = resolveChainDisplayName(context.getChainKey());

        String header = locManager.getLocalizedMessage(mcRPGPlayer,
                LocalizationKey.QUEST_CHAIN_CASCADE_BATCH_HEADER)
                .replace("<chain>", chainDisplayName);
        player.sendMessage(plugin.getMiniMessage().deserialize(header));

        for (CascadeCompletedStep step : context.getAutoCompletedSteps()) {
            String entry = locManager.getLocalizedMessage(mcRPGPlayer,
                    LocalizationKey.QUEST_CHAIN_CASCADE_BATCH_STEP_ENTRY)
                    .replace("<quest>", step.displayName());
            player.sendMessage(plugin.getMiniMessage().deserialize(entry));
        }
    }

    /**
     * Resolves the chain key for a completed quest via the player's chain data
     * reverse index.
     *
     * @param playerUUID        the player UUID
     * @param completedQuestKey the completed quest key
     * @return the chain key, or the quest key itself if resolution fails
     */
    @NotNull
    private NamespacedKey resolveChainKey(@NotNull UUID playerUUID, @NotNull NamespacedKey completedQuestKey) {
        Optional<McRPGPlayer> playerOpt = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (playerOpt.isPresent()) {
            Optional<NamespacedKey> chainKeyOpt = playerOpt.get().getChainData()
                    .getChainKeyForCurrentQuest(completedQuestKey);
            if (chainKeyOpt.isPresent()) {
                return chainKeyOpt.get();
            }
        }
        return completedQuestKey;
    }

    /**
     * Resolves the display name for a quest definition key.
     *
     * @param questKey   the quest definition key
     * @param playerUUID the player UUID (for locale resolution)
     * @return the resolved display name, or the key's value portion as fallback
     */
    @NotNull
    private String resolveQuestDisplayName(@NotNull NamespacedKey questKey, @NotNull UUID playerUUID) {
        Optional<McRPGPlayer> playerOpt = plugin.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(playerUUID);
        if (playerOpt.isEmpty()) {
            return questKey.getKey();
        }
        return RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION)
                .get(questKey)
                .map(def -> def.getDisplayName(playerOpt.get()))
                .orElse(questKey.getKey());
    }

    /**
     * Resolves the display name for a chain definition key.
     *
     * @param chainKey the chain definition key
     * @return the resolved chain display name, or the key's value portion as fallback
     */
    @NotNull
    private String resolveChainDisplayName(@NotNull NamespacedKey chainKey) {
        return RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN)
                .get(chainKey)
                .map(QuestChainDefinition::getDisplayName)
                .orElse(chainKey.getKey());
    }
}
