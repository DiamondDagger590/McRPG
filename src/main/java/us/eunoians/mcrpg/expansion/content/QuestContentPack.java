package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.Quest;

/**
 * A content pack that provides {@link Quest}s for a given {@link ContentExpansion}.
 */
public final class QuestContentPack extends McRPGContentPack<Quest> {

    public QuestContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
