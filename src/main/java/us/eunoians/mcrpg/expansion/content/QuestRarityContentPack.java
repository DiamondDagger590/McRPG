package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;

/**
 * A content pack that provides {@link QuestRarity} instances for a given {@link ContentExpansion}.
 */
public final class QuestRarityContentPack extends McRPGContentPack<QuestRarity> {

    public QuestRarityContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
