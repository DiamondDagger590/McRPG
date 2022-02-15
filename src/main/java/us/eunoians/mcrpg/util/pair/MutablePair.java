package us.eunoians.mcrpg.util.pair;

import org.jetbrains.annotations.NotNull;

/**
 * A mutable variant of {@link Pair} in which the left and right side of the pair can be modified after
 * the object has been created
 *
 * @param <L> The type of the left side of the pair
 * @param <R> The type of the right side of the pair
 */
public final class MutablePair<L, R> extends Pair<L, R> {

    private MutablePair(@NotNull L left, @NotNull R right) {
        super(left, right);
    }

    /**
     * Creates a new {@link MutablePair} with the provided left and right objects.
     *
     * @param left  The object on the left side of the pair
     * @param right The object on the right side of the pair
     * @param <L>   The type of the object on the left side of the pair
     * @param <R>   The type of the object on the right side of the pair
     * @return A new {@link MutablePair} containing the provided left and right objects.
     */
    @NotNull
    public static <L, R> MutablePair<L, R> of(@NotNull L left, @NotNull R right) {
        return new MutablePair<>(left, right);
    }

    /**
     * Sets the object on the left side of this pair
     *
     * @param left The new object to go on the left side of this pair
     */
    public void setLeft(@NotNull L left) {
        this.left = left;
    }

    /**
     * Sets the object on the right side of this pair
     *
     * @param right The new object to go on the right side of this pair
     */
    public void setRight(@NotNull R right) {
        this.right = right;
    }

}
