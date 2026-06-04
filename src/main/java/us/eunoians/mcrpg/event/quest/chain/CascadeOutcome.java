package us.eunoians.mcrpg.event.quest.chain;

/**
 * Describes how a cascade terminated. Carried by {@link CascadeFinalizeEvent}
 * so external plugins can distinguish between normal completion, depth-limit
 * truncation, and internal failures.
 */
public enum CascadeOutcome {

    /**
     * The cascade completed normally — the root chain operation returned
     * and all recursive auto-completions finished within the depth limit.
     */
    SUCCESS,

    /**
     * The cascade was truncated because the depth limit was reached. The
     * chain remains active at the last successfully started step.
     */
    DEPTH_LIMIT_REACHED,

    /**
     * The cascade was interrupted by an unexpected exception during chain
     * progression. The cascade context was still cleaned up and the event
     * was still fired, but message delivery may have been skipped.
     */
    ERROR
}
