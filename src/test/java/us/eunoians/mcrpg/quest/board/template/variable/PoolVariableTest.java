package us.eunoians.mcrpg.quest.board.template.variable;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.variable.Pool;
import us.eunoians.mcrpg.quest.board.template.variable.ResolvedPool;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PoolVariableTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");

    private static Pool stonePool() {
        return new Pool("stone", 1.0, Map.of(COMMON, 80, RARE, 0), List.of("STONE", "COBBLESTONE"));
    }

    private static Pool orePool() {
        return new Pool("ores", 1.5, Map.of(COMMON, 15, RARE, 80), List.of("IRON_ORE", "COAL_ORE"));
    }

    private static Pool preciousPool() {
        return new Pool("precious", 3.0, Map.of(COMMON, 0, RARE, 40), List.of("DIAMOND_ORE"));
    }

    @Test
    @DisplayName("Given min=1 max=1, when resolve is called, then always selects exactly one pool")
    void singlePoolSelectionMin1Max1_alwaysSelectsOne() {
        PoolVariable variable = new PoolVariable("blocks", 1, 1,
                List.of(stonePool(), orePool(), preciousPool()));

        Set<String> stoneValues = Set.of("STONE", "COBBLESTONE");
        Set<String> oreValues = Set.of("IRON_ORE", "COAL_ORE");
        Set<String> preciousValues = Set.of("DIAMOND_ORE");

        for (int i = 0; i < 50; i++) {
            Random random = new Random(i);
            ResolvedPool resolved = variable.resolve(COMMON, random);

            assertNotNull(resolved.mergedValues());
            assertFalse(resolved.mergedValues().isEmpty(),
                    "Expected exactly one pool's values; iteration " + i);
            boolean fromOnePool = stoneValues.containsAll(resolved.mergedValues())
                    || oreValues.containsAll(resolved.mergedValues())
                    || preciousValues.containsAll(resolved.mergedValues());
            assertTrue(fromOnePool,
                    "Values should come from exactly one pool; got " + resolved.mergedValues() + " for iteration " + i);
            assertTrue(resolved.averageDifficulty() >= 1.0 && resolved.averageDifficulty() <= 3.0,
                    "Average difficulty should be from single pool");
        }
    }

    @Test
    @DisplayName("Given min=1 max=3, when resolve is called, then selects between 1 and 3 pools")
    void multiplePoolSelectionMin1Max3_selectsOneToThree() {
        PoolVariable variable = new PoolVariable("blocks", 1, 3,
                List.of(stonePool(), orePool(), preciousPool()));

        Set<Integer> selectionCounts = new java.util.HashSet<>();
        for (int seed = 0; seed < 200; seed++) {
            Random random = new Random(seed);
            ResolvedPool resolved = variable.resolve(RARE, random);

            int distinctSourceCount = countDistinctPoolSources(resolved.mergedValues());
            assertTrue(distinctSourceCount >= 1 && distinctSourceCount <= 3,
                    "Should select 1–3 pools; got distinct sources: " + distinctSourceCount + " for seed " + seed);
            selectionCounts.add(distinctSourceCount);
        }
        assertTrue(selectionCounts.size() >= 2,
                "Over 200 seeds, should see at least 2 different selection counts");
    }

    @Test
    @DisplayName("Given zero-weight pools for a rarity, when resolve is called, then zero-weight pools are never selected")
    void poolSelectionRespectsRarityWeights_zeroWeightNeverSelected() {
        PoolVariable variable = new PoolVariable("blocks", 1, 1,
                List.of(stonePool(), orePool(), preciousPool()));

        for (int i = 0; i < 100; i++) {
            ResolvedPool resolved = variable.resolve(RARE, new Random(i));
            assertFalse(resolved.mergedValues().contains("STONE") || resolved.mergedValues().contains("COBBLESTONE"),
                    "stonePool has weight 0 for RARE; should never be selected");
        }

        for (int i = 0; i < 100; i++) {
            ResolvedPool resolved = variable.resolve(COMMON, new Random(i + 1000));
            assertFalse(resolved.mergedValues().contains("DIAMOND_ORE"),
                    "preciousPool has weight 0 for COMMON; should never be selected");
        }
    }

    @Test
    @DisplayName("Given multiple selected pools, when resolve is called, then averageDifficulty is computed correctly")
    void averageDifficultyComputedCorrectly() {
        PoolVariable variable = new PoolVariable("blocks", 2, 2,
                List.of(stonePool(), orePool(), preciousPool()));

        long seed = 12345;
        Random random = new Random(seed);
        ResolvedPool resolved = variable.resolve(RARE, random);

        assertEquals(2, countDistinctPoolSources(resolved.mergedValues()));
        double expectedMin = Math.min(stonePool().difficulty(), Math.min(orePool().difficulty(), preciousPool().difficulty()));
        double expectedMax = Math.max(stonePool().difficulty(), Math.max(orePool().difficulty(), preciousPool().difficulty()));
        assertTrue(resolved.averageDifficulty() >= expectedMin && resolved.averageDifficulty() <= expectedMax);
    }

    @Test
    @DisplayName("Given multiple selected pools, when resolve is called, then mergedValues combines all selected pool values")
    void mergedValuesCombinesAllSelectedPools() {
        // RARE has ore + precious eligible (stone has 0 weight). min=2 max=2 => exactly 2 pools.
        PoolVariable variable = new PoolVariable("blocks", 2, 2,
                List.of(stonePool(), orePool(), preciousPool()));

        Random random = new Random(9999);
        ResolvedPool resolved = variable.resolve(RARE, random);

        List<String> allPossible = List.of("STONE", "COBBLESTONE", "IRON_ORE", "COAL_ORE", "DIAMOND_ORE");
        assertTrue(resolved.mergedValues().stream().allMatch(allPossible::contains));
        assertEquals(3, resolved.mergedValues().size(),
                "With ore(2 values) + precious(1 value) selected, merged should have 3 values");
    }

    @Test
    @DisplayName("Given all pools have zero weight for a rarity, when resolve is called, then returns empty resolved result")
    void emptyPoolsAllZeroWeight_returnsEmptyResolved() {
        Pool zeroCommon = new Pool("zero", 1.0, Map.of(COMMON, 0, RARE, 100), List.of("A"));
        PoolVariable variable = new PoolVariable("blocks", 1, 1, List.of(zeroCommon));

        ResolvedPool resolved = variable.resolve(COMMON, new Random(42));

        assertTrue(resolved.mergedValues().isEmpty());
        assertEquals(0.0, resolved.averageDifficulty());
    }

    @Test
    @DisplayName("Given same seed, when resolve is called multiple times, then produces deterministic results")
    void seededRandomProducesDeterministicResults() {
        PoolVariable variable = new PoolVariable("blocks", 1, 1,
                List.of(stonePool(), orePool(), preciousPool()));

        ResolvedPool first = variable.resolve(COMMON, new Random(777));
        ResolvedPool second = variable.resolve(COMMON, new Random(777));

        assertEquals(first.mergedValues(), second.mergedValues());
        assertEquals(first.averageDifficulty(), second.averageDifficulty());
    }

    @Test
    @DisplayName("Given minSelections < 1, when constructor is called, then throws IllegalArgumentException")
    void constructorRejectsMinSelectionsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () ->
                new PoolVariable("blocks", 0, 1, List.of(stonePool())));
        assertThrows(IllegalArgumentException.class, () ->
                new PoolVariable("blocks", -1, 1, List.of(stonePool())));
    }

    @Test
    @DisplayName("Given maxSelections < minSelections, when constructor is called, then throws IllegalArgumentException")
    void constructorRejectsMaxSelectionsLessThanMin() {
        assertThrows(IllegalArgumentException.class, () ->
                new PoolVariable("blocks", 2, 1, List.of(stonePool(), orePool())));
        assertThrows(IllegalArgumentException.class, () ->
                new PoolVariable("blocks", 3, 2, List.of(stonePool(), orePool(), preciousPool())));
    }

    private static int countDistinctPoolSources(List<String> merged) {
        if (merged.isEmpty()) return 0;
        int sources = 0;
        if (merged.stream().anyMatch(v -> v.equals("STONE") || v.equals("COBBLESTONE"))) sources++;
        if (merged.stream().anyMatch(v -> v.equals("IRON_ORE") || v.equals("COAL_ORE"))) sources++;
        if (merged.stream().anyMatch(v -> v.equals("DIAMOND_ORE"))) sources++;
        return sources;
    }
}
