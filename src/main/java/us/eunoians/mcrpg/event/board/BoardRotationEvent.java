package us.eunoians.mcrpg.event.board;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;

import java.util.List;

/**
 * Fired when a board rotation completes and new offerings have been generated.
 * This event is fired asynchronously from the database executor thread.
 */
public class BoardRotationEvent extends BoardEvent {

    private static final HandlerList handlers = new HandlerList();

    private final BoardRotation rotation;
    private final List<BoardOffering> offerings;

    /**
     * Creates a new board rotation event.
     *
     * @param board     the board that was rotated
     * @param rotation  the new rotation that was created
     * @param offerings the offerings generated for this rotation
     */
    public BoardRotationEvent(@NotNull QuestBoard board,
                              @NotNull BoardRotation rotation,
                              @NotNull List<BoardOffering> offerings) {
        super(board);
        this.rotation = rotation;
        this.offerings = List.copyOf(offerings);
    }

    /**
     * Gets the rotation that was created.
     *
     * @return the new board rotation
     */
    @NotNull
    public BoardRotation getRotation() {
        return rotation;
    }

    /**
     * Gets the offerings generated for this rotation.
     *
     * @return an unmodifiable list of offerings
     */
    @NotNull
    public List<BoardOffering> getOfferings() {
        return offerings;
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
