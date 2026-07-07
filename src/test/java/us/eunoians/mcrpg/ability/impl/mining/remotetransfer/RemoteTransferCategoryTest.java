package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RemoteTransferCategory")
class RemoteTransferCategoryTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Single-argument constructor")
    class SingleArgConstructor {

        @Test
        @DisplayName("getCategoryKey returns constructor key")
        void getCategoryKey_returnsConstructorKey() {
            RemoteTransferCategory category = new RemoteTransferCategory("ores");
            assertEquals("ores", category.getCategoryKey());
        }

        @Test
        @DisplayName("getCategoryItems returns empty set")
        void getCategoryItems_returnsEmptySet() {
            RemoteTransferCategory category = new RemoteTransferCategory("ores");
            assertTrue(category.getCategoryItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("Two-argument constructor")
    class TwoArgConstructor {

        @Test
        @DisplayName("getCategoryKey returns constructor key")
        void getCategoryKey_returnsConstructorKey() {
            CustomItemWrapper wrapper = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));
            RemoteTransferCategory category = new RemoteTransferCategory("ores", new HashSet<>(Set.of(wrapper)));
            assertEquals("ores", category.getCategoryKey());
        }

        @Test
        @DisplayName("getCategoryItems returns provided items")
        void getCategoryItems_returnsProvidedItems() {
            CustomItemWrapper wrapper = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));
            RemoteTransferCategory category = new RemoteTransferCategory("ores", new HashSet<>(Set.of(wrapper)));
            assertEquals(1, category.getCategoryItems().size());
            assertTrue(category.getCategoryItems().contains(wrapper));
        }
    }

    @Nested
    @DisplayName("getCategoryItems")
    class GetCategoryItems {

        @Test
        @DisplayName("returns defensive copy")
        void getCategoryItems_returnsDefensiveCopy() {
            CustomItemWrapper wrapper = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));
            RemoteTransferCategory category = new RemoteTransferCategory("ores", new HashSet<>(Set.of(wrapper)));
            Set<CustomItemWrapper> items = category.getCategoryItems();
            try {
                items.add(new CustomItemWrapper(new ItemStack(Material.GOLD_ORE)));
            } catch (UnsupportedOperationException ignored) {
                // Expected for unmodifiable sets
            }
            assertEquals(1, category.getCategoryItems().size());
        }
    }

    @Nested
    @DisplayName("setCategoryBlocks")
    class SetCategoryBlocks {

        @Test
        @DisplayName("replaces existing items")
        void setCategoryBlocks_replacesExistingItems() {
            CustomItemWrapper iron = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));
            CustomItemWrapper gold = new CustomItemWrapper(new ItemStack(Material.GOLD_ORE));
            RemoteTransferCategory category = new RemoteTransferCategory("ores", new HashSet<>(Set.of(iron)));

            category.setCategoryBlocks(Set.of(gold));

            assertEquals(1, category.getCategoryItems().size());
            assertTrue(category.getCategoryItems().contains(gold));
            assertFalse(category.getCategoryItems().contains(iron));
        }

        @Test
        @DisplayName("clears items when given empty set")
        void setCategoryBlocks_clearsItemsWhenEmpty() {
            CustomItemWrapper iron = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));
            RemoteTransferCategory category = new RemoteTransferCategory("ores", new HashSet<>(Set.of(iron)));

            category.setCategoryBlocks(Set.of());

            assertTrue(category.getCategoryItems().isEmpty());
        }
    }

    @Nested
    @DisplayName("addCategoryItem")
    class AddCategoryItem {

        @Test
        @DisplayName("adds item to empty category")
        void addCategoryItem_addsToEmptyCategory() {
            RemoteTransferCategory category = new RemoteTransferCategory("ores");
            CustomItemWrapper iron = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));

            category.addCategoryItem(iron);

            assertEquals(1, category.getCategoryItems().size());
            assertTrue(category.getCategoryItems().contains(iron));
        }

        @Test
        @DisplayName("adds item to existing category")
        void addCategoryItem_addsToExistingCategory() {
            CustomItemWrapper iron = new CustomItemWrapper(new ItemStack(Material.IRON_ORE));
            RemoteTransferCategory category = new RemoteTransferCategory("ores", new HashSet<>(Set.of(iron)));
            CustomItemWrapper gold = new CustomItemWrapper(new ItemStack(Material.GOLD_ORE));

            category.addCategoryItem(gold);

            assertEquals(2, category.getCategoryItems().size());
            assertTrue(category.getCategoryItems().contains(iron));
            assertTrue(category.getCategoryItems().contains(gold));
        }
    }
}
