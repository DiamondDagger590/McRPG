package us.eunoians.mcrpg.quest.board.distribution.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContributionThresholdDistributionTypeTest extends McRPGBaseTest {

    private final ContributionThresholdDistributionType type = new ContributionThresholdDistributionType();

    private DistributionTierConfig tierWithThreshold(double threshold) {
        return new DistributionTierConfig("test", ContributionThresholdDistributionType.KEY,
                RewardSplitMode.INDIVIDUAL, List.of(),
                Map.of(DistributionTierConfig.PARAM_MIN_CONTRIBUTION_PERCENT, threshold), null, null);
    }

    @DisplayName("single player with 100% contribution qualifies for any threshold")
    @Test
    void singlePlayerFullContribution() {
        UUID player = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(player, 500L), 500, Set.of(player));

        Set<UUID> result = type.resolve(snapshot, tierWithThreshold(50.0));
        assertEquals(Set.of(player), result);
    }

    @DisplayName("three players: 50/30/20 with threshold 20 → all qualify")
    @Test
    void allQualifyAboveThreshold() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 50L, p2, 30L, p3, 20L), 100, Set.of(p1, p2, p3));

        Set<UUID> result = type.resolve(snapshot, tierWithThreshold(20.0));
        assertEquals(3, result.size());
    }

    @DisplayName("three players: 50/30/20 with threshold 25 → first two qualify")
    @Test
    void partialQualification() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 50L, p2, 30L, p3, 20L), 100, Set.of(p1, p2, p3));

        Set<UUID> result = type.resolve(snapshot, tierWithThreshold(25.0));
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }

    @DisplayName("zero total progress returns empty set")
    @Test
    void zeroTotalProgress() {
        var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of());

        Set<UUID> result = type.resolve(snapshot, tierWithThreshold(10.0));
        assertTrue(result.isEmpty());
    }

    @DisplayName("player at exactly threshold percentage qualifies (inclusive)")
    @Test
    void exactThresholdInclusive() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 75L, p2, 25L), 100, Set.of(p1, p2));

        Set<UUID> result = type.resolve(snapshot, tierWithThreshold(25.0));
        assertEquals(2, result.size());
    }

    @DisplayName("threshold of 0 includes all contributors")
    @Test
    void zeroThreshold() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 1L, p2, 1L), 2, Set.of(p1, p2));

        Set<UUID> result = type.resolve(snapshot, tierWithThreshold(0.0));
        assertEquals(2, result.size());
    }
}
