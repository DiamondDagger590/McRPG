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
us.eunoians.mcrpg.statistic.McRPGStatistic
```

### Global Gameplay Statistics

These statistics are **skill-agnostic** — they track aggregate player actions regardless of which skill triggered them.

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `BLOCKS_MINED` | `mcrpg:blocks_mined` | LONG | Total blocks mined that grant skill XP (any skill) |
| `ORES_MINED` | `mcrpg:ores_mined` | LONG | Ore blocks specifically |
| `TREES_CHOPPED` | `mcrpg:trees_chopped` | LONG | Logs broken that grant skill XP |
| `CROPS_HARVESTED` | `mcrpg:crops_harvested` | LONG | Crops harvested that grant skill XP |
| `MOBS_KILLED` | `mcrpg:mobs_killed` | LONG | Total mobs killed (any combat) |
| `DAMAGE_DEALT` | `mcrpg:damage_dealt` | DOUBLE | Total damage dealt (all sources) |
| `DAMAGE_TAKEN` | `mcrpg:damage_taken` | DOUBLE | Total damage taken (all sources) |

### Skill Progression Statistics

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `TOTAL_SKILL_LEVELS_GAINED` | `mcrpg:total_skill_levels_gained` | LONG | Sum of all levels gained across all skills |
| `TOTAL_SKILL_EXPERIENCE` | `mcrpg:total_skill_experience` | LONG | Sum of all XP earned across all skills |
| `MINING_EXPERIENCE` | `mcrpg:mining_experience` | LONG | Total Mining XP earned |
| `SWORDS_EXPERIENCE` | `mcrpg:swords_experience` | LONG | Total Swords XP earned |
| `HERBALISM_EXPERIENCE` | `mcrpg:herbalism_experience` | LONG | Total Herbalism XP earned |
| `WOODCUTTING_EXPERIENCE` | `mcrpg:woodcutting_experience` | LONG | Total WoodCutting XP earned |
| `MINING_MAX_LEVEL` | `mcrpg:mining_max_level` | INT | Highest Mining level reached |
| `SWORDS_MAX_LEVEL` | `mcrpg:swords_max_level` | INT | Highest Swords level reached |
| `HERBALISM_MAX_LEVEL` | `mcrpg:herbalism_max_level` | INT | Highest Herbalism level reached |
| `WOODCUTTING_MAX_LEVEL` | `mcrpg:woodcutting_max_level` | INT | Highest WoodCutting level reached |

### Ability Statistics

| Constant | Key | Type | Description |
|----------|-----|------|-------------|
| `ABILITIES_ACTIVATED` | `mcrpg:abilities_activated` | LONG | Total ability activations across all abilities |

Per-ability activation counts are registered dynamically during bootstrap by iterating over the `AbilityRegistry`. Each registered ability gets a corresponding statistic:

| Pattern | Key Pattern | Type | Description |
|---------|-------------|------|-------------|
| `<ABILITY>_ACTIVATIONS` | `mcrpg:<ability_key>_activations` | LONG | Times this specific ability has activated |

Examples: `mcrpg:bleed_activations`, `mcrpg:extra_ore_activations`, `mcrpg:extra_lumber_activations`, `mcrpg:mass_harvest_activations`, etc.

This dynamic approach means new abilities added via `ContentExpansion` automatically get their own activation statistic without manual registration.

### Extensibility

`McRPGStatistic` is designed to be extended when new skills or abilities are added via `ContentExpansion`. Third-party expansions can register their own statistics using the same McCore `StatisticRegistry`:

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
│   ├── Registers all McRPGStatistic constants
│   └── Iterates AbilityRegistry to register per-ability activation statistics
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
├── SkillStatisticListener           // listens to PostSkillGainExpEvent, PostSkillGainLevelEvent
├── AbilityStatisticListener         // listens to ability activation events
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
    NamespacedKey xpStatKey = McRPGStatistic.getSkillExperienceKey(skillKey);
    stats.incrementLong(xpStatKey, (long) event.getExperience());

    // Increment total XP stat
    stats.incrementLong(McRPGStatistic.TOTAL_SKILL_EXPERIENCE.getStatisticKey(), (long) event.getExperience());
}

@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onSkillLevelUp(PostSkillGainLevelEvent event) {
    PlayerStatisticData stats = event.getMcRPGPlayer().getStatisticData();

    // Update per-skill max level stat (only increases, never decreases)
    NamespacedKey levelStatKey = McRPGStatistic.getSkillMaxLevelKey(event.getSkill().getSkillKey());
    stats.setMaxInt(levelStatKey, event.getNewLevel());

    // Increment total levels gained
    int levelsGained = event.getNewLevel() - event.getOldLevel();
    stats.incrementLong(McRPGStatistic.TOTAL_SKILL_LEVELS_GAINED.getStatisticKey(), levelsGained);
}
```

### `AbilityStatisticListener`

