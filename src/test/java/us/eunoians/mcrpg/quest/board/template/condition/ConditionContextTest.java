package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.ResolvedVariableContext;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ConditionContextTest extends McRPGBaseTest {

    private static final NamespacedKey RARITY_KEY = new NamespacedKey("mcrpg", "common");

    @Nested
    @DisplayName("forTemplateGeneration")
    class ForTemplateGeneration {

        @DisplayName("Sets rarity, registry, random, and variables")
        @Test
        void setsGenerationFields() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            Random random = new Random(42);
            ResolvedVariableContext vars = new ResolvedVariableContext(Map.of(), 1.0, 1.0, 1.0);

            ConditionContext ctx = ConditionContext.forTemplateGeneration(RARITY_KEY, registry, random, vars);

            assertEquals(RARITY_KEY, ctx.rolledRarity());
            assertEquals(registry, ctx.rarityRegistry());
            assertEquals(random, ctx.random());
            assertEquals(vars, ctx.resolvedVariables());
        }

        @DisplayName("Leaves player fields null")
        @Test
        void leavesPlayerFieldsNull() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            Random random = new Random(42);
            ResolvedVariableContext vars = new ResolvedVariableContext(Map.of(), 1.0, 1.0, 1.0);

            ConditionContext ctx = ConditionContext.forTemplateGeneration(RARITY_KEY, registry, random, vars);

            assertNull(ctx.playerUUID());
            assertNull(ctx.completionHistory());
        }
    }

    @Nested
    @DisplayName("forPersonalGeneration")
    class ForPersonalGeneration {

        @DisplayName("Sets all fields including player data")
        @Test
        void setsAllFields() {
            QuestRarityRegistry registry = new QuestRarityRegistry();
            Random random = new Random(42);
            ResolvedVariableContext vars = new ResolvedVariableContext(Map.of(), 1.0, 1.0, 1.0);
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);

            ConditionContext ctx = ConditionContext.forPersonalGeneration(
                    RARITY_KEY, registry, random, vars, playerUUID, history);

            assertEquals(RARITY_KEY, ctx.rolledRarity());
            assertEquals(registry, ctx.rarityRegistry());
            assertEquals(random, ctx.random());
            assertEquals(vars, ctx.resolvedVariables());
            assertEquals(playerUUID, ctx.playerUUID());
            assertEquals(history, ctx.completionHistory());
        }
    }

    @Nested
    @DisplayName("forPrerequisiteCheck")
    class ForPrerequisiteCheck {

        @DisplayName("Sets player UUID and completion history")
        @Test
        void setsPlayerFields() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);

            assertEquals(playerUUID, ctx.playerUUID());
            assertEquals(history, ctx.completionHistory());
        }

        @DisplayName("Leaves generation fields null")
        @Test
        void leavesGenerationFieldsNull() {
            UUID playerUUID = UUID.randomUUID();
            QuestCompletionHistory history = mock(QuestCompletionHistory.class);

            ConditionContext ctx = ConditionContext.forPrerequisiteCheck(playerUUID, history);

            assertNull(ctx.rolledRarity());
            assertNull(ctx.rarityRegistry());
            assertNull(ctx.random());
            assertNull(ctx.resolvedVariables());
        }
    }

    @Nested
    @DisplayName("forRewardGrant")
    class ForRewardGrant {

        @DisplayName("Sets player UUID and optional rarity")
        @Test
        void setsPlayerAndRarity() {
            UUID playerUUID = UUID.randomUUID();
            QuestRarityRegistry registry = new QuestRarityRegistry();

            ConditionContext ctx = ConditionContext.forRewardGrant(playerUUID, RARITY_KEY, registry);

            assertEquals(playerUUID, ctx.playerUUID());
            assertEquals(RARITY_KEY, ctx.rolledRarity());
            assertEquals(registry, ctx.rarityRegistry());
        }

        @DisplayName("Leaves generation-only fields null")
        @Test
        void leavesGenerationOnlyFieldsNull() {
            UUID playerUUID = UUID.randomUUID();

            ConditionContext ctx = ConditionContext.forRewardGrant(playerUUID, RARITY_KEY, null);

            assertNull(ctx.random());
            assertNull(ctx.resolvedVariables());
            assertNull(ctx.completionHistory());
        }

        @DisplayName("Allows null rarity and registry")
        @Test
        void allowsNullRarityAndRegistry() {
            UUID playerUUID = UUID.randomUUID();

            ConditionContext ctx = ConditionContext.forRewardGrant(playerUUID, null, null);

            assertNotNull(ctx.playerUUID());
            assertNull(ctx.rolledRarity());
            assertNull(ctx.rarityRegistry());
        }
    }
}
