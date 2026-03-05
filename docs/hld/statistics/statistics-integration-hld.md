# McRPG Statistics Integration - High-Level Design

## Overview

McRPG uses McCore's statistics framework to track meaningful gameplay metrics — blocks mined, mobs killed, abilities activated, skill levels gained, and more. These statistics are registered during McRPG's bootstrap, incremented by McRPG's existing event listeners, and persisted through McCore's database layer.

Statistics are tracked **independently of any achievements system**. When a future Runic Achievements plugin is present, it can react to `PostStatisticModifyEvent` and query statistics for retroactive evaluation. When it's absent, statistics are still tracked and available via PAPI placeholders.

For the core statistics framework design, see the [McCore Statistics Framework HLD](https://github.com/DiamondDagger590/McCore/blob/develop/docs/hld/statistics/statistics-framework-hld.md).

---

## Dependency Relationship

```
McCore (statistics framework)
  ├── Future: Runic Achievements (hard depends on McCore, queries stats)
  └── McRPG (hard depends on McCore)
        ├── Registers McRPG-specific statistics during bootstrap
        ├── Increments statistics in existing gameplay listeners
        └── Provides PAPI placeholders for McRPG stats
```

```yaml
# McRPG plugin.yml — no change needed, McCore is already a hard dependency
depend: [McCore]
```

---

## McRPG Statistics Constants

All McRPG statistics are defined as constants in a single class, following the pattern of `McRPGSetting` for player settings and `FileType` for config files.

```
us.eunoians.mcrpg.statistic.McRPGStatistics
```

### Global Gameplay Statistics

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `BLOCKS_MINED` | `mcrpg:blocks_mined` | LONG | Total blocks mined (any block that grants Mining XP) |
| `ORES_MINED` | `mcrpg:ores_mined` | LONG | Ore blocks specifically |
| `TREES_CHOPPED` | `mcrpg:trees_chopped` | LONG | Logs broken (any log that grants WoodCutting XP) |
| `CROPS_HARVESTED` | `mcrpg:crops_harvested` | LONG | Crops harvested (any block that grants Herbalism XP) |
| `MOBS_KILLED` | `mcrpg:mobs_killed` | LONG | Mobs killed via Swords combat |
| `DAMAGE_DEALT` | `mcrpg:damage_dealt` | DOUBLE | Total damage dealt in Swords combat |
| `DAMAGE_TAKEN` | `mcrpg:damage_taken` | DOUBLE | Total damage taken |

### Skill Progression Statistics

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `TOTAL_SKILL_LEVELS_GAINED` | `mcrpg:total_skill_levels_gained` | LONG | Sum of all levels across all skills |
| `TOTAL_SKILL_EXPERIENCE` | `mcrpg:total_skill_experience` | LONG | Sum of all XP earned across all skills |
| `MINING_EXPERIENCE` | `mcrpg:mining_experience` | LONG | Total Mining XP earned |
| `SWORDS_EXPERIENCE` | `mcrpg:swords_experience` | LONG | Total Swords XP earned |
| `HERBALISM_EXPERIENCE` | `mcrpg:herbalism_experience` | LONG | Total Herbalism XP earned |
| `WOODCUTTING_EXPERIENCE` | `mcrpg:woodcutting_experience` | LONG | Total WoodCutting XP earned |
| `MINING_LEVELS` | `mcrpg:mining_levels` | INT | Current Mining level |
| `SWORDS_LEVELS` | `mcrpg:swords_levels` | INT | Current Swords level |
| `HERBALISM_LEVELS` | `mcrpg:herbalism_levels` | INT | Current Herbalism level |
| `WOODCUTTING_LEVELS` | `mcrpg:woodcutting_levels` | INT | Current WoodCutting level |

### Ability Statistics

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `ABILITIES_ACTIVATED` | `mcrpg:abilities_activated` | LONG | Total ability activations across all abilities |
| `BLEED_PROCS` | `mcrpg:bleed_procs` | LONG | Times Bleed has proc'd |
| `EXTRA_ORE_PROCS` | `mcrpg:extra_ore_procs` | LONG | Times Extra Ore has activated |
| `EXTRA_LUMBER_PROCS` | `mcrpg:extra_lumber_procs` | LONG | Times Extra Lumber has activated |

### Quest Statistics

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `QUESTS_COMPLETED` | `mcrpg:quests_completed` | LONG | Total quests completed |
| `UPGRADE_QUESTS_COMPLETED` | `mcrpg:upgrade_quests_completed` | LONG | Ability upgrade quests completed |

### Extensibility

`McRPGStatistics` is designed to be extended when new skills or abilities are added via `ContentExpansion`. Third-party expansions can register their own statistics using the same McCore `StatisticRegistry`:

```java
// In a third-party ContentExpansion's registration:
StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
registry.register(new SimpleStatistic(
    new NamespacedKey("myexpansion", "custom_stat"),
    StatisticType.LONG,
    0L,
    "Custom Stat",
    "A custom stat from my expansion"
));
```

---

## Registration

Statistics are registered during McRPG's bootstrap, after McCore's registries are initialized but before player loading begins.

```
us.eunoians.mcrpg.bootstrap.registrar.StatisticRegistrar implements Registrar<McRPG>
├── register(BootstrapContext<McRPG>)
│   ├── Gets StatisticRegistry from RegistryAccess
│   └── Registers all McRPGStatistics constants
```

This registrar runs for all `StartupProfile`s (both `PROD` and `TEST`) since statistics definitions are needed for testing.

```java
// In McRPGBootstrap, added to the registrar chain:
new StatisticRegistrar().register(bootstrapContext);
```

---

## Incrementing Statistics

Statistics are incremented by hooking into McRPG's **existing event system**. Rather than modifying every ability listener, McRPG adds dedicated statistic listeners that react to McRPG's own events. This keeps the statistic tracking decoupled from ability logic.

### Listener Strategy

```
us.eunoians.mcrpg.listener.statistic
├── SkillStatisticListener           // listens to PostSkillGainExpEvent, SkillGainLevelEvent
├── AbilityStatisticListener         // listens to ability-specific activation events
├── QuestStatisticListener           // listens to QuestCompleteEvent
└── CombatStatisticListener          // listens to EntityDamageByEntityEvent for damage tracking
```

### `SkillStatisticListener`

Listens to existing McRPG events that already fire during gameplay:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onSkillGainExp(PostSkillGainExpEvent event) {
    PlayerStatisticData stats = event.getMcRPGPlayer().getStatisticData();
    NamespacedKey skillKey = event.getSkill().getSkillKey();

    // Increment per-skill XP stat
    NamespacedKey xpStatKey = McRPGStatistics.getSkillExperienceKey(skillKey);
    stats.incrementLong(xpStatKey, (long) event.getExperience());

    // Increment total XP stat
    stats.incrementLong(McRPGStatistics.TOTAL_SKILL_EXPERIENCE.getStatisticKey(), (long) event.getExperience());
}

@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onSkillLevelUp(PostSkillGainLevelEvent event) {
    PlayerStatisticData stats = event.getMcRPGPlayer().getStatisticData();

    // Update per-skill level stat (SET, not increment — levels are current state)
    NamespacedKey levelStatKey = McRPGStatistics.getSkillLevelKey(event.getSkill().getSkillKey());
    stats.setValue(levelStatKey, event.getNewLevel());

    // Increment total levels gained
    int levelsGained = event.getNewLevel() - event.getOldLevel();
    stats.incrementLong(McRPGStatistics.TOTAL_SKILL_LEVELS_GAINED.getStatisticKey(), levelsGained);
}
```

### `AbilityStatisticListener`

Listens to individual ability activation events:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onAbilityActivate(AbilityActivateEvent event) {
    PlayerStatisticData stats = event.getAbilityHolder().getStatisticData();
    stats.incrementLong(McRPGStatistics.ABILITIES_ACTIVATED.getStatisticKey(), 1);
}

@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onBleedActivate(BleedActivateEvent event) {
    PlayerStatisticData stats = event.getAbilityHolder().getStatisticData();
    stats.incrementLong(McRPGStatistics.BLEED_PROCS.getStatisticKey(), 1);
}
```

### `CombatStatisticListener`

Tracks damage dealt and mobs killed. Uses `EventPriority.MONITOR` to read the final damage values after all other plugins have modified them:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
        McRPGPlayer mcRPGPlayer = getPlayer(player);
        if (mcRPGPlayer != null) {
            mcRPGPlayer.getStatisticData().incrementDouble(
                McRPGStatistics.DAMAGE_DEALT.getStatisticKey(), event.getFinalDamage()
            );
        }
    }
    if (event.getEntity() instanceof Player player) {
        McRPGPlayer mcRPGPlayer = getPlayer(player);
        if (mcRPGPlayer != null) {
            mcRPGPlayer.getStatisticData().incrementDouble(
                McRPGStatistics.DAMAGE_TAKEN.getStatisticKey(), event.getFinalDamage()
            );
        }
    }
}
```

### Block-Based Statistics

Block-mined, tree-chopped, and crops-harvested statistics are incremented by listening to `PostSkillGainExpEvent` filtered by skill type, rather than duplicating the block detection logic already in the skill's leveling component:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onSkillGainExp(PostSkillGainExpEvent event) {
    PlayerStatisticData stats = event.getMcRPGPlayer().getStatisticData();
    NamespacedKey skillKey = event.getSkill().getSkillKey();

    // Increment the skill-specific "action count" stat
    if (skillKey.equals(Mining.MINING_KEY)) {
        stats.incrementLong(McRPGStatistics.BLOCKS_MINED.getStatisticKey(), 1);
    } else if (skillKey.equals(WoodCutting.WOOD_CUTTING_KEY)) {
        stats.incrementLong(McRPGStatistics.TREES_CHOPPED.getStatisticKey(), 1);
    } else if (skillKey.equals(Herbalism.HERBALISM_KEY)) {
        stats.incrementLong(McRPGStatistics.CROPS_HARVESTED.getStatisticKey(), 1);
    }
}
```

