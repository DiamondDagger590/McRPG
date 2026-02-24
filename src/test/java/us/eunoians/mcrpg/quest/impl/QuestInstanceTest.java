package us.eunoians.mcrpg.quest.impl;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestExpireEvent;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class QuestInstanceTest extends McRPGBaseTest {

    private QuestDefinition definition;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        definition = QuestTestHelper.singlePhaseQuest("test_quest");
    }

    @DisplayName("Given a definition, when constructing an instance, then state is NOT_STARTED and tree is built")
    @Test
    public void constructor_buildsTreeWithNotStartedState() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        assertEquals(QuestState.NOT_STARTED, instance.getQuestState());
        assertNotNull(instance.getQuestUUID());
        assertFalse(instance.getQuestStageInstances().isEmpty());
        assertFalse(instance.getQuestStageInstances().get(0).getQuestObjectives().isEmpty());
    }

    @DisplayName("Given a NOT_STARTED instance, when starting, then state transitions to IN_PROGRESS and fires QuestStartEvent")
    @Test
    public void start_transitionsToInProgress_andFiresEvent() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.start(definition);
        assertEquals(QuestState.IN_PROGRESS, instance.getQuestState());
        assertTrue(instance.getStartTime().isPresent());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestStartEvent.class));
    }

    @DisplayName("Given a started instance, when starting activates phase 0 stages, then they are IN_PROGRESS")
    @Test
    public void start_activatesPhaseZeroStages() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.start(definition);
        assertFalse(instance.getActiveQuestStages().isEmpty());
        assertEquals(QuestStageState.IN_PROGRESS,
                instance.getStagesForPhase(0).get(0).getQuestStageState());
    }

    @DisplayName("Given an IN_PROGRESS instance, when completing, then state transitions to COMPLETED and fires event")
    @Test
    public void complete_transitionsToCompleted_whenInProgress() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        instance.complete(definition);
        assertEquals(QuestState.COMPLETED, instance.getQuestState());
        assertTrue(instance.getEndTime().isPresent());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestCompleteEvent.class));
    }

    @DisplayName("Given a NOT_STARTED instance, when completing, then it does nothing")
    @Test
    public void complete_doesNothing_whenNotStarted() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.complete(definition);
        assertEquals(QuestState.NOT_STARTED, instance.getQuestState());
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(QuestCompleteEvent.class));
    }

    @DisplayName("Given an IN_PROGRESS instance, when cancelling, then state transitions to CANCELLED and fires event")
    @Test
    public void cancel_transitionsToCancelled_whenInProgress() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        instance.cancel();
        assertEquals(QuestState.CANCELLED, instance.getQuestState());
        assertTrue(instance.getEndTime().isPresent());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestCancelEvent.class));
    }

    @DisplayName("Given a COMPLETED instance, when cancelling, then it does nothing")
    @Test
    public void cancel_doesNothing_whenCompleted() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        instance.complete(definition);
        server.getPluginManager().clearEvents();
        instance.cancel();
        assertEquals(QuestState.COMPLETED, instance.getQuestState());
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(QuestCancelEvent.class));
    }

    @DisplayName("Given an IN_PROGRESS instance, when cancelling, then all child stages are cancelled")
    @Test
    public void cancel_cascadesToChildStages() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        instance.cancel();
        instance.getQuestStageInstances().forEach(stage ->
                assertEquals(QuestStageState.CANCELLED, stage.getQuestStageState()));
    }

    @DisplayName("Given an IN_PROGRESS instance, when expiring, then QuestExpireEvent is fired followed by QuestCancelEvent")
    @Test
    public void expire_firesExpireAndCancelEvents_whenInProgress() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        instance.expire();
        assertEquals(QuestState.CANCELLED, instance.getQuestState());
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestExpireEvent.class));
        assertThat(server.getPluginManager(), hasFiredEventInstance(QuestCancelEvent.class));
    }

    @DisplayName("Given a COMPLETED instance, when expiring, then it does nothing")
    @Test
    public void expire_doesNothing_whenCompleted() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        instance.complete(definition);
        server.getPluginManager().clearEvents();
        instance.expire();
        assertThat(server.getPluginManager(), hasNotFiredEventInstance(QuestExpireEvent.class));
    }

    @DisplayName("Given a fresh instance, when checking dirty flag, then it is false")
    @Test
    public void isDirty_returnsFalse_whenFresh() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        assertFalse(instance.isDirty());
    }

    @DisplayName("Given an instance, when marking dirty, then isDirty returns true")
    @Test
    public void markDirty_setsDirtyFlag() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.markDirty();
        assertTrue(instance.isDirty());
    }

    @DisplayName("Given a dirty instance, when clearing dirty, then isDirty returns false")
    @Test
    public void clearDirty_resetsDirtyFlag() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.markDirty();
        instance.clearDirty();
        assertFalse(instance.isDirty());
    }

    @DisplayName("Given an instance with no scope, when setting scope once, then it succeeds")
    @Test
    public void setQuestScope_succeedsOnce() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        QuestScope mockScope = mock(QuestScope.class);
        instance.setQuestScope(mockScope);
        assertTrue(instance.getQuestScope().isPresent());
    }

    @DisplayName("Given an instance with a scope already set, when setting scope again, then it throws IllegalStateException")
    @Test
    public void setQuestScope_throwsIllegalStateException_whenAlreadySet() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        QuestScope mockScope = mock(QuestScope.class);
        instance.setQuestScope(mockScope);
        assertThrows(IllegalStateException.class, () -> instance.setQuestScope(mock(QuestScope.class)));
    }

    @DisplayName("Given an instance with no expiration, when checking isExpired, then it returns false")
    @Test
    public void isExpired_returnsFalse_whenNoExpiration() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        assertFalse(instance.isExpired());
    }

    @DisplayName("Given an instance with an expiration in the past, when checking isExpired, then it returns true")
    @Test
    public void isExpired_returnsTrue_whenExpirationPassed() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(definition);
        instance.setExpirationTime(1L);
        assertTrue(instance.isExpired());
    }

    @DisplayName("Given a multi-phase quest, when getting stages for a phase, then only matching stages are returned")
    @Test
    public void getStagesForPhase_filtersCorrectly() {
        QuestStageDefinition stage0 = QuestTestHelper.singleStageDef("s0", "o0");
        QuestStageDefinition stage1 = QuestTestHelper.singleStageDef("s1", "o1");
        QuestPhaseDefinition phase0 = QuestTestHelper.phaseDef(0, PhaseCompletionMode.ALL, stage0);
        QuestPhaseDefinition phase1 = QuestTestHelper.phaseDef(1, PhaseCompletionMode.ALL, stage1);
        QuestDefinition multiDef = QuestTestHelper.multiPhaseQuest("multi", phase0, phase1);
        QuestInstance instance = QuestTestHelper.newQuestInstance(multiDef);
        assertEquals(1, instance.getStagesForPhase(0).size());
        assertEquals(1, instance.getStagesForPhase(1).size());
        assertEquals(0, instance.getStagesForPhase(2).size());
    }

    @DisplayName("Given a started instance, when getting active stages, then only IN_PROGRESS stages returned")
    @Test
    public void getActiveQuestStages_returnsOnlyInProgressStages() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(definition);
        List<QuestStageInstance> active = instance.getActiveQuestStages();
        assertFalse(active.isEmpty());
        active.forEach(stage -> assertEquals(QuestStageState.IN_PROGRESS, stage.getQuestStageState()));
    }

    @DisplayName("Given a definition with an expiration, when constructing instance, then expirationTime is set")
    @Test
    public void constructor_setsExpirationTime_whenDefinitionHasExpiration() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition expiringDef = new QuestDefinition(
                new NamespacedKey("mcrpg", "expiring_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                Duration.ofHours(24),
                List.of(phase),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null
        );
        QuestInstance instance = QuestTestHelper.newQuestInstance(expiringDef);
        assertTrue(instance.getExpirationTime().isPresent());
    }
}
