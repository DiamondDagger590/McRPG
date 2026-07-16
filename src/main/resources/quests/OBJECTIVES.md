# Quest Objectives — Integration Guide

This document explains how quest objectives work in McRPG: what built-in types are available, how to configure them in YAML, and how to implement a custom objective type from scratch.

**Audience:** Plugin developers and AI agents working on quest features.

---

## 1. Overview

A quest objective tracks measurable progress toward a concrete goal — "break 64 stone", "kill 20 zombies", etc. Objectives live inside stages, which live inside phases (see [`QUEST-DEFINITIONS.md`](QUEST-DEFINITIONS.md) for the full hierarchy).

Two classes collaborate at runtime:

| Class | Role |
|-------|------|
| [`QuestObjectiveType`](objective/type/QuestObjectiveType.java) | Shared singleton that knows how to parse config, match events, and compute progress deltas. Stateless with respect to any particular player or quest instance. |
| [`QuestObjectiveInstance`](impl/objective/QuestObjectiveInstance.java) | Per-quest runtime counter that tracks current progress, required progress, completion state, and per-player contribution. |

The type does not own player state. It maps `(instance, context) → delta`. The instance owns the counter.

---

## 2. The `QuestObjectiveType` Interface

```java
public interface QuestObjectiveType extends McRPGContent {

    @NotNull NamespacedKey getKey();

    @NotNull QuestObjectiveType parseConfig(@NotNull Section section);

    boolean canProcess(@NotNull QuestObjectiveProgressContext context);

    long processProgress(@NotNull QuestObjectiveInstance instance,
                         @NotNull QuestObjectiveProgressContext context);

    @NotNull default String describeObjective(long requiredProgress) { ... }
}
```

| Method | Purpose |
|--------|---------|
| `getKey()` | Stable `NamespacedKey` identifying this type (e.g. `mcrpg:block_break`). Defined as a `static final` constant on the class. |
| `parseConfig(Section)` | Reads type-specific YAML and returns a **new** configured clone. The base (unconfigured) instance stays in the registry. |
| `canProcess(context)` | Returns `true` if this type understands the given progress context (usually an `instanceof` check). |
| `processProgress(instance, context)` | Returns the progress delta for this event (typically `0` or `1`). Must not mutate the instance directly — the caller applies the returned delta. |
| `describeObjective(requiredProgress)` | English fallback description when no localization entry exists. Override for richer text. |

**Lifecycle:**

1. An unconfigured instance is registered in the `QuestObjectiveTypeRegistry` via `ContentExpansion`.
2. When a quest definition YAML is loaded, `parseConfig(configSection)` clones the type with quest-specific settings.
3. The configured clone is stored on the `QuestObjectiveDefinition`.
4. At runtime, listeners build a `QuestObjectiveProgressContext` and the routing layer calls `canProcess` + `processProgress` on the configured clone.

---

## 3. Built-in Objective Types

McRPG ships nine objective types registered by [`McRPGExpansion`](../expansion/McRPGExpansion.java):

### `mcrpg:block_break`

Tracks block break progress. Supports vanilla materials and custom blocks from McCore-integrated plugins via `CustomBlockWrapper`.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `blocks` | string list | *(empty = any block)* | Vanilla material names (e.g. `DIAMOND_ORE`) or custom block identifiers. |

**Example:**

```yaml
config:
  blocks:
    - STONE
    - COBBLESTONE
    - ANDESITE
```

### `mcrpg:mob_kill`

Tracks mob kill progress. Supports vanilla entity types and custom entities from McCore-integrated plugins via `CustomEntityWrapper`.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `mobs` or `entities` | string list | *(empty = any kill)* | Vanilla entity type names (e.g. `ZOMBIE`) or custom entity identifiers. Either key name is accepted. |

**Example:**

```yaml
config:
  mobs:
    - ZOMBIE
    - SKELETON
    - CREEPER
```

### `mcrpg:skill_level_up`

Tracks when a player gains a level in any skill (or a specific skill). Completes once the player has gained the required number of levels during the life of the quest.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `skill` | namespaced key string | *(empty = any skill)* | Key of the McRPG skill to track (e.g. `mcrpg:mining`). Omit to accept any skill level-up. |
| `levels` | int | `1` | Number of times the player must level up the skill. |

