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
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("FertilizeBlockObjectiveType — extended coverage")
public class FertilizeBlockObjectiveTypeCoverageTest extends McRPGBaseTest {

    private FertilizeBlockObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new FertilizeBlockObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for FertilizeBlockQuestContext")
        public void canProcess_returnsTrue_forFertilizeBlockContext() {
            BlockFertilizeEvent mockEvent = mock(BlockFertilizeEvent.class);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(mockEvent);
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
    @DisplayName("processProgress — empty block filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any block when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            BlockFertilizeEvent event = createMockFertilizeEvent(Material.GRASS_BLOCK);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for farmland when no filter is set")
        public void processProgress_returnsOne_forFarmland_whenNoFilter() {
            BlockFertilizeEvent event = createMockFertilizeEvent(Material.FARMLAND);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with block filter via parseConfig")
    class ProcessProgressWithFilter {

        private FertilizeBlockObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("GRASS_BLOCK", "FARMLAND"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching block")
        public void processProgress_returnsOne_forMatchingBlock() {
            BlockFertilizeEvent event = createMockFertilizeEvent(Material.GRASS_BLOCK);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching block")
        public void processProgress_returnsZero_forNonMatchingBlock() {
            BlockFertilizeEvent event = createMockFertilizeEvent(Material.DIRT);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching block in filter")
        public void processProgress_returnsOne_forSecondMatchingBlock() {
            BlockFertilizeEvent event = createMockFertilizeEvent(Material.FARMLAND);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
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
        @DisplayName("key value is fertilize_block")
        public void getKey_valueIsFertilizeBlock() {
            assertEquals("fertilize_block", baseType.getKey().getKey());
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
            when(section.contains("blocks")).thenReturn(false);

            FertilizeBlockObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no blocks key produces empty filter that accepts any block")
        public void parseConfig_noBlocksKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(false);

            FertilizeBlockObjectiveType parsed = baseType.parseConfig(section);

            BlockFertilizeEvent event = createMockFertilizeEvent(Material.DIRT);
            FertilizeBlockQuestContext context = new FertilizeBlockQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("blocks key with entries produces filtering type")
        public void parseConfig_withBlocks_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("GRASS_BLOCK"));

            FertilizeBlockObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            BlockFertilizeEvent matchEvent = createMockFertilizeEvent(Material.GRASS_BLOCK);
            assertEquals(1, parsed.processProgress(instance, new FertilizeBlockQuestContext(matchEvent)));

            BlockFertilizeEvent noMatchEvent = createMockFertilizeEvent(Material.DIRT);
            assertEquals(0, parsed.processProgress(instance, new FertilizeBlockQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("blocks key with empty list produces empty filter")
        public void parseConfig_emptyBlocksList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of());

            FertilizeBlockObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            BlockFertilizeEvent event = createMockFertilizeEvent(Material.COBBLESTONE);
            assertEquals(1, parsed.processProgress(instance, new FertilizeBlockQuestContext(event)));
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
