# Quest Templates — Integration Guide

This document explains how quest templates work in McRPG: the variable system, condition-based element filtering, objective selection, difficulty scaling, and the full generation lifecycle from template to persisted quest definition.

**Audience:** Plugin developers and AI agents working on quest board templates.

---

## 1. Overview

A `QuestTemplate` is a blueprint that generates unique `QuestDefinition` instances each time the board rotates. Unlike hand-crafted definitions (see [`QUEST-DEFINITIONS.md`](QUEST-DEFINITIONS.md)), templates use **variables**, **conditions**, and **weighted selection** to produce variety.

Templates are loaded from YAML files in `plugins/McRPG/quest-board/templates/` and stored in the `QuestTemplateRegistry`. Templates are organized in skill-based subdirectories: `plugins/McRPG/quest-board/templates/{combat,mining,woodcutting,herbalism,mixed,legendary,land}/`.

---

## 2. Templates vs Definitions

| Aspect | `QuestTemplate` | `QuestDefinition` |
|--------|-----------------|-------------------|
| Source | Template YAML or `ContentExpansion` | Quest YAML, expansion, or template engine output |
| Mutable state | None (immutable blueprint) | None (immutable frame) |
| Variables | Declared, unresolved | Resolved to concrete values |
| Conditions | Present on elements | Absent (filtered during generation) |
| Progress expressions | Raw strings (e.g. `"20*difficulty"`) | Resolved to concrete `long` values |
| Quest key | Template key (e.g. `mcrpg:daily_mining_t1`) | Synthetic key (e.g. `mcrpg:gen_daily_mining_t1_a3f8b2c1`) |

---

## 3. Template YAML Format

Templates live under the `quest-templates:` top-level key:

```yaml
quest-templates:
  mcrpg:daily_mining_t1:
    display-name-route: quests.mcrpg.daily_mining_t1.display-name
    board-eligible: true
    scope: mcrpg:single_player
    supported-rarities:
      - common
      - uncommon
      - rare
      - epic
      - legendary
    rarity-overrides:
      epic:
        difficulty-multiplier: 2.0
        reward-multiplier: 1.5
    variables:
      # ... (see section 4)
    phases:
      # ... (see section 5)
    rewards:
      # ... (see section 6)
    prerequisite:
      min-completions: 5
    display:
      name: "Daily Mining Quest"
```

### Top-level template fields

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `display-name-route` | **yes** | — | Localization route for the generated quest's display name |
| `board-eligible` | no | `true` | Whether the board can select this template |
| `scope` | no | `mcrpg:single_player` | Scope provider key for generated quests |
| `supported-rarities` | **yes** | — | Non-empty list of rarity keys this template supports |
| `rarity-overrides` | no | — | Per-rarity `difficulty-multiplier` and `reward-multiplier` |
| `variables` | no | — | Template variable declarations (see section 4) |
| `phases` | **yes** | — | Template phase definitions |
| `rewards` | no | — | Reward definitions (may use variable expressions) |
| `reward-distribution` | no | — | Distribution config copied to generated definition |
| `prerequisite` | no | — | Condition evaluated before offering to a player |
| `display` | no | — | Inline display overrides for generated quests |

---

## 4. Template Variables

Variables introduce randomness and scaling into generated quests. Two types:

### POOL Variables

Select from weighted pools of values. Used for choosing target blocks, mob types, etc.

```yaml
variables:
  target_blocks:
    type: POOL
    min-selections: 1
    max-selections: 3
    pools:
      ores:
        difficulty: 1.5
        weight:
          common: 10
          rare: 8
          epic: 5
        values:
          - IRON_ORE
          - COPPER_ORE
          - COAL_ORE
      rare_ores:
        difficulty: 3.0
        weight:
          common: 2
          rare: 5
          epic: 10
        values:
          - DIAMOND_ORE
          - EMERALD_ORE
```

**Resolution:** Pools with zero weight for the rolled rarity are excluded. A random selection count in `[min, max]` is chosen. Pools are picked by weighted random without replacement. All `values` lists from selected pools are merged. The average difficulty of selected pools feeds into range variable scaling.

### RANGE Variables

Produce a numeric value within a range, scaled by difficulty.

```yaml
variables:
  block_count:
    type: RANGE
    base:
      min: 10
      max: 30
```

**Resolution:** `uniformRandom(min, max) * poolDifficulty * rarityDifficulty`, rounded to a `Long`.

### Built-in `difficulty` variable

The engine automatically computes and injects a `difficulty` variable:
- `poolDifficulty` = average of per-pool-variable average difficulties (or `1.0` if no pools)
- `rarityDifficulty` = from rarity override or registry
- `difficulty = poolDifficulty * rarityDifficulty`

