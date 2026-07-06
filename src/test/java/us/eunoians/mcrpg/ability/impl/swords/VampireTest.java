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
 * Tests for {@link Vampire} configuration value resolution and metadata accessors.
 */
class VampireTest extends McRPGBaseTest {

    private YamlDocument swordsConfig;
    private Vampire vampire;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);

        vampire = new Vampire(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "vampire-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "vampire-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("10*tier");

            assertEquals(30.0, vampire.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "vampire-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "vampire-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("25");

            assertEquals(25.0, vampire.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getActivationChance_prefersTierSpecific() {
            int tier = 2;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "vampire-activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "vampire-activation-chance");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("50");
            when(swordsConfig.getString(chanceAllTiers)).thenReturn("10*tier");

            assertEquals(50.0, vampire.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getAmountToHeal")
    class GetAmountToHeal {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getAmountToHeal_evaluatesFormulaWithTier() {
            int tier = 4;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route healAllTiers = Route.addTo(allTiersRoute, "amount-to-heal");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "amount-to-heal");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(healAllTiers)).thenReturn("tier+1");

            assertEquals(5, vampire.getAmountToHeal(tier));
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getAmountToHeal_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route healAllTiers = Route.addTo(allTiersRoute, "amount-to-heal");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "amount-to-heal");

            when(swordsConfig.contains(tierRoute)).thenReturn(false);
            when(swordsConfig.getString(healAllTiers)).thenReturn("3");

            assertEquals(3, vampire.getAmountToHeal(tier));
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getAmountToHeal_prefersTierSpecific() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "all-tiers");
            Route healAllTiers = Route.addTo(allTiersRoute, "amount-to-heal");
            Route tierRoute = Route.addTo(
                    Route.addTo(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, "tier-" + tier),
                    "amount-to-heal");

            when(swordsConfig.contains(tierRoute)).thenReturn(true);
            when(swordsConfig.getString(tierRoute)).thenReturn("7");
            when(swordsConfig.getString(healAllTiers)).thenReturn("tier+1");

            assertEquals(7, vampire.getAmountToHeal(tier));
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns VAMPIRE_KEY")
        void getAbilityKey_returnsVampireKey() {
            assertEquals(Vampire.VAMPIRE_KEY, vampire.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns SWORDS_KEY")
        void getSkillKey_returnsSwordsKey() {
            assertEquals(Swords.SWORDS_KEY, vampire.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns vampire")
        void getDatabaseName_returnsVampire() {
            assertEquals("vampire", vampire.getDatabaseName());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(swordsConfig.getInt(SwordsConfigFile.VAMPIRE_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, vampire.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.VAMPIRE_ENABLED, vampire.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(SwordsConfigFile.VAMPIRE_TIER_CONFIGURATION_HEADER, vampire.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(vampire.getYamlDocument());
        }

        @Test
        @DisplayName("getApplicableAttributes returns non-null set")
        void getApplicableAttributes_returnsNonNull() {
            assertNotNull(vampire.getApplicableAttributes());
        }
    }
}
