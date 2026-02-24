package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;

/**
 * A content pack that provides {@link QuestObjectiveType}s for a given {@link ContentExpansion}.
 */
public final class QuestObjectiveTypeContentPack extends McRPGContentPack<QuestObjectiveType> {

    public QuestObjectiveTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
