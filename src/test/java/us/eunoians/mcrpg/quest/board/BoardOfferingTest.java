package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BoardOfferingTest extends McRPGBaseTest {

    private static BoardOffering newOffering() {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"),
                2,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                null,
                Duration.ofHours(24)
        );
    }

    private static BoardOffering newOfferingWithScope(String scopeTargetId) {
        return new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"),
                3,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                scopeTargetId,
                Duration.ofHours(24)
        );
    }

    @DisplayName("New offering starts in VISIBLE state")
    @Test
    void newOffering_startsInVisibleState() {
        BoardOffering offering = newOffering();
        assertEquals(BoardOffering.State.VISIBLE, offering.getState());
    }

    @DisplayName("VISIBLE -> ACCEPTED via accept() succeeds and sets acceptedAt and questInstanceUUID")
    @Test
    void visibleToAccepted_viaAccept_succeeds() {
        BoardOffering offering = newOffering();
        long acceptedAt = System.currentTimeMillis();
        UUID questInstanceUUID = UUID.randomUUID();

        offering.accept(acceptedAt, questInstanceUUID);

        assertEquals(BoardOffering.State.ACCEPTED, offering.getState());
        assertTrue(offering.getAcceptedAt().isPresent());
        assertEquals(acceptedAt, offering.getAcceptedAt().get());
        assertTrue(offering.getQuestInstanceUUID().isPresent());
        assertEquals(questInstanceUUID, offering.getQuestInstanceUUID().get());
    }

    @DisplayName("VISIBLE -> EXPIRED succeeds")
    @Test
    void visibleToExpired_succeeds() {
        BoardOffering offering = newOffering();
        offering.transitionTo(BoardOffering.State.EXPIRED);
        assertEquals(BoardOffering.State.EXPIRED, offering.getState());
    }

    @DisplayName("EXPIRED -> ACCEPTED throws IllegalStateException")
    @Test
    void expiredToAccepted_throwsIllegalStateException() {
        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"),
                0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                null,
                Duration.ofHours(24),
                BoardOffering.State.EXPIRED,
                null,
                null
        );

        assertThrows(IllegalStateException.class, () -> offering.accept(System.currentTimeMillis(), UUID.randomUUID()));
    }

    @DisplayName("COMPLETED is terminal")
    @Test
    void completed_isTerminal() {
        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"),
                0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                null,
                Duration.ofHours(24),
                BoardOffering.State.COMPLETED,
                1L,
                UUID.randomUUID()
        );

        assertFalse(offering.canTransitionTo(BoardOffering.State.VISIBLE));
        assertFalse(offering.canTransitionTo(BoardOffering.State.ACCEPTED));
        assertFalse(offering.canTransitionTo(BoardOffering.State.EXPIRED));
        assertFalse(offering.canTransitionTo(BoardOffering.State.ABANDONED));

        assertThrows(IllegalStateException.class, () -> offering.transitionTo(BoardOffering.State.EXPIRED));
    }

    @DisplayName("ABANDONED is terminal")
    @Test
    void abandoned_isTerminal() {
        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"),
                0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                null,
                Duration.ofHours(24),
                BoardOffering.State.ABANDONED,
                1L,
                UUID.randomUUID()
        );

        assertFalse(offering.canTransitionTo(BoardOffering.State.VISIBLE));
        assertFalse(offering.canTransitionTo(BoardOffering.State.ACCEPTED));
        assertFalse(offering.canTransitionTo(BoardOffering.State.COMPLETED));
        assertFalse(offering.canTransitionTo(BoardOffering.State.EXPIRED));

        assertThrows(IllegalStateException.class, () -> offering.transitionTo(BoardOffering.State.COMPLETED));
    }

    @DisplayName("EXPIRED is terminal")
    @Test
    void expired_isTerminal() {
        BoardOffering offering = new BoardOffering(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "shared_daily"),
                0,
                new NamespacedKey("mcrpg", "test_quest"),
                new NamespacedKey("mcrpg", "common"),
                null,
                Duration.ofHours(24),
                BoardOffering.State.EXPIRED,
                null,
                null
        );

        assertFalse(offering.canTransitionTo(BoardOffering.State.ACCEPTED));
        assertFalse(offering.canTransitionTo(BoardOffering.State.COMPLETED));
        assertFalse(offering.canTransitionTo(BoardOffering.State.ABANDONED));

        assertThrows(IllegalStateException.class, () -> offering.accept(System.currentTimeMillis(), UUID.randomUUID()));
    }

    @DisplayName("canTransitionTo returns true for valid transitions")
    @Test
    void canTransitionTo_validTransitions_returnsTrue() {
        BoardOffering visible = newOffering();
        assertTrue(visible.canTransitionTo(BoardOffering.State.ACCEPTED));
        assertTrue(visible.canTransitionTo(BoardOffering.State.EXPIRED));

        visible.accept(System.currentTimeMillis(), UUID.randomUUID());
        assertTrue(visible.canTransitionTo(BoardOffering.State.COMPLETED));
        assertTrue(visible.canTransitionTo(BoardOffering.State.ABANDONED));
        assertTrue(visible.canTransitionTo(BoardOffering.State.EXPIRED));
    }

    @DisplayName("canTransitionTo returns false for invalid transitions")
    @Test
    void canTransitionTo_invalidTransitions_returnsFalse() {
        BoardOffering visible = newOffering();
        assertFalse(visible.canTransitionTo(BoardOffering.State.COMPLETED));
        assertFalse(visible.canTransitionTo(BoardOffering.State.ABANDONED));

        visible.accept(System.currentTimeMillis(), UUID.randomUUID());
        assertFalse(visible.canTransitionTo(BoardOffering.State.VISIBLE));
    }

    @DisplayName("Getters return correct values")
    @Test
    void getters_returnCorrectValues() {
        UUID offeringId = UUID.randomUUID();
        UUID rotationId = UUID.randomUUID();
        NamespacedKey categoryKey = new NamespacedKey("mcrpg", "shared_daily");
        int slotIndex = 5;
        NamespacedKey questDefKey = new NamespacedKey("mcrpg", "test_quest");
        NamespacedKey rarityKey = new NamespacedKey("mcrpg", "common");
        Duration completionTime = Duration.ofHours(24);

        BoardOffering offering = new BoardOffering(
                offeringId,
                rotationId,
                categoryKey,
                slotIndex,
                questDefKey,
                rarityKey,
                null,
                completionTime
        );

        assertEquals(offeringId, offering.getOfferingId());
        assertEquals(rotationId, offering.getRotationId());
        assertEquals(categoryKey, offering.getCategoryKey());
        assertEquals(slotIndex, offering.getSlotIndex());
        assertEquals(questDefKey, offering.getQuestDefinitionKey());
        assertEquals(rarityKey, offering.getRarityKey());
        assertEquals(completionTime, offering.getCompletionTime());
    }

    @DisplayName("getScopeTargetId returns empty for null")
    @Test
    void getScopeTargetId_null_returnsEmpty() {
        BoardOffering offering = newOffering();
        assertTrue(offering.getScopeTargetId().isEmpty());
    }

    @DisplayName("getScopeTargetId returns present for non-null")
    @Test
    void getScopeTargetId_nonNull_returnsPresent() {
        String scopeTargetId = "land-uuid-123";
        BoardOffering offering = newOfferingWithScope(scopeTargetId);
        assertTrue(offering.getScopeTargetId().isPresent());
        assertEquals(scopeTargetId, offering.getScopeTargetId().get());
    }

    @DisplayName("getAcceptedAt returns empty before acceptance")
    @Test
    void getAcceptedAt_beforeAcceptance_returnsEmpty() {
        BoardOffering offering = newOffering();
        assertTrue(offering.getAcceptedAt().isEmpty());
    }

    @DisplayName("getAcceptedAt returns present after acceptance")
    @Test
    void getAcceptedAt_afterAcceptance_returnsPresent() {
        BoardOffering offering = newOffering();
        long acceptedAt = 1234567890L;
        offering.accept(acceptedAt, UUID.randomUUID());
        assertTrue(offering.getAcceptedAt().isPresent());
        assertEquals(acceptedAt, offering.getAcceptedAt().get());
    }

    @DisplayName("getQuestInstanceUUID returns empty before acceptance")
    @Test
    void getQuestInstanceUUID_beforeAcceptance_returnsEmpty() {
        BoardOffering offering = newOffering();
        assertTrue(offering.getQuestInstanceUUID().isEmpty());
    }

    @DisplayName("getQuestInstanceUUID returns present after acceptance")
    @Test
    void getQuestInstanceUUID_afterAcceptance_returnsPresent() {
        BoardOffering offering = newOffering();
        UUID questInstanceUUID = UUID.randomUUID();
        offering.accept(System.currentTimeMillis(), questInstanceUUID);
        assertTrue(offering.getQuestInstanceUUID().isPresent());
        assertEquals(questInstanceUUID, offering.getQuestInstanceUUID().get());
    }
}
