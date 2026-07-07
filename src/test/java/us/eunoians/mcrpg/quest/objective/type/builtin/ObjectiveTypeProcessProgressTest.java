package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Objective type processProgress coverage")
public class ObjectiveTypeProcessProgressTest extends McRPGBaseTest {

    private final QuestObjectiveInstance mockInstance = mock(QuestObjectiveInstance.class);

    @Nested
    @DisplayName("DamageTakenObjectiveType processProgress")
    class DamageTakenProcessProgress {

        private final DamageTakenObjectiveType type = new DamageTakenObjectiveType();

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_wrongContextType_returnsZero() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("zero damage returns 0")
        public void processProgress_zeroDamage_returnsZero() {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(0.0);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("negative damage returns 0")
        public void processProgress_negativeDamage_returnsZero() {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(-5.0);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(0, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("unconfigured accepts any damage cause")
        public void processProgress_unconfigured_acceptsAnyCause() {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(7.3);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(7, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("rounds damage up at 0.5")
        public void processProgress_roundsDamageUp() {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(3.5);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(4, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("rounds damage down below 0.5")
        public void processProgress_roundsDamageDown() {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(3.4);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(3, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("configured with matching cause returns damage")
        public void processProgress_matchingCause_returnsDamage() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("FALL", "FIRE"));
            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(5.0);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(5, configured.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("configured with non-matching cause returns 0")
        public void processProgress_nonMatchingCause_returnsZero() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("FALL"));
            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(5.0);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.DROWNING);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(0, configured.processProgress(mockInstance, context));
        }
    }

    @Nested
    @DisplayName("DamageTakenObjectiveType parseConfig")
    class DamageTakenParseConfig {

        private final DamageTakenObjectiveType type = new DamageTakenObjectiveType();

        @Test
        @DisplayName("section without causes key accepts any cause")
        public void parseConfig_noCausesKey_acceptsAnyCause() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(false);
            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(10.0);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.VOID);
            DamageTakenQuestContext context = new DamageTakenQuestContext(event);
            assertEquals(10, configured.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("section with causes key filters by cause")
        public void parseConfig_withCauses_filtersCorrectly() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("FIRE", "LAVA"));
            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent fireEvent = mock(EntityDamageEvent.class);
            when(fireEvent.getFinalDamage()).thenReturn(3.0);
            when(fireEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            assertEquals(3, configured.processProgress(mockInstance, new DamageTakenQuestContext(fireEvent)));

            EntityDamageEvent fallEvent = mock(EntityDamageEvent.class);
            when(fallEvent.getFinalDamage()).thenReturn(5.0);
            when(fallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            assertEquals(0, configured.processProgress(mockInstance, new DamageTakenQuestContext(fallEvent)));
        }

        @Test
        @DisplayName("causes are parsed case-insensitively")
        public void parseConfig_causesAreCaseInsensitive() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("fall"));
            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getFinalDamage()).thenReturn(2.0);
            when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            assertEquals(2, configured.processProgress(mockInstance, new DamageTakenQuestContext(event)));
        }

        @Test
        @DisplayName("parsed instance preserves key")
        public void parseConfig_preservesKey() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(false);
            DamageTakenObjectiveType configured = type.parseConfig(section);
            assertEquals(DamageTakenObjectiveType.KEY, configured.getKey());
        }
    }

    @Nested
    @DisplayName("BlockPlaceObjectiveType processProgress")
    class BlockPlaceProcessProgress {

        private final BlockPlaceObjectiveType type = new BlockPlaceObjectiveType();

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_wrongContextType_returnsZero() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured accepts any block")
        public void processProgress_unconfigured_returnsOne() {
            BlockPlaceEvent event = mock(BlockPlaceEvent.class);
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.STONE);
            when(event.getBlock()).thenReturn(block);
            BlockPlaceQuestContext context = new BlockPlaceQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }
    }

    @Nested
    @DisplayName("ConsumeItemObjectiveType processProgress")
    class ConsumeItemProcessProgress {

