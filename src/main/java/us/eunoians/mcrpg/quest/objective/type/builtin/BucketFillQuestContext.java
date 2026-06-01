package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.player.PlayerBucketFillEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping a {@link PlayerBucketFillEvent} for bucket fill objectives.
 */
public class BucketFillQuestContext extends QuestObjectiveProgressContext {

    private final PlayerBucketFillEvent bucketFillEvent;

    public BucketFillQuestContext(@NotNull PlayerBucketFillEvent bucketFillEvent) {
        this.bucketFillEvent = bucketFillEvent;
    }

    /**
     * Gets the underlying bucket fill event.
     *
     * @return the player bucket fill event
     */
    @NotNull
    public PlayerBucketFillEvent getBucketFillEvent() {
        return bucketFillEvent;
    }
}
