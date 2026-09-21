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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HeavySwing} tier-dependent configuration value resolution
 * and block validity checks.
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
        @DisplayName("Given an activation-chance formula, when called with a tier, then evaluates the formula")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier), "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(chanceAllTiers)).thenReturn("10+5*tier");

            assertEquals(25.0, heavySwing.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("Given a tier-specific override, when called, then uses the override value")
        void getActivationChance_usesTierSpecificValue_whenPresent() {
            int tier = 2;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier), "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("50");

            assertEquals(50.0, heavySwing.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("Given a plain literal, when called at tier 1, then returns the literal")
        void getActivationChance_returnsLiteral_whenGivenPlainValue() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier), "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(chanceAllTiers)).thenReturn("15");

            assertEquals(15.0, heavySwing.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getRadius")
    class GetRadius {

        @Test
        @DisplayName("Given a radius formula, when called with a tier, then evaluates the formula")
        void getRadius_evaluatesFormulaWithTier() {
            int tier = 4;
            Route allTiersRoute = Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers");
            Route radiusAllTiers = Route.addTo(allTiersRoute, "radius");
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier), "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(radiusAllTiers)).thenReturn("1+tier");

            assertEquals(5, heavySwing.getRadius(tier));
        }

        @Test
        @DisplayName("Given a tier-specific radius override, when called, then uses the override")
        void getRadius_usesTierSpecificValue_whenPresent() {
            int tier = 5;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier), "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("8");

            assertEquals(8, heavySwing.getRadius(tier));
        }

        @Test
        @DisplayName("Given a plain integer literal, when called, then returns the literal as int")
        void getRadius_returnsLiteralValue() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "all-tiers");
            Route radiusAllTiers = Route.addTo(allTiersRoute, "radius");
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.HEAVY_SWING_CONFIGURATION_HEADER, "tier-" + tier), "radius");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(radiusAllTiers)).thenReturn("3");

            assertEquals(3, heavySwing.getRadius(tier));
        }
    }

    @Nested
    @DisplayName("isBlockValid")
    class IsBlockValid {

        @Test
        @DisplayName("Given a block with a valid material, when checked, then returns true")
        void isBlockValid_returnsTrue_whenMaterialIsInValidSet() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.OAK_LOG);

            assertTrue(heavySwing.isBlockValid(block));
        }

        @Test
        @DisplayName("Given a block with an invalid material, when checked, then returns false")
        void isBlockValid_returnsFalse_whenMaterialIsNotInValidSet() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.STONE);

            assertFalse(heavySwing.isBlockValid(block));
        }

        @Test
        @DisplayName("Given a second valid material, when checked, then returns true")
        void isBlockValid_returnsTrue_forSecondValidMaterial() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.BIRCH_LOG);

            assertTrue(heavySwing.isBlockValid(block));
        }

        @Test
        @DisplayName("Given a similar but non-matching material, when checked, then returns false")
        void isBlockValid_returnsFalse_forSimilarNonMatchingMaterial() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.OAK_PLANKS);

            assertFalse(heavySwing.isBlockValid(block));
        }
    }
}
