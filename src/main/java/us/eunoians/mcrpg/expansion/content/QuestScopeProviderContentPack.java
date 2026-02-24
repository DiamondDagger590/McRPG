package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;

/**
 * A content pack that provides {@link QuestScopeProvider}s for a given {@link ContentExpansion}.
 */
public final class QuestScopeProviderContentPack extends McRPGContentPack<QuestScopeProvider<?>> {

    public QuestScopeProviderContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
