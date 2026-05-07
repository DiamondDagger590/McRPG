package us.eunoians.mcrpg.stat;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;
import us.eunoians.mcrpg.stat.instance.PlayerStatData;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies {@link PlayerStatData} initialization and regen delegation.
 * <p>
 * Uses {@link TestInstance.Lifecycle#PER_CLASS} so the {@link us.eunoians.mcrpg.TestBootstrap}
 * runs exactly once for the whole class. Stat definitions (which are immutable singletons) are
 * registered in {@link #registerStats()} ({@code @BeforeAll}), matching production semantics where
 * stats are registered once at plugin startup and never again.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlayerStatDataTest extends McRPGBaseTest {

    private static final NamespacedKey MANA_KEY = new NamespacedKey("test", "mana");
    private static final NamespacedKey HP_KEY = new NamespacedKey("test", "hp");

    private PlayerStatData data;

    @BeforeAll
    void registerStats() {
        PlayerStatRegistry registry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.PLAYER_STAT);
        registry.register(new ResourcePoolPlayerStat(HP_KEY, "HP", "❤", 200, 0));
        registry.register(new ResourcePoolPlayerStat(MANA_KEY, "Mana", "✦", 100, 10));
    }

    @BeforeEach
    void setUp() {
        data = new PlayerStatData();
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
        PlayerStatInstance mana = data.getInstance(MANA_KEY).orElseThrow();
        assertTrue(mana.consume(30));
        assertEquals(70, mana.getCurrent());
    }
}
