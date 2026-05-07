package us.eunoians.mcrpg.ability.impl.mining;

import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link OreScanner#getRange(int)} correctly uses the {@link com.diamonddagger590.mccore.parser.Parser}
 * with the {@code tier} variable, rather than the legacy {@code getInt()} path.
 */
class OreScannerParserBackportTest extends McRPGBaseTest {

    private OreScanner oreScanner;
    private YamlDocument miningConfig;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        Section emptySection = mock(Section.class);
        when(emptySection.getRoutesAsStrings(false)).thenReturn(java.util.Set.of());
        when(miningConfig.getSection(any(Route.class))).thenReturn(emptySection);
        when(miningConfig.getInt(MiningConfigFile.ORE_SCANNER_AMOUNT_OF_TIERS)).thenReturn(5);
        when(miningConfig.getString(anyString())).thenReturn("10");
        when(miningConfig.contains(any(Route.class))).thenReturn(false);
        when(miningConfig.getStringList(MiningConfigFile.ORE_SCANNER_BLOCK_TYPES)).thenReturn(java.util.List.of());

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        oreScanner = new OreScanner(mcRPG);
        abilityRegistry.register(oreScanner);
    }

    @Test
    @DisplayName("Given a formula string in config, when getRange(tier) is called, then the formula is evaluated with the tier variable")
    void getRange_evaluatesFormulaWithTier() {
        // For each tier 1–5, the formula "5+(tier*2)" should give:
        // tier=1 → 7, tier=2 → 9, tier=3 → 11, tier=4 → 13, tier=5 → 15
        when(miningConfig.getString(any(Route.class))).thenReturn("5+(tier*2)");

        assertEquals(7, oreScanner.getRange(1));
        assertEquals(9, oreScanner.getRange(2));
        assertEquals(11, oreScanner.getRange(3));
        assertEquals(13, oreScanner.getRange(4));
        assertEquals(15, oreScanner.getRange(5));
    }

    @Test
    @DisplayName("Given a plain integer string in config, when getRange(tier) is called, then the value is returned as-is")
    void getRange_returnsLiteralValue_whenGivenPlainInteger() {
        when(miningConfig.getString(any(Route.class))).thenReturn("10");

        assertEquals(10, oreScanner.getRange(1));
        assertEquals(10, oreScanner.getRange(3));
    }

    @Test
    @DisplayName("Given a tier-specific override in config, when getRange(3) is called, then the tier-specific value is used")
    void getRange_usesTierSpecificRoute_whenPresent() {
        // Register general stub first so the more specific stub registered after takes precedence (Mockito LIFO order)
        when(miningConfig.getString(any(Route.class))).thenReturn("5");
        Route tierThreeRoute = Route.addTo(oreScanner.getRouteForTier(3), "range");
        when(miningConfig.contains(tierThreeRoute)).thenReturn(true);
        when(miningConfig.getString(tierThreeRoute)).thenReturn("15");

        assertEquals(15, oreScanner.getRange(3));
        assertEquals(5, oreScanner.getRange(1));
    }
}
