package us.eunoians.mcrpg.event.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;

/**
 * Fired when all steps in a quest chain have been completed by a player.
 * <p>
 * The {@code completionNumber} reflects how many times this player has now completed the
 * chain (1 on first completion, 2 on second, etc.), allowing repeat-mode handling and
 * analytics to use a single event type.
 */
public class QuestChainCompleteEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final int completionNumber;

    /**
     * Creates a new chain-complete event.
     *
     * @param chainDefinition  the definition of the completed chain
     * @param player           the player who completed the chain
     * @param completionNumber how many times the player has now completed this chain
     */
    public QuestChainCompleteEvent(@NotNull QuestChainDefinition chainDefinition,
                                   @NotNull Player player,
                                   int completionNumber) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.completionNumber = completionNumber;
    }

    /**
     * Gets the definition of the completed chain.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player who completed the chain.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets how many times the player has now completed this chain.
     *
     * @return the completion number (≥ 1)
     */
    public int getCompletionNumber() {
        return completionNumber;
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
