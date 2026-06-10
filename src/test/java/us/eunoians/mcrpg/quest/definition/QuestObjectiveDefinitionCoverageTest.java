package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestObjectiveDefinitionCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Expression-based constructor")
    class ExpressionConstructor {

        @DisplayName("blank expression throws IllegalArgumentException")
        @Test
        void constructor_throws_whenExpressionBlank() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestObjectiveDefinition(
                            new NamespacedKey("mcrpg", "obj"),
                            QuestTestHelper.mockObjectiveType("t"),
                            "   ",
                            List.of(),
                            null
                    ));
        }

        @DisplayName("empty expression throws IllegalArgumentException")
        @Test
        void constructor_throws_whenExpressionEmpty() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestObjectiveDefinition(
                            new NamespacedKey("mcrpg", "obj"),
                            QuestTestHelper.mockObjectiveType("t"),
                            "",
                            List.of(),
                            null
                    ));
        }

        @DisplayName("valid expression creates objective successfully")
        @Test
        void constructor_succeeds_whenExpressionValid() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "expr_obj"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "20*(tier^2)",
                    List.of(),
                    null
            );
            assertEquals(new NamespacedKey("mcrpg", "expr_obj"), def.getObjectiveKey());
        }
    }

    @Nested
    @DisplayName("getRequiredProgress (expression-based)")
    class GetRequiredProgressExpression {

        @DisplayName("throws IllegalStateException for expression-based objective")
        @Test
        void getRequiredProgress_throws_whenExpressionBased() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "expr_obj"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "20*(tier^2)",
                    List.of(),
                    null
            );
            IllegalStateException ex = assertThrows(IllegalStateException.class, def::getRequiredProgress);
            assertTrue(ex.getMessage().contains("expr_obj"));
            assertTrue(ex.getMessage().contains("resolveRequiredProgress"));
        }
    }

    @Nested
    @DisplayName("resolveRequiredProgress")
    class ResolveRequiredProgress {

        @DisplayName("static objective returns value directly")
        @Test
        void resolve_returnsStaticValue_whenNotExpressionBased() {
            QuestObjectiveDefinition def = QuestTestHelper.singleObjectiveDef("static_obj", 42);
            assertEquals(42, def.resolveRequiredProgress(Map.of()));
        }

        @DisplayName("expression with tier variable resolves correctly")
        @Test
        void resolve_evaluatesExpression_withTierVariable() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "expr_obj"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "10*tier",
                    List.of(),
                    null
            );
            assertEquals(30, def.resolveRequiredProgress(Map.of("tier", 3)));
        }

        @DisplayName("expression with missing tier variable throws")
        @Test
        void resolve_throws_whenTierVariableMissing() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "expr_obj"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "20*(tier^2)",
                    List.of(),
                    null
            );
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> def.resolveRequiredProgress(Map.of()));
            assertTrue(ex.getMessage().contains("tier"));
        }

        @DisplayName("non-Number values in variables are silently skipped")
        @Test
        void resolve_skipsNonNumberVariables() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "mixed_vars"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "10*tier",
                    List.of(),
                    null
            );
            assertEquals(50, def.resolveRequiredProgress(Map.of("tier", 5, "label", "text")));
        }

        @DisplayName("resolved progress <= 0 throws")
        @Test
        void resolve_throws_whenResolvedNotPositive() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "zero_obj"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "tier-tier",
                    List.of(),
                    null
            );
            assertThrows(IllegalArgumentException.class,
                    () -> def.resolveRequiredProgress(Map.of("tier", 5)));
        }

        @DisplayName("expression without tier token does not require tier variable")
        @Test
        void resolve_doesNotRequireTier_whenNoTierToken() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "no_tier"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "50+10",
                    List.of(),
                    null
            );
            assertEquals(60, def.resolveRequiredProgress(Map.of()));
        }

        @DisplayName("float variable values are accepted as Number")
        @Test
        void resolve_acceptsFloatVariables() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "float_obj"),
                    QuestTestHelper.mockObjectiveType("t"),
                    "tier*10",
                    List.of(),
                    null
            );
            assertEquals(25, def.resolveRequiredProgress(Map.of("tier", 2.5)));
        }
    }

    @Nested
    @DisplayName("rewardDistribution")
    class RewardDistributionTest {

        @DisplayName("returns empty when null")
        @Test
        void getRewardDistribution_returnsEmpty() {
            QuestObjectiveDefinition def = QuestTestHelper.singleObjectiveDef("no_dist", 10);
            assertEquals(Optional.empty(), def.getRewardDistribution());
        }
    }

    @Nested
    @DisplayName("getObjectiveType")
    class GetObjectiveType {

        @DisplayName("returns the configured type")
        @Test
        void getObjectiveType_returnsConfiguredType() {
            var type = QuestTestHelper.mockObjectiveType("custom_type");
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "typed_obj"),
                    type,
                    10,
                    List.of(),
                    null
            );
            assertEquals(type, def.getObjectiveType());
        }
    }
}
