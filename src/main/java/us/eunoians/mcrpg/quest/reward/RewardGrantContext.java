package us.eunoians.mcrpg.quest.reward;

/**
 * Identifies which grant pathway a {@link us.eunoians.mcrpg.event.quest.QuestRewardGrantEvent} or
 * {@link us.eunoians.mcrpg.event.quest.QuestRewardGrantedEvent} originates from. Third-party
 * listeners use this to scope their behavior (e.g. only boost inline completion rewards, or audit
 * pending rewards granted at login).
 * <p>
 * This set of constants may grow in future releases as new grant pathways are added. Listeners that
 * {@code switch} over it should include a {@code default} branch rather than assuming exhaustiveness.
 */
public enum RewardGrantContext {

    /**
     * Rewards granted directly to a quest's scope players on completion (non-distribution path).
     */
    INLINE,

    /**
     * Rewards granted through the scoped/group reward-distribution pipeline.
     */
    DISTRIBUTION,

    /**
     * Rewards that were queued while the player was offline and are being granted at login.
     */
    PENDING
}
