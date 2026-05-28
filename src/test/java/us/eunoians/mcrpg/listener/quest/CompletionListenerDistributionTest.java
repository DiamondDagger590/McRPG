package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.distribution.builtin.MembershipDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.quest.board.QuestBoardTerminator;
import us.eunoians.mcrpg.quest.board.distribution.DistributionCompletionService;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionGranter;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests that distribution rewards are resolved and granted by the completion listeners
 * at the correct level (objective, stage, phase, quest).
 */
public class CompletionListenerDistributionTest extends McRPGBaseTest {

    private static final String NAMESPACE = "mcrpg";
    private static final NamespacedKey EXPANSION_KEY = new NamespacedKey(NAMESPACE, "test_expansion");

    private QuestManager mockQuestManager;
    private DistributionCompletionService distributionService;

    @AfterEach
    void tearDown() {
        HandlerList.unregisterAll(mcRPG);
    }

    @BeforeEach
    void setUp() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        var rarityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_RARITY);
        var typeRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        if (typeRegistry.get(ParticipatedDistributionType.KEY).isEmpty()) {
            typeRegistry.register(new ParticipatedDistributionType());
        }
        if (typeRegistry.get(MembershipDistributionType.KEY).isEmpty()) {
            typeRegistry.register(new MembershipDistributionType());
        }
        var contributionAggregator = new QuestContributionAggregator();
        var distributionResolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
        distributionService = new DistributionCompletionService(rarityRegistry, typeRegistry, new RewardDistributionGranter(mcRPG), contributionAggregator, distributionResolver);
        server.getPluginManager().registerEvents(
                new QuestCompleteListener(new QuestBoardTerminator(mcRPG), distributionService, contributionAggregator), mcRPG);

        mockQuestManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        clearInvocations(mockQuestManager);
    }

    @Nested
    @DisplayName("QuestCompleteListener — event triggers retirement")
    class QuestCompleteDistribution {

        @Test
        @DisplayName("quest with distribution config → distribution resolved and retireQuest called")
        void onQuestComplete_grantsDistributionReward_whenDistributionConfigPresent() {
            PlayerMock playerMock = server.addPlayer();
            UUID playerUUID = playerMock.getUniqueId();

            MockQuestRewardType reward = QuestTestHelper.mockRewardType("membership_reward");
            DistributionTierConfig tier = membershipTier(reward);
            RewardDistributionConfig distributionConfig = new RewardDistributionConfig(List.of(tier));

            QuestDefinition def = questDefWithDistribution("distribute_on_complete", distributionConfig);
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);

            server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));

            verify(mockQuestManager, times(1)).retireQuest(any(QuestInstance.class));
            assertEquals(1, reward.getGrantCount(),
                    "Distribution reward should be granted exactly once per quest completion");
        }

        @Test
        @DisplayName("quest without reward-distribution → retireQuest still called")
        void onQuestComplete_callsRetireQuest_whenNoDistributionConfig() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_distribution");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));

            verify(mockQuestManager, times(1)).retireQuest(any(QuestInstance.class));
        }
    }

    @Nested
    @DisplayName("DistributionCompletionService — resolveAndGrant")
    class DistributionServiceHelper {

        @Test
        @DisplayName("direct call grants rewards to online player")
        void resolveAndGrant_grantsReward_whenPlayerIsOnline() {
            PlayerMock playerMock = server.addPlayer();
            UUID playerUUID = playerMock.getUniqueId();

            MockQuestRewardType reward = QuestTestHelper.mockRewardType("direct_reward");
            DistributionTierConfig tier = participationTier(reward);
            RewardDistributionConfig config = new RewardDistributionConfig(List.of(tier));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("direct_test");
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);

            Map<UUID, Long> contributions = Map.of(playerUUID, 100L);
            Set<UUID> group = Set.of(playerUUID);

            distributionService.resolveAndGrant(config, contributions, group, quest);

            assertEquals(1, reward.getGrantCount(), "Online player should receive distribution reward");
        }

        @Test
        @DisplayName("non-board quest with no rarity key — non-gated tiers still apply")
        void resolveAndGrant_grantsNonGatedTier_whenQuestHasNoRarityKey() {
            PlayerMock playerMock = server.addPlayer();
            UUID playerUUID = playerMock.getUniqueId();

            MockQuestRewardType reward = QuestTestHelper.mockRewardType("non_rarity_reward");
            // Tier with no rarity gate
            DistributionTierConfig tier = participationTier(reward);
            RewardDistributionConfig config = new RewardDistributionConfig(List.of(tier));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("non_board_quest");
            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);

            // Non-board quests have null rarity key
            distributionService.resolveAndGrant(config, Map.of(playerUUID, 100L), Set.of(playerUUID), quest);

            assertEquals(1, reward.getGrantCount());
        }
    }

    private DistributionTierConfig participationTier(MockQuestRewardType reward) {
        return new DistributionTierConfig("participated",
                ParticipatedDistributionType.KEY,
                RewardSplitMode.INDIVIDUAL,
                List.of(reward),
                Map.of(), null, null, true);
    }

    private DistributionTierConfig membershipTier(MockQuestRewardType reward) {
        return new DistributionTierConfig("membership",
                MembershipDistributionType.KEY,
                RewardSplitMode.INDIVIDUAL,
                List.of(reward),
                Map.of(), null, null, true);
    }

    private QuestDefinition questDefWithDistribution(String key, RewardDistributionConfig distribution) {
        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                new NamespacedKey(NAMESPACE, key + "_obj"),
                QuestTestHelper.mockObjectiveType(key + "_type"),
                10, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(
                new NamespacedKey(NAMESPACE, key + "_stage"),
                List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0,
                PhaseCompletionMode.ALL,
                List.of(stage), List.of(), null);
        return new QuestDefinition.Builder(
                new NamespacedKey(NAMESPACE, key),
                new NamespacedKey(NAMESPACE, "single_player"),
                List.of(phase)
        ).rewardDistribution(distribution)
                .build();
    }
}
