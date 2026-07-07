package us.eunoians.mcrpg.quest.board.template.condition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ComparisonOperator")
class ComparisonOperatorTest {

    @Nested
    @DisplayName("GREATER_THAN")
    class GreaterThan {

        @Test
        @DisplayName("returns true when a > b")
        void compare_returnsTrue_whenAboveThreshold() {
            assertTrue(ComparisonOperator.GREATER_THAN.compare(10.0, 5.0));
        }

        @Test
        @DisplayName("returns false when a == b")
        void compare_returnsFalse_whenEqual() {
            assertFalse(ComparisonOperator.GREATER_THAN.compare(5.0, 5.0));
        }

        @Test
        @DisplayName("returns false when a < b")
        void compare_returnsFalse_whenBelowThreshold() {
            assertFalse(ComparisonOperator.GREATER_THAN.compare(3.0, 5.0));
        }
    }

    @Nested
    @DisplayName("LESS_THAN")
    class LessThan {

        @Test
        @DisplayName("returns true when a < b")
        void compare_returnsTrue_whenBelowThreshold() {
            assertTrue(ComparisonOperator.LESS_THAN.compare(3.0, 5.0));
        }

        @Test
        @DisplayName("returns false when a == b")
        void compare_returnsFalse_whenEqual() {
            assertFalse(ComparisonOperator.LESS_THAN.compare(5.0, 5.0));
        }

        @Test
        @DisplayName("returns false when a > b")
        void compare_returnsFalse_whenAboveThreshold() {
            assertFalse(ComparisonOperator.LESS_THAN.compare(10.0, 5.0));
        }
    }

    @Nested
    @DisplayName("GREATER_THAN_OR_EQUAL")
    class GreaterThanOrEqual {

        @Test
        @DisplayName("returns true when a > b")
        void compare_returnsTrue_whenAboveThreshold() {
            assertTrue(ComparisonOperator.GREATER_THAN_OR_EQUAL.compare(10.0, 5.0));
        }

        @Test
        @DisplayName("returns true when a == b")
        void compare_returnsTrue_whenEqual() {
            assertTrue(ComparisonOperator.GREATER_THAN_OR_EQUAL.compare(5.0, 5.0));
        }

        @Test
        @DisplayName("returns false when a < b")
        void compare_returnsFalse_whenBelowThreshold() {
            assertFalse(ComparisonOperator.GREATER_THAN_OR_EQUAL.compare(3.0, 5.0));
        }
    }

    @Nested
    @DisplayName("LESS_THAN_OR_EQUAL")
    class LessThanOrEqual {

        @Test
        @DisplayName("returns true when a < b")
        void compare_returnsTrue_whenBelowThreshold() {
            assertTrue(ComparisonOperator.LESS_THAN_OR_EQUAL.compare(3.0, 5.0));
        }

        @Test
        @DisplayName("returns true when a == b")
        void compare_returnsTrue_whenEqual() {
            assertTrue(ComparisonOperator.LESS_THAN_OR_EQUAL.compare(5.0, 5.0));
        }

        @Test
        @DisplayName("returns false when a > b")
        void compare_returnsFalse_whenAboveThreshold() {
            assertFalse(ComparisonOperator.LESS_THAN_OR_EQUAL.compare(10.0, 5.0));
        }
    }

    @ParameterizedTest
    @EnumSource(ComparisonOperator.class)
    @DisplayName("negative values compare correctly")
    void compare_handlesNegativeValues(ComparisonOperator operator) {
        boolean result = operator.compare(-10.0, -5.0);
        switch (operator) {
            case GREATER_THAN, GREATER_THAN_OR_EQUAL -> assertFalse(result);
            case LESS_THAN, LESS_THAN_OR_EQUAL -> assertTrue(result);
        }
    }

    @ParameterizedTest
    @EnumSource(ComparisonOperator.class)
    @DisplayName("zero compared to zero")
    void compare_zeroAgainstZero(ComparisonOperator operator) {
        boolean result = operator.compare(0.0, 0.0);
        switch (operator) {
            case GREATER_THAN, LESS_THAN -> assertFalse(result);
            case GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL -> assertTrue(result);
        }
    }
}
