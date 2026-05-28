package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;

import java.util.OptionalLong;
import java.util.UUID;

/**
 * Listens for {@link QuestStartEvent} and immediately completes any
 * {@link QuestObjectiveState#IN_PROGRESS} objectives whose state-based
 * auto-complete check passes at the time of quest start.
 * <p>
 * State-based objective types (e.g., {@code skill_target_level}, {@code ability_unlock},
 * {@code loadout_equip}) override {@link QuestObjectiveType#checkAutoComplete(UUID)} to
 * return the progress value when the player's current state already satisfies the condition.
 * Event-only types return {@link OptionalLong#empty()} and are skipped.
 */
public class QuestStartAutoCompleteListener implements Listener {

    private final QuestManager questManager;

    /**
     * Creates a new listener.
     *
     * @param questManager the quest manager used to access active quest data
     */
    public QuestStartAutoCompleteListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Checks all IN_PROGRESS objectives in the newly started quest for auto-complete
     * eligibility. Only the initiating player's state is checked — other scope members
     * are not auto-completed on behalf of the starter.
     *
     * @param event the quest start event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestStart(@NotNull QuestStartEvent event) {
        QuestInstance instance = event.getQuestInstance();
        QuestDefinition definition = event.getQuestDefinition();

        UUID starterUUID = event.getStarterUUID();
        if (starterUUID == null) {
            return;
        }

        for (var stage : instance.getActiveQuestStages()) {
            for (QuestObjectiveInstance objective : stage.getQuestObjectives()) {
                if (objective.getQuestObjectiveState() != QuestObjectiveState.IN_PROGRESS) {
                    continue;
                }
                definition.findObjectiveDefinition(objective.getQuestObjectiveKey())
                        .ifPresent(objDef -> {
                            QuestObjectiveType type = objDef.getObjectiveType();
                            OptionalLong autoProgress = type.checkAutoComplete(starterUUID);
                            if (autoProgress.isPresent()) {
                                long progress = autoProgress.getAsLong();
                                if (progress >= objDef.getRequiredProgress()) {
                                    objective.progress(objDef.getRequiredProgress(), starterUUID);
                                }
                            }
                        });
            }
        }
    }
}
