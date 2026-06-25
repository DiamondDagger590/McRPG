package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerFishEvent;
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

public class FishCatchObjectiveTypeTest extends McRPGBaseTest {

    private FishCatchObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new FishCatchObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns fish_catch key")
        @Test
        public void getKey_returnsFishCatchKey() {
            assertEquals(FishCatchObjectiveType.KEY, type.getKey());
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

        @DisplayName("returns true for FishCatchQuestContext")
        @Test
        public void canProcess_returnsTrue_forFishCatchContext() {
            PlayerFishEvent mockEvent = mock(PlayerFishEvent.class);
            ItemStack mockItem = mock(ItemStack.class);
            FishCatchQuestContext context = new FishCatchQuestContext(mockEvent, mockItem);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("returns false for non-FishCatch context")
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
            PlayerFishEvent fishEvent = mock(PlayerFishEvent.class);
            ItemStack caughtItem = mock(ItemStack.class);
            when(caughtItem.getType()).thenReturn(Material.COD);

            FishCatchQuestContext context = new FishCatchQuestContext(fishEvent, caughtItem);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, type.processProgress(instance, context));
        }

        @DisplayName("returns 1 when caught item matches filter")
        @Test
        public void processProgress_returnsOne_whenItemMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("COD"));

            FishCatchObjectiveType configured = type.parseConfig(section);

            PlayerFishEvent fishEvent = mock(PlayerFishEvent.class);
            ItemStack caughtItem = mock(ItemStack.class);
            when(caughtItem.getType()).thenReturn(Material.COD);

            FishCatchQuestContext context = new FishCatchQuestContext(fishEvent, caughtItem);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("returns 0 when caught item does not match filter")
        @Test
        public void processProgress_returnsZero_whenItemDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("COD"));

            FishCatchObjectiveType configured = type.parseConfig(section);

            PlayerFishEvent fishEvent = mock(PlayerFishEvent.class);
            ItemStack caughtItem = mock(ItemStack.class);
            when(caughtItem.getType()).thenReturn(Material.SALMON);

            FishCatchQuestContext context = new FishCatchQuestContext(fishEvent, caughtItem);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("returns 1 when item matches any entry in multi-item filter")
        @Test
        public void processProgress_returnsOne_whenItemMatchesAnyInMultiFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("COD", "SALMON", "TROPICAL_FISH"));

            FishCatchObjectiveType configured = type.parseConfig(section);

            PlayerFishEvent fishEvent = mock(PlayerFishEvent.class);
            ItemStack caughtItem = mock(ItemStack.class);
            when(caughtItem.getType()).thenReturn(Material.TROPICAL_FISH);

            FishCatchQuestContext context = new FishCatchQuestContext(fishEvent, caughtItem);
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

            FishCatchObjectiveType parsed = type.parseConfig(section);

            PlayerFishEvent fishEvent = mock(PlayerFishEvent.class);
            ItemStack caughtItem = mock(ItemStack.class);
            when(caughtItem.getType()).thenReturn(Material.COD);

            FishCatchQuestContext context = new FishCatchQuestContext(fishEvent, caughtItem);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, context));
        }

        @DisplayName("returns configured instance when section has items key")
        @Test
        public void parseConfig_returnsConfiguredFilter_whenItemsKeyPresent() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("COD"));

            FishCatchObjectiveType parsed = type.parseConfig(section);

            PlayerFishEvent fishEvent = mock(PlayerFishEvent.class);

            ItemStack matchingItem = mock(ItemStack.class);
            when(matchingItem.getType()).thenReturn(Material.COD);

            ItemStack nonMatchingItem = mock(ItemStack.class);
            when(nonMatchingItem.getType()).thenReturn(Material.SALMON);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, new FishCatchQuestContext(fishEvent, matchingItem)));
            assertEquals(0, parsed.processProgress(instance, new FishCatchQuestContext(fishEvent, nonMatchingItem)));
        }
    }
}
