package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.inventory.SmithItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link SmithItemEvent} for smithing objectives.
 */
public class SmithingQuestContext extends QuestObjectiveProgressContext {

    private final SmithItemEvent smithItemEvent;

    public SmithingQuestContext(@NotNull SmithItemEvent smithItemEvent) {
        this.smithItemEvent = smithItemEvent;
    }

    /**
     * Gets the underlying smith item event.
     *
     * @return the smith item event
     */
    @NotNull
    public SmithItemEvent getSmithItemEvent() {
        return smithItemEvent;
    }
}
