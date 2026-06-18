package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RemoteTransferCategoryTypeTest extends McRPGBaseTest {

    @DisplayName("Every category type has a non-null name")
    @ParameterizedTest
    @EnumSource(RemoteTransferCategoryType.class)
    void getName_isNotNull(RemoteTransferCategoryType type) {
        assertNotNull(type.getName());
    }

    @DisplayName("Every category type has a non-null display material")
    @ParameterizedTest
    @EnumSource(RemoteTransferCategoryType.class)
    void getDisplayMaterial_isNotNull(RemoteTransferCategoryType type) {
        assertNotNull(type.getDisplayMaterial());
    }

    @DisplayName("Every category type has a non-null configuration route")
    @ParameterizedTest
    @EnumSource(RemoteTransferCategoryType.class)
    void getConfigurationRoute_isNotNull(RemoteTransferCategoryType type) {
        assertNotNull(type.getConfigurationRoute());
    }

    @Test
    @DisplayName("Enum has exactly 8 values")
    void values_hasExpectedCount() {
        assertEquals(8, RemoteTransferCategoryType.values().length);
    }

    @Test
    @DisplayName("CAVES has correct name and material")
    void caves_hasCorrectProperties() {
        assertEquals("Caves", RemoteTransferCategoryType.CAVES.getName());
        assertEquals(Material.AMETHYST_BLOCK, RemoteTransferCategoryType.CAVES.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_CAVES, RemoteTransferCategoryType.CAVES.getConfigurationRoute());
    }

    @Test
    @DisplayName("ORES has correct name and material")
    void ores_hasCorrectProperties() {
        assertEquals("Ores", RemoteTransferCategoryType.ORES.getName());
        assertEquals(Material.DIAMOND_ORE, RemoteTransferCategoryType.ORES.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_ORES, RemoteTransferCategoryType.ORES.getConfigurationRoute());
    }

    @Test
    @DisplayName("NETHER has correct name and material")
    void nether_hasCorrectProperties() {
        assertEquals("Nether", RemoteTransferCategoryType.NETHER.getName());
        assertEquals(Material.MAGMA_BLOCK, RemoteTransferCategoryType.NETHER.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_NETHER, RemoteTransferCategoryType.NETHER.getConfigurationRoute());
    }

    @Test
    @DisplayName("END has correct name and material")
    void end_hasCorrectProperties() {
        assertEquals("End", RemoteTransferCategoryType.END.getName());
        assertEquals(Material.ENDER_PEARL, RemoteTransferCategoryType.END.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_END, RemoteTransferCategoryType.END.getConfigurationRoute());
    }

    @Test
    @DisplayName("OCEAN has correct name and material")
    void ocean_hasCorrectProperties() {
        assertEquals("Ocean", RemoteTransferCategoryType.OCEAN.getName());
        assertEquals(Material.PRISMARINE, RemoteTransferCategoryType.OCEAN.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_OCEAN, RemoteTransferCategoryType.OCEAN.getConfigurationRoute());
    }

    @Test
    @DisplayName("OVERWORLD has correct name and material")
    void overworld_hasCorrectProperties() {
        assertEquals("Overworld", RemoteTransferCategoryType.OVERWORLD.getName());
        assertEquals(Material.MOSSY_COBBLESTONE, RemoteTransferCategoryType.OVERWORLD.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_OVERWORLD, RemoteTransferCategoryType.OVERWORLD.getConfigurationRoute());
    }

    @Test
    @DisplayName("TERRACOTTA has correct name and material")
    void terracotta_hasCorrectProperties() {
        assertEquals("Terracotta", RemoteTransferCategoryType.TERRACOTTA.getName());
        assertEquals(Material.TERRACOTTA, RemoteTransferCategoryType.TERRACOTTA.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_TERRACOTTA, RemoteTransferCategoryType.TERRACOTTA.getConfigurationRoute());
    }

    @Test
    @DisplayName("CUSTOM has correct name and material")
    void custom_hasCorrectProperties() {
        assertEquals("Custom", RemoteTransferCategoryType.CUSTOM.getName());
        assertEquals(Material.CRAFTING_TABLE, RemoteTransferCategoryType.CUSTOM.getDisplayMaterial());
        assertEquals(MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_CUSTOM, RemoteTransferCategoryType.CUSTOM.getConfigurationRoute());
    }
}
