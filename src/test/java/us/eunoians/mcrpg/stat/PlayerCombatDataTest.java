package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerCombatDataTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = new NamespacedKey("test", "mana");
    private static final NamespacedKey HP_KEY = new NamespacedKey("test", "hp");

    private PlayerCombatData data;

    @BeforeEach
    void setUp() {
        CombatStatRegistry registry = new CombatStatRegistry();
        registry.register(new ResourcePoolCombatStat(HP_KEY, "HP", "❤", 200, 0));
        registry.register(new ResourcePoolCombatStat(MANA_KEY, "Mana", "✦", 100, 10));

        data = new PlayerCombatData();
        data.initFromRegistry(registry);
    }

    @DisplayName("getInstance returns initialized instances for all registered stats")
    @Test
    void getInstanceReturnsInitialized() {
        assertTrue(data.getInstance(HP_KEY).isPresent());
        assertTrue(data.getInstance(MANA_KEY).isPresent());
        assertEquals(200, data.getInstance(HP_KEY).orElseThrow().getCurrent());
        assertEquals(100, data.getInstance(MANA_KEY).orElseThrow().getCurrent());
    }

    @DisplayName("getInstance returns empty for unregistered key")
    @Test
    void getInstanceReturnsEmptyForUnknown() {
        assertTrue(data.getInstance(new NamespacedKey("test", "unknown")).isEmpty());
    }

    @DisplayName("tickRegen ticks all resource pool stats")
    @Test
    void tickRegenTicksAll() {
        data.getInstance(MANA_KEY).orElseThrow().consume(50);
        data.tickRegen(2.0);
        // 50 + (10 * 2.0) = 70
        assertEquals(70, data.getInstance(MANA_KEY).orElseThrow().getCurrent());
        // HP has 0 regen, should stay at 200
        assertEquals(200, data.getInstance(HP_KEY).orElseThrow().getCurrent());
    }

    @DisplayName("Consume via instance works correctly")
    @Test
    void consumeViaInstance() {
        CombatStatInstance mana = data.getInstance(MANA_KEY).orElseThrow();
        assertTrue(mana.consume(30));
        assertEquals(70, mana.getCurrent());
    }
}
