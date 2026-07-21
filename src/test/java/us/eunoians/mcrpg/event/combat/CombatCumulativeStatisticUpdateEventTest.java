package us.eunoians.mcrpg.event.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatistics;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatisticsSnapshot;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatCumulativeStatisticUpdateEvent")
class CombatCumulativeStatisticUpdateEventTest extends McRPGBaseTest {

    @Test
    @DisplayName("Constructor stores entityUUID and statistics")
    void constructor_storesAllFields() {
        UUID entityUUID = UUID.randomUUID();
        CombatSessionStatisticsSnapshot statistics = new CombatSessionStatistics().snapshot();

        CombatCumulativeStatisticUpdateEvent event =
                new CombatCumulativeStatisticUpdateEvent(entityUUID, statistics);

        assertEquals(entityUUID, event.getEntityUUID());
        assertSame(statistics, event.getStatistics());
    }

    @Test
    @DisplayName("Default cancelled state is false")
    void isCancelled_defaultsFalse() {
        CombatCumulativeStatisticUpdateEvent event = new CombatCumulativeStatisticUpdateEvent(
                UUID.randomUUID(), new CombatSessionStatistics().snapshot());

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes isCancelled() return true")
    void setCancelled_true_makesIsCancelledTrue() {
        CombatCumulativeStatisticUpdateEvent event = new CombatCumulativeStatisticUpdateEvent(
                UUID.randomUUID(), new CombatSessionStatistics().snapshot());

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatCumulativeStatisticUpdateEvent.getHandlerList());
    }
}
