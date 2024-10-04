package us.eunoians.mcrpg.expansion.content;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This interface represents an object that can be provided through a {@link McRPGContentPack}.
 * <p>
 * As all content provided in a pack will likely belong to a {@link us.eunoians.mcrpg.expansion.ContentExpansion},
 * this is a common class to be shared that provides a method to get the {@link NamespacedKey} that belongs to
 * the content expansion.
 * <p>
 * In order to support the functionality of things not being provided by a specific content expansion, the expansion key
 * is wrapped by an {@link Optional}, with an empty optional indicating that the content doesn't belong to an expansion.
 */
public interface McRPGContent {

    /**
     * Gets an {@link Optional} containing the {@link NamespacedKey} of the {@link us.eunoians.mcrpg.expansion.ContentExpansion}
     * that provides this content.
     *
     * @return An {@link Optional} containing the {@link NamespacedKey} of the {@link us.eunoians.mcrpg.expansion.ContentExpansion}
     * that provides this content or empty if there is no such expansion.
     */
    @NotNull
    Optional<NamespacedKey> getExpansionKey();
}
