package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbilityUpgradeRewardTypeTest extends McRPGBaseTest {

    @DisplayName("Given the type, when calling getKey, then it returns the ability_upgrade key")
    @Test
    public void getKey_returnsAbilityUpgradeKey() {
        AbilityUpgradeRewardType type = new AbilityUpgradeRewardType();
        assertEquals(AbilityUpgradeRewardType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        AbilityUpgradeRewardType type = new AbilityUpgradeRewardType();
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfigTests {

        @DisplayName("Serializes ability key and tier")
        @Test
        void serializeConfig_includesAbilityAndTier() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:enhanced_bleed", 3);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("mcrpg:enhanced_bleed", serialized.get("ability"));
            assertEquals(3, serialized.get("tier"));
        }

        @DisplayName("Includes display label when present")
        @Test
        void serializeConfig_includesDisplayLabel() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:enhanced_bleed", 2)
                    .withInlineDisplayLabel("Upgrade Bleed to T2");
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("Upgrade Bleed to T2", serialized.get("display"));
        }

        @DisplayName("Omits display label when empty")
        @Test
        void serializeConfig_omitsEmptyDisplayLabel() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:enhanced_bleed", 2);
            Map<String, Object> serialized = configured.serializeConfig();

            assertFalse(serialized.containsKey("display"));
        }

        @DisplayName("Includes localization route when present")
        @Test
        void serializeConfig_includesLocalizationRoute() {
            Route route = Route.fromString("quests.mcrpg.test-quest.rewards.upgrade");
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:enhanced_bleed", 2)
                    .withLocalizationRoute(route);
            Map<String, Object> serialized = configured.serializeConfig();

            assertEquals("quests.mcrpg.test-quest.rewards.upgrade", serialized.get("localization-route"));
        }

        @DisplayName("Omits localization route when null")
        @Test
        void serializeConfig_omitsNullLocalizationRoute() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:enhanced_bleed", 2);
            Map<String, Object> serialized = configured.serializeConfig();

            assertFalse(serialized.containsKey("localization-route"));
        }

        @DisplayName("Serializes empty string for null ability key")
        @Test
        void serializeConfig_emptyStringForNullAbilityKey() {
            AbilityUpgradeRewardType base = new AbilityUpgradeRewardType();
            Map<String, Object> serialized = base.serializeConfig();

            assertEquals("", serialized.get("ability"));
            assertEquals(0, serialized.get("tier"));
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfigTests {

        @DisplayName("Round-trips all fields through serialize/deserialize")
        @Test
        void fromSerializedConfig_roundTripsAllFields() {
            Route route = Route.fromString("quests.mcrpg.test.rewards.upgrade");
            AbilityUpgradeRewardType original = createConfigured("mcrpg:deeper_wound", 4)
                    .withLocalizationRoute(route)
                    .withInlineDisplayLabel("Upgrade Deeper Wound");

            Map<String, Object> serialized = original.serializeConfig();
            AbilityUpgradeRewardType reconstructed = new AbilityUpgradeRewardType().fromSerializedConfig(serialized);
            Map<String, Object> reSerialized = reconstructed.serializeConfig();

            assertEquals(serialized.get("ability"), reSerialized.get("ability"));
            assertEquals(serialized.get("tier"), reSerialized.get("tier"));
            assertEquals(serialized.get("display"), reSerialized.get("display"));
            assertEquals(serialized.get("localization-route"), reSerialized.get("localization-route"));
        }

        @DisplayName("Defaults tier to 1 when missing from config")
        @Test
        void fromSerializedConfig_defaultsTierToOne() {
            Map<String, Object> config = new HashMap<>();
            config.put("ability", "mcrpg:enhanced_bleed");

            AbilityUpgradeRewardType result = new AbilityUpgradeRewardType().fromSerializedConfig(config);
            Map<String, Object> serialized = result.serializeConfig();

            assertEquals(1, serialized.get("tier"));
        }

        @DisplayName("Defaults display label to empty when missing from config")
        @Test
        void fromSerializedConfig_defaultsDisplayToEmpty() {
            Map<String, Object> config = new HashMap<>();
            config.put("ability", "mcrpg:enhanced_bleed");
            config.put("tier", 2);

            AbilityUpgradeRewardType result = new AbilityUpgradeRewardType().fromSerializedConfig(config);
            Map<String, Object> serialized = result.serializeConfig();

            assertFalse(serialized.containsKey("display"));
        }

        @DisplayName("Defaults localization route to null when missing from config")
        @Test
        void fromSerializedConfig_defaultsLocalizationRouteToNull() {
            Map<String, Object> config = new HashMap<>();
            config.put("ability", "mcrpg:enhanced_bleed");
            config.put("tier", 2);

            AbilityUpgradeRewardType result = new AbilityUpgradeRewardType().fromSerializedConfig(config);
            Map<String, Object> serialized = result.serializeConfig();

            assertFalse(serialized.containsKey("localization-route"));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no-arg)")
    class DescribeForDisplayTests {

        @DisplayName("Formats ability name by title-casing underscore-separated parts")
        @Test
        void describeForDisplay_formatsTitleCase() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:enhanced_bleed", 3);
            assertEquals("Upgrade: Enhanced Bleed (Tier 3)", configured.describeForDisplay());
        }

        @DisplayName("Returns 'Unknown' when ability key is null")
        @Test
        void describeForDisplay_unknownForNullKey() {
            AbilityUpgradeRewardType base = new AbilityUpgradeRewardType();
            assertEquals("Upgrade: Unknown (Tier 0)", base.describeForDisplay());
        }

        @DisplayName("Handles single-word ability key")
        @Test
        void describeForDisplay_singleWordKey() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:bleed", 1);
            assertEquals("Upgrade: Bleed (Tier 1)", configured.describeForDisplay());
        }

        @DisplayName("Handles multi-part ability key with three words")
        @Test
        void describeForDisplay_threeWordKey() {
            AbilityUpgradeRewardType configured = createConfigured("mcrpg:its_a_triple", 2);
            assertEquals("Upgrade: Its A Triple (Tier 2)", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRouteTests {

        @DisplayName("Returns a new instance with the route set")
        @Test
        void withLocalizationRoute_returnsNewInstance() {
            AbilityUpgradeRewardType original = createConfigured("mcrpg:enhanced_bleed", 2);
            Route route = Route.fromString("quests.mcrpg.test.rewards.upgrade");
            AbilityUpgradeRewardType result = original.withLocalizationRoute(route);

            assertNotSame(original, result);
            Map<String, Object> serialized = result.serializeConfig();
            assertEquals("quests.mcrpg.test.rewards.upgrade", serialized.get("localization-route"));
            assertEquals("mcrpg:enhanced_bleed", serialized.get("ability"));
            assertEquals(2, serialized.get("tier"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabelTests {

        @DisplayName("Returns a new instance with the label set")
        @Test
        void withInlineDisplayLabel_returnsNewInstance() {
            AbilityUpgradeRewardType original = createConfigured("mcrpg:enhanced_bleed", 3);
            AbilityUpgradeRewardType result = original.withInlineDisplayLabel("Custom Label");

            assertNotSame(original, result);
            Map<String, Object> serialized = result.serializeConfig();
            assertEquals("Custom Label", serialized.get("display"));
            assertEquals("mcrpg:enhanced_bleed", serialized.get("ability"));
            assertEquals(3, serialized.get("tier"));
        }
    }

    /**
     * Creates a configured AbilityUpgradeRewardType by round-tripping through fromSerializedConfig.
     *
     * @param abilityKeyStr the ability key string (e.g. "mcrpg:enhanced_bleed")
     * @param tier          the target tier
     * @return a configured instance
     */
    private AbilityUpgradeRewardType createConfigured(String abilityKeyStr, int tier) {
        Map<String, Object> config = new HashMap<>();
        config.put("ability", abilityKeyStr);
        config.put("tier", tier);
        return new AbilityUpgradeRewardType().fromSerializedConfig(config);
    }
}
