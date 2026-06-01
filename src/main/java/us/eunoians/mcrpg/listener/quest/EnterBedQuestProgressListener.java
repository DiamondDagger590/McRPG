package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.EnterBedQuestContext;

/**
 * Listens for {@link PlayerBedEnterEvent} and drives quest objective progress for any
 * active bed-enter objectives the player is contributing to.
 * <p>
 * Only successful bed entries ({@link PlayerBedEnterEvent.BedEnterResult#OK}) are counted.
 * {@link PlayerBedEnterEvent} is not cancellable in the traditional sense, so
 * {@code ignoreCancelled} is omitted from the event handler annotation.
 */
public class EnterBedQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public EnterBedQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a bed enter event by progressing any matching quest objectives
     * for the player who entered the bed, provided the entry was successful.
     *
     * @param event the bed enter event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnterBed(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }
        progressQuests(questManager, event.getPlayer().getUniqueId(), new EnterBedQuestContext(event));
    }
}
