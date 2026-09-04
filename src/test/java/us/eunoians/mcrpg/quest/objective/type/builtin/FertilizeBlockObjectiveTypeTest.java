package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockFertilizeEvent;
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("FertilizeBlockObjectiveType")
public class FertilizeBlockObjectiveTypeTest extends McRPGBaseTest {

    private FertilizeBlockObjectiveType type;
    private final QuestObjectiveInstance mockInstance = mock(QuestObjectiveInstance.class);

    @BeforeEach
    public void setup() {
        type = new FertilizeBlockObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for FertilizeBlockQuestContext")
        public void canProcess_returnsTrue_forFertilizeBlockContext() {
            BlockFertilizeEvent mockEvent = mock(BlockFertilizeEvent.class);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic context")
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("key returns fertilize_block")
        public void getKey_returnsFertilizeBlockKey() {
            assertEquals(FertilizeBlockObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("expansion key is McRPGExpansion")
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns a new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(false);
            FertilizeBlockObjectiveType parsed = type.parseConfig(section);
            assertNotSame(type, parsed);
        }

        @Test
        @DisplayName("no blocks key produces empty filter")
        public void parseConfig_noBlocksKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(false);
            FertilizeBlockObjectiveType parsed = type.parseConfig(section);

            BlockFertilizeEvent event = createMockFertilizeEvent(Material.GRASS_BLOCK);
            assertEquals(1, parsed.processProgress(mockInstance, new FertilizeBlockQuestContext(event)));
        }

        @Test
        @DisplayName("blocks key with entries produces filtering type")
        public void parseConfig_withBlocks_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("GRASS_BLOCK"));
            FertilizeBlockObjectiveType parsed = type.parseConfig(section);

            BlockFertilizeEvent matchEvent = createMockFertilizeEvent(Material.GRASS_BLOCK);
            assertEquals(1, parsed.processProgress(mockInstance, new FertilizeBlockQuestContext(matchEvent)));

            BlockFertilizeEvent noMatchEvent = createMockFertilizeEvent(Material.DIRT);
            assertEquals(0, parsed.processProgress(mockInstance, new FertilizeBlockQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("parsed instance preserves key")
        public void parseConfig_preservesKey() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(false);
            FertilizeBlockObjectiveType parsed = type.parseConfig(section);
            assertEquals(FertilizeBlockObjectiveType.KEY, parsed.getKey());
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type accepts any block")
        public void processProgress_returnsOne_whenNoFilter() {
            BlockFertilizeEvent event = createMockFertilizeEvent(Material.FARMLAND);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("configured type returns 1 for matching block")
        public void processProgress_returnsOne_forMatchingBlock() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("GRASS_BLOCK", "FARMLAND"));
            FertilizeBlockObjectiveType configured = type.parseConfig(section);

            BlockFertilizeEvent event = createMockFertilizeEvent(Material.FARMLAND);
            assertEquals(1, configured.processProgress(mockInstance, new FertilizeBlockQuestContext(event)));
        }

        @Test
        @DisplayName("configured type returns 0 for non-matching block")
        public void processProgress_returnsZero_forNonMatchingBlock() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("GRASS_BLOCK"));
            FertilizeBlockObjectiveType configured = type.parseConfig(section);

            BlockFertilizeEvent event = createMockFertilizeEvent(Material.STONE);
            assertEquals(0, configured.processProgress(mockInstance, new FertilizeBlockQuestContext(event)));
        }
    }

    private BlockFertilizeEvent createMockFertilizeEvent(Material material) {
        BlockFertilizeEvent event = mock(BlockFertilizeEvent.class);
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(material);
        when(event.getBlock()).thenReturn(block);
        return event;
    }
}
