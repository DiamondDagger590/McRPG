package us.eunoians.mcrpg.quest.board.distribution.builtin;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionRewardEntry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.PotBehavior;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RemainderStrategy;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link QuestAcceptorDistributionType}.
 */
class QuestAcceptorDistributionTypeTest {

    private static final NamespacedKey REWARD_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "leader_reward");

    private QuestAcceptorDistributionType distributionType;

    @BeforeEach
    void setUp() {
        distributionType = new QuestAcceptorDistributionType();
    }

    @Nested
    @DisplayName("resolve() — recipient set")
    class Resolve {

        @Test
        @DisplayName("scoped quest with acceptor UUID → resolves to singleton set")
        void scopedQuestResolvesToAcceptor() {
            UUID acceptorUUID = UUID.randomUUID();
            ContributionSnapshot snapshot = new ContributionSnapshot(
                    Map.of(acceptorUUID, 100L), 100, Set.of(acceptorUUID), acceptorUUID);
            DistributionTierConfig tier = new DistributionTierConfig("leader",
                    QuestAcceptorDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(new DistributionRewardEntry(mockReward(), PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null)),
                    Map.of(), null, null);

            Set<UUID> result = distributionType.resolve(snapshot, tier);

            assertEquals(Set.of(acceptorUUID), result);
        }

        @Test
        @DisplayName("snapshot with null acceptor UUID → resolves to empty set")
        void nullAcceptorResolvesToEmpty() {
            UUID participant = UUID.randomUUID();
            ContributionSnapshot snapshot = new ContributionSnapshot(
                    Map.of(participant, 50L), 50, Set.of(participant), null);
            DistributionTierConfig tier = new DistributionTierConfig("leader",
                    QuestAcceptorDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(new DistributionRewardEntry(mockReward(), PotBehavior.ALL, RemainderStrategy.DISCARD, 1, 1, null)),
                    Map.of(), null, null);

            Set<UUID> result = distributionType.resolve(snapshot, tier);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Integration — acceptor receives reward via resolver")
    class Integration {

        private RewardDistributionTypeRegistry typeRegistry;
        private QuestRarityRegistry rarityRegistry;

        @BeforeEach
        void setUp() {
            typeRegistry = new RewardDistributionTypeRegistry();
            typeRegistry.register(new QuestAcceptorDistributionType());
            typeRegistry.register(new ParticipatedDistributionType());
            rarityRegistry = new QuestRarityRegistry();
        }

        @Test
        @DisplayName("QUEST_ACCEPTOR tier rewards only the acceptor player")
        void acceptorReceivesLeaderReward() {
            UUID acceptor = UUID.randomUUID();
            UUID participant1 = UUID.randomUUID();
            UUID participant2 = UUID.randomUUID();

            QuestRewardType leaderReward = mockReward();
            DistributionTierConfig acceptorTier = new DistributionTierConfig("leader",
                    QuestAcceptorDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(leaderReward), Map.of(), null, null, true);

            var config = new RewardDistributionConfig(List.of(acceptorTier));
            var snapshot = new ContributionSnapshot(
                    Map.of(acceptor, 60L, participant1, 20L, participant2, 20L),
                    100, Set.of(acceptor, participant1, participant2), acceptor);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.containsKey(acceptor), "Acceptor should receive leader reward");
            assertFalse(result.containsKey(participant1), "Non-acceptor should not receive leader reward");
            assertFalse(result.containsKey(participant2), "Non-acceptor should not receive leader reward");
        }

        @Test
        @DisplayName("acceptor also qualifies for PARTICIPATED tier — rewards stack")
        void acceptorAlsoReceivesParticipationReward() {
            UUID acceptor = UUID.randomUUID();
            UUID participant = UUID.randomUUID();

            QuestRewardType leaderReward = mockReward();
            QuestRewardType participationReward = mockReward();

            DistributionTierConfig acceptorTier = new DistributionTierConfig("leader",
                    QuestAcceptorDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(leaderReward), Map.of(), null, null, true);
            DistributionTierConfig participatedTier = new DistributionTierConfig("participated",
                    ParticipatedDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(participationReward), Map.of(), null, null, true);

            var config = new RewardDistributionConfig(List.of(acceptorTier, participatedTier));
            var snapshot = new ContributionSnapshot(
                    Map.of(acceptor, 60L, participant, 40L),
                    100, Set.of(acceptor, participant), acceptor);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            // Acceptor receives both leader bonus and participation rewards
            assertTrue(result.containsKey(acceptor));
            assertEquals(2, result.get(acceptor).size(), "Acceptor should receive both leader and participation rewards");

            // Participant receives only participation reward
            assertTrue(result.containsKey(participant));
            assertEquals(1, result.get(participant).size());
        }

        @Test
        @DisplayName("QUEST_ACCEPTOR with null acceptor — tier produces no rewards")
        void nonScopedQuestYieldsNoRewards() {
            UUID participant = UUID.randomUUID();
            QuestRewardType leaderReward = mockReward();

            DistributionTierConfig acceptorTier = new DistributionTierConfig("leader",
                    QuestAcceptorDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(leaderReward), Map.of(), null, null, true);

            var config = new RewardDistributionConfig(List.of(acceptorTier));
            // null acceptorUUID = non-scoped quest
            var snapshot = new ContributionSnapshot(Map.of(participant, 100L), 100, Set.of(participant), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(config, snapshot, null, rarityRegistry, typeRegistry);

            assertTrue(result.isEmpty(), "QUEST_ACCEPTOR tier should produce no rewards when acceptor is null");
        }
    }

    private QuestRewardType mockReward() {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_KEY);
        when(reward.withAmountMultiplier(1.0)).thenReturn(reward);
        return reward;
    }
}
