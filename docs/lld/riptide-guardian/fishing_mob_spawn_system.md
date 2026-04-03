# Low-Level Design: Fishing Mob Spawn System (LLD-2)

**Status:** Draft
**Date:** 2026-03-16
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Section 3
**Scope:** Spawn tracker, mob pool, per-player state, config file, custom events, MythicMobs spawning

---

## Table of Contents

1. [Overview](#1-overview)
2. [Existing Infrastructure (from LLD-1)](#2-existing-infrastructure-from-lld-1)
3. [Configuration](#3-configuration)
4. [Mob Pool Data Model](#4-mob-pool-data-model)
5. [FishingMobSpawnTracker](#5-fishingmobspawntracker)
6. [Per-Player State](#6-per-player-state)
7. [Spawn Flow](#7-spawn-flow)
8. [Death Callback Integration](#8-death-callback-integration)
9. [Custom Events](#9-custom-events)
10. [Bootstrap Registration](#10-bootstrap-registration)
11. [Anti-Cheese Analysis](#11-anti-cheese-analysis)
12. [Edge Cases & Graceful Degradation](#12-edge-cases--graceful-degradation)
13. [Test Plan](#13-test-plan)
14. [File Manifest](#14-file-manifest)

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

This LLD **depends on** those classes and does **not** modify them. Specifically:
- The tracker spawns mobs via MythicMobs API and tags them with `FishingMobKeys` PDC keys
- `MythicMobsListener` picks up the MM spawn/death events and fires `FishingMobSpawnEvent` / `FishingMobDeathEvent`
- The tracker listens to `FishingMobDeathEvent` to clean up per-player state

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

McRPG does **not** schedule despawn tasks or monitor the ThreatTable. Server owners configure despawn behavior in the MythicMobs mob YAML, not in McRPG config. This avoids duplicating MM's existing capabilities and keeps McRPG's scope limited to spawn triggering and state tracking.

---

## 2. Existing Infrastructure (from LLD-1)

These classes already exist in the codebase and are used by this LLD:

| Class | Location | Role in LLD-2 |
|---|---|---|
| `MythicMobsHook` | `external/mythicmobs/` | Presence check — if not registered, spawn calls are no-ops |
| `MythicMobsListener` | `external/mythicmobs/` | Bridges MM spawn/death events; fires `FishingMobSpawnEvent`/`FishingMobDeathEvent` |
| `FishingMobKeys` | `external/mythicmobs/` | PDC keys written to spawned entities for identification |
| `FishingMobSpawnEvent` | `event/fishing/` | Fired by `MythicMobsListener` when a tagged mob spawns — cancellable |
| `FishingMobDeathEvent` | `event/fishing/` | Fired by `MythicMobsListener` when a tagged mob dies — tracker listens to this |
| `McRPGSkillBookDrop` | `external/mythicmobs/` | MM custom drop type — not directly used but part of the end-to-end flow |

### MythicMobs API Usage

LLD-1 established that all MM API calls should be funneled through a facade. However, the current implementation calls MM API directly from the listener. For LLD-2, MythicMobs spawning is done via a thin helper method on `MythicMobsHook` rather than a full facade class, keeping the scope minimal. If a future LLD introduces `MythicMobsIntegration` as designed in the original LLD-1 spec, the spawn call should migrate there.

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
import org.jetbrains.annotations.NotNull;

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
    public static final Route REQUIRED_BIOMES = Route.fromString(toRoutePath(SPAWN_HEADER, "required-biomes"));
    public static final Route ALLOWED_WORLDS = Route.fromString(toRoutePath(SPAWN_HEADER, "allowed-worlds"));

    // Mob pool (list-based — accessed dynamically)
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

  # Biome restrictions (empty list = all biomes allowed)
  # Uses Bukkit biome names: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Biome.html
  required-biomes: []

  # World restrictions (empty list = all worlds allowed)
  allowed-worlds: []

# Weighted mob pool — on spawn trigger, one mob is selected by weight.
# Each entry must reference a MythicMobs mob type ID.
#
# Despawn behavior (max lifetime, empty threat table) is configured in the
# MythicMobs mob YAML via ~onTimer and ~onDropCombat skills, NOT here.
mob-pool:
  - mythicmobs-mob-id: "RiptideGuardian"
    weight: 1
    # Only eligible when accumulated spawn chance >= this threshold
    min-chance-threshold: 0.10
    # MythicMobs mob level (used for MM's level scaling system)
    mob-level: 1.0
  # Future example:
  # - mythicmobs-mob-id: "TideScout"
  #   weight: 3
  #   min-chance-threshold: 0.0
  #   mob-level: 1.0
```

### 3.4 Design Decisions

**Single file instead of directory:** Unlike LLD-1's binding system (which uses a directory for extensibility), the fishing mob spawn system has exactly one config with a single mob pool. Server owners customize the pool entries, not the file count. A single `FileType` entry keeps things simple.

---

## 4. Mob Pool Data Model

**Package:** `us.eunoians.mcrpg.fishing`

### 4.1 MobPoolEntry

**File:** `src/main/java/us/eunoians/mcrpg/fishing/MobPoolEntry.java`

```java
package us.eunoians.mcrpg.fishing;

import org.jetbrains.annotations.NotNull;

/**
 * A single entry in the weighted fishing mob pool.
 * Parsed from the {@code mob-pool} section of the fishing mob spawn configuration.
 *
 * @param mythicMobsId      the MythicMobs internal type ID to spawn
 * @param weight             the relative weight for random selection (higher = more likely)
 * @param minChanceThreshold the minimum accumulated spawn chance required for this mob to be eligible
 * @param mobLevel           the MythicMobs mob level for MM's scaling system
 */
public record MobPoolEntry(
        @NotNull String mythicMobsId,
        int weight,
        double minChanceThreshold,
        double mobLevel
) {}
```

### 4.2 MobPoolSelector

**File:** `src/main/java/us/eunoians/mcrpg/fishing/MobPoolSelector.java`

Instantiable weighted random selector. Constructed with the pool; selects from eligible entries on each call.

```java
package us.eunoians.mcrpg.fishing;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Selects a mob from a weighted pool based on the player's current spawn chance.
 * Only entries whose {@link MobPoolEntry#minChanceThreshold()} is at or below
 * the current chance are eligible.
 * <p>
 * This is an instantiable class — construct it with the pool once, then call
 * {@link #select(double)} on each spawn trigger.
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
     * Only entries with {@code minChanceThreshold <= currentChance} are eligible.
     *
     * @param currentChance the player's current accumulated spawn chance
     * @return a selected entry, or empty if no entries are eligible
     */
    @NotNull
    public Optional<MobPoolEntry> select(double currentChance) {
        List<MobPoolEntry> eligible = pool.stream()
                .filter(entry -> currentChance >= entry.minChanceThreshold())
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

        // Should not reach here, but return last eligible as fallback
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
}
```

---

## 5. FishingMobSpawnTracker

**File:** `src/main/java/us/eunoians/mcrpg/fishing/FishingMobSpawnTracker.java`

This is the core class. It listens to `PlayerFishEvent` to track fishing behavior and trigger mob spawns. It also listens to `FishingMobDeathEvent` (fired by the existing `MythicMobsListener`) to clean up per-player state.

The tracker also owns mob pool loading — the parsing logic from config is a private method on this class rather than a separate static utility, keeping the loading coupled to its only consumer.

### Design: Listener vs. Manager

The tracker is a Bukkit `Listener` rather than a McCore `Manager`. Reasons:
1. It needs to listen to Bukkit events (`PlayerFishEvent`, `PlayerQuitEvent`, `PlayerChangedWorldEvent`)
2. It needs to listen to McRPG events (`FishingMobDeathEvent`)
3. Its state is transient (session-only, no DB persistence)
4. It has no registry key or cross-system lookup requirements

It is registered conditionally — only when MythicMobs is present and the system is enabled.

```java
package us.eunoians.mcrpg.fishing;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.FishingMobSpawnConfigFile;
import us.eunoians.mcrpg.event.fishing.FishingMobDeathEvent;
import us.eunoians.mcrpg.event.fishing.FishingMobSpawnChanceUpdateEvent;
import us.eunoians.mcrpg.external.mythicmobs.FishingMobKeys;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Tracks per-player fishing behavior and triggers mob spawns via MythicMobs
 * when the accumulated spawn chance succeeds.
 * <p>
 * All state is session-only — logout discards everything. No DB persistence.
 * <p>
 * This listener is only registered when:
 * <ul>
 *   <li>MythicMobs is present (hook registered)</li>
 *   <li>The fishing mob spawn system is enabled in config</li>
 * </ul>
 */
public class FishingMobSpawnTracker implements Listener {

    private final McRPG plugin;
    private final Map<UUID, PlayerFishingState> playerStates = new HashMap<>();
    private final MobPoolSelector mobPoolSelector;

    public FishingMobSpawnTracker(@NotNull McRPG plugin) {
        this.plugin = plugin;
        this.mobPoolSelector = new MobPoolSelector(loadMobPool());
    }

    /**
     * Handles a player catching a fish or entity. Updates the player's spawn chance
     * based on hook location proximity and rolls for a mob spawn.
     *
     * @param event the fish event
     */
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

        Location hookLocation = event.getHook().getLocation();

        // World restriction check
        if (!isWorldAllowed(hookLocation.getWorld())) {
            return;
        }

        // Biome restriction check
        if (!isBiomeAllowed(hookLocation)) {
            return;
        }

        PlayerFishingState state = playerStates.computeIfAbsent(
                player.getUniqueId(), uuid -> new PlayerFishingState(getBaseChance()));

        // Check active mob cap
        int maxActiveMobs = getConfig().getInt(FishingMobSpawnConfigFile.MAX_ACTIVE_MOBS_PER_PLAYER, 1);
        if (state.getActiveMobCount() >= maxActiveMobs) {
            return;
        }

        // Update chance based on proximity to last hook
        updateSpawnChance(player, state, hookLocation);
        state.setLastHookLocation(hookLocation);

        // Roll for spawn
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < state.getCurrentSpawnChance()) {
            attemptSpawnMob(player, state, hookLocation);
        }
    }

    /**
     * Handles a fishing mob death. Removes the mob from the player's active set
     * and resets their spawn chance to the post-kill value.
     *
     * @param event the fishing mob death event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishingMobDeath(@NotNull FishingMobDeathEvent event) {
        UUID mobUUID = event.getMob().getUniqueId();

        // Find the player who owns this mob and clean up
        for (Map.Entry<UUID, PlayerFishingState> entry : playerStates.entrySet()) {
            if (entry.getValue().removeActiveMob(mobUUID)) {
                double postKillChance = getConfig().getDouble(FishingMobSpawnConfigFile.POST_KILL_CHANCE, 0.0);
                entry.getValue().setCurrentSpawnChance(postKillChance);
                break;
            }
        }
    }

    /**
     * Discards all state for a player on logout.
     *
     * @param event the quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        playerStates.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Handles world change — optionally resets spawn chance and nulls last hook location.
     *
     * @param event the world change event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(@NotNull PlayerChangedWorldEvent event) {
        PlayerFishingState state = playerStates.get(event.getPlayer().getUniqueId());
        if (state == null) {
            return;
        }

        boolean resetOnWorldChange = getConfig().getBoolean(FishingMobSpawnConfigFile.RESET_ON_WORLD_CHANGE, true);
        if (resetOnWorldChange) {
            state.setCurrentSpawnChance(getBaseChance());
        }
        state.setLastHookLocation(null);
    }

    private void updateSpawnChance(@NotNull Player player,
                                    @NotNull PlayerFishingState state,
                                    @NotNull Location hookLocation) {
        double oldChance = state.getCurrentSpawnChance();
        double newChance;

        Location lastHook = state.getLastHookLocation();
        double sameAreaRange = getConfig().getDouble(FishingMobSpawnConfigFile.SAME_AREA_RANGE, 10.0);

        if (lastHook == null || !lastHook.getWorld().equals(hookLocation.getWorld())) {
            // First catch or different world — no change
            newChance = oldChance;
        } else {
            double distance = lastHook.distance(hookLocation);
            if (distance <= sameAreaRange) {
                // Same area — increment
                double increment = getConfig().getDouble(FishingMobSpawnConfigFile.CHANCE_INCREMENT_PER_CATCH, 0.02);
                double maxChance = getConfig().getDouble(FishingMobSpawnConfigFile.MAX_CHANCE, 0.35);
                newChance = Math.min(oldChance + increment, maxChance);
            } else {
                // New area — decrement
                double decrement = getConfig().getDouble(FishingMobSpawnConfigFile.CHANCE_DECREMENT_PER_CATCH, 0.05);
                double baseChance = getBaseChance();
                newChance = Math.max(oldChance - decrement, baseChance);
            }
        }

        // Fire chance update event
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
        Optional<MobPoolEntry> selected = mobPoolSelector.select(state.getCurrentSpawnChance());
        if (selected.isEmpty()) {
            return;
        }

        MobPoolEntry entry = selected.get();

        // Calculate spawn location (offset from hook)
        Location spawnLocation = calculateSpawnLocation(hookLocation);

        // Spawn via MythicMobs API
        Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(entry.mythicMobsId());
        if (mythicMob.isEmpty()) {
            plugin.getLogger().warning("Failed to spawn fishing mob: MythicMob type '"
                    + entry.mythicMobsId() + "' not found in MythicMobs registry.");
            return;
        }

        ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(spawnLocation), entry.mobLevel());
        Entity entity = activeMob.getEntity().getBukkitEntity();

        // Tag the entity with PDC keys so MythicMobsListener can identify it
        entity.getPersistentDataContainer().set(FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN, true);
        entity.getPersistentDataContainer().set(FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING, player.getUniqueId().toString());

        // Track the mob
        state.addActiveMob(entity.getUniqueId());

        // Reset spawn chance after successful spawn
        state.setCurrentSpawnChance(getBaseChance());

        plugin.getLogger().fine("Spawned fishing mob '" + entry.mythicMobsId()
                + "' for player " + player.getName() + " at " + spawnLocation);
    }

    @NotNull
    private Location calculateSpawnLocation(@NotNull Location hookLocation) {
        double offset = getConfig().getDouble(FishingMobSpawnConfigFile.SPAWN_OFFSET_FROM_HOOK, 3.0);
        double yOffset = getConfig().getDouble(FishingMobSpawnConfigFile.SPAWN_Y_OFFSET, 1.0);

        // Spawn at a random angle around the hook at the configured offset
        double angle = ThreadLocalRandom.current().nextDouble(2 * Math.PI);
        double x = hookLocation.getX() + offset * Math.cos(angle);
        double z = hookLocation.getZ() + offset * Math.sin(angle);
        double y = hookLocation.getY() + yOffset;

        return new Location(hookLocation.getWorld(), x, y, z);
    }

    private boolean isWorldAllowed(@NotNull World world) {
        List<String> allowedWorlds = getConfig().getStringList(FishingMobSpawnConfigFile.ALLOWED_WORLDS);
        return allowedWorlds == null || allowedWorlds.isEmpty() || allowedWorlds.contains(world.getName());
    }

    private boolean isBiomeAllowed(@NotNull Location location) {
        List<String> requiredBiomes = getConfig().getStringList(FishingMobSpawnConfigFile.REQUIRED_BIOMES);
        if (requiredBiomes == null || requiredBiomes.isEmpty()) {
            return true;
        }

        Biome biome = location.getBlock().getBiome();
        return requiredBiomes.contains(biome.name());
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

    // --- Mob Pool Loading ---

    /**
     * Loads all mob pool entries from the configuration.
     *
     * @return an unmodifiable list of parsed pool entries
     */
    @NotNull
    private List<MobPoolEntry> loadMobPool() {
        YamlDocument config = getConfig();
        Logger logger = plugin.getLogger();
        List<MobPoolEntry> entries = new ArrayList<>();

        if (!config.contains(FishingMobSpawnConfigFile.MOB_POOL)) {
            logger.warning("No mob-pool section found in fishing mob spawn configuration.");
            return Collections.emptyList();
        }

        List<?> poolList = config.getList(FishingMobSpawnConfigFile.MOB_POOL);
        if (poolList == null || poolList.isEmpty()) {
            logger.warning("Mob pool is empty in fishing mob spawn configuration.");
            return Collections.emptyList();
        }

        for (Object element : poolList) {
            if (!(element instanceof Map<?, ?> map)) {
                logger.warning("Invalid mob pool entry (not a map): " + element);
                continue;
            }

            String mobId = getStringValue(map, "mythicmobs-mob-id");
            if (mobId == null || mobId.isBlank()) {
                logger.warning("Mob pool entry missing 'mythicmobs-mob-id', skipping.");
                continue;
            }

            int weight = getIntValue(map, "weight", 1);
            double minThreshold = getDoubleValue(map, "min-chance-threshold", 0.0);
            double mobLevel = getDoubleValue(map, "mob-level", 1.0);

            if (weight <= 0) {
                logger.warning("Mob pool entry '" + mobId + "' has weight <= 0, skipping.");
                continue;
            }

            entries.add(new MobPoolEntry(mobId, weight, minThreshold, mobLevel));
            logger.info("Loaded mob pool entry: " + mobId + " (weight=" + weight
                        + ", threshold=" + minThreshold + ", level=" + mobLevel + ")");
        }

        return Collections.unmodifiableList(entries);
    }

    private static String getStringValue(@NotNull Map<?, ?> map, @NotNull String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private static int getIntValue(@NotNull Map<?, ?> map, @NotNull String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    private static double getDoubleValue(@NotNull Map<?, ?> map, @NotNull String key, double defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }
}
```

> **API Verification Note:** The MythicMobs API calls (`MythicBukkit.inst().getMobManager().getMythicMob()`, `MythicMob.spawn()`, `BukkitAdapter.adapt()`) should be verified against the actual MM5 dependency at implementation time. These match the API patterns already used in `MythicMobsListener`.

### Why Direct MM API Calls?

The existing `MythicMobsListener` (from LLD-1 implementation) calls MM API directly rather than through a facade. For consistency, this tracker does the same. If `MythicMobsIntegration` is introduced later, the spawn call should migrate there. The MM imports are isolated to this one class (plus the existing `MythicMobsListener`).

---

## 6. Per-Player State

**File:** `src/main/java/us/eunoians/mcrpg/fishing/PlayerFishingState.java`

All state is transient — discarded on logout, world change (optionally), or mob death. No database persistence.

```java
package us.eunoians.mcrpg.fishing;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Session-only fishing state for a single player. Tracks spawn chance,
 * last hook location, and active fishing mob UUIDs.
 * <p>
 * This state is never persisted to the database. It is discarded on logout
 * and optionally reset on world change.
 */
public class PlayerFishingState {

    private double currentSpawnChance;
    private Location lastHookLocation;
    private final Set<UUID> activeMobUUIDs;

    /**
     * Creates a new player fishing state with the given initial spawn chance.
     *
     * @param initialChance the starting spawn chance (typically base-chance from config)
     */
    public PlayerFishingState(double initialChance) {
        this.currentSpawnChance = initialChance;
        this.lastHookLocation = null;
        this.activeMobUUIDs = new HashSet<>();
    }

    /**
     * Gets the player's current accumulated spawn chance.
     *
     * @return the current spawn chance (0.0 to max-chance)
     */
    public double getCurrentSpawnChance() {
        return currentSpawnChance;
    }

    /**
     * Sets the player's current spawn chance.
     *
     * @param chance the new spawn chance
     */
    public void setCurrentSpawnChance(double chance) {
        this.currentSpawnChance = chance;
    }

    /**
     * Gets the last known hook location for this player.
     *
     * @return the last hook location, or null if no fishing has occurred yet
     */
    @Nullable
    public Location getLastHookLocation() {
        return lastHookLocation;
    }

    /**
     * Sets the last known hook location.
     *
     * @param location the hook location, or null to clear
     */
    public void setLastHookLocation(@Nullable Location location) {
        this.lastHookLocation = location;
    }

    /**
     * Adds a mob UUID to the active set.
     *
     * @param mobUUID the mob's entity UUID
     */
    public void addActiveMob(@NotNull UUID mobUUID) {
        activeMobUUIDs.add(mobUUID);
    }

    /**
     * Removes a mob UUID from the active set.
     *
     * @param mobUUID the mob's entity UUID
     * @return true if the mob was in the active set
     */
    public boolean removeActiveMob(@NotNull UUID mobUUID) {
        return activeMobUUIDs.remove(mobUUID);
    }

    /**
     * Gets the number of currently active fishing mobs for this player.
     *
     * @return the active mob count
     */
    public int getActiveMobCount() {
        return activeMobUUIDs.size();
    }

    /**
     * Gets an unmodifiable view of the active mob UUIDs.
     *
     * @return the active mob UUIDs
     */
    @NotNull
    public Set<UUID> getActiveMobUUIDs() {
        return Collections.unmodifiableSet(activeMobUUIDs);
    }
}
```

### State Lifecycle

| Event | State Change |
|---|---|
| Player logs in | No state created (lazy on first catch) |
| First catch | State created with `base-chance` |
| Same-area catch | `currentSpawnChance += increment` (capped at `max-chance`) |
| New-area catch | `currentSpawnChance -= decrement` (floored at `base-chance`) |
| Spawn succeeds | `currentSpawnChance = base-chance`, mob UUID added to `activeMobUUIDs` |
| Mob dies | UUID removed from `activeMobUUIDs`, `currentSpawnChance = post-kill-chance` |
| Mob despawns | UUID removed from `activeMobUUIDs` (via death event — MM fires death on despawn too) |
| Player logs out | Entire state discarded |
| World change | `lastHookLocation = null`, optionally `currentSpawnChance = base-chance` |

---

## 7. Spawn Flow

Full sequence from catch to mob appearing:

```
Player catches fish (PlayerFishEvent CAUGHT_FISH/CAUGHT_ENTITY)
  -> FishingMobSpawnTracker.onPlayerFish()
    -> Check: world allowed? biome allowed? active mob cap reached?
    -> Update spawn chance based on hook proximity
    -> Fire FishingMobSpawnChanceUpdateEvent (cancellable)
    -> Roll against currentSpawnChance
    -> On success:
      -> mobPoolSelector.select() — weighted random from eligible entries
      -> Calculate spawn location (offset from hook)
      -> MythicMobs API: spawn mob at location
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

The tracker adds the tracking `Map<UUID, PlayerFishingState>` for spawn-chance management, but mob identification flows through PDC tags.

---

## 8. Death Callback Integration

When a fishing mob dies:

```
MythicMobDeathEvent (from MythicMobs)
  -> MythicMobsListener.onMythicMobDeath() [existing code]
    -> Reads PDC: FISHING_MOB_KEY present?
    -> Fires FishingMobDeathEvent
      -> FishingMobSpawnTracker.onFishingMobDeath() [new code]
        -> Finds owning player in playerStates
        -> Removes mob UUID from activeMobUUIDs
        -> Resets currentSpawnChance to post-kill-chance
```

The integration is event-driven. The tracker doesn't need to know about `MythicMobDeathEvent` — it only listens to the McRPG-domain `FishingMobDeathEvent`.

### Death vs. Despawn

When MythicMobs despawns a mob (via `ActiveMob.despawn()`, `~onTimer`, or `~onDropCombat`), it fires `MythicMobDeathEvent`. The existing `MythicMobsListener` checks PDC tags, which survive despawn, so `FishingMobDeathEvent` is fired for both death and despawn. The tracker handles both cases identically — remove from tracking state.

---

## 9. Custom Events

### 9.1 FishingMobSpawnChanceUpdateEvent (New)

**File:** `src/main/java/us/eunoians/mcrpg/event/fishing/FishingMobSpawnChanceUpdateEvent.java`

Fired when a player's fishing mob spawn chance changes. Cancellable — if cancelled, the chance remains unchanged.

```java
package us.eunoians.mcrpg.event.fishing;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player's fishing mob spawn chance is about to change.
 * <p>
 * Third-party plugins can:
 * <ul>
 *   <li>Cancel the event to prevent the chance update</li>
 *   <li>Modify {@link #setNewChance(double)} to adjust the new value</li>
 * </ul>
 */
public class FishingMobSpawnChanceUpdateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final double oldChance;
    private double newChance;
    private final Location hookLocation;
    private boolean cancelled = false;

    /**
     * Creates a new {@link FishingMobSpawnChanceUpdateEvent}.
     *
     * @param player       the player whose spawn chance is changing
     * @param oldChance    the spawn chance before the update
     * @param newChance    the spawn chance after the update (modifiable)
     * @param hookLocation the current hook location that triggered the update
     */
    public FishingMobSpawnChanceUpdateEvent(@NotNull Player player, double oldChance,
                                             double newChance, @NotNull Location hookLocation) {
        this.player = player;
        this.oldChance = oldChance;
        this.newChance = newChance;
        this.hookLocation = hookLocation;
    }

    /**
     * Gets the player whose spawn chance is changing.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the spawn chance before the update.
     *
     * @return the old chance
     */
    public double getOldChance() {
        return oldChance;
    }

    /**
     * Gets the spawn chance that will be applied after the update.
     *
     * @return the new chance
     */
    public double getNewChance() {
        return newChance;
    }

    /**
     * Sets the spawn chance to apply after the update.
     * Third-party plugins can use this to boost or reduce the chance.
     *
     * @param newChance the modified new chance
     */
    public void setNewChance(double newChance) {
        this.newChance = newChance;
    }

    /**
     * Gets the hook location that triggered the chance update.
     *
     * @return the hook location
     */
    @NotNull
    public Location getHookLocation() {
        return hookLocation;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

### 9.2 Existing Events (No Changes)

These events from LLD-1 implementation are used as-is:

| Event | Fired By | Consumed By |
|---|---|---|
| `FishingMobSpawnEvent` | `MythicMobsListener` | Third-party plugins (cancellable) |
| `FishingMobDeathEvent` | `MythicMobsListener` | `FishingMobSpawnTracker` (cleanup) |

### 9.3 Event Summary

| Event | When | Cancellable | Mutable Fields |
|---|---|---|---|
| `FishingMobSpawnChanceUpdateEvent` | Before spawn chance changes | Yes | `newChance` |
| `FishingMobSpawnEvent` (existing) | After MM spawns a tagged mob | Yes (removes entity) | — |
| `FishingMobDeathEvent` (existing) | After a tagged mob dies | No | — |

---

## 10. Bootstrap Registration

### 10.1 Listener Registration

**Modified file:** `us.eunoians.mcrpg.bootstrap.McRPGListenerRegistrar`

The `FishingMobSpawnTracker` is registered conditionally after the MythicMobs listener block:

```java
// Fishing mob spawn tracker (requires MythicMobs + enabled in config)
if (plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.MYTHIC_MOBS).isPresent()) {
    YamlDocument fishingConfig = plugin.registryAccess()
            .registry(RegistryKey.MANAGER)
            .manager(McRPGManagerKey.FILE)
            .getFile(FileType.FISHING_MOB_SPAWN_CONFIG);
    if (fishingConfig.getBoolean(FishingMobSpawnConfigFile.SPAWN_ENABLED, true)) {
        Bukkit.getPluginManager().registerEvents(new FishingMobSpawnTracker(plugin), plugin);
    }
}
```

### 10.2 Registration Order

```
FileManager init (loads fishing_mob_spawn_configuration.yml via FileType)
  -> McRPGListenerRegistrar
    -> MythicMobsListener registered (if MM present) [existing]
    -> FishingMobSpawnTracker registered (if MM present AND spawn enabled) [new]
  -> McRPGHooksRegistrar
    -> MythicMobsHook registered (if MM present) [existing]
```

Note: The listener registrar runs before the hooks registrar in `McRPGBootstrap`. However, the tracker's conditional check uses `RegistryKey.PLUGIN_HOOK` which requires the hook to be registered. Looking at the current bootstrap order:

```java
new McRPGListenerRegistrar().register(bootstrapContext);  // line 72
new McRPGHooksRegistrar().register(bootstrapContext);      // line 73
```

The hook is registered **after** the listener registrar. The existing `MythicMobsListener` registration already depends on the hook being present:

```java
if (plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.MYTHIC_MOBS).isPresent()) {
```

This works because both registrations happen synchronously during startup. **Wait — this means the hook check would fail** since hooks register after listeners.

**Fix:** Swap the registration order in `McRPGBootstrap`:

```java
new McRPGHooksRegistrar().register(bootstrapContext);      // Hooks FIRST
new McRPGListenerRegistrar().register(bootstrapContext);    // Listeners AFTER
```

This ensures the MythicMobs hook is available when the listener registrar checks for it. The existing `MythicMobsListener` registration has the same dependency, suggesting the current order may be a latent bug (the hook check would always return empty).

> **Implementation note:** Verify the hook registration order on a live server. If the existing `MythicMobsListener` is correctly registering despite the current order, there may be a subtlety in the `PluginHookRegistry` initialization that makes it work. If so, document it; if not, the swap is required.

---

## 11. Anti-Cheese Analysis

The spawn system's anti-cheese properties come from the asymmetric increment/decrement design:

| Strategy | Behavior | Outcome |
|---|---|---|
| **AFK same spot** | Chance accumulates: +0.02 per catch | Mob eventually spawns — **intended behavior**, not cheese (mob is the anti-AFK measure) |
| **Small movement (1-2 blocks)** | Still within `same-area-range` (10 blocks) | Same as AFK — chance still accumulates |
| **Alternating two spots (>10 blocks apart)** | Alternates +0.02 and -0.05 per catch | Net -0.03 per cycle -> chance decreases -> **defeats the exploit** |
| **Teleporting between distant spots** | Each teleport triggers decrement | Rapid chance decay -> **defeats the exploit** |
| **Fishing in new area then returning** | Decrement away, increment on return | Player loses progress — must rebuild chance in the original area |
| **Multiple players in same area** | Fully independent tracking | Each player accumulates independently — no sharing or amplification |
| **Kill mob, immediately fish again** | Chance resets to `post-kill-chance` (0.0) | Must rebuild chance from scratch — **intended pacing** |
| **Ignore mob, keep fishing** | `max-active-mobs-per-player` cap (default 1) | No more spawns until mob dies/despawns — **prevents mob stacking** |
| **Lure mob to pen/trap** | MM's `~onTimer` forces despawn after TTL | Mob eventually disappears — **MM handles this natively** |
| **All players leave area** | MM's `~onDropCombat` empties ThreatTable -> despawn | Mob cleaned up — **MM handles this natively** |

### Tuning Levers for Server Owners

McRPG config controls spawn behavior:

| Config Key | Anti-Cheese Role |
|---|---|
| `same-area-range` | Higher = more lenient, lower = stricter AFK detection |
| `chance-increment-per-catch` | Controls how fast the mob spawns during AFK |
| `chance-decrement-per-catch` | Controls how strongly movement resets progress |
| `post-kill-chance` | Higher = faster re-spawns after kills |
| `max-active-mobs-per-player` | Prevents mob stacking |

MythicMobs mob config controls despawn behavior:

| MM Config | Anti-Cheese Role |
|---|---|
| `~onTimer` skill with `remove` mechanic | Max lifetime — prevents mob storage |
| `~onDropCombat` skill with `remove` mechanic | Cleans up abandoned mobs when ThreatTable empties |

---

## 12. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|---|---|
| MythicMobs not installed | `MythicMobsHook` not registered -> tracker not registered -> system is inert |
| MythicMobs installed but mob type not registered | `getMythicMob()` returns empty -> warning logged -> spawn attempt silently fails |
| Fishing mob spawn system disabled in config | Tracker not registered -> system is inert |
| Empty mob pool | `MobPoolSelector.select()` returns empty -> no spawn -> no error |
| All pool entries have threshold above current chance | Same as empty pool — no eligible entries |
| Player logs out with active mob | State discarded. Mob continues living under MM rules. MM's `~onTimer`/`~onDropCombat` skills handle cleanup |
| Player logs out and back in | Fresh state. Active mob from previous session is "orphaned" but handled by MM's despawn skills |
| Server restart with living fishing mob | MM persists the mob. `MythicMobsListener` fires events on re-spawn. Tracker has no state (fresh session). Mob lives under MM rules. Acceptable — MM's despawn skills handle cleanup |
| Catch event with null hook | Early return in `onPlayerFish()` — no processing |
| World change | `lastHookLocation` nulled. Optionally resets chance (config-driven) |
| Biome/world not in allowed list | Catch event ignored — no chance update, no spawn |
| Multiple rapid catches | Each catch processed independently. Chance accumulates normally. Roll is per-catch |
| Config reload | Mob pool is loaded once at construction. Config values are read live from `YamlDocument` (boostedyaml reloads in place). Pool requires tracker re-creation on reload |

### Config Reload Behavior

Most config values are read live from the `YamlDocument` on each event, so they take effect immediately on `/mcrpg reload`. The exception is `mob-pool`, which is parsed once in the constructor into an immutable list inside `MobPoolSelector`. To support live pool changes, the tracker would need to be re-created on reload. This could be added as a `ReloadableContent` pattern, but is deferred to a future pass since mob pool changes are rare (typically require a server restart anyway to ensure MM has the new mob type registered).

---

## 13. Test Plan

### 13.1 Unit Tests (src/test/java)

| Test Class | Tests |
|---|---|
| `MobPoolSelectorTest` | Weighted selection with single entry, multiple entries, all entries filtered by threshold, equal weights, zero total weight, empty pool, `hasEntries()` |
| `PlayerFishingStateTest` | Initial state values, chance get/set, last hook location get/set/null, active mob add/remove/count, getActiveMobUUIDs immutability |
| `MobPoolEntryTest` | Record accessor correctness |

### 13.2 Tests Requiring MockBukkit (extend McRPGBaseTest)

| Test Class | Tests |
|---|---|
| `FishingMobSpawnChanceUpdateEventTest` | Event creation, cancellation, newChance modification, handler list |
| `FishingMobSpawnTrackerTest` | Mob pool loading from config: valid entries, missing mob ID, zero weight (skipped), missing fields use defaults, empty pool list, non-map entries |

### 13.3 Manual Testing (Paper Server with MythicMobs)

| Scenario | Verification |
|---|---|
| Fish repeatedly in same spot | Chance accumulates. Eventually mob spawns near hook |
| Fish, then move far away and fish | Chance decreases |
| Alternate between two distant spots | Net chance decrease over time |
| Kill spawned mob | Chance resets to `post-kill-chance`. Can rebuild chance again |
| Let mob live, keep fishing | No more spawns once `max-active-mobs-per-player` reached |
| Log out with active mob | State discarded. Mob eventually despawns via MM skills |
| Fish in disallowed world | No chance updates, no spawns |
| Fish in disallowed biome | No chance updates, no spawns |
| Disable system in config + reload | No fishing mob spawns |
| Empty mob pool in config | No spawns, no errors |
| MM not installed | System doesn't register, no errors |
| Server restart with living mob | Mob persists (MM), tracker has fresh state |

---

## 14. File Manifest

### New Files

| File | Type | Description |
|---|---|---|
| `fishing/FishingMobSpawnTracker.java` | Listener | Core tracker — listens to fish/death/quit/world events, loads mob pool |
| `fishing/PlayerFishingState.java` | Data | Per-player session state (chance, hook location, active mobs) |
| `fishing/MobPoolEntry.java` | Record | Single weighted mob pool entry |
| `fishing/MobPoolSelector.java` | Selector | Instantiable weighted random selection from eligible pool entries |
| `configuration/file/FishingMobSpawnConfigFile.java` | Config | Route constants for fishing mob spawn config |
| `event/fishing/FishingMobSpawnChanceUpdateEvent.java` | Event | Fired before spawn chance changes (cancellable) |
| `src/main/resources/fishing_mob_spawn_configuration.yml` | YAML | Default fishing mob spawn configuration |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

### Modified Files

| File | Change |
|---|---|
| `configuration/FileType.java` | Add `FISHING_MOB_SPAWN_CONFIG` entry |
| `bootstrap/McRPGListenerRegistrar.java` | Add conditional `FishingMobSpawnTracker` registration |
| `bootstrap/McRPGBootstrap.java` | **Potentially** swap hooks/listener registration order (see Section 10.2) |

### Not Modified (Used As-Is)

| File | Role |
|---|---|
| `external/mythicmobs/MythicMobsHook.java` | Presence check for conditional registration |
| `external/mythicmobs/MythicMobsListener.java` | Bridges MM events -> fires `FishingMobSpawnEvent`/`FishingMobDeathEvent` |
| `external/mythicmobs/FishingMobKeys.java` | PDC key constants applied to spawned mobs |
| `event/fishing/FishingMobSpawnEvent.java` | Cancellable spawn event (fired by `MythicMobsListener`) |
| `event/fishing/FishingMobDeathEvent.java` | Death event (consumed by tracker for cleanup) |
