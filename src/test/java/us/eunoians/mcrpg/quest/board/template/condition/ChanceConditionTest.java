package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChanceCondition")
class ChanceConditionTest {

    @Nested
    @DisplayName("constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("rejects chance below 0.0")
        void constructor_throws_whenChanceBelowZero() {
            assertThrows(IllegalArgumentException.class, () -> new ChanceCondition(-0.1));
        }

        @Test
        @DisplayName("rejects chance above 1.0")
        void constructor_throws_whenChanceAboveOne() {
            assertThrows(IllegalArgumentException.class, () -> new ChanceCondition(1.1));
        }

        @Test
        @DisplayName("accepts boundary value 0.0")
        void constructor_accepts_zeroChance() {
            assertDoesNotThrow(() -> new ChanceCondition(0.0));
        }

        @Test
        @DisplayName("accepts boundary value 1.0")
        void constructor_accepts_fullChance() {
            assertDoesNotThrow(() -> new ChanceCondition(1.0));
        }

        @Test
        @DisplayName("no-arg prototype constructor creates valid instance")
        void noArgConstructor_createsPrototype() {
            ChanceCondition prototype = new ChanceCondition();
            assertEquals(0.5, prototype.getChance());
        }
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getKey returns mcrpg:chance")
        void getKey_returnsMcrpgChance() {
            assertEquals(NamespacedKey.fromString("mcrpg:chance"), new ChanceCondition(0.5).getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            Optional<NamespacedKey> key = new ChanceCondition(0.5).getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getChance returns configured probability")
        void getChance_returnsConfiguredValue() {
            assertEquals(0.75, new ChanceCondition(0.75).getChance());
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("serializes chance value")
        void serializeConfig_containsChanceKey() {
            Map<String, Object> config = new ChanceCondition(0.3).serializeConfig();
            assertEquals(0.3, config.get("chance"));
        }

        @Test
        @DisplayName("serialized map has exactly one entry")
        void serializeConfig_hasOneEntry() {
            Map<String, Object> config = new ChanceCondition(0.5).serializeConfig();
            assertEquals(1, config.size());
        }
    }

    @Nested
    @DisplayName("evaluate")
    class Evaluate {

        @Test
        @DisplayName("returns true when random is null (pass-through)")
        void evaluate_returnsTrue_whenRandomIsNull() {
            ChanceCondition condition = new ChanceCondition(0.01);
            ConditionContext context = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("returns true when roll is below chance")
        void evaluate_returnsTrue_whenRollBelowChance() {
            Random seeded = new Random(42L);
            double firstRoll = new Random(42L).nextDouble();
            double chanceAboveRoll = firstRoll + 0.01;

            ChanceCondition condition = new ChanceCondition(Math.min(chanceAboveRoll, 1.0));
            ConditionContext context = new ConditionContext(null, null, seeded, null, null, null);
            assertTrue(condition.evaluate(context));
        }

        @Test
        @DisplayName("returns false when roll is above chance")
        void evaluate_returnsFalse_whenRollAboveChance() {
            Random seeded = new Random(42L);
            double firstRoll = new Random(42L).nextDouble();
            double chanceBelowRoll = firstRoll - 0.01;

            ChanceCondition condition = new ChanceCondition(Math.max(chanceBelowRoll, 0.0));
            ConditionContext context = new ConditionContext(null, null, seeded, null, null, null);
            assertFalse(condition.evaluate(context));
        }

        @Test
        @DisplayName("chance 1.0 always passes")
        void evaluate_alwaysPasses_whenChanceIsOne() {
            ChanceCondition condition = new ChanceCondition(1.0);
            Random random = new Random(12345L);
            for (int i = 0; i < 100; i++) {
                ConditionContext context = new ConditionContext(null, null, random, null, null, null);
                assertTrue(condition.evaluate(context), "Failed on iteration " + i);
            }
        }

        @Test
        @DisplayName("chance 0.0 always fails when random is present")
        void evaluate_alwaysFails_whenChanceIsZero() {
            ChanceCondition condition = new ChanceCondition(0.0);
            Random random = new Random(12345L);
            for (int i = 0; i < 100; i++) {
                ConditionContext context = new ConditionContext(null, null, random, null, null, null);
                assertFalse(condition.evaluate(context), "Failed on iteration " + i);
            }
        }

        @Test
        @DisplayName("deterministic with same seed")
        void evaluate_deterministic_whenSameSeed() {
            ChanceCondition condition = new ChanceCondition(0.5);
            boolean[] firstRun = new boolean[20];
            boolean[] secondRun = new boolean[20];

            Random random1 = new Random(999L);
            for (int i = 0; i < 20; i++) {
                ConditionContext context = new ConditionContext(null, null, random1, null, null, null);
                firstRun[i] = condition.evaluate(context);
            }

            Random random2 = new Random(999L);
            for (int i = 0; i < 20; i++) {
                ConditionContext context = new ConditionContext(null, null, random2, null, null, null);
                secondRun[i] = condition.evaluate(context);
            }

            for (int i = 0; i < 20; i++) {
                assertEquals(firstRun[i], secondRun[i], "Mismatch at index " + i);
            }
        }

        @Test
        @DisplayName("50% chance produces both true and false over many evaluations")
        void evaluate_producesMixedResults_whenFiftyPercent() {
            ChanceCondition condition = new ChanceCondition(0.5);
            Random random = new Random(42L);
            int trueCount = 0;
            int total = 200;
            for (int i = 0; i < total; i++) {
                ConditionContext context = new ConditionContext(null, null, random, null, null, null);
                if (condition.evaluate(context)) {
                    trueCount++;
                }
            }
            assertTrue(trueCount > 0, "Expected at least one true result");
            assertTrue(trueCount < total, "Expected at least one false result");
        }
    }
}
