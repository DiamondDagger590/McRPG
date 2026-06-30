package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("DistributionCompletionService")
public class DistributionCompletionServiceTest extends McRPGBaseTest {

    private QuestRarityRegistry rarityRegistry;
    private RewardDistributionTypeRegistry typeRegistry;
    private RewardDistributionGranter granter;
    private QuestContributionAggregator contributionAggregator;
    private QuestRewardDistributionResolver distributionResolver;
    private DistributionCompletionService service;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        typeRegistry = mock(RewardDistributionTypeRegistry.class);
        granter = mock(RewardDistributionGranter.class);
        contributionAggregator = mock(QuestContributionAggregator.class);
        distributionResolver = mock(QuestRewardDistributionResolver.class);
        service = new DistributionCompletionService(
                rarityRegistry, typeRegistry, granter, contributionAggregator, distributionResolver);
    }

    @DisplayName("resolveAndGrant delegates to aggregator, resolver, and granter in order")
    @Test
    void resolveAndGrant_delegatesInOrder() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        Map<UUID, Long> contributions = Map.of(player1, 100L, player2, 50L);
        Set<UUID> groupMembers = Set.of(player1, player2);
        NamespacedKey rarityKey = new NamespacedKey("mcrpg", "rare");
        NamespacedKey questKey = new NamespacedKey("mcrpg", "test_quest");

        QuestInstance quest = mock(QuestInstance.class);
        when(quest.getBoardRarityKey()).thenReturn(Optional.of(rarityKey));
        when(quest.getQuestKey()).thenReturn(questKey);

        ContributionSnapshot snapshot = new ContributionSnapshot(contributions, 150, groupMembers, null);
        when(contributionAggregator.toSnapshot(contributions, groupMembers)).thenReturn(snapshot);

        QuestRewardType reward = mock(QuestRewardType.class);
        Map<UUID, List<QuestRewardType>> resolved = Map.of(player1, List.of(reward));
        when(distributionResolver.resolve(any(), eq(snapshot), eq(rarityKey), eq(rarityRegistry), eq(typeRegistry)))
                .thenReturn(resolved);

        RewardDistributionConfig config = new RewardDistributionConfig(List.of());

        service.resolveAndGrant(config, contributions, groupMembers, quest);

        verify(contributionAggregator).toSnapshot(contributions, groupMembers);
        verify(distributionResolver).resolve(config, snapshot, rarityKey, rarityRegistry, typeRegistry);
        verify(granter).grant(resolved, questKey);
    }

    @DisplayName("resolveAndGrant passes null rarity when quest has no board rarity")
    @Test
    void resolveAndGrant_noBoardRarity_passesNull() {
        UUID player = UUID.randomUUID();
        Map<UUID, Long> contributions = Map.of(player, 50L);
        Set<UUID> groupMembers = Set.of(player);
        NamespacedKey questKey = new NamespacedKey("mcrpg", "non_board_quest");

        QuestInstance quest = mock(QuestInstance.class);
        when(quest.getBoardRarityKey()).thenReturn(Optional.empty());
        when(quest.getQuestKey()).thenReturn(questKey);

        ContributionSnapshot snapshot = new ContributionSnapshot(contributions, 50, groupMembers, null);
        when(contributionAggregator.toSnapshot(contributions, groupMembers)).thenReturn(snapshot);

        Map<UUID, List<QuestRewardType>> resolved = Map.of();
        when(distributionResolver.resolve(any(), eq(snapshot), eq(null), eq(rarityRegistry), eq(typeRegistry)))
                .thenReturn(resolved);

        RewardDistributionConfig config = new RewardDistributionConfig(List.of());

        service.resolveAndGrant(config, contributions, groupMembers, quest);

        verify(distributionResolver).resolve(config, snapshot, null, rarityRegistry, typeRegistry);
        verify(granter).grant(resolved, questKey);
    }

    @DisplayName("resolveAndGrant with empty contributions still delegates through the pipeline")
    @Test
    void resolveAndGrant_emptyContributions_delegatesThrough() {
        Map<UUID, Long> contributions = Map.of();
        Set<UUID> groupMembers = Set.of();
        NamespacedKey questKey = new NamespacedKey("mcrpg", "empty_quest");

        QuestInstance quest = mock(QuestInstance.class);
        when(quest.getBoardRarityKey()).thenReturn(Optional.empty());
        when(quest.getQuestKey()).thenReturn(questKey);

        ContributionSnapshot snapshot = new ContributionSnapshot(contributions, 0, groupMembers, null);
        when(contributionAggregator.toSnapshot(contributions, groupMembers)).thenReturn(snapshot);

        Map<UUID, List<QuestRewardType>> resolved = Map.of();
        when(distributionResolver.resolve(any(), eq(snapshot), eq(null), eq(rarityRegistry), eq(typeRegistry)))
                .thenReturn(resolved);

        RewardDistributionConfig config = new RewardDistributionConfig(List.of());

        service.resolveAndGrant(config, contributions, groupMembers, quest);

        verify(contributionAggregator).toSnapshot(contributions, groupMembers);
        verify(granter).grant(resolved, questKey);
    }
}
