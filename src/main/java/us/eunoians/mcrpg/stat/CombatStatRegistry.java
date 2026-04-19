package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of {@link CombatStat} definitions. Populated during plugin startup
 * and used to seed {@link PlayerCombatData} instances when players join.
 * <p>
 * Stat definitions are registered once and are immutable after registration.
 * Third-party plugins will be able to register custom stats via a future
 * {@code CombatStatContentPack} extension point.
 */
public class CombatStatRegistry {

    private final Map<NamespacedKey, CombatStat> stats = new LinkedHashMap<>();

    /**
     * Registers a stat definition. Duplicate keys are rejected.
     *
     * @param stat The stat definition to register.
     * @throws IllegalArgumentException If a stat with the same key is already registered.
     */
    public void register(@NotNull CombatStat stat) {
        if (stats.containsKey(stat.getKey())) {
            throw new IllegalArgumentException("Combat stat already registered: " + stat.getKey());
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
    public Optional<CombatStat> getStat(@NotNull NamespacedKey key) {
        return Optional.ofNullable(stats.get(key));
    }

    /**
     * @return An unmodifiable collection of all registered stat definitions.
     */
    @NotNull
    public Collection<CombatStat> allStats() {
        return Collections.unmodifiableCollection(stats.values());
    }
}
