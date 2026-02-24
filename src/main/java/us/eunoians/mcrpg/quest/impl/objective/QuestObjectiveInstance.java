package us.eunoians.mcrpg.quest.impl.objective;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestObjectiveProgressEvent;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Mutable runtime instance of a quest objective, tracking progress, state, timestamps,
 * and per-player contributions.
 * <p>
 * An objective is complete when {@link #currentProgression} reaches {@link #requiredProgression}.
 */
public class QuestObjectiveInstance {

    private final QuestStageInstance questStage;
    private final UUID questObjectiveUUID;
    private final NamespacedKey questObjectiveKey;
    private QuestObjectiveState questObjectiveState;
    private Long startTime;
    private Long endTime;
    private long requiredProgression;
    private long currentProgression;
    private final Map<UUID, Long> playerContributionTracker;

    /**
     * Creates a new objective instance in {@link QuestObjectiveState#NOT_STARTED} state
     * with a randomly generated UUID.
     *
     * @param questObjectiveKey the {@link NamespacedKey} of the objective definition
     * @param questStage        the parent stage instance
     */
    public QuestObjectiveInstance(@NotNull NamespacedKey questObjectiveKey, @NotNull QuestStageInstance questStage) {
        this.questObjectiveKey = questObjectiveKey;
        this.questObjectiveUUID = UUID.randomUUID();
        this.questStage = questStage;
        this.questObjectiveState = QuestObjectiveState.NOT_STARTED;
        this.playerContributionTracker = new HashMap<>();
    }

    /**
     * Reconstruction constructor for loading an objective instance from the database.
     *
     * @param questObjectiveKey        the definition key
     * @param questObjectiveUUID       the persisted UUID
     * @param questStage               the parent stage instance
     * @param questObjectiveState      the persisted state
     * @param startTime                the start timestamp in epoch millis, or {@code null}
     * @param endTime                  the end timestamp in epoch millis, or {@code null}
     * @param requiredProgression      the total progress required
     * @param currentProgression       the current progress amount
     * @param playerContributionTracker per-player contribution amounts
     */
    public QuestObjectiveInstance(@NotNull NamespacedKey questObjectiveKey, @NotNull UUID questObjectiveUUID,
                                  @NotNull QuestStageInstance questStage, @NotNull QuestObjectiveState questObjectiveState,
                                  @Nullable Long startTime, @Nullable Long endTime, long requiredProgression,
                                  long currentProgression, @NotNull Map<UUID, Long> playerContributionTracker) {
        this.questObjectiveKey = questObjectiveKey;
        this.questObjectiveUUID = questObjectiveUUID;
        this.questStage = questStage;
        this.questObjectiveState = questObjectiveState;
        this.startTime = startTime;
        this.endTime = endTime;
        this.requiredProgression = requiredProgression;
        this.currentProgression = currentProgression;
        this.playerContributionTracker = new HashMap<>(playerContributionTracker);
    }

    /**
     * Gets the parent stage instance that owns this objective.
     *
     * @return the parent stage instance
     */
    @NotNull
    public QuestStageInstance getQuestStage() {
        return questStage;
    }

    /**
     * Gets the current state of this objective instance.
     *
     * @return the objective state
     */
    @NotNull
    public QuestObjectiveState getQuestObjectiveState() {
        return questObjectiveState;
    }

    /**
     * Gets the unique identifier for this objective instance.
     *
     * @return the objective UUID
     */
    @NotNull
    public UUID getQuestObjectiveUUID() {
        return questObjectiveUUID;
    }

    /**
     * Gets the {@link NamespacedKey} of the objective definition this instance was created from.
     *
     * @return the objective definition key
     */
    @NotNull
    public NamespacedKey getQuestObjectiveKey() {
        return questObjectiveKey;
    }

    /**
     * Gets the timestamp (epoch millis) when this objective was activated, if it has been started.
     *
     * @return an {@link Optional} containing the start time, or empty if not yet started
     */
    @NotNull
    public Optional<Long> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    /**
     * Gets the timestamp (epoch millis) when this objective ended, if it has ended.
     *
     * @return an {@link Optional} containing the end time, or empty if still active
     */
    @NotNull
    public Optional<Long> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    /**
     * Gets the total amount of progress required to complete this objective.
     *
     * @return the required progression
     */
    public long getRequiredProgression() {
        return requiredProgression;
    }

    /**
     * Sets the total amount of progress required to complete this objective.
     *
     * @param requiredProgression the required progression (must be positive)
     * @throws IllegalArgumentException if {@code requiredProgression} is not positive
     */
    public void setRequiredProgression(long requiredProgression) {
        if (requiredProgression <= 0) {
            throw new IllegalArgumentException("requiredProgression must be positive, was: " + requiredProgression);
        }
        this.requiredProgression = requiredProgression;
    }

    /**
     * Gets the current amount of progress toward completing this objective.
     *
     * @return the current progression
     */
    public long getCurrentProgression() {
        return currentProgression;
    }

    /**
     * Sets the current progression to a specific value. Does not trigger completion checks.
     *
     * @param currentProgression the new current progression value (must not be negative)
     * @throws IllegalArgumentException if {@code currentProgression} is negative
     */
    public void setCurrentProgression(long currentProgression) {
        if (currentProgression < 0) {
            throw new IllegalArgumentException("currentProgression must not be negative, was: " + currentProgression);
        }
        this.currentProgression = currentProgression;
    }

    /**
     * Gets an immutable snapshot of per-player contribution amounts for this objective.
     * Keys are player UUIDs and values are the total progress contributed by each player.
     *
     * @return an immutable map of player contributions
     */
    @NotNull
    public Map<UUID, Long> getPlayerContributions() {
        return Map.copyOf(playerContributionTracker);
    }

    /**
     * Gets the total progress contributed by a specific player for this objective.
     *
     * @param playerUUID the UUID of the player
     * @return the amount of progress the player has contributed, or {@code 0} if none
     */
    public long getPlayerContribution(@NotNull UUID playerUUID) {
        return playerContributionTracker.getOrDefault(playerUUID, 0L);
    }

    /**
     * Activates this objective by setting it to {@link QuestObjectiveState#IN_PROGRESS}.
     */
    public void activate() {
        if (questObjectiveState == QuestObjectiveState.NOT_STARTED) {
            questObjectiveState = QuestObjectiveState.IN_PROGRESS;
            startTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        }
    }

    /**
     * Public entry point for progressing this objective. Validates quest state, checks for
     * expiration, fires a cancellable {@link QuestObjectiveProgressEvent}, and if not cancelled,
     * applies the (potentially modified) progress delta. If the objective completes as a result,
     * fires a {@link QuestObjectiveCompleteEvent}.
     *
     * @param delta              the amount of progress to add (must be positive)
     * @param contributingPlayer the UUID of the player contributing, or {@code null} for untracked progress
     * @throws IllegalArgumentException if {@code delta} is not positive
     */
    public void progress(long delta, @Nullable UUID contributingPlayer) {
        if (delta <= 0) {
            throw new IllegalArgumentException("progress delta must be positive, was: " + delta);
        }
        QuestInstance quest = questStage.getQuestInstance();
        if (quest.getQuestState() != QuestState.IN_PROGRESS) {
            return;
        }
        if (quest.isExpired()) {
            quest.expire();
            return;
        }
        if (questObjectiveState != QuestObjectiveState.IN_PROGRESS) {
            return;
        }

        QuestObjectiveProgressEvent progressEvent = new QuestObjectiveProgressEvent(
                quest, questStage, this, contributingPlayer, delta
        );
        Bukkit.getPluginManager().callEvent(progressEvent);

        if (progressEvent.isCancelled()) {
            return;
        }

        boolean completed = addProgress(progressEvent.getProgressDelta(), contributingPlayer);
        if (completed) {
            Bukkit.getPluginManager().callEvent(new QuestObjectiveCompleteEvent(
                    quest, questStage, this
            ));
            quest.saveAsync();
        }
    }

    /**
     * Adds progress to this objective, optionally tracking the contributing player.
     * This is an internal method; external callers should use {@link #progress(long, UUID)}.
     *
     * @param delta      the amount of progress to add
     * @param playerUUID the contributing player, or {@code null} for untracked progress
     * @return {@code true} if the objective is now complete
     */
    private boolean addProgress(long delta, @Nullable UUID playerUUID) {
        if (questObjectiveState != QuestObjectiveState.IN_PROGRESS) {
            return false;
        }
        currentProgression = Math.min(currentProgression + delta, requiredProgression);
        if (playerUUID != null) {
            playerContributionTracker.merge(playerUUID, delta, Long::sum);
        }
        questStage.getQuestInstance().markDirty();
        if (currentProgression >= requiredProgression) {
            completeObjective();
            return true;
        }
        return false;
    }

    /**
     * Force-completes this objective if it is currently in progress.
     * Used when a parent stage is force-completed.
     */
    public void markAsComplete() {
        if (questObjectiveState == QuestObjectiveState.IN_PROGRESS && endTime == null) {
            completeObjective();
        }
    }

    /**
     * Cancels this objective. Used when a parent stage is cancelled
     * (e.g., sibling stage completed in an ANY-mode phase).
     */
    public void cancel() {
        if (questObjectiveState == QuestObjectiveState.IN_PROGRESS || questObjectiveState == QuestObjectiveState.NOT_STARTED) {
            questObjectiveState = QuestObjectiveState.CANCELLED;
            endTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        }
    }

    private void completeObjective() {
        questObjectiveState = QuestObjectiveState.COMPLETED;
        endTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
    }
}
