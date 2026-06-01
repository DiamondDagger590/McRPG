package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.AnvilRepairQuestContext;

/**
 * Listens for {@link InventoryClickEvent} on anvil result slots and drives quest objective
 * progress for any active anvil-repair objectives the repairing player is contributing to.
 * <p>
 * Only clicks on the result slot (raw slot 2) of an {@link InventoryType#ANVIL} inventory
 * with a non-empty result item are processed.
 */
public class AnvilRepairQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public AnvilRepairQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an inventory click event by progressing any matching quest objectives
     * for the player who took an item from an anvil's result slot.
     *
     * @param event the inventory click event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnvilRepair(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        if (event.getRawSlot() != 2) {
            return;
        }
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new AnvilRepairQuestContext(event));
    }
}
