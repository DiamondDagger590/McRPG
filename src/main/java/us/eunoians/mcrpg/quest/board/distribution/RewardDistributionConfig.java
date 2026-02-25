package us.eunoians.mcrpg.quest.board.distribution;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Wraps the full {@code reward-distribution} YAML block for a single level
 * (quest, phase, stage, or objective). Contains an ordered list of distribution tiers
 * that are evaluated sequentially — a player can match multiple tiers and rewards stack.
 */
public final class RewardDistributionConfig {

    private final List<DistributionTierConfig> tiers;

    public RewardDistributionConfig(@NotNull List<DistributionTierConfig> tiers) {
        this.tiers = List.copyOf(tiers);
    }

    /**
     * Gets the ordered, immutable list of distribution tiers. Tiers are evaluated
     * sequentially during resolution — a player can qualify for multiple tiers
     * and rewards from all matched tiers stack.
     *
     * @return the immutable list of tier configurations
     */
    @NotNull
    public List<DistributionTierConfig> getTiers() {
        return tiers;
    }

    /**
     * Returns {@code true} if this config has no tiers, meaning no distribution
     * rewards will be granted. Callers can use this to short-circuit resolution.
     *
     * @return true if the tier list is empty
     */
    public boolean isEmpty() {
        return tiers.isEmpty();
    }
}
