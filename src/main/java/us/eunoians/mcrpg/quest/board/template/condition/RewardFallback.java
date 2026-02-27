package us.eunoians.mcrpg.quest.board.template.condition;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

/**
 * Per-reward conditional fallback. When a reward is granted, if the fallback's
 * condition evaluates to {@code true} (e.g., the player already has the title),
 * the fallback reward is granted instead of the primary.
 *
 * @param condition     the condition to evaluate at grant time
 * @param fallbackReward the reward to grant if the condition passes
 */
public record RewardFallback(
        @NotNull TemplateCondition condition,
        @NotNull QuestRewardType fallbackReward
) {

    /**
     * Resolves which reward to grant based on the condition evaluation.
     *
     * @param context       the grant-time condition context
     * @param primaryReward the primary reward (granted if condition is false)
     * @return the fallback reward if condition passes, otherwise the primary
     */
    @NotNull
    public QuestRewardType resolveReward(@NotNull ConditionContext context,
                                         @NotNull QuestRewardType primaryReward) {
        return condition.evaluate(context) ? fallbackReward : primaryReward;
    }
}
