package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("TemplatePhaseDefinition")
public class TemplatePhaseDefinitionTest {

    @DisplayName("Canonical constructor makes stages list immutable")
    @Test
    void constructor_stagesList_isImmutable() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(createStage()), null);

        assertThrows(UnsupportedOperationException.class,
                () -> phase.stages().add(createStage()));
    }

    @DisplayName("Mutations to original list do not affect the record")
    @Test
    void constructor_defensiveCopy_originalUnaffected() {
        List<TemplateStageDefinition> mutableList = new ArrayList<>();
        mutableList.add(createStage());

        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, mutableList, null);
        mutableList.add(createStage());

        assertEquals(1, phase.stages().size());
    }

    @DisplayName("getCondition() returns empty when condition is null")
    @Test
    void getCondition_nullCondition_returnsEmpty() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(createStage()), null);
        assertTrue(phase.getCondition().isEmpty());
    }

    @DisplayName("getCondition() returns present Optional when condition is non-null")
    @Test
    void getCondition_nonNullCondition_returnsPresent() {
        TemplateCondition mockCondition = mock(TemplateCondition.class);
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ANY, List.of(createStage()), mockCondition);

        assertTrue(phase.getCondition().isPresent());
        assertEquals(mockCondition, phase.getCondition().orElseThrow());
    }

    @DisplayName("Completion mode is stored correctly for ALL")
    @Test
    void constructor_allMode_storedCorrectly() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(createStage()), null);
        assertEquals(PhaseCompletionMode.ALL, phase.completionMode());
    }

    @DisplayName("Completion mode is stored correctly for ANY")
    @Test
    void constructor_anyMode_storedCorrectly() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ANY, List.of(createStage()), null);
        assertEquals(PhaseCompletionMode.ANY, phase.completionMode());
    }

    @DisplayName("withStages returns new instance with updated stages, preserving completionMode and condition")
    @Test
    void withStages_preservesOtherFields() {
        TemplateCondition mockCondition = mock(TemplateCondition.class);
        TemplateStageDefinition originalStage = createStage();
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ANY, List.of(originalStage), mockCondition);

        TemplateStageDefinition replacementStage = createStage();
        TemplatePhaseDefinition updated = phase.withStages(List.of(replacementStage));

        assertNotSame(phase, updated);
        assertEquals(1, updated.stages().size());
        assertEquals(replacementStage, updated.stages().get(0));
        assertEquals(PhaseCompletionMode.ANY, updated.completionMode());
        assertEquals(mockCondition, updated.getCondition().orElseThrow());
    }

    @DisplayName("withStages on null condition preserves null")
    @Test
    void withStages_nullCondition_preservesNull() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(createStage()));
        TemplatePhaseDefinition updated = phase.withStages(List.of(createStage()));

        assertTrue(updated.getCondition().isEmpty());
        assertEquals(PhaseCompletionMode.ALL, updated.completionMode());
    }

    @DisplayName("Backward-compatible constructor sets null condition")
    @Test
    void backwardCompatibleConstructor_setsNullCondition() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(createStage()));

        assertTrue(phase.getCondition().isEmpty());
        assertEquals(PhaseCompletionMode.ALL, phase.completionMode());
        assertEquals(1, phase.stages().size());
    }

    @DisplayName("Multiple stages are preserved in order")
    @Test
    void constructor_multipleStages_preservesOrder() {
        TemplateStageDefinition stage1 = createStage();
        TemplateStageDefinition stage2 = createStage();
        TemplateStageDefinition stage3 = createStage();

        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL,
                List.of(stage1, stage2, stage3), null);

        assertEquals(3, phase.stages().size());
        assertEquals(stage1, phase.stages().get(0));
        assertEquals(stage2, phase.stages().get(1));
        assertEquals(stage3, phase.stages().get(2));
    }

    @DisplayName("Empty stages list is accepted")
    @Test
    void constructor_emptyStagesList_accepted() {
        var phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(), null);
        assertTrue(phase.stages().isEmpty());
    }

    /**
     * @return a minimal stage definition for testing
     */
    private TemplateStageDefinition createStage() {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "test_obj");
        TemplateObjectiveDefinition objective =
                new TemplateObjectiveDefinition("test", key, "10", Map.of(), null, 1);
        return new TemplateStageDefinition(List.of(objective));
    }
}
