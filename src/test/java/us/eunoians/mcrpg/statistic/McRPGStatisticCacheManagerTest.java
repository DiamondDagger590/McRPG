package us.eunoians.mcrpg.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.StatisticType;
import com.diamonddagger590.mccore.statistic.cache.StatisticCache;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("McRPGStatisticCacheManager")
class McRPGStatisticCacheManagerTest extends McRPGBaseTest {

    private McRPGStatisticCacheManager cacheManager;
    private Connection mockConnection;
    private UUID playerUUID;
    private NamespacedKey statKey;

    @BeforeEach
    void setUp() {
        cacheManager = new McRPGStatisticCacheManager(mcRPG, 100, 300);
        playerUUID = UUID.randomUUID();
        statKey = new NamespacedKey("mcrpg", "test_stat");
        mockConnection = mock(Connection.class);
    }

    @Nested
    @DisplayName("getCache")
    class GetCache {

        @Test
        @DisplayName("returns the underlying StatisticCache instance")
        void getCache_returnsCache() {
            StatisticCache cache = cacheManager.getCache();
            assertNotNull(cache);
        }
    }

    @Nested
    @DisplayName("getOfflineStatistic")
    class GetOfflineStatistic {

        @Test
        @DisplayName("Given a cached entry, when getting offline statistic, then returns cached value without DB call")
        void getOfflineStatistic_returnsCachedEntry_whenCacheHit() {
            StatisticEntry entry = new StatisticEntry(statKey, StatisticType.INT, 42);
            cacheManager.getCache().put(playerUUID, statKey, entry);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                Optional<StatisticEntry> result = cacheManager.getOfflineStatistic(mockConnection, playerUUID, statKey);

                assertTrue(result.isPresent());
                assertSame(entry, result.get());
                daoMock.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("Given no cached entry and a DB hit, when getting offline statistic, then returns DB value and populates cache")
        void getOfflineStatistic_returnsDbEntry_whenCacheMissAndDbHit() {
            StatisticEntry dbEntry = new StatisticEntry(statKey, StatisticType.DOUBLE, 3.14);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, statKey))
                        .thenReturn(Optional.of(dbEntry));

                Optional<StatisticEntry> result = cacheManager.getOfflineStatistic(mockConnection, playerUUID, statKey);

                assertTrue(result.isPresent());
                assertSame(dbEntry, result.get());

