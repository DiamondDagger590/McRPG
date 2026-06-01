package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.EnchantItemQuestContext;

/**
 * Listens for {@link EnchantItemEvent} and drives quest objective progress for any
 * active item-enchanting objectives the enchanting player is contributing to.
 */
public class EnchantItemQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public EnchantItemQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an enchant item event by progressing any matching quest objectives
     * for the player who enchanted the item.
     *
     * @param event the enchant item event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        progressQuests(questManager, event.getEnchanter().getUniqueId(), new EnchantItemQuestContext(event));
    }
}
