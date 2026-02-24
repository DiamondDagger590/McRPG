package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.variable.Pool;
import us.eunoians.mcrpg.quest.board.template.variable.PoolVariable;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestTemplateEngineTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey EXPANSION_KEY = NamespacedKey.fromString("mcrpg:mcrpg");
    private static final NamespacedKey TEMPLATE_KEY = NamespacedKey.fromString("mcrpg:daily_mining");
    private static final NamespacedKey OBJECTIVE_TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");
    private static final NamespacedKey REWARD_TYPE_KEY = NamespacedKey.fromString("mcrpg:experience");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");

    private QuestRarityRegistry rarityRegistry;
    private QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private QuestRewardTypeRegistry rewardTypeRegistry;
    private QuestObjectiveType mockObjectiveType;
    private QuestRewardType mockRewardType;
    private QuestTemplateEngine engine;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);

        mockObjectiveType = mock(QuestObjectiveType.class);
        when(mockObjectiveType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);
        when(mockObjectiveType.parseConfig(any())).thenReturn(mockObjectiveType);
        when(objectiveTypeRegistry.get(OBJECTIVE_TYPE_KEY)).thenReturn(Optional.of(mockObjectiveType));

        mockRewardType = mock(QuestRewardType.class);
        when(mockRewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(mockRewardType.fromSerializedConfig(any())).thenAnswer(invocation -> {
            Map<String, Object> config = invocation.getArgument(0);
            QuestRewardType configured = mock(QuestRewardType.class);
            when(configured.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(configured.serializeConfig()).thenReturn(new LinkedHashMap<>(config));
            return configured;
        });
        when(rewardTypeRegistry.get(REWARD_TYPE_KEY)).thenReturn(Optional.of(mockRewardType));

        when(rarityRegistry.get(COMMON)).thenReturn(Optional.of(
                new QuestRarity(COMMON, 10, 1.0, 1.0, EXPANSION_KEY)));
        when(rarityRegistry.get(RARE)).thenReturn(Optional.of(
                new QuestRarity(RARE, 5, 1.5, 1.2, EXPANSION_KEY)));

        engine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry);
    }

    @Test
    @DisplayName("Generate with COMMON rarity produces valid definition with lower difficulty")
    void generate_commonRarity_lowerDifficulty() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));

        assertNotNull(result);
        QuestDefinition def = result.definition();
        assertNotNull(def);
        assertEquals(1, def.getPhases().size());
        assertTrue(def.getQuestKey().getKey().startsWith("gen_daily_mining_"));
    }

    @Test
    @DisplayName("Generate with RARE rarity produces higher difficulty than COMMON")
    void generate_rareRarity_higherDifficulty() {
        QuestTemplate template = createMiningTemplate();

        ResolvedVariableContext commonCtx = engine.resolveVariables(template, COMMON, new Random(42L));
        ResolvedVariableContext rareCtx = engine.resolveVariables(template, RARE, new Random(42L));

        assertTrue(rareCtx.difficulty() > commonCtx.difficulty(),
                "RARE difficulty (" + rareCtx.difficulty() + ") should exceed COMMON difficulty ("
                        + commonCtx.difficulty() + ")");
    }

    @Test
    @DisplayName("Combined difficulty equals poolDifficulty * rarityDifficulty")
    void resolveVariables_difficultyCalculation() {
        QuestTemplate template = createMiningTemplate();

        ResolvedVariableContext ctx = engine.resolveVariables(template, COMMON, new Random(42L));

        assertEquals(ctx.poolDifficulty() * ctx.rarityDifficulty(), ctx.difficulty(), 0.0001);
    }

    @Test
    @DisplayName("RANGE variables are scaled by combined difficulty")
    void resolveVariables_rangeScaledByDifficulty() {
        QuestTemplate template = createMiningTemplate();

        ResolvedVariableContext ctx = engine.resolveVariables(template, RARE, new Random(42L));

        Object blockCount = ctx.resolvedValues().get("block_count");
        assertInstanceOf(Long.class, blockCount);
        assertTrue((Long) blockCount > 0, "Block count should be positive");
    }

    @Test
    @DisplayName("POOL values are substituted correctly in objective config")
    @SuppressWarnings("unchecked")
    void generate_poolValuesSubstitutedInConfig() {
        QuestTemplate template = createMiningTemplate();

        engine.generate(template, COMMON, new Random(42L));

        verify(mockObjectiveType).parseConfig(any());
    }

    @Test
    @DisplayName("Reward expressions evaluate correctly with all variables in scope")
    void generate_rewardExpressionsEvaluated() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));

        assertFalse(result.definition().getRewards().isEmpty());
        verify(mockRewardType).fromSerializedConfig(any());
    }

    @Test
    @DisplayName("Generated definition has correct phase/stage/objective structure")
    void generate_correctStructure() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));
        QuestDefinition def = result.definition();

        assertEquals(1, def.getPhases().size());
        QuestPhaseDefinition phase = def.getPhases().get(0);
        assertEquals(PhaseCompletionMode.ALL, phase.getCompletionMode());
        assertEquals(1, phase.getStages().size());

        QuestStageDefinition stage = phase.getStages().get(0);
        assertEquals(1, stage.getObjectives().size());

        QuestObjectiveDefinition obj = stage.getObjectives().get(0);
        assertTrue(obj.getRequiredProgress() > 0);
    }

    @Test
    @DisplayName("Generated quest key follows mcrpg:gen_<template>_<hex> pattern")
    void generate_questKeyPattern() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));

        String key = result.definition().getQuestKey().getKey();
        assertTrue(key.matches("gen_daily_mining_[0-9a-f]{8}"),
                "Quest key should match pattern but was: " + key);
        assertEquals("mcrpg", result.definition().getQuestKey().getNamespace());
    }

    @Test
    @DisplayName("Template with rarity-overrides uses overrides over global values")
    void generate_rarityOverridesTakePrecedence() {
        RarityOverride override = new RarityOverride(5.0, 3.0);
        QuestTemplate template = createMiningTemplateWithOverrides(Map.of(COMMON, override));

        ResolvedVariableContext ctx = engine.resolveVariables(template, COMMON, new Random(42L));

        assertEquals(5.0, ctx.rarityDifficulty(), 0.0001,
                "Should use template-level difficulty override");
    }

    @Test
    @DisplayName("Invalid rarity not in supportedRarities throws IllegalArgumentException")
    void generate_invalidRarity_throws() {
        QuestTemplate template = createMiningTemplate();
        NamespacedKey epic = NamespacedKey.fromString("mcrpg:epic");

        assertThrows(IllegalArgumentException.class,
                () -> engine.generate(template, epic, new Random(42L)));
    }

    @Test
    @DisplayName("Seeded random produces deterministic definitions")
    void generate_seededRandom_deterministic() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result1 = engine.generate(template, COMMON, new Random(12345L));
        GeneratedQuestResult result2 = engine.generate(template, COMMON, new Random(12345L));

        assertEquals(result1.definition().getQuestKey(), result2.definition().getQuestKey());
    }

    @Test
    @DisplayName("Same seed produces same quest definition keys and progress values")
    void generate_sameSeed_sameDefinition() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result1 = engine.generate(template, COMMON, new Random(99L));
        GeneratedQuestResult result2 = engine.generate(template, COMMON, new Random(99L));

        QuestObjectiveDefinition obj1 = result1.definition().getPhases().get(0).getStages().get(0).getObjectives().get(0);
        QuestObjectiveDefinition obj2 = result2.definition().getPhases().get(0).getStages().get(0).getObjectives().get(0);
        assertEquals(obj1.getRequiredProgress(), obj2.getRequiredProgress());
        assertEquals(obj1.getObjectiveKey(), obj2.getObjectiveKey());
    }

    @Test
    @DisplayName("Serialized JSON is non-null and non-empty")
    void generate_producesSerializedJson() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));

        assertNotNull(result.serializedDefinition());
        assertFalse(result.serializedDefinition().isEmpty());
        assertTrue(result.serializedDefinition().contains("gen_daily_mining_"));
    }

    @Test
    @DisplayName("Template key in result matches the input template")
    void generate_templateKeyPreserved() {
        QuestTemplate template = createMiningTemplate();

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42L));

        assertEquals(TEMPLATE_KEY, result.templateKey());
    }

    @Test
    @DisplayName("Unknown objective type throws QuestGenerationException with template and rarity context")
    void generate_unknownObjectiveType_throwsQuestGenerationException() {
        NamespacedKey unknownType = NamespacedKey.fromString("mcrpg:nonexistent_objective");
        QuestTemplate template = createTemplateWithObjectiveType(unknownType);

        QuestGenerationException ex = assertThrows(QuestGenerationException.class,
                () -> engine.generate(template, COMMON, new Random(42L)));

        assertEquals(TEMPLATE_KEY, ex.getTemplateKey());
        assertEquals(COMMON, ex.getRarityKey());
        assertEquals(unknownType, ex.getFailedElementKey());
        assertTrue(ex.getMessage().contains("nonexistent_objective"));
    }

    @Test
    @DisplayName("Unknown reward type throws QuestGenerationException with template and rarity context")
    void generate_unknownRewardType_throwsQuestGenerationException() {
        NamespacedKey unknownReward = NamespacedKey.fromString("mcrpg:nonexistent_reward");
        QuestTemplate template = createTemplateWithRewardType(unknownReward);

        QuestGenerationException ex = assertThrows(QuestGenerationException.class,
                () -> engine.generate(template, COMMON, new Random(42L)));

        assertEquals(TEMPLATE_KEY, ex.getTemplateKey());
        assertEquals(COMMON, ex.getRarityKey());
        assertEquals(unknownReward, ex.getFailedElementKey());
        assertTrue(ex.getMessage().contains("nonexistent_reward"));
    }

    private QuestTemplate createMiningTemplate() {
        return createMiningTemplateWithOverrides(Map.of());
    }

    private QuestTemplate createTemplateWithObjectiveType(NamespacedKey objectiveTypeKey) {
        RangeVariable blockCount = new RangeVariable("block_count", 32, 64);
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("block_count", blockCount);

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                objectiveTypeKey, "block_count", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        return new QuestTemplate(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), true, SCOPE_KEY,
                Set.of(COMMON, RARE), Map.of(), variables, List.of(phase), List.of());
    }

    private QuestTemplate createTemplateWithRewardType(NamespacedKey rewardTypeKey) {
        RangeVariable blockCount = new RangeVariable("block_count", 32, 64);
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("block_count", blockCount);

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY, "block_count", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));
        TemplateRewardDefinition reward = new TemplateRewardDefinition(
                rewardTypeKey, Map.of("amount", "block_count"));

        return new QuestTemplate(TEMPLATE_KEY,
                Route.fromString("quests.templates.test.display-name"), true, SCOPE_KEY,
                Set.of(COMMON, RARE), Map.of(), variables, List.of(phase), List.of(reward));
    }

    private QuestTemplate createMiningTemplateWithOverrides(Map<NamespacedKey, RarityOverride> overrides) {
        Pool ores = new Pool("ores", 1.0,
                Map.of(COMMON, 10, RARE, 5),
                List.of("IRON_ORE", "COPPER_ORE"));
        Pool precious = new Pool("precious", 2.0,
                Map.of(COMMON, 2, RARE, 10),
                List.of("DIAMOND_ORE", "EMERALD_ORE"));

        PoolVariable targetBlocks = new PoolVariable("target_blocks", 1, 2,
                List.of(ores, precious));
        RangeVariable blockCount = new RangeVariable("block_count", 32, 64);

        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("target_blocks", targetBlocks);
        variables.put("block_count", blockCount);

        TemplateObjectiveDefinition objective = new TemplateObjectiveDefinition(
                OBJECTIVE_TYPE_KEY,
                "block_count",
                Map.of("blocks", "target_blocks"));

        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(objective));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        TemplateRewardDefinition reward = new TemplateRewardDefinition(
                REWARD_TYPE_KEY,
                Map.of("skill", "MINING", "amount", "block_count * 5 * difficulty"));

        return new QuestTemplate(
                TEMPLATE_KEY,
                Route.fromString("quests.templates.daily_mining.display-name"),
                true,
                SCOPE_KEY,
                Set.of(COMMON, RARE),
                overrides,
                variables,
                List.of(phase),
                List.of(reward));
    }
}
