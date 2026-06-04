package us.eunoians.mcrpg.event.quest.chain;

/**
 * Describes how a quest chain completion was triggered, allowing listeners on
 * {@link QuestChainCompleteEvent} to distinguish between the three possible paths:
 * <ul>
 *   <li>{@link #ADVANCEMENT} — the player finished the final chain step during normal gameplay</li>
 *   <li>{@link #RE_RESOLUTION} — the chain was resolved at login or reload after discovering all
 *       steps were already logged as complete (e.g. following a crash between log write and state
 *       persist)</li>
 *   <li>{@link #RESTART} — an admin restart of the chain found all steps already complete in the
 *       completion log, so no new step could be started</li>
 * </ul>
 * Listeners that grant rewards or update statistics should use this to implement idempotent
 * handling and avoid duplicate side effects on re-resolution and restart paths.
 */
public enum ChainCompletionSource {

    /**
     * The player advanced through the final step during normal gameplay via
     * {@code QuestChainManager.advanceChain}.
     */
    ADVANCEMENT,

    /**
     * The chain was marked complete during login or reload re-resolution because all steps
     * were already present in the completion log with no uncompleted step remaining.
     */
    RE_RESOLUTION,

    /**
     * An admin restart of the chain found all steps already complete in the completion log,
     * so no new first step could be started; the chain was marked complete instead.
     */
    RESTART
}
