package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Base event for all quest-related events. Carries a reference to the {@link QuestInstance}
 * that the event pertains to.
 * <p>
 * This abstract base intentionally declares <b>no</b> {@link org.bukkit.event.HandlerList} of its
 * own — every concrete subclass owns its own handler list. Registering a Bukkit listener against
 * the abstract {@code QuestEvent} type is <b>unsupported</b>: Bukkit dispatches events by concrete
 * class, so a base-type handler would never receive any quest event. Listen for the specific
 * concrete event you care about (e.g. {@link QuestCancelEvent}) instead.
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
