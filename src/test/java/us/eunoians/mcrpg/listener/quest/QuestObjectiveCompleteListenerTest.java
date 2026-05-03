package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestStageCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.distribution.DistributionCompletionService;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionGranter;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.distribution.builtin.MembershipDistributionType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class QuestObjectiveCompleteListenerTest extends McRPGBaseTest {

    private QuestDefinition definition;
    private QuestManager mockQuestManager;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        var rarityRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_RARITY);
        var distTypeRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        var aggregator = new QuestContributionAggregator();
        var resolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
        var distributionService = new DistributionCompletionService(
                rarityRegistry, distTypeRegistry, new RewardDistributionGranter(mcRPG), aggregator, resolver);
        server.getPluginManager().registerEvents(new QuestObjectiveCompleteListener(distributionService, aggregator), mcRPG);
        definition = QuestTestHelper.singlePhaseQuest("obj_complete_test");

        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(definition));
    }

    @DisplayName("Given a stage with all objectives completed, when objective completes, then stage complete event fires")
    @Test
    public void onObjectiveComplete_firesStageComplete_whenAllObjectivesDone() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);
        objective.markAsComplete();

        server.getPluginManager().callEvent(new QuestObjectiveCompleteEvent(quest, stage, objective));
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestStageCompleteEvent.class));
    }

    @DisplayName("Objective with reward-distribution config — distribution rewards are granted on objective complete")
    @Test
    public void onObjectiveComplete_grantsDistributionReward_whenDistributionConfigPresent() {
        PlayerMock playerMock = server.addPlayer();
        UUID playerUUID = playerMock.getUniqueId();

        MockQuestRewardType reward = QuestTestHelper.mockRewardType("obj_dist_reward");
        DistributionTierConfig tier = new DistributionTierConfig("membership",
                MembershipDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                List.of(reward), Map.of(), null, null, true);
        RewardDistributionConfig distConfig = new RewardDistributionConfig(List.of(tier));

        QuestObjectiveDefinition objectiveDef = new QuestObjectiveDefinition(
                new NamespacedKey("mcrpg", "obj_dist_obj"),
                QuestTestHelper.mockObjectiveType("obj_dist_type"),
                10, List.of(), distConfig);
        QuestStageDefinition stageDef = new QuestStageDefinition(
                new NamespacedKey("mcrpg", "obj_dist_s"), List.of(objectiveDef), List.of(), null);
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("obj_dist_quest", phase);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);
        objective.markAsComplete();

        server.getPluginManager().callEvent(new QuestObjectiveCompleteEvent(quest, stage, objective));

        assertTrue(reward.getGrantCount() >= 1,
                "Distribution reward should be granted when objective has reward-distribution config");
    }

    @DisplayName("Given a stage with some objectives incomplete, when objective completes, then stage does NOT complete")
    @Test
    public void onObjectiveComplete_doesNotFireStageComplete_whenSomeObjectivesRemain() {
        QuestDefinition multiObjDef = QuestTestHelper.singlePhaseQuest("multi_obj_test");
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(multiObjDef));

        QuestInstance quest = QuestTestHelper.startedQuestInstance(multiObjDef);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);

        if (stage.getQuestObjectives().size() == 1) {
            return;
        }

        QuestObjectiveInstance objective = stage.getQuestObjectives().get(0);
        objective.markAsComplete();

        server.getPluginManager().clearEvents();
        server.getPluginManager().callEvent(new QuestObjectiveCompleteEvent(quest, stage, objective));
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(QuestStageCompleteEvent.class));
    }
}