**Example:**

```yaml
config:
  skill: mcrpg:mining
  levels: 3
```

### `mcrpg:skill_target_level`

Tracks whether a player has reached a specific level in a skill. Auto-completes at quest start if the player's current level already meets the target.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `skill` | namespaced key string | *(empty = any skill)* | Key of the McRPG skill to track. |
| `target-level` | int | `1` | The level the player must reach. |

**Example:**

```yaml
config:
  skill: mcrpg:swords
  target-level: 50
```

### `mcrpg:gui_open`

Tracks when a player opens a specific McRPG GUI identified by its `NamespacedKey`. Useful for tutorial quests that guide players through interfaces.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `gui-type` | namespaced key string | *(required)* | The `NamespacedKey` of the `KeyedGui` implementation to detect. Must be a valid, registered `KeyedGui` key. |

**Example:**

```yaml
config:
  gui-type: mcrpg:board
```

> **Note:** `gui-type` must be a valid `NamespacedKey` matching a registered `KeyedGui`. Invalid values are logged as warnings at load time.

### `mcrpg:ability_unlock`

Tracks when a player unlocks a specific ability (or any ability of a given type). Auto-completes at quest start if the player already has a matching ability unlocked (`ABILITY_UNLOCKED_ATTRIBUTE`).

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `ability` | namespaced key string | *(empty = any ability)* | Key of the ability to track (e.g. `mcrpg:bleed`). |
| `ability-type` | string | *(empty = any type)* | Filter by ability type: `ACTIVE`, `PASSIVE`, or `INNATE`. Ignored if `ability` is set. |

**Example:**

```yaml
config:
  ability: mcrpg:bleed

# Or by type:
config:
  ability-type: ACTIVE
```

### `mcrpg:ability_activate`

Tracks how many times a player activates an ability during the life of the quest. Counts each activation (combo trigger for active abilities, event trigger for passives).

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `ability` | namespaced key string | *(empty = any ability)* | Key of the ability to count activations for. |
| `ability-type` | string | *(empty = any type)* | Filter by ability type: `ACTIVE`, `PASSIVE`, or `INNATE`. Ignored if `ability` is set. |

**Example:**

```yaml
required-progress: 25
config:
  ability: mcrpg:bleed

# Or track any active ability:
required-progress: 10
config:
  ability-type: ACTIVE
```

### `mcrpg:loadout_equip`

Tracks when a player equips an ability into their **active** loadout (`LoadoutAbilityChangeEvent` with EQUIP or SWAP). Auto-completes at quest start if the active loadout already contains a matching ability. Progress is not credited for edits to non-active loadout presets.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `ability` | namespaced key string | *(empty = any ability)* | Key of the specific ability to watch for. |
| `ability-type` | string | *(empty = any type)* | Filter by ability type: `ACTIVE`, `PASSIVE`, or `INNATE`. Ignored if `ability` is set. |

**Example:**

```yaml
config:
  ability: mcrpg:deeper_wound
```

### `mcrpg:quest_board_accept`

Tracks when a player accepts a quest from the quest board. Useful for tutorial quests that guide new players to the board.

**Config fields:**

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `board` | namespaced key string | *(empty = any board)* | Key of the specific board to watch (e.g. `mcrpg:default`). Omit to accept from any board. |

**Example:**

```yaml
config:
  board: mcrpg:default
```

---

## 4. YAML Configuration

Objectives are defined inside stages in quest YAML files. Each objective section has the following fields:

| Field | Required | Description |
|-------|----------|-------------|
| `key` | yes | `NamespacedKey` uniquely identifying this objective within the quest (e.g. `mcrpg:break_stone_blocks`). |
| `type` | yes | `NamespacedKey` of a registered `QuestObjectiveType` (e.g. `mcrpg:block_break`). |
| `required-progress` | yes | Integer or `Parser` expression (e.g. `"20*(tier^2)"`). Must resolve to a positive number. |
| `config` | no | Type-specific section passed to `parseConfig()`. Contents depend on the objective type. |
| `rewards` | no | Inline rewards granted on objective completion (see [`REWARDS.md`](REWARDS.md)). |
| `reward-distribution` | no | Distribution rewards for scoped quests (see [`REWARDS.md`](REWARDS.md) section 4b). |

