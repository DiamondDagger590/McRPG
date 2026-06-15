package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
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

@DisplayName("BlockBreakObjectiveType — extended coverage")
public class BlockBreakObjectiveTypeCoverageTest extends McRPGBaseTest {

    private BlockBreakObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new BlockBreakObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for BlockBreakQuestContext")
        public void canProcess_returnsTrue_forBlockBreakContext() {
            BlockBreakEvent mockEvent = mock(BlockBreakEvent.class);
            BlockBreakQuestContext context = new BlockBreakQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for MobKillQuestContext")
        public void canProcess_returnsFalse_forMobKillContext() {
            MobKillQuestContext context = mock(MobKillQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty block filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any block when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            BlockBreakEvent event = createMockBlockBreakEvent(Material.STONE);
            BlockBreakQuestContext context = new BlockBreakQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for diamond ore when no filter is set")
        public void processProgress_returnsOne_forDiamondOre_whenNoFilter() {
            BlockBreakEvent event = createMockBlockBreakEvent(Material.DIAMOND_ORE);
            BlockBreakQuestContext context = new BlockBreakQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with block filter via parseConfig")
    class ProcessProgressWithFilter {

        private BlockBreakObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("DIAMOND_ORE", "IRON_ORE"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching block")
        public void processProgress_returnsOne_forMatchingBlock() {
            BlockBreakEvent event = createMockBlockBreakEvent(Material.DIAMOND_ORE);
            BlockBreakQuestContext context = new BlockBreakQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching block")
        public void processProgress_returnsZero_forNonMatchingBlock() {
            BlockBreakEvent event = createMockBlockBreakEvent(Material.STONE);
            BlockBreakQuestContext context = new BlockBreakQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching block in filter")
        public void processProgress_returnsOne_forSecondMatchingBlock() {
            BlockBreakEvent event = createMockBlockBreakEvent(Material.IRON_ORE);
            BlockBreakQuestContext context = new BlockBreakQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for non-BlockBreak context")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 for MobKillQuestContext")
        public void processProgress_returnsZero_forMobKillContext() {
            MobKillQuestContext context = mock(MobKillQuestContext.class);
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
        @DisplayName("key value is block_break")
        public void getKey_valueIsBlockBreak() {
            assertEquals("block_break", baseType.getKey().getKey());
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

            BlockBreakObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no blocks key produces empty filter that accepts any block")
        public void parseConfig_noBlocksKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(false);

            BlockBreakObjectiveType parsed = baseType.parseConfig(section);

            BlockBreakEvent event = createMockBlockBreakEvent(Material.DIRT);
            BlockBreakQuestContext context = new BlockBreakQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("blocks key with entries produces filtering type")
        public void parseConfig_withBlocks_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of("DIAMOND_ORE"));

            BlockBreakObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            BlockBreakEvent matchEvent = createMockBlockBreakEvent(Material.DIAMOND_ORE);
            assertEquals(1, parsed.processProgress(instance, new BlockBreakQuestContext(matchEvent)));

            BlockBreakEvent noMatchEvent = createMockBlockBreakEvent(Material.STONE);
            assertEquals(0, parsed.processProgress(instance, new BlockBreakQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("blocks key with empty list produces empty filter")
        public void parseConfig_emptyBlocksList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("blocks")).thenReturn(true);
            when(section.getStringList("blocks")).thenReturn(List.of());

            BlockBreakObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            BlockBreakEvent event = createMockBlockBreakEvent(Material.COBBLESTONE);
            assertEquals(1, parsed.processProgress(instance, new BlockBreakQuestContext(event)));
        }
    }

    private BlockBreakEvent createMockBlockBreakEvent(Material material) {
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        Block block = mock(Block.class);
        when(block.getType()).thenReturn(material);
        when(event.getBlock()).thenReturn(block);
        return event;
    }
}
