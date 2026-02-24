package us.eunoians.mcrpg.quest.impl.scope.impl;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.external.lands.LandsHook;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LandQuestScopeTest extends McRPGBaseTest {

    private LandQuestScope scope;
    private UUID questUUID;

    @BeforeEach
    public void setup() {
        questUUID = UUID.randomUUID();
        scope = new LandQuestScope(questUUID, null);
    }

    @DisplayName("Given a new scope, when setting land name once, then it succeeds")
    @Test
    public void setLandName_succeedsOnce() {
        scope.setLandName("my_land");
        assertTrue(scope.getLandName().isPresent());
    }

    @DisplayName("Given a scope with land name set, when setting again, then it throws IllegalStateException")
    @Test
    public void setLandName_throwsOnSecondCall() {
        scope.setLandName("my_land");
        assertThrows(IllegalStateException.class, () -> scope.setLandName("other_land"));
    }

    @DisplayName("Given a scope without land name, when checking isScopeValid, then it returns false")
    @Test
    public void isScopeValid_returnsFalse_whenLandNameNull() {
        assertFalse(scope.isScopeValid());
    }

    @Nested
    @DisplayName("When Lands hook is not registered")
    class WhenLandsHookNotRegistered {

        @BeforeEach
        void setupWithLandName() {
            scope.setLandName("my_land");
        }

        @DisplayName("Given a scope with land name but no Lands hook, when checking isScopeValid, then it returns false")
        @Test
        void isScopeValid_returnsFalse() {
            assertFalse(scope.isScopeValid());
        }

        @DisplayName("Given a scope without Lands hook, when getting players, then it returns empty set")
        @Test
        void getCurrentPlayersInScope_returnsEmpty() {
            assertTrue(scope.getCurrentPlayersInScope().isEmpty());
        }

        @DisplayName("Given a scope without Lands hook, when checking player membership, then it returns false")
        @Test
        void isPlayerInScope_returnsFalse() {
            assertFalse(scope.isPlayerInScope(UUID.randomUUID()));
        }
    }

    @Nested
    @DisplayName("When Lands hook is registered")
    class WhenLandsHookRegistered {

        private LandsIntegration landsIntegration;

        @BeforeEach
        void setupMockedLandsHook() throws Exception {
            landsIntegration = mock(LandsIntegration.class);
            LandsHook landsHook = mock(LandsHook.class);
            when(landsHook.getLandsIntegration()).thenReturn(landsIntegration);
            registerMockLandsHook(landsHook);
            scope.setLandName("my_land");
        }

        @DisplayName("Given land exists, when checking isScopeValid, then it returns true")
        @Test
        void isScopeValid_returnsTrue_whenLandExists() {
            Land land = mock(Land.class);
            when(landsIntegration.getLandByName("my_land")).thenReturn(land);
            assertTrue(scope.isScopeValid());
        }

        @DisplayName("Given land does not exist, when checking isScopeValid, then it returns false")
        @Test
        void isScopeValid_returnsFalse_whenLandDoesNotExist() {
            when(landsIntegration.getLandByName("my_land")).thenReturn(null);
            assertFalse(scope.isScopeValid());
        }

        @DisplayName("Given land exists with trusted players, when getting players in scope, then it returns them")
        @Test
        void getCurrentPlayersInScope_returnsTrustedPlayers() {
            UUID player1 = UUID.randomUUID();
            UUID player2 = UUID.randomUUID();
            Land land = mock(Land.class);
            when(land.getTrustedPlayers()).thenReturn(List.of(player1, player2));
            when(landsIntegration.getLandByName("my_land")).thenReturn(land);

            Set<UUID> players = scope.getCurrentPlayersInScope();
            assertEquals(Set.of(player1, player2), players);
        }

        @DisplayName("Given land does not exist, when getting players in scope, then it returns empty set")
        @Test
        void getCurrentPlayersInScope_returnsEmpty_whenLandDoesNotExist() {
            when(landsIntegration.getLandByName("my_land")).thenReturn(null);
            assertTrue(scope.getCurrentPlayersInScope().isEmpty());
        }

        @DisplayName("Given land exists and player is trusted, when checking membership, then it returns true")
        @Test
        void isPlayerInScope_returnsTrue_whenPlayerTrusted() {
            UUID playerUUID = UUID.randomUUID();
            Land land = mock(Land.class);
            when(land.isTrusted(playerUUID)).thenReturn(true);
            when(landsIntegration.getLandByName("my_land")).thenReturn(land);

            assertTrue(scope.isPlayerInScope(playerUUID));
        }

        @DisplayName("Given land exists and player is not trusted, when checking membership, then it returns false")
        @Test
        void isPlayerInScope_returnsFalse_whenPlayerNotTrusted() {
            UUID playerUUID = UUID.randomUUID();
            Land land = mock(Land.class);
            when(land.isTrusted(playerUUID)).thenReturn(false);
            when(landsIntegration.getLandByName("my_land")).thenReturn(land);

            assertFalse(scope.isPlayerInScope(playerUUID));
        }

        @DisplayName("Given land does not exist, when checking membership, then it returns false")
        @Test
        void isPlayerInScope_returnsFalse_whenLandDoesNotExist() {
            UUID playerUUID = UUID.randomUUID();
            when(landsIntegration.getLandByName("my_land")).thenReturn(null);

            assertFalse(scope.isPlayerInScope(playerUUID));
        }

        /**
         * Registers a mocked {@link LandsHook} into the {@link PluginHookRegistry} by
         * directly inserting it under the {@code LandsHook.class} key. This is necessary
         * because the registry keys by {@code getClass()}, and Mockito mocks produce a
         * subclass that won't match the {@code LandsHook.class} lookup key.
         */
        private void registerMockLandsHook(LandsHook mock) throws Exception {
            PluginHookRegistry hookRegistry = mcRPG.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
            Field hooksField = PluginHookRegistry.class.getDeclaredField("hooks");
            hooksField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Class<?>, PluginHook<?>> hooks = (Map<Class<?>, PluginHook<?>>) hooksField.get(hookRegistry);
            hooks.put(LandsHook.class, mock);
        }
    }
}