Listens to ability activation events and increments both the global counter and the per-ability counter:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onAbilityActivate(AbilityActivateEvent event) {
    PlayerStatisticData stats = event.getAbilityHolder().getStatisticData();

    // Increment global ability activation count
    stats.incrementLong(McRPGStatistic.ABILITIES_ACTIVATED.getStatisticKey(), 1);

    // Increment per-ability activation count
    NamespacedKey perAbilityKey = McRPGStatistic.getAbilityActivationKey(event.getAbility().getAbilityKey());
    stats.incrementLong(perAbilityKey, 1);
}
```

### `CombatStatisticListener`

Tracks damage dealt and taken across all combat (skill-agnostic). Uses `EventPriority.MONITOR` to read the final damage values after all other plugins have modified them:

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player player) {
        McRPGPlayer mcRPGPlayer = getPlayer(player);
        if (mcRPGPlayer != null) {
            mcRPGPlayer.getStatisticData().incrementDouble(
                McRPGStatistic.DAMAGE_DEALT.getStatisticKey(), event.getFinalDamage()
            );
        }
    }
    if (event.getEntity() instanceof Player player) {
        McRPGPlayer mcRPGPlayer = getPlayer(player);
        if (mcRPGPlayer != null) {
            mcRPGPlayer.getStatisticData().incrementDouble(
                McRPGStatistic.DAMAGE_TAKEN.getStatisticKey(), event.getFinalDamage()
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
        stats.incrementLong(McRPGStatistic.BLOCKS_MINED.getStatisticKey(), 1);
    } else if (skillKey.equals(WoodCutting.WOOD_CUTTING_KEY)) {
        stats.incrementLong(McRPGStatistic.TREES_CHOPPED.getStatisticKey(), 1);
    } else if (skillKey.equals(Herbalism.HERBALISM_KEY)) {
        stats.incrementLong(McRPGStatistic.CROPS_HARVESTED.getStatisticKey(), 1);
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
    FailSafeTransaction statisticTransaction = new FailSafeTransaction(connection);
    statisticTransaction.addAll(
        PlayerStatisticDAO.savePlayerStatistics(connection, getUUID(), getStatisticData().getModifiedEntries())
    );
    if (statisticTransaction.executeTransaction()) {
        getStatisticData().markClean();
    }
}
```

`markClean()` is only called after the transaction succeeds. If the transaction fails, the dirty entries are preserved and will be retried on the next save cycle.

### Unloading

`McRPGPlayerUnloadTask` already calls `savePlayer()` which will now include statistics. No additional changes needed.


---

## PAPI Placeholders

McRPG registers statistic placeholders in its existing `McRPGPapiExpansion`. McCore does not have its own PAPI expansion — downstream plugins are responsible for registering their own placeholders.

New statistic placeholders are added as `McRPGPlaceholder` implementations and registered via `McRPGPlaceHolderType`, following the existing pattern for skill/ability placeholders:

```
%mcrpg_stat_blocks_mined%              → McRPGStatistic.BLOCKS_MINED
%mcrpg_stat_mobs_killed%               → McRPGStatistic.MOBS_KILLED
%mcrpg_stat_total_levels%              → McRPGStatistic.TOTAL_SKILL_LEVELS_GAINED
%mcrpg_stat_abilities_activated%       → McRPGStatistic.ABILITIES_ACTIVATED
%mcrpg_stat_<skill>_xp%               → Per-skill XP
%mcrpg_stat_<skill>_max_level%        → Per-skill highest level reached
%mcrpg_stat_<ability>_activations%    → Per-ability activation count
```

These delegate to `CorePlayer.getStatisticData()` for online players. For offline players, they use McCore's `StatisticCache` (if configured) or fall back to direct `PlayerStatisticDAO` queries.

---

## Configuration

Statistics configuration lives in McRPG's config (McCore is shaded and has no standalone config):

```yaml
statistics:
  # Offline query cache (used by PAPI placeholders, leaderboards, etc.)
  cache:
    # Whether to cache offline stat queries
    enabled: true
    # Maximum number of entries in the cache
    max-size: 1000
    # How long cached entries live before being re-fetched from the database (seconds)
    ttl: 300
```

McRPG constructs the `StatisticCache` during bootstrap using these config values and makes it available to its PAPI expansion and command handlers.

---

## Implementation Phases

### Phase 1: Constants & Registration
1. Create `McRPGStatistic` constants class with all statistic definitions
2. Add dynamic per-ability statistic registration from `AbilityRegistry`
3. Create `StatisticRegistrar` and add to `McRPGBootstrap`
4. Unit tests for statistic registration

### Phase 2: Statistic Listeners
1. `SkillStatisticListener` — XP, max levels, block counts via `PostSkillGainExpEvent`/`PostSkillGainLevelEvent`
2. `AbilityStatisticListener` — global + per-ability activation counts
3. `CombatStatisticListener` — damage dealt/taken, mob kills
4. Register listeners in `McRPGBootstrap`
5. Unit tests for listener behavior

### Phase 3: Player Lifecycle
1. Add `loadPlayerStatistics()` to `McRPGPlayerLoadTask`
2. Add statistics saving to `McRPGPlayer.savePlayer()` (with correct `markClean()` ordering)
3. Unit tests for load/save flow

### Phase 4: Commands, PAPI & Config
1. Mount McCore's base statistic commands under `/mcrpg statistic ...` (view, list, reset)
2. Add statistic placeholders to `McRPGPapiExpansion` via `McRPGPlaceHolderType`
3. Add `statistics` config section to McRPG's main config
4. Construct and configure `StatisticCache` during McRPG bootstrap
