package us.eunoians.mcrpg.quest.board.distribution.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Returns all players whose contribution percentage is at or above the configured
 * {@code minContributionPercent} threshold. The percentage is calculated as
 * {@code (playerContribution / totalProgress) * 100}.
 */
public final class ContributionThresholdDistributionType implements RewardDistributionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "contribution_threshold");

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Set<UUID> resolve(@NotNull ContributionSnapshot snapshot,
                             @NotNull DistributionTierConfig tier) {
        double threshold = tier.getMinContributionPercent().orElse(0.0);
        long total = snapshot.totalProgress();
        if (total == 0) {
            return Set.of();
        }

        return snapshot.contributions().entrySet().stream()
                .filter(e -> {
                    double percent = ((double) e.getValue() / total) * 100.0;
                    return percent >= threshold;
                })
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
