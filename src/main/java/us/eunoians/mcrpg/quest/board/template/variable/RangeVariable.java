package us.eunoians.mcrpg.quest.board.template.variable;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * A numeric range template variable that resolves to a random number within
 * {@code [min, max]}, then scaled by the combined difficulty multiplier
 * (pool difficulty * rarity difficulty).
 */
public final class RangeVariable implements TemplateVariable {

    private final String name;
    private final double min;
    private final double max;

    public RangeVariable(@NotNull String name, double min, double max) {
        if (min <= 0) {
            throw new IllegalArgumentException("min must be > 0, got " + min);
        }
        if (max <= 0) {
            throw new IllegalArgumentException("max must be > 0, got " + max);
        }
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must be <= max (" + max + ")");
        }
        this.name = name;
        this.min = min;
        this.max = max;
    }

    @Override
    @NotNull
    public VariableType getType() {
        return VariableType.RANGE;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the lower bound of the range (inclusive). Always {@code > 0}.
     *
     * @return the minimum value
     */
    public double getMin() {
        return min;
    }

    /**
     * Returns the upper bound of the range (inclusive). Always {@code >= min}.
     *
     * @return the maximum value
     */
    public double getMax() {
        return max;
    }

    /**
     * Resolves this range variable.
     * <p>
     * Final value: {@code random(min, max) * difficultyMultiplier}.
     * The caller combines pool difficulty and rarity difficulty into the single
     * {@code difficultyMultiplier} parameter.
     *
     * @param difficultyMultiplier the combined pool * rarity difficulty multiplier
     * @param random               the seeded random source
     * @return the resolved numeric value
     */
    public double resolve(double difficultyMultiplier, @NotNull Random random) {
        double base = min + (max - min) * random.nextDouble();
        return base * difficultyMultiplier;
    }
}
