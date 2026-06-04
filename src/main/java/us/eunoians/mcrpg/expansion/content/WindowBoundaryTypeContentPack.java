package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundaryType;

/**
 * A content pack that provides {@link WindowBoundaryType}s for a given {@link ContentExpansion}.
 * <p>
 * Native boundary types ({@code mcrpg:fixed}, {@code mcrpg:recurring}) are registered during
 * bootstrap. This pack exists as an extension point so that third-party plugins can contribute
 * custom boundary types programmatically (e.g., cron-based or external-calendar-driven boundaries).
 */
public final class WindowBoundaryTypeContentPack extends McRPGContentPack<WindowBoundaryType> {

    /**
     * Constructs a new content pack owned by the given expansion.
     *
     * @param contentExpansion the expansion that owns this content pack
     */
    public WindowBoundaryTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
