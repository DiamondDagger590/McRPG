package us.eunoians.mcrpg.telemetry;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * An immutable snapshot of all collected metrics for a single aggregation window.
 * <p>
 * This is the output of {@link MetricsAccumulator#snapshotAndReset()} and represents
 * the pre-aggregated data that would eventually be pushed to a remote telemetry backend.
 *
 * @param timestamp            When this snapshot was taken
 * @param windowDurationMillis How long the aggregation window was (in milliseconds)
 * @param skillMetrics         Per-skill XP and level-up metrics
 * @param abilityMetrics       Per-ability activation counts
 * @param loadoutSnapshot      Ability popularity counts and pair co-occurrence data
 * @param onlinePlayerCount    Number of online McRPG players at snapshot time
 */
public record MetricsSnapshot(
        @NotNull Instant timestamp,
        long windowDurationMillis,
        @NotNull Map<NamespacedKey, SkillMetrics> skillMetrics,
        @NotNull Map<NamespacedKey, AbilityMetrics> abilityMetrics,
        @NotNull LoadoutSnapshot loadoutSnapshot,
        int onlinePlayerCount
) {

    /**
     * Per-skill metrics accumulated over one aggregation window.
     *
     * @param skillKey           The skill's namespaced key
     * @param totalXpGained      Total XP gained across all players for this skill
     * @param xpGainEventCount   Number of individual XP gain events
     * @param levelUpCount       Number of level-up events
     * @param activePlayerCount  Distinct players who gained XP in this skill
     */
    public record SkillMetrics(
            @NotNull NamespacedKey skillKey,
            long totalXpGained,
            int xpGainEventCount,
            int levelUpCount,
            int activePlayerCount
    ) {
    }

    /**
     * Per-ability metrics accumulated over one aggregation window.
     *
     * @param abilityKey      The ability's namespaced key
     * @param activationCount Number of times this ability activated
     */
    public record AbilityMetrics(
            @NotNull NamespacedKey abilityKey,
            int activationCount
    ) {
    }

    /**
     * A point-in-time snapshot of loadout compositions across all online players.
     *
     * @param abilityEquipCounts Per-ability count of how many active loadouts contain it
     * @param pairCoOccurrences  Per-ability-pair count of how many active loadouts contain both
     * @param totalLoadouts      Total number of active loadouts sampled
     */
    public record LoadoutSnapshot(
            @NotNull Map<NamespacedKey, Integer> abilityEquipCounts,
            @NotNull Map<AbilityPair, Integer> pairCoOccurrences,
            int totalLoadouts
    ) {
    }

    /**
     * A canonical (lexicographically ordered) pair of ability keys.
     * <p>
     * The ordering ensures that (A, B) and (B, A) are treated as the same pair.
     */
    public record AbilityPair(@NotNull NamespacedKey first, @NotNull NamespacedKey second) {

        /**
         * Creates a canonical pair from two ability keys, ordering them lexicographically.
         *
         * @param a First ability key
         * @param b Second ability key
         * @return A canonical {@link AbilityPair}
         */
        @NotNull
        public static AbilityPair of(@NotNull NamespacedKey a, @NotNull NamespacedKey b) {
            if (a.toString().compareTo(b.toString()) <= 0) {
                return new AbilityPair(a, b);
            }
            return new AbilityPair(b, a);
        }
    }
}
