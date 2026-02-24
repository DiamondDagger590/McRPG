package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;

import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersonalOfferingGeneratorTest extends McRPGBaseTest {

    private static final NamespacedKey COMMON = NamespacedKey.fromString("mcrpg:common");
    private static final NamespacedKey REFRESH_KEY = NamespacedKey.fromString("mcrpg:daily");
    private static final NamespacedKey BOARD_KEY = NamespacedKey.fromString("mcrpg:default_board");
    private static final NamespacedKey SCOPE_KEY = NamespacedKey.fromString("mcrpg:single_player");

    private QuestRarityRegistry rarityRegistry;
    private QuestTemplateEngine templateEngine;
    private QuestPool questPool;
    private BoardRotation rotation;

    @BeforeEach
    void setUp() {
        rarityRegistry = new QuestRarityRegistry();
        rarityRegistry.register(new QuestRarity(COMMON, 100, 1.0, 1.0, NamespacedKey.fromString("mcrpg:mcrpg")));

        QuestDefinitionRegistry defRegistry = new QuestDefinitionRegistry();
        templateEngine = mock(QuestTemplateEngine.class);
        questPool = new QuestPool(defRegistry);

        rotation = new BoardRotation(UUID.randomUUID(), BOARD_KEY, REFRESH_KEY, 12345L, 0L, 86400000L);
    }

    @Test
    @DisplayName("Deterministic seed: same inputs produce same seed")
    void computeSeed_sameInputs_sameSeed() {
        UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
        long seed1 = PersonalOfferingGenerator.computeSeed(player, 100L, 0);
        long seed2 = PersonalOfferingGenerator.computeSeed(player, 100L, 0);
        assertEquals(seed1, seed2);
    }

    @Test
    @DisplayName("Different playerUUIDs produce different seeds")
    void computeSeed_differentPlayers_differentSeeds() {
        UUID player1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID player2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        assertNotEquals(
                PersonalOfferingGenerator.computeSeed(player1, 100L, 0),
                PersonalOfferingGenerator.computeSeed(player2, 100L, 0));
    }

    @Test
    @DisplayName("Different epochs produce different seeds")
    void computeSeed_differentEpochs_differentSeeds() {
        UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
        assertNotEquals(
                PersonalOfferingGenerator.computeSeed(player, 100L, 0),
                PersonalOfferingGenerator.computeSeed(player, 200L, 0));
    }

    @Test
    @DisplayName("Different slotIndexes produce different seeds")
    void computeSeed_differentSlots_differentSeeds() {
        UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
        assertNotEquals(
                PersonalOfferingGenerator.computeSeed(player, 100L, 0),
                PersonalOfferingGenerator.computeSeed(player, 100L, 1));
    }

    @Test
    @DisplayName("Empty categories produces no offerings")
    void generatePersonalOfferings_emptyCategories_noOfferings() {
        UUID player = UUID.randomUUID();
        List<BoardOffering> offerings = PersonalOfferingGenerator.generatePersonalOfferings(
                player, rotation, List.of(), 0, questPool, rarityRegistry, templateEngine, 50, 50);
        assertTrue(offerings.isEmpty());
    }

    @Test
    @DisplayName("Same inputs produce same offerings (deterministic)")
    void generatePersonalOfferings_deterministic() {
        UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
        BoardSlotCategory category = new BoardSlotCategory(
                NamespacedKey.fromString("mcrpg:personal_daily"),
                BoardSlotCategory.Visibility.PERSONAL, REFRESH_KEY,
                Duration.ofDays(1), Duration.ofHours(24), SCOPE_KEY,
                2, 2, 1.0, 10, null, null);

        List<BoardOffering> run1 = PersonalOfferingGenerator.generatePersonalOfferings(
                player, rotation, List.of(category), 0, questPool, rarityRegistry, templateEngine, 50, 50);
        List<BoardOffering> run2 = PersonalOfferingGenerator.generatePersonalOfferings(
                player, rotation, List.of(category), 0, questPool, rarityRegistry, templateEngine, 50, 50);

        assertEquals(run1.size(), run2.size());
    }

    @Test
    @DisplayName("Template-only weight: template weight=100 hc weight=0 uses template when only templates available")
    void generatePersonalOfferings_templateOnlyWeight_producesTemplateOfferings() {
        UUID player = UUID.fromString("11111111-1111-1111-1111-111111111111");
        BoardSlotCategory category = new BoardSlotCategory(
                NamespacedKey.fromString("mcrpg:personal_daily"),
                BoardSlotCategory.Visibility.PERSONAL, REFRESH_KEY,
                Duration.ofDays(1), Duration.ofHours(24), SCOPE_KEY,
                1, 1, 1.0, 10, null, null);

        QuestPool mockedPool = mock(QuestPool.class);
        QuestDefinition generatedDef = QuestTestHelper.singlePhaseQuest("gen_tmpl_only_abcd1234");
        GeneratedQuestResult genResult = new GeneratedQuestResult(
                generatedDef,
                NamespacedKey.fromString("mcrpg:daily_template"),
                "{\"key\":\"mcrpg:gen_tmpl_only_abcd1234\"}");
        when(mockedPool.selectForSlot(any(), any(), any(), eq(0), eq(100)))
                .thenReturn(Optional.of(new SlotSelection.TemplateGenerated(genResult, COMMON)));

        List<BoardOffering> offerings = PersonalOfferingGenerator.generatePersonalOfferings(
                player, rotation, List.of(category), 0, mockedPool, rarityRegistry, templateEngine, 0, 100);

        assertEquals(1, offerings.size());
        assertTrue(offerings.get(0).isTemplateGenerated());
    }

    @Test
    @DisplayName("Template deduplication: duplicate template + same def key → skipped")
    void isDuplicateTemplateOffering_sameDef_skipped() {
        BoardOffering offering1 = createTemplateOffering("tmpl_a", "gen_tmpl_a_abc123");
        BoardOffering offering2 = createTemplateOffering("tmpl_a", "gen_tmpl_a_abc123");

        assertTrue(PersonalOfferingGenerator.isDuplicateTemplateOffering(offering2, List.of(offering1)));
    }

    @Test
    @DisplayName("Template deduplication: same template + different def key → both kept")
    void isDuplicateTemplateOffering_differentDef_kept() {
        BoardOffering offering1 = createTemplateOffering("tmpl_a", "gen_tmpl_a_abc123");
        BoardOffering offering2 = createTemplateOffering("tmpl_a", "gen_tmpl_a_def456");

        assertFalse(PersonalOfferingGenerator.isDuplicateTemplateOffering(offering2, List.of(offering1)));
    }

    @Test
    @DisplayName("Template deduplication: different templates + same def key → both kept")
    void isDuplicateTemplateOffering_differentTemplate_sameDef_kept() {
        BoardOffering offering1 = createTemplateOffering("tmpl_a", "gen_shared_def_abc123");
        BoardOffering offering2 = createTemplateOfferingWithTemplate("tmpl_b", "gen_shared_def_abc123");

        assertFalse(PersonalOfferingGenerator.isDuplicateTemplateOffering(offering2, List.of(offering1)));
    }

    @Test
    @DisplayName("Hand-crafted offerings are never considered duplicates")
    void isDuplicateTemplateOffering_handcrafted_neverDuplicate() {
        BoardOffering handcrafted = new BoardOffering(
                UUID.randomUUID(), UUID.randomUUID(),
                NamespacedKey.fromString("mcrpg:personal"), 0,
                NamespacedKey.fromString("mcrpg:quest_a"), COMMON,
                null, Duration.ofHours(24));

        assertFalse(PersonalOfferingGenerator.isDuplicateTemplateOffering(handcrafted, List.of(handcrafted)));
    }

    private BoardOffering createTemplateOffering(String templateKey, String defKey) {
        return createTemplateOfferingWithTemplate(templateKey, defKey);
    }

    private BoardOffering createTemplateOfferingWithTemplate(String templateKey, String defKey) {
        return new BoardOffering(
                UUID.randomUUID(), UUID.randomUUID(),
                NamespacedKey.fromString("mcrpg:personal"), 0,
                NamespacedKey.fromString("mcrpg:" + defKey), COMMON,
                null, Duration.ofHours(24),
                NamespacedKey.fromString("mcrpg:" + templateKey),
                "{}");
    }
}
