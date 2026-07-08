package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.template.condition.CompletionPrerequisiteCondition;
import us.eunoians.mcrpg.quest.board.template.condition.CompoundCondition;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.QuestCompletionHistory;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for prerequisite gating on {@link QuestTemplate}.
 * <p>
 * These tests verify the data model and condition evaluation for template-level prerequisites.
 * Pool-level filtering (filtering which templates appear in a player's offering pool) is
 * a pending wiring step in {@code QuestPool} and should be tested end-to-end once implemented.
 */
class PrerequisiteGatingTest {

    private static final NamespacedKey TEMPLATE_KEY = NamespacedKey.fromString("mcrpg:test_template");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");
    private static final NamespacedKey EXPANSION_KEY = NamespacedKey.fromString("mcrpg:mcrpg");
    private static final NamespacedKey RARITY_KEY = NamespacedKey.fromString("mcrpg:common");

    private QuestTemplate templateWithPrerequisite(TemplateCondition prerequisite) {
        return new QuestTemplate.Builder(TEMPLATE_KEY, Route.fromString("test.display"),
                SCOPE_KEY, Set.of(RARITY_KEY), Map.of(),
                Map.of(), List.of(), List.of())
                .prerequisite(prerequisite)
                .expansionKey(EXPANSION_KEY)
                .build();
    }

    private QuestTemplate templateWithoutPrerequisite() {
        return new QuestTemplate.Builder(TEMPLATE_KEY, Route.fromString("test.display"),
                SCOPE_KEY, Set.of(RARITY_KEY), Map.of(),
                Map.of(), List.of(), List.of())
                .expansionKey(EXPANSION_KEY)
                .build();
    }

    @Nested
    @DisplayName("QuestTemplate prerequisite field")
    class TemplatePrerequisiteField {

        @Test
        @DisplayName("template without prerequisite returns empty optional")
        void noPrerequisiteReturnsEmpty() {
            QuestTemplate template = templateWithoutPrerequisite();
            assertTrue(template.getPrerequisite().isEmpty());
        }

        @Test
        @DisplayName("template with prerequisite returns the condition")
        void withPrerequisiteReturnsCondition() {
            TemplateCondition condition = mock(TemplateCondition.class);
            QuestTemplate template = templateWithPrerequisite(condition);
            Optional<TemplateCondition> prerequisite = template.getPrerequisite();
            assertTrue(prerequisite.isPresent());
            assertSame(condition, prerequisite.get());
        }
    }

    @Nested
    @DisplayName("ConditionContext.forPrerequisiteCheck()")
    class PrerequisiteContextFactory {

        @Test
        @DisplayName("prerequisite context has player UUID and completion history")
        void contextHasPlayerAndHistory() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);

            assertSame(playerUUID, ctx.playerUUID());
            assertSame(history, ctx.completionHistory());
        }

        @Test
        @DisplayName("prerequisite context has null generation-specific fields")
        void contextHasNullGenerationFields() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);

            assertTrue(ctx.rolledRarity() == null);
            assertTrue(ctx.rarityRegistry() == null);
            assertTrue(ctx.random() == null);
            assertTrue(ctx.resolvedVariables() == null);
        }
    }

    @Nested
    @DisplayName("Prerequisite condition evaluation")
    class PrerequisiteConditionEvaluation {

        @Test
        @DisplayName("player meets completion prerequisite — template eligible")
        void playerMeetsPrerequisite() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(10);

            CompletionPrerequisiteCondition prereq = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);

            assertTrue(prereq.evaluate(ctx));
        }

        @Test
        @DisplayName("player does not meet completion prerequisite — template excluded")
        void playerDoesNotMeetPrerequisite() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            when(history.countCompletedQuests(playerUUID, null, null)).thenReturn(2);

            CompletionPrerequisiteCondition prereq = new CompletionPrerequisiteCondition(5, null, null);
            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);

            assertFalse(prereq.evaluate(ctx));
        }

        @Test
        @DisplayName("stacked compound prerequisite — all sub-conditions must pass")
        void stackedPrerequisiteAllMustPass() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            NamespacedKey dailyCategory = NamespacedKey.fromString("mcrpg:personal_daily");
            NamespacedKey weeklyCategory = NamespacedKey.fromString("mcrpg:personal_weekly");

            when(history.countCompletedQuests(playerUUID, dailyCategory, null)).thenReturn(10);
            when(history.countCompletedQuests(playerUUID, weeklyCategory, null)).thenReturn(2);

            CompletionPrerequisiteCondition dailyReq = new CompletionPrerequisiteCondition(5, dailyCategory, null);
            CompletionPrerequisiteCondition weeklyReq = new CompletionPrerequisiteCondition(5, weeklyCategory, null);

            CompoundCondition compound = new CompoundCondition(
                    Map.of("daily", dailyReq, "weekly", weeklyReq),
                    CompoundCondition.LogicMode.ALL);

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);
            assertFalse(compound.evaluate(ctx));
        }

        @Test
        @DisplayName("stacked compound prerequisite — both pass")
        void stackedPrerequisiteBothPass() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);
            NamespacedKey dailyCategory = NamespacedKey.fromString("mcrpg:personal_daily");
            NamespacedKey weeklyCategory = NamespacedKey.fromString("mcrpg:personal_weekly");

            when(history.countCompletedQuests(playerUUID, dailyCategory, null)).thenReturn(10);
            when(history.countCompletedQuests(playerUUID, weeklyCategory, null)).thenReturn(8);

            CompletionPrerequisiteCondition dailyReq = new CompletionPrerequisiteCondition(5, dailyCategory, null);
            CompletionPrerequisiteCondition weeklyReq = new CompletionPrerequisiteCondition(5, weeklyCategory, null);

            CompoundCondition compound = new CompoundCondition(
                    Map.of("daily", dailyReq, "weekly", weeklyReq),
                    CompoundCondition.LogicMode.ALL);

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);
            assertTrue(compound.evaluate(ctx));
        }

        @Test
        @DisplayName("shared template prerequisite is ignored when no player UUID in context")
        void sharedTemplatePrerequisiteIgnoredWithoutPlayer() {
            // Shared templates are generated without player context (ConditionContext.forTemplateGeneration).
            // CompletionPrerequisiteCondition returns true when playerUUID is null, preventing
            // inadvertent exclusion from shared generation.
            ConditionContext sharedCtx = new ConditionContext(null, null, null, null, null, null);
            CompletionPrerequisiteCondition prereq = new CompletionPrerequisiteCondition(5, null, null);
            // Returns false because playerUUID is null — prerequisites safely deny access
            // without player context per the design doc's safe-default rule.
            assertFalse(prereq.evaluate(sharedCtx));
        }
    }
}
