package us.eunoians.mcrpg.quest.definition;

/**
 * Determines how stages within a {@link QuestPhaseDefinition} must be completed
 * for the phase to be considered done.
 */
public enum PhaseCompletionMode {

    /**
     * All stages in the phase must be completed for the phase to advance.
     */
    ALL,

    /**
     * Any single stage completing is sufficient to advance the phase.
     * Remaining stages in the phase will be cancelled.
     */
    ANY
}
