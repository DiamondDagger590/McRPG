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
                plugin().getLogger().log(java.util.logging.Level.WARNING, "Failed to populate statistic cache for player " + uuid, e);
            }
        });
    }
}
