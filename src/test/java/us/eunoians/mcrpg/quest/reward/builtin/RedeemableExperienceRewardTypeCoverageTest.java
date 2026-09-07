package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RedeemableExperienceRewardType Coverage")
public class RedeemableExperienceRewardTypeCoverageTest extends McRPGBaseTest {

    private RedeemableExperienceRewardType type;

    @BeforeEach
    public void setup() {
        type = new RedeemableExperienceRewardType();
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("getNumericAmount returns 0 for unconfigured instance")
        public void getNumericAmount_returnsZero() {
            assertEquals(0L, type.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("describeForDisplay returns '0 Redeemable XP' for unconfigured instance")
        public void describeForDisplay_returnsZeroLabel() {
            assertEquals("0 Redeemable XP", type.describeForDisplay());
        }

        @Test
        @DisplayName("serializeConfig returns map with amount 0 and no extras")
        public void serializeConfig_returnsAmountZeroOnly() {
            Map<String, Object> serialized = type.serializeConfig();
            assertEquals(0, ((Number) serialized.get("amount")).intValue());
            assertFalse(serialized.containsKey("display"));
            assertFalse(serialized.containsKey("localization-route"));
        }
    }

    @Nested
    @DisplayName("isScalable")
    class IsScalable {

        @Test
        @DisplayName("returns true")
        public void isScalable_returnsTrue() {
            assertTrue(type.isScalable());
        }
    }

    @Nested
    @DisplayName("withExactAmount")
    class WithExactAmount {

        @Test
        @DisplayName("sets amount to exact value")
        public void withExactAmount_setsExactValue() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType exact = configured.withExactAmount(42);
            assertEquals(42L, exact.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("preserves localization route")
        public void withExactAmount_preservesLocalizationRoute() {
            Route route = Route.fromString("my.route");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100))
                    .withLocalizationRoute(route);
            RedeemableExperienceRewardType exact = configured.withExactAmount(50);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals("my.route", serialized.get("localization-route"));
            assertEquals(50, ((Number) serialized.get("amount")).intValue());
        }

        @Test
        @DisplayName("preserves display label")
        public void withExactAmount_preservesDisplayLabel() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100, "display", "Custom Label"));
            RedeemableExperienceRewardType exact = configured.withExactAmount(75);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals("Custom Label", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("sets localization route on serialized output")
        public void withLocalizationRoute_setsRoute() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 200));
            RedeemableExperienceRewardType withRoute = configured.withLocalizationRoute(Route.fromString("custom.label.route"));
            Map<String, Object> serialized = withRoute.serializeConfig();
            assertEquals("custom.label.route", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("preserves amount")
        public void withLocalizationRoute_preservesAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 350));
            RedeemableExperienceRewardType withRoute = configured.withLocalizationRoute(Route.fromString("some.route"));
            assertEquals(350L, withRoute.getNumericAmount().getAsLong());
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("sets display label on serialized output")
        public void withInlineDisplayLabel_setsLabel() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType withLabel = configured.withInlineDisplayLabel("My Custom Display");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals("My Custom Display", serialized.get("display"));
        }

        @Test
        @DisplayName("preserves amount and localization route")
        public void withInlineDisplayLabel_preservesOtherFields() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 250))
                    .withLocalizationRoute(Route.fromString("existing.route"));
            RedeemableExperienceRewardType withLabel = configured.withInlineDisplayLabel("Label");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals(250, ((Number) serialized.get("amount")).intValue());
            assertEquals("existing.route", serialized.get("localization-route"));
            assertEquals("Label", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("serializeConfig with extra fields")
    class SerializeConfigExtras {

        @Test
        @DisplayName("includes display label when non-empty")
        public void serializeConfig_includesDisplayLabel() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("display", "XP Reward");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("XP Reward", serialized.get("display"));
        }

        @Test
        @DisplayName("includes localization-route when present")
        public void serializeConfig_includesLocalizationRoute() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("localization-route", "quest.rewards.redeemable-xp");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("quest.rewards.redeemable-xp", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("omits display when empty string")
        public void serializeConfig_omitsEmptyDisplay() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        public void serializeConfig_omitsNullLocalizationRoute() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("localization-route"));
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig edge cases")
    class FromSerializedConfigEdgeCases {

        @Test
        @DisplayName("missing amount key yields amount 0")
        public void fromSerializedConfig_missingAmount_yieldsZero() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of());
            assertEquals(0L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("string numeric amount is parsed correctly")
        public void fromSerializedConfig_stringAmount_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "123");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            assertEquals(123L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("non-numeric string amount yields 0")
        public void fromSerializedConfig_nonNumericAmount_yieldsZero() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "not-a-number");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            assertEquals(0L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("negative amount is preserved")
        public void fromSerializedConfig_negativeAmount_isPreserved() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", -5));
            assertEquals(-5L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("localization-route is deserialized from config")
        public void fromSerializedConfig_deserializesLocalizationRoute() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("localization-route", "quest.rewards.xp");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> reserialized = configured.serializeConfig();
            assertEquals("quest.rewards.xp", reserialized.get("localization-route"));
        }

        @Test
        @DisplayName("display label is deserialized from config")
        public void fromSerializedConfig_deserializesDisplayLabel() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("display", "Bonus XP");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> reserialized = configured.serializeConfig();
            assertEquals("Bonus XP", reserialized.get("display"));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no player)")
    class DescribeForDisplayNoPlayer {

        @Test
        @DisplayName("includes amount in display string")
        public void describeForDisplay_includesAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 500));
            assertEquals("500 Redeemable XP", configured.describeForDisplay());
        }

        @Test
        @DisplayName("handles zero amount")
        public void describeForDisplay_handlesZeroAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of());
            assertEquals("0 Redeemable XP", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier edge cases")
    class WithAmountMultiplierEdgeCases {

        @Test
        @DisplayName("multiplier of 1.0 preserves amount")
        public void withAmountMultiplier_identity_preservesAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType result = configured.withAmountMultiplier(1.0);
            assertEquals(100L, result.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("multiplier preserves localization route and display label")
        public void withAmountMultiplier_preservesExtras() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("display", "Custom");
            config.put("localization-route", "some.route");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(2.0);
            Map<String, Object> serialized = scaled.serializeConfig();
            assertEquals(200, ((Number) serialized.get("amount")).intValue());
            assertEquals("Custom", serialized.get("display"));
            assertEquals("some.route", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("very large multiplier produces correct result")
        public void withAmountMultiplier_largeMultiplier() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(10.0);
            assertEquals(1000L, scaled.getNumericAmount().getAsLong());
        }
    }

    @Nested
    @DisplayName("Full round-trip with all fields")
    class FullRoundTrip {

        @Test
        @DisplayName("serialize then deserialize preserves all fields")
        public void fullRoundTrip_preservesAllFields() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 250))
                    .withLocalizationRoute(Route.fromString("quest.rewards.display"))
                    .withInlineDisplayLabel("Quest Reward");

            Map<String, Object> serialized = configured.serializeConfig();
            RedeemableExperienceRewardType roundTripped = type.fromSerializedConfig(serialized);

            assertEquals(250L, roundTripped.getNumericAmount().getAsLong());
            Map<String, Object> reReserialized = roundTripped.serializeConfig();
            assertEquals("quest.rewards.display", reReserialized.get("localization-route"));
            assertEquals("Quest Reward", reReserialized.get("display"));
        }
    }
}
