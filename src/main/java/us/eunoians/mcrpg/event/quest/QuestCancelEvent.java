package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Fired when a quest instance is cancelled (manually or due to expiration).
 */
public class QuestCancelEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Creates a new quest cancel event.
     *
     * @param questInstance the quest instance that was cancelled
     */
    public QuestCancelEvent(@NotNull QuestInstance questInstance) {
        super(questInstance);
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
