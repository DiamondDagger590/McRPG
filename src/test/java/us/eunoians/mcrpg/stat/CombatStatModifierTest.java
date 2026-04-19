package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatStatModifierTest extends McRPGBaseTest {

    @DisplayName("Flat modifier increases effective max")
    @Test
    void flatModifierIncreasesMax() {
        CombatStat stat = new ResourcePoolCombatStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        CombatStatInstance instance = new CombatStatInstance(stat);
        instance.addModifier(new CombatStatModifier("item_armor", 50, 0));
        assertEquals(150, instance.getEffectiveMax());
    }

    @DisplayName("Percent modifier scales effective max")
    @Test
    void percentModifierScalesMax() {
        CombatStat stat = new ResourcePoolCombatStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        CombatStatInstance instance = new CombatStatInstance(stat);
        instance.addModifier(new CombatStatModifier("buff_potion", 0, 0.25));
        assertEquals(125, instance.getEffectiveMax());
    }

    @DisplayName("Multiple modifiers from different sources stack additively before percent")
    @Test
    void multipleModifiersStack() {
        CombatStat stat = new ResourcePoolCombatStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        CombatStatInstance instance = new CombatStatInstance(stat);
        instance.addModifier(new CombatStatModifier("source_a", 20, 0));
        instance.addModifier(new CombatStatModifier("source_b", 30, 0.1));
        // (100 + 20 + 30) * (1 + 0.1) = 150 * 1.1 = 165
        assertEquals(165, instance.getEffectiveMax());
    }

    @DisplayName("Replacing a modifier with the same source key overwrites")
    @Test
    void replaceSameSourceKey() {
        CombatStat stat = new ResourcePoolCombatStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        CombatStatInstance instance = new CombatStatInstance(stat);
        instance.addModifier(new CombatStatModifier("buff", 50, 0));
        assertEquals(150, instance.getEffectiveMax());

        instance.addModifier(new CombatStatModifier("buff", 20, 0));
        assertEquals(120, instance.getEffectiveMax());
    }

    @DisplayName("Removing a modifier reverts its effect")
    @Test
    void removeModifierRevertsEffect() {
        CombatStat stat = new ResourcePoolCombatStat(
                new NamespacedKey("test", "hp"), "HP", "❤", 100, 0
        );
        CombatStatInstance instance = new CombatStatInstance(stat);
        instance.addModifier(new CombatStatModifier("buff", 50, 0));
        instance.removeModifier("buff");
        assertEquals(100, instance.getEffectiveMax());
    }
}
