# Low-Level Design: Fishing Mob Spawn System (LLD-2)

**Status:** Implemented
**Date:** 2026-03-16
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Section 3
**Scope:** Spawn tracker, mob pool, per-player state, config file, custom events, MythicMobs spawning

---

## Table of Contents

1. [Overview](#1-overview)
2. [Existing Infrastructure (from LLD-1)](#2-existing-infrastructure-from-lld-1)
3. [Configuration](#3-configuration)
4. [Mob Pool Data Model](#4-mob-pool-data-model)
5. [MythicMobs Integration](#5-mythicmobs-integration)
6. [Per-Player State](#6-per-player-state)
7. [FishingMobSpawnListener](#7-fishingmobspawnlistener)
8. [Spawn Flow](#8-spawn-flow)
9. [Death Callback Integration](#9-death-callback-integration)
10. [Custom Events](#10-custom-events)
11. [Bootstrap Registration](#11-bootstrap-registration)
12. [Anti-Cheese Analysis](#12-anti-cheese-analysis)
13. [Edge Cases & Graceful Degradation](#13-edge-cases--graceful-degradation)
14. [Test Plan](#14-test-plan)
15. [File Manifest](#15-file-manifest)

---

## 1. Overview

The Fishing Mob Spawn System adds probabilistic mob spawning during fishing. When a player fishes repeatedly in the same area, a hidden spawn chance accumulates. Once it triggers, a mob is selected from a weighted pool and spawned via MythicMobs near the player's fishing hook. Moving to a new fishing location reduces the chance, discouraging AFK farming.

**This LLD produces code.** All classes, configs, and tests described here are implementation-ready.

### Boundary with LLD-1 (MythicMobs Binding System)

The existing LLD-1 implementation provides:
- `MythicMobsHook` — registered when MythicMobs is present
- `MythicMobsListener` — bridges MM spawn/death events into McRPG's event system using PDC tags
- `FishingMobKeys` — PDC key constants (`FISHING_MOB_KEY`, `ANGLER_UUID_KEY`) for tagging spawned mobs
- `McRPGSkillBookDrop` — MythicMobs custom drop type for skill books
- `FishingMobSpawnEvent` / `FishingMobDeathEvent` — McRPG custom events fired by the listener

This LLD **depends on** those classes and does **not** modify them (except `MythicMobsHook`, which gains spawn/despawn helper methods — see Section 5). Specifically:
- The listener spawns mobs via `MythicMobsHook` and tags them with `FishingMobKeys` PDC keys
- `MythicMobsListener` picks up the MM spawn/death events and fires `FishingMobSpawnEvent` / `FishingMobDeathEvent`
- The listener listens to `FishingMobDeathEvent` to clean up per-player state

### Boundary with LLD-3 (Skill Book System)

This LLD does **not** cover:
- Skill book creation or consumption (LLD-3)
- MythicMobs drop table configuration for skill books (handled by MM config + `McRPGSkillBookDrop`)

Loot is entirely owned by MythicMobs' drop table system. When a bound mob dies, MM evaluates its configured drops (including `mcrpg_skillbook` entries). McRPG's role is limited to spawning the mob and cleaning up tracking state on death.

### Despawn Responsibility

Mob despawn behavior (max lifetime, empty threat table cleanup) is **entirely owned by MythicMobs** via native mob skills:
- `~onTimer` — schedule forced despawn after a configurable TTL
- `~onDropCombat` — trigger despawn when the ThreatTable empties (all players leave)
- `remove` mechanic — MM's native entity removal

McRPG does **not** schedule despawn tasks or monitor the ThreatTable. Server owners configure despawn behavior in the MythicMobs mob YAML, not in McRPG config.

---

## 2. Existing Infrastructure (from LLD-1)

These classes already exist in the codebase and are used by this LLD:

| Class | Location | Role in LLD-2 |
|---|---|---|
| `MythicMobsHook` | `external/mythicmobs/` | Presence check + MM API facade (gains spawn method in this LLD) |
| `MythicMobsListener` | `external/mythicmobs/` | Bridges MM spawn/death events; fires `FishingMobSpawnEvent`/`FishingMobDeathEvent` |
| `FishingMobKeys` | `external/mythicmobs/` | PDC keys written to spawned entities for identification |
| `FishingMobSpawnEvent` | `event/fishing/` | Fired by `MythicMobsListener` when a tagged mob spawns — cancellable |
| `FishingMobDeathEvent` | `event/fishing/` | Fired by `MythicMobsListener` when a tagged mob dies — listener listens to this |
| `McRPGSkillBookDrop` | `external/mythicmobs/` | MM custom drop type — not directly used but part of the end-to-end flow |

---

## 3. Configuration

### 3.1 FileType Entry

**Modified file:** `us.eunoians.mcrpg.configuration.FileType`

```java
FISHING_MOB_SPAWN_CONFIG("fishing_mob_spawn_configuration.yml", new FishingMobSpawnConfigFile()),
```

### 3.2 FishingMobSpawnConfigFile

**File:** `src/main/java/us/eunoians/mcrpg/configuration/file/FishingMobSpawnConfigFile.java`

```java
package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * Contains all the {@link Route}s used for the fishing_mob_spawn_configuration.yml.
 */
public final class FishingMobSpawnConfigFile extends ConfigFile {

    // Top-level headers
    private static final String SPAWN_HEADER = "spawn";
    private static final String MOB_POOL_HEADER = "mob-pool";

    // Spawn settings
    public static final Route SPAWN_ENABLED = Route.fromString(toRoutePath(SPAWN_HEADER, "enabled"));
    public static final Route BASE_CHANCE = Route.fromString(toRoutePath(SPAWN_HEADER, "base-chance"));
    public static final Route MAX_CHANCE = Route.fromString(toRoutePath(SPAWN_HEADER, "max-chance"));
    public static final Route CHANCE_INCREMENT_PER_CATCH = Route.fromString(toRoutePath(SPAWN_HEADER, "chance-increment-per-catch"));
    public static final Route CHANCE_DECREMENT_PER_CATCH = Route.fromString(toRoutePath(SPAWN_HEADER, "chance-decrement-per-catch"));
    public static final Route SAME_AREA_RANGE = Route.fromString(toRoutePath(SPAWN_HEADER, "same-area-range"));
    public static final Route POST_KILL_CHANCE = Route.fromString(toRoutePath(SPAWN_HEADER, "post-kill-chance"));
    public static final Route RESET_ON_WORLD_CHANGE = Route.fromString(toRoutePath(SPAWN_HEADER, "reset-on-world-change"));
    public static final Route SPAWN_OFFSET_FROM_HOOK = Route.fromString(toRoutePath(SPAWN_HEADER, "spawn-offset-from-hook"));
    public static final Route SPAWN_Y_OFFSET = Route.fromString(toRoutePath(SPAWN_HEADER, "spawn-y-offset"));
    public static final Route MAX_ACTIVE_MOBS_PER_PLAYER = Route.fromString(toRoutePath(SPAWN_HEADER, "max-active-mobs-per-player"));

    // Mob pool (map-based — accessed dynamically by key)
    public static final Route MOB_POOL = Route.fromString(MOB_POOL_HEADER);
}
```

### 3.3 YAML Resource

**File:** `src/main/resources/fishing_mob_spawn_configuration.yml`

```yaml
config-version: 1

# Fishing Mob Spawn System
# When a player fishes repeatedly in the same area, a hidden spawn chance
# accumulates. Once triggered, a mob from the weighted pool is spawned
# near the fishing hook via MythicMobs.

spawn:
  # Master toggle for the fishing mob spawn system
  enabled: true

  # Starting spawn chance for a fresh player/session (0.0 = 0%)
  base-chance: 0.0

  # Maximum spawn chance cap (0.35 = 35%)
  max-chance: 0.35

  # Chance increase per catch when fishing in the same area
  chance-increment-per-catch: 0.02

  # Chance decrease per catch when fishing in a NEW area (> same-area-range blocks away)
  chance-decrement-per-catch: 0.05

  # Distance (blocks) within which two hook locations are considered "same area"
  same-area-range: 10

  # Spawn chance is reset to this value after a fishing mob is killed
  post-kill-chance: 0.0

  # Whether to fully reset spawn chance on world change
  reset-on-world-change: true

  # Horizontal offset from the hook location for mob spawn (blocks)
  spawn-offset-from-hook: 3.0

  # Vertical offset above the hook for mob spawn (blocks)
  spawn-y-offset: 1.0

  # Maximum number of fishing mobs one player can have alive simultaneously
  max-active-mobs-per-player: 1

# Weighted mob pool — when a spawn triggers, one mob is randomly selected
# from all eligible entries. "Eligible" means the player's accumulated spawn
# chance meets the entry's min-chance-threshold AND the hook location passes
# the entry's biome/world/region restrictions.
#
# Selection is weighted: if two mobs are both eligible, the one with the
# higher weight is proportionally more likely to be chosen. For example,
# with weights 3 and 1, the first mob is picked ~75% of the time.
#
# Each entry is keyed by a unique name (used for logging and debugging).
# The key does NOT need to match the MythicMobs mob ID.
#
# Deny lists take priority over allow lists. If both are specified for the
# same dimension (biomes, worlds, regions), the deny list is checked first.
# An empty list means "no restriction" for that dimension.
#
# Despawn behavior (max lifetime, threat table cleanup) is configured in the
# MythicMobs mob YAML via ~onTimer and ~onDropCombat skills, NOT here.
mob-pool:
  riptide-guardian:
    # The MythicMobs internal mob type ID to spawn. Must match the mob's
    # filename/ID in your MythicMobs mobs/ directory.
    mythicmobs-mob-id: "RiptideGuardian"

    # Relative weight for random selection when multiple mobs are eligible.
    # Higher weight = more likely to be chosen. With two entries at weights
    # 1 and 3, the first is picked 25% of the time and the second 75%.
    weight: 1

    # Minimum accumulated spawn chance required before this mob becomes
    # eligible. Use this to gate stronger mobs behind higher thresholds,
    # creating a natural progression (e.g. weak scouts at 0.0, guardians
    # at 0.10, bosses at 0.25).
    # Range: 0.0 (always eligible) to max-chance (only at cap).
    min-chance-threshold: 0.10

    # MythicMobs mob level passed to MM's spawn API. MM uses this for its
    # own level-scaling system (stat scaling, skill scaling, drop scaling).
    # See: https://git.lumine.io/mythiccraft/MythicMobs/-/wikis/Mobs/Levels
    mob-level: 1.0

    # Biome restrictions — controls which biomes this mob can spawn in.
    # Uses Bukkit biome names (e.g. OCEAN, DEEP_OCEAN, RIVER, BEACH).
    # Full list: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html
    #
    # allowed-biomes: only spawn in these biomes (empty = all biomes allowed)
    # denied-biomes:  never spawn in these biomes (takes priority over allowed)
    allowed-biomes: []
    denied-biomes: []

    # World restrictions — controls which worlds this mob can spawn in.
    # Uses the world folder name (e.g. "world", "world_nether", "world_the_end").
    #
    # allowed-worlds: only spawn in these worlds (empty = all worlds allowed)
    # denied-worlds:  never spawn in these worlds (takes priority over allowed)
    allowed-worlds: []
    denied-worlds: []

    # WorldGuard region restrictions — controls which regions this mob can
    # spawn in. Requires WorldGuard to be installed; silently ignored if absent.
    # Uses the region ID as defined in WorldGuard (e.g. "spawn", "arena", "shop").
    #
    # allowed-regions: only spawn inside these regions (empty = all regions allowed)
    # denied-regions:  never spawn inside these regions (takes priority over allowed)
    allowed-regions: []
    denied-regions: []

  # Example: a weaker scout mob that spawns more frequently in oceans.
  # Uncomment and configure a matching MythicMobs mob to use.
  #
  # tide-scout:
  #   mythicmobs-mob-id: "TideScout"
  #   weight: 3                    # 3x more likely than the guardian when both are eligible
  #   min-chance-threshold: 0.0    # eligible immediately — no chance buildup needed
  #   mob-level: 1.0
  #   allowed-biomes: ["OCEAN", "DEEP_OCEAN", "WARM_OCEAN", "LUKEWARM_OCEAN", "COLD_OCEAN"]
  #   denied-biomes: []
  #   allowed-worlds: []
  #   denied-worlds: ["world_nether", "world_the_end"]
  #   allowed-regions: []
  #   denied-regions: ["spawn"]
```

### 3.4 Design Decisions

**Single file instead of directory:** The fishing mob spawn system has exactly one config with a single mob pool. Server owners customize the pool entries, not the file count. A single `FileType` entry keeps things simple.

**Map-based mob pool keys:** The mob pool uses explicit named keys (`riptide-guardian:`, `tide-scout:`) instead of a YAML list format. This is more readable for non-technical server owners — each mob has a clear label, and entries can be commented out individually without disrupting list ordering.

**Per-mob restrictions instead of global:** Biome, world, and region restrictions live on each mob pool entry rather than globally on the spawn system. This allows different mobs to spawn in different contexts (e.g., ocean-only guardians alongside river-only scouts). Deny lists take priority over allow lists.

**WorldGuard region support:** Region restrictions are optional and only evaluated when the WorldGuard hook is registered. If WorldGuard is not present, region restrictions are silently ignored. This follows the existing `WorldGuardHook` pattern.

---

## 4. Mob Pool Data Model

**Package:** `us.eunoians.mcrpg.fishing`

### 4.1 MobPoolEntry

**File:** `src/main/java/us/eunoians/mcrpg/fishing/MobPoolEntry.java`

```java
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
```

### 4.2 MobPoolSelector

**File:** `src/main/java/us/eunoians/mcrpg/fishing/MobPoolSelector.java`

Instantiable weighted random selector. Constructed with the pool; filters by spawn chance threshold and location restrictions on each call.

```java
package us.eunoians.mcrpg.fishing;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;
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
                                          @org.jetbrains.annotations.Nullable WorldGuardHook worldGuardHook) {
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
                                       @org.jetbrains.annotations.Nullable WorldGuardHook worldGuardHook) {
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
```

> **WorldGuard API Note:** The `WorldGuardHook.getRegionIds(Location)` method is assumed to return a `Set<String>` of region IDs at a location. If this method does not exist, it should be added to `WorldGuardHook` following the existing pattern of `WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(location).getRegions()`.

---

## 5. MythicMobs Integration

All MythicMobs API usage is centralized on `MythicMobsHook`. No other class in this LLD imports MM types. This follows the same pattern as `GeyserHook.isBedrockPlayer()` and `LunarClientHook.displayCooldown()` — hooks provide domain-specific utility methods wrapping the external API.

### 5.1 Modified Class: MythicMobsHook

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsHook.java`

The existing hook gains two methods:

```java
/**
 * Spawns a MythicMobs mob at the given location.
 *
 * @param mythicMobsId the MM internal type ID
 * @param location     the Bukkit location to spawn at
 * @param mobLevel     the MM mob level
 * @return the spawned entity, or empty if the type ID is not registered in MM
 */
@NotNull
public Optional<Entity> spawnMob(@NotNull String mythicMobsId,
                                  @NotNull Location location,
                                  double mobLevel) {
    Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mythicMobsId);
    if (mythicMob.isEmpty()) {
        getPlugin().getLogger().warning("MythicMob type '" + mythicMobsId
                + "' not found in MythicMobs registry.");
        return Optional.empty();
    }

    ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), mobLevel);
    return Optional.of(activeMob.getEntity().getBukkitEntity());
}

/**
 * Checks whether a MythicMobs mob type ID is registered.
 *
 * @param mythicMobsId the MM internal type ID
 * @return true if the type exists in the MM registry
 */
public boolean isMobTypeRegistered(@NotNull String mythicMobsId) {
    return MythicBukkit.inst().getMobManager().getMythicMob(mythicMobsId).isPresent();
}
```

### 5.2 Migration Plan for Existing Code

The existing `MythicMobsListener` also imports MM types directly (for event handling). Those imports are acceptable — the listener must reference `MythicMobSpawnEvent`, `MythicMobDeathEvent`, and `MythicDropLoadEvent` because they are Bukkit event types that must appear in `@EventHandler` signatures. The hook centralizes **outbound API calls** (spawn, query), not inbound event types.

---

## 6. Per-Player State

### 6.1 PlayerFishingState

**File:** `src/main/java/us/eunoians/mcrpg/fishing/PlayerFishingState.java`

Session-only fishing state stored as a field on `McRPGPlayer`. Discarded on logout, optionally reset on world change. No database persistence.

```java
package us.eunoians.mcrpg.fishing;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Session-only fishing state for a single player. Tracks the accumulated
 * spawn chance, last hook location, and the set of currently active fishing
 * mob UUIDs.
 * <p>
 * This state is <strong>never persisted</strong> to the database. It is
 * discarded when the player logs out and optionally reset on world change.
 * <p>
 * Created lazily via {@link McRPGPlayer#getOrCreateFishingState(double)}
 * on the player's first catch of the session.
 */
public class PlayerFishingState {

    private double currentSpawnChance;
    private Location lastHookLocation;
    private final Set<UUID> activeMobUUIDs;

    /**
     * Creates a new player fishing state with the given initial spawn chance.
     *
     * @param initialChance the starting spawn chance, typically the {@code base-chance}
     *                      value from the fishing mob spawn configuration (0.0–1.0)
     */
    public PlayerFishingState(double initialChance) {
        this.currentSpawnChance = initialChance;
        this.lastHookLocation = null;
        this.activeMobUUIDs = new HashSet<>();
    }

    /**
     * Gets the player's current accumulated spawn chance.
     * <p>
     * This value increases when the player fishes in the same area and
     * decreases when they move to a new area.
     *
     * @return the current spawn chance, between {@code 0.0} and the configured {@code max-chance}
     */
    public double getCurrentSpawnChance() {
        return currentSpawnChance;
    }

    /**
     * Sets the player's current spawn chance.
     *
     * @param chance the new spawn chance value
     */
    public void setCurrentSpawnChance(double chance) {
        this.currentSpawnChance = chance;
    }

    /**
     * Gets the last known hook location for this player.
     * <p>
     * Used to determine whether consecutive casts are in the "same area"
     * (within {@code same-area-range} blocks). Empty if the player has not
     * yet cast or if the location was cleared on world change.
     *
     * @return the last hook location, or empty if no fishing has occurred yet
     */
    @NotNull
    public Optional<Location> getLastHookLocation() {
        return Optional.ofNullable(lastHookLocation);
    }

    /**
     * Sets the last known hook location. Pass {@code null} to clear
     * (e.g. on world change).
     *
     * @param location the hook location, or {@code null} to clear
     */
    public void setLastHookLocation(@NotNull Optional<Location> location) {
        this.lastHookLocation = location.orElse(null);
    }

    /**
     * Clears the last known hook location.
     */
    public void clearLastHookLocation() {
        this.lastHookLocation = null;
    }

    /**
     * Adds a mob UUID to the set of active fishing mobs owned by this player.
     *
     * @param mobUUID the entity UUID of the spawned fishing mob
     */
    public void addActiveMob(@NotNull UUID mobUUID) {
        activeMobUUIDs.add(mobUUID);
    }

    /**
     * Removes a mob UUID from the active set. Called when the mob dies or despawns.
     *
     * @param mobUUID the entity UUID of the mob to remove
     * @return {@code true} if the mob was in the active set and was removed
     */
    public boolean removeActiveMob(@NotNull UUID mobUUID) {
        return activeMobUUIDs.remove(mobUUID);
    }

    /**
     * Gets the number of currently active fishing mobs for this player.
     * <p>
     * Compared against {@code max-active-mobs-per-player} to determine
     * whether new spawns are allowed.
     *
     * @return the active mob count (zero or greater)
     */
    public int getActiveMobCount() {
        return activeMobUUIDs.size();
    }

    /**
     * Gets an unmodifiable view of the active fishing mob UUIDs.
     *
     * @return an unmodifiable set of active mob entity UUIDs
     */
    @NotNull
    public Set<UUID> getActiveMobUUIDs() {
        return Collections.unmodifiableSet(activeMobUUIDs);
    }
}
```

### 6.2 Storage on McRPGPlayer

**Modified file:** `us.eunoians.mcrpg.entity.player.McRPGPlayer`

`PlayerFishingState` is stored as a session-only field on `McRPGPlayer`, following the same pattern as `standingInSafeZone` — a transient field that is never persisted to the database.

```java
// In McRPGPlayer field declarations
private PlayerFishingState fishingState;

/**
 * Gets the player's fishing state, creating it lazily if needed.
 * <p>
 * This is the primary entry point for the fishing mob spawn system.
 * On the player's first catch of the session, this creates a fresh
 * {@link PlayerFishingState} with the given initial chance.
 *
 * @param initialChance the initial spawn chance if state needs to be created
 *                      (typically {@code base-chance} from config)
 * @return the player's fishing state, never empty
 */
@NotNull
public PlayerFishingState getOrCreateFishingState(double initialChance) {
    if (fishingState == null) {
        fishingState = new PlayerFishingState(initialChance);
    }
    return fishingState;
}

/**
 * Gets the player's fishing state if it exists.
 *
 * @return the fishing state, or empty if the player has not fished this session
 */
@NotNull
public Optional<PlayerFishingState> getFishingState() {
    return Optional.ofNullable(fishingState);
}

/**
 * Resets the player's fishing state. Called on logout or when a full
 * reset is needed (e.g. world change with reset enabled).
 */
public void resetFishingState() {
    this.fishingState = null;
}
```

### 6.3 State Lifecycle

| Event | State Change |
|---|---|
| Player logs in | No state created (lazy on first catch) |
| First catch | State created via `getOrCreateFishingState(baseChance)` |
| Same-area catch | `currentSpawnChance += increment` (capped at `max-chance`) |
| New-area catch | `currentSpawnChance -= decrement` (floored at `base-chance`) |
| Spawn succeeds | `currentSpawnChance = base-chance`, mob UUID added to `activeMobUUIDs` |
| Mob dies | UUID removed from `activeMobUUIDs`, `currentSpawnChance = post-kill-chance` |
| Mob despawns | UUID removed from `activeMobUUIDs` (via death event — MM fires death on despawn too) |
| Player logs out | `resetFishingState()` called by existing player cleanup |
| World change | `lastHookLocation = null`, optionally `currentSpawnChance = base-chance` |

### 6.4 Why Store on McRPGPlayer?

Storing per-player state on the player object rather than a separate `Map<UUID, State>` on the listener:
1. **Follows existing patterns** — `standingInSafeZone`, `readiedAbility`, `activeBoardQuestCount` are all per-player session state stored on the player/holder object
2. **Natural lifecycle** — state is cleaned up automatically when the player object is discarded on logout
3. **No cross-referencing** — the listener doesn't need to maintain and clean up a parallel map
4. **Accessible elsewhere** — other systems (e.g., future fishing skill XP) can read the state without coupling to the listener

---

## 7. FishingMobSpawnListener

**File:** `src/main/java/us/eunoians/mcrpg/listener/fishing/FishingMobSpawnListener.java`

A pure Bukkit `Listener` — no state storage. Per-player state lives on `McRPGPlayer`. Mob pool and config values are accessed via `ReloadableContent`.

```java
package us.eunoians.mcrpg.listener.fishing;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.FishingMobSpawnConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.fishing.FishingMobDeathEvent;
import us.eunoians.mcrpg.event.fishing.FishingMobSpawnChanceUpdateEvent;
import us.eunoians.mcrpg.external.mythicmobs.FishingMobKeys;
import us.eunoians.mcrpg.external.mythicmobs.MythicMobsHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;
import us.eunoians.mcrpg.fishing.MobPoolEntry;
import us.eunoians.mcrpg.fishing.MobPoolSelector;
import us.eunoians.mcrpg.fishing.PlayerFishingState;
import us.eunoians.mcrpg.fishing.ReloadableMobPool;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listens to fishing events and triggers mob spawns via MythicMobs when the
 * player's accumulated spawn chance succeeds.
 * <p>
 * Per-player state is stored on {@link McRPGPlayer#getFishingState()}.
 * The mob pool is loaded via {@link ReloadableMobPool} and refreshes on
 * {@code /mcrpg admin reload}.
 * <p>
 * This listener is only registered when MythicMobs is present and the
 * fishing mob spawn system is enabled in config.
 */
public class FishingMobSpawnListener implements Listener {

    private final McRPG plugin;
    private final ReloadableMobPool reloadableMobPool;

    public FishingMobSpawnListener(@NotNull McRPG plugin, @NotNull ReloadableMobPool reloadableMobPool) {
        this.plugin = plugin;
        this.reloadableMobPool = reloadableMobPool;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(@NotNull PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH
                && event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) {
            return;
        }

        Player player = event.getPlayer();
        if (event.getHook() == null) {
            return;
        }

        Optional<McRPGPlayer> mcRPGPlayerOpt = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }

        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        Location hookLocation = event.getHook().getLocation();

        PlayerFishingState state = mcRPGPlayer.getOrCreateFishingState(getBaseChance());

        // Check active mob cap
        int maxActiveMobs = getConfig().getInt(FishingMobSpawnConfigFile.MAX_ACTIVE_MOBS_PER_PLAYER, 1);
        if (state.getActiveMobCount() >= maxActiveMobs) {
            return;
        }

        // Update chance based on proximity to last hook
        updateSpawnChance(player, state, hookLocation);
        state.setLastHookLocation(Optional.of(hookLocation));

        // Roll for spawn
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < state.getCurrentSpawnChance()) {
            attemptSpawnMob(player, state, hookLocation);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishingMobDeath(@NotNull FishingMobDeathEvent event) {
        UUID mobUUID = event.getMob().getUniqueId();
        double postKillChance = getConfig().getDouble(FishingMobSpawnConfigFile.POST_KILL_CHANCE, 0.0);

        // Find the player who owns this mob via the angler UUID PDC tag
        // (more efficient than iterating all players)
        String anglerUuidString = event.getMob().getPersistentDataContainer()
                .get(FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING);

        if (anglerUuidString == null) {
            return;
        }

        UUID anglerUUID = UUID.fromString(anglerUuidString);
        plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(anglerUUID)
                .ifPresent(mcRPGPlayer -> mcRPGPlayer.getFishingState().ifPresent(state -> {
                    if (state.removeActiveMob(mobUUID)) {
                        state.setCurrentSpawnChance(postKillChance);
                    }
                }));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(event.getPlayer().getUniqueId())
                .ifPresent(mcRPGPlayer -> mcRPGPlayer.getFishingState().ifPresent(state -> {
                    boolean resetOnWorldChange = getConfig().getBoolean(
                            FishingMobSpawnConfigFile.RESET_ON_WORLD_CHANGE, true);
                    if (resetOnWorldChange) {
                        state.setCurrentSpawnChance(getBaseChance());
                    }
                    state.clearLastHookLocation();
                }));
    }

    private void updateSpawnChance(@NotNull Player player,
                                    @NotNull PlayerFishingState state,
                                    @NotNull Location hookLocation) {
        double oldChance = state.getCurrentSpawnChance();
        double newChance;

        Optional<Location> lastHookOpt = state.getLastHookLocation();
        double sameAreaRange = getConfig().getDouble(FishingMobSpawnConfigFile.SAME_AREA_RANGE, 10.0);

        if (lastHookOpt.isEmpty() || !lastHookOpt.get().getWorld().equals(hookLocation.getWorld())) {
            newChance = oldChance;
        } else {
            double distance = lastHookOpt.get().distance(hookLocation);
            if (distance <= sameAreaRange) {
                double increment = getConfig().getDouble(FishingMobSpawnConfigFile.CHANCE_INCREMENT_PER_CATCH, 0.02);
                double maxChance = getConfig().getDouble(FishingMobSpawnConfigFile.MAX_CHANCE, 0.35);
                newChance = Math.min(oldChance + increment, maxChance);
            } else {
                double decrement = getConfig().getDouble(FishingMobSpawnConfigFile.CHANCE_DECREMENT_PER_CATCH, 0.05);
                newChance = Math.max(oldChance - decrement, getBaseChance());
            }
        }

        FishingMobSpawnChanceUpdateEvent updateEvent = new FishingMobSpawnChanceUpdateEvent(
                player, oldChance, newChance, hookLocation);
        Bukkit.getPluginManager().callEvent(updateEvent);

        if (!updateEvent.isCancelled()) {
            state.setCurrentSpawnChance(updateEvent.getNewChance());
        }
    }

    private void attemptSpawnMob(@NotNull Player player,
                                  @NotNull PlayerFishingState state,
                                  @NotNull Location hookLocation) {
        MobPoolSelector selector = reloadableMobPool.getContent();

        // Resolve WorldGuard hook (nullable — region checks are skipped if absent)
        WorldGuardHook worldGuardHook = plugin.registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.WORLD_GUARD)
                .orElse(null);

        Optional<MobPoolEntry> selected = selector.select(
                state.getCurrentSpawnChance(), hookLocation, worldGuardHook);

        if (selected.isEmpty()) {
            return;
        }

        MobPoolEntry entry = selected.get();
        Location spawnLocation = calculateSpawnLocation(hookLocation);

        // Spawn via MythicMobsHook
        MythicMobsHook mmHook = plugin.registryAccess()
                .registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.MYTHIC_MOBS)
                .orElse(null);

        if (mmHook == null) {
            return;
        }

        Optional<Entity> entityOpt = mmHook.spawnMob(entry.mythicMobsId(), spawnLocation, entry.mobLevel());
        if (entityOpt.isEmpty()) {
            return;
        }

        Entity entity = entityOpt.get();

        // Tag the entity with PDC keys so MythicMobsListener can identify it
        entity.getPersistentDataContainer().set(
                FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN, true);
        entity.getPersistentDataContainer().set(
                FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING, player.getUniqueId().toString());

        state.addActiveMob(entity.getUniqueId());
        state.setCurrentSpawnChance(getBaseChance());

        plugin.getLogger().fine("Spawned fishing mob '" + entry.mythicMobsId()
                + "' for player " + player.getName() + " at " + spawnLocation);
    }

    @NotNull
    private Location calculateSpawnLocation(@NotNull Location hookLocation) {
        double offset = getConfig().getDouble(FishingMobSpawnConfigFile.SPAWN_OFFSET_FROM_HOOK, 3.0);
        double yOffset = getConfig().getDouble(FishingMobSpawnConfigFile.SPAWN_Y_OFFSET, 1.0);

        double angle = ThreadLocalRandom.current().nextDouble(2 * Math.PI);
        double x = hookLocation.getX() + offset * Math.cos(angle);
        double z = hookLocation.getZ() + offset * Math.sin(angle);
        double y = hookLocation.getY() + yOffset;

        return new Location(hookLocation.getWorld(), x, y, z);
    }

    private double getBaseChance() {
        return getConfig().getDouble(FishingMobSpawnConfigFile.BASE_CHANCE, 0.0);
    }

    @NotNull
    private YamlDocument getConfig() {
        return plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.FISHING_MOB_SPAWN_CONFIG);
    }
}
```

### Key Design Decisions

**Pure listener, no state:** The listener holds no per-player state. All mutable state lives on `McRPGPlayer.fishingState`. The listener's only field beyond `plugin` is the `ReloadableMobPool`, which is an immutable-per-reload config wrapper.

**No `PlayerQuitEvent` handler needed:** Since `PlayerFishingState` lives on `McRPGPlayer`, it is automatically discarded when the player object is cleaned up on logout. The existing `PlayerLeaveListener` handles player object teardown.

**Death callback uses PDC angler UUID:** Instead of iterating all players to find who owns a mob, the listener reads `ANGLER_UUID_KEY` from the dead mob's PDC and looks up that specific player. More efficient and O(1) instead of O(n).

---

## 8. Spawn Flow

Full sequence from catch to mob appearing:

```
Player catches fish (PlayerFishEvent CAUGHT_FISH/CAUGHT_ENTITY)
  -> FishingMobSpawnListener.onPlayerFish()
    -> Look up McRPGPlayer from player manager
    -> Get or create PlayerFishingState on the player object
    -> Check: active mob cap reached?
    -> Update spawn chance based on hook proximity
    -> Fire FishingMobSpawnChanceUpdateEvent (cancellable)
    -> Roll against currentSpawnChance
    -> On success:
      -> reloadableMobPool.getContent().select() — weighted random from eligible entries
        -> Filters by chance threshold, biome, world, WorldGuard region
      -> Calculate spawn location (offset from hook)
      -> MythicMobsHook.spawnMob() — centralized MM API call
      -> Tag entity with FISHING_MOB_KEY + ANGLER_UUID_KEY (PDC)
      -> state.addActiveMob(entityUUID)
      -> state.setCurrentSpawnChance(baseChance)
      -> [MythicMobs fires MythicMobSpawnEvent internally]
        -> MythicMobsListener.onMythicMobSpawn() (existing code)
          -> Reads PDC tags, finds angler
          -> Fires FishingMobSpawnEvent (cancellable)
          -> If cancelled: entity.remove()
```

### Why PDC Tags Instead of a Tracking Map?

The existing LLD-1 implementation uses PDC tags on entities to identify fishing mobs. This approach:
1. Survives server restarts (PDC is persisted by Minecraft)
2. Doesn't require cross-system coordination (the listener reads tags independently)
3. Is already implemented and working
4. Enables efficient death callback (read angler UUID directly from mob PDC)

---

## 9. Death Callback Integration

When a fishing mob dies:

```
MythicMobDeathEvent (from MythicMobs)
  -> MythicMobsListener.onMythicMobDeath() [existing code]
    -> Reads PDC: FISHING_MOB_KEY present?
    -> Fires FishingMobDeathEvent
      -> FishingMobSpawnListener.onFishingMobDeath() [new code]
        -> Reads ANGLER_UUID_KEY from mob's PDC
        -> Looks up McRPGPlayer by angler UUID
        -> Removes mob UUID from player's fishingState.activeMobUUIDs
        -> Resets currentSpawnChance to post-kill-chance
```

The integration is event-driven. The listener only consumes McRPG-domain `FishingMobDeathEvent`, not `MythicMobDeathEvent`.

### Death vs. Despawn

When MythicMobs despawns a mob (via `~onTimer`, `~onDropCombat`, or `ActiveMob.despawn()`), it fires `MythicMobDeathEvent`. The existing `MythicMobsListener` checks PDC tags, which survive despawn, so `FishingMobDeathEvent` is fired for both death and despawn. The listener handles both identically — remove from tracking state.

---

## 10. Custom Events

### 10.1 FishingMobSpawnChanceUpdateEvent (New)

**File:** `src/main/java/us/eunoians/mcrpg/event/fishing/FishingMobSpawnChanceUpdateEvent.java`

Fired when a player's fishing mob spawn chance changes. Cancellable — if cancelled, the chance remains unchanged. Third-party plugins can also modify the new chance via `setNewChance(double)`.

```java
package us.eunoians.mcrpg.event.fishing;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FishingMobSpawnChanceUpdateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final double oldChance;
    private double newChance;
    private final Location hookLocation;
    private boolean cancelled = false;

    public FishingMobSpawnChanceUpdateEvent(@NotNull Player player, double oldChance,
                                             double newChance, @NotNull Location hookLocation) {
        this.player = player;
        this.oldChance = oldChance;
        this.newChance = newChance;
        this.hookLocation = hookLocation;
    }

    @NotNull public Player getPlayer() { return player; }
    public double getOldChance() { return oldChance; }
    public double getNewChance() { return newChance; }
    public void setNewChance(double newChance) { this.newChance = newChance; }
    @NotNull public Location getHookLocation() { return hookLocation; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return handlers; }
    @NotNull public static HandlerList getHandlerList() { return handlers; }
}
```

### 10.2 Existing Events (No Changes)

| Event | Fired By | Consumed By |
|---|---|---|
| `FishingMobSpawnEvent` | `MythicMobsListener` | Third-party plugins (cancellable) |
| `FishingMobDeathEvent` | `MythicMobsListener` | `FishingMobSpawnListener` (cleanup) |

### 10.3 Event Summary

| Event | When | Cancellable | Mutable Fields |
|---|---|---|---|
| `FishingMobSpawnChanceUpdateEvent` | Before spawn chance changes | Yes | `newChance` |
| `FishingMobSpawnEvent` (existing) | After MM spawns a tagged mob | Yes (removes entity) | -- |
| `FishingMobDeathEvent` (existing) | After a tagged mob dies | No | -- |

---

## 11. Bootstrap Registration

### 11.1 ReloadableMobPool

**File:** `src/main/java/us/eunoians/mcrpg/fishing/ReloadableMobPool.java`

A `ReloadableContent<MobPoolSelector>` that parses the mob pool from config and constructs a fresh `MobPoolSelector` on each reload.

```java
package us.eunoians.mcrpg.fishing;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.FishingMobSpawnConfigFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import us.eunoians.mcrpg.McRPG;

/**
 * A {@link ReloadableContent} that wraps the fishing mob pool configuration.
 * On reload, re-parses the {@code mob-pool} YAML section and constructs a
 * fresh {@link MobPoolSelector}.
 * <p>
 * Registered with the {@code ReloadableContentManager} at startup so that
 * {@code /mcrpg admin reload} picks up pool changes without a server restart.
 */
public class ReloadableMobPool extends ReloadableContent<MobPoolSelector> {

    public ReloadableMobPool(@NotNull YamlDocument config) {
        super(config, FishingMobSpawnConfigFile.MOB_POOL, (doc, route) -> {
            List<MobPoolEntry> entries = parseMobPool(doc);
            return new MobPoolSelector(entries);
        });
    }

    @NotNull
    private static List<MobPoolEntry> parseMobPool(@NotNull YamlDocument config) {
        Logger logger = McRPG.getInstance().getLogger();
        if (!config.contains(FishingMobSpawnConfigFile.MOB_POOL)) {
            logger.warning("No mob-pool section found in fishing mob spawn configuration.");
            return Collections.emptyList();
        }

        // Map-based: each key under mob-pool is a named entry
        var poolSection = config.getSection(FishingMobSpawnConfigFile.MOB_POOL);
        if (poolSection == null) {
            logger.warning("Mob pool section is empty in fishing mob spawn configuration.");
            return Collections.emptyList();
        }

        List<MobPoolEntry> entries = new ArrayList<>();

        for (Object keyObj : poolSection.getKeys()) {
            String key = keyObj.toString();
            var entrySection = poolSection.getSection(key);
            if (entrySection == null) {
                logger.warning("Mob pool entry '" + key + "' is not a valid section, skipping.");
                continue;
            }

            String mobId = entrySection.getString("mythicmobs-mob-id");
            if (mobId == null || mobId.isBlank()) {
                logger.warning("Mob pool entry '" + key + "' missing 'mythicmobs-mob-id', skipping.");
                continue;
            }

            int weight = entrySection.getInt("weight", 1);
            if (weight <= 0) {
                logger.warning("Mob pool entry '" + key + "' has weight <= 0, skipping.");
                continue;
            }

            double minThreshold = entrySection.getDouble("min-chance-threshold", 0.0);
            double mobLevel = entrySection.getDouble("mob-level", 1.0);

            Set<String> allowedBiomes = toStringSet(entrySection.getStringList("allowed-biomes"));
            Set<String> deniedBiomes = toStringSet(entrySection.getStringList("denied-biomes"));
            Set<String> allowedWorlds = toStringSet(entrySection.getStringList("allowed-worlds"));
            Set<String> deniedWorlds = toStringSet(entrySection.getStringList("denied-worlds"));
            Set<String> allowedRegions = toStringSet(entrySection.getStringList("allowed-regions"));
            Set<String> deniedRegions = toStringSet(entrySection.getStringList("denied-regions"));

            entries.add(new MobPoolEntry(key, mobId, weight, minThreshold, mobLevel,
                    allowedBiomes, deniedBiomes, allowedWorlds, deniedWorlds,
                    allowedRegions, deniedRegions));

            logger.info("Loaded mob pool entry: " + key + " -> " + mobId
                        + " (weight=" + weight + ", threshold=" + minThreshold + ")");
        }

        return Collections.unmodifiableList(entries);
    }

    @NotNull
    private static Set<String> toStringSet(@org.jetbrains.annotations.Nullable List<String> list) {
        if (list == null || list.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(list);
    }
}
```

### 11.2 Listener Registration

**Modified file:** `us.eunoians.mcrpg.bootstrap.McRPGListenerRegistrar`

```java
// Fishing mob spawn listener (requires MythicMobs + enabled in config)
if (plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.MYTHIC_MOBS).isPresent()) {
    YamlDocument fishingConfig = plugin.registryAccess()
            .registry(RegistryKey.MANAGER)
            .manager(McRPGManagerKey.FILE)
            .getFile(FileType.FISHING_MOB_SPAWN_CONFIG);

    if (fishingConfig.getBoolean(FishingMobSpawnConfigFile.SPAWN_ENABLED, true)) {
        ReloadableMobPool reloadableMobPool = new ReloadableMobPool(fishingConfig);

        // Register for reload tracking
        plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(Set.of(reloadableMobPool));

        Bukkit.getPluginManager().registerEvents(
                new FishingMobSpawnListener(plugin, reloadableMobPool), plugin);
    }
}
```

### 11.3 Registration Order

Note: The listener registrar runs before the hooks registrar in `McRPGBootstrap`. The hook check in the listener registrar depends on the hook being present. This is a potential latent bug — see the existing `MythicMobsListener` registration which has the same dependency.

**Fix:** Swap the registration order in `McRPGBootstrap`:

```java
new McRPGHooksRegistrar().register(bootstrapContext);      // Hooks FIRST
new McRPGListenerRegistrar().register(bootstrapContext);    // Listeners AFTER
```

> **Implementation note:** Verify on a live server whether the existing order works despite the apparent dependency. If so, document the reason; if not, apply the swap.

---

## 12. Anti-Cheese Analysis

| Strategy | Behavior | Outcome |
|---|---|---|
| **AFK same spot** | Chance accumulates: +0.02 per catch | Mob eventually spawns — **intended behavior** (mob is the anti-AFK measure) |
| **Small movement (1-2 blocks)** | Still within `same-area-range` (10 blocks) | Same as AFK — chance still accumulates |
| **Alternating two spots (>10 blocks apart)** | Alternates +0.02 and -0.05 per catch | Net -0.03 per cycle -> **defeats the exploit** |
| **Teleporting between distant spots** | Each teleport triggers decrement | Rapid chance decay -> **defeats the exploit** |
| **Fishing in new area then returning** | Decrement away, increment on return | Player loses progress — must rebuild chance |
| **Multiple players in same area** | Fully independent tracking per player | No sharing or amplification |
| **Kill mob, immediately fish again** | Chance resets to `post-kill-chance` (0.0) | Must rebuild from scratch — **intended pacing** |
| **Ignore mob, keep fishing** | `max-active-mobs-per-player` cap (default 1) | No more spawns until mob dies/despawns |
| **Lure mob to pen/trap** | MM's `~onTimer` forces despawn after TTL | **MM handles this natively** |
| **All players leave area** | MM's `~onDropCombat` empties ThreatTable -> despawn | **MM handles this natively** |

### Tuning Levers

McRPG config controls spawn behavior:

| Config Key | Anti-Cheese Role |
|---|---|
| `same-area-range` | Higher = more lenient, lower = stricter AFK detection |
| `chance-increment-per-catch` | Controls how fast the mob spawns during AFK |
| `chance-decrement-per-catch` | Controls how strongly movement resets progress |
| `post-kill-chance` | Higher = faster re-spawns after kills |
| `max-active-mobs-per-player` | Prevents mob stacking |

MythicMobs mob config controls despawn:

| MM Config | Anti-Cheese Role |
|---|---|
| `~onTimer` + `remove` | Max lifetime — prevents mob storage |
| `~onDropCombat` + `remove` | Cleans up abandoned mobs |

---

## 13. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|---|---|
| MythicMobs not installed | Hook not registered -> listener not registered -> system is inert |
| MM installed but mob type not registered | `MythicMobsHook.spawnMob()` returns empty -> warning logged -> spawn fails silently |
| System disabled in config | Listener not registered -> system is inert |
| Empty mob pool | `MobPoolSelector.select()` returns empty -> no spawn -> no error |
| All pool entries filtered by threshold | Same as empty pool |
| All pool entries filtered by location restrictions | Same as empty pool for that location |
| WorldGuard not installed | Region restrictions silently ignored (null hook) |
| Player logs out with active mob | `McRPGPlayer` teardown discards `fishingState`. Mob lives under MM rules. MM `~onTimer`/`~onDropCombat` handle cleanup |
| Server restart with living mob | MM persists the mob. `McRPGPlayer` has fresh state (no `fishingState`). Mob lives under MM rules |
| Catch event with null hook | Early return in `onPlayerFish()` |
| World change | `lastHookLocation` nulled. Optionally resets chance |
| Multiple rapid catches | Each processed independently. Chance accumulates normally |
| Config reload (`/mcrpg admin reload`) | `ReloadableMobPool` re-parses mob pool. New `MobPoolSelector` constructed. Takes effect on next spawn trigger. Existing per-player state preserved |

---

## 14. Test Plan

### 14.1 Unit Tests (src/test/java)

| Test Class | Tests |
|---|---|
| `MobPoolSelectorTest` | Weighted selection: single entry, multiple entries, all filtered by threshold, equal weights, zero total weight, empty pool, `hasEntries()`. Location filtering: biome allow/deny, world allow/deny, deny-takes-priority, WorldGuard region checks with mock hook, null WG hook skips regions |
| `PlayerFishingStateTest` | Initial state values, chance get/set, last hook location get/set/null, active mob add/remove/count, getActiveMobUUIDs immutability |
| `MobPoolEntryTest` | Record accessor correctness, set immutability |

### 14.2 Tests Requiring MockBukkit (extend McRPGBaseTest)

| Test Class | Tests |
|---|---|
| `FishingMobSpawnChanceUpdateEventTest` | Event creation, cancellation, newChance modification, handler list |
| `ReloadableMobPoolTest` | Parse valid pool entries from map format, missing mob ID (skipped), zero weight (skipped), missing fields use defaults, empty pool section, per-mob restriction parsing |

### 14.3 Manual Testing (Paper Server with MythicMobs)

| Scenario | Verification |
|---|---|
| Fish repeatedly in same spot | Chance accumulates. Eventually mob spawns near hook |
| Fish, then move far away and fish | Chance decreases |
| Kill spawned mob | Chance resets. Can rebuild again |
| Let mob live, keep fishing | No more spawns once cap reached |
| Log out with active mob | State discarded. Mob despawns via MM skills |
| Fish in denied world | No spawns for that mob pool entry |
| Fish in denied biome | No spawns for that mob pool entry |
| Fish in denied WorldGuard region | No spawns for that mob pool entry (WG required) |
| Disable system in config + reload | No fishing mob spawns |
| Empty mob pool | No spawns, no errors |
| MM not installed | System doesn't register, no errors |
| `/mcrpg admin reload` with changed pool | New pool entries take effect |
| Server restart with living mob | Mob persists (MM), player has fresh state |

---

## 15. File Manifest

### New Files

| File | Type | Description |
|---|---|---|
| `listener/fishing/FishingMobSpawnListener.java` | Listener | Pure listener — fish/death/world events, delegates to player state |
| `fishing/PlayerFishingState.java` | Data | Per-player session state (chance, hook location, active mobs) |
| `fishing/MobPoolEntry.java` | Record | Single weighted mob pool entry with per-mob restrictions |
| `fishing/MobPoolSelector.java` | Selector | Instantiable weighted random selection with location filtering |
| `fishing/ReloadableMobPool.java` | Config | `ReloadableContent<MobPoolSelector>` — parses pool, reloads on command |
| `configuration/file/FishingMobSpawnConfigFile.java` | Config | Route constants for fishing mob spawn config |
| `event/fishing/FishingMobSpawnChanceUpdateEvent.java` | Event | Fired before spawn chance changes (cancellable) |
| `src/main/resources/fishing_mob_spawn_configuration.yml` | YAML | Default fishing mob spawn configuration |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

### Modified Files

| File | Change |
|---|---|
| `external/mythicmobs/MythicMobsHook.java` | Add `spawnMob()` and `isMobTypeRegistered()` methods |
| `entity/player/McRPGPlayer.java` | Add `fishingState` field with get/create/reset methods |
| `configuration/FileType.java` | Add `FISHING_MOB_SPAWN_CONFIG` entry |
| `bootstrap/McRPGListenerRegistrar.java` | Add conditional `FishingMobSpawnListener` registration + `ReloadableMobPool` tracking |
| `bootstrap/McRPGBootstrap.java` | **Potentially** swap hooks/listener registration order (see Section 11.3) |

### Not Modified (Used As-Is)

| File | Role |
|---|---|
| `external/mythicmobs/MythicMobsListener.java` | Bridges MM events -> fires `FishingMobSpawnEvent`/`FishingMobDeathEvent` |
| `external/mythicmobs/FishingMobKeys.java` | PDC key constants applied to spawned mobs |
| `external/worldguard/WorldGuardHook.java` | Region query (may need `getRegionIds()` method added) |
| `event/fishing/FishingMobSpawnEvent.java` | Cancellable spawn event (fired by `MythicMobsListener`) |
| `event/fishing/FishingMobDeathEvent.java` | Death event (consumed by listener for cleanup) |
