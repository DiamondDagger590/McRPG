package us.eunoians.mcrpg.quest.impl.stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestStageCompleteEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;

public class QuestStageInstanceTest extends McRPGBaseTest {

    private QuestDefinition definition;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        definition = QuestTestHelper.singlePhaseQuest("stage_test");
    }

    @DisplayName("Given a NOT_STARTED stage, when activating, then it transitions to IN_PROGRESS and sets start time")
    @Test
    public void activate_transitionsToInProgress_andSetsStartTime() {
        QuestInstance quest = QuestTestHelper.newQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        assertEquals(QuestStageState.NOT_STARTED, stage.getQuestStageState());
        stage.activate();
        assertEquals(QuestStageState.IN_PROGRESS, stage.getQuestStageState());
        assertTrue(stage.getStartTime().isPresent());
    }

    @DisplayName("Given an activated stage, when activating again, then it stays IN_PROGRESS (no-op)")
    @Test
    public void activate_isNoOp_whenAlreadyInProgress() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        Long firstStartTime = stage.getStartTime().orElse(null);
        stage.activate();
        assertEquals(firstStartTime, stage.getStartTime().orElse(null));
    }

    @DisplayName("Given an activated stage, when activating, then all objectives are activated too")
    @Test
    public void activate_activatesAllObjectives() {
        QuestInstance quest = QuestTestHelper.newQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.activate();
        stage.getQuestObjectives().forEach(obj ->
                assertEquals(QuestObjectiveState.IN_PROGRESS, obj.getQuestObjectiveState()));
    }

    @DisplayName("Given an IN_PROGRESS stage, when completing, then it transitions to COMPLETED and fires event")
    @Test
    public void complete_transitionsToCompleted_andFiresEvent() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.complete();
        assertEquals(QuestStageState.COMPLETED, stage.getQuestStageState());
        assertTrue(stage.getEndTime().isPresent());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestStageCompleteEvent.class));
    }

    @DisplayName("Given a completed stage, when checking objectives, then all are force-completed")
    @Test
    public void complete_forceCompletesAllObjectives() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.complete();
        stage.getQuestObjectives().forEach(obj ->
                assertEquals(QuestObjectiveState.COMPLETED, obj.getQuestObjectiveState()));
    }

    @DisplayName("Given an IN_PROGRESS stage, when cancelling, then it transitions to CANCELLED")
    @Test
    public void cancel_transitionsToCancelled_whenInProgress() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.cancel();
        assertEquals(QuestStageState.CANCELLED, stage.getQuestStageState());
        assertTrue(stage.getEndTime().isPresent());
    }

    @DisplayName("Given a NOT_STARTED stage, when cancelling, then it transitions to CANCELLED")
    @Test
    public void cancel_transitionsToCancelled_whenNotStarted() {
        QuestInstance quest = QuestTestHelper.newQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.cancel();
        assertEquals(QuestStageState.CANCELLED, stage.getQuestStageState());
    }

    @DisplayName("Given a cancelled stage, when checking objectives, then all are cancelled")
    @Test
    public void cancel_cancelsAllObjectives() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.cancel();
        stage.getQuestObjectives().forEach(obj ->
                assertEquals(QuestObjectiveState.CANCELLED, obj.getQuestObjectiveState()));
    }

    @DisplayName("Given a stage with all objectives completed, when checking status, then it returns true")
    @Test
    public void checkForUpdatedStatus_returnsTrue_whenAllObjectivesCompleted() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        stage.getQuestObjectives().forEach(QuestObjectiveInstance::markAsComplete);
        assertTrue(stage.checkForUpdatedStatus());
    }

    @DisplayName("Given a stage with some objectives incomplete, when checking status, then it returns false")
    @Test
    public void checkForUpdatedStatus_returnsFalse_whenSomeObjectivesIncomplete() {
        QuestInstance quest = QuestTestHelper.startedQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        assertFalse(stage.checkForUpdatedStatus());
    }

    @DisplayName("Given a stage, when getting objectives, then the returned list is immutable")
    @Test
    public void getQuestObjectives_returnsImmutableList() {
        QuestInstance quest = QuestTestHelper.newQuestInstance(definition);
        QuestStageInstance stage = quest.getQuestStageInstances().get(0);
        assertThrows(UnsupportedOperationException.class, () -> stage.getQuestObjectives().add(null));
    }
}
