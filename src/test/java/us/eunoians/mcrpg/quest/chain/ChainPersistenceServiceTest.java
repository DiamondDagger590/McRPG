package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
        QuestChainPlayerState state = QuestChainPlayerState.newActive(CHAIN_KEY, QUEST_KEY);
        // State is fresh — not dirty
        assertFalse(state.isDirty());

        QuestChainPlayerData chainData = new QuestChainPlayerData();
        chainData.putChainState(state);

        Connection mockConnection = mock(Connection.class);

        persistenceService.flushChainStatesSync(mockConnection, PLAYER_UUID, chainData);

        // prepareStatement should never be called if nothing is dirty
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
}
