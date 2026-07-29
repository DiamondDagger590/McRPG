package us.eunoians.mcrpg.external.papi.placeholder.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.TimeProvider;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CombatSecondsRemainingPlaceholder")
class CombatSecondsRemainingPlaceholderTest extends McRPGBaseTest {

    private CombatTrackerManager combatTrackerManager;
    private CombatSecondsRemainingPlaceholder placeholder;
    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        combatTrackerManager = mock(CombatTrackerManager.class);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(combatTrackerManager);
        placeholder = new CombatSecondsRemainingPlaceholder();
        timeProvider = McRPG.getInstance().getTimeProvider();
    }

    private OfflinePlayer offlinePlayer(UUID uuid) {
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        return offlinePlayer;
    }

    @Test
    @DisplayName("returns 0.0 when the player has no active combat session")
    void parsePlaceholder_returnsZero_whenNoActiveSession() {
        UUID uuid = UUID.randomUUID();
        when(combatTrackerManager.getSession(uuid)).thenReturn(Optional.empty());

        assertEquals("0.0", placeholder.parsePlaceholder(offlinePlayer(uuid)));
    }

    @Test
    @DisplayName("returns the full timeout when the session was just started")
    void parsePlaceholder_returnsFullTimeout_whenJustStarted() {
        UUID uuid = UUID.randomUUID();
        long nowMillis = timeProvider.now().toEpochMilli();
        CombatSession session = new CombatSession(uuid, 16, 8000L);
        when(combatTrackerManager.getSession(uuid)).thenReturn(Optional.of(session));

        assertEquals("8.0", placeholder.parsePlaceholder(offlinePlayer(uuid)));
    }

    @Test
    @DisplayName("returns remaining seconds formatted to one decimal when partially elapsed")
    void parsePlaceholder_returnsPartialRemaining() {
        UUID uuid = UUID.randomUUID();
        CombatSession session = new CombatSession(uuid, 16, 8000L);
        when(combatTrackerManager.getSession(uuid)).thenReturn(Optional.of(session));

        long startMillis = timeProvider.now().toEpochMilli();
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(startMillis + 4000));

        assertEquals("4.0", placeholder.parsePlaceholder(offlinePlayer(uuid)));
    }

    @Test
    @DisplayName("returns 0.0 when the session has already exceeded its timeout")
    void parsePlaceholder_returnsZero_whenTimeoutExceeded() {
        UUID uuid = UUID.randomUUID();
        CombatSession session = new CombatSession(uuid, 16, 8000L);
        when(combatTrackerManager.getSession(uuid)).thenReturn(Optional.of(session));

        long startMillis = timeProvider.now().toEpochMilli();
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(startMillis + 20000));

        assertEquals("0.0", placeholder.parsePlaceholder(offlinePlayer(uuid)));
    }
}
