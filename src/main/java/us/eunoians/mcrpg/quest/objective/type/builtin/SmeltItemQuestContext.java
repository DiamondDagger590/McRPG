package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link FurnaceExtractEvent} for smelt item objectives.
 */
public class SmeltItemQuestContext extends QuestObjectiveProgressContext {

    private final FurnaceExtractEvent furnaceExtractEvent;

    public SmeltItemQuestContext(@NotNull FurnaceExtractEvent furnaceExtractEvent) {
        this.furnaceExtractEvent = furnaceExtractEvent;
    }

    /**
     * Gets the underlying furnace extract event.
     *
     * @return the furnace extract event
     */
    @NotNull
    public FurnaceExtractEvent getFurnaceExtractEvent() {
        return furnaceExtractEvent;
    }
}
