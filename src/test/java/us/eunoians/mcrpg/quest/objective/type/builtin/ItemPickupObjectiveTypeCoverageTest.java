package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ItemPickupObjectiveType — extended coverage")
public class ItemPickupObjectiveTypeCoverageTest extends McRPGBaseTest {

    private ItemPickupObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new ItemPickupObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for ItemPickupQuestContext")
        public void canProcess_returnsTrue_forItemPickupContext() {
            EntityPickupItemEvent mockEvent = mock(EntityPickupItemEvent.class);
            ItemPickupQuestContext context = new ItemPickupQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for BlockBreakQuestContext")
        public void canProcess_returnsFalse_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty item filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns stack amount for any item when no filter is set")
        public void processProgress_returnsAmount_whenNoFilter() {
            EntityPickupItemEvent event = createMockPickupEvent(Material.STONE, 5);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(5, progress);
        }

        @Test
        @DisplayName("returns stack amount of 1 for single item pickup")
        public void processProgress_returnsOne_forSingleItem_whenNoFilter() {
            EntityPickupItemEvent event = createMockPickupEvent(Material.DIAMOND, 1);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns large stack amount for bulk pickup")
        public void processProgress_returnsLargeAmount_forBulkPickup_whenNoFilter() {
            EntityPickupItemEvent event = createMockPickupEvent(Material.COBBLESTONE, 64);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(64, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with item filter via parseConfig")
    class ProcessProgressWithFilter {

        private ItemPickupObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND", "IRON_INGOT"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns stack amount for matching item")
        public void processProgress_returnsAmount_forMatchingItem() {
            EntityPickupItemEvent event = createMockPickupEvent(Material.DIAMOND, 3);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(3, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching item")
        public void processProgress_returnsZero_forNonMatchingItem() {
            EntityPickupItemEvent event = createMockPickupEvent(Material.STONE, 10);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns stack amount for second matching item in filter")
        public void processProgress_returnsAmount_forSecondMatchingItem() {
            EntityPickupItemEvent event = createMockPickupEvent(Material.IRON_INGOT, 7);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(7, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for generic mock context")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 for BlockBreakQuestContext")
        public void processProgress_returnsZero_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcrpg() {
            assertEquals("mcrpg", baseType.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is item_pickup")
        public void getKey_valueIsItemPickup() {
            assertEquals("item_pickup", baseType.getKey().getKey());
        }

        @Test
        @DisplayName("expansion key is present")
        public void getExpansionKey_isPresent() {
            assertTrue(baseType.getExpansionKey().isPresent());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            ItemPickupObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no items key produces empty filter that accepts any item")
        public void parseConfig_noItemsKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            ItemPickupObjectiveType parsed = baseType.parseConfig(section);

            EntityPickupItemEvent event = createMockPickupEvent(Material.DIRT, 2);
            ItemPickupQuestContext context = new ItemPickupQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(2, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("items key with entries produces filtering type")
        public void parseConfig_withItems_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND"));

            ItemPickupObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            EntityPickupItemEvent matchEvent = createMockPickupEvent(Material.DIAMOND, 4);
            assertEquals(4, parsed.processProgress(instance, new ItemPickupQuestContext(matchEvent)));

            EntityPickupItemEvent noMatchEvent = createMockPickupEvent(Material.STONE, 4);
            assertEquals(0, parsed.processProgress(instance, new ItemPickupQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("items key with empty list produces empty filter")
        public void parseConfig_emptyItemsList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of());

            ItemPickupObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            EntityPickupItemEvent event = createMockPickupEvent(Material.COBBLESTONE, 16);
            assertEquals(16, parsed.processProgress(instance, new ItemPickupQuestContext(event)));
        }
    }

    private EntityPickupItemEvent createMockPickupEvent(Material material, int amount) {
        EntityPickupItemEvent event = mock(EntityPickupItemEvent.class);
        Item item = mock(Item.class);
        ItemStack itemStack = mock(ItemStack.class);
        when(itemStack.getType()).thenReturn(material);
        when(itemStack.getAmount()).thenReturn(amount);
        when(item.getItemStack()).thenReturn(itemStack);
        when(event.getItem()).thenReturn(item);
        return event;
    }
}
