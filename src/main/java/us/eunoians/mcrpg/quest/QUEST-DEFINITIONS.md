# Quest Definitions — Integration Guide

This document explains how quest definitions are structured in McRPG: the YAML format, the definition hierarchy, repeat modes, board metadata, and how definitions are loaded and looked up at runtime.

**Audience:** Plugin developers and AI agents working on quest configuration or the quest framework.

---

## 1. Overview

A `QuestDefinition` is an immutable frame describing a quest's structure, rewards, scope, and repeat rules. Multiple `QuestInstance`s can be created from the same definition — the definition holds no per-player state.

Definitions are loaded from YAML files in two directories: `plugins/McRPG/quests/` (non-board quests) and `plugins/McRPG/quest-board/quests/` (hand-crafted board quests). Any `.yml` or `.yaml` file in either directory (including subdirectories) is scanned automatically. Quest identity comes from the YAML key under `quests:`, not the file name.

---

## 2. Definition Hierarchy

```
QuestDefinition
├── phases: List<QuestPhaseDefinition>         (ordered, at least one)
│   ├── completionMode: PhaseCompletionMode    (ALL or ANY)
│   ├── rewardDistribution: optional
│   └── stages: List<QuestStageDefinition>
│       ├── stageKey: NamespacedKey
│       ├── rewards: List<QuestRewardType>
│       ├── rewardDistribution: optional
│       └── objectives: List<QuestObjectiveDefinition>
│           ├── objectiveKey: NamespacedKey
│           ├── objectiveType: QuestObjectiveType (configured)
│           ├── requiredProgress: long or expression
│           ├── rewards: List<QuestRewardType>
│           └── rewardDistribution: optional
├── rewards: List<QuestRewardEntry>
├── rewardDistribution: optional
├── scopeType: NamespacedKey
├── expiration: Duration (optional)
├── repeatMode: QuestRepeatMode
├── metadata: Map<NamespacedKey, QuestDefinitionMetadata>
└── inlineDisplay: Map<String, String>
```

**Key structural rules:**
- Phases are ordered — they are completed sequentially
- All objectives in a stage must complete for the stage to finish
- `completion-mode` on a phase controls stages: `ALL` requires every stage; `ANY` completes when the first stage finishes (remaining stages are cancelled)

---

## 3. YAML Format

### Quest-level fields

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `scope` | no | `mcrpg:single_player` | `NamespacedKey` of the `QuestScopeProvider` to use |
| `expiration` | no | *(no expiry)* | Duration string (see section 5) |
| `repeat-mode` | no | `ONCE` | `ONCE`, `REPEATABLE`, `COOLDOWN`, `LIMITED`, or `COOLDOWN_LIMITED` |
| `repeat-cooldown` | no | — | Duration string; used with `COOLDOWN` and `COOLDOWN_LIMITED` modes |
| `repeat-limit` | no | — | Integer; used with `LIMITED` and `COOLDOWN_LIMITED` modes |
| `rewards` | no | — | Map of labeled reward entries (see [`REWARDS.md`](REWARDS.md)) |
| `reward-distribution` | no | — | Distribution tiers for scoped quests (see [`REWARDS.md`](REWARDS.md) section 4b) |
| `phases` | **yes** | — | Non-empty map of labeled phase entries |
| `board-metadata` | no | — | Board eligibility and configuration (see section 7) |
| `display` | no | — | Inline display overrides (see section 8) |
| `expansion` | no | — | `NamespacedKey` of the owning expansion |

### Phase-level fields

| Field | Default | Description |
|-------|---------|-------------|
| `completion-mode` | `ALL` | `ALL` or `ANY` — controls stage completion logic |
| `stages` | *(required)* | Non-empty map of labeled stage entries |
| `reward-distribution` | — | Optional distribution tiers |

Note: phases do **not** support an inline `rewards` list. Only `reward-distribution` is available at the phase level.

### Stage-level fields

| Field | Required | Description |
|-------|----------|-------------|
| `key` | **yes** | `NamespacedKey` uniquely identifying this stage |
| `objectives` | **yes** | Non-empty map of labeled objective entries |
| `rewards` | no | Inline rewards granted on stage completion |
| `reward-distribution` | no | Distribution tiers for stage-level rewards |

### Objective-level fields

