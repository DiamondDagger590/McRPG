package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.QuestBoardAcceptQuestContext;

/**
 * Listens for {@link BoardOfferingAcceptEvent} and drives quest objective progress for quest board accept objectives.
 */
public class QuestBoardAcceptQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Constructs a new {@link QuestBoardAcceptQuestProgressListener}.
     *
     * @param questManager the {@link QuestManager} used to drive quest progress
     */
    public QuestBoardAcceptQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles {@link BoardOfferingAcceptEvent} to progress any active quest board accept quest objectives
     * for the player that accepted a board offering.
     *
     * @param event the {@link BoardOfferingAcceptEvent} that fired
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBoardOfferingAccept(BoardOfferingAcceptEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new QuestBoardAcceptQuestContext(event));
    }
}
