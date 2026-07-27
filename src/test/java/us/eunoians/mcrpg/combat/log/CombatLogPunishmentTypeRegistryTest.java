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

    @BeforeEach
    void setUp() {
        registry = new CombatLogPunishmentTypeRegistry();
    }

    @Test
    @DisplayName("register then get returns the same type")
    void register_getReturnsType() {
        registry.register(CombatLogPunishmentType.KILL_ON_LOGOUT);

        assertSame(CombatLogPunishmentType.KILL_ON_LOGOUT,
                registry.get(CombatLogPunishmentType.KILL_ON_LOGOUT.getKey()).orElse(null));
    }

    @Test
    @DisplayName("get returns empty for an unregistered key")
    void get_returnsEmpty_whenUnregistered() {
        assertTrue(registry.get(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @Test
    @DisplayName("multiple types with distinct keys coexist")
    void multipleTypes_coexist() {
        registry.register(CombatLogPunishmentType.KILL_ON_LOGOUT);
        registry.register(CombatLogPunishmentType.DROP_ITEMS);
        registry.register(CombatLogPunishmentType.BROADCAST_MESSAGE);

        assertSame(CombatLogPunishmentType.KILL_ON_LOGOUT,
                registry.get(CombatLogPunishmentType.KILL_ON_LOGOUT.getKey()).orElse(null));
        assertSame(CombatLogPunishmentType.DROP_ITEMS,
                registry.get(CombatLogPunishmentType.DROP_ITEMS.getKey()).orElse(null));
        assertSame(CombatLogPunishmentType.BROADCAST_MESSAGE,
                registry.get(CombatLogPunishmentType.BROADCAST_MESSAGE.getKey()).orElse(null));
    }

    @Test
    @DisplayName("registered reflects registration state by key")
    void registered_reflectsRegistrationState() {
        assertFalse(registry.registered(CombatLogPunishmentType.KILL_ON_LOGOUT));

        registry.register(CombatLogPunishmentType.KILL_ON_LOGOUT);

        assertTrue(registry.registered(CombatLogPunishmentType.KILL_ON_LOGOUT));
    }
}
