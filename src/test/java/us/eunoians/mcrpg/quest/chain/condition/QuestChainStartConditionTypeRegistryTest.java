package us.eunoians.mcrpg.quest.chain.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("QuestChainStartConditionTypeRegistry")
class QuestChainStartConditionTypeRegistryTest extends McRPGBaseTest {

    private QuestChainStartConditionTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestChainStartConditionTypeRegistry();
    }

    private QuestChainStartConditionType mockType(String namespace, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        QuestChainStartConditionType type = mock(QuestChainStartConditionType.class);
        when(type.getKey()).thenReturn(namespacedKey);
        return type;
    }

    @Test
    @DisplayName("get returns empty for an unregistered key")
    void get_returnsEmpty_whenKeyNotRegistered() {
        NamespacedKey key = new NamespacedKey("mcrpg", "nonexistent");

        Optional<QuestChainStartConditionType> result = registry.get(key);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("register then get returns the registered type")
    void register_thenGet_returnsRegisteredType() {
        QuestChainStartConditionType type = mockType("mcrpg", "time_gate");

        registry.register(type);
        Optional<QuestChainStartConditionType> result = registry.get(type.getKey());

        assertTrue(result.isPresent());
        assertEquals(type, result.get());
    }

    @Test
    @DisplayName("registered returns true for a registered type")
    void registered_returnsTrue_whenTypeRegistered() {
        QuestChainStartConditionType type = mockType("mcrpg", "time_gate");

        registry.register(type);

        assertTrue(registry.registered(type));
    }

    @Test
    @DisplayName("registered returns false for an unregistered type")
    void registered_returnsFalse_whenTypeNotRegistered() {
        QuestChainStartConditionType type = mockType("mcrpg", "time_gate");

        assertFalse(registry.registered(type));
    }

    @Test
    @DisplayName("register silently replaces an existing type with the same key")
    void register_replacesExisting_whenSameKey() {
        QuestChainStartConditionType first = mockType("mcrpg", "time_gate");
        QuestChainStartConditionType second = mockType("mcrpg", "time_gate");

        registry.register(first);
        registry.register(second);

        Optional<QuestChainStartConditionType> result = registry.get(first.getKey());
        assertTrue(result.isPresent());
        assertEquals(second, result.get());
    }

    @Test
    @DisplayName("getAll returns all registered types")
    void getAll_returnsAllRegisteredTypes() {
        QuestChainStartConditionType typeA = mockType("mcrpg", "time_gate");
        QuestChainStartConditionType typeB = mockType("custom", "weather_gate");

        registry.register(typeA);
        registry.register(typeB);

        Collection<QuestChainStartConditionType> all = registry.getAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(typeA));
        assertTrue(all.contains(typeB));
    }

    @Test
    @DisplayName("getAll returns empty collection when nothing is registered")
    void getAll_returnsEmpty_whenNothingRegistered() {
        Collection<QuestChainStartConditionType> all = registry.getAll();

        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("types with different keys are independently registered")
    void register_differentKeys_areIndependent() {
        QuestChainStartConditionType typeA = mockType("mcrpg", "time_gate");
        QuestChainStartConditionType typeB = mockType("mcrpg", "permission_gate");

        registry.register(typeA);
        registry.register(typeB);

        assertTrue(registry.get(typeA.getKey()).isPresent());
        assertTrue(registry.get(typeB.getKey()).isPresent());
        assertEquals(typeA, registry.get(typeA.getKey()).get());
        assertEquals(typeB, registry.get(typeB.getKey()).get());
    }
}
