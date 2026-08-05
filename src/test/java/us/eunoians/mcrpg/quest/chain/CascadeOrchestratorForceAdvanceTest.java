package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.chain.CascadeFinalizeEvent;
import us.eunoians.mcrpg.event.quest.chain.CascadeStartEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CascadeOrchestrator#forceAdvanceChain(UUID, NamespacedKey)},
 * cascade depth limit enforcement, and {@link CascadeOrchestrator#notifyStepStarted(UUID, NamespacedKey)}
 * during an active cascade.
 */
@DisplayName("CascadeOrchestrator extended coverage")
class CascadeOrchestratorForceAdvanceTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY_1 = new NamespacedKey("mcrpg", "test_quest_1");
    private static final NamespacedKey QUEST_KEY_2 = new NamespacedKey("mcrpg", "test_quest_2");

    private QuestChainManager mockChainManager;
    private McRPGPlayerManager mockPlayerManager;
    private CascadeOrchestrator orchestrator;

    @BeforeEach
    void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        mockPlayerManager = mock(McRPGPlayerManager.class);
        when(mockPlayerManager.getPlayer(any(UUID.class))).thenReturn(Optional.empty());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        mockChainManager = mock(QuestChainManager.class);
        orchestrator = new CascadeOrchestrator(mcRPG, mockChainManager);
    }

    @Nested
    @DisplayName("forceAdvanceChain")
    class ForceAdvanceChain {

        @Test
        @DisplayName("Given no McRPGPlayer found for the UUID, when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenPlayerNotFound() {
            UUID uuid = UUID.randomUUID();
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.empty());

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
            verify(mockChainManager, never()).advanceChain(any(), any());
        }

        @Test
        @DisplayName("Given player has no chain state for the key, when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenNoChainState() {
            UUID uuid = UUID.randomUUID();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
            verify(mockChainManager, never()).advanceChain(any(), any());
        }

        @Test
        @DisplayName("Given player's chain state is COMPLETED (not ACTIVE), when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenChainNotActive() {
            UUID uuid = UUID.randomUUID();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.COMPLETED, 1, null);
            chainData.putChainState(state);
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
            verify(mockChainManager, never()).advanceChain(any(), any());
        }

        @Test
        @DisplayName("Given player's chain state is ACTIVE but currentQuestKey is empty, when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenNoCurrentQuestKey() {
            UUID uuid = UUID.randomUUID();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.ACTIVE, 0, null);
            chainData.putChainState(state);
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
            verify(mockChainManager, never()).advanceChain(any(), any());
        }

        @Test
        @DisplayName("Given an ACTIVE chain with a current quest key, when forceAdvanceChain is called, then delegates to advanceChain with that quest key")
        void forceAdvanceChain_delegatesToAdvanceChain_whenConditionsMet() {
            PlayerMock playerMock = server.addPlayer("ForceAdvancePlayer");
            UUID uuid = playerMock.getUniqueId();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, QUEST_KEY_1, QuestChainState.ACTIVE, 0, null);
            chainData.putChainState(state);
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));
            when(mockChainManager.advanceChain(eq(uuid), eq(QUEST_KEY_1))).thenReturn(true);

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertTrue(result);
            verify(mockChainManager).advanceChain(eq(uuid), eq(QUEST_KEY_1));
        }

        @Test
        @DisplayName("Given player's chain state is ABANDONED, when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenChainAbandoned() {
            UUID uuid = UUID.randomUUID();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.ABANDONED, 0, null);
            chainData.putChainState(state);
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given player's chain state is FAILED, when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenChainFailed() {
            UUID uuid = UUID.randomUUID();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.FAILED, 0, null);
            chainData.putChainState(state);
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given player's chain state is EXPIRED, when forceAdvanceChain is called, then returns false")
        void forceAdvanceChain_returnsFalse_whenChainExpired() {
            UUID uuid = UUID.randomUUID();
            McRPGPlayer player = mock(McRPGPlayer.class);
            QuestChainPlayerData chainData = new QuestChainPlayerData();
            QuestChainPlayerState state = new QuestChainPlayerState(CHAIN_KEY, null, QuestChainState.EXPIRED, 0, null);
            chainData.putChainState(state);
            when(player.getChainData()).thenReturn(chainData);
            when(mockPlayerManager.getPlayer(uuid)).thenReturn(Optional.of(player));

            boolean result = orchestrator.forceAdvanceChain(uuid, CHAIN_KEY);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("advanceChain depth limit")
    class DepthLimit {

        @Test
        @DisplayName("Given depth limit is reached during cascade, when advanceChain is called, then returns false")
        void advanceChain_returnsFalse_whenDepthLimitReached() {
            PlayerMock playerMock = server.addPlayer("DepthLimitPlayer");
            UUID uuid = playerMock.getUniqueId();

            CascadeContext context = new CascadeContext(CHAIN_KEY);
            for (int i = 0; i < 50; i++) {
                context.recordAutoCompletedStep(
                        new NamespacedKey("mcrpg", "step_" + i),
                        "Step " + i);
            }

            CascadeOrchestrator spyOrchestrator = spy(orchestrator);

            try {
                var activeCascadesField = CascadeOrchestrator.class.getDeclaredField("activeCascades");
                activeCascadesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                var activeCascades = (java.util.Map<UUID, CascadeContext>) activeCascadesField.get(spyOrchestrator);
                activeCascades.put(uuid, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            boolean result = spyOrchestrator.advanceChain(uuid, QUEST_KEY_1);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given depth is below limit, when advanceChain is called during a cascade, then delegates to chain manager")
        void advanceChain_delegates_whenBelowDepthLimit() {
            PlayerMock playerMock = server.addPlayer("BelowLimitPlayer");
            UUID uuid = playerMock.getUniqueId();

            when(mockChainManager.advanceChain(eq(uuid), eq(QUEST_KEY_1))).thenReturn(true);

            CascadeContext context = new CascadeContext(CHAIN_KEY);
            for (int i = 0; i < 5; i++) {
                context.recordAutoCompletedStep(
                        new NamespacedKey("mcrpg", "step_" + i),
                        "Step " + i);
            }

            try {
                var activeCascadesField = CascadeOrchestrator.class.getDeclaredField("activeCascades");
                activeCascadesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                var activeCascades = (java.util.Map<UUID, CascadeContext>) activeCascadesField.get(orchestrator);
                activeCascades.put(uuid, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            boolean result = orchestrator.advanceChain(uuid, QUEST_KEY_1);

            assertTrue(result);
            verify(mockChainManager).advanceChain(eq(uuid), eq(QUEST_KEY_1));
        }
    }

    @Nested
    @DisplayName("notifyStepStarted during cascade")
    class NotifyStepStartedDuringCascade {

        @Test
        @DisplayName("Given an active cascade, when notifyStepStarted is called, then the cascade context records the quest key")
        void notifyStepStarted_recordsQuestKey_whenCascadeActive() {
            PlayerMock playerMock = server.addPlayer("NotifyPlayer");
            UUID uuid = playerMock.getUniqueId();
            when(mockChainManager.tryStartChain(eq(playerMock), eq(CHAIN_KEY))).thenAnswer(invocation -> {
                assertTrue(orchestrator.isInCascade(uuid));
                orchestrator.notifyStepStarted(uuid, QUEST_KEY_1);
                Optional<CascadeContext> ctx = orchestrator.getCascadeContext(uuid);
                assertTrue(ctx.isPresent());
                assertEquals(QUEST_KEY_1, ctx.get().getLastStartedQuestKey().orElse(null));
                return true;
            });

            boolean result = orchestrator.tryStartChain(playerMock, CHAIN_KEY);

            assertTrue(result);
            verify(mockChainManager).tryStartChain(eq(playerMock), eq(CHAIN_KEY));
        }
    }

    @Nested
    @DisplayName("finalizeCascade")
    class FinalizeCascade {

        @Test
        @DisplayName("Given null context (no cascade), when finalizeCascade is called, then returns silently")
        void finalizeCascade_returnsSilently_whenNoContext() {
            UUID uuid = UUID.randomUUID();

            orchestrator.finalizeCascade(uuid, null, us.eunoians.mcrpg.event.quest.chain.CascadeOutcome.SUCCESS);

            assertFalse(orchestrator.isInCascade(uuid));
        }

        @Test
        @DisplayName("Given a cascade completes, when finalizeCascade runs, then cascade is cleaned up and event fires")
        void finalizeCascade_cleansUpAndFiresEvent_whenCascadeCompletes() {
            PlayerMock playerMock = server.addPlayer("FinalizeNullPlayer");
            UUID uuid = playerMock.getUniqueId();
            when(mockChainManager.tryStartChain(eq(playerMock), eq(CHAIN_KEY))).thenAnswer(invocation -> {
                return true;
            });

            orchestrator.tryStartChain(playerMock, CHAIN_KEY);

            server.getPluginManager().assertEventFired(CascadeFinalizeEvent.class);
            assertFalse(orchestrator.isInCascade(uuid));
        }
    }
}