**Design Decision:** Tying block stats to `PostSkillGainExpEvent` instead of `BlockBreakEvent` means we only count blocks that the skill system considers valid (matching the configured material list). This avoids counting blocks that don't grant XP and keeps the stat meaningful.

**Tradeoff:** If a block grants XP but is broken by an ability (e.g., Mass Harvest breaking multiple blocks), each XP award counts as one increment. This is the correct behavior — we're counting "skill-relevant actions", not raw block breaks.

---

## Player Lifecycle Integration

### Loading

`McRPGPlayerLoadTask.loadPlayer()` is extended to include statistics loading:

```java
// In loadPlayer(), added alongside existing loads:
updatePlayerDataSyncFunctions.add(loadPlayerStatistics(connection));
```

The `loadPlayerStatistics()` method:
1. Calls `PlayerStatisticDAO.getAllPlayerStatistics(connection, uuid)`
2. Returns an `UpdatePlayerDataSyncFunction` that populates `CorePlayer.getStatisticData()` on the main thread

### Saving

`McRPGPlayer.savePlayer()` is extended to include statistics saving:

```java
// In savePlayer(), added alongside existing saves:
if (getStatisticData().isDirty()) {
    failsafeTransaction.addAll(
        PlayerStatisticDAO.savePlayerStatistics(connection, getUUID(), getStatisticData().getModifiedEntries())
    );
    getStatisticData().markClean();
}
```

