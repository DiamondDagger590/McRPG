package us.eunoians.mcrpg.quest.board.template;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.QuestCompletionHistory;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
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
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link QuestTemplateEngine#generate} chooses {@link ConditionContext#forPersonalGeneration}
 * vs {@link ConditionContext#forTemplateGeneration} based on player + history presence.
 */
class QuestTemplateEngineContextBranchTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey OBJ_TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");
    private static final NamespacedKey REWARD_TYPE_KEY = NamespacedKey.fromString("mcrpg:experience");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");
    private static final NamespacedKey PERSONAL_CONTEXT_GATE_KEY =
            NamespacedKey.fromString("mcrpg:test_personal_context_gate");

    private QuestRarityRegistry rarityRegistry;
    private QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private QuestRewardTypeRegistry rewardTypeRegistry;
    private QuestTemplateEngine engine;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);

        var commonRarity = mock(QuestRarity.class);
        when(commonRarity.getWeight()).thenReturn(100);
        when(commonRarity.getDifficultyMultiplier()).thenReturn(1.0);
        when(commonRarity.getRewardMultiplier()).thenReturn(1.0);
        when(rarityRegistry.get(COMMON)).thenReturn(Optional.of(commonRarity));

        QuestObjectiveType mockObjType = mock(QuestObjectiveType.class);
        when(mockObjType.getKey()).thenReturn(OBJ_TYPE_KEY);
        when(mockObjType.parseConfig(any())).thenReturn(mockObjType);
        when(objectiveTypeRegistry.get(OBJ_TYPE_KEY)).thenReturn(Optional.of(mockObjType));

        QuestRewardType mockRewardType = mock(QuestRewardType.class);
        when(mockRewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(mockRewardType.fromSerializedConfig(any())).thenReturn(mockRewardType);
        when(mockRewardType.withLocalizationRoute(any())).thenReturn(mockRewardType);
        when(mockRewardType.serializeConfig()).thenReturn(Map.of("amount", 100));
        when(rewardTypeRegistry.get(REWARD_TYPE_KEY)).thenReturn(Optional.of(mockRewardType));

        McRPG mockPlugin = mock(McRPG.class);
        when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("QuestTemplateEngineContextBranchTest"));
        var conditionRegistry = mock(TemplateConditionRegistry.class);
        var codec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, rewardTypeRegistry, conditionRegistry);
        engine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry, mockPlugin, new WeightedObjectiveSelector(), codec);
    }

    @Test
    @DisplayName("Shared generation uses template context so personal-only gated phases are excluded")
    void generate_excludesPersonalGatedPhase_whenSharedGeneration() {
        QuestTemplate template = templateWithPersonalGatedSecondPhase();

        assertEquals(1, engine.generate(template, COMMON, new Random(7)).definition().getPhases().size());
    }

    @Test
    @DisplayName("Personal generation passes player context so personal-gated phases are included")
    void generate_includesPersonalGatedPhase_whenPersonalGeneration() {
        QuestTemplate template = templateWithPersonalGatedSecondPhase();
        UUID playerId = UUID.randomUUID();
        QuestCompletionHistory history = mock(QuestCompletionHistory.class);

        assertEquals(2, engine.generate(template, COMMON, new Random(7), playerId, history)
                .definition().getPhases().size());
    }

    private QuestTemplate templateWithPersonalGatedSecondPhase() {
        TemplateCondition personalOnly = new TemplateCondition() {
            @Override
            public @NotNull NamespacedKey getKey() {
                return PERSONAL_CONTEXT_GATE_KEY;
            }

            @Override
            public boolean evaluate(@NotNull ConditionContext context) {
                return context.playerUUID() != null && context.completionHistory() != null;
            }

            @Override
            public @NotNull TemplateCondition fromConfig(@NotNull Section section, @NotNull ConditionParser parser) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull Optional<NamespacedKey> getExpansionKey() {
                return Optional.empty();
            }
        };

        TemplatePhaseDefinition basePhase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))));
        TemplatePhaseDefinition gatedPhase = new TemplatePhaseDefinition(
                PhaseCompletionMode.ALL,
                List.of(new TemplateStageDefinition(List.of(objective()))),
                personalOnly);

        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("count", new RangeVariable("count", 10, 50));

        return new QuestTemplate.Builder(
                NamespacedKey.fromString("mcrpg:ctx_branch_tmpl"),
                Route.fromString("test.display"),
                SCOPE_KEY,
                Set.of(COMMON),
                Map.of(),
                variables,
                List.of(basePhase, gatedPhase),
                List.of())
                .build();
    }

    private TemplateObjectiveDefinition objective() {
        return new TemplateObjectiveDefinition(OBJ_TYPE_KEY, "count", Map.of());
    }
}
