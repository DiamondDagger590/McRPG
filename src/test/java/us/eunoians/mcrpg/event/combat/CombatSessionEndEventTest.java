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
import us.eunoians.mcrpg.combat.state.CombatStateSnapshot;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatistics;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatSessionEndEvent")
class CombatSessionEndEventTest extends McRPGBaseTest {

    /**
     * Creates an empty {@link CombatSessionStatisticsSnapshot} for constructor tests that don't care
     * about specific statistic values.
     *
     * @return A new empty {@link CombatSessionStatisticsSnapshot}.
     */
    private static CombatSessionStatisticsSnapshot emptyStatisticsSnapshot() {
        return new CombatSessionStatistics().snapshot();
    }

    /**
     * Creates an empty {@link CombatStateSnapshot} for constructor tests that don't care about
     * specific state values.
     *
     * @return A new empty {@link CombatStateSnapshot}.
     */
    private static CombatStateSnapshot emptyStateSnapshot() {
        return new CombatStateSnapshot(Map.of(), Map.of());
    }

    @DisplayName("Constructor stores entityUUID, reason, finalParticipants, finalCombatType, durationMillis, statistics, combatState")
    @Test
    void constructor_storesAllFields() {
        UUID entityUUID = UUID.randomUUID();
        CombatParticipant participant = new CombatParticipant(
                UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"), System.currentTimeMillis());
        List<CombatParticipant> participants = List.of(participant);
        CombatSessionStatisticsSnapshot statisticsSnapshot = emptyStatisticsSnapshot();
        CombatStateSnapshot stateSnapshot = emptyStateSnapshot();

        CombatSessionEndEvent event = new CombatSessionEndEvent(
                entityUUID, CombatSessionEndReason.TIMEOUT,
                participants, CombatType.PVE, 5000L, statisticsSnapshot, stateSnapshot);

        assertEquals(entityUUID, event.getEntityUUID());
        assertEquals(CombatSessionEndReason.TIMEOUT, event.getReason());
        assertEquals(CombatType.PVE, event.getFinalCombatType());
        assertEquals(5000L, event.getDurationMillis());
        assertSame(statisticsSnapshot, event.getStatistics());
        assertSame(stateSnapshot, event.getCombatState());
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
                participants, CombatType.PVE, 5000L, emptyStatisticsSnapshot(), emptyStateSnapshot());

        Collection<CombatParticipant> finalParticipants = event.getFinalParticipants();
        assertNotNull(finalParticipants);
        assertEquals(1, finalParticipants.size());
        assertThrows(UnsupportedOperationException.class, () -> finalParticipants.clear());
    }

    @DisplayName("getStatistics() returns the exact snapshot instance passed to the constructor")
    @Test
    void getStatistics_returnsExactInstance() {
        CombatSessionStatisticsSnapshot statisticsSnapshot = emptyStatisticsSnapshot();

        CombatSessionEndEvent event = new CombatSessionEndEvent(
                UUID.randomUUID(), CombatSessionEndReason.TIMEOUT,
                List.of(), CombatType.PVE, 5000L, statisticsSnapshot, emptyStateSnapshot());

        assertSame(statisticsSnapshot, event.getStatistics());
    }

    @DisplayName("getCombatState() returns the exact snapshot instance passed to the constructor")
    @Test
    void getCombatState_returnsExactInstance() {
        CombatStateSnapshot stateSnapshot = emptyStateSnapshot();

        CombatSessionEndEvent event = new CombatSessionEndEvent(
                UUID.randomUUID(), CombatSessionEndReason.TIMEOUT,
                List.of(), CombatType.PVE, 5000L, emptyStatisticsSnapshot(), stateSnapshot);

        assertSame(stateSnapshot, event.getCombatState());
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
