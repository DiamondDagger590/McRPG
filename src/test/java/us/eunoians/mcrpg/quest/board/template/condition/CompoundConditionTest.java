package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("CompoundCondition")
class CompoundConditionTest {

    private TemplateCondition alwaysTrue() {
        TemplateCondition m = mock(TemplateCondition.class);
        when(m.evaluate(any())).thenReturn(true);
        when(m.getKey()).thenReturn(NamespacedKey.fromString("mcrpg:stub_true"));
        when(m.serializeConfig()).thenReturn(Map.of());
        return m;
    }

    private TemplateCondition alwaysFalse() {
        TemplateCondition m = mock(TemplateCondition.class);
        when(m.evaluate(any())).thenReturn(false);
        when(m.getKey()).thenReturn(NamespacedKey.fromString("mcrpg:stub_false"));
        when(m.serializeConfig()).thenReturn(Map.of());
        return m;
    }

    @Nested
    @DisplayName("constructor validation")
    class ConstructorValidation {

        @Test
        @DisplayName("rejects empty conditions map")
        void constructor_throws_whenConditionsEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> new CompoundCondition(Map.of(), CompoundCondition.LogicMode.ALL));
        }

        @Test
        @DisplayName("no-arg prototype creates empty ALL compound")
        void noArgConstructor_createsPrototype() {
            CompoundCondition prototype = new CompoundCondition();
            assertEquals(CompoundCondition.LogicMode.ALL, prototype.getMode());
            assertTrue(prototype.getConditions().isEmpty());
        }
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getKey returns mcrpg:compound")
        void getKey_returnsMcrpgCompound() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue()), CompoundCondition.LogicMode.ALL);
            assertEquals(NamespacedKey.fromString("mcrpg:compound"), condition.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue()), CompoundCondition.LogicMode.ANY);
            Optional<NamespacedKey> key = condition.getExpansionKey();
            assertTrue(key.isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, key.get());
        }

        @Test
        @DisplayName("getMode returns ALL when constructed with ALL")
        void getMode_returnsALL() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue()), CompoundCondition.LogicMode.ALL);
            assertEquals(CompoundCondition.LogicMode.ALL, condition.getMode());
        }

        @Test
        @DisplayName("getMode returns ANY when constructed with ANY")
        void getMode_returnsANY() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue()), CompoundCondition.LogicMode.ANY);
            assertEquals(CompoundCondition.LogicMode.ANY, condition.getMode());
        }

        @Test
        @DisplayName("getConditions returns unmodifiable map")
        void getConditions_returnsUnmodifiableMap() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue()), CompoundCondition.LogicMode.ALL);
            assertThrows(UnsupportedOperationException.class,
                    () -> condition.getConditions().put("b", alwaysFalse()));
        }

        @Test
        @DisplayName("getConditions preserves entries")
        void getConditions_preservesEntries() {
            TemplateCondition child = alwaysTrue();
            CompoundCondition condition = new CompoundCondition(
                    Map.of("label", child), CompoundCondition.LogicMode.ALL);
            assertEquals(1, condition.getConditions().size());
            assertTrue(condition.getConditions().containsKey("label"));
        }
    }

    @Nested
    @DisplayName("evaluate edge cases")
    class EvaluateEdgeCases {

        @Test
        @DisplayName("ALL with single true child returns true")
        void all_singleTrueChild_returnsTrue() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("only", alwaysTrue()), CompoundCondition.LogicMode.ALL);
            assertTrue(condition.evaluate(new ConditionContext(null, null, null, null, null, null)));
        }

        @Test
        @DisplayName("ANY with single false child returns false")
        void any_singleFalseChild_returnsFalse() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("only", alwaysFalse()), CompoundCondition.LogicMode.ANY);
            assertFalse(condition.evaluate(new ConditionContext(null, null, null, null, null, null)));
        }

        @Test
        @DisplayName("ALL with all false children returns false")
        void all_allFalseChildren_returnsFalse() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysFalse(), "b", alwaysFalse()),
                    CompoundCondition.LogicMode.ALL);
            assertFalse(condition.evaluate(new ConditionContext(null, null, null, null, null, null)));
        }

        @Test
        @DisplayName("ANY with all true children returns true")
        void any_allTrueChildren_returnsTrue() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue(), "b", alwaysTrue()),
                    CompoundCondition.LogicMode.ANY);
            assertTrue(condition.evaluate(new ConditionContext(null, null, null, null, null, null)));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("ALL mode serializes under 'all' key")
        void serializeConfig_allMode_usesAllKey() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("child", alwaysTrue()), CompoundCondition.LogicMode.ALL);
            Map<String, Object> config = condition.serializeConfig();
            assertTrue(config.containsKey("all"));
            assertFalse(config.containsKey("any"));
        }

        @Test
        @DisplayName("ANY mode serializes under 'any' key")
        void serializeConfig_anyMode_usesAnyKey() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("child", alwaysTrue()), CompoundCondition.LogicMode.ANY);
            Map<String, Object> config = condition.serializeConfig();
            assertTrue(config.containsKey("any"));
            assertFalse(config.containsKey("all"));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("child entries include type key")
        void serializeConfig_childEntriesIncludeTypeKey() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("label", alwaysTrue()), CompoundCondition.LogicMode.ALL);
            Map<String, Object> config = condition.serializeConfig();
            Map<String, Object> children = (Map<String, Object>) config.get("all");
            Map<String, Object> child = (Map<String, Object>) children.get("label");
            assertEquals("mcrpg:stub_true", child.get("type"));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("multiple children all serialized")
        void serializeConfig_multipleChildren() {
            CompoundCondition condition = new CompoundCondition(
                    Map.of("a", alwaysTrue(), "b", alwaysFalse()),
                    CompoundCondition.LogicMode.ALL);
            Map<String, Object> config = condition.serializeConfig();
            Map<String, Object> children = (Map<String, Object>) config.get("all");
            assertEquals(2, children.size());
        }
    }
}
