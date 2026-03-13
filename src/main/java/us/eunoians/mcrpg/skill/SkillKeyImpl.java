package us.eunoians.mcrpg.skill;

import org.jetbrains.annotations.NotNull;

/**
 * The implementation of a {@link SkillKey}. To create
 * instances of a key, users should call {@link #create(Class)}.
 *
 * @param skill The {@link Class} of the {@link Skill} being represented by this key.
 * @param <S>   The {@link Skill} class stored in this key.
 */
public record SkillKeyImpl<S extends Skill>(@NotNull Class<S> skill) implements SkillKey<S> {

    /**
     * Creates a new instance of a key using the provided {@link Class}.
     *
     * @param clazz The class to store in the key.
     * @param <S>   The type of {@link Skill} to store in this key.
     * @return A {@link SkillKey} representing a {@link Skill}.
     */
    @NotNull
    public static <S extends Skill> SkillKey<S> create(@NotNull Class<S> clazz) {
        return new SkillKeyImpl<>(clazz);
    }

    @NotNull
    @Override
    public Class<S> skillClass() {
        return skill;
    }
}
