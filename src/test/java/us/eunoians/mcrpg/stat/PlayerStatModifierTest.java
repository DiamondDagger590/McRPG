package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;
import us.eunoians.mcrpg.stat.instance.PlayerStatInstance;
import us.eunoians.mcrpg.stat.instance.PlayerStatModifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerStatModifierTest extends McRPGBaseTest {

    @DisplayName("Flat modifier increases effective max")
    @Test
    void flatModifierIncreasesMax() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        PlayerStatInstance instance = new PlayerStatInstance(stat);
        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "item_armor"), 50, 0));
        assertEquals(150, instance.getEffectiveMax());
    }

    @DisplayName("Percent modifier scales effective max")
    @Test
    void percentModifierScalesMax() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        PlayerStatInstance instance = new PlayerStatInstance(stat);
        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "buff_potion"), 0, 0.25));
        assertEquals(125, instance.getEffectiveMax());
    }

    @DisplayName("Multiple modifiers from different sources stack additively before percent")
    @Test
    void multipleModifiersStack() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        PlayerStatInstance instance = new PlayerStatInstance(stat);
        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "source_a"), 20, 0));
        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "source_b"), 30, 0.1));
        // (100 + 20 + 30) * (1 + 0.1) = 150 * 1.1 = 165
        assertEquals(165, instance.getEffectiveMax());
    }

    @DisplayName("Replacing a modifier with the same source key overwrites")
    @Test
    void replaceSameSourceKey() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        PlayerStatInstance instance = new PlayerStatInstance(stat);
        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "buff"), 50, 0));
        assertEquals(150, instance.getEffectiveMax());

        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "buff"), 20, 0));
        assertEquals(120, instance.getEffectiveMax());
    }

    @DisplayName("Removing a modifier reverts its effect")
    @Test
    void removeModifierRevertsEffect() {
        PlayerStat stat = new ResourcePoolPlayerStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        PlayerStatInstance instance = new PlayerStatInstance(stat);
        instance.addModifier(new PlayerStatModifier(new NamespacedKey("test", "buff"), 50, 0));
        instance.removeModifier(new NamespacedKey("test", "buff"));
        assertEquals(100, instance.getEffectiveMax());
    }
}
