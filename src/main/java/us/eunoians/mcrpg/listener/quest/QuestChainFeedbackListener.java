package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestChainCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestChainStartEvent;
import us.eunoians.mcrpg.event.quest.QuestChainStepAdvanceEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;

/**
 * Sends player-facing chat notifications for quest chain lifecycle events:
 * chain start, step advance, and chain complete.
 * <p>
 * All handlers run at {@link EventPriority#MONITOR} so infrastructure listeners
 * finish processing the event before the feedback message is sent.
 */
public class QuestChainFeedbackListener implements Listener {

    /**
     * Notifies the player when their chain starts (first step begins).
     *
     * @param event the chain start event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChainStart(@NotNull QuestChainStartEvent event) {
        Player player = event.getPlayer();
        QuestChainDefinition definition = event.getChainDefinition();
        sendChainMessage(player, LocalizationKey.QUEST_CHAIN_EVENT_START,
                Map.of("chain", definition.getDisplayName()));
    }

    /**
     * Notifies the player when their chain advances to the next step.
     *
     * @param event the step advance event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChainAdvance(@NotNull QuestChainStepAdvanceEvent event) {
        Player player = event.getPlayer();
        QuestChainDefinition definition = event.getChainDefinition();
        int rawIndex = definition.getStepIndex(event.getNextStep().questKey());
        int stepNumber = rawIndex >= 0 ? rawIndex + 1 : 0;
        sendChainMessage(player, LocalizationKey.QUEST_CHAIN_EVENT_ADVANCE,
                Map.of(
                        "chain", definition.getDisplayName(),
                        "step", String.valueOf(stepNumber),
                        "total", String.valueOf(definition.getSteps().size())
                ));
    }

    /**
     * Notifies the player when their chain is fully completed.
     *
     * @param event the chain complete event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChainComplete(@NotNull QuestChainCompleteEvent event) {
        Player player = event.getPlayer();
        QuestChainDefinition definition = event.getChainDefinition();
        sendChainMessage(player, LocalizationKey.QUEST_CHAIN_EVENT_COMPLETE,
                Map.of("chain", definition.getDisplayName()));
    }

    /**
     * Resolves the player's {@link McRPGPlayer} wrapper and sends the localized chain message.
     * If the player is not loaded in the entity manager, the message is not sent.
     *
     * @param player           the Bukkit player
     * @param localizationKey  the locale route
     * @param placeholders     placeholder map for substitution
     */
    private void sendChainMessage(@NotNull Player player,
                                  @NotNull dev.dejvokep.boostedyaml.route.Route localizationKey,
                                  @NotNull Map<String, String> placeholders) {
        McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        Component message = localizationManager.getLocalizedMessageAsComponent(
                mcRPGPlayerOpt.get(), localizationKey, placeholders);
        player.sendMessage(message);
    }
}
