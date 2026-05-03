package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Domain service that resolves and grants distribution rewards when a quest
 * reaches a terminal state. Extracts the distribution logic from the listener
 * layer so it can be tested independently and reused without static coupling.
 */
public class DistributionCompletionService {

    private final QuestRarityRegistry rarityRegistry;
    private final RewardDistributionTypeRegistry typeRegistry;
    private final RewardDistributionGranter granter;
    private final QuestContributionAggregator contributionAggregator;
    private final QuestRewardDistributionResolver distributionResolver;

    public DistributionCompletionService(@NotNull QuestRarityRegistry rarityRegistry,
                                         @NotNull RewardDistributionTypeRegistry typeRegistry,
                                         @NotNull RewardDistributionGranter granter,
                                         @NotNull QuestContributionAggregator contributionAggregator,
                                         @NotNull QuestRewardDistributionResolver distributionResolver) {
        this.rarityRegistry = rarityRegistry;
        this.typeRegistry = typeRegistry;
        this.granter = granter;
        this.contributionAggregator = contributionAggregator;
        this.distributionResolver = distributionResolver;
    }

    /**
     * Builds a contribution snapshot from the raw contribution map, resolves the
     * distribution rewards for each eligible player, and delegates granting to
     * {@link RewardDistributionGranter}.
     *
     * @param config        the reward distribution configuration for this quest
     * @param contributions map of player UUID to their contribution score
     * @param groupMembers  all players who were in scope when the quest completed
     * @param quest         the completed quest instance
     */
    public void resolveAndGrant(@NotNull RewardDistributionConfig config,
                                @NotNull Map<UUID, Long> contributions,
                                @NotNull Set<UUID> groupMembers,
                                @NotNull QuestInstance quest) {
        ContributionSnapshot snapshot = contributionAggregator.toSnapshot(contributions, groupMembers);
        NamespacedKey rarityKey = quest.getBoardRarityKey().orElse(null);
        Map<UUID, List<QuestRewardType>> resolved = distributionResolver.resolve(
                config, snapshot, rarityKey, rarityRegistry, typeRegistry);
        granter.grant(resolved, quest.getQuestKey());
    }
}
