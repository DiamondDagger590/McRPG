package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RewardDistributionTypeRegistry")
public class RewardDistributionTypeRegistryTest extends McRPGBaseTest {

    private RewardDistributionTypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new RewardDistributionTypeRegistry();
    }

    private RewardDistributionType stubType(NamespacedKey key) {
        return new RewardDistributionType() {
            @NotNull
            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @NotNull
            @Override
            public Set<UUID> resolve(@NotNull ContributionSnapshot snapshot,
                                     @NotNull DistributionTierConfig tier) {
                return Set.of();
            }

            @NotNull
            @Override
            public Optional<NamespacedKey> getExpansionKey() {
                return Optional.empty();
            }
        };
    }

    @DisplayName("register and get retrieves by key")
    @Test
    void register_thenGet_returnsType() {
        NamespacedKey key = new NamespacedKey("mcrpg", "top_players");
        RewardDistributionType type = stubType(key);

        registry.register(type);

        assertEquals(type, registry.get(key).orElseThrow());
    }

    @DisplayName("get returns empty for unregistered key")
    @Test
    void get_unregisteredKey_returnsEmpty() {
        Optional<RewardDistributionType> result = registry.get(new NamespacedKey("mcrpg", "nonexistent"));
        assertTrue(result.isEmpty());
    }

    @DisplayName("register duplicate key throws IllegalStateException")
    @Test
    void register_duplicateKey_throws() {
        NamespacedKey key = new NamespacedKey("mcrpg", "top_players");
        registry.register(stubType(key));

        assertThrows(IllegalStateException.class, () -> registry.register(stubType(key)));
    }

    @DisplayName("registered returns true for registered type")
    @Test
    void registered_registeredType_returnsTrue() {
        NamespacedKey key = new NamespacedKey("mcrpg", "participated");
        RewardDistributionType type = stubType(key);
        registry.register(type);

        assertTrue(registry.registered(type));
    }

    @DisplayName("registered returns false for unregistered type")
    @Test
    void registered_unregisteredType_returnsFalse() {
        RewardDistributionType type = stubType(new NamespacedKey("mcrpg", "unknown"));
        assertFalse(registry.registered(type));
    }

    @DisplayName("getAll returns all registered types")
    @Test
    void getAll_returnsAllTypes() {
        RewardDistributionType type1 = stubType(new NamespacedKey("mcrpg", "type_a"));
        RewardDistributionType type2 = stubType(new NamespacedKey("mcrpg", "type_b"));
        registry.register(type1);
        registry.register(type2);

        assertEquals(2, registry.getAll().size());
        assertTrue(registry.getAll().contains(type1));
        assertTrue(registry.getAll().contains(type2));
    }

    @DisplayName("getAll on empty registry returns empty collection")
    @Test
    void getAll_emptyRegistry_returnsEmpty() {
        assertTrue(registry.getAll().isEmpty());
    }

    @DisplayName("getRegisteredKeys returns all keys")
    @Test
    void getRegisteredKeys_returnsAllKeys() {
        NamespacedKey key1 = new NamespacedKey("mcrpg", "key_a");
        NamespacedKey key2 = new NamespacedKey("mcrpg", "key_b");
        registry.register(stubType(key1));
        registry.register(stubType(key2));

        Set<NamespacedKey> keys = registry.getRegisteredKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(key1));
        assertTrue(keys.contains(key2));
    }

    @DisplayName("getRegisteredKeys on empty registry returns empty set")
    @Test
    void getRegisteredKeys_emptyRegistry_returnsEmpty() {
        assertTrue(registry.getRegisteredKeys().isEmpty());
    }

    @DisplayName("registered checks by key not identity")
    @Test
    void registered_checksByKey_notIdentity() {
        NamespacedKey key = new NamespacedKey("mcrpg", "shared_key");
        RewardDistributionType type1 = stubType(key);
        RewardDistributionType type2 = stubType(key);
        registry.register(type1);

        assertTrue(registry.registered(type2));
    }
}
