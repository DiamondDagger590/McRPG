package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.FertilizeBlockQuestContext;

/**
 * Listens for {@link BlockFertilizeEvent} and drives quest objective progress for any
 * active block-fertilize objectives the fertilizing player is contributing to.
 * <p>
 * Dispensers can trigger fertilization without a player, so this listener guards against
 * a {@code null} player before progressing quests.
 */
public class FertilizeBlockQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public FertilizeBlockQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a block fertilize event by progressing any matching quest objectives
     * for the player who fertilized the block, if a player was responsible.
     *
     * @param event the block fertilize event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFertilizeBlock(BlockFertilizeEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        progressQuests(questManager, event.getPlayer().getUniqueId(), new FertilizeBlockQuestContext(event));
    }
}
