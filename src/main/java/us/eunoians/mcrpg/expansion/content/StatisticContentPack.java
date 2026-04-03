package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;

/**
 * A content pack that provides {@link StatisticContent} definitions for a given
 * {@link ContentExpansion}.
 * <p>
 * Statistics included in this pack are registered to McCore's
 * {@link com.diamonddagger590.mccore.statistic.StatisticRegistry} during content expansion
 * processing. Third-party expansions can include their own custom statistics by adding
 * {@link StatisticContent} entries to their pack.
 */
public final class StatisticContentPack extends McRPGContentPack<StatisticContent> {

    /**
     * Creates a new {@link StatisticContentPack} for the given expansion.
     *
     * @param contentExpansion The {@link ContentExpansion} that owns this pack.
     */
    public StatisticContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
