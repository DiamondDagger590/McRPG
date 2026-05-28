package us.eunoians.mcrpg.quest.chain;

/**
 * Lifecycle states for a {@link QuestChainPlayerState}. Only {@code ACTIVE} is a non-terminal
 * state — all other states block further chain progress unless explicitly restarted or reset
 * by an admin.
 */
public enum QuestChainState {

    ACTIVE,
    COMPLETED,
    ABANDONED,
    FAILED,
    EXPIRED;

    /**
     * Terminal states cannot be re-activated without explicit admin intervention
     * or repeat-mode re-evaluation.
     *
     * @return {@code true} if this state represents a terminal chain lifecycle point
     */
    public boolean isTerminal() {
        return this != ACTIVE;
    }

    /**
     * States eligible for repeat-mode re-start evaluation. All terminal states
     * except {@code ACTIVE} are repeat-eligible — the repeat mode on the chain
     * definition controls whether re-start actually happens ({@code ONCE} chains
     * remain permanently terminal for all terminal states including {@code ABANDONED}).
     *
     * @return {@code true} if repeat-mode re-start should be considered
     */
    public boolean isRepeatEligible() {
        return this == COMPLETED || this == FAILED || this == EXPIRED || this == ABANDONED;
    }
}
