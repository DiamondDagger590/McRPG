package us.eunoians.mcrpg.quest.board.template;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.ObjectiveSelectionConfig.ObjectiveSelectionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectiveSelectionConfig")
public class ObjectiveSelectionConfigTest {

    @DisplayName("Valid config stores mode, minCount, and maxCount")
    @Test
    void constructor_validValues_storesFields() {
        var config = new ObjectiveSelectionConfig(ObjectiveSelectionMode.WEIGHTED_RANDOM, 2, 5);
        assertEquals(ObjectiveSelectionMode.WEIGHTED_RANDOM, config.mode());
        assertEquals(2, config.minCount());
        assertEquals(5, config.maxCount());
    }

    @DisplayName("ALL mode accepted with valid counts")
    @Test
    void constructor_allMode_storesCorrectly() {
        var config = new ObjectiveSelectionConfig(ObjectiveSelectionMode.ALL, 1, 10);
        assertEquals(ObjectiveSelectionMode.ALL, config.mode());
        assertEquals(1, config.minCount());
        assertEquals(10, config.maxCount());
    }

    @DisplayName("minCount of zero throws IllegalArgumentException")
    @Test
    void constructor_minCountZero_throws() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ObjectiveSelectionConfig(ObjectiveSelectionMode.ALL, 0, 5));
        assertEquals("minCount must be >= 1, got: 0", exception.getMessage());
    }

    @DisplayName("Negative minCount throws IllegalArgumentException")
    @Test
    void constructor_negativeMinCount_throws() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ObjectiveSelectionConfig(ObjectiveSelectionMode.WEIGHTED_RANDOM, -3, 5));
        assertEquals("minCount must be >= 1, got: -3", exception.getMessage());
    }

    @DisplayName("maxCount less than minCount throws IllegalArgumentException")
    @Test
    void constructor_maxCountLessThanMinCount_throws() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ObjectiveSelectionConfig(ObjectiveSelectionMode.WEIGHTED_RANDOM, 5, 3));
        assertEquals("maxCount must be >= minCount, got: max=3, min=5", exception.getMessage());
    }

    @DisplayName("minCount equals maxCount is accepted")
    @Test
    void constructor_minEqualsMax_accepted() {
        var config = new ObjectiveSelectionConfig(ObjectiveSelectionMode.WEIGHTED_RANDOM, 3, 3);
        assertEquals(3, config.minCount());
        assertEquals(3, config.maxCount());
    }

    @DisplayName("Boundary: minCount 1, maxCount 1 is accepted")
    @Test
    void constructor_singleObjective_accepted() {
        var config = new ObjectiveSelectionConfig(ObjectiveSelectionMode.ALL, 1, 1);
        assertEquals(1, config.minCount());
        assertEquals(1, config.maxCount());
    }

    @DisplayName("ObjectiveSelectionMode has exactly two values")
    @Test
    void objectiveSelectionMode_hasTwoValues() {
        assertEquals(2, ObjectiveSelectionMode.values().length);
        assertEquals(ObjectiveSelectionMode.ALL, ObjectiveSelectionMode.valueOf("ALL"));
        assertEquals(ObjectiveSelectionMode.WEIGHTED_RANDOM, ObjectiveSelectionMode.valueOf("WEIGHTED_RANDOM"));
    }
}
