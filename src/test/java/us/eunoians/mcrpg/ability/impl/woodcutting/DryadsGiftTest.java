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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DryadsGift} configuration value resolution and metadata accessors.
 * <p>
 * DryadsGift is a tier-based ability that uses {@code activation-chance} and
 * {@code experience-to-drop} config keys, each supporting per-tier overrides
 * and a fallback {@code all-tiers} formula with a {@code tier} Parser variable.
 */
class DryadsGiftTest extends McRPGBaseTest {

    private static final String ACTIVATION_FORMULA = "tier*3";
    private static final String EXPERIENCE_FORMULA = "tier*5+2";
    private static final String SENTINEL_FORMULA = "999";

    private YamlDocument woodcuttingConfig;
    private DryadsGift dryadsGift;

    @BeforeEach
    void setUp() {
        woodcuttingConfig = mock(YamlDocument.class);
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.WOODCUTTING_CONFIG)).thenReturn(woodcuttingConfig);

        when(woodcuttingConfig.getInt(WoodcuttingConfigFile.DRYADS_GIFT_AMOUNT_OF_TIERS)).thenReturn(5);
        when(woodcuttingConfig.contains(any(Route.class))).thenReturn(false);
        when(woodcuttingConfig.getString(any(Route.class))).thenReturn(SENTINEL_FORMULA);
        when(woodcuttingConfig.getStringList(any(Route.class))).thenReturn(List.of());

        dryadsGift = new DryadsGift(mcRPG);
    }

    @Nested
    @DisplayName("getActivationChance")
    class GetActivationChance {

        @Test
        @DisplayName("evaluates all-tiers formula with tier variable")
        void getActivationChance_evaluatesAllTiersFormula() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "activation-chance");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(ACTIVATION_FORMULA);

            assertEquals(9.0, dryadsGift.getActivationChance(3), 0.001);
        }

        @Test
        @DisplayName("uses tier-specific override when present")
        void getActivationChance_usesTierSpecificOverride() {
            Route tierRoute = Route.addTo(dryadsGift.getRouteForTier(2), "activation-chance");
            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("25");

            assertEquals(25.0, dryadsGift.getActivationChance(2), 0.001);
        }

        @Test
        @DisplayName("returns literal value when given plain number in all-tiers")
        void getActivationChance_returnsLiteral() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "activation-chance");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("15");

            assertEquals(15.0, dryadsGift.getActivationChance(1), 0.001);
        }

        @Test
        @DisplayName("evaluates formula at max tier")
        void getActivationChance_evaluatesAtMaxTier() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "activation-chance");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(ACTIVATION_FORMULA);

            assertEquals(15.0, dryadsGift.getActivationChance(5), 0.001);
        }

        @Test
        @DisplayName("throws when both tier and all-tiers keys are absent")
        void getActivationChance_throws_whenBothKeysAbsent() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "activation-chance");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(null);

            assertThrows(NullPointerException.class, () -> dryadsGift.getActivationChance(1));
        }
    }

    @Nested
    @DisplayName("getExperienceToDrop")
    class GetExperienceToDrop {

        @Test
        @DisplayName("evaluates all-tiers formula with tier variable")
        void getExperienceToDrop_evaluatesAllTiersFormula() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "experience-to-drop");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(EXPERIENCE_FORMULA);

            assertEquals(12, dryadsGift.getExperienceToDrop(2));
        }

        @Test
        @DisplayName("uses tier-specific override when present")
        void getExperienceToDrop_usesTierSpecificOverride() {
            Route tierRoute = Route.addTo(dryadsGift.getRouteForTier(3), "experience-to-drop");
            when(woodcuttingConfig.contains(tierRoute)).thenReturn(true);
            when(woodcuttingConfig.getString(tierRoute)).thenReturn("50");

            assertEquals(50, dryadsGift.getExperienceToDrop(3));
        }

        @Test
        @DisplayName("truncates decimal to int")
        void getExperienceToDrop_truncatesDecimal() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "experience-to-drop");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn("tier*2.5");

            assertEquals(7, dryadsGift.getExperienceToDrop(3));
        }

        @Test
        @DisplayName("evaluates formula at max tier")
        void getExperienceToDrop_evaluatesAtMaxTier() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "experience-to-drop");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(EXPERIENCE_FORMULA);

            assertEquals(27, dryadsGift.getExperienceToDrop(5));
        }

        @Test
        @DisplayName("throws when both tier and all-tiers keys are absent")
        void getExperienceToDrop_throws_whenBothKeysAbsent() {
            Route allTiersRoute = Route.addTo(dryadsGift.getRouteForAllTiers(), "experience-to-drop");
            when(woodcuttingConfig.getString(allTiersRoute)).thenReturn(null);

            assertThrows(NullPointerException.class, () -> dryadsGift.getExperienceToDrop(1));
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("getAbilityKey returns DRYADS_GIFT_KEY")
        void getAbilityKey_returnsDryadsGiftKey() {
            assertEquals(DryadsGift.DRYADS_GIFT_KEY, dryadsGift.getAbilityKey());
        }

        @Test
        @DisplayName("getSkillKey returns WOODCUTTING_KEY")
        void getSkillKey_returnsWoodcuttingKey() {
            assertEquals(WoodCutting.WOODCUTTING_KEY, dryadsGift.getSkillKey());
        }

        @Test
        @DisplayName("getDatabaseName returns dryads_gift")
        void getDatabaseName_returnsDryadsGift() {
            assertEquals("dryads_gift", dryadsGift.getDatabaseName());
        }

        @Test
        @DisplayName("getAbilityEnabledRoute returns correct route")
        void getAbilityEnabledRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.DRYADS_GIFT_ENABLED, dryadsGift.getAbilityEnabledRoute());
        }

        @Test
        @DisplayName("getMaxTier reads from config")
        void getMaxTier_readsFromConfig() {
            when(woodcuttingConfig.getInt(WoodcuttingConfigFile.DRYADS_GIFT_AMOUNT_OF_TIERS)).thenReturn(5);
            assertEquals(5, dryadsGift.getMaxTier());
        }

        @Test
        @DisplayName("getAbilityTierConfigurationRoute returns correct route")
        void getAbilityTierConfigurationRoute_returnsCorrectRoute() {
            assertEquals(WoodcuttingConfigFile.DRYADS_GIFT_CONFIGURATION_HEADER, dryadsGift.getAbilityTierConfigurationRoute());
        }

        @Test
        @DisplayName("getYamlDocument returns non-null")
        void getYamlDocument_returnsNonNull() {
            assertNotNull(dryadsGift.getYamlDocument());
        }
    }
}
