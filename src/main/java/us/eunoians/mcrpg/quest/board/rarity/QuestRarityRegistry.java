package us.eunoians.mcrpg.quest.board.rarity;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Registry for {@link QuestRarity} instances.
 * <p>
 * Supports dual-source registration: config-loaded rarities (reloadable via
 * {@link #replaceConfigRarities}) and expansion-registered rarities (persistent across reloads).
 */
public class QuestRarityRegistry implements Registry<QuestRarity> {

    private final Map<NamespacedKey, QuestRarity> rarities = new LinkedHashMap<>();
    private final Set<NamespacedKey> configLoadedKeys = new HashSet<>();

    /**
     * Registers a rarity. If a rarity with the same key already exists, it is replaced.
     *
     * @param rarity the rarity to register
     */
    public void register(@NotNull QuestRarity rarity) {
        rarities.put(rarity.getKey(), rarity);
    }

    /**
     * Gets a registered rarity by its key.
     *
     * @param key the namespaced key
     * @return the rarity, or empty if not registered
     */
    @NotNull
    public Optional<QuestRarity> get(@NotNull NamespacedKey key) {
        return Optional.ofNullable(rarities.get(key));
    }

    /**
     * Gets all registered rarities.
     *
     * @return an unmodifiable collection of all rarities
     */
    @NotNull
    public Collection<QuestRarity> getAll() {
        return Set.copyOf(rarities.values());
    }

    /**
     * Gets all registered rarity keys.
     *
     * @return an unmodifiable set of all keys
     */
    @NotNull
    public Set<NamespacedKey> getRegisteredKeys() {
        return Set.copyOf(rarities.keySet());
    }

    /**
     * Rolls a random rarity based on configured weights using weighted random selection.
     *
     * @param random the random source
     * @return the selected rarity
     * @throws IllegalStateException if no rarities are registered
     */
    @NotNull
    public QuestRarity rollRarity(@NotNull Random random) {
        if (rarities.isEmpty()) {
            throw new IllegalStateException("No rarities registered — cannot roll");
        }

        int totalWeight = rarities.values().stream().mapToInt(QuestRarity::getWeight).sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (QuestRarity rarity : rarities.values()) {
            cumulative += rarity.getWeight();
            if (roll < cumulative) {
                return rarity;
            }
        }

        // Should never reach here, but return last as fallback
        return rarities.values().iterator().next();
    }

    /**
     * Replaces config-loaded rarities with a fresh set from {@code board.yml}.
     * Expansion-registered rarities are untouched.
     *
     * @param freshConfig the new config-loaded rarities
     */
    public void replaceConfigRarities(@NotNull Map<NamespacedKey, QuestRarity> freshConfig) {
        configLoadedKeys.forEach(rarities::remove);
        configLoadedKeys.clear();
        freshConfig.forEach((key, rarity) -> {
            rarities.put(key, rarity);
            configLoadedKeys.add(key);
        });
    }

    /**
     * Clears all registered rarities (config-loaded and expansion-registered).
     */
    public void clear() {
        rarities.clear();
        configLoadedKeys.clear();
    }

    @Override
    public boolean registered(@NotNull QuestRarity rarity) {
        return rarities.containsKey(rarity.getKey());
    }
}
