package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestTemplateEngineEdgeCaseTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey EXPANSION_KEY = NamespacedKey.fromString("mcrpg:mcrpg");
    private static final NamespacedKey TEMPLATE_KEY = NamespacedKey.fromString("mcrpg:edge_test");
    private static final NamespacedKey OBJECTIVE_TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");
    private static final NamespacedKey REWARD_TYPE_KEY = NamespacedKey.fromString("mcrpg:experience");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");

    private QuestRarityRegistry rarityRegistry;
    private QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private QuestRewardTypeRegistry rewardTypeRegistry;
    private QuestTemplateEngine engine;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);

        QuestObjectiveType mockObjectiveType = mock(QuestObjectiveType.class);
        when(mockObjectiveType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);
        when(mockObjectiveType.parseConfig(any())).thenReturn(mockObjectiveType);
        when(objectiveTypeRegistry.get(OBJECTIVE_TYPE_KEY)).thenReturn(Optional.of(mockObjectiveType));

        QuestRewardType mockRewardType = mock(QuestRewardType.class);
        when(mockRewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(mockRewardType.fromSerializedConfig(any())).thenAnswer(invocation -> {
            Map<String, Object> config = invocation.getArgument(0);
            QuestRewardType configured = mock(QuestRewardType.class);
            when(configured.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(configured.serializeConfig()).thenReturn(new LinkedHashMap<>(config));
            when(configured.withLocalizationRoute(any())).thenReturn(configured);
            return configured;
        });
        when(rewardTypeRegistry.get(REWARD_TYPE_KEY)).thenReturn(Optional.of(mockRewardType));

        when(rarityRegistry.get(COMMON)).thenReturn(Optional.of(
                new QuestRarity(COMMON, 10, 1.0, 1.0, EXPANSION_KEY)));

        McRPG mockPlugin = mock(McRPG.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("QuestTemplateEngineEdgeCaseTest"));
        var conditionRegistry = mock(TemplateConditionRegistry.class);
        var codec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, rewardTypeRegistry, conditionRegistry);
        engine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry, mockPlugin, new WeightedObjectiveSelector(), codec);
    }

    @Test
    @DisplayName("Given template with only range variables (no pools), when generating, then difficulty defaults to 1.0 * rarity")
    void resolveVariables_noPoolVariables_difficultyDefaultsToRarity() {
        RangeVariable count = new RangeVariable("count", 10, 20);
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("count", count);

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "count", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), variables, List.of(phase), List.of())
                .build();

        ResolvedVariableContext ctx = engine.resolveVariables(template, COMMON, new Random(42L));

        assertEquals(1.0, ctx.poolDifficulty(), 0.0001, "Pool difficulty should default to 1.0 when no pools exist");
        assertEquals(1.0, ctx.rarityDifficulty(), 0.0001);
        assertEquals(1.0, ctx.difficulty(), 0.0001);
    }

    @Test
    @DisplayName("Given template with no variables, when generating, then definition is produced with literal required-progress")
    void generate_noVariables_literalProgress() {
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "50", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), variables, List.of(phase), List.of())
                .build();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        QuestDefinition def = result.definition();
        assertNotNull(def);
        QuestObjectiveDefinition objDef = def.getPhases().get(0).getStages().get(0).getObjectives().get(0);
        assertEquals(50L, objDef.getRequiredProgress());
    }

    @Test
    @DisplayName("Given required-progress expression that evaluates to zero, when generating, then it is clamped to 1")
    void generate_zeroProgress_clampedToOne() {
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "0", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), variables, List.of(phase), List.of())
                .build();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        QuestDefinition def = result.definition();
        long required = def.getPhases().get(0).getStages().get(0).getObjectives().get(0)
                .getRequiredProgress();
        assertTrue(required >= 1, "Required progress should be clamped to at least 1, got: " + required);
    }

    @Test
    @DisplayName("Given all phases filtered out by condition, when generating, then QuestGenerationException is thrown")
    void generate_allPhasesFiltered_throwsQuestGenerationException() {
        TemplateCondition alwaysFalse = mock(TemplateCondition.class);
        when(alwaysFalse.evaluate(any())).thenReturn(false);

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage), alwaysFalse);

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), Map.of(), List.of(phase), List.of())
                .build();

        assertThrows(QuestGenerationException.class,
                () -> engine.generate(template, COMMON, new Random(42L)),
                "Should throw when all phases are filtered out by conditions");
    }

    @Test
    @DisplayName("Given phase with condition that passes, when generating, then phase is included")
    void generate_phaseConditionPasses_phaseIncluded() {
        TemplateCondition alwaysTrue = mock(TemplateCondition.class);
        when(alwaysTrue.evaluate(any())).thenReturn(true);

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage), alwaysTrue);

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), Map.of(), List.of(phase), List.of())
                .build();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        assertEquals(1, result.definition().getPhases().size());
    }

    @Test
    @DisplayName("Given reward with literal 'amount' numeric value, when generating with multiplier > 1, then amount is scaled")
    void generate_rewardNumericAmount_scaledByMultiplier() {
        NamespacedKey rareKey = NamespacedKey.fromString("mcrpg:rare");
        when(rarityRegistry.get(rareKey)).thenReturn(Optional.of(
                new QuestRarity(rareKey, 5, 1.5, 2.0, EXPANSION_KEY)));

        Map<String, TemplateVariable> variables = new LinkedHashMap<>();

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        TemplateRewardDefinition reward = new TemplateRewardDefinition(
                REWARD_TYPE_KEY, "xp_reward",
                Map.of("amount", 100, "display-label", "Experience"));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(rareKey), Map.of(), variables, List.of(phase), List.of(reward))
                .build();

        GeneratedQuestResult result = engine.generate(template, rareKey, new Random(42L));
        QuestRewardType generatedReward = result.definition().getRewards().get(0);
        Map<String, Object> config = generatedReward.serializeConfig();
        long amount = ((Number) config.get("amount")).longValue();
        assertEquals(200L, amount, "Amount 100 * RARE reward multiplier 2.0 should equal 200");
    }

    @Test
    @DisplayName("Given reward with non-amount key and numeric value, when generating, then value is not scaled by multiplier")
    void generate_rewardNonAmountNumericKey_notScaled() {
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        TemplateRewardDefinition reward = new TemplateRewardDefinition(
                REWARD_TYPE_KEY, "xp_reward",
                Map.of("amount", 100, "some-count", 5));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), variables, List.of(phase), List.of(reward))
                .build();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        Map<String, Object> config = result.definition().getRewards().get(0).serializeConfig();
        assertEquals(5, ((Number) config.get("some-count")).intValue(),
                "Non-amount keys should not be scaled by reward multiplier");
    }

    @Test
    @DisplayName("Given unsupported rarity, when generating, then IllegalArgumentException is thrown")
    void generate_unsupportedRarity_throws() {
        NamespacedKey unsupported = NamespacedKey.fromString("mcrpg:legendary");
        when(rarityRegistry.get(unsupported)).thenReturn(Optional.of(
                new QuestRarity(unsupported, 1, 3.0, 3.0, EXPANSION_KEY)));

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), Map.of(), List.of(phase), List.of())
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> engine.generate(template, unsupported, new Random(42L)),
                "Should throw for rarity not in template's supported set");
    }

    @Test
    @DisplayName("Given multiple phases, when generating, then all phases appear in the definition")
    void generate_multiplePhases_allIncluded() {
        TemplateObjectiveDefinition obj1 = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage1 = new TemplateStageDefinition(List.of(obj1));
        TemplatePhaseDefinition phase1 = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage1));

        TemplateObjectiveDefinition obj2 = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "20", Map.of());
        TemplateStageDefinition stage2 = new TemplateStageDefinition(List.of(obj2));
        TemplatePhaseDefinition phase2 = new TemplatePhaseDefinition(
                PhaseCompletionMode.ANY, List.of(stage2));

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), Map.of(), List.of(phase1, phase2), List.of())
                .build();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        assertEquals(2, result.definition().getPhases().size());
        assertEquals(PhaseCompletionMode.ALL, result.definition().getPhases().get(0).getCompletionMode());
        assertEquals(PhaseCompletionMode.ANY, result.definition().getPhases().get(1).getCompletionMode());
    }

    @Test
    @DisplayName("Given template with inline display, when generating, then display keys are present on definition")
    void generate_inlineDisplay_preservedOnDefinition() {
        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "10", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        Map<String, String> inlineDisplay = Map.of(
                "name", "Mining Quest",
                "description", "Mine some blocks");

        QuestTemplate template = new QuestTemplate.Builder(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), SCOPE_KEY,
                Set.of(COMMON), Map.of(), Map.of(), List.of(phase), List.of())
                .inlineDisplay(inlineDisplay)
                .build();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        Map<String, String> display = result.definition().getInlineDisplay();
        assertEquals("Mining Quest", display.get("name"));
        assertEquals("Mine some blocks", display.get("description"));
    }
}
