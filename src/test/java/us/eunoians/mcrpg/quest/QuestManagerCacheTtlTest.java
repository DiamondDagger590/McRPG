package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the Tier 2 (Caffeine) cache TTL behaviour of {@link QuestManager}.
 * <p>
 * Uses a {@link ManualTicker} to advance cache time deterministically without
 * wall-clock dependency.
 */
public class QuestManagerCacheTtlTest extends McRPGBaseTest {

    /**
     * A controllable {@link Ticker} implementation that wraps an {@link AtomicLong}
     * counter. Call {@link #advanceNanos} to move time forward.
     */
    private static final class ManualTicker implements Ticker {
        private final AtomicLong nanos = new AtomicLong(0);

        void advanceNanos(long delta) {
            nanos.addAndGet(delta);
        }

        @Override
        public long read() {
            return nanos.get();
        }
    }

    private ManualTicker ticker;
    private QuestManager questManager;
    private AtomicBoolean dbWasQueried;

    @BeforeEach
    public void setup() throws Exception {
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mock(ReloadableContentManager.class));
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(mainConfig.getInt(any(Route.class), anyInt())).thenAnswer(inv -> inv.getArgument(1));
        FileManager fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(any(FileType.class))).thenReturn(mainConfig);
        ticker = new ManualTicker();
        questManager = new QuestManager(mcRPG, ticker);

        dbWasQueried = new AtomicBoolean(false);

        McRPGDatabaseManager mockDbManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDbManager.getDatabase()).thenReturn(mockDatabase);
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(mockExecutor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            dbWasQueried.set(true);
            return null;
        });

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);
    }

    @DisplayName("Given a retired quest, when TTL has not elapsed, then getQuestInstance does not query the DB")
    @Test
    public void retiredQuest_beforeTtlExpiry_isServedFromTier2WithoutDbHit() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("ttl_hit_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, UUID.randomUUID());
        questManager.retireQuest(quest);

        // Advance to just under the 15-minute keep-alive (default)
        long keepAliveNanos = Duration.ofMinutes(15).toNanos();
        ticker.advanceNanos(keepAliveNanos - 1);

        dbWasQueried.set(false);
        questManager.getQuestInstance(quest.getQuestUUID());

        assertFalse(dbWasQueried.get(),
                "Quest should be served from Tier 2 cache without a DB query before TTL expiry");
    }

    @DisplayName("Given a retired quest, when TTL has elapsed and cleanUp runs, then getQuestInstance falls through to DB")
    @Test
    public void retiredQuest_afterTtlExpiry_isEvictedAndCausesTier3DbHit() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("ttl_eviction_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, UUID.randomUUID());
        questManager.retireQuest(quest);

        // Advance past the 15-minute keep-alive and trigger Caffeine's eviction sweep
        long keepAliveNanos = Duration.ofMinutes(15).toNanos();
        ticker.advanceNanos(keepAliveNanos + 1);
        // Caffeine evicts lazily; force the maintenance cycle
        questManager.saveDirtyQuests(); // any cache operation triggers pending evictions

        dbWasQueried.set(false);
        questManager.getQuestInstance(quest.getQuestUUID());

        assertTrue(dbWasQueried.get(),
                "Quest should be evicted from Tier 2 cache after TTL expires, causing a DB query");
    }

    @DisplayName("Given a retired quest not in Tier 1, then isQuestActive returns false immediately after retirement")
    @Test
    public void retiredQuest_isNotActiveInTier1() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("ttl_tier1_test");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, UUID.randomUUID());
        questManager.trackActiveQuest(quest);

        assertTrue(questManager.isQuestActive(quest.getQuestUUID()),
                "Quest should be active in Tier 1 after tracking");

        questManager.retireQuest(quest);

        assertFalse(questManager.isQuestActive(quest.getQuestUUID()),
                "Quest should not be active in Tier 1 after retirement");
    }
}
