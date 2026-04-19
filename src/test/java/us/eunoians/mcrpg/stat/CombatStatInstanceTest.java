package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatStatInstanceTest extends McRPGBaseTest {

    private CombatStatInstance poolInstance;
    private CombatStatInstance flatInstance;

    @BeforeEach
    void setUp() {
        CombatStat poolStat = new ResourcePoolCombatStat(
                new NamespacedKey("test", "mana"), "Mana", "✦", 100, 5
        );
        poolInstance = new CombatStatInstance(poolStat);

        CombatStat flatStat = new FlatCombatStat(
                new NamespacedKey("test", "defense"), "Defense", "⚔", 50
        );
        flatInstance = new CombatStatInstance(flatStat);
    }

    @DisplayName("Pool instance initializes current to base value")
    @Test
    void poolInitializesCurrentToBase() {
        assertEquals(100, poolInstance.getCurrent());
        assertEquals(100, poolInstance.getEffectiveMax());
    }

    @DisplayName("Flat instance has zero current")
    @Test
    void flatInstanceHasZeroCurrent() {
        assertEquals(0, flatInstance.getCurrent());
        assertEquals(50, flatInstance.getEffectiveValue());
    }

    @DisplayName("Consume succeeds when sufficient resources")
    @Test
    void consumeSuccess() {
        assertTrue(poolInstance.consume(30));
        assertEquals(70, poolInstance.getCurrent());
    }

    @DisplayName("Consume fails when insufficient resources")
    @Test
    void consumeFails() {
        assertFalse(poolInstance.consume(150));
        assertEquals(100, poolInstance.getCurrent());
    }

    @DisplayName("Consume with negative amount throws")
    @Test
    void consumeNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> poolInstance.consume(-5));
    }

    @DisplayName("Restore clamps to effective max")
    @Test
    void restoreClampsToMax() {
        poolInstance.consume(20);
        poolInstance.restore(50);
        assertEquals(100, poolInstance.getCurrent());
    }

    @DisplayName("Restore with negative amount throws")
    @Test
    void restoreNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> poolInstance.restore(-5));
    }

    @DisplayName("tickRegen restores based on elapsed time")
    @Test
    void tickRegenRestores() {
        poolInstance.consume(50);
        poolInstance.tickRegen(2.0);
        assertEquals(60, poolInstance.getCurrent());
    }

    @DisplayName("tickRegen does nothing for flat stats")
    @Test
    void tickRegenNoOpForFlat() {
        flatInstance.tickRegen(10.0);
        assertEquals(0, flatInstance.getCurrent());
    }

    @DisplayName("Modifier affects effective max and clamps current")
    @Test
    void modifierAffectsEffectiveMax() {
        poolInstance.addModifier(new CombatStatModifier("buff", 50, 0));
        assertEquals(150, poolInstance.getEffectiveMax());
        assertEquals(100, poolInstance.getCurrent());

        poolInstance.restore(100);
        assertEquals(150, poolInstance.getCurrent());
    }

    @DisplayName("Percentage modifier scales effective max correctly")
    @Test
    void percentModifierScales() {
        poolInstance.addModifier(new CombatStatModifier("buff", 0, 0.5));
        assertEquals(150, poolInstance.getEffectiveMax());
    }

    @DisplayName("Removing modifier reduces effective max and clamps current")
    @Test
    void removeModifierClampsCurrent() {
        poolInstance.addModifier(new CombatStatModifier("buff", 100, 0));
        poolInstance.restore(200);
        assertEquals(200, poolInstance.getCurrent());

        poolInstance.removeModifier("buff");
        assertEquals(100, poolInstance.getEffectiveMax());
        assertEquals(100, poolInstance.getCurrent());
    }

    @DisplayName("Combined flat and percent modifiers compute correctly")
    @Test
    void combinedModifiers() {
        poolInstance.addModifier(new CombatStatModifier("flat", 100, 0));
        poolInstance.addModifier(new CombatStatModifier("percent", 0, 0.5));
        // (100 + 100) * (1 + 0.5) = 300
        assertEquals(300, poolInstance.getEffectiveMax());
    }

    @DisplayName("setBaseValue re-clamps current for pool stats")
    @Test
    void setBaseValueClampsCurrent() {
        poolInstance.setBaseValue(50);
        assertEquals(50, poolInstance.getEffectiveMax());
        assertEquals(50, poolInstance.getCurrent());
    }
}
