package us.eunoians.mcrpg.event.board;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.QuestBoard;

/**
 * Fired when a player attempts to accept a board offering. This event is
 * {@link Cancellable} -- cancelling it prevents the acceptance from proceeding.
 */
public class BoardOfferingAcceptEvent extends BoardEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final BoardOffering offering;
    private boolean cancelled;

    /**
     * Creates a new offering accept event.
     *
     * @param board    the board the offering belongs to
     * @param player   the player accepting the offering
     * @param offering the offering being accepted
     */
    public BoardOfferingAcceptEvent(@NotNull QuestBoard board,
                                    @NotNull Player player,
                                    @NotNull BoardOffering offering) {
        super(board);
        this.player = player;
        this.offering = offering;
    }

    /**
     * Gets the player accepting the offering.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the offering being accepted.
     *
     * @return the board offering
     */
    @NotNull
    public BoardOffering getOffering() {
        return offering;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
