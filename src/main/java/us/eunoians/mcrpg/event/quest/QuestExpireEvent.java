package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Fired when a quest instance expires due to its expiration time being reached.
 * The quest will be cancelled after this event fires.
 */
public class QuestExpireEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();

    /**
     * Creates a new quest expire event.
     *
     * @param questInstance the quest instance that expired
     */
    public QuestExpireEvent(@NotNull QuestInstance questInstance) {
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
