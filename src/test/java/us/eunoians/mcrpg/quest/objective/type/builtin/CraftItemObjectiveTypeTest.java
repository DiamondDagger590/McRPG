package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
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

public class CraftItemObjectiveTypeTest extends McRPGBaseTest {

    private CraftItemObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new CraftItemObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns craft_item key")
        @Test
        public void getKey_returnsCraftItemKey() {
            assertEquals(CraftItemObjectiveType.KEY, type.getKey());
        }

        @DisplayName("getExpansionKey returns McRPGExpansion key")
        @Test
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("CanProcess")
    class CanProcess {

        @DisplayName("canProcess returns true for CraftItemQuestContext")
        @Test
        public void canProcess_returnsTrue_forCraftItemContext() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("canProcess returns false for other context type")
        @Test
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("ProcessProgress")
    class ProcessProgress {

        @DisplayName("processProgress returns 0 for wrong context type")
        @Test
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, wrongContext));
        }

        @DisplayName("processProgress returns result amount for normal click with no filter")
        @Test
        public void processProgress_returnsResultAmount_forNormalClick() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.DIAMOND_SWORD, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(false);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns stacked result amount for normal click")
        @Test
        public void processProgress_returnsStackedAmount_forNormalClick() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.STICK, 4);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(false);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(4, type.processProgress(instance, context));
        }

        @DisplayName("processProgress multiplies by max crafts for shift-click")
        @Test
        public void processProgress_multipliesByMaxCrafts_forShiftClick() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.STICK, 4);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(true);

            CraftingInventory mockInventory = mock(CraftingInventory.class);
            ItemStack[] matrix = new ItemStack[]{
                    new ItemStack(Material.OAK_PLANKS, 3),
                    new ItemStack(Material.OAK_PLANKS, 5)
            };
            when(mockInventory.getMatrix()).thenReturn(matrix);
            when(mockEvent.getInventory()).thenReturn(mockInventory);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(12, type.processProgress(instance, context));
        }

        @DisplayName("processProgress uses minimum slot amount for shift-click")
        @Test
        public void processProgress_usesMinSlotAmount_forShiftClick() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.IRON_INGOT, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(true);

            CraftingInventory mockInventory = mock(CraftingInventory.class);
            ItemStack[] matrix = new ItemStack[]{
                    new ItemStack(Material.IRON_ORE, 10),
                    new ItemStack(Material.COAL, 2),
                    null
            };
            when(mockInventory.getMatrix()).thenReturn(matrix);
            when(mockEvent.getInventory()).thenReturn(mockInventory);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(2, type.processProgress(instance, context));
        }

        @DisplayName("processProgress skips air slots in matrix")
        @Test
        public void processProgress_skipsAirSlots_inMatrix() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.DIAMOND_SWORD, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(true);

            CraftingInventory mockInventory = mock(CraftingInventory.class);
            ItemStack airStack = new ItemStack(Material.AIR);
            ItemStack[] matrix = new ItemStack[]{
                    new ItemStack(Material.DIAMOND, 7),
                    airStack,
                    null,
                    new ItemStack(Material.STICK, 3)
            };
            when(mockInventory.getMatrix()).thenReturn(matrix);
            when(mockEvent.getInventory()).thenReturn(mockInventory);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(3, type.processProgress(instance, context));
        }

        @DisplayName("processProgress defaults to 1 craft when matrix is all null/air")
        @Test
        public void processProgress_defaultsToOne_whenMatrixAllEmpty() {
            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.STICK, 4);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(true);

            CraftingInventory mockInventory = mock(CraftingInventory.class);
            ItemStack[] matrix = new ItemStack[]{null, null};
            when(mockInventory.getMatrix()).thenReturn(matrix);
            when(mockEvent.getInventory()).thenReturn(mockInventory);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(4, type.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("ParseConfig")
    class ParseConfig {

        @DisplayName("parseConfig with empty section accepts any crafted item")
        @Test
        public void parseConfig_acceptsAnyItem_whenSectionEmpty() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            CraftItemObjectiveType configured = type.parseConfig(section);

            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.DIAMOND_SWORD, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(false);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with item filter accepts matching item")
        @Test
        public void parseConfig_acceptsMatchingItem() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));

            CraftItemObjectiveType configured = type.parseConfig(section);

            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.DIAMOND_SWORD, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(false);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with item filter rejects non-matching item")
        @Test
        public void parseConfig_rejectsNonMatchingItem() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));

            CraftItemObjectiveType configured = type.parseConfig(section);

            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.IRON_SWORD, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(false);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with multiple items accepts any listed item")
        @Test
        public void parseConfig_acceptsAnyListedItem_whenMultipleItems() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD", "IRON_SWORD", "STONE_SWORD"));

            CraftItemObjectiveType configured = type.parseConfig(section);

            CraftItemEvent mockEvent = mock(CraftItemEvent.class);
            Recipe mockRecipe = mock(Recipe.class);
            ItemStack result = new ItemStack(Material.IRON_SWORD, 1);
            when(mockRecipe.getResult()).thenReturn(result);
            when(mockEvent.getRecipe()).thenReturn(mockRecipe);
            when(mockEvent.isShiftClick()).thenReturn(false);

            CraftItemQuestContext context = new CraftItemQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }
    }
}
