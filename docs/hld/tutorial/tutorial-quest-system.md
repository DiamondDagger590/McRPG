# Tutorial Quest System

> **Last Updated:** 2026-06-01
> **Status:** All three phases implemented
> **Scope:** First-class quest chain system, tutorial quest line, new objective/reward types, player onboarding flow
> **Phase 1 LLD:** [phase-1-quest-engine-extensions.md](../../lld/tutorial-quest-system/phase-1-quest-engine-extensions.md)
> **Phase 2 LLD:** [phase-2-quest-chain-system.md](../../lld/tutorial-quest-system/phase-2-quest-chain-system.md)
> **Phase 3 LLD:** [phase-3-tutorial-content.md](../../lld/tutorial-quest-system/phase-3-tutorial-content.md)

---

## Design Philosophy

Three guiding principles:

- **Natural progression**: Quests trigger based on what the player is already doing, never forcing them to stop their gameplay. Every tutorial step is something the player was going to do anyway.
- **Non-restrictive**: No features are gated behind tutorial completion. Veterans can opt out via a player setting. Servers can disable the system entirely.
- **Gamemode agnostic**: Objectives use McRPG-internal events (level ups, GUI opens, ability unlocks), not gamemode-specific actions. Works on survival, factions, towny, skyblock, etc.

---

## Architecture Overview

The tutorial system is built on a new **Quest Chain** orchestration layer that sits on top of the existing quest engine. The chain manages ordering and auto-advancement; individual quests remain independent definitions.

```mermaid
flowchart TD
    subgraph chainLayer [Quest Chain Layer]
        QCD[QuestChainDefinition]
        QCM[QuestChainManager]
        QCR[QuestChainRegistry]
        QCPL[QuestChainProgressListener]
        QCSL[QuestChainStartCondition]
    end

    subgraph existing [Existing Quest Engine]
        QDR[QuestDefinitionRegistry]
        QM[QuestManager]
        QI[QuestInstance]
        Rewards[RewardSystem]
    end

    subgraph newInfra [Phase 1 Infrastructure - Implemented]
        PQSE[PreQuestStartEvent]
        OSM[OnStartMessage + QuestStartMessageListener]
        QMD[QuestMessageDeliverer]
        MRT[MessageRewardType]
        BERT[BoostedExperienceRewardType]
        ObjTypes[7 New Objective Types]
    end

    subgraph tutorial [Tutorial Content]
        Chain[tutorial/chain.yml]
        Q1[Q1: First Steps]
        Q2[Q2: The McRPG Menu]
        Q3[Q3: Natural Talent]
        Q4[Q4: Your Arsenal]
        Q5[Q5: Unleashed Power]
        Q6[Q6: Combo Strike]
        Q7[Q7: The Quest Board]
    end

    QCD --> QCR
    QCM --> QCR
    QCPL -->|"QuestCompleteEvent"| QCM
    QCM -->|"start next step"| QM
    QM -->|"PreQuestStartEvent when player online"| PQSE
    QM -->|"create instance"| QI
    QI -->|"QuestStartEvent"| OSM
    OSM --> QMD
    Chain --> QCD
    Q1 --> QDR
    Q2 --> QDR
    Q3 --> QDR
    Q4 --> QDR
    Q5 --> QDR
    Q6 --> QDR
    Q7 --> QDR
    ObjTypes --> QI
```

---

## Infrastructure Changes

### 1. Quest Chain System (first-class concept)

Quest chains are an orchestration layer on top of existing quest definitions. A chain defines an ordered sequence of quest definitions that auto-advance on completion. Chains have their own source, conditions between steps, and persisted per-player state.

**QuestChainDefinition** (loaded from `chain.yml` co-located with quest files):

```yaml
# quests/tutorial/chain.yml
key: mcrpg:tutorial_chain
source: mcrpg:tutorial
auto-start:
  trigger: mcrpg:first_join
steps:
  first_steps:
    quest: mcrpg:tutorial_first_steps
  explore_menu:
    quest: mcrpg:tutorial_explore_menu
  passive_unlock:
    quest: mcrpg:tutorial_passive_unlock
  open_loadout:
    quest: mcrpg:tutorial_open_loadout
  active_unlock:
    quest: mcrpg:tutorial_active_unlock
  combo_cast:
    quest: mcrpg:tutorial_combo_cast
  quest_board:
    quest: mcrpg:tutorial_quest_board
```

**Key components:**

- `QuestChainDefinition` -- immutable definition loaded from YAML (key, source key, auto-start trigger, ordered step list)
- `QuestChainStep` -- quest key + optional start conditions
- `QuestChainRegistry` -- registered in `McRPGRegistryKey`, stores loaded chain definitions
- `QuestChainManager` -- runtime chain state management (accessed via `McRPGManagerKey.QUEST_CHAIN`)
- `QuestChainProgressListener` -- on `QuestCompleteEvent`, checks if the completed quest belongs to a chain and advances to the next step
- `QuestChainStartCondition` -- extensible interface for gating chain steps (permission, etc.). Built-in conditions deferred to backlog (see `chain-system-backlog.md`).

**Chain state persistence** -- new SQL table:

```sql
CREATE TABLE mcrpg_quest_chain_state (
    player_uuid       TEXT NOT NULL,
    chain_key         TEXT NOT NULL,
    current_quest     TEXT,
    state             TEXT NOT NULL,
    completion_count  INTEGER NOT NULL DEFAULT 0,
    last_completed_at BIGINT,
    PRIMARY KEY (player_uuid, chain_key)
);
```

State values: `ACTIVE`, `COMPLETED`, `ABANDONED`, `FAILED`, `EXPIRED`

`current_quest` is nullable -- `NULL` when state is `COMPLETED` or `ABANDONED` (no meaningful current quest in terminal states). Stores `NamespacedKey` (not index) to handle chain reconfiguration:
- On load, resolve the quest's position in the current chain definition
- If the stored quest no longer exists in the chain, advance to the first uncompleted step (checked against completion log)
- If steps were reordered, the player picks up from wherever their current quest now sits

**Chain completion log** -- separate historical table:

