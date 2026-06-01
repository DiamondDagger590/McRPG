package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerExpChangeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerExpChangeEvent} for gain experience objectives.
 */
public class GainExperienceQuestContext extends QuestObjectiveProgressContext {

    private final PlayerExpChangeEvent expChangeEvent;

    public GainExperienceQuestContext(@NotNull PlayerExpChangeEvent expChangeEvent) {
        this.expChangeEvent = expChangeEvent;
    }

    /**
     * Gets the underlying experience change event.
     *
     * @return the player exp change event
     */
    @NotNull
    public PlayerExpChangeEvent getExpChangeEvent() {
        return expChangeEvent;
    }
}
