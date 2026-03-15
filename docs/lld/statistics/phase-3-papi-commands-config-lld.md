# Phase 3: PAPI Placeholders, Commands & Config — Low-Level Design

**Parent HLD:** [statistics-integration-hld.md](../../hld/statistics/statistics-integration-hld.md) (Phase 4)
**McCore Framework HLD:** [statistics-framework-hld.md](../../../../McCore/docs/hld/statistics/statistics-framework-hld.md)
**Phase 2 LLD:** [phase-2-statistics-integration-lld.md](phase-2-statistics-integration-lld.md) (Sub-Phase 2.4)

This LLD covers the player- and admin-facing surface for McRPG statistics: PAPI placeholders, offline stat caching, admin commands, configuration, and localization.

---

## Table of Contents

1. [New Files Summary](#new-files-summary)
2. [Configuration](#configuration)
3. [Offline Cache Manager](#offline-cache-manager)
4. [PAPI Placeholders](#papi-placeholders)
5. [Command Parser](#command-parser)
6. [Commands](#commands)
7. [Localization](#localization)
8. [Bootstrap & Registration Changes](#bootstrap--registration-changes)
9. [Implementation Order](#implementation-order)
10. [Verification](#verification)

---

## New Files Summary

### Production Code (`src/main/java/us/eunoians/mcrpg/`)

| # | File | Package | Type |
|---|------|---------|------|
| 1 | `McRPGStatisticCacheManager.java` | `statistic` | Class (extends `Manager<McRPG>`) |
| 2 | `StatisticPlaceholder.java` | `external/papi/placeholder/statistic` | Class (extends `McRPGPlaceholder`) |
| 3 | `StatisticParser.java` | `command/parser` | Class (implements `ArgumentParser`, `BlockingSuggestionProvider.Strings`) |
| 4 | `StatisticCommandBase.java` | `command/statistic` | Class (extends `AdminBaseCommand`) |
| 5 | `StatisticViewCommand.java` | `command/statistic` | Class (extends `StatisticCommandBase`) |
| 6 | `StatisticListCommand.java` | `command/statistic` | Class (extends `StatisticCommandBase`) |
| 7 | `StatisticSetCommand.java` | `command/statistic` | Class (extends `StatisticCommandBase`) |
| 8 | `StatisticResetCommand.java` | `command/statistic` | Class (extends `StatisticCommandBase`) |

### Modified Files

| File | Change |
|------|--------|
| `configuration/file/MainConfigFile.java` | Add `STATISTICS_CACHE_ENABLED`, `STATISTICS_CACHE_MAX_SIZE`, `STATISTICS_CACHE_TTL` routes |
| `registry/manager/McRPGManagerKey.java` | Add `STATISTIC_CACHE` constant |
| `command/CommandPlaceholders.java` | Add `STATISTIC_NAME`, `STATISTIC_VALUE` constants |
| `external/papi/placeholder/McRPGPlaceHolderType.java` | Add `STATISTIC` enum constant |
| `configuration/file/localization/LocalizationKey.java` | Add statistic command locale keys |
| `bootstrap/McRPGBootstrap.java` | Register `McRPGStatisticCacheManager` in PROD block |
| `bootstrap/McRPGCommandRegistrar.java` | Register 4 statistic commands |
| `task/player/McRPGPlayerLoadTask.java` | Add cache invalidation in `onPlayerLoadSuccessfully()` |
| `src/main/resources/config.yml` | Add `statistics.cache` YAML section |
| `src/main/resources/localization/english/en_commands.yml` | Add statistic command locale entries |

### Test Code (`src/test/java/us/eunoians/mcrpg/`)

| # | File | Package |
|---|------|---------|
| 1 | `StatisticPlaceholderTest.java` | `external/papi/placeholder/statistic` |
| 2 | `StatisticParserTest.java` | `command/parser` |

---

## Configuration

### New Config Routes in `MainConfigFile`

**File:** `src/main/java/us/eunoians/mcrpg/configuration/file/MainConfigFile.java`

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

### YAML Config Addition

**File:** `src/main/resources/config.yml`

Appended under the existing `configuration:` section:

```yaml
  # Configure the offline statistics cache used by PAPI placeholders and commands
  statistics:
    cache:
      # Whether to cache offline stat queries (used by PAPI placeholders)
      enabled: true
      # Maximum number of entries in the cache
      max-size: 1000
      # How long cached entries live before being re-fetched from the database (seconds)
      ttl: 300
```

---

## Offline Cache Manager

### `McRPGStatisticCacheManager`

**File:** `src/main/java/us/eunoians/mcrpg/statistic/McRPGStatisticCacheManager.java`

A lightweight manager that wraps McCore's `StatisticCache` and provides the offline lookup logic used by PAPI placeholders and commands.

```java
package us.eunoians.mcrpg.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.statistic.cache.StatisticCache;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * A manager that wraps McCore's {@link StatisticCache} and provides offline player
 * statistic lookups for PAPI placeholders and commands.
 * <p>
 * For online players, statistics are read directly from {@link com.diamonddagger590.mccore.statistic.PlayerStatisticData}.
 * For offline players, this manager checks the cache first, then falls back to
 * {@link PlayerStatisticDAO#getPlayerStatistic(Connection, UUID, NamespacedKey)}.
 */
public class McRPGStatisticCacheManager extends Manager<McRPG> {

    private final StatisticCache cache;

    /**
     * Creates a new {@link McRPGStatisticCacheManager}.
     *
     * @param plugin     The plugin instance.
     * @param maxSize    Maximum number of entries in the cache.
     * @param ttlSeconds Time-to-live in seconds for each entry.
     */
    public McRPGStatisticCacheManager(@NotNull McRPG plugin, long maxSize, long ttlSeconds) {
        super(plugin);
        this.cache = new StatisticCache(maxSize, ttlSeconds);
    }

    /**
     * Gets the underlying {@link StatisticCache}.
     *
     * @return The cache.
     */
    @NotNull
    public StatisticCache getCache() {
        return cache;
    }

    /**
     * Gets a statistic value for an offline player, using the cache with a database fallback.
     * <p>
     * This is a <b>synchronous</b> method that may block on a cache miss. Prefer
     * {@link #populateAsync(UUID, NamespacedKey)} for non-blocking cache warming.
     *
     * @param connection The database connection.
     * @param uuid       The player's UUID.
     * @param key        The statistic key.
     * @return The statistic entry, or empty if the player has no value for this stat.
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

    /**
     * Asynchronously populates the cache for an offline player's statistic.
     * <p>
     * Used by PAPI placeholders to avoid blocking on a cache miss. Returns immediately;
     * the cached value will be available on subsequent calls.
     *
     * @param uuid The player's UUID.
     * @param key  The statistic key.
     */
    public void populateAsync(@NotNull UUID uuid, @NotNull NamespacedKey key) {
        Database database = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                Optional<StatisticEntry> fromDb = PlayerStatisticDAO.getPlayerStatistic(connection, uuid, key);
                fromDb.ifPresent(entry -> cache.put(uuid, key, entry));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
```

**Design decisions:**

- Extends `Manager<McRPG>` so it can be registered in the `ManagerRegistry` and accessed via `McRPGManagerKey.STATISTIC_CACHE`. This follows the pattern used by all other McRPG managers (`RestedExperienceManager`, `DisplayManager`, etc.).
- `getOfflineStatistic()` is synchronous for command use where the caller is already on an async thread. `populateAsync()` is non-blocking for PAPI's main-thread calls.
- The LLD specified `McRPGStatisticCacheManager` as a plain class, but extending `Manager<McRPG>` is required to fit the registry access pattern.

### `McRPGManagerKey` — add `STATISTIC_CACHE`

**File:** `src/main/java/us/eunoians/mcrpg/registry/manager/McRPGManagerKey.java`

```java
ManagerKey<McRPGStatisticCacheManager> STATISTIC_CACHE = create(McRPGStatisticCacheManager.class);
```

---

## PAPI Placeholders

### `StatisticPlaceholder`

**File:** `src/main/java/us/eunoians/mcrpg/external/papi/placeholder/statistic/StatisticPlaceholder.java`

A single generic placeholder implementation that handles all statistic types. Online players read from live `PlayerStatisticData`; offline players check the cache with an async DB fallback on miss.

```java
package us.eunoians.mcrpg.external.papi.placeholder.statistic;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.external.papi.placeholder.McRPGPlaceholder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.statistic.McRPGStatisticCacheManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Optional;

/**
 * A PAPI placeholder for any registered {@link com.diamonddagger590.mccore.statistic.Statistic}.
 * <p>
 * For online players, reads directly from {@link com.diamonddagger590.mccore.statistic.PlayerStatisticData}.
 * For offline players, checks the {@link com.diamonddagger590.mccore.statistic.cache.StatisticCache}
 * and triggers an async DB fetch on cache miss.
 */
public class StatisticPlaceholder extends McRPGPlaceholder {

    private static final NumberFormat INTEGER_FORMAT = NumberFormat.getIntegerInstance();
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#,##0.00");

    private final NamespacedKey statisticKey;

    /**
     * Creates a new {@link StatisticPlaceholder}.
     *
     * @param identifier   The placeholder identifier (without the {@code mcrpg_} prefix).
     * @param statisticKey The statistic key to look up.
     */
    public StatisticPlaceholder(@NotNull String identifier, @NotNull NamespacedKey statisticKey) {
        super(identifier);
        this.statisticKey = statisticKey;
    }

    @Nullable
    @Override
    public String parsePlaceholder(@NotNull OfflinePlayer offlinePlayer) {
        McRPG mcRPG = McRPG.getInstance();
        ManagerRegistry managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);

        // Online player: read from live PlayerStatisticData
        Optional<McRPGPlayer> playerOptional = managerRegistry.manager(McRPGManagerKey.PLAYER)
                .getPlayer(offlinePlayer.getUniqueId());
        if (playerOptional.isPresent()) {
            return formatValue(playerOptional.get().getStatisticData().getValue(statisticKey).orElse(null));
        }

        // Offline player: check cache, trigger async DB fetch on miss
        if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
            McRPGStatisticCacheManager cacheManager = managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE);
            Optional<StatisticEntry> cached = cacheManager.getCache().get(offlinePlayer.getUniqueId(), statisticKey);
            if (cached.isPresent()) {
                return formatValue(cached.get().value());
            }
            // Cache miss: trigger async DB fetch; return null now, cached on next call
            cacheManager.populateAsync(offlinePlayer.getUniqueId(), statisticKey);
        }
        return null;
    }

    /**
     * Formats a statistic value for display. Integers and longs are formatted with commas,
     * doubles to 2 decimal places, and other types use {@link Object#toString()}.
     *
     * @param value The value to format, or {@code null} for a default.
     * @return The formatted string.
     */
    @NotNull
    static String formatValue(@Nullable Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Integer i) {
            return INTEGER_FORMAT.format(i);
        }
        if (value instanceof Long l) {
            return INTEGER_FORMAT.format(l);
        }
        if (value instanceof Double d) {
            return DOUBLE_FORMAT.format(d);
        }
        return value.toString();
    }
}
```

**Design decisions:**

- `formatValue()` is package-private (`static`) so it can be reused by `StatisticCommandBase` for consistent formatting across placeholders and commands.
- Returns `null` on cache miss rather than `"0"` — PAPI treats `null` as "no replacement", which means the raw placeholder text is shown briefly until the async fetch completes. On the next scoreboard/tab-list refresh, the cached value is available.
- Checks `managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)` because the cache manager is only registered in PROD when cache is enabled.

### `McRPGPlaceHolderType` — add `STATISTIC`

**File:** `src/main/java/us/eunoians/mcrpg/external/papi/placeholder/McRPGPlaceHolderType.java`

New enum constant added before the trailing semicolon:

```java
STATISTIC((mcRPG, mcRPGPapiExpansion) -> {
    // Fixed global stats
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_blocks_mined", McRPGStatistic.BLOCKS_MINED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_ores_mined", McRPGStatistic.ORES_MINED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_trees_chopped", McRPGStatistic.TREES_CHOPPED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_crops_harvested", McRPGStatistic.CROPS_HARVESTED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_mobs_killed", McRPGStatistic.MOBS_KILLED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_damage_dealt", McRPGStatistic.DAMAGE_DEALT.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_damage_taken", McRPGStatistic.DAMAGE_TAKEN.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_total_levels", McRPGStatistic.TOTAL_SKILL_LEVELS_GAINED.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_total_xp", McRPGStatistic.TOTAL_SKILL_EXPERIENCE.getStatisticKey()));
    mcRPGPapiExpansion.registerPlaceholder(
            new StatisticPlaceholder("stat_abilities_activated", McRPGStatistic.ABILITIES_ACTIVATED.getStatisticKey()));

    // Per-skill dynamic stats (XP + max level)
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

    // Per-ability dynamic stats (activation count)
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

**Placeholder identifier table:**

| Placeholder | Statistic Key | Example Output |
|-------------|---------------|----------------|
| `%mcrpg_stat_blocks_mined%` | `mcrpg:blocks_mined` | `1,542` |
| `%mcrpg_stat_ores_mined%` | `mcrpg:ores_mined` | `387` |
| `%mcrpg_stat_trees_chopped%` | `mcrpg:trees_chopped` | `215` |
| `%mcrpg_stat_crops_harvested%` | `mcrpg:crops_harvested` | `892` |
| `%mcrpg_stat_mobs_killed%` | `mcrpg:mobs_killed` | `430` |
| `%mcrpg_stat_damage_dealt%` | `mcrpg:damage_dealt` | `12,543.50` |
| `%mcrpg_stat_damage_taken%` | `mcrpg:damage_taken` | `8,761.25` |
| `%mcrpg_stat_total_levels%` | `mcrpg:total_skill_levels_gained` | `45` |
| `%mcrpg_stat_total_xp%` | `mcrpg:total_skill_experience` | `98,500` |
| `%mcrpg_stat_abilities_activated%` | `mcrpg:abilities_activated` | `312` |
| `%mcrpg_stat_<skill>_xp%` | `mcrpg:<skill>_experience` | `24,500` |
| `%mcrpg_stat_<skill>_max_level%` | `mcrpg:<skill>_max_level` | `50` |
| `%mcrpg_stat_<ability>_activations%` | `mcrpg:<ability>_activations` | `87` |

---

## Command Parser

### `StatisticParser`

**File:** `src/main/java/us/eunoians/mcrpg/command/parser/StatisticParser.java`

Cloud command parser for `Statistic` type, following the `SkillParser` pattern. Parses statistic display names against the `StatisticRegistry`.

```java
package us.eunoians.mcrpg.command.parser;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticRegistry;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionVariable;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.exception.parsing.ParserException;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Cloud command parser for {@link Statistic} arguments. Parses against the display names
 * of all registered statistics and provides tab completion.
 */
public class StatisticParser implements ArgumentParser<CommandSourceStack, Statistic>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    /**
     * Creates a {@link ParserDescriptor} for this parser.
     *
     * @return A parser descriptor.
     */
    public static @NotNull ParserDescriptor<CommandSourceStack, Statistic> statisticParser() {
        return ParserDescriptor.of(new StatisticParser(), Statistic.class);
    }

    @Override
    public @NotNull ArgumentParseResult<Statistic> parse(
            @NotNull CommandContext<CommandSourceStack> commandContext,
            @NotNull CommandInput commandInput) {
        String input = commandInput.peekString();
        StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
        for (Statistic statistic : registry.getRegisteredStatistics()) {
            if (statistic.getDisplayName().equalsIgnoreCase(input)) {
                commandInput.readString();
                return ArgumentParseResult.success(statistic);
            }
        }
        return ArgumentParseResult.failure(new StatisticParseException(input, commandContext));
    }

    @Override
    public @NotNull Iterable<String> stringSuggestions(
            @NotNull CommandContext<CommandSourceStack> commandContext,
            @NotNull CommandInput input) {
        StatisticRegistry registry = RegistryAccess.registryAccess().registry(RegistryKey.STATISTIC);
        return registry.getRegisteredStatistics().stream()
                .map(Statistic::getDisplayName)
                .map(String::toLowerCase)
                .toList();
    }

    private static class StatisticParseException extends ParserException {

        private final String input;

        public StatisticParseException(final @NotNull String input, final @NotNull CommandContext<?> context) {
            super(
                    StatisticParser.class,
                    context,
                    Caption.of("argument.parse.failure.statistic"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public String input() {
            return this.input;
        }
    }
}
```

---

## Commands

### `CommandPlaceholders` — new constants

**File:** `src/main/java/us/eunoians/mcrpg/command/CommandPlaceholders.java`

```java
STATISTIC_NAME("statistic-name"),
STATISTIC_VALUE("statistic-value"),
```

### `StatisticCommandBase`

**File:** `src/main/java/us/eunoians/mcrpg/command/statistic/StatisticCommandBase.java`

```java
package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.statistic.Statistic;
import net.kyori.adventure.audience.Audience;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.command.McRPGCommandBase;
import us.eunoians.mcrpg.command.admin.AdminBaseCommand;
import us.eunoians.mcrpg.external.papi.placeholder.statistic.StatisticPlaceholder;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.command.CommandPlaceholders.STATISTIC_NAME;
import static us.eunoians.mcrpg.command.CommandPlaceholders.STATISTIC_VALUE;

/**
 * Base command for all {@code /mcrpg statistic} commands.
 */
public class StatisticCommandBase extends AdminBaseCommand {

    protected static final Permission STATISTIC_COMMAND_ROOT_PERMISSION = Permission.of("mcrpg.statistic.*");

    /**
     * Builds a placeholder map that includes the standard sender/target placeholders
     * plus the statistic name and formatted value.
     *
     * @param messageAudience  The audience the message is being built for.
     * @param senderAudience   The command sender.
     * @param receiverAudience The command target.
     * @param statistic        The statistic, or {@code null} if not applicable.
     * @param value            The statistic value, or {@code null} for default.
     * @return A mutable placeholder map.
     */
    @NotNull
    protected static Map<String, String> getStatisticPlaceholders(
            @NotNull Audience messageAudience,
            @NotNull Audience senderAudience,
            @NotNull Audience receiverAudience,
            @Nullable Statistic statistic,
            @Nullable Object value) {
        Map<String, String> placeholders = new HashMap<>(McRPGCommandBase.getPlaceholders(messageAudience, senderAudience, receiverAudience));
        placeholders.put(STATISTIC_NAME.getPlaceholder(), statistic != null ? statistic.getDisplayName() : "Unknown");
        placeholders.put(STATISTIC_VALUE.getPlaceholder(), StatisticPlaceholder.formatValue(value));
        return placeholders;
    }
}
```

### `StatisticViewCommand`

**File:** `src/main/java/us/eunoians/mcrpg/command/statistic/StatisticViewCommand.java`

```java
package us.eunoians.mcrpg.command.statistic;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.manager.ManagerRegistry;
import com.diamonddagger590.mccore.statistic.Statistic;
import com.diamonddagger590.mccore.statistic.StatisticEntry;
import com.diamonddagger590.mccore.task.core.CoreTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.command.parser.StatisticParser;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.statistic.McRPGStatisticCacheManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Command: {@code /mcrpg statistic view <player> <statistic>}
 * <p>
 * Views a specific statistic value for a player. Supports both online and offline targets.
 */
public class StatisticViewCommand extends StatisticCommandBase {

    private static final Permission VIEW_PERMISSION = Permission.of("mcrpg.statistic.view");

    public static void registerCommand() {
        McRPG mcRPG = McRPG.getInstance();
        CommandManager<CommandSourceStack> commandManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(ManagerKey.COMMAND).getCommandManager();
        McRPGLocalizationManager localizationManager = mcRPG.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);

        commandManager.command(commandManager.commandBuilder("mcrpg")
                .literal("statistic")
                .commandDescription(RichDescription.richDescription(
                        localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC)))
                .literal("view")
                .required("player", OfflinePlayerParser.offlinePlayerParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_VIEW_PLAYER)))
                .required("statistic", StatisticParser.statisticParser(),
                        RichDescription.richDescription(
                                localizationManager.getLocalizedMessageAsComponent(LocalizationKey.COMMAND_DESCRIPTION_STATISTIC_VIEW_STATISTIC)))
                .permission(Permission.anyOf(ROOT_PERMISSION, STATISTIC_COMMAND_ROOT_PERMISSION, VIEW_PERMISSION))
                .handler(commandContext -> {
                    OfflinePlayer target = commandContext.get(CloudKey.of("player", OfflinePlayer.class));
                    Statistic statistic = commandContext.get(CloudKey.of("statistic", Statistic.class));
                    Audience sender = commandContext.sender().getSender();

                    // Online player: read directly
                    Optional<McRPGPlayer> onlineOptional = mcRPG.registryAccess()
                            .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                            .getPlayer(target.getUniqueId());
                    if (onlineOptional.isPresent()) {
                        Object value = onlineOptional.get().getStatisticData()
                                .getValue(statistic.getStatisticKey()).orElse(statistic.getDefaultValue());
                        Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, value);
                        sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                sender, LocalizationKey.STATISTIC_VIEW_MESSAGE, placeholders));
                        return;
                    }

                    // Offline player: async DB query
                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                            sender, LocalizationKey.STATISTIC_LOADING_MESSAGE,
                            getStatisticPlaceholders(sender, sender, sender, statistic, null)));

                    Database database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                            .manager(McRPGManagerKey.DATABASE).getDatabase();
                    database.getDatabaseExecutorService().submit(() -> {
                        try (Connection connection = database.getConnection()) {
                            Optional<StatisticEntry> entry = PlayerStatisticDAO.getPlayerStatistic(
                                    connection, target.getUniqueId(), statistic.getStatisticKey());
                            Object value = entry.map(StatisticEntry::value).orElse(statistic.getDefaultValue());

                            // Cache the result if cache manager is available
                            ManagerRegistry managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);
                            if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
                                entry.ifPresent(e -> managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE)
                                        .getCache().put(target.getUniqueId(), statistic.getStatisticKey(), e));
                            }

                            Map<String, String> placeholders = getStatisticPlaceholders(sender, sender, sender, statistic, value);
                            new CoreTask(mcRPG) {
                                @Override
                                public void run() {
                                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                            sender, LocalizationKey.STATISTIC_VIEW_MESSAGE, placeholders));
                                }
                            }.runTask();
                        } catch (SQLException e) {
                            new CoreTask(mcRPG) {
                                @Override
                                public void run() {
                                    sender.sendMessage(localizationManager.getLocalizedMessageAsComponent(
                                            sender, LocalizationKey.STATISTIC_LOOKUP_ERROR_MESSAGE));
                                }
                            }.runTask();
                            e.printStackTrace();
                        }
                    });
                }));
    }
}
```

**Design decisions:**

- Uses `OfflinePlayerParser` instead of `PlayerParser` to support offline player targets. Online players are resolved first through `McRPGPlayerManager.getPlayer()`.
- Async callback uses `CoreTask.runTask()` to return to the main thread, following the pattern in `ResetPlayerCommand`.
- Caches offline query results opportunistically to benefit subsequent PAPI placeholder calls.

### `StatisticListCommand`

**File:** `src/main/java/us/eunoians/mcrpg/command/statistic/StatisticListCommand.java`

Same async pattern as `StatisticViewCommand`. Key differences:

- Uses `PlayerStatisticDAO.getAllPlayerStatistics()` for offline players
- Iterates all registered statistics from `StatisticRegistry`
- Sends header message + one entry per statistic
- For online players, reads from `PlayerStatisticData` for each registered stat

```
Command: /mcrpg statistic list <player>
Permission: mcrpg.statistic.list
```

### `StatisticSetCommand`

**File:** `src/main/java/us/eunoians/mcrpg/command/statistic/StatisticSetCommand.java`

Key differences from `StatisticViewCommand`:

- Permission: `mcrpg.statistic.set` (admin)
- Takes an additional `<value>` string argument
- Parses value based on `statistic.getStatisticType()` (INT → `Integer.parseInt()`, LONG → `Long.parseLong()`, DOUBLE → `Double.parseDouble()`)
- For online players: calls `PlayerStatisticData.setValue()`
- For offline players: async `PlayerStatisticDAO.savePlayerStatistic()`
- Requires confirmation: `.meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)`

```
Command: /mcrpg statistic set <player> <statistic> <value>
Permission: mcrpg.statistic.set
Requires: /mcrpg confirm
```

### `StatisticResetCommand`

**File:** `src/main/java/us/eunoians/mcrpg/command/statistic/StatisticResetCommand.java`

Key differences:

- Permission: `mcrpg.statistic.reset` (admin)
- Optional `[statistic]` argument (if omitted, resets all)
- Resets to `Statistic.getDefaultValue()`
- For online players: calls `PlayerStatisticData.setValue()` with the default
- For offline players: async `PlayerStatisticDAO.savePlayerStatistic()` or `deletePlayerStatistic()` + cache invalidation
- Requires confirmation: `.meta(ConfirmationManager.META_CONFIRMATION_REQUIRED, true)`
- Uses different locale keys for single-stat reset vs reset-all

```
Command: /mcrpg statistic reset <player> [statistic]
Permission: mcrpg.statistic.reset
Requires: /mcrpg confirm
```

---

## Localization

### `LocalizationKey` — new constants

**File:** `src/main/java/us/eunoians/mcrpg/configuration/file/localization/LocalizationKey.java`

```java
// Statistic commands
private static final String STATISTIC_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "statistic");
public static final Route STATISTIC_VIEW_MESSAGE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "view-message"));
public static final Route STATISTIC_LIST_HEADER = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "list-header"));
public static final Route STATISTIC_LIST_ENTRY = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "list-entry"));
public static final Route STATISTIC_SET_SUCCESS = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "set-success"));
public static final Route STATISTIC_RESET_SUCCESS = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "reset-success"));
public static final Route STATISTIC_RESET_ALL_SUCCESS = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "reset-all-success"));
public static final Route STATISTIC_NOT_FOUND = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "not-found"));
public static final Route STATISTIC_PLAYER_NOT_FOUND = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "player-not-found"));
public static final Route STATISTIC_LOADING_MESSAGE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "loading-message"));
public static final Route STATISTIC_LOOKUP_ERROR_MESSAGE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "lookup-error-message"));
public static final Route STATISTIC_INVALID_VALUE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "invalid-value"));

