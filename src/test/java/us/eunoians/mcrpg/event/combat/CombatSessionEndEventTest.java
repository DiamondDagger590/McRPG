package us.eunoians.mcrpg.event.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.event.Cancellable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.ParticipantType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatSessionEndEvent")
class CombatSessionEndEventTest extends McRPGBaseTest {

    @DisplayName("Constructor stores entityUUID, reason, finalParticipants, finalCombatType, durationMillis")
    @Test
    void constructor_storesAllFields() {
        UUID entityUUID = UUID.randomUUID();
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), System.currentTimeMillis());
        List<CombatParticipant> participants = List.of(participant);

        CombatSessionEndEvent event = new CombatSessionEndEvent(
                entityUUID, CombatSessionEndReason.TIMEOUT,
                participants, CombatType.PVE, 5000L);

        assertEquals(entityUUID, event.getEntityUUID());
        assertEquals(CombatSessionEndReason.TIMEOUT, event.getReason());
        assertEquals(CombatType.PVE, event.getFinalCombatType());
        assertEquals(5000L, event.getDurationMillis());
    }

    @DisplayName("getFinalParticipants() returns an unmodifiable collection")
    @Test
    void getFinalParticipants_returnsUnmodifiable() {
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), System.currentTimeMillis());
        List<CombatParticipant> participants = List.of(participant);

        CombatSessionEndEvent event = new CombatSessionEndEvent(
                UUID.randomUUID(), CombatSessionEndReason.TIMEOUT,
                participants, CombatType.PVE, 5000L);

        Collection<CombatParticipant> finalParticipants = event.getFinalParticipants();
        assertNotNull(finalParticipants);
        assertEquals(1, finalParticipants.size());
        assertThrows(UnsupportedOperationException.class, () -> finalParticipants.clear());
    }

    @DisplayName("getSessionStatistics() returns an empty map (placeholder)")
    @Test
    void getSessionStatistics_returnsEmptyMap() {
        CombatSessionEndEvent event = new CombatSessionEndEvent(
                UUID.randomUUID(), CombatSessionEndReason.DEATH,
                List.of(), CombatType.PVE, 1000L);

        assertNotNull(event.getSessionStatistics());
        assertTrue(event.getSessionStatistics().isEmpty());
    }

    @DisplayName("getCombatStateData() returns an empty map (placeholder)")
    @Test
    void getCombatStateData_returnsEmptyMap() {
        CombatSessionEndEvent event = new CombatSessionEndEvent(
                UUID.randomUUID(), CombatSessionEndReason.DEATH,
                List.of(), CombatType.PVE, 1000L);

        assertNotNull(event.getCombatStateData());
        assertTrue(event.getCombatStateData().isEmpty());
    }

    @DisplayName("Event is not cancellable")
    @Test
    void event_isNotCancellable() {
        assertFalse(Cancellable.class.isAssignableFrom(CombatSessionEndEvent.class));
    }

    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    @Test
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatSessionEndEvent.getHandlerList());
    }
}