| Field | Required | Description |
|-------|----------|-------------|
| `key` | **yes** | `NamespacedKey` uniquely identifying this objective |
| `type` | **yes** | `NamespacedKey` of a registered `QuestObjectiveType` |
| `required-progress` | **yes** | Integer or `Parser` expression string (e.g. `"20*(tier^2)"`) |
| `config` | no | Type-specific section passed to `objectiveType.parseConfig()` |
| `rewards` | no | Inline rewards granted on objective completion |
| `reward-distribution` | no | Distribution tiers for objective-level rewards |

---

## 4. Complete YAML Example

```yaml
quests:
  mcrpg:example_branching:
    scope: mcrpg:single_player
    expiration: 7d
    repeat-mode: COOLDOWN
    repeat-cooldown: 24h
    rewards:
      diamond_reward:
        type: mcrpg:command
        commands:
          - "give {player} diamond 3"
        display: "3 Diamonds"
    phases:
      # Phase 0: Mine basic ores (all objectives required)
      mine_basics:
        completion-mode: ALL
        stages:
          mine_iron:
            key: mcrpg:mine_iron
            objectives:
              break_iron:
                key: mcrpg:break_iron_ore
                type: mcrpg:block_break
                required-progress: 16
                config:
                  blocks:
                    - IRON_ORE
                    - DEEPSLATE_IRON_ORE

      # Phase 1: Choose specialty (ANY = first stage to finish wins)
      choose_specialty:
        completion-mode: ANY
        stages:
          mine_diamonds:
            key: mcrpg:mine_diamonds
            rewards:
              diamond_xp:
                type: mcrpg:experience
                skill: MINING
                amount: 250
            objectives:
              break_diamond:
                key: mcrpg:break_diamond_ore
                type: mcrpg:block_break
                required-progress: 8
                config:
                  blocks:
                    - DIAMOND_ORE
                    - DEEPSLATE_DIAMOND_ORE
          mine_emeralds:
            key: mcrpg:mine_emeralds
            rewards:
              emerald_xp:
                type: mcrpg:experience
                skill: MINING
                amount: 250
            objectives:
              break_emerald:
                key: mcrpg:break_emerald_ore
                type: mcrpg:block_break
                required-progress: 8
                config:
                  blocks:
                    - EMERALD_ORE
                    - DEEPSLATE_EMERALD_ORE
```

---

## 5. Duration Format

The `expiration`, `repeat-cooldown`, and `acceptance-cooldown` fields accept:

- Plain integer → **seconds** (e.g. `3600`)
- Component format → any combination of `d` (days), `h` (hours), `m` (minutes), `s` (seconds)
  - `24h`, `7d`, `1d12h30m`, `30m`, `90s`
  - Case-insensitive

---

## 6. Repeat Modes

| Mode | Behavior | Required fields |
|------|----------|-----------------|
| `ONCE` | One completion per player, ever | — |
| `REPEATABLE` | No restriction — can be completed any number of times | — |
| `COOLDOWN` | Can be repeated after `repeat-cooldown` has elapsed since last completion | `repeat-cooldown` |
| `LIMITED` | Can be repeated up to `repeat-limit` times per player | `repeat-limit` |
| `COOLDOWN_LIMITED` | Can be repeated up to `repeat-limit` times total, and each repeat requires `repeat-cooldown` to elapse since the last completion. Both constraints must be satisfied. | `repeat-cooldown`, `repeat-limit` |

Enforcement happens in `QuestManager.canPlayerStartQuest()` which checks the completion log in the database.
Missing required fields for `COOLDOWN_LIMITED` are not fatal — the loader logs a warning and falls back to safe defaults (zero cooldown, limit of 1).

---

## 7. Board Metadata

The optional `board-metadata` block controls how the quest definition interacts with the quest board system:

```yaml
board-metadata:
  board-eligible: true
  supported-rarities:
    - common
    - uncommon
    - rare
    - epic
    - legendary
  supported-refresh-types:
    - DAILY
    - WEEKLY
  acceptance-cooldown: 12h
  cooldown-scope: PLAYER
```

