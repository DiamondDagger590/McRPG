# Low-Level Design: MythicMobs Example Configuration (LLD-4)

**Status:** Draft
**Date:** 2026-04-03
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Sections 4, 8, 9
**Scope:** Bundled MythicMobs mob YAML, resource extraction logic, mob abilities, drop table, despawn config

---

## Table of Contents

1. [Overview](#1-overview)
2. [Design Decisions](#2-design-decisions)
3. [Mob Statistics & Traits](#3-mob-statistics--traits)
4. [Mob Abilities (MythicMobs Skills)](#4-mob-abilities-mythicmobs-skills)
5. [Drop Table](#5-drop-table)
6. [Despawn Configuration](#6-despawn-configuration)
7. [Full RiptideGuardian.yml](#7-full-riptideguardianyml)
8. [Resource Extraction](#8-resource-extraction)
9. [Bootstrap Registration](#9-bootstrap-registration)
10. [Server Owner Customization Guide](#10-server-owner-customization-guide)
11. [Edge Cases & Graceful Degradation](#11-edge-cases--graceful-degradation)
12. [Test Plan](#12-test-plan)
13. [File Manifest](#13-file-manifest)
14. [Future LLD Notes](#14-future-lld-notes)

---

## 1. Overview

This LLD defines the bundled MythicMobs example configuration for the Riptide Guardian — the first fishing mob shipped with McRPG. The configuration is a MythicMobs pack deployed to `plugins/MythicMobs/Packs/McRPG/`, containing mob definitions, drop tables, and skill configurations.

**This LLD produces:**
- A MythicMobs pack (`Packs/McRPG/`) with mob, drop table, and skill YAML files bundled in the McRPG JAR
- A custom MythicMobs mechanic (`mcrpg_ability`) that delegates mob ability execution to McRPG's ability system
- A custom MythicMobs condition (`mcrpg_ability_unlocked`) for unlock-aware drop rates
- Java extraction logic that deploys the pack to the MythicMobs directory on first startup
- Documentation of every skill, stat, and drop table entry for maintainability

### Boundary with Prior LLDs

| LLD | Relationship |
|-----|-------------|
| **LLD-1** (MythicMobs Binding) | Provides `McRPGSkillBookDrop` custom drop type used in the drop table. Provides `MythicMobsListener` that bridges MM events into McRPG. |
| **LLD-2** (Fishing Mob Spawn) | References `RiptideGuardian` as the `mythicmobs-mob-id` in the mob pool config. Owns spawn triggering — this LLD owns what happens after the mob exists. |
| **LLD-3** (Skill Book System) | Defines the `mcrpg_skillbook{ability=...}` drop format and item consumption flow. This LLD's drop table produces those items. |

### What This LLD Does NOT Cover

- Player abilities unlocked by skill books (LLD-6)
- UnlockCondition refactor (LLD-5)
- Custom model / ModelEngine integration (future backlog)
- Additional mob variants or tiers (future backlog)

---

## 2. Design Decisions

### 2.1 MythicMobs Pack Structure

The example config ships as a MythicMobs **pack** — a self-contained directory under `plugins/MythicMobs/Packs/McRPG/`:

```
plugins/MythicMobs/Packs/McRPG/
  Mobs/
    RiptideGuardian.yml
  Skills/
    RiptideGuardianSkills.yml
  DropTables/
    RiptideGuardianDrops.yml
```

MythicMobs automatically discovers and loads packs from `Packs/`. Benefits over scattering files in `Mobs/`:
- **Namespaced** — all McRPG content is isolated from server owner custom mobs
- **Easy to identify** — obvious what came from McRPG vs custom content
- **Easy to disable** — rename or delete the `McRPG/` folder to remove all McRPG mobs
- **Extensible** — future mobs (Cavern Golem, Timber Wraith) drop into the same pack

### 2.2 Player-Usable Skill Book Drops

Of the four mob abilities, only **two** drop as skill books (player-unlockable abilities):

| Ability | Drops as Skill Book | Rationale |
|---------|:---:|-----------|
| Phase Shift | **Yes** | Mobility/teleport is a universally useful player ability |
| Whirlpool | **Yes** | AoE zone control translates well to player combat |
| Waterlogged Strike | No | A generic ranged projectile doesn't feel "special" as an unlock |
| Tsunami Wall | No | Particle wall mechanic is mob-centric, poor player UX |

All four abilities are McRPG abilities executed via the `mcrpg_ability` mechanic — the mob uses them all in combat. Only the two marked above appear in the drop table as `mcrpg_skillbook` entries (player-unlockable).

### 2.3 Rarity-Differentiated Drop Rates

Skill book drops have different rarities to create chase items:

| Skill Book | Drop Chance | Rarity Feel |
|------------|:-----------:|-------------|
| Whirlpool | 12% | Uncommon — players see it regularly |
| Phase Shift | 5% | Rare — exciting drop, worth farming for |

These are starting values. Server owners can tune them in their copy of the YAML.

### 2.4 Unlock-Aware Drop Rate Reduction

Players who have already unlocked an ability receive a **reduced** skill book drop rate for that ability. This discourages farming books purely for resale/trading while still allowing drops for alt accounts or guild members.

**Implementation:** A custom MythicMobs condition (`mcrpg_ability_unlocked`) is registered via `MythicConditionLoadEvent`. The mob's drops are split into DropTables with `TriggerConditions` (checked against the killer):

| Scenario | Whirlpool Rate | Phase Shift Rate |
|----------|:-:|:-:|
| Ability **not** unlocked | 12% | 5% |
| Ability **already** unlocked | 2% | 1% |

Each ability gets two DropTable entries — one for unlocked, one for not-unlocked — referenced from the mob's `Drops:` list. MythicMobs DropTables support `TriggerConditions` at the table level (per the [MM wiki](https://git.mythiccraft.io/mythiccraft/MythicMobs/-/wikis/drops/DropTables)), which evaluate against the killing player. Individual drop lines do not support inline conditions, so the split-table approach is required.

The condition syntax follows MM's standard `condition{params} true/false` format:
```yaml
TriggerConditions:
- mcrpg_ability_unlocked{ability=mcrpg:whirlpool} false
```

### 2.5 Extraction Strategy: First-Run Copy

McRPG extracts the bundled pack files to `plugins/MythicMobs/Packs/McRPG/` **only if each file does not already exist**. This means:
- First install: file is auto-extracted, mob works out of the box
- Subsequent runs: server owner's modifications are preserved
- Updates: McRPG never overwrites a customized file. If the bundled version changes, server owners must manually update or delete-and-restart.

### 2.6 McRPG Owns Ability Execution, MM Owns AI

The mob's combat abilities are executed via a custom MythicMobs mechanic (`mcrpg_ability`) that delegates to McRPG's ability system. MythicMobs owns the AI layer — when to fire, cooldowns, conditions, and targeting — while McRPG owns execution: damage formulas, effects, attribute scaling, and event firing.

This means:
- **Balance is centralized** — tuning an ability in McRPG automatically applies to mob and (future) player versions
- **McRPG events fire** — `AbilityActivateEvent` etc. can be observed by quests, stats, and other systems
- **`AbilityHolder` is entity-agnostic** — the existing holder hierarchy supports non-player entities by design (see CLAUDE.md)
- **McRPG is required** — all four combat abilities are purely `mcrpg_ability` driven. If McRPG is removed or the abilities aren't registered, all skills become no-ops (MM logs unknown mechanic) and the mob is melee-only. This is acceptable — the mob is an McRPG feature and is designed to require McRPG.

### 2.7 Despawn Owned by MythicMobs

Per LLD-2's design decision, McRPG does not schedule despawn timers. The mob YAML includes MM-native `~onTimer` and `~onCombat` triggers that handle max lifetime and combat dropout despawn. Server owners configure these values directly in the mob YAML.

---

## 3. Mob Statistics & Traits

The Riptide Guardian is a mid-difficulty combat encounter designed for solo or small-group fishing players. Stats are balanced for a player in iron-to-diamond gear.

### Base Statistics

| Stat | Value | Notes |
|------|------:|-------|
| Health | 80 (40 hearts) | Enough for a 30-60 second fight |
| Damage | 6 (3 hearts) | Melee hit — punishing but survivable |
| Armor | 8 | Equivalent to ~32% damage reduction |
| Speed | 0.3 | Slightly faster than a player's walk speed (0.2) |
| Follow Range | 24 | Keeps pressure on the player within a reasonable area |
| Knockback Resistance | 0.4 | Partially resists knockback — can't be easily juggled |
| Attack Speed | 1.0 | Default attack cooldown |

### MythicMobs Configuration

```yaml
RiptideGuardian:
  Type: DROWNED
  Display: "&3Riptide Guardian"
  Health: 80
  Damage: 6
  Armor: 8
  Options:
    MovementSpeed: 0.3
    FollowRange: 24
    KnockbackResistance: 0.4
    PreventOtherDrops: true
    PreventRandomEquipment: true
    PreventSunburn: true
    Silent: false
  Faction: mcrpg_fishing_mob
  ThreatTable: true
```

### Stat Rationale

- **Type: DROWNED** — Thematically appropriate (aquatic undead), swims in water, has a trident attack animation natively.
- **Health: 80** — Low enough for solo play with iron gear (~45s fight), high enough to feel threatening. Diamond-geared players finish in ~25s.
- **Damage: 6** — Three-hearts melee matches a drowned's trident throw. Combined with ability damage, total DPS is ~4-5 hearts/sec during burst windows.
- **Armor: 8** — Keeps the fight from being trivialized by sharpness swords while not being a damage sponge.
- **PreventOtherDrops: true** — Only the custom drop table fires. No vanilla drowned loot (tridents, nautilus shells) to confuse the economy.
- **ThreatTable: true** — Enables MM's aggro system. The spawning player is seeded as initial target (see LLD-2 spawn flow).
- **Faction: mcrpg_fishing_mob** — Prevents infighting with other McRPG mobs if multiple are ever spawned.

---

## 4. Mob Abilities (MythicMobs Skills)

All four abilities from the HLD are present in the mob's combat kit. All four are implemented via the `mcrpg_ability` custom mechanic — McRPG owns all execution (VFX, damage, effects, events) while MM owns AI/targeting/cooldowns. This keeps the entire combat system internal to McRPG, making balance changes, event tracking, and future extensibility consistent across all abilities. The mob requires McRPG to function with its full combat kit.

### Custom Mechanic: `mcrpg_ability`

**Class:** `us.eunoians.mcrpg.external.mythicmobs.McRPGAbilityMechanic`

Registered via `MythicMechanicLoadEvent` in `MythicMobsListener`. Implements `ITargetedEntitySkill` — receives the caster (mob) and target from MM, looks up the mob's tracked `AbilityHolder` (created at spawn time), lazily registers the ability with its configured tier, and fires a `MobAbilityTriggerEvent` through `Bukkit.getPluginManager().callEvent()`.

**Syntax:** `mcrpg_ability{ability=<key>}` or `mcrpg_ability{ability=<key>;tier=<n>}` (tier defaults to 1)

**AbilityHolder Lifecycle:**

Each MythicMob gets an `AbilityHolder` tracked in `EntityManager`:
- **Spawn:** `MythicMobSpawnEvent` → create empty holder, track in `EntityManager`
- **First ability fire:** `McRPGAbilityMechanic` lazily registers the ability + `AbilityData(tier)` on the holder
- **Death:** `MythicMobDeathEvent` → remove holder from `EntityManager`
- **Despawn:** `MythicMobDespawnEvent` → remove holder from `EntityManager`

Abilities are lazily populated (not parsed from MM's skill tree at spawn time) because MM nests skills behind `skill:` references, making recursive parsing fragile. The lazy approach is functionally equivalent — abilities are inferred from the MM config since they only get registered when MM fires them.

**Event:** `us.eunoians.mcrpg.event.ability.MobAbilityTriggerEvent`

A Bukkit event (extends `AbilityActivateEvent`) that carries the caster `LivingEntity` and target `LivingEntity`. The mechanic fires this event through Bukkit's event system. A separate listener (`OnMobAbilityTriggerListener`) handles the event and calls `ability.activateAbility(abilityHolder, event)` — the same activation method used by all abilities. This keeps the MM integration decoupled from McRPG's activation logic.

**Listener:** `us.eunoians.mcrpg.listener.ability.OnMobAbilityTriggerListener`

A Bukkit listener registered conditionally when MythicMobs is present. Listens for `MobAbilityTriggerEvent` and directly calls `activateAbility()` on the specific ability from the event. Component checks are intentionally bypassed — MythicMobs has already decided the ability should fire.

This means:
- **No special mob API** — abilities use the same `activateAbility(AbilityHolder, Event)` contract for both player and mob execution
- **Decoupled design** — the mechanic fires an event, a listener handles activation, following McRPG's standard event-driven pattern
- **Tracked holder** — mob holders are tracked in `EntityManager` from spawn, cleaned up on death/despawn
- **Tier support** — abilities carry per-mob tier via `AbilityTierAttribute` in `AbilityData` (defaults to 1)
- **`AbilityHolder` is entity-agnostic** (see CLAUDE.md) — no player assumptions
- **MM cooldowns prevent double-firing** — McRPG does not manage cooldowns for mob abilities
- **McRPG events fire** — `MobAbilityTriggerEvent` extends `AbilityActivateEvent`, so listeners observing ability activations see mob activations too
- If the ability isn't registered (e.g., LLD-6 hasn't been implemented yet), the mechanic returns `CONDITION_FAILED` and the skill is a no-op

**Mechanic registration** (added to `MythicMobsListener`):

```java
@EventHandler
public void onMythicMechanicLoad(@NotNull MythicMechanicLoadEvent event) {
    if (event.getMechanicName().equalsIgnoreCase("mcrpg_ability")) {
        event.register(new McRPGAbilityMechanic(event.getConfig()));
    }
}
```

### 4.1 Phase Shift (mcrpg_ability)

**Purpose:** Anti-cheese teleport that punishes pillar-up, wall-in, and range-kiting tactics.

**Trigger:** Target is >8 blocks away OR out of line-of-sight for 3+ seconds.

**MythicMobs Skill:**

```yaml
PhaseShift:
  Skills:
  - mcrpg_ability{ability=mcrpg:phase_shift} @target
  Cooldown: 8
  Conditions:
  - distance{d=>8} @target
  TargetConditions:
  - lineofsight false
```

**Notes:**
- Purely driven by McRPG's ability system — all VFX, teleport logic, damage, and events are handled inside `Ability.activateAbility()`
- The `distance` and `lineofsight` conditions are OR'd — MM evaluates the skill tree and fires if either condition triggers
- If the ability isn't registered in McRPG (e.g., LLD-6 not yet implemented), the mechanic returns `CONDITION_FAILED` and the skill is a no-op. The mob loses this ability until the McRPG ability is implemented — this is intentional, not a fallback scenario.

### 4.2 Whirlpool (mcrpg_ability)

**Purpose:** AoE zone that forces movement and prevents face-tanking.

**Trigger:** Target is within 5 blocks (close-range engagement).

**MythicMobs Skill:**

```yaml
Whirlpool:
  Skills:
  - mcrpg_ability{ability=mcrpg:whirlpool} @target
  Cooldown: 12
  Conditions:
  - distance{d=<5} @target
```

**Notes:**
- Purely driven by McRPG's ability system — AoE zone placement, damage ticks, VFX, slowness, and events are all handled inside `Ability.activateAbility()`
- McRPG controls all tunable values: damage per tick, radius, duration, slowness level
- Same no-op behavior as Phase Shift if the ability isn't registered yet

### 4.3 Waterlogged Strike (mcrpg_ability)

**Purpose:** Ranged projectile that punishes bow kiting at mid-range.

**Trigger:** Target is 5-15 blocks away.

**MythicMobs Skill:**

```yaml
WaterloggedStrike:
  Skills:
  - mcrpg_ability{ability=mcrpg:waterlogged_strike} @target
  Cooldown: 6
  Conditions:
  - distance{d=5to15} @target
```

**Notes:**
- Purely driven by McRPG — projectile spawning, trail VFX, hit damage (6 / 3 hearts), slowness, and impact effects are all handled inside `Ability.activateAbility()`
- McRPG controls projectile speed, max range, damage, and slowness duration
- Same no-op behavior as Phase Shift if the ability isn't registered yet

### 4.4 Tsunami Wall (mcrpg_ability)

**Purpose:** Blocks retreat when the mob is wounded, forcing continued engagement.

**Trigger:** Mob HP below 50%.

**MythicMobs Skill:**

```yaml
TsunamiWall:
  Skills:
  - mcrpg_ability{ability=mcrpg:tsunami_wall} @target
  Cooldown: 15
  Conditions:
  - health{h=<50%}
  TargetConditions:
  - distance{d=<10} @target
```

**Notes:**
- Purely driven by McRPG — wall placement, particle VFX, slowness, knockback, and duration are all handled inside `Ability.activateAbility()`
- Only triggers below 50% HP and when target is within 10 blocks (no wall if target already far away)
- Same no-op behavior as Phase Shift if the ability isn't registered yet

### AI Priority (Skill List Order)

MythicMobs evaluates skills top-to-bottom. The first skill whose conditions are met fires (respecting cooldowns). The ordering implements the HLD's priority:

```yaml
Skills:
- skill:PhaseShift        # Priority 1: unreachable target
- skill:WaterloggedStrike # Priority 2: mid-range target
- skill:Whirlpool         # Priority 3: close-range target
- skill:TsunamiWall       # Priority 4: low HP defense
```

All four skills use `mcrpg_ability`. Melee attacks are handled by MM's default AI — when no skills fire, the mob melees.

**Degraded mode:** If McRPG abilities aren't registered (pre-LLD-6), all four skills are no-ops and the mob is melee-only. This is acceptable — the mob is an McRPG feature and its full combat kit requires McRPG.

---

## 5. Drop Table

The Riptide Guardian uses MythicMobs' native DropTable system with the `mcrpg_skillbook` custom drop type (LLD-1) and the `mcrpg_ability_unlocked` custom condition (this LLD).

### DropTable Architecture

Because MythicMobs DropTables support `TriggerConditions` at the table level but **not** on individual drop lines, each skill book requires two DropTable entries — one for players who haven't unlocked the ability (full rate) and one for players who have (reduced rate).

The mob's `Drops:` list references all four DropTables. MM evaluates each table's `TriggerConditions` against the killer; only the matching table fires.

### DropTable Configuration

```yaml
# ─── Drop Tables (placed in DropTables/ or same file) ──────

WhirlpoolBookDrop:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:whirlpool} false
  Drops:
  - mcrpg_skillbook{ability=mcrpg:whirlpool} 1 0.12

WhirlpoolBookDropReduced:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:whirlpool} true
  Drops:
  - mcrpg_skillbook{ability=mcrpg:whirlpool} 1 0.02

PhaseShiftBookDrop:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:phase_shift} false
  Drops:
  - mcrpg_skillbook{ability=mcrpg:phase_shift} 1 0.05

PhaseShiftBookDropReduced:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:phase_shift} true
  Drops:
  - mcrpg_skillbook{ability=mcrpg:phase_shift} 1 0.01
```

The mob references these tables:
```yaml
RiptideGuardian:
  Drops:
  - WhirlpoolBookDrop
  - WhirlpoolBookDropReduced
  - PhaseShiftBookDrop
  - PhaseShiftBookDropReduced
```

### Drop Rate Summary

| Skill Book | Not Unlocked | Already Unlocked |
|------------|:---:|:---:|
| Whirlpool | 12% | 2% |
| Phase Shift | 5% | 1% |

### Custom Condition: `mcrpg_ability_unlocked`

**Class:** `us.eunoians.mcrpg.external.mythicmobs.McRPGAbilityUnlockedCondition`

Registered via `MythicConditionLoadEvent` in `MythicMobsListener`. Implements `IEntityCondition` — checks whether the entity (the killer) has the specified ability unlocked in McRPG.

```java
package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGManagerKey;

import java.util.Optional;

/**
 * A custom MythicMobs condition that checks whether a player has unlocked
 * the specified McRPG ability.
 * <p>
 * Registered as {@code mcrpg_ability_unlocked} in MythicMobs. Used in
 * DropTable {@code TriggerConditions} to vary drop rates based on unlock state.
 * <p>
 * Configuration:
 * <pre>
 *   TriggerConditions:
 *   - mcrpg_ability_unlocked{ability=mcrpg:whirlpool} true
 * </pre>
 */
public class McRPGAbilityUnlockedCondition implements IEntityCondition {

    private final NamespacedKey abilityKey;

    public McRPGAbilityUnlockedCondition(@NotNull MythicLineConfig config) {
        String keyString = config.getString("ability", "");
        this.abilityKey = NamespacedKey.fromString(keyString);
    }

    @Override
    public boolean check(@NotNull AbstractEntity abstractEntity) {
        if (abilityKey == null) {
            return false;
        }
        if (!abstractEntity.isPlayer()) {
            return false;
        }
        Player player = (Player) abstractEntity.getBukkitEntity();
        Optional<McRPGPlayer> mcRPGPlayerOpt = McRPG.getInstance().registryAccess()
                .registry(us.eunoians.mcrpg.registry.RegistryKey.MANAGER)
                .manager(McRPGManagerKey.ENTITY)
                .getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        return mcRPGPlayer.asSkillHolder().getAbilityData(abilityKey)
                .flatMap(data -> data.getAbilityAttribute(
                        AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE))
                .filter(attr -> attr instanceof AbilityUnlockedAttribute)
                .map(attr -> ((AbilityUnlockedAttribute) attr).getContent())
                .orElse(false);
    }
}
```

### Condition Registration

Added to `MythicMobsListener`:

```java
@EventHandler
public void onMythicConditionLoad(@NotNull MythicConditionLoadEvent event) {
    if (event.getConditionName().equalsIgnoreCase("mcrpg_ability_unlocked")) {
        event.register(new McRPGAbilityUnlockedCondition(event.getConfig()));
    }
}
```

### Drop Flow

1. Riptide Guardian dies → MM evaluates each referenced DropTable
2. For each table, MM checks `TriggerConditions` against the killing player
3. `mcrpg_ability_unlocked` queries the player's `AbilityUnlockedAttribute` via McRPG
4. Only the matching table (unlocked or not-unlocked) fires its drop entries
5. MM rolls the chance; on success, calls `McRPGSkillBookDrop.getDrop()` (LLD-1/3)
6. `McRPGSkillBookDrop` delegates to `SkillBookFactory.createSkillBook()` (LLD-3)
7. The factory creates an `ENCHANTED_BOOK` ItemStack with PDC tags and localized display text
8. MM drops the item at the mob's death location

### Why No Vanilla Drops

`PreventOtherDrops: true` in the mob options ensures only the custom drop table fires. This prevents:
- Trident drops from drowned (economy disruption)
- Nautilus shells or other vanilla drowned loot
- XP orb duplication (MM controls XP separately if desired)

Server owners who want vanilla drops can set `PreventOtherDrops: false` in their copy.

---

## 6. Despawn Configuration

Per LLD-2's design decision, MythicMobs owns all despawn behavior. The mob YAML includes MM-native triggers for two despawn scenarios.

### Max Lifetime Despawn

```yaml
  Skills:
  - ...
  - skill:DespawnSelf ~onTimer:6000
```

The mob auto-despawns after 300 seconds (6000 ticks / 5 minutes). This prevents:
- Players luring the mob to a storage location
- Orphaned mobs from disconnected players persisting indefinitely
- Server entity count bloat

### Combat Dropout Despawn

```yaml
  Skills:
  - ...
  - skill:DespawnSelf ~onCombat:600
```

If the mob's ThreatTable has been empty for 30 seconds (600 ticks), the mob despawns. This handles:
- Player death (respawn elsewhere)
- Player logout
- Player running far enough away that MM drops combat

### Despawn Skill

```yaml
DespawnSelf:
  Skills:
  - effect:particles{particle=WATER_SPLASH;amount=50;speed=0.5;ySpread=1.0;xSpread=1.0;zSpread=1.0} @self
  - sound{s=entity.elder_guardian.ambient;v=1.0;p=0.4} @self
  - remove @self
```

A shared skill used by both triggers. Plays a water-burst VFX and sound before removing the entity, giving nearby players visual feedback that the mob is gone (not just silently vanishing).

---

## 7. Full Pack Files

The Riptide Guardian configuration ships as a MythicMobs pack with three files. Each file below is the complete, ready-to-use version bundled in the McRPG JAR.

### 7.1 Mobs/RiptideGuardian.yml

```yaml
#
# Riptide Guardian — McRPG Fishing Mob
#
# This file is auto-extracted by McRPG on first startup.
# Customize freely — McRPG will not overwrite your changes.
#
# Requires: MythicMobs 5.7+, McRPG (for mcrpg_skillbook drop type,
# mcrpg_ability mechanic, and mcrpg_ability_unlocked condition)
#
# If McRPG is removed, the mob still spawns and melees but
# mcrpg_ability skills become no-ops and skill book drops
# silently fail.
#

RiptideGuardian:
  Type: DROWNED
  Display: "&3Riptide Guardian"
  Health: 80
  Damage: 6
  Armor: 8
  Options:
    MovementSpeed: 0.3
    FollowRange: 24
    KnockbackResistance: 0.4
    PreventOtherDrops: true
    PreventRandomEquipment: true
    PreventSunburn: true
    Silent: false
  Faction: mcrpg_fishing_mob
  ThreatTable: true
  Drops:
  - WhirlpoolBookDrop
  - WhirlpoolBookDropReduced
  - PhaseShiftBookDrop
  - PhaseShiftBookDropReduced
  Skills:
  - skill:PhaseShift
  - skill:WaterloggedStrike
  - skill:Whirlpool
  - skill:TsunamiWall
  - skill:DespawnSelf ~onTimer:6000
  - skill:DespawnSelf ~onCombat:600
```

### 7.2 Skills/RiptideGuardianSkills.yml

```yaml
#
# Riptide Guardian Skills — McRPG Fishing Mob
#
# All four combat abilities use the mcrpg_ability mechanic to
# delegate execution to McRPG's ability system. McRPG owns all
# VFX, damage, effects, and events. MM owns AI (when to fire,
# cooldowns, conditions, targeting).
#
# If McRPG is removed, all skills become no-ops and the mob
# is melee-only.
#

# ─── Phase Shift ────────────────────────────────────────────

PhaseShift:
  Skills:
  - mcrpg_ability{ability=mcrpg:phase_shift} @target
  Cooldown: 8
  Conditions:
  - distance{d=>8} @target
  TargetConditions:
  - lineofsight false

# ─── Waterlogged Strike ────────────────────────────────────

WaterloggedStrike:
  Skills:
  - mcrpg_ability{ability=mcrpg:waterlogged_strike} @target
  Cooldown: 6
  Conditions:
  - distance{d=5to15} @target

# ─── Whirlpool ─────────────────────────────────────────────

Whirlpool:
  Skills:
  - mcrpg_ability{ability=mcrpg:whirlpool} @target
  Cooldown: 12
  Conditions:
  - distance{d=<5} @target

# ─── Tsunami Wall ──────────────────────────────────────────

TsunamiWall:
  Skills:
  - mcrpg_ability{ability=mcrpg:tsunami_wall} @target
  Cooldown: 15
  Conditions:
  - health{h=<50%}
  TargetConditions:
  - distance{d=<10} @target

# ─── Despawn ────────────────────────────────────────────────

DespawnSelf:
  Skills:
  - effect:particles{particle=WATER_SPLASH;amount=50;speed=0.5;ySpread=1.0;xSpread=1.0;zSpread=1.0} @self
  - sound{s=entity.elder_guardian.ambient;v=1.0;p=0.4} @self
  - remove @self
```

### 7.3 DropTables/RiptideGuardianDrops.yml

```yaml
#
# Riptide Guardian Drop Tables — McRPG Fishing Mob
#
# Each skill book has two tables: full rate (ability not unlocked)
# and reduced rate (ability already unlocked). The
# mcrpg_ability_unlocked condition checks the killer's McRPG
# unlock state.
#
# If McRPG is removed, mcrpg_skillbook and mcrpg_ability_unlocked
# are unknown to MM — drop tables silently produce no items.
#

WhirlpoolBookDrop:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:whirlpool} false
  Drops:
  - mcrpg_skillbook{ability=mcrpg:whirlpool} 1 0.12

WhirlpoolBookDropReduced:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:whirlpool} true
  Drops:
  - mcrpg_skillbook{ability=mcrpg:whirlpool} 1 0.02

PhaseShiftBookDrop:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:phase_shift} false
  Drops:
  - mcrpg_skillbook{ability=mcrpg:phase_shift} 1 0.05

PhaseShiftBookDropReduced:
  TriggerConditions:
  - mcrpg_ability_unlocked{ability=mcrpg:phase_shift} true
  Drops:
  - mcrpg_skillbook{ability=mcrpg:phase_shift} 1 0.01
```

---

## 8. Resource Extraction

McRPG bundles the pack files inside the JAR under `mythicmobs/Packs/McRPG/`. On startup, when MythicMobs is present, the entire pack directory structure is extracted to `plugins/MythicMobs/Packs/McRPG/`.

### Extraction Logic

**Class:** `us.eunoians.mcrpg.external.mythicmobs.MythicMobsConfigExtractor`

```java
package us.eunoians.mcrpg.external.mythicmobs;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

/**
 * Extracts bundled MythicMobs pack files from the McRPG JAR to the
 * MythicMobs Packs directory on first startup.
 * <p>
 * Files are only extracted if they do not already exist in the target
 * directory, preserving any server-owner customizations. The pack is
 * deployed to {@code plugins/MythicMobs/Packs/McRPG/}.
 */
public class MythicMobsConfigExtractor {

    /**
     * Bundled pack files (paths relative to the pack root).
     * Add new entries here when additional mobs or configs are shipped.
     */
    private static final List<String> BUNDLED_PACK_FILES = List.of(
            "Mobs/RiptideGuardian.yml",
            "Skills/RiptideGuardianSkills.yml",
            "DropTables/RiptideGuardianDrops.yml"
    );

    private static final String JAR_RESOURCE_PREFIX = "mythicmobs/Packs/McRPG/";

    private MythicMobsConfigExtractor() {
    }

    /**
     * Extracts all bundled pack files to the MythicMobs
     * {@code Packs/McRPG/} directory. Skips any file that already
     * exists on disk.
     *
     * @param plugin the McRPG plugin instance
     */
    public static void extractBundledConfigs(@NotNull McRPG plugin) {
        Path packRoot = plugin.getDataFolder().toPath()
                .getParent()  // plugins/
                .resolve("MythicMobs")
                .resolve("Packs")
                .resolve("McRPG");

        for (String relativePath : BUNDLED_PACK_FILES) {
            Path targetFile = packRoot.resolve(relativePath);

            if (Files.exists(targetFile)) {
                plugin.getLogger().info("MythicMobs pack file '"
                        + relativePath + "' already exists, skipping extraction.");
                continue;
            }

            // Ensure parent directories exist (e.g., Mobs/, Skills/, DropTables/)
            try {
                Files.createDirectories(targetFile.getParent());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Could not create directory for MythicMobs pack file '"
                                + relativePath + "'", e);
                continue;
            }

            String resourcePath = JAR_RESOURCE_PREFIX + relativePath;
            try (InputStream resourceStream = plugin.getResource(resourcePath)) {
                if (resourceStream == null) {
                    plugin.getLogger().warning("Bundled MythicMobs pack file '"
                            + resourcePath + "' not found in JAR.");
                    continue;
                }
                Files.copy(resourceStream, targetFile);
                plugin.getLogger().info("Extracted MythicMobs pack file '"
                        + relativePath + "' to " + targetFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to extract MythicMobs pack file '"
                                + relativePath + "'", e);
            }
        }
    }
}
```

### Why Not `plugin.saveResource()`?

Bukkit's `saveResource()` writes to the plugin's own data folder (`plugins/McRPG/`). We need to write to `plugins/MythicMobs/Packs/McRPG/`, which is a sibling plugin's directory. Direct `Files.copy()` from an `InputStream` is the simplest approach.

### JAR Resource Layout

The pack files are placed at:
```
src/main/resources/mythicmobs/Packs/McRPG/
  Mobs/RiptideGuardian.yml
  Skills/RiptideGuardianSkills.yml
  DropTables/RiptideGuardianDrops.yml
```

Gradle packages them into the JAR preserving the directory structure, accessible via `plugin.getResource("mythicmobs/Packs/McRPG/Mobs/RiptideGuardian.yml")` etc.

---

## 9. Bootstrap Registration

The extraction runs once during plugin enable, after hooks are registered (so we know MythicMobs is present).

### Integration Point

**File:** `us.eunoians.mcrpg.bootstrap.McRPGListenerRegistrar` (or equivalent bootstrap class)

The extraction is called conditionally, only when the MythicMobs hook is active:

```java
// In the bootstrap sequence, after hooks are registered:
if (mcRPG.registryAccess()
        .registry(RegistryKey.MANAGER)
        .manager(McRPGManagerKey.HOOK)
        .isHookActive(McRPGPluginHookKey.MYTHICMOBS)) {
    MythicMobsConfigExtractor.extractBundledConfigs(mcRPG);
}
```

### Timing

Extraction runs during `onEnable()`, before MythicMobs loads its mob configs. The Paper/Spigot plugin load order ensures this works because:
1. McRPG declares MythicMobs as a `softdepend` in `plugin.yml` / `paper-plugin.yml`
2. This means McRPG loads **after** MythicMobs
3. However, MythicMobs reloads its configs on a delayed task after all plugins enable
4. By extracting during `onEnable()`, the file is in place before MM's reload pass picks it up

If MM has already loaded by the time McRPG enables (edge case with async loading), the mob will be available after the next `/mm reload` or server restart. This is acceptable for a first-run scenario.

---

## 10. Server Owner Customization Guide

This section documents the tunable values server owners are most likely to modify.

### Stat Tuning

| What to Change | Where | Default | Notes |
|----------------|-------|---------|-------|
| Mob HP | `Health:` | 80 | Scale with your server's average gear level |
| Melee damage | `Damage:` | 6 | Halve for casual servers, double for hardcore |
| Movement speed | `MovementSpeed:` | 0.3 | Player walk is 0.2, sprint is 0.26 |
| Armor | `Armor:` | 8 | 0 = no damage reduction, 30 = nearly invulnerable |

### Drop Rate Tuning

| What to Change | Where | Default | Notes |
|----------------|-------|---------|-------|
| Whirlpool book rate (not unlocked) | `WhirlpoolBookDrop:` → `Drops:` chance | 0.12 | 12% per kill |
| Whirlpool book rate (already unlocked) | `WhirlpoolBookDropReduced:` → `Drops:` chance | 0.02 | 2% per kill |
| Phase Shift book rate (not unlocked) | `PhaseShiftBookDrop:` → `Drops:` chance | 0.05 | 5% per kill |
| Phase Shift book rate (already unlocked) | `PhaseShiftBookDropReduced:` → `Drops:` chance | 0.01 | 1% per kill |
| Disable unlock-aware reduction | Remove the `*Reduced` DropTable references from mob `Drops:` and remove `TriggerConditions` from the remaining tables | — | All players get the same rate |
| Add vanilla drops | Set `PreventOtherDrops: false` | true | Enables vanilla drowned drops alongside skill books |

### Ability Tuning

| What to Change | Where | Default | Notes |
|----------------|-------|---------|-------|
| Phase Shift cooldown | `PhaseShift:` → `Cooldown:` | 8 | Seconds between teleports |
| Whirlpool damage/tick | `WhirlpoolTick:` → `damage{a=...}` | 3 (1.5 hearts) | Raw damage per tick |
| Whirlpool radius | `WhirlpoolTick:` → `@PlayersInRadius{r=...}` | 4 | Blocks |
| Waterlogged Strike damage | `WaterloggedStrikeHit:` → `damage{a=...}` | 6 (3 hearts) | Raw damage |
| Tsunami Wall HP threshold | `TsunamiWall:` → `health{h=<...%}` | 50% | Lower = later activation |

### Despawn Tuning

| What to Change | Where | Default | Notes |
|----------------|-------|---------|-------|
| Max lifetime | `~onTimer:` value | 6000 (5 min) | In ticks (20 ticks = 1 second) |
| Combat dropout | `~onCombat:` value | 600 (30 sec) | Ticks after threat table empties |

### Adding Custom Drops

Server owners can add any MythicMobs-compatible drop alongside skill books:

```yaml
Drops:
- mcrpg_skillbook{ability=mcrpg:whirlpool} 1 0.12
- mcrpg_skillbook{ability=mcrpg:phase_shift} 1 0.05
- DIAMOND 1-3 0.25          # 25% chance for 1-3 diamonds
- EXPERIENCE_BOTTLE 2-5 1.0 # Guaranteed XP bottles
```

---

## 11. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|----------|----------|
| MythicMobs not installed | Extraction is skipped (hook check). Mob pool references `RiptideGuardian` but `MythicMobsHook.spawnMob()` returns empty. No crash. |
| McRPG removed after extraction | Pack files remain on disk. Mob spawns but is melee-only — all `mcrpg_ability` mechanics log "unknown mechanic" and are no-ops. `mcrpg_skillbook` drops silently fail. |
| LLD-6 not yet implemented | `mcrpg_ability` returns `CONDITION_FAILED` for all four abilities (not in registry). Mob is melee-only until McRPG abilities are registered. |
| Server owner deletes a pack file | Mob stops spawning (MM can't find type ID). Next McRPG restart re-extracts the missing file. |
| Server owner modifies pack files | McRPG never overwrites existing files. Modifications are preserved across restarts and updates. |
| MythicMobs `Packs/McRPG/` directory doesn't exist | Extractor creates it and subdirectories via `Files.createDirectories()`. |
| Multiple McRPG instances (BungeeCord) | Each server instance extracts independently. No cross-server conflict. |
| `plugin.getResource()` returns null | Logged as warning, extraction skipped for that file. Other files still extracted. |
| MM loads before McRPG enables | File won't be present for MM's initial load. Available after `/mm reload` or server restart. Acceptable for first-run only. |

---

## 12. Test Plan

### Unit Tests

| Test | Class | Assertion |
|------|-------|-----------|
| Extractor skips existing file | `MythicMobsConfigExtractorTest` | When target file exists, `Files.copy()` is not called |
| Extractor creates missing pack directories | `MythicMobsConfigExtractorTest` | When `Packs/McRPG/Mobs/` etc. don't exist, they are created |
| Extractor copies all pack files | `MythicMobsConfigExtractorTest` | All 3 extracted files match JAR resources byte-for-byte |
| Extractor handles missing resource | `MythicMobsConfigExtractorTest` | When JAR resource is null, logs warning and continues |
| Extractor handles IO failure | `MythicMobsConfigExtractorTest` | When `Files.copy()` throws, logs warning and continues to next file |

### Manual Validation

| Scenario | Steps | Expected |
|----------|-------|----------|
| First-run extraction | Install McRPG + MM, start server | `plugins/MythicMobs/Packs/McRPG/` directory appears with all 3 files |
| Mob spawns via fishing | Fish until spawn triggers | Riptide Guardian (drowned) spawns near hook |
| Phase Shift fires | Pillar up >8 blocks | Mob teleports behind player with portal particles |
| Whirlpool fires | Stand within 5 blocks | AoE zone appears, deals damage + slowness |
| Waterlogged Strike fires | Stand 5-15 blocks away | Projectile with water trail, impact damage + slowness |
| Tsunami Wall fires | Get mob below 50% HP | Particle wall spawns between mob and player |
| Skill book drops | Kill mob multiple times | Whirlpool book (~12%) and Phase Shift book (~5%) drop |
| Skill book consumption | Right-click dropped book | Ability unlocked (if not already), book consumed |
| Despawn on timeout | Spawn mob, walk away, wait 5 min | Mob despawns with water-burst VFX |
| Despawn on combat drop | Spawn mob, die, wait 30s | Mob despawns with water-burst VFX |
| No overwrite on restart | Modify YAML, restart server | Modifications preserved |

---

## 13. File Manifest

| File | Action | Description |
|------|--------|-------------|
| `src/main/resources/mythicmobs/Packs/McRPG/Mobs/RiptideGuardian.yml` | **NEW** | Mob definition (type, stats, options, skill/drop refs) |
| `src/main/resources/mythicmobs/Packs/McRPG/Skills/RiptideGuardianSkills.yml` | **NEW** | All skills: Phase Shift, Whirlpool (with fallbacks), Waterlogged Strike, Tsunami Wall, DespawnSelf |
| `src/main/resources/mythicmobs/Packs/McRPG/DropTables/RiptideGuardianDrops.yml` | **NEW** | Unlock-aware skill book drop tables (4 tables for 2 abilities) |
| `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsConfigExtractor.java` | **NEW** | Extracts bundled pack files to `plugins/MythicMobs/Packs/McRPG/` |
| `src/main/java/us/eunoians/mcrpg/external/mythicmobs/McRPGAbilityMechanic.java` | **NEW** | Custom MM mechanic: fires `MobAbilityTriggerEvent` through Bukkit event system |
| `src/main/java/us/eunoians/mcrpg/event/ability/MobAbilityTriggerEvent.java` | **NEW** | Bukkit event carrying caster + target for mob-triggered ability activation |
| `src/main/java/us/eunoians/mcrpg/listener/ability/OnMobAbilityTriggerListener.java` | **NEW** | Listener for `MobAbilityTriggerEvent`: calls `activateAbility()` on the event's ability |
| `src/main/java/us/eunoians/mcrpg/external/mythicmobs/McRPGAbilityUnlockedCondition.java` | **NEW** | Custom MM condition: checks player's ability unlock state |
| `src/main/java/us/eunoians/mcrpg/external/mythicmobs/MythicMobsListener.java` | **MODIFY** | Add mechanic/condition load handlers, AbilityHolder spawn/death/despawn lifecycle |
| `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGListenerRegistrar.java` | **MODIFY** | Add extraction call, register `OnMobAbilityTriggerListener` in MM conditional block |

---

## 14. Future LLD Notes

### LLD-5 (UnlockCondition Refactor)

Will define a new `UnlockCondition` interface that replaces the current `UnlockableAbility.getUnlockLevel()` mechanism. Skill book consumption (LLD-3) will become one implementation of `UnlockCondition`, alongside level-based unlocking and potential future conditions (quest completion, achievement, etc.).

### LLD-6 (Player Abilities)

Will define the player-side implementations of Phase Shift and Whirlpool as McRPG abilities. These are the abilities unlocked by the skill books dropped by this mob. Key interactions with this LLD:

- **`MobAbilityTriggerEvent` handling:** LLD-6 ability implementations receive `MobAbilityTriggerEvent` as the `Event` parameter in `activateAbility()`. The event carries the caster and target `LivingEntity` references. Abilities should check `event instanceof MobAbilityTriggerEvent` to extract caster/target context for mob-triggered execution. Component checks are bypassed for mob abilities — `OnMobAbilityTriggerListener` calls `activateAbility()` directly.
- **Event listener audit:** `AbilityActivateEvent` listeners and downstream event handlers may currently assume player-only context. LLD-6 should audit event fields and listeners to ensure they handle non-player `AbilityHolder` instances (the `AbilityHolder` base class is already entity-agnostic).
- **Ability activation:** Once LLD-6 registers all four abilities in the `AbilityRegistry`, the `mcrpg_ability` mechanic will find them and the mob's full combat kit becomes active. Until then, the mob is melee-only.

### Future: Additional Mobs

The pack structure and extraction system (`BUNDLED_PACK_FILES` list) are designed for multiple mobs. Future mobs (e.g., Cavern Golem for mining, Timber Wraith for woodcutting) can be added by:
1. Creating new YAML files in the appropriate `src/main/resources/mythicmobs/Packs/McRPG/` subdirectories
2. Adding the relative paths to `MythicMobsConfigExtractor.BUNDLED_PACK_FILES`
3. Adding the mob to the relevant skill's mob pool config (e.g., `fishing_mob_spawn_configuration.yml`)

### Future: ModelEngine Integration

MythicMobs natively supports ModelEngine for custom mob models. Server owners can add `Model:` configuration to the mob YAML without any McRPG changes. A future pass could bundle a default model ID reference.
