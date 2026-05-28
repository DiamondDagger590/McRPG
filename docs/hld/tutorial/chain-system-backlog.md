# Quest Chain System -- Backlog (Design Now, Build Later)

> **Created:** 2026-05-23
> **Status:** Designed, not yet scheduled
> **Prerequisite:** Phase 2 (Quest Chain System) must be complete first
> **Purpose:** Detailed design for features that are schema-ready but not functionally implemented in the initial release. Each section is cut-ready for a GitHub issue.

---

## 1. Availability Windows

**Summary:** Chains (and optionally quest board templates) can define time-based availability windows that control when the chain can be started and what happens to active instances when the window closes.

### YAML Schema

```yaml
key: mcrpg:christmas_event
source: mcrpg:manual
repeat-mode: unlimited
auto-start:
  trigger: login
availability:
  windows:
    holiday-period:
      from: "--12-01T00:00:00"    # MonthDay-based recurring (no year = yearly)
      until: "--01-03T23:59:59"
  timezone: "America/New_York"
  on-window-close: expire-active  # expire-active | allow-finish | expire-with-grace
  grace-period: 48h               # only used with expire-with-grace
```

**Window format:**
- Full ISO-8601 (`2026-12-01T00:00:00`) — one-shot events
- Partial date without year (`--12-01T00:00:00`) — yearly recurring
- Timezone is explicit per-chain (defaults to server timezone if omitted)
- Map-based keys (server owners name them: `holiday-period`, `summer-event`, etc.)

### On-Window-Close Policies

| Policy | Behavior |
|--------|----------|
| `expire-active` | When window closes, cancel all active instances for this chain. Chain state → `EXPIRED`. |
| `allow-finish` | Block new starts but let active chains run to completion. Gentle. |
| `expire-with-grace` | Like `expire-active` but delayed by `grace-period`. Players get a warning message when grace period starts. |

### Implementation Notes

- `ChainAvailabilityChecker` — scheduled task (configurable interval, default 60s) that:
  - Checks all chains with `availability` sections
  - For chains whose window just closed: apply the `on-window-close` policy
  - For chains whose window just opened: no action (auto-start triggers handle new starts via `login` trigger)
- Login listener checks availability before allowing chain auto-start
- `QuestChainManager.isChainAvailable(NamespacedKey chainKey)` — public API for programmatic checks
- Grace period: when a window-close is detected and policy is `expire-with-grace`, schedule a delayed task. Send a warning message to affected players immediately. After grace period, apply `expire-active`.

### Quest Board Integration

The same `availability:` section can be added to `QuestTemplate` definitions. The board generation logic (`PersonalOfferingGenerator`, `SlotGenerationLogic`) checks `isTemplateAvailable()` before including a template in the pool. This enables seasonal board quests without needing chains.

```yaml
# In a quest template file
templates:
  christmas_gathering:
    availability:
      windows:
        holiday:
          from: "--12-01T00:00:00"
          until: "--01-03T23:59:59"
      timezone: "America/New_York"
    # ... rest of template definition
```

---

## 2. Chain Repeatability (beyond ONCE)

**Summary:** Chains support multiple completion modes. The schema columns (`completion_count`, `last_completed_at`) are present from Phase 2; this work makes them functional.

### Repeat Modes

| Mode | YAML | Behavior |
|------|------|----------|
| `ONCE` | `repeat-mode: once` | Single completion, permanently terminal. (Tutorial) |
| `UNLIMITED` | `repeat-mode: unlimited` | Re-startable immediately after any terminal state except `ABANDONED`. |
| `COOLDOWN` | `repeat-mode: cooldown` + `repeat-cooldown: 168h` | Re-startable after cooldown from `last_completed_at`. |
| `LIMITED` | `repeat-mode: limited` + `max-completions: 5` | Completable N times. After N, permanently terminal. |
| `COOLDOWN_LIMITED` | `repeat-mode: cooldown-limited` + both fields | Both constraints apply. |

### Re-start Flow

1. Chain reaches a terminal state (`COMPLETED`, `FAILED`, `EXPIRED`)
2. On next trigger evaluation (login, manual API call):
   - Check repeat mode allows another attempt
   - Check cooldown (if applicable): `now - last_completed_at >= repeat-cooldown`
   - Check completion count (if applicable): `completion_count < max-completions`
   - Check availability window (if applicable)
3. If all pass: reset `current_quest` to first step, set state to `ACTIVE`, increment nothing yet (count increments on completion)

