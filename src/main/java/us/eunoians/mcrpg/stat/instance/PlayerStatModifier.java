package us.eunoians.mcrpg.stat.instance;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * A modifier that contributes to a {@link PlayerStatInstance}'s effective value.
 * <p>
 * Modifiers come from sources like passive abilities, equipped items, or temporary buffs.
 * Each modifier is identified by a unique {@link NamespacedKey} so it can be added and removed
 * cleanly when the source is slotted/unslotted or a buff expires, and so that third-party
 * plugins cannot collide with each other's keys.
 * <p>
 * Effective max is computed as: {@code (base + sumEffectiveFlat) * (1 + sumEffectivePercent)}.
 * <p>
 * This base implementation is immutable and never expires. Subclasses may override
 * {@link #getEffectiveFlatBonus()} and {@link #getEffectivePercentBonus()} to scale
 * dynamically (e.g., per-stack multipliers), and {@link #tick(double)} /
 * {@link #isExpired()} to support time-based or stacking falloff behaviour.
 */
public class PlayerStatModifier {

    private final NamespacedKey sourceKey;
    private final double flatBonus;
    private final double percentBonus;

    /**
     * Creates a simple, permanent modifier with fixed bonuses.
     *
     * @param sourceKey    Unique identifier for the source of this modifier (e.g., ability key,
     *                     item key). Used as the map key in {@link PlayerStatInstance}.
     * @param flatBonus    Flat additive bonus applied before percentage scaling.
     * @param percentBonus Percentage bonus (0.1 = +10%). Applied multiplicatively after flat bonuses.
     */
    public PlayerStatModifier(@NotNull NamespacedKey sourceKey, double flatBonus, double percentBonus) {
        this.sourceKey = sourceKey;
        this.flatBonus = flatBonus;
        this.percentBonus = percentBonus;
    }

    /**
     * @return The unique key identifying the source of this modifier.
     */
    @NotNull
    public NamespacedKey getSourceKey() {
        return sourceKey;
    }

    /**
     * Returns the effective flat bonus contributed by this modifier.
     * <p>
     * The base implementation returns the raw {@code flatBonus}. Subclasses
     * (e.g., stackable modifiers) override this to scale dynamically.
     *
     * @return The effective flat bonus.
     */
    public double getEffectiveFlatBonus() {
        return flatBonus;
    }

    /**
     * Returns the effective percentage bonus contributed by this modifier.
     * <p>
     * The base implementation returns the raw {@code percentBonus}. Subclasses
     * (e.g., stackable modifiers) override this to scale dynamically.
     *
     * @return The effective percentage bonus.
     */
    public double getEffectivePercentBonus() {
        return percentBonus;
    }

    /**
     * Called once per tick by {@link PlayerStatInstance} to advance any internal
     * timers or state (e.g., duration countdown, stack falloff).
     * <p>
     * The base implementation is a no-op — simple permanent modifiers do not
     * need tick updates. Subclasses override this for time-based behaviour.
     *
     * @param secondsElapsed The time elapsed since the last tick, in seconds.
     */
    public void tick(double secondsElapsed) {
        // No-op for permanent modifiers.
    }

    /**
     * Returns whether this modifier has expired and should be removed from
     * the {@link PlayerStatInstance}.
     * <p>
     * The base implementation always returns {@code false} — permanent modifiers
     * never expire. Subclasses override this to signal expiration (e.g., when a
     * timed duration elapses or stacks reach zero).
     *
     * @return {@code true} if this modifier should be removed.
     */
    public boolean isExpired() {
        return false;
    }
}
