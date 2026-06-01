package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.CraftItemQuestContext;

/**
 * Listens for {@link CraftItemEvent} and drives quest objective progress for any
 * active item-crafting objectives the crafting player is contributing to.
 */
public class CraftItemQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public CraftItemQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a craft item event by progressing any matching quest objectives
     * for the player who crafted the item.
     *
     * @param event the craft item event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new CraftItemQuestContext(event));
    }
}
