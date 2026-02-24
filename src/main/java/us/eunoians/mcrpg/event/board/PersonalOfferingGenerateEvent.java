package us.eunoians.mcrpg.event.board;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fired after personal offerings are generated for a specific player but before
 * they are persisted to the database. Third-party plugins can modify the offering
 * list (add, remove, or replace offerings) before persistence.
 * <p>
 * This mirrors {@link BoardOfferingGenerateEvent} for personal (per-player) offerings.
 */
public class PersonalOfferingGenerateEvent extends BoardEvent {

    private static final HandlerList handlers = new HandlerList();

    private final UUID playerUUID;
    private final BoardRotation rotation;
    private final List<BoardOffering> offerings;

    /**
     * Creates a new personal offering generate event.
     *
     * @param board      the board offerings were generated for
     * @param playerUUID the UUID of the player these offerings are for
     * @param rotation   the rotation the offerings belong to
     * @param offerings  the mutable list of generated offerings
     */
    public PersonalOfferingGenerateEvent(@NotNull QuestBoard board,
                                         @NotNull UUID playerUUID,
                                         @NotNull BoardRotation rotation,
                                         @NotNull List<BoardOffering> offerings) {
        super(board);
        this.playerUUID = playerUUID;
        this.rotation = rotation;
        this.offerings = new ArrayList<>(offerings);
    }

    /**
     * Gets the UUID of the player these offerings are for.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
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
     * reflected in what gets persisted and displayed to the player.
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
