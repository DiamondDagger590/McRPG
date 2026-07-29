package us.eunoians.mcrpg.combat.log;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatLogPunishmentTypeRegistry")
class CombatLogPunishmentTypeRegistryTest {

    private CombatLogPunishmentTypeRegistry registry;
    private KillOnLogoutPunishment killOnLogout;
    private DropItemsPunishment dropItems;
    private BroadcastMessagePunishment broadcastMessage;

    @BeforeEach
    void setUp() {
        registry = new CombatLogPunishmentTypeRegistry();
        killOnLogout = new KillOnLogoutPunishment((NamespacedKey) null);
        dropItems = new DropItemsPunishment((NamespacedKey) null);
        broadcastMessage = new BroadcastMessagePunishment((NamespacedKey) null);
    }

    @Test
    @DisplayName("register then get returns the same type")
    void register_getReturnsType() {
        registry.register(killOnLogout);

        assertSame(killOnLogout,
                registry.get(KillOnLogoutPunishment.KEY).orElse(null));
    }

    @Test
    @DisplayName("get returns empty for an unregistered key")
    void get_returnsEmpty_whenUnregistered() {
        assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("multiple types with distinct keys coexist")
    void multipleTypes_coexist() {
        registry.register(killOnLogout);
        registry.register(dropItems);
        registry.register(broadcastMessage);

        assertSame(killOnLogout,
                registry.get(KillOnLogoutPunishment.KEY).orElse(null));
        assertSame(dropItems,
                registry.get(DropItemsPunishment.KEY).orElse(null));
        assertSame(broadcastMessage,
                registry.get(BroadcastMessagePunishment.KEY).orElse(null));
    }

    @Test
    @DisplayName("registered reflects registration state by key")
    void registered_reflectsRegistrationState() {
        assertFalse(registry.registered(killOnLogout));

        registry.register(killOnLogout);

        assertTrue(registry.registered(killOnLogout));
    }
}
