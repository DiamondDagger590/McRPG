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
        @DisplayName("uses all-tiers route when tier-specific route is absent")
        void getActivationChance_usesAllTiersRoute_whenTierRouteAbsent() {
            int tier = 2;
            Route tierRoute = Route.addTo(itsATriple.getRouteForTier(tier), "activation-chance");
            Route allTiersRoute = Route.addTo(itsATriple.getRouteForAllTiers(), "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(allTiersRoute)).thenReturn("tier*10");

            assertEquals(20.0, itsATriple.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("uses tier-specific route when present")
        void getActivationChance_usesTierRoute_whenPresent() {
            int tier = 3;
            Route tierRoute = Route.addTo(itsATriple.getRouteForTier(tier), "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(true);
            when(miningConfig.getString(tierRoute)).thenReturn("50");

            assertEquals(50.0, itsATriple.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("resolves tier-1 route correctly")
        void getActivationChance_resolvesTierOne() {
            int tier = 1;
            Route tierRoute = Route.addTo(itsATriple.getRouteForTier(tier), "activation-chance");
            Route allTiersRoute = Route.addTo(itsATriple.getRouteForAllTiers(), "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(allTiersRoute)).thenReturn("tier*10");

            assertEquals(10.0, itsATriple.getActivationChance(tier), 0.001);
        }

        @Test
        @DisplayName("sets tier variable in formula")
        void getActivationChance_setsTierVariable() {
            int tier = 5;
            Route tierRoute = Route.addTo(itsATriple.getRouteForTier(tier), "activation-chance");
            Route allTiersRoute = Route.addTo(itsATriple.getRouteForAllTiers(), "activation-chance");

            when(miningConfig.contains(tierRoute)).thenReturn(false);
            when(miningConfig.getString(allTiersRoute)).thenReturn("tier*5+10");

            assertEquals(35.0, itsATriple.getActivationChance(tier), 0.001);
        }
    }

    @Nested
    @DisplayName("getMaxTier")
    class GetMaxTier {

        @Test
        @DisplayName("returns value from config")
        void getMaxTier_returnsConfigValue() {
            when(miningConfig.getInt(MiningConfigFile.ITS_A_TRIPLE_AMOUNT_OF_TIERS)).thenReturn(5);

            assertEquals(5, itsATriple.getMaxTier());
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
    }
}
