package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;

/**
 * A content pack that provides {@link QuestTemplate} instances for a given
 * {@link ContentExpansion}. Templates registered through this content pack are
 * expansion-registered (not config-loaded) and survive reloads.
 */
public final class QuestTemplateContentPack extends McRPGContentPack<QuestTemplate> {

    public QuestTemplateContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
