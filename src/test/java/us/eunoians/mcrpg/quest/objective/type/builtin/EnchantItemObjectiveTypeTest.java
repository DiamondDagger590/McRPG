package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnchantItemObjectiveTypeTest extends McRPGBaseTest {

    private EnchantItemObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new EnchantItemObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns enchant_item key")
        void getKey_returnsEnchantItemKey() {
            assertEquals(EnchantItemObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for EnchantItemQuestContext")
        void canProcess_returnsTrue_forEnchantItemContext() {
            EnchantItemEvent mockEvent = mock(EnchantItemEvent.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for non-matching context")
        void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no items or enchantments keys leaves both filters empty")
        void parseConfig_noKeys_emptyFilters() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);
            when(section.contains("enchantments")).thenReturn(false);

            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("sharpness"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 3));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses items list from config")
        void parseConfig_withItems_restrictsToThoseItems() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));
            when(section.contains("enchantments")).thenReturn(false);

            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("sharpness"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 1));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses enchantments list from config")
        void parseConfig_withEnchantments_restrictsToThoseEnchantments() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);
            when(section.contains("enchantments")).thenReturn(true);
            when(section.getStringList("enchantments")).thenReturn(List.of("sharpness"));

            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("sharpness"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 3));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0L, type.processProgress(instance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type returns 1 for any enchantment")
        void processProgress_returnsOne_whenUnconfigured() {
            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.IRON_PICKAXE));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("efficiency"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 4));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(1L, type.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when item does not match filter")
        void processProgress_returnsZero_whenItemDoesNotMatch() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));
            when(section.contains("enchantments")).thenReturn(false);
            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.IRON_PICKAXE));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when enchantment does not match filter")
        void processProgress_returnsZero_whenEnchantmentDoesNotMatch() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);
            when(section.contains("enchantments")).thenReturn(true);
            when(section.getStringList("enchantments")).thenReturn(List.of("sharpness"));
            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("efficiency"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 2));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 when both item and enchantment match dual filter")
        void processProgress_returnsOne_whenBothFiltersMatch() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));
            when(section.contains("enchantments")).thenReturn(true);
            when(section.getStringList("enchantments")).thenReturn(List.of("sharpness"));
            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("sharpness"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 5));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when item matches but enchantment does not in dual filter")
        void processProgress_returnsZero_whenItemMatchesButEnchantmentDoesNot() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));
            when(section.contains("enchantments")).thenReturn(true);
            when(section.getStringList("enchantments")).thenReturn(List.of("sharpness"));
            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("looting"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 1));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when enchantment matches but item does not in dual filter")
        void processProgress_returnsZero_whenEnchantmentMatchesButItemDoesNot() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(true);
            when(section.getStringList("items")).thenReturn(List.of("DIAMOND_SWORD"));
            when(section.contains("enchantments")).thenReturn(true);
            when(section.getStringList("enchantments")).thenReturn(List.of("sharpness"));
            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.IRON_PICKAXE));
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getKey()).thenReturn(NamespacedKey.minecraft("sharpness"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(enchantment, 3));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("matches when one of multiple enchantments matches")
        void processProgress_returnsOne_whenOneOfMultipleEnchantsMatches() {
            Section section = mock(Section.class);
            when(section.contains("items")).thenReturn(false);
            when(section.contains("enchantments")).thenReturn(true);
            when(section.getStringList("enchantments")).thenReturn(List.of("sharpness", "fire_aspect"));
            EnchantItemObjectiveType configured = type.parseConfig(section);

            EnchantItemEvent event = mock(EnchantItemEvent.class);
            when(event.getItem()).thenReturn(new ItemStack(Material.DIAMOND_SWORD));
            Enchantment sharpness = mock(Enchantment.class);
            when(sharpness.getKey()).thenReturn(NamespacedKey.minecraft("sharpness"));
            Enchantment knockback = mock(Enchantment.class);
            when(knockback.getKey()).thenReturn(NamespacedKey.minecraft("knockback"));
            when(event.getEnchantsToAdd()).thenReturn(Map.of(sharpness, 3, knockback, 1));

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            EnchantItemQuestContext context = new EnchantItemQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }
    }
}
