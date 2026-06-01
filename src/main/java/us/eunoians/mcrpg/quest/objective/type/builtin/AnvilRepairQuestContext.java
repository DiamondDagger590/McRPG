package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link InventoryClickEvent} on an anvil result slot
 * for anvil repair objectives.
 */
public class AnvilRepairQuestContext extends QuestObjectiveProgressContext {

    private final InventoryClickEvent inventoryClickEvent;

    public AnvilRepairQuestContext(@NotNull InventoryClickEvent inventoryClickEvent) {
        this.inventoryClickEvent = inventoryClickEvent;
    }

    /**
     * Gets the underlying inventory click event.
     *
     * @return the inventory click event
     */
    @NotNull
    public InventoryClickEvent getInventoryClickEvent() {
        return inventoryClickEvent;
    }
}
