package us.eunoians.mcrpg.ability.impl.mining.orescanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class OreScannerBlockTypeTest {

    @Nested
    @DisplayName("Record accessors")
    class Accessors {

        @Test
        @DisplayName("typeName returns constructor value")
        void typeName_returnsConstructorValue() {
            OreScannerBlockType type = new OreScannerBlockType(Set.of(Material.DIAMOND_ORE), "Diamonds", ChatColor.AQUA, 10);
            assertEquals("Diamonds", type.typeName());
        }

        @Test
        @DisplayName("color returns constructor value")
        void color_returnsConstructorValue() {
            OreScannerBlockType type = new OreScannerBlockType(Set.of(Material.IRON_ORE), "Iron", ChatColor.WHITE, 5);
            assertEquals(ChatColor.WHITE, type.color());
        }

        @Test
        @DisplayName("weight returns constructor value")
        void weight_returnsConstructorValue() {
            OreScannerBlockType type = new OreScannerBlockType(Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 8);
            assertEquals(8, type.weight());
        }
    }

    @Nested
    @DisplayName("scannableOres")
    class ScannableOres {

        @Test
        @DisplayName("returns set equal to constructor input")
        void scannableOres_returnsEqualSet() {
            Set<Material> original = Set.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE);
            OreScannerBlockType type = new OreScannerBlockType(original, "Diamonds", ChatColor.AQUA, 10);

            Set<Material> returned = type.scannableOres();
            assertEquals(original, returned);
        }

        @Test
        @DisplayName("returned set is immutable")
        void scannableOres_returnsImmutableSet() {
            OreScannerBlockType type = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            Set<Material> returned = type.scannableOres();
            org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                    () -> returned.add(Material.DIAMOND_ORE));
        }

        @Test
        @DisplayName("contains all provided materials")
        void scannableOres_containsAllMaterials() {
            Set<Material> materials = Set.of(Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE);
            OreScannerBlockType type = new OreScannerBlockType(materials, "Iron", ChatColor.WHITE, 5);

            Set<Material> returned = type.scannableOres();
            assertTrue(returned.contains(Material.IRON_ORE));
            assertTrue(returned.contains(Material.DEEPSLATE_IRON_ORE));
            assertEquals(2, returned.size());
        }
    }

    @Nested
    @DisplayName("isBlockScannable")
    class IsBlockScannable {

        @Test
        @DisplayName("returns true for matching block material")
        void isBlockScannable_returnsTrue_whenBlockMaterialMatches() {
            OreScannerBlockType type = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                    "Diamonds", ChatColor.AQUA, 10);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.DIAMOND_ORE);

            assertTrue(type.isBlockScannable(block));
        }

        @Test
        @DisplayName("returns true for second matching material in set")
        void isBlockScannable_returnsTrue_whenBlockMatchesSecondMaterial() {
            OreScannerBlockType type = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE),
                    "Diamonds", ChatColor.AQUA, 10);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.DEEPSLATE_DIAMOND_ORE);

            assertTrue(type.isBlockScannable(block));
        }

        @Test
        @DisplayName("returns false for non-matching block material")
        void isBlockScannable_returnsFalse_whenBlockMaterialDoesNotMatch() {
            OreScannerBlockType type = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE),
                    "Diamonds", ChatColor.AQUA, 10);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.STONE);

            assertFalse(type.isBlockScannable(block));
        }

        @Test
        @DisplayName("returns false for empty scannable set")
        void isBlockScannable_returnsFalse_whenSetIsEmpty() {
            OreScannerBlockType type = new OreScannerBlockType(
                    Set.of(), "Empty", ChatColor.WHITE, 0);

            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.DIAMOND_ORE);

            assertFalse(type.isBlockScannable(block));
        }
    }

    @Nested
    @DisplayName("Record equality")
    class RecordEquality {

        @Test
        @DisplayName("equal records have same hashCode")
        void equals_sameFields_sameHashCode() {
            OreScannerBlockType a = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            OreScannerBlockType b = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different weight means not equal")
        void equals_differentWeight_notEqual() {
            OreScannerBlockType a = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            OreScannerBlockType b = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 5);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("different materials means not equal")
        void equals_differentMaterials_notEqual() {
            OreScannerBlockType a = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            OreScannerBlockType b = new OreScannerBlockType(Set.of(Material.IRON_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            assertNotEquals(a, b);
        }
    }
}
