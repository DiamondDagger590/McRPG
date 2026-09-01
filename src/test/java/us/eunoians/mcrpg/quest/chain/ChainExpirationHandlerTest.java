package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.event.quest.chain.QuestChainCompleteEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainFailEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainRestartEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainStepAdvanceEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainStepRetryEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChainExpirationHandler}. Validates the four expiration behaviors
 * (fail, retry, restart-chain, skip) and the retry counter management logic.
 */
class ChainExpirationHandlerTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("mcrpg", "chain");
    private static final NamespacedKey TRIGGER_KEY = new NamespacedKey("mcrpg", "login");
    private static final NamespacedKey QUEST_A = new NamespacedKey("mcrpg", "quest_a");
    private static final NamespacedKey QUEST_B = new NamespacedKey("mcrpg", "quest_b");
    private static final NamespacedKey QUEST_C = new NamespacedKey("mcrpg", "quest_c");

    private ChainExpirationHandler handler;
    private ChainPersistenceService mockPersistence;
    private ChainQuestStarter mockStarter;
    private Map<ChainExpirationHandler.RetryKey, Integer> retryCounters;

    private QuestChainDefinition threeStepChain;

    @BeforeEach
    void setUp() {
        mockPersistence = mock(ChainPersistenceService.class);
        mockStarter = mock(ChainQuestStarter.class);
        retryCounters = new ConcurrentHashMap<>();
        handler = new ChainExpirationHandler(mcRPG, mockPersistence, mockStarter, retryCounters);

        McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(mockExecutor);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

        QuestChainStep stepA = new QuestChainStep(QUEST_A, List.of(), "fail-chain", 3, null);
        QuestChainStep stepB = new QuestChainStep(QUEST_B, List.of(), "retry", 2, null);
        QuestChainStep stepC = QuestChainStep.simple(QUEST_C);

        threeStepChain = new QuestChainDefinition.Builder(CHAIN_KEY, SOURCE_KEY, TRIGGER_KEY, List.of(stepA, stepB, stepC))
                .build();
    }

    @Nested
    @DisplayName("handleExpireFail")
    class HandleExpireFail {

        @Test
        @DisplayName("Given active chain, when fail is called, then state is FAILED and persistence is triggered")
        void handleExpireFail_setsStateToFailed() {
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            UUID playerUUID = UUID.randomUUID();

            handler.handleExpireFail(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            assertEquals(QuestChainState.FAILED, state.getState());
            assertTrue(state.getCurrentQuestKey().isEmpty());
            verify(mockPersistence).saveChainStateAsync(eq(playerUUID), eq(state));
        }

        @Test
        @DisplayName("Given active chain, when fail is called, then QuestChainFailEvent is fired")
        void handleExpireFail_firesFailEvent() {
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            UUID playerUUID = UUID.randomUUID();

            handler.handleExpireFail(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            server.getPluginManager().assertEventFired(QuestChainFailEvent.class);
        }

        @Test
        @DisplayName("Given retry counters exist for this chain, when fail is called, then counters are cleared")
        void handleExpireFail_clearsRetryCounters() {
            UUID playerUUID = UUID.randomUUID();
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A), 2);
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_B), 1);
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireFail(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            assertTrue(retryCounters.isEmpty());
        }
    }

    @Nested
    @DisplayName("handleExpireRetry")
    class HandleExpireRetry {

        @Test
        @DisplayName("Given retries remaining, when retry is called, then step quest is restarted")
        void handleExpireRetry_restartsQuest_whenRetriesRemain() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            assertEquals(1, retryCounters.get(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A)));
            assertEquals(QuestChainState.ACTIVE, state.getState());
        }

        @Test
        @DisplayName("Given retries remaining, when retry succeeds, then QuestChainStepRetryEvent is fired")
        void handleExpireRetry_firesRetryEvent() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            server.getPluginManager().assertEventFired(QuestChainStepRetryEvent.class);
        }

        @Test
        @DisplayName("Given max retries exhausted, when retry is called, then chain fails instead")
        void handleExpireRetry_failsChain_whenMaxRetriesExhausted() {
            UUID playerUUID = UUID.randomUUID();
            ChainExpirationHandler.RetryKey retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            retryCounters.put(retryKey, 3);
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
            server.getPluginManager().assertEventFired(QuestChainFailEvent.class);
            assertFalse(retryCounters.containsKey(retryKey));
        }

        @Test
        @DisplayName("Given unlimited retries, when retry is called many times, then it never fails from exhaustion")
        void handleExpireRetry_neverExhausts_whenUnlimitedRetries() {
            NamespacedKey unlimitedQuestKey = new NamespacedKey("mcrpg", "unlimited_quest");
            QuestChainStep unlimitedStep = new QuestChainStep(unlimitedQuestKey, List.of(), "retry", -1, null);
            QuestChainDefinition singleChain = new QuestChainDefinition.Builder(
                    CHAIN_KEY, SOURCE_KEY, TRIGGER_KEY, List.of(unlimitedStep)).build();

            UUID playerUUID = UUID.randomUUID();
            ChainExpirationHandler.RetryKey retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, unlimitedQuestKey);
            retryCounters.put(retryKey, 999);
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, unlimitedQuestKey);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(singleChain), any())).thenReturn(true);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, singleChain, state, chainData, unlimitedQuestKey);

            assertEquals(QuestChainState.ACTIVE, state.getState());
            assertEquals(1000, retryCounters.get(retryKey));
        }

        @Test
        @DisplayName("Given quest key not in definition steps, when retry is called, then chain fails")
        void handleExpireRetry_failsChain_whenStepNotInDefinition() {
            UUID playerUUID = UUID.randomUUID();
            NamespacedKey unknownQuest = new NamespacedKey("mcrpg", "unknown_quest");
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, unknownQuest);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, unknownQuest);

            assertEquals(QuestChainState.FAILED, state.getState());
        }

        @Test
        @DisplayName("Given quest starter fails, when retry is called, then chain fails")
        void handleExpireRetry_failsChain_whenStarterFails() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(false);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
        }
    }

    @Nested
    @DisplayName("handleExpireRestartChain")
    class HandleExpireRestartChain {

        @Test
        @DisplayName("Given active chain, when restart is called, then state resets to first step")
        void handleExpireRestartChain_resetsToFirstStep() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_B);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            assertEquals(QuestChainState.ACTIVE, state.getState());
            assertEquals(Optional.of(QUEST_A), state.getCurrentQuestKey());
            verify(mockPersistence).saveChainStateAsync(eq(playerUUID), eq(state));
        }

        @Test
        @DisplayName("Given active chain, when restart succeeds, then QuestChainRestartEvent is fired")
        void handleExpireRestartChain_firesRestartEvent() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_B);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            server.getPluginManager().assertEventFired(QuestChainRestartEvent.class);
        }

        @Test
        @DisplayName("Given quest starter fails, when restart is called, then chain fails instead")
        void handleExpireRestartChain_failsChain_whenStarterFails() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_B);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(false);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            assertEquals(QuestChainState.FAILED, state.getState());
            assertTrue(state.getCurrentQuestKey().isEmpty());
        }

        @Test
        @DisplayName("Given retry counters exist for this chain, when restart is called, then counters are cleared")
        void handleExpireRestartChain_clearsRetryCounters() {
            UUID playerUUID = UUID.randomUUID();
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A), 2);
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_B), 1);
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_B);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, threeStepChain, state, chainData);

            assertTrue(retryCounters.isEmpty());
        }
    }

    @Nested
    @DisplayName("handleExpireSkip")
    class HandleExpireSkip {

        @Test
        @DisplayName("Given middle step, when skip is called, then chain advances to next step")
        void handleExpireSkip_advancesToNextStep() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            assertEquals(QuestChainState.ACTIVE, state.getState());
            assertEquals(Optional.of(QUEST_B), state.getCurrentQuestKey());
            verify(mockPersistence).saveChainStateAsync(eq(playerUUID), eq(state));
        }

        @Test
        @DisplayName("Given last step, when skip is called, then chain completes")
        void handleExpireSkip_completesChain_whenLastStep() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_C);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_C);

            assertEquals(QuestChainState.COMPLETED, state.getState());
            assertTrue(state.getCurrentQuestKey().isEmpty());
            assertEquals(1, state.getCompletionCount());
            server.getPluginManager().assertEventFired(QuestChainCompleteEvent.class);
        }

        @Test
        @DisplayName("Given middle step with online player, when skip is called, then QuestChainStepAdvanceEvent is fired")
        void handleExpireSkip_firesAdvanceEvent_whenPlayerOnline() {
            UUID playerUUID = UUID.randomUUID();
            server.addPlayer(new org.mockbukkit.mockbukkit.entity.PlayerMock(server, "SkipPlayer", playerUUID));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(true);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            server.getPluginManager().assertEventFired(QuestChainStepAdvanceEvent.class);
        }

        @Test
        @DisplayName("Given quest starter fails on next step, when skip is called, then chain fails")
        void handleExpireSkip_failsChain_whenStarterFails() {
            UUID playerUUID = UUID.randomUUID();
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);
            when(mockStarter.startStepQuest(eq(playerUUID), eq(threeStepChain), any())).thenReturn(false);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, threeStepChain, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
        }
    }

    @Nested
    @DisplayName("clearRetryCountersForChain")
    class ClearRetryCountersForChain {

        @Test
        @DisplayName("Given counters for multiple chains, when clearing one chain, then only that chain's counters are removed")
        void clearRetryCountersForChain_removesOnlyTargetChain() {
            UUID playerUUID = UUID.randomUUID();
            NamespacedKey otherChain = new NamespacedKey("mcrpg", "other_chain");
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A), 2);
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_B), 1);
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerUUID, otherChain, QUEST_A), 3);

            handler.clearRetryCountersForChain(playerUUID, CHAIN_KEY);

            assertEquals(1, retryCounters.size());
            assertTrue(retryCounters.containsKey(new ChainExpirationHandler.RetryKey(playerUUID, otherChain, QUEST_A)));
        }

        @Test
        @DisplayName("Given counters for multiple players, when clearing one player's chain, then other players are unaffected")
        void clearRetryCountersForChain_doesNotAffectOtherPlayers() {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerA, CHAIN_KEY, QUEST_A), 2);
            retryCounters.put(new ChainExpirationHandler.RetryKey(playerB, CHAIN_KEY, QUEST_A), 1);

            handler.clearRetryCountersForChain(playerA, CHAIN_KEY);

            assertEquals(1, retryCounters.size());
            assertTrue(retryCounters.containsKey(new ChainExpirationHandler.RetryKey(playerB, CHAIN_KEY, QUEST_A)));
        }
    }

    @Nested
    @DisplayName("RetryKey")
    class RetryKeyTests {

        @Test
        @DisplayName("Given same values, when comparing two RetryKeys, then they are equal")
        void retryKey_isEqual_whenSameValues() {
            UUID uuid = UUID.randomUUID();
            ChainExpirationHandler.RetryKey key1 = new ChainExpirationHandler.RetryKey(uuid, CHAIN_KEY, QUEST_A);
            ChainExpirationHandler.RetryKey key2 = new ChainExpirationHandler.RetryKey(uuid, CHAIN_KEY, QUEST_A);

            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }
    }
}
