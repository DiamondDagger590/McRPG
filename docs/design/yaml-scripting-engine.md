# YAML Scripting Engine for McRPG — Design Document

**Status**: Draft
**Date**: 2026-02-26
**Scope**: Ability-only scripting (skills remain Java-defined)

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Core Interfaces](#3-core-interfaces)
4. [ScriptedAbility Class Hierarchy](#4-scriptedability-class-hierarchy)
5. [YAML Schema](#5-yaml-schema)
6. [Integration Points](#6-integration-points)
7. [Built-in Library](#7-built-in-library)
8. [Validation and Error Handling](#8-validation-and-error-handling)
9. [Hot-Reload Support](#9-hot-reload-support)
10. [Expression System](#10-expression-system)
11. [Migration and Coexistence](#11-migration-and-coexistence)
12. [Concrete Examples](#12-concrete-examples)
13. [Phased Implementation Plan](#13-phased-implementation-plan)
14. [Key Design Decisions](#14-key-design-decisions)

---

## 1. Overview

### Problem

Adding new abilities to McRPG currently requires writing Java classes, understanding the
component architecture (`EventActivatableComponent`, `ReadyingComponent`, etc.), and
rebuilding the plugin. Server administrators cannot create or customize abilities without
developer involvement.

### Solution

A YAML-based scripting engine that allows defining abilities declaratively. The engine
introduces four composable primitives — **Mechanics**, **Conditions**, **Targeters**, and
**Triggers** — that are parsed from YAML at load time and assembled into concrete `Ability`
objects indistinguishable from hand-written Java ones. Scripted abilities flow through the
existing activation pipeline, database persistence, loadout system, and GUI with zero
changes to those systems.

### Key Insight

McRPG already separates "what conditions must hold" (components) from "what happens" (the
`activateAbility` method). The scripting engine makes the `activateAbility` method
data-driven while reusing the existing component system for activation conditions.

### Design Principles

- **Additive only**: No existing files need restructuring. Native abilities remain unchanged.
- **Type-safe**: Concrete subclasses preserve `instanceof` semantics the codebase relies on.
- **Explicit registration**: Factory-based registries, no reflection/classpath scanning.
- **Admin-friendly**: One YAML file per ability, clear error messages, hot-reload support.
- **Extensible**: Third-party plugins can register custom mechanics/conditions/targeters.

---

## 2. Architecture

### Package Structure

All new code lives under a new top-level package. No existing files need restructuring.

```
us.eunoians.mcrpg.scripting/
  ScriptedAbility.java                     -- abstract base for YAML-built abilities
  ScriptedPassiveAbility.java              -- passive variant
  ScriptedActiveAbility.java               -- active (readied) variant
  ScriptedPassiveSkillAbility.java         -- passive + linked to a skill
  ScriptedActiveSkillAbility.java          -- active + linked to a skill (cooldownable, tierable)
  ScriptedAbilityDefinition.java           -- parsed YAML data object
  ScriptedAbilityExpansion.java            -- ContentExpansion for all YAML-loaded abilities
  ScriptedAbilityLoader.java              -- parses YAML files, validates, constructs instances
  ScriptedAbilityReloader.java            -- hot-reload support

us.eunoians.mcrpg.scripting.mechanic/
  Mechanic.java                            -- interface: void execute(MechanicContext)
  MechanicContext.java                     -- carries holder, event, targets, parameters, expressions
  MechanicRegistry.java                    -- String -> Mechanic factory registry
  MechanicEntry.java                       -- groups a mechanic with its targeter and optional condition
  impl/
    DamageMechanic.java
    HealMechanic.java
    PotionEffectMechanic.java
    ParticleMechanic.java
    MessageMechanic.java
    SoundMechanic.java
    IgniteMechanic.java
    CancelEventMechanic.java
    CooldownMechanic.java
    SetActiveMechanic.java
    CommandMechanic.java                   -- Phase 2
    ProjectileMechanic.java                -- Phase 2
    DropMultiplierMechanic.java            -- Phase 2
    DelayMechanic.java                     -- Phase 2
    RepeatMechanic.java                    -- Phase 2
    FireCustomEventMechanic.java           -- Phase 2

us.eunoians.mcrpg.scripting.condition/
  ScriptedCondition.java                   -- interface: boolean evaluate(ConditionContext)
  ConditionContext.java                    -- carries holder, event, expression resolver
  ConditionRegistry.java                   -- String -> Condition factory registry
  impl/
    ChanceCondition.java
    HoldingItemCondition.java
    OnAttackCondition.java
    OnBlockBreakCondition.java
    SkillLevelCondition.java
    AbilityTierCondition.java
    AbilityUnlockedCondition.java
    NotOnCooldownCondition.java
    PermissionCondition.java
    BlockTypeCondition.java
    NaturalBlockCondition.java
    EntityTypeCondition.java
    NotCondition.java                      -- inverts another condition
    AllCondition.java                      -- AND combinator
    AnyCondition.java                      -- OR combinator
    HealthCondition.java                   -- Phase 2
    BiomeCondition.java                    -- Phase 2
    WorldCondition.java                    -- Phase 2
    DaytimeCondition.java                  -- Phase 2
    AbilityActiveCondition.java            -- Phase 2

us.eunoians.mcrpg.scripting.targeter/
  Targeter.java                            -- interface: Collection<Entity> resolve(TargeterContext)
  TargeterContext.java                     -- carries holder, event, source location
  TargeterRegistry.java                    -- String -> Targeter factory registry
  impl/
    SelfTargeter.java                      -- @self
    TargetEntityTargeter.java              -- @target
    NearbyEntitiesTargeter.java            -- @nearby{radius=N}
    EventBlockTargeter.java                -- @block
    NearbyPlayersTargeter.java             -- Phase 2
    LineOfSightTargeter.java               -- Phase 2

us.eunoians.mcrpg.scripting.trigger/
  TriggerType.java                         -- enum mapping YAML names to Bukkit Event classes
  TriggerDefinition.java                   -- parsed trigger with event class + conditions
  TriggerRegistry.java                     -- extensible trigger type registration

us.eunoians.mcrpg.scripting.context/
  ScriptedAbilityContext.java              -- unified context shared across components
  ExpressionResolver.java                  -- wraps Parser with standard variable population

us.eunoians.mcrpg.scripting.bridge/
  ScriptedActivatableComponent.java        -- bridges ScriptedCondition to EventActivatableComponent
```

### File Organization on Disk

```
plugins/McRPG/
  scripted_abilities/                      -- folder scanned recursively on startup
    fire_sword.yml
    ground_slam.yml
    my_custom_abilities/                   -- subfolders supported for organization
      ice_blast.yml
      lightning_strike.yml
```

One YAML file defines exactly one ability.

---

## 3. Core Interfaces

### 3a. Mechanic

Mechanics are the **effects** that execute when an ability activates.

```java
package us.eunoians.mcrpg.scripting.mechanic;

/**
 * A single effect that executes against resolved targets when an ability activates.
 */
public interface Mechanic {

    /**
     * Executes this mechanic's effect.
     *
     * @param context carries the ability holder, triggering event, resolved targets,
     *                YAML parameters, and expression resolver
     */
    void execute(@NotNull MechanicContext context);
}
```

`MechanicContext` carries:
- `AbilityHolder holder` — the player/entity using the ability
- `Event event` — the triggering Bukkit event
- `Collection<? extends Entity> targets` — resolved from the Targeter
- `Map<String, Object> parameters` — parsed from YAML (e.g., `amount`, `duration`)
- `ExpressionResolver expressions` — for evaluating math expressions with variables

### 3b. ScriptedCondition

Conditions gate whether an ability fires (at the trigger level) or whether a specific
mechanic executes (at the mechanic level).

```java
package us.eunoians.mcrpg.scripting.condition;

/**
 * Evaluates whether a condition holds true for a given context.
 */
public interface ScriptedCondition {

    boolean evaluate(@NotNull ConditionContext context);
}
```

`ConditionContext` carries:
- `AbilityHolder holder`
- `Event event`
- `ExpressionResolver expressions`

**Why separate from `EventActivatableComponent`?** The existing `EventActivatableComponent.shouldActivate()`
takes `(AbilityHolder, Event)` which is sufficient for trigger-level conditions. But inline mechanic
conditions need to evaluate against resolved targets or parameters. `ScriptedCondition` has the richer
`ConditionContext`. A bridge adapter (`ScriptedActivatableComponent`) wraps `ScriptedCondition` to work
as an `EventActivatableComponent` for trigger-level use.

### 3c. Targeter

Targeters resolve **who or what** a mechanic affects.

```java
package us.eunoians.mcrpg.scripting.targeter;

/**
 * Resolves a collection of entities (or blocks) that a mechanic will act upon.
 */
public interface Targeter {

    @NotNull
    Collection<? extends Entity> resolve(@NotNull TargeterContext context);
}
```

`TargeterContext` carries:
- `AbilityHolder holder`
- `Event event`
- Source location for area-based targeters

### 3d. Registries

All three registries follow the same factory pattern:

```java
public class MechanicRegistry {
    private final Map<String, Function<Map<String, Object>, Mechanic>> factories = new HashMap<>();

    public void register(@NotNull String name,
                         @NotNull Function<Map<String, Object>, Mechanic> factory) {
        factories.put(name.toLowerCase(), factory);
    }

    public Optional<Mechanic> create(@NotNull String name,
                                     @NotNull Map<String, Object> parameters) {
        var factory = factories.get(name.toLowerCase());
        return factory != null ? Optional.of(factory.apply(parameters)) : Optional.empty();
    }
}
```

Factory functions accept a `Map<String, Object>` of parameters parsed from YAML and return a
configured instance. This allows Java developers to register new mechanics/conditions/targeters
from external plugins during `onEnable()`.

---

## 4. ScriptedAbility Class Hierarchy

McRPG uses `instanceof` checks extensively throughout the codebase:

- `AbilityRegistry.register()` routes to `abilitiesWithSkills` vs `abilitiesWithoutSkills`
  based on `instanceof SkillAbility`
- `AbilityListener.activateAbilities()` checks `instanceof CooldownableAbility`
- `LoadoutHolder.getAvailableDefaultAbilities()` checks `instanceof UnlockableAbility`

A single class implementing all interfaces would break these semantics. Instead, the loader
constructs one of four concrete subclasses based on YAML flags:

```
                     BaseAbility
                         |
                  ScriptedAbility (abstract)
                   /              \
   ScriptedPassiveAbility    ScriptedActiveAbility
                                (implements CooldownableAbility)
                   /              \
  ScriptedPassiveSkillAbility  ScriptedActiveSkillAbility
  (implements SkillAbility,    (implements SkillAbility,
   PassiveAbility)              CooldownableAbility,
                                TierableAbility,
                                UnlockableAbility)
```

### ScriptedAbility (abstract base)

```java
public abstract class ScriptedAbility extends BaseAbility implements ConfigurableAbility {

    protected final ScriptedAbilityDefinition definition;
    protected final List<MechanicEntry> mechanics;

    protected ScriptedAbility(Plugin plugin, ScriptedAbilityDefinition definition) {
        super(plugin, definition.getAbilityKey());
        this.definition = definition;
        this.mechanics = definition.getMechanics();

        // Register activation components from each trigger
        for (TriggerDefinition trigger : definition.getTriggers()) {
            Class<? extends Event> eventClass = trigger.getEventClass();
            for (int i = 0; i < trigger.getConditions().size(); i++) {
                ScriptedCondition condition = trigger.getConditions().get(i);
                addActivatableComponent(
                    new ScriptedActivatableComponent(condition, definition),
                    eventClass,
                    i  // priority
                );
            }
        }
    }

    @Override
    public void activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        ExpressionResolver resolver = new ExpressionResolver(abilityHolder, this, definition);

        for (MechanicEntry entry : mechanics) {
            // Evaluate optional inline condition
            if (entry.getCondition() != null) {
                ConditionContext condCtx = new ConditionContext(abilityHolder, event, resolver);
                if (!entry.getCondition().evaluate(condCtx)) {
                    continue;
                }
            }

            // Resolve targets
            TargeterContext targeterCtx = new TargeterContext(abilityHolder, event);
            Collection<? extends Entity> targets = entry.getTargeter().resolve(targeterCtx);

            // Execute mechanic
            MechanicContext mechCtx = new MechanicContext(
                abilityHolder, event, targets, entry.getParameters(), resolver
            );
            entry.getMechanic().execute(mechCtx);
        }
    }

    @Override
    public NamespacedKey getExpansionKey() {
        return ScriptedAbilityExpansion.EXPANSION_KEY;
    }

    // getDatabaseName(), getDisplayItemBuilder(), etc. delegate to definition
}
```

### Subclass Selection Logic

The `ScriptedAbilityLoader` determines the subclass from YAML:

| `type` | `skill` present? | Subclass |
|--------|-------------------|----------|
| `passive` | no | `ScriptedPassiveAbility` |
| `passive` | yes | `ScriptedPassiveSkillAbility` |
| `active` | no | `ScriptedActiveAbility` |
| `active` | yes | `ScriptedActiveSkillAbility` |

Active skill abilities additionally:
- Register readying components based on the `ready` YAML section
- Implement `CooldownableAbility` with cooldown duration from the `cooldown` mechanic or tier config
- Implement `TierableAbility` using the `tiers` YAML section
- Implement `UnlockableAbility` with unlock level from tier config

---

## 5. YAML Schema

### Full Schema Reference

```yaml
# ============================================================
# REQUIRED FIELDS
# ============================================================

# Unique ability identifier. If no namespace prefix, defaults to "scripted:".
id: "namespace:ability_name"

# Display name, supports MiniMessage formatting.
display-name: "<red>Ability Name"

# Key used for database storage. Must be unique across all abilities.
database-name: "ability_name"

# Behavior type.
#   passive = triggers automatically, no ready required
#   active  = requires ready action before triggering, has cooldown
type: passive | active

# ============================================================
# OPTIONAL FIELDS
# ============================================================

# Link this ability to an existing skill. NamespacedKey of parent skill.
# When set, the ability appears under that skill in loadout/GUI.
skill: "mcrpg:swords"

# Tier configuration (only meaningful when skill is set).
tiers:
  max-tier: 5                          # maximum number of tiers
  tier-values:
    all-tiers:                         # values applied to every tier (overridable)
      cooldown: "240 - (tier * 20)"    # expressions supported
      unlock-level: "50 * tier"
      upgrade-cost: 1
    tier-1:                            # per-tier overrides
      unlock-level: 50
      cooldown: 240
    tier-2:
      unlock-level: 150
      cooldown: 220
    # ...

# ============================================================
# TRIGGERS — when does this ability fire?
# ============================================================
# At least one trigger is required.
triggers:
  - event: entity_damage_by_entity     # maps to a Bukkit event class
    conditions:                        # all must pass for ability to activate
      - holding_item:
          items: [DIAMOND_SWORD, NETHERITE_SWORD]
      - on_attack:
          affects: living_entity
      - chance:
          value: "(swords_level) * 0.033"

# ============================================================
# READY — only for active abilities
# ============================================================
ready:
  type: right_click                    # ready mechanism
  materials: [DIAMOND_PICKAXE, NETHERITE_PICKAXE]
  message: "<green>You ready your pickaxe!"
  unready-message: "<red>Your pickaxe is no longer readied."

# ============================================================
# MECHANICS — what happens when the ability activates
# ============================================================
# Processed in order. At least one mechanic is required.
mechanics:
  - mechanic: damage                   # mechanic name (from MechanicRegistry)
    targeter: "@target"                # who is affected (from TargeterRegistry)
    parameters:                        # mechanic-specific configuration
      amount: "5 + (tier * 2)"         # expressions supported in string values
      ignore-armor: true

  - mechanic: particle
    targeter: "@target"
    parameters:
      type: FLAME
      count: 20

  - mechanic: message
    targeter: "@self"
    parameters:
      text: "<red>Your blade strikes true!"

  # Mechanics can have inline conditions (optional)
  - mechanic: heal
    targeter: "@self"
    condition:                         # only executes if condition passes
      health:
        operator: "<"
        value: 10
    parameters:
      amount: 4

  - mechanic: cooldown                 # for active abilities
    parameters:
      duration: "240 - (tier * 20)"
```

---

## 6. Integration Points

### 6a. ContentExpansion Integration

A new `ScriptedAbilityExpansion` extends `ContentExpansion`. It is registered **after** the
native `McRPGExpansion` during bootstrap.

```java
public final class ScriptedAbilityExpansion extends ContentExpansion {

    public static final NamespacedKey EXPANSION_KEY =
        new NamespacedKey("mcrpg", "scripted-expansion");

    @Override
    public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
        AbilityContentPack pack = new AbilityContentPack(this);
        ScriptedAbilityLoader.loadAll().forEach(pack::addContent);
        return Set.of(pack);
    }
}
```

In `McRPGBootstrap.start()`, after the existing `new McRPGExpansionRegistrar().register(...)`:

```java
contentExpansionManager.registerContentExpansion(new ScriptedAbilityExpansion(mcRPG));
```

Scripted abilities flow through the exact same `ContentHandlerType.ABILITY` processor
and get registered via `AbilityRegistry.register()`.

### 6b. AbilityRegistry Integration

**No changes needed.** `ScriptedAbility` extends `BaseAbility`, so `register()` works as-is.
Because skill-linked subclasses implement `SkillAbility`, the registry's `abilitiesWithSkills`
map is populated automatically.

### 6c. Database Integration

**No schema changes needed.** `AbilityData` and `AbilityAttribute` are already fully generic
and key-based. A `ScriptedAbility` declares its `getApplicableAttributes()` based on YAML flags:

- Always: `ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY`
- If tierable: add `ABILITY_TIER_ATTRIBUTE_KEY`, `ABILITY_QUEST_ATTRIBUTE`, `ABILITY_UNLOCKED_ATTRIBUTE`
- If cooldownable: add `ABILITY_COOLDOWN_ATTRIBUTE_KEY`

The `getDatabaseName()` returns the `database-name` field from YAML. The `SkillDAO` stores
attributes as key-value pairs and is completely generic.

### 6d. Loadout Integration

**No changes needed.** `LoadoutHolder.getAvailableAbilitiesToUse()` already works generically
with `NamespacedKey`. Whether an ability appears in `getAvailableDefaultAbilities()` (non-unlockable)
vs loadout-only (unlockable) is controlled by whether the subclass implements `UnlockableAbility`.

### 6e. Listener Integration

**No new listeners needed.** The existing listeners (`OnAttackAbilityListener`,
`OnBlockBreakListener`, `OnInteractAbilityListener`, etc.) call
`AbilityListener.activateAbilities()` which iterates over all registered abilities for the
holder, checks `canEventActivateAbility()`, and calls `activateAbility()`. Because
`ScriptedAbility` registers its triggers as `EventActivatableComponent` mappings on the
appropriate `Event` classes, it is picked up automatically by the existing pipeline.

### 6f. Condition-to-Component Bridge

The bridge between YAML conditions and the existing component system:

```java
public class ScriptedActivatableComponent implements EventActivatableComponent {

    private final ScriptedCondition condition;
    private final ScriptedAbilityDefinition definition;

    @Override
    public boolean shouldActivate(@NotNull AbilityHolder abilityHolder,
                                  @NotNull Event event) {
        ExpressionResolver resolver = new ExpressionResolver(abilityHolder, definition);
        ConditionContext ctx = new ConditionContext(abilityHolder, event, resolver);
        return condition.evaluate(ctx);
    }
}
```

This allows YAML-defined conditions to integrate seamlessly with
`BaseAbility.checkIfComponentFailsActivation()`, maintaining the existing priority-ordered chain.

---

## 7. Built-in Library

### Phase 1 Mechanics

| Name | Description | Key Parameters |
|------|-------------|----------------|
| `damage` | Deal damage to targets | `amount` (expr), `ignore-armor` (bool) |
| `heal` | Heal targets | `amount` (expr) |
| `potion_effect` | Apply potion effect | `type`, `duration` (expr), `amplifier` (expr) |
| `ignite` | Set targets on fire | `ticks` (expr) |
| `particle` | Spawn particles at target location | `type`, `count` (expr), `offset-x/y/z` |
| `sound` | Play sound at location | `sound`, `volume`, `pitch` |
| `message` | Send MiniMessage text | `text` |
| `cancel_event` | Cancel the triggering Bukkit event | — |
| `cooldown` | Put holder on cooldown | `duration` (expr) |
| `set_active` | Mark ability as "active" for duration | `duration` (expr) |

### Phase 1 Conditions

| Name | Description | Key Parameters |
|------|-------------|----------------|
| `chance` | Random % with expression value | `value` (expr) |
| `holding_item` | Check main hand material | `items` (list) |
| `on_attack` | Validate attacker/target | `affects` (living_entity, player, etc.) |
| `on_block_break` | Validate breaker/block | (implicit from event) |
| `skill_level` | Compare skill level | `skill`, `operator`, `value` (expr) |
| `ability_tier` | Compare current tier | `operator`, `value` |
| `ability_unlocked` | Check if unlocked | `ability` (key) |
| `not_on_cooldown` | Check cooldown not active | — |
| `permission` | Bukkit permission check | `node` |
| `block_type` | Check block material | `blocks` (list) |
| `natural_block` | Check if naturally generated | — |
| `entity_type` | Check target entity type | `types` (list) |
| `not` | Invert another condition | `condition` (nested) |
| `all` | AND combinator | `conditions` (list) |
| `any` | OR combinator | `conditions` (list) |

### Phase 1 Targeters

| Name | Syntax | Description |
|------|--------|-------------|
| Self | `@self` | The ability holder |
| Target | `@target` | The event target entity |
| Nearby | `@nearby{radius=N}` | All living entities within radius |
| Block | `@block` | The event block (block break events) |

### Phase 1 Triggers

| YAML Name | Bukkit Event Class |
|---|---|
| `entity_damage_by_entity` | `EntityDamageByEntityEvent` |
| `block_break` | `BlockBreakEvent` |
| `player_interact` | `PlayerInteractEvent` |
| `player_interact_entity` | `PlayerInteractEntityEvent` |
| `food_level_change` | `FoodLevelChangeEvent` |
| `player_move` | `PlayerMoveEvent` |
| `sneak` | `PlayerToggleSneakEvent` |

### Phase 2 Additions

**Mechanics**: `command`, `projectile`, `drop_multiplier`, `repeat`, `delay`, `fire_custom_event`
**Conditions**: `health`, `biome`, `world`, `daytime`, `ability_active`
**Targeters**: `@nearby_players{radius=N}`, `@line_of_sight{range=N}`

---

## 8. Validation and Error Handling

The `ScriptedAbilityLoader` validates at parse time, not at runtime. Errors are logged
with file name and specific YAML key path.

### Validation Rules

1. **Required fields**: `id`, `display-name`, `database-name`, `type`, at least one
   `trigger`, at least one `mechanic`. Missing any → log error, skip file.

2. **NamespacedKey format**: `id` must be valid. If no colon, prefix with `scripted:`.

3. **Trigger event validation**: Each trigger's `event` must map to a known `TriggerType`.
   Unknown → log warning.

4. **Condition validation**: Each condition name must exist in `ConditionRegistry`.
   Unknown → log warning, treated as "always true".

5. **Mechanic validation**: Each mechanic name must exist in `MechanicRegistry`.
   Unknown → log error, skip entire ability (broken mechanics could cause unexpected behavior).

6. **Expression validation**: All expression strings are pre-parsed with dummy variables
   to catch syntax errors. Invalid → log error with key path.

7. **Skill reference validation**: If `skill` is specified, validate it exists in
   `SkillRegistry`. Not found → log warning (may be registered later by another expansion).

8. **Duplicate ID detection**: Two files with same `id` → log error, skip second file.

### Load Result

```java
public record ScriptedAbilityLoadResult(
    List<ScriptedAbility> abilities,
    List<ValidationError> errors
) {}
```

The bootstrap code can display a startup summary:
```
[McRPG] Scripted Abilities: Loaded 12 abilities from 14 files (2 errors)
[McRPG]   ERROR: fire_sword.yml - Unknown mechanic 'firedamage' at mechanics[0].mechanic
[McRPG]   ERROR: ice_blast.yml - Missing required field 'database-name'
```

---

## 9. Hot-Reload Support

A `/mcrpg reload scripted` command triggers:

1. Iterate over all registered abilities where `ability.getExpansionKey()` equals
   `ScriptedAbilityExpansion.EXPANSION_KEY`.
2. For each, call `AbilityRegistry.unregisterAbility(abilityKey)`.
3. Re-run `ScriptedAbilityLoader.loadAll()`.
4. Re-register all newly loaded abilities via `AbilityRegistry.register()`.

**Safety guarantees:**
- `AbilityRegistry.unregisterAbility()` already exists and handles removal from
  `abilitiesWithSkills`/`abilitiesWithoutSkills`.
- Existing `AbilityData` on players remains in the database keyed by `database-name`.
  If re-registered with the same key, data reconnects seamlessly.
- Active abilities referencing stale instances are cleared during reload.
- Players do NOT need to rejoin or have their data reloaded.

---

## 10. Expression System

All parameter values that are strings are evaluated as mathematical expressions using
the existing `com.diamonddagger590.mccore.parser.Parser`.

### ExpressionResolver

```java
public class ExpressionResolver {

    public double resolve(String expression, AbilityHolder holder,
                          ScriptedAbilityDefinition def) {
        Parser parser = new Parser(expression);

        // Skill-specific variables
        if (holder instanceof SkillHolder sh && def.getSkillKey() != null) {
            sh.getSkillHolderData(def.getSkillKey()).ifPresent(data -> {
                parser.setVariable("skill_level", data.getCurrentLevel());
                // Named variant: e.g., "swords_level"
                String skillName = def.getSkillKey().getKey();
                parser.setVariable(skillName + "_level", data.getCurrentLevel());
            });
        }

        // Tier variable
        if (def.isTierable()) {
            int tier = getCurrentTier(holder);
            parser.setVariable("tier", tier);
        }

        // Generic player variables
        if (holder instanceof Player player) {
            parser.setVariable("max_health",
                player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            parser.setVariable("current_health", player.getHealth());
        }

        return parser.getValue();
    }
}
```

Server admins can write expressions like `"(swords_level) * 0.033 + (tier * 5)"` in any
parameter field.

---

## 11. Migration and Coexistence

### Coexistence Rules

1. **Native abilities remain as Java classes.** Bleed, ExtraOre, SerratedStrikes, etc.
   continue to work exactly as they do today. No changes needed.

2. **Scripted abilities coexist with native abilities** for the same skill. A server admin
   can add a custom "Poison Strike" to the Swords skill alongside native Bleed.

3. **Native abilities are NOT converted.** The scripting engine is for new content creation,
   not replacing existing content.

4. **Scripted abilities can reference native abilities** via conditions like `ability_active`
   (checking if `mcrpg:serrated_strikes` is active) or mechanics that fire events consumed
   by native listeners.

5. **Java developers can extend the scripting engine** by registering new mechanics,
   conditions, or targeters from their own plugins:

```java
// In your plugin's onEnable()
McRPG.getInstance().getScriptingEngine()
    .getMechanicRegistry()
    .register("my_custom_effect", params -> new MyCustomMechanic(params));
```

---

## 12. Concrete Examples

### 12a. Recreating Bleed as YAML

```yaml
id: "scripted:bleed_scripted"
display-name: "<red>Bleed"
database-name: "bleed_scripted"
type: passive
skill: "mcrpg:swords"

triggers:
  - event: entity_damage_by_entity
    conditions:
      - holding_item:
          items:
            - WOODEN_SWORD
            - STONE_SWORD
            - IRON_SWORD
            - DIAMOND_SWORD
            - GOLDEN_SWORD
            - NETHERITE_SWORD
      - on_attack:
          affects: living_entity
      - chance:
          value: "(swords_level) * 0.033"

mechanics:
  - mechanic: potion_effect
    targeter: "@target"
    parameters:
      type: WITHER
      duration: "60"
      amplifier: 0
  - mechanic: particle
    targeter: "@target"
    parameters:
      type: BLOCK_CRACK
      count: 15
  - mechanic: message
    targeter: "@self"
    parameters:
      text: "<red>Your sword causes the target to bleed!"
```

### 12b. Custom Fire Sword

```yaml
id: "scripted:fire_sword"
display-name: "<gold>Fire Sword"
database-name: "fire_sword"
type: passive
skill: "mcrpg:swords"

tiers:
  max-tier: 3
  tier-values:
    tier-1:
      unlock-level: 100
    tier-2:
      unlock-level: 250
    tier-3:
      unlock-level: 400

triggers:
  - event: entity_damage_by_entity
    conditions:
      - holding_item:
          items: [WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD,
                  GOLDEN_SWORD, NETHERITE_SWORD]
      - on_attack:
          affects: living_entity
      - chance:
          value: "(swords_level) * 0.02 + (tier * 5)"

mechanics:
  - mechanic: damage
    targeter: "@target"
    parameters:
      amount: "3 + (tier * 2)"
      ignore-armor: true
  - mechanic: ignite
    targeter: "@target"
    parameters:
      ticks: "60 + (tier * 40)"
  - mechanic: particle
    targeter: "@target"
    parameters:
      type: FLAME
      count: "10 + (tier * 5)"
  - mechanic: sound
    targeter: "@self"
    parameters:
      sound: ENTITY_BLAZE_SHOOT
      volume: 0.5
      pitch: 1.2
```

### 12c. Active Ability with Ready Mechanic

```yaml
id: "scripted:ground_slam"
display-name: "<yellow>Ground Slam"
database-name: "ground_slam"
type: active
skill: "mcrpg:mining"

tiers:
  max-tier: 3
  tier-values:
    all-tiers:
      cooldown: "300 - (tier * 40)"
    tier-1:
      unlock-level: 200
    tier-2:
      unlock-level: 400
    tier-3:
      unlock-level: 600

ready:
  type: right_click
  materials: [WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE,
              DIAMOND_PICKAXE, GOLDEN_PICKAXE, NETHERITE_PICKAXE]
  message: "<yellow>You raise your pickaxe..."
  unready-message: "<gray>You lower your pickaxe."

triggers:
  - event: entity_damage_by_entity
    conditions:
      - on_attack:
          affects: living_entity

mechanics:
  - mechanic: damage
    targeter: "@nearby{radius=5}"
    parameters:
      amount: "4 + (tier * 3)"
  - mechanic: particle
    targeter: "@self"
    parameters:
      type: EXPLOSION
      count: 30
  - mechanic: sound
    targeter: "@self"
    parameters:
      sound: ENTITY_GENERIC_EXPLODE
      volume: 1.0
  - mechanic: cooldown
    parameters:
      duration: "300 - (tier * 40)"
```

---

## 13. Phased Implementation Plan

### Phase 1: Foundation (Weeks 1-3)

**Goal**: A single YAML-defined passive ability can activate and execute mechanics.

1. Create the `scripting` package structure
2. Implement `Mechanic`, `MechanicContext`, `MechanicRegistry` interfaces
3. Implement `ScriptedCondition`, `ConditionContext`, `ConditionRegistry` interfaces
4. Implement `Targeter`, `TargeterContext`, `TargeterRegistry` interfaces
5. Implement `TriggerType` enum with `entity_damage_by_entity` and `block_break`
6. Implement `ExpressionResolver` wrapping `Parser`
7. Implement `ScriptedActivatableComponent` bridge
8. Implement `ScriptedAbility` abstract base and `ScriptedPassiveAbility`
9. Implement `ScriptedAbilityDefinition` data object
10. Implement `ScriptedAbilityLoader` (parse single YAML file)
11. Implement `ScriptedAbilityExpansion`
12. Wire into `McRPGBootstrap`
13. Implement Phase 1 mechanics: `damage`, `heal`, `message`, `particle`, `sound`, `cancel_event`
14. Implement Phase 1 conditions: `chance`, `holding_item`, `on_attack`, `on_block_break`, `entity_type`, `block_type`
15. Implement Phase 1 targeters: `@self`, `@target`, `@block`
16. Test: create a "Fire Sword" YAML ability that deals extra damage on attack

### Phase 2: Active Abilities and Tiers (Weeks 4-5)

**Goal**: YAML abilities can be active (readied), tiered, cooldownable, and unlockable.

1. Implement 3 remaining subclasses (`ScriptedActiveAbility`, `ScriptedPassiveSkillAbility`, `ScriptedActiveSkillAbility`)
2. Add ready-trigger parsing to YAML loader
3. Add tier configuration parsing
4. Implement `cooldown` and `set_active` mechanics
5. Implement `skill_level`, `ability_tier`, `ability_unlocked`, `not_on_cooldown` conditions
6. Implement `@nearby{radius=N}` targeter
7. Implement `potion_effect` and `ignite` mechanics
8. Test: create a "Ground Slam" active ability with cooldown and tiers

### Phase 3: Validation, Hot-Reload, Polish (Weeks 6-7)

**Goal**: Production-ready with error reporting and live reload.

1. Implement comprehensive YAML validation in `ScriptedAbilityLoader`
2. Implement `ScriptedAbilityReloader` for `/mcrpg reload scripted`
3. Add command integration
4. Implement display item support (`getDisplayItemBuilder`, `getItemBuilderPlaceholders`)
5. Implement localization support for scripted ability names/descriptions
6. Ship example YAML files in `scripted_abilities/examples/`
7. Write unit tests

### Phase 4: Extended Library (Weeks 8+)

**Goal**: Expand the mechanic/condition/targeter library.

1. Implement Phase 2 mechanics: `command`, `projectile`, `drop_multiplier`, `repeat`, `delay`
2. Implement Phase 2 targeters: `@line_of_sight`, `@nearby_players`
3. Implement Phase 2 conditions: `health`, `biome`, `world`, `daytime`, `ability_active`
4. Publish API documentation for third-party developers

---

## 14. Key Design Decisions

### Decision 1: Separate ScriptedCondition vs reusing EventActivatableComponent

**Choice**: New `ScriptedCondition` interface bridged to `EventActivatableComponent`.

**Reasoning**: `EventActivatableComponent.shouldActivate()` takes `(AbilityHolder, Event)`
which is too narrow for inline mechanic conditions that need to evaluate against resolved
targets or parameters. The bridge adapter keeps compatibility while allowing richer
condition semantics.

### Decision 2: Concrete subclass hierarchy

**Choice**: 4 concrete subclasses of `ScriptedAbility`.

**Reasoning**: McRPG uses `instanceof` checks extensively (`ability instanceof SkillAbility`,
`ability instanceof CooldownableAbility`, etc.) in `AbilityRegistry.register()`,
`LoadoutHolder.getAvailableDefaultAbilities()`, and `AbilityListener`. A single class
implementing all interfaces would break the semantics of these checks. A dynamic proxy
approach was considered but rejected as harder to debug and unconventional in the Bukkit ecosystem.

### Decision 3: One file per ability

**Choice**: One YAML file defines exactly one ability.

**Reasoning**: Simpler for server admins, easier error reporting with file-level granularity,
cleaner hot-reload (can identify which file changed), and matches MythicMobs convention
that admins are already familiar with.

### Decision 4: Factory-based registries

**Choice**: Factory functions (`Function<Map<String, Object>, Mechanic>`) over
reflection/annotation scanning.

**Reasoning**: Explicit, testable, no classpath scanning overhead, and follows the existing
McRPG pattern of explicit registration (`AbilityRegistry.register()`,
`ContentExpansionManager.registerContentHandler()`).

### Decision 5: No new database tables

**Choice**: Reuse existing `AbilityAttribute` system entirely.

**Reasoning**: `AbilityData` + `AbilityAttribute` is already generic enough to handle any
ability. Scripted abilities declare which attributes they use via `getApplicableAttributes()`,
and the existing `SkillDAO` handles persistence. Adding new tables would create unnecessary
coupling and migration burden.

### Decision 6: Abilities only (not skills)

**Choice**: The scripting engine defines abilities only. Skills (leveling categories with
XP sources) remain Java-defined.

**Reasoning**: Skills involve deep integration with XP gain listeners, level-up logic,
skill holder data management, and GUI skill trees. The complexity of scripting these
outweighs the benefit. Abilities are the unit that server admins most want to customize.
A skill scripting engine could be added in a future iteration if demand warrants it.
