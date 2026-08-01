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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChainPersistenceService}. Covers the synchronous flush path and the
 * dirty-flag lifecycle. Async persistence paths and the write-generation gate rely on the
 * {@link FailSafeTransaction} contract and the write-generation CAS in the DB lambdas.
 */
public class ChainPersistenceServiceTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    private ChainPersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService = new ChainPersistenceService(mcRPG);
    }

    @Test
    @DisplayName("Given a dirty state and a successful DAO write, When flushChainStatesSync is called, Then the state is no longer dirty")
    void flushChainStatesSync_clearsDirty_whenDAOSucceeds() throws SQLException {
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        // Mutate to make dirty
        state.advance(QUEST_KEY);
        assertTrue(state.isDirty());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        assertFalse(state.isDirty(),
                "Dirty flag should be cleared after a successful synchronous flush");
    }

    @Test
    @DisplayName("Given a dirty state and a failing DAO write, When flushChainStatesSync is called, Then the state remains dirty")
    void flushChainStatesSync_retainsDirty_whenDAOFails() throws SQLException {
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.advance(QUEST_KEY);
        assertTrue(state.isDirty());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("simulated failure"));

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        assertTrue(state.isDirty(),
                "Dirty flag should remain set when the DAO write fails");
    }

    @Test
    @DisplayName("Given no dirty states, When flushChainStatesSync is called, Then no SQL is executed")
    void flushChainStatesSync_doesNothing_whenNoStatesDirty() throws SQLException {
        QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, QUEST_KEY, QuestChainState.ACTIVE, 0, null);
        assertFalse(state.isDirty());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        verify(mockConnection, never()).prepareStatement(anyString());
    }

    @Test
    @DisplayName("Given no pending save, When cleanupPlayer is called, Then subsequent calls do not throw")
    void cleanupPlayer_isIdempotent() {
        assertDoesNotThrow(() -> {
            persistenceService.cleanupPlayer(PLAYER_UUID);
            persistenceService.cleanupPlayer(PLAYER_UUID);
        }, "cleanupPlayer must be safe to call multiple times for the same player");
    }

    @Test
    @DisplayName("Given a dirty state with pending advancements, When flushChainStatesSync is called, Then advancement log entries are included in the transaction")
    void flushChainStatesSync_replaysAdvancements_whenStateHasPending() throws SQLException {
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.recordAdvancement(QUEST_KEY, Instant.ofEpochMilli(1000L), 1);
        assertTrue(state.isDirty());
        assertEquals(1, state.getPendingAdvancements().size());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        // State upsert + completion log = at least 2 prepared statements
        verify(mockConnection, atLeast(2)).prepareStatement(anyString());
        verify(mockConnection).prepareStatement(contains("INSERT OR REPLACE INTO mcrpg_quest_chain_completion_log"));
        assertFalse(state.isDirty(), "Dirty flag should be cleared after successful flush");
        assertTrue(state.getPendingAdvancements().isEmpty(), "Pending advancements should be cleared after flush");
    }

    @Test
    @DisplayName("Given a dirty state with no pending advancements, When flushChainStatesSync is called, Then only state upsert is written")
    void flushChainStatesSync_writesOnlyState_whenNoPendingAdvancements() throws SQLException {
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        assertTrue(state.isDirty());
        assertTrue(state.getPendingAdvancements().isEmpty());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        verify(mockConnection, never()).prepareStatement(contains("mcrpg_quest_chain_completion_log"));
        assertFalse(state.isDirty());
    }

    @Test
    @DisplayName("Given pending advancements added before flush, When flushChainStatesSync completes, Then advancements are cleared")
    void flushChainStatesSync_clearsPendingAdvancements_afterSuccessfulFlush() throws SQLException {
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.recordAdvancement(QUEST_KEY, Instant.ofEpochMilli(1000L), 1);
        assertEquals(1, state.getPendingAdvancements().size());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        assertTrue(state.getPendingAdvancements().isEmpty(),
                "Pending advancements must be cleared after successful flush");
    }

    @Test
    @DisplayName("Given pending advancements fail to persist, When flushChainStatesSync fails, Then advancements are NOT cleared")
    void flushChainStatesSync_retainsPendingAdvancements_whenFlushFails() throws SQLException {
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.recordAdvancement(QUEST_KEY, Instant.ofEpochMilli(1000L), 1);
        assertEquals(1, state.getPendingAdvancements().size());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("simulated failure"));

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        assertEquals(1, state.getPendingAdvancements().size(),
                "Pending advancements must not be cleared when the flush fails");
    }

    @Test
    @DisplayName("Given state with pending advancements, When saveChainStateAsync is called, Then pendingAdvancements is not cleared")
    void saveChainStateAsync_doesNotClearPendingAdvancements() {
        // saveChainStateAsync must snapshot without clearing so the sync flush at logout
        // can replay entries if the async write was skipped or failed.
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        state.recordAdvancement(QUEST_KEY, Instant.ofEpochMilli(1000L), 1);
        assertEquals(1, state.getPendingAdvancements().size(), "Precondition: one pending advancement");

        // Mock database manager with a no-op executor so the async task is never executed.
        // We only care that the snapshot (without clear) happens on the calling thread.
        McRPGDatabaseManager mockDatabaseManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        when(mockDatabaseManager.getDatabase()).thenReturn(mockDatabase);
        ThreadPoolExecutor noOpExecutor = mock(ThreadPoolExecutor.class);
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(noOpExecutor);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDatabaseManager);

        persistenceService.saveChainStateAsync(PLAYER_UUID, state);

        assertEquals(1, state.getPendingAdvancements().size(),
                "Pending advancements must not be cleared by saveChainStateAsync so the sync flush can replay them on failure");
    }

}
