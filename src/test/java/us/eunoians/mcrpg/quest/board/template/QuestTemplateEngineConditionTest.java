package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.RarityCondition;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
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

class QuestTemplateEngineConditionTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey LEGENDARY = NamespacedKey.fromString("mcrpg:legendary");
    private static final NamespacedKey TEMPLATE_KEY = NamespacedKey.fromString("mcrpg:test_template");
    private static final NamespacedKey OBJ_TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");
    private static final NamespacedKey REWARD_TYPE_KEY = NamespacedKey.fromString("mcrpg:experience");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");
    private static final NamespacedKey EXPANSION_KEY = NamespacedKey.fromString("mcrpg:mcrpg");

    private QuestRarityRegistry rarityRegistry;
    private QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private QuestRewardTypeRegistry rewardTypeRegistry;
    private QuestTemplateEngine engine;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);

        QuestRarity commonRarity = mock(QuestRarity.class);
        QuestRarity rareRarity = mock(QuestRarity.class);
        QuestRarity legendaryRarity = mock(QuestRarity.class);
        when(commonRarity.getWeight()).thenReturn(100);
        when(commonRarity.getDifficultyMultiplier()).thenReturn(1.0);
        when(commonRarity.getRewardMultiplier()).thenReturn(1.0);
        when(rareRarity.getWeight()).thenReturn(20);
        when(rareRarity.getDifficultyMultiplier()).thenReturn(1.5);
        when(rareRarity.getRewardMultiplier()).thenReturn(1.5);
        when(legendaryRarity.getWeight()).thenReturn(5);
        when(legendaryRarity.getDifficultyMultiplier()).thenReturn(3.0);
        when(legendaryRarity.getRewardMultiplier()).thenReturn(3.0);
        when(rarityRegistry.get(COMMON)).thenReturn(Optional.of(commonRarity));
        when(rarityRegistry.get(RARE)).thenReturn(Optional.of(rareRarity));
        when(rarityRegistry.get(LEGENDARY)).thenReturn(Optional.of(legendaryRarity));

        QuestObjectiveType mockObjType = mock(QuestObjectiveType.class);
        when(mockObjType.getKey()).thenReturn(OBJ_TYPE_KEY);
        when(mockObjType.parseConfig(any())).thenReturn(mockObjType);
        when(objectiveTypeRegistry.get(OBJ_TYPE_KEY)).thenReturn(Optional.of(mockObjType));

        QuestRewardType mockRewardType = mock(QuestRewardType.class);
        when(mockRewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(mockRewardType.fromSerializedConfig(any())).thenReturn(mockRewardType);
        when(mockRewardType.serializeConfig()).thenReturn(Map.of("amount", 100));
        when(rewardTypeRegistry.get(REWARD_TYPE_KEY)).thenReturn(Optional.of(mockRewardType));

        engine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry);
    }

    private QuestTemplate buildTemplate(List<TemplatePhaseDefinition> phases) {
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        List<TemplateRewardDefinition> rewards = List.of(
                new TemplateRewardDefinition(REWARD_TYPE_KEY, Map.of("amount", 100)));
        return new QuestTemplate(TEMPLATE_KEY, Route.fromString("test.display"),
                true, SCOPE_KEY, Set.of(COMMON, RARE, LEGENDARY),
                Map.of(), variables, phases, rewards, null, null);
    }

    private TemplateObjectiveDefinition objective() {
        return new TemplateObjectiveDefinition(OBJ_TYPE_KEY, "10", Map.of());
    }

    @Test
    @DisplayName("unconditional phase is always included")
    void unconditionalPhaseAlwaysIncluded() {
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))));
        QuestTemplate template = buildTemplate(List.of(phase));

        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42));
        assertEquals(1, result.definition().getPhases().size());
    }

    @Test
    @DisplayName("phase with rarity condition excluded when rarity too low")
    void phaseExcludedByRarityCondition() {
        TemplateCondition rareGate = new RarityCondition(RARE);
        TemplatePhaseDefinition conditionalPhase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))),
                rareGate);
        TemplatePhaseDefinition unconditionalPhase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))));

        QuestTemplate template = buildTemplate(List.of(unconditionalPhase, conditionalPhase));
        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42));
        assertEquals(1, result.definition().getPhases().size());
    }

    @Test
    @DisplayName("phase with rarity condition included when rarity meets threshold")
    void phaseIncludedByRarityCondition() {
        TemplateCondition rareGate = new RarityCondition(RARE);
        TemplatePhaseDefinition conditionalPhase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))),
                rareGate);
        TemplatePhaseDefinition unconditionalPhase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))));

        QuestTemplate template = buildTemplate(List.of(unconditionalPhase, conditionalPhase));
        GeneratedQuestResult result = engine.generate(template, LEGENDARY, new Random(42));
        assertEquals(2, result.definition().getPhases().size());
    }

    @Test
    @DisplayName("all phases excluded throws QuestGenerationException")
    void allPhasesExcludedThrows() {
        TemplateCondition legendaryGate = new RarityCondition(LEGENDARY);
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))),
                legendaryGate);

        QuestTemplate template = buildTemplate(List.of(phase));
        assertThrows(QuestGenerationException.class,
                () -> engine.generate(template, COMMON, new Random(42)));
    }

    @Test
    @DisplayName("conditional objective excluded, stage survives with other objectives")
    void objectiveExcludedStageSurvives() {
        TemplateCondition rareGate = new RarityCondition(RARE);
        TemplateObjectiveDefinition unconditionalObj = objective();
        TemplateObjectiveDefinition conditionalObj = new TemplateObjectiveDefinition(
                OBJ_TYPE_KEY, "20", Map.of(), rareGate, 1);

        TemplateStageDefinition stage = new TemplateStageDefinition(
                List.of(unconditionalObj, conditionalObj));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL, List.of(stage));

        QuestTemplate template = buildTemplate(List.of(phase));
        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42));
        assertEquals(1, result.definition().getPhases().get(0).getStages().get(0).getObjectives().size());
    }

    @Test
    @DisplayName("stage with all objectives excluded is removed")
    void stageRemovedWhenAllObjectivesExcluded() {
        TemplateCondition legendaryGate = new RarityCondition(LEGENDARY);
        TemplateObjectiveDefinition conditionalObj = new TemplateObjectiveDefinition(
                OBJ_TYPE_KEY, "10", Map.of(), legendaryGate, 1);

        TemplateStageDefinition conditionalStage = new TemplateStageDefinition(
                List.of(conditionalObj));
        TemplateStageDefinition unconditionalStage = new TemplateStageDefinition(
                List.of(objective()));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(unconditionalStage, conditionalStage));

        QuestTemplate template = buildTemplate(List.of(phase));
        GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42));
        assertEquals(1, result.definition().getPhases().get(0).getStages().size());
    }
}
