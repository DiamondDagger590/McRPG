package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SmeltItemObjectiveTypeTest extends McRPGBaseTest {

    private SmeltItemObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new SmeltItemObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns smelt_item key")
        void getKey_returnsSmeltItemKey() {
            assertEquals(SmeltItemObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for SmeltItemQuestContext")
        void canProcess_returnsTrue_forSmeltItemContext() {
            FurnaceExtractEvent mockEvent = mock(FurnaceExtractEvent.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for non-matching context")
        void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no items key leaves filter empty")
        void parseConfig_noItemsKey_emptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.IRON_INGOT);
            when(event.getItemAmount()).thenReturn(5);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(5L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses material list from config")
        void parseConfig_withItems_restrictsToThoseMaterials() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("IRON_INGOT", "GOLD_INGOT"));

            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.IRON_INGOT);
            when(event.getItemAmount()).thenReturn(3);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(3L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("invalid material names are filtered out")
        void parseConfig_invalidMaterialName_filteredOut() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("NOT_A_REAL_MATERIAL", "IRON_INGOT"));

            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.IRON_INGOT);
            when(event.getItemAmount()).thenReturn(2);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(2L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("empty items list creates filter that still accepts any material")
        void parseConfig_emptyItemsList_acceptsAnyMaterial() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of());

            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.DIAMOND);
            when(event.getItemAmount()).thenReturn(1);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0L, type.processProgress(instance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type returns item amount for any material")
        void processProgress_returnsItemAmount_whenUnconfigured() {
            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.COPPER_INGOT);
            when(event.getItemAmount()).thenReturn(8);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(8L, type.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns item amount when material matches filter")
        void processProgress_returnsItemAmount_whenMaterialMatches() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("IRON_INGOT"));
            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.IRON_INGOT);
            when(event.getItemAmount()).thenReturn(4);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(4L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when material does not match filter")
        void processProgress_returnsZero_whenMaterialDoesNotMatch() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("IRON_INGOT"));
            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.GOLD_INGOT);
            when(event.getItemAmount()).thenReturn(10);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns correct amount for multi-item extract")
        void processProgress_returnsCorrectAmount_forMultiItemExtract() {
            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.GLASS);
            when(event.getItemAmount()).thenReturn(64);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(64L, type.processProgress(instance, context));
        }

        @Test
        @DisplayName("matches second material in multi-material filter")
        void processProgress_matchesSecondMaterial_inMultiFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("IRON_INGOT", "GOLD_INGOT"));
            SmeltItemObjectiveType configured = type.parseConfig(section);

            FurnaceExtractEvent event = mock(FurnaceExtractEvent.class);
            when(event.getItemType()).thenReturn(Material.GOLD_INGOT);
            when(event.getItemAmount()).thenReturn(6);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            SmeltItemQuestContext context = new SmeltItemQuestContext(event);
            assertEquals(6L, configured.processProgress(instance, context));
        }
    }
}
