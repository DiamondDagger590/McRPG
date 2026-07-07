package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
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

public class BlockPlaceObjectiveTypeTest extends McRPGBaseTest {

    private BlockPlaceObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new BlockPlaceObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns block_place key")
        @Test
        public void getKey_returnsBlockPlaceKey() {
            assertEquals(BlockPlaceObjectiveType.KEY, type.getKey());
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

        @DisplayName("returns true for BlockPlaceQuestContext")
        @Test
        public void canProcess_returnsTrue_forBlockPlaceContext() {
            BlockPlaceEvent mockEvent = mock(BlockPlaceEvent.class);
            BlockPlaceQuestContext context = new BlockPlaceQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("returns false for non-BlockPlace context")
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
            when(block.getType()).thenReturn(Material.COBBLESTONE);

            BlockPlaceEvent event = mock(BlockPlaceEvent.class);
            when(event.getBlock()).thenReturn(block);

            BlockPlaceQuestContext context = new BlockPlaceQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, type.processProgress(instance, context));
        }

        @DisplayName("returns 1 when placed block matches filter")
        @Test
        public void processProgress_returnsOne_whenBlockMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("STONE"));

            BlockPlaceObjectiveType configured = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.STONE);

            BlockPlaceEvent event = mock(BlockPlaceEvent.class);
            when(event.getBlock()).thenReturn(block);

            BlockPlaceQuestContext context = new BlockPlaceQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, configured.processProgress(instance, context));
        }

        @DisplayName("returns 0 when placed block does not match filter")
        @Test
        public void processProgress_returnsZero_whenBlockDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("STONE"));

            BlockPlaceObjectiveType configured = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.DIRT);

            BlockPlaceEvent event = mock(BlockPlaceEvent.class);
            when(event.getBlock()).thenReturn(block);

            BlockPlaceQuestContext context = new BlockPlaceQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("returns 1 when block matches any entry in multi-block filter")
        @Test
        public void processProgress_returnsOne_whenBlockMatchesAnyInMultiFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("STONE", "COBBLESTONE", "GRANITE"));

            BlockPlaceObjectiveType configured = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.GRANITE);

            BlockPlaceEvent event = mock(BlockPlaceEvent.class);
            when(event.getBlock()).thenReturn(block);

            BlockPlaceQuestContext context = new BlockPlaceQuestContext(event);
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

            BlockPlaceObjectiveType parsed = type.parseConfig(section);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.STONE);

            BlockPlaceEvent event = mock(BlockPlaceEvent.class);
            when(event.getBlock()).thenReturn(block);

            BlockPlaceQuestContext context = new BlockPlaceQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, context));
        }

        @DisplayName("returns configured instance when section has blocks key")
        @Test
        public void parseConfig_returnsConfiguredFilter_whenBlocksKeyPresent() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("DIAMOND_BLOCK"));

            BlockPlaceObjectiveType parsed = type.parseConfig(section);

            Block matchingBlock = mock(Block.class);
            when(matchingBlock.getType()).thenReturn(Material.DIAMOND_BLOCK);
            BlockPlaceEvent matchEvent = mock(BlockPlaceEvent.class);
            when(matchEvent.getBlock()).thenReturn(matchingBlock);

            Block nonMatchingBlock = mock(Block.class);
            when(nonMatchingBlock.getType()).thenReturn(Material.GOLD_BLOCK);
            BlockPlaceEvent noMatchEvent = mock(BlockPlaceEvent.class);
            when(noMatchEvent.getBlock()).thenReturn(nonMatchingBlock);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1, parsed.processProgress(instance, new BlockPlaceQuestContext(matchEvent)));
            assertEquals(0, parsed.processProgress(instance, new BlockPlaceQuestContext(noMatchEvent)));
        }
    }
}