| Field | Default | Description |
|-------|---------|-------------|
| `board-eligible` | `true` | Whether the board can select this definition as an offering |
| `supported-rarities` | *(all registered)* | Which rarities this definition can appear under (5-tier default: common, uncommon, rare, epic, legendary). Bare keys default to `mcrpg:` namespace |
| `supported-refresh-types` | *(all)* | Which rotation types can include this definition (e.g. `DAILY`, `WEEKLY`) |
| `acceptance-cooldown` | — | Duration before the same player can accept this quest again from the board |
| `cooldown-scope` | — | `GLOBAL`, `PLAYER`, or `SCOPE_ENTITY`. `GLOBAL` makes the cooldown apply server-wide — useful for legendary quests where acceptance should be gated for all players |

---

## 8. Inline Display Overrides

The optional `display` block provides literal fallback strings when localization entries are missing:

```yaml
display:
  name: "Mining Challenge"
  description: "Complete mining objectives for rewards"
  objectives:
    break_stone_blocks: "<gray>Break Stone & Cobblestone"
    break_deepslate: "<gray>Break Deepslate"
```

**Fallback chain for quest name:** localization route → inline `display.name` → formatted key
**Fallback chain for objective descriptions:** localization route → inline `display.objectives.<key>` → `objectiveType.describeObjective()`

---

## 9. NamespacedKey Format

Keys throughout quest YAML use `namespace:key` format:
- `mcrpg:daily_mining` — fully qualified
- `daily_mining` — bare key, auto-namespaced to `mcrpg:`

Keys are lowercased during parsing. Use underscores, not hyphens, in key paths (hyphens can conflict with `Parser` expressions that interpret `-` as subtraction).

---

## 10. Loading and Registration

Quest definitions are loaded by [`QuestConfigLoader`](../configuration/QuestConfigLoader.java) from all `.yml`/`.yaml` files in two directories:

1. `plugins/McRPG/quests/` — non-board quests (upgrades, examples, manually granted quests)
2. `plugins/McRPG/quest-board/quests/` — hand-crafted board quests

Both directories are scanned recursively, sorted by path.

**Loading rules:**
- Each file must have a top-level `quests:` map
- Each entry under `quests:` is a quest definition keyed by its `NamespacedKey`
- Duplicate keys across files: warning logged, first loaded wins
- Parse errors: warning logged, quest skipped (other quests still load)

**Runtime registry:** [`QuestDefinitionRegistry`](definition/QuestDefinitionRegistry.java) accessed via `McRPGRegistryKey.QUEST_DEFINITION`.

**Lookup:**

```java
// Via QuestManager
QuestManager qm = RegistryAccess.registryAccess()
        .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
Optional<QuestDefinition> def = qm.getQuestDefinition(questKey);

// Direct registry access
QuestDefinitionRegistry registry = RegistryAccess.registryAccess()
        .registry(McRPGRegistryKey.QUEST_DEFINITION);
Optional<QuestDefinition> def = registry.get(questKey);
```

**Reload:** `/mcrpg admin reload` calls `QuestManager.loadQuestDefinitions()` which clears and replaces all config-loaded definitions. Expansion-registered definitions are re-added by their content handlers.

---

## Key Files Reference

| File | Purpose |
|------|---------|
| [`QuestDefinition`](definition/QuestDefinition.java) | Immutable quest frame |
| [`QuestPhaseDefinition`](definition/QuestPhaseDefinition.java) | Phase: stages + completion mode |
| [`QuestStageDefinition`](definition/QuestStageDefinition.java) | Stage: key, objectives, rewards |
| [`QuestObjectiveDefinition`](definition/QuestObjectiveDefinition.java) | Objective: key, type, progress, rewards |
| [`PhaseCompletionMode`](definition/PhaseCompletionMode.java) | `ALL` / `ANY` enum |
| [`QuestRepeatMode`](definition/QuestRepeatMode.java) | `ONCE`, `REPEATABLE`, `COOLDOWN`, `LIMITED` |
| [`QuestDefinitionRegistry`](definition/QuestDefinitionRegistry.java) | Runtime registry |
| [`QuestDefinitionMetadata`](definition/QuestDefinitionMetadata.java) | Extensible metadata interface |
| [`BoardMetadata`](board/BoardMetadata.java) | Board-specific metadata |
| [`QuestConfigLoader`](../configuration/QuestConfigLoader.java) | YAML parsing and loading |
| [`QuestManager`](QuestManager.java) | Definition management, quest lifecycle |
