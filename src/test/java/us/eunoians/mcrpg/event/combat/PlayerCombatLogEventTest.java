package us.eunoians.mcrpg.event.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("PlayerCombatLogEvent")
class PlayerCombatLogEventTest extends McRPGBaseTest {

    @Test
    @DisplayName("constructor stores player, session, combatType, and participants")
    void constructor_storesAllFields() {
        var player = server.addPlayer();
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatParticipant participant = mock(CombatParticipant.class);
        List<CombatParticipant> participants = List.of(participant);

        PlayerCombatLogEvent event = new PlayerCombatLogEvent(player, session, CombatType.PVP, participants);

        assertSame(player, event.getPlayer());
        assertSame(session, event.getSession());
        assertEquals(CombatType.PVP, event.getCombatType());
        assertEquals(1, event.getParticipants().size());
    }

    @Test
    @DisplayName("getParticipants returns an unmodifiable collection")
    void getParticipants_returnsUnmodifiable() {
        var player = server.addPlayer();
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        PlayerCombatLogEvent event = new PlayerCombatLogEvent(player, session, CombatType.PVE, List.of());

        Collection<CombatParticipant> participants = event.getParticipants();
        assertNotNull(participants);
        assertThrows(UnsupportedOperationException.class, () -> participants.add(mock(CombatParticipant.class)));
    }

    @Test
    @DisplayName("default cancelled state is false")
    void defaultCancelled_isFalse() {
        var player = server.addPlayer();
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        PlayerCombatLogEvent event = new PlayerCombatLogEvent(player, session, CombatType.PVE, List.of());

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes isCancelled return true")
    void setCancelled_true_makesIsCancelledTrue() {
        var player = server.addPlayer();
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        PlayerCombatLogEvent event = new PlayerCombatLogEvent(player, session, CombatType.PVE, List.of());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("getHandlerList returns a non-null static HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(PlayerCombatLogEvent.getHandlerList());
    }
}
