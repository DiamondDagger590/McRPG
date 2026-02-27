package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.ResolvedVariableContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TemplateConditionTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey UNCOMMON = NamespacedKey.fromString("mcrpg:uncommon");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey LEGENDARY = NamespacedKey.fromString("mcrpg:legendary");

    private QuestRarityRegistry rarityRegistry;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        QuestRarity commonRarity = mock(QuestRarity.class);
        QuestRarity uncommonRarity = mock(QuestRarity.class);
        QuestRarity rareRarity = mock(QuestRarity.class);
        QuestRarity legendaryRarity = mock(QuestRarity.class);

        when(commonRarity.getWeight()).thenReturn(100);
        when(uncommonRarity.getWeight()).thenReturn(50);
        when(rareRarity.getWeight()).thenReturn(20);
        when(legendaryRarity.getWeight()).thenReturn(5);

        when(rarityRegistry.get(COMMON)).thenReturn(Optional.of(commonRarity));
        when(rarityRegistry.get(UNCOMMON)).thenReturn(Optional.of(uncommonRarity));
        when(rarityRegistry.get(RARE)).thenReturn(Optional.of(rareRarity));
        when(rarityRegistry.get(LEGENDARY)).thenReturn(Optional.of(legendaryRarity));
    }

    @Nested
    @DisplayName("RarityCondition")
    class RarityConditionTests {

        @Test
        @DisplayName("rolled rarity meets minimum threshold")
        void rarityAtOrAboveThreshold() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(RARE, rarityRegistry, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("rolled rarity exceeds minimum threshold")
        void rarityAboveThreshold() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(LEGENDARY, rarityRegistry, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("rolled rarity below threshold")
        void rarityBelowThreshold() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(COMMON, rarityRegistry, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("null rolled rarity returns true (pass-through when context unavailable)")
        void nullRarityReturnsTrue() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(null, rarityRegistry, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("null rarity registry returns true (pass-through when context unavailable)")
        void nullRegistryReturnsTrue() {
            RarityCondition condition = new RarityCondition(RARE);
            ConditionContext ctx = new ConditionContext(RARE, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("ChanceCondition")
    class ChanceConditionTests {

        @Test
        @DisplayName("probability 1.0 always true")
        void alwaysTrue() {
            ChanceCondition condition = new ChanceCondition(1.0);
            ConditionContext ctx = new ConditionContext(null, null, new Random(42), null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("probability 0.0 always false")
        void alwaysFalse() {
            ChanceCondition condition = new ChanceCondition(0.0);
            ConditionContext ctx = new ConditionContext(null, null, new Random(42), null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("seeded random produces deterministic result")
        void deterministicWithSeed() {
            ChanceCondition condition = new ChanceCondition(0.5);
            boolean result1 = condition.evaluate(
                    new ConditionContext(null, null, new Random(12345), null, null, null));
            boolean result2 = condition.evaluate(
                    new ConditionContext(null, null, new Random(12345), null, null, null));
            assertEquals(result1, result2);
        }

        @Test
        @DisplayName("null random returns true (safe default for non-generation contexts)")
        void nullRandomReturnsTrue() {
            ChanceCondition condition = new ChanceCondition(0.5);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("VariableCondition")
    class VariableConditionTests {

        private ResolvedVariableContext varsWithBlocks;
        private ResolvedVariableContext varsWithCount;

        @BeforeEach
        void setUpVars() {
            varsWithBlocks = new ResolvedVariableContext(
                    Map.of("target_blocks", List.of("DIAMOND_ORE", "GOLD_ORE", "IRON_ORE"),
                           "block_count", 60L),
                    1.0, 1.0, 1.0);
            varsWithCount = new ResolvedVariableContext(
                    Map.of("block_count", 50L), 1.0, 1.0, 1.0);
        }

        @Test
        @DisplayName("ContainsAny with matching value returns true")
        void containsAnyMatches() {
            VariableCondition condition = new VariableCondition("target_blocks",
                    new VariableCheck.ContainsAny(List.of("DIAMOND_ORE")));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithBlocks, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("ContainsAny with no matching value returns false")
        void containsAnyNoMatch() {
            VariableCondition condition = new VariableCondition("target_blocks",
                    new VariableCheck.ContainsAny(List.of("EMERALD_ORE")));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithBlocks, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("NumericComparison GREATER_THAN with value above threshold")
        void greaterThanTrue() {
            VariableCondition condition = new VariableCondition("block_count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 50));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithBlocks, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("NumericComparison GREATER_THAN with value equal to threshold")
        void greaterThanEqualFalse() {
            VariableCondition condition = new VariableCondition("block_count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 50));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithCount, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("NumericComparison GREATER_THAN_OR_EQUAL with value equal to threshold")
        void greaterThanOrEqualTrue() {
            VariableCondition condition = new VariableCondition("block_count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN_OR_EQUAL, 50));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithCount, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("NumericComparison LESS_THAN with value below threshold")
        void lessThanTrue() {
            VariableCondition condition = new VariableCondition("block_count",
                    new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN, 100));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithCount, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("unresolved variable returns false")
        void unresolvedVariableReturnsFalse() {
            VariableCondition condition = new VariableCondition("nonexistent_var",
                    new VariableCheck.ContainsAny(List.of("X")));
            ConditionContext ctx = new ConditionContext(null, null, null, varsWithBlocks, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("null resolved variables returns true (pass-through when context unavailable)")
        void nullVarsReturnsTrue() {
            VariableCondition condition = new VariableCondition("block_count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 10));
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("CompoundCondition")
    class CompoundConditionTests {

        private TemplateCondition alwaysTrue() {
            TemplateCondition mock = mock(TemplateCondition.class);
            when(mock.evaluate(any())).thenReturn(true);
            return mock;
        }

        private TemplateCondition alwaysFalse() {
            TemplateCondition mock = mock(TemplateCondition.class);
            when(mock.evaluate(any())).thenReturn(false);
            return mock;
        }

        @Test
        @DisplayName("ALL with both true returns true")
        void allBothTrue() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue(), "b", alwaysTrue()),
                    CompoundCondition.LogicMode.ALL);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("ALL with one false returns false")
        void allOneFalse() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue(), "b", alwaysFalse()),
                    CompoundCondition.LogicMode.ALL);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("ANY with one true returns true")
        void anyOneTrue() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue(), "b", alwaysFalse()),
                    CompoundCondition.LogicMode.ANY);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("ANY with both false returns false")
        void anyBothFalse() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysFalse(), "b", alwaysFalse()),
                    CompoundCondition.LogicMode.ANY);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("nested compound conditions evaluate correctly")
        void nestedCompound() {
            CompoundCondition inner = new CompoundCondition(
                    Map.of("t", alwaysTrue(), "f", alwaysFalse()),
                    CompoundCondition.LogicMode.ANY);
            CompoundCondition outer = new CompoundCondition(
                    Map.of("inner", inner, "other", alwaysTrue()),
                    CompoundCondition.LogicMode.ALL);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, null);
            assertTrue(outer.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("CompletionPrerequisiteCondition")
    class CompletionPrerequisiteConditionTests {

        @Test
        @DisplayName("player meets completion threshold")
        void meetsThreshold() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(10);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("player does not meet completion threshold")
        void doesNotMeetThreshold() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(3);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("with category filter")
        void categoryFilter() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            NamespacedKey category = NamespacedKey.fromString("mcrpg:personal_daily");
            when(history.countCompletedQuests(playerUUID, category, null)).thenReturn(8);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, category, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("null player UUID returns false")
        void nullPlayerReturnsFalse() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, null, history);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("null completion history returns false")
        void nullHistoryReturnsFalse() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, UUID.randomUUID(), null);
            assertFalse(condition.evaluate(ctx));
        }
    }
}
