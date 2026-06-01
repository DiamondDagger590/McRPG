package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.inventory.CraftItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link CraftItemEvent} for craft item objectives.
 */
public class CraftItemQuestContext extends QuestObjectiveProgressContext {

    private final CraftItemEvent craftItemEvent;

    public CraftItemQuestContext(@NotNull CraftItemEvent craftItemEvent) {
        this.craftItemEvent = craftItemEvent;
    }

    /**
     * Gets the underlying craft item event.
     *
     * @return the craft item event
     */
    @NotNull
    public CraftItemEvent getCraftItemEvent() {
        return craftItemEvent;
    }
}
