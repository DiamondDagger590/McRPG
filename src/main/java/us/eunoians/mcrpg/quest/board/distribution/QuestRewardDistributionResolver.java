package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Stateless utility that performs distribution resolution. No Bukkit dependency beyond
 * NamespacedKey — takes pure data inputs and returns a map of player UUIDs to their
 * earned reward lists.
 * <p>
 * Handles {@link RewardSplitMode} scaling: for {@code SPLIT_EVEN} and
 * {@code SPLIT_PROPORTIONAL} tiers, reward amounts are divided among qualifying
 * players via {@link QuestRewardType#withAmountMultiplier(double)}.
 */
public final class QuestRewardDistributionResolver {

    private static final Logger LOGGER = Logger.getLogger(QuestRewardDistributionResolver.class.getName());

    private QuestRewardDistributionResolver() {
    }

    /**
     * Evaluates all tiers in the distribution config against the contribution
     * snapshot and returns a map of player UUID to list of rewards earned.
     * A player can match multiple tiers; rewards stack.
     *
     * @param config         the reward distribution configuration
     * @param snapshot       the contribution snapshot for the relevant scope
     * @param questRarity    the rarity key of the quest instance (nullable for non-board quests)
     * @param rarityRegistry the rarity registry for ordering comparisons
     * @param typeRegistry   the distribution type registry
     * @return map of player UUID to the list of rewards earned across all matched tiers
     */
    @NotNull
    public static Map<UUID, List<QuestRewardType>> resolve(
            @NotNull RewardDistributionConfig config,
            @NotNull ContributionSnapshot snapshot,
            @Nullable NamespacedKey questRarity,
            @NotNull QuestRarityRegistry rarityRegistry,
            @NotNull RewardDistributionTypeRegistry typeRegistry) {

        Map<UUID, List<QuestRewardType>> result = new HashMap<>();

        for (DistributionTierConfig tier : config.getTiers()) {
            if (!tier.passesRarityGate(questRarity, rarityRegistry)) {
                continue;
            }
            Optional<RewardDistributionType> type = typeRegistry.get(tier.getTypeKey());
            if (type.isEmpty()) {
                LOGGER.warning("Unrecognized distribution type key: " + tier.getTypeKey()
                        + " in tier '" + tier.getTierKey() + "' — skipping");
                continue;
            }
            Set<UUID> qualifyingPlayers = type.get().resolve(snapshot, tier);
            if (qualifyingPlayers.isEmpty()) {
                continue;
            }

            applyTierRewards(tier, qualifyingPlayers, snapshot, result);
        }

        return result;
    }

    /**
     * Applies a single tier's rewards to qualifying players according to the tier's
     * {@link RewardSplitMode}. For {@code INDIVIDUAL}, each player gets the full
     * reward list. For {@code SPLIT_EVEN}, the pot is divided equally. For
     * {@code SPLIT_PROPORTIONAL}, the pot is divided by each player's contribution
     * share (falls back to even split when total contribution is zero).
     *
     * @param tier              the tier configuration
     * @param qualifyingPlayers the set of player UUIDs that qualified for this tier
     * @param snapshot          the contribution snapshot (used for proportional calculation)
     * @param result            the accumulator map of player UUID to reward list (mutated in-place)
     */
    private static void applyTierRewards(@NotNull DistributionTierConfig tier,
                                         @NotNull Set<UUID> qualifyingPlayers,
                                         @NotNull ContributionSnapshot snapshot,
                                         @NotNull Map<UUID, List<QuestRewardType>> result) {
        switch (tier.getSplitMode()) {
            case INDIVIDUAL -> {
                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                            .addAll(tier.getRewards());
                }
            }
            case SPLIT_EVEN -> {
                double multiplier = 1.0 / qualifyingPlayers.size();
                List<QuestRewardType> scaledRewards = scaleRewards(tier.getRewards(), multiplier);
                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                            .addAll(scaledRewards);
                }
            }
            case SPLIT_PROPORTIONAL -> {
                long totalContribution = qualifyingPlayers.stream()
                        .mapToLong(uuid -> snapshot.contributions().getOrDefault(uuid, 0L))
                        .sum();
                if (totalContribution == 0) {
                    double fallback = 1.0 / qualifyingPlayers.size();
                    List<QuestRewardType> scaledRewards = scaleRewards(tier.getRewards(), fallback);
                    for (UUID playerUUID : qualifyingPlayers) {
                        result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                                .addAll(scaledRewards);
                    }
                } else {
                    for (UUID playerUUID : qualifyingPlayers) {
                        long playerContribution = snapshot.contributions().getOrDefault(playerUUID, 0L);
                        double multiplier = (double) playerContribution / totalContribution;
                        List<QuestRewardType> scaledRewards = scaleRewards(tier.getRewards(), multiplier);
                        result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                                .addAll(scaledRewards);
                    }
                }
            }
        }
    }

    /**
     * Creates a new reward list where each reward's amount is scaled by the given
     * multiplier via {@link QuestRewardType#withAmountMultiplier(double)}. Reward
     * types that are not scalable (e.g., commands) return themselves unchanged.
     *
     * @param rewards    the original reward list
     * @param multiplier the scaling factor (e.g., 0.5 for half)
     * @return a new list of scaled reward instances
     */
    @NotNull
    private static List<QuestRewardType> scaleRewards(@NotNull List<QuestRewardType> rewards,
                                                       double multiplier) {
        return rewards.stream()
                .map(reward -> reward.withAmountMultiplier(multiplier))
                .toList();
    }
}
