package us.eunoians.mcrpg.ability.impl.mining;

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
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.mining.Mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ItsATriple} configuration value resolution and metadata accessors.
 * <p>
 * ItsATriple is a tierable ability that uses the {@code tier} Parser variable
 * with tier-specific route fallback to all-tiers.
 */
class ItsATripleTest extends McRPGBaseTest {

    private YamlDocument miningConfig;
    private ItsATriple itsATriple;

    @BeforeEach
    void setUp() {
        miningConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.MINING_CONFIG)).thenReturn(miningConfig);

        itsATriple = new ItsATriple(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates formula with tier variable")
        void getActivationChance_evaluatesFormulaWithTier() {
            int tier = 3;
            Route allTiersRoute = Route.addTo(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(chanceAllTiers)).thenReturn("10*tier");

            assertEquals(30.0, itsATriple.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number")
        void getActivationChance_returnsLiteral() {
            int tier = 1;
            Route allTiersRoute = Route.addTo(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(chanceAllTiers)).thenReturn("25");

            assertEquals(25.0, itsATriple.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("prefers tier-specific route over all-tiers")
        void getActivationChance_prefersTierSpecific() {
            int tier = 2;
            Route allTiersRoute = Route.addTo(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, "all-tiers");
            Route chanceAllTiers = Route.addTo(allTiersRoute, "activation-chance");
            Route tierRoute = Route.addTo(
                    Route.addTo(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, "tier-" + tier),
                    "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(true);
            when(miningConfig.getString(tierRoute)).thenReturn("50");
            when(miningConfig.getString(chanceAllTiers)).thenReturn("10*tier");

            assertEquals(50.0, itsATriple.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns ITS_A_TRIPLE_KEY")
        void getAbilityKey_returnsItsATripleKey() {
            assertEquals(ItsATriple.ITS_A_TRIPLE_KEY, itsATriple.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns MINING_KEY")
        void getSkillKey_returnsMiningKey() {
            assertEquals(Mining.MINING_KEY, itsATriple.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns its_a_triple")
        void getDatabaseName_returnsItsATriple() {
            assertEquals("its_a_triple", itsATriple.getDatabaseName());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(miningConfig.getInt(MiningConfigFile.ITS_A_TRIPLE_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, itsATriple.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(MiningConfigFile.ITS_A_TRIPLE_ENABLED, itsATriple.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(MiningConfigFile.ITS_A_TRIPLE_CONFIGURATION_HEADER, itsATriple.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(itsATriple.getYamlDocument());
        }

        @Test
        @DisplayName("getApplicableAttributes returns non-null set")
        void getApplicableAttributes_returnsNonNull() {
            assertNotNull(itsATriple.getApplicableAttributes());
        }
    }
}
