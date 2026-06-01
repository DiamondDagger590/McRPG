package us.eunoians.mcrpg.listener.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.ItemPickupQuestContext;

/**
 * Listens for {@link EntityPickupItemEvent} and drives quest objective progress for any
 * active item-pickup objectives the picking-up player is contributing to.
 * <p>
 * Only events where the picking entity is a {@link Player} are processed.
 */
public class ItemPickupQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public ItemPickupQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an entity pickup item event by progressing any matching quest objectives
     * for the player who picked up the item.
     *
     * @param event the entity pickup item event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        progressQuests(questManager, player.getUniqueId(), new ItemPickupQuestContext(event));
    }
}
