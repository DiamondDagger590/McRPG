package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests covering the objective index built during {@link QuestDefinition.Builder#build()}.
 * These tests confirm that {@link QuestDefinition#findObjectiveDefinition} indexes objectives
 * across all phases and stages, and that the builder applies correct defaults.
 */
public class QuestDefinitionObjectiveIndexTest extends McRPGBaseTest {

    @DisplayName("build() indexes all objectives across multiple phases and stages")
    @Test
    public void build_indexesAllObjectives_acrossMultiplePhasesAndStages() {
        QuestStageDefinition stage1 = QuestTestHelper.singleStageDef("s1", "obj1");
        QuestStageDefinition stage2 = QuestTestHelper.singleStageDef("s2", "obj2");
        QuestPhaseDefinition phase1 = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage1);
        QuestPhaseDefinition phase2 = QuestTestHelper.phaseDef(1, PhaseCompletionMode.ALL, stage2);
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "multi_phase"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phase1, phase2)
        ).build();

        assertTrue(def.findObjectiveDefinition(new NamespacedKey("mcrpg", "obj1")).isPresent());
        assertTrue(def.findObjectiveDefinition(new NamespacedKey("mcrpg", "obj2")).isPresent());
    }

    @DisplayName("build() objective index lookup returns the correct definition by key")
    @Test
    public void build_objectiveIndex_returnsCorrectObjective() {
        QuestObjectiveDefinition objDef = QuestTestHelper.singleObjectiveDef("specific_obj", 5);
        QuestStageDefinition stage = new QuestStageDefinition(
                new NamespacedKey("mcrpg", "specific_stage"),
                List.of(objDef),
                List.of(),
                null
        );
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "specific_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phase)
        ).build();

        var found = def.findObjectiveDefinition(new NamespacedKey("mcrpg", "specific_obj"));
        assertTrue(found.isPresent());
        assertSame(objDef, found.get());
    }

    @DisplayName("Builder default repeatMode is ONCE")
    @Test
    public void builder_defaultRepeatMode_isOnce() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("defaults_quest");
        assertEquals(QuestRepeatMode.ONCE, def.getRepeatMode());
    }

    @DisplayName("Builder default expiration is empty")
    @Test
    public void builder_defaultExpiration_isEmpty() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("defaults_expiry");
        assertTrue(def.getExpiration().isEmpty());
    }

    @DisplayName("Builder default rewardEntries is empty")
    @Test
    public void builder_defaultRewardEntries_isEmpty() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("defaults_rewards");
        assertTrue(def.getRewards().isEmpty());
    }

    @DisplayName("Builder onStartMessages setter stores messages that are later retrievable")
    @Test
    public void builder_onStartMessages_storesAndRetrievesMessages() {
        var message = OnStartMessage.fromInline(List.of("<primary>Welcome!"));
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("msg_stage", "msg_obj");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "msg_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phase)
        ).onStartMessages(List.of(message)).build();

        assertEquals(1, def.getOnStartMessages().size());
    }

    @Test
    @DisplayName("Given duplicate objective keys across stages, when build is called, then it throws IllegalStateException")
    public void build_duplicateObjectiveKey_throwsIllegalStateException() {
        QuestObjectiveDefinition sharedObj = QuestTestHelper.singleObjectiveDef("duplicate_obj", 1);
        QuestStageDefinition stage1 = new QuestStageDefinition(
                new NamespacedKey("mcrpg", "stage_a"),
                List.of(sharedObj),
                List.of(),
                null
        );
        QuestStageDefinition stage2 = new QuestStageDefinition(
                new NamespacedKey("mcrpg", "stage_b"),
                List.of(QuestTestHelper.singleObjectiveDef("duplicate_obj", 1)),
                List.of(),
                null
        );
        QuestPhaseDefinition phase = QuestTestHelper.phaseDef(0, PhaseCompletionMode.ALL, stage1, stage2);

        assertThrows(IllegalStateException.class, () -> new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "dup_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(phase)
        ).build());
    }
}