**Full example:**

```yaml
quests:
  mcrpg:example_mining:
    scope: mcrpg:single_player
    expiration: 24h
    rewards:
      mining_xp:
        type: mcrpg:experience
        skill: MINING
        amount: 500
    phases:
      mine_blocks:
        completion-mode: ALL
        stages:
          mine_stone:
            key: mcrpg:mine_stone
            objectives:
              break_stone:
                key: mcrpg:break_stone_blocks
                type: mcrpg:block_break
                required-progress: 64
                config:
                  blocks:
                    - STONE
                    - COBBLESTONE
              break_deepslate:
                key: mcrpg:break_deepslate
                type: mcrpg:block_break
                required-progress: 32
                config:
                  blocks:
                    - DEEPSLATE
```

**Expression-based progress:**

For template-generated quests where progress scales with difficulty, `required-progress` can be a `Parser` expression. Variables are resolved at quest instance creation time:

```yaml
required-progress: "20*(tier^2)"
```

The `tier` variable is provided by the template engine. If the expression references `tier` but no value is supplied, an error is thrown at creation time.

---

## 5. Progress Context System

Each objective type defines its own `QuestObjectiveProgressContext` subclass that wraps the relevant Bukkit event data:

```
QuestObjectiveProgressContext   (abstract base — marker class)
├── BlockBreakQuestContext      wraps BlockBreakEvent
├── MobKillQuestContext         wraps EntityDeathEvent + CustomEntityWrapper
├── SkillLevelQuestContext      wraps SkillGainLevelEvent
├── AbilityActivateQuestContext wraps ability activation
├── LoadoutEquipQuestContext    wraps LoadoutAbilityChangeEvent (equip/swap)
└── QuestBoardAcceptQuestContext wraps board acceptance
```

The context is a simple data carrier. It does not perform any logic — its job is to give the objective type access to the event details it needs.

**`BlockBreakQuestContext`:**

```java
public class BlockBreakQuestContext extends QuestObjectiveProgressContext {
    public BlockBreakQuestContext(@NotNull BlockBreakEvent event) { ... }
    @NotNull public BlockBreakEvent getBlockBreakEvent() { ... }
}
```

**`MobKillQuestContext`:**

```java
public class MobKillQuestContext extends QuestObjectiveProgressContext {
    public MobKillQuestContext(@NotNull EntityDeathEvent event) { ... }
    @NotNull public EntityDeathEvent getDeathEvent() { ... }
    @NotNull public CustomEntityWrapper getEntityWrapper() { ... }
}
```

Custom objective types define their own context subclass (see section 9 for a worked example).

---

## 6. Listener Routing

Progress is driven by Bukkit event listeners that implement the [`QuestProgressListener`](../listener/quest/QuestProgressListener.java) interface. The interface provides one key default method: `progressQuests`.

**The routing flow:**

```
Bukkit Event
  → Concrete listener (e.g. BlockBreakQuestProgressListener)
    → Builds a QuestObjectiveProgressContext
    → Calls progressQuests(playerUUID, context)
      → For each active quest for the player
        → For each active stage
          → For each IN_PROGRESS objective
            → Loads QuestObjectiveDefinition from the quest definition
            → Calls type.canProcess(context)
            → If true: delta = type.processProgress(objective, context)
            → If delta > 0: objective.progress(delta, playerUUID)
```

**Concrete listeners are minimal.** Here is the entire `BlockBreakQuestProgressListener`:

```java
public class BlockBreakQuestProgressListener implements QuestProgressListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        progressQuests(event.getPlayer().getUniqueId(), new BlockBreakQuestContext(event));
    }
}
```

All the fan-out logic lives in the `progressQuests` default method — concrete listeners only need to:

1. Listen for the right Bukkit event
2. Extract the player UUID
3. Build the context
4. Call `progressQuests`

