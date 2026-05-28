package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.UUID;

/**
 * Progress context wrapping a {@link SkillGainLevelEvent}. Carries the skill key,
 * the number of levels gained in a single event, and the UUID of the player who leveled up.
 */
public class SkillLevelQuestContext extends QuestObjectiveProgressContext {

    private final SkillGainLevelEvent skillGainLevelEvent;

    /**
     * Creates a context from the given skill gain level event.
     *
     * @param skillGainLevelEvent the event that triggered this context
     */
    public SkillLevelQuestContext(@NotNull SkillGainLevelEvent skillGainLevelEvent) {
        this.skillGainLevelEvent = skillGainLevelEvent;
    }

    /**
     * Gets the key of the skill that gained levels.
     *
     * @return the skill's namespaced key
     */
    @NotNull
    public NamespacedKey getSkillKey() {
        return skillGainLevelEvent.getSkillKey();
    }

    /**
     * Gets the number of levels gained in this event.
     *
     * @return the levels gained
     */
    public int getLevelsGained() {
        return skillGainLevelEvent.getLevels();
    }

    /**
     * Gets the player's skill level after the level-up event.
     * <p>
     * Because {@link SkillGainLevelEvent} fires after the level cache has been recalculated,
     * the skill holder's current level at event-fire time is the post-gain level. This method
     * reads that value directly from the holder, avoiding a registry lookup in objective types.
     *
     * @return the new current skill level, or {@code 0} if the skill data is unavailable
     */
    public int getNewLevel() {
        return skillGainLevelEvent.getSkillHolder()
                .getSkillHolderData(skillGainLevelEvent.getSkillKey())
                .map(SkillHolder.SkillHolderData::getCurrentLevel)
                .orElseGet(() -> {
                    McRPG.getInstance().getLogger().warning(
                            "Skill data unavailable for skill '" + skillGainLevelEvent.getSkillKey()
                                    + "' on holder " + skillGainLevelEvent.getSkillHolder().getUUID()
                                    + " — returning 0 for getNewLevel()");
                    return 0;
                });
    }

    /**
     * Gets the UUID of the player who gained skill levels.
     *
     * @return the player's UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return skillGainLevelEvent.getSkillHolder().getUUID();
    }
}
