package us.eunoians.mcrpg.ability.impl.swords;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.skill.SwordsConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SerratedStrikes#getManaCost(AbilityHolder)}.
 * <p>
 * Verifies formula evaluation via the {@link com.diamonddagger590.mccore.parser.Parser},
 * the global-minimum floor, and tier-specific route precedence.
 */
class SerratedStrikesManaCostTest extends McRPGBaseTest {

    private static final int GLOBAL_MINIMUM = 5;

    private SerratedStrikes serratedStrikes;
    private YamlDocument swordsConfig;

    @BeforeEach
    void setUp() {
        swordsConfig = mock(YamlDocument.class);
        YamlDocument mainConfig = mock(YamlDocument.class);

        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        when(fileManager.getFile(FileType.SWORDS_CONFIG)).thenReturn(swordsConfig);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getInt(eq(MainConfigFile.MANA_MINIMUM_ABILITY_COST), anyInt()))
                .thenReturn(GLOBAL_MINIMUM);

        serratedStrikes = new SerratedStrikes(mcRPG);
    }

    /**
     * Creates a mock {@link AbilityHolder} with {@link AbilityData} containing an
     * {@link AbilityTierAttribute} set to the given tier.
     */
    private AbilityHolder holderWithTier(int tier) {
        AbilityHolder holder = mock(AbilityHolder.class);
        AbilityData abilityData = new AbilityData(
                SerratedStrikes.SERRATED_STRIKES_KEY,
                new AbilityTierAttribute(tier));
        when(holder.getAbilityData(serratedStrikes)).thenReturn(Optional.of(abilityData));
        return holder;
    }

    @Test
    @DisplayName("Given a mana-cost formula, when getManaCost is called at tier 1, then the formula is evaluated correctly")
    void getManaCost_evaluatesFormulaWithTier() {
        AbilityHolder holder = holderWithTier(1);

        Route tier1ManaRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-1"), "mana-cost");
        Route allTiersManaRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers"), "mana-cost");

        when(swordsConfig.contains(tier1ManaRoute)).thenReturn(false);
        when(swordsConfig.getString(eq(allTiersManaRoute), any())).thenReturn("55-(6.5*tier)");

        // tier=1 → 55 - 6.5 = 48.5 → cast to int → 48
        assertEquals(48, serratedStrikes.getManaCost(holder));
    }

    @Test
    @DisplayName("Given a missing mana-cost key, when getManaCost is called, then it returns the global minimum")
    void getManaCost_returnsGlobalMinimum_whenManaCostKeyMissing() {
        AbilityHolder holder = holderWithTier(1);

        Route tier1ManaRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-1"), "mana-cost");
        Route allTiersManaRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers"), "mana-cost");

        when(swordsConfig.contains(tier1ManaRoute)).thenReturn(false);
        // getString with default "0" — absent key means formula "0" evaluates to 0
        when(swordsConfig.getString(eq(allTiersManaRoute), any())).thenReturn("0");

        // 0 < globalMinimum(5) → clamped to 5
        assertEquals(GLOBAL_MINIMUM, serratedStrikes.getManaCost(holder));
    }

    @Test
    @DisplayName("Given a formula that evaluates below the global minimum, when getManaCost is called, then it returns the global minimum")
    void getManaCost_returnsGlobalMinimum_whenComputedValueIsBelow() {
        AbilityHolder holder = holderWithTier(1);

        Route tier1ManaRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "tier-1"), "mana-cost");
        Route allTiersManaRoute = Route.addTo(
                Route.addTo(SwordsConfigFile.SERRATED_STRIKES_CONFIGURATION_HEADER, "all-tiers"), "mana-cost");

        when(swordsConfig.contains(tier1ManaRoute)).thenReturn(false);
        when(swordsConfig.getString(eq(allTiersManaRoute), any())).thenReturn("-10");

        assertEquals(GLOBAL_MINIMUM, serratedStrikes.getManaCost(holder));
    }
}
