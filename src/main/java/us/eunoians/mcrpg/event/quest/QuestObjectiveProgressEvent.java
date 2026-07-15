package us.eunoians.mcrpg.event.quest;

import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.util.UUID;

/**
 * Fired when an objective is about to receive progress. This is the only cancellable
 * quest event -- cancelling it prevents the progress from being applied.
 * <p>
 * The progress delta can also be modified before the event completes.
 */
public class QuestObjectiveProgressEvent extends QuestEvent implements Cancellable {

    private final QuestStageInstance stageInstance;
    private final QuestObjectiveInstance objectiveInstance;
    private final UUID contributingPlayer;
    private long progressDelta;
    private boolean cancelled;

    /**
     * Creates a new objective progress event.
     *
     * @param questInstance     the parent quest instance
     * @param stageInstance     the parent stage instance
     * @param objectiveInstance the objective receiving progress
     * @param contributingPlayer the UUID of the player contributing, or {@code null}
     * @param progressDelta     the amount of progress to add
     */
    public QuestObjectiveProgressEvent(@NotNull QuestInstance questInstance,
                                       @NotNull QuestStageInstance stageInstance,
                                       @NotNull QuestObjectiveInstance objectiveInstance,
                                       @Nullable UUID contributingPlayer,
                                       long progressDelta) {
        super(questInstance);
        if (progressDelta < 0) {
            throw new IllegalArgumentException("progressDelta must not be negative, was: " + progressDelta);
        }
        this.stageInstance = stageInstance;
        this.objectiveInstance = objectiveInstance;
        this.contributingPlayer = contributingPlayer;
        this.progressDelta = progressDelta;
    }

    /**
     * Gets the stage instance that contains the objective.
     *
     * @return the parent stage instance
     */
    @NotNull
    public QuestStageInstance getStageInstance() {
        return stageInstance;
    }

    /**
     * Gets the objective instance that is receiving progress.
     *
     * @return the objective instance
     */
    @NotNull
    public QuestObjectiveInstance getObjectiveInstance() {
        return objectiveInstance;
    }

    /**
     * Gets the UUID of the player whose action contributed to this progress, if any.
     *
     * @return the contributing player's UUID, or {@code null}
     */
    @Nullable
    public UUID getContributingPlayer() {
        return contributingPlayer;
    }

    /**
     * Gets the amount of progress to be added to the objective.
     *
     * @return the progress delta
     */
    public long getProgressDelta() {
        return progressDelta;
    }

    /**
     * Sets the amount of progress to be added. Can be modified by listeners.
     *
     * @param progressDelta the new progress delta (must not be negative)
     * @throws IllegalArgumentException if {@code progressDelta} is negative
     */
    public void setProgressDelta(long progressDelta) {
        if (progressDelta < 0) {
            throw new IllegalArgumentException("progressDelta must not be negative, was: " + progressDelta);
        }
        this.progressDelta = progressDelta;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
