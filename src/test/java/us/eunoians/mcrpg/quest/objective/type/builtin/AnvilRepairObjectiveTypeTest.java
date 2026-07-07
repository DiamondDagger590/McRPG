package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
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

@DisplayName("AnvilRepairObjectiveType")
class AnvilRepairObjectiveTypeTest extends McRPGBaseTest {

    private AnvilRepairObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new AnvilRepairObjectiveType();
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns anvil_repair key")
        void getKey_returnsAnvilRepairKey() {
            assertEquals(AnvilRepairObjectiveType.KEY, type.getKey());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for AnvilRepairQuestContext")
        void canProcess_returnsTrue_forAnvilRepairContext() {
            InventoryClickEvent mockEvent = mock(InventoryClickEvent.class);
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for other context types")
        void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = type.processProgress(instance, wrongContext);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for any item when no filter configured")
        void processProgress_returnsOne_whenNoFilterConfigured() {
            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.DIAMOND_PICKAXE));
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = type.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 when no filter and current item is null")
        void processProgress_returnsOne_whenNoFilterAndNullItem() {
            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getCurrentItem()).thenReturn(null);
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = type.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 when item matches filter")
        void processProgress_returnsOne_whenItemMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_PICKAXE"));
            AnvilRepairObjectiveType configured = type.parseConfig(section);

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.DIAMOND_PICKAXE));
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 when item does not match filter")
        void processProgress_returnsZero_whenItemDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_PICKAXE"));
            AnvilRepairObjectiveType configured = type.parseConfig(section);

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.IRON_SWORD));
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 when current item is null")
        void processProgress_returnsZero_whenCurrentItemIsNull() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_PICKAXE"));
            AnvilRepairObjectiveType configured = type.parseConfig(section);

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getCurrentItem()).thenReturn(null);
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns unfiltered instance when no items key")
        void parseConfig_returnsUnfiltered_whenNoItemsKey() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            AnvilRepairObjectiveType configured = type.parseConfig(section);

            InventoryClickEvent event = mock(InventoryClickEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.IRON_CHESTPLATE));
            AnvilRepairQuestContext context = new AnvilRepairQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns filtered instance with parsed items")
        void parseConfig_returnsFilteredInstance() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_PICKAXE", "DIAMOND_SWORD"));

            AnvilRepairObjectiveType configured = type.parseConfig(section);

            InventoryClickEvent matchEvent = mock(InventoryClickEvent.class);
            when(matchEvent.getCurrentItem()).thenReturn(new ItemStack(Material.DIAMOND_PICKAXE));

            InventoryClickEvent noMatchEvent = mock(InventoryClickEvent.class);
            when(noMatchEvent.getCurrentItem()).thenReturn(new ItemStack(Material.IRON_PICKAXE));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, configured.processProgress(instance, new AnvilRepairQuestContext(matchEvent)));
            assertEquals(0, configured.processProgress(instance, new AnvilRepairQuestContext(noMatchEvent)));
        }
    }
}
