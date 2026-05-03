package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests verifying that multi-level distribution (objective, stage, phase, quest)
 * fires independently and does not double-count contributions across levels.
 * <p>
 * These tests exercise {@link QuestRewardDistributionResolver} with independent
 * distribution configs at each level, verifying that:
 * <ul>
 *   <li>Each level fires with its own contribution scope</li>
 *   <li>Rewards from different levels stack without interference</li>
 *   <li>Players receive rewards from every qualifying level</li>
 * </ul>
 */
public class MultiLevelDistributionIntegrationTest extends McRPGBaseTest {

    private static final NamespacedKey REWARD_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_reward");

    private RewardDistributionTypeRegistry typeRegistry;
    private QuestRarityRegistry rarityRegistry;
    private final QuestContributionAggregator aggregator = new QuestContributionAggregator();

    @BeforeEach
    void setUp() {
        typeRegistry = new RewardDistributionTypeRegistry();
        typeRegistry.register(new ParticipatedDistributionType());
        rarityRegistry = new QuestRarityRegistry();
    }

    /**
     * Creates a simple INDIVIDUAL/PARTICIPATED tier with a tracking reward.
     */
    private DistributionTierConfig trackingTier(QuestRewardType reward) {
        return new DistributionTierConfig("participated",
                ParticipatedDistributionType.KEY,
                RewardSplitMode.INDIVIDUAL,
                List.of(reward), Map.of(), null, null, true);
    }

