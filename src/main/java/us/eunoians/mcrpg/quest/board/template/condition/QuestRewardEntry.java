package us.eunoians.mcrpg.quest.board.template.condition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

/**
 * Wrapper for standard quest rewards carrying an optional {@link RewardFallback}.
 * Replaces raw {@code List<QuestRewardType>} in quest definitions.
 *
 * @param reward   the primary reward
 * @param fallback optional conditional fallback (null if none)
 */
public record QuestRewardEntry(
        @NotNull QuestRewardType reward,
        @Nullable RewardFallback fallback
) {

    /**
     * Convenience constructor with no fallback.
     */
    public QuestRewardEntry(@NotNull QuestRewardType reward) {
        this(reward, null);
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