**Events fired during progress:**

| Event | When | Cancellable? | Mutable? |
|-------|------|-------------|----------|
| [`QuestObjectiveProgressEvent`](../event/quest/QuestObjectiveProgressEvent.java) | Before progress is applied to the instance | Yes | `progressDelta` can be modified |
| [`QuestObjectiveCompleteEvent`](../event/quest/QuestObjectiveCompleteEvent.java) | After an objective reaches its required progress | No | — |

`QuestObjectiveProgressEvent` is the only cancellable quest event. External plugins can listen for it to block progress (e.g., anti-cheat validation) or modify the delta (e.g., double-progress events).

**Completion cascade:**

When `QuestObjectiveCompleteEvent` fires, the [`QuestObjectiveCompleteListener`](../listener/quest/QuestObjectiveCompleteListener.java) handles:

1. Granting objective-level rewards (inline + distribution)
2. Checking if the parent stage is now complete (`stage.checkForUpdatedStatus()`)
3. If so, granting stage-level rewards and calling `stage.complete()`

---

## 7. Localization (Objective Descriptions)

Objective descriptions follow a three-level fallback chain:

1. **Locale route** — `quests.{namespace}.{quest_key}.objectives.{objective_key}.description` resolved through the player's locale chain
2. **Inline display** — `display.objectives.<key>` from the quest YAML (a literal string)
3. **Auto-generated** — `objectiveType.describeObjective(requiredProgress)` (English-only fallback)

**Locale file example** (`en.yml` or `en_quest.yml`):

```yaml
quests:
  mcrpg:
    example_mining:
      objectives:
        break_stone_blocks:
          description: "<gray>Break Stone & Cobblestone"
        break_deepslate:
          description: "<gray>Break Deepslate"
```

**Inline display example** (in the quest YAML):

```yaml
display:
  objectives:
    break_stone_blocks: "<gray>Break Stone & Cobblestone"
```

The `getDescription` method on `QuestObjectiveDefinition` handles the fallback chain automatically.

---

## 8. Registering a Custom Objective Type

Adding a custom objective type requires three pieces:

1. **A `QuestObjectiveType` implementation** — defines the type, config parsing, and progress logic
2. **A `QuestObjectiveProgressContext` subclass** — wraps the Bukkit event data
3. **A `QuestProgressListener` implementation** — listens for Bukkit events and calls `progressQuests`

Then register the type via `ContentExpansion` and the listener via Bukkit's plugin manager.

**Step 1: Implement the type**

```java
public class FishingObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY =
            new NamespacedKey("myplugin", "fishing");

    private final Set<Material> validFish;

    public FishingObjectiveType() {
        this.validFish = Set.of();
    }

    private FishingObjectiveType(@NotNull Set<Material> validFish) {
        this.validFish = validFish;
    }

    @NotNull @Override
    public NamespacedKey getKey() { return KEY; }

    @NotNull @Override
    public FishingObjectiveType parseConfig(@NotNull Section section) {
        Set<Material> fish = Set.of();
        if (section.contains("fish-types")) {
            fish = section.getStringList("fish-types").stream()
                    .map(s -> Material.matchMaterial(s.toUpperCase()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());
        }
        return new FishingObjectiveType(fish);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof FishingQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof FishingQuestContext fishContext)) {
            return 0;
        }
        if (validFish.isEmpty()) {
            return 1;
        }
        ItemStack caught = fishContext.getCaughtItem();
        return caught != null && validFish.contains(caught.getType()) ? 1 : 0;
    }

    @NotNull @Override
    public String describeObjective(long requiredProgress) {
        if (validFish.isEmpty()) {
            return "Catch " + requiredProgress + " fish";
        }
        return "Catch " + requiredProgress + " specific fish";
    }

    @NotNull @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(MyPluginExpansion.EXPANSION_KEY);
    }
}
```

**Step 2: Create the progress context**

