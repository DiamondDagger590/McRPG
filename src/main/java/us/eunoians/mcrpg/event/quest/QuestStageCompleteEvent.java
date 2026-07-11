package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

/**
 * Fired when a stage instance transitions to {@code COMPLETED} (all objectives done).
 * <p>
 * Internal listeners use this to check if the parent phase should advance.
 */
public class QuestStageCompleteEvent extends QuestEvent {

    private final QuestStageInstance stageInstance;

    /**
     * Creates a new stage complete event.
     *
     * @param questInstance the parent quest instance
     * @param stageInstance the stage that completed
     */
    public QuestStageCompleteEvent(@NotNull QuestInstance questInstance,
                                    @NotNull QuestStageInstance stageInstance) {
        super(questInstance);
        this.stageInstance = stageInstance;
    }

    /**
     * Gets the stage instance that completed.
     *
     * @return the completed stage instance
     */
    @NotNull
    public QuestStageInstance getStageInstance() {
        return stageInstance;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Gets the handler list for this event.
     *
     * @return the handler list
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static handler list for this event, used by Bukkit for registration.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
