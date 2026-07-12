package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.util.TimeProvider;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
import us.eunoians.mcrpg.event.combat.CombatParticipantAddEvent;
import us.eunoians.mcrpg.event.combat.CombatParticipantRemoveEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionStartEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CombatSessionTimeoutTask")
class CombatSessionTimeoutTaskTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();
        CombatTestSupport.mockCombatConfig(mcRPG, 8.0, 16, 0.5);
        manager = new CombatTrackerManager(mcRPG);
    }

    /**
     * Cleans up state that MockBukkit and the shared registries keep across test methods: anonymous
     * combat-event listeners registered by individual tests, and combat conditions registered into
     * the shared {@link CombatConditionRegistry}. Both would otherwise leak into later tests (in this
     * class or others). Production registers no listeners for these custom events and ships no
     * built-in conditions, so this cleanup is safe.
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

    @Test
    @DisplayName("timed-out participants are removed from sessions")
    void timedOutParticipants_areRemoved() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
        assertTrue(session.hasParticipant(mobUUID));

        // Advance time past the timeout
        long currentMillis = timeProvider.now().toEpochMilli();
        Instant futureInstant = Instant.ofEpochMilli(currentMillis + 9000);
        when(timeProvider.now()).thenReturn(futureInstant);

        List<CombatParticipantRemoveEvent> captured = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onRemove(CombatParticipantRemoveEvent event) {
                captured.add(event);
            }
        }, mcRPG);

        manager.scanSessionsForTimeout();

        assertFalse(captured.isEmpty());
        assertEquals(ParticipantRemovalReason.TIMEOUT, captured.get(0).getReason());
    }

    @Test
    @DisplayName("sessions with empty rosters after participant timeout end with ALL_PARTICIPANTS_GONE")
    void emptyRosterAfterTimeout_endsSession() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        long currentMillis = timeProvider.now().toEpochMilli();
        Instant futureInstant = Instant.ofEpochMilli(currentMillis + 9000);
        when(timeProvider.now()).thenReturn(futureInstant);

        List<CombatSessionEndEvent> captured = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onEnd(CombatSessionEndEvent event) {
                captured.add(event);
            }
        }, mcRPG);

        manager.scanSessionsForTimeout();

        assertFalse(captured.isEmpty());
        assertEquals(CombatSessionEndReason.ALL_PARTICIPANTS_GONE, captured.get(0).getReason());
        assertFalse(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("an empty condition-created session past its timeout ends with TIMEOUT")
    void emptyConditionSessionPastTimeout_endsWithTimeout() {
        PlayerMock player = server.addPlayer();

        // Condition-created sessions carry no participants. With no condition holding it open, such a
        // session reaches the session-level timeout and ends with TIMEOUT (not ALL_PARTICIPANTS_GONE,
        // which only fires when a participant removal empties the roster).
        manager.reportConditionActivity(player.getUniqueId(), new NamespacedKey("mcrpg", "proximity"));

        long currentMillis = timeProvider.now().toEpochMilli();
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(currentMillis + 9000));

        List<CombatSessionEndEvent> captured = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onEnd(CombatSessionEndEvent event) {
                captured.add(event);
            }
        }, mcRPG);

        manager.scanSessionsForTimeout();

        assertFalse(captured.isEmpty());
        assertEquals(CombatSessionEndReason.TIMEOUT, captured.get(0).getReason());
        assertEquals(player.getUniqueId(), captured.get(0).getEntityUUID());
        assertFalse(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("sessions past timeout are held open when a condition returns true")
    void sessionHeldOpen_whenConditionReturnsTrue() {
        PlayerMock player = server.addPlayer();
        PlayerMock otherPlayer = server.addPlayer();

        manager.handleCombatInteraction(player.getUniqueId(), otherPlayer.getUniqueId(),
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("PLAYER"));

        // Register a condition that holds the session open
        CombatConditionRegistry conditionRegistry = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);
        CombatCondition holdOpenCondition = mock(CombatCondition.class);
        when(holdOpenCondition.getKey()).thenReturn(new NamespacedKey("mcrpg", "hold_open"));
        when(holdOpenCondition.getCheckIntervalSeconds()).thenReturn(1.0);
        when(holdOpenCondition.isInCombat(any(LivingEntity.class))).thenReturn(true);
        when(holdOpenCondition.getExpansionKey()).thenReturn(Optional.empty());
        conditionRegistry.register(holdOpenCondition);

        long currentMillis = timeProvider.now().toEpochMilli();
        Instant futureInstant = Instant.ofEpochMilli(currentMillis + 9000);
        when(timeProvider.now()).thenReturn(futureInstant);

        manager.scanSessionsForTimeout();

        assertTrue(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("emptied sessions still end when a registered condition returns false")
    void sessionEnds_whenConditionReturnsFalse() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        CombatConditionRegistry conditionRegistry = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);
        CombatCondition idleCondition = mock(CombatCondition.class);
        when(idleCondition.getKey()).thenReturn(new NamespacedKey("mcrpg", "idle_condition"));
        when(idleCondition.getCheckIntervalSeconds()).thenReturn(1.0);
        when(idleCondition.isInCombat(any(LivingEntity.class))).thenReturn(false);
        when(idleCondition.getExpansionKey()).thenReturn(Optional.empty());
        conditionRegistry.register(idleCondition);

        long currentMillis = timeProvider.now().toEpochMilli();
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(currentMillis + 9000));

        manager.scanSessionsForTimeout();

        assertFalse(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("a condition that throws does not abort the timeout scan")
    void scanCompletes_whenConditionThrows() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        CombatConditionRegistry conditionRegistry = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_CONDITION);
        CombatCondition throwingCondition = mock(CombatCondition.class);
        when(throwingCondition.getKey()).thenReturn(new NamespacedKey("mcrpg", "throwing_condition"));
        when(throwingCondition.getCheckIntervalSeconds()).thenReturn(1.0);
        when(throwingCondition.isInCombat(any(LivingEntity.class)))
                .thenThrow(new RuntimeException("condition failure"));
        when(throwingCondition.getExpansionKey()).thenReturn(Optional.empty());
        conditionRegistry.register(throwingCondition);

        long currentMillis = timeProvider.now().toEpochMilli();
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(currentMillis + 9000));

        // The throwing condition is caught and skipped, so the scan completes and the timed-out
        // session is ended rather than being wrongly held open.
        manager.scanSessionsForTimeout();

        assertFalse(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("sessions within the timeout window are not ended")
    void sessionsWithinTimeout_areNotEnded() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        // Don't advance time — session is fresh
        manager.scanSessionsForTimeout();

        assertTrue(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("fresh participants are not removed even when session is near timeout")
    void freshParticipants_notRemoved_whenSessionNearTimeout() {
        PlayerMock player = server.addPlayer();
        UUID mob1 = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mob1,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        // Advance time to just below the timeout
        long currentMillis = timeProvider.now().toEpochMilli();
        Instant nearTimeout = Instant.ofEpochMilli(currentMillis + 7000);
        when(timeProvider.now()).thenReturn(nearTimeout);

        // Add a fresh participant
        UUID mob2 = UUID.randomUUID();
        manager.handleCombatInteraction(player.getUniqueId(), mob2,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("SKELETON"));

        // Advance time past original mob's timeout but not the new one's
        Instant pastFirstTimeout = Instant.ofEpochMilli(currentMillis + 8500);
        when(timeProvider.now()).thenReturn(pastFirstTimeout);

        manager.scanSessionsForTimeout();

        // The session must survive: the timed-out mob1 is removed, but the fresh mob2 keeps it alive.
        CombatSession session = manager.getSession(player.getUniqueId()).orElseThrow();
        assertTrue(session.hasParticipant(mob2));
        assertFalse(session.hasParticipant(mob1));
    }
}
