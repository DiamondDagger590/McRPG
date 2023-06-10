package us.eunoians.mcrpg.ability.attribute;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

/**
 * This type of {@link AbilityAttribute} only saves if {@link #shouldContentBeSaved(Object)}
 * returns {@code true}. This allows better optimization of database storage by not saving trivial data,
 * such as not needing to save an ability's tier if it isn't unlocked yet, since that tier would be 0.
 *
 * @param <T> The type of the value that should be stored in this attribute
 */
public abstract class OptionalAbilityAttribute<T> extends AbilityAttribute<T> {

    protected OptionalAbilityAttribute(@NotNull String databaseKeyName, @NotNull NamespacedKey namespacedKey) {
        super(databaseKeyName, namespacedKey);
    }

    protected OptionalAbilityAttribute(@NotNull String databaseKeyName, @NotNull NamespacedKey namespacedKey, @NotNull T content) {
        super(databaseKeyName, namespacedKey, content);
    }

    protected OptionalAbilityAttribute(@NotNull String databaseKeyName, @NotNull NamespacedKey namespacedKey, @NotNull T content, @NotNull NamespacedKey abilityType) {
        super(databaseKeyName, namespacedKey, content, abilityType);
    }

    /**
     * Checks to see if the provided content should be saved to the database
     *
     * @param content The content to check
     * @return {@code true} if the provided content should be saved to the database.
     */
    public abstract boolean shouldContentBeSaved(@NotNull T content);
}
