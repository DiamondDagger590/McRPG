package us.eunoians.mcrpg.event.board;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * Fired after offerings are generated for a new rotation but before they are
 * persisted to the database. Third-party plugins can modify the offering list
 * (add, remove, or replace offerings) before persistence.
 * <p>
 * This event is fired asynchronously from the database executor thread.
 */
public class BoardOfferingGenerateEvent extends BoardEvent {

    private static final HandlerList handlers = new HandlerList();

    private final BoardRotation rotation;
    private final List<BoardOffering> offerings;

    /**
     * Creates a new offering generate event.
     *
     * @param board     the board offerings were generated for
     * @param rotation  the rotation the offerings belong to
     * @param offerings the mutable list of generated offerings
     */
    public BoardOfferingGenerateEvent(@NotNull QuestBoard board,
                                      @NotNull BoardRotation rotation,
                                      @NotNull List<BoardOffering> offerings) {
        super(board);
        this.rotation = rotation;
        this.offerings = new ArrayList<>(offerings);
    }

    /**
     * Gets the rotation these offerings belong to.
     *
     * @return the board rotation
     */
    @NotNull
    public BoardRotation getRotation() {
        return rotation;
    }

    /**
     * Gets the mutable list of offerings. Modifications to this list will be
     * reflected in what gets persisted and displayed.
     *
     * @return the mutable offerings list
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
