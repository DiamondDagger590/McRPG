package us.eunoians.mcrpg.fishing;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A single entry in the weighted fishing mob pool.
 * Parsed from the {@code mob-pool} section of the fishing mob spawn configuration.
 *
 * @param key                a unique identifier for this entry (the YAML key, used for logging)
 * @param mythicMobsId       the MythicMobs internal type ID to spawn
 * @param weight              the relative weight for random selection (higher = more likely)
 * @param minChanceThreshold  the minimum accumulated spawn chance required for this mob to be eligible
 * @param mobLevel            the MythicMobs mob level for MM's scaling system
 * @param allowedBiomes       biome allow list (empty = all biomes allowed)
 * @param deniedBiomes        biome deny list (takes priority over allow list)
 * @param allowedWorlds       world allow list (empty = all worlds allowed)
 * @param deniedWorlds        world deny list (takes priority over allow list)
 * @param allowedRegions      WorldGuard region allow list (empty = all regions allowed, ignored if WG absent)
 * @param deniedRegions       WorldGuard region deny list (takes priority over allow list)
 */
public record MobPoolEntry(
        @NotNull String key,
        @NotNull String mythicMobsId,
        int weight,
        double minChanceThreshold,
        double mobLevel,
        @NotNull Set<String> allowedBiomes,
        @NotNull Set<String> deniedBiomes,
        @NotNull Set<String> allowedWorlds,
        @NotNull Set<String> deniedWorlds,
        @NotNull Set<String> allowedRegions,
        @NotNull Set<String> deniedRegions
) {}
