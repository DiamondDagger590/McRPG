package us.eunoians.mcrpg.stat;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of {@link PlayerStat} definitions. Populated during plugin startup via
 * {@link us.eunoians.mcrpg.expansion.content.PlayerStatContentPack} processing and used
 * to seed {@link us.eunoians.mcrpg.stat.instance.PlayerStatData} instances when players join.
 * <p>
 * Stat definitions are registered once and are immutable after registration.
 * Third-party plugins register custom stats via their own
 * {@link us.eunoians.mcrpg.expansion.content.PlayerStatContentPack} in a
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion}.
 * <p>
 * Accessed as a top-level registry via
 * {@code registryAccess().registry(McRPGRegistryKey.PLAYER_STAT)}.
 */
public class PlayerStatRegistry implements Registry<PlayerStat> {

    private final Map<NamespacedKey, PlayerStat> stats = new LinkedHashMap<>();

    /**
     * Registers a stat definition. Duplicate keys are rejected.
     *
     * @param stat The stat definition to register.
     * @throws IllegalArgumentException If a stat with the same key is already registered.
     */
    public void register(@NotNull PlayerStat stat) {
        if (stats.containsKey(stat.getKey())) {
            throw new IllegalArgumentException("Player stat already registered: " + stat.getKey());
        }
        stats.put(stat.getKey(), stat);
    }

    /**
     * Looks up a stat definition by its key.
     *
     * @param key The stat key.
     * @return An optional containing the stat definition, or empty if not registered.
     */
    @NotNull
    public Optional<PlayerStat> getStat(@NotNull NamespacedKey key) {
        return Optional.ofNullable(stats.get(key));
    }

    /**
     * @return An unmodifiable collection of all registered stat definitions.
     */
    @NotNull
    public Collection<PlayerStat> allStats() {
        return Collections.unmodifiableCollection(stats.values());
    }

    /**
     * Checks whether the given {@link PlayerStat} is registered.
     *
     * @param stat The stat to check.
     * @return {@code true} if the stat is registered.
     */
    @Override
    public boolean registered(@NotNull PlayerStat stat) {
        return stats.containsKey(stat.getKey());
    }
}
