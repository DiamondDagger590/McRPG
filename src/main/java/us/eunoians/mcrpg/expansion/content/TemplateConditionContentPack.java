package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;

/**
 * A content pack that provides {@link TemplateCondition}s for a given {@link ContentExpansion}.
 */
public final class TemplateConditionContentPack extends McRPGContentPack<TemplateCondition> {

    public TemplateConditionContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
