package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.BucketEmptyQuestContext;

/**
 * Listens for {@link PlayerBucketEmptyEvent} and drives quest objective progress for any
 * active bucket-empty objectives the emptying player is contributing to.
 */
public class BucketEmptyQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Creates a new listener with the provided quest manager.
     *
     * @param questManager the quest manager used to resolve and progress active quests
     */
    public BucketEmptyQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles a bucket empty event by progressing any matching quest objectives
     * for the player who emptied the bucket.
     *
     * @param event the player bucket empty event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        progressQuests(questManager, event.getPlayer().getUniqueId(), new BucketEmptyQuestContext(event));
    }
}
