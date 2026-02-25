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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MembershipDistributionTypeTest extends McRPGBaseTest {

    private final MembershipDistributionType type = new MembershipDistributionType();

    private final DistributionTierConfig tier = new DistributionTierConfig("test",
            MembershipDistributionType.KEY, RewardSplitMode.INDIVIDUAL, List.of(),
            Map.of(), null, null);

    @DisplayName("all group members qualify regardless of contribution")
    @Test
    void allMembersQualify() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(p1, 100L), 100, Set.of(p1, p2, p3));

        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(3, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
        assertTrue(result.contains(p3));
    }

    @DisplayName("members with zero contribution still qualify")
    @Test
    void zerContributionMembersQualify() {
        UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of(p1, p2, p3));

        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(3, result.size());
    }

    @DisplayName("empty group returns empty set")
    @Test
    void emptyGroup() {
        var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of());

        Set<UUID> result = type.resolve(snapshot, tier);
        assertTrue(result.isEmpty());
    }

    @DisplayName("non-members who contributed do not appear in result")
    @Test
    void nonMembersExcluded() {
        UUID member = UUID.randomUUID(), nonMember = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(
                Map.of(member, 50L, nonMember, 100L), 150, Set.of(member));

        Set<UUID> result = type.resolve(snapshot, tier);
        assertEquals(1, result.size());
        assertTrue(result.contains(member));
        assertFalse(result.contains(nonMember));
    }
}