```java
public class FishingQuestContext extends QuestObjectiveProgressContext {

    private final PlayerFishEvent fishEvent;

    public FishingQuestContext(@NotNull PlayerFishEvent fishEvent) {
        this.fishEvent = fishEvent;
    }

    @Nullable
    public ItemStack getCaughtItem() {
        if (fishEvent.getCaught() instanceof Item item) {
            return item.getItemStack();
        }
        return null;
    }

    @NotNull
    public PlayerFishEvent getFishEvent() {
        return fishEvent;
    }
}
```

**Step 3: Create the listener**

```java
public class FishingQuestProgressListener implements QuestProgressListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        progressQuests(event.getPlayer().getUniqueId(), new FishingQuestContext(event));
    }
}
```

**Step 4: Register via `ContentExpansion`**

```java
public class MyPluginExpansion extends ContentExpansion {

    public static final NamespacedKey EXPANSION_KEY =
            new NamespacedKey("myplugin", "myplugin-expansion");

    public MyPluginExpansion() {
        super(EXPANSION_KEY);
    }

    @NotNull @Override
    public Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent() {
        QuestObjectiveTypeContentPack pack = new QuestObjectiveTypeContentPack(this);
        pack.addContent(new FishingObjectiveType());
        return Set.of(pack);
    }

    // ... getExpansionName, etc.
}
```

**Step 5: Register the listener in your plugin's `onEnable`**

```java
@Override
public void onEnable() {
    // Register the expansion (handles type registration automatically)
    McRPG.getInstance().registryAccess()
            .registry(RegistryKey.MANAGER)
            .manager(McRPGManagerKey.EXPANSION)
            .registerExpansion(new MyPluginExpansion());

    // Register the listener (must be done separately)
    getServer().getPluginManager().registerEvents(
            new FishingQuestProgressListener(), this);
}
```

**Step 6: Use it in quest YAML**

```yaml
objectives:
  catch_salmon:
    key: myplugin:catch_salmon
    type: myplugin:fishing
    required-progress: 10
    config:
      fish-types:
        - SALMON
        - COD
```

---

## 9. Common Pitfalls

**Forgetting to register the listener.** The type alone doesn't drive progress. Without a listener that constructs the context and calls `progressQuests`, the objective will never advance. The type and the listener are always a pair.

**Mutating the instance in `processProgress`.** The method should return a delta, not call `instance.progress()` directly. The routing layer in `progressQuests` handles calling `progress()` and firing the `QuestObjectiveProgressEvent`. Calling it yourself bypasses the event pipeline and contribution tracking.

**Returning wrong delta for wrong context type.** Always guard `processProgress` with an `instanceof` check matching `canProcess`. If `canProcess` returns `true` but `processProgress` receives an unexpected context subclass, return `0` rather than throwing.

**Expression-based progress without `tier` variable.** If `required-progress` uses a `Parser` expression containing `tier`, the variable must be supplied at quest instance creation time. Template-generated quests handle this automatically; manually created quests must provide it in the variables map.

**Not using `MONITOR` priority on listeners.** Quest progress listeners should run at `EventPriority.MONITOR` with `ignoreCancelled = true` so they only fire after all other plugins have had a chance to cancel the underlying event.

---

## Key Files Reference

