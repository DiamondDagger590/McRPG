package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerAdvancementDoneEvent} for advancement completion objectives.
 */
public class AdvancementCompleteQuestContext extends QuestObjectiveProgressContext {

    private final PlayerAdvancementDoneEvent advancementEvent;

    public AdvancementCompleteQuestContext(@NotNull PlayerAdvancementDoneEvent advancementEvent) {
        this.advancementEvent = advancementEvent;
    }

    /**
     * Gets the underlying advancement done event.
     *
     * @return the player advancement done event
     */
    @NotNull
    public PlayerAdvancementDoneEvent getAdvancementEvent() {
        return advancementEvent;
    }
}