### Edge Cases

- `ABANDONED` is always terminal regardless of repeat mode (player explicitly opted out)
- Cooldown is computed from `last_completed_at`, not from when the chain was started
- If a chain is `EXPIRED` due to window close and the window reopens, the repeat check fires on next login trigger — player restarts from step 1

---

## 3. Quest Expiration Within Chains (retry / restart-chain)

**Summary:** When a chain-managed quest expires, the chain can respond with behaviors beyond `fail-chain`.

### Step-Level Configuration

```yaml
steps:
  timed_challenge:
    quest: mcrpg:christmas_gift_rush
    on-quest-expire: retry
    max-retries: 3          # -1 or omit for unlimited retries
  final_boss:
    quest: mcrpg:christmas_boss_fight
    on-quest-expire: fail-chain
  optional_bonus:
    quest: mcrpg:bonus_task
    on-quest-expire: skip   # future: advance past this step
```

### Behaviors

| Behavior | Effect |
|----------|--------|
| `fail-chain` | Chain state → `FAILED`. Active quest stays cancelled. (Default) |
| `retry` | Cancel the expired quest. Re-start the same quest immediately. Decrement retry counter. If retries exhausted, fall through to `fail-chain`. |
| `restart-chain` | Cancel the expired quest. Reset chain to step 1. All completion log entries for this chain's quests are cleared. Effectively a full reset. |
| `skip` | Advance to the next step as if the quest completed (but no completion rewards are granted for the skipped quest). |

### Implementation Notes

- `QuestChainProgressListener` listens for `QuestExpireEvent` in addition to `QuestCompleteEvent`
- On expire: look up the chain and step, check `on-quest-expire` behavior
- Retry counter stored transiently on `QuestChainManager` (in-memory map of `(player, chain, step) → retries_remaining`). Not persisted — server restart resets retry counters (intentional: avoids permanent lockout if a quest is misconfigured with too-short duration)
- `restart-chain` triggers `QuestChainRestartEvent` (new lifecycle event)

---

## 4. Availability Windows on Quest Board Templates

**Summary:** Board templates can have their own availability windows independent of chains. Seasonal quests appear on the board only during their configured periods.

### Design

- Add optional `availability` section to `QuestTemplate` YAML schema (same format as chain availability)
- `QuestPool` filters templates by `isTemplateAvailable()` before weighted selection
- No "on-window-close" concept for board quests — they simply stop appearing in new rotations. Existing accepted instances follow normal quest expiration rules.
- This is additive to the existing template condition system (conditions gate generation; availability gates pool inclusion)

---

## 5. Chain Lifecycle — Additional Events

**Summary:** Additional events for third-party plugin integration beyond the initial Start/StepAdvance/Complete set.

| Event | Fires when |
|-------|------------|
| `QuestChainFailEvent` | Chain transitions to `FAILED` state |
| `QuestChainExpireEvent` | Chain transitions to `EXPIRED` state (window close) |
| `QuestChainRestartEvent` | A repeatable chain is re-started from step 1 |
| `QuestChainStepRetryEvent` | A step's quest is retried after expiration |

All carry the chain definition, player, and relevant context. All are non-cancellable (the state transition has already occurred — these are notification events).

---

## 6. Built-in Chain Start Conditions (TimeGateChainCondition)

**Summary:** The `QuestChainStartCondition` interface ships in Phase 2 but with no built-in implementations. This ticket adds the first built-in condition: time-gating individual chain steps.

### Design

```yaml
# quests/monthly_event/chain.yml
key: mcrpg:monthly_event
source: mcrpg:manual
steps:
  part_one:
    quest: mcrpg:event_part_1
  part_two:
    quest: mcrpg:event_part_2
    conditions:
      time-gate:
        type: mcrpg:time_gate
        after: "2026-06-15T00:00:00"
        timezone: "America/New_York"
```

- `TimeGateChainCondition` implements `QuestChainStartCondition`
- `evaluate(player)` returns `true` if current time is after the configured timestamp
- Timezone defaults to server timezone when omitted (avoids server owner confusion)
- Registered via `QuestChainStartConditionContentPack`
- When a step's condition is not met, the chain manager does not advance. On player login, conditions are re-evaluated (for `login`-triggered chains).

---

## 7. Content Expansion Introspection Commands

**Summary:** Admin/debug commands for listing all registered content expansions, their content packs, and the keys each pack contributed. Essential for debugging registration issues, verifying third-party expansions loaded correctly, and understanding which expansion owns which content.

