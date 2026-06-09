package us.eunoians.mcrpg.quest.board.template.condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("VariableCheck")
class VariableCheckTest {

    @Nested
    @DisplayName("ContainsAny")
    class ContainsAnyTests {

        @Test
        @DisplayName("matches when list contains a target value")
        void test_returnsTrue_whenListContainsMatch() {
            var check = new VariableCheck.ContainsAny(List.of("DIAMOND_ORE", "GOLD_ORE"));
            assertTrue(check.test(List.of("IRON_ORE", "DIAMOND_ORE", "STONE")));
        }

        @Test
        @DisplayName("no match when list has no overlap")
        void test_returnsFalse_whenListHasNoOverlap() {
            var check = new VariableCheck.ContainsAny(List.of("DIAMOND_ORE"));
            assertFalse(check.test(List.of("IRON_ORE", "STONE")));
        }

        @Test
        @DisplayName("matches scalar value via toString")
        void test_returnsTrue_whenScalarMatches() {
            var check = new VariableCheck.ContainsAny(List.of("hello", "world"));
            assertTrue(check.test("world"));
        }

        @Test
        @DisplayName("no match for scalar when value absent")
        void test_returnsFalse_whenScalarDoesNotMatch() {
            var check = new VariableCheck.ContainsAny(List.of("hello"));
            assertFalse(check.test("goodbye"));
        }

        @Test
        @DisplayName("scalar numeric converts via toString")
        void test_matchesNumericScalar_viaToString() {
            var check = new VariableCheck.ContainsAny(List.of("42"));
            assertTrue(check.test(42));
        }

        @Test
        @DisplayName("empty target list never matches")
        void test_returnsFalse_whenTargetListEmpty() {
            var check = new VariableCheck.ContainsAny(List.of());
            assertFalse(check.test(List.of("anything")));
        }

        @Test
        @DisplayName("empty resolved list never matches")
        void test_returnsFalse_whenResolvedListEmpty() {
            var check = new VariableCheck.ContainsAny(List.of("DIAMOND_ORE"));
            assertFalse(check.test(List.of()));
        }

        @Test
        @DisplayName("values list is defensively copied")
        void values_areDefensivelyCopied() {
            var mutable = new ArrayList<>(List.of("A", "B"));
            var check = new VariableCheck.ContainsAny(mutable);
            mutable.add("C");
            assertEquals(2, check.values().size());
        }

        @Test
        @DisplayName("values list is unmodifiable")
        void values_areUnmodifiable() {
            var check = new VariableCheck.ContainsAny(List.of("A"));
            assertThrows(UnsupportedOperationException.class, () -> check.values().add("B"));
        }
    }

    @Nested
    @DisplayName("NumericComparison")
    class NumericComparisonTests {

        @Test
        @DisplayName("passes when Number satisfies comparison")
        void test_returnsTrue_whenNumberSatisfiesComparison() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 10.0);
            assertTrue(check.test(15));
        }

        @Test
        @DisplayName("fails when Number does not satisfy comparison")
        void test_returnsFalse_whenNumberFailsComparison() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 10.0);
            assertFalse(check.test(5));
        }

        @Test
        @DisplayName("works with Long value")
        void test_handlesLong() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN_OR_EQUAL, 100.0);
            assertTrue(check.test(50L));
        }

        @Test
        @DisplayName("works with Double value")
        void test_handlesDouble() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN_OR_EQUAL, 2.5);
            assertTrue(check.test(2.5));
        }

        @Test
        @DisplayName("returns false for non-Number value")
        void test_returnsFalse_whenValueNotNumber() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 0.0);
            assertFalse(check.test("not a number"));
        }

        @Test
        @DisplayName("returns false for list value")
        void test_returnsFalse_whenValueIsList() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN, 0.0);
            assertFalse(check.test(List.of(1, 2, 3)));
        }

        @Test
        @DisplayName("operator accessor returns stored operator")
        void operator_returnsStoredValue() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN, 5.0);
            assertEquals(ComparisonOperator.LESS_THAN, check.operator());
        }

        @Test
        @DisplayName("threshold accessor returns stored value")
        void threshold_returnsStoredValue() {
            var check = new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN, 42.5);
            assertEquals(42.5, check.threshold());
        }
    }
}