```sql
CREATE TABLE mcrpg_quest_chain_completion_log (
    player_uuid       TEXT NOT NULL,
    chain_key         TEXT NOT NULL,
    completed_at      BIGINT NOT NULL,
    completion_number INTEGER NOT NULL,
    PRIMARY KEY (player_uuid, chain_key, completion_number)
);
```

Written on every chain completion by `QuestChainManager`. The state table is operational (where is the player now?); the completion log is historical (when did they finish each time?). This mirrors the existing `QuestCompletionLogDAO` pattern for individual quests. For the tutorial (ONCE), only one row is ever written. For future repeatable chains, this provides full completion history.

**Quest History GUI -- chain grouping:**

Completed chains appear as a single grouped entry in `QuestHistoryGui` rather than showing each quest independently:
- Chain entry uses the chain's source material (e.g., `KNOWLEDGE_BOOK` for tutorial) and displays the chain name
- Clicking the chain entry opens a sub-view (`QuestChainHistoryDetailGui`) showing the individual quest completions within that chain run
- For repeatable chains (future), multiple chain entries appear (one per completion), each expandable
- Individual quests that are NOT part of any chain continue showing as independent entries in the history
- Ordering: chain entries sort by `completed_at`; within a chain, quests sort by step order

**Auto-start triggers:**

Auto-start triggers are extensible via `ChainAutoStartTriggerRegistry`. Each trigger defines *when* the chain system evaluates whether to start a chain for a player. Triggers are independent of conditions (which define *if* the chain should start).

- `ChainAutoStartTrigger` -- interface: `getKey()`, responsible for registering its own Bukkit listener or hook that calls `QuestChainManager.tryStartChain()`
- `ChainAutoStartTriggerRegistry` -- registered in `McRPGRegistryKey`
- `ChainAutoStartTriggerContentPack` -- third-party registration

Built-in triggers:

