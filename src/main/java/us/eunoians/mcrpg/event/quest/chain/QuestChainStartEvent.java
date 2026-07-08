package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.UUID;

/**
 * Fired when a quest chain starts for a player — the first step quest has been started.
 * <p>
 * Non-cancellable: the chain manager fires this event after the first step quest has
 * already been submitted to {@link us.eunoians.mcrpg.quest.QuestManager}. External
 * plugins may use this event for analytics or side-effect logic only.
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
     * Gets the UUID of the player for whom the chain started.
     * Convenience accessor for cases where the UUID is needed without the live {@link Player} reference.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return player.getUniqueId();
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
