package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.HarvestCropQuestContext;

/**
 * Listens for {@link PlayerHarvestBlockEvent} and drives quest objective progress for any
 * active crop-harvest objectives the harvesting player is contributing to.
 */
public class HarvestCropQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public HarvestCropQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a crop harvest event by progressing any matching quest objectives
     * for the player who harvested the crop.
     *
     * @param event the harvest block event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHarvestCrop(PlayerHarvestBlockEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new HarvestCropQuestContext(event));
    }
}
