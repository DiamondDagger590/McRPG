package us.eunoians.mcrpg.quest.board.scope;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScopedBoardAdapterRegistryTest extends McRPGBaseTest {

    private ScopedBoardAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ScopedBoardAdapterRegistry();
    }

    @DisplayName("register and get returns the registered adapter")
    @Test
    void register_and_get_returnsAdapter() {
        ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope");
        registry.register(adapter);

        Optional<ScopedBoardAdapter> result = registry.get(new NamespacedKey("mcrpg", "land_scope"));

        assertTrue(result.isPresent());
        assertSame(adapter, result.get());
    }

    @DisplayName("get with unregistered key returns empty")
    @Test
    void get_unregistered_returnsEmpty() {
        Optional<ScopedBoardAdapter> result = registry.get(new NamespacedKey("mcrpg", "nonexistent"));

        assertTrue(result.isEmpty());
    }

    @DisplayName("getAll returns all registered adapters")
    @Test
    void getAll_returnsAllRegistered() {
        ScopedBoardAdapter a1 = stubAdapter("mcrpg", "land_scope");
        ScopedBoardAdapter a2 = stubAdapter("mcrpg", "faction_scope");
        registry.register(a1);
        registry.register(a2);

        Collection<ScopedBoardAdapter> all = registry.getAll();

        assertEquals(2, all.size());
        assertTrue(all.contains(a1));
        assertTrue(all.contains(a2));
    }

    @DisplayName("getAll on empty registry returns empty collection")
    @Test
    void getAll_empty_returnsEmptyCollection() {
        Collection<ScopedBoardAdapter> all = registry.getAll();

        assertTrue(all.isEmpty());
    }

    @DisplayName("registered returns true for registered adapter")
    @Test
    void registered_registeredAdapter_returnsTrue() {
        ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope");
        registry.register(adapter);

        assertTrue(registry.registered(adapter));
    }

    @DisplayName("registered returns false for unregistered adapter")
    @Test
    void registered_unregisteredAdapter_returnsFalse() {
        ScopedBoardAdapter adapter = stubAdapter("mcrpg", "land_scope");

        assertFalse(registry.registered(adapter));
    }

    @DisplayName("registering duplicate key replaces existing adapter")
    @Test
    void register_duplicateKey_replacesExisting() {
        ScopedBoardAdapter first = stubAdapter("mcrpg", "land_scope");
        ScopedBoardAdapter second = stubAdapter("mcrpg", "land_scope");
        registry.register(first);
        registry.register(second);

        Optional<ScopedBoardAdapter> result = registry.get(new NamespacedKey("mcrpg", "land_scope"));

        assertTrue(result.isPresent());
        assertSame(second, result.get());
        assertEquals(1, registry.getAll().size());
    }

    private static ScopedBoardAdapter stubAdapter(String namespace, String key) {
        NamespacedKey scopeKey = new NamespacedKey(namespace, key);
        return new ScopedBoardAdapter() {
            @Override
            public NamespacedKey getScopeProviderKey() { return scopeKey; }
            @Override
            public Set<String> getAllActiveEntities() { return Set.of(); }
            @Override
            public Set<String> getMemberEntities(UUID playerUUID) { return Set.of(); }
            @Override
            public Set<String> getManageableEntities(UUID playerUUID) { return Set.of(); }
            @Override
            public boolean canManageQuests(UUID playerUUID, String entityId) { return false; }
            @Override
            public Optional<String> getEntityDisplayName(String entityId) { return Optional.empty(); }
            @Override
            public Optional<NamespacedKey> getExpansionKey() { return Optional.empty(); }
        };
    }
}
