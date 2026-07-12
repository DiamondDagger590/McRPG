package us.eunoians.mcrpg.quest.board.generation;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.board.TemplateQuestGenerateEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.board.template.TemplateObjectiveDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplatePhaseDefinition;
import us.eunoians.mcrpg.quest.board.template.TemplateStageDefinition;
import us.eunoians.mcrpg.quest.board.template.variable.RangeVariable;
import us.eunoians.mcrpg.quest.board.template.variable.TemplateVariable;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionMetadata;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import us.eunoians.mcrpg.quest.board.template.condition.CompletionPrerequisiteCondition;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionContext;
import us.eunoians.mcrpg.quest.board.template.condition.QuestCompletionHistory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestPoolTest extends McRPGBaseTest {

    private static final NamespacedKey COMMON_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey RARE_KEY = new NamespacedKey("mcrpg", "rare");
    private static final NamespacedKey EPIC_KEY = new NamespacedKey("mcrpg", "epic");

    private QuestDefinitionRegistry registry;
    private QuestTemplateRegistry templateRegistry;
    private QuestTemplateEngine templateEngine;
    private QuestPool questPool;

    @BeforeEach
    void setUp() {
        registry = new QuestDefinitionRegistry();
        templateRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_TEMPLATE);
        templateEngine = mock(QuestTemplateEngine.class);
        questPool = new QuestPool(registry, templateRegistry, mcRPG.getLogger(), mcRPG.getTimeProvider());
    }

    private static final NamespacedKey SINGLE_PLAYER_SCOPE = new NamespacedKey("mcrpg", "single_player");

    private QuestDefinition questWithBoardMetadata(String questKey, boolean boardEligible, Set<NamespacedKey> supportedRarities) {
        var stage = QuestTestHelper.singleStageDef(questKey + "_stage", questKey + "_obj");
        var phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
        Map<NamespacedKey, QuestDefinitionMetadata> metadata = Map.of(
                BoardMetadata.METADATA_KEY, new BoardMetadata(boardEligible, supportedRarities, Set.of(), null, null)
        );
        return new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", questKey),
                SINGLE_PLAYER_SCOPE,
                List.of(phase)
        ).metadata(metadata)
                .build();
    }

    @DisplayName("getEligibleDefinitions: returns only definitions with matching rarity in supportedRarities")
    @Test
    void getEligibleDefinitions_returnsOnlyMatchingRarity() {
        QuestDefinition commonQuest = questWithBoardMetadata("common_quest", true, Set.of(COMMON_KEY));
        QuestDefinition rareQuest = questWithBoardMetadata("rare_quest", true, Set.of(RARE_KEY));
        QuestDefinition bothQuest = questWithBoardMetadata("both_quest", true, Set.of(COMMON_KEY, RARE_KEY));

        registry.register(commonQuest);
        registry.register(rareQuest);
        registry.register(bothQuest);

        List<NamespacedKey> commonEligible = questPool.getEligibleDefinitions(COMMON_KEY);
        assertEquals(2, commonEligible.size());
        assertTrue(commonEligible.contains(commonQuest.getQuestKey()));
        assertTrue(commonEligible.contains(bothQuest.getQuestKey()));
        assertFalse(commonEligible.contains(rareQuest.getQuestKey()));

        List<NamespacedKey> rareEligible = questPool.getEligibleDefinitions(RARE_KEY);
        assertEquals(2, rareEligible.size());
        assertTrue(rareEligible.contains(rareQuest.getQuestKey()));
        assertTrue(rareEligible.contains(bothQuest.getQuestKey()));
    }

    @DisplayName("getEligibleDefinitions: empty supportedRarities matches every rolled rarity")
    @Test
    void getEligibleDefinitions_emptySupportedRarities_matchesAnyRarity() {
        QuestDefinition anyRarityQuest = questWithBoardMetadata("any_rarity_quest", true, Set.of());
        registry.register(anyRarityQuest);

        assertTrue(questPool.getEligibleDefinitions(COMMON_KEY).contains(anyRarityQuest.getQuestKey()));
        assertTrue(questPool.getEligibleDefinitions(RARE_KEY).contains(anyRarityQuest.getQuestKey()));
        assertTrue(questPool.getEligibleDefinitions(EPIC_KEY).contains(anyRarityQuest.getQuestKey()));
    }

    @DisplayName("getEligibleDefinitions: definitions without board metadata excluded")
    @Test
    void getEligibleDefinitions_definitionsWithoutBoardMetadataExcluded() {
        QuestDefinition noBoardMeta = QuestTestHelper.singlePhaseQuest("no_board");
        QuestDefinition withBoardMeta = questWithBoardMetadata("with_board", true, Set.of(COMMON_KEY));

        registry.register(noBoardMeta);
        registry.register(withBoardMeta);

        List<NamespacedKey> eligible = questPool.getEligibleDefinitions(COMMON_KEY);
        assertEquals(1, eligible.size());
        assertEquals(withBoardMeta.getQuestKey(), eligible.get(0));
    }

    @DisplayName("getEligibleDefinitions: boardEligible=false excluded")
    @Test
    void getEligibleDefinitions_boardEligibleFalseExcluded() {
        QuestDefinition ineligible = questWithBoardMetadata("ineligible", false, Set.of(COMMON_KEY));
        QuestDefinition eligible = questWithBoardMetadata("eligible", true, Set.of(COMMON_KEY));

        registry.register(ineligible);
        registry.register(eligible);

        List<NamespacedKey> result = questPool.getEligibleDefinitions(COMMON_KEY);
        assertEquals(1, result.size());
        assertEquals(eligible.getQuestKey(), result.get(0));
    }

    @DisplayName("getEligibleDefinitions: empty registry returns empty list")
    @Test
    void getEligibleDefinitions_emptyRegistry_returnsEmptyList() {
        List<NamespacedKey> result = questPool.getEligibleDefinitions(COMMON_KEY);
        assertTrue(result.isEmpty());
    }

    @DisplayName("getAllBoardEligibleDefinitions: returns all board-eligible definitions regardless of rarity")
    @Test
    void getAllBoardEligibleDefinitions_returnsAllRegardlessOfRarity() {
        QuestDefinition commonQuest = questWithBoardMetadata("common_quest", true, Set.of(COMMON_KEY));
        QuestDefinition rareQuest = questWithBoardMetadata("rare_quest", true, Set.of(RARE_KEY));
        QuestDefinition epicQuest = questWithBoardMetadata("epic_quest", true, Set.of(EPIC_KEY));

        registry.register(commonQuest);
        registry.register(rareQuest);
        registry.register(epicQuest);

        List<NamespacedKey> all = questPool.getAllBoardEligibleDefinitions();
        assertEquals(3, all.size());
        assertTrue(all.contains(commonQuest.getQuestKey()));
        assertTrue(all.contains(rareQuest.getQuestKey()));
        assertTrue(all.contains(epicQuest.getQuestKey()));
    }

    @DisplayName("getAllBoardEligibleDefinitions: non-eligible excluded")
    @Test
    void getAllBoardEligibleDefinitions_nonEligibleExcluded() {
        QuestDefinition eligible = questWithBoardMetadata("eligible", true, Set.of(COMMON_KEY));
        QuestDefinition ineligible = questWithBoardMetadata("ineligible", false, Set.of(COMMON_KEY));
        QuestDefinition noBoardMeta = QuestTestHelper.singlePhaseQuest("no_meta");

        registry.register(eligible);
        registry.register(ineligible);
        registry.register(noBoardMeta);

        List<NamespacedKey> result = questPool.getAllBoardEligibleDefinitions();
        assertEquals(1, result.size());
        assertEquals(eligible.getQuestKey(), result.get(0));
    }

    @DisplayName("getEligibleTemplates: returns only templates matching rarity")
    @Test
    void getEligibleTemplates_returnsOnlyMatchingRarity() {
        QuestTemplate commonTemplate = createTemplate("common_tmpl", Set.of(COMMON_KEY));
        QuestTemplate rareTemplate = createTemplate("rare_tmpl", Set.of(RARE_KEY));
        templateRegistry.register(commonTemplate);
        templateRegistry.register(rareTemplate);

        List<QuestTemplate> eligible = questPool.getEligibleTemplates(COMMON_KEY);
        assertEquals(1, eligible.size());
        assertEquals(commonTemplate.getKey(), eligible.get(0).getKey());
    }

    @DisplayName("generateFromTemplate: returns result when eligible template exists")
    @Test
    void generateFromTemplate_returnsResult() {
        QuestTemplate template = createTemplate("gen_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_tmpl_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        Optional<GeneratedQuestResult> result = questPool.generateFromTemplate(COMMON_KEY, new Random(42), templateEngine);
        assertTrue(result.isPresent());
        assertEquals(template.getKey(), result.get().templateKey());
    }

    @DisplayName("generateFromTemplate: no eligible templates returns empty")
    @Test
    void generateFromTemplate_noEligible_returnsEmpty() {
        Optional<GeneratedQuestResult> result = questPool.generateFromTemplate(COMMON_KEY, new Random(42), templateEngine);
        assertTrue(result.isEmpty());
    }

    @DisplayName("generateFromTemplate: engine failure returns empty (does not throw)")
    @Test
    void generateFromTemplate_engineFailure_returnsEmpty() {
        QuestTemplate template = createTemplate("fail_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);
        when(templateEngine.generate(any(), any(), any())).thenThrow(new RuntimeException("Boom"));

        Optional<GeneratedQuestResult> result = questPool.generateFromTemplate(COMMON_KEY, new Random(42), templateEngine);
        assertTrue(result.isEmpty());
    }

    @DisplayName("generateFromTemplate: returns result directly without firing TemplateQuestGenerateEvent")
    @Test
    void generateFromTemplate_returnsResult_withoutFiringEvent() {
        QuestTemplate template = createTemplate("cancel_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_cancel_tmpl_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        // Event cancellation no longer applies to generateFromTemplate — the event is fired by selectForSlot
        Optional<GeneratedQuestResult> result = questPool.generateFromTemplate(COMMON_KEY, new Random(42), templateEngine);
        assertTrue(result.isPresent());
        assertEquals(mockResult, result.get());
    }

    @DisplayName("selectForSlot: TemplateQuestGenerateEvent cancellation falls back to hand-crafted when available")
    @Test
    void selectForSlot_eventCancelled_fallsBackToHandCrafted() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_fallback_cancel", true, Set.of(COMMON_KEY));
        registry.register(hcQuest);

        QuestTemplate template = createTemplate("select_cancel_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_select_cancel_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        Listener canceller = new Listener() {
            @EventHandler
            public void onGenerate(TemplateQuestGenerateEvent event) {
                event.setCancelled(true);
            }
        };
        Bukkit.getPluginManager().registerEvents(canceller, McRPG.getInstance());

        try {
            Optional<SlotSelection> result = questPool.selectForSlot(
                    COMMON_KEY, new Random(42), templateEngine, 0, 100, (String) null, Set.of());
            // With event cancelled and an HC fallback present, a HandCrafted selection is returned
            assertTrue(result.isPresent());
            assertInstanceOf(SlotSelection.HandCrafted.class, result.get());
        } finally {
            TemplateQuestGenerateEvent.getHandlerList().unregister(canceller);
        }
    }

    @DisplayName("selectForSlot: TemplateQuestGenerateEvent cancellation returns empty when no hand-crafted fallback")
    @Test
    void selectForSlot_eventCancelled_returnsEmptyWhenNoFallback() {
        QuestTemplate template = createTemplate("select_cancel_nofallback_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_select_cancel_nofallback_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        Listener canceller = new Listener() {
            @EventHandler
            public void onGenerate(TemplateQuestGenerateEvent event) {
                event.setCancelled(true);
            }
        };
        Bukkit.getPluginManager().registerEvents(canceller, McRPG.getInstance());

        try {
            Optional<SlotSelection> result = questPool.selectForSlot(
                    COMMON_KEY, new Random(42), templateEngine, 0, 100, (String) null, Set.of());
            // No HC fallback, event cancelled — should return empty
            assertTrue(result.isEmpty());
        } finally {
            TemplateQuestGenerateEvent.getHandlerList().unregister(canceller);
        }
    }

    // --- selectForSlot tests ---

    @DisplayName("selectForSlot: both pools available, 50/50 weight, returns a selection")
    @Test
    void selectForSlot_bothAvailable_returnsSelection() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_quest", true, Set.of(COMMON_KEY));
        registry.register(hcQuest);

        QuestTemplate template = createTemplate("tmpl_a", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_tmpl_a_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 50, 50);
        assertTrue(result.isPresent());
    }

    @DisplayName("selectForSlot: hcWeight=0 always picks template when both available")
    @Test
    void selectForSlot_hcWeightZero_alwaysTemplate() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_quest2", true, Set.of(COMMON_KEY));
        registry.register(hcQuest);

        QuestTemplate template = createTemplate("tmpl_b", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_tmpl_b_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        for (int seed = 0; seed < 20; seed++) {
            Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(seed), templateEngine, 0, 100);
            assertTrue(result.isPresent());
            assertInstanceOf(SlotSelection.TemplateGenerated.class, result.get());
        }
    }

    @DisplayName("selectForSlot: templateWeight=0 always picks hand-crafted when both available")
    @Test
    void selectForSlot_templateWeightZero_alwaysHandCrafted() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_quest3", true, Set.of(COMMON_KEY));
        registry.register(hcQuest);

        QuestTemplate template = createTemplate("tmpl_c", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        for (int seed = 0; seed < 20; seed++) {
            Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(seed), templateEngine, 100, 0);
            assertTrue(result.isPresent());
            assertInstanceOf(SlotSelection.HandCrafted.class, result.get());
        }
    }

    @DisplayName("selectForSlot: only templates available, returns TemplateGenerated regardless of weights")
    @Test
    void selectForSlot_onlyTemplates_returnsTemplate() {
        QuestTemplate template = createTemplate("only_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition mockDef = QuestTestHelper.singlePhaseQuest("gen_only_tmpl_result");
        GeneratedQuestResult mockResult = new GeneratedQuestResult(mockDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(mockResult);

        Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 100, 0);
        assertTrue(result.isPresent());
        assertInstanceOf(SlotSelection.TemplateGenerated.class, result.get());
    }

    @DisplayName("selectForSlot: only hand-crafted available, returns HandCrafted regardless of weights")
    @Test
    void selectForSlot_onlyHandCrafted_returnsHandCrafted() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_only", true, Set.of(COMMON_KEY));
        registry.register(hcQuest);

        Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 0, 100);
        assertTrue(result.isPresent());
        assertInstanceOf(SlotSelection.HandCrafted.class, result.get());
    }

    @DisplayName("selectForSlot: both pools empty for rarity, falls back to all-rarity hand-crafted")
    @Test
    void selectForSlot_bothEmpty_fallsBackToAnyRarity() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_rare_only", true, Set.of(RARE_KEY));
        registry.register(hcQuest);

        Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 50, 50);
        assertTrue(result.isPresent());
        assertInstanceOf(SlotSelection.HandCrafted.class, result.get());
        assertEquals(hcQuest.getQuestKey(), ((SlotSelection.HandCrafted) result.get()).definitionKey());
    }

    @DisplayName("selectForSlot: completely empty pools returns empty")
    @Test
    void selectForSlot_completelyEmpty_returnsEmpty() {
        Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 50, 50);
        assertTrue(result.isEmpty());
    }

    @DisplayName("selectForSlot: template generation fails, falls back to hand-crafted")
    @Test
    void selectForSlot_templateFails_fallsBackToHandCrafted() {
        QuestDefinition hcQuest = questWithBoardMetadata("hc_fallback", true, Set.of(COMMON_KEY));
        registry.register(hcQuest);

        QuestTemplate template = createTemplate("fail_tmpl2", Set.of(COMMON_KEY));
        templateRegistry.register(template);
        when(templateEngine.generate(any(), any(), any())).thenThrow(new RuntimeException("Boom"));

        // With hcWeight=0, it should try template first, fail, then fall back to hc
        Optional<SlotSelection> result = questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 0, 100);
        assertTrue(result.isPresent());
        assertInstanceOf(SlotSelection.HandCrafted.class, result.get());
    }

    @DisplayName("selectForSlot with player context forwards UUID and completion history to the template engine")
    @Test
    void selectForSlot_forwardsPlayerContextToEngine_whenPlayerProvided() {
        QuestTemplate template = createTemplate("ctx_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition generatedDef = questWithBoardMetadata("gen_ctx", true, Set.of(COMMON_KEY));
        GeneratedQuestResult genResult = new GeneratedQuestResult(generatedDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any(), any(), any())).thenReturn(genResult);

        UUID playerUUID = UUID.randomUUID();
        QuestCompletionHistory history = mock(QuestCompletionHistory.class);

        questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 0, 100, playerUUID, history);

        verify(templateEngine).generate(any(), any(), any(), eq(playerUUID), eq(history));
    }

    @DisplayName("selectForSlot without player context passes null UUID and null history to the template engine")
    @Test
    void selectForSlot_passesNullContext_whenNoPlayerProvided() {
        QuestTemplate template = createTemplate("noctx_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(template);

        QuestDefinition generatedDef = questWithBoardMetadata("gen_noctx", true, Set.of(COMMON_KEY));
        GeneratedQuestResult genResult = new GeneratedQuestResult(generatedDef, template.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any(), any(), any())).thenReturn(genResult);

        questPool.selectForSlot(COMMON_KEY, new Random(42), templateEngine, 0, 100, (UUID) null, (QuestCompletionHistory) null);

        verify(templateEngine).generate(any(), any(), any(), isNull(), isNull());
    }

    @DisplayName("getEligibleTemplates with prerequisite: excluded when context is null (shared generation)")
    @Test
    void getEligibleTemplates_prerequisiteExcluded_whenContextNull() {
        QuestTemplate gated = createTemplateWithPrerequisite("gated_tmpl", Set.of(COMMON_KEY), 5);
        QuestTemplate ungated = createTemplate("ungated_tmpl", Set.of(COMMON_KEY));
        templateRegistry.register(gated);
        templateRegistry.register(ungated);

        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, (NamespacedKey) null, null);
        assertEquals(1, result.size());
        assertEquals(ungated.getKey(), result.get(0).getKey());
    }

    @DisplayName("getEligibleTemplates with prerequisite: included when context satisfies prerequisite")
    @Test
    void getEligibleTemplates_prerequisiteIncluded_whenContextSatisfies() {
        QuestTemplate gated = createTemplateWithPrerequisite("gated_tmpl2", Set.of(COMMON_KEY), 5);
        templateRegistry.register(gated);

        QuestCompletionHistory history = mock(QuestCompletionHistory.class);
        when(history.countCompletedQuests(any(), isNull(), isNull())).thenReturn(10);
        ConditionContext context = ConditionContext.forPrerequisiteCheck(UUID.randomUUID(), history);

        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, (NamespacedKey) null, context);
        assertEquals(1, result.size());
        assertEquals(gated.getKey(), result.get(0).getKey());
    }

    @DisplayName("getEligibleTemplates with prerequisite: excluded when context does not meet min-completions")
    @Test
    void getEligibleTemplates_prerequisiteExcluded_whenContextDoesNotMeet() {
        QuestTemplate gated = createTemplateWithPrerequisite("gated_tmpl3", Set.of(COMMON_KEY), 15);
        templateRegistry.register(gated);

        QuestCompletionHistory history = mock(QuestCompletionHistory.class);
        when(history.countCompletedQuests(any(), isNull(), isNull())).thenReturn(3);
        ConditionContext context = ConditionContext.forPrerequisiteCheck(UUID.randomUUID(), history);

        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, (NamespacedKey) null, context);
        assertTrue(result.isEmpty());
    }

    @DisplayName("getEligibleTemplates without prerequisite: templates without prerequisites always included with null context")
    @Test
    void getEligibleTemplates_noPrerequisite_alwaysIncludedWithNullContext() {
        QuestTemplate ungated = createTemplate("ungated_null", Set.of(COMMON_KEY));
        templateRegistry.register(ungated);

        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, (NamespacedKey) null, (ConditionContext) null);
        assertEquals(1, result.size());
    }

    @DisplayName("getEligibleTemplates with scope: land templates excluded from single_player scope")
    @Test
    void getEligibleTemplates_landExcluded_fromSinglePlayerScope() {
        QuestTemplate singlePlayer = createTemplate("sp_tmpl", Set.of(COMMON_KEY));
        QuestTemplate land = createTemplateWithScope("land_tmpl", Set.of(COMMON_KEY), new NamespacedKey("mcrpg", "land_scope"));
        templateRegistry.register(singlePlayer);
        templateRegistry.register(land);

        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, SINGLE_PLAYER_SCOPE);
        assertEquals(1, result.size());
        assertEquals(singlePlayer.getKey(), result.get(0).getKey());
    }

    @DisplayName("getEligibleTemplates with scope: land templates included for land_scope")
    @Test
    void getEligibleTemplates_landIncluded_forLandScope() {
        QuestTemplate singlePlayer = createTemplate("sp_tmpl2", Set.of(COMMON_KEY));
        QuestTemplate land = createTemplateWithScope("land_tmpl2", Set.of(COMMON_KEY), new NamespacedKey("mcrpg", "land_scope"));
        templateRegistry.register(singlePlayer);
        templateRegistry.register(land);

        NamespacedKey landScope = new NamespacedKey("mcrpg", "land_scope");
        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, landScope);
        assertEquals(1, result.size());
        assertEquals(land.getKey(), result.get(0).getKey());
    }

    @DisplayName("selectForSlot with scope: land template not selected when single_player scope is given")
    @Test
    void selectForSlot_scopeAware_landTemplateExcluded_forSinglePlayerScope() {
        QuestTemplate land = createTemplateWithScope("scope_land_tmpl", Set.of(COMMON_KEY), new NamespacedKey("mcrpg", "land_scope"));
        templateRegistry.register(land);

        // Template only, no hand-crafted; with land template excluded the pool is empty → no selection
        Optional<SlotSelection> result = questPool.selectForSlot(
                COMMON_KEY, new Random(42), templateEngine, 0, 100,
                null, Set.of(), SINGLE_PLAYER_SCOPE);
        assertTrue(result.isEmpty());
    }

    @DisplayName("selectForSlot with scope: land template selected when land_scope is given")
    @Test
    void selectForSlot_scopeAware_landTemplateIncluded_forLandScope() {
        NamespacedKey landScope = new NamespacedKey("mcrpg", "land_scope");
        QuestTemplate land = createTemplateWithScope("scope_land_tmpl2", Set.of(COMMON_KEY), landScope);
        templateRegistry.register(land);

        QuestDefinition generatedDef = questWithBoardMetadata("scope_gen", true, Set.of(COMMON_KEY));
        GeneratedQuestResult genResult = new GeneratedQuestResult(generatedDef, land.getKey(), "{}");
        when(templateEngine.generate(any(), any(), any())).thenReturn(genResult);

        Optional<SlotSelection> result = questPool.selectForSlot(
                COMMON_KEY, new Random(42), templateEngine, 0, 100,
                null, Set.of(), landScope);
        assertTrue(result.isPresent());
        assertInstanceOf(SlotSelection.TemplateGenerated.class, result.get());
    }

    @DisplayName("selectForSlot player-context with scope: land template excluded for single_player scope")
    @Test
    void selectForSlot_playerContext_scopeAware_landTemplateExcluded() {
        QuestTemplate land = createTemplateWithScope("scope_land_tmpl3", Set.of(COMMON_KEY), new NamespacedKey("mcrpg", "land_scope"));
        templateRegistry.register(land);

        // Template only; with land template excluded, pool is empty → no selection
        Optional<SlotSelection> result = questPool.selectForSlot(
                COMMON_KEY, new Random(42), templateEngine, 0, 100,
                UUID.randomUUID(), null, SINGLE_PLAYER_SCOPE);
        assertTrue(result.isEmpty());
    }

    @DisplayName("getEligibleTemplates with null scope: returns all templates regardless of scope")
    @Test
    void getEligibleTemplates_nullScope_returnsAll() {
        QuestTemplate singlePlayer = createTemplate("sp_tmpl3", Set.of(COMMON_KEY));
        QuestTemplate land = createTemplateWithScope("land_tmpl3", Set.of(COMMON_KEY), new NamespacedKey("mcrpg", "land_scope"));
        templateRegistry.register(singlePlayer);
        templateRegistry.register(land);

        List<QuestTemplate> result = questPool.getEligibleTemplates(COMMON_KEY, (NamespacedKey) null);
        assertEquals(2, result.size());
    }

    private QuestTemplate createTemplateWithScope(String key, Set<NamespacedKey> rarities, NamespacedKey scopeKey) {
        RangeVariable rangeVar = new RangeVariable("count", 10, 50);
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("count", rangeVar);

        NamespacedKey objType = new NamespacedKey("mcrpg", "block_break");
        TemplateObjectiveDefinition obj = new TemplateObjectiveDefinition(objType, "count", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(obj));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(stage));

        return new QuestTemplate.Builder(
                new NamespacedKey("mcrpg", key),
                Route.fromString("quests.templates." + key + ".display-name"),
                scopeKey,
                rarities,
                Map.of(),
                variables,
                List.of(phase),
                List.of())
                .build();
    }

    private QuestTemplate createTemplateWithPrerequisite(String key, Set<NamespacedKey> rarities, int minCompletions) {
        RangeVariable rangeVar = new RangeVariable("count", 10, 50);
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("count", rangeVar);

        NamespacedKey objType = new NamespacedKey("mcrpg", "block_break");
        TemplateObjectiveDefinition obj = new TemplateObjectiveDefinition(objType, "count", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(obj));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(stage));

        CompletionPrerequisiteCondition prereq = new CompletionPrerequisiteCondition(minCompletions, null, null);

        return new QuestTemplate.Builder(
                new NamespacedKey("mcrpg", key),
                Route.fromString("quests.templates." + key + ".display-name"),
                new NamespacedKey("mcrpg", "single_player"),
                rarities,
                Map.of(),
                variables,
                List.of(phase),
                List.of())
                .prerequisite(prereq)
                .build();
    }

    private QuestTemplate createTemplate(String key, Set<NamespacedKey> rarities) {
        RangeVariable rangeVar = new RangeVariable("count", 10, 50);
        Map<String, TemplateVariable> variables = new LinkedHashMap<>();
        variables.put("count", rangeVar);

        NamespacedKey objType = new NamespacedKey("mcrpg", "block_break");
        TemplateObjectiveDefinition obj = new TemplateObjectiveDefinition(objType, "count", Map.of());
        TemplateStageDefinition stage = new TemplateStageDefinition(List.of(obj));
        TemplatePhaseDefinition phase = new TemplatePhaseDefinition(PhaseCompletionMode.ALL, List.of(stage));

        return new QuestTemplate.Builder(
                new NamespacedKey("mcrpg", key),
                Route.fromString("quests.templates." + key + ".display-name"),
                new NamespacedKey("mcrpg", "single_player"),
                rarities,
                Map.of(),
                variables,
                List.of(phase),
                List.of())
                .build();
    }
}
