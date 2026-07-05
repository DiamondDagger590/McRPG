package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.inventory.SmithItemEvent;
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

@DisplayName("SmithingObjectiveType")
class SmithingObjectiveTypeTest extends McRPGBaseTest {

    private SmithingObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new SmithingObjectiveType();
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns smithing key")
        void getKey_returnsSmithingKey() {
            assertEquals(SmithingObjectiveType.KEY, type.getKey());
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
        @DisplayName("returns true for SmithingQuestContext")
        void canProcess_returnsTrue_forSmithingContext() {
            SmithItemEvent mockEvent = mock(SmithItemEvent.class);
            SmithingQuestContext context = new SmithingQuestContext(mockEvent);
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
            SmithItemEvent event = mock(SmithItemEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.NETHERITE_SWORD));
            SmithingQuestContext context = new SmithingQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = type.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 when no filter and current item is null")
        void processProgress_returnsOne_whenNoFilterAndNullItem() {
            SmithItemEvent event = mock(SmithItemEvent.class);
            when(event.getCurrentItem()).thenReturn(null);
            SmithingQuestContext context = new SmithingQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = type.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 when item matches filter")
        void processProgress_returnsOne_whenItemMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("NETHERITE_SWORD"));
            SmithingObjectiveType configured = type.parseConfig(section);

            SmithItemEvent event = mock(SmithItemEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.NETHERITE_SWORD));
            SmithingQuestContext context = new SmithingQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 when item does not match filter")
        void processProgress_returnsZero_whenItemDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("NETHERITE_SWORD"));
            SmithingObjectiveType configured = type.parseConfig(section);

            SmithItemEvent event = mock(SmithItemEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            SmithingQuestContext context = new SmithingQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            long progress = configured.processProgress(instance, context);

            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 when current item is null")
        void processProgress_returnsZero_whenCurrentItemIsNull() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("NETHERITE_SWORD"));
            SmithingObjectiveType configured = type.parseConfig(section);

            SmithItemEvent event = mock(SmithItemEvent.class);
            when(event.getCurrentItem()).thenReturn(null);
            SmithingQuestContext context = new SmithingQuestContext(event);

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

            SmithingObjectiveType configured = type.parseConfig(section);

            SmithItemEvent event = mock(SmithItemEvent.class);
            when(event.getCurrentItem()).thenReturn(new ItemStack(Material.NETHERITE_CHESTPLATE));
            SmithingQuestContext context = new SmithingQuestContext(event);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns filtered instance with parsed items")
        void parseConfig_returnsFilteredInstance() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("NETHERITE_SWORD", "NETHERITE_PICKAXE"));

            SmithingObjectiveType configured = type.parseConfig(section);

            SmithItemEvent matchEvent = mock(SmithItemEvent.class);
            when(matchEvent.getCurrentItem()).thenReturn(new ItemStack(Material.NETHERITE_SWORD));

            SmithItemEvent noMatchEvent = mock(SmithItemEvent.class);
            when(noMatchEvent.getCurrentItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, configured.processProgress(instance, new SmithingQuestContext(matchEvent)));
            assertEquals(0, configured.processProgress(instance, new SmithingQuestContext(noMatchEvent)));
        }
    }
}
