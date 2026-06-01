package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link BlockPlaceEvent} for block place objectives.
 */
public class BlockPlaceQuestContext extends QuestObjectiveProgressContext {

    private final BlockPlaceEvent blockPlaceEvent;

    public BlockPlaceQuestContext(@NotNull BlockPlaceEvent blockPlaceEvent) {
        this.blockPlaceEvent = blockPlaceEvent;
    }

    /**
     * Gets the underlying block place event.
     *
     * @return the block place event
     */
    @NotNull
    public BlockPlaceEvent getBlockPlaceEvent() {
        return blockPlaceEvent;
    }
}
