package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Performs distribution resolution. Handles {@link RewardSplitMode} scaling with
 * per-reward {@link PotBehavior}, {@link RemainderStrategy}, and {@code minScaledAmount} controls.
 * <p>
 * Stateless — a new instance can be created whenever needed.
 */
public final class QuestRewardDistributionResolver {

    private final Logger logger;

    /**
     * Creates a new distribution resolver with the given logger.
     *
     * @param logger the logger for warning messages about unrecognized or non-scalable types
     */
    public QuestRewardDistributionResolver(@NotNull Logger logger) {
        this.logger = logger;
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
    public Map<UUID, List<QuestRewardType>> resolve(
            @NotNull RewardDistributionConfig config,
            @NotNull ContributionSnapshot snapshot,
            @Nullable NamespacedKey questRarity,
            @NotNull QuestRarityRegistry rarityRegistry,
            @NotNull RewardDistributionTypeRegistry typeRegistry) {
        return resolve(config, snapshot, questRarity, rarityRegistry, typeRegistry, new Random());
    }

    /**
     * Evaluates all tiers with a provided random instance for deterministic remainder distribution.
     *
     * @param config         the reward distribution configuration
     * @param snapshot       the contribution snapshot for the relevant scope
     * @param questRarity    the rarity key of the quest instance (nullable for non-board quests)
     * @param rarityRegistry the rarity registry for ordering comparisons
     * @param typeRegistry   the distribution type registry
     * @param random         the random instance to use for remainder distribution
     * @return map of player UUID to the list of rewards earned across all matched tiers
     */
    @NotNull
    public Map<UUID, List<QuestRewardType>> resolve(
            @NotNull RewardDistributionConfig config,
            @NotNull ContributionSnapshot snapshot,
            @Nullable NamespacedKey questRarity,
            @NotNull QuestRarityRegistry rarityRegistry,
            @NotNull RewardDistributionTypeRegistry typeRegistry,
            @NotNull Random random) {

        Map<UUID, List<QuestRewardType>> result = new HashMap<>();

        for (DistributionTierConfig tier : config.getTiers()) {
            if (!tier.passesRarityGate(questRarity, rarityRegistry)) {
                continue;
            }
            Optional<RewardDistributionType> type = typeRegistry.get(tier.getTypeKey());
            if (type.isEmpty()) {
                logger.warning("Unrecognized distribution type key: " + tier.getTypeKey()
                        + " in tier '" + tier.getTierKey() + "' — skipping");
                continue;
            }
            Set<UUID> qualifyingPlayers = type.get().resolve(snapshot, tier);
            if (qualifyingPlayers.isEmpty()) {
                continue;
            }

            applyTierRewards(tier, qualifyingPlayers, snapshot, result, random);
        }

        return result;
    }

    /**
     * Applies tier rewards to qualifying players based on the split mode.
     *
     * @param tier              the tier configuration
     * @param qualifyingPlayers the set of qualifying players
     * @param snapshot          the contribution snapshot
     * @param result            the reward map to populate
     * @param random            the random source for remainder distribution
     */
    private void applyTierRewards(@NotNull DistributionTierConfig tier,
                                  @NotNull Set<UUID> qualifyingPlayers,
                                  @NotNull ContributionSnapshot snapshot,
                                  @NotNull Map<UUID, List<QuestRewardType>> result,
                                  @NotNull Random random) {
        switch (tier.getSplitMode()) {
            case INDIVIDUAL -> {
                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                            .addAll(tier.getRewards());
                }
            }
            case SPLIT_EVEN -> {
                double baseMultiplier = 1.0 / qualifyingPlayers.size();
                for (DistributionRewardEntry entry : tier.getRewardEntries()) {
                    distributeRewardEntry(entry, baseMultiplier, qualifyingPlayers,
                            snapshot, result, random);
                }
            }
            case SPLIT_PROPORTIONAL -> {
                long totalContribution = qualifyingPlayers.stream()
                        .mapToLong(uuid -> snapshot.contributions().getOrDefault(uuid, 0L))
                        .sum();
                if (totalContribution == 0) {
                    double fallback = 1.0 / qualifyingPlayers.size();
                    for (DistributionRewardEntry entry : tier.getRewardEntries()) {
                        distributeRewardEntry(entry, fallback, qualifyingPlayers,
                                snapshot, result, random);
                    }
                } else {
                    for (DistributionRewardEntry entry : tier.getRewardEntries()) {
                        distributeProportional(entry, qualifyingPlayers, snapshot,
                                totalContribution, result, random);
                    }
                }
            }
        }
    }