                Optional<StatisticEntry> cachedAfter = cacheManager.getCache().get(playerUUID, statKey);
                assertTrue(cachedAfter.isPresent());
                assertSame(dbEntry, cachedAfter.get());
            }
        }

        @Test
        @DisplayName("Given no cached entry and no DB row, when getting offline statistic, then returns empty and does not populate cache")
        void getOfflineStatistic_returnsEmpty_whenCacheMissAndDbMiss() {
            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, statKey))
                        .thenReturn(Optional.empty());

                Optional<StatisticEntry> result = cacheManager.getOfflineStatistic(mockConnection, playerUUID, statKey);

                assertTrue(result.isEmpty());

                Optional<StatisticEntry> cachedAfter = cacheManager.getCache().get(playerUUID, statKey);
                assertTrue(cachedAfter.isEmpty());
            }
        }

        @Test
        @DisplayName("Given a cached entry for one key and DB query for another, when getting second key, then only queries DB for the second key")
        void getOfflineStatistic_queriesDbForUncachedKey_whenDifferentKeysCached() {
            NamespacedKey cachedKey = new NamespacedKey("mcrpg", "cached_stat");
            NamespacedKey uncachedKey = new NamespacedKey("mcrpg", "uncached_stat");
            StatisticEntry cachedEntry = new StatisticEntry(cachedKey, StatisticType.LONG, 100L);
            StatisticEntry dbEntry = new StatisticEntry(uncachedKey, StatisticType.INT, 5);

            cacheManager.getCache().put(playerUUID, cachedKey, cachedEntry);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, uncachedKey))
                        .thenReturn(Optional.of(dbEntry));

                Optional<StatisticEntry> resultCached = cacheManager.getOfflineStatistic(mockConnection, playerUUID, cachedKey);
                Optional<StatisticEntry> resultUncached = cacheManager.getOfflineStatistic(mockConnection, playerUUID, uncachedKey);

                assertTrue(resultCached.isPresent());
                assertSame(cachedEntry, resultCached.get());
                assertTrue(resultUncached.isPresent());
                assertSame(dbEntry, resultUncached.get());

                daoMock.verify(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, cachedKey), never());
                daoMock.verify(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, uncachedKey));
            }
        }

        @Test
        @DisplayName("Given a second call with same key after DB population, when getting offline statistic, then returns from cache without second DB call")
        void getOfflineStatistic_returnsCachedOnSecondCall_whenFirstCallPopulatedCache() {
            StatisticEntry dbEntry = new StatisticEntry(statKey, StatisticType.INT, 99);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, statKey))
                        .thenReturn(Optional.of(dbEntry));

                cacheManager.getOfflineStatistic(mockConnection, playerUUID, statKey);
                Optional<StatisticEntry> secondCall = cacheManager.getOfflineStatistic(mockConnection, playerUUID, statKey);

                assertTrue(secondCall.isPresent());
                assertEquals(99, secondCall.get().value());

                daoMock.verify(() -> PlayerStatisticDAO.getPlayerStatistic(mockConnection, playerUUID, statKey));
            }
        }

        @Test
        @DisplayName("Given different players with same stat key, when getting offline statistic, then returns correct values per player")
        void getOfflineStatistic_returnsSeparateEntriesPerPlayer() {
            UUID player1 = UUID.randomUUID();
            UUID player2 = UUID.randomUUID();
            StatisticEntry entry1 = new StatisticEntry(statKey, StatisticType.INT, 10);
            StatisticEntry entry2 = new StatisticEntry(statKey, StatisticType.INT, 20);

            cacheManager.getCache().put(player1, statKey, entry1);
            cacheManager.getCache().put(player2, statKey, entry2);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                Optional<StatisticEntry> result1 = cacheManager.getOfflineStatistic(mockConnection, player1, statKey);
                Optional<StatisticEntry> result2 = cacheManager.getOfflineStatistic(mockConnection, player2, statKey);

                assertTrue(result1.isPresent());
                assertEquals(10, result1.get().value());
                assertTrue(result2.isPresent());
                assertEquals(20, result2.get().value());

                daoMock.verifyNoInteractions();
            }
        }
    }

    @Nested
    @DisplayName("populateAsync")
    class PopulateAsync {

        @Test
        @DisplayName("Given a DB hit, when populating async, then cache is populated after the submitted task runs")
        void populateAsync_populatesCache_whenDbHit() throws SQLException {
            StatisticEntry dbEntry = new StatisticEntry(statKey, StatisticType.INT, 55);

            McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
            when(mockDatabase.getConnection()).thenReturn(mockConnection);
            ThreadPoolExecutor directExecutor = mock(ThreadPoolExecutor.class);
            when(mockDatabase.getDatabaseExecutorService()).thenReturn(directExecutor);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(any(Connection.class), eq(playerUUID), eq(statKey)))
                        .thenReturn(Optional.of(dbEntry));

                cacheManager.populateAsync(playerUUID, statKey);

                var runnableCaptor = org.mockito.ArgumentCaptor.forClass(Runnable.class);
                verify(directExecutor).submit(runnableCaptor.capture());
                runnableCaptor.getValue().run();

                Optional<StatisticEntry> cached = cacheManager.getCache().get(playerUUID, statKey);
                assertTrue(cached.isPresent());
                assertEquals(55, cached.get().value());
            }
        }

        @Test
        @DisplayName("Given a DB miss, when populating async, then cache remains empty after the submitted task runs")
        void populateAsync_doesNotPopulateCache_whenDbMiss() throws SQLException {
            McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
            when(mockDatabase.getConnection()).thenReturn(mockConnection);
            ThreadPoolExecutor directExecutor = mock(ThreadPoolExecutor.class);
            when(mockDatabase.getDatabaseExecutorService()).thenReturn(directExecutor);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(any(Connection.class), eq(playerUUID), eq(statKey)))
                        .thenReturn(Optional.empty());

                cacheManager.populateAsync(playerUUID, statKey);

                var runnableCaptor = org.mockito.ArgumentCaptor.forClass(Runnable.class);
                verify(directExecutor).submit(runnableCaptor.capture());
                runnableCaptor.getValue().run();

                Optional<StatisticEntry> cached = cacheManager.getCache().get(playerUUID, statKey);
                assertTrue(cached.isEmpty());
            }
        }

        @Test
        @DisplayName("Given a SQLException on connection close, when populating async, then exception is caught and cache remains empty")
        void populateAsync_catchesException_whenSqlExceptionOnClose() throws SQLException {
            Connection closingConnection = mock(Connection.class);
            org.mockito.Mockito.doThrow(new SQLException("close failed")).when(closingConnection).close();

            McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
            when(mockDatabase.getConnection()).thenReturn(closingConnection);
            ThreadPoolExecutor directExecutor = mock(ThreadPoolExecutor.class);
            when(mockDatabase.getDatabaseExecutorService()).thenReturn(directExecutor);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

            try (MockedStatic<PlayerStatisticDAO> daoMock = mockStatic(PlayerStatisticDAO.class)) {
                daoMock.when(() -> PlayerStatisticDAO.getPlayerStatistic(any(Connection.class), eq(playerUUID), eq(statKey)))
                        .thenReturn(Optional.empty());

                cacheManager.populateAsync(playerUUID, statKey);

                var runnableCaptor = org.mockito.ArgumentCaptor.forClass(Runnable.class);
                verify(directExecutor).submit(runnableCaptor.capture());
                runnableCaptor.getValue().run();

                Optional<StatisticEntry> cached = cacheManager.getCache().get(playerUUID, statKey);
                assertTrue(cached.isEmpty());
            }
        }
    }
}
