package us.eunoians.mcrpg.expansion.content;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.ContentExpansion;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionType;

/**
 * A content pack that provides {@link RewardDistributionType}s for a given {@link ContentExpansion}.
 */
public final class RewardDistributionTypeContentPack extends McRPGContentPack<RewardDistributionType> {

    public RewardDistributionTypeContentPack(@NotNull ContentExpansion contentExpansion) {
        super(contentExpansion);
    }
}
