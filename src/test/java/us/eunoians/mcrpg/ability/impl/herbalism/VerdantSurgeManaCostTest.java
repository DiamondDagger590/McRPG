package us.eunoians.mcrpg.ability.impl.herbalism;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.HerbalismConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link VerdantSurge#getManaCost(AbilityHolder)} correctly evaluates
 * the per-tier mana-cost formula from {@code herbalism_configuration.yml}.
 */
class VerdantSurgeManaCostTest extends McRPGBaseTest {

    private static final String FORMULA = "42-(5.5*tier)";

    private VerdantSurge verdantSurge;
    private YamlDocument herbalismConfig;
    private YamlDocument mainConfig;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);

        herbalismConfig = mock(YamlDocument.class);
        mainConfig = mock(YamlDocument.class);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.HERBALISM_CONFIG)).thenReturn(herbalismConfig);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);

        when(herbalismConfig.getInt(HerbalismConfigFile.VERDANT_SURGE_AMOUNT_OF_TIERS)).thenReturn(5);
        when(herbalismConfig.contains(any(Route.class))).thenReturn(false);
        when(herbalismConfig.getString(any(Route.class))).thenReturn(FORMULA);
        when(herbalismConfig.getString(any(Route.class), any())).thenReturn(FORMULA);
        when(herbalismConfig.getStringList(any(Route.class))).thenReturn(java.util.List.of());

        when(mainConfig.getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5)).thenReturn(1);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);
        verdantSurge = new VerdantSurge(mcRPG);
        abilityRegistry.register(verdantSurge);
    }

    @Test
    @DisplayName("Given formula \"42-(5.5*tier)\" and tier=1, when getManaCost() is called, then it returns 36 (int truncation of 36.5)")
    void getManaCost_evaluatesFormulaWithTier() {
        AbilityHolder holder = holderAtTier(1);
        assertEquals(36, verdantSurge.getManaCost(holder));
    }

    @Test
    @DisplayName("Given formula \"42-(5.5*tier)\" and tier=5, when getManaCost() is called, then it returns 14 (int truncation of 14.5)")
    void getManaCost_evaluatesFormulaForHigherTier() {
        AbilityHolder holder = holderAtTier(5);
        assertEquals(14, verdantSurge.getManaCost(holder));
    }

    @Test
    @DisplayName("Given formula \"42-(5.5*tier)\" and tier=8, when getManaCost() is called, then it is clamped to the global minimum")
    void getManaCost_clampsToGlobalMinimum_whenFormulaProducesNegative() {
        // tier=8: 42 - 5.5*8 = 42 - 44 = -2 → clamped to minimum (1)
        when(mainConfig.getInt(MainConfigFile.MANA_MINIMUM_ABILITY_COST, 5)).thenReturn(1);
        AbilityHolder holder = holderAtTier(8);
        assertEquals(1, verdantSurge.getManaCost(holder));
    }

    @Test
    @DisplayName("Given a tier-specific mana-cost override, when getManaCost() is called, then the tier value is used instead of all-tiers formula")
    void getManaCost_usesTierSpecificOverride_whenPresent() {
        Route tierThreeManaRoute = Route.addTo(verdantSurge.getRouteForTier(3), "mana-cost");
        when(herbalismConfig.contains(tierThreeManaRoute)).thenReturn(true);
        when(herbalismConfig.getString(tierThreeManaRoute, "0")).thenReturn("20");

        AbilityHolder holder = holderAtTier(3);
        assertEquals(20, verdantSurge.getManaCost(holder));
    }

    /**
     * Creates a mock {@link AbilityHolder} with ability data at the specified tier.
     *
     * @param tier The tier to configure the ability data at.
     * @return A mock {@link AbilityHolder} returning tier-set ability data.
     */
    private AbilityHolder holderAtTier(int tier) {
        AbilityData abilityData = new AbilityData(
                verdantSurge.getAbilityKey(),
                new AbilityTierAttribute(tier));
        AbilityHolder holder = mock(AbilityHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
        when(holder.getAbilityData(verdantSurge)).thenReturn(Optional.of(abilityData));
        return holder;
    }
}
