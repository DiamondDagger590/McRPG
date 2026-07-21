package us.eunoians.mcrpg.combat.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable per-session statistics container. Stores double-valued and long-valued statistics in
 * separate maps. Instantiated once per {@link us.eunoians.mcrpg.combat.CombatSession} and populated
 * by stat-tracking listeners. Thread-safety is not required — all access is on the main server thread
 * (inherited from the session's threading contract).
 * <p>
 * {@link CombatSessionStatisticKey#SESSION_DURATION} is stored in the double stats map like any
 * other double stat. It is computed and written once at snapshot time by
 * {@code CombatSession.createStatisticsSnapshot()} — not maintained live during the session. During
 * combat, callers should use {@code CombatSession.getDurationMillis()} for the live value.
 */
public class CombatSessionStatistics {

    private final Map<NamespacedKey, Double> doubleStats;
    private final Map<NamespacedKey, Long> longStats;

    /**
     * Constructs a new empty {@link CombatSessionStatistics}.
     */
    public CombatSessionStatistics() {
        this.doubleStats = new HashMap<>();
        this.longStats = new HashMap<>();
    }

    /**
     * Increments a double-valued statistic.
     *
     * @param key    The statistic key.
     * @param amount The amount to add (may be negative for decrements).
     */
    public void incrementDouble(@NotNull NamespacedKey key, double amount) {
        doubleStats.merge(key, amount, Double::sum);
    }

    /**
     * Increments a long-valued statistic.
     *
     * @param key    The statistic key.
     * @param amount The amount to add (may be negative for decrements).
     */
    public void incrementLong(@NotNull NamespacedKey key, long amount) {
        longStats.merge(key, amount, Long::sum);
    }

    /**
     * Gets the current value of a double-valued statistic.
     *
     * @param key The statistic key.
     * @return The current value, or {@code 0.0} if the key has not been set.
     */
    public double getDouble(@NotNull NamespacedKey key) {
        return doubleStats.getOrDefault(key, 0.0);
    }

    /**
     * Gets the current value of a long-valued statistic.
     *
     * @param key The statistic key.
     * @return The current value, or {@code 0} if the key has not been set.
     */
    public long getLong(@NotNull NamespacedKey key) {
        return longStats.getOrDefault(key, 0L);
    }

    /**
     * Sets a double-valued statistic to an absolute value.
     *
     * @param key   The statistic key.
     * @param value The value to set.
     */
    public void setDouble(@NotNull NamespacedKey key, double value) {
        doubleStats.put(key, value);
    }

    /**
     * Sets a long-valued statistic to an absolute value.
     *
     * @param key   The statistic key.
     * @param value The value to set.
     */
    public void setLong(@NotNull NamespacedKey key, long value) {
        longStats.put(key, value);
    }

    /**
     * Creates an immutable snapshot of the current state of all statistics in this container.
     *
     * @return A new {@link CombatSessionStatisticsSnapshot}.
     */
    @NotNull
    public CombatSessionStatisticsSnapshot snapshot() {
        return new CombatSessionStatisticsSnapshot(
                Map.copyOf(doubleStats),
                Map.copyOf(longStats));
    }
}
