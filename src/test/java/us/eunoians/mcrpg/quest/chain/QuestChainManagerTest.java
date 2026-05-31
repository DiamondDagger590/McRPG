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
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
        state.complete(1000L);
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
    @DisplayName("Given DAO prepareStatement throws RuntimeException, When resetChain is called, Then callback always receives false")
    void resetChain_callbackReceivesFalse_whenDAOThrowsRuntimeException() throws Exception {
        // Load player with chain state
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        QuestChainPlayerData playerData = new QuestChainPlayerData();
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        playerData.putChainState(state);
        when(mockPlayer.getChainData()).thenReturn(playerData);
        when(mockPlayerManager.getPlayer(PLAYER_UUID)).thenReturn(Optional.of(mockPlayer));

        // Mock database manager: executor runs submitted tasks synchronously on the calling thread
        McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);

        ThreadPoolExecutor syncExecutor = mock(ThreadPoolExecutor.class);
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return mock(Future.class);
        }).when(syncExecutor).submit(any(Runnable.class));
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(syncExecutor);

        // Connection throws RuntimeException on prepareStatement (e.g. wrapped JDBC driver error)
        Connection mockConnection = mock(Connection.class);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new RuntimeException("simulated DAO failure"));

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

        // Capture callback result
        AtomicReference<Boolean> callbackResult = new AtomicReference<>();
        chainManager.resetChain(PLAYER_UUID, CHAIN_KEY, callbackResult::set);

        // Tick the MockBukkit scheduler to deliver the main-thread callback
        server.getScheduler().performTicks(1);

        assertNotNull(callbackResult.get(), "Callback must always be called, even on RuntimeException");
        assertFalse(callbackResult.get(), "Callback must receive false when the DAO throws RuntimeException");
    }
}
