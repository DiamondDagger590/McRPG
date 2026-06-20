package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityUpgradeRewardTypeCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("parses ability and tier from config")
        void fromSerializedConfig_parsesAbilityAndTier() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:enhanced_bleed",
                            "tier", 3
                    ));

            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("mcrpg:enhanced_bleed", serialized.get("ability"));
            assertEquals(3, serialized.get("tier"));
        }

        @Test
        @DisplayName("defaults tier to 1 when missing")
        void fromSerializedConfig_defaultsTierToOne_whenMissing() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed"
                    ));

            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(1, serialized.get("tier"));
        }

        @Test
        @DisplayName("preserves localization route when present")
        void fromSerializedConfig_preservesLocalizationRoute() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2,
                            "localization-route", "quests.mcrpg.test.rewards.upgrade"
                    ));

            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("quests.mcrpg.test.rewards.upgrade", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("preserves display label when present")
        void fromSerializedConfig_preservesDisplayLabel() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2,
                            "display", "Upgrade Bleed to Tier 2"
                    ));

            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Upgrade Bleed to Tier 2", serialized.get("display"));
        }

        @Test
        @DisplayName("handles Number type for tier")
        void fromSerializedConfig_handlesNumberType() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", (Number) 4L
                    ));

            assertEquals(4, configured.serializeConfig().get("tier"));
        }

        @Test
        @DisplayName("returns new instance, not the base")
        void fromSerializedConfig_returnsNewInstance() {
            AbilityUpgradeRewardType base = new AbilityUpgradeRewardType();
            AbilityUpgradeRewardType configured = base.fromSerializedConfig(Map.of(
                    "ability", "mcrpg:bleed",
                    "tier", 2
            ));

            assertNotSame(base, configured);
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("base instance serializes with empty ability and zero tier")
        void serializeConfig_baseInstance_emptyAbilityZeroTier() {
            AbilityUpgradeRewardType base = new AbilityUpgradeRewardType();
            Map<String, Object> serialized = base.serializeConfig();

            assertEquals("", serialized.get("ability"));
            assertEquals(0, serialized.get("tier"));
        }

        @Test
        @DisplayName("omits display when empty")
        void serializeConfig_omitsDisplayWhenEmpty() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 1
                    ));

            assertFalse(configured.serializeConfig().containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        void serializeConfig_omitsLocalizationRouteWhenNull() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 1
                    ));

            assertFalse(configured.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("includes display when non-empty")
        void serializeConfig_includesDisplayWhenNonEmpty() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 1,
                            "display", "Upgrade Reward"
                    ));

            assertTrue(configured.serializeConfig().containsKey("display"));
            assertEquals("Upgrade Reward", configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("round-trips through serialize and deserialize")
        void serializeConfig_roundTrip() {
            Map<String, Object> original = new HashMap<>();
            original.put("ability", "mcrpg:enhanced_bleed");
            original.put("tier", 3);
            original.put("display", "Test Label");
            original.put("localization-route", "quests.ns.key.rewards.label");

            AbilityUpgradeRewardType first = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(original);
            Map<String, Object> serialized = first.serializeConfig();
            AbilityUpgradeRewardType second = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(serialized);

            Map<String, Object> reSerialized = second.serializeConfig();
            assertEquals(serialized.get("ability"), reSerialized.get("ability"));
            assertEquals(serialized.get("tier"), reSerialized.get("tier"));
            assertEquals(serialized.get("display"), reSerialized.get("display"));
            assertEquals(serialized.get("localization-route"), reSerialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns new instance with route set")
        void withLocalizationRoute_returnsNewInstance() {
            AbilityUpgradeRewardType original = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2
                    ));

            AbilityUpgradeRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.upgrade"));

            assertNotSame(original, withRoute);
            assertTrue(withRoute.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("preserves ability and tier")
        void withLocalizationRoute_preservesFields() {
            AbilityUpgradeRewardType original = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 3
                    ));

            AbilityUpgradeRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.key.rewards.label"));

            Map<String, Object> serialized = withRoute.serializeConfig();
            assertEquals("mcrpg:bleed", serialized.get("ability"));
            assertEquals(3, serialized.get("tier"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label set")
        void withInlineDisplayLabel_returnsNewInstance() {
            AbilityUpgradeRewardType original = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2
                    ));

            AbilityUpgradeRewardType withLabel = original.withInlineDisplayLabel("Custom Label");

            assertNotSame(original, withLabel);
            assertEquals("Custom Label", withLabel.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves ability and tier")
        void withInlineDisplayLabel_preservesFields() {
            AbilityUpgradeRewardType original = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:enhanced_bleed",
                            "tier", 4
                    ));

            AbilityUpgradeRewardType withLabel = original.withInlineDisplayLabel("My Label");

            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals("mcrpg:enhanced_bleed", serialized.get("ability"));
            assertEquals(4, serialized.get("tier"));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("returns same instance since ability upgrades are not scalable")
        void withAmountMultiplier_returnsSameInstance() {
            AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2
                    ));

            assertSame(reward, reward.withAmountMultiplier(0.5));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("returns empty since ability upgrades have no numeric amount")
        void getNumericAmount_returnsEmpty() {
            AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType();
            assertTrue(reward.getNumericAmount().isEmpty());
        }

        @Test
        @DisplayName("returns empty for configured instance too")
        void getNumericAmount_configuredInstance_returnsEmpty() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 3
                    ));
            assertTrue(configured.getNumericAmount().isEmpty());
        }
    }

    @Nested
    @DisplayName("describeForDisplay no-arg")
    class DescribeForDisplayNoArg {

        @Test
        @DisplayName("formats ability name with title case")
        void describeForDisplay_formatsAbilityName() {
            AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:enhanced_bleed",
                            "tier", 2
                    ));

            String result = reward.describeForDisplay();
            assertEquals("Upgrade: Enhanced Bleed (Tier 2)", result);
        }

        @Test
        @DisplayName("handles single word ability name")
        void describeForDisplay_singleWordAbility() {
            AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 1
                    ));

            String result = reward.describeForDisplay();
            assertEquals("Upgrade: Bleed (Tier 1)", result);
        }

        @Test
        @DisplayName("handles multi-word ability name")
        void describeForDisplay_multiWordAbility() {
            AbilityUpgradeRewardType reward = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:its_a_triple",
                            "tier", 3
                    ));

            String result = reward.describeForDisplay();
            assertEquals("Upgrade: Its A Triple (Tier 3)", result);
        }

    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("key preserved through getKey on configured instance")
        void getKey_configuredInstance_returnsAbilityUpgradeKey() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2
                    ));

            assertEquals(AbilityUpgradeRewardType.KEY, configured.getKey());
            assertEquals("mcrpg:ability_upgrade", configured.getKey().toString());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("configured instance preserves expansion key")
        void getExpansionKey_configuredInstance_preservesKey() {
            AbilityUpgradeRewardType configured = new AbilityUpgradeRewardType()
                    .fromSerializedConfig(Map.of(
                            "ability", "mcrpg:bleed",
                            "tier", 2
                    ));

            assertTrue(configured.getExpansionKey().isPresent());
        }
    }
}
