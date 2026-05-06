package us.eunoians.mcrpg.stat.instance;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.stat.PlayerStat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Per-player mutable state for a single {@link PlayerStat}.
 * <p>
 * For resource pool stats (HP, Mana): tracks a {@code current} value clamped to
 * {@code [0, effectiveMax]}. Supports consumption, restoration, and passive regen.
 * <p>
 * For flat stats (Defense, Attack Power): {@code current} is unused; only
 * {@link #getEffectiveValue()} matters (the computed total from base + modifiers).
 * <p>
 * Modifiers are keyed by {@link NamespacedKey} and contribute via virtual
 * {@link PlayerStatModifier#getEffectiveFlatBonus()} / {@link PlayerStatModifier#getEffectivePercentBonus()}.
 * The effective maximum is computed as:
 * {@code (base + sumEffectiveFlat) * (1 + sumEffectivePercent)}.
 * Base and regen values are delegated to the {@link PlayerStat} definition so that
 * config reloads propagate automatically to all online players with zero iteration.
 * <p>
 * Not thread-safe — all access must occur on the main server thread.
 */
public class PlayerStatInstance {

    private final PlayerStat definition;
    private double current;
    private final Map<NamespacedKey, PlayerStatModifier> modifiers = new HashMap<>();

    /**
     * Creates a new instance from a stat definition, initializing the current value
     * (for resource pools) to the definition's base value.
     *
     * @param definition The stat definition this instance tracks.
     */
    public PlayerStatInstance(@NotNull PlayerStat definition) {
        this.definition = definition;
        this.current = definition.isResourcePool() ? definition.getBaseValue() : 0;
    }

    /**
     * @return The stat definition this instance is tracking.
     */
    @NotNull
    public PlayerStat getDefinition() {
        return definition;
    }

    /**
     * Computes the effective maximum value: {@code (base + flatSum) * (1 + percentSum)}.
     * Uses {@link PlayerStatModifier#getEffectiveFlatBonus()} and
     * {@link PlayerStatModifier#getEffectivePercentBonus()} so subclass modifiers
     * (e.g., stackable) contribute their scaled values automatically.
     * <p>
     * Reads {@code definition.getBaseValue()} live, so config reloads propagate
     * automatically to all online players with zero iteration.
     *
     * @return The effective max, always at least 0.
     */
    public double getEffectiveMax() {
        double flatSum = modifiers.values().stream().mapToDouble(PlayerStatModifier::getEffectiveFlatBonus).sum();
        double percentSum = modifiers.values().stream().mapToDouble(PlayerStatModifier::getEffectivePercentBonus).sum();
        return Math.max(0, (definition.getBaseValue() + flatSum) * (1 + percentSum));
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
     * @throws IllegalArgumentException If {@code amount} is negative.
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
     * @throws IllegalArgumentException If {@code amount} is negative.
     */
    public void restore(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Restore amount must be non-negative: " + amount);
        }
        current = Math.min(current + amount, getEffectiveMax());
    }

    /**
     * Advances all modifier timers, removes expired modifiers, and then applies
     * passive regeneration based on elapsed time.
     * <p>
     * Modifier ticking is performed first so that any modifier whose duration or
     * stacks have elapsed is removed before the regen calculation reads the
     * effective max.
     *
     * @param secondsElapsed The time elapsed since the last tick, in seconds.
     */
    public void tickRegen(double secondsElapsed) {
        tickModifiers(secondsElapsed);
        if (definition.getRegenPerSecond() <= 0 || !definition.isResourcePool()) {
            return;
        }
        restore(definition.getRegenPerSecond() * secondsElapsed);
    }

    /**
     * Ticks all modifiers and removes any that have expired.
     * <p>
     * Each modifier's {@link PlayerStatModifier#tick(double)} is called, then
     * {@link PlayerStatModifier#isExpired()} is checked. Expired modifiers are
     * removed and the current value is re-clamped.
     *
     * @param secondsElapsed The time elapsed since the last tick, in seconds.
     */
    private void tickModifiers(double secondsElapsed) {
        boolean anyRemoved = false;
        Iterator<PlayerStatModifier> iterator = modifiers.values().iterator();
        while (iterator.hasNext()) {
            PlayerStatModifier modifier = iterator.next();
            modifier.tick(secondsElapsed);
            if (modifier.isExpired()) {
                iterator.remove();
                anyRemoved = true;
            }
        }
        if (anyRemoved) {
            clampCurrent();
        }
    }

    /**
     * Adds a modifier from a source. If a modifier with the same source key
     * already exists, it is replaced.
     *
     * @param modifier The modifier to add.
     */
    public void addModifier(@NotNull PlayerStatModifier modifier) {
        modifiers.put(modifier.getSourceKey(), modifier);
        clampCurrent();
    }

    /**
     * Removes the modifier with the given source key, if present.
     *
     * @param sourceKey The source key of the modifier to remove.
     */
    public void removeModifier(@NotNull NamespacedKey sourceKey) {
        modifiers.remove(sourceKey);
        clampCurrent();
    }

    /**
     * @return An unmodifiable view of all active modifiers.
     */
    @NotNull
    public Map<NamespacedKey, PlayerStatModifier> getModifiers() {
        return Collections.unmodifiableMap(modifiers);
    }

    /**
     * Clamps the current value to the effective max when the stat is a resource pool.
     */
    private void clampCurrent() {
        if (definition.isResourcePool()) {
            double max = getEffectiveMax();
            current = Math.max(0, Math.min(current, max));
        }
    }
}
