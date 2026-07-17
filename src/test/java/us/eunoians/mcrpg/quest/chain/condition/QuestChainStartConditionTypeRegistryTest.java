package us.eunoians.mcrpg.quest.chain.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.condition.builtin.TimeGateChainConditionType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestChainStartConditionTypeRegistryTest extends McRPGBaseTest {

    private QuestChainStartConditionTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestChainStartConditionTypeRegistry();
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("registers a condition type")
        void register_registersConditionType() {
            QuestChainStartConditionType type = mockType("test_condition");
            registry.register(type);
            assertTrue(registry.registered(type));
        }

        @Test
        @DisplayName("replaces existing type with same key")
        void register_replacesExistingType() {
            NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "dupe_condition");
            QuestChainStartConditionType original = mockType(key);
            QuestChainStartConditionType replacement = mockType(key);

            registry.register(original);
            registry.register(replacement);

            Optional<QuestChainStartConditionType> result = registry.get(key);
            assertTrue(result.isPresent());
            assertSame(replacement, result.get());
        }
    }

    @Nested
    @DisplayName("registered")
    class RegisteredTests {

        @Test
        @DisplayName("returns false for unregistered type")
        void registered_returnsFalse_whenNotRegistered() {
            QuestChainStartConditionType type = mockType("unregistered");
            assertFalse(registry.registered(type));
        }

        @Test
        @DisplayName("returns true for registered type")
        void registered_returnsTrue_whenRegistered() {
            QuestChainStartConditionType type = mockType("registered");
            registry.register(type);
            assertTrue(registry.registered(type));
        }
    }

    @Nested
    @DisplayName("get")
    class GetTests {

        @Test
        @DisplayName("returns empty for unknown key")
        void get_returnsEmpty_whenKeyUnknown() {
            NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");
            assertTrue(registry.get(key).isEmpty());
        }

        @Test
        @DisplayName("returns the registered type for known key")
        void get_returnsType_whenKeyKnown() {
            QuestChainStartConditionType type = mockType("known");
            registry.register(type);

            Optional<QuestChainStartConditionType> result = registry.get(type.getKey());
            assertTrue(result.isPresent());
            assertSame(type, result.get());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAllTests {

        @Test
        @DisplayName("returns empty collection when registry is empty")
        void getAll_returnsEmpty_whenRegistryEmpty() {
            Collection<QuestChainStartConditionType> all = registry.getAll();
            assertTrue(all.isEmpty());
        }

        @Test
        @DisplayName("returns all registered types")
        void getAll_returnsAllRegisteredTypes() {
            QuestChainStartConditionType type1 = mockType("type_a");
            QuestChainStartConditionType type2 = mockType("type_b");

            registry.register(type1);
            registry.register(type2);

            Collection<QuestChainStartConditionType> all = registry.getAll();
            assertEquals(2, all.size());
            assertTrue(all.contains(type1));
            assertTrue(all.contains(type2));
        }

        @Test
        @DisplayName("integrates with built-in TimeGateChainConditionType")
        void getAll_integratesWithBuiltInType() {
            TimeGateChainConditionType builtIn = new TimeGateChainConditionType();
            registry.register(builtIn);

            Optional<QuestChainStartConditionType> result = registry.get(TimeGateChainConditionType.KEY);
            assertTrue(result.isPresent());
            assertSame(builtIn, result.get());
        }
    }

    private QuestChainStartConditionType mockType(@NotNull String keyValue) {
        return mockType(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), keyValue));
    }

    private QuestChainStartConditionType mockType(@NotNull NamespacedKey key) {
        QuestChainStartConditionType type = mock(QuestChainStartConditionType.class);
        when(type.getKey()).thenReturn(key);
        return type;
    }
}
