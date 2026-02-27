package us.eunoians.mcrpg.telemetry;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.AbilityMetrics;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.AbilityPair;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.LoadoutSnapshot;
import us.eunoians.mcrpg.telemetry.MetricsSnapshot.SkillMetrics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory accumulator for McRPG telemetry metrics.
 * <p>
 * This class collects metrics from game events (XP gains, level-ups, ability activations)
 * and periodically produces immutable {@link MetricsSnapshot}s via {@link #snapshotAndReset()}.
 * <p>
 * All increment methods are safe to call from any thread (they use atomic operations and
 * concurrent maps). The {@link #snapshotAndReset()} method should only be called from
 * a single thread (the scheduled task).
 */
public class MetricsAccumulator {

    private final McRPG plugin;

    // Skill metrics: keyed by skill NamespacedKey
    private final ConcurrentHashMap<NamespacedKey, SkillAccumulator> skillAccumulators = new ConcurrentHashMap<>();

    // Ability activation counts: keyed by ability NamespacedKey
    private final ConcurrentHashMap<NamespacedKey, AtomicInteger> abilityActivationCounts = new ConcurrentHashMap<>();

    // Tracks the start time of the current aggregation window
    private volatile long windowStartMillis;

    public MetricsAccumulator(@NotNull McRPG plugin) {
        this.plugin = plugin;
        this.windowStartMillis = System.currentTimeMillis();
    }

    /**
     * Records an XP gain event for a skill.
     *
     * @param skillKey The skill that received XP
     * @param xpAmount The amount of XP gained
     * @param playerUuid The UUID of the player who gained XP
     */
    public void recordXpGain(@NotNull NamespacedKey skillKey, int xpAmount, @NotNull UUID playerUuid) {
        SkillAccumulator accumulator = skillAccumulators.computeIfAbsent(skillKey, k -> new SkillAccumulator());
        accumulator.totalXpGained.addAndGet(xpAmount);
        accumulator.xpGainEventCount.incrementAndGet();
        accumulator.activePlayerUuids.add(playerUuid);
    }

    /**
     * Records a level-up event for a skill.
     *
     * @param skillKey The skill that leveled up
     * @param levelsGained The number of levels gained
     */
    public void recordLevelUp(@NotNull NamespacedKey skillKey, int levelsGained) {
        SkillAccumulator accumulator = skillAccumulators.computeIfAbsent(skillKey, k -> new SkillAccumulator());
        accumulator.levelUpCount.addAndGet(levelsGained);
    }

    /**
     * Records an ability activation.
     *
     * @param abilityKey The ability that was activated
     */
    public void recordAbilityActivation(@NotNull NamespacedKey abilityKey) {
        abilityActivationCounts.computeIfAbsent(abilityKey, k -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Takes a point-in-time snapshot of all online players' active loadouts.
     * <p>
     * This scans every online McRPG player's currently selected loadout and computes:
     * <ul>
     *   <li>Per-ability equip counts (how many loadouts contain each ability)</li>
     *   <li>Per-pair co-occurrence counts (how many loadouts contain both abilities in a pair)</li>
     * </ul>
     *
     * @return A {@link LoadoutSnapshot} of the current loadout state
     */
    @NotNull
    public LoadoutSnapshot captureLoadoutSnapshot() {
        Set<McRPGPlayer> allPlayers = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getAllPlayers();

        Map<NamespacedKey, Integer> equipCounts = new HashMap<>();
        Map<AbilityPair, Integer> pairCounts = new HashMap<>();
        int totalLoadouts = 0;

        for (McRPGPlayer player : allPlayers) {
            SkillHolder skillHolder = player.asSkillHolder();
            Loadout loadout = skillHolder.getLoadout();
            Set<NamespacedKey> abilities = loadout.getAbilities();

            if (abilities.isEmpty()) {
                continue;
            }

            totalLoadouts++;

            // Count individual ability equips
            for (NamespacedKey abilityKey : abilities) {
                equipCounts.merge(abilityKey, 1, Integer::sum);
            }

            // Count all pairwise co-occurrences
            List<NamespacedKey> abilityList = new ArrayList<>(abilities);
            for (int i = 0; i < abilityList.size(); i++) {
                for (int j = i + 1; j < abilityList.size(); j++) {
                    AbilityPair pair = AbilityPair.of(abilityList.get(i), abilityList.get(j));
                    pairCounts.merge(pair, 1, Integer::sum);
                }
            }
        }

        return new LoadoutSnapshot(
                Map.copyOf(equipCounts),
                Map.copyOf(pairCounts),
                totalLoadouts
        );
    }

    /**
     * Creates an immutable snapshot of all accumulated metrics and resets the accumulators
     * for the next aggregation window.
     * <p>
     * The loadout snapshot is captured at snapshot time (point-in-time), while skill and
     * ability metrics are accumulated over the window since the last snapshot.
     *
     * @return An immutable {@link MetricsSnapshot} representing the completed aggregation window
     */
    @NotNull
    public MetricsSnapshot snapshotAndReset() {
        long now = System.currentTimeMillis();
        long windowDuration = now - windowStartMillis;

        // Snapshot skill metrics and reset
        Map<NamespacedKey, SkillMetrics> skillMetrics = new HashMap<>();
        for (Map.Entry<NamespacedKey, SkillAccumulator> entry : skillAccumulators.entrySet()) {
            NamespacedKey key = entry.getKey();
            SkillAccumulator acc = entry.getValue();
            skillMetrics.put(key, new SkillMetrics(
                    key,
                    acc.totalXpGained.get(),
                    acc.xpGainEventCount.get(),
                    acc.levelUpCount.get(),
                    acc.activePlayerUuids.size()
            ));
        }

        // Snapshot ability metrics and reset
        Map<NamespacedKey, AbilityMetrics> abilityMetrics = new HashMap<>();
        for (Map.Entry<NamespacedKey, AtomicInteger> entry : abilityActivationCounts.entrySet()) {
            NamespacedKey key = entry.getKey();
            abilityMetrics.put(key, new AbilityMetrics(key, entry.getValue().get()));
        }

        // Capture loadout snapshot at this point in time
        LoadoutSnapshot loadoutSnapshot = captureLoadoutSnapshot();

        // Get online player count
        int onlinePlayerCount = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getAllPlayers().size();

        // Reset accumulators for next window
        skillAccumulators.clear();
        abilityActivationCounts.clear();
        windowStartMillis = now;

        return new MetricsSnapshot(
                Instant.ofEpochMilli(now),
                windowDuration,
                Map.copyOf(skillMetrics),
                Map.copyOf(abilityMetrics),
                loadoutSnapshot,
                onlinePlayerCount
        );
    }

    /**
     * Internal mutable accumulator for a single skill's metrics within an aggregation window.
     */
    private static class SkillAccumulator {
        final AtomicLong totalXpGained = new AtomicLong();
        final AtomicInteger xpGainEventCount = new AtomicInteger();
        final AtomicInteger levelUpCount = new AtomicInteger();
        final Set<UUID> activePlayerUuids = ConcurrentHashMap.newKeySet();
    }
}
