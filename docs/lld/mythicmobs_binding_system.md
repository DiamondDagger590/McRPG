# Low-Level Design: MythicMobs Binding System (LLD-1)

**Status:** Draft
**Date:** 2026-03-16
**HLD Reference:** [Riptide Guardian HLD](../hld/riptide_guardian.md), Sections 2, 10
**Scope:** Hook, integration facade, binding registry, config file, custom events, event listener

---

## Table of Contents

1. [Overview](#1-overview)
2. [Build Dependency](#2-build-dependency)
3. [MythicMobsHook](#3-mythicmobshook)
4. [MythicMobsIntegration](#4-mythicmobsintegration)
5. [MythicMobBinding](#5-mythicmobbinding)
6. [MythicMobBindingRegistry](#6-mythicmobbindingregistry)
7. [Configuration](#7-configuration)
8. [Custom Events](#8-custom-events)
9. [Event Listener](#9-event-listener)
10. [Bootstrap Registration](#10-bootstrap-registration)
11. [Loot Evaluation](#11-loot-evaluation)
12. [Despawn Scheduling](#12-despawn-scheduling)
13. [Error Handling & Graceful Degradation](#13-error-handling--graceful-degradation)
14. [Test Plan](#14-test-plan)
15. [File Manifest](#15-file-manifest)

---

## 1. Overview

The MythicMobs Binding System lets McRPG react to MythicMobs lifecycle events (spawn, death, despawn) for specific MM type IDs. McRPG never wraps or manages the entity — it observes events and applies McRPG behavior (loot drops, VFX, custom events) based on YAML-configured bindings.

**This LLD produces code.** All classes, configs, and tests described here are implementation-ready.

### Boundary with LLD-2 (Fishing Mob Spawn System)

This LLD does **not** cover:
- The `FishingMobSpawnTracker` or spawn probability logic (LLD-2)
- Mob pool selection and weighted random (LLD-2)
- Per-player spawn state tracking (LLD-2)

This LLD **does** cover:
- The `MythicMobsIntegration.spawnMob()` method that LLD-2 will call
- Despawn policy scheduling (triggered after spawn, owned by the binding system)
- Death loot evaluation (triggered by MM death event)

### Boundary with LLD-3 (Skill Book System)

This LLD does **not** cover:
- `SkillBookFactory` or skill book item creation (LLD-3)
- `OnSkillBookConsumeListener` (LLD-3)

This LLD **does** cover:
- Loot table evaluation that references loot entry types (including `skill-book`)
- The `LootEntry` data model that LLD-3's factory will consume

---

## 2. Build Dependency

MythicMobs publishes to the Lumine Releases repo. Add to `build.gradle.kts`:

**Repository:**
```kotlin
maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs
```

**Dependency:**
```kotlin
val mythicMobsVersion = "5.7.2"
compileOnly("io.lumine:Mythic-Dist:$mythicMobsVersion")
```

MythicMobs is `compileOnly` — it's a soft dependency, not shaded. The version should track the latest stable MM5 release. If MM's Maven coordinates change, update accordingly.

> **Note:** The exact MM5 artifact coordinates and API package names should be verified against the latest MythicMobs documentation at implementation time. MM5 has changed artifact names historically (`MythicMobs`, `Mythic-Dist`, `mythic-dist`). The implementation PR should confirm the correct artifact before merging.

---

## 3. MythicMobsHook

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsHook.java`

A lightweight marker hook following the same pattern as `McMMOHook`, `GeyserHook`, etc. Its presence in the `PluginHookRegistry` signals that MythicMobs is available.

```java
package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * Hook registered when MythicMobs is present on the server.
 * Provides access to {@link MythicMobsIntegration} for all MM API interactions.
 */
public class MythicMobsHook extends PluginHook<McRPG> {

    private final MythicMobsIntegration integration;

    public MythicMobsHook(@NotNull McRPG plugin) {
        super(plugin);
        this.integration = new MythicMobsIntegration();
    }

    /**
     * Gets the integration facade for MythicMobs API calls.
     *
     * @return the {@link MythicMobsIntegration} instance
     */
    @NotNull
    public MythicMobsIntegration getIntegration() {
        return integration;
    }
}
```

**Plugin hook key addition** in `McRPGPluginHookKey.java`:

```java
PluginHookKey<MythicMobsHook> MYTHICMOBS = PluginHookKey.create(MythicMobsHook.class);
```

### Design Decision: Integration on the Hook

The `MythicMobsIntegration` instance lives on the hook rather than as a standalone manager because:
1. It should only exist when MM is present (hook guarantees this)
2. All callers already retrieve the hook via `Optional` — natural null-safety
3. No need for a separate manager key since it has no independent lifecycle

---

## 4. MythicMobsIntegration

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsIntegration.java`

A facade that wraps all MythicMobs API calls. **All MM API usage in McRPG is funneled through this class.** This provides a single point of change if MM's API evolves.

```java
package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade for all MythicMobs API interactions. Centralizes MM API calls
 * so that API changes only require updates in one place.
 */
public class MythicMobsIntegration {

    /**
     * Checks whether a MythicMob type ID is registered in MythicMobs.
     *
     * @param typeId the MM internal type ID
     * @return true if the type ID is registered
     */
    public boolean isMobTypeRegistered(@NotNull String typeId) {
        return MythicBukkit.inst().getMobManager().getMythicMob(typeId).isPresent();
    }

    /**
     * Spawns a MythicMob at the given location.
     *
     * @param typeId   the MM internal type ID
     * @param location the spawn location
     * @param level    the mob level (MM's scaling system)
     * @return the spawned entity, or empty if the type ID is not registered
     */
    @NotNull
    public Optional<Entity> spawnMob(@NotNull String typeId, @NotNull Location location, double level) {
        Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(typeId);
        if (mythicMob.isEmpty()) {
            return Optional.empty();
        }
        ActiveMob activeMob = mythicMob.get().spawn(
                io.lumine.mythic.bukkit.BukkitAdapter.adapt(location),
                level
        );
        return Optional.ofNullable(activeMob.getEntity().getBukkitEntity());
    }

    /**
     * Despawns a MythicMob by its Bukkit entity UUID.
     *
     * @param entityUUID the UUID of the Bukkit entity
     * @return true if the mob was found and despawned
     */
    public boolean despawnMob(@NotNull UUID entityUUID) {
        Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(entityUUID);
        if (activeMob.isPresent()) {
            activeMob.get().despawn();
            return true;
        }
        return false;
    }

    /**
     * Gets the MM internal type ID for a Bukkit entity, if it is a MythicMob.
     *
     * @param entityUUID the UUID of the Bukkit entity
     * @return the type ID, or empty if the entity is not a MythicMob
     */
    @NotNull
    public Optional<String> getMobTypeId(@NotNull UUID entityUUID) {
        return MythicBukkit.inst().getMobManager().getActiveMob(entityUUID)
                .map(activeMob -> activeMob.getMobType());
    }

    /**
     * Gets all UUIDs currently in the MythicMob's threat table.
     *
     * @param entityUUID the UUID of the MythicMob's Bukkit entity
     * @return collection of threat target UUIDs, or empty collection if not found or no threat table
     */
    @NotNull
    public Collection<UUID> getThreatTableTargets(@NotNull UUID entityUUID) {
        Optional<ActiveMob> activeMob = MythicBukkit.inst().getMobManager().getActiveMob(entityUUID);
        if (activeMob.isPresent() && activeMob.get().hasThreatTable()) {
            return activeMob.get().getThreatTable().getAllThreatTargets().stream()
                    .map(target -> target.getUniqueId())
                    .toList();
        }
        return List.of();
    }

    /**
     * Adds initial threat to a MythicMob's threat table for a specific entity.
     *
     * @param mobUUID    the UUID of the MythicMob
     * @param targetUUID the UUID of the target entity to add threat for
     * @param amount     the threat amount to add
     */
    public void addThreat(@NotNull UUID mobUUID, @NotNull UUID targetUUID, double amount) {
        MythicBukkit.inst().getMobManager().getActiveMob(mobUUID).ifPresent(activeMob -> {
            if (activeMob.hasThreatTable()) {
                Entity target = activeMob.getEntity().getBukkitEntity().getWorld()
                        .getEntity(targetUUID);
                if (target != null) {
                    activeMob.getThreatTable().threatGain(
                            io.lumine.mythic.bukkit.BukkitAdapter.adapt(target),
                            amount
                    );
                }
            }
        });
    }
}
```

> **API Verification Note:** The exact method signatures (`getMobManager()`, `getActiveMob()`, `getThreatTable()`, `threatGain()`, `BukkitAdapter.adapt()`) are based on MM5's documented API. These should be verified against the actual MM5 dependency at implementation time. If method names differ, update this facade — all callers go through `MythicMobsIntegration`, so changes are localized.

### Why a Facade?

- MM's API has changed between major versions. A single facade means one file to update.
- Testability: callers can mock `MythicMobsIntegration` without depending on MM internals.
- All MM imports are contained in two files (`MythicMobsIntegration` and `OnMythicMobEventListener`).

---

## 5. MythicMobBinding

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobBinding.java`

An immutable data class representing what McRPG should do when events fire for a specific MM type ID. Built from YAML config at startup.

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Configuration binding between a MythicMobs type ID and McRPG behavior.
 * Immutable — constructed from YAML at startup or reload.
 *
 * @param typeId          the MythicMobs internal type ID this binding applies to
 * @param enabled         whether this binding is active
 * @param despawnPolicy   the despawn policy for mobs matching this binding
 * @param lootTable       the loot table evaluated on death
 * @param spawnEffects    optional spawn VFX (particles + sound)
 * @param fireEvents      whether to fire custom McRPG events for this binding
 */
public record MythicMobBinding(
        @NotNull String typeId,
        boolean enabled,
        @NotNull DespawnPolicy despawnPolicy,
        @NotNull LootTable lootTable,
        @Nullable SpawnEffects spawnEffects,
        boolean fireEvents
) {

    /**
     * Despawn policy configuration.
     *
     * @param maxLifetimeSeconds          maximum seconds before forced despawn (0 = disabled)
     * @param despawnIfNoThreat           whether to despawn when MM ThreatTable empties
     * @param despawnNoThreatDelaySeconds grace period (seconds) before despawning on empty threat
     */
    public record DespawnPolicy(
            int maxLifetimeSeconds,
            boolean despawnIfNoThreat,
            int despawnNoThreatDelaySeconds
    ) {}

    /**
     * Loot table configuration.
     *
     * @param exclusiveDrop if true, at most one entry wins per kill
     * @param entries       the list of loot entries, evaluated in order
     */
    public record LootTable(
            boolean exclusiveDrop,
            @NotNull List<LootEntry> entries
    ) {}

    /**
     * A single loot table entry.
     *
     * @param id      unique identifier for this entry (for logging/debugging)
     * @param type    the loot type (e.g., "skill-book")
     * @param ability the ability NamespacedKey string (for skill-book type), nullable for other types
     * @param chance  drop chance as a decimal (0.0 to 1.0)
     */
    public record LootEntry(
            @NotNull String id,
            @NotNull String type,
            @Nullable String ability,
            double chance
    ) {}

    /**
     * Optional spawn visual/audio effects.
     *
     * @param particle the particle type to play on spawn
     * @param sound    the sound to play on spawn
     */
    public record SpawnEffects(
            @Nullable Particle particle,
            @Nullable Sound sound
    ) {}
}
```

### Design Decisions

**Records over classes:** Bindings are pure data with no behavior. Records give immutability, `equals`/`hashCode`/`toString` for free, and signal "this is a DTO" clearly.

**`LootEntry.type` as String:** Using a string rather than an enum allows future loot types (e.g., `"item"`, `"command"`, `"experience"`) to be added without modifying the binding system. LLD-3 and future LLDs define handlers for specific type strings. The binding system evaluates the chance roll; the type-specific handler produces the reward.

**`LootEntry.ability` nullable:** Only relevant for `skill-book` type. Other future types may use different fields. If the number of type-specific fields grows, a refactor to a `Map<String, String> properties` would be warranted — but YAGNI for now.

---

## 6. MythicMobBindingRegistry

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobBindingRegistry.java`

Maps MM type IDs to `MythicMobBinding` objects. Populated from YAML at startup. Supports programmatic registration for content expansions.

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry mapping MythicMobs type IDs to {@link MythicMobBinding} configurations.
 * Populated from YAML at startup. Content expansions can also register bindings
 * programmatically via {@link #register(MythicMobBinding)}.
 */
public class MythicMobBindingRegistry {

    private final Map<String, MythicMobBinding> bindings = new HashMap<>();

    /**
     * Registers a binding. If a binding for the same type ID already exists,
     * it is replaced and a warning should be logged by the caller.
     *
     * @param binding the binding to register
     */
    public void register(@NotNull MythicMobBinding binding) {
        bindings.put(binding.typeId(), binding);
    }

    /**
     * Removes a binding by type ID.
     *
     * @param typeId the MM type ID to unregister
     * @return true if a binding was removed
     */
    public boolean unregister(@NotNull String typeId) {
        return bindings.remove(typeId) != null;
    }

    /**
     * Looks up a binding by MM type ID.
     *
     * @param typeId the MM type ID
     * @return the binding if registered and enabled, or empty
     */
    @NotNull
    public Optional<MythicMobBinding> getBinding(@NotNull String typeId) {
        MythicMobBinding binding = bindings.get(typeId);
        if (binding != null && binding.enabled()) {
            return Optional.of(binding);
        }
        return Optional.empty();
    }

    /**
     * Gets all registered bindings (including disabled ones).
     *
     * @return unmodifiable view of all bindings
     */
    @NotNull
    public Collection<MythicMobBinding> getAllBindings() {
        return Collections.unmodifiableCollection(bindings.values());
    }

    /**
     * Clears all bindings. Used during reload.
     */
    public void clear() {
        bindings.clear();
    }
}
```

### Why Not a McCore `Registry<T>`?

McCore's `Registry<T>` is keyed by `NamespacedKey` and designed for McRPG-owned content (abilities, skills). Bindings are keyed by MM type ID strings and represent external config — a plain `Map` wrapper is simpler and avoids forcing MM type IDs into the `NamespacedKey` scheme.

### Reload Support

`clear()` + re-populate from YAML supports hot-reload via `FileManager.reloadFiles()`. The binding loader (in the config file parser) should call `clear()` then re-register all bindings. This is called from `MythicMobBindingConfigFile.loadBindings()` (see Section 7).

---

## 7. Configuration

### 7.1 FileType Entry

Add to `FileType.java`:

```java
MYTHICMOB_BINDINGS_CONFIG("mythicmob_bindings_configuration.yml", new MythicMobBindingsConfigFile()),
```

### 7.2 Config File Wrapper

**File:** `src/main/java/us/eunoians/mcrpg/configuration/file/MythicMobBindingsConfigFile.java`

```java
package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBindingRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Configuration file wrapper for {@code mythicmob_bindings_configuration.yml}.
 * Defines routes for MythicMob binding configuration and provides
 * a loader method to populate the {@link MythicMobBindingRegistry}.
 */
public final class MythicMobBindingsConfigFile extends ConfigFile {

    private static final int CURRENT_VERSION = 1;

    // Top-level
    public static final String BINDINGS_HEADER = "bindings";

    // Per-binding routes (relative to binding section — used with dynamic route construction)
    public static final String ENABLED = "enabled";
    public static final String DESPAWN_HEADER = "despawn";
    public static final String DESPAWN_MAX_LIFETIME = "max-lifetime-seconds";
    public static final String DESPAWN_IF_NO_THREAT = "despawn-if-no-threat";
    public static final String DESPAWN_NO_THREAT_DELAY = "despawn-no-threat-delay-seconds";
    public static final String LOOT_HEADER = "loot";
    public static final String LOOT_EXCLUSIVE_DROP = "exclusive-drop";
    public static final String LOOT_ENTRIES = "entries";
    public static final String LOOT_ENTRY_TYPE = "type";
    public static final String LOOT_ENTRY_ABILITY = "ability";
    public static final String LOOT_ENTRY_CHANCE = "chance";
    public static final String SPAWN_EFFECTS_HEADER = "spawn-effects";
    public static final String SPAWN_EFFECTS_PARTICLES = "particles";
    public static final String SPAWN_EFFECTS_SOUND = "sound";
    public static final String FIRE_EVENTS = "fire-events";

    @NotNull
    @Override
    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder()
                .setVersioning(new BasicVersioning("config-version"))
                .addIgnoredRoutes(getIgnoredRoutes())
                .build();
    }

    @NotNull
    private Map<String, Set<Route>> getIgnoredRoutes() {
        Map<String, Set<Route>> ignoredRoutes = new HashMap<>();
        for (int i = 1; i <= CURRENT_VERSION; i++) {
            Set<Route> ignoredRouteSet = new HashSet<>();
            // The entire bindings section is dynamic (server owners add/remove entries)
            ignoredRouteSet.add(Route.fromString(BINDINGS_HEADER));
            ignoredRoutes.put(String.valueOf(i), ignoredRouteSet);
        }
        return ignoredRoutes;
    }

    /**
     * Parses the YAML document and populates the binding registry.
     * Clears existing bindings first to support hot-reload.
     *
     * @param yamlDocument the loaded YAML document
     * @param registry     the binding registry to populate
     * @param logger       the plugin logger for warnings
     */
    public static void loadBindings(@NotNull YamlDocument yamlDocument,
                                    @NotNull MythicMobBindingRegistry registry,
                                    @NotNull Logger logger) {
        registry.clear();

        if (!yamlDocument.contains(BINDINGS_HEADER)) {
            return;
        }

        var bindingsSection = yamlDocument.getSection(BINDINGS_HEADER);
        if (bindingsSection == null) {
            return;
        }

        for (String typeId : bindingsSection.getRoutesAsStrings(false)) {
            try {
                MythicMobBinding binding = parseBinding(yamlDocument, typeId, logger);
                registry.register(binding);
                logger.info("Loaded MythicMob binding: " + typeId
                            + " (enabled=" + binding.enabled() + ", "
                            + binding.lootTable().entries().size() + " loot entries)");
            } catch (Exception e) {
                logger.warning("Failed to parse MythicMob binding '" + typeId + "': " + e.getMessage());
            }
        }
    }

    private static MythicMobBinding parseBinding(@NotNull YamlDocument doc,
                                                  @NotNull String typeId,
                                                  @NotNull Logger logger) {
        String prefix = BINDINGS_HEADER + "." + typeId + ".";

        boolean enabled = doc.getBoolean(Route.fromString(prefix + ENABLED), true);

        // Despawn policy
        String despawnPrefix = prefix + DESPAWN_HEADER + ".";
        var despawnPolicy = new MythicMobBinding.DespawnPolicy(
                doc.getInt(Route.fromString(despawnPrefix + DESPAWN_MAX_LIFETIME), 300),
                doc.getBoolean(Route.fromString(despawnPrefix + DESPAWN_IF_NO_THREAT), true),
                doc.getInt(Route.fromString(despawnPrefix + DESPAWN_NO_THREAT_DELAY), 30)
        );

        // Loot table
        String lootPrefix = prefix + LOOT_HEADER + ".";
        boolean exclusiveDrop = doc.getBoolean(Route.fromString(lootPrefix + LOOT_EXCLUSIVE_DROP), true);
        List<MythicMobBinding.LootEntry> lootEntries = new ArrayList<>();

        String entriesPath = lootPrefix + LOOT_ENTRIES;
        if (doc.contains(Route.fromString(entriesPath))) {
            var entriesSection = doc.getSection(Route.fromString(entriesPath));
            if (entriesSection != null) {
                for (String entryId : entriesSection.getRoutesAsStrings(false)) {
                    String entryPrefix = entriesPath + "." + entryId + ".";
                    String type = doc.getString(Route.fromString(entryPrefix + LOOT_ENTRY_TYPE), "skill-book");
                    String ability = doc.getString(Route.fromString(entryPrefix + LOOT_ENTRY_ABILITY));
                    double chance = doc.getDouble(Route.fromString(entryPrefix + LOOT_ENTRY_CHANCE), 0.0);

                    lootEntries.add(new MythicMobBinding.LootEntry(entryId, type, ability, chance));
                }
            }
        }
        var lootTable = new MythicMobBinding.LootTable(exclusiveDrop, List.copyOf(lootEntries));

        // Spawn effects (optional)
        MythicMobBinding.SpawnEffects spawnEffects = null;
        String effectsPrefix = prefix + SPAWN_EFFECTS_HEADER + ".";
        if (doc.contains(Route.fromString(prefix + SPAWN_EFFECTS_HEADER))) {
            Particle particle = parseEnum(Particle.class,
                    doc.getString(Route.fromString(effectsPrefix + SPAWN_EFFECTS_PARTICLES)), logger);
            Sound sound = parseEnum(Sound.class,
                    doc.getString(Route.fromString(effectsPrefix + SPAWN_EFFECTS_SOUND)), logger);
            if (particle != null || sound != null) {
                spawnEffects = new MythicMobBinding.SpawnEffects(particle, sound);
            }
        }

        boolean fireEvents = doc.getBoolean(Route.fromString(prefix + FIRE_EVENTS), true);

        return new MythicMobBinding(typeId, enabled, despawnPolicy, lootTable, spawnEffects, fireEvents);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E parseEnum(@NotNull Class<E> enumClass,
                                                    String value,
                                                    @NotNull Logger logger) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid " + enumClass.getSimpleName() + " value: '" + value + "'");
            return null;
        }
    }
}
```

### 7.3 YAML Resource

**File:** `src/main/resources/mythicmob_bindings_configuration.yml`

```yaml
config-version: 1

# MythicMob Bindings
# ---
# Each entry maps a MythicMobs internal type ID to McRPG behavior.
# Server owners can add, remove, or modify entries freely.
# The type ID must match the MythicMobs mob configuration file name (without .yml).

bindings:
  RiptideGuardian:
    enabled: true

    # Despawn policy — prevents mob storage and ensures cleanup
    despawn:
      max-lifetime-seconds: 300       # Force despawn after 5 minutes
      despawn-if-no-threat: true      # Despawn if all players leave (ThreatTable empties)
      despawn-no-threat-delay-seconds: 30  # Grace period before despawning on empty threat

    # Loot table — evaluated on mob death
    # exclusive-drop: true means at most one entry drops per kill
    loot:
      exclusive-drop: true
      entries:
        phase-shift-book:
          type: skill-book
          ability: "mcrpg:phase_shift"
          chance: 0.15
        whirlpool-book:
          type: skill-book
          ability: "mcrpg:whirlpool"
          chance: 0.15
        waterlogged-strike-book:
          type: skill-book
          ability: "mcrpg:waterlogged_strike"
          chance: 0.15
        tsunami-wall-book:
          type: skill-book
          ability: "mcrpg:tsunami_wall"
          chance: 0.15

    # Spawn effects — VFX played when the mob spawns
    spawn-effects:
      particles: SPLASH
      sound: ENTITY_ELDER_GUARDIAN_CURSE

    # Whether to fire custom McRPG events (CustomMobSpawnEvent, etc.)
    # Third-party plugins can listen to these events
    fire-events: true
```

### 7.4 Binding Validation at Startup

After loading bindings, the hook constructor should validate each binding against MM's registry:

```java
// In MythicMobsHook constructor, after loadBindings():
for (MythicMobBinding binding : bindingRegistry.getAllBindings()) {
    if (binding.enabled() && !integration.isMobTypeRegistered(binding.typeId())) {
        plugin.getLogger().warning("MythicMob binding '" + binding.typeId()
                + "' references a type ID not registered in MythicMobs. "
                + "The binding will be skipped until the mob is registered.");
    }
    for (MythicMobBinding.LootEntry entry : binding.lootTable().entries()) {
        if ("skill-book".equals(entry.type()) && entry.ability() != null) {
            NamespacedKey abilityKey = NamespacedKey.fromString(entry.ability());
            if (abilityKey == null || !abilityRegistry.registered(abilityKey)) {
                plugin.getLogger().warning("Loot entry '" + entry.id()
                        + "' in binding '" + binding.typeId()
                        + "' references unknown ability: " + entry.ability()
                        + ". This entry will be skipped at drop time.");
            }
        }
    }
}
```

This is advisory only — the binding is still loaded (the mob might be added later via MM reload).

---

## 8. Custom Events

**Package:** `us.eunoians.mcrpg.event.entity.mythicmob`

Three events following the existing McRPG event pattern. All extend a new abstract base.

### 8.1 Base Event

**File:** `src/main/java/us/eunoians/mcrpg/event/entity/mythicmob/CustomMobEvent.java`

```java
package us.eunoians.mcrpg.event.entity.mythicmob;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;

/**
 * Base event for all McRPG events related to bound MythicMobs.
 */
public abstract class CustomMobEvent extends Event {

    private final Entity entity;
    private final MythicMobBinding binding;

    protected CustomMobEvent(@NotNull Entity entity, @NotNull MythicMobBinding binding) {
        this.entity = entity;
        this.binding = binding;
    }

    /**
     * Gets the Bukkit entity of the MythicMob.
     *
     * @return the entity
     */
    @NotNull
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the McRPG binding associated with this MythicMob.
     *
     * @return the binding
     */
    @NotNull
    public MythicMobBinding getBinding() {
        return binding;
    }
}
```

### 8.2 Spawn Event

**File:** `src/main/java/us/eunoians/mcrpg/event/entity/mythicmob/CustomMobSpawnEvent.java`

```java
package us.eunoians.mcrpg.event.entity.mythicmob;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;

/**
 * Fired when a bound MythicMob spawns. Cancelling this event will cause McRPG
 * to despawn the mob and skip all binding behavior (VFX, despawn scheduling, etc.).
 */
public class CustomMobSpawnEvent extends CustomMobEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public CustomMobSpawnEvent(@NotNull Entity entity, @NotNull MythicMobBinding binding) {
        super(entity, binding);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

### 8.3 Death Event

**File:** `src/main/java/us/eunoians/mcrpg/event/entity/mythicmob/CustomMobDeathEvent.java`

```java
package us.eunoians.mcrpg.event.entity.mythicmob;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;

import java.util.UUID;

/**
 * Fired when a bound MythicMob dies. Not cancellable — the mob is already dead.
 * Fired before loot processing so listeners can modify binding state if needed.
 */
public class CustomMobDeathEvent extends CustomMobEvent {

    private static final HandlerList handlers = new HandlerList();
    private final UUID killerUUID;

    /**
     * @param entity  the MythicMob entity
     * @param binding the binding
     * @param killerUUID the UUID of the killer, or null if no killer (e.g., environment)
     */
    public CustomMobDeathEvent(@NotNull Entity entity,
                               @NotNull MythicMobBinding binding,
                               @Nullable UUID killerUUID) {
        super(entity, binding);
        this.killerUUID = killerUUID;
    }

    /**
     * Gets the UUID of the entity that killed the mob, if any.
     *
     * @return the killer's UUID, or null if there was no killer
     */
    @Nullable
    public UUID getKillerUUID() {
        return killerUUID;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

### 8.4 Despawn Event

**File:** `src/main/java/us/eunoians/mcrpg/event/entity/mythicmob/CustomMobDespawnEvent.java`

```java
package us.eunoians.mcrpg.event.entity.mythicmob;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;

/**
 * Fired when a bound MythicMob despawns (not via death). Not cancellable.
 * No rewards are distributed on despawn.
 */
public class CustomMobDespawnEvent extends CustomMobEvent {

    private static final HandlerList handlers = new HandlerList();

    public CustomMobDespawnEvent(@NotNull Entity entity, @NotNull MythicMobBinding binding) {
        super(entity, binding);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
```

---

## 9. Event Listener

**File:** `src/main/java/us/eunoians/mcrpg/listener/entity/OnMythicMobEventListener.java`

Listens to MythicMobs events and bridges them to the binding system. **Only registered if MythicMobs is present.**

```java
package us.eunoians.mcrpg.listener.entity;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.entity.mythicmob.CustomMobDeathEvent;
import us.eunoians.mcrpg.event.entity.mythicmob.CustomMobDespawnEvent;
import us.eunoians.mcrpg.event.entity.mythicmob.CustomMobSpawnEvent;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBindingRegistry;

import java.util.Optional;

/**
 * Bridges MythicMobs lifecycle events to the McRPG binding system.
 * Only registered when MythicMobs is present on the server.
 */
public class OnMythicMobEventListener implements Listener {

    private final McRPG plugin;
    private final MythicMobBindingRegistry bindingRegistry;

    public OnMythicMobEventListener(@NotNull McRPG plugin,
                                     @NotNull MythicMobBindingRegistry bindingRegistry) {
        this.plugin = plugin;
        this.bindingRegistry = bindingRegistry;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobSpawn(@NotNull MythicMobSpawnEvent event) {
        String typeId = event.getMobType().getInternalName();
        Optional<MythicMobBinding> bindingOpt = bindingRegistry.getBinding(typeId);
        if (bindingOpt.isEmpty()) {
            return;
        }

        MythicMobBinding binding = bindingOpt.get();
        Entity entity = event.getEntity();

        // Fire custom event if configured
        if (binding.fireEvents()) {
            CustomMobSpawnEvent spawnEvent = new CustomMobSpawnEvent(entity, binding);
            Bukkit.getPluginManager().callEvent(spawnEvent);
            if (spawnEvent.isCancelled()) {
                event.setCancelled();
                return;
            }
        }

        // Play spawn effects
        if (binding.spawnEffects() != null) {
            MythicMobBinding.SpawnEffects effects = binding.spawnEffects();
            if (effects.particle() != null) {
                entity.getWorld().spawnParticle(effects.particle(),
                        entity.getLocation(), 30, 1.0, 1.0, 1.0, 0.1);
            }
            if (effects.sound() != null) {
                entity.getWorld().playSound(entity.getLocation(), effects.sound(), 1.0f, 1.0f);
            }
        }

        // Schedule despawn (see Section 12)
        MythicMobDespawnScheduler.schedule(plugin, entity.getUniqueId(), binding);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(@NotNull MythicMobDeathEvent event) {
        String typeId = event.getMobType().getInternalName();
        Optional<MythicMobBinding> bindingOpt = bindingRegistry.getBinding(typeId);
        if (bindingOpt.isEmpty()) {
            return;
        }

        MythicMobBinding binding = bindingOpt.get();
        Entity entity = event.getEntity();
        Entity killer = event.getKiller();

        // Fire custom event if configured
        if (binding.fireEvents()) {
            CustomMobDeathEvent deathEvent = new CustomMobDeathEvent(
                    entity, binding, killer != null ? killer.getUniqueId() : null);
            Bukkit.getPluginManager().callEvent(deathEvent);
        }

        // Cancel any pending despawn task
        MythicMobDespawnScheduler.cancel(entity.getUniqueId());

        // Evaluate loot (see Section 11)
        if (killer instanceof Player player) {
            MythicMobLootEvaluator.evaluateAndDrop(plugin, binding.lootTable(), player, entity.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDespawn(@NotNull MythicMobDespawnEvent event) {
        String typeId = event.getMobType().getInternalName();
        Optional<MythicMobBinding> bindingOpt = bindingRegistry.getBinding(typeId);
        if (bindingOpt.isEmpty()) {
            return;
        }

        MythicMobBinding binding = bindingOpt.get();
        Entity entity = event.getEntity();

        // Fire custom event if configured
        if (binding.fireEvents()) {
            CustomMobDespawnEvent despawnEvent = new CustomMobDespawnEvent(entity, binding);
            Bukkit.getPluginManager().callEvent(despawnEvent);
        }

        // Cancel any pending despawn task
        MythicMobDespawnScheduler.cancel(entity.getUniqueId());
    }
}
```

> **API Verification Note:** The exact MM5 event class names (`MythicMobSpawnEvent`, `MythicMobDeathEvent`, `MythicMobDespawnEvent`) and their method signatures (`getMobType().getInternalName()`, `getEntity()`, `getKiller()`, `setCancelled()`) should be verified against the MM5 dependency at implementation time. MM5 has used different event class names and packages across versions.

---

## 10. Bootstrap Registration

### 10.1 Hook Registration

Add to `McRPGHooksRegistrar.register()`:

```java
if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
    logger.info("MythicMobs found... enabling binding support.");
    pluginHookRegistry.register(new MythicMobsHook(plugin));
}
```

### 10.2 Listener Registration

Add to `McRPGListenerRegistrar.register()`, conditionally:

```java
// MythicMobs listener — only registered if MM hook is active
plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
        .pluginHook(McRPGPluginHookKey.MYTHICMOBS)
        .ifPresent(hook -> {
            Bukkit.getPluginManager().registerEvents(
                    new OnMythicMobEventListener(plugin, hook.getBindingRegistry()),
                    plugin);
        });
```

### 10.3 Binding Loading

The `MythicMobsHook` constructor loads bindings from config:

```java
public MythicMobsHook(@NotNull McRPG plugin) {
    super(plugin);
    this.integration = new MythicMobsIntegration();
    this.bindingRegistry = new MythicMobBindingRegistry();

    // Load bindings from config
    YamlDocument yamlDocument = plugin.registryAccess()
            .registry(RegistryKey.MANAGER)
            .manager(McRPGManagerKey.FILE)
            .getFile(FileType.MYTHICMOB_BINDINGS_CONFIG);
    MythicMobBindingsConfigFile.loadBindings(yamlDocument, bindingRegistry, plugin.getLogger());

    // Validate bindings against MM registry
    validateBindings(plugin);
}
```

### 10.4 Registration Order

The hook registration happens in `McRPGHooksRegistrar`, which runs **after** `FileManager` is initialized but alongside other hooks. The listener registration happens in `McRPGListenerRegistrar`, which runs after hooks. Current bootstrap order already supports this — no changes to `McRPGBootstrap` ordering needed.

```
FileManager (constructor loads all FileTypes including MYTHICMOB_BINDINGS_CONFIG)
  → McRPGHooksRegistrar (MythicMobsHook reads the loaded config, populates registry)
    → McRPGListenerRegistrar (registers OnMythicMobEventListener if hook exists)
```

### 10.5 MythicMobsHook Updated Signature

The hook needs to expose the binding registry for the listener:

```java
public class MythicMobsHook extends PluginHook<McRPG> {

    private final MythicMobsIntegration integration;
    private final MythicMobBindingRegistry bindingRegistry;

    // ... constructor as above ...

    @NotNull
    public MythicMobsIntegration getIntegration() {
        return integration;
    }

    @NotNull
    public MythicMobBindingRegistry getBindingRegistry() {
        return bindingRegistry;
    }
}
```

---

## 11. Loot Evaluation

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobLootEvaluator.java`

Evaluates a binding's loot table and distributes drops. The initial implementation handles only the `skill-book` type. Future LLDs add handlers for other types.

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Evaluates a {@link MythicMobBinding.LootTable} and drops rewards at the given location.
 * Handles exclusive-drop logic (at most one entry wins per evaluation).
 */
public final class MythicMobLootEvaluator {

    private MythicMobLootEvaluator() {}

    /**
     * Evaluates the loot table and drops items at the given location.
     *
     * @param plugin    the McRPG plugin instance
     * @param lootTable the loot table to evaluate
     * @param player    the player who killed the mob (for eligibility/messaging)
     * @param location  the drop location
     */
    public static void evaluateAndDrop(@NotNull McRPG plugin,
                                       @NotNull MythicMobBinding.LootTable lootTable,
                                       @NotNull Player player,
                                       @NotNull Location location) {
        for (MythicMobBinding.LootEntry entry : lootTable.entries()) {
            double roll = ThreadLocalRandom.current().nextDouble();
            if (roll >= entry.chance()) {
                continue;
            }

            // Entry won the roll
            boolean dropped = processEntry(plugin, entry, player, location);

            if (dropped && lootTable.exclusiveDrop()) {
                // Exclusive mode — stop after first successful drop
                return;
            }
        }
    }

    /**
     * Processes a single loot entry. Returns true if the entry produced a drop.
     */
    private static boolean processEntry(@NotNull McRPG plugin,
                                        @NotNull MythicMobBinding.LootEntry entry,
                                        @NotNull Player player,
                                        @NotNull Location location) {
        return switch (entry.type()) {
            case "skill-book" -> processSkillBookEntry(plugin, entry, player, location);
            default -> {
                plugin.getLogger().warning("Unknown loot entry type: '" + entry.type()
                        + "' in entry '" + entry.id() + "'");
                yield false;
            }
        };
    }

    /**
     * Processes a skill-book loot entry. Delegates to LLD-3's SkillBookFactory
     * once implemented. For now, validates the ability key and logs.
     */
    private static boolean processSkillBookEntry(@NotNull McRPG plugin,
                                                 @NotNull MythicMobBinding.LootEntry entry,
                                                 @NotNull Player player,
                                                 @NotNull Location location) {
        if (entry.ability() == null) {
            plugin.getLogger().warning("Skill-book loot entry '" + entry.id()
                    + "' has no ability key configured.");
            return false;
        }

        NamespacedKey abilityKey = NamespacedKey.fromString(entry.ability());
        if (abilityKey == null) {
            plugin.getLogger().warning("Invalid ability key format in loot entry '"
                    + entry.id() + "': " + entry.ability());
            return false;
        }

        // Validate ability exists in registry
        var abilityRegistry = plugin.registryAccess()
                .registry(us.eunoians.mcrpg.registry.McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(abilityKey)) {
            plugin.getLogger().warning("Loot entry '" + entry.id()
                    + "' references unregistered ability: " + entry.ability());
            return false;
        }

        // TODO (LLD-3): Replace with SkillBookFactory.createSkillBook() and drop at location
        // For now, this is a placeholder that will be completed when LLD-3 is implemented.
        // ItemStack skillBook = SkillBookFactory.createSkillBook(ability, sourceKey);
        // location.getWorld().dropItemNaturally(location, skillBook);
        plugin.getLogger().info("Skill book drop triggered for ability " + entry.ability()
                + " — awaiting LLD-3 implementation.");
        return true;
    }
}
```

### Design Decisions

**`switch` on type string:** Clean dispatch pattern. Adding a new type is one case arm. If the type roster grows beyond 4-5, refactor to a `Map<String, LootEntryHandler>` strategy pattern.

**Placeholder for LLD-3:** The skill-book processing validates the ability key and logs, but doesn't create the actual `ItemStack` yet. LLD-3 will implement `SkillBookFactory` and this method will be updated to call it. This avoids circular LLD dependencies.

**`ThreadLocalRandom`:** Thread-safe, no contention, appropriate for per-event random rolls.

---

## 12. Despawn Scheduling

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobDespawnScheduler.java`

Manages scheduled despawn tasks for bound mobs. Handles both `max-lifetime-seconds` (fixed timer) and `despawn-if-no-threat` (periodic ThreatTable check).

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.external.mythicmobs.MythicMobsHook;
import us.eunoians.mcrpg.external.mythicmobs.MythicMobsIntegration;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages scheduled despawn tasks for bound MythicMobs.
 * Each mob can have up to two tasks: a max-lifetime timer and a periodic threat check.
 */
public final class MythicMobDespawnScheduler {

    private static final long THREAT_CHECK_INTERVAL_TICKS = 100L; // 5 seconds
    private static final Map<UUID, ScheduledDespawn> activeTasks = new ConcurrentHashMap<>();

    private MythicMobDespawnScheduler() {}

    /**
     * Schedules despawn tasks for a bound mob based on its binding's despawn policy.
     *
     * @param plugin     the McRPG plugin instance
     * @param mobUUID    the UUID of the mob entity
     * @param binding    the binding containing the despawn policy
     */
    public static void schedule(@NotNull McRPG plugin,
                                @NotNull UUID mobUUID,
                                @NotNull MythicMobBinding binding) {
        MythicMobBinding.DespawnPolicy policy = binding.despawnPolicy();
        BukkitTask lifetimeTask = null;
        BukkitTask threatCheckTask = null;

        // Max lifetime timer
        if (policy.maxLifetimeSeconds() > 0) {
            long delayTicks = policy.maxLifetimeSeconds() * 20L;
            lifetimeTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                despawnMob(plugin, mobUUID, "max lifetime exceeded");
            }, delayTicks);
        }

        // Periodic threat table check
        if (policy.despawnIfNoThreat()) {
            long graceDelayTicks = policy.despawnNoThreatDelaySeconds() * 20L;
            threatCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
                private long emptyThreatTicks = 0;

                @Override
                public void run() {
                    MythicMobsIntegration integration = getIntegration(plugin);
                    if (integration == null) {
                        return;
                    }

                    Collection<UUID> targets = integration.getThreatTableTargets(mobUUID);
                    if (targets.isEmpty()) {
                        emptyThreatTicks += THREAT_CHECK_INTERVAL_TICKS;
                        if (emptyThreatTicks >= graceDelayTicks) {
                            despawnMob(plugin, mobUUID, "no threat targets");
                        }
                    } else {
                        emptyThreatTicks = 0;
                    }
                }
            }, THREAT_CHECK_INTERVAL_TICKS, THREAT_CHECK_INTERVAL_TICKS);
        }

        if (lifetimeTask != null || threatCheckTask != null) {
            activeTasks.put(mobUUID, new ScheduledDespawn(lifetimeTask, threatCheckTask));
        }
    }

    /**
     * Cancels all pending despawn tasks for a mob (e.g., on death or manual despawn).
     *
     * @param mobUUID the UUID of the mob entity
     */
    public static void cancel(@NotNull UUID mobUUID) {
        ScheduledDespawn scheduled = activeTasks.remove(mobUUID);
        if (scheduled != null) {
            if (scheduled.lifetimeTask != null) {
                scheduled.lifetimeTask.cancel();
            }
            if (scheduled.threatCheckTask != null) {
                scheduled.threatCheckTask.cancel();
            }
        }
    }

    private static void despawnMob(@NotNull McRPG plugin, @NotNull UUID mobUUID, @NotNull String reason) {
        cancel(mobUUID);
        MythicMobsIntegration integration = getIntegration(plugin);
        if (integration != null) {
            boolean despawned = integration.despawnMob(mobUUID);
            if (despawned) {
                plugin.getLogger().fine("Despawned bound MythicMob " + mobUUID + ": " + reason);
            }
        }
    }

    private static MythicMobsIntegration getIntegration(@NotNull McRPG plugin) {
        return plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
                .pluginHook(McRPGPluginHookKey.MYTHICMOBS)
                .map(MythicMobsHook::getIntegration)
                .orElse(null);
    }

    private record ScheduledDespawn(BukkitTask lifetimeTask, BukkitTask threatCheckTask) {}
}
```

### Design Decisions

**Static utility:** The scheduler has no per-instance state beyond the `ConcurrentHashMap` of active tasks. Static methods keep the API simple — callers don't need to find an instance.

**`ConcurrentHashMap`:** Tasks may be cancelled from different threads (main thread death event vs. scheduled task firing).

**Grace period as tick accumulation:** Rather than scheduling a single delayed task when the threat table first empties (which would need cancellation if a player re-engages), the periodic check accumulates empty ticks. Simpler state management, and the 5-second check interval is coarse enough to not impact performance.

---

## 13. Error Handling & Graceful Degradation

| Scenario | Behavior |
|---|---|
| MythicMobs not installed | `MythicMobsHook` not registered. Listener not registered. Config file still loaded (no error). |
| MM type ID not found in MM registry | Warning logged at startup per binding. Binding remains loaded (mob may be added later). |
| Binding references invalid ability key | Warning logged at startup. Loot entry skipped at drop time. |
| MM event fires for unknown type ID | `getBinding()` returns empty. No processing. |
| Loot entry has unknown type | Warning logged. Entry skipped. Other entries still evaluated. |
| Despawn task fires after mob already dead | `integration.despawnMob()` returns false. No error. |
| Config YAML missing `bindings` section | No bindings loaded. No errors. System is inert. |
| Invalid Particle/Sound enum value in config | Warning logged. That effect field is null. Other effects still apply. |

---

## 14. Test Plan

### 14.1 Unit Tests (src/test/java)

| Test Class | Tests |
|---|---|
| `MythicMobBindingRegistryTest` | Register, unregister, getBinding (enabled/disabled), getAllBindings, clear |
| `MythicMobBindingsConfigFileTest` | Parse valid YAML, parse with missing sections, parse with invalid enum values, exclusive-drop flag |
| `MythicMobLootEvaluatorTest` | Exclusive-drop stops after first hit, non-exclusive evaluates all, unknown type logged, chance 0.0 never drops, chance 1.0 always drops |
| `MythicMobBindingTest` | Record equality, immutability of loot entry list |

### 14.2 Tests Requiring MockBukkit (extend McRPGBaseTest)

| Test Class | Tests |
|---|---|
| `CustomMobSpawnEventTest` | Event creation, cancellation, handler list |
| `CustomMobDeathEventTest` | Event creation, killer UUID nullable |
| `CustomMobDespawnEventTest` | Event creation, handler list |

### 14.3 Manual Testing (Paper Server)

| Scenario | Verification |
|---|---|
| MM installed, valid binding | Spawn event fires, VFX play, despawn timer starts |
| MM installed, invalid type ID | Warning logged, no crash |
| MM not installed | No errors, no listener registered, config loads silently |
| Kill bound mob | Death event fires, loot rolls execute (placeholder log for now) |
| Mob despawn by lifetime | Mob removed after configured seconds |
| Mob despawn by empty threat | Mob removed after grace period when all players leave |
| Hot reload (`/mcrpg reload`) | Bindings re-parsed from YAML |

---

## 15. File Manifest

### New Files

| File | Type | Description |
|---|---|---|
| `src/main/java/.../external/mythicmobs/MythicMobsHook.java` | Hook | Plugin hook for MythicMobs |
| `src/main/java/.../external/mythicmobs/MythicMobsIntegration.java` | Facade | All MM API calls |
| `src/main/java/.../external/mythicmobs/binding/MythicMobBinding.java` | Record | Binding data model |
| `src/main/java/.../external/mythicmobs/binding/MythicMobBindingRegistry.java` | Registry | Type ID → binding map |
| `src/main/java/.../external/mythicmobs/binding/MythicMobLootEvaluator.java` | Utility | Loot table evaluation |
| `src/main/java/.../external/mythicmobs/binding/MythicMobDespawnScheduler.java` | Utility | Despawn task management |
| `src/main/java/.../configuration/file/MythicMobBindingsConfigFile.java` | Config | YAML wrapper + parser |
| `src/main/java/.../event/entity/mythicmob/CustomMobEvent.java` | Event | Abstract base |
| `src/main/java/.../event/entity/mythicmob/CustomMobSpawnEvent.java` | Event | Spawn event (cancellable) |
| `src/main/java/.../event/entity/mythicmob/CustomMobDeathEvent.java` | Event | Death event |
| `src/main/java/.../event/entity/mythicmob/CustomMobDespawnEvent.java` | Event | Despawn event |
| `src/main/java/.../listener/entity/OnMythicMobEventListener.java` | Listener | MM event bridge |
| `src/main/resources/mythicmob_bindings_configuration.yml` | Config | Default YAML |
| `src/test/java/.../external/mythicmobs/binding/MythicMobBindingRegistryTest.java` | Test | Registry tests |
| `src/test/java/.../external/mythicmobs/binding/MythicMobBindingsConfigFileTest.java` | Test | Config parser tests |
| `src/test/java/.../external/mythicmobs/binding/MythicMobLootEvaluatorTest.java` | Test | Loot evaluation tests |
| `src/test/java/.../event/entity/mythicmob/CustomMobSpawnEventTest.java` | Test | Event tests |

### Modified Files

| File | Change |
|---|---|
| `build.gradle.kts` | Add Lumine repo + Mythic-Dist compileOnly dependency |
| `registry/plugin/McRPGPluginHookKey.java` | Add `MYTHICMOBS` key |
| `configuration/FileType.java` | Add `MYTHICMOB_BINDINGS_CONFIG` entry |
| `bootstrap/McRPGHooksRegistrar.java` | Add MythicMobs hook registration |
| `bootstrap/McRPGListenerRegistrar.java` | Add conditional MM listener registration |
