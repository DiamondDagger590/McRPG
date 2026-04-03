# Low-Level Design: MythicMobs Binding System (LLD-1)

**Status:** Implemented
**Date:** 2026-03-16
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Sections 2, 10
**Scope:** Hook, event bridge, PDC tagging, custom drop type, custom events

---

## Table of Contents

1. [Overview](#1-overview)
2. [Build Dependency](#2-build-dependency)
3. [MythicMobsHook](#3-mythicmobshook)
4. [FishingMobKeys](#4-fishingmobkeys)
5. [MythicMobsListener](#5-mythicmobslistener)
6. [McRPGSkillBookDrop](#6-mcrpgskillbookdrop)
7. [Custom Events](#7-custom-events)
8. [Bootstrap Registration](#8-bootstrap-registration)
9. [Config Responsibility: MM vs McRPG](#9-config-responsibility-mm-vs-mcrpg)
10. [Error Handling & Graceful Degradation](#10-error-handling--graceful-degradation)
11. [Test Plan](#11-test-plan)
12. [File Manifest](#12-file-manifest)

---

## 1. Overview

The MythicMobs Binding System bridges MythicMobs lifecycle events into McRPG's domain. McRPG never wraps or manages the MythicMob entity — it uses PersistentDataContainer (PDC) tags to identify fishing mobs and fires McRPG-domain events that downstream systems consume.

**This LLD is implemented.** The code below reflects what was actually built.

### Design Philosophy

- **PDC-based identification** instead of a binding registry. When McRPG spawns a mob via MM, it tags the entity with PDC keys. When MM fires spawn/death events, the listener checks for those tags.
- **No binding registry, no loot evaluation, no despawn scheduling.** Loot is owned by MM's drop table system (with `mcrpg_skillbook` as a custom drop type). Despawn is owned by MM's native `~onTimer`/`~onDropCombat` skills.
- **MM API calls centralized on the hook.** `MythicMobsHook` provides `spawnMob()` and `isMobTypeRegistered()` so no other class needs MM imports (except the listener, which must reference MM event types in `@EventHandler` signatures).

### Boundary with LLD-2 (Fishing Mob Spawn System)

This LLD does **not** cover:
- Spawn probability logic, mob pool selection, per-player state (LLD-2)
- The `FishingMobSpawnListener` that triggers spawns on `PlayerFishEvent` (LLD-2)

This LLD **does** cover:
- `MythicMobsHook.spawnMob()` — the MM API call that LLD-2 invokes
- The event bridge that fires `FishingMobSpawnEvent`/`FishingMobDeathEvent` for LLD-2 to consume

### Boundary with LLD-3 (Skill Book System)

This LLD covers `McRPGSkillBookDrop` (the MM custom drop type). LLD-3 will cover the consumption listener and `SkillBookFactory`.

---

## 2. Build Dependency

**Modified file:** `build.gradle.kts`

```kotlin
// Repository
maven("https://mvn.lumine.io/repository/maven-public/") // MythicMobs

// Dependency
val mythicMobsVersion = "5.7.2"
compileOnly("io.lumine:Mythic-Dist:$mythicMobsVersion")
```

MythicMobs is a `compileOnly` dependency — it is not shaded into the McRPG jar. The MM classes are only available at runtime when the MM plugin is installed.

**Soft dependency declaration** (already present):

`src/main/resources/paper-plugin.yml`:
```yaml
dependencies:
  server:
    MythicMobs:
      load: BEFORE
      required: false
```

---

## 3. MythicMobsHook

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsHook.java`

A `PluginHook<McRPG>` registered when MythicMobs is present. Follows the same pattern as `GeyserHook`, `LunarClientHook`, etc. — the hook provides domain-specific utility methods wrapping the external API.

### Current Implementation (as committed)

```java
package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

public class MythicMobsHook extends PluginHook<McRPG> {

    public MythicMobsHook(@NotNull McRPG plugin) {
        super(plugin);
    }
}
```

### Additions from LLD-2

LLD-2 adds two methods to this class (not yet implemented):

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

This centralizes all outbound MM API calls on the hook, so no other McRPG class needs to import `MythicBukkit`, `MythicMob`, `ActiveMob`, or `BukkitAdapter`.

### Hook Key

**File:** `src/main/java/us/eunoians/mcrpg/registry/plugin/McRPGPluginHookKey.java`

```java
PluginHookKey<MythicMobsHook> MYTHIC_MOBS = create(MythicMobsHook.class);
```

---

## 4. FishingMobKeys

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/FishingMobKeys.java`

PDC key constants written to spawned entities. Read by `MythicMobsListener` to identify fishing mobs.

```java
package us.eunoians.mcrpg.external.mythicmobs;

import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.util.McRPGMethods;

public final class FishingMobKeys {

    /** Boolean key indicating this entity was spawned by the McRPG fishing skill. */
    public static final NamespacedKey FISHING_MOB_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "fishing_mob");

    /** String key containing the UUID of the player who triggered the mob spawn. */
    public static final NamespacedKey ANGLER_UUID_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "fishing_mob_angler");

    private FishingMobKeys() {}
}
```

### Why PDC Instead of a Tracking Map?

1. **Survives server restarts** — PDC is persisted by Minecraft on the entity
2. **No cross-system coordination** — the listener reads tags independently of whoever wrote them
3. **Enables efficient death callback** — LLD-2's listener reads `ANGLER_UUID_KEY` directly from the dead mob's PDC to find the owning player (O(1) instead of iterating all players)

---

## 5. MythicMobsListener

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsListener.java`

Bridges MythicMobs events into McRPG's fishing mob event system. Only registered when MythicMobs is present.

Three responsibilities:
1. Registers the `mcrpg_skillbook` custom drop type via `MythicDropLoadEvent`
2. Fires `FishingMobSpawnEvent` when a PDC-tagged mob spawns
3. Fires `FishingMobDeathEvent` when a PDC-tagged mob dies

```java
package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.fishing.FishingMobDeathEvent;
import us.eunoians.mcrpg.event.fishing.FishingMobSpawnEvent;

import java.util.UUID;

public class MythicMobsListener implements Listener {

    @EventHandler
    public void onMythicDropLoad(@NotNull MythicDropLoadEvent event) {
        if (event.getDropName().equalsIgnoreCase("mcrpg_skillbook")) {
            event.register(new McRPGSkillBookDrop(event.getConfig(), event.getArgument()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobSpawn(@NotNull MythicMobSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN)) {
            return;
        }

        String anglerUuidString = entity.getPersistentDataContainer()
                .get(FishingMobKeys.ANGLER_UUID_KEY, PersistentDataType.STRING);
        if (anglerUuidString == null) {
            return;
        }

        Player angler = Bukkit.getPlayer(UUID.fromString(anglerUuidString));
        if (angler == null) {
            return;
        }

        String mobType = event.getMobType().getInternalName();
        FishingMobSpawnEvent fishingEvent = new FishingMobSpawnEvent(
                angler, entity, mobType, entity.getLocation());
        Bukkit.getPluginManager().callEvent(fishingEvent);

        if (fishingEvent.isCancelled()) {
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMythicMobDeath(@NotNull MythicMobDeathEvent event) {
        Entity entity = event.getEntity();
        if (!entity.getPersistentDataContainer().has(FishingMobKeys.FISHING_MOB_KEY, PersistentDataType.BOOLEAN)) {
            return;
        }

        Player killer = null;
        if (event.getKiller() instanceof Player player) {
            killer = player;
        }

        String mobType = event.getMobType().getInternalName();
        FishingMobDeathEvent fishingEvent = new FishingMobDeathEvent(entity, killer, mobType);
        Bukkit.getPluginManager().callEvent(fishingEvent);
    }
}
```

### Why MM Event Imports Are Acceptable Here

The listener must reference `MythicMobSpawnEvent`, `MythicMobDeathEvent`, and `MythicDropLoadEvent` because they are Bukkit event types that must appear in `@EventHandler` method signatures. This is the same pattern as any other event listener — the imports are unavoidable. The hook centralizes **outbound API calls** (spawn, query), not inbound event types.

---

## 6. McRPGSkillBookDrop

**File:** `src/main/java/us/eunoians/mcrpg/external/mythicmobs/McRPGSkillBookDrop.java`

A custom MythicMobs drop type implementing `IItemDrop`. Registered as `mcrpg_skillbook` in MM drop tables.

### MM Drop Table Usage

```yaml
# In the MythicMobs mob YAML:
RiptideGuardian:
  Type: DROWNED
  Drops:
    - mcrpg_skillbook{skill=Fishing} 1 0.1
```

### Implementation

```java
package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;

public class McRPGSkillBookDrop implements IItemDrop {

    public static final NamespacedKey SKILL_BOOK_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book");
    public static final NamespacedKey SKILL_BOOK_SKILL_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book_skill");

    private final String skillName;

    public McRPGSkillBookDrop(@NotNull MythicLineConfig config, @NotNull String argument) {
        this.skillName = config.getString("skill", argument);
    }

    @Override
    @NotNull
    public AbstractItemStack getDrop(@NotNull DropMetadata dropMetadata, double amount) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, Math.max(1, (int) amount));
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(Component.text("Skill Book: " + skillName)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("A mysterious tome of knowledge.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Use this to gain experience in " + skillName + ".")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(SKILL_BOOK_SKILL_KEY, PersistentDataType.STRING, skillName);

        itemStack.setItemMeta(meta);
        return new BukkitItemStack(itemStack);
    }
}
```

### Why MM Imports Are Acceptable Here

`McRPGSkillBookDrop` implements `IItemDrop` — it **is** a MythicMobs extension point. The MM API types (`AbstractItemStack`, `MythicLineConfig`, `DropMetadata`, `BukkitItemStack`) are part of the contract. This class lives in `external/mythicmobs/` specifically because it is tightly coupled to MM.

---

## 7. Custom Events

### 7.1 FishingMobSpawnEvent

**File:** `src/main/java/us/eunoians/mcrpg/event/fishing/FishingMobSpawnEvent.java`

Fired by `MythicMobsListener` after a PDC-tagged mob spawns. Cancellable — cancelling removes the entity.

| Field | Type | Description |
|---|---|---|
| `angler` | `Player` | The player whose fishing triggered the spawn |
| `mob` | `Entity` | The spawned MythicMob entity |
| `mythicMobType` | `String` | MM internal type ID |
| `spawnLocation` | `Location` | Where the mob was spawned |

### 7.2 FishingMobDeathEvent

**File:** `src/main/java/us/eunoians/mcrpg/event/fishing/FishingMobDeathEvent.java`

Fired by `MythicMobsListener` after a PDC-tagged mob dies. Not cancellable.

| Field | Type | Description |
|---|---|---|
| `mob` | `Entity` | The dead MythicMob entity |
| `killer` | `Player` (nullable) | The player who killed the mob, or null |
| `mythicMobType` | `String` | MM internal type ID |

### 7.3 Event Flow

```
MythicMobs spawns entity
  -> MythicMobSpawnEvent (MM event)
    -> MythicMobsListener.onMythicMobSpawn()
      -> Check PDC: FISHING_MOB_KEY present?
      -> Read ANGLER_UUID_KEY, resolve Player
      -> Fire FishingMobSpawnEvent (McRPG event, cancellable)
      -> If cancelled: entity.remove()

MythicMobs mob dies (or despawns)
  -> MythicMobDeathEvent (MM event)
    -> MythicMobsListener.onMythicMobDeath()
      -> Check PDC: FISHING_MOB_KEY present?
      -> Resolve killer (if Player)
      -> Fire FishingMobDeathEvent (McRPG event)
        -> Consumed by FishingMobSpawnListener (LLD-2) for state cleanup

MythicMobs loads drop tables
  -> MythicDropLoadEvent (MM event)
    -> MythicMobsListener.onMythicDropLoad()
      -> Register mcrpg_skillbook drop type
```

---

## 8. Bootstrap Registration

### 8.1 Hook Registration

**File:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGHooksRegistrar.java`

```java
if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
    logger.info("MythicMobs found... enabling fishing mob support.");
    pluginHookRegistry.register(new MythicMobsHook(plugin));
}
```

### 8.2 Listener Registration

**File:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGListenerRegistrar.java`

```java
if (plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK)
        .pluginHook(McRPGPluginHookKey.MYTHIC_MOBS).isPresent()) {
    Bukkit.getPluginManager().registerEvents(new MythicMobsListener(), plugin);
}
```

### 8.3 Registration Order Issue

The listener registrar runs **before** the hooks registrar in `McRPGBootstrap`:

```java
new McRPGListenerRegistrar().register(bootstrapContext);  // line 72
new McRPGHooksRegistrar().register(bootstrapContext);      // line 73
```

The listener registration checks for the hook's presence, but the hook hasn't been registered yet. This means the hook check always returns empty and the listener is never registered under the current order.

**Fix (deferred to LLD-2 implementation):** Swap the registration order so hooks register first:

```java
new McRPGHooksRegistrar().register(bootstrapContext);      // Hooks FIRST
new McRPGListenerRegistrar().register(bootstrapContext);    // Listeners AFTER
```

> **Implementation note:** Verify on a live server. If the existing code works despite the apparent ordering issue, there may be a subtlety in the bootstrap lifecycle not captured here.

---

## 9. Config Responsibility: MM vs McRPG

| Concern | Owner | Where Configured |
|---|---|---|
| Mob stats (HP, damage, armor) | MythicMobs | MM mob YAML |
| Mob abilities (skills, AI) | MythicMobs | MM mob YAML |
| Mob despawn (TTL, threat table) | MythicMobs | MM mob YAML (`~onTimer`, `~onDropCombat`) |
| Loot drops (items, skill books) | MythicMobs | MM mob YAML (`Drops:` section) |
| Spawn effects (particles, sound) | MythicMobs | MM mob YAML (`Skills:` section) |
| Custom drop type registration | McRPG | `McRPGSkillBookDrop` registered via `MythicDropLoadEvent` |
| Mob spawn triggering | McRPG | `fishing_mob_spawn_configuration.yml` (LLD-2) |
| Spawn probability / anti-cheese | McRPG | `fishing_mob_spawn_configuration.yml` (LLD-2) |
| PDC tagging of spawned mobs | McRPG | `FishingMobSpawnListener` writes tags (LLD-2) |
| Event bridging (MM -> McRPG) | McRPG | `MythicMobsListener` reads tags, fires McRPG events |

McRPG's role is minimal: trigger spawns, tag entities, bridge events, and provide the `mcrpg_skillbook` drop type. Everything about the mob itself is owned by MythicMobs config.

---

## 10. Error Handling & Graceful Degradation

| Scenario | Behavior |
|---|---|
| MythicMobs not installed | Hook not registered, listener not registered, system is inert. Warning logged at startup |
| MM type ID not found in MM registry | `MythicMobsHook.spawnMob()` returns empty, warning logged |
| PDC tag missing on MM mob | Listener ignores the event (not a fishing mob) |
| Angler UUID invalid or player offline | Listener ignores the spawn event (no `FishingMobSpawnEvent` fired) |
| `mcrpg_skillbook` drop with invalid skill name | Item created with the raw skill name string. Consumption validation happens in LLD-3 |
| Server restart with living fishing mob | MM persists the mob. On next MM spawn event, listener fires `FishingMobSpawnEvent`. Player tracking state is lost (session-only, handled by LLD-2) |

---

## 11. Test Plan

### 11.1 Unit Tests (src/test/java)

| Test Class | Tests |
|---|---|
| `FishingMobKeysTest` | Key namespaces are correct, keys are non-null |

### 11.2 Tests Requiring MockBukkit (extend McRPGBaseTest)

| Test Class | Tests |
|---|---|
| `FishingMobSpawnEventTest` | Event creation, cancellation, field accessors, handler list |
| `FishingMobDeathEventTest` | Event creation, null killer, field accessors, handler list |

### 11.3 Manual Testing (Paper Server with MythicMobs)

| Scenario | Verification |
|---|---|
| MM installed, mob spawned with PDC tags | `FishingMobSpawnEvent` fires, angler resolved correctly |
| MM mob dies | `FishingMobDeathEvent` fires, killer resolved (or null) |
| `FishingMobSpawnEvent` cancelled | Entity removed from world |
| MM mob without PDC tags spawns/dies | No McRPG events fired |
| `mcrpg_skillbook` drop configured | Enchanted book drops with correct PDC tags and display |
| MM not installed | No errors, no listeners registered |

---

## 12. File Manifest

### New Files (Implemented)

| File | Type | Description |
|---|---|---|
| `external/mythicmobs/MythicMobsHook.java` | Hook | Plugin hook for MM presence + API facade |
| `external/mythicmobs/MythicMobsListener.java` | Listener | Bridges MM events into McRPG events via PDC |
| `external/mythicmobs/FishingMobKeys.java` | Constants | PDC `NamespacedKey` constants for fishing mobs |
| `external/mythicmobs/McRPGSkillBookDrop.java` | Drop | MM custom drop type (`mcrpg_skillbook`) |
| `event/fishing/FishingMobSpawnEvent.java` | Event | Cancellable spawn event for fishing mobs |
| `event/fishing/FishingMobDeathEvent.java` | Event | Death event for fishing mobs |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

### Modified Files

| File | Change |
|---|---|
| `build.gradle.kts` | Added MythicMobs repository and `compileOnly` dependency |
| `registry/plugin/McRPGPluginHookKey.java` | Added `MYTHIC_MOBS` hook key |
| `bootstrap/McRPGHooksRegistrar.java` | Added conditional MM hook registration |
| `bootstrap/McRPGListenerRegistrar.java` | Added conditional MM listener registration |

### Not Modified (Pre-existing)

| File | Relevance |
|---|---|
| `src/main/resources/paper-plugin.yml` | MythicMobs already listed as optional soft dependency |
