package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.CascadeContext;
import us.eunoians.mcrpg.quest.chain.CascadeOrchestrator;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.message.QuestMessageDeliverer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Listens for {@link QuestStartEvent} and delivers on-start messages from the quest
 * definition to all online players in the quest's scope.
 * <p>
 * If an {@link OnStartMessage} has a locale key, the message is resolved per-player
 * through {@link McRPGLocalizationManager}. Otherwise, inline MiniMessage strings
 * are parsed and sent directly. Message delivery is handled by {@link QuestMessageDeliverer}.
 * <p>
 * When the starting player is in a cascade (tracked by {@link CascadeOrchestrator}),
 * on-start messages are deferred to the {@link CascadeContext} rather than sent immediately.
 * The orchestrator's {@code finalizeCascade} method delivers only the final
 * (non-auto-completed) step's messages after the full cascade settles.
 */
public class QuestStartMessageListener implements Listener {

    private final McRPG mcRPG;
    private final QuestMessageDeliverer messageDeliverer;
    private final CascadeOrchestrator cascadeOrchestrator;

    /**
     * Creates a new listener.
     *
     * @param mcRPG the plugin instance used to access managers
     */
    public QuestStartMessageListener(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        McRPGLocalizationManager locManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        this.messageDeliverer = new QuestMessageDeliverer(locManager, mcRPG.getMiniMessage(), mcRPG.getLogger());
        QuestChainManager chainManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST_CHAIN);
        this.cascadeOrchestrator = chainManager.getCascadeOrchestrator();
    }

    /**
     * Creates a new listener with an injected {@link QuestMessageDeliverer} and
     * {@link CascadeOrchestrator}. Intended for tests that need to verify delivery
     * without bootstrapping the full stack.
     *
     * @param mcRPG                the plugin instance used to access managers
     * @param messageDeliverer     the deliverer to use for sending messages
     * @param cascadeOrchestrator  the cascade orchestrator for checking cascade state
     */
    QuestStartMessageListener(@NotNull McRPG mcRPG, @NotNull QuestMessageDeliverer messageDeliverer,
                               @NotNull CascadeOrchestrator cascadeOrchestrator) {
        this.mcRPG = mcRPG;
        this.messageDeliverer = messageDeliverer;
        this.cascadeOrchestrator = cascadeOrchestrator;
    }

    /**
     * Sends on-start messages from the quest definition to all online scope members.
     * Messages are sent in declaration order. Locale-keyed messages fall back to inline
     * MiniMessage strings when the player's chain has no translation.
     * <p>
     * When the quest starter is in a cascade, messages are deferred to the
     * {@link CascadeContext} rather than delivered immediately. The orchestrator
     * delivers the final step's messages after the cascade settles.
     *
     * @param event the quest start event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestStart(@NotNull QuestStartEvent event) {
        QuestDefinition definition = event.getQuestDefinition();
        List<OnStartMessage> messages = definition.getOnStartMessages();
        UUID starterUUID = event.getStarterUUID();

        // Cascade tracking must always update lastStartedQuestKey, even when
        // the definition has no on-start messages, so finalizeCascade delivers
        // the correct step's deferred messages.
        if (starterUUID != null && cascadeOrchestrator.isInCascade(starterUUID)) {
            Optional<CascadeContext> contextOpt = cascadeOrchestrator.getCascadeContext(starterUUID);
            if (contextOpt.isPresent()) {
                if (!messages.isEmpty()) {
                    contextOpt.get().deferMessages(definition.getQuestKey(), messages);
                }
                cascadeOrchestrator.notifyStepStarted(starterUUID, definition.getQuestKey());
                return;
            }
            // Context missing despite isInCascade=true — map inconsistency.
            // Fall through to immediate delivery rather than silently dropping messages.
            mcRPG.getLogger().warning("[QuestStartMessageListener] isInCascade=true but "
                    + "CascadeContext missing for player " + starterUUID
                    + " — falling through to immediate delivery");
        }

        if (messages.isEmpty()) {
            return;
        }

        // Pre-parse locale routes once here so Route.fromString() is not called per-player inside the loop.
        List<Route> precomputedRoutes = new ArrayList<>(messages.size());
        for (OnStartMessage msg : messages) {
            precomputedRoutes.add(msg.localeKey().map(Route::fromString).orElse(null));
        }

        QuestInstance instance = event.getQuestInstance();
        var playerManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);

        for (UUID playerUUID : instance.getQuestScope().map(scope -> scope.getCurrentPlayersInScope()).orElse(Set.of())) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue;
            }
            Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(playerUUID);

            for (int i = 0; i < messages.size(); i++) {
                messageDeliverer.deliver(player, mcRPGPlayerOpt.orElse(null),
                        precomputedRoutes.get(i), messages.get(i).inlineMessages());
            }
        }
    }
}
