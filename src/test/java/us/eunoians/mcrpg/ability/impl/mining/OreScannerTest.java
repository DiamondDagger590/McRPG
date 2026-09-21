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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class OreScannerTest extends McRPGBaseTest {

    private YamlDocument miningConfig;
    private OreScanner oreScanner;

    @BeforeEach
    void setUp() {
        miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        Section blockTypesSection = mock(Section.class);
        when(blockTypesSection.getRoutesAsStrings(false)).thenReturn(Collections.emptySet());
        when(miningConfig.getSection(MiningConfigFile.ORE_SCANNER_BLOCK_TYPES)).thenReturn(blockTypesSection);

        oreScanner = new OreScanner(mcRPG);
    }

    @Nested
    @DisplayName("getHighestWeightedScanType")
    class GetHighestWeightedScanType {

        @Test
        @DisplayName("Given multiple scan types, when called, then returns the highest weighted type")
        void getHighestWeightedScanType_returnsHighestWeight() {
            OreScannerBlockType coal = new OreScannerBlockType(Set.of(Material.COAL_ORE), "Coal", ChatColor.DARK_GRAY, 1);
            OreScannerBlockType iron = new OreScannerBlockType(Set.of(Material.IRON_ORE), "Iron", ChatColor.WHITE, 5);
            OreScannerBlockType diamond = new OreScannerBlockType(Set.of(Material.DIAMOND_ORE), "Diamond", ChatColor.AQUA, 10);

            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of(coal, iron, diamond));

            assertTrue(result.isPresent());
            assertSame(diamond, result.get());
        }

        @Test
        @DisplayName("Given a single scan type, when called, then returns that type")
        void getHighestWeightedScanType_returnsSingleType() {
            OreScannerBlockType gold = new OreScannerBlockType(Set.of(Material.GOLD_ORE), "Gold", ChatColor.GOLD, 8);

            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of(gold));

            assertTrue(result.isPresent());
            assertSame(gold, result.get());
        }

        @Test
        @DisplayName("Given an empty set, when called, then returns empty optional")
        void getHighestWeightedScanType_returnsEmpty_whenSetIsEmpty() {
            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given types with equal weight, when called, then returns one of them")
        void getHighestWeightedScanType_returnsOneOfEquals_whenTied() {
            OreScannerBlockType ironA = new OreScannerBlockType(Set.of(Material.IRON_ORE), "Iron A", ChatColor.WHITE, 5);
            OreScannerBlockType ironB = new OreScannerBlockType(Set.of(Material.DEEPSLATE_IRON_ORE), "Iron B", ChatColor.GRAY, 5);

            Optional<OreScannerBlockType> result = oreScanner.getHighestWeightedScanType(Set.of(ironA, ironB));

            assertTrue(result.isPresent());
            assertEquals(5, result.get().weight());
        }
    }

    @Nested
    @DisplayName("getRange")
    class GetRange {

        @Test
        @DisplayName("Given a range formula in all-tiers, when getRange is called, then evaluates the formula with tier")
        void getRange_evaluatesFormulaWithTier() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "all-tiers");
            Route rangeAllTiers = Route.addTo(allTiersRoute, "range");
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "tier-" + tier), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(rangeAllTiers)).thenReturn("2+tier");

            assertEquals(5, oreScanner.getRange(tier));
        }

        @Test
        @DisplayName("Given a tier-specific range override, when getRange is called, then uses the tier-specific value")
        void getRange_usesTierSpecificValue_whenPresent() {
            int tier = 2;
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "tier-" + tier), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(true);
            when(miningConfig.getString(tierRoute)).thenReturn("10");

            assertEquals(10, oreScanner.getRange(tier));
        }

        @Test
        @DisplayName("Given a plain integer literal, when getRange is called, then returns that literal")
        void getRange_returnsLiteralValue_whenGivenPlainInteger() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "all-tiers");
            Route rangeAllTiers = Route.addTo(allTiersRoute, "range");
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "tier-" + tier), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(rangeAllTiers)).thenReturn("7");

            assertEquals(7, oreScanner.getRange(tier));
        }

        @Test
        @DisplayName("Given a formula with multiplication, when getRange is called at tier 5, then evaluates correctly")
        void getRange_evaluatesMultiplication_atHighTier() {
            int tier = 5;
            Route allTiersRoute = Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "all-tiers");
            Route rangeAllTiers = Route.addTo(allTiersRoute, "range");
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ORE_SCANNER_CONFIGURATION_HEADER, "tier-" + tier), "range");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(rangeAllTiers)).thenReturn("3*tier");

            assertEquals(15, oreScanner.getRange(tier));
        }
    }
}
