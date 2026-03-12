package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestExpireEvent;
import us.eunoians.mcrpg.event.quest.QuestPhaseCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Sends player-facing chat notifications for all quest lifecycle events:
 * start, complete, cancel (non-expiry), expire, and phase complete.
 * <p>
 * All handlers run at {@link EventPriority#MONITOR} so infrastructure listeners
 * (e.g. {@link QuestStartListener}) process the quest first.
 */
public class QuestFeedbackListener implements Listener {

    /**
     * Notifies in-scope players when a quest starts.
     * This covers silent starts such as ability-upgrade quests and shared-scope
     * Lands quests, in addition to board-accepted quests.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestStart(@NotNull QuestStartEvent event) {
        notifyScope(event.getQuestInstance(), LocalizationKey.QUEST_STARTED_NOTIFICATION,
                player -> Map.of("quest_name", resolveDisplayName(event.getQuestInstance(), player)));
    }

    /**
     * Notifies in-scope players when a quest is completed and plays a completion sound.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestComplete(@NotNull QuestCompleteEvent event) {
        QuestInstance quest = event.getQuestInstance();
        notifyScope(quest, LocalizationKey.QUEST_COMPLETED_NOTIFICATION,
                player -> Map.of("quest_name", resolveDisplayName(quest, player)));
        playSoundToScope(quest, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Notifies in-scope players when a quest expires.
     * Expiry fires before the resulting {@link QuestCancelEvent}, so the cancel
     * handler skips its notification by checking {@link QuestInstance#isExpired()}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestExpire(@NotNull QuestExpireEvent event) {
        notifyScope(event.getQuestInstance(), LocalizationKey.QUEST_EXPIRED_NOTIFICATION,
                player -> Map.of("quest_name", resolveDisplayName(event.getQuestInstance(), player)));
    }

    /**
     * Notifies in-scope players when a quest is manually abandoned (cancelled by the player,
     * not by expiry). Skipped when the quest expired to avoid sending two notifications.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestCancel(@NotNull QuestCancelEvent event) {
        if (event.getQuestInstance().isExpired()) {
            return;
        }
        notifyScope(event.getQuestInstance(), LocalizationKey.QUEST_CANCELLED_NOTIFICATION,
                player -> Map.of("quest_name", resolveDisplayName(event.getQuestInstance(), player)));
    }

    /**
     * Notifies in-scope players when a quest phase completes.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestPhaseComplete(@NotNull QuestPhaseCompleteEvent event) {
        int humanPhaseNumber = event.getCompletedPhaseIndex() + 1;
        notifyScope(event.getQuestInstance(), LocalizationKey.QUEST_PHASE_COMPLETED_NOTIFICATION,
                player -> Map.of(
                        "quest_name", resolveDisplayName(event.getQuestInstance(), player),
                        "phase_number", String.valueOf(humanPhaseNumber)
                ));
    }

    /**
     * Resolves the display name for a quest instance using the given player's locale.
     * Falls back to the quest key string if the definition is no longer in the registry.
     *
     * @param quest  the quest instance to resolve a name for
     * @param player the player whose locale to use
     * @return the display name string (MiniMessage formatted, safe to embed as a placeholder)
     */
    @NotNull
    private String resolveDisplayName(@NotNull QuestInstance quest, @NotNull McRPGPlayer player) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        return definitionRegistry.get(quest.getQuestKey())
                .map(def -> def.getDisplayName(player))
                .orElse(quest.getQuestKey().getKey());
    }

    /**
     * Sends a localized message to all currently online players in the quest's scope.
     * The placeholder map is produced per-player via the supplied function so that
     * locale-sensitive values (such as the quest's display name) are resolved
     * using each player's own locale.
     *
     * @param quest          the quest instance whose scope to notify
     * @param messageRoute   the {@link Route} for the localized message template
     * @param placeholdersFn function that produces placeholder key-value pairs for one player
     */
    private void notifyScope(@NotNull QuestInstance quest,
                             @NotNull Route messageRoute,
                             @NotNull Function<McRPGPlayer, Map<String, String>> placeholdersFn) {
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);

        quest.getQuestScope().ifPresent(scope -> {
            for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(playerUUID);
                if (mcRPGPlayerOpt.isEmpty()) {
                    continue;
                }
                McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
                mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                    Component message = localizationManager.getLocalizedMessageAsComponent(
                            mcRPGPlayer, messageRoute, placeholdersFn.apply(mcRPGPlayer));
                    player.sendMessage(message);
                });
            }
        });
    }

    /**
     * Plays a sound to all currently online players in the quest's scope.
     *
     * @param quest  the quest instance whose scope to play the sound for
     * @param sound  the Bukkit sound to play
     * @param volume the sound volume
     * @param pitch  the sound pitch
     */
    private void playSoundToScope(@NotNull QuestInstance quest,
                                  @NotNull Sound sound,
                                  float volume,
                                  float pitch) {
        McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);

        quest.getQuestScope().ifPresent(scope -> {
            for (UUID playerUUID : scope.getCurrentPlayersInScope()) {
                playerManager.getPlayer(playerUUID).flatMap(McRPGPlayer::getAsBukkitPlayer)
                        .ifPresent(player -> {
                            Location loc = player.getLocation();
                            player.playSound(loc, sound, volume, pitch);
                        });
            }
        });
    }
}
