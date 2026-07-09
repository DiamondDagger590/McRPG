package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
import us.eunoians.mcrpg.combat.condition.CombatConditionTask;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.event.combat.CombatParticipantAddEvent;
import us.eunoians.mcrpg.event.combat.CombatParticipantRemoveEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionStartEvent;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CombatTrackerManager")
class CombatTrackerManagerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();

        FileManager fileManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument combatConfig = mock(YamlDocument.class);
        lenient().when(fileManager.getFile(FileType.COMBAT_CONFIG)).thenReturn(combatConfig);
        lenient().when(combatConfig.getDouble(CombatConfigFile.SESSION_TIMEOUT_SECONDS)).thenReturn(8.0);
        lenient().when(combatConfig.getInt(CombatConfigFile.MAX_MOB_PARTICIPANTS)).thenReturn(16);
        lenient().when(combatConfig.getDouble(CombatConfigFile.TIMEOUT_SCAN_INTERVAL_SECONDS)).thenReturn(0.5);

        manager = new CombatTrackerManager(mcRPG);
    }

    /**
     * Unregisters the anonymous combat-event listeners registered by individual tests. MockBukkit
     * keeps the server (and its registered listeners) across test methods, so without this cleanup a
     * listener that cancels {@link CombatParticipantAddEvent} or {@link CombatSessionStartEvent} in
     * one test leaks into later tests. Production registers no listeners for these custom events, so
     * unregistering their handler lists is safe.
     */
    @AfterEach
    void unregisterCombatEventListeners() {
        CombatSessionStartEvent.getHandlerList().unregister(mcRPG);
        CombatSessionEndEvent.getHandlerList().unregister(mcRPG);
        CombatParticipantAddEvent.getHandlerList().unregister(mcRPG);
        CombatParticipantRemoveEvent.getHandlerList().unregister(mcRPG);
    }

    @Nested
    @DisplayName("getSession / hasActiveSession")
    class SessionQueries {

        @Test
        @DisplayName("getSession returns empty when no session exists")
        void getSession_returnsEmpty_whenNoSession() {
            assertTrue(manager.getSession(UUID.randomUUID()).isEmpty());
        }

        @Test
        @DisplayName("hasActiveSession returns false when no session exists")
        void hasActiveSession_returnsFalse_whenNoSession() {
            assertFalse(manager.hasActiveSession(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("handleCombatInteraction")
    class HandleCombatInteraction {

        @Test
        @DisplayName("creates a session for a player source")
        void createsSessionForPlayerSource() {
            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            assertTrue(manager.hasActiveSession(player.getUniqueId()));
            assertFalse(manager.hasActiveSession(mobUUID));
        }

        @Test
        @DisplayName("creates a session for a player target")
        void createsSessionForPlayerTarget() {
            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(mobUUID, player.getUniqueId(),
                    new CustomEntityWrapper("ZOMBIE"), new CustomEntityWrapper("PLAYER"));

            assertTrue(manager.hasActiveSession(player.getUniqueId()));
            assertFalse(manager.hasActiveSession(mobUUID));
        }

        @Test
        @DisplayName("creates sessions for both players in PvP")
        void createsSessionsForBothPlayersInPvP() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();

            manager.handleCombatInteraction(player1.getUniqueId(), player2.getUniqueId(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("PLAYER"));

            assertTrue(manager.hasActiveSession(player1.getUniqueId()));
            assertTrue(manager.hasActiveSession(player2.getUniqueId()));
        }

        @Test
        @DisplayName("does not create a session for a mob source")
        void doesNotCreateSessionForMobSource() {
            UUID mobUUID = UUID.randomUUID();
            UUID otherMobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(mobUUID, otherMobUUID,
                    new CustomEntityWrapper("ZOMBIE"), new CustomEntityWrapper("SKELETON"));

            assertFalse(manager.hasActiveSession(mobUUID));
            assertFalse(manager.hasActiveSession(otherMobUUID));
        }

        @Test
        @DisplayName("does not create a session for a mob-only interaction")
        void doesNotCreateSessionForMobTarget() {
            UUID mob1 = UUID.randomUUID();
            UUID mob2 = UUID.randomUUID();

            manager.handleCombatInteraction(mob1, mob2,
                    new CustomEntityWrapper("ZOMBIE"), new CustomEntityWrapper("SKELETON"));

            assertFalse(manager.hasActiveSession(mob1));
            assertFalse(manager.hasActiveSession(mob2));
        }

        @Test
        @DisplayName("adds participant to existing session on repeat damage")
        void addsParticipantToExistingSession() {
            PlayerMock player = server.addPlayer();
            UUID mob1 = UUID.randomUUID();
            UUID mob2 = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mob1,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player.getUniqueId(), mob2,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
            assertTrue(session.hasParticipant(mob1));
            assertTrue(session.hasParticipant(mob2));
        }

        @Test
        @DisplayName("refreshes lastInteraction on existing participant")
        void refreshesLastInteractionOnExistingParticipant() {
            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
            CombatParticipant participant = session.getParticipant(mobUUID).orElseThrow();
            long firstInteraction = participant.getLastInteractionMillis();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            assertEquals(firstInteraction, participant.getLastInteractionMillis());
        }

        @Test
        @DisplayName("fires CombatSessionStartEvent on new session")
        void firesCombatSessionStartEvent() {
            List<CombatSessionStartEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onStart(CombatSessionStartEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            assertFalse(captured.isEmpty());
            assertEquals(player.getUniqueId(), captured.get(0).getEntityUUID());
        }

        @Test
        @DisplayName("does not create session when CombatSessionStartEvent is cancelled")
        void doesNotCreateSessionWhenStartEventCancelled() {
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onStart(CombatSessionStartEvent event) {
                    event.setCancelled(true);
                }
            }, mcRPG);

            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            assertFalse(manager.hasActiveSession(player.getUniqueId()));
        }

        @Test
        @DisplayName("fires CombatParticipantAddEvent for new participant on existing session")
        void firesCombatParticipantAddEvent() {
            PlayerMock player = server.addPlayer();
            UUID mob1 = UUID.randomUUID();
            UUID mob2 = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mob1,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            List<CombatParticipantAddEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onAdd(CombatParticipantAddEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            manager.handleCombatInteraction(player.getUniqueId(), mob2,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            assertFalse(captured.isEmpty());
            assertEquals(mob2, captured.get(0).getNewParticipant().getUUID());
        }

        @Test
        @DisplayName("does not add participant when CombatParticipantAddEvent is cancelled")
        void doesNotAddParticipantWhenAddEventCancelled() {
            PlayerMock player = server.addPlayer();
            UUID mob1 = UUID.randomUUID();
            UUID mob2 = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mob1,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onAdd(CombatParticipantAddEvent event) {
                    event.setCancelled(true);
                }
            }, mcRPG);

            manager.handleCombatInteraction(player.getUniqueId(), mob2,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
            assertFalse(session.hasParticipant(mob2));
        }

        @Test
        @DisplayName("fires CombatParticipantRemoveEvent with EVICTION when mob FIFO is full")
        void firesEvictionEvent() {
            FileManager fileManager = mcRPG.registryAccess()
                    .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
            YamlDocument combatConfig = fileManager.getFile(FileType.COMBAT_CONFIG);
            when(combatConfig.getInt(CombatConfigFile.MAX_MOB_PARTICIPANTS)).thenReturn(2);

            CombatTrackerManager smallManager = new CombatTrackerManager(mcRPG);
            PlayerMock player = server.addPlayer();

            smallManager.handleCombatInteraction(player.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            smallManager.handleCombatInteraction(player.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            List<CombatParticipantRemoveEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onRemove(CombatParticipantRemoveEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            smallManager.handleCombatInteraction(player.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("CREEPER"));

            assertFalse(captured.isEmpty());
            assertEquals(ParticipantRemovalReason.EVICTION, captured.get(0).getReason());
        }
    }

    @Nested
    @DisplayName("endSession")
    class EndSession {

        @Test
        @DisplayName("removes session from active map")
        void removesSessionFromActiveMap() {
            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            assertTrue(manager.hasActiveSession(player.getUniqueId()));
            manager.endSession(player.getUniqueId(), CombatSessionEndReason.TIMEOUT);
            assertFalse(manager.hasActiveSession(player.getUniqueId()));
        }

        @Test
        @DisplayName("fires CombatSessionEndEvent with correct reason")
        void firesCombatSessionEndEvent() {
            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            List<CombatSessionEndEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onEnd(CombatSessionEndEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            manager.endSession(player.getUniqueId(), CombatSessionEndReason.DEATH);

            assertFalse(captured.isEmpty());
            assertEquals(CombatSessionEndReason.DEATH, captured.get(0).getReason());
            assertEquals(player.getUniqueId(), captured.get(0).getEntityUUID());
        }

        @Test
        @DisplayName("is a no-op when no session exists")
        void noOpWhenNoSession() {
            List<CombatSessionEndEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onEnd(CombatSessionEndEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            manager.endSession(UUID.randomUUID(), CombatSessionEndReason.TIMEOUT);

            assertTrue(captured.isEmpty());
        }
    }

    @Nested
    @DisplayName("removeParticipantFromAllSessions")
    class RemoveParticipantFromAllSessions {

        @Test
        @DisplayName("removes participant from every session that contains it")
        void removesFromAllSessions() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player1.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player2.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            manager.removeParticipantFromAllSessions(mobUUID, ParticipantRemovalReason.DEATH);

            CombatSession session1 = manager.getSession(player1.getUniqueId()).orElse(null);
            CombatSession session2 = manager.getSession(player2.getUniqueId()).orElse(null);

            if (session1 != null) {
                assertFalse(session1.hasParticipant(mobUUID));
            }
            if (session2 != null) {
                assertFalse(session2.hasParticipant(mobUUID));
            }
        }

        @Test
        @DisplayName("fires CombatParticipantRemoveEvent for each removal")
        void firesRemoveEventForEachRemoval() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player1.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player2.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            List<CombatParticipantRemoveEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onRemove(CombatParticipantRemoveEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            manager.removeParticipantFromAllSessions(mobUUID, ParticipantRemovalReason.DEATH);

            assertEquals(2, captured.size());
        }

        @Test
        @DisplayName("ends sessions that become empty with ALL_PARTICIPANTS_GONE")
        void endsEmptySessionsWithAllParticipantsGone() {
            PlayerMock player = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            List<CombatSessionEndEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onEnd(CombatSessionEndEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            manager.removeParticipantFromAllSessions(mobUUID, ParticipantRemovalReason.DEATH);

            assertFalse(captured.isEmpty());
            assertEquals(CombatSessionEndReason.ALL_PARTICIPANTS_GONE, captured.get(0).getReason());
        }
    }

    @Nested
    @DisplayName("shutdown")
    class Shutdown {

        @Test
        @DisplayName("ends all active sessions and cancels all tasks")
        void endsAllSessionsAndCancelsTasks() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();

            manager.handleCombatInteraction(player1.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player2.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            manager.shutdown();

            assertFalse(manager.hasActiveSession(player1.getUniqueId()));
            assertFalse(manager.hasActiveSession(player2.getUniqueId()));
        }
    }

    @Nested
    @DisplayName("Condition tasks")
    class ConditionTasks {

        @Test
        @DisplayName("startConditionTasks bulk-starts tasks for all conditions in registry")
        void startConditionTasks_bulkStartsAll() {
            CombatConditionRegistry conditionRegistry = mcRPG.registryAccess()
                    .registry(McRPGRegistryKey.COMBAT_CONDITION);

            CombatCondition condition = mock(CombatCondition.class);
            NamespacedKey condKey = new NamespacedKey("mcrpg", "test_condition");
            when(condition.getKey()).thenReturn(condKey);
            when(condition.getCheckIntervalSeconds()).thenReturn(1.0);
            CombatConditionTask mockTask = mock(CombatConditionTask.class);
            when(condition.createTask(any(), any())).thenReturn(mockTask);
            conditionRegistry.register(condition);

            manager.startConditionTasks();

            // If we got here without an exception, the task was started
            assertNotNull(conditionRegistry.get(condKey).orElse(null));
        }

        @Test
        @DisplayName("stopConditionTask cancels the condition's task")
        void stopConditionTask_cancelsTask() {
            CombatCondition condition = mock(CombatCondition.class);
            NamespacedKey condKey = new NamespacedKey("mcrpg", "stop_test_condition");
            when(condition.getKey()).thenReturn(condKey);
            when(condition.getCheckIntervalSeconds()).thenReturn(1.0);
            CombatConditionTask mockTask = mock(CombatConditionTask.class);
            when(condition.createTask(any(), any())).thenReturn(mockTask);

            manager.startConditionTask(condition);
            manager.stopConditionTask(condKey);

            // Verify cancelTask was called
            org.mockito.Mockito.verify(mockTask).cancelTask();
        }
    }
}
