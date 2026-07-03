package us.eunoians.mcrpg.event.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.Cancellable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.ParticipantRemovalReason;
import us.eunoians.mcrpg.combat.ParticipantType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("CombatParticipantRemoveEvent")
class CombatParticipantRemoveEventTest extends McRPGBaseTest {

    @DisplayName("Constructor stores session, removedParticipant, reason, previousCombatType, newCombatType")
    @Test
    void constructor_storesAllFields() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), System.currentTimeMillis());

        CombatParticipantRemoveEvent event = new CombatParticipantRemoveEvent(
                session, participant, ParticipantRemovalReason.DEATH,
                CombatType.PVE, CombatType.PVE);

        assertEquals(session, event.getSession());
        assertEquals(participant, event.getRemovedParticipant());
        assertEquals(ParticipantRemovalReason.DEATH, event.getReason());
        assertEquals(CombatType.PVE, event.getPreviousCombatType());
        assertEquals(CombatType.PVE, event.getNewCombatType());
    }

    @DisplayName("Event is not cancellable")
    @Test
    void event_isNotCancellable() {
        assertFalse(Cancellable.class.isAssignableFrom(CombatParticipantRemoveEvent.class));
    }

    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    @Test
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatParticipantRemoveEvent.getHandlerList());
    }
}
