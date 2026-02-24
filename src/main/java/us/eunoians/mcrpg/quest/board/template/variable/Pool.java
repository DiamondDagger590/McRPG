package us.eunoians.mcrpg.quest.board.template.variable;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * A single pool within a {@link PoolVariable}. Each pool has a set of values
 * (e.g., block types or entity types), a difficulty scalar, and per-rarity
 * weights that control how likely this pool is to be selected for a given rarity.
 *
 * @param name       human-readable pool name (e.g., "ores", "precious")
 * @param difficulty difficulty scalar contributed when this pool is selected
 * @param weights    per-rarity selection weights; rarities not present default to 0
 * @param values     the values contributed when this pool is selected (block names, entity names, etc.)
 */
public record Pool(
        @NotNull String name,
        double difficulty,
        @NotNull Map<NamespacedKey, Integer> weights,
        @NotNull List<String> values
) {

    public Pool {
        weights = Map.copyOf(weights);
        values = List.copyOf(values);
    }

    /**
     * Returns the weight for a specific rarity, defaulting to 0 if not present.
     */
    public int getWeightForRarity(@NotNull NamespacedKey rarityKey) {
        return weights.getOrDefault(rarityKey, 0);
    }
}
