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
 * Tests for {@link EnhancedBleed} configuration value resolution and metadata accessors.
 */
class EnhancedBleedTest extends McRPGBaseTest {

    private YamlDocument swordsConfig;
    private EnhancedBleed enhancedBleed;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        enhancedBleed = new EnhancedBleed(mcRPG);
    }

    @Nested
    @DisplayName("getBaseBleedDamageIncrease")
    class GetBaseBleedDamageIncrease {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getBaseBleedDamageIncrease_evaluatesFormulaWithTier() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route damageAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-base-damage-increase");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-base-damage-increase");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(damageAllTiers)).thenReturn("2*tier");

            assertEquals(6, enhancedBleed.getBaseBleedDamageIncrease(tier));
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getBaseBleedDamageIncrease_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route damageAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-base-damage-increase");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-base-damage-increase");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(damageAllTiers)).thenReturn("4");

            assertEquals(4, enhancedBleed.getBaseBleedDamageIncrease(tier));
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getBaseBleedDamageIncrease_prefersTierSpecific() {
            int tier = 2;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route damageAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-base-damage-increase");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-base-damage-increase");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("10");
            when(swordsConfig.getString(damageAllTiers)).thenReturn("2*tier");

            assertEquals(10, enhancedBleed.getBaseBleedDamageIncrease(tier));
        }
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 4;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("5*tier");

            assertEquals(20.0, enhancedBleed.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("15");

            assertEquals(15.0, enhancedBleed.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getActivationChance_prefersTierSpecific() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("40");
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("5*tier");

            assertEquals(40.0, enhancedBleed.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getAdditionalBleedDamageBoost")
    class GetAdditionalBleedDamageBoost {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getAdditionalBleedDamageBoost_evaluatesFormulaWithTier() {
            int tier = 5;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route boostAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-damage-boost");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-damage-boost");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(boostAllTiers)).thenReturn("tier+2");

            assertEquals(7, enhancedBleed.getAdditionalBleedDamageBoost(tier));
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getAdditionalBleedDamageBoost_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route boostAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-damage-boost");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-damage-boost");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(boostAllTiers)).thenReturn("5");

            assertEquals(5, enhancedBleed.getAdditionalBleedDamageBoost(tier));
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getAdditionalBleedDamageBoost_prefersTierSpecific() {
            int tier = 2;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route boostAllTiers = Route.addTo(allTiersRoute, "enhanced-bleed-damage-boost");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "enhanced-bleed-damage-boost");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("12");
            when(swordsConfig.getString(boostAllTiers)).thenReturn("tier+2");

            assertEquals(12, enhancedBleed.getAdditionalBleedDamageBoost(tier));
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns ENHANCED_BLEED_KEY")
        void getAbilityKey_returnsEnhancedBleedKey() {
            assertEquals(EnhancedBleed.ENHANCED_BLEED_KEY, enhancedBleed.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns SWORDS_KEY")
        void getSkillKey_returnsSwordsKey() {
            assertEquals(Swords.SWORDS_KEY, enhancedBleed.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns enhanced_bleed")
        void getDatabaseName_returnsEnhancedBleed() {
            assertEquals("enhanced_bleed", enhancedBleed.getDatabaseName());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(swordsConfig.getInt(SwordsConfigFile.ENHANCED_BLEED_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, enhancedBleed.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.ENHANCED_BLEED_ENABLED, enhancedBleed.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.ENHANCED_BLEED_TIER_CONFIGURATION_HEADER, enhancedBleed.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(enhancedBleed.getYamlDocument());
        }

        @Test
        @DisplayName("getApplicableAttributes returns non-null set")
        void getApplicableAttributes_returnsNonNull() {
            assertNotNull(enhancedBleed.getApplicableAttributes());
        }
    }
}
