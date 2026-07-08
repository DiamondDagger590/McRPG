package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.SkillLevelQuestContext;

/**
 * Listens for {@link SkillGainLevelEvent} and drives quest objective progress for skill level-up objectives.
 */
public class SkillLevelQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Constructs a new {@link SkillLevelQuestProgressListener}.
     *
     * @param questManager the {@link QuestManager} used to drive quest progress
     */
    public SkillLevelQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles {@link SkillGainLevelEvent} to progress any active skill level-up quest objectives
     * for the skill holder that gained a level.
     *
     * @param event the {@link SkillGainLevelEvent} that fired
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSkillGainLevel(SkillGainLevelEvent event) {
        progressQuests(questManager, event.getSkillHolder().getUUID(), new SkillLevelQuestContext(event));
    }
}
