package us.eunoians.mcrpg.quest.board.distribution;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.RewardFallback;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

/**
 * Wraps a single {@link QuestRewardType} within a distribution tier, carrying
 * per-reward split behavior, remainder handling, minimum scaling threshold,
 * top-N count, and optional fallback.
 *
 * @param reward            the reward type to grant
 * @param potBehavior       how this reward is handled in split-mode tiers
 * @param remainderStrategy how integer truncation remainders are distributed
 * @param minScaledAmount   minimum amount after scaling (below this, skip the reward)
 * @param topCount          number of top contributors for TOP_N behavior (defaults to 1)
 * @param fallback          optional conditional fallback reward
 */
public record DistributionRewardEntry(
        @NotNull QuestRewardType reward,
        @NotNull PotBehavior potBehavior,
        @NotNull RemainderStrategy remainderStrategy,
        int minScaledAmount,
        int topCount,
        @Nullable RewardFallback fallback
) {

    public DistributionRewardEntry {
        if (minScaledAmount < 0) {
            throw new IllegalArgumentException("minScaledAmount must be >= 0, got: " + minScaledAmount);
        }
        if (topCount < 1) {
            throw new IllegalArgumentException("topCount must be >= 1, got: " + topCount);
        }
    }

    /**
     * Convenience constructor with defaults: SCALE behavior, DISCARD remainder,
     * min 1, top-count 1, no fallback. Preserves backward compatibility with Phase 3 behavior.
     */
    public DistributionRewardEntry(@NotNull QuestRewardType reward) {
        this(reward, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, null);
    }

    /**
     * Resolves which reward to grant based on fallback condition evaluation.
     *
     * @param context the grant-time condition context
     * @return the resolved reward (primary or fallback)
     */
    @NotNull
    public QuestRewardType resolveForPlayer(@NotNull ConditionContext context) {
        if (fallback == null) {
            return reward;
        }
        return fallback.resolveReward(context, reward);
    }
}
