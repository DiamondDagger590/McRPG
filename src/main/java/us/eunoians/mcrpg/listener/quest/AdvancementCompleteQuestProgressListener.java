package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.AdvancementCompleteQuestContext;

/**
 * Listens for {@link PlayerAdvancementDoneEvent} and drives quest objective progress for any
 * active advancement-completion objectives the completing player is contributing to.
 * <p>
 * Internal advancements (recipes and other advancements without a display) are skipped.
 * {@link PlayerAdvancementDoneEvent} is not cancellable, so {@code ignoreCancelled} is omitted
 * from the event handler annotation.
 */
public class AdvancementCompleteQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public AdvancementCompleteQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an advancement done event by progressing any matching quest objectives
     * for the player who completed the advancement, skipping internal advancements
     * that have no display.
     *
     * @param event the player advancement done event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAdvancementComplete(PlayerAdvancementDoneEvent event) {
        if (event.getAdvancement().getDisplay() == null) {
            return;
        }
        progressQuests(questManager, event.getPlayer().getUniqueId(), new AdvancementCompleteQuestContext(event));
    }
}
