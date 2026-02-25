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

public class ParticipatedDistributionTypeTest extends McRPGBaseTest {

    private final ParticipatedDistributionType type = new ParticipatedDistributionType();

    private final DistributionTierConfig tier = new DistributionTierConfig("test",
            ParticipatedDistributionType.KEY, RewardSplitMode.INDIVIDUAL, List.of(),
            Map.of(), null, null);

    @DisplayName("single contributor qualifies")
    @Test
    void singleContributor() {
        UUID player = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(player, 50L), 50, Set.of(player));

        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(Set.of(player), result);
    }

    @DisplayName("multiple contributors all qualify")
    @Test
    void multipleContributors() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 30L, p2, 20L, p3, 10L), 60, Set.of(p1, p2, p3));

        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(3, result.size());
    }

    @DisplayName("player with zero contribution does not qualify")
    @Test
    void zeroContributionExcluded() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L, p2, 0L), 100, Set.of(p1, p2));

        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(1, result.size());
        assertTrue(result.contains(p1));
    }

    @DisplayName("empty contributions returns empty set")
    @Test
    void emptyContributions() {
        var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of());

        Set<UUID> result = type.resolve(snapshot, tier);
        assertTrue(result.isEmpty());
    }
}
