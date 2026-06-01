package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerItemConsumeEvent} for consume item objectives.
 */
public class ConsumeItemQuestContext extends QuestObjectiveProgressContext {

    private final PlayerItemConsumeEvent consumeEvent;

    public ConsumeItemQuestContext(@NotNull PlayerItemConsumeEvent consumeEvent) {
        this.consumeEvent = consumeEvent;
    }

    /**
     * Gets the underlying consume event.
     *
     * @return the player item consume event
     */
    @NotNull
    public PlayerItemConsumeEvent getConsumeEvent() {
        return consumeEvent;
    }
}
