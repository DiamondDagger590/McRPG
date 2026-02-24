package us.eunoians.mcrpg.event.quest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests construction validation, cancellation, and delta modification for
 * {@link QuestObjectiveProgressEvent}.
 */
public class QuestObjectiveProgressEventTest extends McRPGBaseTest {

    private QuestInstance quest;
    private QuestStageInstance stage;
    private QuestObjectiveInstance objective;

    @BeforeEach
    public void setup() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("event_test");
        quest = QuestTestHelper.startedQuestInstance(def);
        stage = quest.getActiveQuestStages().get(0);
        objective = stage.getQuestObjectives().get(0);
    }

    @DisplayName("Given a negative delta, when constructing event, then IllegalArgumentException is thrown")
    @Test
    public void constructor_throwsOnNegativeDelta() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestObjectiveProgressEvent(quest, stage, objective, UUID.randomUUID(), -1));
    }

    @DisplayName("Given a negative value, when calling setProgressDelta, then IllegalArgumentException is thrown")
    @Test
    public void setProgressDelta_throwsOnNegativeValue() {
        QuestObjectiveProgressEvent event = new QuestObjectiveProgressEvent(
                quest, stage, objective, UUID.randomUUID(), 5);
        assertThrows(IllegalArgumentException.class, () -> event.setProgressDelta(-1));
    }

    @DisplayName("Given zero, when calling setProgressDelta, then it succeeds")
    @Test
    public void setProgressDelta_allowsZero() {
        QuestObjectiveProgressEvent event = new QuestObjectiveProgressEvent(
                quest, stage, objective, UUID.randomUUID(), 5);
        event.setProgressDelta(0);
        assertEquals(0, event.getProgressDelta());
    }

    @DisplayName("Given a new event, when checking isCancelled, then it defaults to false")
    @Test
    public void isCancelled_defaultsFalse() {
        QuestObjectiveProgressEvent event = new QuestObjectiveProgressEvent(
                quest, stage, objective, UUID.randomUUID(), 5);
        assertFalse(event.isCancelled());
    }

    @DisplayName("Given an event, when setCancelled(true) is called, then isCancelled returns true")
    @Test
    public void setCancelled_true_reflectsInIsCancelled() {
        QuestObjectiveProgressEvent event = new QuestObjectiveProgressEvent(
                quest, stage, objective, UUID.randomUUID(), 5);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @DisplayName("Given an event with delta 5, when setProgressDelta(10) is called, then getProgressDelta returns 10")
    @Test
    public void getProgressDelta_returnsModifiedValue() {
        QuestObjectiveProgressEvent event = new QuestObjectiveProgressEvent(
                quest, stage, objective, UUID.randomUUID(), 5);
        event.setProgressDelta(10);
        assertEquals(10, event.getProgressDelta());
    }
}
