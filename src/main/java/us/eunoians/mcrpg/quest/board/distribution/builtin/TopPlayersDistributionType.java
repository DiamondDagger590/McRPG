package us.eunoians.mcrpg.quest.board.distribution.builtin;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Returns the top {@code topPlayerCount} contributors ranked by total contribution.
 * Ties at the boundary are resolved by including all tied players (e.g., if
 * {@code topPlayerCount=1} and two players tie for first, both qualify).
 */
public final class TopPlayersDistributionType implements RewardDistributionType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "top_players");

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Set<UUID> resolve(@NotNull ContributionSnapshot snapshot,
                             @NotNull DistributionTierConfig tier) {
        int count = tier.getTopPlayerCount().orElse(1);
        Map<UUID, Long> contributions = snapshot.contributions();
        if (contributions.isEmpty()) {
            return Set.of();
        }

        List<Map.Entry<UUID, Long>> sorted = contributions.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .toList();

        if (sorted.isEmpty()) {
            return Set.of();
        }

        Set<UUID> result = new HashSet<>();
        long boundaryValue = -1;

        for (Map.Entry<UUID, Long> entry : sorted) {
            if (result.size() < count) {
                result.add(entry.getKey());
                boundaryValue = entry.getValue();
            } else if (entry.getValue() == boundaryValue) {
                result.add(entry.getKey());
            } else {
                break;
            }
        }

        return Set.copyOf(result);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
