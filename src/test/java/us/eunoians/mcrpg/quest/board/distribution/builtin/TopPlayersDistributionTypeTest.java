package us.eunoians.mcrpg.quest.board.distribution.builtin;

import org.bukkit.NamespacedKey;
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

public class TopPlayersDistributionTypeTest extends McRPGBaseTest {

    private final TopPlayersDistributionType type = new TopPlayersDistributionType();

    private DistributionTierConfig tierWithCount(int count) {
        return new DistributionTierConfig("test", TopPlayersDistributionType.KEY,
                RewardSplitMode.INDIVIDUAL, List.of(),
                Map.of(DistributionTierConfig.PARAM_TOP_PLAYER_COUNT, count), null, null, true);
    }

    @DisplayName("single contributor qualifies as top 1")
    @Test
    void singleContributor() {
        UUID player = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(player, 100L), 100, Set.of(player), null);

        Set<UUID> result = type.resolve(snapshot, tierWithCount(1));
        assertEquals(Set.of(player), result);
    }

    @DisplayName("top N selected correctly from multiple contributors")
    @Test
    void topNFromMultiple() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 300L, p2, 200L, p3, 100L), 600, Set.of(p1, p2, p3), null);

        Set<UUID> top2 = type.resolve(snapshot, tierWithCount(2));
        assertEquals(2, top2.size());
        assertTrue(top2.contains(p1));
        assertTrue(top2.contains(p2));
    }

    @DisplayName("tie at boundary includes all tied players")
    @Test
    void tieAtBoundary() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L, p2, 100L, p3, 50L), 250, Set.of(p1, p2, p3), null);

        Set<UUID> top1 = type.resolve(snapshot, tierWithCount(1));
        assertEquals(2, top1.size());
        assertTrue(top1.contains(p1));
        assertTrue(top1.contains(p2));
    }

    @DisplayName("topPlayerCount exceeding contributor count returns all contributors")
    @Test
    void topPlayerCountExceedsContributors() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L, p2, 50L), 150, Set.of(p1, p2), null);

        Set<UUID> top5 = type.resolve(snapshot, tierWithCount(5));
        assertEquals(2, top5.size());
    }

    @DisplayName("zero contributions returns empty set")
    @Test
    void zeroContributions() {
        var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of(), null);

        Set<UUID> result = type.resolve(snapshot, tierWithCount(1));
        assertTrue(result.isEmpty());
    }

    @DisplayName("players with zero contribution excluded")
    @Test
    void zeroContributionExcluded() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L, p2, 0L), 100, Set.of(p1, p2), null);

        Set<UUID> result = type.resolve(snapshot, tierWithCount(2));
        assertEquals(1, result.size());
        assertTrue(result.contains(p1));
    }

    @DisplayName("null topPlayerCount defaults to 1")
    @Test
    void nullTopPlayerCountDefaultsToOne() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 200L, p2, 100L), 300, Set.of(p1, p2), null);

        var tier = new DistributionTierConfig("test", TopPlayersDistributionType.KEY,
                RewardSplitMode.INDIVIDUAL, List.of(), Map.of(), null, null, true);
        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(1, result.size());
        assertTrue(result.contains(p1));
    }
}
