package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
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
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
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
import static org.mockito.Mockito.when;

public class QuestPhaseCompleteListenerTest extends McRPGBaseTest {

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
        server.getPluginManager().registerEvents(new QuestPhaseCompleteListener(distributionService, aggregator), mcRPG);

        mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
    }

    @DisplayName("Given a multi-phase quest, when phase 0 completes, then phase 1 stages are activated")
    @Test
    public void onPhaseComplete_activatesNextPhaseStages() {
        QuestStageDefinition stage0 = QuestTestHelper.singleStageDef("p0_s", "p0_o");
        QuestStageDefinition stage1 = QuestTestHelper.singleStageDef("p1_s", "p1_o");
        QuestPhaseDefinition phase0 = QuestTestHelper.phaseDef(0, PhaseCompletionMode.ALL, stage0);
        QuestPhaseDefinition phase1 = QuestTestHelper.phaseDef(1, PhaseCompletionMode.ALL, stage1);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("mp_listener", phase0, phase1);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

        server.getPluginManager().callEvent(new QuestPhaseCompleteEvent(quest, phase0, 0));
        assertEquals(QuestStageState.IN_PROGRESS, quest.getStagesForPhase(1).get(0).getQuestStageState());
    }

    @DisplayName("Phase with reward-distribution config — distribution rewards are granted on phase complete")
    @Test
    public void onPhaseComplete_grantsDistributionReward_whenDistributionConfigPresent() {
        PlayerMock playerMock = server.addPlayer();
        UUID playerUUID = playerMock.getUniqueId();

        MockQuestRewardType reward = QuestTestHelper.mockRewardType("phase_dist_reward");
        DistributionTierConfig tier = new DistributionTierConfig("membership",
                MembershipDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                List.of(reward), Map.of(), null, null, true);
        RewardDistributionConfig distConfig = new RewardDistributionConfig(List.of(tier));

        QuestStageDefinition stage = QuestTestHelper.singleStageDef("phase_dist_s", "phase_dist_o");
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of(stage), List.of(), distConfig);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("phase_dist_quest", phase);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);

        server.getPluginManager().callEvent(new QuestPhaseCompleteEvent(quest, phase, 0));

        assertTrue(reward.getGrantCount() >= 1,
                "Distribution reward should be granted when phase has reward-distribution config");
    }

    @DisplayName("Given a single-phase quest, when the only phase completes, then quest complete fires")
    @Test
    public void onPhaseComplete_completesQuest_whenLastPhase() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("sp_s", "sp_o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("sp_listener", phase);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.of(def));

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

        server.getPluginManager().callEvent(new QuestPhaseCompleteEvent(quest, phase, 0));
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestCompleteEvent.class));
    }
}
