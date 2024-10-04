package us.eunoians.mcrpg.expansion;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;

import java.util.Set;

/**
 * A content expansion is a grouping of {@link McRPGContentPack}s that come bundled together. The base example
 * is providing all the native content from McRPG through the {@link McRPGExpansion}.
 * <p>
 * This allows for third party plugins to create their own content expansions to easily group their own unique abilities and
 * skills while allowing for the ability to display the origin of content to players.
 */
public abstract class ContentExpansion {

    private final NamespacedKey expansionKey;

    public ContentExpansion(@NotNull NamespacedKey expansionKey) {
        this.expansionKey = expansionKey;
    }

    /**
     * Gets the {@link NamespacedKey} for the content expansion.
     *
     * @return The {@link NamespacedKey} for this content expansion.
     */
    @NotNull
    public NamespacedKey getExpansionKey() {
        return expansionKey;
    }

    /**
     * Gets a {@link Set} of {@link McRPGContentPack}s that are provided by this expansion.
     *
     * @return A {@link Set} of {@link McRPGContentPack}s that are provided by this expansion.
     */
    @NotNull
    public abstract Set<McRPGContentPack<? extends McRPGContent>> getExpansionContent();
}
