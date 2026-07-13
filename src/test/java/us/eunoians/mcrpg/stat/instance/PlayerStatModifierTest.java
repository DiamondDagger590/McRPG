package us.eunoians.mcrpg.stat.instance;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PlayerStatModifierTest extends McRPGBaseTest {

    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("mcrpg", "test_modifier");

    @Nested
    @DisplayName("constructor and getters")
    class ConstructorAndGetters {

        @Test
        @DisplayName("getSourceKey returns constructor value")
        void getSourceKey_returnsConstructorValue() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 10.0, 0.5);

            assertEquals(SOURCE_KEY, modifier.getSourceKey());
        }

        @Test
        @DisplayName("getEffectiveFlatBonus returns constructor flat value")
        void getEffectiveFlatBonus_returnsConstructorValue() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 25.0, 0.0);

            assertEquals(25.0, modifier.getEffectiveFlatBonus());
        }

        @Test
        @DisplayName("getEffectivePercentBonus returns constructor percent value")
        void getEffectivePercentBonus_returnsConstructorValue() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 0.0, 0.15);

            assertEquals(0.15, modifier.getEffectivePercentBonus());
        }

        @Test
        @DisplayName("zero flat bonus is valid")
        void zeroFlatBonus_isValid() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 0.0, 0.1);

            assertEquals(0.0, modifier.getEffectiveFlatBonus());
        }

        @Test
        @DisplayName("zero percent bonus is valid")
        void zeroPercentBonus_isValid() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 5.0, 0.0);

            assertEquals(0.0, modifier.getEffectivePercentBonus());
        }

        @Test
        @DisplayName("negative flat bonus is valid")
        void negativeFlatBonus_isValid() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, -10.0, 0.0);

            assertEquals(-10.0, modifier.getEffectiveFlatBonus());
        }

        @Test
        @DisplayName("negative percent bonus is valid")
        void negativePercentBonus_isValid() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 0.0, -0.25);

            assertEquals(-0.25, modifier.getEffectivePercentBonus());
        }
    }

    @Nested
    @DisplayName("tick")
    class Tick {

        @Test
        @DisplayName("tick is a no-op for base modifier")
        void tick_isNoOp() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 10.0, 0.5);

            modifier.tick(1.0);
            modifier.tick(100.0);

            assertEquals(10.0, modifier.getEffectiveFlatBonus());
            assertEquals(0.5, modifier.getEffectivePercentBonus());
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("base modifier never expires")
        void baseModifier_neverExpires() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 10.0, 0.5);

            assertFalse(modifier.isExpired());
        }

        @Test
        @DisplayName("base modifier does not expire after tick")
        void baseModifier_doesNotExpireAfterTick() {
            PlayerStatModifier modifier = new PlayerStatModifier(SOURCE_KEY, 10.0, 0.5);

            modifier.tick(999999.0);

            assertFalse(modifier.isExpired());
        }
    }
}
