package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategory;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SlotSelection")
public class SlotSelectionTest extends McRPGBaseTest {

    private static final NamespacedKey DEFINITION_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final NamespacedKey RARITY_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey CATEGORY_KEY = new NamespacedKey("mcrpg", "daily");
    private static final NamespacedKey BOARD_KEY = new NamespacedKey("mcrpg", "default");
    private static final NamespacedKey REFRESH_KEY = new NamespacedKey("mcrpg", "daily");
    private static final NamespacedKey SCOPE_KEY = new NamespacedKey("mcrpg", "single_player");
    private static final NamespacedKey TEMPLATE_KEY = new NamespacedKey("mcrpg", "mining_template");
    private static final Duration COMPLETION_TIME = Duration.ofHours(24);

    private BoardRotation createRotation() {
        return new BoardRotation(UUID.randomUUID(), BOARD_KEY, REFRESH_KEY, 1, 1000L, 2000L);
    }

    private BoardSlotCategory createCategory() {
        return new BoardSlotCategory(
                CATEGORY_KEY, BoardSlotCategory.Visibility.SHARED, REFRESH_KEY,
                Duration.ofDays(1), COMPLETION_TIME, SCOPE_KEY,
                1, 5, 0.5, 0, null, null, null);
    }

    @Nested
    @DisplayName("HandCrafted")
    class HandCraftedTests {

        @DisplayName("record stores definitionKey and rarityKey")
        @Test
        void handCrafted_storesDefinitionKeyAndRarityKey() {
            var handCrafted = new SlotSelection.HandCrafted(DEFINITION_KEY, RARITY_KEY);

            assertEquals(DEFINITION_KEY, handCrafted.definitionKey());
            assertEquals(RARITY_KEY, handCrafted.rarityKey());
        }

        @DisplayName("toOffering creates offering with correct fields")
        @Test
        void toOffering_createsCorrectOffering() {
            var handCrafted = new SlotSelection.HandCrafted(DEFINITION_KEY, RARITY_KEY);
            BoardRotation rotation = createRotation();
            BoardSlotCategory category = createCategory();

            BoardOffering offering = handCrafted.toOffering(rotation, category, 3, null);

            assertNotNull(offering.getOfferingId());
            assertEquals(rotation.getRotationId(), offering.getRotationId());
            assertEquals(CATEGORY_KEY, offering.getCategoryKey());
            assertEquals(3, offering.getSlotIndex());
            assertEquals(DEFINITION_KEY, offering.getQuestDefinitionKey());
            assertEquals(RARITY_KEY, offering.getRarityKey());
            assertTrue(offering.getScopeTargetId().isEmpty());
            assertEquals(COMPLETION_TIME, offering.getCompletionTime());
            assertEquals(BoardOffering.State.VISIBLE, offering.getState());
        }

        @DisplayName("toOffering passes through ownerIdentifier for personal offerings")
        @Test
        void toOffering_withOwnerIdentifier() {
            var handCrafted = new SlotSelection.HandCrafted(DEFINITION_KEY, RARITY_KEY);
            String ownerId = UUID.randomUUID().toString();

            BoardOffering offering = handCrafted.toOffering(createRotation(), createCategory(), 0, ownerId);

            assertTrue(offering.getScopeTargetId().isPresent());
            assertEquals(ownerId, offering.getScopeTargetId().get());
        }

        @DisplayName("toOffering creates non-template offering")
        @Test
        void toOffering_isNotTemplateGenerated() {
            var handCrafted = new SlotSelection.HandCrafted(DEFINITION_KEY, RARITY_KEY);

            BoardOffering offering = handCrafted.toOffering(createRotation(), createCategory(), 0, null);

            assertFalse(offering.isTemplateGenerated());
            assertTrue(offering.getTemplateKey().isEmpty());
            assertTrue(offering.getGeneratedDefinition().isEmpty());
        }

