package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link BlockBreakEvent} for block break objectives.
 */
public class BlockBreakQuestContext extends QuestObjectiveProgressContext {

    private final BlockBreakEvent blockBreakEvent;

    public BlockBreakQuestContext(@NotNull BlockBreakEvent blockBreakEvent) {
        this.blockBreakEvent = blockBreakEvent;
    }

    /**
     * Gets the underlying block break event.
     *
     * @return the block break event
     */
    @NotNull
    public BlockBreakEvent getBlockBreakEvent() {
        return blockBreakEvent;
    }
}
