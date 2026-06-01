package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.ConsumeItemQuestContext;

/**
 * Listens for {@link PlayerItemConsumeEvent} and drives quest objective progress for any
 * active item-consumption objectives the consuming player is contributing to.
 */
public class ConsumeItemQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public ConsumeItemQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles an item consume event by progressing any matching quest objectives
     * for the player who consumed the item.
     *
     * @param event the item consume event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new ConsumeItemQuestContext(event));
    }
}
