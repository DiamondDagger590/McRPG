package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the scope guards that keep scoped/personal offerings off the shared board and out of the
 * shared-board acceptance path. These are pure-Java static helpers, so no MockBukkit bootstrap is needed.
 */
public class QuestBoardManagerScopeGuardTest {

    private BoardOffering offering(@Nullable String scopeTargetId, int slot) {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "daily_shared"),
                slot,
                new NamespacedKey("mcrpg", "mine_stone"),
                new NamespacedKey("mcrpg", "common"),
                scopeTargetId,
                Duration.ofDays(1));
    }

    @Test
    @DisplayName("filterSharedOfferings drops scoped and personal offerings")
    void filterSharedOfferings_dropsScopedAndPersonal() {
        BoardOffering shared = offering(null, 0);
        BoardOffering scoped = offering("lands-entity-42", 1);
        BoardOffering personal = offering(UUID.randomUUID().toString(), 2);

        List<BoardOffering> result = QuestBoardManager.filterSharedOfferings(List.of(shared, scoped, personal));

        assertEquals(1, result.size());
        assertEquals(shared.getOfferingId(), result.get(0).getOfferingId());
    }

    @Test
    @DisplayName("canAcceptThroughSharedBoard allows an unscoped offering")
    void canAcceptThroughSharedBoard_allowsUnscoped() {
        assertTrue(QuestBoardManager.canAcceptThroughSharedBoard(offering(null, 0), UUID.randomUUID()));
    }

    @Test
    @DisplayName("canAcceptThroughSharedBoard rejects a scoped offering")
    void canAcceptThroughSharedBoard_rejectsScoped() {
        assertFalse(QuestBoardManager.canAcceptThroughSharedBoard(offering("lands-entity-42", 0), UUID.randomUUID()));
    }

    @Test
    @DisplayName("canAcceptThroughSharedBoard rejects another player's personal offering")
    void canAcceptThroughSharedBoard_rejectsOtherPlayersPersonal() {
        UUID owner = UUID.randomUUID();
        UUID clicker = UUID.randomUUID();
        assertFalse(QuestBoardManager.canAcceptThroughSharedBoard(offering(owner.toString(), 0), clicker));
    }

    @Test
    @DisplayName("canAcceptThroughSharedBoard allows the player's own personal offering")
    void canAcceptThroughSharedBoard_allowsOwnPersonal() {
        UUID player = UUID.randomUUID();
        assertTrue(QuestBoardManager.canAcceptThroughSharedBoard(offering(player.toString(), 0), player));
    }

    @Test
    @DisplayName("WRONG_SCOPE is not an accepted result")
    void wrongScope_isNotAccepted() {
        assertFalse(OfferingAcceptResult.WRONG_SCOPE.isAccepted());
    }
}
