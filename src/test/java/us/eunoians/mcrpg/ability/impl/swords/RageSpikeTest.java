package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;
import us.eunoians.mcrpg.util.McRPGMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RageSpike} tier-dependent configuration value resolution,
 * simple accessors, and route/key identity methods.
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

    @Nested
    @DisplayName("getDamage")
    class GetDamage {

        @Test
        @DisplayName("Given a damage formula in all-tiers, when getDamage is called, then the formula is evaluated with the tier variable")
        void getDamage_evaluatesFormulaWithTier() {
            int tier = 4;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "all-tiers");
            Route damageAllTiers = Route.addTo(allTiersRoute, "damage");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "damage");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(damageAllTiers)).thenReturn("2*tier");

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
        @DisplayName("Given a tier-specific override, when getDamage is called, then the tier-specific value is used")
        void getDamage_usesTierSpecificOverride_whenPresent() {
            int tier = 3;
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "damage");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("12");

            assertEquals(12.0, rageSpike.getDamage(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getVelocity")
    class GetVelocity {

        @Test
        @DisplayName("Given a velocity formula in all-tiers, when getVelocity is called, then the formula is evaluated")
        void getVelocity_evaluatesFormulaWithTier() {
            int tier = 4;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "all-tiers");
            Route velocityAllTiers = Route.addTo(allTiersRoute, "velocity");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "velocity");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(velocityAllTiers, "5")).thenReturn("tier+2");

            // tier=4 → 4+2 = 6, distinct from the default of 5
            assertEquals(6, rageSpike.getVelocity(tier));
        }

        @Test
        @DisplayName("Given a tier-specific override, when getVelocity is called, then the override is used")
        void getVelocity_usesTierSpecificOverride_whenPresent() {
            int tier = 2;
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "velocity");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("10");

            assertEquals(10, rageSpike.getVelocity(tier));
        }

        @Test
        @DisplayName("Given no config value, when getVelocity is called, then the default of 5 is used")
        void getVelocity_usesDefault_whenNoConfigValue() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "all-tiers");
            Route velocityAllTiers = Route.addTo(allTiersRoute, "velocity");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, "tier-" + tier), "velocity");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(velocityAllTiers, "5")).thenReturn("5");

            assertEquals(5, rageSpike.getVelocity(tier));
        }
    }

    @Nested
    @DisplayName("getMaxVerticalVelocity")
    class GetMaxVerticalVelocity {

        @Test
        @DisplayName("Given a configured max vertical velocity, when getMaxVerticalVelocity is called, then it returns the configured value")
        void getMaxVerticalVelocity_returnsConfiguredValue() {
            when(swordsConfig.getDouble(SwordsConfigFile.RAGE_SPIKE_MAX_VERTICAL_VELOCITY, 0.3)).thenReturn(0.5);

            assertEquals(0.5, rageSpike.getMaxVerticalVelocity(), 0.001);
        }

        @Test
        @DisplayName("Given no config value, when getMaxVerticalVelocity is called, then the default 0.3 is returned")
        void getMaxVerticalVelocity_returnsDefault_whenNotConfigured() {
            when(swordsConfig.getDouble(SwordsConfigFile.RAGE_SPIKE_MAX_VERTICAL_VELOCITY, 0.3)).thenReturn(0.3);

            assertEquals(0.3, rageSpike.getMaxVerticalVelocity(), 0.001);
        }
    }

    @Nested
    @DisplayName("getMaxTier")
    class GetMaxTier {

        @Test
        @DisplayName("Given a configured amount of tiers, when getMaxTier is called, then the configured value is returned")
        void getMaxTier_returnsConfiguredValue() {
            when(swordsConfig.getInt(SwordsConfigFile.RAGE_SPIKE_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, rageSpike.getMaxTier());
        }
    }

    @Nested
    @DisplayName("Identity and routes")
    class IdentityAndRoutes {

        @Test
        @DisplayName("getAbilityKey returns rage_spike key under mcrpg namespace")
        void getAbilityKey_returnsRageSpikeKey() {
            assertEquals(RageSpike.RAGE_SPIKE_KEY, rageSpike.getAbilityKey());
            assertEquals("mcrpg", rageSpike.getAbilityKey().getNamespace());
            assertEquals("rage_spike", rageSpike.getAbilityKey().getKey());
        }

        @Test
        @DisplayName("getDatabaseName returns rage_spike")
        void getDatabaseName_returnsRageSpike() {
            assertEquals("rage_spike", rageSpike.getDatabaseName());
        }

        @Test
        @DisplayName("getSkillKey returns Swords key")
        void getSkillKey_returnsSwordsKey() {
            assertEquals(Swords.SWORDS_KEY, rageSpike.getSkillKey());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns the rage spike enabled route")
        void getAbilityEnabledRoute_returnsRageSpikeEnabledRoute() {
            assertEquals(SwordsConfigFile.RAGE_SPIKE_ENABLED, rageSpike.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getDisplayItemRoute returns the rage spike display item header route")
        void getDisplayItemRoute_returnsRageSpikeDisplayItemRoute() {
            assertEquals(LocalizationKey.RAGE_SPIKE_DISPLAY_ITEM_HEADER, rageSpike.getDisplayItemRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns the rage spike tier config header")
        void getAbilityTierConfigurationRoute_returnsRageSpikeTierConfigHeader() {
            assertEquals(SwordsConfigFile.RAGE_SPIKE_CONFIGURATION_HEADER, rageSpike.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns the swords config from FileManager")
        void getYamlDocument_returnsSwordsConfig() {
            assertNotNull(rageSpike.getYamlDocument());
            assertEquals(swordsConfig, rageSpike.getYamlDocument());
        }
    }
}
