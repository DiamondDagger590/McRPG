package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CompletionPrerequisiteCondition")
class CompletionPrerequisiteConditionTest {

    private static final NamespacedKey DAILY = NamespacedKey.fromString("mcrpg:personal_daily");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");

    @Nested
    @DisplayName("constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("rejects minCompletions below 1")
        void constructor_throws_whenMinCompletionsZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CompletionPrerequisiteCondition(0, null, null));
        }

        @Test
        @DisplayName("rejects negative minCompletions")
        void constructor_throws_whenMinCompletionsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CompletionPrerequisiteCondition(-1, null, null));
        }

        @Test
        @DisplayName("accepts boundary value 1")
        void constructor_accepts_minimumOneCompletion() {
            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(1, null, null);
            assertEquals(1, condition.getMinCompletions());
        }

        @Test
        @DisplayName("no-arg prototype has minCompletions of 1")
        void noArgConstructor_hasDefaultValues() {
            CompletionPrerequisiteCondition prototype = new CompletionPrerequisiteCondition();
            assertEquals(1, prototype.getMinCompletions());
            assertTrue(prototype.getCategoryKey().isEmpty());
            assertTrue(prototype.getMinRarity().isEmpty());
        }
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getKey returns mcrpg:completion_prerequisite")
        void getKey_returnsMcrpgCompletionPrerequisite() {
            assertEquals(NamespacedKey.fromString("mcrpg:completion_prerequisite"),
                    new CompletionPrerequisiteCondition(5, null, null).getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            Optional<NamespacedKey> key = new CompletionPrerequisiteCondition(5, null, null).getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getMinCompletions returns configured value")
        void getMinCompletions_returnsConfiguredValue() {
            assertEquals(10, new CompletionPrerequisiteCondition(10, null, null).getMinCompletions());
        }

        @Test
        @DisplayName("getCategoryKey returns empty when null")
        void getCategoryKey_returnsEmpty_whenNull() {
            assertTrue(new CompletionPrerequisiteCondition(5, null, null).getCategoryKey().isEmpty());
        }

        @Test
        @DisplayName("getCategoryKey returns present when set")
        void getCategoryKey_returnsPresent_whenSet() {
            Optional<NamespacedKey> key = new CompletionPrerequisiteCondition(5, DAILY, null).getCategoryKey();
            assertTrue(key.isPresent());
            assertEquals(DAILY, key.get());
        }

        @Test
        @DisplayName("getMinRarity returns empty when null")
        void getMinRarity_returnsEmpty_whenNull() {
            assertTrue(new CompletionPrerequisiteCondition(5, null, null).getMinRarity().isEmpty());
        }

        @Test
        @DisplayName("getMinRarity returns present when set")
        void getMinRarity_returnsPresent_whenSet() {
            Optional<NamespacedKey> key = new CompletionPrerequisiteCondition(5, null, RARE).getMinRarity();
            assertTrue(key.isPresent());
            assertEquals(RARE, key.get());
        }
    }

    @Nested
    @DisplayName("evaluate edge cases")
    class EvaluateEdgeCases {

        @Test
        @DisplayName("exact threshold count passes")
        void evaluate_returnsTrue_whenCountEqualsThreshold() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(5);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("one below threshold fails")
        void evaluate_returnsFalse_whenOneBelow() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(4);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertFalse(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("with both category and rarity filters")
        void evaluate_usesBothFilters() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            when(history.countCompletedQuests(playerUUID, DAILY, RARE)).thenReturn(3);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(3, DAILY, RARE);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertTrue(condition.evaluate(ctx));
        }

        @Test
        @DisplayName("zero completions with threshold of 1 fails")
        void evaluate_zeroCompletions_fails() {
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            UUID playerUUID = UUID.randomUUID();
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(0);

            CompletionPrerequisiteCondition condition = new CompletionPrerequisiteCondition(1, null, null);
            ConditionContext ctx = new ConditionContext(null, null, null, null, playerUUID, history);
            assertFalse(condition.evaluate(ctx));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("serializes min-completions only when no filters")
        void serializeConfig_noFilters_onlyMinCompletions() {
            Map<String, Object> config = new CompletionPrerequisiteCondition(5, null, null).serializeConfig();
            assertEquals(5, config.get("min-completions"));
            assertEquals(1, config.size());
        }

        @Test
        @DisplayName("serializes category when present")
        void serializeConfig_withCategory() {
            Map<String, Object> config = new CompletionPrerequisiteCondition(5, DAILY, null).serializeConfig();
            assertEquals(5, config.get("min-completions"));
            assertEquals("mcrpg:personal_daily", config.get("category"));
            assertEquals(2, config.size());
        }

        @Test
        @DisplayName("serializes min-rarity when present")
        void serializeConfig_withMinRarity() {
            Map<String, Object> config = new CompletionPrerequisiteCondition(5, null, RARE).serializeConfig();
            assertEquals(5, config.get("min-completions"));
            assertEquals("mcrpg:rare", config.get("min-rarity"));
            assertEquals(2, config.size());
        }

        @Test
        @DisplayName("serializes all fields when both filters present")
        void serializeConfig_withBothFilters() {
            Map<String, Object> config = new CompletionPrerequisiteCondition(10, DAILY, RARE).serializeConfig();
            assertEquals(10, config.get("min-completions"));
            assertEquals("mcrpg:personal_daily", config.get("category"));
            assertEquals("mcrpg:rare", config.get("min-rarity"));
            assertEquals(3, config.size());
        }
    }
}