| Key | Behavior | Listener |
|---|---|---|
| `mcrpg:first_join` | Evaluate on first join (chain state doesn't exist for player) | `QuestChainFirstJoinListener` |
| `mcrpg:login` | Re-evaluate every login (for time-gated/repeatable chains) | `QuestChainLoginListener` |
| `mcrpg:manual` | Never auto-evaluate; started only via API or command | No listener (inert) |

Third-party plugins can register custom triggers (e.g., `myplugin:region_enter`, `myplugin:npc_interact`) via content pack. Each custom trigger provides its own listener that calls `QuestChainManager.tryStartChain(player, chainKey)`.

YAML references triggers by key:

```yaml
auto-start:
  trigger: mcrpg:first_join
```

Trigger detection lives in dedicated listeners (`QuestChainFirstJoinListener`, `QuestChainLoginListener`) that delegate to `QuestChainManager` -- the manager itself does not listen for Bukkit events.

**Extension infrastructure:**
- `QuestChainContentPack` -- third-party plugins register chains via content expansion
- `QuestChainStartConditionContentPack` -- register custom chain step conditions (deferred to backlog; interface ships but no built-in conditions or content pack initially)
- Lifecycle events: `QuestChainStartEvent`, `QuestChainStepAdvanceEvent`, `QuestChainCompleteEvent`
- `ContentHandlerType.QUEST_CHAIN`

**Relationship to quest definitions:** Quest definitions stay independent and reusable. They do not know they are part of a chain. The chain is purely an orchestration/ordering concern.

### 2. PreQuestStartEvent (general-purpose, cancellable) — **Implemented**

A cancellable Bukkit event fired from `QuestManager.startQuest()` **when the initiating player is online** (`Bukkit.getPlayer(initialPlayerUUID) != null`). Offline or system-initiated starts skip the pre-event. Gives third-party plugins a general-purpose hook to gate quest starts.

```java
public class PreQuestStartEvent extends Event implements Cancellable {
    private final QuestDefinition definition;
    private final Player player;
    private final QuestSource source;
    // ...
}
```

**Event ownership:** `PreQuestStartEvent` fires from `QuestManager.startQuest()` before instance creation. `QuestStartEvent` fires from `QuestInstance.start(definition, starterUUID)` after phase-0 activation — not from the manager. All quest starts must route through `QuestManager` so the pre-event gate cannot be bypassed (`QuestInstance.start()` is `@ApiStatus.Internal`).

**Tutorial opt-out:** `TutorialPreQuestStartListener` checks three gates in order: (1) `tutorial.enabled` config toggle, (2) `mcrpg.tutorial.bypass` permission, (3) `DisableTutorialSetting`. If any gate applies, the `PreQuestStartEvent` is cancelled for `TutorialQuestSource`-sourced quests.

### 3. On-Start Messages on QuestDefinition — **Implemented**

Dedicated message-only concept on `QuestDefinition` — **not** completion rewards. Delivered on `QuestStartEvent` via `QuestStartMessageListener` and `QuestMessageDeliverer`.

Schema:
- `List<OnStartMessage> onStartMessages` (empty by default)
- `QuestDefinition.Builder` is the only construction path (previous constructors removed)

YAML (`on-start-messages:`):

```yaml
on-start-messages:
  welcome:
    key: tutorial.first-steps.welcome
  explain_skills:
    messages:
      - "<primary>As you play, your skills will level up automatically."
      - "<body>Try breaking some blocks to see your Mining skill grow."
```

Locale keys are resolved per-player; inline `messages` are used when resolution fails or no key is set. See [`OBJECTIVES.md`](../../../src/main/resources/quests/OBJECTIVES.md) / Phase 1 LLD for delivery details.

### 4. MessageRewardType — **Implemented**

Completion reward type (`mcrpg:message`) for sending player-facing messages. `grant()` uses `QuestMessageDeliverer` (same locale-first, inline-fallback behavior as on-start messages). Supports both locale route lookup and inline MiniMessage strings with palette resolution.

```yaml
# Route-based (preferred for translatable text)
welcome:
  type: mcrpg:message
  key: tutorial.welcome-message

# Inline fallback (for quick prototyping or non-translatable text)
hint:
  type: mcrpg:message
  messages:
    - "<primary>Tip:</primary> <body>Run /mcrpg to open your menu!"

# Both (route lookup with inline fallback)
greeting:
  type: mcrpg:message
  key: tutorial.greeting
  messages:
    - "<primary>Welcome!</primary> <body>Your adventure begins."
```

Resolution order:
1. Locale route lookup via `McRPGLocalizationManager` (if `key` is set)
2. Inline `messages` as fallback (if route lookup fails or `key` is absent)

All strings pass through palette resolution (`buildPaletteReplacements()`) and MiniMessage parsing. Standard placeholders (`<player>`, etc.) are supported.

`MessageRewardType` is chat-only. When rendered in GUI contexts (e.g., `QuestDetailRewardSlot`), `describeForDisplay()` returns a short summary, not the full message list.

### 5. TutorialQuestSource

New `QuestSource` subclass: non-abandonable, with `NamespacedKey` `mcrpg:tutorial`. Serves two purposes:
- **UI treatment**: Tutorial quests visually distinguished in the Active Quests GUI via a distinct material (e.g., `KNOWLEDGE_BOOK` instead of `WRITABLE_BOOK`) and a `<tutorial>` palette placeholder for the quest name color
- **PreQuestStartEvent gating**: `TutorialPreQuestStartListener` checks the player setting and cancels starts for this source when tutorials are disabled

New palette entry in `config.yml`:

| Role | Placeholder | Default Value | When to Use |
|---|---|---|---|
| **tutorial** | `<tutorial>` | `<color:#E8C97A>` | Tutorial quest names and chain-related UI elements |

Non-abandonable quests show a `<body>Tutorial quests cannot be abandoned.` lore line and play a deny sound + action bar message on right-click attempt.

### 6. Seven New Objective Types — **Implemented**

All types follow the `QuestObjectiveType` pattern. Ability-based types use `AbilityObjectiveFilter` and `AbilityType` (`Ability.getAbilityType()`). State-based objectives support **immediate** auto-complete on quest start via `QuestStartAutoCompleteListener` for the **quest starter only** (`QuestStartEvent.getStarterUUID()`).

Auto-complete is immediate when state checks pass (no delay). On-start message suppression during chain cascade batching is handled by `CascadeOrchestrator` (batch summary messaging replaces individual messages for auto-completed steps).

| Type Key | Kind | Trigger | Config Filters | Auto-Complete Check |
|---|---|---|---|---|
| `mcrpg:skill_level_up` | Event | `SkillGainLevelEvent` | `skill` (optional), `levels` (min levels per event, default 1) | N/A |
| `mcrpg:skill_target_level` | State + Event | `SkillGainLevelEvent` | `skill` (optional), `target-level` (default 1) | Player skill level >= target |
| `mcrpg:gui_open` | Event | `CoreGuiOpenEvent` | `gui-type` (required; e.g. `mcrpg:home`, `mcrpg:loadout_selection`, `mcrpg:board`) | N/A |
| `mcrpg:ability_unlock` | State + Event | `AbilityUnlockEvent` | `ability-type` (`ACTIVE`, `PASSIVE`, `INNATE`), optional `ability` | Unlocked ability matching filter |
| `mcrpg:ability_activate` | Event | `AbilityActivateEvent` | `ability-type`, optional `ability` | N/A (covers combo activations via `ACTIVE`) |
| `mcrpg:loadout_equip` | State + Event | `LoadoutAbilityChangeEvent` (EQUIP/SWAP) | `ability-type`, optional `ability` | Active loadout contains matching ability |
| `mcrpg:quest_board_accept` | Event | `BoardOfferingAcceptEvent` | Optional `board` | N/A |

`mcrpg:combo_activate` was **not** implemented — tutorial Q6 uses `mcrpg:ability_activate` with `ability-type: ACTIVE`.

Detailed YAML and behavior: [`OBJECTIVES.md`](../../../src/main/resources/quests/OBJECTIVES.md).

**GUI Key System:**

McCore defines `KeyedGui` — a generic interface for identifying GUI types by `NamespacedKey`. McRPG GUI classes implement it:

```java
// In McCore:
public interface KeyedGui {
    @NotNull Optional<NamespacedKey> getGuiKey();
}

// Example on HomeGui (McRPG):
public class HomeGui extends BaseGui<McRPGPlayer> implements FillerItemGui, KeyedGui {
    public static final NamespacedKey GUI_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "home");

    @Override
    @NotNull
    public Optional<NamespacedKey> getGuiKey() { return Optional.of(GUI_KEY); }
}
```

This allows any plugin using McCore's GUI system to implement `KeyedGui` on their GUIs and have them identifiable by key. GUIs that do not implement `KeyedGui` still fire the open event but with `Optional.empty()` for the key.

**McCore `CoreGuiOpenEvent`:**

McCore fires `CoreGuiOpenEvent` directly from `GuiManager.trackPlayerGui()` after tracking completes. No template method or downstream override needed:

```java
// In McCore GuiManager:
public void trackPlayerGui(@NotNull UUID uuid, @NotNull Gui<P> gui) {
    // ... existing tracking logic ...
    Optional<NamespacedKey> guiKey = (gui instanceof KeyedGui keyed) ? keyed.getGuiKey() : Optional.empty();
    Bukkit.getPluginManager().callEvent(new CoreGuiOpenEvent(uuid, gui, guiKey));
}
```

`CoreGuiOpenEvent` carries the player UUID, the `Gui` instance, and `Optional<NamespacedKey>`. It is a McCore-level event — any plugin using the McCore GUI system gets it for free. McRPG's `GuiOpenQuestProgressListener` simply listens for `CoreGuiOpenEvent`.

**New / updated events (Phase 1):**
- `CoreGuiOpenEvent` (McCore) — from `GuiManager.trackPlayerGui()`. Back-button re-opens count intentionally.
- `LoadoutAbilityChangeEvent` — unified equip/unequip/swap on `Loadout` (`ChangeReason` enum).
- `LoadoutPositionSwapEvent` — combo-slot reorder only (not used by quest objectives).

**Loadout API:** `Loadout.equipAbility()` / `unequipAbility()` / `swapAbility()` replace direct `addAbility()` / `removeAbility()` / `replaceAbility()` (now private). `LoadoutEquipQuestProgressListener` only credits the **active** loadout.

**Existing events leveraged:**
- `AbilityUnlockEvent` — progress listener added; event already fired from skill level-up path.

### 7. BoostedExperienceRewardType — **Implemented**

New reward type (`mcrpg:boosted_experience`) that adds to a player's boosted experience bank directly. State ownership: `grant()` resolves the `McRPGPlayer` and mutates `PlayerExperienceExtras.modifyBoostedExperience(amount)`.

```yaml
boosted_xp:
  type: mcrpg:boosted_experience
  amount: 500
```

### 8. RedeemableExperienceRewardType and RedeemableLevelsRewardType — **Implemented**

New reward types for adding to a player's redeemable XP and redeemable levels banks. State ownership: both mutate `PlayerExperienceExtras` (`modifyRedeemableExperience(amount)`, `modifyRedeemableLevels(amount)`).

```yaml
redeemable_xp:
  type: mcrpg:redeemable_experience
  amount: 500

redeemable_levels:
  type: mcrpg:redeemable_levels
  amount: 1
```

### 9. Configuration Toggles

**Server-wide** (`config.yml`):

```yaml
# Whether the tutorial quest chain auto-starts for new players.
# When disabled, no new tutorial chains will start. Existing active
# tutorial quests are NOT cancelled -- only new starts are suppressed.
# Requires restart to take effect.
tutorial:
  enabled: true
```

No `config-version` bump needed — no migration logic required for this addition.

**Player setting** (new `PlayerSetting` impl):
- `DISABLE_TUTORIAL` boolean setting, default `false`
- When toggled to `true`: shows a confirmation prompt (reuses the existing `ConfirmationManager` pattern). On confirm: cancel any active tutorial quest via chain manager, set chain state to `ABANDONED`
- Toggling back to `false` after abandonment does NOT restart the chain or grant missed rewards -- the chain remains `ABANDONED`
- Exposed in the Player Settings GUI with distinct materials for enabled/disabled + deny sound on destructive toggle
- `TutorialPreQuestStartListener` checks this setting and cancels `PreQuestStartEvent` for tutorial sources

**Permission nodes:**

| Permission | Default | Purpose |
|---|---|---|
| `mcrpg.*` | `op` | Root wildcard — grants everything below |
| `mcrpg.admin.*` | `op` | All admin commands |
| `mcrpg.quest.chain.*` | `op` | All chain admin subcommands |
| `mcrpg.quest.chain.restart` | `op` | Restart a player's chain from step 1 (preserves history) |
| `mcrpg.quest.chain.reset` | `op` | Hard reset a player's chain state (wipes history) |
| `mcrpg.quest.chain.advance` | `op` | Force-advance a player's chain to the next step |
| `mcrpg.quest.chain.skip` | `op` | Force-complete all remaining steps in a player's chain |
| `mcrpg.quest.chain.status` | `op` | View a player's chain state and progress |
| `mcrpg.tutorial.bypass` | `op` | Exempt from tutorial auto-start (checked in `QuestChainFirstJoinListener`) |

Standard Bukkit permission inheritance applies — granting `mcrpg.*` implicitly grants every child node. The `plugin.yml` `children` block declares the full hierarchy so permission plugins resolve wildcards correctly.

---

## Tutorial Quest Chain

Seven quests progressing from passive discovery through active mastery. The chain auto-starts on first join. All quests are `QuestRepeatMode.ONCE`, `SinglePlayerQuestScope`, sourced from the `mcrpg:tutorial_chain` chain.

```mermaid
flowchart LR
    Q1["Q1: First Steps\n(Level up any skill)"]
    Q2["Q2: The McRPG Menu\n(Open Home GUI)"]
    Q3["Q3: Natural Talent\n(Unlock a passive ability)"]
    Q4["Q4: Your Arsenal\n(Open loadout)"]
    Q5["Q5: Unleashed Power\n(Unlock an active ability)"]
    Q6["Q6: Combo Strike\n(Cast an active ability)"]
    Q7["Q7: The Quest Board\n(Accept a board quest)"]

    Q1 -->|"chain advances"| Q2
    Q2 -->|"chain advances"| Q3
    Q3 -->|"chain advances"| Q4
    Q4 -->|"chain advances"| Q5
    Q5 -->|"chain advances"| Q6
    Q6 -->|"chain advances"| Q7
```

**Auto-complete pacing:** The Phase 1 engine auto-completes immediately for the quest starter when state checks pass. The Phase 3 `CascadeOrchestrator` provides cascade batching: on-start messages for auto-completed chain steps are deferred and selectively discarded, and a configurable batch summary is delivered at cascade finalization via the localization system (`quest-chain.cascade.batch-header` / `batch-step-entry` locale keys).

### Quest 1: "First Steps"

- **Auto-starts**: On first join via chain `auto-start: trigger: mcrpg:first_join`
- **On-start messages**: Welcome text explaining skills level up as they play (`on-start-messages:` with locale key or inline)
- **Objective**: Have any skill at level 1 or above (`mcrpg:skill_target_level`, `target-level: 1`)
- **Completion rewards**: 1,000 boosted XP
- **Auto-complete**: Immediate if player already has skill level >= 1

### Quest 2: "The McRPG Menu"

- **On-start messages**: Tell player to run `/mcrpg`
- **Objective**: Open the Home GUI (`mcrpg:gui_open`, `gui-type: mcrpg:home`)
- **Completion rewards**: 1,000 boosted XP

### Quest 3: "Natural Talent"

- **On-start messages**: Explain passive abilities trigger automatically
- **Objective**: Unlock a passive ability (`mcrpg:ability_unlock`, `ability-type: PASSIVE`)
- **Completion rewards**: 1,500 boosted XP + 1 redeemable level
- **Auto-complete**: Immediate if player already has a matching unlocked passive

### Quest 4: "Your Arsenal"

- **On-start messages**: Loadouts and auto-equip behavior
- **Objective**: Open the Loadout GUI (`mcrpg:gui_open`, `gui-type: mcrpg:loadout_selection`)
- **Completion rewards**: 1,000 boosted XP + 1,500 redeemable XP

### Quest 5: "Unleashed Power"

- **On-start messages**: Active abilities and combo clicks
- **Objective**: Unlock an active ability (`mcrpg:ability_unlock`, `ability-type: ACTIVE`)
- **Completion rewards**: 2,000 boosted XP + 1 redeemable level
- **Auto-complete**: Immediate if player already has a matching unlocked active ability

### Quest 6: "Combo Strike"

- **On-start messages**: Combo patterns (RRR, RRL, RLR), mana costs, allowed items
- **Objective**: Activate any active ability (`mcrpg:ability_activate`, `ability-type: ACTIVE`)
- **Completion rewards**: 2,000 boosted XP + 1 redeemable level

### Quest 7: "The Quest Board"

- **On-start messages**: Quest board rotating challenges and rewards
- **Objective**: Accept a quest from the quest board (`mcrpg:quest_board_accept`)
- **Completion rewards**: 2,500 boosted XP + 2,500 redeemable XP + 1 redeemable level (graduation bonus)

### Reward Summary

| Quest | Boosted XP | Redeemable XP | Redeemable Levels | Cumulative Boosted |
|---|---|---|---|---|
| Q1: First Steps | 1,000 | -- | -- | 1,000 |
| Q2: The McRPG Menu | 1,000 | -- | -- | 2,000 |
| Q3: Natural Talent | 1,500 | -- | 1 | 3,500 |
| Q4: Your Arsenal | 1,000 | 1,500 | -- | 4,500 |
| Q5: Unleashed Power | 2,000 | -- | 1 | 6,500 |
| Q6: Combo Strike | 2,000 | -- | 1 | 8,500 |
| Q7: The Quest Board | 2,500 | 2,500 | 1 | 11,000 |
| **Total** | **11,000** | **4,000** | **4** | |

**Value analysis (level-up equation: `200+(0.8*(skill_level^1.5))`):**

Early levels cost ~200 XP each; cumulative to reach level 10 ≈ 2,100 XP, level 20 ≈ 4,300 XP, level 50 ≈ 15,300 XP.

- **11,000 boosted XP** (at 2x consumption rate): doubles the player's next 11,000 earned XP across all skills. A player actively playing 2 skills gets roughly 5,500 bonus XP per skill — catapulting each to ~level 20-25 much faster than baseline.
- **4,000 redeemable XP**: enough to instantly push one skill from level 0 to ~level 18, or spread across skills.
- **4 redeemable levels**: immediate gratification applied to any skill(s).

**Net effect:** A new player who finishes the tutorial and plays for an hour has a realistic path to level 20-30 in their primary skill — putting them well past the "something is happening" threshold and into meaningful ability scaling. This is intentionally front-loaded: early progression should feel fast and rewarding to hook players, while the curve naturally slows them into long-term goals (first active ability at ~25, first passive at ~40, tier upgrades via quests).

### Veteran Skip Behavior

When a player toggles `DISABLE_TUTORIAL`, the active tutorial quest is cancelled and the chain state is set to `ABANDONED`. No rewards are granted for uncompleted steps (abandon approach). The chain cannot be restarted after abandonment. Revisit if playtesting shows the reward gap is too impactful.

### Future Tutorial Extensions (revisit later)

These are not part of the initial chain but are candidates for future tutorial steps appended after Q7:
- Open the Ability Edit GUI (teaches configuration/toggling)
- Open the Experience Bank GUI (teaches boosted/rested/redeemable systems)
- Complete a quest board quest (teaches the full board lifecycle)

---

## Chain Reload Behavior

When `/mcrpg admin reload` is executed and chain definitions are reloaded:

1. `QuestChainRegistry` replaces its definitions (same clear-and-replace pattern as `QuestDefinitionRegistry`).
2. For each online player with an `ACTIVE` chain state, re-resolve their `current_quest` against the new chain definition:
   - **Quest still in chain:** No action. Player continues on their current step.
   - **Quest removed from chain:** Cancel the player's active quest instance. Re-resolve to the first uncompleted step (checked against the quest completion log). Start it.
   - **Chain definition entirely removed:** Do nothing — leave the active quest instance running, chain state stays `ACTIVE` but becomes inert (no advancement will fire since the listener can't find the definition). Log a `WARNING`: `"Player {name} has active chain state for '{chain_key}' but no chain definition is loaded. Chain is suspended until the definition is restored."`
3. If the chain definition is re-added on a future reload, re-resolution kicks in normally on the next advancement trigger.

### Login-time re-resolution

When a player logs in with an `ACTIVE` chain state, `QuestChainLoginListener` performs the same re-resolution logic as step 2 above. This covers players who were offline during a reload:

- **Chain definition exists, `current_quest` still valid:** Resume normally — the chain listener picks up advancement from here.
- **Chain definition exists, `current_quest` no longer in chain:** Cancel the stale quest instance (if still persisted as active), re-resolve to the first uncompleted step, start it.
- **Chain definition missing entirely:** Leave chain state as `ACTIVE` (inert). Log a `WARNING` with the player's name and chain key. No quest is started — if the definition is restored on a future reload or restart, the next advancement trigger (or next login) re-resolves normally.

This ensures that regardless of whether a player was online or offline during a reload, they always converge to a consistent state on their next interaction with the system.

---

## Chain Repeat Mode and State Model

The chain state enum accommodates both the tutorial (ONCE) and future event chains:

| State | Meaning |
|---|---|
| `ACTIVE` | Chain is in progress, player has a current quest |
| `COMPLETED` | All steps finished naturally |
| `ABANDONED` | Player opted out (tutorial disable toggle, or future "abandon chain" action) |
| `FAILED` | A step's `on-quest-expire` triggered `fail-chain` |
| `EXPIRED` | Availability window closed with `expire-active` policy |

For repeatable chains, `COMPLETED`/`FAILED`/`EXPIRED` are all re-startable states (subject to repeat mode + cooldown + availability window). `ABANDONED` is terminal.

Chain definitions carry a `repeat-mode` field:

| Mode | Behavior |
|---|---|
| `ONCE` | Chain can only be completed once per player. (Tutorial default) |
| `UNLIMITED` | Re-startable immediately after completion. |
| `COOLDOWN` | Re-startable after a configurable cooldown. |
| `LIMITED` | Completable N times total. |
| `COOLDOWN_LIMITED` | Combination of cooldown + max completions. |

The chain state table includes forward-compatible columns:

```sql
CREATE TABLE mcrpg_quest_chain_state (
    player_uuid       TEXT NOT NULL,
    chain_key         TEXT NOT NULL,
    current_quest     TEXT,
    state             TEXT NOT NULL,
    completion_count  INTEGER NOT NULL DEFAULT 0,
    last_completed_at BIGINT,
    PRIMARY KEY (player_uuid, chain_key)
);
```

`repeat-mode` defaults to `ONCE` when omitted from YAML. For the initial implementation only `ONCE` is functional; other modes are parsed and stored but treated as `ONCE` until the availability/repeatability backlog work lands.

Chain steps carry an optional `on-quest-expire` field:

```yaml
steps:
  timed_challenge:
    quest: mcrpg:christmas_gift_rush
    on-quest-expire: retry       # retry | fail-chain | restart-chain
    max-retries: 3
```

Default when omitted: `fail-chain`. For the initial implementation, only `fail-chain` is functional.

---

## Admin Commands

Generic chain management commands (not tutorial-specific):

| Command | Permission | Purpose |
|---|---|---|
| `/mcrpg quest chain restart <player> <chain>` | `mcrpg.quest.chain.restart` | Restart a player's chain from step 1 — cancels the active quest, sets state back to `ACTIVE` with `current_quest` at the first step. Completion log is preserved; quests already in the log are skipped (player won't redo completed steps or re-earn their rewards). |
| `/mcrpg quest chain reset <player> <chain>` | `mcrpg.quest.chain.reset` | Hard reset — clears chain state, completion log entries, and completion count. Player experiences the chain as if for the first time (rewards re-granted on re-completion). For the tutorial chain, also resets `DisableTutorialSetting` to `ENABLED`. |
| `/mcrpg quest chain advance <player> <chain>` | `mcrpg.quest.chain.advance` | Force-advance to the next step — completes the current quest (granting rewards) and starts the next step. If the player is on the last step, this completes the chain. Delegates to `CascadeOrchestrator` for cascade-aware advancement. |
| `/mcrpg quest chain skip <player> <chain>` | `mcrpg.quest.chain.skip` | Force-complete all remaining steps — loops `forceAdvanceChain()` until the chain leaves `ACTIVE` state. Rewards fire through the normal chain-completion path. |
| `/mcrpg quest chain status <player> <chain>` | `mcrpg.quest.chain.status` | Display the player's current chain state, current quest step, and completion count. |

**Distinction:** `restart` is for support ("player is stuck, let them retry from the top without double-dipping rewards"). `reset` is for QA/dev ("pretend this player never touched this chain"). Both cancel any currently active quest instance first. `skip` is for rapid testing ("complete the entire chain without playing through it").

All commands use tab-completion for online players and registered chain keys.

### Fail Cases

| Condition | Applies to | Behavior |
|---|---|---|
| Chain key not in registry | all | Error message: "No chain definition found for '{chain}'." No state change. |
| Player offline | all | Error message: "Player must be online." (Quest start/cancel requires Bukkit main thread interaction with the player entity.) |
| Player has no chain state for this chain | `advance`, `restart`, `skip` | Error message: "Player has no active or prior state for chain '{chain}'." |
| Player's chain state is terminal | `advance`, `restart`, `skip` | Error message: "Player's chain '{chain}' is in state {state} and cannot be advanced/restarted via this command. Use `reset` to clear it." |
| Player is on the last step | `advance` | Complete the chain — grants the final quest's rewards, sets chain state to `COMPLETED`, fires `QuestChainCompleteEvent`. Success message notes the chain is now finished. |
| All steps already in completion log | `restart` | Chain state set to `COMPLETED` immediately (all steps skipped). Message: "All steps already completed; chain marked complete." |
| Player has no chain state for this chain | `reset` | No-op with success message: "Player has no state for chain '{chain}' — nothing to reset." (Idempotent — not an error.) |

---

## Implementation Phases

### Phase 1 — Quest Engine Extensions + McCore Hook — **Implemented**

McCore (`1.0.0.17-SNAPSHOT`):
- `KeyedGui`, `CoreGuiOpenEvent` from `GuiManager.trackPlayerGui()`

McRPG (implemented):
- `PreQuestStartEvent` (online player only), `QuestDefinition.Builder`, `on-start-messages`, `OnStartMessage`
- `QuestStartMessageListener`, `QuestMessageDeliverer`, `QuestStartAutoCompleteListener` (starter-scoped, immediate)
- `QuestStartEvent` + `QuestSource` + `starterUUID`
- `AbilityType`, `AbilityObjectiveFilter`, objective index on `QuestDefinition`
- `Loadout` equip/unequip/swap + `LoadoutAbilityChangeEvent`, `LoadoutPositionSwapEvent`
- 4 reward types, 7 objective types, 6 progress listeners
- `KeyedGui` retrofit (22 GUI classes)
- Tests (see Phase 1 LLD section 7)

**Shippable value:** New objective/reward types work on any quest definition today. See [`REWARDS.md`](../../../src/main/resources/quests/REWARDS.md) and [`OBJECTIVES.md`](../../../src/main/resources/quests/OBJECTIVES.md).

### Phase 2 — Quest Chain System — **Implemented**

- `QuestChainDefinition`, `QuestChainStep`, `QuestChainRegistry`, `QuestChainManager`
- `QuestChainState` enum (ACTIVE, COMPLETED, ABANDONED, FAILED, EXPIRED)
- `QuestChainStateDAO` + `QuestChainCompletionLogDAO` + table creation
- `QuestChainConfigLoader` + YAML validation
- `QuestChainStartCondition` interface (extensible, no built-in conditions in initial release)
- `QuestChainProgressListener`, `QuestChainFirstJoinListener`, `QuestChainLoginListener`
- Chain lifecycle events (Start, StepAdvance, Complete, Abandon, Fail)
- `ContentHandlerType.QUEST_CHAIN` + content packs
- Eager reload behavior + Option C logging for removed definitions
- Repeat mode field (parsed, only `ONCE` functional initially)
- `on-quest-expire` field on step (parsed, only `fail-chain` functional initially)
- Admin commands (`/mcrpg quest chain reset/restart/advance/status`)
- Quest history GUI chain grouping (`QuestChainHistoryDetailGui`, `QuestChainHistorySlot`)
- Tests

**Shippable value:** The chain orchestration layer is fully functional. Third-party plugins can define chains.

### Phase 3 — Tutorial Content — **Implemented**

- `TutorialQuestSource` — non-abandonable quest source
- `DisableTutorialSetting` + `DisableTutorialConfirmGui` + setting slot
- `TutorialPreQuestStartListener` — gates tutorial starts via config toggle, bypass permission, and player setting
- `CascadeOrchestrator` + `CascadeContext` — same-tick recursive cascade batching with message deferral and batch summary
- `SoundRewardType` + `TitleRewardType` — experiential reward types
- `ChainSkipCommand` — admin command to force-complete all remaining chain steps
- `QuestChainFirstJoinListener` bypass permission check (`mcrpg.tutorial.bypass`)
- `ActiveQuestSlot` per-quest display-item via locale route + non-abandonable lore
- `config.yml` tutorial toggle + `<tutorial>` palette entry + permission nodes
- 7 tutorial quest YAML definitions + `chain.yml` (using `on-start-messages:`)
- Locale entries in `en_quest.yml` + `en_gui.yml`
- Chain admin command path restructure (`/mcrpg quest chain ...` — removed `admin` literal)
- `ChainResetCommand` resets `DisableTutorialSetting` for tutorial chains
- Tests

**Shippable value:** The tutorial goes live. Players get onboarded.

---

## File Changes Summary

### New Files -- Quest Chain System
- `quest/chain/QuestChainDefinition.java`
- `quest/chain/QuestChainStep.java`
- `quest/chain/QuestChainRegistry.java`
- `quest/chain/QuestChainManager.java`
- `quest/chain/QuestChainState.java` (enum: ACTIVE, COMPLETED, ABANDONED, FAILED, EXPIRED)
- `quest/chain/QuestChainRepeatMode.java` (enum: ONCE, UNLIMITED, COOLDOWN, LIMITED, COOLDOWN_LIMITED)
- `quest/chain/QuestChainStartCondition.java` (extensible condition interface)
- `quest/chain/QuestChainConfigLoader.java`
- `quest/chain/trigger/ChainAutoStartTrigger.java` (extensible trigger interface)
- `quest/chain/trigger/ChainAutoStartTriggerRegistry.java`
- `quest/chain/trigger/builtin/FirstJoinAutoStartTrigger.java`
- `quest/chain/trigger/builtin/LoginAutoStartTrigger.java`
- `quest/chain/trigger/builtin/ManualAutoStartTrigger.java`
- `expansion/content/QuestChainContentPack.java`
- `expansion/content/ChainAutoStartTriggerContentPack.java`
- `listener/quest/QuestChainProgressListener.java`
- `listener/quest/QuestChainFirstJoinListener.java`
- `listener/quest/QuestChainLoginListener.java`
- `database/table/quest/QuestChainStateDAO.java`
- `database/table/quest/QuestChainCompletionLogDAO.java`
- `command/admin/chain/ChainResetCommand.java`
- `command/admin/chain/ChainRestartCommand.java`
- `command/admin/chain/ChainAdvanceCommand.java`
- `command/admin/chain/ChainStatusCommand.java`
- `command/admin/chain/ChainAdminCommandBase.java`
- `command/admin/chain/ChainKeyParser.java`
- `gui/quest/QuestChainHistoryDetailGui.java`
- `gui/quest/slot/QuestChainHistorySlot.java`

### New Files -- Phase 1 (implemented)

**Events:**
- `event/quest/PreQuestStartEvent.java`
- `event/loadout/LoadoutAbilityChangeEvent.java`
- `event/loadout/LoadoutPositionSwapEvent.java`

**Quest definition / messaging:**
- `quest/definition/OnStartMessage.java`
- `quest/message/QuestMessageDeliverer.java`

**Ability classification:**
- `ability/AbilityType.java`
- `quest/objective/type/builtin/AbilityObjectiveFilter.java`

**Reward types:**
- `quest/reward/builtin/MessageRewardType.java`
- `quest/reward/builtin/BoostedExperienceRewardType.java`
- `quest/reward/builtin/RedeemableExperienceRewardType.java`
- `quest/reward/builtin/RedeemableLevelsRewardType.java`

**Objective types + contexts:**
- `SkillLevelUpObjectiveType`, `SkillTargetLevelObjectiveType`, `GuiOpenObjectiveType`
- `AbilityUnlockObjectiveType`, `AbilityActivateObjectiveType`, `LoadoutEquipObjectiveType`, `QuestBoardAcceptObjectiveType`
- Contexts: `SkillLevelQuestContext`, `GuiOpenQuestContext`, `AbilityUnlockQuestContext`, `AbilityActivateQuestContext`, `LoadoutEquipQuestContext`, `QuestBoardAcceptQuestContext`

**Progress listeners:**
- `SkillLevelQuestProgressListener` (both skill types)
- `GuiOpenQuestProgressListener`, `AbilityUnlockQuestProgressListener`, `AbilityActivateQuestProgressListener`
- `LoadoutEquipQuestProgressListener`, `QuestBoardAcceptQuestProgressListener`
- `QuestStartMessageListener`, `QuestStartAutoCompleteListener`

**Tests:** Matching `*Test.java` under `src/test/java/...` (see Phase 1 LLD).

### New Files -- Phase 2 (chain system, implemented)

- `event/quest/QuestChainStartEvent.java`, `QuestChainStepAdvanceEvent.java`, `QuestChainCompleteEvent.java`
- `event/quest/QuestChainAbandonEvent.java`, `QuestChainFailEvent.java`
- `quest/chain/*` (definition, manager, registry, DAOs, triggers, listeners, commands, GUI)

### New Files -- Phase 3 (tutorial content, implemented)

**Source + Setting (2):**
- `quest/source/builtin/TutorialQuestSource.java`
- `setting/impl/DisableTutorialSetting.java`

**Reward Types (2):**
- `quest/reward/builtin/SoundRewardType.java`
- `quest/reward/builtin/TitleRewardType.java`

**GUI (5):**
- `gui/setting/slot/DisableTutorialSettingSlot.java`
- `gui/tutorial/DisableTutorialConfirmGui.java`
- `gui/tutorial/slot/DisableTutorialConfirmSlot.java`
- `gui/tutorial/slot/DisableTutorialInfoSlot.java`
- `gui/tutorial/slot/DisableTutorialCancelSlot.java`

**Listener (1):**
- `listener/quest/TutorialPreQuestStartListener.java`

**Chain infrastructure (2):**
- `quest/chain/CascadeOrchestrator.java`
- `quest/chain/CascadeContext.java`

**Command (1):**
- `command/admin/chain/ChainSkipCommand.java`

**Quest definitions (8):**
- `src/main/resources/quests/tutorial/chain.yml`
- `src/main/resources/quests/tutorial/first_steps.yml`
- `src/main/resources/quests/tutorial/mcrpg_menu.yml`
- `src/main/resources/quests/tutorial/natural_talent.yml`
- `src/main/resources/quests/tutorial/your_arsenal.yml`
- `src/main/resources/quests/tutorial/unleashed_power.yml`
- `src/main/resources/quests/tutorial/combo_strike.yml`
- `src/main/resources/quests/tutorial/quest_board.yml`
- Locale entries in `en_quest.yml` + `en_gui.yml` for all tutorial text

### Modified Files -- Phase 1 (implemented)

- `QuestDefinition.java` — builder-only; `onStartMessages`; `objectiveIndex` / `findObjectiveDefinition()`
- `QuestConfigLoader.java` — `parseOnStartMessages()`; builder migration
- `QuestManager.java` — `PreQuestStartEvent` when player online
- `QuestInstance.java` — `start(definition, starterUUID)` fires `QuestStartEvent`
- `QuestObjectiveType.java` — `checkAutoComplete()` default
- `Ability.java` — `getAbilityType()`; `PassiveAbility` / `ActiveAbility` — removed `isPassive()` defaults
- `Loadout.java`, `LoadoutAbilityDAO.java` — private mutations; constructor-based DAO load
- `McRPGExpansion.java`, `McRPGListenerRegistrar.java`, `LocalizationKey.java`, `en_quest.yml`
- All 22 GUI classes — `KeyedGui` + `GUI_KEY`
- `quests/OBJECTIVES.md`, `quests/REWARDS.md` — developer guides updated
- Loadout GUI slots, `OnAbilityUnlockListener` — use `equipAbility()` / `swapAbility()`

### Modified Files -- Phase 2 + 3 (implemented)

- `ContentHandlerType.java`, `McRPGRegistryKey.java`, `McRPGManagerKey.java` — chain registry keys
- `config.yml` — `tutorial.enabled` toggle + `palette.tutorial` entry
- `DatabaseManager` — chain state + completion log tables
- `QuestHistoryGui.java` — chain grouping
- `plugin.yml` — tutorial bypass, chain admin, and skip permissions
- `quest/chain/QuestChainManager.java` — compose `CascadeOrchestrator`, expose via getter
- `listener/quest/QuestStartMessageListener.java` — cascade deferral via `CascadeOrchestrator`
- `listener/quest/QuestChainFirstJoinListener.java` — bypass permission check
- `gui/quest/slot/ActiveQuestSlot.java` — per-quest display-item via locale route + non-abandonable lore
- `expansion/McRPGExpansion.java` — register source + setting + reward types
- `bootstrap/McRPGListenerRegistrar.java` — register `TutorialPreQuestStartListener`
- `configuration/file/MainConfigFile.java` — `TUTORIAL_ENABLED` route
- `configuration/file/localization/LocalizationKey.java` — all new route constants
- `command/admin/chain/ChainResetCommand.java` — reset `DisableTutorialSetting` on tutorial chain reset
- `command/admin/chain/ChainAdvanceCommand.java` — remove `admin` literal, delegate to `CascadeOrchestrator`
- `command/admin/chain/ChainRestartCommand.java` — remove `admin` literal from command path
- `command/admin/chain/ChainStatusCommand.java` — remove `admin` literal from command path
- `bootstrap/McRPGCommandRegistrar.java` — register `ChainSkipCommand`

### McCore (shipped in `1.0.0.17-SNAPSHOT`)

- `KeyedGui`, `CoreGuiOpenEvent`, `GuiManager.trackPlayerGui()` event firing

---

## Related Documents

- [Phase 1 LLD — Quest Engine Extensions](../../lld/tutorial-quest-system/phase-1-quest-engine-extensions.md) (implemented)
- [Phase 2 LLD — Quest Chain System](../../lld/tutorial-quest-system/phase-2-quest-chain-system.md) (implemented)
- [Phase 3 LLD — Tutorial Content](../../lld/tutorial-quest-system/phase-3-tutorial-content.md) (implemented)
- [Quest OBJECTIVES.md](../../../src/main/resources/quests/OBJECTIVES.md) — YAML reference for objective types
- [Quest REWARDS.md](../../../src/main/resources/quests/REWARDS.md) — YAML reference for reward types
- [Quest System Architecture](../quest/quest-system-architecture.md)
- [Quest Board Feature Design](../quest/quest-board.md)
- [Mana & Ability Activation System](../mana/mana-ability-system.md)
- [GUI/UX System & Color Palette](../gui-ux-system.md)
- [Chain System Backlog](chain-system-backlog.md) — deferred features; section 8 covers post-Phase-1 AbilityType follow-ups
