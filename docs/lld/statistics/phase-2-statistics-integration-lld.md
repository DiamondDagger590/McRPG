# Phase 2: McRPG Statistics Integration — Low-Level Design

**Parent HLD:** [statistics-integration-hld.md](../../hld/statistics/statistics-integration-hld.md)
**McCore Framework HLD:** [statistics-framework-hld.md](https://github.com/DiamondDagger590/McCore/blob/develop/docs/hld/statistics/statistics-framework-hld.md)

---

## Scope

This LLD covers the McRPG-side integration of McCore's statistics framework. It is broken into four sub-phases:

| Sub-Phase | Scope | Status |
|-----------|-------|--------|
| 2.1 | Constants & Registration | Complete |
| 2.2 | GainReason & Statistic Listeners | Complete |
| 2.3 | Player Lifecycle (Load/Save) | In Progress |
| 2.4 | PAPI Placeholders, Commands & Config | Not Started |

---

## Sub-Phase 2.1: Constants & Registration (Complete)

### New Files

#### `us.eunoians.mcrpg.statistic.McRPGStatistic`

Central constants class holding all McRPG statistic definitions as `SimpleStatistic` instances. Groups:

| Group | Constants | Type | Count |
|-------|-----------|------|-------|
| Global Gameplay | `BLOCKS_MINED`, `ORES_MINED`, `TREES_CHOPPED`, `CROPS_HARVESTED`, `MOBS_KILLED` | LONG | 5 |
| Global Gameplay | `DAMAGE_DEALT`, `DAMAGE_TAKEN` | DOUBLE | 2 |
| Skill Progression | `TOTAL_SKILL_LEVELS_GAINED`, `TOTAL_SKILL_EXPERIENCE` | LONG | 2 |
| Per-Skill XP | `MINING_EXPERIENCE`, `SWORDS_EXPERIENCE`, `HERBALISM_EXPERIENCE`, `WOODCUTTING_EXPERIENCE` | LONG | 4 |
| Per-Skill Max Level | `MINING_MAX_LEVEL`, `SWORDS_MAX_LEVEL`, `HERBALISM_MAX_LEVEL`, `WOODCUTTING_MAX_LEVEL` | INT | 4 |
| Ability | `ABILITIES_ACTIVATED` | LONG | 1 |
| **Total static** | | | **19** |

**Helper methods:**

| Method | Signature | Purpose |
|--------|-----------|---------|
| `getSkillExperienceKey` | `(NamespacedKey skillKey) → NamespacedKey` | Returns `mcrpg:<skill>_experience` |
| `getSkillMaxLevelKey` | `(NamespacedKey skillKey) → NamespacedKey` | Returns `mcrpg:<skill>_max_level` |
| `getAbilityActivationKey` | `(NamespacedKey abilityKey) → NamespacedKey` | Returns `mcrpg:<ability>_activations` |
| `createAbilityActivationStatistic` | `(NamespacedKey abilityKey, String displayName) → Statistic` | Factory for per-ability activation stats |

**Static collection:** `ALL_STATIC_STATISTICS` (`Set<Statistic>`) contains all 19 predefined statistics for bulk registration.

#### `us.eunoians.mcrpg.bootstrap.McRPGStatisticRegistrar`

Package-private `final class` implementing `Registrar<McRPG>`. Called from `McRPGBootstrap.start()` after `McRPGExpansionRegistrar` (which populates the `AbilityRegistry`).

**Registration logic:**
1. Registers all 19 static statistics from `McRPGStatistic.ALL_STATIC_STATISTICS`
2. Iterates `AbilityRegistry.getAllAbilities()` to dynamically create and register per-ability activation statistics

**Bootstrap placement:**
```
McRPGBootstrap.start()
├── ... managers initialized ...
├── McRPGExpansionRegistrar  ← populates AbilityRegistry
├── McRPGStatisticRegistrar  ← registers all statistics (needs AbilityRegistry populated)
├── McRPGListenerRegistrar   ← registers event listeners
└── ...
```

### Modified Files

| File | Change |
|------|--------|
| `McRPGBootstrap.java` | Added `new McRPGStatisticRegistrar().register(bootstrapContext)` after expansion registrar |

---

## Sub-Phase 2.2: GainReason & Statistic Listeners (Complete)

### Design Decision: GainReason Threading

The HLD specified creating `RedeemExperienceContext` and `CommandExperienceContext` subclasses of `SkillExperienceContext`. However, `SkillExperienceContext<T extends Event>` requires a non-null Bukkit `Event`. Redeem and command flows have no triggering Bukkit event, so creating these contexts would require refactoring the `SkillExperienceContext` hierarchy to allow null events.

**Chosen approach:** Thread `GainReason` independently through `SkillHolder.addExperience()` and the XP events. This is simpler, achieves the same filtering capability, and doesn't require modifying the context hierarchy.

### New Files

#### `us.eunoians.mcrpg.skill.experience.context.GainReason` (Interface)

```java
public interface GainReason {
    @NotNull NamespacedKey getKey();
    @NotNull String getDisplayName();
}
```

Extensible interface allowing third-party `ContentExpansion` plugins to define custom gain reasons.

#### `us.eunoians.mcrpg.skill.experience.context.McRPGGainReason` (Enum)

```java
public enum McRPGGainReason implements GainReason {
    BLOCK_BREAK("Block Break"),
    ENTITY_DAMAGE("Entity Damage"),
    REDEEM("Redeem"),
    COMMAND("Command"),
    OTHER("Other");
}
```

Each constant creates a `NamespacedKey("mcrpg", name().toLowerCase())`.

#### `us.eunoians.mcrpg.listener.statistic.SkillStatisticListener`

Listens at `EventPriority.MONITOR` (read-only, after all other plugins):

**`onSkillGainExp(SkillGainExpEvent)`:**
- Increments per-skill XP via `McRPGStatistic.getSkillExperienceKey(skillKey)`
- Increments `TOTAL_SKILL_EXPERIENCE`
- If `gainReason == McRPGGainReason.BLOCK_BREAK`:
  - Increments `BLOCKS_MINED` (all skills)
  - Increments `ORES_MINED` if skill is Mining
  - Increments `TREES_CHOPPED` if skill is WoodCutting
  - Increments `CROPS_HARVESTED` if skill is Herbalism

**`onSkillLevelUp(PostSkillGainLevelEvent)`:**
- Updates per-skill max level via `setMaxInt()` (only increases)
- Increments `TOTAL_SKILL_LEVELS_GAINED` by `afterLevel - beforeLevel`

**Note:** This listener uses `SkillGainExpEvent` (the pre-event) for XP tracking. See [Known Issue: Overflow XP at Max Level](#known-issue-overflow-xp-at-max-level) for implications.

#### `us.eunoians.mcrpg.listener.statistic.AbilityStatisticListener`

Listens at `EventPriority.MONITOR`:

**`onAbilityActivate(AbilityActivateEvent)`:**
- Resolves `McRPGPlayer` from `AbilityHolder.getUUID()` via `PlayerManager`
- Increments `ABILITIES_ACTIVATED`
- Increments per-ability count via `McRPGStatistic.getAbilityActivationKey(abilityKey)`

#### `us.eunoians.mcrpg.listener.statistic.CombatStatisticListener`

Listens at `EventPriority.MONITOR`:

**`onDamage(EntityDamageByEntityEvent)`:**
- If damager is `Player`: increments `DAMAGE_DEALT` by `event.getFinalDamage()`
- If victim is `Player`: increments `DAMAGE_TAKEN` by `event.getFinalDamage()`
- Resolves `McRPGPlayer` via `PlayerManager.getPlayer(uuid)`

**`onEntityDeath(EntityDeathEvent)`:**
- If `entity.getKiller()` is non-null (returns `Player`): resolves `McRPGPlayer` and increments `MOBS_KILLED`

### Modified Files

| File | Change |
|------|--------|
| `SkillExperienceContext.java` | Added `abstract GainReason getGainReason()` |
| `BlockBreakContext.java` | Added `getGainReason()` → `McRPGGainReason.BLOCK_BREAK` |
| `EntityDamageContext.java` | Added `getGainReason()` → `McRPGGainReason.ENTITY_DAMAGE` |
| `MockExperienceContext.java` (test) | Added `getGainReason()` → `McRPGGainReason.OTHER` |
| `SkillGainExpEvent.java` | Added `GainReason gainReason` field; new constructor with `GainReason`; backward-compatible constructors default to `McRPGGainReason.OTHER` |
| `PostSkillGainExpEvent.java` | Added `int experience` and `GainReason gainReason` fields; new constructor; backward-compatible constructors default to `0` experience and `McRPGGainReason.OTHER` |
| `SkillHolder.java` | Original `addExperience(int)` delegates to new `addExperience(int, GainReason)` with `McRPGGainReason.OTHER`; new method passes `gainReason` to both `SkillGainExpEvent` and `PostSkillGainExpEvent` |
| `SkillListener.java` | Extracts `GainReason` from `SkillExperienceContext.getGainReason()` and passes to `addExperience(exp, gainReason)` |
| `RedeemExperienceCommand.java` | Changed to `addExperience(amount, McRPGGainReason.REDEEM)` |
| `GiveExperienceCommand.java` | Changed to `addExperience(amount, McRPGGainReason.COMMAND)` |
| `McRPGListenerRegistrar.java` | Added registration of `SkillStatisticListener`, `AbilityStatisticListener`, `CombatStatisticListener` |

---

## Sub-Phase 2.3: Player Lifecycle (In Progress)

### Overview

Statistics must be loaded from the database when a player joins and saved when they leave (or on periodic auto-save). McCore's `PlayerStatisticData` is already instantiated by `CorePlayer`'s constructor — we need to populate it from the database and persist dirty entries.

### Modified Files

#### `us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask`

**Change:** Add `loadPlayerStatistics(connection)` call in `loadPlayer()` method.

**Placement in `loadPlayer()`:**
```java
List<UpdatePlayerDataSyncFunction> updatePlayerDataSyncFunctions = new ArrayList<>();
updatePlayerDataSyncFunctions.add(loadPlayerSkills(connection));
updatePlayerDataSyncFunctions.add(loadPlayerLoadouts(connection));
updatePlayerDataSyncFunctions.add(loadPlayerSettings(connection));
updatePlayerDataSyncFunctions.add(loadPlayerExperienceExtras(connection));
updatePlayerDataSyncFunctions.add(loadPlayerStatistics(connection));  // ← NEW
updatePlayerDataSyncFunctions.add(awardRestedExperience(connection));
```

**New method:**
```java
@NotNull
private UpdatePlayerDataSyncFunction loadPlayerStatistics(@NotNull Connection connection) {
    UUID uuid = getCorePlayer().getUUID();
    Map<NamespacedKey, StatisticEntry> entries = PlayerStatisticDAO.getAllPlayerStatistics(connection, uuid);
    return () -> {
        getCorePlayer().getStatisticData().populateFromEntries(entries);
    };
}
```

**Threading model:** Database read happens on the async load thread (inside `loadPlayer()`). The `populateFromEntries()` call happens on the main thread via the `UpdatePlayerDataSyncFunction` lambda, consistent with all other player data loading.

**Import additions:**
- `com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO`
- `com.diamonddagger590.mccore.statistic.StatisticEntry`

#### `us.eunoians.mcrpg.entity.player.McRPGPlayer`

**Change:** Add statistics saving to `savePlayer(Connection)` method.

**Placement in `savePlayer()`:**
```java
// After existing save logic (FailSafeTransaction + BatchTransaction)
failsafeTransaction.executeTransaction();
batchTransaction.executeTransaction();

// Save statistics separately — only mark clean after successful transaction
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

**Design decisions:**
1. **Separate transaction:** Statistics are saved in their own `FailSafeTransaction`, isolated from skill/loadout data. If statistics fail to save, skill data is not affected.
2. **Dirty check:** Only creates a transaction if `isDirty()` is true, avoiding unnecessary database calls.
3. **Conditional `markClean()`:** Only called after `executeTransaction()` returns `true`. If the transaction fails, dirty entries are preserved and will be retried on the next save cycle.
4. **No unload changes needed:** `McRPGPlayerUnloadTask` already calls `savePlayer()`, which now includes statistics. No additional changes required.

**Import additions:**
- `com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO`

### McCore API Methods Used

| Method | Class | Signature | Purpose |
|--------|-------|-----------|---------|
| `getAllPlayerStatistics` | `PlayerStatisticDAO` | `(Connection, UUID) → Map<NamespacedKey, StatisticEntry>` | Load all stats for a player |
| `savePlayerStatistics` | `PlayerStatisticDAO` | `(Connection, UUID, Map<NamespacedKey, StatisticEntry>) → List<PreparedStatement>` | Generate save statements for modified stats |
| `populateFromEntries` | `PlayerStatisticData` | `(Map<NamespacedKey, StatisticEntry>) → void` | Populate live data from DB entries; clears dirty set |
| `isDirty` | `PlayerStatisticData` | `() → boolean` | Check if any stats modified since last save |
| `getModifiedEntries` | `PlayerStatisticData` | `() → Map<NamespacedKey, StatisticEntry>` | Get only modified entries for delta save |
| `markClean` | `PlayerStatisticData` | `() → void` | Clear dirty set after successful save |

---

## Sub-Phase 2.4: PAPI Placeholders, Commands & Config

### 2.4.1: PAPI Placeholders

#### New Placeholder Class

**File:** `us.eunoians.mcrpg.external.papi.placeholder.statistic.StatisticPlaceholder`

A single generic placeholder implementation that handles all statistic types:

```java
public class StatisticPlaceholder extends McRPGPlaceholder {

    private final NamespacedKey statisticKey;

    public StatisticPlaceholder(@NotNull String identifier, @NotNull NamespacedKey statisticKey) {
        super(identifier);
        this.statisticKey = statisticKey;
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        // Online player: read from live PlayerStatisticData
        Optional<McRPGPlayer> playerOptional = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .getPlayer(offlinePlayer.getUniqueId());

        if (playerOptional.isPresent()) {
            return formatValue(playerOptional.get().getStatisticData().getValue(statisticKey).orElse(null));
        }

        // Offline player: use StatisticCache if available, otherwise direct DB query
        // (Implementation depends on 2.4.3 cache setup)
        return null;
    }
}
```

**Placeholder identifiers follow the pattern:** `stat_<statistic_name>`

| Placeholder | Statistic Key | Example Output |
|-------------|---------------|----------------|
| `%mcrpg_stat_blocks_mined%` | `mcrpg:blocks_mined` | `1542` |
| `%mcrpg_stat_ores_mined%` | `mcrpg:ores_mined` | `387` |
| `%mcrpg_stat_trees_chopped%` | `mcrpg:trees_chopped` | `215` |
| `%mcrpg_stat_crops_harvested%` | `mcrpg:crops_harvested` | `892` |
| `%mcrpg_stat_mobs_killed%` | `mcrpg:mobs_killed` | `430` |
| `%mcrpg_stat_damage_dealt%` | `mcrpg:damage_dealt` | `12543.50` |
| `%mcrpg_stat_damage_taken%` | `mcrpg:damage_taken` | `8761.25` |
| `%mcrpg_stat_total_levels%` | `mcrpg:total_skill_levels_gained` | `45` |
| `%mcrpg_stat_total_xp%` | `mcrpg:total_skill_experience` | `98500` |
| `%mcrpg_stat_abilities_activated%` | `mcrpg:abilities_activated` | `312` |
| `%mcrpg_stat_<skill>_xp%` | `mcrpg:<skill>_experience` | `24500` |
| `%mcrpg_stat_<skill>_max_level%` | `mcrpg:<skill>_max_level` | `50` |
| `%mcrpg_stat_<ability>_activations%` | `mcrpg:<ability>_activations` | `87` |

#### Registration in `McRPGPlaceHolderType`

Add a new enum constant `STATISTIC`:

```java
STATISTIC((mcRPG, mcRPGPapiExpansion) -> {
    // Register fixed-name global stats
    mcRPGPapiExpansion.registerPlaceholder(
        new StatisticPlaceholder("stat_blocks_mined", McRPGStatistic.BLOCKS_MINED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
        new StatisticPlaceholder("stat_ores_mined", McRPGStatistic.ORES_MINED.getStatisticKey()));
    // ... all other fixed stats ...

    // Register per-skill stats dynamically
    mcRPG.registryAccess().registry(McRPGRegistryKey.SKILL)
         .getRegisteredSkillKeys()
         .forEach(skillKey -> {
             String skillName = skillKey.getKey();
             mcRPGPapiExpansion.registerPlaceholder(
                 new StatisticPlaceholder("stat_" + skillName + "_xp",
                     McRPGStatistic.getSkillExperienceKey(skillKey)));
             mcRPGPapiExpansion.registerPlaceholder(
                 new StatisticPlaceholder("stat_" + skillName + "_max_level",
                     McRPGStatistic.getSkillMaxLevelKey(skillKey)));
         });

    // Register per-ability stats dynamically
    mcRPG.registryAccess().registry(McRPGRegistryKey.ABILITY)
         .getAllAbilities()
         .forEach(abilityKey -> {
             String abilityName = abilityKey.getKey();
             mcRPGPapiExpansion.registerPlaceholder(
                 new StatisticPlaceholder("stat_" + abilityName + "_activations",
                     McRPGStatistic.getAbilityActivationKey(abilityKey)));
         });
}),
```

### 2.4.2: Offline Player Support

For offline players, PAPI placeholders need to read statistics from the database. Two strategies:

**Option A: StatisticCache (recommended)**
- McRPG constructs a `StatisticCache` during bootstrap using config values
- Placeholders check cache first, then fall back to `PlayerStatisticDAO.getPlayerStatistic()`
- Cache is invalidated when a player joins (live `PlayerStatisticData` takes over)
- Cache is populated on DB query results

**Option B: Direct DB query**
- Each placeholder call for an offline player executes a DB query
- Simple but potentially slow for scoreboards/leaderboards that query many players

**Chosen approach:** Option A with cache. The cache is constructed during bootstrap and made available through the McRPG manager registry (or stored on the PAPI expansion itself).

#### Cache Lifecycle

```
Player joins  → StatisticCache.invalidate(uuid)  // live data takes over
Player quits  → savePlayer() persists stats       // DB is up to date
PAPI query    → check PlayerManager for online    // if online, use live data
              → if offline, check cache            // cache hit = fast
              → if cache miss, query DB            // populate cache on result
```

#### StatisticCache Integration

**New class:** `us.eunoians.mcrpg.statistic.McRPGStatisticCacheManager`

This is a lightweight wrapper that owns the `StatisticCache` instance and provides the offline lookup logic used by placeholders:

```java
public class McRPGStatisticCacheManager {

    private final StatisticCache cache;

    public McRPGStatisticCacheManager(long maxSize, long ttlSeconds) {
        this.cache = new StatisticCache(maxSize, ttlSeconds);
    }

    @NotNull
    public StatisticCache getCache() {
        return cache;
    }

    /**
     * Gets a statistic value for an offline player, using cache with DB fallback.
     */
    @NotNull
    public Optional<StatisticEntry> getOfflineStatistic(
            @NotNull Connection connection,
            @NotNull UUID uuid,
            @NotNull NamespacedKey key) {
        Optional<StatisticEntry> cached = cache.get(uuid, key);
        if (cached.isPresent()) {
            return cached;
        }
        Optional<StatisticEntry> fromDb = PlayerStatisticDAO.getPlayerStatistic(connection, uuid, key);
        fromDb.ifPresent(entry -> cache.put(uuid, key, entry));
        return fromDb;
    }
}
```

**Cache invalidation on player join:** Add `cache.invalidate(uuid)` in `McRPGPlayerLoadTask.onPlayerLoadSuccessfully()` or in the `CorePlayerLoadListener`.

### 2.4.3: Configuration

#### New Config Routes in `MainConfigFile`

```java
// Statistics section
private static final String STATISTICS_HEADER =
    toRoutePath(CONFIGURATION_HEADER, "statistics");
private static final String STATISTICS_CACHE_HEADER =
    toRoutePath(STATISTICS_HEADER, "cache");

public static final Route STATISTICS_CACHE_ENABLED =
    Route.fromString(toRoutePath(STATISTICS_CACHE_HEADER, "enabled"));
public static final Route STATISTICS_CACHE_MAX_SIZE =
    Route.fromString(toRoutePath(STATISTICS_CACHE_HEADER, "max-size"));
public static final Route STATISTICS_CACHE_TTL =
    Route.fromString(toRoutePath(STATISTICS_CACHE_HEADER, "ttl"));
```

#### YAML Config Addition (main config)

```yaml
configuration:
  statistics:
    cache:
      # Whether to cache offline stat queries (used by PAPI placeholders)
      enabled: true
      # Maximum number of entries in the cache
      max-size: 1000
      # How long cached entries live before being re-fetched (seconds)
      ttl: 300
```

#### Bootstrap Integration

The cache is constructed during `McRPGBootstrap.start()`, after file manager is initialized but before PAPI expansion registration:

```java
// In McRPGBootstrap.start(), after file manager init:
boolean cacheEnabled = fileManager.getFile(FileType.MAIN_CONFIG)
    .getBoolean(MainConfigFile.STATISTICS_CACHE_ENABLED, true);
if (cacheEnabled) {
    long maxSize = fileManager.getFile(FileType.MAIN_CONFIG)
        .getLong(MainConfigFile.STATISTICS_CACHE_MAX_SIZE, 1000);
    long ttl = fileManager.getFile(FileType.MAIN_CONFIG)
        .getLong(MainConfigFile.STATISTICS_CACHE_TTL, 300);
    McRPGStatisticCacheManager cacheManager = new McRPGStatisticCacheManager(maxSize, ttl);
    // Store cacheManager somewhere accessible (e.g., on McRPG instance or in a manager)
}
```

### 2.4.4: Commands

The HLD specifies mounting McCore's base statistic commands under `/mcrpg statistic ...`. McCore provides the DAO and data model; McRPG provides the player-facing command interface.

#### Command Structure

```
/mcrpg statistic view <player> <statistic>     — View a specific stat value
/mcrpg statistic list <player>                  — List all stats for a player
/mcrpg statistic set <player> <statistic> <value>   — Admin: set a stat value
/mcrpg statistic reset <player> [statistic]     — Admin: reset one or all stats
```

#### New Files

**`us.eunoians.mcrpg.command.statistic.StatisticViewCommand`**
- Permission: `mcrpg.statistic.view` (or player's own stats with no permission)
- Resolves player from `PlayerManager` (online) or queries DB (offline)
- Formats statistic value based on `StatisticType` (integers formatted with commas, doubles to 2dp)

**`us.eunoians.mcrpg.command.statistic.StatisticListCommand`**
- Permission: `mcrpg.statistic.list`
- Lists all registered statistics with current values for the target player
- Paginated output for large stat lists

**`us.eunoians.mcrpg.command.statistic.StatisticSetCommand`**
- Permission: `mcrpg.statistic.set` (admin only)
- Uses `PlayerStatisticData.setValue()` for online players
- For offline players: direct `PlayerStatisticDAO.savePlayerStatistic()` call
- Requires confirmation via `/mcrpg confirm`

**`us.eunoians.mcrpg.command.statistic.StatisticResetCommand`**
- Permission: `mcrpg.statistic.reset` (admin only)
- Resets to default value from `Statistic.getDefaultValue()`
- Optional `[statistic]` argument; if omitted, resets all
- Requires confirmation via `/mcrpg confirm`

#### Command Parser

**`us.eunoians.mcrpg.command.parser.StatisticParser`**

Cloud command parser for `Statistic` type, using `StatisticRegistry` for tab completion and validation:

```java
public class StatisticParser implements ArgumentParser<CommandSourceStack, Statistic> {
    // Parses statistic key strings (e.g., "mcrpg:blocks_mined") against StatisticRegistry
    // Provides tab completion from registered statistic keys
}
```

#### Registration

Add to `McRPGCommandRegistrar.register()`:
```java
StatisticViewCommand.registerCommand();
StatisticListCommand.registerCommand();
StatisticSetCommand.registerCommand();
StatisticResetCommand.registerCommand();
```

### 2.4.5: Localization Keys

New locale keys needed in `LocalizationKey.java`:

```java
private static final String STATISTIC_HEADER = toRoutePath(PARENT_HEADER, "statistic");

// Command messages
public static final Route STATISTIC_VIEW_MESSAGE = Route.fromString(toRoutePath(STATISTIC_HEADER, "view-message"));
public static final Route STATISTIC_LIST_HEADER = Route.fromString(toRoutePath(STATISTIC_HEADER, "list-header"));
public static final Route STATISTIC_LIST_ENTRY = Route.fromString(toRoutePath(STATISTIC_HEADER, "list-entry"));
public static final Route STATISTIC_SET_SUCCESS = Route.fromString(toRoutePath(STATISTIC_HEADER, "set-success"));
public static final Route STATISTIC_RESET_SUCCESS = Route.fromString(toRoutePath(STATISTIC_HEADER, "reset-success"));
public static final Route STATISTIC_RESET_ALL_SUCCESS = Route.fromString(toRoutePath(STATISTIC_HEADER, "reset-all-success"));
public static final Route STATISTIC_NOT_FOUND = Route.fromString(toRoutePath(STATISTIC_HEADER, "not-found"));
public static final Route STATISTIC_PLAYER_NOT_FOUND = Route.fromString(toRoutePath(STATISTIC_HEADER, "player-not-found"));

// Command descriptions
public static final Route COMMAND_DESCRIPTION_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTION_HEADER, "statistic"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW = Route.fromString(toRoutePath(COMMAND_DESCRIPTION_HEADER, "statistic-view"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_LIST = Route.fromString(toRoutePath(COMMAND_DESCRIPTION_HEADER, "statistic-list"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_SET = Route.fromString(toRoutePath(COMMAND_DESCRIPTION_HEADER, "statistic-set"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET = Route.fromString(toRoutePath(COMMAND_DESCRIPTION_HEADER, "statistic-reset"));
```

Corresponding entries must be added to the bundled English locale YAML files.

---

## Uncapped Experience with Dynamic Level Calculation

### Problem

The HLD requires that experience statistics track **all XP earned, including overflow past max level**. The current `SkillHolder.SkillHolderData.addExperience()` implementation has two problems:

1. **Early return at max level** (line 332): When `cachedLevel >= skill.getMaxLevel()`, the method returns immediately — no events fire, no statistics track.
2. **Experience capping** (lines 355-361): When gaining XP pushes past max level, `totalExperience` is capped at `totalExpForMaxLevel` and the overflow is discarded.

### Solution: Remove the Experience Cap

Instead of capping `totalExperience`, allow it to grow unbounded. The player's **effective level** is calculated dynamically and clamped to `skill.getMaxLevel()`. This means:

- XP always accumulates, even past max level
- Events always fire (no early return)
- Statistics naturally track all XP earned
- If the server admin later raises `maxLevel`, players who earned overflow XP are already at the correct level without data migration

### Changes to `SkillHolder.SkillHolderData.addExperience()`

```java
public int addExperience(int experience, @NotNull GainReason gainReason) {
    if (experience <= 0) {
        return 0;
    }

    SkillGainExpEvent skillGainExpEvent = new SkillGainExpEvent(
        getSkillHolder(), getSkillKey(), Math.max(0, experience), gainReason);
    Bukkit.getPluginManager().callEvent(skillGainExpEvent);
    if (skillGainExpEvent.isCancelled()) {
        return experience;
    }

    int previousLevel = getCurrentLevel();
    experience = skillGainExpEvent.getExperience();

    // Always accumulate — no cap
    totalExperience += experience;

    // Recalculate level from new total (getCurrentLevel() clamps to maxLevel)
    recalculateLevelCache();

    // Fire level up event if effective level changed
    int newLevel = getCurrentLevel();
    int levelsGained = newLevel - previousLevel;
    if (levelsGained > 0) {
        SkillGainLevelEvent skillGainLevelEvent = new SkillGainLevelEvent(
            getSkillHolder(), getSkillKey(), levelsGained);
        Bukkit.getPluginManager().callEvent(skillGainLevelEvent);
        Bukkit.getPluginManager().callEvent(
            new PostSkillGainLevelEvent(skillHolder, getSkillKey(), previousLevel, newLevel));
    }

    Bukkit.getPluginManager().callEvent(
        new PostSkillGainExpEvent(skillHolder, getSkillKey(), experience, gainReason));
    return 0;  // No leftover — all experience is always consumed
}
```

### Changes to `getCurrentLevel()`

The level calculation must clamp to `skill.getMaxLevel()`:

```java
public int getCurrentLevel() {
    ensureCacheValid();
    return Math.min(cachedLevel, skill.getMaxLevel());
}
```

Alternatively, the clamping can happen inside `recalculateLevelCache()` so that `cachedLevel` is always the effective level. Either approach works — the key invariant is that **`getCurrentLevel()` never exceeds `skill.getMaxLevel()`** while **`totalExperience` is never artificially capped**.

### Changes to `getCurrentExperience()` (XP within current level)

The "current experience toward next level" display must handle the at-max-level case gracefully. When at max level, either:
- Return `totalExperience - totalExpForMaxLevel` (showing overflow), or
- Return `0` (showing no progress since there's no next level)

The choice depends on how the UI should behave. Returning `0` is simpler and avoids confusing players.

### Unit Tests Required

| Test | Assertion |
|------|-----------|
| `addExperience_atMaxLevel_stillAccumulatesXP` | `totalExperience` increases even when `getCurrentLevel() == maxLevel` |
| `addExperience_atMaxLevel_firesSkillGainExpEvent` | `SkillGainExpEvent` fires with correct XP amount |
| `addExperience_atMaxLevel_firesPostSkillGainExpEvent` | `PostSkillGainExpEvent` fires with correct XP and `GainReason` |
| `addExperience_atMaxLevel_doesNotFireLevelUpEvent` | No `SkillGainLevelEvent` or `PostSkillGainLevelEvent` fired |
| `addExperience_atMaxLevel_getCurrentLevelClamped` | `getCurrentLevel()` returns `maxLevel`, not the raw calculated level |
| `addExperience_crossingMaxLevel_firesLevelUpToMax` | When XP pushes from below max to above, level-up event fires with `newLevel == maxLevel` |
| `addExperience_crossingMaxLevel_allXpConsumed` | Return value is `0` (no leftover) |
| `getCurrentExperience_atMaxLevel_returnsZero` | XP-within-level display returns `0` when at max |
| `maxLevelIncrease_retroactiveLevel` | If `maxLevel` is raised, `getCurrentLevel()` reflects the higher level from accumulated XP |

---

## Implementation Order

### Phase 2.3 (Remaining Work)
1. Verify committed changes in `McRPGPlayerLoadTask` and `McRPGPlayer` are correct
2. Refactor `SkillHolder.addExperience()` to remove experience cap and early return at max level
3. Update `getCurrentLevel()` to clamp to `skill.getMaxLevel()`
4. Update `getCurrentExperience()` to handle at-max-level gracefully
5. Add unit tests for uncapped experience behavior (see test table above)
6. Run tests and verify build
7. Commit and push

### Phase 2.4
1. Add config routes to `MainConfigFile` and default values to YAML
2. Create `StatisticPlaceholder` class
3. Add `STATISTIC` constant to `McRPGPlaceHolderType` enum
4. Create `McRPGStatisticCacheManager` for offline player lookups
5. Wire cache construction in bootstrap
6. Add cache invalidation on player load
7. Create `StatisticParser` for command argument parsing
8. Create `StatisticViewCommand` and `StatisticListCommand`
9. Create `StatisticSetCommand` and `StatisticResetCommand` (with confirmation)
10. Register commands in `McRPGCommandRegistrar`
11. Add localization keys and English locale entries
12. Run tests and verify build
13. Commit and push

---

## File Summary

### New Files (Phase 2, all sub-phases)

| File | Sub-Phase | Purpose |
|------|-----------|---------|
| `statistic/McRPGStatistic.java` | 2.1 | Statistic constants and helpers |
| `bootstrap/McRPGStatisticRegistrar.java` | 2.1 | Bootstrap registration |
| `skill/experience/context/GainReason.java` | 2.2 | Extensible gain reason interface |
| `skill/experience/context/McRPGGainReason.java` | 2.2 | Built-in gain reasons enum |
| `listener/statistic/SkillStatisticListener.java` | 2.2 | Skill XP and level tracking |
| `listener/statistic/AbilityStatisticListener.java` | 2.2 | Ability activation tracking |
| `listener/statistic/CombatStatisticListener.java` | 2.2 | Combat stat tracking |
| `external/papi/placeholder/statistic/StatisticPlaceholder.java` | 2.4 | PAPI placeholder for statistics |
| `statistic/McRPGStatisticCacheManager.java` | 2.4 | Offline cache wrapper |
| `command/statistic/StatisticViewCommand.java` | 2.4 | View stat command |
| `command/statistic/StatisticListCommand.java` | 2.4 | List stats command |
| `command/statistic/StatisticSetCommand.java` | 2.4 | Admin set stat command |
| `command/statistic/StatisticResetCommand.java` | 2.4 | Admin reset stat command |
| `command/parser/StatisticParser.java` | 2.4 | Cloud parser for Statistic type |

### Modified Files (Phase 2, all sub-phases)

| File | Sub-Phase | Change |
|------|-----------|--------|
| `bootstrap/McRPGBootstrap.java` | 2.1 | Add statistic registrar call |
| `skill/experience/context/SkillExperienceContext.java` | 2.2 | Add abstract `getGainReason()` |
| `skill/experience/context/BlockBreakContext.java` | 2.2 | Implement `getGainReason()` |
| `skill/experience/context/EntityDamageContext.java` | 2.2 | Implement `getGainReason()` |
| `event/skill/SkillGainExpEvent.java` | 2.2 | Add `GainReason` field and getter |
| `event/skill/PostSkillGainExpEvent.java` | 2.2 | Add `experience` and `GainReason` fields |
| `entity/holder/SkillHolder.java` | 2.2, 2.3 | Add `GainReason` param to `addExperience()`; remove experience cap; clamp level dynamically |
| `listener/skill/SkillListener.java` | 2.2 | Pass `GainReason` to `addExperience()` |
| `command/redeem/RedeemExperienceCommand.java` | 2.2 | Use `McRPGGainReason.REDEEM` |
| `command/give/GiveExperienceCommand.java` | 2.2 | Use `McRPGGainReason.COMMAND` |
| `bootstrap/McRPGListenerRegistrar.java` | 2.2 | Register statistic listeners |
| `task/player/McRPGPlayerLoadTask.java` | 2.3 | Add `loadPlayerStatistics()` |
| `entity/player/McRPGPlayer.java` | 2.3 | Add statistics saving in `savePlayer()` |
| `external/papi/placeholder/McRPGPlaceHolderType.java` | 2.4 | Add `STATISTIC` constant |
| `configuration/file/MainConfigFile.java` | 2.4 | Add statistics cache config routes |
| `bootstrap/McRPGCommandRegistrar.java` | 2.4 | Register statistic commands |
| `configuration/file/localization/LocalizationKey.java` | 2.4 | Add statistic locale keys |
| `src/main/resources/localization/english/en.yml` | 2.4 | Add English locale entries |
| `src/test/.../MockExperienceContext.java` | 2.2 | Implement `getGainReason()` |
