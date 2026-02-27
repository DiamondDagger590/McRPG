package us.eunoians.mcrpg.quest.board.distribution;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContributionSnapshotTest extends McRPGBaseTest {

    @DisplayName("constructor creates defensive copies — input mutations don't affect snapshot")
    @Test
    void defensiveCopies() {
        UUID player = UUID.randomUUID();
        Map<UUID, Long> mutableContributions = new HashMap<>();
        mutableContributions.put(player, 100L);
        Set<UUID> mutableMembers = new HashSet<>();
        mutableMembers.add(player);

        var snapshot = new ContributionSnapshot(mutableContributions, 100, mutableMembers, null);

        mutableContributions.put(UUID.randomUUID(), 50L);
        mutableMembers.add(UUID.randomUUID());

        assertEquals(1, snapshot.contributions().size());
        assertEquals(1, snapshot.groupMembers().size());
    }

    @DisplayName("contributions map is unmodifiable")
    @Test
    void contributionsUnmodifiable() {
        UUID player = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(player, 100L), 100, Set.of(player), null);

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.contributions().put(UUID.randomUUID(), 50L));
    }

    @DisplayName("groupMembers set is unmodifiable")
    @Test
    void groupMembersUnmodifiable() {
        UUID player = UUID.randomUUID();
        var snapshot = new ContributionSnapshot(Map.of(player, 100L), 100, Set.of(player), null);

        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.groupMembers().add(UUID.randomUUID()));
    }

    @DisplayName("totalProgress matches provided value")
    @Test
    void totalProgressPreserved() {
        var snapshot = new ContributionSnapshot(Map.of(UUID.randomUUID(), 42L), 42, Set.of(), null);
        assertEquals(42, snapshot.totalProgress());
    }

    @DisplayName("empty contributions and empty members")
    @Test
    void emptyState() {
        var snapshot = new ContributionSnapshot(Map.of(), 0, Set.of(), null);

        assertTrue(snapshot.contributions().isEmpty());
        assertTrue(snapshot.groupMembers().isEmpty());
        assertEquals(0, snapshot.totalProgress());
    }
}
