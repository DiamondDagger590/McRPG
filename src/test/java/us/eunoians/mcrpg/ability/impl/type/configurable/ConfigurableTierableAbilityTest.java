package us.eunoians.mcrpg.ability.impl.type.configurable;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.impl.McRPGAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurableTierableAbilityTest extends McRPGBaseTest {

    private static final Route TIER_CONFIG_ROUTE = Route.fromString("test.ability.tier-configuration");

    private StubTierableAbility ability;
    private YamlDocument yamlDocument;

    @BeforeEach
    void setUp() {
        yamlDocument = mock(YamlDocument.class);
        ability = new StubTierableAbility(yamlDocument);
        ConfigurableTierableAbility.INFERRED_UPGRADE_QUEST_WARNED.clear();
    }

    @Nested
    @DisplayName("getUnlockLevelForTier")
    class GetUnlockLevelForTierTests {

        @Test
        @DisplayName("uses tier-specific route when present")
        void getUnlockLevelForTier_usesTierSpecific_whenPresent() {
            Route tier2Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-2"), "unlock-level");

            when(yamlDocument.contains(tier2Route)).thenReturn(true);
            when(yamlDocument.getString(tier2Route)).thenReturn("15");

            int level = ability.getUnlockLevelForTier(2);

            assertEquals(15, level);
        }

        @Test
        @DisplayName("falls back to all-tiers route when tier-specific is absent")
        void getUnlockLevelForTier_usesAllTiers_whenTierSpecificAbsent() {
            Route tier3Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-3"), "unlock-level");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "unlock-level");

            when(yamlDocument.contains(tier3Route)).thenReturn(false);
            when(yamlDocument.getString(allTiersRoute)).thenReturn("10*tier");

            int level = ability.getUnlockLevelForTier(3);

            assertEquals(30, level);
        }

        @Test
        @DisplayName("evaluates Parser formula with tier variable")
        void getUnlockLevelForTier_evaluatesFormulaWithTier() {
            Route tier1Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-1"), "unlock-level");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "unlock-level");

            when(yamlDocument.contains(tier1Route)).thenReturn(false);
            when(yamlDocument.getString(allTiersRoute)).thenReturn("5+(10*tier)");

            int level = ability.getUnlockLevelForTier(1);

            assertEquals(15, level);
        }

        @Test
        @DisplayName("returns integer-truncated value from formula")
        void getUnlockLevelForTier_truncatesToInt() {
            Route tier1Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-1"), "unlock-level");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "unlock-level");

            when(yamlDocument.contains(tier1Route)).thenReturn(false);
            when(yamlDocument.getString(allTiersRoute)).thenReturn("7.9");

            int level = ability.getUnlockLevelForTier(1);

            assertEquals(7, level);
        }
    }

    @Nested
    @DisplayName("getUpgradeQuestKey")
    class GetUpgradeQuestKeyTests {

        @Test
        @DisplayName("uses tier-specific upgrade-quest when present")
        void getUpgradeQuestKey_usesTierSpecific_whenPresent() {
            Route tier2Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-2"), "upgrade-quest");

            when(yamlDocument.contains(tier2Route)).thenReturn(true);
            when(yamlDocument.getString(tier2Route)).thenReturn("mcrpg:bleed_upgrade_t2");

            Optional<NamespacedKey> result = ability.getUpgradeQuestKey(2);

            assertTrue(result.isPresent());
            assertEquals("mcrpg", result.get().getNamespace());
            assertEquals("bleed_upgrade_t2", result.get().getKey());
        }

        @Test
        @DisplayName("falls back to all-tiers upgrade-quest when tier-specific absent")
        void getUpgradeQuestKey_usesAllTiers_whenTierSpecificAbsent() {
            Route tier3Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-3"), "upgrade-quest");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "upgrade-quest");

            when(yamlDocument.contains(tier3Route)).thenReturn(false);
            when(yamlDocument.contains(allTiersRoute)).thenReturn(true);
            when(yamlDocument.getString(allTiersRoute)).thenReturn("mcrpg:stub_upgrade_t{tier}");

            Optional<NamespacedKey> result = ability.getUpgradeQuestKey(3);

            assertTrue(result.isPresent());
            assertEquals("mcrpg", result.get().getNamespace());
            assertEquals("stub_upgrade_t3", result.get().getKey());
        }

        @Test
        @DisplayName("replaces {tier} placeholder in all-tiers value")
        void getUpgradeQuestKey_replacesTierPlaceholder() {
            Route tier5Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-5"), "upgrade-quest");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "upgrade-quest");

            when(yamlDocument.contains(tier5Route)).thenReturn(false);
            when(yamlDocument.contains(allTiersRoute)).thenReturn(true);
            when(yamlDocument.getString(allTiersRoute)).thenReturn("mcrpg:quest_{tier}_upgrade");

            Optional<NamespacedKey> result = ability.getUpgradeQuestKey(5);

            assertTrue(result.isPresent());
            assertEquals("quest_5_upgrade", result.get().getKey());
        }

        @Test
        @DisplayName("returns inferred key when no config present")
        void getUpgradeQuestKey_returnsInferred_whenNoConfig() {
            Route tier2Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-2"), "upgrade-quest");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "upgrade-quest");

            when(yamlDocument.contains(tier2Route)).thenReturn(false);
            when(yamlDocument.contains(allTiersRoute)).thenReturn(false);

            Optional<NamespacedKey> result = ability.getUpgradeQuestKey(2);

            assertTrue(result.isPresent());
            assertEquals("mcrpg", result.get().getNamespace());
            assertEquals("stub_tierable_ability_upgrade", result.get().getKey());
        }

        @Test
        @DisplayName("logs inference warning only once per ability key")
        void getUpgradeQuestKey_logsWarningOnce() {
            Route tier2Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-2"), "upgrade-quest");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "upgrade-quest");
            Route tier3Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-3"), "upgrade-quest");

            when(yamlDocument.contains(tier2Route)).thenReturn(false);
            when(yamlDocument.contains(tier3Route)).thenReturn(false);
            when(yamlDocument.contains(allTiersRoute)).thenReturn(false);

            ability.getUpgradeQuestKey(2);
            assertTrue(ConfigurableTierableAbility.INFERRED_UPGRADE_QUEST_WARNED.contains(ability.getAbilityKey()));

            ability.getUpgradeQuestKey(3);
            assertEquals(1, ConfigurableTierableAbility.INFERRED_UPGRADE_QUEST_WARNED.size());
        }

        @Test
        @DisplayName("returns empty optional when configured value is empty string")
        void getUpgradeQuestKey_returnsInferred_whenEmpty() {
            Route tier2Route = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "tier-2"), "upgrade-quest");
            Route allTiersRoute = Route.addTo(Route.addTo(TIER_CONFIG_ROUTE, "all-tiers"), "upgrade-quest");

            when(yamlDocument.contains(tier2Route)).thenReturn(true);
            when(yamlDocument.getString(tier2Route)).thenReturn("");
            when(yamlDocument.contains(allTiersRoute)).thenReturn(false);

            Optional<NamespacedKey> result = ability.getUpgradeQuestKey(2);

            assertTrue(result.isPresent());
            assertEquals("stub_tierable_ability_upgrade", result.get().getKey());
        }
    }

    private class StubTierableAbility extends McRPGAbility implements ConfigurableTierableAbility {

        private static final NamespacedKey KEY =
                new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "stub_tierable_ability");

        private final YamlDocument yamlDocument;

        StubTierableAbility(@NotNull YamlDocument yamlDocument) {
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
            return "stub_tierable_ability";
        }

        @Override
        @NotNull
        public String getName(@NotNull McRPGPlayer player) {
            return "StubTierableAbility";
        }

        @Override
        @NotNull
        public String getName() {
            return "StubTierableAbility";
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName(@NotNull McRPGPlayer player) {
            return net.kyori.adventure.text.Component.text("StubTierableAbility");
        }

        @Override
        @NotNull
        public net.kyori.adventure.text.Component getDisplayName() {
            return net.kyori.adventure.text.Component.text("StubTierableAbility");
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
