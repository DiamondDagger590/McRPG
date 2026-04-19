package us.eunoians.mcrpg.stat;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Per-player mutable state for a single {@link CombatStat}.
 * <p>
 * For resource pool stats (HP, Mana): tracks a {@code current} value clamped to
 * {@code [0, effectiveMax]}. Supports consumption, restoration, and passive regen.
 * <p>
 * For flat stats (Defense, Attack Power): {@code current} is unused; only
 * {@link #getEffectiveValue()} matters (the computed total from base + modifiers).
 * <p>
 * Modifiers are named contributions keyed by a source string. The effective maximum
 * is computed as: {@code (baseValue + sumFlatBonuses) * (1 + sumPercentBonuses)}.
 * <p>
 * Not thread-safe — all access must occur on the main server thread.
 */
public class CombatStatInstance {

    private final CombatStat definition;
    private double baseValue;
    private double current;
    private double regenPerSecond;
    private final Map<String, CombatStatModifier> modifiers = new HashMap<>();

    /**
     * Creates a new instance from a stat definition, initializing the base value and
     * current value (for resource pools) to the definition's defaults.
     *
     * @param definition The stat definition this instance tracks.
     */
    public CombatStatInstance(@NotNull CombatStat definition) {
        this.definition = definition;
        this.baseValue = definition.getDefaultBaseValue();
        this.regenPerSecond = definition.getDefaultRegenPerSecond();
        this.current = definition.isResourcePool() ? baseValue : 0;
    }

    /**
     * @return The stat definition this instance is tracking.
     */
    @NotNull
    public CombatStat getDefinition() {
        return definition;
    }

    /**
     * @return The base value before modifiers.
     */
    public double getBaseValue() {
        return baseValue;
    }

    /**
     * Sets the base value and re-clamps current if this is a resource pool.
     *
     * @param baseValue The new base value.
     */
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
        clampCurrent();
    }

    /**
     * Computes the effective maximum value: {@code (base + flatSum) * (1 + percentSum)}.
     *
     * @return The effective max, always at least 0.
     */
    public double getEffectiveMax() {
        double flatSum = modifiers.values().stream().mapToDouble(CombatStatModifier::flatBonus).sum();
        double percentSum = modifiers.values().stream().mapToDouble(CombatStatModifier::percentBonus).sum();
        return Math.max(0, (baseValue + flatSum) * (1 + percentSum));
    }

    /**
     * Alias for {@link #getEffectiveMax()} — meaningful for flat stats where there
     * is no current/max distinction.
     *
     * @return The effective computed value of this stat.
     */
    public double getEffectiveValue() {
        return getEffectiveMax();
    }

    /**
     * Returns the current value for resource pool stats.
     *
     * @return The current value, clamped to {@code [0, effectiveMax]}.
     */
    public double getCurrent() {
        return current;
    }

    /**
     * Directly sets the current value, clamping to {@code [0, effectiveMax]}.
     *
     * @param value The new current value.
     */
    public void setCurrent(double value) {
        this.current = value;
        clampCurrent();
    }

    /**
     * Attempts to consume the given amount from the current pool.
     *
     * @param amount The amount to consume (must be non-negative).
     * @return {@code true} if the consumption succeeded (sufficient resources),
     *         {@code false} if insufficient resources (pool unchanged).
     */
    public boolean consume(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Consume amount must be non-negative: " + amount);
        }
        if (current < amount) {
            return false;
        }
        current -= amount;
        return true;
    }

    /**
     * Restores the given amount to the current pool, clamping to the effective max.
     *
     * @param amount The amount to restore (must be non-negative).
     */
    public void restore(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Restore amount must be non-negative: " + amount);
        }
        current = Math.min(current + amount, getEffectiveMax());
    }

    /**
     * Applies passive regeneration based on elapsed time.
     *
     * @param secondsElapsed The time elapsed since the last regen tick, in seconds.
     */
    public void tickRegen(double secondsElapsed) {
        if (regenPerSecond <= 0 || !definition.isResourcePool()) {
            return;
        }
        restore(regenPerSecond * secondsElapsed);
    }

    /**
     * @return The current passive regen rate per second.
     */
    public double getRegenPerSecond() {
        return regenPerSecond;
    }

    /**
     * Sets the passive regen rate per second.
     *
     * @param regenPerSecond The new regen rate.
     */
    public void setRegenPerSecond(double regenPerSecond) {
        this.regenPerSecond = regenPerSecond;
    }

    /**
     * Adds a named modifier from a source. If a modifier with the same source key
     * already exists, it is replaced.
     *
     * @param modifier The modifier to add.
     */
    public void addModifier(@NotNull CombatStatModifier modifier) {
        modifiers.put(modifier.sourceKey(), modifier);
        clampCurrent();
    }

    /**
     * Removes the modifier with the given source key, if present.
     *
     * @param sourceKey The source key of the modifier to remove.
     */
    public void removeModifier(@NotNull String sourceKey) {
        modifiers.remove(sourceKey);
        clampCurrent();
    }

    /**
     * @return An unmodifiable view of all active modifiers.
     */
    @NotNull
    public Map<String, CombatStatModifier> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    private void clampCurrent() {
        if (definition.isResourcePool()) {
            double max = getEffectiveMax();
            current = Math.max(0, Math.min(current, max));
        }
    }
}
