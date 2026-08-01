package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link QuestChainManager}. Covers the defensive no-op paths that guard against
 * absent players and the DAO delegation path in {@link QuestChainManager#loadChainStates}.
 */
public class QuestChainManagerTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    private QuestChainManager chainManager;
    private McRPGPlayerManager mockPlayerManager;

    @BeforeEach
    void setUp() {
        mockPlayerManager = mock(McRPGPlayerManager.class);
        when(mockPlayerManager.getPlayer(any(UUID.class))).thenReturn(Optional.empty());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        chainManager = new QuestChainManager(mcRPG);
    }

    @Test
    @DisplayName("Given player not loaded, When getChainStatus is called, Then it returns empty")
    void getChainStatus_returnsEmpty_whenPlayerNotLoaded() {
        Optional<QuestChainPlayerState> result = chainManager.getChainStatus(PLAYER_UUID, CHAIN_KEY);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Given player is loaded with chain state, When getChainStatus is called, Then it returns the state")
    void getChainStatus_returnsState_whenPlayerIsLoaded() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        Optional<QuestChainPlayerState> result = chainManager.getChainStatus(PLAYER_UUID, CHAIN_KEY);

        assertTrue(result.isPresent());
        assertEquals(QuestChainState.ACTIVE, result.get().getState());
    }

    @Test
    @DisplayName("Given player not loaded, When handleQuestCancelled is called, Then it is a no-op")
    void handleQuestCancelled_isNoOp_whenPlayerNotLoaded() {
        chainManager.handleQuestCancelled(PLAYER_UUID, QUEST_KEY);

        // The guard performs exactly one player-lookup and then returns early; nothing else
        // is called on the player manager (no getChainData, no state mutations).
        verify(mockPlayerManager).getPlayer(PLAYER_UUID);
        verifyNoMoreInteractions(mockPlayerManager);
    }

    @Test
    @DisplayName("Given player not loaded, When handleQuestExpired is called, Then it is a no-op")
    void handleQuestExpired_isNoOp_whenPlayerNotLoaded() {
        chainManager.handleQuestExpired(PLAYER_UUID, QUEST_KEY);

        verify(mockPlayerManager).getPlayer(PLAYER_UUID);
        verifyNoMoreInteractions(mockPlayerManager);
    }

    @Test
    @DisplayName("Given player not loaded, When forceAdvanceChain is called, Then it returns false")
    void forceAdvanceChain_returnsFalse_whenPlayerNotLoaded() {
        boolean result = chainManager.forceAdvanceChain(PLAYER_UUID, CHAIN_KEY);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given player has chain in non-ACTIVE state, When forceAdvanceChain is called, Then it returns false")
    void forceAdvanceChain_returnsFalse_whenChainNotActive() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.complete(Instant.ofEpochMilli(1000L));
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        boolean result = chainManager.forceAdvanceChain(PLAYER_UUID, CHAIN_KEY);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given player has no chain state, When forceAdvanceChain is called, Then it returns false")
    void forceAdvanceChain_returnsFalse_whenNoChainState() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        when(mockPlayer.getChainData()).thenReturn(new QuestChainPlayerData());
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        boolean result = chainManager.forceAdvanceChain(PLAYER_UUID, CHAIN_KEY);

        assertFalse(result);
    }

    @Test
    @DisplayName("Given a mocked connection returning no rows, When loadChainStates is called, Then it returns an empty list")
    void loadChainStates_returnsEmpty_whenNoStatesInDatabase() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        List<QuestChainPlayerState> states = chainManager.loadChainStates(mockConnection, PLAYER_UUID);

        assertNotNull(states);
        assertTrue(states.isEmpty());
    }

    @Test
    @DisplayName("Given a mocked connection returning one row, When loadChainStates is called, Then it returns one state")
    void loadChainStates_returnsOneState_whenDatabaseHasOneRow() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("chain_key")).thenReturn(CHAIN_KEY.toString());
        when(mockResultSet.getString("current_quest")).thenReturn(QUEST_KEY.toString());
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getInt("completion_count")).thenReturn(0);
        when(mockResultSet.getLong("last_completed_at")).thenReturn(0L);
        when(mockResultSet.wasNull()).thenReturn(true);

        List<QuestChainPlayerState> states = chainManager.loadChainStates(mockConnection, PLAYER_UUID);

        assertEquals(1, states.size());
        assertEquals(CHAIN_KEY, states.get(0).getChainKey());
        assertEquals(QuestChainState.ACTIVE, states.get(0).getState());
    }

    @Test
    @DisplayName("Given player has active chain and quest not in index, When handleQuestCancelled is called with non-chain quest, Then state remains ACTIVE")
    void handleQuestCancelled_isNoOp_whenQuestNotPartOfChain() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        NamespacedKey otherQuestKey = new NamespacedKey("mcrpg", "other_quest");
        chainManager.handleQuestCancelled(PLAYER_UUID, otherQuestKey);

        // Chain state should remain ACTIVE since the cancelled quest is not part of this chain
        assertEquals(QuestChainState.ACTIVE, state.getState());
    }

    @Test
    @DisplayName("Given advanceChain where next step quest definition is missing, When advanceChain is called, Then completed step is recorded as pending advancement")
    void advanceChain_recordsAdvancement_whenStartStepQuestFails() throws Exception {
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "test_source");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "test_trigger");
        NamespacedKey nextQuestKey = new NamespacedKey("mcrpg", "next_quest_adv");
        QuestChainStep step1 = QuestChainStep.simple(QUEST_KEY);
        QuestChainStep step2 = QuestChainStep.simple(nextQuestKey);

        QuestChainDefinition definition = new QuestChainDefinition.Builder(
                CHAIN_KEY, sourceKey, triggerKey, List.of(step1, step2))
                .build();

        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        chainRegistry.register(definition);

        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        // Provide a database manager so that saveChainStateAsync does not NPE when resolving
        // the executor. The async task is not run by the mock executor, but the snapshot of
        // pendingAdvancements (without clearing) happens on the calling thread before submission.
        McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
        ThreadPoolExecutor syncExecutor = mock(ThreadPoolExecutor.class);
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(syncExecutor);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

        // nextQuestKey is not registered in QuestDefinitionRegistry, so startStepQuest returns false
        boolean result = chainManager.advanceChain(PLAYER_UUID, QUEST_KEY);

        assertFalse(result, "advanceChain should return false when next step cannot start");
        // saveChainStateAsync snapshots without clearing, so pendingAdvancements is still intact
        assertEquals(1, state.getPendingAdvancements().size(),
                "Completed step should remain in pending advancements after failed advance");
        assertEquals(QUEST_KEY, state.getPendingAdvancements().get(0).questKey(),
                "The pending advancement should be for the completed quest key");
    }

    @Test
    @DisplayName("Given restartChain force=true where no online player exists, When restartChain is called, Then callback receives false and chain state is unchanged")
    void restartChain_callbackFalse_andStateUnchanged_whenPlayerOfflineDuringForce() {
        // No online player — Bukkit.getPlayer returns null, so startStepForPlayer short-circuits
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "test_source2");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "test_trigger2");
        NamespacedKey restartQuestKey = new NamespacedKey("mcrpg", "restart_quest");
        NamespacedKey restartChainKey = new NamespacedKey("mcrpg", "restart_chain");
        QuestChainStep step1 = QuestChainStep.simple(restartQuestKey);

        QuestChainDefinition definition = new QuestChainDefinition.Builder(
                restartChainKey, sourceKey, triggerKey, List.of(step1))
                .build();

        QuestChainRegistry chainRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_CHAIN);
        chainRegistry.register(definition);

        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(restartChainKey, restartQuestKey);
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        AtomicReference<Boolean> callbackResult = new AtomicReference<>();
        chainManager.restartChain(PLAYER_UUID, restartChainKey, true, callbackResult::set);

        assertNotNull(callbackResult.get(), "Callback must always be invoked");
        assertFalse(callbackResult.get(), "Callback must receive false when the online player is absent");
        // State must not be mutated — the old quest key is preserved (no cancel + reset happened)
        assertEquals(QuestChainState.ACTIVE, state.getState(),
                "Chain must remain ACTIVE when startStepForPlayer fails");
        assertEquals(restartQuestKey, state.getCurrentQuestKey().orElseThrow(),
                "Current quest key must be unchanged when start fails before any cancel");
    }

    @Test
    @DisplayName("Given player not loaded, when abandonChain is called, then it logs a warning and does nothing")
    void abandonChain_noOp_whenPlayerNotLoaded() {
        chainManager.abandonChain(PLAYER_UUID, CHAIN_KEY);

        verify(mockPlayerManager).getPlayer(PLAYER_UUID);
        verifyNoMoreInteractions(mockPlayerManager);
    }

    @Test
    @DisplayName("Given player is loaded with an ACTIVE chain, when abandonChain is called, then the state transitions to ABANDONED")
    void abandonChain_transitionsToAbandoned_whenActive() {
        QuestChainDefinition definition = new QuestChainDefinition.Builder(
                CHAIN_KEY,
                new NamespacedKey("mcrpg", "manual"),
                new NamespacedKey("mcrpg", "manual"),
                List.of(QuestChainStep.simple(QUEST_KEY))
        ).build();
        RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN).register(definition);

        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        // Only present so saveChainStateAsync can resolve an executor without NPE-ing. The write
        // itself is not exercised here: it goes through CompletableFuture#runAsync, which calls
        // Executor#execute, and nothing stubs that — the assertions below are on the state
        // transition abandonChain performs synchronously before submitting anything.
        McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(mock(ThreadPoolExecutor.class));
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

        chainManager.abandonChain(PLAYER_UUID, CHAIN_KEY);

        assertEquals(QuestChainState.ABANDONED, state.getState());
        assertTrue(state.getCurrentQuestKey().isEmpty());
    }

    @Test
    @DisplayName("Given player is loaded with a COMPLETED chain, when abandonChain is called, then the state is not modified")
    void abandonChain_noOp_whenAlreadyTerminal() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.complete(Instant.now());
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        chainManager.abandonChain(PLAYER_UUID, CHAIN_KEY);

        assertEquals(QuestChainState.COMPLETED, state.getState());
    }

    @Test
    @DisplayName("Given player is loaded with no chain state for the key, when abandonChain is called, then no exception is thrown and chain data is unchanged")
    void abandonChain_noOp_whenNoChainState() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        chainManager.abandonChain(PLAYER_UUID, CHAIN_KEY);

        assertTrue(playerData.getChainState(CHAIN_KEY).isEmpty(),
                "No chain state should exist after abandoning a non-existent chain");
    }
}
