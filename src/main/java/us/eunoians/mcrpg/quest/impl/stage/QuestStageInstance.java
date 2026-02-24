package us.eunoians.mcrpg.quest.impl.stage;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestStageCompleteEvent;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Mutable runtime instance of a quest stage, tracking state, timestamps, and child objective instances.
 * <p>
 * Each stage belongs to a specific phase (identified by {@link #phaseIndex}) within its parent
 * {@link QuestInstance}. A stage is complete when all of its objectives are complete.
 */
public class QuestStageInstance {

    private final QuestInstance questInstance;
    private final UUID questStageUUID;
    private final NamespacedKey stageKey;
    private final int phaseIndex;
    private QuestStageState questStageState;
    private Long startTime;
    private Long endTime;

    private final List<QuestObjectiveInstance> objectives;

    /**
     * Creates a new stage instance in {@link QuestStageState#NOT_STARTED} state with a randomly generated UUID.
     *
     * @param stageKey      the {@link NamespacedKey} of the stage definition
     * @param phaseIndex    the zero-based index of the phase this stage belongs to
     * @param questInstance the parent quest instance
     */
    public QuestStageInstance(@NotNull NamespacedKey stageKey, int phaseIndex, @NotNull QuestInstance questInstance) {
        this.stageKey = stageKey;
        this.phaseIndex = phaseIndex;
        this.questInstance = questInstance;
        this.questStageUUID = UUID.randomUUID();
        this.questStageState = QuestStageState.NOT_STARTED;
        this.objectives = new ArrayList<>();
    }

    /**
     * Reconstruction constructor for loading a stage instance from the database.
     *
     * @param stageKey        the definition key
     * @param questStageUUID  the persisted UUID
     * @param phaseIndex      the phase index this stage belongs to
     * @param questInstance   the parent quest instance
     * @param questStageState the persisted state
     * @param startTime       the start timestamp in epoch millis, or {@code null}
     * @param endTime         the end timestamp in epoch millis, or {@code null}
     */
    public QuestStageInstance(@NotNull NamespacedKey stageKey, @NotNull UUID questStageUUID, int phaseIndex,
                              @NotNull QuestInstance questInstance, @NotNull QuestStageState questStageState,
                              @Nullable Long startTime, @Nullable Long endTime) {
        this.stageKey = stageKey;
        this.questStageUUID = questStageUUID;
        this.phaseIndex = phaseIndex;
        this.questInstance = questInstance;
        this.questStageState = questStageState;
        this.startTime = startTime;
        this.endTime = endTime;
        this.objectives = new ArrayList<>();
    }

    /**
     * Gets the {@link NamespacedKey} of the stage definition this instance was created from.
     *
     * @return the stage definition key
     */
    @NotNull
    public NamespacedKey getStageKey() {
        return stageKey;
    }

    /**
     * Gets the zero-based index of the phase this stage belongs to within the parent quest.
     *
     * @return the phase index
     */
    public int getPhaseIndex() {
        return phaseIndex;
    }

    /**
     * Gets the current state of this stage instance.
     *
     * @return the stage state
     */
    @NotNull
    public QuestStageState getQuestStageState() {
        return questStageState;
    }

    /**
     * Gets the unique identifier for this stage instance.
     *
     * @return the stage UUID
     */
    @NotNull
    public UUID getQuestStageUUID() {
        return questStageUUID;
    }

    /**
     * Gets the parent quest instance that owns this stage.
     *
     * @return the parent quest instance
     */
    @NotNull
    public QuestInstance getQuestInstance() {
        return questInstance;
    }

    /**
     * Gets the timestamp (epoch millis) when this stage was activated, if it has been started.
     *
     * @return an {@link Optional} containing the start time, or empty if not yet started
     */
    @NotNull
    public Optional<Long> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    /**
     * Gets the timestamp (epoch millis) when this stage ended, if it has ended.
     *
     * @return an {@link Optional} containing the end time, or empty if still active
     */
    @NotNull
    public Optional<Long> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    /**
     * Appends an objective instance to this stage's objective list.
     *
     * @param questObjectiveInstance the objective instance to add
     */
    public void addQuestObjective(@NotNull QuestObjectiveInstance questObjectiveInstance) {
        this.objectives.add(questObjectiveInstance);
    }

    /**
     * Inserts an objective instance at the specified index in this stage's objective list.
     *
     * @param questObjectiveInstance the objective instance to add
     * @param index                  the index at which to insert
     */
    public void addQuestObjective(@NotNull QuestObjectiveInstance questObjectiveInstance, int index) {
        this.objectives.add(index, questObjectiveInstance);
    }

    /**
     * Appends all provided objective instances to this stage's objective list.
     *
     * @param questObjectives the objective instances to add
     */
    public void addQuestObjectives(@NotNull List<QuestObjectiveInstance> questObjectives) {
        this.objectives.addAll(questObjectives);
    }

    /**
     * Gets an immutable copy of all objective instances belonging to this stage.
     *
     * @return an immutable list of objective instances
     */
    @NotNull
    public List<QuestObjectiveInstance> getQuestObjectives() {
        return List.copyOf(this.objectives);
    }

    /**
     * Gets an immutable list of all currently active (in-progress) objectives in this stage.
     *
     * @return an immutable list of in-progress objective instances
     */
    @NotNull
    public List<QuestObjectiveInstance> getActiveObjectives() {
        return List.copyOf(this.objectives.stream()
                .filter(questObjectiveInstance -> questObjectiveInstance.getQuestObjectiveState() == QuestObjectiveState.IN_PROGRESS)
                .toList());
    }

    /**
     * Activates this stage by setting it to {@link QuestStageState#IN_PROGRESS} and
     * activating all of its objectives.
     */
    public void activate() {
        if (questStageState == QuestStageState.NOT_STARTED) {
            questStageState = QuestStageState.IN_PROGRESS;
            startTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
            for (QuestObjectiveInstance objective : objectives) {
                objective.activate();
            }
        }
    }

    /**
     * Checks if all objectives in this stage are completed without performing any state mutations.
     *
     * @return {@code true} if the stage is in progress and all objectives are completed
     */
    public boolean checkForUpdatedStatus() {
        if (questStageState != QuestStageState.IN_PROGRESS) {
            return false;
        }
        return objectives.stream()
                .allMatch(obj -> obj.getQuestObjectiveState() == QuestObjectiveState.COMPLETED);
    }

    /**
     * Completes this stage by transitioning it to {@link QuestStageState#COMPLETED}, recording the
     * end time, force-completing any objectives still in progress, and firing a
     * {@link QuestStageCompleteEvent}.
     */
    public void complete() {
        if (questStageState == QuestStageState.IN_PROGRESS && endTime == null) {
            questStageState = QuestStageState.COMPLETED;
            endTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
            for (QuestObjectiveInstance objectiveInstance : objectives) {
                objectiveInstance.markAsComplete();
            }
            Bukkit.getPluginManager().callEvent(new QuestStageCompleteEvent(questInstance, this));
        }
    }

    /**
     * Cancels this stage and all of its objectives. Used when a sibling stage completes
     * in an {@link us.eunoians.mcrpg.quest.definition.PhaseCompletionMode#ANY ANY}-mode phase.
     */
    public void cancel() {
        if (questStageState == QuestStageState.IN_PROGRESS || questStageState == QuestStageState.NOT_STARTED) {
            questStageState = QuestStageState.CANCELLED;
            endTime = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
            for (QuestObjectiveInstance objective : objectives) {
                objective.cancel();
            }
        }
    }
}
