package us.eunoians.mcrpg.ability.impl.woodcutting;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HeavySwing} configuration value resolution and metadata accessors.
 */
class HeavySwingTest extends McRPGBaseTest {

    private YamlDocument woodcuttingConfig;
    private HeavySwing heavySwing;

    @BeforeEach
    void setUp() {
        woodcuttingConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodcuttingConfig);

        when(woodcuttingConfig.getStringList(WoodcuttingConfigFile.HEAVY_SWING_VALID_BLOCKS))
                .thenReturn(List.of("OAK_LOG", "BIRCH_LOG", "SPRUCE_LOG"));

        heavySwing = new HeavySwing(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 3;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*2.5");

            assertEquals(7.5, heavySwing.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            int tier = 1;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("5");

            assertEquals(5.0, heavySwing.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getActivationChance_prefersTierSpecific() {
            int tier = 2;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("15");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*2");

            assertEquals(15.0, heavySwing.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getRadius")
    class GetRadius {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getRadius_evaluatesFormulaWithTier() {
            int tier = 4;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "radius");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier");

            assertEquals(4, heavySwing.getRadius(tier));
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getRadius_returnsLiteral() {
            int tier = 1;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "radius");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("1");

            assertEquals(1, heavySwing.getRadius(tier));
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getRadius_prefersTierSpecific() {
            int tier = 5;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "radius");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("3");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier");

            assertEquals(3, heavySwing.getRadius(tier));
        }

        @Test
        @DisplayName("truncates fractional values to integer")
        void getRadius_truncatesFractionalValue() {
            int tier = 3;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier),
                    "radius");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers"),
                    "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*0.6");

            assertEquals(1, heavySwing.getRadius(tier));
        }
    }

    @Nested
    @DisplayName("isBlockValid")
    class IsBlockValid {

        @Test
        @DisplayName("returns true for block type in valid set")
        void isBlockValid_returnsTrue_whenBlockInValidSet() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.OAK_LOG);

            assertTrue(heavySwing.isBlockValid(block));
        }

        @Test
        @DisplayName("returns false for block type not in valid set")
        void isBlockValid_returnsFalse_whenBlockNotInValidSet() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.STONE);

            assertFalse(heavySwing.isBlockValid(block));
        }

        @Test
        @DisplayName("returns true for all configured valid blocks")
        void isBlockValid_returnsTrue_forAllConfiguredBlocks() {
            for (Material material : List.of(Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG)) {
                Block block = mock(Block.class);
                when(block.getType()).thenReturn(material);
                assertTrue(heavySwing.isBlockValid(block), material + " should be valid");
            }
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns HEAVY_SWING_KEY")
        void getAbilityKey_returnsHeavySwingKey() {
            assertEquals(HeavySwing.HEAVY_SWING_KEY, heavySwing.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns WOODCUTTING_KEY")
        void getSkillKey_returnsWoodcuttingKey() {
            assertEquals(WoodCutting.WOODCUTTING_KEY, heavySwing.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns heavy_swing")
        void getDatabaseName_returnsHeavySwing() {
            assertEquals("heavy_swing", heavySwing.getDatabaseName());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(woodcuttingConfig.getInt(WoodcuttingConfigFile.HEAVY_SWING_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, heavySwing.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.HEAVY_SWING_ENABLED, heavySwing.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, heavySwing.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(heavySwing.getYamlDocument());
        }

        @Test
        @DisplayName("getApplicableAttributes returns non-null set")
        void getApplicableAttributes_returnsNonNull() {
            assertNotNull(heavySwing.getApplicableAttributes());
        }

        @Test
        @DisplayName("getReloadableContent returns non-empty set")
        void getReloadableContent_returnsNonEmptySet() {
            assertFalse(heavySwing.getReloadableContent().isEmpty());
        }
    }
}