This can be used in `required-progress` expressions.

### Variable naming

Use **underscores** in variable names (e.g. `block_count`), not hyphens. The `Parser` interprets `-` as subtraction, so `block-count` in an expression would be parsed as `block` minus `count`.

---

## 5. Template Phases, Stages, and Objectives

### Phase fields

| Field | Default | Description |
|-------|---------|-------------|
| `completion-mode` | `ALL` | `ALL` or `ANY` — same as hand-crafted definitions |
| `condition` | — | Optional condition evaluated during generation; phase excluded if `false` |
| `stages` | *(required)* | Map of stage definitions |

### Stage fields

| Field | Default | Description |
|-------|---------|-------------|
| `condition` | — | Optional condition; stage excluded if `false` |
| `objective-selection` | — | Optional subset selection (see section 7) |
| Objective entries | *(required)* | Map of objective definitions |

### Objective fields

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `type` | **yes** | — | Registered `QuestObjectiveType` key |
| `required-progress` | **yes** | — | Integer or `Parser` expression (e.g. `"block_count * difficulty"`) |
| `condition` | no | — | Optional condition; objective excluded if `false` |
| `weight` | no | `1` | Selection weight (used by `WEIGHTED_RANDOM` objective selection) |
| `config` | no | — | Type-specific config; string values matching a variable name are substituted with the resolved value |

**Config variable substitution:** If an objective config value is a string that exactly matches a declared variable name, it is replaced with the resolved value. For example:

```yaml
config:
  blocks: target_blocks    # replaced with the resolved POOL list
```

---

## 6. Template Rewards

Rewards in templates use the same format as hand-crafted quests, with one addition: numeric `amount` fields can be variable expressions, and amounts are scaled by the rarity's `reward-multiplier`.

The template engine scales **both** `amount` and `base-amount` fields by the rarity's `reward-multiplier`. This means `mcrpg:scalable_command` rewards correctly have their `base-amount` scaled at higher rarities, and the `mcrpg:item` reward type's top-level `amount` is also scaled. See [`REWARDS.md`](REWARDS.md) section 6 for the full reward-side perspective.

```yaml
rewards:
  mining_xp:
    type: mcrpg:experience
    skill: MINING
    amount: "block_count * 10"    # expression using a resolved variable
```

---

## 7. Objective Selection

By default all objectives in a stage that pass their conditions are included. The `objective-selection` block enables subset selection:

```yaml
stages:
  mining_stage:
    objective-selection:
      mode: WEIGHTED_RANDOM
      min-count: 2
      max-count: 3
    objectives:
      break_stone:
        type: mcrpg:block_break
        weight: 3
        required-progress: 32
        config:
          blocks: [STONE]
      break_iron:
        type: mcrpg:block_break
        weight: 2
        required-progress: 16
        config:
          blocks: [IRON_ORE]
      break_diamond:
        type: mcrpg:block_break
        weight: 1
        required-progress: 8
        config:
          blocks: [DIAMOND_ORE]
```

| Field | Default | Description |
|-------|---------|-------------|
| `mode` | `ALL` | `ALL` (include everything) or `WEIGHTED_RANDOM` (subset selection) |
| `min-count` | `1` | Minimum objectives to select |
| `max-count` | `min-count` | Maximum objectives to select |

A count is chosen uniformly in `[min-count, max-count]`, capped by the number of candidates that passed their conditions. Objectives are picked by weighted random without replacement, preserving their original definition order in the output.

---

## 8. Conditions on Template Elements

Any phase, stage, or objective can have an optional `condition:` block. During generation, the condition is evaluated against a `ConditionContext` and the element is excluded if it returns `false`.

```yaml
phases:
  rare_phase:
    condition:
      rarity-at-least: rare
    stages:
      # ... only included for rare or rarer rolls
```

See [`CONDITIONS.md`](CONDITIONS.md) for the full condition reference. All built-in and registered custom conditions are available.

---

## 9. Generation Lifecycle

```
1. Load          QuestTemplateConfigLoader → QuestTemplateRegistry
2. Select        QuestPool picks template + rarity (weighted random)
                   → scope-aware filtering (templates filtered by scope-provider match)
                   → prerequisite evaluation (personal: checked against ConditionContext;
                     shared: templates with player-dependent prerequisites excluded)
3. Resolve       QuestTemplateEngine.resolveVariables()
                   → POOL selections + RANGE scaling → ResolvedVariableContext
4. Filter        filterPhases → filterStages → filterObjectives
                   (evaluate conditions, apply objective selection)
5. Build         buildDefinition: resolve expressions, substitute config values,
                   create QuestDefinition with synthetic key
6. Serialize     GeneratedQuestDefinitionSerializer.serialize() → JSON string
7. Store         BoardOffering holds the JSON + definition reference
8. Deserialize   On load/restart: JSON → QuestDefinition via registries
```