// Statistic command descriptions
public static final Route COMMAND_DESCRIPTION_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-view"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-view-player"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-view-statistic"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_LIST = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-list"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_LIST_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-list-player"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_SET = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_SET_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set-player"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_SET_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set-statistic"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_SET_VALUE = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set-value"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-reset"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-reset-player"));
public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-reset-statistic"));
```

### English Locale YAML

**File:** `src/main/resources/localization/english/en_commands.yml`

Added under `commands:`:

```yaml
  # Configure statistic commands
  # Supports the following placeholders: <statistic-name>, <statistic-value>, <target>, <sender>
  statistic:
    # The message shown when viewing a statistic value.
    view-message: "<gray><gold><target></gold>'s <gold><statistic-name></gold>: <gold><statistic-value></gold>"
    # The header shown at the top of the statistic list.
    list-header: "<gray>--- Statistics for <gold><target></gold> ---"
    # Each line in the statistic list.
    list-entry: "<gray>  <gold><statistic-name></gold>: <gold><statistic-value></gold>"
    # The message shown when a statistic is set successfully.
    set-success: "<gray>Set <gold><statistic-name></gold> to <gold><statistic-value></gold> for <gold><target></gold>."
    # The message shown when a statistic is reset successfully.
    reset-success: "<gray>Reset <gold><statistic-name></gold> for <gold><target></gold>."
    # The message shown when all statistics are reset.
    reset-all-success: "<gray>Reset all statistics for <gold><target></gold>."
    # The message shown when a statistic key is not recognized.
    not-found: "<red>Statistic not found: <gray><statistic-name></gray>"
    # The message shown when the target player is not found.
    player-not-found: "<red>Player <gray><target></gray> not found."
    # The message shown while loading offline player data.
    loading-message: "<gray>Loading statistics for <gold><target></gold>..."
    # The message shown when a database lookup fails.
    lookup-error-message: "<red>Failed to look up statistics. Please try again."
    # The message shown when an invalid value is provided for the set command.
    invalid-value: "<red>Invalid value <gray><statistic-value></gray> for statistic <gray><statistic-name></gray>."
