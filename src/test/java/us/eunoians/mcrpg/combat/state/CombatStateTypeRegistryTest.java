package us.eunoians.mcrpg.combat.state;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatStateTypeRegistry")
class CombatStateTypeRegistryTest {

    private CombatStateTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CombatStateTypeRegistry();
    }

    private CombatStateType<Integer> sessionType(String key) {
        return CombatStateType.of(new NamespacedKey("mcrpg", key), Integer.class, 0, null);
    }

    private CombatStateType<Integer> persistentType(String key) {
        return CombatStateType.persistent(new NamespacedKey("mcrpg", key), Integer.class, 0,
                String::valueOf, Integer::parseInt, null);
    }

    @Test
    @DisplayName("register then get returns the same type")
    void register_getReturnsSameType() {
        CombatStateType<Integer> type = sessionType("stacks");
        registry.register(type);

        assertSame(type, registry.get(type.getKey()).orElse(null));
    }

    @Test
    @DisplayName("get returns empty for an unregistered key")
    void get_returnsEmpty_whenUnregistered() {
        assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("register throws IllegalStateException on a duplicate key")
    void register_throwsIllegalState_onDuplicateKey() {
        registry.register(sessionType("stacks"));

        assertThrows(IllegalStateException.class, () -> registry.register(sessionType("stacks")));
    }

    @Test
    @DisplayName("unregister removes and returns the type")
    void unregister_removesAndReturnsType() {
        CombatStateType<Integer> type = sessionType("stacks");
        registry.register(type);

        assertSame(type, registry.unregister(type.getKey()).orElse(null));
        assertFalse(registry.isRegistered(type.getKey()));
    }

    @Test
    @DisplayName("unregister returns empty when the key was not registered")
    void unregister_returnsEmpty_whenNotRegistered() {
        assertTrue(registry.unregister(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("isRegistered reflects registration state")
    void isRegistered_reflectsRegistrationState() {
        CombatStateType<Integer> type = sessionType("stacks");

        assertFalse(registry.isRegistered(type.getKey()));
        registry.register(type);
        assertTrue(registry.isRegistered(type.getKey()));
    }

    @Test
    @DisplayName("getAll returns all registered types")
    void getAll_returnsAllRegistered() {
        CombatStateType<Integer> a = sessionType("a");
        CombatStateType<Integer> b = sessionType("b");
        registry.register(a);
        registry.register(b);

        assertEquals(2, registry.getAll().size());
        assertTrue(registry.getAll().contains(a));
        assertTrue(registry.getAll().contains(b));
    }

    @Test
    @DisplayName("getPersistentTypes returns only types with PERSISTENT lifecycle")
    void getPersistentTypes_returnsOnlyPersistent() {
        CombatStateType<Integer> session = sessionType("session_scoped");
        CombatStateType<Integer> persistent = persistentType("persistent_scoped");
        registry.register(session);
        registry.register(persistent);

        assertEquals(1, registry.getPersistentTypes().size());
        assertSame(persistent, registry.getPersistentTypes().get(0));
    }

    @Test
    @DisplayName("getPersistentTypes returns an empty list when no persistent types are registered")
    void getPersistentTypes_returnsEmpty_whenNonePersistent() {
        registry.register(sessionType("session_scoped"));

        assertTrue(registry.getPersistentTypes().isEmpty());
    }

    @Test
    @DisplayName("registered compares by key")
    void registered_comparesByKey() {
        CombatStateType<Integer> type = sessionType("stacks");

        assertFalse(registry.registered(type));
        registry.register(type);
        assertTrue(registry.registered(type));
    }
}
