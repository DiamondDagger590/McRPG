package us.eunoians.mcrpg.quest.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuestPhaseDefinitionTest extends McRPGBaseTest {

    @DisplayName("Given a negative phaseIndex, when constructing, then it throws IllegalArgumentException")
    @Test
    public void constructor_throwsIllegalArgumentException_whenPhaseIndexNegative() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        assertThrows(IllegalArgumentException.class,
                () -> new QuestPhaseDefinition(-1, PhaseCompletionMode.ALL, List.of(stage)));
    }

    @DisplayName("Given an empty stages list, when constructing, then it throws IllegalArgumentException")
    @Test
    public void constructor_throwsIllegalArgumentException_whenStagesEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of()));
    }

    @DisplayName("Given a valid phase, when getting stages, then the returned list is immutable")
    @Test
    public void getStages_returnsImmutableList() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of(stage));
        assertThrows(UnsupportedOperationException.class, () -> phase.getStages().add(null));
    }

    @DisplayName("Given a valid phase, when getting fields, then they match constructor args")
    @Test
    public void getters_returnConstructorValues() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        QuestPhaseDefinition phase = new QuestPhaseDefinition(2, PhaseCompletionMode.ANY, List.of(stage));
        assertEquals(2, phase.getPhaseIndex());
        assertEquals(PhaseCompletionMode.ANY, phase.getCompletionMode());
        assertEquals(1, phase.getStages().size());
    }
}
