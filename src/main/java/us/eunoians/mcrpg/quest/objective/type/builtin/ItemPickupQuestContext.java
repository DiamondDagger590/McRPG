package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EntityPickupItemEvent} for item pickup objectives.
 */
public class ItemPickupQuestContext extends QuestObjectiveProgressContext {

    private final EntityPickupItemEvent pickupEvent;

    public ItemPickupQuestContext(@NotNull EntityPickupItemEvent pickupEvent) {
        this.pickupEvent = pickupEvent;
    }

    /**
     * Gets the underlying entity pickup item event.
     *
     * @return the entity pickup item event
     */
    @NotNull
    public EntityPickupItemEvent getPickupEvent() {
        return pickupEvent;
    }
}
