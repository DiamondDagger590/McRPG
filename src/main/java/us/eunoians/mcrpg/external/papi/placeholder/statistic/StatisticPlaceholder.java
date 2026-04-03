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

    private static final ThreadLocal<NumberFormat> INTEGER_FORMAT = ThreadLocal.withInitial(NumberFormat::getIntegerInstance);
    private static final ThreadLocal<DecimalFormat> DOUBLE_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,##0.00"));

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
    public static String formatValue(@Nullable Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Integer i) {
            return INTEGER_FORMAT.get().format(i);
        }
        if (value instanceof Long l) {
            return INTEGER_FORMAT.get().format(l);
        }
        if (value instanceof Double d) {
            return DOUBLE_FORMAT.get().format(d);
        }
        return value.toString();
    }
}
