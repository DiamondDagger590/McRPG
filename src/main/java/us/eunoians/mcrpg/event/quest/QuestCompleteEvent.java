package us.eunoians.mcrpg.event.quest;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Fired when a quest instance transitions to {@code COMPLETED} (all phases done).
 */
public class QuestCompleteEvent extends QuestEvent {

    private final QuestDefinition questDefinition;

    /**
     * Creates a new quest complete event.
     *
     * @param questInstance   the quest instance that completed
     * @param questDefinition the definition the quest was created from
     */
    public QuestCompleteEvent(@NotNull QuestInstance questInstance, @NotNull QuestDefinition questDefinition) {
        super(questInstance);
        this.questDefinition = questDefinition;
    }

    /**
     * Gets the definition the completed quest was created from.
     *
     * @return the quest definition
     */
    @NotNull
    public QuestDefinition getQuestDefinition() {
        return questDefinition;
    }
}
