package us.eunoians.mcrpg.fishing;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Selects a mob from a weighted pool based on the player's current spawn chance
 * and the hook location's biome, world, and region.
 * <p>
 * Constructed once with the full pool. On each spawn trigger, call
 * {@link #select(double, Location, WorldGuardHook)} to get an eligible entry.
 */
public final class MobPoolSelector {

    private final List<MobPoolEntry> pool;

    /**
     * Creates a new selector with the given mob pool.
     *
     * @param pool the weighted mob pool entries
     */
    public MobPoolSelector(@NotNull List<MobPoolEntry> pool) {
        this.pool = List.copyOf(pool);
    }

    /**
     * Selects a random mob from the pool, weighted by {@link MobPoolEntry#weight()}.
     * Only entries that pass all eligibility checks are considered:
     * <ul>
     *   <li>{@code minChanceThreshold <= currentChance}</li>
     *   <li>Location passes biome allow/deny lists</li>
     *   <li>Location passes world allow/deny lists</li>
     *   <li>Location passes WorldGuard region allow/deny lists (if WG present)</li>
     * </ul>
     *
     * @param currentChance  the player's current accumulated spawn chance
     * @param hookLocation   the fishing hook location (for biome/world/region checks)
     * @param worldGuardHook the WorldGuard hook, or null if WG is not present
     * @return a selected entry, or empty if no entries are eligible
     */
    @NotNull
    public Optional<MobPoolEntry> select(double currentChance,
                                          @NotNull Location hookLocation,
                                          @Nullable WorldGuardHook worldGuardHook) {
        List<MobPoolEntry> eligible = pool.stream()
                .filter(entry -> currentChance >= entry.minChanceThreshold())
                .filter(entry -> isLocationAllowed(entry, hookLocation, worldGuardHook))
                .toList();

        if (eligible.isEmpty()) {
            return Optional.empty();
        }

        int totalWeight = eligible.stream().mapToInt(MobPoolEntry::weight).sum();
        if (totalWeight <= 0) {
            return Optional.empty();
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cumulative = 0;
        for (MobPoolEntry entry : eligible) {
            cumulative += entry.weight();
            if (roll < cumulative) {
                return Optional.of(entry);
            }
        }

        return Optional.of(eligible.getLast());
    }

    /**
     * Returns whether the pool has any entries.
     *
     * @return true if the pool is non-empty
     */
    public boolean hasEntries() {
        return !pool.isEmpty();
    }

    private boolean isLocationAllowed(@NotNull MobPoolEntry entry,
                                       @NotNull Location location,
                                       @Nullable WorldGuardHook worldGuardHook) {
        // World check — deny takes priority
        String worldName = location.getWorld().getName();
        if (!entry.deniedWorlds().isEmpty() && entry.deniedWorlds().contains(worldName)) {
            return false;
        }
        if (!entry.allowedWorlds().isEmpty() && !entry.allowedWorlds().contains(worldName)) {
            return false;
        }

        // Biome check — deny takes priority
        String biomeName = location.getBlock().getBiome().name();
        if (!entry.deniedBiomes().isEmpty() && entry.deniedBiomes().contains(biomeName)) {
            return false;
        }
        if (!entry.allowedBiomes().isEmpty() && !entry.allowedBiomes().contains(biomeName)) {
            return false;
        }

        // WorldGuard region check — only if WG is present and entry has region restrictions
        if (worldGuardHook != null) {
            Set<String> regionsAtLocation = worldGuardHook.getRegionIds(location);
            if (!entry.deniedRegions().isEmpty()) {
                for (String denied : entry.deniedRegions()) {
                    if (regionsAtLocation.contains(denied)) {
                        return false;
                    }
                }
            }
            if (!entry.allowedRegions().isEmpty()) {
                boolean inAllowed = false;
                for (String allowed : entry.allowedRegions()) {
                    if (regionsAtLocation.contains(allowed)) {
                        inAllowed = true;
                        break;
                    }
                }
                if (!inAllowed) {
                    return false;
                }
            }
        }

        return true;
    }
}
