package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

/**
 * A content pack that provides {@link QuestRewardType}s for a given {@link ContentExpansion}.
 */
public final class QuestRewardTypeContentPack extends McRPGContentPack<QuestRewardType> {

    public QuestRewardTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
