package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerBucketEmptyEvent} for bucket empty objectives.
 */
public class BucketEmptyQuestContext extends QuestObjectiveProgressContext {

    private final PlayerBucketEmptyEvent bucketEmptyEvent;

    public BucketEmptyQuestContext(@NotNull PlayerBucketEmptyEvent bucketEmptyEvent) {
        this.bucketEmptyEvent = bucketEmptyEvent;
    }

    /**
     * Gets the underlying bucket empty event.
     *
     * @return the player bucket empty event
     */
    @NotNull
    public PlayerBucketEmptyEvent getBucketEmptyEvent() {
        return bucketEmptyEvent;
    }
}
