package us.eunoians.mcrpg.quest.board.distribution;

/**
 * Controls how a distribution tier's reward amounts are distributed among qualifying players.
 * <p>
 * The default {@link #INDIVIDUAL} preserves existing behavior where each qualifying player
 * receives the full reward amount. The two "pot" modes divide a fixed reward pool among
 * qualifying players, preventing large groups from generating unbounded total reward value.
 */
public enum RewardSplitMode {

    /**
     * Each qualifying player receives the full reward amount.
     * If 10 players qualify for a tier with 1000 XP, each gets 1000 XP (10,000 total).
     */
    INDIVIDUAL,

    /**
     * The reward amount is a fixed pot divided equally among qualifying players.
     * If 10 players qualify for a tier with 1000 XP, each gets 100 XP (1,000 total).
     */
    SPLIT_EVEN,

    /**
     * The reward amount is a fixed pot divided proportionally by contribution.
     * If 2 players qualify (60% and 40% contribution) for a tier with 1000 XP,
     * they get 600 XP and 400 XP respectively (1,000 total).
     * Falls back to {@link #SPLIT_EVEN} if contribution data is unavailable.
     */
    SPLIT_PROPORTIONAL
}
