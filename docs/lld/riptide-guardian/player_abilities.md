# Low-Level Design: Player Abilities (LLD-6)

**Status:** Draft
**Date:** 2026-05-31
**Last Updated:** 2026-05-31
**HLD Reference:** [Riptide Guardian HLD](../../hld/riptide-guardian/riptide_guardian.md), Section 6
**Scope:** Four standalone, non-tiered, combo-activated abilities (Phase Shift, Whirlpool, Waterlogged Strike, Tsunami Wall), shared config file, custom events, localization, bootstrap registration

---

## Table of Contents

1. [Overview](#1-overview)
2. [Existing Infrastructure](#2-existing-infrastructure)
3. [Design Decisions](#3-design-decisions)
4. [Configuration](#4-configuration)
5. [Phase Shift](#5-phase-shift)
6. [Whirlpool](#6-whirlpool)
7. [Waterlogged Strike](#7-waterlogged-strike)
8. [Tsunami Wall](#8-tsunami-wall)
9. [Custom Events](#9-custom-events)
10. [Custom Statistics](#10-custom-statistics)
11. [Unlock Conditions](#11-unlock-conditions)
12. [Localization Keys](#12-localization-keys)
13. [Bootstrap Registration](#13-bootstrap-registration)
14. [Edge Cases & Graceful Degradation](#14-edge-cases--graceful-degradation)
15. [Test Plan](#15-test-plan)
16. [File Manifest](#16-file-manifest)
17. [Future LLD Notes](#17-future-lld-notes)

---

## 1. Overview

The Riptide Guardian drops skill books that teach players four water-themed combat abilities. These abilities are **standalone** (no parent skill), **non-tiered** (flat power, no upgrade path), and **combo-activated** (use the standard combo system with mana costs). They work with any held item and function in both PvE and PvP.

**This LLD produces code.** All classes, configs, and tests described here are implementation-ready.

### Ability Summary

| Ability | Key | Description | Mana | Cooldown |
|---|---|---|---|---|
| Phase Shift | `mcrpg:phase_shift` | Teleport behind last-attacked target, reset attack timer, guaranteed crit window | 40 | 12s |
| Whirlpool | `mcrpg:whirlpool` | Stationary AoE zone with pull + Slowness | 25 | 12s |
| Waterlogged Strike | `mcrpg:waterlogged_strike` | Ranged water projectile with damage + Slowness II | 15 | 1s |
| Tsunami Wall | `mcrpg:tsunami_wall` | Forward-facing particle wall with knockback + Slowness III | 50 | 15s |

### Mana Cost Rationale

These abilities use custom mana costs that sit below the standard bucket ranges defined in the mana balance framework. The standard buckets (Light 28-32, Medium 42-50, Heavy 70-80) were designed for tiered abilities where T1 costs are high and T5 costs reward progression. Since these abilities are non-tiered, they have no tier scaling — the fixed cost must be playable without the progression reward of cost reduction. The chosen values (15-50) ensure these abilities are usable alongside skill-based abilities without being prohibitively expensive, while still forcing sequencing decisions when players slot multiple guardian abilities.

| Ability | Cost | Reasoning |
|---|---|---|
| Phase Shift | 40 | High-impact repositioning + guaranteed crit. Two uses nearly empties the pool. |
| Whirlpool | 25 | Crowd control utility. Can combo with other abilities without immediately emptying pool. |
| Waterlogged Strike | 15 | Low-commitment poke. Spammable but mana-draining over sustained use (15 casts to empty). |
| Tsunami Wall | 50 | Strongest zone denial. Two walls = full pool. Forces commitment. |

### Boundary with Prior LLDs

- **LLD-1 (MythicMobs Binding):** Mob spawning and death events are already implemented. This LLD does not modify any LLD-1 classes.
- **LLD-2 (Fishing Mob Spawn):** Spawn probability and mob pool are already implemented. This LLD does not modify any LLD-2 classes.
- **LLD-3 (Skill Book System):** `SkillBookFactory`, `SkillBookConsumeListener`, and `SkillBookRewardType` are already implemented. Skill books for these abilities use the existing system. This LLD does not modify any LLD-3 classes.
- **LLD-4 (MythicMobs Example Config):** The bundled `RiptideGuardian.yml` already includes `mcrpg_skillbook` drop entries for these four abilities.
- **LLD-5 (UnlockCondition System):** These abilities use `DisplayHintUnlockConditionType` as their default unlock condition — books bypass conditions and unlock directly via `SkillBookConsumeListener`.

### What this LLD does NOT cover

| Out of scope | Reason |
|---|---|
| Tier progression / upgrade quests | These abilities are non-tiered by design |
| Parent skill (Fishing skill) | Standalone abilities with no skill association |
| Mob abilities (MythicMobs-side) | Already documented in HLD Section 4; owned by MM config |
| Advanced loot / threat-based rewards | Backlog item (HLD Section 2.1) |

---

## 2. Existing Infrastructure

### 2.1 Interfaces used by these abilities

| Interface | Location | Role |
|---|---|---|
| `McRPGAbility` | `ability/` | Base class — component registration, display name, plugin reference |
| `ConfigurableAbility` | `ability/impl/type/configurable/` | YAML config support — `getYamlDocument()`, `getDisplayItemRoute()`, `getAbilityEnabledRoute()` |
| `UnlockableAbility` | `ability/impl/type/` | Skill book unlock — `getDefaultUnlockConditions()`, `isAnyConditionMet()`, `isAbilityUnlocked()` |
| `CooldownableAbility` | `ability/impl/type/` | Cooldown — `getCooldown()`, `isAbilityOnCooldown()`, `putHolderOnCooldown()` |
| `ManaAbility` | `ability/impl/type/` | Mana cost — `getManaCost()` |
| `ActiveAbility` | `ability/impl/type/` | Activation statistic tracking — `getActivationStatisticKey()` |
| `ComboActivatable` | `ability/combo/` | Combo activation — `comboActivate()` |

### 2.2 Interfaces NOT used

| Interface | Reason |
|---|---|
| `TierableAbility` | No tiers |
| `ConfigurableTierableAbility` | No tier-based config routing |
| `ConfigurableActiveAbility` | Provides tier-formula mana/cooldown resolution with tier variable; standalone abilities use Parser directly without tier |
| `ConfigurableSkillAbility` | No parent skill |
| `SkillAbility` | No parent skill |

### 2.3 Classes used as-is

| Class | Location | Role |
|---|---|---|
| `AbilityRegistry` | `ability/` | Registration of all four abilities |
| `AbilityUnlockedAttribute` | `ability/attribute/` | Canonical unlock state |
| `SkillBookFactory` | `item/skillbook/` | Creates skill book items for these abilities |
| `SkillBookConsumeListener` | `listener/item/` | Handles skill book consumption and unlock |
| `OnComboCompleteListener` | `listener/ability/` | Handles mana consumption, cooldown application, refund |
| `DisplayHintUnlockConditionType` | `ability/unlock/builtin/` | Default unlock condition — "Obtain from Riptide Guardian" |
| `McRPGExpansion` | `expansion/` | Registration point for abilities in `getAbilityContent()` |

---

## 3. Design Decisions

### 3.1 Standalone abilities with no parent skill

These abilities implement `ConfigurableAbility` + `UnlockableAbility` + `ComboActivatable` but NOT `SkillAbility` or `TierableAbility`. They have no skill association, no skill-level unlock gate, and no tier progression.

**Rationale.** The Riptide Guardian abilities are unlocked by consuming skill books dropped from a mob, not by leveling a skill. Creating a Fishing skill solely to house these abilities would add an empty progression system with no XP source. The abilities stand on their own — they are combat tools earned through gameplay, not skill mastery.

### 3.2 Parser formulas for mana costs and cooldowns

Mana costs and cooldowns are read as strings from YAML via `getYamlDocument().getString(route)` and evaluated through `new Parser(formula).getValue()`, even though current default values are simple integers (e.g. `"40"`). No `tier` variable or `all-tiers` / `tier-N` config routing — these abilities are non-tiered.

**Rationale.** Using Parser formulas from the start ensures these abilities are forward-compatible with future expansion. If modifiers, scaling, or conditional costs are added later, they can be expressed as formulas in config without touching Java code. Reading a string that happens to be `"40"` and parsing it works identically to `getInt()` in the default case, with zero additional config complexity for server owners.

### 3.3 Single shared config file

All four abilities share one config file (`guardian_abilities_configuration.yml`) with per-ability sections. This groups thematically related abilities and reduces file proliferation.

**Rationale.** Skill-based abilities live in their skill's config file (e.g. `swords_configuration.yml`). These abilities have no skill, so they need their own file. One file for four related abilities is manageable and keeps all guardian ability tuning in one place.

### 3.4 Any held item for combo activation

No held item restriction. Players can activate these abilities while holding any item (sword, fishing rod, trident, pickaxe, etc.).

**Rationale.** These abilities are general combat tools, not tied to a specific weapon type. Restricting to fishing rod would make them unusable in melee combat where they are most relevant (Phase Shift, Whirlpool). Restricting to trident would exclude players who don't have a trident.

### 3.5 PvE + PvP with shared tuning

All four abilities affect both mobs and players using the same damage/duration values. No split PvP/PvE tuning.

**Rationale.** Split tuning doubles the config surface and test matrix for a first release. Server owners can disable specific abilities via `enabled: false` if they cause PvP balance issues. A future pass can add a `pvp-damage-multiplier` config key additively without touching ability code.

### 3.6 Water/aquatic particle theme

All abilities use water-themed particles (WATER_SPLASH, DRIP_WATER, BUBBLE_POP) consistent with the mob versions in the HLD.

**Rationale.** Visual consistency with the Riptide Guardian's own abilities reinforces the thematic connection between the mob and the abilities it teaches.

### 3.7 Per-player combat state tracking

Phase Shift requires knowing the player's last-attacked target and when the attack occurred. This state is **session-only, per-player, not persisted**. It lives on `McRPGPlayer` as an optional transient field, similar to `PlayerFishingState`.

**Rationale.** Combat target tracking is a volatile, high-frequency state change that would be wasteful to persist. It resets on logout, death, or world change — exactly the lifecycle of a session field.

> **Backlog:** Create a general-purpose "combat tracker" system (plugin-wide, not guardian-specific) that tracks last-attacked target, last attacker, recent damage events, etc. Once built, Phase Shift should integrate with it instead of maintaining its own `CombatTargetState`. The current `CombatTargetState` implementation is an acceptable interim solution that can be migrated later.

---

## 4. Configuration

### 4.1 Config File

**New FileType:** `GUARDIAN_ABILITIES_CONFIG` → `guardian_abilities_configuration.yml`
**New ConfigFile:** `GuardianAbilitiesConfigFile` with static Route constants.

- Java wrapper: `src/main/java/us/eunoians/mcrpg/configuration/file/GuardianAbilitiesConfigFile.java` (extends `ConfigFile`)
- YAML resource: `src/main/resources/guardian_abilities_configuration.yml`

### 4.2 GuardianAbilitiesConfigFile

**New file:** `src/main/java/us/eunoians/mcrpg/configuration/file/GuardianAbilitiesConfigFile.java`

```java
package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.route.Route;

public class GuardianAbilitiesConfigFile extends ConfigFile {

    private static final String ABILITY_CONFIGURATION_HEADER = "ability-configuration";

    // Phase Shift
    private static final String PHASE_SHIFT_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "phase-shift");
    public static final Route PHASE_SHIFT_ENABLED = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "enabled"));
    public static final Route PHASE_SHIFT_MANA_COST = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "mana-cost"));
    public static final Route PHASE_SHIFT_COOLDOWN = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "cooldown"));
    public static final Route PHASE_SHIFT_MAX_RANGE = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "max-range"));
    public static final Route PHASE_SHIFT_LAST_HIT_WINDOW_SECONDS = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "last-hit-window-seconds"));
    public static final Route PHASE_SHIFT_CRIT_WINDOW_TICKS = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "crit-window-ticks"));
    public static final Route PHASE_SHIFT_CRIT_DAMAGE_MULTIPLIER = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "crit-damage-multiplier"));
    public static final Route PHASE_SHIFT_TELEPORT_OFFSET = Route.fromString(toRoutePath(PHASE_SHIFT_HEADER, "teleport-offset-behind-target"));

    // Whirlpool
    private static final String WHIRLPOOL_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "whirlpool");
    public static final Route WHIRLPOOL_ENABLED = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "enabled"));
    public static final Route WHIRLPOOL_MANA_COST = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "mana-cost"));
    public static final Route WHIRLPOOL_COOLDOWN = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "cooldown"));
    public static final Route WHIRLPOOL_RADIUS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "radius"));
    public static final Route WHIRLPOOL_DURATION_TICKS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "duration-ticks"));
    public static final Route WHIRLPOOL_PULL_VELOCITY = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "pull-velocity"));
    public static final Route WHIRLPOOL_SLOWNESS_AMPLIFIER = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "slowness-amplifier"));
    public static final Route WHIRLPOOL_SLOWNESS_DURATION_TICKS = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "slowness-duration-ticks"));
    public static final Route WHIRLPOOL_TICK_INTERVAL = Route.fromString(toRoutePath(WHIRLPOOL_HEADER, "tick-interval"));

    // Waterlogged Strike
    private static final String WATERLOGGED_STRIKE_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "waterlogged-strike");
    public static final Route WATERLOGGED_STRIKE_ENABLED = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "enabled"));
    public static final Route WATERLOGGED_STRIKE_MANA_COST = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "mana-cost"));
    public static final Route WATERLOGGED_STRIKE_COOLDOWN = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "cooldown"));
    public static final Route WATERLOGGED_STRIKE_DAMAGE = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "damage"));
    public static final Route WATERLOGGED_STRIKE_MAX_RANGE = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "max-range"));
    public static final Route WATERLOGGED_STRIKE_PROJECTILE_SPEED = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "projectile-speed"));
    public static final Route WATERLOGGED_STRIKE_SLOWNESS_AMPLIFIER = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "slowness-amplifier"));
    public static final Route WATERLOGGED_STRIKE_SLOWNESS_DURATION_TICKS = Route.fromString(toRoutePath(WATERLOGGED_STRIKE_HEADER, "slowness-duration-ticks"));

    // Tsunami Wall
    private static final String TSUNAMI_WALL_HEADER = toRoutePath(ABILITY_CONFIGURATION_HEADER, "tsunami-wall");
    public static final Route TSUNAMI_WALL_ENABLED = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "enabled"));
    public static final Route TSUNAMI_WALL_MANA_COST = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "mana-cost"));
    public static final Route TSUNAMI_WALL_COOLDOWN = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "cooldown"));
    public static final Route TSUNAMI_WALL_WIDTH = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "width"));
    public static final Route TSUNAMI_WALL_HEIGHT = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "height"));
    public static final Route TSUNAMI_WALL_DURATION_TICKS = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "duration-ticks"));
    public static final Route TSUNAMI_WALL_KNOCKBACK_STRENGTH = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "knockback-strength"));
    public static final Route TSUNAMI_WALL_SLOWNESS_AMPLIFIER = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "slowness-amplifier"));
    public static final Route TSUNAMI_WALL_SLOWNESS_DURATION_TICKS = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "slowness-duration-ticks"));
    public static final Route TSUNAMI_WALL_SPAWN_DISTANCE = Route.fromString(toRoutePath(TSUNAMI_WALL_HEADER, "spawn-distance"));
}
```

### 4.3 YAML Config File

**New file:** `src/main/resources/guardian_abilities_configuration.yml`

```yaml
config-version: 1

ability-configuration:

  phase-shift:
    enabled: true
    mana-cost: 40
    cooldown: 12
    # Max distance (blocks) to the last-attacked target for teleport to work
    max-range: 12
    # How recently (seconds) the player must have attacked the target
    last-hit-window-seconds: 5
    # Ticks after teleport during which the next attack is a guaranteed crit
    crit-window-ticks: 60
    # Damage multiplier applied to the guaranteed crit attack
    crit-damage-multiplier: 1.5
    # Blocks behind the target to teleport to
    teleport-offset-behind-target: 1.5
    unlock-conditions:
      book-source:
        type: mcrpg:display_hint
        locale-key: ability.unlock-condition.source.riptide-guardian
    display-item:
      name: "<ability-active>Phase Shift"
      lore:
        - "<body>Teleport behind your target and"
        - "<body>strike with a guaranteed critical hit."

  whirlpool:
    enabled: true
    mana-cost: 25
    cooldown: 12
    # Radius of the AoE zone in blocks
    radius: 4.0
    # Duration of the zone in ticks (100 ticks = 5 seconds)
    duration-ticks: 100
    # Velocity applied toward center each tick (0.1 = gentle pull)
    pull-velocity: 0.1
    # Slowness potion amplifier (0 = Slowness I, 1 = Slowness II)
    slowness-amplifier: 0
    # Duration of slowness effect in ticks per application
    slowness-duration-ticks: 40
    # Ticks between each pull/slow tick (4 = 5 times per second)
    tick-interval: 4
    unlock-conditions:
      book-source:
        type: mcrpg:display_hint
        locale-key: ability.unlock-condition.source.riptide-guardian
    display-item:
      name: "<ability-active>Whirlpool"
      lore:
        - "<body>Create a vortex at your feet that"
        - "<body>pulls in and slows nearby enemies."

  waterlogged-strike:
    enabled: true
    mana-cost: 15
    cooldown: 1
    # Damage in half-hearts (3.0 = 1.5 hearts)
    damage: 3.0
    # Max projectile travel distance in blocks
    max-range: 28
    # Projectile speed (blocks per tick)
    projectile-speed: 1.5
    # Slowness potion amplifier (1 = Slowness II)
    slowness-amplifier: 1
    # Duration of slowness effect in ticks
    slowness-duration-ticks: 60
    unlock-conditions:
      book-source:
        type: mcrpg:display_hint
        locale-key: ability.unlock-condition.source.riptide-guardian
    display-item:
      name: "<ability-active>Waterlogged Strike"
      lore:
        - "<body>Fire a water bolt that damages"
        - "<body>and heavily slows the target."

  tsunami-wall:
    enabled: true
    mana-cost: 50
    cooldown: 15
    # Wall dimensions in blocks
    width: 5
    height: 3
    # Duration the wall persists in ticks (140 ticks = 7 seconds)
    duration-ticks: 140
    # Knockback strength applied on contact
    knockback-strength: 1.5
    # Slowness potion amplifier (2 = Slowness III)
    slowness-amplifier: 2
    # Duration of slowness effect in ticks
    slowness-duration-ticks: 60
    # Distance in front of the player to spawn the wall
    spawn-distance: 2.0
    unlock-conditions:
      book-source:
        type: mcrpg:display_hint
        locale-key: ability.unlock-condition.source.riptide-guardian
    display-item:
      name: "<ability-active>Tsunami Wall"
      lore:
        - "<body>Summon a wall of water ahead"
        - "<body>that knocks back and slows enemies."
```

---

## 5. Phase Shift

### 5.1 Ability Summary

Teleport behind the last entity the player attacked (within a 5-second window and 12-block range). On arrival, the player's attack timer is reset and they receive a guaranteed critical hit window on their next attack within 3 seconds. No line-of-sight requirement.

### 5.2 Combat State Tracking

**New class:** `us.eunoians.mcrpg.entity.player.CombatTargetState`

A session-only, per-player state container tracking the last entity attacked by the player. Stored on `McRPGPlayer` as an optional transient field.

```java
package us.eunoians.mcrpg.entity.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class CombatTargetState {

    private UUID lastAttackedEntityUUID;
    private long lastAttackTimestamp;

    public void recordAttack(@NotNull UUID entityUUID, long timestamp) {
        this.lastAttackedEntityUUID = entityUUID;
        this.lastAttackTimestamp = timestamp;
    }

    @Nullable
    public UUID getLastAttackedEntityUUID() {
        return lastAttackedEntityUUID;
    }

    public long getLastAttackTimestamp() {
        return lastAttackTimestamp;
    }

    public boolean hasRecentTarget(long currentTime, long windowMillis) {
        return lastAttackedEntityUUID != null
                && (currentTime - lastAttackTimestamp) <= windowMillis;
    }

    public void clear() {
        lastAttackedEntityUUID = null;
        lastAttackTimestamp = 0;
    }
}
```

**McRPGPlayer modifications:**

```java
// New field
private CombatTargetState combatTargetState;

// Lazy initializer
@NotNull
public CombatTargetState getOrCreateCombatTargetState() {
    if (combatTargetState == null) {
        combatTargetState = new CombatTargetState();
    }
    return combatTargetState;
}

// Nullable accessor
@Nullable
public CombatTargetState getCombatTargetState() {
    return combatTargetState;
}

// Reset on logout (add to existing cleanup)
public void resetCombatTargetState() {
    combatTargetState = null;
}
```

### 5.3 Combat Target Listener

**New file:** `src/main/java/us/eunoians/mcrpg/listener/entity/OnPlayerAttackCombatTargetListener.java`

Listens for `EntityDamageByEntityEvent` where the damager is a player. Records the target UUID and timestamp on the player's `CombatTargetState`.

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onPlayerAttack(@NotNull EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
        return;
    }
    McRPGPlayer mcRPGPlayer = resolvePlayer(player);
    if (mcRPGPlayer == null) {
        return;
    }
    mcRPGPlayer.getOrCreateCombatTargetState().recordAttack(
            event.getEntity().getUniqueId(),
            System.currentTimeMillis());
}
```

### 5.4 Phase Shift Ability Class

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/PhaseShift.java`

```java
package us.eunoians.mcrpg.ability.impl.guardian;

public final class PhaseShift extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
                   CooldownableAbility, ManaAbility,
                   ActiveAbility, ComboActivatable {

    public static final NamespacedKey PHASE_SHIFT_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "phase_shift");

    public PhaseShift(@NotNull McRPG mcRPG) {
        super(mcRPG, PHASE_SHIFT_KEY);
    }

    @Override
    public int getManaCost(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_MANA_COST, "40");
        int cost = (int) new Parser(formula).getValue();
        int globalMinimum = resolveGlobalMinimumManaCost();
        return Math.max(cost, globalMinimum);
    }

    @Override
    public long getCooldown(@NotNull AbilityHolder abilityHolder) {
        String formula = getYamlDocument().getString(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_COOLDOWN, "12");
        return (long) new Parser(formula).getValue();
    }

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        Optional<McRPGPlayer> playerOpt = resolvePlayer(abilityHolder);
        if (playerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();
        Player player = mcRPGPlayer.getAsBukkitPlayer();

        // Check for recent combat target
        CombatTargetState state = mcRPGPlayer.getCombatTargetState();
        if (state == null) {
            sendNoTargetMessage(mcRPGPlayer);
            return false;
        }
        long windowMillis = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_LAST_HIT_WINDOW_SECONDS, 5) * 1000L;
        if (!state.hasRecentTarget(System.currentTimeMillis(), windowMillis)) {
            sendNoTargetMessage(mcRPGPlayer);
            return false;
        }

        // Resolve target entity (cannot Phase Shift to yourself)
        UUID targetUUID = state.getLastAttackedEntityUUID();
        if (targetUUID.equals(player.getUniqueId())) {
            sendNoTargetMessage(mcRPGPlayer);
            return false;
        }
        Entity target = player.getWorld().getEntity(targetUUID);
        if (target == null || target.isDead()) {
            sendNoTargetMessage(mcRPGPlayer);
            return false;
        }

        // Range check (no LOS required)
        double maxRange = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_MAX_RANGE, 12.0);
        if (player.getLocation().distanceSquared(target.getLocation()) > maxRange * maxRange) {
            sendOutOfRangeMessage(mcRPGPlayer);
            return false;
        }

        // Fire event
        PhaseShiftActivateEvent event = new PhaseShiftActivateEvent(
                abilityHolder, this, target);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        // Calculate teleport destination (behind target)
        double offset = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_TELEPORT_OFFSET, 1.5);
        Location destination = calculateBehindTarget(target, offset);
        if (!isSafeLocation(destination)) {
            destination = target.getLocation();
        }
        destination.setYaw(calculateFacingYaw(destination, target.getLocation()));
        destination.setPitch(0);

        // Teleport (async via Paper API)
        player.teleportAsync(destination);

        // Reset attack timer
        player.resetCooldown();

        // Apply crit window (via metadata tag, consumed by damage listener)
        int critWindowTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.PHASE_SHIFT_CRIT_WINDOW_TICKS, 60);
        applyCritWindow(mcRPGPlayer, critWindowTicks);

        // VFX
        spawnTeleportParticles(player.getLocation());
        player.getWorld().playSound(player.getLocation(),
                Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);

        return true;
    }

    // Unlock conditions are config-driven (loaded from YAML unlock-conditions section),
    // not hardcoded in Java. See Section 10.

    // ... config routes, display item, enabled check, helper methods ...
}
```

### 5.5 Phase Shift Crit Window

The crit window is tracked via a specialized `CoreTask`. When Phase Shift activates:

1. A `NamespacedKey`-tagged metadata entry is placed on the `McRPGPlayer` marking the crit window as active.
2. A `PhaseShiftCritWindowTask` (extends `DelayableCoreTask`) is scheduled to remove the tag after `crit-window-ticks` ticks.
3. A damage listener (`OnPhaseShiftCritListener`) checks for the tag on `EntityDamageByEntityEvent`. If present:
   - Multiplies damage by `crit-damage-multiplier` (default 1.5x)
   - Removes the tag (one-time crit, not sustained)
   - Plays a crit VFX (CRIT particles + ENTITY_PLAYER_ATTACK_CRIT sound)

**New listener:** `src/main/java/us/eunoians/mcrpg/listener/ability/guardian/OnPhaseShiftCritListener.java`

```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onPlayerAttack(@NotNull EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
        return;
    }
    Optional<McRPGPlayer> playerOpt = resolvePlayer(player);
    if (playerOpt.isEmpty()) {
        return;
    }
    McRPGPlayer mcRPGPlayer = playerOpt.get();
    if (!mcRPGPlayer.hasCritWindow()) {
        return;
    }
    double multiplier = getYamlDocument().getDouble(
            GuardianAbilitiesConfigFile.PHASE_SHIFT_CRIT_DAMAGE_MULTIPLIER, 1.5);
    event.setDamage(event.getDamage() * multiplier);
    mcRPGPlayer.consumeCritWindow();
    spawnCritParticles(event.getEntity().getLocation());
    player.getWorld().playSound(event.getEntity().getLocation(),
            Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
}
```

### 5.6 Teleport Destination Calculation

```java
private Location calculateBehindTarget(@NotNull Entity target, double offset) {
    Location targetLoc = target.getLocation();
    Vector direction = targetLoc.getDirection().normalize();
    // "Behind" = opposite of target's facing direction
    return targetLoc.clone().subtract(direction.multiply(offset));
}

private float calculateFacingYaw(@NotNull Location from, @NotNull Location to) {
    double dx = to.getX() - from.getX();
    double dz = to.getZ() - from.getZ();
    return (float) Math.toDegrees(Math.atan2(-dx, dz));
}

private boolean isSafeLocation(@NotNull Location location) {
    Block feet = location.getBlock();
    Block head = feet.getRelative(BlockFace.UP);
    Block ground = feet.getRelative(BlockFace.DOWN);
    return feet.isPassable() && head.isPassable() && !ground.isPassable();
}
```

### 5.7 Phase Shift VFX

```java
private void spawnTeleportParticles(@NotNull Location location) {
    location.getWorld().spawnParticle(Particle.SPLASH, location, 30, 0.5, 1.0, 0.5, 0.1);
    location.getWorld().spawnParticle(Particle.DRIPPING_WATER, location, 15, 0.3, 0.8, 0.3, 0.05);
}
```

---

## 6. Whirlpool

### 6.1 Ability Summary

Create a stationary AoE zone at the player's current location. For 5 seconds, the zone pulls entities toward its center with a gentle pull (0.1 velocity) and applies Slowness I. No damage.

### 6.2 Whirlpool Ability Class

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/Whirlpool.java`

```java
public final class Whirlpool extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
                   CooldownableAbility, ManaAbility,
                   ActiveAbility, ComboActivatable {

    public static final NamespacedKey WHIRLPOOL_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "whirlpool");

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        Optional<McRPGPlayer> playerOpt = resolvePlayer(abilityHolder);
        if (playerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();
        Player player = mcRPGPlayer.getAsBukkitPlayer();

        // Fire event
        WhirlpoolActivateEvent event = new WhirlpoolActivateEvent(
                abilityHolder, this, player.getLocation());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        Location center = player.getLocation().clone();

        // Read config
        double radius = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WHIRLPOOL_RADIUS, 4.0);
        int durationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_DURATION_TICKS, 100);
        double pullVelocity = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WHIRLPOOL_PULL_VELOCITY, 0.1);
        int slownessAmplifier = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_AMPLIFIER, 0);
        int slownessDurationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_SLOWNESS_DURATION_TICKS, 40);
        int tickInterval = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WHIRLPOOL_TICK_INTERVAL, 4);

        // Schedule repeating CoreTask for zone effects
        UUID casterUUID = player.getUniqueId();
        WhirlpoolZoneTask task = new WhirlpoolZoneTask(center, radius, pullVelocity,
                slownessAmplifier, slownessDurationTicks,
                casterUUID, durationTicks);
        task.scheduleRepeating(getPlugin(), 0L, tickInterval);

        // Spawn sound
        player.getWorld().playSound(center, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f, 0.5f);

        return true;
    }
}
```

### 6.3 WhirlpoolZoneTask

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/WhirlpoolZoneTask.java`

A specialized `CoreTask` (extends McCore's `CoreTask`) that ticks the whirlpool zone. Each tick:

1. Find all `LivingEntity` within `radius` of `center`, excluding the caster and allies (via `isAllies` check).
2. For each entity: calculate direction vector from entity to center, normalize, multiply by `pullVelocity`, apply as velocity.
3. Apply Slowness potion effect.
4. Spawn water particles in a spiral pattern at the zone boundary.
5. After `durationTicks` total elapsed, cancel self.

```java
@Override
public void run() {
    elapsedTicks += tickInterval;
    if (elapsedTicks >= durationTicks || !center.getWorld().isChunkLoaded(
            center.getBlockX() >> 4, center.getBlockZ() >> 4)) {
        cancel();
        return;
    }

    double radiusSquared = radius * radius;
    for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
        if (!(entity instanceof LivingEntity living)) {
            continue;
        }
        if (entity.getUniqueId().equals(casterUUID)) {
            continue;
        }
        // Skip allies (party members, etc.)
        if (entity instanceof Player targetPlayer && isAllies(casterUUID, targetPlayer)) {
            continue;
        }
        double distSq = entity.getLocation().distanceSquared(center);
        if (distSq > radiusSquared) {
            continue;
        }
        // Pull toward center
        Vector pullDirection = center.toVector().subtract(entity.getLocation().toVector());
        if (pullDirection.lengthSquared() > 0.01) {
            pullDirection.normalize().multiply(pullVelocity);
            entity.setVelocity(entity.getVelocity().add(pullDirection));
        }
        // Apply slowness
        living.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS, slownessDurationTicks,
                slownessAmplifier, false, true, true));
    }

    // Spiral water particles
    spawnWhirlpoolParticles(center, radius, elapsedTicks);
}
```

### 6.4 Whirlpool VFX

Spiral particle pattern rotating over time:

```java
private void spawnWhirlpoolParticles(@NotNull Location center, double radius, int ticks) {
    World world = center.getWorld();
    double angleOffset = (ticks * 0.2) % (2 * Math.PI);
    for (int i = 0; i < 12; i++) {
        double angle = angleOffset + (2 * Math.PI * i / 12);
        double x = center.getX() + radius * Math.cos(angle);
        double z = center.getZ() + radius * Math.sin(angle);
        world.spawnParticle(Particle.SPLASH, x, center.getY() + 0.1, z, 2, 0.1, 0.0, 0.1, 0);
    }
    // Center bubble
    world.spawnParticle(Particle.BUBBLE_POP, center, 3, 0.3, 0.1, 0.3, 0);
}
```

---

## 7. Waterlogged Strike

### 7.1 Ability Summary

Fire an invisible projectile entity (snowball with custom metadata, set invisible via `setInvisible(true)`) in the player's look direction. The projectile is represented visually by a dense particle trail (DRIPPING_WATER + SPLASH) rather than the snowball model. On impact with a `LivingEntity`, deal 1.5 hearts (3 half-hearts) of damage and apply Slowness II for 3 seconds. Max range ~28 blocks.

### 7.2 Waterlogged Strike Ability Class

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/WaterloggedStrike.java`

```java
public final class WaterloggedStrike extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
                   CooldownableAbility, ManaAbility,
                   ActiveAbility, ComboActivatable {

    public static final NamespacedKey WATERLOGGED_STRIKE_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "waterlogged_strike");

    public static final NamespacedKey PROJECTILE_TAG =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "waterlogged_strike_projectile");

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        Optional<McRPGPlayer> playerOpt = resolvePlayer(abilityHolder);
        if (playerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();
        Player player = mcRPGPlayer.getAsBukkitPlayer();

        // Fire event
        WaterloggedStrikeActivateEvent event = new WaterloggedStrikeActivateEvent(
                abilityHolder, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        // Launch projectile
        double speed = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_PROJECTILE_SPEED, 1.5);
        Snowball projectile = player.launchProjectile(Snowball.class,
                player.getLocation().getDirection().normalize().multiply(speed));
        projectile.setInvisible(true);
        projectile.getPersistentDataContainer().set(
                PROJECTILE_TAG, PersistentDataType.BOOLEAN, true);

        // Schedule particle trail + range limit
        int maxRange = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_MAX_RANGE, 28);
        WaterloggedStrikeTrailTask trailTask = new WaterloggedStrikeTrailTask(
                projectile, player.getLocation(), maxRange);
        trailTask.scheduleRepeating(getPlugin(), 1L, 1L);

        // Launch sound
        player.getWorld().playSound(player.getLocation(),
                Sound.ENTITY_FISHING_BOBBER_THROW, 1.0f, 0.8f);

        return true;
    }
}
```

### 7.3 Projectile Impact Listener

**New file:** `src/main/java/us/eunoians/mcrpg/listener/ability/guardian/OnWaterloggedStrikeImpactListener.java`

```java
@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
public void onProjectileHit(@NotNull ProjectileHitEvent event) {
    if (!(event.getEntity() instanceof Snowball snowball)) {
        return;
    }
    if (!Boolean.TRUE.equals(snowball.getPersistentDataContainer()
            .get(WaterloggedStrike.PROJECTILE_TAG, PersistentDataType.BOOLEAN))) {
        return;
    }
    if (event.getHitEntity() == null || !(event.getHitEntity() instanceof LivingEntity target)) {
        return;
    }
    if (!(snowball.getShooter() instanceof Player player)) {
        return;
    }

    double damage = getYamlDocument().getDouble(
            GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_DAMAGE, 3.0);
    int slownessAmplifier = getYamlDocument().getInt(
            GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_SLOWNESS_AMPLIFIER, 1);
    int slownessDurationTicks = getYamlDocument().getInt(
            GuardianAbilitiesConfigFile.WATERLOGGED_STRIKE_SLOWNESS_DURATION_TICKS, 60);

    target.damage(damage, player);
    target.addPotionEffect(new PotionEffect(
            PotionEffectType.SLOWNESS, slownessDurationTicks,
            slownessAmplifier, false, true, true));

    // Impact VFX
    Location hitLoc = target.getLocation();
    hitLoc.getWorld().spawnParticle(Particle.SPLASH, hitLoc, 20, 0.3, 0.5, 0.3, 0.1);
    hitLoc.getWorld().playSound(hitLoc, Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.2f);
}
```

### 7.4 Trail Task

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/WaterloggedStrikeTrailTask.java`

A specialized `CoreTask` that spawns dense water particles along the invisible projectile's path each tick, making the projectile visually distinct. Cancels when the projectile is dead, on the ground, or has exceeded `maxRange` from the origin.

```java
@Override
public void run() {
    if (projectile.isDead() || projectile.isOnGround()
            || projectile.getLocation().distanceSquared(origin) > maxRangeSquared) {
        projectile.remove();
        cancel();
        return;
    }
    Location loc = projectile.getLocation();
    // Dense particle trail to represent the invisible projectile
    loc.getWorld().spawnParticle(Particle.DRIPPING_WATER, loc, 8, 0.1, 0.1, 0.1, 0);
    loc.getWorld().spawnParticle(Particle.SPLASH, loc, 4, 0.05, 0.05, 0.05, 0);
}
```

---

## 8. Tsunami Wall

### 8.1 Ability Summary

Summon a 5-wide x 3-tall particle wall in front of the player, facing the player's look direction. The wall persists for 7 seconds. Any entity that contacts the wall is knocked back and receives Slowness III for 3 seconds. The wall is purely visual (particles, not real blocks).

### 8.2 Tsunami Wall Ability Class

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/TsunamiWall.java`

```java
public final class TsunamiWall extends McRPGAbility
        implements ConfigurableAbility, UnlockableAbility,
                   CooldownableAbility, ManaAbility,
                   ActiveAbility, ComboActivatable {

    public static final NamespacedKey TSUNAMI_WALL_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "tsunami_wall");

    @Override
    public boolean comboActivate(@NotNull AbilityHolder abilityHolder) {
        Optional<McRPGPlayer> playerOpt = resolvePlayer(abilityHolder);
        if (playerOpt.isEmpty()) {
            return false;
        }
        McRPGPlayer mcRPGPlayer = playerOpt.get();
        Player player = mcRPGPlayer.getAsBukkitPlayer();

        // Fire event
        TsunamiWallActivateEvent event = new TsunamiWallActivateEvent(
                abilityHolder, this, player.getLocation());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        // Read config
        int width = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_WIDTH, 5);
        int height = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_HEIGHT, 3);
        int durationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_DURATION_TICKS, 140);
        double knockbackStrength = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_KNOCKBACK_STRENGTH, 1.5);
        int slownessAmplifier = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_AMPLIFIER, 2);
        int slownessDurationTicks = getYamlDocument().getInt(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SLOWNESS_DURATION_TICKS, 60);
        double spawnDistance = getYamlDocument().getDouble(
                GuardianAbilitiesConfigFile.TSUNAMI_WALL_SPAWN_DISTANCE, 2.0);

        // Calculate wall position and orientation
        Location playerLoc = player.getLocation();
        Vector forward = playerLoc.getDirection().setY(0).normalize();
        Location wallCenter = playerLoc.clone().add(forward.multiply(spawnDistance));
        Vector wallRight = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        UUID casterUUID = player.getUniqueId();
        TsunamiWallTask task = new TsunamiWallTask(wallCenter, wallRight, width, height,
                knockbackStrength, slownessAmplifier, slownessDurationTicks,
                forward, casterUUID, durationTicks);
        task.scheduleRepeating(getPlugin(), 0L, 2L);

        // Spawn sound
        player.getWorld().playSound(wallCenter,
                Sound.ENTITY_GENERIC_SPLASH, 1.0f, 0.6f);

        return true;
    }
}
```

### 8.3 TsunamiWallTask

**New file:** `src/main/java/us/eunoians/mcrpg/ability/impl/guardian/TsunamiWallTask.java`

A specialized `CoreTask` that maintains the wall. Each tick (every 2 ticks):

1. Render particles along the wall surface (grid of WATER_SPLASH + ENCHANTED_HIT).
2. Check for entities intersecting the wall AABB.
3. For each intersecting entity (excluding caster): apply knockback away from the wall surface + Slowness III.

```java
@Override
public void run() {
    elapsedTicks += 2;
    if (elapsedTicks >= durationTicks || !wallCenter.getWorld().isChunkLoaded(
            wallCenter.getBlockX() >> 4, wallCenter.getBlockZ() >> 4)) {
        cancel();
        return;
    }

    World world = wallCenter.getWorld();

    // Render particles along the wall
    for (int w = 0; w < width; w++) {
        for (int h = 0; h < height; h++) {
            double offsetW = (w - width / 2.0) + 0.5;
            double x = wallCenter.getX() + wallRight.getX() * offsetW;
            double y = wallCenter.getY() + h;
            double z = wallCenter.getZ() + wallRight.getZ() * offsetW;
            world.spawnParticle(Particle.SPLASH, x, y, z, 1, 0.1, 0.1, 0.1, 0);
        }
    }

    // Contact check: entities within 0.5 blocks of the wall plane
    double halfWidth = width / 2.0;
    for (Entity entity : world.getNearbyEntities(wallCenter, halfWidth + 1, height, 1.5)) {
        if (!(entity instanceof LivingEntity living)) {
            continue;
        }
        if (entity.getUniqueId().equals(casterUUID)) {
            continue;
        }
        // Skip allies (party members, etc.)
        if (entity instanceof Player targetPlayer && isAllies(casterUUID, targetPlayer)) {
            continue;
        }
        if (isInWallBounds(entity.getLocation())) {
            // Knockback away from wall (in the forward direction)
            living.setVelocity(forward.clone().normalize().multiply(knockbackStrength)
                    .setY(0.3));
            living.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS, slownessDurationTicks,
                    slownessAmplifier, false, true, true));
        }
    }
}
```

### 8.4 Wall Bounds Check

The wall is an infinitely-thin plane. Contact is defined as being within 0.5 blocks of the plane and within the wall's width/height bounds.

```java
private boolean isInWallBounds(@NotNull Location entityLoc) {
    Vector relative = entityLoc.toVector().subtract(wallCenter.toVector());
    double alongWall = relative.dot(wallRight);
    double heightAbove = entityLoc.getY() - wallCenter.getY();
    double perpendicular = relative.dot(forward);
    return Math.abs(alongWall) <= width / 2.0
            && heightAbove >= 0 && heightAbove <= height
            && Math.abs(perpendicular) <= 0.8;
}
```

---

## 9. Custom Events

Guardian abilities fire two categories of cancellable custom events:

1. **Activation events** — fired before the ability applies its effect. If cancelled, `comboActivate` returns `false` and mana is refunded by `OnComboCompleteListener`.
2. **Effect events** — fired when an ability deals damage or applies effects to a target. If cancelled, the specific damage/effect is skipped but the ability activation is not reverted.

### 9.1 Base Event Class

**New package:** `src/main/java/us/eunoians/mcrpg/event/ability/guardian/`

```java
package us.eunoians.mcrpg.event.ability.guardian;

