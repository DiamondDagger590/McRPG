package us.eunoians.mcrpg.util.pair;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable variant of {@link Pair} in which the left and right side of the pair are final and can not be
 * modified after creation.
 *
 * @param <L> The type of the left side of the pair
 * @param <R> The type of the right side of the pair
 */
public final class ImmutablePair<L, R> extends Pair<L, R> {

    ImmutablePair(@NotNull L left, @NotNull R right) {
        super(left, right);
    }

    /**
     * Creates a new {@link ImmutablePair} with the provided left and right objects.
     *
     * @param left  The object on the left side of the pair
     * @param right The object on the right side of the pair
     * @param <L>   The type of the object on the left side of the pair
     * @param <R>   The type of the object on the right side of the pair
     * @return A new {@link ImmutablePair} containing the provided left and right objects.
     */
    @NotNull
    public static <L, R> ImmutablePair<L, R> of(@NotNull L left, @NotNull R right) {
        return new ImmutablePair<>(left, right);
    }
}
