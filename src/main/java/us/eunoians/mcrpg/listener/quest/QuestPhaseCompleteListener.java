package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestPhaseCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Listens for {@link QuestPhaseCompleteEvent} and either activates the next phase
 * or completes the quest if this was the last phase.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so external listeners can react first.
 */
public class QuestPhaseCompleteListener implements Listener {

    /**
     * Handles phase completion: looks up the definition, determines whether there is a
     * subsequent phase to activate or whether the quest should be completed.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPhaseComplete(@NotNull QuestPhaseCompleteEvent event) {
        QuestInstance quest = event.getQuestInstance();
        if (quest.getQuestState() != QuestState.IN_PROGRESS) {
            return;
        }

        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);

        questManager.getQuestDefinition(quest.getQuestKey()).ifPresent(definition -> {
            int completedPhaseIndex = event.getCompletedPhaseIndex();
            definition.getPhase(completedPhaseIndex).ifPresent(phaseDef ->
                    phaseDef.getRewardDistribution().ifPresent(config -> {
                        Map<UUID, Long> contributions = QuestContributionAggregator.fromPhase(quest, completedPhaseIndex);
                        Set<UUID> groupMembers = quest.getQuestScope()
                                .map(scope -> scope.getCurrentPlayersInScope())
                                .orElse(Set.of());
                        QuestCompleteListener.resolveAndGrantDistribution(config, contributions, groupMembers, quest);
                    })
            );

            int nextPhaseIndex = completedPhaseIndex + 1;
            if (definition.hasPhase(nextPhaseIndex)) {
                for (QuestStageInstance stage : quest.getStagesForPhase(nextPhaseIndex)) {
                    stage.activate();
                }
            } else {
                quest.complete(definition);
            }
        });
    }
}
