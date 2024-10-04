package us.eunoians.mcrpg.expansion.content;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;

import java.util.HashSet;
import java.util.Set;

/**
 * A content pack provides a collection of {@link McRPGContent} for a {@link ContentExpansion}.
 *
 * @param <T> The type of content that is provided by this pack.
 */
public abstract class McRPGContentPack<T extends McRPGContent> {

    private final ContentExpansion contentExpansion;
    private final Set<T> content;

    public McRPGContentPack(@NotNull ContentExpansion contentExpansion) {
        this.contentExpansion = contentExpansion;
        this.content = new HashSet<>();
    }

    /**
     * Gets an {@link ImmutableSet} of all {@link McRPGContent} that is being provided by this
     * content pack.
     *
     * @return An {@link ImmutableSet} of all {@link McRPGContent} that is being provided by this
     * content pack.
     */
    @NotNull
    public Set<T> getContent() {
        return ImmutableSet.copyOf(content);
    }

    /**
     * Adds the provided {@link McRPGContent} to the content pack.
     *
     * @param content The {@link McRPGContent} to add.
     */
    public void addContent(@NotNull T content) {
        this.content.add(content);
    }

    /**
     * Gets the {@link ContentExpansion} that owns this content pack.
     *
     * @return The {@link ContentExpansion} that owns this content pack.
     */
    @NotNull
    public ContentExpansion getContentExpansion() {
        return contentExpansion;
    }
}
