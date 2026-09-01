package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TemplateStageDefinitionTest {

    @Nested
    @DisplayName("Canonical constructor")
    class CanonicalConstructorTests {

        @Test
        @DisplayName("Given a mutable list, when constructing, then objectives are defensively copied")
        void constructor_defensivelyCopiesObjectives() {
            TemplateObjectiveDefinition objective = mock(TemplateObjectiveDefinition.class);
            ArrayList<TemplateObjectiveDefinition> mutableList = new ArrayList<>();
            mutableList.add(objective);

            TemplateStageDefinition stage = new TemplateStageDefinition(mutableList, null, null);
            mutableList.add(mock(TemplateObjectiveDefinition.class));

            assertEquals(1, stage.objectives().size());
        }

        @Test
        @DisplayName("Given objectives list, when accessing, then returned list is immutable")
        void constructor_returnsImmutableList() {
            TemplateObjectiveDefinition objective = mock(TemplateObjectiveDefinition.class);
            TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective), null, null);

            assertThrows(UnsupportedOperationException.class,
                    () -> stage.objectives().add(mock(TemplateObjectiveDefinition.class)));
        }
    }

    @Nested
    @DisplayName("Backward-compatible constructor")
    class BackwardCompatibleConstructorTests {

        @Test
        @DisplayName("Given only objectives, when using single-arg constructor, then condition and selection are null")
        void singleArgConstructor_setsNullDefaults() {
            TemplateObjectiveDefinition objective = mock(TemplateObjectiveDefinition.class);
            TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));

            assertTrue(stage.getCondition().isEmpty());
            assertTrue(stage.getObjectiveSelection().isEmpty());
        }
    }

    @Nested
    @DisplayName("getCondition")
    class GetConditionTests {

        @Test
        @DisplayName("Given null condition, when getCondition is called, then returns empty Optional")
        void getCondition_returnsEmpty_whenNull() {
            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(mock(TemplateObjectiveDefinition.class)), null, null);

            assertTrue(stage.getCondition().isEmpty());
        }

        @Test
        @DisplayName("Given non-null condition, when getCondition is called, then returns present Optional")
        void getCondition_returnsPresent_whenNonNull() {
            TemplateCondition condition = mock(TemplateCondition.class);
            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(mock(TemplateObjectiveDefinition.class)), condition, null);

            assertTrue(stage.getCondition().isPresent());
            assertEquals(condition, stage.getCondition().get());
        }
    }

    @Nested
    @DisplayName("getObjectiveSelection")
    class GetObjectiveSelectionTests {

        @Test
        @DisplayName("Given null selection config, when getObjectiveSelection is called, then returns empty Optional")
        void getObjectiveSelection_returnsEmpty_whenNull() {
            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(mock(TemplateObjectiveDefinition.class)), null, null);

            assertTrue(stage.getObjectiveSelection().isEmpty());
        }

        @Test
        @DisplayName("Given non-null selection config, when getObjectiveSelection is called, then returns present Optional")
        void getObjectiveSelection_returnsPresent_whenNonNull() {
            ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                    ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 1, 3);
            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(mock(TemplateObjectiveDefinition.class)), null, config);

            assertTrue(stage.getObjectiveSelection().isPresent());
            assertEquals(config, stage.getObjectiveSelection().get());
        }
    }

    @Nested
    @DisplayName("withObjectives")
    class WithObjectivesTests {

        @Test
        @DisplayName("Given a stage, when withObjectives is called, then returns new instance with updated objectives")
        void withObjectives_returnsNewInstance() {
            TemplateObjectiveDefinition original = mock(TemplateObjectiveDefinition.class);
            TemplateObjectiveDefinition replacement = mock(TemplateObjectiveDefinition.class);
            TemplateCondition condition = mock(TemplateCondition.class);
            ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                    ObjectiveSelectionConfig.ObjectiveSelectionMode.WEIGHTED_RANDOM, 1, 2);

            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(original), condition, config);
            TemplateStageDefinition result = stage.withObjectives(List.of(replacement));

            assertNotSame(stage, result);
            assertEquals(1, result.objectives().size());
            assertEquals(replacement, result.objectives().getFirst());
        }

        @Test
        @DisplayName("Given a stage with condition, when withObjectives is called, then condition is preserved")
        void withObjectives_preservesCondition() {
            TemplateCondition condition = mock(TemplateCondition.class);
            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(mock(TemplateObjectiveDefinition.class)), condition, null);

            TemplateStageDefinition result = stage.withObjectives(List.of(mock(TemplateObjectiveDefinition.class)));

            assertTrue(result.getCondition().isPresent());
            assertEquals(condition, result.getCondition().get());
        }

        @Test
        @DisplayName("Given a stage with selection config, when withObjectives is called, then selection is preserved")
        void withObjectives_preservesObjectiveSelection() {
            ObjectiveSelectionConfig config = new ObjectiveSelectionConfig(
                    ObjectiveSelectionConfig.ObjectiveSelectionMode.ALL, 1, 5);
            TemplateStageDefinition stage = new TemplateStageDefinition(
                    List.of(mock(TemplateObjectiveDefinition.class)), null, config);

            TemplateStageDefinition result = stage.withObjectives(List.of(mock(TemplateObjectiveDefinition.class)));

            assertTrue(result.getObjectiveSelection().isPresent());
            assertEquals(config, result.getObjectiveSelection().get());
        }
    }
}
