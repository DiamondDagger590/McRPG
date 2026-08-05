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
 * Tests for {@link DryadsGift} configuration value resolution and metadata accessors.
 */
class DryadsGiftTest extends McRPGBaseTest {

    private YamlDocument woodcuttingConfig;
    private DryadsGift dryadsGift;

    @BeforeEach
    void setUp() {
        woodcuttingConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodcuttingConfig);

        when(woodcuttingConfig.getStringList(WoodcuttingConfigFile.DRYADS_GIFT_VALID_BLOCKS))
                .thenReturn(List.of("OAK_LOG", "BIRCH_LOG", "DARK_OAK_LOG"));

        dryadsGift = new DryadsGift(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 3;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*2");

            assertEquals(6.0, dryadsGift.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            int tier = 1;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("3");

            assertEquals(3.0, dryadsGift.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getActivationChance_prefersTierSpecific() {
            int tier = 4;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "activation-chance");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("7.5");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*2");

            assertEquals(7.5, dryadsGift.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getExperienceToDrop")
    class GetExperienceToDrop {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getExperienceToDrop_evaluatesFormulaWithTier() {
            int tier = 3;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "experience-to-drop");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "experience-to-drop");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*5");

            assertEquals(15, dryadsGift.getExperienceToDrop(tier));
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getExperienceToDrop_returnsLiteral() {
            int tier = 1;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "experience-to-drop");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "experience-to-drop");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("5");

            assertEquals(5, dryadsGift.getExperienceToDrop(tier));
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getExperienceToDrop_prefersTierSpecific() {
            int tier = 5;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "experience-to-drop");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "experience-to-drop");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("22");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*5");

            assertEquals(22, dryadsGift.getExperienceToDrop(tier));
        }

        @Test
        @DisplayName("truncates fractional values to integer")
        void getExperienceToDrop_truncatesFractionalValue() {
            int tier = 3;
            Route tierRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "tier-" + tier),
                    "experience-to-drop");
            Route allTiersRoute = Route.addTo(
                    Route.addTo(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, "all-tiers"),
                    "experience-to-drop");

            when(woodcuttingConfig.contains(tierRoute)).thenReturn(false);
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*3.7");

            assertEquals(11, dryadsGift.getExperienceToDrop(tier));
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

            assertTrue(dryadsGift.isBlockValid(block));
        }

        @Test
        @DisplayName("returns false for block type not in valid set")
        void isBlockValid_returnsFalse_whenBlockNotInValidSet() {
            Block block = mock(Block.class);
            when(block.getType()).thenReturn(Material.DIAMOND_ORE);

            assertFalse(dryadsGift.isBlockValid(block));
        }

        @Test
        @DisplayName("returns true for all configured valid blocks")
        void isBlockValid_returnsTrue_forAllConfiguredBlocks() {
            for (Material material : List.of(Material.OAK_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG)) {
                Block block = mock(Block.class);
                when(block.getType()).thenReturn(material);
                assertTrue(dryadsGift.isBlockValid(block), material + " should be valid");
            }
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns DRYADS_GIFT_KEY")
        void getAbilityKey_returnsDryadsGiftKey() {
            assertEquals(DryadsGift.DRYADS_GIFT_KEY, dryadsGift.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns WOODCUTTING_KEY")
        void getSkillKey_returnsWoodcuttingKey() {
            assertEquals(WoodCutting.WOODCUTTING_KEY, dryadsGift.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns dryads_gift")
        void getDatabaseName_returnsDryadsGift() {
            assertEquals("dryads_gift", dryadsGift.getDatabaseName());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(woodcuttingConfig.getInt(WoodcuttingConfigFile.DRYADS_GIFT_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, dryadsGift.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.DRYADS_GIFT_ENABLED, dryadsGift.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, dryadsGift.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(dryadsGift.getYamlDocument());
        }

        @Test
        @DisplayName("getApplicableAttributes returns non-null set")
        void getApplicableAttributes_returnsNonNull() {
            assertNotNull(dryadsGift.getApplicableAttributes());
        }

        @Test
        @DisplayName("getReloadableContent returns non-empty set")
        void getReloadableContent_returnsNonEmptySet() {
            assertFalse(dryadsGift.getReloadableContent().isEmpty());
        }
    }
}
