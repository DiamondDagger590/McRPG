package us.eunoians.mcrpg.quest.board.template.variable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RangeVariableTest {

    private static final double TOLERANCE = 0.0001;

    @Test
    @DisplayName("Given min and max with difficulty multiplier, when resolve is called, then value is within [min * difficulty, max * difficulty]")
    void resolvedValueWithinScaledRange() {
        RangeVariable variable = new RangeVariable("count", 10.0, 100.0);
        double difficulty = 2.0;
        double minExpected = 10.0 * difficulty;
        double maxExpected = 100.0 * difficulty;

        for (int seed = 0; seed < 100; seed++) {
            double resolved = variable.resolve(difficulty, new Random(seed));
            assertTrue(resolved >= minExpected - TOLERANCE && resolved <= maxExpected + TOLERANCE,
                    "Resolved " + resolved + " should be in [" + minExpected + ", " + maxExpected + "] for seed " + seed);
        }
    }

    @Test
    @DisplayName("Given difficulty multiplier 1.0, when resolve is called, then value is within original range")
    void difficultyMultiplierOne_valueWithinOriginalRange() {
        RangeVariable variable = new RangeVariable("amount", 5.0, 50.0);
        double difficulty = 1.0;

        for (int seed = 0; seed < 100; seed++) {
            double resolved = variable.resolve(difficulty, new Random(seed));
            assertTrue(resolved >= 5.0 - TOLERANCE && resolved <= 50.0 + TOLERANCE,
                    "Resolved " + resolved + " should be in [5, 50] for seed " + seed);
        }
    }

    @Test
    @DisplayName("Given difficulty multiplier 2.0, when resolve is called, then value is approximately doubled")
    void difficultyMultiplierTwo_valueApproximatelyDoubled() {
        RangeVariable variable = new RangeVariable("count", 20.0, 40.0);
        double difficulty = 2.0;
        double minExpected = 40.0;
        double maxExpected = 80.0;

        for (int seed = 0; seed < 50; seed++) {
            double resolved = variable.resolve(difficulty, new Random(seed));
            assertTrue(resolved >= minExpected - TOLERANCE && resolved <= maxExpected + TOLERANCE,
                    "Resolved " + resolved + " should be in [" + minExpected + ", " + maxExpected + "] for seed " + seed);
        }
    }

    @Test
    @DisplayName("Given same seed, when resolve is called multiple times, then produces deterministic results")
    void seededRandomProducesDeterministicResults() {
        RangeVariable variable = new RangeVariable("count", 1.0, 10.0);
        double difficulty = 1.5;

        double first = variable.resolve(difficulty, new Random(42));
        double second = variable.resolve(difficulty, new Random(42));

        assertEquals(first, second, TOLERANCE);
    }

    @Test
    @DisplayName("Given min equals max, when resolve is called, then always returns min * difficulty within floating point tolerance")
    void minEqualsMax_alwaysReturnsScaledValue() {
        RangeVariable variable = new RangeVariable("fixed", 25.0, 25.0);
        double difficulty = 3.0;
        double expected = 25.0 * difficulty;

        for (int seed = 0; seed < 20; seed++) {
            double resolved = variable.resolve(difficulty, new Random(seed));
            assertEquals(expected, resolved, TOLERANCE, "Seed " + seed + " should always yield " + expected);
        }
    }

    @Test
    @DisplayName("Given min > max, when constructor is called, then throws IllegalArgumentException")
    void constructorRejectsMinGreaterThanMax() {
        assertThrows(IllegalArgumentException.class, () ->
                new RangeVariable("count", 100.0, 10.0));
        assertThrows(IllegalArgumentException.class, () ->
                new RangeVariable("count", 50.0, 25.0));
    }

    @Test
    @DisplayName("Given min <= 0, when constructor is called, then throws IllegalArgumentException")
    void constructorRejectsMinLessOrEqualZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new RangeVariable("count", 0.0, 10.0));
        assertThrows(IllegalArgumentException.class, () ->
                new RangeVariable("count", -5.0, 10.0));
    }

    @Test
    @DisplayName("Given max <= 0, when constructor is called, then throws IllegalArgumentException")
    void constructorRejectsMaxLessOrEqualZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new RangeVariable("count", 1.0, 0.0));
        assertThrows(IllegalArgumentException.class, () ->
                new RangeVariable("count", 5.0, -3.0));
    }
}
