package us.eunoians.mcrpg.stat.instance;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.stat.PlayerStat;
import us.eunoians.mcrpg.stat.PlayerStatRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Per-player container for all {@link PlayerStatInstance} entries.
 * <p>
 * Initialized by resolving the {@link PlayerStatRegistry} from {@link RegistryAccess} on
 * construction, seeding one {@link PlayerStatInstance} per registered definition. Callers
 * interact with instances directly via {@link #getInstance(NamespacedKey)} for consumption,
 * restoration, and queries.
 * <p>
 * Not thread-safe — all access must occur on the main server thread.
 */
public class PlayerStatData {

    private final Map<NamespacedKey, PlayerStatInstance> stats;

    /**
     * Creates a fully initialized stat data container by resolving the
     * {@link PlayerStatRegistry} from {@link RegistryAccess} and seeding
     * an instance for each registered stat definition.
     */
    public PlayerStatData() {
        this.stats = new LinkedHashMap<>();
        PlayerStatRegistry registry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.PLAYER_STAT);
        for (PlayerStat stat : registry.allStats()) {
            stats.put(stat.getKey(), new PlayerStatInstance(stat));
        }
    }

    /**
     * Returns the {@link PlayerStatInstance} for the given stat key.
     *
     * @param key The stat key to look up.
     * @return An optional containing the instance, or empty if not tracked.
     */
    @NotNull
    public Optional<PlayerStatInstance> getInstance(@NotNull NamespacedKey key) {
        return Optional.ofNullable(stats.get(key));
    }

    /**
     * Ticks passive regen for all resource pool stats.
     *
     * @param secondsElapsed The time elapsed since the last tick, in seconds.
     */
    public void tickRegen(double secondsElapsed) {
        for (PlayerStatInstance instance : stats.values()) {
            instance.tickRegen(secondsElapsed);
        }
    }
}
