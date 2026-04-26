# McRPG Quests — Server Owner Guide

This guide walks you through creating and configuring quests for your server. No coding knowledge required — everything is done by editing YAML files and running a reload command.

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [Your First Quest](#2-your-first-quest)
3. [How Quests Are Structured](#3-how-quests-are-structured)
4. [Objective Types](#4-objective-types)
5. [Reward Types](#5-reward-types)
6. [Multi-Step Quests (Phases)](#6-multi-step-quests-phases)
7. [Branching Quests (Choose Your Own Path)](#7-branching-quests-choose-your-own-path)
8. [Time Limits](#8-time-limits)
9. [Repeatable Quests](#9-repeatable-quests)
10. [Display Names and Descriptions](#10-display-names-and-descriptions)
11. [The Quest Board](#11-the-quest-board)
12. [Board Settings (board.yml)](#12-board-settings-boardyml)
13. [Rarities](#13-rarities)
14. [Board Categories](#14-board-categories)
15. [Hand-Crafted Board Quests](#15-hand-crafted-board-quests)
16. [Template Quests (Auto-Generated Variety)](#16-template-quests-auto-generated-variety)
17. [Advanced Templates](#17-advanced-templates)
18. [Tips and Troubleshooting](#18-tips-and-troubleshooting)

---

## 1. Quick Start

**Where quest files live:**

```
plugins/McRPG/
├── quests/                              ← Non-board quest files
│   ├── example_quest.yml
│   └── upgrades/                        ← Ability upgrade quests (system-managed)
├── quest-board/
│   ├── board.yml                        ← Board settings (rarities, rotation, limits)
│   ├── categories/                      ← Slot categories (one file per category)
│   │   ├── shared-daily.yml
│   │   ├── shared-weekly.yml
│   │   ├── personal-daily.yml
│   │   ├── personal-weekly.yml
│   │   ├── land-daily.yml
│   │   └── land-weekly.yml
│   ├── templates/                       ← Template quest blueprints
│   │   ├── combat/                      ← Combat templates
│   │   ├── mining/                      ← Mining templates
│   │   ├── woodcutting/                 ← Woodcutting templates
│   │   ├── herbalism/                   ← Herbalism templates
│   │   ├── mixed/                       ← Multi-skill and advanced templates
│   │   ├── legendary/                   ← Legendary templates
│   │   └── land/                        ← Land-scoped templates
│   └── quests/                          ← Hand-crafted board quests
│       ├── daily/                       ← Daily board quests
│       ├── weekly/                      ← Weekly board quests
│       ├── land/                        ← Land-scoped board quests
│       └── legendary/                   ← Legendary board quests
```

**How to reload after making changes:**

Run `/mcrpg quest admin reload` in-game or from the console. You do not need to restart the server.

**File format:** All quest files use `.yml` extension (YAML). Any `.yml` or `.yaml` file placed inside the `quests/` or `quest-board/quests/` folders (including subfolders) is loaded automatically. You can organize files however you want — create subfolders by theme, skill, difficulty, etc.

---

## 2. Your First Quest

Create a new file at `plugins/McRPG/quests/my_first_quest.yml` and paste this:

```yaml
quests:
  mcrpg:my_first_quest:
    scope: mcrpg:single_player
    expiration: 24h
    rewards:
      coal_reward:
        type: mcrpg:experience
        skill: MINING
        amount: 100
    phases:
      main:
        completion-mode: ALL
        stages:
          mine_stone:
            key: mcrpg:my_mine_stone
            objectives:
              break_stone:
                key: mcrpg:my_break_stone
                type: mcrpg:block_break
                required-progress: 32
                config:
                  blocks:
                    - STONE
                    - COBBLESTONE
```

Run `/mcrpg quest admin reload` and the quest is live.

**What each part means:**

| Line | What it does |
|------|-------------|
| `quests:` | Required. Tells McRPG this file contains quest definitions. |
| `mcrpg:my_first_quest:` | The unique ID for your quest. The `mcrpg:` part is the namespace (use `mcrpg:` for your own quests). |
| `scope: mcrpg:single_player` | This quest is for individual players (not groups). |
| `expiration: 24h` | Players have 24 hours to finish the quest. |
| `rewards:` | What the player gets for completing the entire quest. |
| `type: mcrpg:experience` | This reward gives McRPG skill XP. |
| `phases:` | The steps of the quest (explained in section 3). |
| `completion-mode: ALL` | All objectives must be completed. |
| `key: mcrpg:my_mine_stone` | Unique ID for this stage. Must be unique across all quests. |
| `type: mcrpg:block_break` | This objective tracks block breaking. |
| `required-progress: 32` | The player needs to break 32 blocks. |
| `blocks:` | Which block types count toward the objective. |

---

## 3. How Quests Are Structured

Every quest has this hierarchy:

```
Quest
└── Phase 1 (must finish before Phase 2 starts)
    └── Stage A
        ├── Objective 1  ← ALL objectives must complete
        └── Objective 2
└── Phase 2
    └── Stage B
        └── Objective 3
```

**Phases** are completed in order — Phase 2 doesn't start until Phase 1 is done.

**Stages** within a phase can run at the same time. Whether ALL stages must finish or just ANY one depends on the `completion-mode` (more on this in section 7).

**Objectives** within a stage always ALL need to be completed.

For simple quests, you only need one phase with one stage. The hierarchy gives you flexibility for complex multi-step quests when you want it.

---

## 4. Objective Types

These are the actions players can do to make progress on a quest.

### Block Breaking — `mcrpg:block_break`

Tracks when a player breaks specific block types.

```yaml
objectives:
  mine_ores:
    key: mcrpg:mine_ores
    type: mcrpg:block_break
    required-progress: 64
    config:
      blocks:
        - IRON_ORE
        - DEEPSLATE_IRON_ORE
        - GOLD_ORE
        - DEEPSLATE_GOLD_ORE
```

**Block names** use the Minecraft material names (same names you see in `/give` commands). Common ones:

| Category | Block names |
|----------|-------------|
| Stone | `STONE`, `COBBLESTONE`, `ANDESITE`, `DIORITE`, `GRANITE` |
| Deepslate | `DEEPSLATE`, `COBBLED_DEEPSLATE` |
| Coal | `COAL_ORE`, `DEEPSLATE_COAL_ORE` |
| Iron | `IRON_ORE`, `DEEPSLATE_IRON_ORE` |
| Copper | `COPPER_ORE`, `DEEPSLATE_COPPER_ORE` |
| Gold | `GOLD_ORE`, `DEEPSLATE_GOLD_ORE` |
| Lapis | `LAPIS_ORE`, `DEEPSLATE_LAPIS_ORE` |
| Redstone | `REDSTONE_ORE`, `DEEPSLATE_REDSTONE_ORE` |
| Diamond | `DIAMOND_ORE`, `DEEPSLATE_DIAMOND_ORE` |
| Emerald | `EMERALD_ORE`, `DEEPSLATE_EMERALD_ORE` |
| Nether | `ANCIENT_DEBRIS`, `NETHER_GOLD_ORE`, `NETHER_QUARTZ_ORE` |
| Wood | `OAK_LOG`, `BIRCH_LOG`, `SPRUCE_LOG`, `DARK_OAK_LOG`, `JUNGLE_LOG`, `ACACIA_LOG`, `CHERRY_LOG`, `MANGROVE_LOG` |
| Crops | `WHEAT`, `CARROTS`, `POTATOES`, `BEETROOTS`, `NETHER_WART` |

### Mob Killing — `mcrpg:mob_kill`

Tracks when a player kills specific mob types.

```yaml
objectives:
  slay_zombies:
    key: mcrpg:slay_zombies
    type: mcrpg:mob_kill
    required-progress: 20
    config:
      mobs:
        - ZOMBIE
        - HUSK
        - DROWNED
```

**Mob names** use the Minecraft entity type names. Common ones:

| Category | Mob names |
|----------|-----------|
| Undead | `ZOMBIE`, `HUSK`, `DROWNED`, `SKELETON`, `STRAY`, `WITHER_SKELETON`, `PHANTOM` |
| Spiders | `SPIDER`, `CAVE_SPIDER` |
| Overworld | `CREEPER`, `WITCH`, `SLIME`, `ENDERMAN`, `PILLAGER`, `VINDICATOR`, `RAVAGER` |
| Nether | `BLAZE`, `PIGLIN_BRUTE`, `HOGLIN`, `GHAST`, `MAGMA_CUBE`, `ZOMBIFIED_PIGLIN` |
| End | `ENDERMAN`, `SHULKER`, `ENDERMITE` |
| Animals | `PIG`, `COW`, `SHEEP`, `CHICKEN`, `RABBIT` |

---

## 5. Reward Types

Rewards are given when a quest (or stage, or objective) is completed.

### Skill Experience — `mcrpg:experience`

Gives the player McRPG skill XP.

```yaml
rewards:
  mining_xp:
    type: mcrpg:experience
    skill: MINING
    amount: 500
```

**Available skills:** `MINING`, `SWORDS`, `WOODCUTTING`, `HERBALISM` (and any others added by your McRPG configuration).

### Run a Command — `mcrpg:command`

Runs one or more console commands when the reward is granted. This is the most flexible reward type — you can give items, money, titles, or anything else your server supports.

```yaml
rewards:
  diamond_bonus:
    type: mcrpg:command
    display: "3 Diamonds"
    commands:
      - "give {player} diamond 3"
```

- `{player}` is automatically replaced with the player's name.
- `display` is what shows up in the quest GUI so the player knows what they're getting. This is a label, not the command itself.
- You can list multiple commands — they all run in order.

**Examples:**

```yaml
# Give items
commands:
  - "give {player} diamond 5"

# Give money (requires an economy plugin like EssentialsX)
commands:
  - "eco give {player} 1000"

# Give a permission (requires LuckPerms or similar)
commands:
  - "lp user {player} permission set some.cool.perk"

# Multiple commands at once
commands:
  - "give {player} diamond 3"
  - "eco give {player} 500"
  - "title {player} subtitle \"Quest Complete!\""
```

### Scalable Command — `mcrpg:scalable_command`

Like a regular command reward, but the amount can be scaled by the quest board's rarity system (more on this in section 16). The `{amount}` placeholder is replaced with the calculated number.

```yaml
rewards:
  scaled_diamonds:
    type: mcrpg:scalable_command
    display: "Bonus Diamonds"
    command: "give {player} diamond {amount}"
    base-amount: 3
```

For a Common rarity quest with `reward-multiplier: 1.0`, the player gets 3 diamonds. For a Rare quest with `reward-multiplier: 2.5`, the player gets 8 diamonds. This only matters for board quests with rarities.

### Item Reward — `mcrpg:item`

Gives the player a custom item with optional enchantments, name, lore, and glowing effect.

```yaml
rewards:
  enchanted_pick:
    type: mcrpg:item
    item:
      material: DIAMOND_PICKAXE
      amount: 1
      name: "<gold>Miner's Pickaxe"
      lore:
        - "<gray>A reward for dedicated miners"
      enchantments:
        efficiency: 3
        unbreaking: 2
      glowing: true
```

**Item fields:**

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `material` | yes | — | Minecraft material name (e.g. `DIAMOND_PICKAXE`, `GOLDEN_APPLE`) |
| `amount` | no | 1 | Stack size (not scaled by rarity) |
| `name` | no | — | Custom display name (supports MiniMessage formatting) |
| `lore` | no | — | List of lore lines (supports MiniMessage) |
| `enchantments` | no | — | Map of enchantment name to level (e.g. `efficiency: 3`) |
| `glowing` | no | false | Whether the item has an enchantment shimmer |
| `custom-model-data` | no | — | Custom model data for resource packs |

For template quests, you can add a top-level `amount` field that scales with rarity:

```yaml
rewards:
  scaled_diamonds:
    type: mcrpg:item
    amount: 5
    item:
      material: DIAMOND
```

At Common rarity (1.0x reward), the player gets 5 diamonds. At Rare (2.5x), they get 13.

### Where Rewards Can Go

Rewards can be attached at four levels:

| Level | When granted | Use for |
|-------|-------------|---------|
| Quest-level (`rewards:` at the top) | When the entire quest is completed | Main quest rewards |
| Phase-level | When all stages in that phase are done | Mid-quest milestone rewards between phases |
| Stage-level | When all objectives in that stage are done | Bonus rewards for completing a specific stage |
| Objective-level | When that specific objective is done | Small milestone rewards |

```yaml
quests:
  mcrpg:multi_reward_example:
    # ... quest-level reward (given at the very end)
    rewards:
      big_reward:
        type: mcrpg:experience
        skill: MINING
        amount: 1000
    phases:
      main:
        completion-mode: ALL
        # ... phase-level reward (given when all stages in this phase are done)
        rewards:
          phase_bonus:
            type: mcrpg:experience
            skill: MINING
            amount: 500
        stages:
          stage_one:
            key: mcrpg:stage_one
            # ... stage-level reward (given when this stage finishes)
            rewards:
              stage_bonus:
                type: mcrpg:experience
                skill: MINING
                amount: 200
            objectives:
              mine_iron:
                key: mcrpg:mine_iron
                type: mcrpg:block_break
                required-progress: 16
                # ... objective-level reward (given as soon as this objective completes)
                rewards:
                  small_bonus:
                    type: mcrpg:command
                    display: "Iron Ingot"
                    commands:
                      - "give {player} iron_ingot 1"
                config:
                  blocks:
                    - IRON_ORE
```

---

## 6. Multi-Step Quests (Phases)

Phases let you create quests with sequential steps. Phase 2 only becomes active after Phase 1 is fully completed.

```yaml
quests:
  mcrpg:two_step_quest:
    scope: mcrpg:single_player
    expiration: 7d
    rewards:
      final_reward:
        type: mcrpg:experience
        skill: MINING
        amount: 500
    phases:
      # Step 1: Mine iron first
      step_one:
        completion-mode: ALL
        # Phase reward: granted when Step 1 completes (before Step 2 activates)
        rewards:
          step_one_bonus:
            type: mcrpg:experience
            skill: MINING
            amount: 100
        stages:
          mine_iron:
            key: mcrpg:step1_mine_iron
            objectives:
              break_iron:
                key: mcrpg:step1_break_iron
                type: mcrpg:block_break
                required-progress: 16
                config:
                  blocks:
                    - IRON_ORE
                    - DEEPSLATE_IRON_ORE

      # Step 2: Then mine diamonds (only starts after Step 1 is done)
      step_two:
        completion-mode: ALL
        stages:
          mine_diamonds:
            key: mcrpg:step2_mine_diamonds
            objectives:
              break_diamond:
                key: mcrpg:step2_break_diamond
                type: mcrpg:block_break
                required-progress: 8
                config:
                  blocks:
                    - DIAMOND_ORE
                    - DEEPSLATE_DIAMOND_ORE
```

---

## 7. Branching Quests (Choose Your Own Path)

Setting `completion-mode: ANY` on a phase means the player only needs to complete **one** of the stages in that phase. The other stages are automatically cancelled once one finishes. This lets you create "choose your path" quests.

```yaml
quests:
  mcrpg:choose_your_specialty:
    scope: mcrpg:single_player
    expiration: 24h
    rewards:
      xp_reward:
        type: mcrpg:experience
        skill: MINING
        amount: 250
    phases:
      # Phase 1: Normal objectives everyone does
      warmup:
        completion-mode: ALL
        stages:
          mine_basics:
            key: mcrpg:warmup_mine
            objectives:
              break_stone:
                key: mcrpg:warmup_break_stone
                type: mcrpg:block_break
                required-progress: 32
                config:
                  blocks:
                    - STONE

      # Phase 2: Choose mining OR combat (player picks by doing one)
      choose_path:
        completion-mode: ANY
        stages:
          # Option A: Mine diamonds
          mining_path:
            key: mcrpg:mining_path
            rewards:
              mining_bonus:
                type: mcrpg:experience
                skill: MINING
                amount: 100
            objectives:
              mine_diamonds:
                key: mcrpg:choose_mine_diamonds
                type: mcrpg:block_break
                required-progress: 5
                config:
                  blocks:
                    - DIAMOND_ORE

          # Option B: Kill zombies
          combat_path:
            key: mcrpg:combat_path
            rewards:
              combat_bonus:
                type: mcrpg:experience
                skill: SWORDS
                amount: 100
            objectives:
              kill_zombies:
                key: mcrpg:choose_kill_zombies
                type: mcrpg:mob_kill
                required-progress: 15
                config:
                  mobs:
                    - ZOMBIE
```

**Quick summary:**

| Completion Mode | Meaning |
|----------------|---------|
| `ALL` | Every stage in the phase must be completed |
| `ANY` | Only one stage needs to finish (the rest are cancelled) |

---

## 8. Time Limits

The `expiration` field sets how long a player has to complete the quest after accepting it. If time runs out, the quest expires and progress is lost.

```yaml
expiration: 24h      # 24 hours
expiration: 7d       # 7 days
expiration: 1d12h    # 1 day and 12 hours
expiration: 30m      # 30 minutes
expiration: 3600     # 3600 seconds (1 hour)
```

Leave out `expiration` entirely if you want the quest to never expire.

Supported units: `d` (days), `h` (hours), `m` (minutes), `s` (seconds). You can combine them: `1d12h30m`.

---

## 9. Repeatable Quests

By default, a quest can only be completed once per player. You can change this with `repeat-mode`:

```yaml
quests:
  mcrpg:daily_grind:
    repeat-mode: REPEATABLE    # Can be done unlimited times
    # ...

  mcrpg:weekly_challenge:
    repeat-mode: COOLDOWN      # Can repeat after a cooldown
    repeat-cooldown: 7d        # Must wait 7 days between completions
    # ...

  mcrpg:special_quest:
    repeat-mode: LIMITED       # Can only be done a set number of times
    repeat-limit: 3            # Maximum 3 completions per player, ever
    # ...
```

| Mode | Behavior |
|------|----------|
| `ONCE` (default) | One completion per player, ever |
| `REPEATABLE` | No limits — can be completed any number of times |
| `COOLDOWN` | Can repeat, but only after `repeat-cooldown` time has passed since last completion |
| `LIMITED` | Can be completed up to `repeat-limit` times total |

---

## 10. Display Names and Descriptions

The `display` block lets you set the name, description, and objective/reward labels that players see in the quest GUI:

```yaml
quests:
  mcrpg:ore_rush:
    display:
      name: "Ore Rush"
      description: "Mine precious ores for big rewards!"
      objectives:
        break_gold: "Mine gold ore"
        break_diamond: "Mine diamond ore"
      rewards:
        mining_xp: "Mining XP"
        gem_bonus: "Bonus Diamonds"
    # ...
```

- `name` — The quest title shown in the GUI
- `description` — A short description of the quest
- `objectives.<label>` — What each objective is called (the label must match the objective's map key, NOT the `key:` field)
- `rewards.<label>` — What each reward is called (the label must match the reward's map key)

If you don't set display values, McRPG will auto-generate names from the quest key (e.g. `mcrpg:daily_stone_haul` becomes "Daily Stone Haul").

---

## 11. The Quest Board

The quest board is a rotating menu of quest offerings that players can browse and accept. It refreshes on a schedule (daily, weekly, etc.) and can show a mix of hand-crafted quests and randomly generated template quests.

**How it works from the player's perspective:**

1. Player opens the quest board GUI
2. They see a list of available quests with names, rarities, and descriptions
3. They click to accept a quest
4. The quest appears in their active quests and they can start working on it
5. When the board rotates (e.g. at midnight), old unclaimed quests are replaced with new ones

**Two types of board quests:**

| Type | Description | When to use |
|------|-------------|-------------|
| **Hand-crafted** | Quests you write by hand with fixed objectives and rewards | When you want specific, predictable quests |
| **Template-generated** | Blueprints that generate unique quests each rotation using randomized variables | When you want infinite variety without writing hundreds of quests |

---

## 12. Board Settings (board.yml)

The file `plugins/McRPG/quest-board/board.yml` controls the overall board behavior:

```yaml
# How many quest slots appear on the board at minimum
slot-layout:
  minimum-total-offerings: 3

# How many board quests a player can have active at once
max-accepted-quests: 3

# How often hand-crafted vs template quests appear
# Higher weight = more likely. 50/50 means equal chance.
quest-source-weights:
  hand-crafted: 50
  template: 50

# When the board refreshes
rotation:
  time: "00:00"            # Time of day for daily rotation (24-hour format)
  timezone: "UTC"          # Your server's timezone (e.g., "America/New_York")
  weekly-reset-day: MONDAY # Which day weekly quests rotate
  task-check-interval-seconds: 60

# Player notifications
notifications:
  near-expiry-threshold-minutes: 60      # Warn players this many minutes before quest expires
  progress-thresholds: [25, 50, 75]      # Notify at these % milestones (e.g., "50% complete!")
```

---

## 13. Rarities

Rarities are tiers that affect how hard a quest is and how good the rewards are. They're configured in `board.yml`:

```yaml
rarities:
  COMMON:
    weight: 50                  # Most likely to appear
    difficulty-multiplier: 1.0  # Normal difficulty
    reward-multiplier: 1.0      # Normal rewards
    display:
      material: PAPER           # Item icon in the GUI
      name-color: "<white>"     # Name color

  UNCOMMON:
    weight: 29
    difficulty-multiplier: 1.25 # 25% harder
    reward-multiplier: 1.5      # 50% better rewards
    display:
      material: WRITABLE_BOOK
      name-color: "<green>"

  RARE:
    weight: 15
    difficulty-multiplier: 1.75 # 75% harder
    reward-multiplier: 2.5      # 2.5x rewards
    display:
      material: ENCHANTED_BOOK
      name-color: "<aqua>"
      settings:
        glowing: true           # Enchantment shimmer on the item

  EPIC:
    weight: 5
    difficulty-multiplier: 2.5  # 2.5x difficulty
    reward-multiplier: 4.0      # 4x rewards
    display:
      material: AMETHYST_SHARD
      name-color: "<dark_purple>"
      settings:
        glowing: true

  LEGENDARY:
    weight: 1                   # Extremely rare
    difficulty-multiplier: 3.5  # 3.5x difficulty
    reward-multiplier: 6.0      # 6x rewards
    display:
      material: NETHER_STAR
      name-color: "<gold>"
      settings:
        glowing: true
```

**What the multipliers do:**
- `difficulty-multiplier` scales objective targets in template quests. A template that normally asks for 32 blocks will ask for 112 at `3.5` difficulty (Legendary).
- `reward-multiplier` scales reward amounts in template quests. A template that normally gives 100 XP will give 600 XP at `6.0` reward multiplier (Legendary).
- `weight` controls how likely each rarity is to be rolled. Higher number = more common. In the example above, ~50% of quests will be Common, ~1% will be Legendary.

These multipliers only apply to **template** quests. Hand-crafted quests use their exact configured values regardless of rarity.

**You can add, remove, or rename rarities however you want.** Want a MYTHIC tier? Just add it:

```yaml
  MYTHIC:
    weight: 1
    difficulty-multiplier: 3.0
    reward-multiplier: 5.0
    display:
      material: DRAGON_EGG
      name-color: "<light_purple>"
      settings:
        glowing: true
```

---

## 14. Board Categories

Categories control **how many slots** appear on the board and **what type** of quests fill them. They're configured in `plugins/McRPG/quest-board/categories/` (one file per category).

**Default 6 categories:**

There are three visibility types:
- **SHARED** — all players see the same offerings (used by `shared-daily`, `shared-weekly`)
- **PERSONAL** — each player gets unique offerings generated from a player-specific seed (used by `personal-daily`, `personal-weekly`)
- **SCOPED** — offerings are generated per scope entity such as a Lands territory (used by `land-daily`, `land-weekly`)

**Example: `shared-daily.yml`**

```yaml
shared-daily:
  visibility: SHARED            # Everyone sees the same quests
  refresh-type: DAILY           # Refreshes every day
  refresh-interval: 1d
  completion-time: 1d           # Players get 1 day to complete after accepting
  scope-provider: mcrpg:single_player
  min: 3                        # At least 3 daily quest slots
  max: 5                        # Up to 5 daily quest slots
  chance-per-slot: 1.0          # 100% chance each slot is filled
  priority: 10                  # Higher priority fills first
```

**Example: `personal-daily.yml`**

```yaml
personal-daily:
  visibility: PERSONAL          # Unique quests per player
  refresh-type: DAILY
  refresh-interval: 1d
  completion-time: 1d
  scope-provider: mcrpg:single_player
  min: 2
  max: 3
  chance-per-slot: 1.0
  priority: 8
```

**Example: `land-weekly.yml`**

```yaml
land-weekly:
  visibility: SCOPED            # Per scope entity (e.g. per land/territory)
  refresh-type: WEEKLY
  refresh-interval: 7d
  completion-time: 7d
  scope-provider: mcrpg:land_scope    # Quests scoped to Lands territories
  min: 1
  max: 2
  chance-per-slot: 0.8
  priority: 3
```

**Key settings:**

| Setting | What it does |
|---------|-------------|
| `visibility` | `SHARED` = same for everyone, `PERSONAL` = unique per player, `SCOPED` = per group (e.g. per land) |
| `refresh-type` | `DAILY` or `WEEKLY` — which rotation schedule to follow |
| `scope-provider` | Which scope provider to use. Land categories use `mcrpg:land_scope` |
| `min` / `max` | Range of quest slots this category contributes |
| `chance-per-slot` | 0.0 to 1.0 — probability each slot actually generates a quest |
| `priority` | Higher priority categories get their slots first |
| `appearance-cooldown` | How long before the same quest can appear again (e.g. `7d`) |
| `required-permission` | Optional permission node — only players with this permission see these quests |

---

## 15. Hand-Crafted Board Quests

To make one of your hand-written quests appear on the quest board, add a `board-metadata` block:

```yaml
quests:
  mcrpg:daily_stone_haul:
    display:
      name: "Stone Haul"
      description: "Mine a stack of stone types"
    scope: mcrpg:single_player
    expiration: 24h

    # This block makes the quest eligible for the board
    board-metadata:
      board-eligible: true
      supported-rarities:
        - mcrpg:common
        - mcrpg:uncommon
      supported-refresh-types:
        - DAILY

    rewards:
      mining_xp:
        type: mcrpg:experience
        skill: MINING
        amount: 200
    phases:
      dig:
        completion-mode: ALL
        stages:
          mine_stone:
            key: mcrpg:daily_mine_stone
            objectives:
              break_stone:
                key: mcrpg:daily_break_stone
                type: mcrpg:block_break
                required-progress: 64
                config:
                  blocks:
                    - STONE
                    - COBBLESTONE
```

**Board metadata settings:**

| Setting | Default | What it does |
|---------|---------|-------------|
| `board-eligible` | `true` | Set to `false` to temporarily remove from the board without deleting |
| `supported-rarities` | all rarities | Which rarities this quest can appear under. Use this to control difficulty tiers — easy quests for Common/Uncommon, hard quests for Rare/Legendary |
| `supported-refresh-types` | all types | `DAILY`, `WEEKLY`, or both. Controls which rotation this quest can appear in |
| `acceptance-cooldown` | none | How long before the same player can accept this quest again (e.g. `12h`) |

**Remember:** Hand-crafted board quests use their exact configured values. The rarity's difficulty/reward multipliers do NOT affect them — those only affect template quests. The rarity is used for display purposes (icon, color, slot filtering).

### Legendary One-Time Quests

For special quests that should only be completable once per player and have a server-wide cooldown before they can appear again, combine `repeat-mode: ONCE` with a global acceptance cooldown:

```yaml
quests:
  mcrpg:legendary_dragon_slayer:
    repeat-mode: ONCE                    # One-time per player
    board-metadata:
      board-eligible: true
      supported-rarities:
        - legendary
      acceptance-cooldown: 14d           # 14-day cooldown before anyone can accept again
      cooldown-scope: GLOBAL             # Server-wide — once someone accepts, ALL players wait 14 days
```

This pattern is ideal for rare, prestigious quests on the legendary board — once a player completes it they can never accept it again, and the `GLOBAL` cooldown prevents it from being claimed by another player for 14 days.

---

## 16. Template Quests (Auto-Generated Variety)

Templates are blueprints that generate unique quests each time the board rotates. Instead of writing 50 mining quests by hand, you write one template and it produces different variations automatically.

Template files go in `plugins/McRPG/quest-board/templates/` and use `quest-templates:` instead of `quests:`.

### Simple Template Example

```yaml
quest-templates:
  mcrpg:template_deepslate_miner:
    display:
      name: "Deepslate Miner"
      description: "Mine deepslate underground"
    display-name-route: quest-templates.deepslate-miner
    board-eligible: true
    scope: mcrpg:single_player
    supported-rarities:
      - mcrpg:uncommon
      - mcrpg:rare
      - mcrpg:legendary

    # Variables — these get random values each time the quest is generated
    variables:
      deepslate_count:
        type: RANGE
        base:
          min: 64     # At least 64 blocks
          max: 256    # Up to 256 blocks

    phases:
      dig_deep:
        completion-mode: ALL
        stages:
          mine_deepslate:
            objectives:
              break_deepslate:
                type: mcrpg:block_break
                required-progress: "{deepslate_count}"   # Uses the variable!
                config:
                  blocks:
                    - DEEPSLATE
                    - COBBLED_DEEPSLATE
    rewards:
      mining_xp:
        type: mcrpg:experience
        skill: MINING
        amount: "{deepslate_count} * 2"   # Math expressions work too!
```

### How Variables Work

**RANGE variables** pick a random number between `min` and `max`, then scale it by rarity difficulty. So at Legendary (2.0x difficulty), a range of 64-256 becomes 128-512.

```yaml
variables:
  block_count:
    type: RANGE
    base:
      min: 32    # Minimum before scaling
      max: 96    # Maximum before scaling
```

**POOL variables** pick from groups of values, with different groups being more or less likely at different rarities. Great for making rare quests target harder materials.

```yaml
variables:
  target_blocks:
    type: POOL
    min-selections: 1    # Pick at least 1 pool
    max-selections: 3    # Pick up to 3 pools
    pools:
      basic_stone:
        difficulty: 1.0
        weight:                    # How likely this pool is per rarity
          mcrpg:common: 80         # Very likely at Common
          mcrpg:uncommon: 40
          mcrpg:rare: 10           # Unlikely at Rare
        values:
          - STONE
          - COBBLESTONE
          - ANDESITE
      ores:
        difficulty: 2.0
        weight:
          mcrpg:common: 10
          mcrpg:uncommon: 50
          mcrpg:rare: 70           # Very likely at Rare
        values:
          - COAL_ORE
          - IRON_ORE
          - COPPER_ORE
```

When a quest is generated, pools are selected based on the rolled rarity. A Common quest might get `basic_stone`, while a Rare quest is much more likely to get `ores`. Setting a weight to `0` means that pool can never be selected for that rarity.

### Using Variables in Objectives and Rewards

Reference variables with `{variable_name}` in `required-progress`, `config` values, and reward `amount` fields:

```yaml
# Use as an objective target
required-progress: "{block_count}"

# Use a pool's values as the block/mob list
config:
  blocks: "{target_blocks}"

# Use math expressions for rewards
amount: "{block_count} * 3"

# Combine multiple variables
amount: "({block_count} + {kill_count}) * 5"
```

### Template-Specific Settings

| Setting | Required | What it does |
|---------|----------|-------------|
| `display-name-route` | yes | Localization key for the quest name |
| `board-eligible` | no (default: true) | Whether the board can use this template |
| `supported-rarities` | yes | Which rarities this template can generate for |
| `rarity-overrides` | no | Override the difficulty/reward multiplier for specific rarities |

**Rarity overrides** let you fine-tune a specific template:

```yaml
rarity-overrides:
  mcrpg:legendary:
    difficulty-multiplier: 2.5    # Override the global 2.0 for this template only
    reward-multiplier: 4.0        # Override the global 3.0 for this template only
```

---

## 17. Advanced Templates

These features are optional but powerful. Skip this section until you're comfortable with basic templates.

### Multi-Phase Templates

Templates support the same phase system as hand-crafted quests:

```yaml
quest-templates:
  mcrpg:template_mine_and_fight:
    # ...
    variables:
      block_count:
        type: RANGE
        base: { min: 32, max: 80 }
      mob_count:
        type: RANGE
        base: { min: 10, max: 30 }
    phases:
      mine_phase:
        completion-mode: ALL
        stages:
          gather:
            objectives:
              mine_ores:
                type: mcrpg:block_break
                required-progress: "{block_count}"
                config:
                  blocks: [IRON_ORE, GOLD_ORE]
      fight_phase:
        completion-mode: ALL
        stages:
          fight:
            objectives:
              kill_mobs:
                type: mcrpg:mob_kill
                required-progress: "{mob_count}"
                config:
                  mobs: [ZOMBIE, SKELETON, CREEPER]
```

### Branching Templates

Use `completion-mode: ANY` to create "choose your path" templates:

```yaml
phases:
  choose:
    completion-mode: ANY
    stages:
      mining_path:
        objectives:
          mine_coal:
            type: mcrpg:block_break
            required-progress: "{amount}"
            config:
              blocks: [COAL_ORE]
      combat_path:
        objectives:
          kill_zombies:
            type: mcrpg:mob_kill
            required-progress: "{amount}"
            config:
              mobs: [ZOMBIE]
```

### Prerequisite Gating

Only show a template to players who have completed enough quests:

```yaml
quest-templates:
  mcrpg:template_veteran_miner:
    prerequisite:
      min-completions: 5    # Player must have completed 5+ quests first
    # ...
```

**Board interaction:** Prerequisite gating is wired into the board system. Templates with prerequisites like `min-completions: 15` will only appear for players who have completed 15+ quests on the **personal** board (where player context is available). They will **never** appear on the shared board because there is no player context to evaluate the prerequisite against.

### Random Objective Selection

Instead of always giving all objectives, randomly pick a subset:

```yaml
stages:
  random_tasks:
    objective-selection:
      mode: WEIGHTED_RANDOM
      min-count: 2         # Pick at least 2 objectives
      max-count: 3         # Pick up to 3 objectives
    objectives:
      mine_stone:
        type: mcrpg:block_break
        weight: 30          # More likely to be picked
        required-progress: "{amount}"
        config:
          blocks: [STONE]
      mine_ores:
        type: mcrpg:block_break
        weight: 20
        required-progress: "{amount}"
        config:
          blocks: [IRON_ORE]
      kill_undead:
        type: mcrpg:mob_kill
        weight: 25
        required-progress: "{amount}"
        config:
          mobs: [ZOMBIE, SKELETON]
      kill_spiders:
        type: mcrpg:mob_kill
        weight: 25
        required-progress: "{amount}"
        config:
          mobs: [SPIDER, CAVE_SPIDER]
```

Each rotation, 2-3 of these 4 objectives are randomly selected. Higher `weight` = more likely to be picked.

### Rarity-Gated Phases

Only include a bonus phase when the quest rolls a higher rarity:

```yaml
phases:
  main_phase:
    completion-mode: ALL
    stages:
      mine_iron:
        objectives:
          break_iron:
            type: mcrpg:block_break
            required-progress: "{ore_count}"
            config:
              blocks: [IRON_ORE]

  # This phase only appears for Rare and above
  bonus_phase:
    condition:
      type: mcrpg:rarity_gate
      min-rarity: rare
    completion-mode: ALL
    stages:
      mine_diamonds:
        objectives:
          break_diamonds:
            type: mcrpg:block_break
            required-progress: 8
            config:
              blocks: [DIAMOND_ORE]
```

### Reward Fallbacks

Give an alternative reward if the player already has something:

```yaml
rewards:
  champion_title:
    type: mcrpg:command
    display: "Champion Title"
    commands:
      - "title grant {player} champion"
    fallback:
      condition:
        permission: "mcrpg.title.champion"    # If they already have this permission...
      reward:
        type: mcrpg:experience                # ...give XP instead
        skill: SWORDS
        amount: 2000
```

---

## 18. Tips and Troubleshooting

### Common Mistakes

**Duplicate keys:** Every `key:` value must be unique across your entire server. If two quests use `key: mcrpg:mine_stone`, only the first one loaded will work. Use a prefix to keep things unique (e.g. `mcrpg:daily_mine_stone`, `mcrpg:weekly_mine_stone`).

**Missing `quests:` or `quest-templates:`:** The first line of your file (after comments) must be either `quests:` or `quest-templates:`. Without it, the file is silently skipped.

**Incorrect block/mob names:** Block and mob names must match Minecraft's internal names exactly (all uppercase, underscores). `diamond_ore` won't work — use `DIAMOND_ORE`. Check the Minecraft wiki for the correct technical names.

**Template variable names with hyphens:** Use underscores (`block_count`) not hyphens (`block-count`). Hyphens are interpreted as subtraction in math expressions, so `{block-count}` would try to calculate `block` minus `count`.

### Quest Not Appearing?

1. Check for YAML syntax errors — even one misplaced space can break the whole file. Use a YAML validator (many are available online for free).
2. Run `/mcrpg quest admin reload` after saving changes.
3. Check the server console for warnings — McRPG logs specific error messages when a quest fails to load.
4. For board quests: make sure `board-metadata.board-eligible` is `true` and `supported-rarities` / `supported-refresh-types` match your board configuration.
5. For templates: make sure `supported-rarities` lists at least one rarity that exists in your `board.yml`.

### Board Not Rotating?

1. Check `rotation.timezone` in `board.yml` — it must be a valid timezone string (e.g. `"America/New_York"`, `"Europe/London"`, `"UTC"`).
2. Make sure `rotation.time` uses 24-hour format (`"00:00"` not `"12:00 AM"`).
3. Check that at least one category exists in `quest-board/categories/` with a matching `refresh-type`.

### Testing Tips

- Start simple. Get one basic quest working before adding phases, branching, or templates.
- Use short expirations (`5m`) and low targets (`required-progress: 3`) while testing, then increase for production.
- For board testing, you can temporarily set all rarity weights the same to test each tier evenly.
- Check the console after `/mcrpg quest admin reload` for any errors or warnings.

### Performance

- There is no practical limit on how many quest files you can have. The plugin loads them all at startup and on reload.
- Template quests are generated at rotation time, not when a player opens the board. There is no ongoing performance cost for having many templates.
- Extremely high `required-progress` values (millions) work fine — progress tracking is lightweight.
