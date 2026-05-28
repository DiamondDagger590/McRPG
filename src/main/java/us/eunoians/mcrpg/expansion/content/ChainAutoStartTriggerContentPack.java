package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTrigger;

/**
 * A content pack that provides {@link ChainAutoStartTrigger}s for a given {@link ContentExpansion}.
 */
public final class ChainAutoStartTriggerContentPack extends McRPGContentPack<ChainAutoStartTrigger> {

    public ChainAutoStartTriggerContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
