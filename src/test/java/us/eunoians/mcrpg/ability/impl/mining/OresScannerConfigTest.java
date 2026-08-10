package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.OreScannerBlockType;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class OresScannerConfigTest extends McRPGBaseTest {

    private YamlDocument miningConfig;
    private OreScanner oreScanner;

    @BeforeEach
    void setUp() {
        miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        Section emptySection = mock(Section.class);
        when(emptySection.getRoutesAsStrings(false)).thenReturn(Set.of());
        when(miningConfig.getSection(any(Route.class))).thenReturn(emptySection);

        oreScanner = new OreScanner(mcRPG);
    }

    @Nested
    @DisplayName("getRange")
    class GetRange {

        @Test
        @DisplayName("uses all-tiers route when tier-specific route is absent")
        void getRange_usesAllTiersRoute_whenTierRouteAbsent() {
            int tier = 2;
            Route tierRoute = Route.addTo(oreScanner.getRouteForTier(tier), "range");
            Route allTiersRoute = Route.addTo(oreScanner.getRouteForAllTiers(), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(allTiersRoute)).thenReturn("tier*5");

            assertEquals(10, oreScanner.getRange(tier));
        }

        @Test
        @DisplayName("uses tier-specific route when present")
        void getRange_usesTierRoute_whenPresent() {
            int tier = 3;
            Route tierRoute = Route.addTo(oreScanner.getRouteForTier(tier), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(true);
            when(miningConfig.getString(tierRoute)).thenReturn("20");

            assertEquals(20, oreScanner.getRange(tier));
        }

        @Test
        @DisplayName("sets tier variable in formula")
        void getRange_setsTierVariable() {
            int tier = 4;
            Route tierRoute = Route.addTo(oreScanner.getRouteForTier(tier), "range");
            Route allTiersRoute = Route.addTo(oreScanner.getRouteForAllTiers(), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(allTiersRoute)).thenReturn("5+tier*3");

            assertEquals(17, oreScanner.getRange(tier));
        }

        @Test
        @DisplayName("truncates fractional result to int")
        void getRange_truncatesFractionalResult() {
            int tier = 3;
            Route tierRoute = Route.addTo(oreScanner.getRouteForTier(tier), "range");
            Route allTiersRoute = Route.addTo(oreScanner.getRouteForAllTiers(), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(allTiersRoute)).thenReturn("tier*2.5");

            assertEquals(7, oreScanner.getRange(tier));
        }
    }

    @Nested
    @DisplayName("getHighestWeightedScanType")
    class GetHighestWeightedScanType {

        @Test
        @DisplayName("returns empty for empty set")
        void getHighestWeightedScanType_returnsEmpty_whenSetEmpty() {
            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns single type from singleton set")
        void getHighestWeightedScanType_returnsSingleType() {
            OreScannerBlockType type = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10);

            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of(type));

            assertTrue(result.isPresent());
            assertEquals(type, result.get());
        }

        @Test
        @DisplayName("returns highest weighted type from multiple types")
        void getHighestWeightedScanType_returnsHighest() {
            OreScannerBlockType low = new OreScannerBlockType(
                    Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            OreScannerBlockType mid = new OreScannerBlockType(
                    Set.of(Material.IRON_ORE), "Iron", ChatColor.WHITE, 5);
            OreScannerBlockType high = new OreScannerBlockType(
                    Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10);

            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of(low, mid, high));

            assertTrue(result.isPresent());
            assertEquals(high, result.get());
        }

        @Test
        @DisplayName("handles types with equal weight")
        void getHighestWeightedScanType_handlesEqualWeight() {
            OreScannerBlockType first = new OreScannerBlockType(
                    Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 5);
            OreScannerBlockType second = new OreScannerBlockType(
                    Set.of(Material.IRON_ORE), "Iron", ChatColor.WHITE, 5);

            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of(first, second));

            assertTrue(result.isPresent());
            assertEquals(5, result.get().weight());
        }
    }

    @Nested
    @DisplayName("getMaxTier")
    class GetMaxTier {

        @Test
        @DisplayName("returns value from config")
        void getMaxTier_returnsConfigValue() {
            when(miningConfig.getInt(MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, oreScanner.getMaxTier());
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns ORE_SCANNER_KEY")
        void getAbilityKey_returnsOreScannerKey() {
            assertEquals(OreScanner.ORE_SCANNER_KEY, oreScanner.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns MINING_KEY")
        void getSkillKey_returnsMiningKey() {
            assertEquals(Mining.MINING_KEY, oreScanner.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns ore_scanner")
        void getDatabaseName_returnsOreScanner() {
            assertEquals("ore_scanner", oreScanner.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(MiningConfigFile.ORE_SCANNER_ENABLED, oreScanner.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, oreScanner.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(oreScanner.getYamlDocument());
        }
    }
}