public abstract class AbilityActivateEvent extends McRPGPlayerEvent implements Cancellable {

    private final AbilityHolder abilityHolder;
    private final Ability ability;
    private boolean cancelled;

    protected AbilityActivateEvent(@NotNull AbilityHolder holder,
                                    @NotNull Ability ability) {
        super(resolvePlayer(holder));
        this.abilityHolder = holder;
        this.ability = ability;
    }

    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    @NotNull
    public Ability getAbility() {
        return ability;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
```

### 9.2 Activation Events

| Event | Extends | Extra Fields |
|---|---|---|
| `PhaseShiftActivateEvent` | `AbilityActivateEvent` | `Entity target` |
| `WhirlpoolActivateEvent` | `AbilityActivateEvent` | `Location center` |
| `WaterloggedStrikeActivateEvent` | `AbilityActivateEvent` | — |
| `TsunamiWallActivateEvent` | `AbilityActivateEvent` | `Location wallCenter` |

```java
public class PhaseShiftActivateEvent extends AbilityActivateEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Entity target;

    public PhaseShiftActivateEvent(@NotNull AbilityHolder holder,
                                    @NotNull Ability ability,
                                    @NotNull Entity target) {
        super(holder, ability);
        this.target = target;
    }

    @NotNull
    public Entity getTarget() {
        return target;
    }

    @Override @NotNull public HandlerList getHandlers() { return handlers; }
    @NotNull public static HandlerList getHandlerList() { return handlers; }
}
```

`WhirlpoolActivateEvent`, `WaterloggedStrikeActivateEvent`, and `TsunamiWallActivateEvent` follow the same pattern with their respective extra fields.

### 9.3 Effect Events

These events are fired when an ability applies damage or effects to a specific target. They are cancellable — cancelling prevents the damage/effect from being applied to that target without reverting the ability activation itself. Third-party plugins can listen to these events to modify, log, or cancel specific interactions.

| Event | Extends | Extra Fields | Fired When |
|---|---|---|---|
| `PhaseShiftCritDamageEvent` | `McRPGPlayerEvent` | `Entity target`, `double originalDamage`, `double critDamage`, `double multiplier` | Crit window consumed and damage multiplied |
| `WhirlpoolPullEvent` | `McRPGPlayerEvent` | `Player caster` (inherited), `LivingEntity target`, `Location center`, `Vector pullVector` | Entity pulled toward whirlpool center |
| `WaterloggedStrikeImpactEvent` | `McRPGPlayerEvent` | `LivingEntity target`, `double damage`, `int slownessAmplifier`, `int slownessDurationTicks` | Projectile hits a living entity |
| `TsunamiWallContactEvent` | `McRPGPlayerEvent` | `LivingEntity target`, `Vector knockbackVector`, `int slownessAmplifier`, `int slownessDurationTicks` | Entity contacts the wall |

All effect events implement `Cancellable`. Example:

```java
public class WhirlpoolPullEvent extends McRPGPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity target;
    private final Location center;
    private Vector pullVector;
    private boolean cancelled;

