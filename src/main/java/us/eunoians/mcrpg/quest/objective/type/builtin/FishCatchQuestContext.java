package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerFishEvent} for fish catch objectives.
 * <p>
 * Pre-extracts the caught item stack so the objective type doesn't need to re-cast
 * the caught entity.
 */
public class FishCatchQuestContext extends QuestObjectiveProgressContext {

    private final PlayerFishEvent fishEvent;
    private final ItemStack caughtItem;

    public FishCatchQuestContext(@NotNull PlayerFishEvent fishEvent, @NotNull ItemStack caughtItem) {
        this.fishEvent = fishEvent;
        this.caughtItem = caughtItem;
    }

    /**
     * Gets the underlying fish event.
     *
     * @return the player fish event
     */
    @NotNull
    public PlayerFishEvent getFishEvent() {
        return fishEvent;
    }

    /**
     * Gets the item stack that was caught.
     *
     * @return the caught item stack
     */
    @NotNull
    public ItemStack getCaughtItem() {
        return caughtItem;
    }
}
