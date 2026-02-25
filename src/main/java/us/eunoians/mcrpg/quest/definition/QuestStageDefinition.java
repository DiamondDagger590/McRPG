package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.List;
import java.util.Optional;

/**
 * An immutable definition (frame) for a single quest stage.
 * <p>
 * A stage contains one or more {@link QuestObjectiveDefinition objectives} that must all be
 * completed for the stage to be considered done. Stages may also carry optional rewards
 * granted upon completion.
 */
public class QuestStageDefinition {

    private final NamespacedKey stageKey;
    private final List<QuestObjectiveDefinition> objectives;
    private final List<QuestRewardType> rewards;
    private final RewardDistributionConfig rewardDistribution;

    /**
     * Creates a new stage definition.
     *
     * @param stageKey            the unique key identifying this stage within its parent quest
     * @param objectives          the objective definitions that must all complete for this stage to finish (must contain at least one)
     * @param rewards             the rewards granted upon stage completion
     * @param rewardDistribution  the distribution configuration for stage-level rewards, or {@code null} if none
     * @throws IllegalArgumentException if {@code objectives} is empty
     */
    public QuestStageDefinition(@NotNull NamespacedKey stageKey,
                                @NotNull List<QuestObjectiveDefinition> objectives,
                                @NotNull List<QuestRewardType> rewards,
                                @Nullable RewardDistributionConfig rewardDistribution) {
        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("A stage must have at least one objective");
        }
        this.stageKey = stageKey;
        this.objectives = List.copyOf(objectives);
        this.rewards = List.copyOf(rewards);
        this.rewardDistribution = rewardDistribution;
    }

    /**
     * Gets the unique key identifying this stage within its parent quest.
     *
     * @return the stage's namespaced key
     */
    @NotNull
    public NamespacedKey getStageKey() {
        return stageKey;
    }

    /**
     * Gets the immutable list of objective definitions that must all be completed for this stage to finish.
     *
     * @return an immutable list of objective definitions
     */
    @NotNull
    public List<QuestObjectiveDefinition> getObjectives() {
        return objectives;
    }

    /**
     * Gets the immutable list of rewards granted when a stage created from this definition completes.
     *
     * @return an immutable list of configured reward types
     */
    @NotNull
    public List<QuestRewardType> getRewards() {
        return rewards;
    }

    /**
     * Gets the optional reward distribution configuration for stage-level completion rewards.
     *
     * @return an {@link Optional} containing the distribution config, or empty if standard (non-distributed) rewards apply
     */
    @NotNull
    public Optional<RewardDistributionConfig> getRewardDistribution() {
        return Optional.ofNullable(rewardDistribution);
    }
}
