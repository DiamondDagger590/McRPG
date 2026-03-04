# McRPG Achievements Integration - High-Level Design

## Overview

McRPG integrates with Runic Achievements (RA) via a **soft dependency**. When RA is present, McRPG registers game-specific achievements and custom reward types. When RA is absent, McRPG still tracks statistics via McCore - they just don't power any achievements.

This document covers McRPG's side of the integration. For the core achievements system design, see the [McCore achievements HLD](https://github.com/DiamondDagger590/McCore/tree/develop/docs/hld/achievements).

---

## Dependency Relationship

```
McCore (statistics framework)
  ├── Runic Achievements (hard depends on McCore)
  └── McRPG (hard depends on McCore, soft depends on Runic Achievements)
        ├── Always: Registers and increments McCore statistics during gameplay
        └── When RA present: Registers achievements + reward types into RA
```

```yaml
# McRPG plugin.yml
depend: [McCore]
softdepend: [RunicAchievements]
```

---

## StatisticRepository Implementation

McRPG registers a `StatisticRepository` with RA so that RA can bulk-query McRPG's statistic values for achievement evaluation. This is the primary integration point.

The repository:
- Closes over McRPG's `Database` reference at registration time
- Runs queries on McRPG's DB thread pool (not RA's)
- Returns a `CompletableFuture` so RA can query multiple plugins in parallel

```java
// McRPG registers this during bootstrap when RA is present
Database mcRPGDatabase = McRPG.getInstance().getMcRPGDatabase();

raHook.registerRepository(new StatisticRepository() {
    @Override
    public CompletableFuture<Map<UUID, Map<StatisticKey, Number>>> getBulkValues(
            Set<UUID> playerUUIDs, Set<StatisticKey> statisticKeys) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = mcRPGDatabase.getConnection()) {
                return PlayerStatisticDAO.getBulkStatistics(conn, playerUUIDs, statisticKeys);
            }
        }, mcRPGDatabase.getExecutor());
    }
});
```

This enables RA to retroactively evaluate achievements: when a new achievement is added that references an existing statistic (e.g., `mcrpg:blocks_mined`), RA's polling loop queries the repository and awards it to players who already meet the threshold.

---

## Soft Dependency Hook

```
us.eunoians.mcrpg.external.runic.RunicAchievementsHook extends PluginHook
├── isRunicAchievementsPresent(): boolean
├── registerStatisticRepository()              // registers the StatisticRepository with RA
├── registerMcRPGAchievements()                // registers game-specific achievements
├── registerMcRPGRewardTypes()                 // registers McRPG reward types
```

```java
// In McRPG bootstrap
if (Bukkit.getPluginManager().isPluginEnabled("RunicAchievements")) {
    RunicAchievementsHook hook = new RunicAchievementsHook();
    hook.registerStatisticRepository();
    hook.registerMcRPGAchievements();
    hook.registerMcRPGRewardTypes();
}
```

---

## McRPG Statistics (Registered into McCore)

These are registered **regardless** of whether RA is present. They're McCore statistics that McRPG increments during gameplay.

```
us.eunoians.mcrpg.statistic.McRPGStatistics
├── BLOCKS_MINED (mcrpg:blocks_mined, LONG)
├── ORES_MINED (mcrpg:ores_mined, LONG)
├── TREES_CHOPPED (mcrpg:trees_chopped, LONG)
├── CROPS_HARVESTED (mcrpg:crops_harvested, LONG)
├── MOBS_KILLED (mcrpg:mobs_killed, LONG)
├── DAMAGE_DEALT (mcrpg:damage_dealt, DOUBLE)
├── DAMAGE_TAKEN (mcrpg:damage_taken, DOUBLE)
├── ABILITIES_ACTIVATED (mcrpg:abilities_activated, LONG)
├── SKILL_LEVELS_GAINED (mcrpg:skill_levels_gained, LONG)
├── TOTAL_SKILL_EXPERIENCE (mcrpg:total_skill_experience, LONG)
├── QUESTS_COMPLETED (mcrpg:quests_completed, LONG)
├── BLEED_PROCS (mcrpg:bleed_procs, LONG)
├── ... per-skill and per-ability stats
```

```java
// In McRPG's block break listener
player.getStatisticData().incrementLong(McRPGStatistics.BLOCKS_MINED.getKey(), 1);
```

---

## McRPG-Specific Achievements (Registered into RA)

Defined in McRPG's codebase, registered into RA when present:

```
us.eunoians.mcrpg.external.runic.achievement
├── MasterMiner extends StatisticAchievement      // blocks mined
├── BladeMaster extends StatisticAchievement       // mobs killed with sword
├── LumberjackLegend extends StatisticAchievement  // trees chopped
├── HarvestKing extends StatisticAchievement       // crops harvested
├── BloodThirsty extends StatisticAchievement      // bleed procs
├── SkillSavant extends StatisticAchievement       // total skill levels gained
├── QuestConqueror extends StatisticAchievement    // quests completed
├── ... more McRPG-specific achievements
```

---

## McRPG-Specific Reward Types (Registered into RA)

```
us.eunoians.mcrpg.external.runic.reward
├── ExperienceReward implements AchievementReward  // type: "mcrpg_experience"
├── LevelReward implements AchievementReward       // type: "mcrpg_levels"
└── UpgradePointReward implements AchievementReward // type: "mcrpg_upgrade_points"
```

Registered into RA's `AchievementRewardRegistry` so they can be used in YAML configs:

```yaml
rewards:
  - type: mcrpg_experience
    skill: "mcrpg:mining"
    amount: 500
```

RA has no idea what "skill experience" is. McRPG registers the reward type, RA just invokes it.

---

## Data Migration

Existing McRPG data (skill XP, levels) predates the statistics system. A one-time migration task populates statistics from existing data:

- Total skill XP → `mcrpg:total_mining_xp`, etc.
- Skill levels → `mcrpg:skill_levels_gained`
- Run on first boot after update, flag as complete in config

---

## Implementation Phases

### Phase 1: Statistics Integration (no RA dependency)
1. Define `McRPGStatistics` constants
2. Register stats in McRPG's bootstrap
3. Add listeners that increment stats during gameplay
4. Add PAPI placeholders for McRPG statistics
5. Wire up loading/saving in `McRPGPlayerLoadTask` and `McRPGPlayer.savePlayer()`
6. One-time migration task

### Phase 2: Runic Achievements Integration (when RA exists)
1. `RunicAchievementsHook` (soft dependency hook)
2. `StatisticRepository` implementation + registration
3. McRPG-specific achievements (`MasterMiner`, `BladeMaster`, etc.)
4. McRPG-specific reward types (`ExperienceReward`, `LevelReward`, `UpgradePointReward`)
5. Registration logic in McRPG bootstrap
