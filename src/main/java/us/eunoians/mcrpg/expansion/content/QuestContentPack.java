package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

/**
 * A content pack that provides {@link QuestDefinition}s for a given {@link ContentExpansion}.
 */
public final class QuestContentPack extends McRPGContentPack<QuestDefinition> {

    public QuestContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
