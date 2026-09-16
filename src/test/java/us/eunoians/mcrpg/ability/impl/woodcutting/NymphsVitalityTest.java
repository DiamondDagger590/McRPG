package us.eunoians.mcrpg.ability.impl.woodcutting;

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
import us.eunoians.mcrpg.configuration.file.skill.WoodcuttingConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link NymphsVitality} configuration value resolution and metadata accessors.
 * <p>
 * NymphsVitality is a tier-based ability that uses the {@code minimum-hunger}
 * config key, supporting per-tier overrides and a fallback {@code all-tiers}
 * formula with a {@code tier} Parser variable.
 */
class NymphsVitalityTest extends McRPGBaseTest {

    private static final String HUNGER_FORMULA = "tier*4";

    private YamlDocument woodcuttingConfig;
    private NymphsVitality nymphsVitality;

    @BeforeEach
    void setUp() {
        woodcuttingConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodcuttingConfig);

        when(woodcuttingConfig.getInt(WoodcuttingConfigFile.NYMPHS_VITALITY_AMOUNT_OF_TIERS)).thenReturn(5);
        when(woodcuttingConfig.contains(any(Route.class))).thenReturn(false);
        when(woodcuttingConfig.getString(any(Route.class))).thenReturn(HUNGER_FORMULA);
        when(woodcuttingConfig.getStringList(any(Route.class))).thenReturn(List.of());

        nymphsVitality = new NymphsVitality(mcRPG);
    }

    @Nested
    @DisplayName("getMinimumHunger")
    class GetMinimumHunger {

        @Test
        @DisplayName("evaluates all-tiers formula with tier variable")
        void getMinimumHunger_evaluatesAllTiersFormula() {
            Route allTiersRoute = Route.addTo(nymphsVitality.getRouteForAllTiers(), "minimum-hunger");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(HUNGER_FORMULA);

            assertEquals(8, nymphsVitality.getMinimumHunger(2));
        }

        @Test
        @DisplayName("uses tier-specific override when present")
        void getMinimumHunger_usesTierSpecificOverride() {
            Route tierRoute = Route.addTo(nymphsVitality.getRouteForTier(3), "minimum-hunger");
            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("15");

            assertEquals(15, nymphsVitality.getMinimumHunger(3));
        }

        @Test
        @DisplayName("returns literal value when given plain number in all-tiers")
        void getMinimumHunger_returnsLiteral() {
            Route allTiersRoute = Route.addTo(nymphsVitality.getRouteForAllTiers(), "minimum-hunger");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("10");

            assertEquals(10, nymphsVitality.getMinimumHunger(1));
        }

        @Test
        @DisplayName("truncates decimal to int")
        void getMinimumHunger_truncatesDecimal() {
            Route allTiersRoute = Route.addTo(nymphsVitality.getRouteForAllTiers(), "minimum-hunger");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*2.5");

            assertEquals(7, nymphsVitality.getMinimumHunger(3));
        }

        @Test
        @DisplayName("evaluates formula at tier 5")
        void getMinimumHunger_evaluatesAtHighTier() {
            Route allTiersRoute = Route.addTo(nymphsVitality.getRouteForAllTiers(), "minimum-hunger");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(HUNGER_FORMULA);

            assertEquals(20, nymphsVitality.getMinimumHunger(5));
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns NYMPHS_VITALITY_KEY")
        void getAbilityKey_returnsNymphsVitalityKey() {
            assertEquals(NymphsVitality.NYMPHS_VITALITY_KEY, nymphsVitality.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns WOODCUTTING_KEY")
        void getSkillKey_returnsWoodcuttingKey() {
            assertEquals(WoodCutting.WOODCUTTING_KEY, nymphsVitality.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns nymphs_vitality")
        void getDatabaseName_returnsNymphsVitality() {
            assertEquals("nymphs_vitality", nymphsVitality.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.NYMPHS_VITALITY_ENABLED, nymphsVitality.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(woodcuttingConfig.getInt(WoodcuttingConfigFile.NYMPHS_VITALITY_AMOUNT_OF_TIERS)).thenReturn(5);
            assertEquals(5, nymphsVitality.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.NYMPHS_VITALITY_CONFIGURATION_HEADER, nymphsVitality.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(nymphsVitality.getYamlDocument());
        }
    }
}
