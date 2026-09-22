package us.eunoians.mcrpg.quest.chain.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class QuestChainStartConditionTypeRegistryTest {

    private QuestChainStartConditionTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestChainStartConditionTypeRegistry();
    }

    private static QuestChainStartConditionType stubType(NamespacedKey key) {
        return new QuestChainStartConditionType() {
            @Override
            public @NotNull NamespacedKey getKey() {
                return key;
            }

            @Override
            public @NotNull QuestChainStartCondition parse(@NotNull Section config) {
                return mock(QuestChainStartCondition.class);
            }

            @Override
            public @NotNull Optional<NamespacedKey> getExpansionKey() {
                return Optional.empty();
            }
        };
    }

    @Test
    @DisplayName("Registering a type makes it retrievable by key")
    void register_makesRetrievableByKey() {
        NamespacedKey key = new NamespacedKey("mcrpg", "time_gate");
        QuestChainStartConditionType type = stubType(key);

        registry.register(type);

        Optional<QuestChainStartConditionType> result = registry.get(key);
        assertTrue(result.isPresent());
        assertSame(type, result.get());
    }

    @Test
    @DisplayName("registered() returns true for a registered type")
    void registered_returnsTrue_whenRegistered() {
        NamespacedKey key = new NamespacedKey("mcrpg", "permission_gate");
        QuestChainStartConditionType type = stubType(key);

        registry.register(type);

        assertTrue(registry.registered(type));
    }

    @Test
    @DisplayName("registered() returns false for an unregistered type")
    void registered_returnsFalse_whenNotRegistered() {
        NamespacedKey key = new NamespacedKey("mcrpg", "unknown");
        QuestChainStartConditionType type = stubType(key);

        assertFalse(registry.registered(type));
    }

    @Test
    @DisplayName("get() returns empty Optional for an unregistered key")
    void get_returnsEmpty_whenNotRegistered() {
        NamespacedKey key = new NamespacedKey("mcrpg", "nonexistent");

        Optional<QuestChainStartConditionType> result = registry.get(key);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAll() returns all registered types")
    void getAll_returnsAllRegistered() {
        QuestChainStartConditionType type1 = stubType(new NamespacedKey("mcrpg", "type_a"));
        QuestChainStartConditionType type2 = stubType(new NamespacedKey("mcrpg", "type_b"));

        registry.register(type1);
        registry.register(type2);

        Collection<QuestChainStartConditionType> all = registry.getAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(type1));
        assertTrue(all.contains(type2));
    }

    @Test
    @DisplayName("getAll() returns empty collection when nothing is registered")
    void getAll_returnsEmpty_whenNothingRegistered() {
        Collection<QuestChainStartConditionType> all = registry.getAll();

        assertTrue(all.isEmpty());
    }

    @Test
    @DisplayName("Registering a duplicate key silently replaces the previous entry")
    void register_duplicateKey_replacesExisting() {
        NamespacedKey key = new NamespacedKey("mcrpg", "duplicate");
        QuestChainStartConditionType original = stubType(key);
        QuestChainStartConditionType replacement = stubType(key);

        registry.register(original);
        registry.register(replacement);

        Optional<QuestChainStartConditionType> result = registry.get(key);
        assertTrue(result.isPresent());
        assertSame(replacement, result.get());
        assertEquals(1, registry.getAll().size());
    }

    @Test
    @DisplayName("getAll() returns an unmodifiable collection")
    void getAll_returnsUnmodifiableCollection() {
        QuestChainStartConditionType type = stubType(new NamespacedKey("mcrpg", "test"));
        registry.register(type);

        Collection<QuestChainStartConditionType> all = registry.getAll();

        assertThrows(UnsupportedOperationException.class, () -> all.add(stubType(new NamespacedKey("mcrpg", "hack"))));
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, Runnable runnable) {
        org.junit.jupiter.api.Assertions.assertThrows(expectedType, runnable::run);
    }
}
