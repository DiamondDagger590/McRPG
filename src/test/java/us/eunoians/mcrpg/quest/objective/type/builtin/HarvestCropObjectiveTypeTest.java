package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
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

public class HarvestCropObjectiveTypeTest extends McRPGBaseTest {

    private HarvestCropObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new HarvestCropObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns harvest_crop key")
        @Test
        public void getKey_returnsHarvestCropKey() {
            assertEquals(HarvestCropObjectiveType.KEY, type.getKey());
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

        @DisplayName("returns true for HarvestCropQuestContext")
        @Test
        public void canProcess_returnsTrue_forHarvestCropContext() {
            PlayerHarvestBlockEvent mockEvent = mock(PlayerHarvestBlockEvent.class);
            HarvestCropQuestContext context = new HarvestCropQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("returns false for non-HarvestCrop context")
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

        @DisplayName("returns 1 for any block when filter is empty")
        @Test
        public void processProgress_returnsOne_whenFilterEmpty() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.WHEAT);

            PlayerHarvestBlockEvent event = mock(PlayerHarvestBlockEvent.class);
            when(event.getHarvestedBlock()).thenReturn(block);

            HarvestCropQuestContext context = new HarvestCropQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, type.processProgress(instance, context));
        }

        @DisplayName("returns 1 when harvested block matches filter")
        @Test
        public void processProgress_returnsOne_whenBlockMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("WHEAT"));

            HarvestCropObjectiveType configured = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.WHEAT);

            PlayerHarvestBlockEvent event = mock(PlayerHarvestBlockEvent.class);
            when(event.getHarvestedBlock()).thenReturn(block);

            HarvestCropQuestContext context = new HarvestCropQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("returns 0 when harvested block does not match filter")
        @Test
        public void processProgress_returnsZero_whenBlockDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("WHEAT"));

            HarvestCropObjectiveType configured = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.CARROTS);

            PlayerHarvestBlockEvent event = mock(PlayerHarvestBlockEvent.class);
            when(event.getHarvestedBlock()).thenReturn(block);

            HarvestCropQuestContext context = new HarvestCropQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("returns 1 when block matches any entry in multi-block filter")
        @Test
        public void processProgress_returnsOne_whenBlockMatchesAnyInMultiFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("WHEAT", "CARROTS", "POTATOES"));

            HarvestCropObjectiveType configured = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.POTATOES);

            PlayerHarvestBlockEvent event = mock(PlayerHarvestBlockEvent.class);
            when(event.getHarvestedBlock()).thenReturn(block);

            HarvestCropQuestContext context = new HarvestCropQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("ParseConfig")
    class ParseConfig {

        @DisplayName("returns instance with empty filter when section has no blocks key")
        @Test
        public void parseConfig_returnsEmptyFilter_whenNoBlocksKey() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(false);

            HarvestCropObjectiveType parsed = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.WHEAT);

            PlayerHarvestBlockEvent event = mock(PlayerHarvestBlockEvent.class);
            when(event.getHarvestedBlock()).thenReturn(block);

            HarvestCropQuestContext context = new HarvestCropQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, context));
        }

        @DisplayName("returns configured instance when section has blocks key")
        @Test
        public void parseConfig_returnsConfiguredFilter_whenBlocksKeyPresent() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("WHEAT"));

            HarvestCropObjectiveType parsed = type.parseConfig(section);

            Block matchingBlock = mock(Block.class);
            when(matchingBlock.getType()).thenReturn(Material.WHEAT);
            PlayerHarvestBlockEvent matchEvent = mock(PlayerHarvestBlockEvent.class);
            when(matchEvent.getHarvestedBlock()).thenReturn(matchingBlock);

            Block nonMatchingBlock = mock(Block.class);
            when(nonMatchingBlock.getType()).thenReturn(Material.CARROTS);
            PlayerHarvestBlockEvent noMatchEvent = mock(PlayerHarvestBlockEvent.class);
            when(noMatchEvent.getHarvestedBlock()).thenReturn(nonMatchingBlock);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, new HarvestCropQuestContext(matchEvent)));
            assertEquals(0, parsed.processProgress(instance, new HarvestCropQuestContext(noMatchEvent)));
        }
    }
}
