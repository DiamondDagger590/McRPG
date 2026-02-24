package us.eunoians.mcrpg.quest.board.template.variable;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A pool-based template variable that selects one or more {@link Pool}s by
 * weighted random (per rarity), merges their values, and computes an average
 * difficulty scalar. Selection is performed <b>without replacement</b> when
 * multiple pools are selected.
 */
public final class PoolVariable implements TemplateVariable {

    private final String name;
    private final int minSelections;
    private final int maxSelections;
    private final List<Pool> pools;

    public PoolVariable(@NotNull String name,
                        int minSelections,
                        int maxSelections,
                        @NotNull List<Pool> pools) {
        if (minSelections < 1) {
            throw new IllegalArgumentException("minSelections must be >= 1, got " + minSelections);
        }
        if (maxSelections < minSelections) {
            throw new IllegalArgumentException("maxSelections (" + maxSelections
                    + ") must be >= minSelections (" + minSelections + ")");
        }
        this.name = name;
        this.minSelections = minSelections;
        this.maxSelections = maxSelections;
        this.pools = List.copyOf(pools);
    }

    @Override
    @NotNull
    public VariableType getType() {
        return VariableType.POOL;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Returns the minimum number of pools that will be selected during resolution.
     * Always {@code >= 1}.
     *
     * @return the minimum pool selection count
     */
    public int getMinSelections() {
        return minSelections;
    }

    /**
     * Returns the maximum number of pools that may be selected during resolution.
     * Always {@code >= minSelections}. The actual count is capped by the number
     * of eligible (non-zero-weight) pools.
     *
     * @return the maximum pool selection count
     */
    public int getMaxSelections() {
        return maxSelections;
    }

    /**
     * Returns the immutable list of candidate pools for this variable.
     *
     * @return the pools available for weighted selection
     */
    @NotNull
    public List<Pool> getPools() {
        return pools;
    }

    /**
     * Resolves this pool variable for a given rarity by performing weighted
     * random selection of pools and merging their values.
     *
     * @param rarityKey the rarity to use for weight lookup
     * @param random    the seeded random source
     * @return the resolved pool result (merged values + average difficulty)
     */
    @NotNull
    public ResolvedPool resolve(@NotNull NamespacedKey rarityKey, @NotNull Random random) {
        List<Pool> eligible = pools.stream()
                .filter(pool -> pool.getWeightForRarity(rarityKey) > 0)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (eligible.isEmpty()) {
            return new ResolvedPool(List.of(), 0.0);
        }

        int selectionCount = minSelections == maxSelections
                ? minSelections
                : minSelections + random.nextInt(maxSelections - minSelections + 1);
        selectionCount = Math.min(selectionCount, eligible.size());

        List<Pool> selected = new ArrayList<>(selectionCount);
        for (int i = 0; i < selectionCount; i++) {
            Pool pick = weightedSelect(eligible, rarityKey, random);
            selected.add(pick);
            eligible.remove(pick);
        }

        List<String> mergedValues = new ArrayList<>();
        double totalDifficulty = 0.0;
        for (Pool pool : selected) {
            mergedValues.addAll(pool.values());
            totalDifficulty += pool.difficulty();
        }

        double averageDifficulty = totalDifficulty / selected.size();
        return new ResolvedPool(mergedValues, averageDifficulty);
    }

    /**
     * Selects a single pool from the eligible list using weighted random selection.
     * Each pool's weight for the given rarity determines its probability of being
     * chosen. Pools with higher weights are proportionally more likely to be picked.
     *
     * @param eligible  the non-empty list of pools with positive weights to choose from
     * @param rarityKey the rarity key used to look up per-pool weights
     * @param random    the random source for the weighted roll
     * @return the selected pool
     */
    @NotNull
    private static Pool weightedSelect(@NotNull List<Pool> eligible,
                                       @NotNull NamespacedKey rarityKey,
                                       @NotNull Random random) {
        int totalWeight = eligible.stream()
                .mapToInt(p -> p.getWeightForRarity(rarityKey))
                .sum();
        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (Pool pool : eligible) {
            cumulative += pool.getWeightForRarity(rarityKey);
            if (roll < cumulative) {
                return pool;
            }
        }

        return eligible.get(eligible.size() - 1);
    }
}
