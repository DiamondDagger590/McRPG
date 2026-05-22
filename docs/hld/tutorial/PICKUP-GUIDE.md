# Tutorial Quest System -- Pickup Guide

> **Purpose:** Context document for resuming this work with a fresh agent.
> **Created:** 2026-05-22
> **Prior conversation:** [Tutorial Quest Design](1e533846-f781-4097-89e8-5c4dd0cea41e)

---

## What This Is

We are designing and implementing a **tutorial quest chain** for McRPG -- a Minecraft RPG plugin. The chain walks new players through the core gameplay systems (skills, abilities, loadouts, combos, quest board) via an auto-advancing sequence of simple quests that blend naturally into gameplay.

This requires building several new infrastructure systems first (quest chains, new events, new objective/reward types), then writing the tutorial content on top.

---

## Documents to Read

Read these in order:

1. **[tutorial-quest-system.md](tutorial-quest-system.md)** -- The full HLD. Contains all architecture decisions, the quest chain system design, all 7 tutorial quests with objectives and rewards, YAML schemas, file change lists, and infrastructure specifications.

2. **[tutorial-review-findings.md](tutorial-review-findings.md)** -- 26 review findings from four persona reviews (GUI/UX, Server Owner, Architecture, Extensibility), ordered by criticality. The "Critical" items (1-5) are baked into the HLD already. The "Important" items (6-15) should be addressed during implementation. The "Lower Priority" items (16-26) are polish.

3. **Project rules at `.cursor/rules/core.mdc`** and **`CLAUDE.md`** -- Essential project conventions, naming patterns, anti-patterns, and architecture patterns. Any implementation must follow these.

---

## Current State

- **Design:** Complete. The HLD covers all infrastructure and content.
- **Review:** Four-persona review complete. 26 findings documented. Critical items resolved in the HLD.
- **Implementation:** Not started. Zero code has been written.

---

## Implementation Order (suggested)

The work has natural dependency layers. Here is the recommended build order:

### Layer 1 -- Core Infrastructure (no dependencies on each other)

These can be built in any order or in parallel:

1. **PreQuestStartEvent** -- New cancellable event. Fire from `QuestManager.startQuest()`. See HLD section 2.
2. **On-start rewards on QuestDefinition** -- Add `onStartRewardEntries` field. Parse in `QuestConfigLoader`. Fire via `QuestStartRewardListener`. See HLD section 3.
3. **New Bukkit events** -- `McRPGGuiOpenEvent`, `AbilityUnlockEvent`, `LoadoutAbilityEquipEvent`. See HLD section 6 (events subsection).

### Layer 2 -- Quest Chain System (depends on Layer 1 events)

4. **Quest Chain System** -- `QuestChainDefinition`, `QuestChainStep`, `QuestChainRegistry`, `QuestChainManager`, `QuestChainState`, `QuestChainConfigLoader`, `QuestChainStateDAO`, `QuestChainProgressListener`, `QuestChainFirstJoinListener`. See HLD section 1.

### Layer 3 -- Reward Types (independent)

5. **MessageRewardType** -- See HLD section 4.
6. **BoostedExperienceRewardType** -- See HLD section 7.
7. **RedeemableExperienceRewardType + RedeemableLevelsRewardType** -- See HLD section 8.

### Layer 4 -- Objective Types (depends on Layer 1 events)

8. **All 7 objective types** -- See HLD section 6 table. Each needs: objective type class, progress context class, progress listener, registration in `McRPGExpansion`.

### Layer 5 -- Tutorial Content (depends on everything above)

9. **TutorialQuestSource** -- See HLD section 5.
10. **DisableTutorialSetting** -- See HLD section 9.
11. **TutorialPreQuestStartListener** -- Cancels tutorial starts for opted-out players.
12. **Tutorial YAML files** -- 7 quest definitions + chain.yml + locale entries.

### Layer 6 -- Tests

13. **Unit tests** for all new types, following existing test patterns in `src/test/java/`.

**Important:** The entire test suite must pass before the work is considered complete. Run `./gradlew verifiedShadowJar` and verify zero failures.

---

## Key Design Decisions Already Made

These decisions were discussed and agreed upon. Don't revisit unless there's a blocking issue:

1. **Quest chains are a first-class concept** with their own registry, manager, and SQL persistence. Not a reward-type hack.
2. **YAML uses map format**, not lists. Server owners find lists too technical.
3. **PreQuestStartEvent fires from QuestManager**, not QuestInstance. Instance.start() is pure state mutation.
4. **MessageRewardType** supports both locale route lookup and inline MiniMessage strings with palette resolution.
5. **Tutorial is non-abandonable** (`TutorialQuestSource.isAbandonable() = false`). Players opt out via a player setting that permanently abandons the chain.
6. **Auto-complete** is supported -- if a player already meets an objective on quest start, it completes immediately. Rapid-fire auto-completes are batched.
7. **McRPGGuiOpenEvent** fires from `GuiManager.trackPlayerGui()` (single centralized point).
8. **GuiOpenObjectiveType** uses `NamespacedKey` for gui-type, not a Java enum (extensibility).
9. **Chain state persists current quest as NamespacedKey** (not index) to handle chain reconfiguration.

---

## Open Questions (lower priority, can decide during implementation)

These were raised in the review but don't have final answers:

- **Review item 22:** Timezone handling for `TimeGateChainCondition` -- default to server timezone when no offset specified?
- **Review item 26:** Should admin tutorial reset also clear completion log entries? (Current lean: yes, so rewards re-grant on re-completion)
- **Reward tuning:** Exact XP/level amounts in the HLD are initial estimates. Will need playtesting.

---

## Quick Reference -- Existing Patterns to Follow

| What you're building | Existing example to reference |
|---|---|
| New objective type | `BlockBreakObjectiveType`, `MobKillObjectiveType` in `quest/objective/type/builtin/` |
| New reward type | `ExperienceRewardType`, `CommandRewardType` in `quest/reward/builtin/` |
| New quest source | `BoardPersonalQuestSource`, `AbilityUpgradeQuestSource` in `quest/source/` |
| New Bukkit event | `QuestStartEvent`, `QuestCompleteEvent` in `event/quest/` |
| New progress listener | `BlockBreakQuestProgressListener`, `MobKillQuestProgressListener` in `listener/quest/` |
| New content pack | `QuestObjectiveTypeContentPack`, `QuestRewardTypeContentPack` in `expansion/content/` |
| New DAO | `QuestInstanceDAO`, `QuestChainStateDAO` pattern in `database/table/quest/` |
| New player setting | Existing settings in `setting/impl/` |
| New registry key | `McRPGRegistryKey` enum |
| New manager key | `McRPGManagerKey` enum |
