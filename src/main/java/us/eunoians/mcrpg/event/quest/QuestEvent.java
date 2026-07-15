package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Base event for all quest-related events. Carries a reference to the {@link QuestInstance}
 * that the event pertains to.
 * <p>
 * This abstract base owns the single shared {@link HandlerList} for the whole quest-event
 * hierarchy; concrete subclasses inherit it and must <b>not</b> redeclare their own. This mirrors
 * Bukkit's own idiom (e.g. {@code EntityDamageEvent} holds the list and {@code
 * EntityDamageByEntityEvent} inherits it): a listener registered against a specific subclass is
 * filtered by Bukkit's {@code isAssignableFrom} executor guard and only receives that subtype,
 * while a listener registered against {@code QuestEvent} itself receives <b>every</b> quest event.
 * A subclass declaring its own list would silently break out of that shared dispatch — the bug that
 * previously hid {@link QuestCancelEvent}s from base-type listeners.
 */
public abstract class QuestEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

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

    /**
     * Gets the shared handler list for the quest-event hierarchy.
     *
     * @return the shared handler list
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the shared handler list for the quest-event hierarchy. Bukkit requires this static
     * accessor for event registration; every concrete subclass inherits it.
     *
     * @return the shared handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
