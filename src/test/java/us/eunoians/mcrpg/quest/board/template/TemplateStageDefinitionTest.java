package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.template.ObjectiveSelectionConfig.ObjectiveSelectionMode;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("TemplateStageDefinition")
public class TemplateStageDefinitionTest extends McRPGBaseTest {

    @DisplayName("Canonical constructor makes objectives list immutable")
    @Test
    void constructor_objectivesList_isImmutable() {
        TemplateObjectiveDefinition objective = createObjective("test_obj");
        var stage = new TemplateStageDefinition(List.of(objective), null, null);

        assertThrows(UnsupportedOperationException.class,
                () -> stage.objectives().add(createObjective("another")));
    }

    @DisplayName("Mutations to original list do not affect the record")
    @Test
    void constructor_defensiveCopy_originalUnaffected() {
        TemplateObjectiveDefinition objective = createObjective("test_obj");
        List<TemplateObjectiveDefinition> mutableList = new ArrayList<>();
        mutableList.add(objective);

        var stage = new TemplateStageDefinition(mutableList, null, null);
        mutableList.add(createObjective("sneaky"));

        assertEquals(1, stage.objectives().size());
    }

    @DisplayName("getCondition() returns empty when condition is null")
    @Test
    void getCondition_nullCondition_returnsEmpty() {
        var stage = new TemplateStageDefinition(List.of(createObjective("obj")), null, null);
        assertTrue(stage.getCondition().isEmpty());
    }

    @DisplayName("getCondition() returns present Optional when condition is non-null")
    @Test
    void getCondition_nonNullCondition_returnsPresent() {
        TemplateCondition mockCondition = mock(TemplateCondition.class);
        var stage = new TemplateStageDefinition(List.of(createObjective("obj")), mockCondition, null);

        assertTrue(stage.getCondition().isPresent());
        assertEquals(mockCondition, stage.getCondition().orElseThrow());
    }

    @DisplayName("getObjectiveSelection() returns empty when selection config is null")
    @Test
    void getObjectiveSelection_null_returnsEmpty() {
        var stage = new TemplateStageDefinition(List.of(createObjective("obj")), null, null);
        assertTrue(stage.getObjectiveSelection().isEmpty());
    }

    @DisplayName("getObjectiveSelection() returns present Optional when config is non-null")
    @Test
    void getObjectiveSelection_nonNull_returnsPresent() {
        var selection = new ObjectiveSelectionConfig(ObjectiveSelectionMode.WEIGHTED_RANDOM, 1, 3);
        var stage = new TemplateStageDefinition(List.of(createObjective("obj")), null, selection);

        assertTrue(stage.getObjectiveSelection().isPresent());
        assertEquals(selection, stage.getObjectiveSelection().orElseThrow());
    }

    @DisplayName("withObjectives returns new instance with updated objectives, preserving condition")
    @Test
    void withObjectives_preservesCondition() {
        TemplateCondition mockCondition = mock(TemplateCondition.class);
        var selection = new ObjectiveSelectionConfig(ObjectiveSelectionMode.ALL, 1, 2);
        TemplateObjectiveDefinition original = createObjective("original");
        var stage = new TemplateStageDefinition(List.of(original), mockCondition, selection);

        TemplateObjectiveDefinition replacement = createObjective("replacement");
        TemplateStageDefinition updated = stage.withObjectives(List.of(replacement));

        assertNotSame(stage, updated);
        assertEquals(1, updated.objectives().size());
        assertEquals(replacement, updated.objectives().get(0));
        assertEquals(mockCondition, updated.getCondition().orElseThrow());
        assertEquals(selection, updated.getObjectiveSelection().orElseThrow());
    }

    @DisplayName("withObjectives on null condition and selection preserves nulls")
    @Test
    void withObjectives_nullFields_preservesNulls() {
        var stage = new TemplateStageDefinition(List.of(createObjective("a")));
        TemplateStageDefinition updated = stage.withObjectives(List.of(createObjective("b")));

        assertTrue(updated.getCondition().isEmpty());
        assertTrue(updated.getObjectiveSelection().isEmpty());
    }

    @DisplayName("Backward-compatible constructor sets null condition and null objectiveSelection")
    @Test
    void backwardCompatibleConstructor_setsNullFields() {
        var stage = new TemplateStageDefinition(List.of(createObjective("obj")));

        assertTrue(stage.getCondition().isEmpty());
        assertTrue(stage.getObjectiveSelection().isEmpty());
        assertEquals(1, stage.objectives().size());
    }

    /**
     * @param label the objective label
     * @return a minimal objective definition for testing
     */
    private TemplateObjectiveDefinition createObjective(String label) {
        NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), label);
        return new TemplateObjectiveDefinition(label, key, "10", Map.of(), null, 1);
    }
}