| File | Purpose |
|------|---------|
| [`QuestObjectiveType`](objective/type/QuestObjectiveType.java) | Extensibility contract |
| [`QuestObjectiveProgressContext`](objective/type/QuestObjectiveProgressContext.java) | Event payload base class |
| [`QuestObjectiveTypeRegistry`](objective/type/QuestObjectiveTypeRegistry.java) | Type registry |
| [`BlockBreakObjectiveType`](objective/type/builtin/BlockBreakObjectiveType.java) | Built-in block break type |
| [`MobKillObjectiveType`](objective/type/builtin/MobKillObjectiveType.java) | Built-in mob kill type |
| [`SkillLevelUpObjectiveType`](objective/type/builtin/SkillLevelUpObjectiveType.java) | Built-in skill level-up type |
| [`SkillTargetLevelObjectiveType`](objective/type/builtin/SkillTargetLevelObjectiveType.java) | Built-in skill target level type |
| [`GuiOpenObjectiveType`](objective/type/builtin/GuiOpenObjectiveType.java) | Built-in GUI open type |
| [`AbilityUnlockObjectiveType`](objective/type/builtin/AbilityUnlockObjectiveType.java) | Built-in ability unlock type |
| [`AbilityActivateObjectiveType`](objective/type/builtin/AbilityActivateObjectiveType.java) | Built-in ability activate type |
| [`LoadoutEquipObjectiveType`](objective/type/builtin/LoadoutEquipObjectiveType.java) | Built-in loadout equip type |
| [`QuestBoardAcceptObjectiveType`](objective/type/builtin/QuestBoardAcceptObjectiveType.java) | Built-in quest board accept type |
| [`AbilityObjectiveFilter`](objective/type/builtin/AbilityObjectiveFilter.java) | Shared filter for ability-based objective types |
| [`BlockBreakQuestContext`](objective/type/builtin/BlockBreakQuestContext.java) | Block break context |
| [`MobKillQuestContext`](objective/type/builtin/MobKillQuestContext.java) | Mob kill context |
| [`SkillLevelQuestContext`](objective/type/builtin/SkillLevelQuestContext.java) | Skill level-up context |
| [`AbilityActivateQuestContext`](objective/type/builtin/AbilityActivateQuestContext.java) | Ability activation context |
| [`QuestObjectiveDefinition`](definition/QuestObjectiveDefinition.java) | YAML definition frame + description resolution |
| [`QuestObjectiveInstance`](impl/objective/QuestObjectiveInstance.java) | Runtime counter + completion logic |
| [`QuestProgressListener`](../listener/quest/QuestProgressListener.java) | Fan-out routing interface |
| [`BlockBreakQuestProgressListener`](../listener/quest/BlockBreakQuestProgressListener.java) | Bukkit entry point for block breaks |
| [`MobKillQuestProgressListener`](../listener/quest/MobKillQuestProgressListener.java) | Bukkit entry point for mob kills |
| [`QuestObjectiveProgressEvent`](../event/quest/QuestObjectiveProgressEvent.java) | Cancellable progress event |
| [`QuestObjectiveCompleteEvent`](../event/quest/QuestObjectiveCompleteEvent.java) | Completion event |
| [`QuestObjectiveCompleteListener`](../listener/quest/QuestObjectiveCompleteListener.java) | Reward granting + stage advancement |
| [`QuestObjectiveTypeContentPack`](../expansion/content/QuestObjectiveTypeContentPack.java) | Expansion pack for types |
| [`McRPGExpansion`](../expansion/McRPGExpansion.java) | Registers built-in types |

---

## Persisting structured progress state (custom objective types)

The scalar progress counter cannot express set-like objectives ("visit 5
*distinct* biomes", "kill one of each boss"). A custom `QuestObjectiveType` can
persist arbitrary structured state on the objective instance via
`QuestObjectiveInstance.getCustomData()` — a `Map<String, Object>` serialized to
a JSON column and restored on load.

```java
@Override
public long processProgress(QuestObjectiveInstance instance, QuestObjectiveProgressContext context) {
    String biome = ((BiomeVisitContext) context).getBiomeKey();
    @SuppressWarnings("unchecked")
    List<String> visited = (List<String>) instance.getCustomData()
            .computeIfAbsent("visited_biomes", key -> new ArrayList<String>());
    if (visited.contains(biome)) {
        return 0; // already counted — revisiting does not progress
    }
    visited.add(biome);
    instance.markCustomDataDirty(); // flag the change for persistence
    return 1;
}
```

Mutate the map in place and call `markCustomDataDirty()` (or replace it wholesale
with `setCustomData(...)`, which marks the quest dirty for you). Custom data
round-trips through save → restart → load. Keep values JSON-friendly (strings,
numbers, booleans, lists, maps).

**Numbers come back as `Double`.** Custom data is persisted with Gson into an
untyped `Map<String, Object>`, so after a save/load cycle a stored `5` reads back
as `5.0` (`Double`) and an integer list as a `List<Double>`. Read numbers through
`Number` — e.g. `((Number) instance.getCustomData().get("count")).longValue()` —
rather than casting straight to `Integer`/`Long`, which throws `ClassCastException`
on a reloaded quest.
