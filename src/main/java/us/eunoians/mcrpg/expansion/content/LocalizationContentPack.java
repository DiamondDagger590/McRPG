package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalization;

/**
 * A content pack that provides {@link McRPGLocalization}s for a given {@link ContentExpansion}.
 */
public class LocalizationContentPack extends McRPGContentPack<McRPGLocalization> {

    public LocalizationContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
