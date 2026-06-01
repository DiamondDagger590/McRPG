package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.GainExperienceQuestContext;

/**
 * Listens for {@link PlayerExpChangeEvent} and drives quest objective progress for any
 * active experience-gain objectives the player is contributing to.
 * <p>
 * {@link PlayerExpChangeEvent} is not cancellable, so {@code ignoreCancelled} is omitted
 * from the event handler annotation.
 */
public class GainExperienceQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public GainExperienceQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an experience change event by progressing any matching quest objectives
     * for the player who gained experience.
     *
     * @param event the experience change event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGainExperience(PlayerExpChangeEvent event) {
        if (event.getAmount() <= 0) {
            return;
        }
        progressQuests(questManager, event.getPlayer().getUniqueId(), new GainExperienceQuestContext(event));
    }
}
