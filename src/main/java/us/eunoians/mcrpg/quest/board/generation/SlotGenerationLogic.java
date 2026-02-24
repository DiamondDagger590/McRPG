package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Stateless utility with pure functions for slot count computation and quest selection.
 * No Bukkit dependency for testability.
 */
public final class SlotGenerationLogic {

    private SlotGenerationLogic() {
    }

    /**
     * Computes how many slots each category should receive for a rotation.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Categories are processed in priority order (descending).</li>
     *   <li>Each category gets at least {@code min} slots.</li>
     *   <li>Additional slots up to {@code max} are granted based on {@code chancePerSlot} rolls.</li>
     *   <li>Categories on cooldown are skipped.</li>
     *   <li>If total slots fall below {@code minimumTotalOfferings}, the highest-priority
     *       non-cooldown categories receive backfill slots.</li>
     * </ol>
     *
     * @param categories            categories sorted by priority (descending)
     * @param minimumTotalOfferings the minimum total offerings the board must have
     * @param random                the random source
     * @param isCategoryOnCooldown  predicate returning {@code true} if a category is on appearance cooldown
     * @return a map of category key to slot count
     */
    @NotNull
    public static Map<NamespacedKey, Integer> computeSlotCounts(
            @NotNull List<BoardSlotCategory> categories,
            int minimumTotalOfferings,
            @NotNull Random random,
            @NotNull Predicate<NamespacedKey> isCategoryOnCooldown) {

        Map<NamespacedKey, Integer> counts = new LinkedHashMap<>();
        int totalSlots = 0;

        for (BoardSlotCategory category : categories) {
            if (isCategoryOnCooldown.test(category.getKey())) {
                continue;
            }

            int slots = category.getMin();
            for (int i = slots; i < category.getMax(); i++) {
                if (random.nextDouble() < category.getChancePerSlot()) {
                    slots++;
                }
            }

            if (slots > 0) {
                counts.put(category.getKey(), slots);
                totalSlots += slots;
            }
        }

        // Backfill if below minimum
        if (totalSlots < minimumTotalOfferings && !counts.isEmpty()) {
            int deficit = minimumTotalOfferings - totalSlots;
            for (BoardSlotCategory category : categories) {
                if (deficit <= 0) break;
                if (isCategoryOnCooldown.test(category.getKey())) continue;

                int current = counts.getOrDefault(category.getKey(), 0);
                int available = category.getMax() - current;
                int add = Math.min(available, deficit);
                if (add > 0) {
                    counts.merge(category.getKey(), add, Integer::sum);
                    deficit -= add;
                }
            }
        }

        return counts;
    }

    /**
     * Selects a random quest from a list of eligible definitions.
     *
     * @param eligibleDefinitions the eligible definition keys
     * @param random              the random source
     * @return the selected definition key, or empty if the list is empty
     */
    @NotNull
    public static Optional<NamespacedKey> selectQuestForSlot(
            @NotNull List<NamespacedKey> eligibleDefinitions,
            @NotNull Random random) {
        if (eligibleDefinitions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(eligibleDefinitions.get(random.nextInt(eligibleDefinitions.size())));
    }
}