```

Added under the existing `descriptions:` section:

```yaml
    statistic: "<gray>The subcommand for viewing and managing statistics."
    statistic-view: "<gray>View a specific statistic for a player."
    statistic-view-player: "<gray>The player to view statistics for."
    statistic-view-statistic: "<gray>The statistic to view."
    statistic-list: "<gray>List all statistics for a player."
    statistic-list-player: "<gray>The player to list statistics for."
    statistic-set: "<gray>Set a statistic value for a player."
    statistic-set-player: "<gray>The player to set the statistic for."
    statistic-set-statistic: "<gray>The statistic to set."
    statistic-set-value: "<gray>The value to set the statistic to."
    statistic-reset: "<gray>Reset a statistic (or all statistics) for a player."
    statistic-reset-player: "<gray>The player to reset the statistic for."
    statistic-reset-statistic: "<gray>The statistic to reset. If omitted, resets all."
```

---

## Bootstrap & Registration Changes

### `McRPGBootstrap` — register cache manager

**File:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGBootstrap.java`

Inside the `if (startupProfile == StartupProfile.PROD)` block, **after** `McRPGDatabaseManager` registration and **before** `McRPGCommandRegistrar`:

```java
// Statistic cache manager (used by PAPI placeholders and commands)
FileManager fileManager = registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
boolean cacheEnabled = fileManager.getFile(FileType.MAIN_CONFIG)
        .getBoolean(MainConfigFile.STATISTICS_CACHE_ENABLED, true);
if (cacheEnabled) {
    long maxSize = fileManager.getFile(FileType.MAIN_CONFIG)
            .getLong(MainConfigFile.STATISTICS_CACHE_MAX_SIZE, 1000);
    long ttl = fileManager.getFile(FileType.MAIN_CONFIG)
            .getLong(MainConfigFile.STATISTICS_CACHE_TTL, 300);
    registryAccess.registry(RegistryKey.MANAGER)
            .register(new McRPGStatisticCacheManager(mcRPG, maxSize, ttl));
}
```

