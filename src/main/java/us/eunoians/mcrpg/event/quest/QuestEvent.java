package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Base event for all quest-related events. Carries a reference to the {@link QuestInstance}
 * that the event pertains to.
 */
public abstract class QuestEvent extends Event {

    private final QuestInstance questInstance;

    /**
     * Creates a new quest event.
     *
     * @param questInstance the quest instance associated with this event
     */
    public QuestEvent(@NotNull QuestInstance questInstance) {
        this.questInstance = questInstance;
    }

    /**
     * Gets the {@link QuestInstance} associated with this event.
     *
     * @return the quest instance
     */
    @NotNull
    public QuestInstance getQuestInstance() {
        return questInstance;
    }
}