**Synthetic key format:** `mcrpg:gen_<template_key_suffix>_<8 hex chars>`

**Deterministic seeding:** Personal offerings use `computeSeed(playerUUID, rotationEpoch, slotIndex)` so the same player sees the same offerings within a rotation.

### 9.1 Scope-Aware Template Filtering

During step 2 of the generation lifecycle, `QuestPool` filters templates by their `scope` field to match the board category's `scope-provider`. This ensures that templates only appear in categories where their scope is valid:

- **Land-scoped templates** (e.g., `scope: mcrpg:land`) only appear in land categories.
- **Personal templates** (e.g., `scope: mcrpg:single_player`) only appear in personal or shared categories.

Templates whose scope does not match the category's scope-provider are excluded before any rarity or weight selection occurs.

### 9.2 Prerequisite Evaluation During Selection

Template-level `prerequisite:` conditions are now evaluated during board offering selection in `QuestPool`, not just at display time.

- **Personal boards:** The player's completion history is available. `ConditionContext.forPrerequisiteCheck(playerUUID, history)` is used, so player-dependent conditions like `min-completions` are evaluated accurately against the specific player's data.
- **Shared boards:** No player context is available during generation (using `forTemplateGeneration`). Templates with player-dependent prerequisites (like `min-completions: 15`) are excluded because the player-dependent conditions return `false` when the player UUID and history fields are null. This is intentional — it prevents prerequisite-gated templates (e.g., legendary templates requiring 15+ quest completions) from appearing on shared boards where no specific player's history can be checked.

---

## 10. Serialization

[`GeneratedQuestDefinitionSerializer`](board/template/GeneratedQuestDefinitionSerializer.java) persists generated definitions as JSON for board offerings. The JSON includes:

- `quest_key`, `template_key`, `rarity_key`, `scope`
- `variables` — full resolved variable map
- `phases` with stages and objectives (type key, required progress, config, rewards)
- `rewards` at each level (type key + `serializeConfig()` output)
- `reward_distribution` tiers
- `inline_display` overrides

On deserialization, objective types use `parseConfig(Section)` via a BoostedYAML bridge, and reward types use `fromSerializedConfig(Map)`. The `TemplateConditionRegistry` is required for deserializing reward fallback conditions.

---

## 11. Registration

Templates can be loaded from YAML or registered programmatically:

**YAML:** Place files in `plugins/McRPG/quest-board/templates/{skill-subdirectory}/` with `quest-templates:` top-level key. Skill-based subdirectories include `combat/`, `mining/`, `woodcutting/`, `herbalism/`, `mixed/`, `legendary/`, and `land/`.

**ContentExpansion:** Return a `QuestTemplateContentPack` from `getExpansionContent()`:

```java
QuestTemplateContentPack pack = new QuestTemplateContentPack(this);
pack.addContent(myTemplate);
return Set.of(pack);
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| [`QuestTemplate`](board/template/QuestTemplate.java) | Immutable template blueprint |
| [`QuestTemplateEngine`](board/template/QuestTemplateEngine.java) | Full generation pipeline |
| [`QuestTemplateRegistry`](board/template/QuestTemplateRegistry.java) | Template storage and lookup |
| [`ResolvedVariableContext`](board/template/ResolvedVariableContext.java) | Resolved variable values + difficulty scalars |
| [`GeneratedQuestResult`](board/template/GeneratedQuestResult.java) | Generation output (definition + JSON) |
| [`GeneratedQuestDefinitionSerializer`](board/template/GeneratedQuestDefinitionSerializer.java) | JSON round-trip |
| [`TemplatePhaseDefinition`](board/template/TemplatePhaseDefinition.java) | Template phase with optional condition |
| [`TemplateStageDefinition`](board/template/TemplateStageDefinition.java) | Template stage with optional condition + selection config |
| [`TemplateObjectiveDefinition`](board/template/TemplateObjectiveDefinition.java) | Template objective with weight and condition |
| [`ObjectiveSelectionConfig`](board/template/ObjectiveSelectionConfig.java) | `ALL` / `WEIGHTED_RANDOM` + count range |
| [`WeightedObjectiveSelector`](board/template/WeightedObjectiveSelector.java) | Weighted subset selection |
| [`PoolVariable`](board/template/variable/PoolVariable.java) | Pool variable resolution |
| [`RangeVariable`](board/template/variable/RangeVariable.java) | Range variable resolution |
| [`QuestTemplateConfigLoader`](../configuration/QuestTemplateConfigLoader.java) | Template YAML parsing |
