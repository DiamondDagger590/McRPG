package us.eunoians.mcrpg.event.combat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.state.CombatStateType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatStateChangeEvent")
class CombatStateChangeEventTest extends McRPGBaseTest {

    private static final CombatStateType<Integer> TYPE =
            CombatStateType.of(new NamespacedKey("mcrpg", "stacks"), Integer.class, 0, null);

    @Test
    @DisplayName("Constructor stores session, stateType, oldValue, newValue")
    void constructor_storesAllFields() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);

        CombatStateChangeEvent event = new CombatStateChangeEvent(session, TYPE, 1, 2);

        assertSame(session, event.getSession());
        assertEquals(TYPE, event.getStateType());
        assertEquals(1, event.getOldValue());
        assertEquals(2, event.getNewValue());
    }

    @Test
    @DisplayName("Default cancelled state is false")
    void isCancelled_defaultsFalse() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatStateChangeEvent event = new CombatStateChangeEvent(session, TYPE, 1, 2);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("setCancelled(true) makes isCancelled() return true")
    void setCancelled_true_makesIsCancelledTrue() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatStateChangeEvent event = new CombatStateChangeEvent(session, TYPE, 1, 2);

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("setNewValue replaces the proposed new value")
    void setNewValue_replacesProposedValue() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatStateChangeEvent event = new CombatStateChangeEvent(session, TYPE, 1, 2);

        event.setNewValue(10);

        assertEquals(10, event.getNewValue());
    }

    @Test
    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatStateChangeEvent.getHandlerList());
    }
}