**Note:** The PAPI hooks registrar runs **before** the PROD block, but this is fine — `McRPGPapiExpansion` registers placeholder types during hook init, but the actual `parsePlaceholder()` calls happen at runtime (long after bootstrap). The `StatisticPlaceholder.parsePlaceholder()` checks `managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)` at call time.

### `McRPGCommandRegistrar` — register commands

**File:** `src/main/java/us/eunoians/mcrpg/bootstrap/McRPGCommandRegistrar.java`

Add in `register()`:

```java
// Statistic commands
StatisticViewCommand.registerCommand();
StatisticListCommand.registerCommand();
StatisticSetCommand.registerCommand();
StatisticResetCommand.registerCommand();
```

### `McRPGPlayerLoadTask` — cache invalidation

**File:** `src/main/java/us/eunoians/mcrpg/task/player/McRPGPlayerLoadTask.java`

In `onPlayerLoadSuccessfully()`, after player tracking:

```java
// Invalidate offline stat cache — live data takes over
ManagerRegistry managerRegistry = getPlugin().registryAccess().registry(RegistryKey.MANAGER);
if (managerRegistry.registered(McRPGManagerKey.STATISTIC_CACHE)) {
    managerRegistry.manager(McRPGManagerKey.STATISTIC_CACHE).getCache().invalidate(getCorePlayer().getUUID());
}
```

