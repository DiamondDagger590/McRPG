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
 */
public class QuestStartMessageListener implements Listener {

    private final McRPG mcRPG;
    private final QuestMessageDeliverer messageDeliverer;

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
    }

    /**
     * Creates a new listener with an injected {@link QuestMessageDeliverer}. Intended for tests
     * that need to verify delivery without bootstrapping the full localization stack.
     *
     * @param mcRPG            the plugin instance used to access managers
     * @param messageDeliverer the deliverer to use for sending messages
     */
    QuestStartMessageListener(@NotNull McRPG mcRPG, @NotNull QuestMessageDeliverer messageDeliverer) {
        this.mcRPG = mcRPG;
        this.messageDeliverer = messageDeliverer;
    }

    /**
     * Sends on-start messages from the quest definition to all online scope members.
     * Messages are sent in declaration order. Locale-keyed messages fall back to inline
     * MiniMessage strings when the player's chain has no translation.
     *
     * @param event the quest start event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestStart(@NotNull QuestStartEvent event) {
        QuestDefinition definition = event.getQuestDefinition();
        List<OnStartMessage> messages = definition.getOnStartMessages();
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
