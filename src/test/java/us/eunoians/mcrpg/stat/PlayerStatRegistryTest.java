package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatRegistryTest extends McRPGBaseTest {

    private PlayerStatRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PlayerStatRegistry();
    }

    @DisplayName("Register and retrieve a stat by key")
    @Test
    void registerAndRetrieve() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 200, 0
        );
        registry.register(stat);

        var result = registry.getStat(new NamespacedKey("test", "hp"));
        assertTrue(result.isPresent());
        assertEquals("HP", result.get().getDisplayName());
    }

    @DisplayName("Duplicate registration throws")
    @Test
    void duplicateThrows() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 200, 0
        );
        registry.register(stat);
        assertThrows(IllegalArgumentException.class, () -> registry.register(stat));
    }

    @DisplayName("Get unregistered key returns empty")
    @Test
    void unregisteredReturnsEmpty() {
        assertTrue(registry.getStat(new NamespacedKey("test", "missing")).isEmpty());
    }

    @DisplayName("allStats returns all registered stats")
    @Test
    void allStatsReturnsAll() {
        registry.register(new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 200, 0
        ));
        registry.register(new ResourcePoolPlayerStat(
                new NamespacedKey("test", "mana"), "Mana", "✦", 100, 5
        ));
        assertEquals(2, registry.allStats().size());
    }
}
