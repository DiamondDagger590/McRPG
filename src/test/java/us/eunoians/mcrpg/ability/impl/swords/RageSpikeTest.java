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
 * Tests for {@link RageSpike} tier-dependent configuration value resolution.
 * <p>
 * Verifies that {@code getDamage} and {@code getVelocity} correctly evaluate formulas
 * through the {@link com.diamonddagger590.mccore.parser.Parser} and handle plain
 * integer literals.
 */
class RageSpikeTest extends McRPGBaseTest {

    private YamlDocument swordsConfig;
    private RageSpike rageSpike;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        rageSpike = new RageSpike(mcRPG);
    }

    @Test
    @DisplayName("Given a damage formula, when getDamage is called with a tier, then the formula is evaluated")
    void getDamage_evaluatesFormulaWithTier() {
        int tier = 4;
        Route allTiersRoute = Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "all-tiers");
        Route damageAllTiers = Route.addTo(allTiersRoute, "damage");
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "damage");

        when(swordsConfig.contains(tierRoute)).thenReturn(false);
        when(swordsConfig.getString(damageAllTiers)).thenReturn("2*tier");

        // tier=4 → 2*4 = 8.0
        assertEquals(8.0, rageSpike.getDamage(tier), 0.001);
    }

    @Test
    @DisplayName("Given a plain integer literal in YAML, when getDamage is called, then it returns the literal value")
    void getDamage_returnsLiteralValue_whenGivenPlainInteger() {
        int tier = 1;
        Route allTiersRoute = Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "all-tiers");
        Route damageAllTiers = Route.addTo(allTiersRoute, "damage");
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "damage");

        when(swordsConfig.contains(tierRoute)).thenReturn(false);
        when(swordsConfig.getString(damageAllTiers)).thenReturn("5");

        assertEquals(5.0, rageSpike.getDamage(tier), 0.001);
    }

    @Test
    @DisplayName("Given a velocity formula, when getVelocity is called with a tier, then the formula is evaluated")
    void getVelocity_evaluatesFormulaWithTier() {
        int tier = 3;
        Route allTiersRoute = Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "all-tiers");
        Route velocityAllTiers = Route.addTo(allTiersRoute, "velocity");
        Route tierRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "velocity");

        when(swordsConfig.contains(tierRoute)).thenReturn(false);
        when(swordsConfig.getString(velocityAllTiers, "5")).thenReturn("tier+2");

        // tier=3 → 3+2 = 5
        assertEquals(5, rageSpike.getVelocity(tier));
    }
}
