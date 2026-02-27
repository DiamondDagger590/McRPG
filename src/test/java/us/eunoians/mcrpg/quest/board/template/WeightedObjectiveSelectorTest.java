package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WeightedObjectiveSelectorTest {

    private static final NamespacedKey TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");

    private TemplateObjectiveDefinition obj(String name, int weight) {
        return new TemplateObjectiveDefinition(
                NamespacedKey.fromString("mcrpg:" + name), "10", Map.of(), null, weight);
    }

    @Test
    @DisplayName("selects exactly the requested count")
    void selectsExactCount() {
        List<TemplateObjectiveDefinition> candidates = List.of(
                obj("a", 80), obj("b", 60), obj("c", 40));
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 2, 2);

        List<TemplateObjectiveDefinition> result = WeightedObjectiveSelector.select(
                candidates, config, new Random(42));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("min-count equals candidate count selects all")
    void minCountEqualsSize() {
        List<TemplateObjectiveDefinition> candidates = List.of(
                obj("a", 10), obj("b", 20), obj("c", 30));
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 3, 3);

        List<TemplateObjectiveDefinition> result = WeightedObjectiveSelector.select(
                candidates, config, new Random(42));
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("fewer candidates than min-count throws exception")
    void tooFewCandidatesThrows() {
        List<TemplateObjectiveDefinition> candidates = List.of(
                obj("a", 10), obj("b", 20));
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 3, 3);

        assertThrows(IllegalStateException.class,
                () -> WeightedObjectiveSelector.select(candidates, config, new Random(42)));
    }

    @Test
    @DisplayName("single candidate with select 1 returns that candidate")
    void singleCandidate() {
        TemplateObjectiveDefinition sole = obj("sole", 1);
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 1, 1);

        List<TemplateObjectiveDefinition> result = WeightedObjectiveSelector.select(
                List.of(sole), config, new Random(42));
        assertEquals(1, result.size());
        assertSame(sole, result.get(0));
    }

    @Test
    @DisplayName("selection is without replacement (no duplicates)")
    void noDuplicates() {
        List<TemplateObjectiveDefinition> candidates = List.of(
                obj("a", 100), obj("b", 1), obj("c", 1));
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 3, 3);

        List<TemplateObjectiveDefinition> result = WeightedObjectiveSelector.select(
                candidates, config, new Random(42));
        Set<TemplateObjectiveDefinition> unique = new HashSet<>(result);
        assertEquals(result.size(), unique.size());
    }

    @Test
    @DisplayName("selected objectives preserve relative order")
    void preservesOrder() {
        TemplateObjectiveDefinition a = obj("a", 50);
        TemplateObjectiveDefinition b = obj("b", 50);
        TemplateObjectiveDefinition c = obj("c", 50);
        List<TemplateObjectiveDefinition> candidates = List.of(a, b, c);
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 2, 2);

        List<TemplateObjectiveDefinition> result = WeightedObjectiveSelector.select(
                candidates, config, new Random(42));

        int idx0 = candidates.indexOf(result.get(0));
        int idx1 = candidates.indexOf(result.get(1));
        assertTrue(idx0 < idx1, "Selected objectives should preserve relative order from candidates");
    }

    @Test
    @DisplayName("count between min and max")
    void countBetweenMinMax() {
        List<TemplateObjectiveDefinition> candidates = List.of(
                obj("a", 10), obj("b", 20), obj("c", 30), obj("d", 40), obj("e", 50));
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 2, 4);

        for (int seed = 0; seed < 50; seed++) {
            List<TemplateObjectiveDefinition> result = WeightedObjectiveSelector.select(
                    candidates, config, new Random(seed));
            assertTrue(result.size() >= 2 && result.size() <= 4,
                    "Result size " + result.size() + " should be between 2 and 4");
        }
    }

    @Test
    @DisplayName("seeded random produces deterministic selection")
    void deterministicWithSeed() {
        List<TemplateObjectiveDefinition> candidates = List.of(
                obj("a", 80), obj("b", 60), obj("c", 40), obj("d", 20));
        ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 2, 2);

        List<TemplateObjectiveDefinition> result1 = WeightedObjectiveSelector.select(
                candidates, config, new Random(99));
        List<TemplateObjectiveDefinition> result2 = WeightedObjectiveSelector.select(
                candidates, config, new Random(99));
        assertEquals(result1, result2);
    }
}
