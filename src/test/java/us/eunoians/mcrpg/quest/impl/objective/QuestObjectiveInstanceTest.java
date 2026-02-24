package us.eunoians.mcrpg.quest.impl.objective;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestObjectiveProgressEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;

public class QuestObjectiveInstanceTest extends McRPGBaseTest {

    private QuestDefinition definition;
    private QuestInstance quest;
    private QuestStageInstance stage;
    private QuestObjectiveInstance objective;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        definition = QuestTestHelper.singlePhaseQuest("obj_test");
        quest = QuestTestHelper.startedQuestInstance(definition);
        stage = quest.getQuestStageInstances().get(0);
        objective = stage.getQuestObjectives().get(0);
    }

    @DisplayName("Given an IN_PROGRESS objective, when progressing with positive delta, then it fires QuestObjectiveProgressEvent")
    @Test
    public void progress_firesProgressEvent_withPositiveDelta() {
        UUID player = UUID.randomUUID();
        objective.progress(5, player);
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestObjectiveProgressEvent.class));
    }

    @DisplayName("Given an objective, when progressing with zero delta, then it throws IllegalArgumentException")
    @Test
    public void progress_throwsIllegalArgumentException_whenDeltaZero() {
        assertThrows(IllegalArgumentException.class, () -> objective.progress(0, UUID.randomUUID()));
    }

    @DisplayName("Given an objective, when progressing with negative delta, then it throws IllegalArgumentException")
    @Test
    public void progress_throwsIllegalArgumentException_whenDeltaNegative() {
        assertThrows(IllegalArgumentException.class, () -> objective.progress(-1, UUID.randomUUID()));
    }

    @DisplayName("Given an objective near completion, when progressing past required, then progress caps at required")
    @Test
    public void progress_capsAtRequiredProgress() {
        objective.setCurrentProgression(9);
        objective.progress(5, UUID.randomUUID());
        assertEquals(objective.getRequiredProgression(), objective.getCurrentProgression());
    }

    @DisplayName("Given an objective, when progressing with a player UUID, then player contribution is tracked")
    @Test
    public void progress_tracksPlayerContribution() {
        UUID player = UUID.randomUUID();
        objective.progress(3, player);
        assertEquals(3, objective.getPlayerContribution(player));
    }

    @DisplayName("Given an objective reaching required progress, when progressing, then QuestObjectiveCompleteEvent is fired")
    @Test
    public void progress_firesCompleteEvent_whenThresholdReached() {
        objective.setCurrentProgression(objective.getRequiredProgression() - 1);
        objective.progress(1, UUID.randomUUID());
        assertEquals(QuestObjectiveState.COMPLETED, objective.getQuestObjectiveState());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestObjectiveCompleteEvent.class));
    }

    @DisplayName("Given an objective progressed, when checking dirty flag on parent quest, then it is dirty")
    @Test
    public void progress_marksParentQuestDirty() {
        assertFalse(quest.isDirty());
        objective.progress(1, UUID.randomUUID());
        assertTrue(quest.isDirty());
    }

    @DisplayName("Given a NOT_IN_PROGRESS objective, when progressing, then it is ignored")
    @Test
    public void progress_doesNothing_whenObjectiveNotInProgress() {
        objective.markAsComplete();
        server.getPluginManager().clearEvents();
        objective.progress(1, UUID.randomUUID());
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(QuestObjectiveProgressEvent.class));
    }

    @DisplayName("Given an objective on an expired quest, when progressing, then the quest expires")
    @Test
    public void progress_expiresQuest_whenQuestExpired() {
        quest.setExpirationTime(1L);
        objective.progress(1, UUID.randomUUID());
        assertEquals(us.eunoians.mcrpg.quest.impl.QuestState.CANCELLED, quest.getQuestState());
    }

    @DisplayName("Given a valid objective, when setting requiredProgression to zero, then it throws IllegalArgumentException")
    @Test
    public void setRequiredProgression_throwsIllegalArgumentException_whenZero() {
        assertThrows(IllegalArgumentException.class, () -> objective.setRequiredProgression(0));
    }

    @DisplayName("Given a valid objective, when setting currentProgression to negative, then it throws IllegalArgumentException")
    @Test
    public void setCurrentProgression_throwsIllegalArgumentException_whenNegative() {
        assertThrows(IllegalArgumentException.class, () -> objective.setCurrentProgression(-1));
    }

    @DisplayName("Given an unknown player UUID, when getting contribution, then it returns 0")
    @Test
    public void getPlayerContribution_returnsZero_whenUnknownPlayer() {
        assertEquals(0, objective.getPlayerContribution(UUID.randomUUID()));
    }

    @DisplayName("Given a player who contributed, when getting contribution, then it returns the correct value")
    @Test
    public void getPlayerContribution_returnsCorrectValue_afterProgress() {
        UUID player = UUID.randomUUID();
        objective.progress(7, player);
        assertEquals(7, objective.getPlayerContribution(player));
    }

    @DisplayName("Given a NOT_STARTED objective, when activating, then it transitions to IN_PROGRESS")
    @Test
    public void activate_transitionsToInProgress() {
        QuestInstance freshQuest = QuestTestHelper.newQuestInstance(definition);
        QuestObjectiveInstance freshObj = freshQuest.getQuestStageInstances().get(0).getQuestObjectives().get(0);
        assertEquals(QuestObjectiveState.NOT_STARTED, freshObj.getQuestObjectiveState());
        freshObj.activate();
        assertEquals(QuestObjectiveState.IN_PROGRESS, freshObj.getQuestObjectiveState());
        assertTrue(freshObj.getStartTime().isPresent());
    }

    @DisplayName("Given an IN_PROGRESS objective, when force-completing, then it transitions to COMPLETED")
    @Test
    public void markAsComplete_transitionsToCompleted_whenInProgress() {
        objective.markAsComplete();
        assertEquals(QuestObjectiveState.COMPLETED, objective.getQuestObjectiveState());
        assertTrue(objective.getEndTime().isPresent());
    }

    @DisplayName("Given an IN_PROGRESS objective, when cancelling, then it transitions to CANCELLED")
    @Test
    public void cancel_transitionsToCancelled() {
        objective.cancel();
        assertEquals(QuestObjectiveState.CANCELLED, objective.getQuestObjectiveState());
        assertTrue(objective.getEndTime().isPresent());
    }

    @DisplayName("Given a COMPLETED objective, when cancelling, then it does nothing")
    @Test
    public void cancel_doesNothing_whenCompleted() {
        objective.markAsComplete();
        objective.cancel();
        assertEquals(QuestObjectiveState.COMPLETED, objective.getQuestObjectiveState());
    }
}
