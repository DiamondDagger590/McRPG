package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;

/**
 * A content pack that provides {@link QuestChainDefinition}s for a given {@link ContentExpansion}.
 * <p>
 * Native chain definitions are loaded from YAML via {@link us.eunoians.mcrpg.configuration.QuestChainConfigLoader}
 * rather than through this pack. The pack exists as an extension point so that third-party plugins
 * can contribute chain definitions programmatically.
 */
public final class QuestChainContentPack extends McRPGContentPack<QuestChainDefinition> {

    public QuestChainContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
