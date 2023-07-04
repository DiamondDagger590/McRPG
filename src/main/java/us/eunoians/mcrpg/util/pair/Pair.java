package us.eunoians.mcrpg.util.pair;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An object containing two values that have no predefined relation to one another.
 * <p>
 * This object is mostly helpful for quickly coupling two objects together for a quick return
 * in a method.
 *
 * @param <L> the type of the "left" value
 * @param <R> the type of the "right" value
 */
public abstract class Pair<L, R> {

    protected L left;
    protected R right;

    Pair(@NotNull L left, @NotNull R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Gets the object on the left side of this pair, of type {@link L}
     *
     * @return The object on the left side of this pair, of type {@link L}
     */
    @NotNull
    public final L getLeft() {
        return left;
    }

    /**
     * Gets the object on the right side of this pair, of type {@link R}
     *
     * @return The object on the right side of this pair, of type {@link R}
     */
    @NotNull
    public final R getRight() {
        return right;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        else if (!(obj instanceof Pair)) {
            return false;
        }
        else {
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return Objects.equals(getLeft(), other.getLeft())
                   && Objects.equals(getRight(), other.getRight());
        }
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public final int hashCode() {
        int a = getLeft() == null ? 0 : getLeft().hashCode();
        int b = getRight() == null ? 0 : getRight().hashCode();
        return a ^ b;
    }

	/**
	 * {@inheritDoc}
	 */
    @Override
    public final String toString() {
        return getClass().getSimpleName() + "(" + getLeft() + ';' + getRight() + ')';
    }
}
