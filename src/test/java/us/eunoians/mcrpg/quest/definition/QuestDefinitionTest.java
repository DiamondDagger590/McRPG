package us.eunoians.mcrpg.quest.definition;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestDefinitionTest extends McRPGBaseTest {

    @DisplayName("Given empty phases list, when constructing, then it throws IllegalArgumentException")
    @Test
    public void constructor_throwsIllegalArgumentException_whenPhasesEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new QuestDefinition(
                new NamespacedKey("mcrpg", "test"),
                new NamespacedKey("mcrpg", "single_player"),
                null,
                List.of(),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null
        ));
    }

    @DisplayName("Given a valid definition, when getting phases, then returned list is immutable")
    @Test
    public void getPhases_returnsImmutableList() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("immutable_test");
        assertThrows(UnsupportedOperationException.class, () -> def.getPhases().add(null));
    }

    @DisplayName("Given a valid definition, when getting rewards, then returned list is immutable")
    @Test
    public void getRewards_returnsImmutableList() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("reward_immutable_test");
        assertThrows(UnsupportedOperationException.class, () -> def.getRewards().add(null));
    }

    @DisplayName("Given a valid definition with one phase, when calling getPhase(0), then it returns the phase")
    @Test
    public void getPhase_returnsPhase_whenIndexValid() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("phase_test");
        assertTrue(def.getPhase(0).isPresent());
        assertEquals(0, def.getPhase(0).get().getPhaseIndex());
    }

    @DisplayName("Given a valid definition with one phase, when calling getPhase(5), then it returns empty")
    @Test
    public void getPhase_returnsEmpty_whenIndexOutOfBounds() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("oob_test");
        assertTrue(def.getPhase(5).isEmpty());
    }

    @DisplayName("Given a valid definition with one phase, when calling getPhase(-1), then it returns empty")
    @Test
    public void getPhase_returnsEmpty_whenIndexNegative() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("neg_test");
        assertTrue(def.getPhase(-1).isEmpty());
    }

    @DisplayName("Given a valid definition, when calling hasPhase, then it returns correct boolean")
    @Test
    public void hasPhase_returnsCorrectBoolean() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("has_test");
        assertTrue(def.hasPhase(0));
        assertFalse(def.hasPhase(1));
        assertFalse(def.hasPhase(-1));
    }

    @DisplayName("Given a definition with a known stage, when calling findStageDefinition, then it returns the stage")
    @Test
    public void findStageDefinition_returnsStage_whenKeyExists() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("find_stage");
        NamespacedKey stageKey = new NamespacedKey("mcrpg", "find_stage_stage");
        assertTrue(def.findStageDefinition(stageKey).isPresent());
    }

    @DisplayName("Given a definition, when calling findStageDefinition with unknown key, then it returns empty")
    @Test
    public void findStageDefinition_returnsEmpty_whenKeyNotFound() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("find_stage_missing");
        assertTrue(def.findStageDefinition(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @DisplayName("Given a definition with a known objective, when calling findObjectiveDefinition, then it returns the objective")
    @Test
    public void findObjectiveDefinition_returnsObjective_whenKeyExists() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("find_obj");
        NamespacedKey objKey = new NamespacedKey("mcrpg", "find_obj_obj");
        assertTrue(def.findObjectiveDefinition(objKey).isPresent());
    }

    @DisplayName("Given a definition, when calling findObjectiveDefinition with unknown key, then it returns empty")
    @Test
    public void findObjectiveDefinition_returnsEmpty_whenKeyNotFound() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("find_obj_missing");
        assertTrue(def.findObjectiveDefinition(new NamespacedKey("mcrpg", "nonexistent")).isEmpty());
    }

    @DisplayName("Given a definition with no expiration, when calling getExpiration, then it returns empty")
    @Test
    public void getExpiration_returnsEmpty_whenNull() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_expiry");
        assertTrue(def.getExpiration().isEmpty());
    }

    @DisplayName("Given a definition with an expiration, when calling getExpiration, then it returns the duration")
    @Test
    public void getExpiration_returnsDuration_whenSet() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("mcrpg", "expiry_test"),
                new NamespacedKey("mcrpg", "single_player"),
                Duration.ofHours(24),
                List.of(phase),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null
        );
        assertTrue(def.getExpiration().isPresent());
        assertEquals(Duration.ofHours(24), def.getExpiration().get());
    }

    @DisplayName("Given a definition with COOLDOWN repeat mode and a cooldown, when getting repeat fields, then they return correctly")
    @Test
    public void repeatFields_returnCorrectValues() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("mcrpg", "repeat_test"),
                new NamespacedKey("mcrpg", "single_player"),
                null,
                List.of(phase),
                List.of(),
                QuestRepeatMode.COOLDOWN,
                Duration.ofHours(1),
                -1,
                null
        );
        assertEquals(QuestRepeatMode.COOLDOWN, def.getRepeatMode());
        assertTrue(def.getRepeatCooldown().isPresent());
        assertEquals(Duration.ofHours(1), def.getRepeatCooldown().get());
        assertTrue(def.getRepeatLimit().isEmpty());
    }

    @DisplayName("Given a definition with LIMITED repeat mode, when getting repeatLimit, then it returns the limit")
    @Test
    public void getRepeatLimit_returnsLimit_whenSet() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("mcrpg", "limited_test"),
                new NamespacedKey("mcrpg", "single_player"),
                null,
                List.of(phase),
                List.of(),
                QuestRepeatMode.LIMITED,
                null,
                5,
                null
        );
        assertTrue(def.getRepeatLimit().isPresent());
        assertEquals(5, def.getRepeatLimit().getAsInt());
    }

    @DisplayName("Given a definition with no expansion key, when getting expansionKey, then it returns empty")
    @Test
    public void getExpansionKey_returnsEmpty_whenNull() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_expansion");
        assertTrue(def.getExpansionKey().isEmpty());
    }

    @DisplayName("Given a definition with an expansion key, when getting expansionKey, then it returns the key")
    @Test
    public void getExpansionKey_returnsKey_whenSet() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        NamespacedKey expansionKey = new NamespacedKey("mcrpg", "seasonal");
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("mcrpg", "expansion_test"),
                new NamespacedKey("mcrpg", "single_player"),
                null,
                List.of(phase),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                expansionKey
        );
        assertTrue(def.getExpansionKey().isPresent());
        assertEquals(expansionKey, def.getExpansionKey().get());
    }

    @DisplayName("Given a valid definition, when getting phaseCount, then it matches the number of phases")
    @Test
    public void getPhaseCount_matchesPhaseListSize() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("count_test");
        assertEquals(1, def.getPhaseCount());
    }

    @DisplayName("Given a valid definition, when getting displayNameRoute, then route follows quests.{namespace}.{key}.display-name pattern")
    @Test
    public void getDisplayNameRoute_returnsCorrectRoute() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("route_test");
        Route expected = Route.fromString("quests.mcrpg.route_test.display-name");
        assertEquals(expected, def.getDisplayNameRoute());
    }

    @DisplayName("Given a definition with a custom namespace, when getting displayNameRoute, then route includes the namespace")
    @Test
    public void getDisplayNameRoute_includesCustomNamespace() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        QuestDefinition def = new QuestDefinition(
                new NamespacedKey("custom_ns", "my_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                null,
                List.of(phase),
                List.of(),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null
        );
        Route expected = Route.fromString("quests.custom_ns.my_quest.display-name");
        assertEquals(expected, def.getDisplayNameRoute());
    }
}
