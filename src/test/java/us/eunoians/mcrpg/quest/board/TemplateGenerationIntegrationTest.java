package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.listener.quest.QuestCompleteListener;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.distribution.ContributionSnapshot;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.distribution.builtin.MembershipDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.ParticipatedDistributionType;
import us.eunoians.mcrpg.quest.board.distribution.builtin.TopPlayersDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.TemplateObjectiveDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplatePhaseDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateRewardDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateStageDefinition;
import us.eunoians.mcrpg.quest.board.template.condition.RarityCondition;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestDefinitionCodec;
import us.eunoians.mcrpg.quest.board.template.WeightedObjectiveSelector;
import us.eunoians.mcrpg.quest.board.distribution.QuestContributionAggregator;
import us.eunoians.mcrpg.quest.board.distribution.QuestRewardDistributionResolver;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.MockQuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.quest.board.QuestBoardTerminator;
import us.eunoians.mcrpg.quest.board.distribution.DistributionCompletionService;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionGranter;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * End-to-end integration tests for Phase 4 features:
 * conditional objective generation, reward fallback resolution, quest-level
 * distribution at completion, and key UI data structures.
 */
public class TemplateGenerationIntegrationTest extends McRPGBaseTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey RARE = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey LEGENDARY = NamespacedKey.fromString("mcrpg:legendary");
    private static final NamespacedKey OBJ_TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");
    private static final NamespacedKey REWARD_TYPE_KEY = NamespacedKey.fromString("mcrpg:experience");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");

    private QuestRarityRegistry rarityRegistry;
    private QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private QuestRewardTypeRegistry rewardTypeRegistry;
    private QuestTemplateEngine engine;
    private RewardDistributionTypeRegistry distributionTypeRegistry;
    private QuestManager mockQuestManager;

    @BeforeEach
    void setUp() {
        rarityRegistry = mock(QuestRarityRegistry.class);
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);

        var commonRarity = mock(QuestRarity.class);
        var rareRarity = mock(QuestRarity.class);
        var legendaryRarity = mock(QuestRarity.class);
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
        when(mockRewardType.withLocalizationRoute(any())).thenReturn(mockRewardType);
        when(mockRewardType.serializeConfig()).thenReturn(Map.of("amount", 100));
        when(rewardTypeRegistry.get(REWARD_TYPE_KEY)).thenReturn(Optional.of(mockRewardType));

        var conditionRegistry = mock(TemplateConditionRegistry.class);
        var codec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, rewardTypeRegistry, conditionRegistry);
        engine = new QuestTemplateEngine(rarityRegistry, objectiveTypeRegistry, rewardTypeRegistry, mcRPG, new WeightedObjectiveSelector(), codec);

        distributionTypeRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        if (distributionTypeRegistry.get(ParticipatedDistributionType.KEY).isEmpty()) {
            distributionTypeRegistry.register(new ParticipatedDistributionType());
        }
        if (distributionTypeRegistry.get(MembershipDistributionType.KEY).isEmpty()) {
            distributionTypeRegistry.register(new MembershipDistributionType());
        }

        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        var rarityReg = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_RARITY);
        var distTypeReg = RegistryAccess.registryAccess().registry(McRPGRegistryKey.REWARD_DISTRIBUTION_TYPE);
        var aggregator = new QuestContributionAggregator();
        var resolver = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test"));
        var distService = new DistributionCompletionService(rarityReg, distTypeReg, new RewardDistributionGranter(mcRPG), aggregator, resolver);
        server.getPluginManager().registerEvents(new QuestCompleteListener(new QuestBoardTerminator(mcRPG), distService, aggregator), mcRPG);
        mockQuestManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
    }

    @AfterEach
    void tearDown() {
        HandlerList.unregisterAll(mcRPG);
    }

    @Nested
    @DisplayName("Template generation → conditional phase inclusion")
    class ConditionalGeneration {

        @Test
        @DisplayName("COMMON rarity → rare-gated phase excluded, quest still generates")
        void commonRarityExcludesRarePhase() {
            TemplateCondition rareGate = new RarityCondition(RARE);
            TemplatePhaseDefinition basePhase = new TemplatePhaseDefinition(
                    PhaseCompletionMode.ALL,
                    List.of(new TemplateStageDefinition(List.of(objective()))));
            TemplatePhaseDefinition rarePhase = new TemplatePhaseDefinition(
                    PhaseCompletionMode.ALL,
                    List.of(new TemplateStageDefinition(List.of(objective()))),
                    rareGate);
            QuestTemplate template = buildTemplate(List.of(basePhase, rarePhase));

            GeneratedQuestResult result = engine.generate(template, COMMON, new Random(1));
            assertEquals(1, result.definition().getPhases().size(),
                    "COMMON rarity should only include the unconditional phase");
        }

        @Test
        @DisplayName("LEGENDARY rarity → all phases included")
        void legendaryRarityIncludesAllPhases() {
            TemplateCondition rareGate = new RarityCondition(RARE);
            TemplatePhaseDefinition basePhase = new TemplatePhaseDefinition(
                    PhaseCompletionMode.ALL,
                    List.of(new TemplateStageDefinition(List.of(objective()))));
            TemplatePhaseDefinition rarePhase = new TemplatePhaseDefinition(
                    PhaseCompletionMode.ALL,
                    List.of(new TemplateStageDefinition(List.of(objective()))),
                    rareGate);
            QuestTemplate template = buildTemplate(List.of(basePhase, rarePhase));

            GeneratedQuestResult result = engine.generate(template, LEGENDARY, new Random(1));
            assertEquals(2, result.definition().getPhases().size(),
                    "LEGENDARY rarity should include all phases");
        }

        @Test
        @DisplayName("generated quest definition has valid structure")
        void generatedDefinitionIsValid() {
            TemplatePhaseDefinition phase = new TemplatePhaseDefinition(
                    PhaseCompletionMode.ALL,
                    List.of(new TemplateStageDefinition(List.of(objective()))));
            QuestTemplate template = buildTemplate(List.of(phase));

            GeneratedQuestResult result = engine.generate(template, COMMON, new Random(42));
            QuestDefinition def = result.definition();

            assertNotNull(def);
            assertNotNull(def.getQuestKey());
            assertFalse(def.getPhases().isEmpty());
        }
    }

    @Nested
    @DisplayName("Quest completion → distribution rewards granted")
    class QuestCompletionDistribution {

        @Test
        @DisplayName("completed quest fires distribution to all scope members")
        void completedQuestDistributesRewards() {
            PlayerMock player = server.addPlayer();
            UUID playerUUID = player.getUniqueId();

            MockQuestRewardType distReward = QuestTestHelper.mockRewardType("dist_reward");
            // MembershipDistributionType qualifies all scope members regardless of contribution,
            // so rewards are granted even when no objective progress was recorded.
            DistributionTierConfig tier = new DistributionTierConfig("membership",
                    MembershipDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(distReward), Map.of(), null, null, true);
            RewardDistributionConfig distConfig = new RewardDistributionConfig(List.of(tier));

            QuestDefinition def = QuestTestHelper.questDef(
                    "phase4_end_to_end",
                    List.of(QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL,
                            QuestTestHelper.singleStageDef("stage", "obj"))),
                    List.of(),
                    QuestRepeatMode.ONCE);

            // Wrap with distribution config using the builder
            QuestDefinition defWithDist = new QuestDefinition.Builder(
                    def.getQuestKey(),
                    def.getScopeType(),
                    def.getPhases()
            ).rewardDistribution(distConfig)
                    .build();

            QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(defWithDist, playerUUID);

            server.getPluginManager().callEvent(new QuestCompleteEvent(quest, defWithDist));

            verify(mockQuestManager, times(1)).retireQuest(any());
            assertEquals(1, distReward.getGrantCount(),
                    "Distribution reward should be granted at least once to the online scope member");
        }
    }

    @Nested
    @DisplayName("Distribution resolver — complete scenario")
    class FullDistributionScenario {

        @Test
        @DisplayName("two-tier config: top-contributor bonus + everyone participation")
        void twoTierDistributionScenario() {
            UUID p1 = UUID.randomUUID(), p2 = UUID.randomUUID(), p3 = UUID.randomUUID();

            QuestRewardType topBonus = scalableReward(500);
            QuestRewardType participation = scalableReward(100);

            DistributionTierConfig topTier = new DistributionTierConfig("top",
                    TopPlayersDistributionType.KEY,
                    RewardSplitMode.INDIVIDUAL,
                    List.of(topBonus),
                    Map.of(DistributionTierConfig.PARAM_TOP_PLAYER_COUNT, 1),
                    null, null, true);
            DistributionTierConfig allTier = new DistributionTierConfig("all",
                    ParticipatedDistributionType.KEY, RewardSplitMode.INDIVIDUAL,
                    List.of(participation), Map.of(), null, null, true);

            RewardDistributionTypeRegistry fullRegistry = new RewardDistributionTypeRegistry();
            fullRegistry.register(new ParticipatedDistributionType());
            fullRegistry.register(new TopPlayersDistributionType());

            RewardDistributionConfig config = new RewardDistributionConfig(List.of(topTier, allTier));
            var snapshot = new ContributionSnapshot(Map.of(p1, 70L, p2, 20L, p3, 10L), 100,
                    Set.of(p1, p2, p3), null);

            var result = new QuestRewardDistributionResolver(java.util.logging.Logger.getLogger("test")).resolve(
                    config, snapshot, null, new QuestRarityRegistry(), fullRegistry);

            // p1 is top contributor → gets both top-bonus and participation
            assertTrue(result.containsKey(p1));
            assertEquals(2, result.get(p1).size(), "Top contributor should receive bonus + participation");

            // p2 and p3 only get participation
            assertTrue(result.containsKey(p2));
            assertEquals(1, result.get(p2).size());
            assertTrue(result.containsKey(p3));
            assertEquals(1, result.get(p3).size());
        }
    }

    private QuestTemplate buildTemplate(List<TemplatePhaseDefinition> phases) {
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        List<TemplateRewardDefinition> rewards = List.of(
                new TemplateRewardDefinition(REWARD_TYPE_KEY, "test_reward", Map.of("amount", 100)));
        NamespacedKey templateKey = NamespacedKey.fromString("mcrpg:phase4_integration_template");
        return new QuestTemplate(templateKey, Route.fromString("test.display"),
                true, SCOPE_KEY, Set.of(COMMON, RARE, LEGENDARY),
                Map.of(), variables, phases, rewards, null, null);
    }

    private TemplateObjectiveDefinition objective() {
        return new TemplateObjectiveDefinition(OBJ_TYPE_KEY, "10", Map.of());
    }

    private QuestRewardType scalableReward(long amount) {
        QuestRewardType reward = mock(QuestRewardType.class);
        when(reward.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(reward.getNumericAmount()).thenReturn(OptionalLong.of(amount));
        when(reward.withAmountMultiplier(1.0)).thenReturn(reward);
        return reward;
    }
}
