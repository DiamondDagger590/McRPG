package us.eunoians.mcrpg.loadout;

import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadoutDisplayTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @DisplayName("Material constructor sets material and display name")
        @Test
        void constructor_material_setsFields() {
            LoadoutDisplay display = new LoadoutDisplay(Material.DIAMOND_SWORD, "My Loadout");

            assertTrue(display.getDisplayItem().material().isPresent());
            assertEquals(Material.DIAMOND_SWORD, display.getDisplayItem().material().orElseThrow());
            assertEquals(Optional.of("My Loadout"), display.getDisplayName());
        }

        @DisplayName("Material constructor with null display name returns empty optional")
        @Test
        void constructor_material_nullDisplayName_returnsEmpty() {
            LoadoutDisplay display = new LoadoutDisplay(Material.IRON_SWORD, null);

            assertEquals(Optional.empty(), display.getDisplayName());
        }

        @DisplayName("ItemStack constructor sets material from item type")
        @Test
        void constructor_itemStack_setsMaterial() {
            ItemStack itemStack = new ItemStack(Material.GOLDEN_AXE);
            LoadoutDisplay display = new LoadoutDisplay(itemStack, "Chopper");

            assertNotNull(display.getDisplayItem());
            assertEquals(Optional.of("Chopper"), display.getDisplayName());
        }

        @DisplayName("CustomItemWrapper constructor preserves wrapper")
        @Test
        void constructor_customItemWrapper_preservesWrapper() {
            CustomItemWrapper wrapper = new CustomItemWrapper(Material.BOW);
            LoadoutDisplay display = new LoadoutDisplay(wrapper, "Ranged");

            assertEquals(wrapper, display.getDisplayItem());
            assertEquals(Optional.of("Ranged"), display.getDisplayName());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @DisplayName("setDisplayItem with Material updates display item")
        @Test
        void setDisplayItem_material_updatesItem() {
            LoadoutDisplay display = new LoadoutDisplay(Material.STONE, "Original");

            display.setDisplayItem(Material.DIAMOND);

            assertTrue(display.getDisplayItem().material().isPresent());
            assertEquals(Material.DIAMOND, display.getDisplayItem().material().orElseThrow());
        }

        @DisplayName("setDisplayItem with ItemStack updates display item")
        @Test
        void setDisplayItem_itemStack_updatesItem() {
            LoadoutDisplay display = new LoadoutDisplay(Material.STONE, "Original");

            display.setDisplayItem(new ItemStack(Material.EMERALD));

            assertNotNull(display.getDisplayItem());
        }

        @DisplayName("setDisplayName updates name")
        @Test
        void setDisplayName_updatesName() {
            LoadoutDisplay display = new LoadoutDisplay(Material.STONE, "Original");

            display.setDisplayName("Updated");

            assertEquals(Optional.of("Updated"), display.getDisplayName());
        }

        @DisplayName("setDisplayName to null clears name")
        @Test
        void setDisplayName_null_clearsName() {
            LoadoutDisplay display = new LoadoutDisplay(Material.STONE, "Original");

            display.setDisplayName(null);

            assertEquals(Optional.empty(), display.getDisplayName());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @DisplayName("equal displays with same material and name are equal")
        @Test
        void equals_sameMaterialAndName_areEqual() {
            LoadoutDisplay display1 = new LoadoutDisplay(Material.DIAMOND_SWORD, "PvP");
            LoadoutDisplay display2 = new LoadoutDisplay(Material.DIAMOND_SWORD, "PvP");

            assertEquals(display1, display2);
            assertEquals(display1.hashCode(), display2.hashCode());
        }

        @DisplayName("displays with different materials are not equal")
        @Test
        void equals_differentMaterial_notEqual() {
            LoadoutDisplay display1 = new LoadoutDisplay(Material.DIAMOND_SWORD, "PvP");
            LoadoutDisplay display2 = new LoadoutDisplay(Material.IRON_SWORD, "PvP");

            assertNotEquals(display1, display2);
        }

        @DisplayName("displays with different names are not equal")
        @Test
        void equals_differentName_notEqual() {
            LoadoutDisplay display1 = new LoadoutDisplay(Material.DIAMOND_SWORD, "PvP");
            LoadoutDisplay display2 = new LoadoutDisplay(Material.DIAMOND_SWORD, "PvE");

            assertNotEquals(display1, display2);
        }

        @DisplayName("display with null name vs non-null name are not equal")
        @Test
        void equals_nullVsNonNullName_notEqual() {
            LoadoutDisplay display1 = new LoadoutDisplay(Material.DIAMOND_SWORD, null);
            LoadoutDisplay display2 = new LoadoutDisplay(Material.DIAMOND_SWORD, "PvP");

            assertNotEquals(display1, display2);
        }

        @DisplayName("two displays with null names and same material are equal")
        @Test
        void equals_bothNullNames_sameMaterial_areEqual() {
            LoadoutDisplay display1 = new LoadoutDisplay(Material.DIAMOND_SWORD, null);
            LoadoutDisplay display2 = new LoadoutDisplay(Material.DIAMOND_SWORD, null);

            assertEquals(display1, display2);
            assertEquals(display1.hashCode(), display2.hashCode());
        }

        @DisplayName("equals returns false for non-LoadoutDisplay object")
        @Test
        void equals_differentType_returnsFalse() {
            LoadoutDisplay display = new LoadoutDisplay(Material.STONE, "Test");

            assertNotEquals(display, "not a LoadoutDisplay");
        }

        @DisplayName("equals returns false for null")
        @Test
        void equals_null_returnsFalse() {
            LoadoutDisplay display = new LoadoutDisplay(Material.STONE, "Test");

            assertFalse(display.equals(null));
        }
    }

    @Nested
    @DisplayName("clone")
    class Clone {

        @DisplayName("clone returns a new object with same properties")
        @Test
        void clone_returnsEqualButDistinctObject() {
            LoadoutDisplay original = new LoadoutDisplay(Material.DIAMOND_PICKAXE, "Mining");

            Object cloned = original.clone();

            assertNotNull(cloned);
            assertNotSame(original, cloned);
            assertEquals(original, cloned);
        }

        @DisplayName("clone with null display name preserves null")
        @Test
        void clone_nullDisplayName_preservesNull() {
            LoadoutDisplay original = new LoadoutDisplay(Material.DIAMOND_PICKAXE, null);

            LoadoutDisplay cloned = (LoadoutDisplay) original.clone();

            assertEquals(Optional.empty(), cloned.getDisplayName());
            assertEquals(original, cloned);
        }

        @DisplayName("modifying clone does not affect original")
        @Test
        void clone_modifyClone_doesNotAffectOriginal() {
            LoadoutDisplay original = new LoadoutDisplay(Material.DIAMOND_PICKAXE, "Mining");
            LoadoutDisplay cloned = (LoadoutDisplay) original.clone();

            cloned.setDisplayName("Changed");

            assertEquals(Optional.of("Mining"), original.getDisplayName());
            assertEquals(Optional.of("Changed"), cloned.getDisplayName());
        }
    }
}
