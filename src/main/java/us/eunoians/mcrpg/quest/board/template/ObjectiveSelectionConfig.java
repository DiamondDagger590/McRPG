package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;

/**
 * Configures how objectives are selected within a template stage.
 * The default mode ({@code ALL}) preserves existing behavior.
 * {@code WEIGHTED_RANDOM} selects a random subset by weight without replacement.
 *
 * @param mode     the selection mode
 * @param minCount the minimum number of objectives to select
 * @param maxCount the maximum number of objectives to select
 */
public record ObjectiveSelectionConfig(
        @NotNull ObjectiveSelectionMode mode,
        int minCount,
        int maxCount
) {

    public ObjectiveSelectionConfig {
        if (minCount < 1) {
            throw new IllegalArgumentException("minCount must be >= 1, got: " + minCount);
        }
        if (maxCount < minCount) {
            throw new IllegalArgumentException(
                    "maxCount must be >= minCount, got: max=" + maxCount + ", min=" + minCount);
        }
    }

    public enum ObjectiveSelectionMode {
        ALL,
        WEIGHTED_RANDOM
    }
}
