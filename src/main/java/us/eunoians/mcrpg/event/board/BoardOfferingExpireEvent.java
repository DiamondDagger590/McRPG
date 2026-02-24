package us.eunoians.mcrpg.event.board;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;

import java.util.UUID;

/**
 * Fired when offerings from a previous rotation are expired as part of a
 * new rotation cycle. This event is fired asynchronously from the database
 * executor thread.
 */
public class BoardOfferingExpireEvent extends BoardEvent {

    private static final HandlerList handlers = new HandlerList();

    private final BoardRotation expiredRotation;

    /**
     * Creates a new offering expire event.
     *
     * @param board           the board whose offerings expired
     * @param expiredRotation the rotation whose offerings were expired
     */
    public BoardOfferingExpireEvent(@NotNull QuestBoard board,
                                    @NotNull BoardRotation expiredRotation) {
        super(board);
        this.expiredRotation = expiredRotation;
    }

    /**
     * Gets the rotation whose offerings were expired.
     *
     * @return the expired rotation
     */
    @NotNull
    public BoardRotation getExpiredRotation() {
        return expiredRotation;
    }

    /**
     * Gets the rotation ID of the expired rotation.
     *
     * @return the expired rotation UUID
     */
    @NotNull
    public UUID getExpiredRotationId() {
        return expiredRotation.getRotationId();
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
