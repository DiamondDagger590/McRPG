package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SerratedStrikes} tier-dependent configuration value resolution.
 * <p>
 * Verifies that {@code getDuration} and {@code getBoostToBleedActivation} correctly
 * evaluate formulas through the {@link com.diamonddagger590.mccore.parser.Parser},
 * handle plain integer literals, and prefer tier-specific routes over all-tiers routes.
 */
class SerratedStrikesTest extends McRPGBaseTest {

    private YamlDocument swordsConfig;
    private SerratedStrikes serratedStrikes;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        serratedStrikes = new SerratedStrikes(mcRPG);
    }

    @Test
    @DisplayName("Given a duration formula, when getDuration is called with a tier, then the formula is evaluated")
    void getDuration_evaluatesFormulaWithTier() {
        int tier = 3;
        Route allTiersRoute = Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers");
        Route durationAllTiers = Route.addTo(allTiersRoute, "duration");
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-" + tier), "duration");

        when(swordsConfig.contains(tierRoute)).thenReturn(false);
        when(swordsConfig.getString(durationAllTiers)).thenReturn("tier+2");

        // tier=3 → 3+2 = 5
        assertEquals(5, serratedStrikes.getDuration(tier));
    }

    @Test
    @DisplayName("Given a plain integer literal in YAML, when getDuration is called, then it returns the literal value")
    void getDuration_returnsLiteralValue_whenGivenPlainInteger() {
        int tier = 1;
        Route allTiersRoute = Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers");
        Route durationAllTiers = Route.addTo(allTiersRoute, "duration");
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-" + tier), "duration");

        when(swordsConfig.contains(tierRoute)).thenReturn(false);
        when(swordsConfig.getString(durationAllTiers)).thenReturn("240");

        assertEquals(240, serratedStrikes.getDuration(tier));
    }

    @Test
    @DisplayName("Given both a tier-specific and an all-tiers route, when getDuration is called, then the tier-specific route takes precedence")
    void getDuration_usesTierSpecificRoute_whenPresent() {
        int tier = 2;
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-" + tier), "duration");
        Route allTiersRoute = Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers");
        Route durationAllTiers = Route.addTo(allTiersRoute, "duration");

        when(swordsConfig.contains(tierRoute)).thenReturn(true);
        when(swordsConfig.getString(tierRoute)).thenReturn("10");
        when(swordsConfig.getString(durationAllTiers)).thenReturn("tier+2");

        assertEquals(10, serratedStrikes.getDuration(tier));
    }

    @Test
    @DisplayName("Given a boost formula, when getBoostToBleedActivation is called with a tier, then the formula is evaluated")
    void getBoostToBleedActivation_evaluatesFormulaWithTier() {
        int tier = 2;
        Route allTiersRoute = Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers");
        Route boostAllTiers = Route.addTo(allTiersRoute, "bleed-activation-boost");
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-" + tier),
                "bleed-activation-boost");

        when(swordsConfig.contains(tierRoute)).thenReturn(false);
        when(swordsConfig.getString(boostAllTiers)).thenReturn("5*tier");

        // tier=2 → 5*2 = 10.0
        assertEquals(10.0, serratedStrikes.getBoostToBleedActivation(tier), 0.001);
    }
}
