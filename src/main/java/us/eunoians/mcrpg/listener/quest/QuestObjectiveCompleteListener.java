package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * Listens for {@link QuestObjectiveCompleteEvent} and cascades to the parent stage:
 * grants objective-level rewards, grants stage-level rewards and calls
 * {@link QuestStageInstance#complete()} if all objectives in the stage are done.
 * <p>
 * Runs at {@link EventPriority#MONITOR} so external listeners can react first.
 */
public class QuestObjectiveCompleteListener implements Listener {

    /**
     * Handles objective completion: checks if the parent stage is now complete,
     * grants stage-level rewards, and calls {@link QuestStageInstance#complete()} if so.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onObjectiveComplete(@NotNull QuestObjectiveCompleteEvent event) {
        QuestInstance quest = event.getQuestInstance();
        if (quest.getQuestState() != QuestState.IN_PROGRESS) {
            return;
        }

        QuestStageInstance stage = event.getStageInstance();
        QuestManager questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        Optional<QuestDefinition> definition = questManager.getQuestDefinition(quest.getQuestKey());

        definition.flatMap(def -> def.findObjectiveDefinition(event.getObjectiveInstance().getQuestObjectiveKey()))
                .ifPresent(objectiveDef -> quest.grantRewards(objectiveDef.getRewards()));

        if (stage.checkForUpdatedStatus()) {
            definition.flatMap(def -> def.findStageDefinition(stage.getStageKey()))
                    .ifPresent(stageDef -> quest.grantRewards(stageDef.getRewards()));
            stage.complete();
        }
    }
}
