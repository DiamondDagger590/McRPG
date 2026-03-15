# High-Level Design: Riptide Guardian Mob System

**Status:** Draft
**Date:** 2026-03-15
**Author:** McRPG Team

---

**This is an HLD only.** Each section (or group of related sections) will have its own LLD for granular design before implementation begins. No code is produced from this document directly.

## Table of Contents

1. [Overview & Goals](#1-overview--goals)
2. [MythicMobs Binding System](#2-mythicmobs-binding-system)
3. [Generic Fishing Mob Spawn System](#3-generic-fishing-mob-spawn-system)
4. [Mob Abilities (MythicMobs-side)](#4-mob-abilities-mythicmobs-side-documentation-only)
5. [Skill Book System](#5-skill-book-system)
6. [Player Ability Concepts](#6-player-ability-concepts-ideas-only--not-implemented)
7. [UnlockCondition System](#7-unlockcondition-system-design-document--not-implemented)
8. [Server Restart & Mob Lifecycle](#8-server-restart--mob-lifecycle)
9. [MythicMobs ThreatTable & Aggro Integration](#9-mythicmobs-threattable--aggro-integration)
10. [Custom Events](#10-custom-events-extension-points)
11. [Future Considerations](#11-future-considerations)
12. [GitHub Backlog Issues](#12-github-backlog-issues-to-cut)
13. [LLD Breakdown](#13-lld-breakdown)
14. [Key Existing Files](#14-key-existing-files)

---

## 1. Overview & Goals

**Problem:** AFK fishing exploits economy and progression with zero engagement.

**Solution:** Probabilistic mob spawn system that escalates with stationary fishing. MythicMobs owns the mob. McRPG owns spawn triggering, lifecycle hooks, and rewards.

**Principles:**
- **MythicMobs is the mob.** McRPG does not wrap, manage, or own the entity. It binds behavior (loot, spawn tracking, events) to MythicMob type IDs via configuration.
- **Config-driven.** Server owners can bind McRPG behavior (loot tables, spawn rules) to ANY MythicMob via YAML — no plugin code needed.
- **Framework-first.** The binding, spawn, and skill book systems are generic. The Riptide Guardian is the first consumer, not the only one.
- **Requires MythicMobs.** No MM = no mob spawning. Graceful degradation: spawn tracking runs but spawn calls are no-ops.
- **Multi-mob extensible.** The spawn system supports weighted mob pools, not just a single mob ID, so future content can add variety (e.g., a weaker mob at low chance, guardian at high chance).

---

## 2. MythicMobs Binding System

### Philosophy

The mob IS a MythicMob. McRPG doesn't wrap it in a `CustomMob` abstraction. Instead, McRPG provides a **binding** system: server owners (or content expansions) declare what McRPG should do when specific MythicMobs events occur for specific MM type IDs.

### Architecture

**`MythicMobBinding`** — a config-driven data object (not an entity wrapper) that says:
- "When MythicMob type ID `X` spawns/dies/despawns, McRPG does `Y`"

**New classes:**

| Class | Package | Purpose |
|---|---|---|
| `MythicMobsHook` | `external.mythicmobs` | Registered in `McRPGHooksRegistrar` when MM is present |
| `MythicMobsIntegration` | `external.mythicmobs` | Wraps MM API calls (spawn mob, check type ID, query ThreatTable). All MM API usage funneled here. |
| `MythicMobBinding` | `external.mythicmobs.binding` | Data class: MM type ID → loot table, custom events to fire, spawn VFX, despawn policy |
| `MythicMobBindingRegistry` | `external.mythicmobs.binding` | Maps MM type ID → `MythicMobBinding`. Populated from YAML config at startup. |
| `OnMythicMobEventListener` | `listener.entity` | Bridges MM events. Only registered if hook active. Looks up binding by type ID. |

**Event flow (observer pattern, not lifecycle management):**

```
MythicMobSpawnEvent → listener checks binding registry for type ID
  → if bound: fire CustomMobSpawnEvent, apply spawn VFX
  → McRPG does NOT create an AbilityHolder or track the entity

MythicMobDeathEvent → listener checks binding registry
  → if bound: evaluate loot table, distribute rewards, fire CustomMobDeathEvent

MythicMobDespawnEvent → listener checks binding registry
  → if bound: fire CustomMobDespawnEvent (no rewards)
```

**Key difference from original design:** McRPG never "registers" the entity in `EntityManager`. The mob is purely a MythicMob. McRPG just observes events and reacts.

### Config File Location

**New FileType:** `MYTHICMOB_BINDINGS` → `mythicmob_bindings.yml`
**New ConfigFile:** `MythicMobBindingsConfigFile` with static Route constants.

Config files follow the existing pattern:
- Java wrapper: `src/main/java/us/eunoians/mcrpg/configuration/file/MythicMobBindingsConfigFile.java` (extends `ConfigFile`)
- YAML resource: `src/main/resources/mythicmob_bindings.yml`
- FileType entry: `MYTHICMOB_BINDINGS("mythicmob_bindings.yml", new MythicMobBindingsConfigFile())`

```yaml
config-version: 1

bindings:
  RiptideGuardian:  # MythicMobs internal type ID
    enabled: true
    # Despawn policy (see Section 8)
    despawn:
      max-lifetime-seconds: 300  # 5 minutes — prevents indefinite storage
      despawn-if-no-threat: true  # Despawn if ThreatTable empties (all players leave)
      despawn-no-threat-delay-seconds: 30
    # Loot table evaluated on death (basic version — see Section 2.1 for future plans)
    loot:
      exclusive-drop: true  # At most one entry wins per kill
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
    # Optional spawn VFX
    spawn-effects:
      particles: WATER_SPLASH
      sound: ENTITY_ELDER_GUARDIAN_CURSE
    # Custom McRPG events to fire (for third-party plugin hooks)
    fire-events: true
```

Content expansions can also register bindings programmatically via `MythicMobBindingRegistry.register()`.

### 2.1 Future: Advanced Loot System (Backlog — GitHub Issue)

The initial loot system is simple: roll against flat per-entry chances, optionally exclusive. A **future pass** should add:

- **Top-N damage rewards** — bonus loot entries for top N players by damage dealt
- **Last-hit bonus** — reward the player who lands the killing blow
- **Damage-taken rewards** — reward the "tank" who absorbed the most damage
- **Healing contribution** — reward healers based on healing done to participants
- **Participation threshold** — minimum damage/time-in-combat to qualify for any loot

This will require tracking combat contributions via MythicMobs ThreatTable data and custom McRPG tracking. **Not implemented in this pass** — a GitHub issue will be cut to track this as backlog.

### Graceful Degradation

- MM not installed → `MythicMobsHook` not registered, listener not active, spawn calls no-op, warning logged
- MM type ID not found in MM registry → warning logged per binding at startup, binding skipped
- Binding references invalid ability key → warning logged, that loot entry skipped

**Key reference file:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGHooksRegistrar.java` — follow the existing conditional registration pattern.

---

## 3. Generic Fishing Mob Spawn System

### Design: Multi-Mob Pool

Instead of hard-coding the spawn logic to a single "Riptide Guardian" mob ID, the spawn system uses a **weighted mob pool**. When a spawn triggers, a mob is selected from the pool based on configured weights. This allows:

- Adding weaker "scout" mobs at lower fishing thresholds
- Rare guardian spawns mixed with common lesser mobs
- Server owners customizing the mob variety without code changes
- Future content (e.g., seasonal mobs, biome-specific variants)

### Class: `us.eunoians.mcrpg.listener.fishing.FishingMobSpawnTracker`

Listens to `PlayerFishEvent`. Per-player, session-only (no DB persistence).

**Per-player state:**
- `currentSpawnChance: double` (starts at `base-chance`, default 0.0)
- `lastHookLocation: Location` (nullable)
- `activeMobUUIDs: Set<UUID>` (prevents stacking — tracks all spawned mobs, not just one)

**On `PlayerFishEvent` (CAUGHT_FISH / CAUGHT_ENTITY):**
1. If `activeMobUUIDs` is at max capacity → skip
2. Distance from `lastHookLocation` to current hook
3. If ≤ `same-area-range` (10 blocks): increase by `chance-increment` (0.02), cap at `max-chance` (0.35)
4. If > range: decrease by `chance-decrement` (0.05), floor at `base-chance` (0.0)
5. Update `lastHookLocation`
6. Roll against `currentSpawnChance`
7. On success → select mob from weighted pool → call `MythicMobsIntegration.spawnMob(typeId, location)` → add UUID to `activeMobUUIDs`, reset chance
8. On mob death/despawn callback → remove UUID from `activeMobUUIDs`, reset to `post-kill-chance`

**Resets:** Logout → discard all state. World change → null `lastHookLocation` (configurable full reset). Mob death/despawn → remove from `activeMobUUIDs`, reset to `post-kill-chance`.

**Multi-player:** Fully independent per-player. Mob targets its triggering player (stored as MM mob metadata, ties into ThreatTable — see Section 9).

**Anti-cheese:**
- Small movement (1-2 blocks): range of 10 blocks means no help
- Alternating two spots: decrement (0.05) > increment (0.02), net loss
- Large teleports: triggers reset

### Config File Location

**New FileType:** `FISHING_MOB_SPAWN_CONFIG` → `fishing_mob_spawn_configuration.yml`
**New ConfigFile:** `FishingMobSpawnConfigFile` with static Route constants.

- Java wrapper: `src/main/java/us/eunoians/mcrpg/configuration/file/FishingMobSpawnConfigFile.java` (extends `ConfigFile`)
- YAML resource: `src/main/resources/fishing_mob_spawn_configuration.yml`

```yaml
config-version: 1

spawn:
  enabled: true
  base-chance: 0.0
  max-chance: 0.35
  chance-increment-per-catch: 0.02
  chance-decrement-per-catch: 0.05
  same-area-range: 10
  post-kill-chance: 0.0
  reset-on-world-change: true
  spawn-offset-from-hook: 3.0
  spawn-y-offset: 1.0
  max-active-mobs-per-player: 1
  required-biomes: []   # Empty = all biomes allowed
  allowed-worlds: []    # Empty = all worlds allowed

  # Weighted mob pool — on spawn trigger, one mob is selected by weight
  mob-pool:
    - mythicmobs-mob-id: "RiptideGuardian"
      weight: 1
      min-chance-threshold: 0.10  # Only eligible when chance >= this value
    # Future example:
    # - mythicmobs-mob-id: "TideScout"
    #   weight: 3
    #   min-chance-threshold: 0.0
```

---

## 4. Mob Abilities (MythicMobs-side, documentation only)

All abilities are MythicMobs config. McRPG does not execute any of this. Documented for config authoring reference.

### 4.1 Phase Shift
- Teleport behind aggro target when >8 blocks away or out of LoS for 3+ seconds
- CD: 8s. VFX: PORTAL particles, ENDERMAN_TELEPORT sound
- **Anti-cheese:** Counters pillar-up and wall-in

### 4.2 Whirlpool
- AoE zone at target (radius ~4, 5s). 1.5 hearts/sec + Slowness II
- CD: 12s. VFX: spiral WATER_SPLASH + BUBBLE_POP
- **Anti-cheese:** Forces movement, prevents face-tanking

### 4.3 Waterlogged Strike
- Ranged projectile. 3 hearts + Slowness I (4s). Mid-range (5-15 blocks)
- CD: 6s. VFX: DRIP_WATER trail, SPLASH burst
- **Anti-cheese:** Punishes bow kiting

### 4.4 Tsunami Wall
- Particle wall (3 tall, 5 wide, 4s) + knockback/Slowness III on contact. NOT real blocks.
- CD: 15s. Only below 50% HP. VFX: WATER_SPLASH + ENCHANTMENT_TABLE shimmer
- **Anti-cheese:** Blocks retreat, forces engagement

### AI Priority
Phase Shift (unreachable target) > Waterlogged Strike (mid-range) > Whirlpool (close range) > Tsunami Wall (<50% HP) > melee filler. MythicMobs native skill priority + cooldown + conditions handle this.

---

## 5. Skill Book System

### Item Representation

`ItemStack` with PersistentDataContainer tags:

| PDC Key | Type | Value |
|---|---|---|
| `mcrpg:skill_book` | BOOLEAN | `true` (marker) |
| `mcrpg:skill_book_ability` | STRING | Ability NamespacedKey (e.g. `"mcrpg:phase_shift"`) |
| `mcrpg:skill_book_source` | STRING | Source identifier (e.g. `"mcrpg:riptide_guardian"`) |

Material: `ENCHANTED_BOOK`. Localized name/lore via `LocalizationKey`.

**Factory:** `us.eunoians.mcrpg.item.skillbook.SkillBookFactory` — `createSkillBook(Ability, NamespacedKey sourceKey)` → `ItemStack`. Works for any ability from any source.

### Consumption

**Listener:** `us.eunoians.mcrpg.listener.item.OnSkillBookConsumeListener`
**Trigger:** `PlayerInteractEvent` RIGHT_CLICK with skill book in hand.

**Flow:**
1. Check `mcrpg:skill_book` PDC tag on held item
2. Read `mcrpg:skill_book_ability` → get ability NamespacedKey
3. Validate ability exists in `AbilityRegistry`
4. Check `AbilityUnlockedAttribute` → if already unlocked, send message + cancel
5. Fire `SkillBookConsumeEvent` (cancellable)
6. If not cancelled: set `AbilityUnlockedAttribute` true, add to available abilities, remove item, play sound, send message

### Drop Configuration

Handled by the MythicMob binding loot table (see Section 2). Per-ability drop chances, `exclusive-drop` option. Each entry independently enabled/disabled.

### Extensibility

- Future mobs add their own binding loot entries producing skill books
- Quest rewards, NPC trades, world events can use `SkillBookFactory` directly
- `SkillBookConsumeEvent` lets third parties intercept consumption

---

## 6. Player Ability Concepts (Ideas Only — Not Implemented)

These are documented for future iteration when the ability activation system rework is complete. **No code will be written for these abilities.** They may or may not have tiers.

### 6.1 Player Phase Shift (`mcrpg:phase_shift`)
- Teleport behind current attack target during melee. Active ability.
- Possible activation: Ready with right-click sword → attack to teleport + bonus damage.
- Skill book source: Riptide Guardian

### 6.2 Player Whirlpool (`mcrpg:whirlpool`)
- Small AoE zone at player's feet that damages/slows nearby enemies.
- Possible activation: Sneak while holding fishing rod/trident.
- Smaller than mob version (radius ~2-3 vs ~4, shorter duration).
- Skill book source: Riptide Guardian

### 6.3 Player Waterlogged Strike (`mcrpg:waterlogged_strike`)
- Fire a water projectile (particle-trailed snowball) that deals damage + Slowness.
- Possible activation: Right-click fishing rod while not actively fishing.
- Lower damage/slow than mob version.
- Skill book source: Riptide Guardian

### 6.4 Player Tsunami Wall (`mcrpg:tsunami_wall`)
- Summon a particle wall in facing direction that slows/knocks back enemies on contact.
- Possible activation: Ready with trident → sneak to place.
- Smaller than mob version.
- Skill book source: Riptide Guardian

### Integration with Loadout

Once implemented, these abilities should participate in the loadout system — players must slot them, creating meaningful choices between skill abilities and standalone abilities. They are NOT "default abilities" that bypass the loadout.

---

## 7. UnlockCondition System (Design Document — Not Implemented)

### Problem

`UnlockableAbility` currently has:
```java
boolean checkIfAbilityCanBeUnlocked(SkillHolder skillHolder, Skill skill);
int getUnlockLevel();
```

This is tightly coupled to skill-level progression. Skill book abilities, quest rewards, achievements, and other unlock methods don't fit this interface. The GUI also has no standardized way to display diverse unlock conditions to players.

### Proposed Design

**Replace the skill-coupled methods with a flexible `UnlockCondition` system:**

```java
public interface UnlockCondition {
    /** Whether this condition is currently met for the given holder. */
    boolean isMet(@NotNull AbilityHolder holder);

    /** Localized description for GUI display (e.g., "Reach Swords Level 15"). */
    @NotNull Component getDisplayDescription(@NotNull McRPGPlayer player);

    /** Short icon/label for compact GUI display. */
    @NotNull Component getDisplayLabel(@NotNull McRPGPlayer player);

    /** Progress fraction 0.0-1.0 for progress bar rendering (optional). */
    default double getProgress(@NotNull AbilityHolder holder) { return isMet(holder) ? 1.0 : 0.0; }
}
```

**Concrete implementations:**

| Implementation | Condition | Display |
|---|---|---|
| `SkillLevelUnlockCondition` | Skill reaches level N | "Reach Swords Level 15" |
| `SkillBookUnlockCondition` | Always false — unlock happens via book consumption | "Obtain from Riptide Guardian" |
| `QuestCompleteUnlockCondition` | Quest X completed | "Complete quest: Defeat the Guardian" |
| `AchievementUnlockCondition` | Custom achievement flag | "Earn achievement: Deep Sea Hunter" |

**Refactored `UnlockableAbility`:**

```java
public interface UnlockableAbility extends Ability {
    /** The condition(s) that must be met to unlock this ability. */
    @NotNull UnlockCondition getUnlockCondition();

    /** Existing method — unchanged. Checks AbilityUnlockedAttribute. */
    default boolean isAbilityUnlocked(@NotNull AbilityHolder holder) { ... }

    /** Existing method — unchanged. Provides applicable attributes. */
    @NotNull Set<NamespacedKey> getApplicableAttributes();
}
```

The old `checkIfAbilityCanBeUnlocked(SkillHolder, Skill)` and `getUnlockLevel()` are removed. Existing skill-based abilities migrate to `SkillLevelUnlockCondition`.

**GUI integration:** The ability info panel calls `ability.getUnlockCondition().getDisplayDescription(player)` and `getProgress(holder)` to render unlock state regardless of unlock type. This standardizes the display for all current and future unlock methods.

**Migration path:** All existing abilities that implement `UnlockableAbility` would update from `getUnlockLevel()` / `checkIfAbilityCanBeUnlocked()` to returning a `SkillLevelUnlockCondition`. The behavior is identical — this is a refactor, not a behavior change.

---

## 8. Server Restart & Mob Lifecycle

### Problem

What happens to bound MythicMobs on server restart, player logout, or if players try to "store" mobs for future farming?

### Server Restart Behavior

MythicMobs entities **persist across server restarts** — MM stores mob data and re-associates entities on startup. This means:

- Bound mobs survive restarts and continue functioning (MM handles this natively)
- McRPG's `OnMythicMobEventListener` will pick up death/despawn events for these mobs post-restart
- **However**, McRPG's per-player spawn tracking state (`FishingMobSpawnTracker`) is session-only and resets on restart, so the `activeMobUUIDs` set will be empty. This is acceptable — orphaned mobs will still die/despawn normally via MM and the despawn policy below.

### Despawn Policy

Each binding has a configurable despawn policy to prevent mob storage:

```yaml
despawn:
  max-lifetime-seconds: 300    # Auto-despawn after 5 minutes regardless
  despawn-if-no-threat: true   # Despawn if MM ThreatTable empties (all players leave area)
  despawn-no-threat-delay-seconds: 30  # Grace period before despawning on empty threat
```

**Implementation:**
- `max-lifetime-seconds`: On spawn, McRPG schedules a delayed task. If the mob is still alive when it fires, call `MythicMobsIntegration.despawnMob()`. Survives restarts because MM re-fires spawn events.
- `despawn-if-no-threat`: Periodic check (every ~5s) via MM's ThreatTable API. If table is empty for `despawn-no-threat-delay-seconds`, despawn the mob. This handles players logging out, dying, or running away.

### Edge Cases

| Scenario | Behavior |
|---|---|
| Server restart with living guardian | MM persists the mob. On re-spawn event, McRPG re-registers despawn timer. Player spawn tracker resets (acceptable). |
| Player logs out with guardian alive | ThreatTable empties → despawn after grace period. Player's spawn tracker is discarded on logout. |
| Player dies | Respawns, can re-engage. ThreatTable may still have them. If not, grace period applies. |
| Player tries to lure mob to storage | `max-lifetime-seconds` ensures eventual despawn regardless. |
| Multiple players engage | ThreatTable handles targeting. Despawn only if ALL players leave. |

---

## 9. MythicMobs ThreatTable & Aggro Integration

### Existing MM Capability

MythicMobs has a **built-in ThreatTable system** — an MMO-style aggro mechanic. When `ThreatTable: true` is set in the MM mob config:

- MM tracks threat per-entity automatically (damage dealt = threat generated)
- The mob targets the highest-threat entity
- API methods: `addThreat()`, `reduceThreat()`, `taunt()`, `getAllThreatTargets()`, `getTopThreatHolder()`
- The table auto-updates on combat actions

### McRPG Integration (This Pass)

For the initial implementation, McRPG's role with ThreatTable is **minimal and read-only**:

1. **Initial targeting:** When McRPG spawns the mob, store the triggering player's UUID as MM mob metadata. MM's ThreatTable will naturally target this player once combat begins. McRPG may optionally call `addThreat(mob, triggeringPlayer, initialThreat)` to seed aggro.
2. **Despawn checks:** Query `getAllThreatTargets()` to determine if any players are still engaged (see Section 8 despawn policy).
3. **Loot eligibility:** On death, query ThreatTable participants to determine who qualifies for loot drops (basic: anyone in the table).

McRPG does **NOT** implement its own aggro system — MM's ThreatTable is sufficient.

### Future: McRPG Aggro Mechanics (Backlog)

In a future pass, McRPG could layer additional mechanics on top of MM's ThreatTable:

- **Aggro management abilities** — skills that modify threat (taunt, threat reduction, threat transfer)
- **Tank/DPS/Healer role bonuses** — reward players who fulfill roles during mob fights
- **Threat-based loot weighting** — higher threat = better drop chances
- **Threat display** — show aggro meters in the action bar or boss bar

This creates interesting PvE mechanics (aggro management, role specialization) without reimplementing what MM already provides. **Not implemented this pass.**

---

## 10. Custom Events (Extension Points)

| Event | When | Cancellable |
|---|---|---|
| `CustomMobSpawnEvent` | After MM spawns a bound mob | Yes |
| `CustomMobDeathEvent` | Before loot processing | No |
| `CustomMobDespawnEvent` | Before cleanup | No |
| `FishingMobSpawnChanceUpdateEvent` | When probability changes | Yes |
| `SkillBookConsumeEvent` | Before book consumption | Yes |
| `SkillBookDropEvent` | Before book added to drops | Yes |
| Per-ability activate events | Inside `activateAbility()` | Yes |

---

## 11. Future Considerations

- **ModelEngine:** MythicMobs natively supports it. Custom model = config change only.
- **Additional mobs:** Add to the mob pool config. New bindings for loot. Potential: Cavern Golem (mining), Timber Wraith (woodcutting).
- **Fishing skill:** When implemented, guardian spawn chance could scale inversely with Fishing level. Kills could grant Fishing XP.
- **Third-party mobs:** Server owners add bindings via `mythicmob_bindings.yml` and mob pool entries. No code needed.
- **Advanced loot system:** See Section 2.1 — contribution-based rewards (damage, tanking, healing, last hit).
- **Aggro mechanics:** See Section 9 — McRPG abilities that interact with MM ThreatTable.

---

## 12. GitHub Backlog Issues to Cut

The following items are explicitly called out as future work. GitHub issues should be created during or after this HLD is merged:

1. **Advanced Loot System** — Contribution-based rewards (top-N damage, last hit, tank, healer). See Section 2.1.
2. **McRPG Aggro Mechanics** — Abilities that interact with MM ThreatTable (taunt, threat reduction, role bonuses). See Section 9.

---

## 13. LLD Breakdown

This HLD will be broken into the following LLDs for implementation:

| LLD | Sections Covered | Scope |
|---|---|---|
| **LLD-1: MythicMobs Binding System** | Sections 2, 10 (binding events) | Hook, integration, binding registry, config file, event listener |
| **LLD-2: Fishing Mob Spawn System** | Sections 3, 8 (despawn policy) | Spawn tracker, mob pool, despawn scheduling, config file |
| **LLD-3: Skill Book System** | Sections 5, 10 (book events) | Factory, consumption listener, PDC tags, localization keys |
| **LLD-4: UnlockCondition Refactor** | Section 7 | Interface, implementations, migration of existing abilities |
| **LLD-5: Player Abilities** | Section 6 | Deferred until ability system rework — LLD written then |

Each LLD will include class-level design, method signatures, test coverage plan, and config schema.

---

## 14. Key Existing Files

| File | Relevance |
|---|---|
| `bootstrap/McRPGHooksRegistrar.java` | Pattern for MM hook registration |
| `registry/plugin/McRPGPluginHookKey.java` | Hook key registry — add `MYTHICMOBS` key |
| `configuration/FileType.java` | Add new FileType entries |
| `configuration/file/ConfigFile.java` | Base class for new config file wrappers |
| `configuration/file/skill/SkillConfigFile.java` | Reference for config file pattern |
| `configuration/FileManager.java` | Loads all FileType entries at startup |
| `ability/impl/type/UnlockableAbility.java` | Current unlock interface — needs `UnlockCondition` refactor |
| `ability/impl/type/configurable/ConfigurableAbility.java` | Standalone abilities use this |
| `entity/holder/LoadoutHolder.java` | `getAvailableDefaultAbilities()` excludes `UnlockableAbility` |
| `expansion/McRPGExpansion.java` | New content packs registered here |
| `src/main/resources/plugin.yml` | MythicMobs already listed as softdepend |
| `src/main/resources/paper-plugin.yml` | MythicMobs already listed as softdepend |
