package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Fired when a quest instance is started (transitions to {@code IN_PROGRESS} and
 * the first phase's stages are activated).
 */
public class QuestStartEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    private final QuestDefinition questDefinition;

    /**
     * Creates a new quest start event.
     *
     * @param questInstance   the quest instance that was started
     * @param questDefinition the definition the quest was created from
     */
    public QuestStartEvent(@NotNull QuestInstance questInstance, @NotNull QuestDefinition questDefinition) {
        super(questInstance);
        this.questDefinition = questDefinition;
    }

    /**
     * Gets the definition the quest was created from.
     *
     * @return the quest definition
     */
    @NotNull
    public QuestDefinition getQuestDefinition() {
        return questDefinition;
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
