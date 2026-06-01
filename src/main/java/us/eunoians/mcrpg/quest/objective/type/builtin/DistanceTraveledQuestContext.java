package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerMoveEvent} for distance traveled objectives.
 * <p>
 * Pre-computes the block distance between the from and to locations so the objective
 * type doesn't need to recalculate.
 */
public class DistanceTraveledQuestContext extends QuestObjectiveProgressContext {

    private final PlayerMoveEvent moveEvent;
    private final long blockDistance;

    public DistanceTraveledQuestContext(@NotNull PlayerMoveEvent moveEvent, long blockDistance) {
        this.moveEvent = moveEvent;
        this.blockDistance = blockDistance;
    }

    /**
     * Gets the underlying move event.
     *
     * @return the player move event
     */
    @NotNull
    public PlayerMoveEvent getMoveEvent() {
        return moveEvent;
    }

    /**
     * Gets the pre-computed block distance between the from and to locations.
     *
     * @return the block distance traveled
     */
    public long getBlockDistance() {
        return blockDistance;
    }
}
