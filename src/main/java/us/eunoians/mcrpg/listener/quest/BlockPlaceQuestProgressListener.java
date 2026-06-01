package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockPlaceQuestContext;

/**
 * Listens for {@link BlockPlaceEvent} and drives quest objective progress for any
 * active block-place objectives the placing player is contributing to.
 */
public class BlockPlaceQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public BlockPlaceQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a block place event by progressing any matching quest objectives
     * for the player who placed the block.
     *
     * @param event the block place event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new BlockPlaceQuestContext(event));
    }
}
