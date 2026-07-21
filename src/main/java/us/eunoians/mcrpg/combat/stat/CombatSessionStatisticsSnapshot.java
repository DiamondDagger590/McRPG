package us.eunoians.mcrpg.combat.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Immutable snapshot of a session's per-session statistics, created at session end for inclusion in
 * {@link us.eunoians.mcrpg.event.combat.CombatSessionEndEvent} and
 * {@link us.eunoians.mcrpg.event.combat.CombatCumulativeStatisticUpdateEvent}. Session duration is
 * included as a regular double stat under {@link CombatSessionStatisticKey#SESSION_DURATION}.
 */
public final class CombatSessionStatisticsSnapshot {

    private final Map<NamespacedKey, Double> doubleStatistics;
    private final Map<NamespacedKey, Long> longStatistics;

    /**
     * Constructs a new {@link CombatSessionStatisticsSnapshot}.
     *
     * @param doubleStatistics Immutable map of double-valued statistics.
     * @param longStatistics   Immutable map of long-valued statistics.
     */
    CombatSessionStatisticsSnapshot(@NotNull Map<NamespacedKey, Double> doubleStatistics,
                                    @NotNull Map<NamespacedKey, Long> longStatistics) {
        this.doubleStatistics = doubleStatistics;
        this.longStatistics = longStatistics;
    }

    /**
     * Gets the value of a double-valued statistic.
     *
     * @param key The statistic key.
     * @return The value, or {@code 0.0} if absent.
     */
    public double getDouble(@NotNull NamespacedKey key) {
        return doubleStatistics.getOrDefault(key, 0.0);
    }

    /**
     * Gets the value of a long-valued statistic.
     *
     * @param key The statistic key.
     * @return The value, or {@code 0} if absent.
     */
    public long getLong(@NotNull NamespacedKey key) {
        return longStatistics.getOrDefault(key, 0L);
    }

    /**
     * Gets all double-valued statistics.
     *
     * @return An unmodifiable {@link Map} of double-valued statistics.
     */
    @NotNull
    public Map<NamespacedKey, Double> getDoubleStatistics() {
        return doubleStatistics;
    }

    /**
     * Gets all long-valued statistics.
     *
     * @return An unmodifiable {@link Map} of long-valued statistics.
     */
    @NotNull
    public Map<NamespacedKey, Long> getLongStatistics() {
        return longStatistics;
    }
}