### Unloading

`McRPGPlayerUnloadTask` already calls `savePlayer()` which will now include statistics. No additional changes needed.

---

## Data Migration

Existing McRPG players have skill XP and levels that predate the statistics system. A one-time migration populates the relevant statistics from existing data.

```
us.eunoians.mcrpg.task.migration.StatisticMigrationTask extends CoreTask
├── Reads all player skill data from SkillDAO
├── For each player: calculates total XP, per-skill XP, per-skill levels, total levels
├── Writes the derived statistics to PlayerStatisticDAO
├── Marks migration as complete in McRPG config
```

**Trigger:** Runs once on first server boot after the statistics update. McRPG config gains:

```yaml
statistics:
  # Internal — set to true after migration completes. Do not modify.
  migration-complete: false
```

**Scope:** Only migrates skill XP and level statistics. Action counts (blocks mined, mobs killed, etc.) cannot be retroactively derived and will start from zero. This is an acceptable tradeoff — players understand that action tracking starts when the feature is added.

**Performance:** Migration runs asynchronously on the database executor. For large servers (100k+ player records), it processes in batches of 100 players to avoid memory pressure and long-running transactions.

---

## PAPI Placeholders

McRPG registers additional PAPI placeholders for its statistics on top of McCore's generic `%mccore_stat_*%` placeholders:

```
%mcrpg_stat_blocks_mined%              → McRPGStatistics.BLOCKS_MINED
%mcrpg_stat_mobs_killed%               → McRPGStatistics.MOBS_KILLED
%mcrpg_stat_total_levels%              → McRPGStatistics.TOTAL_SKILL_LEVELS_GAINED
%mcrpg_stat_abilities_activated%       → McRPGStatistics.ABILITIES_ACTIVATED
%mcrpg_stat_<skill>_xp%               → Per-skill XP
%mcrpg_stat_<skill>_level%            → Per-skill level
```

These are convenience aliases that delegate to `CorePlayer.getStatisticData()` under the hood.

---

## Implementation Phases

### Phase 1: Constants & Registration
1. Create `McRPGStatistics` constants class with all statistic definitions
2. Create `StatisticRegistrar` and add to `McRPGBootstrap`
3. Unit tests for statistic registration

### Phase 2: Statistic Listeners
1. `SkillStatisticListener` — XP, levels, block counts via `PostSkillGainExpEvent`/`PostSkillGainLevelEvent`
2. `AbilityStatisticListener` — ability activation counts
3. `CombatStatisticListener` — damage dealt/taken, mob kills
4. `QuestStatisticListener` — quest completion counts
5. Register listeners in `McRPGBootstrap`

### Phase 3: Player Lifecycle
1. Add `loadPlayerStatistics()` to `McRPGPlayerLoadTask`
2. Add statistics saving to `McRPGPlayer.savePlayer()`
3. Unit tests for load/save flow

### Phase 4: Migration & Polish
1. `StatisticMigrationTask` for existing player data
2. PAPI placeholder registration
3. Integration testing on a live server
