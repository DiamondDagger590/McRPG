package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.block.BlockFertilizeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link BlockFertilizeEvent} for fertilize block objectives.
 */
public class FertilizeBlockQuestContext extends QuestObjectiveProgressContext {

    private final BlockFertilizeEvent fertilizeEvent;

    public FertilizeBlockQuestContext(@NotNull BlockFertilizeEvent fertilizeEvent) {
        this.fertilizeEvent = fertilizeEvent;
    }

    /**
     * Gets the underlying block fertilize event.
     *
     * @return the block fertilize event
     */
    @NotNull
    public BlockFertilizeEvent getFertilizeEvent() {
        return fertilizeEvent;
    }
}