### Commands

| Command | Output |
|---------|--------|
| `/mcrpg admin content expansions` | Lists all registered `ContentExpansion` instances by name/key |
| `/mcrpg admin content packs <expansion>` | Lists all content packs provided by a specific expansion (e.g., `QuestObjectiveTypeContentPack`, `AbilityContentPack`) |
| `/mcrpg admin content keys <content-pack-type>` | Lists all registered keys for a content pack type across ALL expansions (e.g., all objective type keys from every expansion) |
| `/mcrpg admin content keys <content-pack-type> <expansion>` | Lists registered keys for a content pack type from a SPECIFIC expansion only |

### Examples

```
> /mcrpg admin content expansions
Registered expansions:
  - mcrpg (McRPGExpansion) — 14 content packs
  - myplugin (MyPluginExpansion) — 3 content packs

> /mcrpg admin content packs mcrpg
Content packs for 'mcrpg':
  - AbilityContentPack (12 entries)
  - SkillContentPack (4 entries)
  - QuestObjectiveTypeContentPack (9 entries)
  - QuestRewardTypeContentPack (10 entries)
  - QuestSourceContentPack (4 entries)
  - ChainAutoStartTriggerContentPack (3 entries)
  ...

> /mcrpg admin content keys QuestObjectiveTypeContentPack
All registered quest objective types:
  - mcrpg:block_break (mcrpg)
  - mcrpg:mob_kill (mcrpg)
  - mcrpg:skill_level_up (mcrpg)
  - mcrpg:gui_open (mcrpg)
  - myplugin:fishing (myplugin)
  ...

> /mcrpg admin content keys QuestRewardTypeContentPack mcrpg
Quest reward types from 'mcrpg':
  - mcrpg:experience
  - mcrpg:command
  - mcrpg:message
  - mcrpg:boosted_experience
  ...
```

### Implementation Notes

- Permission: `mcrpg.admin.content` (default: op)
- Tab-completion for expansion names, content pack type names, and where applicable content keys
- Output uses pagination or clickable "next page" for large lists
- Content pack type argument uses the simple class name (e.g., `QuestObjectiveTypeContentPack`) or a short alias (`objective-types`, `reward-types`, `abilities`, etc.)
- Requires `ContentExpansionManager` to expose introspection methods: `getRegisteredExpansions()`, `getContentPacks(expansion)`, `getKeysForPack(packType)`, `getKeysForPack(packType, expansion)`

---

## 8. AbilityType Refactor Deferred Items

**Summary:** Follow-up work identified during the AbilityType enum refactor audit (post Phase 1 quest engine extensions). Not regressions from the refactor itself.

| Item | Rationale |
|------|-----------|
| **`LoadoutHolder.getAvailableDefaultAbilities()` partial migration** | Private method with semantically different intent (non-unlockable abilities). Migrating it to `AbilityType` would change behavior. Defer to a separate ticket. |
| **`resolveAbilityName` SRP concern** | Name resolution lives on `AbilityObjectiveFilter` alongside filter matching, with global registry/logger access. Extracting to a dedicated collaborator would touch all three ability objective types. Defer. |
| **New `LoadoutHolder`/filter tests** | `getAvailableActiveAbilities()`, `PassiveAbilityFilter`, and `ActiveAbilityFilter` lack dedicated tests. Coverage gaps for pre-existing untested code, not regressions from the refactor. Defer to a broader test coverage ticket. |

---

## Issue Cutting Guide

Each numbered section above maps to one GitHub issue. Suggested labels and dependencies:

| # | Title | Labels | Depends On |
|---|-------|--------|------------|
| 1 | Implement chain availability windows | `feature`, `quest-chain`, `scheduling` | Phase 2 complete |
| 2 | Implement chain repeat modes (beyond ONCE) | `feature`, `quest-chain` | Phase 2 complete |
| 3 | Implement quest expiration behaviors within chains (retry/restart/skip) | `feature`, `quest-chain` | Phase 2 complete |
| 4 | Add availability windows to quest board templates | `feature`, `quest-board`, `scheduling` | #1 (shared availability logic) |
| 5 | Add chain lifecycle events (fail/expire/restart/retry) | `feature`, `quest-chain`, `extensibility` | #2, #3 |
| 6 | Implement TimeGateChainCondition | `feature`, `quest-chain`, `extensibility` | Phase 2 complete |
| 7 | Content expansion introspection commands | `feature`, `admin`, `extensibility` | None (independent) |
