package us.eunoians.mcrpg.quest.definition;

import java.util.Collections;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An immutable definition for a quest phase -- an ordered grouping of stages within a quest.
 * <p>
 * Phases are sequential: a quest progresses through phases in order. Within a phase,
 * the {@link PhaseCompletionMode} determines whether all stages must complete ({@code ALL})
 * or any single stage completing is sufficient ({@code ANY}, enabling branching).
 * <p>
 * Phases exist only in the definition layer. They are not persisted as instances;
 * phase state is computed at runtime from the states of the child stage instances and
 * the phase's completion mode.
 */
public class QuestPhaseDefinition {

    private final int phaseIndex;
    private final PhaseCompletionMode completionMode;
    private final List<QuestStageDefinition> stages;

    /**
     * Creates a new phase definition.
     *
     * @param phaseIndex     the zero-based index of this phase within the parent quest
     * @param completionMode the mode determining how stages must complete for the phase to advance
     * @param stages         the stage definitions within this phase (must contain at least one)
     * @throws IllegalArgumentException if {@code phaseIndex} is negative or {@code stages} is empty
     */
    public QuestPhaseDefinition(int phaseIndex,
                                @NotNull PhaseCompletionMode completionMode,
                                @NotNull List<QuestStageDefinition> stages) {
        if (phaseIndex < 0) {
            throw new IllegalArgumentException("phaseIndex must be non-negative");
        }
        if (stages.isEmpty()) {
            throw new IllegalArgumentException("A phase must have at least one stage");
        }
        this.phaseIndex = phaseIndex;
        this.completionMode = completionMode;
        this.stages = List.copyOf(stages);
    }

    /**
     * Gets the zero-based index of this phase within the parent quest.
     *
     * @return the phase index
     */
    public int getPhaseIndex() {
        return phaseIndex;
    }

    /**
     * Gets the completion mode that determines how stages in this phase must be
     * completed for the phase to advance.
     *
     * @return the completion mode
     */
    @NotNull
    public PhaseCompletionMode getCompletionMode() {
        return completionMode;
    }

    /**
     * Gets the immutable list of stage definitions within this phase.
     *
     * @return an immutable list of stage definitions
     */
    @NotNull
    public List<QuestStageDefinition> getStages() {
        return stages;
    }
}
