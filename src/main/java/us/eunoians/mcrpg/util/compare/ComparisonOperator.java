package us.eunoians.mcrpg.util.compare;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * A reusable string-symbol comparison operator. Supports the six standard relational operators
 * (>=, >, ==, <=, <, !=) parsed from their YAML-friendly symbol form.
 * <p>
 * {@link #compare(String, String)} performs a numeric comparison when both operands parse as
 * numbers (locale-independent, commas stripped), falling back to case-insensitive string
 * equality when at least one operand is non-numeric. Non-numeric values are only meaningful
 * for {@link #EQUAL} / {@link #NOT_EQUAL}; the ordering operators on non-numeric operands
 * return {@code false}.
 */
public enum ComparisonOperator {

    GREATER_THAN_OR_EQUAL(">="),
    GREATER_THAN(">"),
    EQUAL("=="),
    LESS_THAN_OR_EQUAL("<="),
    LESS_THAN("<"),
    NOT_EQUAL("!=");

    private final String symbol;

    ComparisonOperator(@NotNull String symbol) {
        this.symbol = symbol;
    }

    /**
     * The textual form used in YAML config and scripting surfaces.
     *
     * @return the operator symbol (e.g. {@code ">="})
     */
    @NotNull
    public String getSymbol() {
        return symbol;
    }

    /**
     * Parses an operator from its textual form.
     *
     * @param symbol the operator symbol
     * @return the matching operator, or empty if no operator uses that symbol
     */
    @NotNull
    public static Optional<ComparisonOperator> fromSymbol(@NotNull String symbol) {
        for (ComparisonOperator op : values()) {
            if (op.symbol.equals(symbol)) {
                return Optional.of(op);
            }
        }
        return Optional.empty();
    }

    /**
     * Compares two operands using this operator. Numeric when both parse as doubles; otherwise
     * case-insensitive string equality (ordering operators on non-numeric operands return
     * {@code false}).
     *
     * @param left  the left-hand operand
     * @param right the right-hand operand
     * @return {@code true} if the operands satisfy this operator
     */
    public boolean compare(@NotNull String left, @NotNull String right) {
        Optional<Double> leftNum = tryParseDouble(left);
        Optional<Double> rightNum = tryParseDouble(right);
        if (leftNum.isPresent() && rightNum.isPresent()) {
            return compareNumeric(leftNum.get(), rightNum.get());
        }
        return switch (this) {
            case EQUAL -> left.equalsIgnoreCase(right);
            case NOT_EQUAL -> !left.equalsIgnoreCase(right);
            default -> false;
        };
    }

    /**
     * Numeric comparison only — for callers that already have parsed values.
     *
     * @param left  the left-hand value
     * @param right the right-hand value
     * @return {@code true} if the values satisfy this operator
     */
    public boolean compareNumeric(double left, double right) {
        return switch (this) {
            case GREATER_THAN_OR_EQUAL -> left >= right;
            case GREATER_THAN -> left > right;
            case EQUAL -> left == right;
            case LESS_THAN_OR_EQUAL -> left <= right;
            case LESS_THAN -> left < right;
            case NOT_EQUAL -> left != right;
        };
    }

    /**
     * Attempts to parse a string as a double. Locale-independent — commas are stripped so
     * machine-formatted PAPI values like {@code "1,234.5"} parse cleanly.
     *
     * @param raw the raw value
     * @return the parsed value, or empty if not numeric
     */
    @NotNull
    private static Optional<Double> tryParseDouble(@NotNull String raw) {
        try {
            return Optional.of(Double.parseDouble(raw.replace(",", "").trim().toLowerCase(Locale.ROOT)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
