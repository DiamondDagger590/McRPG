package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.template.ResolvedVariableContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("VariableCondition")
class VariableConditionTest {

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("no-arg prototype has empty variable name")
        void noArgConstructor_hasEmptyVariableName() {
            VariableCondition prototype = new VariableCondition();
            assertEquals("", prototype.getVariableName());
        }

        @Test
        @DisplayName("no-arg prototype has ContainsAny check")
        void noArgConstructor_hasContainsAnyCheck() {
            VariableCondition prototype = new VariableCondition();
            assertInstanceOf(VariableCheck.ContainsAny.class, prototype.getCheck());
        }
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getKey returns mcrpg:variable_check")
        void getKey_returnsMcrpgVariableCheck() {
            VariableCondition condition = new VariableCondition("test",
                    new VariableCheck.ContainsAny(List.of()));
            assertEquals(NamespacedKey.fromString("mcrpg:variable_check"), condition.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            VariableCondition condition = new VariableCondition("test",
                    new VariableCheck.ContainsAny(List.of()));
            Optional<NamespacedKey> key = condition.getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getVariableName returns configured name")
        void getVariableName_returnsConfiguredName() {
            VariableCondition condition = new VariableCondition("target_blocks",
                    new VariableCheck.ContainsAny(List.of()));
            assertEquals("target_blocks", condition.getVariableName());
        }

        @Test
        @DisplayName("getCheck returns configured check")
        void getCheck_returnsConfiguredCheck() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 5.0);
            VariableCondition condition = new VariableCondition("count", check);
            assertEquals(check, condition.getCheck());
        }
    }

    @Nested
    @DisplayName("evaluate edge cases")
    class EvaluateEdgeCases {

        @Test
        @DisplayName("ContainsAny matches partial overlap in list variable")
        void evaluate_containsAny_partialListOverlap() {
            VariableCondition condition = new VariableCondition("blocks",
                    new VariableCheck.ContainsAny(List.of("DIAMOND_ORE", "EMERALD_ORE")));
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("blocks", List.of("STONE", "DIAMOND_ORE")), 1.0, 1.0, 1.0);
            ConditionContext ctx = new ConditionContext(null, null, null, vars, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("NumericComparison LESS_THAN_OR_EQUAL boundary passes")
        void evaluate_numericComparison_lessThanOrEqual_boundary() {
            VariableCondition condition = new VariableCondition("count",
                    new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN_OR_EQUAL, 50.0));
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("count", 50L), 1.0, 1.0, 1.0);
            ConditionContext ctx = new ConditionContext(null, null, null, vars, null, null);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("non-Number variable with numeric check returns false")
        void evaluate_nonNumberVariable_withNumericCheck_returnsFalse() {
            VariableCondition condition = new VariableCondition("name",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 0.0));
            ResolvedVariableContext vars = new ResolvedVariableContext(
                    Map.of("name", "text"), 1.0, 1.0, 1.0);
            ConditionContext ctx = new ConditionContext(null, null, null, vars, null, null);
            assertFalse(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("ContainsAny serializes with name and contains-any keys")
        void serializeConfig_containsAny() {
            VariableCondition condition = new VariableCondition("blocks",
                    new VariableCheck.ContainsAny(List.of("A", "B")));
            Map<String, Object> config = condition.serializeConfig();
            assertEquals("blocks", config.get("name"));
            assertEquals(List.of("A", "B"), config.get("contains-any"));
            assertEquals(2, config.size());
        }

        @Test
        @DisplayName("GREATER_THAN serializes as greater-than key")
        void serializeConfig_greaterThan() {
            VariableCondition condition = new VariableCondition("count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 10.0));
            Map<String, Object> config = condition.serializeConfig();
            assertEquals("count", config.get("name"));
            assertEquals(10.0, config.get("greater-than"));
        }

        @Test
        @DisplayName("LESS_THAN serializes as less-than key")
        void serializeConfig_lessThan() {
            VariableCondition condition = new VariableCondition("count",
                    new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN, 5.0));
            Map<String, Object> config = condition.serializeConfig();
            assertEquals(5.0, config.get("less-than"));
        }

        @Test
        @DisplayName("GREATER_THAN_OR_EQUAL serializes as at-least key")
        void serializeConfig_atLeast() {
            VariableCondition condition = new VariableCondition("count",
                    new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN_OR_EQUAL, 3.0));
            Map<String, Object> config = condition.serializeConfig();
            assertEquals(3.0, config.get("at-least"));
        }

        @Test
        @DisplayName("LESS_THAN_OR_EQUAL serializes as at-most key")
        void serializeConfig_atMost() {
            VariableCondition condition = new VariableCondition("count",
                    new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN_OR_EQUAL, 100.0));
            Map<String, Object> config = condition.serializeConfig();
            assertEquals(100.0, config.get("at-most"));
        }

        @ParameterizedTest
        @EnumSource(ComparisonOperator.class)
        @DisplayName("all operators produce a map with exactly 2 entries")
        void serializeConfig_numericComparison_hasTwoEntries(ComparisonOperator operator) {
            VariableCondition condition = new VariableCondition("x",
                    new VariableCheck.NumericComparison(operator, 1.0));
            Map<String, Object> config = condition.serializeConfig();
            assertEquals(2, config.size());
            assertTrue(config.containsKey("name"));
        }
    }
}
