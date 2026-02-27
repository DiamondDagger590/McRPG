package us.eunoians.mcrpg.quest.board.template.condition;

/**
 * Comparison operators for numeric variable checks.
 */
public enum ComparisonOperator {

    GREATER_THAN {
        @Override
        public boolean compare(double a, double b) {
            return a > b;
        }
    },
    LESS_THAN {
        @Override
        public boolean compare(double a, double b) {
            return a < b;
        }
    },
    GREATER_THAN_OR_EQUAL {
        @Override
        public boolean compare(double a, double b) {
            return a >= b;
        }
    },
    LESS_THAN_OR_EQUAL {
        @Override
        public boolean compare(double a, double b) {
            return a <= b;
        }
    };

    /**
     * Compares two double values according to this operator.
     *
     * @param a the left-hand operand
     * @param b the right-hand operand (threshold)
     * @return true if the comparison holds
     */
    public abstract boolean compare(double a, double b);
}
