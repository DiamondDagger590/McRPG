package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestPhaseCompleteEvent;
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
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuestStageCompleteListenerTest extends McRPGBaseTest {

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
        server.getPluginManager().registerEvents(new QuestStageCompleteListener(distributionService, aggregator), mcRPG);

        if (!RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).registered(McRPGManagerKey.QUEST)) {
            mockQuestManager = mock(QuestManager.class);
            RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockQuestManager);
        } else {
            mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        }
    }

    @DisplayName("Given an ALL-mode phase with all stages complete, when stage complete fires, then phase complete fires")
    @Test
    public void onStageComplete_allMode_firesPhaseComplete_whenAllStagesDone() {
        QuestStageDefinition stageDef = QuestTestHelper.singleStageDef("all_s", "all_o");
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("all_phase_test", phaseDef);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stage = quest.getStagesForPhase(0).get(0);

        server.getPluginManager().clearEvents();
        stage.complete();

        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestPhaseCompleteEvent.class));
    }

    @DisplayName("Stage with reward-distribution config — distribution rewards are granted on stage complete")
    @Test
    public void onStageComplete_grantsDistributionReward_whenDistributionConfigPresent() {
        PlayerMock playerMock = server.addPlayer();
        UUID playerUUID = playerMock.getUniqueId();

        MockQuestRewardType reward = QuestTestHelper.mockRewardType("stage_dist_reward");
        DistributionTierConfig tier = new DistributionTierConfig("membership",
                MembershipDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                List.of(reward), Map.of(), null, null, true);
        RewardDistributionConfig distConfig = new RewardDistributionConfig(List.of(tier));

        QuestObjectiveDefinition objective = QuestTestHelper.singleObjectiveDef("stage_dist_obj", 10);
        QuestStageDefinition stageDef = new QuestStageDefinition(
                new NamespacedKey("mcrpg", "stage_dist_s"), List.of(objective), List.of(), distConfig);
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageDef);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("stage_dist_quest", phase);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        QuestStageInstance stage = quest.getStagesForPhase(0).get(0);

        server.getPluginManager().clearEvents();
        stage.complete();

        assertTrue(reward.getGrantCount() >= 1,
                "Distribution reward should be granted when stage has reward-distribution config");
    }

    @DisplayName("Given an ANY-mode phase, when one stage completes, then phase complete fires and siblings are cancelled")
    @Test
    public void onStageComplete_anyMode_firesPhaseComplete_andCancelsSiblings() {
        QuestStageDefinition stageA = QuestTestHelper.singleStageDef("any_a", "any_oa");
        QuestStageDefinition stageB = QuestTestHelper.singleStageDef("any_b", "any_ob");
        QuestPhaseDefinition phaseDef = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ANY, stageA, stageB);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("any_phase_test", phaseDef);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stageInstanceA = quest.getStagesForPhase(0).get(0);

        server.getPluginManager().clearEvents();
        stageInstanceA.complete();

        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestPhaseCompleteEvent.class));
        QuestStageInstance sibling = quest.getStagesForPhase(0).get(1);
        assertEquals(QuestStageState.CANCELLED, sibling.getQuestStageState());
    }
}
