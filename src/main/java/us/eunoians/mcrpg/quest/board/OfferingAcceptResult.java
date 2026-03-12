package us.eunoians.mcrpg.quest.board;

/**
 * The result of a {@link QuestBoardManager#acceptOffering} call, allowing callers to
 * distinguish between different failure reasons and provide targeted player feedback.
 */
public enum OfferingAcceptResult {

    /**
     * The offering was accepted and the quest was started successfully.
     */
    ACCEPTED,

    /**
     * The player has reached their active quest slot limit for this board.
     */
    SLOTS_FULL,

    /**
     * The offering no longer exists or is in a state that cannot be accepted
     * (e.g., already taken by another player, expired, or not on the current board).
     */
    NOT_AVAILABLE,

    /**
     * Acceptance was blocked by a {@link us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent}
     * listener (external plugin veto or server-side guard).
     */
    CANCELLED_BY_EVENT,

    /**
     * The quest definition associated with the offering could not be resolved.
     * This is an internal error state and should not normally be presented to players.
     */
    DEFINITION_NOT_FOUND;

    /**
     * Returns {@code true} if this result represents a successful acceptance.
     *
     * @return {@code true} for {@link #ACCEPTED}, {@code false} for all failure results
     */
    public boolean isAccepted() {
        return this == ACCEPTED;
    }
}
