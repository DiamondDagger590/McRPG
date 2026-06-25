package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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

public class ConsumeItemObjectiveTypeTest extends McRPGBaseTest {

    private ConsumeItemObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new ConsumeItemObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns consume_item key")
        @Test
        public void getKey_returnsConsumeItemKey() {
            assertEquals(ConsumeItemObjectiveType.KEY, type.getKey());
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

        @DisplayName("returns true for ConsumeItemQuestContext")
        @Test
        public void canProcess_returnsTrue_forConsumeItemContext() {
            PlayerItemConsumeEvent mockEvent = mock(PlayerItemConsumeEvent.class);
            ConsumeItemQuestContext context = new ConsumeItemQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("returns false for non-ConsumeItem context")
        @Test
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("ProcessProgress")
    class ProcessProgress {

        @DisplayName("returns 0 for wrong context type")
        @Test
        public void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, wrongContext));
        }

        @DisplayName("returns 1 for any item when filter is empty")
        @Test
        public void processProgress_returnsOne_whenFilterEmpty() {
            ItemStack item = mock(ItemStack.class);
            when(item.getType()).thenReturn(Material.GOLDEN_APPLE);

            PlayerItemConsumeEvent event = mock(PlayerItemConsumeEvent.class);
            when(event.getItem()).thenReturn(item);

            ConsumeItemQuestContext context = new ConsumeItemQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, type.processProgress(instance, context));
        }

        @DisplayName("returns 1 when consumed item matches filter")
        @Test
        public void processProgress_returnsOne_whenItemMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("GOLDEN_APPLE"));

            ConsumeItemObjectiveType configured = type.parseConfig(section);

            ItemStack item = mock(ItemStack.class);
            when(item.getType()).thenReturn(Material.GOLDEN_APPLE);

            PlayerItemConsumeEvent event = mock(PlayerItemConsumeEvent.class);
            when(event.getItem()).thenReturn(item);

            ConsumeItemQuestContext context = new ConsumeItemQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("returns 0 when consumed item does not match filter")
        @Test
        public void processProgress_returnsZero_whenItemDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("GOLDEN_APPLE"));

            ConsumeItemObjectiveType configured = type.parseConfig(section);

            ItemStack item = mock(ItemStack.class);
            when(item.getType()).thenReturn(Material.BREAD);

            PlayerItemConsumeEvent event = mock(PlayerItemConsumeEvent.class);
            when(event.getItem()).thenReturn(item);

            ConsumeItemQuestContext context = new ConsumeItemQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("returns 1 when item matches any entry in multi-item filter")
        @Test
        public void processProgress_returnsOne_whenItemMatchesAnyInMultiFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("GOLDEN_APPLE", "BREAD", "COOKED_BEEF"));

            ConsumeItemObjectiveType configured = type.parseConfig(section);

            ItemStack item = mock(ItemStack.class);
            when(item.getType()).thenReturn(Material.COOKED_BEEF);

            PlayerItemConsumeEvent event = mock(PlayerItemConsumeEvent.class);
            when(event.getItem()).thenReturn(item);

            ConsumeItemQuestContext context = new ConsumeItemQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("ParseConfig")
    class ParseConfig {

        @DisplayName("returns instance with empty filter when section has no items key")
        @Test
        public void parseConfig_returnsEmptyFilter_whenNoItemsKey() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            ConsumeItemObjectiveType parsed = type.parseConfig(section);

            ItemStack item = mock(ItemStack.class);
            when(item.getType()).thenReturn(Material.GOLDEN_APPLE);

            PlayerItemConsumeEvent event = mock(PlayerItemConsumeEvent.class);
            when(event.getItem()).thenReturn(item);

            ConsumeItemQuestContext context = new ConsumeItemQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, context));
        }

        @DisplayName("returns configured instance when section has items key")
        @Test
        public void parseConfig_returnsConfiguredFilter_whenItemsKeyPresent() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("GOLDEN_APPLE"));

            ConsumeItemObjectiveType parsed = type.parseConfig(section);

            ItemStack matchingItem = mock(ItemStack.class);
            when(matchingItem.getType()).thenReturn(Material.GOLDEN_APPLE);
            PlayerItemConsumeEvent matchEvent = mock(PlayerItemConsumeEvent.class);
            when(matchEvent.getItem()).thenReturn(matchingItem);

            ItemStack nonMatchingItem = mock(ItemStack.class);
            when(nonMatchingItem.getType()).thenReturn(Material.BREAD);
            PlayerItemConsumeEvent noMatchEvent = mock(PlayerItemConsumeEvent.class);
            when(noMatchEvent.getItem()).thenReturn(nonMatchingItem);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, new ConsumeItemQuestContext(matchEvent)));
            assertEquals(0, parsed.processProgress(instance, new ConsumeItemQuestContext(noMatchEvent)));
        }
    }
}
