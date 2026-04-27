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
        QuestInstance quest = event.getQuestInstance();
        Optional<QuestDefinition> defOpt = resolveDefinition(quest);
        notifyScope(quest, LocalizationKey.QUEST_STARTED_NOTIFICATION,
                player -> Map.of("quest_name",
                        defOpt.map(def -> def.getDisplayName(player))
                              .orElseGet(() -> formatKeyAsDisplayName(quest.getQuestKey().getKey()))));
    }

    /**
     * Notifies in-scope players when a quest is completed and plays a completion sound.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestComplete(@NotNull QuestCompleteEvent event) {
        QuestInstance quest = event.getQuestInstance();
        Optional<QuestDefinition> defOpt = resolveDefinition(quest);
        notifyScope(quest, LocalizationKey.QUEST_COMPLETED_NOTIFICATION,
                player -> Map.of("quest_name",
                        defOpt.map(def -> def.getDisplayName(player))
                              .orElseGet(() -> formatKeyAsDisplayName(quest.getQuestKey().getKey()))));
        playSoundToScope(quest, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    }

    /**
     * Notifies in-scope players when a quest expires.
     * Expiry fires before the resulting {@link QuestCancelEvent}, so the cancel
     * handler skips its notification by checking {@link QuestInstance#isExpired()}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestExpire(@NotNull QuestExpireEvent event) {
        QuestInstance quest = event.getQuestInstance();
        Optional<QuestDefinition> defOpt = resolveDefinition(quest);
        notifyScope(quest, LocalizationKey.QUEST_EXPIRED_NOTIFICATION,
                player -> Map.of("quest_name",
                        defOpt.map(def -> def.getDisplayName(player))
                              .orElseGet(() -> formatKeyAsDisplayName(quest.getQuestKey().getKey()))));
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
        QuestInstance quest = event.getQuestInstance();
        Optional<QuestDefinition> defOpt = resolveDefinition(quest);
        notifyScope(quest, LocalizationKey.QUEST_CANCELLED_NOTIFICATION,
                player -> Map.of("quest_name",
                        defOpt.map(def -> def.getDisplayName(player))
                              .orElseGet(() -> formatKeyAsDisplayName(quest.getQuestKey().getKey()))));
    }

    /**
     * Notifies in-scope players when a quest phase completes.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestPhaseComplete(@NotNull QuestPhaseCompleteEvent event) {
        QuestInstance quest = event.getQuestInstance();
        Optional<QuestDefinition> defOpt = resolveDefinition(quest);
        int humanPhaseNumber = event.getCompletedPhaseIndex() + 1;
        notifyScope(quest, LocalizationKey.QUEST_PHASE_COMPLETED_NOTIFICATION,
                player -> Map.of(
                        "quest_name", defOpt.map(def -> def.getDisplayName(player))
                                            .orElseGet(() -> formatKeyAsDisplayName(quest.getQuestKey().getKey())),
                        "phase_number", String.valueOf(humanPhaseNumber)
                ));
    }

    /**
     * Resolves the {@link QuestDefinition} for the given quest instance from the registry.
     * The result is resolved once per event handler invocation and captured in the
     * per-player placeholder lambda to avoid a registry lookup per in-scope player.
     *
     * @param quest the quest instance to resolve a definition for
     * @return the definition, or empty if it has already been deregistered (e.g. ephemeral quest)
     */
    @NotNull
    private Optional<QuestDefinition> resolveDefinition(@NotNull QuestInstance quest) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        return definitionRegistry.get(quest.getQuestKey());
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

    /**
     * Produces a human-readable display name from a raw quest key as a safety fallback
     * for the rare case when no {@link QuestDefinition} can be resolved at runtime.
     * Strips the {@code mcrpg:} namespace prefix if present and replaces underscores with spaces.
     *
     * @param rawKey the raw namespaced key string (e.g. {@code "mcrpg:choose_path"})
     * @return a display-ready string (e.g. {@code "choose path"})
     */
    @NotNull
    private String formatKeyAsDisplayName(@NotNull String rawKey) {
        String key = rawKey.contains(":") ? rawKey.substring(rawKey.indexOf(':') + 1) : rawKey;
        return key.replace('_', ' ');
    }
}
