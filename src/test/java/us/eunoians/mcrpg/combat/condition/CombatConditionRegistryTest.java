package us.eunoians.mcrpg.combat.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CombatConditionRegistry")
class CombatConditionRegistryTest extends McRPGBaseTest {

    private CombatConditionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CombatConditionRegistry();
    }

    /**
     * Creates a mock {@link CombatCondition} whose key is {@code mcrpg:<key>}.
     *
     * @param key The key path.
     * @return A mock condition.
     */
    private CombatCondition condition(String key) {
        CombatCondition condition = mock(CombatCondition.class);
        when(condition.getKey()).thenReturn(new NamespacedKey("mcrpg", key));
        return condition;
    }

    @Test
    @DisplayName("register then get returns the same condition")
    void register_getReturnsCondition() {
        CombatCondition condition = condition("proximity");
        registry.register(condition);

        assertSame(condition, registry.get(new NamespacedKey("mcrpg", "proximity")).orElse(null));
    }

    @Test
    @DisplayName("get returns empty for an unregistered key")
    void get_returnsEmpty_whenNotRegistered() {
        assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("register throws IllegalStateException on a duplicate key")
    void register_throwsIllegalState_whenDuplicateKey() {
        registry.register(condition("proximity"));

        assertThrows(IllegalStateException.class, () -> registry.register(condition("proximity")));
    }

    @Test
    @DisplayName("unregister removes and returns the condition")
    void unregister_removesAndReturnsCondition() {
        CombatCondition condition = condition("proximity");
        NamespacedKey key = new NamespacedKey("mcrpg", "proximity");
        registry.register(condition);

        assertSame(condition, registry.unregister(key).orElse(null));
        assertFalse(registry.isRegistered(key));
    }

    @Test
    @DisplayName("unregister returns empty when the key was not registered")
    void unregister_returnsEmpty_whenNotRegistered() {
        assertTrue(registry.unregister(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("isRegistered and registered reflect registration state")
    void isRegistered_reflectsRegistration() {
        CombatCondition condition = condition("proximity");
        NamespacedKey key = new NamespacedKey("mcrpg", "proximity");

        assertFalse(registry.isRegistered(key));
        assertFalse(registry.registered(condition));

        registry.register(condition);

        assertTrue(registry.isRegistered(key));
        assertTrue(registry.registered(condition));
    }

    @Test
    @DisplayName("getAll and getRegisteredKeys return all registered conditions")
    void getAll_returnsAllRegistered() {
        CombatCondition proximity = condition("proximity");
        CombatCondition region = condition("region");
        registry.register(proximity);
        registry.register(region);

        assertEquals(2, registry.getAll().size());
        assertTrue(registry.getAll().contains(proximity));
        assertTrue(registry.getAll().contains(region));
        assertEquals(2, registry.getRegisteredKeys().size());
        assertTrue(registry.getRegisteredKeys().contains(new NamespacedKey("mcrpg", "proximity")));
    }
}
