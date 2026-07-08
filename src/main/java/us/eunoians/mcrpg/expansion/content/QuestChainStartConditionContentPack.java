package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.chain.condition.QuestChainStartConditionType;

/**
 * A content pack that provides {@link QuestChainStartConditionType}s for a given
 * {@link ContentExpansion}.
 * <p>
 * Start condition types are registered in the
 * {@link us.eunoians.mcrpg.quest.chain.condition.QuestChainStartConditionTypeRegistry} and
 * evaluated by {@link us.eunoians.mcrpg.quest.chain.QuestChainManager} before a chain step
 * is started. Third-party plugins use this pack to contribute custom condition types that
 * gate chain starts on arbitrary criteria (e.g., permission checks, economy balance,
 * time-of-day constraints).
 */
public final class QuestChainStartConditionContentPack extends McRPGContentPack<QuestChainStartConditionType> {

    /**
     * Constructs a new content pack owned by the given expansion.
     *
     * @param contentExpansion the expansion that owns this content pack
     */
    public QuestChainStartConditionContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