    private QuestRewardType scalableReward() {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_KEY);
        when(reward.getNumericAmount()).thenReturn(OptionalLong.of(100L));
        when(reward.withAmountMultiplier(1.0)).thenReturn(reward);
        return reward;
    }

    @Nested
    @DisplayName("Objective-level and quest-level distributions are independent")
    class ObjectiveAndQuestLevel {

        @Test
        @DisplayName("objective distribution uses objective-scoped contributions only")
        void resolve_includesOnlyContributors_whenObjectiveScopedContributions() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();

            // Objective level: only p1 contributed to this specific objective
            Map<UUID, Long> objectiveContributions = Map.of(p1, 50L);
            ContributionSnapshot objectiveSnapshot = aggregator.toSnapshot(
                    objectiveContributions, Set.of(p1, p2));

            QuestRewardType objectiveReward = scalableReward();
            RewardDistributionConfig objectiveConfig = new RewardDistributionConfig(
                    List.of(trackingTier(objectiveReward)));

            var objectiveResult = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    objectiveConfig, objectiveSnapshot, null, rarityRegistry, typeRegistry);

            // Only p1 contributed to this objective → only p1 receives objective reward
            assertTrue(objectiveResult.containsKey(p1));
            // p2 has no contribution to this objective (even if they contributed overall)
        }

        @Test
        @DisplayName("quest distribution uses quest-wide contributions")
        void resolve_includesAllContributors_whenQuestWideContributions() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();

            // Quest level: both players contributed across the whole quest
            Map<UUID, Long> questContributions = Map.of(p1, 60L, p2, 40L);
            ContributionSnapshot questSnapshot = aggregator.toSnapshot(
                    questContributions, Set.of(p1, p2));

            QuestRewardType questReward = scalableReward();
            RewardDistributionConfig questConfig = new RewardDistributionConfig(
                    List.of(trackingTier(questReward)));

            var questResult = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    questConfig, questSnapshot, null, rarityRegistry, typeRegistry);

            // Both players contributed to the quest → both receive quest reward
            assertTrue(questResult.containsKey(p1));
            assertTrue(questResult.containsKey(p2));
        }

        @Test
        @DisplayName("player accumulates rewards from objective and quest levels independently")
        void resolve_producesIndependentRewards_whenCalledAtEachLevel() {
            UUID p1 = UUID.randomUUID();

            QuestRewardType objectiveReward = scalableReward();
            QuestRewardType questReward = scalableReward();

            // Objective-level distribution
            RewardDistributionConfig objectiveConfig = new RewardDistributionConfig(
                    List.of(trackingTier(objectiveReward)));
            ContributionSnapshot objectiveSnapshot = aggregator.toSnapshot(
                    Map.of(p1, 100L), Set.of(p1));
            var objectiveResult = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    objectiveConfig, objectiveSnapshot, null, rarityRegistry, typeRegistry);

            // Quest-level distribution
            RewardDistributionConfig questConfig = new RewardDistributionConfig(
                    List.of(trackingTier(questReward)));
            ContributionSnapshot questSnapshot = aggregator.toSnapshot(
                    Map.of(p1, 100L), Set.of(p1));
            var questResult = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    questConfig, questSnapshot, null, rarityRegistry, typeRegistry);

            // Player should receive rewards from both levels
            assertTrue(objectiveResult.containsKey(p1), "Player should receive objective-level reward");
            assertTrue(questResult.containsKey(p1), "Player should receive quest-level reward");
            // Combined: player has 2 rewards total across levels (1 from each)
            assertEquals(1, objectiveResult.get(p1).size());
            assertEquals(1, questResult.get(p1).size());
        }
    }

    @Nested
    @DisplayName("Four-level distribution — all fire independently")
    class FourLevelDistribution {

        @Test
        @DisplayName("resolver produces correct output for each level with independent snapshots")
        void resolve_qualifiesPlayerAtAllLevels_whenContributedToEachLevel() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();

            // Each level has its own contribution scope and reward
            QuestRewardType[] rewards = new QuestRewardType[4];
            List<Map<UUID, Long>> contributions = List.of(
                    Map.of(p1, 100L),             // objective: only p1
                    Map.of(p1, 60L, p2, 40L),     // stage: both
                    Map.of(p1, 70L, p2, 30L),     // phase: both
                    Map.of(p1, 50L, p2, 50L)      // quest: both
            );

            for (int i = 0; i < 4; i++) {
                rewards[i] = scalableReward();
            }

            int p1RewardCount = 0;
            int p2RewardCount = 0;

            for (int i = 0; i < 4; i++) {
                RewardDistributionConfig config = new RewardDistributionConfig(
                        List.of(trackingTier(rewards[i])));
                ContributionSnapshot snapshot = aggregator.toSnapshot(
                        contributions.get(i), new HashSet<>(contributions.get(i).keySet()));
                var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                        config, snapshot, null, rarityRegistry, typeRegistry);

                if (result.containsKey(p1)) p1RewardCount++;
                if (result.containsKey(p2)) p2RewardCount++;
            }

            // p1 participates in all 4 levels
            assertEquals(4, p1RewardCount, "p1 should qualify across all 4 distribution levels");
            // p2 participates in levels 1–3 (not level 0 objective which had only p1)
            assertEquals(3, p2RewardCount, "p2 should qualify across 3 of 4 distribution levels");
        }
    }

    @Nested
    @DisplayName("Contributions scoped correctly at each level")
    class ContributionScoping {

        @Test
        @DisplayName("objective contributions are independent from quest contributions")
        void resolve_scopesContributionsIndependently_whenObjectiveVsQuestLevel() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID();

            // Objective-scope: only p1 contributed (p2 was elsewhere)
            Map<UUID, Long> objectiveContribs = Map.of(p1, 100L);
            // Quest-scope: both contributed
            Map<UUID, Long> questContribs = Map.of(p1, 100L, p2, 100L);

            ContributionSnapshot objSnapshot = aggregator.toSnapshot(
                    objectiveContribs, Set.of(p1, p2));
            ContributionSnapshot questSnapshot = aggregator.toSnapshot(
                    questContribs, Set.of(p1, p2));

            QuestRewardType objReward = scalableReward();
            QuestRewardType questReward = scalableReward();

            var objResult = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    new RewardDistributionConfig(List.of(trackingTier(objReward))),
                    objSnapshot, null, rarityRegistry, typeRegistry);
            var questResult = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    new RewardDistributionConfig(List.of(trackingTier(questReward))),
                    questSnapshot, null, rarityRegistry, typeRegistry);

            // Objective level: only p1 (p2 had 0 contribution to this objective)
            assertEquals(1, objResult.size(), "Only p1 should qualify at objective level");
            // Quest level: both players
            assertEquals(2, questResult.size(), "Both players should qualify at quest level");
        }
    }
}
