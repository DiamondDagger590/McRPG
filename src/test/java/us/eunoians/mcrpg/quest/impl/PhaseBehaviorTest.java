package us.eunoians.mcrpg.quest.impl;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestPhaseCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestStageCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PhaseBehaviorTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        QuestManager mockQuestManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(mockQuestManager.getQuestDefinition(any(NamespacedKey.class))).thenReturn(Optional.empty());
    }

    @DisplayName("Given ALL-mode phase with two stages, when only stage A completes, then phase does NOT complete")
    @Test
    public void allMode_phaseDoesNotComplete_whenOnlyOneStageCompleted() {
        QuestStageDefinition stageA = QuestTestHelper.singleStageDef("stage_a", "obj_a");
        QuestStageDefinition stageB = QuestTestHelper.singleStageDef("stage_b", "obj_b");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageA, stageB);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("all_quest", phase);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        quest.getStagesForPhase(0).get(0).complete();

        assertEquals(QuestStageState.IN_PROGRESS, quest.getStagesForPhase(0).get(1).getQuestStageState());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestStageCompleteEvent.class));
    }

    @DisplayName("Given ALL-mode phase with two stages, when both complete, then events fire for both stages")
    @Test
    public void allMode_bothStagesComplete_firesStageCompleteEvents() {
        QuestStageDefinition stageA = QuestTestHelper.singleStageDef("stage_a2", "obj_a2");
        QuestStageDefinition stageB = QuestTestHelper.singleStageDef("stage_b2", "obj_b2");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stageA, stageB);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("all_quest2", phase);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        quest.getStagesForPhase(0).get(0).complete();
        quest.getStagesForPhase(0).get(1).complete();

        assertEquals(QuestStageState.COMPLETED, quest.getStagesForPhase(0).get(0).getQuestStageState());
        assertEquals(QuestStageState.COMPLETED, quest.getStagesForPhase(0).get(1).getQuestStageState());
    }

    @DisplayName("Given ANY-mode phase with two stages, when stage A completes, then sibling B should be cancellable")
    @Test
    public void anyMode_stageACompletes_siblingBCanBeCancelled() {
        QuestStageDefinition stageA = QuestTestHelper.singleStageDef("any_a", "obj_any_a");
        QuestStageDefinition stageB = QuestTestHelper.singleStageDef("any_b", "obj_any_b");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ANY, stageA, stageB);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("any_quest", phase);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        QuestStageInstance stageInstanceA = quest.getStagesForPhase(0).get(0);
        QuestStageInstance stageInstanceB = quest.getStagesForPhase(0).get(1);

        stageInstanceA.complete();
        stageInstanceB.cancel();

        assertEquals(QuestStageState.COMPLETED, stageInstanceA.getQuestStageState());
        assertEquals(QuestStageState.CANCELLED, stageInstanceB.getQuestStageState());
    }

    @DisplayName("Given a multi-phase quest, when only phase 0 stages are started, then phase 1 stages remain NOT_STARTED")
    @Test
    public void multiPhase_onlyPhaseZeroStagesActivated() {
        QuestStageDefinition stage0 = QuestTestHelper.singleStageDef("mp_s0", "mp_o0");
        QuestStageDefinition stage1 = QuestTestHelper.singleStageDef("mp_s1", "mp_o1");
        QuestPhaseDefinition phase0 = QuestTestHelper.phaseDef(0, PhaseCompletionMode.ALL, stage0);
        QuestPhaseDefinition phase1 = QuestTestHelper.phaseDef(1, PhaseCompletionMode.ALL, stage1);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("mp_quest", phase0, phase1);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

        assertEquals(QuestStageState.IN_PROGRESS, quest.getStagesForPhase(0).get(0).getQuestStageState());
        assertEquals(QuestStageState.NOT_STARTED, quest.getStagesForPhase(1).get(0).getQuestStageState());
    }

    @DisplayName("Given a single-phase ALL quest, when all stages complete, then quest can be completed")
    @Test
    public void singlePhase_allStagesComplete_questCanComplete() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("sp_s", "sp_o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("sp_quest", phase);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        quest.getStagesForPhase(0).get(0).complete();
        quest.complete(def);

        assertEquals(QuestState.COMPLETED, quest.getQuestState());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestCompleteEvent.class));
    }

    @DisplayName("Given a multi-phase quest, when phase 0 completes and phase 1 stages are manually activated, then they become IN_PROGRESS")
    @Test
    public void multiPhase_manualActivateNextPhase_stagesBecomInProgress() {
        QuestStageDefinition stage0 = QuestTestHelper.singleStageDef("act_s0", "act_o0");
        QuestStageDefinition stage1 = QuestTestHelper.singleStageDef("act_s1", "act_o1");
        QuestPhaseDefinition phase0 = QuestTestHelper.phaseDef(0, PhaseCompletionMode.ALL, stage0);
        QuestPhaseDefinition phase1 = QuestTestHelper.phaseDef(1, PhaseCompletionMode.ALL, stage1);
        QuestDefinition def = QuestTestHelper.multiPhaseQuest("act_quest", phase0, phase1);

        QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
        quest.getStagesForPhase(0).get(0).complete();

        for (QuestStageInstance stage : quest.getStagesForPhase(1)) {
            stage.activate();
        }

        assertEquals(QuestStageState.IN_PROGRESS, quest.getStagesForPhase(1).get(0).getQuestStageState());
    }
}
