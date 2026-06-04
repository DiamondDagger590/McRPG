package us.eunoians.mcrpg.database.table.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Lightweight DTO representing a single completed chain run for a player's history GUI.
 *
 * @param chainKey        the chain definition key
 * @param completionNumber the 1-based completion index for this chain (1 = first completion)
 * @param completedAt     the timestamp of the last step's completion in this run
 * @param stepCount       the number of quest steps recorded in this run
 */
public record ChainCompletionRun(
        @NotNull NamespacedKey chainKey,
        int completionNumber,
        @NotNull Instant completedAt,
        int stepCount
) {
}
