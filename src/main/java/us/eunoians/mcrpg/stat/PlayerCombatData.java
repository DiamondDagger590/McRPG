package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Per-player container for all {@link CombatStatInstance} entries.
 * <p>
 * Initialized from {@link CombatStatRegistry} definitions when a player joins.
 * Exposes {@link #getInstance(NamespacedKey)} so callers interact with the
 * {@link CombatStatInstance} directly for consumption, restoration, and queries.
 * <p>
 * This class handles lifecycle concerns: initialization, regen ticking across
 * all resource pool stats, and cleanup on player quit.
 * <p>
 * Not thread-safe — all access must occur on the main server thread.
 */
public class PlayerCombatData {

    private final Map<NamespacedKey, CombatStatInstance> stats = new LinkedHashMap<>();

    /**
     * Initializes this container from all stat definitions in the given registry.
     * Each registered {@link CombatStat} gets a fresh {@link CombatStatInstance}
     * with default values.
     *
     * @param registry The registry to seed from.
     */
    public void initFromRegistry(@NotNull CombatStatRegistry registry) {
        for (CombatStat stat : registry.allStats()) {
            stats.put(stat.getKey(), new CombatStatInstance(stat));
        }
    }

    /**
     * Returns the {@link CombatStatInstance} for the given stat key.
     *
     * @param key The stat key to look up.
     * @return An optional containing the instance, or empty if not tracked.
     */
    @NotNull
    public Optional<CombatStatInstance> getInstance(@NotNull NamespacedKey key) {
        return Optional.ofNullable(stats.get(key));
    }

    /**
     * Ticks passive regen for all resource pool stats.
     *
     * @param secondsElapsed The time elapsed since the last tick, in seconds.
     */
    public void tickRegen(double secondsElapsed) {
        for (CombatStatInstance instance : stats.values()) {
            instance.tickRegen(secondsElapsed);
        }
    }
}
