package us.eunoians.mcrpg.event.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.ParticipantType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatParticipantAddEvent")
class CombatParticipantAddEventTest extends McRPGBaseTest {

    @DisplayName("Constructor stores session, newParticipant, previousCombatType, newCombatType")
    @Test
    void constructor_storesAllFields() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.PLAYER,
                new CustomEntityWrapper("PLAYER"), System.currentTimeMillis());

        CombatParticipantAddEvent event = new CombatParticipantAddEvent(
                session, participant, CombatType.PVE, CombatType.PVP);

        assertEquals(session, event.getSession());
        assertEquals(participant, event.getNewParticipant());
        assertEquals(CombatType.PVE, event.getPreviousCombatType());
        assertEquals(CombatType.PVP, event.getNewCombatType());
    }

    @DisplayName("Default cancelled state is false")
    @Test
    void defaultCancelledState_isFalse() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), System.currentTimeMillis());

        CombatParticipantAddEvent event = new CombatParticipantAddEvent(
                session, participant, CombatType.PVE, CombatType.PVE);

        assertFalse(event.isCancelled());
    }

    @DisplayName("setCancelled(true) makes isCancelled() return true")
    @Test
    void setCancelled_makesIsCancelledTrue() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), System.currentTimeMillis());

        CombatParticipantAddEvent event = new CombatParticipantAddEvent(
                session, participant, CombatType.PVE, CombatType.PVE);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    @Test
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatParticipantAddEvent.getHandlerList());
    }
}
