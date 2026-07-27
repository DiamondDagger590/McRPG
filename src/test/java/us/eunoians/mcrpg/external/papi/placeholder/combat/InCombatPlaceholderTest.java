package us.eunoians.mcrpg.external.papi.placeholder.combat;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatTrackerManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("InCombatPlaceholder")
class InCombatPlaceholderTest extends McRPGBaseTest {

    private CombatTrackerManager combatTrackerManager;
    private InCombatPlaceholder placeholder;

    @BeforeEach
    void setUp() {
        combatTrackerManager = mock(CombatTrackerManager.class);
        mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(combatTrackerManager);
        placeholder = new InCombatPlaceholder();
    }

    @Test
    @DisplayName("returns true when the player has an active combat session")
    void parsePlaceholder_returnsTrue_whenActiveSession() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        when(combatTrackerManager.hasActiveSession(uuid)).thenReturn(true);

        assertEquals("true", placeholder.parsePlaceholder(offlinePlayer));
    }

    @Test
    @DisplayName("returns false when the player has no active combat session")
    void parsePlaceholder_returnsFalse_whenNoActiveSession() {
        UUID uuid = UUID.randomUUID();
        OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
        when(offlinePlayer.getUniqueId()).thenReturn(uuid);
        when(combatTrackerManager.hasActiveSession(uuid)).thenReturn(false);

        assertEquals("false", placeholder.parsePlaceholder(offlinePlayer));
    }
}
