package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
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

@DisplayName("VillagerTradeObjectiveType — extended coverage")
public class VillagerTradeObjectiveTypeCoverageTest extends McRPGBaseTest {

    private VillagerTradeObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new VillagerTradeObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for VillagerTradeQuestContext")
        public void canProcess_returnsTrue_forVillagerTradeContext() {
            PlayerTradeEvent mockEvent = mock(PlayerTradeEvent.class);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(mockEvent);
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
        @DisplayName("returns trade result amount for any trade when no filter is set")
        public void processProgress_returnsAmount_whenNoFilter() {
            PlayerTradeEvent event = createMockTradeEvent(Material.EMERALD, 3);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(3, progress);
        }

        @Test
        @DisplayName("returns 1 for single-item trade result")
        public void processProgress_returnsOne_forSingleItemTrade_whenNoFilter() {
            PlayerTradeEvent event = createMockTradeEvent(Material.DIAMOND_SWORD, 1);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with item filter via parseConfig")
    class ProcessProgressWithFilter {

        private VillagerTradeObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("EMERALD", "DIAMOND"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns trade result amount for matching item")
        public void processProgress_returnsAmount_forMatchingItem() {
            PlayerTradeEvent event = createMockTradeEvent(Material.EMERALD, 5);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(5, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching item")
        public void processProgress_returnsZero_forNonMatchingItem() {
            PlayerTradeEvent event = createMockTradeEvent(Material.BREAD, 8);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns trade result amount for second matching item in filter")
        public void processProgress_returnsAmount_forSecondMatchingItem() {
            PlayerTradeEvent event = createMockTradeEvent(Material.DIAMOND, 2);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(2, progress);
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
        @DisplayName("key value is villager_trade")
        public void getKey_valueIsVillagerTrade() {
            assertEquals("villager_trade", baseType.getKey().getKey());
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

            VillagerTradeObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no items key produces empty filter that accepts any trade")
        public void parseConfig_noItemsKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);

            VillagerTradeObjectiveType parsed = baseType.parseConfig(section);

            PlayerTradeEvent event = createMockTradeEvent(Material.WHEAT, 4);
            VillagerTradeQuestContext context = new VillagerTradeQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(4, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("items key with entries produces filtering type")
        public void parseConfig_withItems_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("EMERALD"));

            VillagerTradeObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerTradeEvent matchEvent = createMockTradeEvent(Material.EMERALD, 6);
            assertEquals(6, parsed.processProgress(instance, new VillagerTradeQuestContext(matchEvent)));

            PlayerTradeEvent noMatchEvent = createMockTradeEvent(Material.BREAD, 6);
            assertEquals(0, parsed.processProgress(instance, new VillagerTradeQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("items key with empty list produces empty filter")
        public void parseConfig_emptyItemsList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of());

            VillagerTradeObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerTradeEvent event = createMockTradeEvent(Material.PAPER, 12);
            assertEquals(12, parsed.processProgress(instance, new VillagerTradeQuestContext(event)));
        }
    }

    private PlayerTradeEvent createMockTradeEvent(Material resultMaterial, int amount) {
        PlayerTradeEvent event = mock(PlayerTradeEvent.class);
        MerchantRecipe recipe = mock(MerchantRecipe.class);
        ItemStack result = mock(ItemStack.class);
        when(result.getType()).thenReturn(resultMaterial);
        when(result.getAmount()).thenReturn(amount);
        when(recipe.getResult()).thenReturn(result);
        when(event.getTrade()).thenReturn(recipe);
        return event;
    }
}
