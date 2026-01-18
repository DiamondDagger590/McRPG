package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation of an {@link AbilityKey}. To create
 * instances of a key, users should call {@link #create(Class)}.
 *
 * @param ability The {@link Class} of the {@link Ability} being represented by this key.
 * @param <A>     The {@link Ability} class stored in this key.
 */
public record AbilityKeyImpl<A extends Ability>(@NotNull Class<A> ability) implements AbilityKey<A> {

    /**
     * Creates a new instance of a key using the provided {@link Class}.
     *
     * @param clazz The class to store in the key.
     * @param <A>   The type of {@link Ability} to store in this key.
     * @return An {@link AbilityKey} representing an {@link Ability}.
     */
    @NotNull
    public static <A extends Ability> AbilityKey<A> create(@NotNull Class<A> clazz) {
        return new AbilityKeyImpl<>(clazz);
    }

    @NotNull
    @Override
    public Class<A> abilityClass() {
        return ability;
    }
}
