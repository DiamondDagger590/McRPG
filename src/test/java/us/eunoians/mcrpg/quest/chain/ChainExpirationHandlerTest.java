package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.event.quest.chain.QuestChainCompleteEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainFailEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainRestartEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainStepAdvanceEvent;
import us.eunoians.mcrpg.event.quest.chain.QuestChainStepRetryEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ChainExpirationHandler")
class ChainExpirationHandlerTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = NamespacedKey.fromString("mcrpg:test_chain");
    private static final NamespacedKey QUEST_A = NamespacedKey.fromString("mcrpg:quest_a");
    private static final NamespacedKey QUEST_B = NamespacedKey.fromString("mcrpg:quest_b");
    private static final NamespacedKey QUEST_C = NamespacedKey.fromString("mcrpg:quest_c");
    private static final NamespacedKey SOURCE_KEY = NamespacedKey.fromString("mcrpg:chain_source");
    private static final NamespacedKey TRIGGER_KEY = NamespacedKey.fromString("mcrpg:login");

    private ChainPersistenceService persistenceService;
    private ChainQuestStarter chainQuestStarter;
    private Map<ChainExpirationHandler.RetryKey, Integer> retryCounters;
    private ChainExpirationHandler handler;
    private UUID playerUUID;

    @BeforeEach
    void setUp() throws Exception {
        persistenceService = mock(ChainPersistenceService.class);
        chainQuestStarter = mock(ChainQuestStarter.class);
        retryCounters = new ConcurrentHashMap<>();
        handler = new ChainExpirationHandler(mcRPG, persistenceService, chainQuestStarter, retryCounters);

        PlayerMock player = server.addPlayer();
        playerUUID = player.getUniqueId();

        McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);

        ThreadPoolExecutor syncExecutor = mock(ThreadPoolExecutor.class);
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return mock(Future.class);
        }).when(syncExecutor).submit(any(Runnable.class));
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(syncExecutor);

        Connection mockConnection = mock(Connection.class);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);
    }

    private QuestChainDefinition buildDefinition(List<QuestChainStep> steps) {
        return new QuestChainDefinition.Builder(CHAIN_KEY, SOURCE_KEY, TRIGGER_KEY, steps).build();
    }

    @Nested
    @DisplayName("RetryKey")
    class RetryKeyTests {

        @Test
        @DisplayName("equal keys with same fields are equal")
        void equalKeys_areEqual() {
            var key1 = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            var key2 = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            assertEquals(key1, key2);
            assertEquals(key1.hashCode(), key2.hashCode());
        }

        @Test
        @DisplayName("keys with different quest keys are not equal")
        void differentQuestKeys_notEqual() {
            var key1 = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            var key2 = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_B);
            assertNotEquals(key1, key2);
        }

        @Test
        @DisplayName("accessors return constructor values")
        void accessors_returnConstructorValues() {
            var key = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            assertEquals(playerUUID, key.playerUUID());
            assertEquals(CHAIN_KEY, key.chainKey());
            assertEquals(QUEST_A, key.questKey());
        }
    }

    @Nested
    @DisplayName("clearRetryCountersForChain")
    class ClearRetryCountersTests {

        @Test
        @DisplayName("removes all counters for player+chain combination")
        void clearRetryCountersForChain_removesMatchingEntries() {
            var keyA = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            var keyB = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_B);
            retryCounters.put(keyA, 2);
            retryCounters.put(keyB, 1);

            handler.clearRetryCountersForChain(playerUUID, CHAIN_KEY);

            assertTrue(retryCounters.isEmpty());
        }

        @Test
        @DisplayName("preserves counters for other players")
        void clearRetryCountersForChain_preservesOtherPlayers() {
            UUID otherPlayer = UUID.randomUUID();
            var myKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            var otherKey = new ChainExpirationHandler.RetryKey(otherPlayer, CHAIN_KEY, QUEST_A);
            retryCounters.put(myKey, 2);
            retryCounters.put(otherKey, 3);

            handler.clearRetryCountersForChain(playerUUID, CHAIN_KEY);

            assertFalse(retryCounters.containsKey(myKey));
            assertTrue(retryCounters.containsKey(otherKey));
            assertEquals(3, retryCounters.get(otherKey));
        }

        @Test
        @DisplayName("preserves counters for other chains")
        void clearRetryCountersForChain_preservesOtherChains() {
            NamespacedKey otherChain = NamespacedKey.fromString("mcrpg:other_chain");
            var myKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            var otherKey = new ChainExpirationHandler.RetryKey(playerUUID, otherChain, QUEST_A);
            retryCounters.put(myKey, 2);
            retryCounters.put(otherKey, 1);

            handler.clearRetryCountersForChain(playerUUID, CHAIN_KEY);

            assertFalse(retryCounters.containsKey(myKey));
            assertTrue(retryCounters.containsKey(otherKey));
        }

        @Test
        @DisplayName("no-op on empty map")
        void clearRetryCountersForChain_noOpOnEmpty() {
            handler.clearRetryCountersForChain(playerUUID, CHAIN_KEY);
            assertTrue(retryCounters.isEmpty());
        }
    }

    @Nested
    @DisplayName("handleExpireFail")
    class HandleExpireFailTests {

        @Test
        @DisplayName("sets state to FAILED")
        void handleExpireFail_setsStateFailed() {
            QuestChainDefinition definition = buildDefinition(List.of(QuestChainStep.simple(QUEST_A)));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireFail(playerUUID, CHAIN_KEY, definition, state, chainData);

            assertEquals(QuestChainState.FAILED, state.getState());
            assertTrue(state.getCurrentQuestKey().isEmpty());
        }

        @Test
        @DisplayName("saves state asynchronously")
        void handleExpireFail_savesStateAsync() {
            QuestChainDefinition definition = buildDefinition(List.of(QuestChainStep.simple(QUEST_A)));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireFail(playerUUID, CHAIN_KEY, definition, state, chainData);

            verify(persistenceService).saveChainStateAsync(playerUUID, state);
        }

        @Test
        @DisplayName("fires QuestChainFailEvent")
        void handleExpireFail_firesFailEvent() {
            QuestChainDefinition definition = buildDefinition(List.of(QuestChainStep.simple(QUEST_A)));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireFail(playerUUID, CHAIN_KEY, definition, state, chainData);

            server.getPluginManager().assertEventFired(QuestChainFailEvent.class);
        }

        @Test
        @DisplayName("clears retry counters for the chain")
        void handleExpireFail_clearsRetryCounters() {
            QuestChainDefinition definition = buildDefinition(List.of(QuestChainStep.simple(QUEST_A)));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            var retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            retryCounters.put(retryKey, 3);

            handler.handleExpireFail(playerUUID, CHAIN_KEY, definition, state, chainData);

            assertFalse(retryCounters.containsKey(retryKey));
        }
    }

    @Nested
    @DisplayName("handleExpireRetry")
    class HandleExpireRetryTests {

        @Test
        @DisplayName("retries step and increments counter on first attempt")
        void handleExpireRetry_retriesStep_firstAttempt() {
            QuestChainStep step = new QuestChainStep(QUEST_A, List.of(), "retry", 3, null);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(step))).thenReturn(true);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            var retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            assertEquals(1, retryCounters.get(retryKey));
            assertEquals(QuestChainState.ACTIVE, state.getState());
        }

        @Test
        @DisplayName("fires QuestChainStepRetryEvent on success")
        void handleExpireRetry_firesRetryEvent() {
            QuestChainStep step = new QuestChainStep(QUEST_A, List.of(), "retry", 3, null);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(step))).thenReturn(true);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            server.getPluginManager().assertEventFired(QuestChainStepRetryEvent.class);
        }

        @Test
        @DisplayName("falls back to fail when max retries exhausted")
        void handleExpireRetry_fallsBackToFail_whenMaxRetriesExhausted() {
            QuestChainStep step = new QuestChainStep(QUEST_A, List.of(), "retry", 2, null);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            var retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            retryCounters.put(retryKey, 2);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
            assertFalse(retryCounters.containsKey(retryKey));
            server.getPluginManager().assertEventFired(QuestChainFailEvent.class);
        }

        @Test
        @DisplayName("unlimited retries never exhaust (-1 maxRetries)")
        void handleExpireRetry_unlimitedRetries() {
            QuestChainStep step = new QuestChainStep(QUEST_A, List.of(), "retry", -1, null);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            var retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            retryCounters.put(retryKey, 100);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(step))).thenReturn(true);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(101, retryCounters.get(retryKey));
            assertEquals(QuestChainState.ACTIVE, state.getState());
        }

        @Test
        @DisplayName("falls back to fail when quest start fails")
        void handleExpireRetry_fallsBackToFail_whenQuestStartFails() {
            QuestChainStep step = new QuestChainStep(QUEST_A, List.of(), "retry", -1, null);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(step))).thenReturn(false);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
            server.getPluginManager().assertEventFired(QuestChainFailEvent.class);
        }

        @Test
        @DisplayName("falls back to fail when step not found in definition")
        void handleExpireRetry_fallsBackToFail_whenStepNotFound() {
            QuestChainStep step = QuestChainStep.simple(QUEST_A);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            NamespacedKey unknownQuest = NamespacedKey.fromString("mcrpg:unknown_quest");

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, unknownQuest);

            assertEquals(QuestChainState.FAILED, state.getState());
            verify(chainQuestStarter, never()).startStepQuest(any(), any(), any());
        }

        @Test
        @DisplayName("zero maxRetries means no retries allowed")
        void handleExpireRetry_zeroMaxRetries_failsImmediately() {
            QuestChainStep step = new QuestChainStep(QUEST_A, List.of(), "retry", 0, null);
            QuestChainDefinition definition = buildDefinition(List.of(step));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireRetry(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
        }
    }

    @Nested
    @DisplayName("handleExpireRestartChain")
    class HandleExpireRestartChainTests {

        @Test
        @DisplayName("resets state to first step")
        void handleExpireRestartChain_resetsToFirstStep() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainStep stepB = QuestChainStep.simple(QUEST_B);
            QuestChainDefinition definition = buildDefinition(List.of(stepA, stepB));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_B);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepA))).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, definition, state, chainData);

            assertEquals(QuestChainState.ACTIVE, state.getState());
            assertTrue(state.getCurrentQuestKey().isPresent());
            assertEquals(QUEST_A, state.getCurrentQuestKey().orElseThrow());
        }

        @Test
        @DisplayName("fires QuestChainRestartEvent on success")
        void handleExpireRestartChain_firesRestartEvent() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainDefinition definition = buildDefinition(List.of(stepA));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepA))).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, definition, state, chainData);

            server.getPluginManager().assertEventFired(QuestChainRestartEvent.class);
        }

        @Test
        @DisplayName("saves state asynchronously on success")
        void handleExpireRestartChain_savesStateAsync() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainDefinition definition = buildDefinition(List.of(stepA));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepA))).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, definition, state, chainData);

            verify(persistenceService).saveChainStateAsync(playerUUID, state);
        }

        @Test
        @DisplayName("fails chain when quest start fails")
        void handleExpireRestartChain_failsChain_whenQuestStartFails() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainDefinition definition = buildDefinition(List.of(stepA));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepA))).thenReturn(false);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, definition, state, chainData);

            assertEquals(QuestChainState.FAILED, state.getState());
            verify(persistenceService).saveChainStateAsync(playerUUID, state);
        }

        @Test
        @DisplayName("clears retry counters")
        void handleExpireRestartChain_clearsRetryCounters() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainDefinition definition = buildDefinition(List.of(stepA));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            var retryKey = new ChainExpirationHandler.RetryKey(playerUUID, CHAIN_KEY, QUEST_A);
            retryCounters.put(retryKey, 5);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepA))).thenReturn(true);

            handler.handleExpireRestartChain(playerUUID, CHAIN_KEY, definition, state, chainData);

            assertTrue(retryCounters.isEmpty());
        }
    }

    @Nested
    @DisplayName("handleExpireSkip")
    class HandleExpireSkipTests {

        @Test
        @DisplayName("advances to next step when middle step skipped")
        void handleExpireSkip_advancesToNextStep() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainStep stepB = QuestChainStep.simple(QUEST_B);
            QuestChainDefinition definition = buildDefinition(List.of(stepA, stepB));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepB))).thenReturn(true);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QUEST_B, state.getCurrentQuestKey().orElseThrow());
            assertEquals(QuestChainState.ACTIVE, state.getState());
            verify(persistenceService).saveChainStateAsync(playerUUID, state);
        }

        @Test
        @DisplayName("fires QuestChainStepAdvanceEvent when player is online")
        void handleExpireSkip_firesAdvanceEvent_whenPlayerOnline() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainStep stepB = QuestChainStep.simple(QUEST_B);
            QuestChainDefinition definition = buildDefinition(List.of(stepA, stepB));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepB))).thenReturn(true);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            server.getPluginManager().assertEventFired(QuestChainStepAdvanceEvent.class);
        }

        @Test
        @DisplayName("completes chain when last step skipped")
        void handleExpireSkip_completesChain_whenLastStep() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainDefinition definition = buildDefinition(List.of(stepA));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QuestChainState.COMPLETED, state.getState());
            assertTrue(state.getCurrentQuestKey().isEmpty());
            assertEquals(1, state.getCompletionCount());
            server.getPluginManager().assertEventFired(QuestChainCompleteEvent.class);
            verify(persistenceService).saveChainStateAsync(playerUUID, state);
        }

        @Test
        @DisplayName("fails chain when next step quest start fails")
        void handleExpireSkip_failsChain_whenQuestStartFails() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainStep stepB = QuestChainStep.simple(QUEST_B);
            QuestChainDefinition definition = buildDefinition(List.of(stepA, stepB));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepB))).thenReturn(false);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QuestChainState.FAILED, state.getState());
            verify(persistenceService).saveChainStateAsync(playerUUID, state);
        }

        @Test
        @DisplayName("offline player still advances but does not fire advance event")
        void handleExpireSkip_advancesWithoutEvent_whenPlayerOffline() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainStep stepB = QuestChainStep.simple(QUEST_B);
            QuestChainDefinition definition = buildDefinition(List.of(stepA, stepB));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_A);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            UUID offlineUUID = UUID.randomUUID();

            when(chainQuestStarter.startStepQuest(eq(offlineUUID), eq(definition), eq(stepB))).thenReturn(true);

            server.getPluginManager().clearEvents();
            handler.handleExpireSkip(offlineUUID, CHAIN_KEY, definition, state, chainData, QUEST_A);

            assertEquals(QUEST_B, state.getCurrentQuestKey().orElseThrow());
            assertEquals(QuestChainState.ACTIVE, state.getState());
            verify(persistenceService).saveChainStateAsync(offlineUUID, state);
            server.getPluginManager().assertEventNotFired(QuestChainStepAdvanceEvent.class);
        }

        @Test
        @DisplayName("skip middle of three-step chain advances correctly")
        void handleExpireSkip_threeStepChain_advancesFromMiddle() {
            QuestChainStep stepA = QuestChainStep.simple(QUEST_A);
            QuestChainStep stepB = QuestChainStep.simple(QUEST_B);
            QuestChainStep stepC = QuestChainStep.simple(QUEST_C);
            QuestChainDefinition definition = buildDefinition(List.of(stepA, stepB, stepC));
            QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_B);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            chainData.putChainState(state);

            when(chainQuestStarter.startStepQuest(eq(playerUUID), eq(definition), eq(stepC))).thenReturn(true);

            handler.handleExpireSkip(playerUUID, CHAIN_KEY, definition, state, chainData, QUEST_B);

            assertEquals(QUEST_C, state.getCurrentQuestKey().orElseThrow());
            assertEquals(QuestChainState.ACTIVE, state.getState());
        }
    }
}
