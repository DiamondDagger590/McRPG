package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.refresh.builtin.DailyRefreshType;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlotGenerationLogicTest extends McRPGBaseTest {

    private static final NamespacedKey SCOPE_KEY = new NamespacedKey("mcrpg", "test_scope");
    private static final NamespacedKey DAILY_KEY = DailyRefreshType.KEY;

    private BoardSlotCategory category(NamespacedKey key, int min, int max, double chancePerSlot, int priority) {
        return new BoardSlotCategory(
                key,
                BoardSlotCategory.Visibility.SHARED,
                DAILY_KEY,
                Duration.ofHours(24),
                Duration.ofHours(48),
                SCOPE_KEY,
                min,
                max,
                chancePerSlot,
                priority,
                null,
                null
        );
    }

    @DisplayName("computeSlotCounts: min slots are guaranteed per category")
    @Test
    void computeSlotCounts_minSlotsGuaranteedPerCategory() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "test_cat");
        BoardSlotCategory cat = category(catKey, 2, 5, 0.0, 1); // chance=0 means no extras
        List<BoardSlotCategory> categories = List.of(cat);

        Map<NamespacedKey, Integer> counts = SlotGenerationLogic.computeSlotCounts(
                categories, 1, new Random(42), k -> false);

        assertEquals(1, counts.size());
        assertEquals(2, counts.get(catKey));
    }

    @DisplayName("computeSlotCounts: categories on cooldown are skipped")
    @Test
    void computeSlotCounts_categoriesOnCooldownSkipped() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "cooldown_cat");
        BoardSlotCategory cat = category(catKey, 2, 5, 1.0, 1);
        List<BoardSlotCategory> categories = List.of(cat);

        Map<NamespacedKey, Integer> counts = SlotGenerationLogic.computeSlotCounts(
                categories, 1, new Random(42), k -> true); // all on cooldown

        assertTrue(counts.isEmpty());
    }

    @DisplayName("computeSlotCounts: backfill when below minimumTotalOfferings")
    @Test
    void computeSlotCounts_backfillWhenBelowMinimum() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "backfill_cat");
        BoardSlotCategory cat = category(catKey, 1, 5, 0.0, 1); // chance=0, so only 1 slot
        List<BoardSlotCategory> categories = List.of(cat);

        Map<NamespacedKey, Integer> counts = SlotGenerationLogic.computeSlotCounts(
                categories, 4, new Random(42), k -> false);

        assertEquals(4, counts.get(catKey).intValue()); // backfilled to reach minimum 4
    }

    @DisplayName("computeSlotCounts: chance=0 gives only min slots, chance=1.0 gives max slots")
    @Test
    void computeSlotCounts_chance0MinChance1Max() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "chance_cat");

        BoardSlotCategory catZero = category(catKey, 2, 5, 0.0, 1);
        Map<NamespacedKey, Integer> countsZero = SlotGenerationLogic.computeSlotCounts(
                List.of(catZero), 1, new Random(12345), k -> false);
        assertEquals(2, countsZero.get(catKey).intValue());

        BoardSlotCategory catOne = category(catKey, 2, 5, 1.0, 1);
        Map<NamespacedKey, Integer> countsOne = SlotGenerationLogic.computeSlotCounts(
                List.of(catOne), 1, new Random(12345), k -> false);
        assertEquals(5, countsOne.get(catKey).intValue());
    }

    @DisplayName("computeSlotCountForCategory: chance=0 gives exactly min slots")
    @Test
    void computeSlotCountForCategory_chanceZero_givesMin() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "single_cat");
        BoardSlotCategory cat = category(catKey, 2, 5, 0.0, 1);

        int count = SlotGenerationLogic.computeSlotCountForCategory(cat, new Random(42));
        assertEquals(2, count);
    }

    @DisplayName("computeSlotCountForCategory: chance=1.0 gives exactly max slots")
    @Test
    void computeSlotCountForCategory_chanceOne_givesMax() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "single_cat");
        BoardSlotCategory cat = category(catKey, 2, 5, 1.0, 1);

        int count = SlotGenerationLogic.computeSlotCountForCategory(cat, new Random(42));
        assertEquals(5, count);
    }

    @DisplayName("computeSlotCountForCategory: min == max always returns that value")
    @Test
    void computeSlotCountForCategory_minEqualsMax_returnsExact() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "fixed_cat");
        BoardSlotCategory cat = category(catKey, 3, 3, 0.5, 1);

        int count = SlotGenerationLogic.computeSlotCountForCategory(cat, new Random(42));
        assertEquals(3, count);
    }

    @DisplayName("computeSlotCountForCategory: deterministic with same seed")
    @Test
    void computeSlotCountForCategory_deterministicWithSameSeed() {
        NamespacedKey catKey = new NamespacedKey("mcrpg", "seed_cat");
        BoardSlotCategory cat = category(catKey, 1, 10, 0.5, 1);

        int count1 = SlotGenerationLogic.computeSlotCountForCategory(cat, new Random(999));
        int count2 = SlotGenerationLogic.computeSlotCountForCategory(cat, new Random(999));
        assertEquals(count1, count2);
    }

    @DisplayName("selectQuestForSlot: single eligible returns it")
    @Test
    void selectQuestForSlot_singleEligible_returnsIt() {
        NamespacedKey questKey = new NamespacedKey("mcrpg", "single_quest");
        Optional<NamespacedKey> result = SlotGenerationLogic.selectQuestForSlot(
                List.of(questKey), new Random());

        assertTrue(result.isPresent());
        assertEquals(questKey, result.get());
    }

    @DisplayName("selectQuestForSlot: empty list returns empty Optional")
    @Test
    void selectQuestForSlot_emptyList_returnsEmpty() {
        Optional<NamespacedKey> result = SlotGenerationLogic.selectQuestForSlot(
                List.of(), new Random());

        assertFalse(result.isPresent());
    }

    @DisplayName("selectQuestForSlot: uses Random for selection")
    @Test
    void selectQuestForSlot_usesRandomForSelection() {
        NamespacedKey a = new NamespacedKey("mcrpg", "a");
        NamespacedKey b = new NamespacedKey("mcrpg", "b");
        List<NamespacedKey> eligible = List.of(a, b);

        // Deterministic seed - same seed gives same result
        Optional<NamespacedKey> result1 = SlotGenerationLogic.selectQuestForSlot(eligible, new Random(999));
        Optional<NamespacedKey> result2 = SlotGenerationLogic.selectQuestForSlot(eligible, new Random(999));
        assertEquals(result1, result2);

        // Result is one of the eligible
        assertTrue(result1.isPresent());
        assertTrue(eligible.contains(result1.get()));
    }
}
