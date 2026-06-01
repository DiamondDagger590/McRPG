package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.SmeltItemQuestContext;

/**
 * Listens for {@link FurnaceExtractEvent} and drives quest objective progress for any
 * active item-smelting objectives the extracting player is contributing to.
 * <p>
 * {@link FurnaceExtractEvent} is not cancellable, so {@code ignoreCancelled} is omitted
 * from the event handler annotation.
 */
public class SmeltItemQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public SmeltItemQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a furnace extract event by progressing any matching quest objectives
     * for the player who extracted the smelted item.
     *
     * @param event the furnace extract event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSmeltItem(FurnaceExtractEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new SmeltItemQuestContext(event));
    }
}
