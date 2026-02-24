package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Fired when a phase's completion criteria are met (either ANY or ALL stages completed,
 * depending on the phase's {@link us.eunoians.mcrpg.quest.definition.PhaseCompletionMode}).
 * <p>
 * Internal listeners use this to activate the next phase or complete the quest.
 */
public class QuestPhaseCompleteEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    private final QuestPhaseDefinition phaseDefinition;
    private final int completedPhaseIndex;

    /**
     * Creates a new phase complete event.
     *
     * @param questInstance       the parent quest instance
     * @param phaseDefinition     the definition of the phase that completed
     * @param completedPhaseIndex the zero-based index of the completed phase
     */
    public QuestPhaseCompleteEvent(@NotNull QuestInstance questInstance,
                                    @NotNull QuestPhaseDefinition phaseDefinition,
                                    int completedPhaseIndex) {
        super(questInstance);
        this.phaseDefinition = phaseDefinition;
        this.completedPhaseIndex = completedPhaseIndex;
    }

    /**
     * Gets the definition of the phase that completed.
     *
     * @return the phase definition
     */
    @NotNull
    public QuestPhaseDefinition getPhaseDefinition() {
        return phaseDefinition;
    }

    /**
     * Gets the zero-based index of the completed phase within the quest.
     *
     * @return the completed phase index
     */
    public int getCompletedPhaseIndex() {
        return completedPhaseIndex;
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