---

## Implementation Order

1. Add config routes to `MainConfigFile` and default values to `config.yml`
2. Create `McRPGStatisticCacheManager`
3. Add `STATISTIC_CACHE` to `McRPGManagerKey`
4. Wire cache construction in `McRPGBootstrap`
5. Add cache invalidation in `McRPGPlayerLoadTask.onPlayerLoadSuccessfully()`
6. Create `StatisticPlaceholder`
7. Add `STATISTIC` to `McRPGPlaceHolderType`
8. Add `STATISTIC_NAME` and `STATISTIC_VALUE` to `CommandPlaceholders`
9. Add localization keys to `LocalizationKey`
10. Add English locale entries to `en_commands.yml`
11. Create `StatisticParser`
12. Create `StatisticCommandBase`
13. Create `StatisticViewCommand`
14. Create `StatisticListCommand`
15. Create `StatisticSetCommand` (with confirmation)
16. Create `StatisticResetCommand` (with confirmation)
17. Register commands in `McRPGCommandRegistrar`
18. Add unit tests for `StatisticPlaceholder.formatValue()` and `StatisticParser`
19. Run `./gradlew verifiedShadowJar` — build + all existing tests pass
20. Commit and push

---

## Verification

1. `./gradlew verifiedShadowJar` — build + all existing tests pass
2. New PAPI placeholders resolve for online players (manual test on running server)
3. Offline stat queries return cached values on second call
4. `/mcrpg statistic view <player> <statistic>` — online and offline targets
5. `/mcrpg statistic list <player>` — shows all stats with formatted values
6. `/mcrpg statistic set <player> <statistic> <value>` — requires `/mcrpg confirm`, updates value
7. `/mcrpg statistic reset <player> [statistic]` — requires `/mcrpg confirm`, resets to default
8. Config changes (`enabled: false`) disable cache; placeholders still work for online players
9. Tab completion works for `<statistic>` argument in all commands
