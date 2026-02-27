package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Stateless utility that performs weighted random selection of objectives from a
 * candidate pool. Selection is without replacement — an objective is never selected
 * twice. Uses the generation context's {@link Random} for deterministic seeding.
 */
public final class WeightedObjectiveSelector {

    private WeightedObjectiveSelector() {}

    /**
     * Selects a random subset of objectives from the candidates using weighted
     * random selection without replacement.
     *
     * @param candidates      objectives that passed condition evaluation
     * @param selectionConfig min/max count and mode
     * @param random          seeded random for deterministic generation
     * @return the selected subset, preserving relative order from the candidate list
     * @throws QuestGenerationException if fewer candidates exist than minCount
     */
    @NotNull
    public static List<TemplateObjectiveDefinition> select(
            @NotNull List<TemplateObjectiveDefinition> candidates,
            @NotNull ObjectiveSelectionConfig selectionConfig,
            @NotNull Random random) {

        if (candidates.size() < selectionConfig.minCount()) {
            throw new IllegalStateException(
                    "Weighted objective selection requires at least " + selectionConfig.minCount()
                    + " candidates, but only " + candidates.size() + " passed condition evaluation");
        }

        int count = random.nextInt(selectionConfig.minCount(), selectionConfig.maxCount() + 1);
        count = Math.min(count, candidates.size());

        List<TemplateObjectiveDefinition> pool = new ArrayList<>(candidates);
        Set<Integer> selectedIndices = new LinkedHashSet<>();

        for (int i = 0; i < count; i++) {
            int totalWeight = pool.stream().mapToInt(TemplateObjectiveDefinition::getWeight).sum();
            int roll = random.nextInt(totalWeight);
            int cumulative = 0;
            for (int j = 0; j < pool.size(); j++) {
                cumulative += pool.get(j).getWeight();
                if (roll < cumulative) {
                    selectedIndices.add(candidates.indexOf(pool.get(j)));
                    pool.remove(j);
                    break;
                }
            }
        }

        return selectedIndices.stream()
                .sorted()
                .map(candidates::get)
                .toList();
    }
}
