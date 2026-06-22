package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.objective.type.MockQuestObjectiveType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("QuestObjectiveDefinition expression and distribution coverage")
class QuestObjectiveDefinitionExpressionTest extends McRPGBaseTest {

    private static final NamespacedKey OBJ_KEY = new NamespacedKey("mcrpg", "test_obj");

    private MockQuestObjectiveType mockType() {
        return QuestTestHelper.mockObjectiveType("test_type");
    }

    @Nested
    @DisplayName("Expression-based constructor")
    class ExpressionConstructor {

        @Test
        @DisplayName("blank expression throws IllegalArgumentException")
        void blankExpression_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestObjectiveDefinition(OBJ_KEY, mockType(), "   ", List.of(), null));
        }

        @Test
        @DisplayName("empty expression throws IllegalArgumentException")
        void emptyExpression_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestObjectiveDefinition(OBJ_KEY, mockType(), "", List.of(), null));
        }

        @Test
        @DisplayName("valid expression stores correctly")
        void validExpression_storesCorrectly() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "10*tier", List.of(), null);

            assertEquals(OBJ_KEY, def.getObjectiveKey());
        }

        @Test
        @DisplayName("getRequiredProgress on expression-based throws IllegalStateException")
        void getRequiredProgress_onExpressionBased_throwsIllegalStateException() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "10*tier", List.of(), null);

            assertThrows(IllegalStateException.class, def::getRequiredProgress);
        }
    }

    @Nested
    @DisplayName("resolveRequiredProgress")
    class ResolveRequiredProgress {

        @Test
        @DisplayName("static progress returns value without variables")
        void staticProgress_returnsValueWithoutVariables() {
            QuestObjectiveDefinition def = QuestTestHelper.singleObjectiveDef("obj", 50);
            assertEquals(50, def.resolveRequiredProgress(Map.of()));
        }

        @Test
        @DisplayName("expression with tier variable resolves correctly")
        void expressionWithTier_resolvesCorrectly() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "10*tier", List.of(), null);

            assertEquals(30, def.resolveRequiredProgress(Map.of("tier", 3)));
        }

        @Test
        @DisplayName("expression with missing tier variable throws IllegalArgumentException")
        void expressionMissingTier_throwsIllegalArgumentException() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "10*tier", List.of(), null);

            assertThrows(IllegalArgumentException.class, () -> def.resolveRequiredProgress(Map.of()));
        }

        @Test
        @DisplayName("expression resolving to zero throws IllegalArgumentException")
        void expressionResolvingToZero_throwsIllegalArgumentException() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "0*tier", List.of(), null);

            assertThrows(IllegalArgumentException.class,
                    () -> def.resolveRequiredProgress(Map.of("tier", 1)));
        }

        @Test
        @DisplayName("expression resolving to negative throws IllegalArgumentException")
        void expressionResolvingToNegative_throwsIllegalArgumentException() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "0-tier", List.of(), null);

            assertThrows(IllegalArgumentException.class,
                    () -> def.resolveRequiredProgress(Map.of("tier", 5)));
        }

        @Test
        @DisplayName("expression without tier token resolves without requiring tier variable")
        void expressionWithoutTier_resolvesWithoutTierVariable() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "100+50", List.of(), null);

            assertEquals(150, def.resolveRequiredProgress(Map.of()));
        }
    }

    @Nested
    @DisplayName("Static constructor validation")
    class StaticConstructorValidation {

        @Test
        @DisplayName("zero requiredProgress throws IllegalArgumentException")
        void zeroRequired_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestObjectiveDefinition(OBJ_KEY, mockType(), 0, List.of(), null));
        }

        @Test
        @DisplayName("negative requiredProgress throws IllegalArgumentException")
        void negativeRequired_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class,
                    () -> new QuestObjectiveDefinition(OBJ_KEY, mockType(), -5, List.of(), null));
        }
    }

    @Nested
    @DisplayName("Reward distribution")
    class RewardDistributionTests {

        @Test
        @DisplayName("getRewardDistribution returns empty when null")
        void getRewardDistribution_returnsEmpty_whenNull() {
            QuestObjectiveDefinition def = QuestTestHelper.singleObjectiveDef("obj", 10);
            assertTrue(def.getRewardDistribution().isEmpty());
        }

        @Test
        @DisplayName("getRewardDistribution returns present when provided (static constructor)")
        void getRewardDistribution_returnsPresent_staticConstructor() {
            RewardDistributionConfig config = new RewardDistributionConfig(List.of());
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), 10, List.of(), config);

            assertTrue(def.getRewardDistribution().isPresent());
            assertSame(config, def.getRewardDistribution().get());
        }

        @Test
        @DisplayName("getRewardDistribution returns present when provided (expression constructor)")
        void getRewardDistribution_returnsPresent_expressionConstructor() {
            RewardDistributionConfig config = new RewardDistributionConfig(List.of());
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "100", List.of(), config);

            assertTrue(def.getRewardDistribution().isPresent());
            assertSame(config, def.getRewardDistribution().get());
        }
    }

    @Nested
    @DisplayName("Description route")
    class DescriptionRouteTests {

        @Test
        @DisplayName("getDescriptionRoute returns non-null route")
        void getDescriptionRoute_returnsNonNullRoute() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "gather_stone"),
                    mockType(), 50, List.of(), null);

            NamespacedKey questKey = new NamespacedKey("mcrpg", "daily_mining");
            var route = def.getDescriptionRoute(questKey);
            assertNotNull(route);
        }

        @Test
        @DisplayName("getDescriptionRoute differs for different quest keys")
        void getDescriptionRoute_differsForDifferentQuestKeys() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    new NamespacedKey("mcrpg", "gather_stone"),
                    mockType(), 50, List.of(), null);

            var route1 = def.getDescriptionRoute(new NamespacedKey("mcrpg", "quest_a"));
            var route2 = def.getDescriptionRoute(new NamespacedKey("mcrpg", "quest_b"));
            assertNotEquals(route1, route2);
        }
    }

    @Nested
    @DisplayName("Rewards immutability")
    class RewardsImmutability {

        @Test
        @DisplayName("getRewards returns immutable list (static constructor)")
        void getRewards_immutable_staticConstructor() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), 10, List.of(), null);
            assertThrows(UnsupportedOperationException.class, () -> def.getRewards().add(null));
        }

        @Test
        @DisplayName("getRewards returns immutable list (expression constructor)")
        void getRewards_immutable_expressionConstructor() {
            QuestObjectiveDefinition def = new QuestObjectiveDefinition(
                    OBJ_KEY, mockType(), "50", List.of(), null);
            assertThrows(UnsupportedOperationException.class, () -> def.getRewards().add(null));
        }
    }
}