    public WhirlpoolPullEvent(@NotNull Player caster, @NotNull LivingEntity target,
                               @NotNull Location center, @NotNull Vector pullVector) {
        super(caster);
        this.target = target;
        this.center = center;
        this.pullVector = pullVector;
    }

    @NotNull public LivingEntity getTarget() { return target; }
    @NotNull public Location getCenter() { return center; }
    @NotNull public Vector getPullVector() { return pullVector; }
    public void setPullVector(@NotNull Vector pullVector) { this.pullVector = pullVector; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override @NotNull public HandlerList getHandlers() { return handlers; }
    @NotNull public static HandlerList getHandlerList() { return handlers; }
}
```

**Integration points:**

- `OnPhaseShiftCritListener`: fires `PhaseShiftCritDamageEvent` before applying the multiplied damage. If cancelled, damage is not modified and the crit window is still consumed.
- `WhirlpoolZoneTask`: fires `WhirlpoolPullEvent` before applying velocity/slowness to each entity. If cancelled, that entity is skipped for that tick.
- `OnWaterloggedStrikeImpactListener`: fires `WaterloggedStrikeImpactEvent` before applying damage/slowness. If cancelled, no damage or slowness applied.
- `TsunamiWallTask`: fires `TsunamiWallContactEvent` before applying knockback/slowness. If cancelled, that entity is unaffected.

---

## 10. Custom Statistics

Each guardian ability tracks per-player statistics via `McRPGStatistic`. These statistics persist across sessions and are designed to feed into quest objectives, achievements, and leaderboards.

### 10.1 Statistic Keys

Activation counts are already tracked out-of-box via `ActiveAbility.getActivationStatisticKey()`. The statistics below are **additional** per-ability metrics beyond simple activation counts.

| Statistic Key | Type | Description |
|---|---|---|
| `mcrpg:phase_shift_crit_damage_dealt` | Accumulator | Total bonus crit damage dealt via Phase Shift |
| `mcrpg:phase_shift_distance_teleported` | Accumulator | Total blocks teleported via Phase Shift |
| `mcrpg:whirlpool_total_pull_distance` | Accumulator | Total meters entities were pulled toward center |
| `mcrpg:whirlpool_entities_affected` | Counter | Total unique entities affected by Whirlpool |
| `mcrpg:waterlogged_strike_hits` | Counter | Total successful projectile hits |
| `mcrpg:waterlogged_strike_damage_dealt` | Accumulator | Total damage dealt by Waterlogged Strike impacts |
| `mcrpg:tsunami_wall_entities_knocked` | Counter | Total entities knocked back by Tsunami Wall |
| `mcrpg:tsunami_wall_total_knockback_applied` | Accumulator | Total knockback force applied across all contacts |

### 10.2 Statistic Registration

**Modified file:** `src/main/java/us/eunoians/mcrpg/statistic/McRPGStatistic.java`

Add static `NamespacedKey` constants for each guardian ability statistic. Register in `McRPGExpansion` alongside ability registration.

### 10.3 Statistic Increment Points

| Ability | Increment Location |
|---|---|
| Phase Shift | `comboActivate` increments activation + distance; `OnPhaseShiftCritListener` increments crit damage |
| Whirlpool | `comboActivate` increments activation; `WhirlpoolZoneTask` increments pull distance + entities affected |
| Waterlogged Strike | `comboActivate` increments activation; `OnWaterloggedStrikeImpactListener` increments hits + damage |
| Tsunami Wall | `comboActivate` increments activation; `TsunamiWallTask` increments entities knocked + force applied |

### 10.4 Quest/Achievement Integration

These statistics use the existing `McRPGStatistic` system, which is already queryable by the quest framework. Example quest objectives that become possible:

- "Pull 500 total meters with Whirlpool"
- "Land 100 Waterlogged Strike hits"
- "Teleport 1000 blocks with Phase Shift"
- "Knock back 200 entities with Tsunami Wall"

---

## 11. Unlock Conditions

All four abilities use config-driven unlock conditions defined in `guardian_abilities_configuration.yml`. The default config uses `DisplayHintUnlockConditionType` with a `locale-key` pointing to a translatable "Obtain from Riptide Guardian" string. Books bypass conditions entirely — `SkillBookConsumeListener` sets `AbilityUnlockedAttribute` directly.

### 10.1 Config-Driven (No Java Default)

Unlock conditions are **not hardcoded** in `getDefaultUnlockConditions()`. Instead, each ability reads its unlock conditions from the `unlock-conditions` section in the YAML config. This allows server owners full control without needing to override Java defaults.

Each ability's unlock condition loading:

```java
@Override
@NotNull
public List<UnlockConditionType> getDefaultUnlockConditions() {
    // Loaded from config YAML unlock-conditions section, not hardcoded
    return loadUnlockConditionsFromConfig(
            GuardianAbilitiesConfigFile.PHASE_SHIFT_UNLOCK_CONDITIONS);
}
```

### 10.2 Locale Entry

In `en_abilities.yml`:

```yaml
ability:
  unlock-condition:
    source:
      riptide-guardian: "<body>Obtain from <primary>Riptide Guardian"
```

### 10.3 Server Owner Customization

Server owners can add, remove, or modify unlock display entries per-ability directly in `guardian_abilities_configuration.yml`:

```yaml
phase-shift:
  unlock-conditions:
    book-source:
      type: mcrpg:display_hint
      locale-key: ability.unlock-condition.source.riptide-guardian
    epic-crates:
      type: mcrpg:display_hint
      text: "<body>Can be unlocked from <primary>Epic Crates<body>!"
```

---

## 12. Localization Keys

### 12.1 LocalizationKey.java Additions

**Modified file:** `configuration/file/localization/LocalizationKey.java`

```java
// Guardian abilities
private static final String GUARDIAN_ABILITY_HEADER = toRoutePath(ABILITY_HEADER, "guardian");
public static final Route PHASE_SHIFT_NO_TARGET =
        Route.fromString(toRoutePath(GUARDIAN_ABILITY_HEADER, "phase-shift.no-target"));
public static final Route PHASE_SHIFT_OUT_OF_RANGE =
        Route.fromString(toRoutePath(GUARDIAN_ABILITY_HEADER, "phase-shift.out-of-range"));
public static final Route PHASE_SHIFT_ACTIVATED =
        Route.fromString(toRoutePath(GUARDIAN_ABILITY_HEADER, "phase-shift.activated"));
public static final Route WHIRLPOOL_ACTIVATED =
        Route.fromString(toRoutePath(GUARDIAN_ABILITY_HEADER, "whirlpool.activated"));
public static final Route WATERLOGGED_STRIKE_ACTIVATED =
        Route.fromString(toRoutePath(GUARDIAN_ABILITY_HEADER, "waterlogged-strike.activated"));
public static final Route TSUNAMI_WALL_ACTIVATED =
        Route.fromString(toRoutePath(GUARDIAN_ABILITY_HEADER, "tsunami-wall.activated"));
```

### 12.2 en_abilities.yml Additions

```yaml
  guardian:
    phase-shift:
      no-target: "<negative>No recent target to Phase Shift to."
      out-of-range: "<negative>Target is too far away for Phase Shift."
      activated: "<positive>Phase Shifted behind target!"
    whirlpool:
      activated: "<positive>Whirlpool created!"
    waterlogged-strike:
      activated: "<positive>Waterlogged Strike fired!"
    tsunami-wall:
      activated: "<positive>Tsunami Wall deployed!"
```

### 12.3 Unlock Condition Source Entry

```yaml
  unlock-condition:
    source:
      riptide-guardian: "<body>Obtain from <primary>Riptide Guardian"
```

---

## 13. Bootstrap Registration

### 13.1 McRPGExpansion

**Modified file:** `src/main/java/us/eunoians/mcrpg/expansion/McRPGExpansion.java`

In `getAbilityContent()`:

```java
// Guardian abilities (standalone, non-tiered)
abilityContent.addContent(new PhaseShift(mcRPG));
abilityContent.addContent(new Whirlpool(mcRPG));
abilityContent.addContent(new WaterloggedStrike(mcRPG));
abilityContent.addContent(new TsunamiWall(mcRPG));
```

### 13.2 FileType

**Modified file:** `src/main/java/us/eunoians/mcrpg/configuration/FileType.java`

```java
GUARDIAN_ABILITIES_CONFIG("guardian_abilities_configuration.yml", new GuardianAbilitiesConfigFile())
```

### 13.3 Listener Registration

**Modified file:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGListenerRegistrar.java`

```java
// Guardian ability listeners
Bukkit.getPluginManager().registerEvents(new OnPlayerAttackCombatTargetListener(), plugin);
Bukkit.getPluginManager().registerEvents(new OnPhaseShiftCritListener(), plugin);
Bukkit.getPluginManager().registerEvents(new OnWaterloggedStrikeImpactListener(), plugin);
```

---

## 14. Edge Cases & Graceful Degradation

| Scenario | Behavior |
|---|---|
| Phase Shift target dies between activation and teleport | `comboActivate` returns `false` (target dead check), mana refunded |
| Phase Shift destination is inside a block | Falls back to target's exact location; if still unsafe, returns `false` |
| Phase Shift target in different world | World.getEntity(UUID) returns null → "no target" message, returns `false` |
| Whirlpool zone chunk unloads | Zone task cancels on next tick (chunk loaded check) |
| Whirlpool on self (player inside zone) | Caster excluded from pull/slow by UUID check |
| Waterlogged Strike hits a block | Snowball despawns naturally; no entity damage; trail task cancels |
| Waterlogged Strike exceeds max range | Trail task removes projectile and cancels |
| Tsunami Wall chunk unloads | Wall task cancels on next tick (chunk loaded check) |
| Tsunami Wall placed in mid-air | Wall renders at the placement location; entities below wall are unaffected (height check) |
| Player logs out during active zone/wall | CoreTask continues until duration expires; UUID exclusion prevents ghost interactions |
| Ability disabled in config | `isAbilityEnabled()` returns false → cannot be slotted in loadout |
| Server restart during active zone/wall | All CoreTask tasks are cancelled by Bukkit on shutdown |
| Player attacks themselves (self-damage) | `CombatTargetState` records self UUID, but `comboActivate` checks `targetUUID.equals(player.getUniqueId())` and returns `false` with "no target" message — Phase Shift cannot target yourself |

---

## 15. Test Plan

### 15.1 Unit Tests (`src/test/java`)

| Test Class | Coverage |
|---|---|
| `CombatTargetStateTest` | `recordAttack` stores UUID + timestamp; `hasRecentTarget` true within window, false outside; `clear` resets state |
| `PhaseShiftTeleportCalculationTest` | `calculateBehindTarget` places destination behind target facing direction; `calculateFacingYaw` faces the target; `isSafeLocation` rejects solid blocks, accepts passable |
| `GuardianAbilitiesConfigFileTest` | All Route constants resolve to valid YAML paths in the bundled config file |

### 15.2 MockBukkit Tests (extend `McRPGBaseTest`)

| Test Class | Coverage |
|---|---|
| `PhaseShiftTest` | Activation with valid target: teleport + crit window + event fired. No recent target: returns false, mana refunded. Target dead: returns false. Target out of range: returns false. Event cancelled: returns false |
| `WhirlpoolTest` | Activation creates zone: pull + slowness applied to nearby entity. Caster excluded from effects. Zone expires after duration |
| `WaterloggedStrikeTest` | Projectile launched with PDC tag. Impact applies damage + slowness. Block impact: no entity damage. Range exceeded: projectile removed |
| `TsunamiWallTest` | Wall created at correct location. Contact applies knockback + slowness. Caster excluded. Wall expires after duration |
| `OnPhaseShiftCritListenerTest` | Crit window active: damage multiplied on next attack, window consumed. No crit window: no modification |
| `OnWaterloggedStrikeImpactListenerTest` | Tagged snowball hitting entity: damage + slowness. Untagged snowball: no effect. Non-entity hit: no effect |

### 15.3 Manual Testing (Paper Server)

| Scenario | Verification |
|---|---|
| Obtain skill book from Riptide Guardian kill | Book drops, right-click unlocks ability, ability appears in loadout |
| Phase Shift in melee combat | Teleport behind target, attack timer reset, crit hit on next swing |
| Phase Shift with no recent target | "No recent target" message, mana refunded |
| Whirlpool in group combat | Zone pulls enemies, slows them, caster unaffected |
| Waterlogged Strike ranged poke | Projectile fires, particle trail visible, impact damages and slows |
| Tsunami Wall chokepoint denial | Wall renders, entities knocked back on contact, wall expires after duration |
| All four abilities in loadout rotation | Slot any into loadout, combo activates correctly, cooldowns respected |
| PvP testing | All abilities affect other players as expected |
| Config reload | `/mcrpg admin reload` updates mana costs, cooldowns, damage values |
| Disabled ability | Set `enabled: false` → ability cannot be slotted or activated |

---

## 16. File Manifest

### New Files

| File | Type | Description |
|---|---|---|
| `ability/impl/guardian/PhaseShift.java` | Ability | Phase Shift ability class |
| `ability/impl/guardian/Whirlpool.java` | Ability | Whirlpool ability class |
| `ability/impl/guardian/WaterloggedStrike.java` | Ability | Waterlogged Strike ability class |
| `ability/impl/guardian/TsunamiWall.java` | Ability | Tsunami Wall ability class |
| `ability/impl/guardian/WhirlpoolZoneTask.java` | Task | Whirlpool repeating zone logic |
| `ability/impl/guardian/WaterloggedStrikeTrailTask.java` | Task | Projectile trail + range enforcement |
| `ability/impl/guardian/TsunamiWallTask.java` | Task | Wall rendering + contact detection |
| `ability/impl/guardian/PhaseShiftCritWindowTask.java` | Task | Crit window expiration (extends DelayableCoreTask) |
| `entity/player/CombatTargetState.java` | State | Per-player last-attacked target tracking |
| `event/ability/guardian/AbilityActivateEvent.java` | Event | Base cancellable event for guardian abilities |
| `event/ability/guardian/PhaseShiftActivateEvent.java` | Event | Phase Shift activation event |
| `event/ability/guardian/WhirlpoolActivateEvent.java` | Event | Whirlpool activation event |
| `event/ability/guardian/WaterloggedStrikeActivateEvent.java` | Event | Waterlogged Strike activation event |
| `event/ability/guardian/TsunamiWallActivateEvent.java` | Event | Tsunami Wall activation event |
| `event/ability/guardian/PhaseShiftCritDamageEvent.java` | Event | Crit damage effect event |
| `event/ability/guardian/WhirlpoolPullEvent.java` | Event | Whirlpool pull effect event |
| `event/ability/guardian/WaterloggedStrikeImpactEvent.java` | Event | Projectile impact effect event |
| `event/ability/guardian/TsunamiWallContactEvent.java` | Event | Wall contact effect event |
| `listener/entity/OnPlayerAttackCombatTargetListener.java` | Listener | Records last-attacked target for Phase Shift |
| `listener/ability/guardian/OnPhaseShiftCritListener.java` | Listener | Applies crit damage during crit window |
| `listener/ability/guardian/OnWaterloggedStrikeImpactListener.java` | Listener | Handles projectile impact damage + effects |
| `configuration/file/GuardianAbilitiesConfigFile.java` | Config | Route constants for guardian abilities config |
| `src/main/resources/guardian_abilities_configuration.yml` | Config | Default config file |

All Java files under `src/main/java/us/eunoians/mcrpg/`.

### Modified Files

| File | Change |
|---|---|
| `entity/player/McRPGPlayer.java` | Add `CombatTargetState` field + accessors + cleanup on logout |
| `configuration/FileType.java` | Add `GUARDIAN_ABILITIES_CONFIG` enum entry |
| `expansion/McRPGExpansion.java` | Register four guardian abilities in `getAbilityContent()` |
| `bootstrap/McRPGListenerRegistrar.java` | Register combat target, crit, and impact listeners |
| `configuration/file/localization/LocalizationKey.java` | Add guardian ability locale route constants |
| `src/main/resources/localization/english/en_abilities.yml` | Add `ability.guardian.*` messages + `ability.unlock-condition.source.riptide-guardian` |
| `statistic/McRPGStatistic.java` | Add guardian ability statistic key constants |

### Not Modified (Used As-Is)

| File | Role |
|---|---|
| `SkillBookFactory.java` | Creates skill book items for these abilities |
| `SkillBookConsumeListener.java` | Handles skill book consumption and unlock |
| `OnComboCompleteListener.java` | Handles mana consumption, cooldown, refund |
| `AbilityRegistry.java` | Registers all four abilities |
| `AbilityUnlockedAttribute.java` | Tracks unlock state |
| `DisplayHintUnlockConditionType.java` | Default unlock condition |

---

## 17. Future LLD Notes

- **Fishing skill integration.** If a Fishing skill is added, these abilities could optionally be re-parented to it by implementing `SkillAbility` and adding skill-level unlock conditions alongside the book source. The `UnlockConditionType` system already supports OR-combining multiple paths.
- **Tier system.** If tiers are added later, migrate from `ConfigurableAbility` to `ConfigurableTierableAbility` with tier-variable Parser formulas. The config structure would gain `tier-configuration` sections. Current Parser formula support makes this migration straightforward.
- **PvP split tuning.** Add `pvp-damage-multiplier` and `pvp-slowness-amplifier` config keys per ability. The damage/effect application code checks `target instanceof Player` and applies the multiplier.
- **Phase Shift wall-clip prevention.** Advanced teleport destination validation with collision checking could supplement the basic `isSafeLocation` check.
- **General combat tracker.** Replace `CombatTargetState` with a plugin-wide combat tracker system (see Section 3.7 backlog note).
