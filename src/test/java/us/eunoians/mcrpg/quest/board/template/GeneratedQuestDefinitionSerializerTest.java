package us.eunoians.mcrpg.quest.board.template;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GeneratedQuestDefinitionSerializerTest {

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

    @BeforeEach
    void setUp() {
        objectiveTypeRegistry = mock(QuestObjectiveTypeRegistry.class);
        rewardTypeRegistry = mock(QuestRewardTypeRegistry.class);

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

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

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

        String json = GeneratedQuestDefinitionSerializer.serialize(
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

        QuestDefinition def = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

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

        QuestDefinition def = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

        assertEquals(1, def.getRewards().size());
        verify(rewardTypeRegistry).get(REWARD_TYPE_KEY);
    }

    @Test
    @DisplayName("Deserialized definition resolves objective types from registry")
    void deserialize_resolvesObjectiveTypesFromRegistry() {
        String json = serializeTestDefinition();

        GeneratedQuestDefinitionSerializer.deserialize(json, objectiveTypeRegistry, rewardTypeRegistry);

        verify(objectiveTypeRegistry).get(OBJECTIVE_TYPE_KEY);
    }

    @Test
    @DisplayName("Deserialization with unknown objective type throws QuestDeserializationException")
    void deserialize_unknownObjectiveType_throwsQuestDeserializationException() {
        String json = serializeTestDefinition();
        QuestObjectiveTypeRegistry emptyRegistry = mock(QuestObjectiveTypeRegistry.class);
        when(emptyRegistry.get(any())).thenReturn(Optional.empty());

        QuestDeserializationException ex = assertThrows(QuestDeserializationException.class,
                () -> GeneratedQuestDefinitionSerializer.deserialize(json, emptyRegistry, rewardTypeRegistry));

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

        QuestDeserializationException ex = assertThrows(QuestDeserializationException.class,
                () -> GeneratedQuestDefinitionSerializer.deserialize(json, objectiveTypeRegistry, emptyRegistry));

        assertNotNull(ex.getQuestKey());
        assertTrue(ex.getFailedElement().contains("reward type"));
    }

    @Test
    @DisplayName("Round-trip preserves quest-level reward distribution")
    void roundTrip_preservesQuestLevelRewardDistribution() {
        QuestDefinition original = createDefinitionWithDistribution();
        ResolvedVariableContext context = createTestContext();
        Map<NamespacedKey, Map<String, Object>> objectiveConfigs = createObjectiveConfigs();

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

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

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

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

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

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

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, context, objectiveConfigs);
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

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

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

        DistributionTierConfig deserializedTier = deserialized.getRewardDistribution().get().getTiers().get(0);
        assertTrue(deserializedTier.getMinRarity().isPresent());
        assertEquals(minRarity, deserializedTier.getMinRarity().get());
        assertTrue(deserializedTier.getRequiredRarity().isPresent());
        assertEquals(requiredRarity, deserializedTier.getRequiredRarity().get());
    }

    @Test
    @DisplayName("Round-trip without reward distribution keeps it empty")
    void roundTrip_noDistribution_remainsEmpty() {
        QuestDefinition original = createTestDefinition();

        String json = GeneratedQuestDefinitionSerializer.serialize(
                original, TEMPLATE_KEY, RARITY_KEY, createTestContext(), createObjectiveConfigs());
        QuestDefinition deserialized = GeneratedQuestDefinitionSerializer.deserialize(
                json, objectiveTypeRegistry, rewardTypeRegistry);

        assertTrue(deserialized.getRewardDistribution().isEmpty(),
                "Definition without reward distribution should remain empty after roundtrip");
    }

    private QuestDefinition createTestDefinition() {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);

        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), null);

        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), null);

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(
                Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition(
                QUEST_KEY,
                SCOPE_KEY,
                null,
                List.of(phase),
                List.of(rewardType),
                QuestRepeatMode.ONCE,
                null,
                -1,
                null);
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
        return GeneratedQuestDefinitionSerializer.serialize(
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
                0, PhaseCompletionMode.ALL, List.of(stage), null);

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition(
                QUEST_KEY, SCOPE_KEY, null, List.of(phase), List.of(rewardType),
                QuestRepeatMode.ONCE, null, -1, null, null, dist);
    }

    private QuestDefinition createDefinitionWithStageDistribution() {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), createSingleTierDistribution());
        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), null);

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition(
                QUEST_KEY, SCOPE_KEY, null, List.of(phase), List.of(rewardType),
                QuestRepeatMode.ONCE, null, -1, null);
    }

    private QuestDefinition createDefinitionWithPhaseDistribution() {
        QuestObjectiveType objType = mock(QuestObjectiveType.class);
        when(objType.getKey()).thenReturn(OBJECTIVE_TYPE_KEY);

        QuestObjectiveDefinition objective = new QuestObjectiveDefinition(
                OBJECTIVE_KEY, objType, 126L, List.of(), null);
        QuestStageDefinition stage = new QuestStageDefinition(
                STAGE_KEY, List.of(objective), List.of(), null);
        QuestPhaseDefinition phase = new QuestPhaseDefinition(
                0, PhaseCompletionMode.ALL, List.of(stage), createSingleTierDistribution());

        QuestRewardType rewardType = mock(QuestRewardType.class);
        when(rewardType.getKey()).thenReturn(REWARD_TYPE_KEY);
        when(rewardType.serializeConfig()).thenReturn(Map.of("skill", "MINING", "amount", 1654));

        return new QuestDefinition(
                QUEST_KEY, SCOPE_KEY, null, List.of(phase), List.of(rewardType),
                QuestRepeatMode.ONCE, null, -1, null);
    }
}
