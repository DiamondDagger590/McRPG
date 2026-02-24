package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

/**
 * Fired when an objective instance transitions to {@code COMPLETED}.
 * <p>
 * Internal listeners use this to check if the parent stage should also complete.
 */
public class QuestObjectiveCompleteEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    private final QuestStageInstance stageInstance;
    private final QuestObjectiveInstance objectiveInstance;

    /**
     * Creates a new objective complete event.
     *
     * @param questInstance     the parent quest instance
     * @param stageInstance     the parent stage instance
     * @param objectiveInstance the objective that completed
     */
    public QuestObjectiveCompleteEvent(@NotNull QuestInstance questInstance,
                                       @NotNull QuestStageInstance stageInstance,
                                       @NotNull QuestObjectiveInstance objectiveInstance) {
        super(questInstance);
        this.stageInstance = stageInstance;
        this.objectiveInstance = objectiveInstance;
    }

    /**
     * Gets the stage instance that contains the completed objective.
     *
     * @return the parent stage instance
     */
    @NotNull
    public QuestStageInstance getStageInstance() {
        return stageInstance;
    }

    /**
     * Gets the objective instance that completed.
     *
     * @return the completed objective instance
     */
    @NotNull
    public QuestObjectiveInstance getObjectiveInstance() {
        return objectiveInstance;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event type.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
