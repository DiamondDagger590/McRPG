package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.UUID;

/**
 * Progress context wrapping a {@link BoardOfferingAcceptEvent}. Carries the key of the
 * board from which the offering was accepted and the UUID of the accepting player.
 */
public class QuestBoardAcceptQuestContext extends QuestObjectiveProgressContext {

    private final BoardOfferingAcceptEvent boardOfferingAcceptEvent;

    /**
     * Creates a context from the given board offering accept event.
     *
     * @param boardOfferingAcceptEvent the event that triggered this context
     */
    public QuestBoardAcceptQuestContext(@NotNull BoardOfferingAcceptEvent boardOfferingAcceptEvent) {
        this.boardOfferingAcceptEvent = boardOfferingAcceptEvent;
    }

    /**
     * Gets the key of the board from which the offering was accepted.
     *
     * @return the board's namespaced key
     */
    @NotNull
    public NamespacedKey getBoardKey() {
        return boardOfferingAcceptEvent.getBoard().getBoardKey();
    }

    /**
     * Gets the UUID of the player who accepted the offering.
     *
     * @return the player's UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return boardOfferingAcceptEvent.getPlayer().getUniqueId();
    }
}
