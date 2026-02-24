package us.eunoians.mcrpg.quest.board.template.variable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The result of resolving a {@link PoolVariable} for a specific rarity.
 * <p>
 * When a {@link PoolVariable} selects one or more pools during resolution, each
 * selected pool contributes its values list. {@code mergedValues} is the flat
 * concatenation of all selected pools' value lists. For example, if the engine
 * selects pool "ores" (values: [IRON_ORE, COPPER_ORE]) and pool "precious"
 * (values: [DIAMOND_ORE]), then {@code mergedValues} = [IRON_ORE, COPPER_ORE,
 * DIAMOND_ORE]. This merged list is substituted into objective/reward config maps
 * wherever the pool variable name is referenced.
 *
 * @param mergedValues     flat concatenation of all values from every selected pool
 * @param averageDifficulty arithmetic mean of the difficulty scalars of all selected pools
 */
public record ResolvedPool(
        @NotNull List<String> mergedValues,
        double averageDifficulty
) {

    public ResolvedPool {
        mergedValues = List.copyOf(mergedValues);
    }
}
