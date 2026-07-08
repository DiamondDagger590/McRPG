package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.event.quest.chain.CascadeFinalizeEvent;
import us.eunoians.mcrpg.event.quest.chain.CascadeStartEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CascadeOrchestratorTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");

    private QuestChainManager mockChainManager;
    private CascadeOrchestrator orchestrator;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        McRPGPlayerManager mockPlayerManager = mock(McRPGPlayerManager.class);
        when(mockPlayerManager.getPlayer(any(UUID.class))).thenReturn(Optional.empty());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        mockChainManager = mock(QuestChainManager.class);
        orchestrator = new CascadeOrchestrator(mcRPG, mockChainManager);
    }

    @Test
    @DisplayName("Given a player not in a cascade, when tryStartChain is called and succeeds, then isInCascade returns false after the call")
    public void tryStartChain_cleansUpCascade_afterSuccess() {
        PlayerMock player = server.addPlayer("CascadeTestPlayer");
        when(mockChainManager.tryStartChain(eq(player), eq(CHAIN_KEY))).thenReturn(true);

        boolean result = orchestrator.tryStartChain(player, CHAIN_KEY);

        assertTrue(result);
        assertFalse(orchestrator.isInCascade(player.getUniqueId()));
    }

    @Test
    @DisplayName("Given a player not in a cascade, when tryStartChain is called and fails, then isInCascade returns false after the call")
    public void tryStartChain_cleansUpCascade_afterFailure() {
        PlayerMock player = server.addPlayer("CascadeFailPlayer");
        when(mockChainManager.tryStartChain(eq(player), eq(CHAIN_KEY))).thenReturn(false);

        boolean result = orchestrator.tryStartChain(player, CHAIN_KEY);

        assertFalse(result);
        assertFalse(orchestrator.isInCascade(player.getUniqueId()));
    }

    @Test
    @DisplayName("Given a player not in a cascade, when tryStartChain throws, then isInCascade returns false (try/finally guard)")
    public void tryStartChain_cleansUpCascade_onException() {
        PlayerMock player = server.addPlayer("CascadeExceptionPlayer");
        when(mockChainManager.tryStartChain(eq(player), eq(CHAIN_KEY)))
                .thenThrow(new RuntimeException("test exception"));

        assertThrows(RuntimeException.class,
                () -> orchestrator.tryStartChain(player, CHAIN_KEY));
        assertFalse(orchestrator.isInCascade(player.getUniqueId()));
    }

    @Test
    @DisplayName("Given a player not in a cascade, when tryStartChain is called, then CascadeStartEvent is fired")
    public void tryStartChain_firesCascadeStartEvent() {
        PlayerMock player = server.addPlayer("CascadeStartEventPlayer");
        when(mockChainManager.tryStartChain(eq(player), eq(CHAIN_KEY))).thenReturn(true);

        orchestrator.tryStartChain(player, CHAIN_KEY);

        server.getPluginManager().assertEventFired(CascadeStartEvent.class);
    }

    @Test
    @DisplayName("Given a player not in a cascade, when tryStartChain succeeds, then CascadeFinalizeEvent is fired after finalization")
    public void tryStartChain_firesCascadeFinalizeEvent() {
        PlayerMock player = server.addPlayer("CascadeFinalizePlayer");
        when(mockChainManager.tryStartChain(eq(player), eq(CHAIN_KEY))).thenReturn(true);

        orchestrator.tryStartChain(player, CHAIN_KEY);

        server.getPluginManager().assertEventFired(CascadeFinalizeEvent.class);
    }

    @Test
    @DisplayName("Given a player not in a cascade, when advanceChain is called and succeeds, then isInCascade returns false after the call")
    public void advanceChain_cleansUpCascade_afterSuccess() {
        PlayerMock player = server.addPlayer("AdvanceCascadePlayer");
        UUID playerUUID = player.getUniqueId();
        when(mockChainManager.advanceChain(eq(playerUUID), eq(QUEST_KEY))).thenReturn(true);

        boolean result = orchestrator.advanceChain(playerUUID, QUEST_KEY);

        assertTrue(result);
        assertFalse(orchestrator.isInCascade(playerUUID));
    }

    @Test
    @DisplayName("Given a player not in a cascade, when advanceChain throws, then isInCascade returns false (try/finally guard)")
    public void advanceChain_cleansUpCascade_onException() {
        PlayerMock player = server.addPlayer("AdvanceExceptionPlayer");
        UUID playerUUID = player.getUniqueId();
        when(mockChainManager.advanceChain(eq(playerUUID), eq(QUEST_KEY)))
                .thenThrow(new RuntimeException("test exception"));

        assertThrows(RuntimeException.class,
                () -> orchestrator.advanceChain(playerUUID, QUEST_KEY));
        assertFalse(orchestrator.isInCascade(playerUUID));
    }

    @Test
    @DisplayName("Given a player not in a cascade, when advanceChain is called, then CascadeStartEvent is fired")
    public void advanceChain_firesCascadeStartEvent() {
        PlayerMock player = server.addPlayer("AdvanceStartEventPlayer");
        UUID playerUUID = player.getUniqueId();
        when(mockChainManager.advanceChain(eq(playerUUID), eq(QUEST_KEY))).thenReturn(true);

        orchestrator.advanceChain(playerUUID, QUEST_KEY);

        server.getPluginManager().assertEventFired(CascadeStartEvent.class);
    }

    @Test
    @DisplayName("Given a player not in a cascade, when advanceChain succeeds, then CascadeFinalizeEvent is fired")
    public void advanceChain_firesCascadeFinalizeEvent() {
        PlayerMock player = server.addPlayer("AdvanceFinalizePlayer");
        UUID playerUUID = player.getUniqueId();
        when(mockChainManager.advanceChain(eq(playerUUID), eq(QUEST_KEY))).thenReturn(true);

        orchestrator.advanceChain(playerUUID, QUEST_KEY);

        server.getPluginManager().assertEventFired(CascadeFinalizeEvent.class);
    }

    @Test
    @DisplayName("Given a player not in a cascade, when notifyStepStarted is called, then getCascadeContext returns empty (no cascade active)")
    public void notifyStepStarted_noOp_whenNoCascade() {
        UUID playerUUID = UUID.randomUUID();

        orchestrator.notifyStepStarted(playerUUID, QUEST_KEY);

        assertTrue(orchestrator.getCascadeContext(playerUUID).isEmpty());
    }

    @Test
    @DisplayName("Given a player, when isInCascade is called before any cascade, then it returns false")
    public void isInCascade_returnsFalse_whenNoCascadeActive() {
        assertFalse(orchestrator.isInCascade(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Given a player, when getCascadeContext is called before any cascade, then it returns empty")
    public void getCascadeContext_returnsEmpty_whenNoCascadeActive() {
        assertTrue(orchestrator.getCascadeContext(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Given a successful tryStartChain, when the chain manager is verified, then tryStartChain was called on it")
    public void tryStartChain_delegatesToChainManager() {
        PlayerMock player = server.addPlayer("DelegatePlayer");
        when(mockChainManager.tryStartChain(eq(player), eq(CHAIN_KEY))).thenReturn(true);

        orchestrator.tryStartChain(player, CHAIN_KEY);

        verify(mockChainManager).tryStartChain(eq(player), eq(CHAIN_KEY));
    }

    @Test
    @DisplayName("Given a successful advanceChain, when the chain manager is verified, then advanceChain was called on it")
    public void advanceChain_delegatesToChainManager() {
        PlayerMock player = server.addPlayer("AdvanceDelegatePlayer");
        UUID playerUUID = player.getUniqueId();
        when(mockChainManager.advanceChain(eq(playerUUID), eq(QUEST_KEY))).thenReturn(true);

        orchestrator.advanceChain(playerUUID, QUEST_KEY);

        verify(mockChainManager).advanceChain(eq(playerUUID), eq(QUEST_KEY));
    }
}
