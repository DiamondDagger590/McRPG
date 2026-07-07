package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TemplateObjectiveDefinitionTest extends McRPGBaseTest {

    private static final NamespacedKey BLOCK_BREAK_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "block_break");

    @Nested
    @DisplayName("Canonical constructor")
    class CanonicalConstructor {

        @Test
        @DisplayName("Stores all provided values")
        void storesAllProvidedValues() {
            Map<String, Object> config = Map.of("blocks", "stone");
            TemplateCondition condition = mock(TemplateCondition.class);

            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "mine_stone", BLOCK_BREAK_KEY, "block_count", config, condition, 5);

            assertEquals("mine_stone", def.label());
            assertEquals(BLOCK_BREAK_KEY, def.typeKey());
            assertEquals("block_count", def.requiredProgressExpression());
            assertEquals(condition, def.condition());
            assertEquals(5, def.weight());
        }

        @Test
        @DisplayName("Weight zero is clamped to one")
        void weightZero_clampedToOne() {
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), null, 0);
            assertEquals(1, def.weight());
        }

        @Test
        @DisplayName("Negative weight is clamped to one")
        void negativeWeight_clampedToOne() {
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), null, -5);
            assertEquals(1, def.weight());
        }

        @Test
        @DisplayName("Weight one is preserved")
        void weightOne_preserved() {
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), null, 1);
            assertEquals(1, def.weight());
        }

        @Test
        @DisplayName("Large positive weight is preserved")
        void largeWeight_preserved() {
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), null, 100);
            assertEquals(100, def.weight());
        }

        @Test
        @DisplayName("Config map is made immutable")
        void configMapImmutable() {
            Map<String, Object> mutableConfig = new HashMap<>();
            mutableConfig.put("blocks", "stone");

            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", mutableConfig, null, 1);

            assertThrows(UnsupportedOperationException.class,
                    () -> def.config().put("new_key", "value"));
        }

        @Test
        @DisplayName("Mutations to original map do not affect the record")
        void originalMapMutationDoesNotAffectRecord() {
            Map<String, Object> mutableConfig = new HashMap<>();
            mutableConfig.put("blocks", "stone");

            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", mutableConfig, null, 1);

            mutableConfig.put("blocks", "dirt");
            assertEquals("stone", def.config().get("blocks"));
        }
    }

    @Nested
    @DisplayName("Backward-compatible constructor")
    class BackwardCompatibleConstructor {

        @Test
        @DisplayName("Sets empty label, null condition, weight 1")
        void setsDefaults() {
            Map<String, Object> config = Map.of("blocks", "stone");
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    BLOCK_BREAK_KEY, "50", config);

            assertEquals("", def.label());
            assertEquals(BLOCK_BREAK_KEY, def.typeKey());
            assertEquals("50", def.requiredProgressExpression());
            assertFalse(def.getCondition().isPresent());
            assertEquals(1, def.weight());
        }
    }

    @Nested
    @DisplayName("getCondition")
    class GetCondition {

        @Test
        @DisplayName("Returns empty when condition is null")
        void returnsEmpty_whenNull() {
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), null, 1);
            assertTrue(def.getCondition().isEmpty());
        }

        @Test
        @DisplayName("Returns present when condition exists")
        void returnsPresent_whenConditionExists() {
            TemplateCondition condition = mock(TemplateCondition.class);
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), condition, 1);
            assertTrue(def.getCondition().isPresent());
            assertEquals(condition, def.getCondition().get());
        }
    }

    @Nested
    @DisplayName("getWeight")
    class GetWeight {

        @Test
        @DisplayName("Returns the stored weight value")
        void returnsStoredWeight() {
            TemplateObjectiveDefinition def = new TemplateObjectiveDefinition(
                    "label", BLOCK_BREAK_KEY, "10", Map.of(), null, 7);
            assertEquals(7, def.getWeight());
        }
    }
}
