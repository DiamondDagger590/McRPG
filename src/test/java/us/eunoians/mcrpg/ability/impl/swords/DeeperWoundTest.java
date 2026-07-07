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
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DeeperWound} configuration value resolution and metadata accessors.
 */
class DeeperWoundTest extends McRPGBaseTest {

    private YamlDocument swordsConfig;
    private DeeperWound deeperWound;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        deeperWound = new DeeperWound(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "deeper-wound-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "deeper-wound-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("10*tier");

            assertEquals(30.0, deeperWound.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "deeper-wound-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "deeper-wound-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("25");

            assertEquals(25.0, deeperWound.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getActivationChance_prefersTierSpecific() {
            int tier = 2;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "deeper-wound-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "deeper-wound-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("50");
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("10*tier");

            assertEquals(50.0, deeperWound.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getAdditionalBleedCycles")
    class GetAdditionalBleedCycles {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getAdditionalBleedCycles_evaluatesFormulaWithTier() {
            int tier = 4;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route cyclesAllTiers = Route.addTo(allTiersRoute, "deeper-wound-cycle-increase");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "deeper-wound-cycle-increase");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(cyclesAllTiers)).thenReturn("tier+1");

            assertEquals(5, deeperWound.getAdditionalBleedCycles(tier));
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getAdditionalBleedCycles_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route cyclesAllTiers = Route.addTo(allTiersRoute, "deeper-wound-cycle-increase");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "deeper-wound-cycle-increase");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(cyclesAllTiers)).thenReturn("3");

            assertEquals(3, deeperWound.getAdditionalBleedCycles(tier));
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getAdditionalBleedCycles_prefersTierSpecific() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route cyclesAllTiers = Route.addTo(allTiersRoute, "deeper-wound-cycle-increase");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "deeper-wound-cycle-increase");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("7");
            when(swordsConfig.getString(cyclesAllTiers)).thenReturn("tier+1");

            assertEquals(7, deeperWound.getAdditionalBleedCycles(tier));
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns DEEPER_WOUND_KEY")
        void getAbilityKey_returnsDeeperWoundKey() {
            assertEquals(DeeperWound.DEEPER_WOUND_KEY, deeperWound.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns SWORDS_KEY")
        void getSkillKey_returnsSwordsKey() {
            assertEquals(Swords.SWORDS_KEY, deeperWound.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns deeper_wound")
        void getDatabaseName_returnsDeeperWound() {
            assertEquals("deeper_wound", deeperWound.getDatabaseName());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(swordsConfig.getInt(SwordsConfigFile.DEEPER_WOUND_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, deeperWound.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.DEEPER_WOUND_ENABLED, deeperWound.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.DEEPER_WOUND_TIER_CONFIGURATION_HEADER, deeperWound.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(deeperWound.getYamlDocument());
        }

        @Test
        @DisplayName("getApplicableAttributes returns non-null set")
        void getApplicableAttributes_returnsNonNull() {
            assertNotNull(deeperWound.getApplicableAttributes());
        }
    }
}