        @DisplayName("toOffering generates unique offering IDs")
        @Test
        void toOffering_uniqueIds() {
            var handCrafted = new SlotSelection.HandCrafted(DEFINITION_KEY, RARITY_KEY);
            BoardRotation rotation = createRotation();
            BoardSlotCategory category = createCategory();

            BoardOffering first = handCrafted.toOffering(rotation, category, 0, null);
            BoardOffering second = handCrafted.toOffering(rotation, category, 1, null);

            assertNotEquals(first.getOfferingId(), second.getOfferingId());
        }
    }

    @Nested
    @DisplayName("TemplateGenerated")
    class TemplateGeneratedTests {

        private GeneratedQuestResult createResult() {
            QuestDefinition definition = mock(QuestDefinition.class);
            when(definition.getQuestKey()).thenReturn(DEFINITION_KEY);
            return new GeneratedQuestResult(definition, TEMPLATE_KEY, "{\"type\":\"generated\"}");
        }

        @DisplayName("record stores result and rarityKey")
        @Test
        void templateGenerated_storesResultAndRarityKey() {
            GeneratedQuestResult result = createResult();
            var templateGenerated = new SlotSelection.TemplateGenerated(result, RARITY_KEY);

            assertEquals(result, templateGenerated.result());
            assertEquals(RARITY_KEY, templateGenerated.rarityKey());
        }

        @DisplayName("toOffering creates offering with template metadata")
        @Test
        void toOffering_includesTemplateMetadata() {
            GeneratedQuestResult result = createResult();
            var templateGenerated = new SlotSelection.TemplateGenerated(result, RARITY_KEY);
            BoardRotation rotation = createRotation();
            BoardSlotCategory category = createCategory();

            BoardOffering offering = templateGenerated.toOffering(rotation, category, 2, null);

            assertNotNull(offering.getOfferingId());
            assertEquals(rotation.getRotationId(), offering.getRotationId());
            assertEquals(CATEGORY_KEY, offering.getCategoryKey());
            assertEquals(2, offering.getSlotIndex());
            assertEquals(DEFINITION_KEY, offering.getQuestDefinitionKey());
            assertEquals(RARITY_KEY, offering.getRarityKey());
            assertEquals(COMPLETION_TIME, offering.getCompletionTime());
            assertEquals(BoardOffering.State.VISIBLE, offering.getState());
        }

        @DisplayName("toOffering is template-generated with correct keys")
        @Test
        void toOffering_isTemplateGenerated() {
            GeneratedQuestResult result = createResult();
            var templateGenerated = new SlotSelection.TemplateGenerated(result, RARITY_KEY);

            BoardOffering offering = templateGenerated.toOffering(createRotation(), createCategory(), 0, null);

            assertTrue(offering.isTemplateGenerated());
            assertTrue(offering.getTemplateKey().isPresent());
            assertEquals(TEMPLATE_KEY, offering.getTemplateKey().get());
            assertTrue(offering.getGeneratedDefinition().isPresent());
            assertEquals("{\"type\":\"generated\"}", offering.getGeneratedDefinition().get());
        }

        @DisplayName("toOffering passes through ownerIdentifier for scoped offerings")
        @Test
        void toOffering_withOwnerIdentifier() {
            GeneratedQuestResult result = createResult();
            var templateGenerated = new SlotSelection.TemplateGenerated(result, RARITY_KEY);
            String ownerId = "land-uuid-123";

            BoardOffering offering = templateGenerated.toOffering(createRotation(), createCategory(), 0, ownerId);

            assertTrue(offering.getScopeTargetId().isPresent());
            assertEquals(ownerId, offering.getScopeTargetId().get());
        }

        @DisplayName("toOffering with null owner produces empty scopeTargetId")
        @Test
        void toOffering_nullOwner_emptyScopeTarget() {
            GeneratedQuestResult result = createResult();
            var templateGenerated = new SlotSelection.TemplateGenerated(result, RARITY_KEY);

            BoardOffering offering = templateGenerated.toOffering(createRotation(), createCategory(), 0, null);

            assertTrue(offering.getScopeTargetId().isEmpty());
        }
    }
}