        private final ConsumeItemObjectiveType type = new ConsumeItemObjectiveType();

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_wrongContextType_returnsZero() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured accepts any item")
        public void processProgress_unconfigured_returnsOne() {
            PlayerItemConsumeEvent event = mock(PlayerItemConsumeEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.GOLDEN_APPLE));
            ConsumeItemQuestContext context = new ConsumeItemQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }
    }

    @Nested
    @DisplayName("CraftItemObjectiveType processProgress")
    class CraftItemProcessProgress {

        private final CraftItemObjectiveType type = new CraftItemObjectiveType();

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_wrongContextType_returnsZero() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("normal click returns result amount")
        public void processProgress_normalClick_returnsResultAmount() {
            CraftItemEvent event = mock(CraftItemEvent.class);
            Recipe recipe = mock(Recipe.class);
            when(recipe.getResult()).thenReturn(new ItemStack(Material.DIAMOND_SWORD, 1));
            when(event.getRecipe()).thenReturn(recipe);
            when(event.isShiftClick()).thenReturn(false);
            CraftItemQuestContext context = new CraftItemQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("normal click with multi-amount result returns full amount")
        public void processProgress_normalClick_multiAmountResult() {
            CraftItemEvent event = mock(CraftItemEvent.class);
            Recipe recipe = mock(Recipe.class);
            when(recipe.getResult()).thenReturn(new ItemStack(Material.STICK, 4));
            when(event.getRecipe()).thenReturn(recipe);
            when(event.isShiftClick()).thenReturn(false);
            CraftItemQuestContext context = new CraftItemQuestContext(event);
            assertEquals(4, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("shift click computes max crafts from matrix")
        public void processProgress_shiftClick_computesMaxCrafts() {
            CraftItemEvent event = mock(CraftItemEvent.class);
            Recipe recipe = mock(Recipe.class);
            when(recipe.getResult()).thenReturn(new ItemStack(Material.STICK, 4));
            when(event.getRecipe()).thenReturn(recipe);
            when(event.isShiftClick()).thenReturn(true);
            CraftingInventory inventory = mock(CraftingInventory.class);
            when(event.getInventory()).thenReturn(inventory);
            ItemStack[] matrix = new ItemStack[]{
                    new ItemStack(Material.OAK_PLANKS, 3),
                    new ItemStack(Material.OAK_PLANKS, 3),
                    null, null, null, null, null, null, null
            };
            when(inventory.getMatrix()).thenReturn(matrix);
            CraftItemQuestContext context = new CraftItemQuestContext(event);
            assertEquals(12, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("shift click uses minimum slot amount")
        public void processProgress_shiftClick_usesMinimumSlotAmount() {
            CraftItemEvent event = mock(CraftItemEvent.class);
            Recipe recipe = mock(Recipe.class);
            when(recipe.getResult()).thenReturn(new ItemStack(Material.IRON_INGOT, 1));
            when(event.getRecipe()).thenReturn(recipe);
            when(event.isShiftClick()).thenReturn(true);
            CraftingInventory inventory = mock(CraftingInventory.class);
            when(event.getInventory()).thenReturn(inventory);
            ItemStack[] matrix = new ItemStack[]{
                    new ItemStack(Material.IRON_ORE, 5),
                    new ItemStack(Material.COAL, 2),
                    null, null, null, null, null, null, null
            };
            when(inventory.getMatrix()).thenReturn(matrix);
            CraftItemQuestContext context = new CraftItemQuestContext(event);
            assertEquals(2, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("shift click with all-null matrix defaults to 1 craft")
        public void processProgress_shiftClickEmptyMatrix_defaultsToOneCraft() {
            CraftItemEvent event = mock(CraftItemEvent.class);
            Recipe recipe = mock(Recipe.class);
            when(recipe.getResult()).thenReturn(new ItemStack(Material.DIAMOND_SWORD, 1));
            when(event.getRecipe()).thenReturn(recipe);
            when(event.isShiftClick()).thenReturn(true);
            CraftingInventory inventory = mock(CraftingInventory.class);
            when(event.getInventory()).thenReturn(inventory);
            ItemStack[] matrix = new ItemStack[]{null, null, null, null, null, null, null, null, null};
            when(inventory.getMatrix()).thenReturn(matrix);
            CraftItemQuestContext context = new CraftItemQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }
    }
}
