package us.eunoians.mcrpg.event.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.QuestSource;

/**
 * Cancellable event fired from {@link us.eunoians.mcrpg.quest.QuestManager#startQuest} before
 * a new {@link us.eunoians.mcrpg.quest.impl.QuestInstance} is created.
 * <p>
 * Cancelling this event prevents the quest from starting — no instance is created, no scope is
 * allocated, and the starting player receives no feedback from the quest system. Third-party
 * plugins that need to gate quest starts (e.g., tutorial opt-out, prerequisite checks, cooldown
 * overrides) should listen at {@link org.bukkit.event.EventPriority#NORMAL} or lower so that
 * {@link org.bukkit.event.EventPriority#MONITOR} listeners see the final cancellation state.
 */
public class PreQuestStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final QuestDefinition definition;
    private final Player player;
    private final QuestSource source;
    private boolean cancelled;

    /**
     * Creates a new pre-quest-start event.
     *
     * @param definition the quest definition about to be instantiated
     * @param player     the player initiating or being assigned the quest
     * @param source     the source that originated this quest start
     */
    public PreQuestStartEvent(@NotNull QuestDefinition definition,
                              @NotNull Player player,
                              @NotNull QuestSource source) {
        this.definition = definition;
        this.player = player;
        this.source = source;
    }

    /**
     * Gets the quest definition that would be instantiated if this event is not cancelled.
     *
     * @return the quest definition
     */
    @NotNull
    public QuestDefinition getDefinition() {
        return definition;
    }

    /**
     * Gets the player who is initiating or being assigned this quest.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the source that originated this quest start.
     *
     * @return the quest source
     */
    @NotNull
    public QuestSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
