package us.eunoians.mcrpg.ability;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.ability.AbilityRegisterEvent;
import us.eunoians.mcrpg.event.ability.AbilityUnregisterEvent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbilityRegistryTest {

    private static ServerMock server;
    private AbilityRegistry registry;

    @BeforeAll
    static void setupServer() {
        server = MockBukkit.mock();
    }

    @AfterAll
    static void teardownServer() {
        MockBukkit.unmock();
    }

    @BeforeEach
    void createRegistry() {
        McRPG mockMcRPG = mock(McRPG.class);
        registry = new AbilityRegistry(mockMcRPG);
    }

    private Ability createMockAbility(String name) {
        Ability ability = mock(Ability.class);
        NamespacedKey key = NamespacedKey.fromString("mcrpg:" + name);
        when(ability.getAbilityKey()).thenReturn(key);
        return ability;
    }

    @Nested
    @DisplayName("Soft-disable and re-enable")
    class SoftDisableReEnable {

        @Test
        @DisplayName("softDisableAbility removes from active registry")
        void softDisableAbility_removesFromActiveRegistry() {
            Ability ability = createMockAbility("test_ability");
            registry.register(ability);
            assertTrue(registry.registered(ability.getAbilityKey()));

            registry.softDisableAbility(ability.getAbilityKey());

            assertFalse(registry.registered(ability.getAbilityKey()));
        }

        @Test
        @DisplayName("softDisableAbility adds to soft-disabled map")
        void softDisableAbility_addsToSoftDisabledMap() {
            Ability ability = createMockAbility("test_ability");
            registry.register(ability);

            registry.softDisableAbility(ability.getAbilityKey());

            assertTrue(registry.getSoftDisabledAbilities().containsKey(ability.getAbilityKey()));
            assertSame(ability, registry.getSoftDisabledAbilities().get(ability.getAbilityKey()));
        }

        @Test
        @DisplayName("reEnableAbility restores to active registry")
        void reEnableAbility_restoresToActiveRegistry() {
            Ability ability = createMockAbility("test_ability");
            registry.register(ability);
            registry.softDisableAbility(ability.getAbilityKey());

            boolean result = registry.reEnableAbility(ability.getAbilityKey());

            assertTrue(result);
            assertTrue(registry.registered(ability.getAbilityKey()));
            assertFalse(registry.getSoftDisabledAbilities().containsKey(ability.getAbilityKey()));
        }

        @Test
        @DisplayName("reEnableAbility returns false for non-existent key")
        void reEnableAbility_returnsFalse_whenKeyNotSoftDisabled() {
            NamespacedKey unknownKey = NamespacedKey.fromString("mcrpg:nonexistent");

            boolean result = registry.reEnableAbility(unknownKey);

            assertFalse(result);
        }

        @Test
        @DisplayName("softDisableAbility is no-op for unregistered key")
        void softDisableAbility_noOp_whenKeyNotRegistered() {
            NamespacedKey unknownKey = NamespacedKey.fromString("mcrpg:nonexistent");

            registry.softDisableAbility(unknownKey);

            assertTrue(registry.getSoftDisabledAbilities().isEmpty());
        }

        @Test
        @DisplayName("softDisableAbility fires AbilityUnregisterEvent with SOFT_DISABLE reason")
        void softDisableAbility_firesUnregisterEvent() {
            Ability ability = createMockAbility("event_test");
            registry.register(ability);

            List<AbilityUnregisterEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onUnregister(AbilityUnregisterEvent event) {
                    captured.add(event);
                }
            }, MockBukkit.createMockPlugin());

            registry.softDisableAbility(ability.getAbilityKey());

            assertFalse(captured.isEmpty());
            AbilityUnregisterEvent event = captured.getLast();
            assertEquals(AbilityUnregisterEvent.UnregisterReason.SOFT_DISABLE, event.getReason());
            assertSame(ability, event.getAbility());
        }

        @Test
        @DisplayName("reEnableAbility fires AbilityRegisterEvent")
        void reEnableAbility_firesRegisterEvent() {
            Ability ability = createMockAbility("event_test_reenable");
            registry.register(ability);
            registry.softDisableAbility(ability.getAbilityKey());

            List<AbilityRegisterEvent> captured = new ArrayList<>();
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onRegister(AbilityRegisterEvent event) {
                    captured.add(event);
                }
            }, MockBukkit.createMockPlugin());

            registry.reEnableAbility(ability.getAbilityKey());

            assertFalse(captured.isEmpty());
            AbilityRegisterEvent event = captured.getLast();
            assertSame(ability, event.getAbility());
        }
    }
}
