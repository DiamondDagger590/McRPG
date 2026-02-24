package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.source.QuestSource;

/**
 * A content pack that provides {@link QuestSource}s for a given {@link ContentExpansion}.
 */
public final class QuestSourceContentPack extends McRPGContentPack<QuestSource> {

    public QuestSourceContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
