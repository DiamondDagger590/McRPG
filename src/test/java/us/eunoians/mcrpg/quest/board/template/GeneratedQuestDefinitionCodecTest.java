package us.eunoians.mcrpg.quest.board.template;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionRewardEntry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.PotBehavior;
import us.eunoians.mcrpg.quest.board.distribution.RemainderStrategy;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.QuestRewardEntry;
import us.eunoians.mcrpg.quest.board.template.condition.RewardFallback;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GeneratedQuestDefinitionCodecTest {

    private static final NamespacedKey QUEST_KEY = NamespacedKey.fromString("mcrpg:gen_daily_mining_abcd1234");
    private static final NamespacedKey TEMPLATE_KEY = NamespacedKey.fromString("mcrpg:daily_mining");
    private static final NamespacedKey RARITY_KEY = NamespacedKey.fromString("mcrpg:rare");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");
    private static final NamespacedKey OBJECTIVE_TYPE_KEY = NamespacedKey.fromString("mcrpg:block_break");
    private static final NamespacedKey REWARD_TYPE_KEY = NamespacedKey.fromString("mcrpg:experience");
    private static final NamespacedKey STAGE_KEY = NamespacedKey.fromString("mcrpg:gen_daily_mining_abcd1234_p0s0");
    private static final NamespacedKey OBJECTIVE_KEY = NamespacedKey.fromString("mcrpg:gen_daily_mining_abcd1234_p0s0o0");

    private QuestObjectiveTypeRegistry objectiveTypeRegistry;
    private QuestRewardTypeRegistry rewardTypeRegistry;
    private TemplateConditionRegistry conditionRegistry;
    private GeneratedQuestDefinitionCodec codec;

    @BeforeEach
    void setUp() {
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);
        conditionRegistry = mock(TemplateConditionRegistry.class);
        codec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, rewardTypeRegistry, conditionRegistry);

        QuestObjectiveType mockObjType = mock(QuestObjectiveType.class);
        when(mockObjType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);
        when(mockObjType.parseConfig(any(Section.class))).thenReturn(mockObjType);
        when(objectiveTypeRegistry.get(OBJECTIVE_TYPE_KEY)).thenReturn(Optional.of(mockObjType));

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
    }

    @Test
    @DisplayName("Serialize -> deserialize round-trip produces equivalent definition")
    void roundTrip_producesEquivalentDefinition() {
        QuestDefinition original = createTestDefinition();
        ResolvedVariableContext context = createTestContext();
        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = createObjectiveConfigs();

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = codec.deserialize(json);

        assertEquals(original.getQuestKey(), deserialized.getQuestKey());
        assertEquals(original.getScopeType(), deserialized.getScopeType());
        assertEquals(original.getPhases().size(), deserialized.getPhases().size());
        assertEquals(original.getRewards().size(), deserialized.getRewards().size());
    }

    @Test
    @DisplayName("Serialized JSON contains all required top-level fields")
    void serialize_containsAllRequiredFields() {
        QuestDefinition definition = createTestDefinition();
        ResolvedVariableContext context = createTestContext();

        String json = codec.serialize(
                definition, TEMPLATE_KEY, RARITY_KEY, context, createObjectiveConfigs());
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        assertTrue(root.has("quest_key"));
        assertTrue(root.has("template_key"));
        assertTrue(root.has("rarity_key"));
        assertTrue(root.has("scope"));
        assertTrue(root.has("variables"));
        assertTrue(root.has("phases"));
        assertTrue(root.has("rewards"));

        assertEquals(QUEST_KEY.toString(), root.get("quest_key").getAsString());
        assertEquals(TEMPLATE_KEY.toString(), root.get("template_key").getAsString());
        assertEquals(RARITY_KEY.toString(), root.get("rarity_key").getAsString());
        assertEquals(SCOPE_KEY.toString(), root.get("scope").getAsString());
    }

    @Test
    @DisplayName("Deserialized definition has correct phases, stages, objectives")
    void deserialize_correctPhaseStageObjectiveStructure() {
        String json = serializeTestDefinition();

        QuestDefinition def = codec.deserialize(json);

        assertEquals(1, def.getPhases().size());
        QuestPhaseDefinition phase = def.getPhases().get(0);
        assertEquals(PhaseCompletionMode.ALL, phase.getCompletionMode());
        assertEquals(1, phase.getStages().size());

        QuestStageDefinition stage = phase.getStages().get(0);
        assertEquals(STAGE_KEY, stage.getStageKey());
        assertEquals(1, stage.getObjectives().size());

        QuestObjectiveDefinition obj = stage.getObjectives().get(0);
        assertEquals(OBJECTIVE_KEY, obj.getObjectiveKey());
        assertEquals(126L, obj.getRequiredProgress());
    }

    @Test
    @DisplayName("Deserialized definition has correct rewards")
    void deserialize_correctRewards() {
        String json = serializeTestDefinition();

        QuestDefinition def = codec.deserialize(json);

        assertEquals(1, def.getRewards().size());
        verify(rewardTypeRegistry).get(REWARD_TYPE_KEY);
    }

    @Test
    @DisplayName("Deserialized definition resolves objective types from registry")
    void deserialize_resolvesObjectiveTypesFromRegistry() {
        String json = serializeTestDefinition();

        codec.deserialize(json);

        verify(objectiveTypeRegistry).get(OBJECTIVE_TYPE_KEY);
    }

    @Test
    @DisplayName("Deserialization with unknown objective type throws QuestDeserializationException")
    void deserialize_unknownObjectiveType_throwsQuestDeserializationException() {
        String json = serializeTestDefinition();
        QuestObjectiveTypeRegistry emptyRegistry = mock(QuestObjectiveTypeRegistry.class);
        when(emptyRegistry.get(any())).thenReturn(Optional.empty());

        var emptyCodec = new GeneratedQuestDefinitionCodec(emptyRegistry, rewardTypeRegistry, conditionRegistry);
        QuestDeserializationException ex = assertThrows(QuestDeserializationException.class,
                () -> emptyCodec.deserialize(json));

        assertNotNull(ex.getQuestKey());
        assertTrue(ex.getFailedElement().contains("objective type"));
        assertTrue(ex.getMessage().contains(OBJECTIVE_TYPE_KEY.toString()));
    }

    @Test
    @DisplayName("Deserialization with unknown reward type throws QuestDeserializationException")
    void deserialize_unknownRewardType_throwsQuestDeserializationException() {
        String json = serializeTestDefinition();
        QuestRewardTypeRegistry emptyRegistry = mock(QuestRewardTypeRegistry.class);
        when(emptyRegistry.get(any())).thenReturn(Optional.empty());

        var emptyCodec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, emptyRegistry, conditionRegistry);
        QuestDeserializationException ex = assertThrows(QuestDeserializationException.class,
                () -> emptyCodec.deserialize(json));

        assertNotNull(ex.getQuestKey());
        assertTrue(ex.getFailedElement().contains("reward type"));
    }

    @Test
    @DisplayName("Round-trip preserves quest-level reward distribution")
    void roundTrip_preservesQuestLevelRewardDistribution() {
        QuestDefinition original = createDefinitionWithDistribution();
        ResolvedVariableContext context = createTestContext();
        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = createObjectiveConfigs();

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = codec.deserialize(json);

        assertTrue(deserialized.getRewardDistribution().isPresent(),
                "Quest-level reward distribution should survive roundtrip");
        RewardDistributionConfig dist = deserialized.getRewardDistribution().get();
        assertEquals(2, dist.getTiers().size());

        DistributionTierConfig firstTier = dist.getTiers().get(0);
        assertEquals("top-contributors", firstTier.getTierKey());
        assertEquals(NamespacedKey.fromString("mcrpg:top_players"), firstTier.getTypeKey());
        assertEquals(RewardSplitMode.INDIVIDUAL, firstTier.getSplitMode());
        assertEquals(1, firstTier.getRewards().size());

        DistributionTierConfig secondTier = dist.getTiers().get(1);
        assertEquals("all-members", secondTier.getTierKey());
        assertEquals(NamespacedKey.fromString("mcrpg:membership"), secondTier.getTypeKey());
        assertEquals(RewardSplitMode.SPLIT_EVEN, secondTier.getSplitMode());
    }

    @Test
    @DisplayName("Round-trip preserves stage-level reward distribution")
    void roundTrip_preservesStageLevelRewardDistribution() {
        QuestDefinition original = createDefinitionWithStageDistribution();
        ResolvedVariableContext context = createTestContext();
        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = createObjectiveConfigs();

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = codec.deserialize(json);

        QuestStageDefinition stage = deserialized.getPhases().get(0).getStages().get(0);
        assertTrue(stage.getRewardDistribution().isPresent(),
                "Stage-level reward distribution should survive roundtrip");
        assertEquals(1, stage.getRewardDistribution().get().getTiers().size());
    }

    @Test
    @DisplayName("Round-trip preserves phase-level reward distribution")
    void roundTrip_preservesPhaseLevelRewardDistribution() {
        QuestDefinition original = createDefinitionWithPhaseDistribution();
        ResolvedVariableContext context = createTestContext();
        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = createObjectiveConfigs();

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = codec.deserialize(json);

        QuestPhaseDefinition phase = deserialized.getPhases().get(0);
        assertTrue(phase.getRewardDistribution().isPresent(),
                "Phase-level reward distribution should survive roundtrip");
        assertEquals(1, phase.getRewardDistribution().get().getTiers().size());
    }

    @Test
    @DisplayName("Round-trip preserves distribution tier type parameters")
    void roundTrip_preservesDistributionTypeParameters() {
        QuestDefinition original = createDefinitionWithDistribution();
        ResolvedVariableContext context = createTestContext();
        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = createObjectiveConfigs();

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = codec.deserialize(json);

        DistributionTierConfig tier = deserialized.getRewardDistribution().get().getTiers().get(0);
        assertTrue(tier.getTopPlayerCount().isPresent());
        assertEquals(3, tier.getTopPlayerCount().get());
    }

    @Test
    @DisplayName("Round-trip preserves distribution tier rarity gates")
    void roundTrip_preservesDistributionRarityGates() {
        NamespacedKey minRarity = NamespacedKey.fromString("mcrpg:uncommon");
        NamespacedKey requiredRarity = NamespacedKey.fromString("mcrpg:legendary");

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("amount", 100));

        DistributionTierConfig tier = new DistributionTierConfig(
                "gated-tier", NamespacedKey.fromString("mcrpg:participated"),
                RewardSplitMode.INDIVIDUAL, List.of(rewardType),
                Map.of(), minRarity, requiredRarity, true);

        QuestDefinition original = createDefinitionWithCustomDistribution(
                new RewardDistributionConfig(List.of(tier)));

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = codec.deserialize(json);

        DistributionTierConfig deserializedTier = deserialized.getRewardDistribution().get().getTiers().get(0);
        assertTrue(deserializedTier.getMinRarity().isPresent());
        assertEquals(minRarity, deserializedTier.getMinRarity().get());
        assertTrue(deserializedTier.getRequiredRarity().isPresent());
        assertEquals(requiredRarity, deserializedTier.getRequiredRarity().get());
    }

    @Test
    @DisplayName("Round-trip with a locale-key on-start message preserves the locale key and returns empty inline list")
    void roundTrip_preservesLocaleKeyOnStartMessage() {
        QuestDefinition original = createDefinitionWithOnStartMessages(
                List.of(OnStartMessage.fromLocaleKey("quest.tutorial.started")));

        String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = codec.deserialize(json);

        assertEquals(1, deserialized.getOnStartMessages().size());
        OnStartMessage msg = deserialized.getOnStartMessages().get(0);
        assertTrue(msg.localeKey().isPresent());
        assertEquals("quest.tutorial.started", msg.localeKey().get());
        assertTrue(msg.inlineMessages().isEmpty());
    }

    @Test
    @DisplayName("Round-trip with an inline on-start message preserves all inline lines and returns empty locale key")
    void roundTrip_preservesInlineOnStartMessage() {
        List<String> lines = List.of("<primary>Quest started!", "<body>Collect 50 stone.");
        QuestDefinition original = createDefinitionWithOnStartMessages(
                List.of(OnStartMessage.fromInline(lines)));

        String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = codec.deserialize(json);

        assertEquals(1, deserialized.getOnStartMessages().size());
        OnStartMessage msg = deserialized.getOnStartMessages().get(0);
        assertTrue(msg.localeKey().isEmpty());
        assertEquals(lines, msg.inlineMessages());
    }

    @Test
    @DisplayName("Round-trip without on-start messages keeps the list empty")
    void roundTrip_noOnStartMessages_remainsEmpty() {
        QuestDefinition original = createTestDefinition();
        String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = codec.deserialize(json);
        assertTrue(deserialized.getOnStartMessages().isEmpty());
    }

    @Test
    @DisplayName("Round-trip without reward distribution keeps it empty")
    void roundTrip_noDistribution_remainsEmpty() {
        QuestDefinition original = createTestDefinition();

        String json = codec.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = codec.deserialize(json);

        assertTrue(deserialized.getRewardDistribution().isEmpty(),
                "Definition without reward distribution should remain empty after roundtrip");
    }

    @Nested
    @DisplayName("Reward fallback serialization")
    class RewardFallbackTests {

        private static final NamespacedKey CONDITION_TYPE_KEY = NamespacedKey.fromString("mcrpg:rarity_gate");
        private static final NamespacedKey FALLBACK_REWARD_KEY = NamespacedKey.fromString("mcrpg:experience");

        @Test
        @DisplayName("Round-trip preserves quest-level reward fallback with condition and fallback reward")
        void roundTrip_preservesRewardFallback() {
            TemplateCondition condition = mockCondition(CONDITION_TYPE_KEY, Map.of("min_rarity", "uncommon"));
            when(conditionRegistry.get(CONDITION_TYPE_KEY)).thenReturn(Optional.of(condition));

            QuestRewardType fallbackReward = mockReward(FALLBACK_REWARD_KEY, Map.of("amount", 50));
            RewardFallback fallback = new RewardFallback(condition, fallbackReward);

            QuestRewardType primaryReward = mockReward(REWARD_TYPE_KEY, Map.of("amount", 200));
            QuestRewardEntry entry = new QuestRewardEntry(primaryReward, fallback);

            QuestDefinition original = createDefinitionWithRewardEntries(List.of(entry));
            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject rewardObj = root.getAsJsonArray("rewards").get(0).getAsJsonObject();
            assertTrue(rewardObj.has("fallback"), "Serialized reward should contain fallback block");
            JsonObject fallbackObj = rewardObj.getAsJsonObject("fallback");
            assertEquals(CONDITION_TYPE_KEY.toString(), fallbackObj.get("condition_type").getAsString());
            assertEquals(FALLBACK_REWARD_KEY.toString(), fallbackObj.get("fallback_reward_type").getAsString());
        }

        @Test
        @DisplayName("Deserialization reconstructs reward fallback from condition and reward registries")
        void deserialize_reconstructsRewardFallback() {
            TemplateCondition condition = mockCondition(CONDITION_TYPE_KEY, Map.of("min_rarity", "uncommon"));
            when(conditionRegistry.get(CONDITION_TYPE_KEY)).thenReturn(Optional.of(condition));

            QuestRewardType fallbackReward = mockReward(FALLBACK_REWARD_KEY, Map.of("amount", 50));
            RewardFallback fallback = new RewardFallback(condition, fallbackReward);

            QuestRewardType primaryReward = mockReward(REWARD_TYPE_KEY, Map.of("amount", 200));
            QuestRewardEntry entry = new QuestRewardEntry(primaryReward, fallback);

            QuestDefinition original = createDefinitionWithRewardEntries(List.of(entry));
            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());

            QuestDefinition deserialized = codec.deserialize(json);
            assertEquals(1, deserialized.getRewardEntries().size());
            QuestRewardEntry deserializedEntry = deserialized.getRewardEntries().get(0);
            assertNotNull(deserializedEntry.fallback());
            verify(conditionRegistry).get(CONDITION_TYPE_KEY);
        }

        @Test
        @DisplayName("Deserialization with unknown condition type in fallback throws QuestDeserializationException")
        void deserialize_unknownConditionType_throwsException() {
            TemplateCondition condition = mockCondition(CONDITION_TYPE_KEY, Map.of("threshold", 5));
            when(conditionRegistry.get(CONDITION_TYPE_KEY)).thenReturn(Optional.of(condition));

            QuestRewardType fallbackReward = mockReward(FALLBACK_REWARD_KEY, Map.of("amount", 10));
            QuestRewardEntry entry = new QuestRewardEntry(
                    mockReward(REWARD_TYPE_KEY, Map.of("amount", 100)),
                    new RewardFallback(condition, fallbackReward));

            String json = codec.serialize(
                    createDefinitionWithRewardEntries(List.of(entry)),
                    TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());

            TemplateConditionRegistry emptyCondRegistry = mock(TemplateConditionRegistry.class);
            when(emptyCondRegistry.get(any())).thenReturn(Optional.empty());
            var failCodec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, rewardTypeRegistry, emptyCondRegistry);

            QuestDeserializationException ex = assertThrows(QuestDeserializationException.class,
                    () -> failCodec.deserialize(json));
            assertTrue(ex.getFailedElement().contains("condition type"));
        }

        private TemplateCondition mockCondition(NamespacedKey key, Map<String, Object> config) {
            TemplateCondition condition = mock(TemplateCondition.class);
            when(condition.getKey()).thenReturn(key);
            when(condition.serializeConfig()).thenReturn(config);
            when(condition.fromConfig(any(Section.class), any(ConditionParser.class))).thenReturn(condition);
            return condition;
        }

        private QuestRewardType mockReward(NamespacedKey key, Map<String, Object> config) {
            QuestRewardType reward = mock(QuestRewardType.class);
            when(reward.getKey()).thenReturn(key);
            when(reward.serializeConfig()).thenReturn(config);
            when(reward.fromSerializedConfig(any())).thenAnswer(inv -> {
                Map<String, Object> c = inv.getArgument(0);
                QuestRewardType configured = mock(QuestRewardType.class);
                when(configured.getKey()).thenReturn(key);
                when(configured.serializeConfig()).thenReturn(new LinkedHashMap<>(c));
                when(configured.withLocalizationRoute(any())).thenReturn(configured);
                return configured;
            });
            when(rewardTypeRegistry.get(key)).thenReturn(Optional.of(reward));
            return reward;
        }
    }

    @Nested
    @DisplayName("Phase rewards serialization")
    class PhaseRewardsTests {

        @Test
        @DisplayName("Round-trip preserves phase-level rewards")
        void roundTrip_preservesPhaseRewards() {
            QuestRewardType phaseReward = mock(QuestRewardType.class);
            when(phaseReward.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(phaseReward.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 250));

            QuestObjectiveType objType = mock(QuestObjectiveType.class);
            when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

            QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                    OBJECTIVE_KEY, objType, 50L, List.of(), null);
            QuestStageDefinition stage = new QuestStageDefinition(
                    STAGE_KEY, List.of(objective), List.of(), null);
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ALL, List.of(stage), List.of(phaseReward), null);

            QuestDefinition original = new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                    .build();

            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
            QuestDefinition deserialized = codec.deserialize(json);

            QuestPhaseDefinition deserializedPhase = deserialized.getPhases().get(0);
            assertEquals(1, deserializedPhase.getRewards().size());
            assertEquals(REWARD_TYPE_KEY, deserializedPhase.getRewards().get(0).getKey());
        }

        @Test
        @DisplayName("Phase with unknown reward type throws QuestDeserializationException")
        void deserialize_unknownPhaseRewardType_throwsException() {
            NamespacedKey unknownKey = NamespacedKey.fromString("mcrpg:unknown_reward");
            QuestRewardType phaseReward = mock(QuestRewardType.class);
            when(phaseReward.getKey()).thenReturn(unknownKey);
            when(phaseReward.serializeConfig()).thenReturn(Map.of("value", 1));

            QuestObjectiveType objType = mock(QuestObjectiveType.class);
            when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

            QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                    OBJECTIVE_KEY, objType, 10L, List.of(), null);
            QuestStageDefinition stage = new QuestStageDefinition(
                    STAGE_KEY, List.of(objective), List.of(), null);
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ALL, List.of(stage), List.of(phaseReward), null);

            QuestDefinition original = new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                    .build();

            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());

            Optional<QuestRewardType> knownReward = rewardTypeRegistry.get(REWARD_TYPE_KEY);
            QuestRewardTypeRegistry restrictedRegistry = mock(QuestRewardTypeRegistry.class);
            when(restrictedRegistry.get(REWARD_TYPE_KEY)).thenReturn(knownReward);
            when(restrictedRegistry.get(unknownKey)).thenReturn(Optional.empty());

            var failCodec = new GeneratedQuestDefinitionCodec(objectiveTypeRegistry, restrictedRegistry, conditionRegistry);
            QuestDeserializationException ex = assertThrows(QuestDeserializationException.class,
                    () -> failCodec.deserialize(json));
            assertTrue(ex.getFailedElement().contains("phase reward type"));
        }
    }

    @Nested
    @DisplayName("DistributionRewardEntry fields")
    class DistributionRewardEntryTests {

        @Test
        @DisplayName("Round-trip preserves pot_behavior, remainder_strategy, min_scaled_amount, and top_count")
        void roundTrip_preservesAllDistributionRewardEntryFields() {
            QuestRewardType reward = mock(QuestRewardType.class);
            when(reward.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(reward.serializeConfig()).thenReturn(Map.of("amount", 500));

            DistributionRewardEntry entry = new DistributionRewardEntry(
                    reward, PotBehavior.TOP_N, RemainderStrategy.TOP_CONTRIBUTOR, 5, 3, null);

            DistributionTierConfig tier = new DistributionTierConfig(
                    "test-tier", NamespacedKey.fromString("mcrpg:top_players"),
                    RewardSplitMode.INDIVIDUAL, List.of(entry),
                    Map.of(), null, null);

            QuestDefinition original = createDefinitionWithCustomDistribution(
                    new RewardDistributionConfig(List.of(tier)));

            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
            QuestDefinition deserialized = codec.deserialize(json);

            DistributionTierConfig deserializedTier = deserialized.getRewardDistribution().get().getTiers().get(0);
            DistributionRewardEntry deserializedEntry = deserializedTier.getRewardEntries().get(0);

            assertEquals(PotBehavior.TOP_N, deserializedEntry.potBehavior());
            assertEquals(RemainderStrategy.TOP_CONTRIBUTOR, deserializedEntry.remainderStrategy());
            assertEquals(5, deserializedEntry.minScaledAmount());
            assertEquals(3, deserializedEntry.topCount());
        }

        @Test
        @DisplayName("Deserialization defaults to SCALE pot behavior when field is absent")
        void deserialize_missingPotBehavior_defaultsToScale() {
            String json = buildDistributionJsonWithoutField("pot_behavior");
            QuestDefinition deserialized = codec.deserialize(json);

            DistributionRewardEntry entry = deserialized.getRewardDistribution()
                    .get().getTiers().get(0).getRewardEntries().get(0);
            assertEquals(PotBehavior.SCALE, entry.potBehavior());
        }

        @Test
        @DisplayName("Deserialization defaults to DISCARD remainder strategy when field is absent")
        void deserialize_missingRemainderStrategy_defaultsToDiscard() {
            String json = buildDistributionJsonWithoutField("remainder_strategy");
            QuestDefinition deserialized = codec.deserialize(json);

            DistributionRewardEntry entry = deserialized.getRewardDistribution()
                    .get().getTiers().get(0).getRewardEntries().get(0);
            assertEquals(RemainderStrategy.DISCARD, entry.remainderStrategy());
        }

        @Test
        @DisplayName("Deserialization defaults min_scaled_amount to 1 when field is absent")
        void deserialize_missingMinScaledAmount_defaultsToOne() {
            String json = buildDistributionJsonWithoutField("min_scaled_amount");
            QuestDefinition deserialized = codec.deserialize(json);

            DistributionRewardEntry entry = deserialized.getRewardDistribution()
                    .get().getTiers().get(0).getRewardEntries().get(0);
            assertEquals(1, entry.minScaledAmount());
        }

        @Test
        @DisplayName("Deserialization defaults top_count to 1 when field is absent")
        void deserialize_missingTopCount_defaultsToOne() {
            String json = buildDistributionJsonWithoutField("top_count");
            QuestDefinition deserialized = codec.deserialize(json);

            DistributionRewardEntry entry = deserialized.getRewardDistribution()
                    .get().getTiers().get(0).getRewardEntries().get(0);
            assertEquals(1, entry.topCount());
        }

        private String buildDistributionJsonWithoutField(String fieldToRemove) {
            QuestRewardType reward = mock(QuestRewardType.class);
            when(reward.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(reward.serializeConfig()).thenReturn(Map.of("amount", 100));

            DistributionRewardEntry entry = new DistributionRewardEntry(
                    reward, PotBehavior.ALL, RemainderStrategy.RANDOM, 10, 5, null);

            DistributionTierConfig tier = new DistributionTierConfig(
                    "tier", NamespacedKey.fromString("mcrpg:participated"),
                    RewardSplitMode.INDIVIDUAL, List.of(entry),
                    Map.of(), null, null);

            QuestDefinition def = createDefinitionWithCustomDistribution(
                    new RewardDistributionConfig(List.of(tier)));

            String json = codec.serialize(def, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject rewardObj = root.getAsJsonObject("reward_distribution")
                    .getAsJsonArray("tiers").get(0).getAsJsonObject()
                    .getAsJsonArray("rewards").get(0).getAsJsonObject();
            rewardObj.remove(fieldToRemove);

            return root.toString();
        }
    }

    @Nested
    @DisplayName("Inline display serialization")
    class InlineDisplayTests {

        @Test
        @DisplayName("Round-trip preserves inline display entries")
        void roundTrip_preservesInlineDisplay() {
            Map<String, String> display = new LinkedHashMap<>();
            display.put("name", "<primary>Daily Mining Quest");
            display.put("description", "<body>Mine blocks to earn rewards");

            QuestDefinition original = createDefinitionWithInlineDisplay(display);
            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
            QuestDefinition deserialized = codec.deserialize(json);

            assertEquals(2, deserialized.getInlineDisplay().size());
            assertEquals("<primary>Daily Mining Quest", deserialized.getInlineDisplay().get("name"));
            assertEquals("<body>Mine blocks to earn rewards", deserialized.getInlineDisplay().get("description"));
        }

        @Test
        @DisplayName("Definition without inline display deserializes with empty map")
        void roundTrip_noInlineDisplay_remainsEmpty() {
            QuestDefinition original = createTestDefinition();
            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
            QuestDefinition deserialized = codec.deserialize(json);
            assertTrue(deserialized.getInlineDisplay().isEmpty());
        }
    }

    @Nested
    @DisplayName("Variable type diversity in serialization")
    class VariableTypeTests {

        @Test
        @DisplayName("Boolean variable values survive round-trip through toJsonElement")
        void serialize_booleanVariable_survivesRoundTrip() {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("require_silk_touch", true);
            values.put("block_count", 50L);
            ResolvedVariableContext context = new ResolvedVariableContext(values, 1.0, 1.0, 1.0);

            QuestDefinition def = createTestDefinition();
            String json = codec.serialize(def, TEMPLATE_KEY, RARITY_KEY, context, createObjectiveConfigs());
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject vars = root.getAsJsonObject("variables");

            assertTrue(vars.get("require_silk_touch").getAsBoolean());
        }

        @Test
        @DisplayName("String variable values survive round-trip through toJsonElement")
        void serialize_stringVariable_survivesRoundTrip() {
            Map<String, Object> values = new LinkedHashMap<>();
            values.put("biome_name", "PLAINS");
            values.put("count", 10);
            ResolvedVariableContext context = new ResolvedVariableContext(values, 1.0, 1.0, 1.0);

            QuestDefinition def = createTestDefinition();
            String json = codec.serialize(def, TEMPLATE_KEY, RARITY_KEY, context, createObjectiveConfigs());
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject vars = root.getAsJsonObject("variables");

            assertEquals("PLAINS", vars.get("biome_name").getAsString());
        }
    }

    @Nested
    @DisplayName("Objective-level distribution")
    class ObjectiveLevelDistributionTests {

        @Test
        @DisplayName("Round-trip preserves objective-level reward distribution")
        void roundTrip_preservesObjectiveLevelDistribution() {
            QuestRewardType reward = mock(QuestRewardType.class);
            when(reward.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(reward.serializeConfig()).thenReturn(Map.of("amount", 75));

            DistributionTierConfig tier = new DistributionTierConfig(
                    "obj-tier", NamespacedKey.fromString("mcrpg:participated"),
                    RewardSplitMode.SPLIT_EVEN, List.of(reward),
                    Map.of(), null, null, true);
            RewardDistributionConfig objDist = new RewardDistributionConfig(List.of(tier));

            QuestObjectiveType objType = mock(QuestObjectiveType.class);
            when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

            QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                    OBJECTIVE_KEY, objType, 30L, List.of(), objDist);
            QuestStageDefinition stage = new QuestStageDefinition(
                    STAGE_KEY, List.of(objective), List.of(), null);
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

            QuestDefinition original = new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                    .build();

            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(),
                    Map.of(OBJECTIVE_KEY, Map.of("blocks", List.of("STONE"))));
            QuestDefinition deserialized = codec.deserialize(json);

            QuestObjectiveDefinition deserializedObj = deserialized.getPhases().get(0)
                    .getStages().get(0).getObjectives().get(0);
            assertTrue(deserializedObj.getRewardDistribution().isPresent(),
                    "Objective-level distribution should survive round-trip");
            assertEquals(1, deserializedObj.getRewardDistribution().get().getTiers().size());
            assertEquals("obj-tier", deserializedObj.getRewardDistribution().get().getTiers().get(0).getTierKey());
        }
    }

    @Nested
    @DisplayName("Distribution reward entry with fallback")
    class DistributionRewardEntryFallbackTests {

        @Test
        @DisplayName("Round-trip preserves fallback on distribution reward entry")
        void roundTrip_preservesFallbackOnDistributionEntry() {
            NamespacedKey condKey = NamespacedKey.fromString("mcrpg:rarity_gate");
            TemplateCondition condition = mock(TemplateCondition.class);
            when(condition.getKey()).thenReturn(condKey);
            when(condition.serializeConfig()).thenReturn(Map.of("min", "rare"));
            when(condition.fromConfig(any(Section.class), any(ConditionParser.class))).thenReturn(condition);
            when(conditionRegistry.get(condKey)).thenReturn(Optional.of(condition));

            QuestRewardType primary = mock(QuestRewardType.class);
            when(primary.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(primary.serializeConfig()).thenReturn(Map.of("amount", 500));

            QuestRewardType fallbackReward = mock(QuestRewardType.class);
            when(fallbackReward.getKey()).thenReturn(REWARD_TYPE_KEY);
            when(fallbackReward.serializeConfig()).thenReturn(Map.of("amount", 100));

            RewardFallback fallback = new RewardFallback(condition, fallbackReward);
            DistributionRewardEntry entry = new DistributionRewardEntry(
                    primary, PotBehavior.SCALE, RemainderStrategy.DISCARD, 1, 1, fallback);

            DistributionTierConfig tier = new DistributionTierConfig(
                    "fb-tier", NamespacedKey.fromString("mcrpg:participated"),
                    RewardSplitMode.INDIVIDUAL, List.of(entry),
                    Map.of(), null, null);

            QuestDefinition original = createDefinitionWithCustomDistribution(
                    new RewardDistributionConfig(List.of(tier)));

            String json = codec.serialize(original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject distReward = root.getAsJsonObject("reward_distribution")
                    .getAsJsonArray("tiers").get(0).getAsJsonObject()
                    .getAsJsonArray("rewards").get(0).getAsJsonObject();
            assertTrue(distReward.has("fallback"),
                    "Distribution reward entry should contain fallback block in JSON");

            QuestDefinition deserialized = codec.deserialize(json);
            RewardDistributionConfig deserializedDist = deserialized.getRewardDistribution().orElseThrow();
            DistributionRewardEntry deserializedEntry = deserializedDist.getTiers().get(0).getRewardEntries().get(0);
            assertNotNull(deserializedEntry.fallback(),
                    "Deserialized distribution reward entry should have a non-null fallback");
        }
    }

    private QuestDefinition createTestDefinition() {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);

        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), null);

        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(
                Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .rewards(List.of(rewardType))
                .build();
    }

    private ResolvedVariableContext createTestContext() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("target_blocks", List.of("IRON_ORE", "COPPER_ORE", "DIAMOND_ORE"));
        values.put("block_count", 126L);
        values.put("difficulty", 2.625);
        return new ResolvedVariableContext(values, 1.75, 1.5, 2.625);
    }

    private Map<NamespacedKey, Map<String, Object>> createObjectiveConfigs() {
        return Map.of(OBJECTIVE_KEY,
                Map.of("blocks", List.of("IRON_ORE", "COPPER_ORE", "DIAMOND_ORE")));
    }

    private String serializeTestDefinition() {
        return codec.serialize(
                createTestDefinition(), TEMPLATE_KEY, RARITY_KEY,
                createTestContext(), createObjectiveConfigs());
    }

    private RewardDistributionConfig createTwoTierDistribution() {
        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 500));

        DistributionTierConfig topTier = new DistributionTierConfig(
                "top-contributors", NamespacedKey.fromString("mcrpg:top_players"),
                RewardSplitMode.INDIVIDUAL, List.of(rewardType),
                Map.of(DistributionTierConfig.PARAM_TOP_PLAYER_COUNT, 3),
                null, null, true);

        DistributionTierConfig memberTier = new DistributionTierConfig(
                "all-members", NamespacedKey.fromString("mcrpg:membership"),
                RewardSplitMode.SPLIT_EVEN, List.of(rewardType),
                Map.of(), null, null, true);

        return new RewardDistributionConfig(List.of(topTier, memberTier));
    }

    private RewardDistributionConfig createSingleTierDistribution() {
        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("amount", 250));

        DistributionTierConfig tier = new DistributionTierConfig(
                "participated", NamespacedKey.fromString("mcrpg:participated"),
                RewardSplitMode.INDIVIDUAL, List.of(rewardType),
                Map.of(), null, null, true);

        return new RewardDistributionConfig(List.of(tier));
    }

    private QuestDefinition createDefinitionWithDistribution() {
        return createDefinitionWithCustomDistribution(createTwoTierDistribution());
    }

    private QuestDefinition createDefinitionWithCustomDistribution(@NotNull RewardDistributionConfig dist) {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .rewards(List.of(rewardType))
                .rewardDistribution(dist)
                .build();
    }

    private QuestDefinition createDefinitionWithStageDistribution() {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), createSingleTierDistribution());
        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .rewards(List.of(rewardType))
                .build();
    }

    private QuestDefinition createDefinitionWithOnStartMessages(@NotNull List<OnStartMessage> messages) {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(OBJECTIVE_KEY, objType, 1L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(STAGE_KEY, List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .onStartMessages(messages)
                .build();
    }

    private QuestDefinition createDefinitionWithPhaseDistribution() {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), List.of(), createSingleTierDistribution());

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .rewards(List.of(rewardType))
                .build();
    }

    private QuestDefinition createDefinitionWithRewardEntries(@NotNull List<QuestRewardEntry> entries) {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(OBJECTIVE_KEY, objType, 1L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(STAGE_KEY, List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .rewardEntries(entries)
                .build();
    }

    private QuestDefinition createDefinitionWithInlineDisplay(@NotNull Map<String, String> display) {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(OBJECTIVE_KEY, objType, 1L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(STAGE_KEY, List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

        return new QuestDefinition.Builder(QUEST_KEY, SCOPE_KEY, List.of(phase))
                .inlineDisplay(display)
                .build();
    }
}
