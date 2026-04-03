# Low-Level Design: MythicMobs Binding System (LLD-1)

**Status:** Implemented
**Date:** 2026-03-16
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Sections 2, 10
**Scope:** Hook, integration facade, binding registry, config file, custom events, event listener

---

## Table of Contents

1. [Overview](#1-overview)
2. [Build Dependency](#2-build-dependency)
3. [MythicMobsHook](#3-mythicmobshook)
4. [MythicMobsIntegration](#4-mythicmobsintegration)
5. [Binding Data Model](#5-binding-data-model)
6. [MythicMobBindingRegistry](#6-mythicmobbindingregistry)
7. [Configuration](#7-configuration)
8. [Custom Events](#8-custom-events)
9. [Event Listener](#9-event-listener)
10. [Bootstrap Registration](#10-bootstrap-registration)
11. [Loot Evaluation](#11-loot-evaluation)
12. [Despawn Scheduling](#12-despawn-scheduling)
13. [Config Responsibility: MM vs McRPG](#13-config-responsibility-mm-vs-mcrpg)
14. [Error Handling & Graceful Degradation](#14-error-handling--graceful-degradation)
15. [Test Plan](#15-test-plan)
16. [File Manifest](#16-file-manifest)

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

A lightweight hook following the same pattern as `McMMOHook`, `GeyserHook`, etc. Its presence in the `PluginHookRegistry` signals that MythicMobs is available. Owns both the integration facade and the binding registry.

```java
package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBinding;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBindingLoader;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobBindingRegistry;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Hook registered when MythicMobs is present on the server.
 * Provides access to {@link MythicMobsIntegration} for all MM API interactions
 * and {@link MythicMobBindingRegistry} for binding lookups.
 */
public class MythicMobsHook extends PluginHook<McRPG> {

    private final MythicMobsIntegration integration;
    private final MythicMobBindingRegistry bindingRegistry;

    public MythicMobsHook(@NotNull McRPG plugin) {
        super(plugin);
        this.integration = new MythicMobsIntegration();
        this.bindingRegistry = new MythicMobBindingRegistry();

        // Load bindings from config directory
        MythicMobBindingLoader.loadBindings(plugin, bindingRegistry);

        // Validate bindings against MM registry
        validateBindings(plugin);
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

    /**
     * Gets the binding registry for looking up bindings by type ID.
     *
     * @return the {@link MythicMobBindingRegistry} instance
     */
    @NotNull
    public MythicMobBindingRegistry getBindingRegistry() {
        return bindingRegistry;
    }

    private void validateBindings(@NotNull McRPG plugin) {
        for (MythicMobBinding binding : bindingRegistry.getAllBindings()) {
            if (binding.enabled() && !integration.isMobTypeRegistered(binding.typeId())) {
                plugin.getLogger().warning("MythicMob binding '" + binding.typeId()
                        + "' references a type ID not registered in MythicMobs. "
                        + "The binding will be skipped until the mob is registered.");
            }
        }
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

### Return Type: Why Not CustomEntityWrapper?

McCore's `CustomEntityWrapper` wraps a Bukkit `Entity` for entity-type detection — it determines whether an entity is vanilla or custom (e.g., from a custom model) and provides `entityType()` / `customEntity()` accessors. It's designed for **type identification** at config lookup time (e.g., "what experience does this entity type give?").

`spawnMob()` returns the raw Bukkit `Entity` because:
- Callers need the entity UUID for despawn scheduling and tracking, not type identification
- The MM type ID is already known (it was passed in as a parameter)
- `CustomEntityWrapper` adds no value here — wrapping it would imply entity-type routing is needed, but it isn't
- Callers that later need to identify the entity type (e.g., loot evaluation) can wrap it themselves at that point

```java
package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
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
        ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), level);
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
                .map(ActiveMob::getMobType);
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
                    activeMob.getThreatTable().threatGain(BukkitAdapter.adapt(target), amount);
                }
            }
        });
    }
}
```

> **API Verification Note:** The exact method signatures (`getMobManager()`, `getActiveMob()`, `getThreatTable()`, `threatGain()`, `BukkitAdapter.adapt()`) are based on MM5's documented API. These should be verified against the actual MM5 dependency at implementation time.

### Why a Facade?

- MM's API has changed between major versions. A single facade means one file to update.
- Testability: callers can mock `MythicMobsIntegration` without depending on MM internals.
- All MM imports are contained in two files (`MythicMobsIntegration` and `OnMythicMobEventListener`).

---

## 5. Binding Data Model

**Package:** `us.eunoians.mcrpg.external.mythicmobs.binding`

Each data class is its own file — no inner records. All are immutable.

### 5.1 MythicMobBinding

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobBinding.java`

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration binding between a MythicMobs type ID and McRPG behavior.
 * Immutable — constructed from YAML at startup or reload.
 *
 * @param typeId        the MythicMobs internal type ID this binding applies to
 * @param enabled       whether this binding is active
 * @param despawnPolicy the despawn policy for mobs matching this binding
 * @param lootTable     the loot table evaluated on death
 * @param spawnEffects  optional spawn VFX (particles + sound)
 * @param fireEvents    whether to fire custom McRPG events for this binding
 */
public record MythicMobBinding(
        @NotNull String typeId,
        boolean enabled,
        @NotNull DespawnPolicy despawnPolicy,
        @NotNull LootTable lootTable,
        @Nullable SpawnEffects spawnEffects,
        boolean fireEvents
) {}
```

### 5.2 DespawnPolicy

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/DespawnPolicy.java`

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

/**
 * Despawn policy configuration for a bound MythicMob.
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
```

### 5.3 LootTable

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/LootTable.java`

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Loot table configuration for a bound MythicMob.
 *
 * @param exclusiveDrop if true, at most one entry wins per kill
 * @param entries       the list of loot entries, evaluated in order
 */
public record LootTable(
        boolean exclusiveDrop,
        @NotNull List<LootEntry> entries
) {}
```

### 5.4 LootEntry

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/LootEntry.java`

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A single loot table entry. Type-specific configuration is stored in
 * {@link #properties()} to keep the model abstract and extensible.
 *
 * <p>Known property keys by type:</p>
 * <ul>
 *   <li>{@code skill-book}: {@code "ability"} — the ability NamespacedKey string (e.g., {@code "mcrpg:phase_shift"})</li>
 * </ul>
 *
 * @param id         unique identifier for this entry (for logging/debugging)
 * @param type       the loot type (e.g., "skill-book", "item", "command")
 * @param chance     drop chance as a decimal (0.0 to 1.0)
 * @param properties type-specific key-value pairs parsed from YAML
 */
public record LootEntry(
        @NotNull String id,
        @NotNull String type,
        double chance,
        @NotNull Map<String, String> properties
) {

    /** Property key for the ability NamespacedKey string (used by "skill-book" type). */
    public static final String PROPERTY_ABILITY = "ability";
}
```

### 5.5 SpawnEffects

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/SpawnEffects.java`

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Spawn visual/audio effects for a bound MythicMob.
 * Supports multiple effect entries to allow mixing Bukkit particles/sounds
 * with future extensibility (ModelEngine effects, etc.).
 *
 * @param effects the list of individual effects to play on spawn
 */
public record SpawnEffects(
        @NotNull List<SpawnEffect> effects
) {

    /**
     * A single spawn effect.
     *
     * @param type  the effect type (e.g., "particle", "sound"). Extensible for future types
     *              like "modelengine" or "mythicmobs-effect"
     * @param value the effect value (Bukkit Particle name, Sound name, or ME effect ID)
     */
    public record SpawnEffect(
            @NotNull String type,
            @NotNull String value
    ) {}
}
```

### Design Decisions

**Separate files, not inner records:** Each data class gets its own file under the `binding` package. This keeps files focused and avoids a monolithic `MythicMobBinding.java`.

**`LootEntry` uses `Map<String, String> properties`:** Instead of a nullable `ability` field that only applies to one type, all type-specific config goes into a generic properties map. This means adding a new loot type (e.g., `"command"` with a `"command"` property, or `"item"` with `"material"` and `"amount"`) requires zero changes to `LootEntry` itself. The type-specific handler (in `MythicMobLootEvaluator`) reads the properties it needs. Known property keys are documented as constants on `LootEntry`.

**`SpawnEffects` uses a list of typed effects:** Instead of hardcoding `Particle` and `Sound` fields, effects are a list of `(type, value)` pairs. This allows:
- Multiple particles + multiple sounds per spawn
- Future ModelEngine effects via `type: "modelengine"` without changing the data model
- Future MythicMobs skill-based effects via `type: "mythicmobs-effect"`
- The listener resolves `type` → handler at runtime (see Section 9)

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

---

## 7. Configuration

### 7.1 Directory-Based Loading

Instead of a single monolithic YAML file, binding configs are loaded from a **directory**. This mirrors the localization system's directory-scanning pattern already in `FileManager` and prevents a single file from becoming unwieldy as bindings grow.

**Directory:** `plugins/McRPG/mythicmob_bindings/`

At startup, McRPG:
1. Creates the directory if it doesn't exist
2. Copies the bundled default file (`riptide_guardian.yml`) into it if the directory is empty
3. Scans all `.yml` files in the directory
4. Parses each file and registers all bindings found

Server owners can:
- Add new binding files (one mob per file, or grouped however they prefer)
- Delete the default file if they don't want the Riptide Guardian binding
- Organize bindings however makes sense for their server

### 7.2 Binding Loader

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobBindingLoader.java`

This is a static utility class (not a `ConfigFile` subclass) since it loads from a directory rather than a single `FileType` entry.

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Loads {@link MythicMobBinding} configurations from all YAML files
 * in the {@code mythicmob_bindings/} directory. Supports hot-reload
 * by clearing and re-populating the registry.
 */
public final class MythicMobBindingLoader {

    private static final String BINDINGS_DIRECTORY = "mythicmob_bindings";
    private static final String DEFAULT_BINDING_FILE = "riptide_guardian.yml";

    // YAML keys
    private static final String TYPE_ID = "type-id";
    private static final String ENABLED = "enabled";
    private static final String DESPAWN_HEADER = "despawn";
    private static final String DESPAWN_MAX_LIFETIME = "max-lifetime-seconds";
    private static final String DESPAWN_IF_NO_THREAT = "despawn-if-no-threat";
    private static final String DESPAWN_NO_THREAT_DELAY = "despawn-no-threat-delay-seconds";
    private static final String LOOT_HEADER = "loot";
    private static final String LOOT_EXCLUSIVE_DROP = "exclusive-drop";
    private static final String LOOT_ENTRIES = "entries";
    private static final String LOOT_ENTRY_TYPE = "type";
    private static final String LOOT_ENTRY_CHANCE = "chance";
    private static final String SPAWN_EFFECTS_HEADER = "spawn-effects";
    private static final String FIRE_EVENTS = "fire-events";

    /** Known loot entry property keys that are parsed separately from the properties map. */
    private static final List<String> RESERVED_LOOT_KEYS = List.of("type", "chance");

    private MythicMobBindingLoader() {}

    /**
     * Loads all binding files from the bindings directory into the registry.
     * Clears existing bindings first to support hot-reload.
     *
     * @param plugin   the McRPG plugin instance
     * @param registry the binding registry to populate
     */
    public static void loadBindings(@NotNull McRPG plugin, @NotNull MythicMobBindingRegistry registry) {
        Logger logger = plugin.getLogger();
        registry.clear();

        File bindingsDir = new File(plugin.getDataFolder(), BINDINGS_DIRECTORY);
        ensureDirectoryExists(plugin, bindingsDir);

        File[] files = bindingsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            logger.info("No MythicMob binding files found in " + BINDINGS_DIRECTORY + "/");
            return;
        }

        for (File file : files) {
            try {
                loadBindingFile(file, registry, logger);
            } catch (Exception e) {
                logger.warning("Failed to load binding file '" + file.getName() + "': " + e.getMessage());
            }
        }
    }

    private static void ensureDirectoryExists(@NotNull Plugin plugin, @NotNull File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
            // Copy default binding file
            try (InputStream defaultStream = plugin.getResource(BINDINGS_DIRECTORY + "/" + DEFAULT_BINDING_FILE)) {
                if (defaultStream != null) {
                    Files.copy(defaultStream, new File(dir, DEFAULT_BINDING_FILE).toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().warning("Could not copy default binding file: " + e.getMessage());
            }
        }
    }

    private static void loadBindingFile(@NotNull File file,
                                         @NotNull MythicMobBindingRegistry registry,
                                         @NotNull Logger logger) throws IOException {
        YamlDocument doc = YamlDocument.create(file, GeneralSettings.DEFAULT);

        String typeId = doc.getString(Route.fromString(TYPE_ID));
        if (typeId == null || typeId.isBlank()) {
            logger.warning("Binding file '" + file.getName() + "' missing 'type-id' field, skipping.");
            return;
        }

        boolean enabled = doc.getBoolean(Route.fromString(ENABLED), true);

        // Despawn policy
        var despawnPolicy = new DespawnPolicy(
                doc.getInt(Route.fromString(DESPAWN_HEADER + "." + DESPAWN_MAX_LIFETIME), 300),
                doc.getBoolean(Route.fromString(DESPAWN_HEADER + "." + DESPAWN_IF_NO_THREAT), true),
                doc.getInt(Route.fromString(DESPAWN_HEADER + "." + DESPAWN_NO_THREAT_DELAY), 30)
        );

        // Loot table
        boolean exclusiveDrop = doc.getBoolean(Route.fromString(LOOT_HEADER + "." + LOOT_EXCLUSIVE_DROP), true);
        List<LootEntry> lootEntries = parseLootEntries(doc, logger);
        var lootTable = new LootTable(exclusiveDrop, List.copyOf(lootEntries));

        // Spawn effects
        SpawnEffects spawnEffects = parseSpawnEffects(doc);

        boolean fireEvents = doc.getBoolean(Route.fromString(FIRE_EVENTS), true);

        var binding = new MythicMobBinding(typeId, enabled, despawnPolicy, lootTable, spawnEffects, fireEvents);
        registry.register(binding);
        logger.info("Loaded MythicMob binding: " + typeId + " from " + file.getName()
                     + " (enabled=" + enabled + ", " + lootEntries.size() + " loot entries)");
    }

    private static List<LootEntry> parseLootEntries(@NotNull YamlDocument doc,
                                                     @NotNull Logger logger) {
        List<LootEntry> entries = new ArrayList<>();
        String entriesPath = LOOT_HEADER + "." + LOOT_ENTRIES;

        if (!doc.contains(Route.fromString(entriesPath))) {
            return entries;
        }

        var entriesSection = doc.getSection(Route.fromString(entriesPath));
        if (entriesSection == null) {
            return entries;
        }

        for (String entryId : entriesSection.getRoutesAsStrings(false)) {
            String entryPrefix = entriesPath + "." + entryId + ".";
            String type = doc.getString(Route.fromString(entryPrefix + LOOT_ENTRY_TYPE), "skill-book");
            double chance = doc.getDouble(Route.fromString(entryPrefix + LOOT_ENTRY_CHANCE), 0.0);

            // All non-reserved keys become properties
            Map<String, String> properties = new HashMap<>();
            var entrySection = doc.getSection(Route.fromString(entriesPath + "." + entryId));
            if (entrySection != null) {
                for (String key : entrySection.getRoutesAsStrings(false)) {
                    if (!RESERVED_LOOT_KEYS.contains(key)) {
                        Object value = entrySection.get(Route.fromString(key));
                        if (value != null) {
                            properties.put(key, value.toString());
                        }
                    }
                }
            }

            entries.add(new LootEntry(entryId, type, chance, Map.copyOf(properties)));
        }
        return entries;
    }

    private static SpawnEffects parseSpawnEffects(@NotNull YamlDocument doc) {
        if (!doc.contains(Route.fromString(SPAWN_EFFECTS_HEADER))) {
            return null;
        }

        var effectsSection = doc.getSection(Route.fromString(SPAWN_EFFECTS_HEADER));
        if (effectsSection == null) {
            return null;
        }

        List<SpawnEffects.SpawnEffect> effects = new ArrayList<>();
        for (String key : effectsSection.getRoutesAsStrings(false)) {
            String value = effectsSection.getString(Route.fromString(key));
            if (value != null && !value.isBlank()) {
                effects.add(new SpawnEffects.SpawnEffect(key, value));
            }
        }

        return effects.isEmpty() ? null : new SpawnEffects(List.copyOf(effects));
    }
}
```

### 7.3 Default Binding File (Bundled Resource)

**File:** `src/main/resources/mythicmob_bindings/riptide_guardian.yml`

Each file represents one binding. The `type-id` field at the top level identifies the MythicMobs mob.

```yaml
# Riptide Guardian MythicMob Binding
# This file defines what McRPG does when the RiptideGuardian MythicMob
# spawns, dies, or despawns. The MythicMob itself must be configured
# in MythicMobs — this file only controls McRPG's reactions.

type-id: RiptideGuardian
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

# Spawn effects — played when the mob spawns
# Each key is an effect type, value is the effect identifier.
# Supported types: "particle" (Bukkit Particle), "sound" (Bukkit Sound)
# Future types: "modelengine" (ModelEngine effect ID), "mythicmobs-effect" (MM skill)
spawn-effects:
  particle: SPLASH
  sound: ENTITY_ELDER_GUARDIAN_CURSE

# Whether to fire custom McRPG events (CustomMobSpawnEvent, etc.)
# Third-party plugins can listen to these events
fire-events: true
```

### 7.4 Why Directory Instead of FileType?

The existing `FileType` enum loads a single file per entry. Adding directory support to `FileType` would require changing `FileManager` and affecting all existing configs. Instead, `MythicMobBindingLoader` handles its own directory scanning — following the same pattern `FileManager` already uses for localization files. This keeps the change isolated.

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
     * @param entity     the MythicMob entity
     * @param binding    the binding
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

### Spawn Effect Resolution

The listener resolves `SpawnEffect.type()` at runtime to handle different effect systems:

```java
private void playSpawnEffects(@NotNull Entity entity, @NotNull SpawnEffects spawnEffects) {
    for (SpawnEffects.SpawnEffect effect : spawnEffects.effects()) {
        switch (effect.type()) {
            case "particle" -> playParticleEffect(entity, effect.value());
            case "sound" -> playSoundEffect(entity, effect.value());
            default -> plugin.getLogger().warning(
                    "Unknown spawn effect type: '" + effect.type() + "' with value '" + effect.value() + "'");
        }
    }
}
```

Future ModelEngine support would add a `case "modelengine"` branch — the data model already supports it without changes.

### Full Listener

```java
package us.eunoians.mcrpg.listener.entity;

import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobDespawnScheduler;
import us.eunoians.mcrpg.external.mythicmobs.binding.MythicMobLootEvaluator;
import us.eunoians.mcrpg.external.mythicmobs.binding.SpawnEffects;

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
            playSpawnEffects(entity, binding.spawnEffects());
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

        // Evaluate loot
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

    private void playSpawnEffects(@NotNull Entity entity, @NotNull SpawnEffects spawnEffects) {
        for (SpawnEffects.SpawnEffect effect : spawnEffects.effects()) {
            switch (effect.type()) {
                case "particle" -> playParticleEffect(entity, effect.value());
                case "sound" -> playSoundEffect(entity, effect.value());
                default -> plugin.getLogger().warning(
                        "Unknown spawn effect type: '" + effect.type()
                        + "' with value '" + effect.value() + "'");
            }
        }
    }

    private void playParticleEffect(@NotNull Entity entity, @NotNull String particleName) {
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            entity.getWorld().spawnParticle(particle, entity.getLocation(), 30, 1.0, 1.0, 1.0, 0.1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle name: '" + particleName + "'");
        }
    }

    private void playSoundEffect(@NotNull Entity entity, @NotNull String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            entity.getWorld().playSound(entity.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound name: '" + soundName + "'");
        }
    }
}
```

> **API Verification Note:** The exact MM5 event class names (`MythicMobSpawnEvent`, `MythicMobDeathEvent`, `MythicMobDespawnEvent`) and their method signatures should be verified against the MM5 dependency at implementation time.

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

### 10.3 Registration Order

The hook registration happens in `McRPGHooksRegistrar`, which runs after `FileManager` is initialized but alongside other hooks. The listener registration happens in `McRPGListenerRegistrar`, which runs after hooks. No changes to `McRPGBootstrap` ordering needed.

```
FileManager init
  → McRPGHooksRegistrar (MythicMobsHook reads bindings directory, populates registry)
    → McRPGListenerRegistrar (registers OnMythicMobEventListener if hook exists)
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Evaluates a {@link LootTable} and drops rewards at the given location.
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
                                       @NotNull LootTable lootTable,
                                       @NotNull Player player,
                                       @NotNull Location location) {
        for (LootEntry entry : lootTable.entries()) {
            double roll = ThreadLocalRandom.current().nextDouble();
            if (roll >= entry.chance()) {
                continue;
            }

            boolean dropped = processEntry(plugin, entry, player, location);

            if (dropped && lootTable.exclusiveDrop()) {
                return;
            }
        }
    }

    private static boolean processEntry(@NotNull McRPG plugin,
                                        @NotNull LootEntry entry,
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

    private static boolean processSkillBookEntry(@NotNull McRPG plugin,
                                                 @NotNull LootEntry entry,
                                                 @NotNull Player player,
                                                 @NotNull Location location) {
        String abilityKeyStr = entry.properties().get(LootEntry.PROPERTY_ABILITY);
        if (abilityKeyStr == null) {
            plugin.getLogger().warning("Skill-book loot entry '" + entry.id()
                    + "' has no '" + LootEntry.PROPERTY_ABILITY + "' property configured.");
            return false;
        }

        NamespacedKey abilityKey = NamespacedKey.fromString(abilityKeyStr);
        if (abilityKey == null) {
            plugin.getLogger().warning("Invalid ability key format in loot entry '"
                    + entry.id() + "': " + abilityKeyStr);
            return false;
        }

        var abilityRegistry = plugin.registryAccess().registry(McRPGRegistryKey.ABILITY);
        if (!abilityRegistry.registered(abilityKey)) {
            plugin.getLogger().warning("Loot entry '" + entry.id()
                    + "' references unregistered ability: " + abilityKeyStr);
            return false;
        }

        // TODO (LLD-3): Replace with SkillBookFactory.createSkillBook() and drop at location
        // ItemStack skillBook = SkillBookFactory.createSkillBook(ability, sourceKey);
        // location.getWorld().dropItemNaturally(location, skillBook);
        plugin.getLogger().info("Skill book drop triggered for ability " + abilityKeyStr
                + " — awaiting LLD-3 implementation.");
        return true;
    }
}
```

### Design Decisions

**`switch` on type string:** Clean dispatch. Adding a new type is one case arm. If the type roster grows beyond 4-5, refactor to a `Map<String, LootEntryHandler>` strategy pattern.

**`LootEntry.PROPERTY_ABILITY`:** The handler reads from the generic `properties()` map using the documented constant. No type-specific fields on the data model.

**`ThreadLocalRandom`:** Thread-safe, no contention, appropriate for per-event random rolls.

---

## 12. Despawn Scheduling

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/binding/MythicMobDespawnScheduler.java`

Manages despawn lifecycle for bound mobs. Uses McCore's `DelayableCoreTask` and `CancelableCoreTask` abstractions instead of raw Bukkit scheduler calls, consistent with all other McRPG scheduled tasks.

### Server Reboot & Chunk Unload Resilience

**Problem:** What happens when:
1. Server reboots with living bound mobs?
2. The chunk containing a bound mob gets unloaded and the despawn timer fires?

**Server Reboot:**
- McRPG's in-memory despawn tasks are lost on shutdown (they're `ConcurrentHashMap` entries).
- MythicMobs persists its mobs and re-fires `MythicMobSpawnEvent` on startup for persisted mobs.
- When that spawn event fires, `OnMythicMobEventListener` processes it like any new spawn → schedules a fresh despawn timer.
- The mob gets a new full `max-lifetime-seconds` window. This is acceptable — the alternative (persisting remaining time to DB) adds complexity for minimal benefit. The mob was already going to die within a few minutes.

**Chunk Unload:**
- MythicMobs tracks its mobs independently of chunk state. `ActiveMob` references survive chunk unloads.
- If a despawn timer fires while the chunk is unloaded, `MythicMobsIntegration.despawnMob()` calls MM's `ActiveMob.despawn()` which handles this correctly — MM removes the mob from its tracking, and the entity is cleaned up.
- If the periodic threat-check task fires while the chunk is unloaded, `getThreatTableTargets()` returns empty (no players nearby). The grace period accumulates and the mob despawns. This is the desired behavior — a mob in an unloaded chunk with no players nearby should despawn.

**Plugin disable/reload:**
- On `McRPG.onDisable()`, all scheduled Bukkit tasks are cancelled automatically (Bukkit cancels all tasks for a disabling plugin).
- `activeTasks` map becomes stale but harmless — no references leak.
- On re-enable, MM re-fires spawn events and fresh tasks are scheduled.

```java
package us.eunoians.mcrpg.external.mythicmobs.binding;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.CancelableCoreTask;
import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import org.bukkit.Bukkit;
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
 * Uses McCore task abstractions ({@link DelayableCoreTask}, {@link CancelableCoreTask}).
 */
public final class MythicMobDespawnScheduler {

    private static final double THREAT_CHECK_INTERVAL_SECONDS = 5.0;
    private static final Map<UUID, ScheduledDespawn> activeTasks = new ConcurrentHashMap<>();

    private MythicMobDespawnScheduler() {}

    /**
     * Schedules despawn tasks for a bound mob based on its binding's despawn policy.
     *
     * @param plugin  the McRPG plugin instance
     * @param mobUUID the UUID of the mob entity
     * @param binding the binding containing the despawn policy
     */
    public static void schedule(@NotNull McRPG plugin,
                                @NotNull UUID mobUUID,
                                @NotNull MythicMobBinding binding) {
        DespawnPolicy policy = binding.despawnPolicy();
        DelayableCoreTask lifetimeTask = null;
        CancelableCoreTask threatCheckTask = null;

        // Max lifetime timer
        if (policy.maxLifetimeSeconds() > 0) {
            lifetimeTask = new DelayableCoreTask(plugin, policy.maxLifetimeSeconds()) {
                @Override
                public void run() {
                    despawnMob(plugin, mobUUID, "max lifetime exceeded");
                }
            };
            lifetimeTask.runTask();
        }

        // Periodic threat table check
        if (policy.despawnIfNoThreat()) {
            int graceDelaySeconds = policy.despawnNoThreatDelaySeconds();
            threatCheckTask = new CancelableCoreTask(plugin, THREAT_CHECK_INTERVAL_SECONDS) {
                private double emptyThreatSeconds = 0;

                @Override
                protected void onIntervalComplete() {
                    MythicMobsIntegration integration = getIntegration(plugin);
                    if (integration == null) {
                        return;
                    }

                    Collection<UUID> targets = integration.getThreatTableTargets(mobUUID);
                    if (targets.isEmpty()) {
                        emptyThreatSeconds += THREAT_CHECK_INTERVAL_SECONDS;
                        if (emptyThreatSeconds >= graceDelaySeconds) {
                            despawnMob(plugin, mobUUID, "no threat targets");
                        }
                    } else {
                        emptyThreatSeconds = 0;
                    }
                }
            };
            threatCheckTask.runTask();
        }

        if (lifetimeTask != null || threatCheckTask != null) {
            activeTasks.put(mobUUID, new ScheduledDespawn(
                    lifetimeTask != null ? lifetimeTask.getBukkitTaskId() : -1,
                    threatCheckTask != null ? threatCheckTask.getBukkitTaskId() : -1));
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
            if (scheduled.lifetimeTaskId != -1) {
                Bukkit.getScheduler().cancelTask(scheduled.lifetimeTaskId);
            }
            if (scheduled.threatCheckTaskId != -1) {
                Bukkit.getScheduler().cancelTask(scheduled.threatCheckTaskId);
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

    private record ScheduledDespawn(int lifetimeTaskId, int threatCheckTaskId) {}
}
```

### Design Decisions

**McCore tasks instead of raw Bukkit scheduler:** `DelayableCoreTask` for the one-shot lifetime timer, `CancelableCoreTask` for the repeating threat check. Follows the established McRPG pattern (see `AbilityHolder.addActiveAbility()`, `BleedManager`, etc.).

**Task ID tracking for cancellation:** McCore tasks are tracked by Bukkit task ID (via `getBukkitTaskId()`) in a `ConcurrentHashMap`. This mirrors the pattern in `AbilityHolder` where `abilityCooldownExpireTasks` maps keys to task IDs for later cancellation.

**`ConcurrentHashMap`:** Tasks may be cancelled from different contexts (death event handler vs. scheduled task firing on the main thread). While both run on the main thread in Bukkit, the concurrent map is defensive and signals intent.

**Grace period as seconds accumulation:** Rather than scheduling a single delayed task when the threat table first empties (which would need cancellation if a player re-engages), the periodic check accumulates empty seconds. Simpler state management.

---

## 13. Config Responsibility: MM vs McRPG

This is an important question. Here's the breakdown of what lives where and why:

### What MythicMobs Already Handles

| Config | Where | Why MM Owns It |
|---|---|---|
| Mob stats (HP, damage, armor) | MM mob YAML | MM's mob definition system |
| Mob AI and skills | MM mob YAML | MM's skill/AI system (Phase Shift, Whirlpool, etc.) |
| Mob model/appearance | MM mob YAML (+ ModelEngine) | MM's rendering hooks |
| ThreatTable enabled/behavior | MM mob YAML | MM's native aggro system |
| Mob targeting behavior | MM mob YAML | MM's targeting system |
| Spawn animation/particles | MM mob YAML | MM supports onSpawn skills |

### What McRPG Must Handle

| Config | Where | Why McRPG Owns It |
|---|---|---|
| Loot table → skill books | McRPG binding YAML | Skill books are McRPG items. MM can't create them. |
| Despawn policy (lifetime + threat check) | McRPG binding YAML | McRPG schedules its own cleanup tasks. MM's native despawn rules (chunk unload, `/mm mobs killall`) are separate concerns. |
| Fire custom McRPG events | McRPG binding YAML | McRPG event bus, not MM's. |

### What Could Go Either Way

| Config | Current Owner | Could Move? | Recommendation |
|---|---|---|---|
| Spawn VFX (particles/sounds) | McRPG binding YAML | Yes → MM onSpawn skill | **Move to MM.** MM's onSpawn skill system already handles this well. Remove `spawn-effects` from binding config. Server owners configure it in MM's mob YAML where they already configure all other mob VFX. |

### Recommendation

Remove `spawn-effects` from the binding config. Server owners should configure spawn particles/sounds in MythicMobs' mob YAML using MM's `onSpawn` skill — that's where they're already configuring every other visual/audio aspect of the mob.

This reduces McRPG's binding config to only what McRPG **must** own:
- **Loot tables** (McRPG items)
- **Despawn policy** (McRPG's cleanup logic)
- **Event firing** (McRPG's event bus)

The binding config becomes leaner and the responsibility split is clearer.

> **Decision needed:** If you agree with removing spawn-effects, I'll strip it from the data model, loader, listener, and config. The `SpawnEffects` and `SpawnEffect` classes go away entirely. If you want to keep it as an option (for cases where server owners want McRPG to handle VFX independently of MM), I'll keep it but add a comment noting that MM's onSpawn is the preferred approach.

---

## 14. Error Handling & Graceful Degradation

| Scenario | Behavior |
|---|---|
| MythicMobs not installed | `MythicMobsHook` not registered. Listener not registered. Binding directory still created (no error). |
| MM type ID not found in MM registry | Warning logged at startup per binding. Binding remains loaded (mob may be added later via MM reload). |
| Binding references invalid ability key | Warning logged at startup. Loot entry skipped at drop time. |
| MM event fires for unknown type ID | `getBinding()` returns empty. No processing. |
| Loot entry has unknown type | Warning logged. Entry skipped. Other entries still evaluated. |
| Loot entry missing required property | Warning logged. Entry skipped. |
| Despawn task fires after mob already dead | `integration.despawnMob()` returns false. No error. |
| No `.yml` files in bindings directory | No bindings loaded. No errors. System is inert. |
| Invalid YAML in a binding file | That file skipped with warning. Other files still loaded. |
| Server restart with living bound mobs | MM re-fires spawn event → fresh despawn timer scheduled. |
| Chunk unload with active despawn timer | MM handles entity state. Despawn call still works via MM's `ActiveMob`. |
| Plugin disable/reload | Bukkit cancels all tasks. MM re-fires spawn events on re-enable. |

---

## 15. Test Plan

### 15.1 Unit Tests (src/test/java)

| Test Class | Tests |
|---|---|
| `MythicMobBindingRegistryTest` | Register, unregister, getBinding (enabled/disabled), getAllBindings, clear |
| `MythicMobBindingLoaderTest` | Parse valid YAML, parse with missing sections, missing type-id field, multiple files in directory |
| `MythicMobLootEvaluatorTest` | Exclusive-drop stops after first hit, non-exclusive evaluates all, unknown type logged, chance 0.0 never drops, chance 1.0 always drops, missing property handled |
| `LootEntryTest` | Properties map immutability, PROPERTY_ABILITY constant |

### 15.2 Tests Requiring MockBukkit (extend McRPGBaseTest)

| Test Class | Tests |
|---|---|
| `CustomMobSpawnEventTest` | Event creation, cancellation, handler list |
| `CustomMobDeathEventTest` | Event creation, killer UUID nullable |
| `CustomMobDespawnEventTest` | Event creation, handler list |

### 15.3 Manual Testing (Paper Server)

| Scenario | Verification |
|---|---|
| MM installed, valid binding file | Spawn event fires, despawn timer starts |
| MM installed, invalid type ID | Warning logged, no crash |
| MM not installed | No errors, no listener registered, directory created silently |
| Kill bound mob | Death event fires, loot rolls execute (placeholder log for now) |
| Mob despawn by lifetime | Mob removed after configured seconds |
| Mob despawn by empty threat | Mob removed after grace period when all players leave |
| Server restart with living mob | MM re-fires spawn, McRPG re-schedules despawn |
| Add new `.yml` binding file + `/mcrpg reload` | New binding loaded |
| Multiple binding files in directory | All loaded correctly |

---

## 16. File Manifest

### New Files

| File | Type | Description |
|---|---|---|
| `external/mythicmobs/MythicMobsHook.java` | Hook | Plugin hook for MythicMobs |
| `external/mythicmobs/MythicMobsIntegration.java` | Facade | All MM API calls |
| `external/mythicmobs/binding/MythicMobBinding.java` | Record | Top-level binding data |
| `external/mythicmobs/binding/DespawnPolicy.java` | Record | Despawn policy config |
| `external/mythicmobs/binding/LootTable.java` | Record | Loot table config |
| `external/mythicmobs/binding/LootEntry.java` | Record | Single loot entry with properties map |
| `external/mythicmobs/binding/SpawnEffects.java` | Record | Spawn VFX config (pending removal — see Section 13) |
| `external/mythicmobs/binding/MythicMobBindingRegistry.java` | Registry | Type ID → binding map |
| `external/mythicmobs/binding/MythicMobBindingLoader.java` | Loader | Directory-based YAML parser |
| `external/mythicmobs/binding/MythicMobLootEvaluator.java` | Utility | Loot table evaluation |
| `external/mythicmobs/binding/MythicMobDespawnScheduler.java` | Utility | Despawn task management (McCore tasks) |
| `event/entity/mythicmob/CustomMobEvent.java` | Event | Abstract base |
| `event/entity/mythicmob/CustomMobSpawnEvent.java` | Event | Spawn event (cancellable) |
| `event/entity/mythicmob/CustomMobDeathEvent.java` | Event | Death event |
| `event/entity/mythicmob/CustomMobDespawnEvent.java` | Event | Despawn event |
| `listener/entity/OnMythicMobEventListener.java` | Listener | MM event bridge |
| `src/main/resources/mythicmob_bindings/riptide_guardian.yml` | Config | Default binding |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

### Modified Files

| File | Change |
|---|---|
| `build.gradle.kts` | Add Lumine repo + Mythic-Dist compileOnly dependency |
| `registry/plugin/McRPGPluginHookKey.java` | Add `MYTHICMOBS` key |
| `bootstrap/McRPGHooksRegistrar.java` | Add MythicMobs hook registration |
| `bootstrap/McRPGListenerRegistrar.java` | Add conditional MM listener registration |

### Removed from Previous Draft

| Item | Reason |
|---|---|
| `FileType.MYTHICMOB_BINDINGS_CONFIG` | Not needed — directory-based loading bypasses FileType |
| `MythicMobBindingsConfigFile.java` | Replaced by `MythicMobBindingLoader` |
| Inner records on `MythicMobBinding` | Extracted to separate files |
| `LootEntry.ability` field | Replaced by generic `properties` map |
| Raw Bukkit scheduler calls | Replaced by McCore `DelayableCoreTask` / `CancelableCoreTask` |
| Fully qualified import names in code | Fixed |