    /**
     * Distributes a single reward entry to qualifying players using the configured pot behavior.
     *
     * @param entry             the reward entry to distribute
     * @param baseMultiplier    the base multiplier for SCALE mode
     * @param qualifyingPlayers the set of qualifying players
     * @param snapshot          the contribution snapshot
     * @param result            the reward map to populate
     * @param random            the random source for remainder distribution
     */
    private void distributeRewardEntry(
            @NotNull DistributionRewardEntry entry,
            double baseMultiplier,
            @NotNull Set<UUID> qualifyingPlayers,
            @NotNull ContributionSnapshot snapshot,
            @NotNull Map<UUID, List<QuestRewardType>> result,
            @NotNull Random random) {

        switch (entry.potBehavior()) {
            case ALL -> {
                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                            .add(entry.reward());
                }
            }
            case TOP_N -> {
                List<UUID> topN = findTopContributors(qualifyingPlayers, snapshot, entry.topCount());
                for (UUID top : topN) {
                    result.computeIfAbsent(top, k -> new ArrayList<>())
                            .add(entry.reward());
                }
            }
            case SCALE -> {
                if (!entry.reward().isScalable()) {
                    logger.warning("Non-scalable reward '" + entry.reward().getKey()
                            + "' used with SCALE pot-behavior; granting unscaled to all qualifying players");
                    for (UUID playerUUID : qualifyingPlayers) {
                        result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                                .add(entry.reward());
                    }
                    return;
                }

                QuestRewardType scaled = entry.reward().withAmountMultiplier(baseMultiplier);
                OptionalLong scaledAmount = scaled.getNumericAmount();
                if (scaledAmount.isPresent() && scaledAmount.getAsLong() < entry.minScaledAmount()) {
                    return;
                }

                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(scaled);
                }

                // Distribute the leftover between the pot total and what was actually granted. Passing the
                // real per-player amount (rather than re-deriving it from the multiplier) keeps the remainder
                // consistent with the reward type's own rounding, so granted + remainder == pot.
                if (entry.remainderStrategy() != RemainderStrategy.DISCARD && scaledAmount.isPresent()) {
                    distributeRemainder(entry, scaledAmount.getAsLong(), qualifyingPlayers,
                            snapshot, result, random);
                }
            }
        }
    }

    /**
     * Distributes a reward entry proportionally to qualifying players based on their contributions.
     *
     * @param entry             the reward entry to distribute
     * @param qualifyingPlayers the set of qualifying players
     * @param snapshot          the contribution snapshot
     * @param totalContribution the total contribution across all qualifying players
     * @param result            the reward map to populate
     * @param random            the random source (unused in proportional but passed for consistency)
     */
    private void distributeProportional(
            @NotNull DistributionRewardEntry entry,
            @NotNull Set<UUID> qualifyingPlayers,
            @NotNull ContributionSnapshot snapshot,
            long totalContribution,
            @NotNull Map<UUID, List<QuestRewardType>> result,
            @NotNull Random random) {

        switch (entry.potBehavior()) {
            case ALL -> {
                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                            .add(entry.reward());
                }
            }
            case TOP_N -> {
                List<UUID> topN = findTopContributors(qualifyingPlayers, snapshot, entry.topCount());
                for (UUID top : topN) {
                    result.computeIfAbsent(top, k -> new ArrayList<>())
                            .add(entry.reward());
                }
            }
            case SCALE -> {
                for (UUID playerUUID : qualifyingPlayers) {
                    long playerContribution = snapshot.contributions().getOrDefault(playerUUID, 0L);
                    double multiplier = (double) playerContribution / totalContribution;
                    if (!entry.reward().isScalable()) {
                        logger.warning("Non-scalable reward '" + entry.reward().getKey()
                                + "' used with SCALE pot-behavior; granting unscaled to all");
                        for (UUID uuid : qualifyingPlayers) {
                            result.computeIfAbsent(uuid, k -> new ArrayList<>())
                                    .add(entry.reward());
                        }
                        return;
                    }

                    QuestRewardType scaled = entry.reward().withAmountMultiplier(multiplier);
                    OptionalLong scaledAmount = scaled.getNumericAmount();
                    if (scaledAmount.isPresent() && scaledAmount.getAsLong() < entry.minScaledAmount()) {
                        continue;
                    }

                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(scaled);
                }
            }
        }
    }

    /**
     * Distributes any remainder reward to one or more players based on the configured strategy.
     * <p>
     * {@code perPlayerAmount} is the amount each qualifying player actually received, so the
     * remainder ({@code total - perPlayerAmount * players}) reflects the reward type's real rounding
     * rather than a re-derived estimate. Remainder shares are granted with
     * {@link QuestRewardType#withExactAmount(long)} so no rounding drift is introduced.
     *
     * @param entry             the reward entry with remainder configuration
     * @param perPlayerAmount   the exact numeric amount each qualifying player was granted
     * @param qualifyingPlayers the set of qualifying players
     * @param snapshot          the contribution snapshot
     * @param result            the reward map to populate
     * @param random            the random source for RANDOM remainder strategy
     */
    private void distributeRemainder(
            @NotNull DistributionRewardEntry entry,
            long perPlayerAmount,
            @NotNull Set<UUID> qualifyingPlayers,
            @NotNull ContributionSnapshot snapshot,
            @NotNull Map<UUID, List<QuestRewardType>> result,
            @NotNull Random random) {

        OptionalLong originalAmount = entry.reward().getNumericAmount();
        if (originalAmount.isEmpty()) {
            return;
        }

        long total = originalAmount.getAsLong();
        long distributed = perPlayerAmount * qualifyingPlayers.size();
        long remainder = total - distributed;

        if (remainder <= 0) {
            return;
        }

        switch (entry.remainderStrategy()) {
            case TOP_CONTRIBUTOR -> findTopContributor(qualifyingPlayers, snapshot).ifPresent(top -> {
                QuestRewardType extra = entry.reward().withExactAmount(remainder);
                result.computeIfAbsent(top, k -> new ArrayList<>()).add(extra);
            });
            case RANDOM -> {
                List<UUID> shuffled = new ArrayList<>(qualifyingPlayers);
                Collections.shuffle(shuffled, random);
                for (int i = 0; i < remainder && i < shuffled.size(); i++) {
                    QuestRewardType extra = entry.reward().withExactAmount(1);
                    result.computeIfAbsent(shuffled.get(i), k -> new ArrayList<>()).add(extra);
                }
            }
            case DISCARD -> {}
        }
    }

    /**
     * Returns the top N contributors from the qualifying players, sorted by contribution descending.
     *
     * @param qualifyingPlayers the set of qualifying players
     * @param snapshot          the contribution snapshot
     * @param count             the maximum number of top contributors to return
     * @return the list of top contributor UUIDs, up to {@code count}
     */
    @NotNull
    private static List<UUID> findTopContributors(@NotNull Set<UUID> qualifyingPlayers,
                                                   @NotNull ContributionSnapshot snapshot,
                                                   int count) {
        return qualifyingPlayers.stream()
                .sorted(Comparator.comparingLong(
                                (UUID uuid) -> snapshot.contributions().getOrDefault(uuid, 0L))
                        .reversed()
                        .thenComparing(Comparator.naturalOrder()))
                .limit(count)
                .toList();
    }

    /**
     * Returns the single top contributor from the qualifying players.
     *
     * @param qualifyingPlayers the set of qualifying players
     * @param snapshot          the contribution snapshot
     * @return the UUID of the top contributor, or empty if no players are present
     */
    @NotNull
    private static Optional<UUID> findTopContributor(@NotNull Set<UUID> qualifyingPlayers,
                                                      @NotNull ContributionSnapshot snapshot) {
        return qualifyingPlayers.stream()
                .max(Comparator.comparingLong(
                        uuid -> snapshot.contributions().getOrDefault(uuid, 0L)));
    }
}
