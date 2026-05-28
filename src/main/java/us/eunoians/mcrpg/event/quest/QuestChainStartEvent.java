package us.eunoians.mcrpg.event.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

/**
 * Fired when a quest chain starts for a player — the first step is about to begin.
 * <p>
 * Non-cancellable: the chain manager has already validated that the chain can start
 * before this event is fired. External plugins may use this event for analytics or
 * side-effect logic only.
 */
public class QuestChainStartEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final QuestChainStep firstStep;

    /**
     * Creates a new start event.
     *
     * @param chainDefinition the definition of the chain that just started
     * @param player          the player for whom the chain started
     * @param firstStep       the first step of the chain
     */
    public QuestChainStartEvent(@NotNull QuestChainDefinition chainDefinition,
                                @NotNull Player player,
                                @NotNull QuestChainStep firstStep) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.firstStep = firstStep;
    }

    /**
     * Gets the definition of the chain that started.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player for whom the chain started.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the first step of the started chain.
     *
     * @return the first chain step
     */
    @NotNull
    public QuestChainStep getFirstStep() {
        return firstStep;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
