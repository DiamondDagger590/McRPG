package us.eunoians.mcrpg.quest.board.template.condition;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Sealed interface for variable value checks used by {@link VariableCondition}.
 */
public sealed interface VariableCheck {

    /**
     * Tests whether the resolved variable value satisfies this check.
     *
     * @param resolvedValue the resolved variable value
     * @return true if the check passes
     */
    boolean test(@NotNull Object resolvedValue);

    /**
     * Checks whether a resolved POOL variable's merged value list contains
     * any of the specified values.
     */
    record ContainsAny(@NotNull List<String> values) implements VariableCheck {

        public ContainsAny {
            values = List.copyOf(values);
        }

        @Override
        public boolean test(@NotNull Object resolvedValue) {
            if (resolvedValue instanceof List<?> list) {
                return list.stream()
                        .map(Object::toString)
                        .anyMatch(values::contains);
            }
            return values.contains(resolvedValue.toString());
        }
    }

    /**
     * Checks whether a resolved RANGE variable's numeric value satisfies
     * a comparison against a threshold.
     */
    record NumericComparison(
            @NotNull ComparisonOperator operator,
            double threshold
    ) implements VariableCheck {

        @Override
        public boolean test(@NotNull Object resolvedValue) {
            if (resolvedValue instanceof Number n) {
                return operator.compare(n.doubleValue(), threshold);
            }
            return false;
        }
    }
}
