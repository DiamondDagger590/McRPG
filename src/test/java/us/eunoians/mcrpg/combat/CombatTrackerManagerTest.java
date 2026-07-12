package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CombatTrackerManager")
class CombatTrackerManagerTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();
        CombatTestSupport.mockCombatConfig(mcRPG, 8.0, 16, 0.5);
        manager = new CombatTrackerManager(mcRPG);
    }

    /**
     * Cleans up state that MockBukkit and the shared registries keep across test methods:
     * <ul>
     *   <li>Anonymous combat-event listeners registered by individual tests — without this a listener
     *       that cancels {@link CombatParticipantAddEvent} or {@link CombatSessionStartEvent} leaks
     *       into later tests. Production registers no listeners for these custom events, so
     *       unregistering their handler lists is safe.</li>
     *   <li>Combat conditions registered into the shared {@link CombatConditionRegistry} — a leaked
     *       condition would be consulted by later tests' timeout scans. McRPG ships no built-in
     *       conditions, so clearing the registry is safe.</li>
     * </ul>
     */
    @AfterEach
    void cleanUpSharedCombatState() {
        CombatSessionStartEvent.getHandlerList().unregister(mcRPG);
        CombatSessionEndEvent.getHandlerList().unregister(mcRPG);
        CombatParticipantAddEvent.getHandlerList().unregister(mcRPG);
        CombatParticipantRemoveEvent.getHandlerList().unregister(mcRPG);

        CombatConditionRegistry conditionRegistry = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);
        for (NamespacedKey key : conditionRegistry.getRegisteredKeys()) {
            conditionRegistry.unregister(key);
        }
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
        void handleCombatInteraction_createsSession_whenSourceIsPlayer() {
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

            // Advance the clock so a genuine refresh is observable (the fixed clock would otherwise
            // make this assertion pass whether or not the timestamp is actually updated).
            long later = firstInteraction + 3000;
            when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(later));

            manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            assertEquals(later, participant.getLastInteractionMillis());
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
        void handleCombatInteraction_firesEvictionEvent_whenMobQueueFull() {
            YamlDocument combatConfig = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.COMBAT_CONFIG);
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
            CombatParticipantRemoveEvent evictionEvent = captured.get(0);
            assertEquals(ParticipantRemovalReason.EVICTION, evictionEvent.getReason());
            // A mob-for-mob eviction leaves the player count unchanged, so the type is PVE either side.
            assertEquals(CombatType.PVE, evictionEvent.getPreviousCombatType());
            assertEquals(CombatType.PVE, evictionEvent.getNewCombatType());
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
        void endSession_isNoOp_whenNoSession() {
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
        @DisplayName("removes participant from surviving sessions and keeps their other participants")
        void removesFromAllSessions() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();
            UUID sharedMob = UUID.randomUUID();
            UUID otherMob1 = UUID.randomUUID();
            UUID otherMob2 = UUID.randomUUID();

            // Give each session a second participant so removing the shared mob does not empty it.
            manager.handleCombatInteraction(player1.getUniqueId(), sharedMob,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player1.getUniqueId(), otherMob1,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));
            manager.handleCombatInteraction(player2.getUniqueId(), sharedMob,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player2.getUniqueId(), otherMob2,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            manager.removeParticipantFromAllSessions(sharedMob, ParticipantRemovalReason.DEATH);

            CombatSession session1 = manager.getSession(player1.getUniqueId()).orElseThrow();
            CombatSession session2 = manager.getSession(player2.getUniqueId()).orElseThrow();
            assertFalse(session1.hasParticipant(sharedMob));
            assertTrue(session1.hasParticipant(otherMob1));
            assertFalse(session2.hasParticipant(sharedMob));
            assertTrue(session2.hasParticipant(otherMob2));
        }

        @Test
        @DisplayName("ends sessions that the removed participant empties")
        void endsSessions_whenParticipantWasSoleMember() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();
            UUID mobUUID = UUID.randomUUID();

            manager.handleCombatInteraction(player1.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player2.getUniqueId(), mobUUID,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

            manager.removeParticipantFromAllSessions(mobUUID, ParticipantRemovalReason.DEATH);

            assertFalse(manager.hasActiveSession(player1.getUniqueId()));
            assertFalse(manager.hasActiveSession(player2.getUniqueId()));
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
        @DisplayName("ends all active sessions and cancels all condition tasks")
        void endsAllSessionsAndCancelsTasks() {
            PlayerMock player1 = server.addPlayer();
            PlayerMock player2 = server.addPlayer();

            manager.handleCombatInteraction(player1.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player2.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            // Start a mocked condition task so its cancellation on shutdown is observable.
            CombatCondition condition = mock(CombatCondition.class);
            when(condition.getKey()).thenReturn(new NamespacedKey("mcrpg", "shutdown_condition"));
            when(condition.getCheckIntervalSeconds()).thenReturn(1.0);
            CombatConditionTask mockTask = mock(CombatConditionTask.class);
            when(condition.createTask(any(), any())).thenReturn(mockTask);
            manager.startConditionTask(condition);

            manager.shutdown();

            assertFalse(manager.hasActiveSession(player1.getUniqueId()));
            assertFalse(manager.hasActiveSession(player2.getUniqueId()));
            verify(mockTask).cancelTask();
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

            verify(mockTask).runTask();
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

            verify(mockTask).cancelTask();
        }

        @Test
        @DisplayName("startConditionTask cancels the previous task when called twice for the same key")
        void startConditionTask_cancelsPreviousTask_whenCalledTwice() {
            CombatCondition condition = mock(CombatCondition.class);
            NamespacedKey condKey = new NamespacedKey("mcrpg", "double_start_condition");
            when(condition.getKey()).thenReturn(condKey);
            when(condition.getCheckIntervalSeconds()).thenReturn(1.0);
            CombatConditionTask firstTask = mock(CombatConditionTask.class);
            CombatConditionTask secondTask = mock(CombatConditionTask.class);
            when(condition.createTask(any(), any())).thenReturn(firstTask, secondTask);

            manager.startConditionTask(condition);
            manager.startConditionTask(condition);

            verify(firstTask).cancelTask();
        }
    }

    @Nested
    @DisplayName("Configuration validation")
    class ConfigurationValidation {

        @Test
        @DisplayName("getMaxMobParticipants clamps a configured value below 1 up to 1")
        void getMaxMobParticipants_clampsToOne_whenConfiguredBelowOne() {
            YamlDocument config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.FILE).getFile(FileType.COMBAT_CONFIG);
            when(config.getInt(CombatConfigFile.MAX_MOB_PARTICIPANTS)).thenReturn(0);

            assertEquals(1, manager.getMaxMobParticipants());
        }
    }

    @Nested
    @DisplayName("removeParticipantFromSession")
    class RemoveParticipantFromSession {

        @Test
        @DisplayName("removes a single participant and fires a remove event with the given reason")
        void removesParticipant_firesEvent_whenPresent() {
            PlayerMock player = server.addPlayer();
            UUID mob1 = UUID.randomUUID();
            UUID mob2 = UUID.randomUUID();
            manager.handleCombatInteraction(player.getUniqueId(), mob1,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            manager.handleCombatInteraction(player.getUniqueId(), mob2,
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

            List<CombatParticipantRemoveEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onRemove(CombatParticipantRemoveEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            Optional<CombatParticipant> removed = manager.removeParticipantFromSession(
                    player.getUniqueId(), mob1, ParticipantRemovalReason.PLUGIN);

            assertTrue(removed.isPresent());
            assertEquals(mob1, removed.get().getUUID());
            assertEquals(1, captured.size());
            assertEquals(ParticipantRemovalReason.PLUGIN, captured.get(0).getReason());
            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
            assertFalse(session.hasParticipant(mob1));
            assertTrue(session.hasParticipant(mob2));
        }

        @Test
        @DisplayName("returns empty when the owner has no session")
        void returnsEmpty_whenNoSession() {
            Optional<CombatParticipant> removed = manager.removeParticipantFromSession(
                    UUID.randomUUID(), UUID.randomUUID(), ParticipantRemovalReason.PLUGIN);

            assertTrue(removed.isEmpty());
        }
    }

    @Nested
    @DisplayName("Main-thread enforcement")
    class MainThreadEnforcement {

        @Test
        @DisplayName("a mutating call from a non-main thread throws IllegalStateException")
        void mutatingCall_throws_whenOffMainThread() throws InterruptedException {
            AtomicReference<Throwable> thrown = new AtomicReference<>();
            Thread offThread = new Thread(() -> {
                try {
                    manager.endSession(UUID.randomUUID(), CombatSessionEndReason.PLUGIN);
                } catch (Throwable t) {
                    thrown.set(t);
                }
            });

            offThread.start();
            offThread.join();

            assertNotNull(thrown.get());
            assertTrue(thrown.get() instanceof IllegalStateException);
        }
    }

    @Nested
    @DisplayName("reportCombatActivity / reportConditionActivity")
    class ReportActivity {

        @Test
        @DisplayName("reportCombatActivity creates a session when both entities are loaded")
        void reportCombatActivity_createsSession_whenBothEntitiesLoaded() {
            PlayerMock player = server.addPlayer();
            Zombie mob = spawnEntity(Zombie.class);

            manager.reportCombatActivity(player.getUniqueId(), mob.getUniqueId());

            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
            assertTrue(session.hasParticipant(mob.getUniqueId()));
        }

        @Test
        @DisplayName("reportConditionActivity creates an empty session carrying the condition key")
        void reportConditionActivity_createsEmptySession_withConditionKey() {
            PlayerMock player = server.addPlayer();
            NamespacedKey conditionKey = new NamespacedKey("mcrpg", "proximity");
            List<CombatSessionStartEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onStart(CombatSessionStartEvent event) {
                    captured.add(event);
                }
            }, mcRPG);

            manager.reportConditionActivity(player.getUniqueId(), conditionKey);

            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
            assertTrue(session.isEmpty());
            assertEquals(1, captured.size());
            assertEquals(conditionKey, captured.get(0).getTriggeringConditionKey().orElse(null));
        }

        @Test
        @DisplayName("reportConditionActivity refreshes an existing session's activity timer")
        void reportConditionActivity_refreshesExistingSession() {
            PlayerMock player = server.addPlayer();
            manager.handleCombatInteraction(player.getUniqueId(), UUID.randomUUID(),
                    new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));
            CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();

            long later = timeProvider.now().toEpochMilli() + 4000;
            when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(later));

            manager.reportConditionActivity(player.getUniqueId(), new NamespacedKey("mcrpg", "proximity"));

            assertEquals(later, session.getLastActivityMillis());
        }

        @Test
        @DisplayName("reportConditionActivity is a no-op when the player is offline")
        void reportConditionActivity_noOp_whenPlayerOffline() {
            UUID offlineUUID = UUID.randomUUID();

            manager.reportConditionActivity(offlineUUID, new NamespacedKey("mcrpg", "proximity"));

            assertFalse(manager.hasActiveSession(offlineUUID));
        }

        @Test
        @DisplayName("reportConditionActivity does not create a session when the start event is cancelled")
        void reportConditionActivity_respectsCancellation() {
            PlayerMock player = server.addPlayer();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onStart(CombatSessionStartEvent event) {
                    event.setCancelled(true);
                }
            }, mcRPG);

            manager.reportConditionActivity(player.getUniqueId(), new NamespacedKey("mcrpg", "proximity"));

            assertFalse(manager.hasActiveSession(player.getUniqueId()));
        }
    }
}
