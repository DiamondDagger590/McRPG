package us.eunoians.mcrpg.ability.impl.type.configurable;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the default {@link ConfigurableActiveAbility#getManaCost(AbilityHolder)} implementation:
 * formula evaluation with the {@code tier} variable, global-minimum floor, and tier-specific
 * route precedence over the all-tiers route.
 * <p>
 * Uses a concrete inner {@link StubConfigurableActiveAbility} class so that calls made from
 * within the default {@code getManaCost} method reach the overridden helpers rather than
 * bypassing Mockito proxy interception (which occurs with {@code CALLS_REAL_METHODS} on
 * interface mocks when default methods call other default methods internally).
 */
class ConfigurableActiveAbilityManaCostTest extends McRPGBaseTest {

    private static final Route TIER_CONFIG_ROUTE = Route.fromString("test.ability");
    private static final int GLOBAL_MINIMUM = 5;

    private StubConfigurableActiveAbility ability;
    private YamlDocument abilityConfig;

    @BeforeEach
    void setUp() {
        abilityConfig = mock(YamlDocument.class);
        ability = new StubConfigurableActiveAbility(abilityConfig);

        // Stub the FileManager so getManaCost can read the global minimum
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE);
        YamlDocument mainConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mainConfig);
        when(mainConfig.getInt(eq(MainConfigFile.MANA_MINIMUM_ABILITY_COST), anyInt()))
                .thenReturn(GLOBAL_MINIMUM);
    }

    @Test
    @DisplayName("Given a mana-cost formula, when getManaCost is called at tier 1, then the formula is evaluated correctly")
    void getManaCost_evaluatesFormulaWithTier() {
        ability.tier = 1;
        Route tier1ManaRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-1"), "mana-cost");
        Route allTiersManaRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "mana-cost");

        when(abilityConfig.contains(tier1ManaRoute)).thenReturn(false);
        when(abilityConfig.getString(eq(allTiersManaRoute), any())).thenReturn("50-(7*tier)");

        int cost = ability.getManaCost(mock(AbilityHolder.class));

        // 50 - (7 * 1) = 43, which is above globalMinimum (5)
        assertEquals(43, cost);
    }

    @Test
    @DisplayName("Given a formula that evaluates below the global minimum, when getManaCost is called, then it returns the global minimum")
    void getManaCost_returnsGlobalMinimum_whenComputedValueIsBelow() {
        ability.tier = 10;
        Route tier10ManaRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-10"), "mana-cost");
        Route allTiersManaRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "mana-cost");

        when(abilityConfig.contains(tier10ManaRoute)).thenReturn(false);
        // 50 - (7 * 10) = -20, which is below globalMinimum (5)
        when(abilityConfig.getString(eq(allTiersManaRoute), any())).thenReturn("50-(7*tier)");

        int cost = ability.getManaCost(mock(AbilityHolder.class));

        assertEquals(GLOBAL_MINIMUM, cost);
    }

    @Test
    @DisplayName("Given both a tier-specific and an all-tiers route, when getManaCost is called, then the tier-specific route takes precedence")
    void getManaCost_usesTierSpecificRoute_whenPresent() {
        ability.tier = 2;
        Route tier2ManaRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-2"), "mana-cost");
        Route allTiersManaRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "mana-cost");

        when(abilityConfig.contains(tier2ManaRoute)).thenReturn(true);
        when(abilityConfig.getString(eq(tier2ManaRoute), any())).thenReturn("30");
        when(abilityConfig.getString(eq(allTiersManaRoute), any())).thenReturn("50-(7*tier)");

        int cost = ability.getManaCost(mock(AbilityHolder.class));

        // Tier-2 specific: 30, not the all-tiers formula result (50 - 14 = 36)
        assertEquals(30, cost);
    }

    /**
     * Minimal concrete implementation of {@link ConfigurableActiveAbility} used to test
     * the default {@code getManaCost} logic.
     * <p>
     * Overrides {@link #getCurrentAbilityTier(AbilityHolder)} to return the controllable
     * {@link #tier} field, bypassing the real attribute-lookup path that would require a
     * fully-wired {@link AbilityHolder}.
     */
    private class StubConfigurableActiveAbility extends McRPGAbility implements ConfigurableActiveAbility {

        private static final NamespacedKey KEY =
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "stub_configurable_ability");

        int tier = 1;
        private final YamlDocument yamlDocument;

        StubConfigurableActiveAbility(@NotNull YamlDocument yamlDocument) {
            super(mcRPG, KEY);
            this.yamlDocument = yamlDocument;
        }

        @Override
        @NotNull
        public YamlDocument getYamlDocument() {
            return yamlDocument;
        }

        @Override
        @NotNull
        public Route getAbilityTierConfigurationRoute() {
            return TIER_CONFIG_ROUTE;
        }

        @Override
        @NotNull
        public Route getAbilityEnabledRoute() {
            return Route.fromString("test.ability.enabled");
        }

        @Override
        @NotNull
        public Route getDisplayItemRoute() {
            return Route.fromString("test.ability.display");
        }

        @Override
        public int getCurrentAbilityTier(@NotNull AbilityHolder abilityHolder) {
            return tier;
        }

        @Override
        public int getMaxTier() {
            return 5;
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return true;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @Override
        @NotNull
        public String getDatabaseName() {
            return "stub_configurable_ability";
        }

        @Override
        @NotNull
        public String getName(@NotNull McRPGPlayer player) {
            return "StubConfigurableAbility";
        }

        @Override
        @NotNull
        public String getName() {
            return "StubConfigurableAbility";
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName(@NotNull McRPGPlayer player) {
            return net.kyori.adventure.text.Component.text("StubConfigurableAbility");
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName() {
            return net.kyori.adventure.text.Component.text("StubConfigurableAbility");
        }

        @Override
        @NotNull
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException("Not used in tests");
        }

        @Override
        @NotNull
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
