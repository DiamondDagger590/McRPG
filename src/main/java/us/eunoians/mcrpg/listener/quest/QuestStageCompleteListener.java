package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestPhaseCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestStageCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Listens for {@link QuestStageCompleteEvent} and cascades to the parent phase:
 * checks the phase completion mode (ANY/ALL), cancels sibling stages when appropriate,
 * and fires {@link QuestPhaseCompleteEvent} if the phase is done.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so external listeners can react first.
 */
public class QuestStageCompleteListener implements Listener {

    /**
     * Handles stage completion: looks up the phase definition, checks if the phase
     * is complete based on its {@link PhaseCompletionMode}, cancels incomplete
     * sibling stages for ANY-mode phases, and fires {@link QuestPhaseCompleteEvent}.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onStageComplete(@NotNull QuestStageCompleteEvent event) {
        QuestInstance quest = event.getQuestInstance();
        if (quest.getQuestState() != QuestState.IN_PROGRESS) {
            return;
        }

        QuestStageInstance stage = event.getStageInstance();
        int phaseIndex = stage.getPhaseIndex();
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);

        questManager.getQuestDefinition(quest.getQuestKey()).ifPresent(definition -> {
            definition.findStageDefinition(stage.getStageKey())
                    .flatMap(stageDef -> stageDef.getRewardDistribution())
                    .ifPresent(config -> {
                        Map<UUID, Long> contributions = QuestContributionAggregator.fromStage(stage);
                        Set<UUID> groupMembers = quest.getQuestScope()
                                .map(scope -> scope.getCurrentPlayersInScope())
                                .orElse(Set.of());
                        QuestCompleteListener.resolveAndGrantDistribution(config, contributions, groupMembers, quest);
                    });

            definition.getPhase(phaseIndex).ifPresent(phaseDef -> {
                if (isPhaseComplete(quest, phaseDef)) {
                    if (phaseDef.getCompletionMode() == PhaseCompletionMode.ANY) {
                        cancelIncompleteSiblingStages(quest, phaseIndex);
                    }
                    Bukkit.getPluginManager().callEvent(new QuestPhaseCompleteEvent(quest, phaseDef, phaseIndex));
                }
            });
        });
    }

    private boolean isPhaseComplete(@NotNull QuestInstance quest, @NotNull QuestPhaseDefinition phaseDef) {
        List<QuestStageInstance> phaseStages = quest.getStagesForPhase(phaseDef.getPhaseIndex());

        if (phaseDef.getCompletionMode() == PhaseCompletionMode.ANY) {
            return phaseStages.stream()
                    .anyMatch(s -> s.getQuestStageState() == QuestStageState.COMPLETED);
        } else {
            return phaseStages.stream()
                    .allMatch(s -> s.getQuestStageState() == QuestStageState.COMPLETED);
        }
    }

    private void cancelIncompleteSiblingStages(@NotNull QuestInstance quest, int phaseIndex) {
        for (QuestStageInstance stage : quest.getStagesForPhase(phaseIndex)) {
            if (stage.getQuestStageState() != QuestStageState.COMPLETED) {
                stage.cancel();
            }
        }
    }
}
