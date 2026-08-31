package us.eunoians.mcrpg.quest.chain.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainStartConditionTypeRegistryTest extends McRPGBaseTest {

    private QuestChainStartConditionTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new QuestChainStartConditionTypeRegistry();
    }

    private static QuestChainStartConditionType stubType(NamespacedKey key) {
        return new QuestChainStartConditionType() {
            @Override
            @NotNull
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            @NotNull
            public QuestChainStartCondition parse(@NotNull Section config) {
                return new QuestChainStartCondition() {
                    @Override
                    @NotNull
                    public NamespacedKey getKey() {
                        return key;
                    }

                    @Override
                    public boolean evaluate(@NotNull Player player, @NotNull Instant now) {
                        return true;
                    }
                };
            }

            @Override
            @NotNull
            public Optional<NamespacedKey> getExpansionKey() {
                return Optional.empty();
            }
        };
    }

    @DisplayName("get returns empty for an unregistered key")
    @Test
    void get_returnsEmpty_whenKeyNotRegistered() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");
        assertTrue(registry.get(key).isEmpty());
    }

    @DisplayName("register adds a type that can be retrieved by key")
    @Test
    void register_addsType_retrievableByKey() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_type");
        QuestChainStartConditionType type = stubType(key);

        registry.register(type);

        assertTrue(registry.get(key).isPresent());
        assertSame(type, registry.get(key).get());
    }

    @DisplayName("registered returns true for a registered type")
    @Test
    void registered_returnsTrue_whenTypeRegistered() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "reg_type");
        QuestChainStartConditionType type = stubType(key);

        registry.register(type);

        assertTrue(registry.registered(type));
    }

    @DisplayName("registered returns false for a type not registered")
    @Test
    void registered_returnsFalse_whenTypeNotRegistered() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "not_reg");
        QuestChainStartConditionType type = stubType(key);

        assertFalse(registry.registered(type));
    }

    @DisplayName("register silently replaces a type with the same key")
    @Test
    void register_replacesExisting_whenSameKey() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "replaceable");
        QuestChainStartConditionType first = stubType(key);
        QuestChainStartConditionType second = stubType(key);

        registry.register(first);
        registry.register(second);

        assertSame(second, registry.get(key).get());
    }

    @DisplayName("getAll returns empty collection when nothing is registered")
    @Test
    void getAll_returnsEmpty_whenNothingRegistered() {
        Collection<QuestChainStartConditionType> all = registry.getAll();
        assertTrue(all.isEmpty());
    }

    @DisplayName("getAll returns all registered types")
    @Test
    void getAll_returnsAllRegisteredTypes() {
        NamespacedKey keyA = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "type_a");
        NamespacedKey keyB = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "type_b");
        QuestChainStartConditionType typeA = stubType(keyA);
        QuestChainStartConditionType typeB = stubType(keyB);

        registry.register(typeA);
        registry.register(typeB);

        Collection<QuestChainStartConditionType> all = registry.getAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(typeA));
        assertTrue(all.contains(typeB));
    }

    @DisplayName("getAll returns unmodifiable collection")
    @Test
    void getAll_returnsUnmodifiableCollection() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unmod");
        registry.register(stubType(key));

        Collection<QuestChainStartConditionType> all = registry.getAll();
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> all.clear());
    }

    @DisplayName("registered checks by key, not reference identity")
    @Test
    void registered_checksByKey_notIdentity() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "identity_check");
        QuestChainStartConditionType original = stubType(key);
        QuestChainStartConditionType sameKey = stubType(key);

        registry.register(original);

        assertTrue(registry.registered(sameKey),
                "registered should match by key, not by object identity");
    }
}
