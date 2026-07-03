package us.eunoians.mcrpg.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.condition.CombatCondition;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
import us.eunoians.mcrpg.combat.task.CombatSessionTimeoutTask;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.event.combat.CombatParticipantRemoveEvent;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CombatSessionTimeoutTask")
class CombatSessionTimeoutTaskTest extends McRPGBaseTest {

    private CombatTrackerManager manager;
    private CombatSessionTimeoutTask timeoutTask;
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
        timeoutTask = new CombatSessionTimeoutTask(manager, 0.5);
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

        timeoutTask.onIntervalComplete();

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

        timeoutTask.onIntervalComplete();

        assertFalse(captured.isEmpty());
        assertEquals(CombatSessionEndReason.ALL_PARTICIPANTS_GONE, captured.get(0).getReason());
        assertFalse(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("sessions past the inactivity timeout are ended with TIMEOUT")
    void sessionPastTimeout_endsWithTimeout() {
        PlayerMock player = server.addPlayer();
        PlayerMock otherPlayer = server.addPlayer();

        manager.handleCombatInteraction(player.getUniqueId(), otherPlayer.getUniqueId(),
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("PLAYER"));

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

        timeoutTask.onIntervalComplete();

        boolean hasTimeout = captured.stream()
                .anyMatch(e -> e.getEntityUUID().equals(player.getUniqueId())
                        && e.getReason() == CombatSessionEndReason.TIMEOUT);
        assertTrue(hasTimeout);
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

        timeoutTask.onIntervalComplete();

        assertTrue(manager.hasActiveSession(player.getUniqueId()));
    }

    @Test
    @DisplayName("sessions within the timeout window are not ended")
    void sessionsWithinTimeout_areNotEnded() {
        PlayerMock player = server.addPlayer();
        UUID mobUUID = UUID.randomUUID();

        manager.handleCombatInteraction(player.getUniqueId(), mobUUID,
                new CustomEntityWrapper("PLAYER"), new CustomEntityWrapper("ZOMBIE"));

        // Don't advance time — session is fresh
        timeoutTask.onIntervalComplete();

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

        timeoutTask.onIntervalComplete();

        // Session should still have the fresh participant
        Optional<CombatSession> session = manager.getSession(player.getUniqueId());
        if (session.isPresent()) {
            assertTrue(session.get().hasParticipant(mob2));
        }
    }
}
