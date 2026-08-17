package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.state.CombatStateCodec;
import us.eunoians.mcrpg.combat.state.CombatStateType;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@DisplayName("PersistentCombatStateStore")
class PersistentCombatStateStoreTest extends McRPGBaseTest {

    private static final long TIMEOUT_MILLIS = 8000L;
    private static final int MAX_MOB_PARTICIPANTS = 3;

    private PersistentCombatStateStore store;
    private CombatStateCodec codec;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(1000L));
        codec = new CombatStateCodec(McRPG.getInstance().getLogger());
        store = new PersistentCombatStateStore(McRPG.getInstance(), codec);
    }

    @Nested
    @DisplayName("cache")
    class Cache {

        @DisplayName("caches state for a new entity")
        @Test
        void cache_cacheStateForNewEntity() {
            UUID entityUUID = UUID.randomUUID();
            Map<String, String> state = Map.of("mcrpg:test_key", "42");

            store.cache(entityUUID, state);

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            CombatStateType<Integer> stateType = CombatStateType.persistent(
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_key"),
                    Integer.class, 0, String::valueOf, Integer::parseInt, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            store.applyCachedState(session);

            assertEquals(42, session.getRawStateMap().get(stateType.getKey()));
        }

        @DisplayName("existing values win over new values (putIfAbsent semantics)")
        @Test
        void cache_existingValuesWin() {
            UUID entityUUID = UUID.randomUUID();
            store.cache(entityUUID, Map.of("mcrpg:test_key", "first"));
            store.cache(entityUUID, Map.of("mcrpg:test_key", "second"));

            CombatStateType<String> stateType = CombatStateType.persistent(
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_key"),
                    String.class, "", s -> s, s -> s, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertEquals("first", session.getRawStateMap().get(stateType.getKey()));
        }

        @DisplayName("merges new keys into existing entity entry")
        @Test
        void cache_mergesNewKeys() {
            UUID entityUUID = UUID.randomUUID();
            store.cache(entityUUID, Map.of("mcrpg:key_a", "alpha"));
            store.cache(entityUUID, Map.of("mcrpg:key_b", "beta"));

            CombatStateType<String> typeA = CombatStateType.persistent(
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "key_a"),
                    String.class, "", s -> s, s -> s, null);
            CombatStateType<String> typeB = CombatStateType.persistent(
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "key_b"),
                    String.class, "", s -> s, s -> s, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(typeA);
            registry.register(typeB);

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertEquals("alpha", session.getRawStateMap().get(typeA.getKey()));
            assertEquals("beta", session.getRawStateMap().get(typeB.getKey()));
        }
    }

    @Nested
    @DisplayName("clearCache")
    class ClearCache {

        @DisplayName("removes entity's cached state")
        @Test
        void clearCache_removesEntityState() {
            UUID entityUUID = UUID.randomUUID();
            store.cache(entityUUID, Map.of("mcrpg:test_key", "value"));

            store.clearCache(entityUUID);

            CombatStateType<String> stateType = CombatStateType.persistent(
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_key"),
                    String.class, "default", s -> s, s -> s, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertFalse(session.getRawStateMap().containsKey(stateType.getKey()));
        }

        @DisplayName("does not throw when clearing non-existent entity")
        @Test
        void clearCache_noThrowOnMissingEntity() {
            assertDoesNotThrow(() -> store.clearCache(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("clearCacheWhenWritesSettle")
    class ClearCacheWhenWritesSettle {

        @DisplayName("clears immediately when no pending writes exist")
        @Test
        void clearCacheWhenWritesSettle_clearsImmediatelyWhenNoPendingWrites() {
            UUID entityUUID = UUID.randomUUID();
            store.cache(entityUUID, Map.of("mcrpg:test_key", "value"));

            store.clearCacheWhenWritesSettle(entityUUID);

            CombatStateType<String> stateType = CombatStateType.persistent(
                    new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_key"),
                    String.class, "default", s -> s, s -> s, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertFalse(session.getRawStateMap().containsKey(stateType.getKey()));
        }
    }

    @Nested
    @DisplayName("awaitPendingWrites")
    class AwaitPendingWrites {

        @DisplayName("returns immediately when no pending writes exist")
        @Test
        void awaitPendingWrites_returnsImmediatelyWhenEmpty() {
            assertDoesNotThrow(() -> store.awaitPendingWrites());
        }
    }

    @Nested
    @DisplayName("applyCachedState")
    class ApplyCachedState {

        @DisplayName("applies cached persistent state into a new session")
        @Test
        void applyCachedState_appliesCachedState() {
            UUID entityUUID = UUID.randomUUID();
            NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "cached_int");
            CombatStateType<Integer> stateType = CombatStateType.persistent(
                    key, Integer.class, 0, String::valueOf, Integer::parseInt, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            store.cache(entityUUID, Map.of(key.toString(), "99"));

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertEquals(99, session.getRawStateMap().get(key));
        }

        @DisplayName("skips entries with invalid NamespacedKey format")
        @Test
        void applyCachedState_skipsInvalidKeys() {
            UUID entityUUID = UUID.randomUUID();
            Map<String, String> cached = new HashMap<>();
            cached.put("not a valid key!", "value");

            store.cache(entityUUID, cached);

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertTrue(session.getRawStateMap().isEmpty());
        }

        @DisplayName("skips entries whose key is not registered in CombatStateTypeRegistry")
        @Test
        void applyCachedState_skipsUnregisteredKeys() {
            UUID entityUUID = UUID.randomUUID();
            NamespacedKey unregisteredKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unregistered_state");

            store.cache(entityUUID, Map.of(unregisteredKey.toString(), "some_value"));

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertFalse(session.getRawStateMap().containsKey(unregisteredKey));
        }

        @DisplayName("no-ops when entity has no cached state")
        @Test
        void applyCachedState_noopsWhenNoCachedState() {
            UUID entityUUID = UUID.randomUUID();
            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);

            assertDoesNotThrow(() -> store.applyCachedState(session));
            assertTrue(session.getRawStateMap().isEmpty());
        }

        @DisplayName("no-ops when cached map is empty")
        @Test
        void applyCachedState_noopsWhenCachedMapEmpty() {
            UUID entityUUID = UUID.randomUUID();
            store.cache(entityUUID, Map.of());

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);

            assertDoesNotThrow(() -> store.applyCachedState(session));
            assertTrue(session.getRawStateMap().isEmpty());
        }

        @DisplayName("skips entry when codec decode fails")
        @Test
        void applyCachedState_skipsWhenDecodeFails() {
            UUID entityUUID = UUID.randomUUID();
            NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "bad_decode");
            CombatStateType<Integer> stateType = CombatStateType.persistent(
                    key, Integer.class, 0, String::valueOf, s -> {
                        throw new NumberFormatException("bad");
                    }, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            store.cache(entityUUID, Map.of(key.toString(), "not_a_number"));

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertFalse(session.getRawStateMap().containsKey(key));
        }

        @DisplayName("applies multiple state types from cache")
        @Test
        void applyCachedState_appliesMultipleStateTypes() {
            UUID entityUUID = UUID.randomUUID();
            NamespacedKey keyA = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "state_a");
            NamespacedKey keyB = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "state_b");

            CombatStateType<String> typeA = CombatStateType.persistent(
                    keyA, String.class, "", s -> s, s -> s, null);
            CombatStateType<Integer> typeB = CombatStateType.persistent(
                    keyB, Integer.class, 0, String::valueOf, Integer::parseInt, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(typeA);
            registry.register(typeB);

            store.cache(entityUUID, Map.of(keyA.toString(), "hello", keyB.toString(), "7"));

            CombatSession session = new CombatSession(entityUUID, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            store.applyCachedState(session);

            assertEquals("hello", session.getRawStateMap().get(keyA));
            assertEquals(7, session.getRawStateMap().get(keyB));
        }
    }

    @Nested
    @DisplayName("warnUnregisteredStateKey")
    class WarnUnregisteredStateKey {

        @DisplayName("logs only once per state key across multiple sessions")
        @Test
        void warnUnregisteredStateKey_logsOnlyOncePerKey() {
            NamespacedKey persistentKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "persistent_warn_test");
            CombatStateType<Integer> stateType = CombatStateType.persistent(
                    persistentKey, Integer.class, 0, String::valueOf, Integer::parseInt, null);

            CombatStateTypeRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.COMBAT_STATE_TYPE);
            registry.register(stateType);

            NamespacedKey unregisteredKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unregistered_key");

            UUID entity1 = UUID.randomUUID();
            UUID entity2 = UUID.randomUUID();

            CombatSession session1 = new CombatSession(entity1, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            session1.setRawState(unregisteredKey, "some_value");

            CombatSession session2 = new CombatSession(entity2, MAX_MOB_PARTICIPANTS, TIMEOUT_MILLIS);
            session2.setRawState(unregisteredKey, "another_value");

            assertDoesNotThrow(() -> store.saveAsync(session1));
            assertDoesNotThrow(() -> store.saveAsync(session2));
        }
    }
}
