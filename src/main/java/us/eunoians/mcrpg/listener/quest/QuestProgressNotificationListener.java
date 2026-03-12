package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.QuestObjectiveProgressEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.QuestProgressNotificationSetting;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Sends a chat notification to the contributing player whenever they cross a
 * configured objective completion percentage threshold during a quest.
 * <p>
 * Thresholds are defined server-side in {@code board.yml} (defaults: 25, 50, 75 %).
 * Players can suppress these notifications via {@link QuestProgressNotificationSetting}.
 */
public class QuestProgressNotificationListener implements Listener {

    /**
     * Checks whether the progress delta crosses any configured threshold and, if so,
     * sends the contributing player a localized notification — provided their setting
     * is enabled.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onObjectiveProgress(@NotNull QuestObjectiveProgressEvent event) {
        UUID contributingPlayer = event.getContributingPlayer();
        if (contributingPlayer == null) {
            return;
        }

        McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);
        Optional<McRPGPlayer> playerOpt = playerManager.getPlayer(contributingPlayer);
        if (playerOpt.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();

        // Respect the player's notification preference
        var settingOpt = mcRPGPlayer.getPlayerSetting(QuestProgressNotificationSetting.SETTING_KEY);
        if (settingOpt.isPresent()
                && settingOpt.get() instanceof QuestProgressNotificationSetting setting
                && !setting.isEnabled()) {
            return;
        }

        QuestObjectiveInstance objectiveInstance = event.getObjectiveInstance();
        long required = objectiveInstance.getRequiredProgression();
        if (required <= 0) {
            return;
        }

        long currentProgress = objectiveInstance.getCurrentProgression();
        long progressDelta = event.getProgressDelta();

        List<Integer> thresholds = loadThresholds();
        int crossedAt = findCrossedThreshold(currentProgress, progressDelta, required, thresholds);

        if (crossedAt < 0) {
            return;
        }

        int finalCrossedAt = crossedAt;
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION);

            QuestInstance quest = event.getQuestInstance();
            String questName = resolveQuestDisplayName(quest, mcRPGPlayer);
            String objectiveName = resolveObjectiveName(event, mcRPGPlayer);

            Component message = localizationManager.getLocalizedMessageAsComponent(
                    mcRPGPlayer,
                    LocalizationKey.QUEST_OBJECTIVE_THRESHOLD_NOTIFICATION,
                    Map.of(
                            "quest_name", questName,
                            "objective_name", objectiveName,
                            "percentage", String.valueOf(finalCrossedAt)
                    ));
            player.sendMessage(message);
        });
    }

    /**
     * Finds the first configured threshold crossed by the progress delta, or {@code -1} if none.
     * A threshold is considered crossed when the old percentage was strictly below the threshold
     * and the new percentage is at or above it.
     *
     * @param currentProgress the objective's progress before the delta is applied
     * @param progressDelta   the amount of progress being added
     * @param required        the total required progression for the objective
     * @param thresholds      the list of integer percentage thresholds to check (e.g. [25, 50, 75])
     * @return the first crossed threshold value, or {@code -1} if none was crossed
     */
    @VisibleForTesting
    static int findCrossedThreshold(long currentProgress, long progressDelta, long required,
                                    @NotNull List<Integer> thresholds) {
        double oldPct = (currentProgress / (double) required) * 100.0;
        double newPct = ((currentProgress + progressDelta) / (double) required) * 100.0;
        for (int threshold : thresholds) {
            if (oldPct < threshold && newPct >= threshold) {
                return threshold;
            }
        }
        return -1;
    }

    /**
     * Loads the configured progress threshold list from {@code board.yml}.
     * Returns the default list {@code [25, 50, 75]} if the config entry is missing.
     *
     * @return list of integer percentage thresholds (e.g. [25, 50, 75])
     */
    @NotNull
    private List<Integer> loadThresholds() {
        YamlDocument boardConfig = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.BOARD_CONFIG);
        List<?> raw = boardConfig.getList(BoardConfigFile.PROGRESS_NOTIFICATION_THRESHOLDS);
        if (raw == null || raw.isEmpty()) {
            return List.of(25, 50, 75);
        }
        return raw.stream()
                .filter(o -> o instanceof Number)
                .map(o -> ((Number) o).intValue())
                .toList();
    }

    /**
     * Resolves the display name for the quest, using the player's locale.
     */
    @NotNull
    private String resolveQuestDisplayName(@NotNull QuestInstance quest, @NotNull McRPGPlayer player) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        return definitionRegistry.get(quest.getQuestKey())
                .map(def -> def.getDisplayName(player))
                .orElse(quest.getQuestKey().getKey());
    }

    /**
     * Resolves the localized description of the objective receiving progress.
     * Falls back to the objective key string if the definition cannot be found.
     */
    @NotNull
    private String resolveObjectiveName(@NotNull QuestObjectiveProgressEvent event,
                                        @NotNull McRPGPlayer player) {
        QuestDefinitionRegistry definitionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_DEFINITION);
        QuestInstance quest = event.getQuestInstance();
        Optional<QuestDefinition> defOpt = definitionRegistry.get(quest.getQuestKey());
        if (defOpt.isEmpty()) {
            return event.getObjectiveInstance().getQuestObjectiveKey().getKey();
        }
        QuestDefinition definition = defOpt.get();
        Optional<QuestObjectiveDefinition> objDefOpt = definition.findObjectiveDefinition(
                event.getObjectiveInstance().getQuestObjectiveKey());
        return objDefOpt
                .map(objDef -> objDef.getDescription(player, quest.getQuestKey()).split("\n")[0])
                .orElse(event.getObjectiveInstance().getQuestObjectiveKey().getKey());
    }
}
