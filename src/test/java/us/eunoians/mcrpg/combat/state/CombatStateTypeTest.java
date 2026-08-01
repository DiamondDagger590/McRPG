package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatStateType")
class CombatStateTypeTest {

    private static final NamespacedKey KEY = new NamespacedKey("mcrpg", "test_state");
    private static final NamespacedKey EXPANSION_KEY = new NamespacedKey("mcrpg", "mcrpg-expansion");

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("creates a SESSION-scoped type with no resolver and no serializer")
        void of_createsSessionScopedType() {
            CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, EXPANSION_KEY);

            assertEquals(CombatStateLifecycle.SESSION, type.getLifecycle());
            assertFalse(type.hasResolver());
            assertTrue(type.getSerializer().isEmpty());
            assertTrue(type.getDeserializer().isEmpty());
            assertFalse(type.isPersistent());
        }
    }

    @Nested
    @DisplayName("resolved()")
    class Resolved {

        @Test
        @DisplayName("creates a SESSION-scoped type with a resolver and no serializer")
        void resolved_createsResolvedType() {
            CombatStateResolver<Integer> resolver = (session, raw) -> raw;
            CombatStateType<Integer> type = CombatStateType.resolved(KEY, Integer.class, 0, resolver, EXPANSION_KEY);

            assertEquals(CombatStateLifecycle.SESSION, type.getLifecycle());
            assertTrue(type.hasResolver());
            assertEquals(resolver, type.getResolver().orElseThrow());
            assertTrue(type.getSerializer().isEmpty());
            assertFalse(type.isPersistent());
        }
    }

    @Nested
    @DisplayName("persistent()")
    class Persistent {

        @Test
        @DisplayName("creates a PERSISTENT-scoped type with serializer/deserializer and no resolver")
        void persistent_createsPersistentType() {
            CombatStateType<Integer> type = CombatStateType.persistent(
                    KEY, Integer.class, 0, String::valueOf, Integer::parseInt, EXPANSION_KEY);

            assertEquals(CombatStateLifecycle.PERSISTENT, type.getLifecycle());
            assertFalse(type.hasResolver());
            assertTrue(type.getSerializer().isPresent());
            assertTrue(type.getDeserializer().isPresent());
            assertTrue(type.isPersistent());
        }
    }

    @Test
    @DisplayName("getKey returns the construction key")
    void getKey_returnsConstructionKey() {
        CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, null);
        assertEquals(KEY, type.getKey());
    }

    @Test
    @DisplayName("getType returns the construction class")
    void getType_returnsConstructionClass() {
        CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, null);
        assertEquals(Integer.class, type.getType());
    }

    @Test
    @DisplayName("getDefaultValue returns the construction default")
    void getDefaultValue_returnsConstructionDefault() {
        CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 42, null);
        assertEquals(42, type.getDefaultValue());
    }

    @Test
    @DisplayName("getExpansionKey returns the construction expansion key wrapped in Optional")
    void getExpansionKey_returnsWrappedKey() {
        CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, EXPANSION_KEY);
        assertEquals(EXPANSION_KEY, type.getExpansionKey().orElseThrow());
    }

    @Test
    @DisplayName("getExpansionKey returns empty when constructed with a null expansion key")
    void getExpansionKey_returnsEmpty_whenNullExpansionKey() {
        CombatStateType<Integer> type = CombatStateType.of(KEY, Integer.class, 0, null);
        assertTrue(type.getExpansionKey().isEmpty());
    }
}
