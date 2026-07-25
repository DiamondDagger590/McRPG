package us.eunoians.mcrpg.event.combat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.state.CombatStateType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @DisplayName("setNewValue rejects a value of the wrong type")
    void setNewValue_throws_whenTypeDoesNotMatch() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatStateChangeEvent event = new CombatStateChangeEvent(session, TYPE, 1, 2);

        // Validating here rather than at the store means Bukkit blames the listener that made the
        // bad call, instead of surfacing a ClassCastException in whoever reads the state next.
        assertThrows(IllegalArgumentException.class, () -> event.setNewValue("not an integer"));
        assertEquals(2, event.getNewValue());
    }

    @Test
    @DisplayName("setNewValue rejects null")
    void setNewValue_throws_whenNull() {
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatStateChangeEvent event = new CombatStateChangeEvent(session, TYPE, 1, 2);

        assertThrows(IllegalArgumentException.class, () -> event.setNewValue(null));
    }

    @Test
    @DisplayName("setNewValue accepts a boxed value for a primitive class token")
    void setNewValue_acceptsBoxedValue_forPrimitiveToken() {
        // CombatStateType.of(key, int.class, 0, null) compiles — int.class has static type
        // Class<Integer> — so a plain isInstance check would reject every write to such a type.
        CombatStateType<Integer> primitiveTokenType = CombatStateType.of(
                new NamespacedKey("mcrpg", "primitive_stacks"), int.class, 0, null);
        CombatSession session = new CombatSession(UUID.randomUUID(), 16, 8000L);
        CombatStateChangeEvent event = new CombatStateChangeEvent(session, primitiveTokenType, 1, 2);

        assertDoesNotThrow(() -> event.setNewValue(10));
        assertEquals(10, event.getNewValue());
    }

    @Test
    @DisplayName("isAssignableToStateType maps primitive tokens to their boxed types")
    void isAssignableToStateType_handlesPrimitiveTokens() {
        CombatStateType<Integer> primitiveTokenType = CombatStateType.of(
                new NamespacedKey("mcrpg", "primitive_stacks"), int.class, 0, null);

        assertTrue(CombatStateChangeEvent.isAssignableToStateType(primitiveTokenType, 5));
        assertFalse(CombatStateChangeEvent.isAssignableToStateType(primitiveTokenType, "five"));
        assertFalse(CombatStateChangeEvent.isAssignableToStateType(primitiveTokenType, null));
    }

    @Test
    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatStateChangeEvent.getHandlerList());
    }
}
