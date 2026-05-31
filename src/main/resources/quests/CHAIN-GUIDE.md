# McRPG Quest Chains — Server Owner Guide

Quest chains are sequences of quests that players complete in order. Each chain step starts the next quest only after the current one finishes. This guide covers everything you need to create and configure quest chains.

---

## Table of Contents

1. [Quick Start](#1-quick-start)
2. [File Format](#2-file-format)
3. [Two-File Workflow](#3-two-file-workflow)
4. [Step Ordering](#4-step-ordering)
5. [Trigger Types](#5-trigger-types)
6. [Repeat Modes](#6-repeat-modes)
7. [Expiration Behavior](#7-expiration-behavior)
8. [Reload Behavior](#8-reload-behavior)
9. [Admin Commands](#9-admin-commands)
10. [Tips and Troubleshooting](#10-tips-and-troubleshooting)

---

## 1. Quick Start

Create a new file at `plugins/McRPG/quests/my_chain.yml` and paste this:

```yaml
quest-chain-file: true

chains:
  mcrpg:my_first_chain:
    display-name: "My First Chain"
    source: mcrpg:manual
    auto-start:
      trigger: mcrpg:first_join
    repeat-mode: once
    steps:
      step_one:
        quest: mcrpg:my_first_quest
      step_two:
        quest: mcrpg:my_second_quest
```

Run `/mcrpg admin reload` and the chain is live. Players who join for the first time will automatically start `mcrpg:my_first_quest`. When they complete it, `mcrpg:my_second_quest` starts automatically.

**What each part means:**

| Field | What it does |
|-------|-------------|
| `quest-chain-file: true` | Required marker so McRPG knows this file is a chain file, not a quest file |
| `mcrpg:my_first_chain:` | Unique chain ID. The `mcrpg:` prefix is the namespace. Use your own plugin name or `mcrpg:` for custom chains |
| `display-name` | Player-facing name shown in GUIs and messages. Falls back to the key's value portion if omitted |
| `source` | Controls who started the chain-managed quests. Use `mcrpg:manual` unless you have a custom source |
| `trigger` | When the chain system evaluates whether to start this chain (see [section 5](#5-trigger-types)) |
| `repeat-mode` | Whether the chain can be completed more than once (see [section 6](#6-repeat-modes)) |
| `steps` | The ordered sequence of quests. Map key (`step_one`) is a label only — order is determined by YAML position |

---

## 2. File Format

Chain files must start with `quest-chain-file: true`. Without this marker, the file is parsed as a quest file and chain definitions in it will be silently ignored.

```yaml
# Required — marks this as a chain file
quest-chain-file: true

chains:
  # One or more chains, each with a unique key
  mcrpg:chain_one:
    # ... chain definition

  mcrpg:chain_two:
    # ... chain definition
```

**Where chain files live:** Place them in `plugins/McRPG/quests/` or any subdirectory. You can mix chain files and quest files in the same directory. The marker differentiates them.

**Full chain schema:**

```yaml
quest-chain-file: true

chains:
  mcrpg:example_chain:
    # Optional: player-facing display name
    display-name: "Example Chain"

    # Required: quest source key for chain-managed quests
    source: mcrpg:manual

    # Required: when to evaluate starting this chain
    auto-start:
      trigger: mcrpg:first_join

    # Optional: repeat behavior (default: once)
    # Note: only 'once' is currently enforced. Other modes are reserved for future use.
    repeat-mode: once

    # Required: ordered sequence of quest steps
    steps:
      label_one:
        quest: mcrpg:some_quest_key
        # Optional: what happens when this step's quest expires
        # Values: fail-chain (default). Other values reserved for future use.
        on-quest-expire: fail-chain

      label_two:
        quest: mcrpg:another_quest_key
```

---

## 3. Two-File Workflow

Quest chains reference quest definitions by key. The quest definitions must exist in separate quest files. The chain file and the quest files can be in the same directory or different directories — McRPG loads all `.yml` files from `quests/` (and subdirectories) regardless.

**Example layout:**

```
plugins/McRPG/quests/
├── tutorial/
│   ├── tutorial_chain.yml      ← chain file (quest-chain-file: true)
│   ├── tutorial_step_1.yml     ← quest file (quests: mcrpg:tutorial_step_1)
│   └── tutorial_step_2.yml     ← quest file (quests: mcrpg:tutorial_step_2)
└── daily/
    └── other_quests.yml
```

**Important:** The chain file references quest definitions by key. McRPG loads all quest files first, then chain files. As long as the quest key exists somewhere in any loaded quest file, the chain will find it.

---

## 4. Step Ordering

Steps are ordered by their position in the YAML map. The first step listed is the first quest started, the second step is started after the first completes, and so on.

```yaml
steps:
  mine_stone:       # ← starts first
    quest: mcrpg:tutorial_mine_stone
  chop_wood:        # ← starts second (after mine_stone is done)
    quest: mcrpg:tutorial_chop_wood
  kill_mobs:        # ← starts third (after chop_wood is done)
    quest: mcrpg:tutorial_kill_mobs
```

**The step labels** (`mine_stone`, `chop_wood`, `kill_mobs`) are for readability only. They are not exposed to players. Identity comes from the `quest:` key.

**No branching:** Chains are strictly linear. Use the quest system's `completion-mode: ANY` phases within individual steps if you want branching within a step.

---

## 5. Trigger Types

The `auto-start.trigger` key determines when the chain system evaluates whether to start the chain for a player.

| Trigger key | When it fires |
|-------------|--------------|
| `mcrpg:first_join` | Once, the very first time a player joins the server (`Player#hasPlayedBefore()` is false). Ideal for tutorial chains. |
| `mcrpg:login` | Every time a player logs in. Combined with `repeat-mode: once`, this effectively fires once (the chain starts on first eligible login, then the player is in a terminal state and skipped on future logins). |
| `mcrpg:manual` | Never fires automatically. The chain must be started via the admin command (`/mcrpg quest admin chain advance`) or by a third-party plugin calling the API. |

**All triggers are idempotent.** If the player already has an active or terminal state for the chain, the trigger evaluation skips them. You cannot accidentally start a chain twice for the same player.

**Login trigger timing:** Chains with `mcrpg:login` trigger are evaluated after the player's chain state is re-resolved (in case a definition changed while they were offline). First-join chains (`mcrpg:first_join`) are evaluated after login chains.

---

## 6. Repeat Modes

The `repeat-mode` key controls whether a player can complete the chain more than once.

| Value | Behavior |
|-------|----------|
| `once` | One completion per player, ever. After reaching any terminal state (COMPLETED, FAILED, or ABANDONED), the chain cannot be started again. **This is the only currently enforced mode.** |
| `unlimited` | Reserved for future use. Currently behaves identically to `once`. |
| `cooldown` | Reserved for future use. Currently behaves identically to `once`. |
| `limited` | Reserved for future use. Currently behaves identically to `once`. |
| `cooldown-limited` | Reserved for future use. Currently behaves identically to `once`. |

**Note:** Modes other than `once` are parsed and stored, but the enforcement logic is not yet implemented. Configuring `unlimited` or `cooldown` will have no effect until this is implemented in a future update. See the [chain system backlog](../../../docs/hld/tutorial/chain-system-backlog.md) for details.

---

## 7. Expiration Behavior

Each step can configure what happens when the step's quest expires without being completed.

```yaml
steps:
  timed_challenge:
    quest: mcrpg:some_quest
    on-quest-expire: fail-chain   # Default behavior
```

| Value | Behavior |
|-------|----------|
| `fail-chain` | The chain transitions to FAILED state. The player cannot restart the chain (unless the repeat mode allows it). This is the default if `on-quest-expire` is omitted. |

**Note:** Other expiration behaviors (`retry`, `restart-chain`, `skip`) are reserved for future use. Only `fail-chain` is currently functional. Using other values will log a warning and fall back to `fail-chain`.

---

## 8. Reload Behavior

Running `/mcrpg admin reload` reloads all chain definitions. **Active chains are not cancelled** — players in the middle of a chain continue where they left off.

**What happens on reload with active players:**

- Players with an active chain state for a definition that still exists: no change.
- Players whose current chain step references a quest key that was **removed** from the chain definition: the chain is re-resolved. McRPG reads their completion log and advances them to the first step they haven't completed yet. If all steps are completed, the chain is marked as COMPLETED.
- Players whose chain definition was **removed entirely**: the chain state is left as ACTIVE (inert). The player is stuck in a non-advancing state until the definition is restored or the chain is manually reset via admin command.

**Best practices:**

- Do not remove quest keys from the middle of a chain definition while players are actively working through it.
- If you need to restructure a chain, use `/mcrpg quest admin chain reset <player> <chain>` to clear affected players' state before making the change.

---

## 9. Admin Commands

All admin commands require the `mcrpg.quest.admin.chain.*` permission (or the specific sub-permission).

| Command | What it does |
|---------|-------------|
| `/mcrpg quest admin chain status <player> <chain>` | Shows the player's current chain state: active/completed/failed, current step, completion count, last completed time |
| `/mcrpg quest admin chain advance <player> <chain>` | Force-advances the player's chain to the next step, bypassing the current quest's normal completion flow |
| `/mcrpg quest admin chain restart <player> <chain> [force]` | Restarts the chain. Without `force`, skips already-completed steps. With `force`, restarts from step 1 |
| `/mcrpg quest admin chain reset <player> <chain>` | Hard-resets the chain — clears all state and completion history. The player experiences the chain as if they never started it |

**Tab-completion** is supported for player names and registered chain keys.

---

## 10. Tips and Troubleshooting

### Chain Not Starting?

1. Make sure the chain file starts with `quest-chain-file: true`.
2. Check that the trigger matches what you expect (`mcrpg:first_join`, `mcrpg:login`, or `mcrpg:manual`).
3. Check that the quest keys referenced in `steps` exist in loaded quest files. Check the console after `/mcrpg admin reload` for warnings about unknown quest keys.
4. For `mcrpg:first_join` trigger: the chain only fires for players who have never joined before (`hasPlayedBefore()` is false). Use `/mcrpg quest admin chain advance` to manually test it on an existing player.
5. Check `repeat-mode` — a player who previously completed, abandoned, or failed the chain will not be automatically restarted (all modes currently behave as `once`).

### Step Not Advancing?

1. Verify the player completed the correct quest (not just any quest).
2. Run `/mcrpg quest admin chain status <player> <chain>` to see the current step.
3. If the chain state shows ACTIVE but no current step, the player's state may have become inert (the chain definition changed). Use `/mcrpg quest admin chain restart` to fix it.

### Duplicate Chain Keys

Every chain key must be unique across all chain files. If two chain files define `mcrpg:tutorial_chain`, only the first one loaded will take effect. The console will log a warning about the duplicate.

### Testing Tips

- Use `mcrpg:manual` trigger while building your chain, then switch to `mcrpg:first_join` or `mcrpg:login` when ready.
- Use `/mcrpg quest admin chain reset <yourself> <chain>` after each test run to clear your state.
- Use short quest expirations (`5m`) and low targets (`required-progress: 1`) while testing step transitions.
