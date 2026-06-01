package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.BucketFillQuestContext;

/**
 * Listens for {@link PlayerBucketFillEvent} and drives quest objective progress for any
 * active bucket-fill objectives the filling player is contributing to.
 */
public class BucketFillQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public BucketFillQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a bucket fill event by progressing any matching quest objectives
     * for the player who filled the bucket.
     *
     * @param event the player bucket fill event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new BucketFillQuestContext(event));
    }
}
