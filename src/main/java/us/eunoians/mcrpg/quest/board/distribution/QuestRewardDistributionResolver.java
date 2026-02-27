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
 * Stateless utility that performs distribution resolution. Handles {@link RewardSplitMode}
 * scaling with per-reward {@link PotBehavior}, {@link RemainderStrategy}, and
 * {@code minScaledAmount} controls.
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
        return resolve(config, snapshot, questRarity, rarityRegistry, typeRegistry, new Random());
    }

    /**
     * Evaluates all tiers with a provided random instance for deterministic remainder distribution.
     */
    @NotNull
    public static Map<UUID, List<QuestRewardType>> resolve(
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
                LOGGER.warning("Unrecognized distribution type key: " + tier.getTypeKey()
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

    private static void applyTierRewards(@NotNull DistributionTierConfig tier,
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

    private static void distributeRewardEntry(
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
                QuestRewardType scaled = entry.reward().withAmountMultiplier(baseMultiplier);
                boolean isScalable = scaled != entry.reward();

                if (!isScalable) {
                    LOGGER.warning("Non-scalable reward '" + entry.reward().getKey()
                            + "' used with SCALE pot-behavior; granting unscaled to all qualifying players");
                    for (UUID playerUUID : qualifyingPlayers) {
                        result.computeIfAbsent(playerUUID, k -> new ArrayList<>())
                                .add(entry.reward());
                    }
                    return;
                }

                OptionalLong scaledAmount = scaled.getNumericAmount();
                if (scaledAmount.isPresent() && scaledAmount.getAsLong() < entry.minScaledAmount()) {
                    return;
                }

                for (UUID playerUUID : qualifyingPlayers) {
                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(scaled);
                }

                if (entry.remainderStrategy() != RemainderStrategy.DISCARD) {
                    distributeRemainder(entry, baseMultiplier, qualifyingPlayers,
                            snapshot, result, random);
                }
            }
        }
    }

    private static void distributeProportional(
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
                    QuestRewardType scaled = entry.reward().withAmountMultiplier(multiplier);
                    boolean isScalable = scaled != entry.reward();

                    if (!isScalable) {
                        LOGGER.warning("Non-scalable reward '" + entry.reward().getKey()
                                + "' used with SCALE pot-behavior; granting unscaled to all");
                        for (UUID uuid : qualifyingPlayers) {
                            result.computeIfAbsent(uuid, k -> new ArrayList<>())
                                    .add(entry.reward());
                        }
                        return;
                    }

                    OptionalLong scaledAmount = scaled.getNumericAmount();
                    if (scaledAmount.isPresent() && scaledAmount.getAsLong() < entry.minScaledAmount()) {
                        continue;
                    }

                    result.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(scaled);
                }
            }
        }
    }

    private static void distributeRemainder(
            @NotNull DistributionRewardEntry entry,
            double baseMultiplier,
            @NotNull Set<UUID> qualifyingPlayers,
            @NotNull ContributionSnapshot snapshot,
            @NotNull Map<UUID, List<QuestRewardType>> result,
            @NotNull Random random) {

        OptionalLong originalAmount = entry.reward().getNumericAmount();
        if (originalAmount.isEmpty()) {
            return;
        }

        long total = originalAmount.getAsLong();
        long perPlayer = Math.max(entry.minScaledAmount(),
                Math.round(total * baseMultiplier));
        long distributed = perPlayer * qualifyingPlayers.size();
        long remainder = total - distributed;

        if (remainder <= 0) {
            return;
        }

        switch (entry.remainderStrategy()) {
            case TOP_CONTRIBUTOR -> findTopContributor(qualifyingPlayers, snapshot).ifPresent(top -> {
                QuestRewardType extra = entry.reward().withAmountMultiplier(
                        (double) remainder / total);
                result.computeIfAbsent(top, k -> new ArrayList<>()).add(extra);
            });
            case RANDOM -> {
                List<UUID> shuffled = new ArrayList<>(qualifyingPlayers);
                Collections.shuffle(shuffled, random);
                for (int i = 0; i < remainder && i < shuffled.size(); i++) {
                    QuestRewardType extra = entry.reward().withAmountMultiplier(1.0 / total);
                    result.computeIfAbsent(shuffled.get(i), k -> new ArrayList<>()).add(extra);
                }
            }
            case DISCARD -> {}
        }
    }

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

    @NotNull
    private static Optional<UUID> findTopContributor(@NotNull Set<UUID> qualifyingPlayers,
                                                      @NotNull ContributionSnapshot snapshot) {
        return qualifyingPlayers.stream()
                .max(Comparator.comparingLong(
                        uuid -> snapshot.contributions().getOrDefault(uuid, 0L)));
    }
}
