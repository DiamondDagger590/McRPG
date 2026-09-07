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

@DisplayName("RedeemableLevelsRewardType Coverage")
public class RedeemableLevelsRewardTypeCoverageTest extends McRPGBaseTest {

    private RedeemableLevelsRewardType type;

    @BeforeEach
    public void setup() {
        type = new RedeemableLevelsRewardType();
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
        @DisplayName("describeForDisplay returns '0 Redeemable Level(s)' for unconfigured instance")
        public void describeForDisplay_returnsZeroLabel() {
            assertEquals("0 Redeemable Level(s)", type.describeForDisplay());
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
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10));
            RedeemableLevelsRewardType exact = configured.withExactAmount(7);
            assertEquals(7L, exact.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("zero amount yields zero")
        public void withExactAmount_zeroAmount_yieldsZero() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10));
            RedeemableLevelsRewardType exact = configured.withExactAmount(0);
            assertEquals(0L, exact.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("preserves localization route")
        public void withExactAmount_preservesLocalizationRoute() {
            Route route = Route.fromString("my.route");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10))
                    .withLocalizationRoute(route);
            RedeemableLevelsRewardType exact = configured.withExactAmount(3);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals("my.route", serialized.get("localization-route"));
            assertEquals(3, ((Number) serialized.get("amount")).intValue());
        }

        @Test
        @DisplayName("preserves display label")
        public void withExactAmount_preservesDisplayLabel() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10, "display", "Level Up"))
                    .withExactAmount(5);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Level Up", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("sets localization route on serialized output")
        public void withLocalizationRoute_setsRoute() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            RedeemableLevelsRewardType withRoute = configured.withLocalizationRoute(Route.fromString("custom.label.route"));
            Map<String, Object> serialized = withRoute.serializeConfig();
            assertEquals("custom.label.route", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("preserves amount")
        public void withLocalizationRoute_preservesAmount() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 8));
            RedeemableLevelsRewardType withRoute = configured.withLocalizationRoute(Route.fromString("some.route"));
            assertEquals(8L, withRoute.getNumericAmount().getAsLong());
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("sets display label on serialized output")
        public void withInlineDisplayLabel_setsLabel() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
            RedeemableLevelsRewardType withLabel = configured.withInlineDisplayLabel("Bonus Levels");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals("Bonus Levels", serialized.get("display"));
        }

        @Test
        @DisplayName("preserves amount and localization route")
        public void withInlineDisplayLabel_preservesOtherFields() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 6))
                    .withLocalizationRoute(Route.fromString("existing.route"));
            RedeemableLevelsRewardType withLabel = configured.withInlineDisplayLabel("Label");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals(6, ((Number) serialized.get("amount")).intValue());
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
            config.put("amount", 5);
            config.put("display", "Level Reward");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Level Reward", serialized.get("display"));
        }

        @Test
        @DisplayName("includes localization-route when present")
        public void serializeConfig_includesLocalizationRoute() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 5);
            config.put("localization-route", "quest.rewards.levels");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("quest.rewards.levels", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("omits display when empty string")
        public void serializeConfig_omitsEmptyDisplay() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        public void serializeConfig_omitsNullLocalizationRoute() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
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
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of());
            assertEquals(0L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("string numeric amount is parsed correctly")
        public void fromSerializedConfig_stringAmount_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "7");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            assertEquals(7L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("non-numeric string amount yields 0")
        public void fromSerializedConfig_nonNumericAmount_yieldsZero() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "invalid");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            assertEquals(0L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("negative amount is preserved")
        public void fromSerializedConfig_negativeAmount_isPreserved() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", -2));
            assertEquals(-2L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("localization-route is deserialized from config")
        public void fromSerializedConfig_deserializesLocalizationRoute() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 5);
            config.put("localization-route", "quest.rewards.levels");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> reserialized = configured.serializeConfig();
            assertEquals("quest.rewards.levels", reserialized.get("localization-route"));
        }

        @Test
        @DisplayName("display label is deserialized from config")
        public void fromSerializedConfig_deserializesDisplayLabel() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 5);
            config.put("display", "Skill Levels");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> reserialized = configured.serializeConfig();
            assertEquals("Skill Levels", reserialized.get("display"));
        }
    }

    @Nested
    @DisplayName("describeForDisplay (no player)")
    class DescribeForDisplayNoPlayer {

        @Test
        @DisplayName("includes amount in display string")
        public void describeForDisplay_includesAmount() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
            assertEquals("3 Redeemable Level(s)", configured.describeForDisplay());
        }

        @Test
        @DisplayName("handles zero amount")
        public void describeForDisplay_handlesZeroAmount() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of());
            assertEquals("0 Redeemable Level(s)", configured.describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier edge cases")
    class WithAmountMultiplierEdgeCases {

        @Test
        @DisplayName("multiplier of 1.0 preserves amount")
        public void withAmountMultiplier_identity_preservesAmount() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            RedeemableLevelsRewardType result = configured.withAmountMultiplier(1.0);
            assertEquals(5L, result.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("very large multiplier produces correct result")
        public void withAmountMultiplier_largeMultiplier() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 4));
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(10.0);
            assertEquals(40L, scaled.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("multiplier preserves localization route and display label")
        public void withAmountMultiplier_preservesExtras() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 4);
            config.put("display", "Custom");
            config.put("localization-route", "some.route");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(3.0);
            Map<String, Object> serialized = scaled.serializeConfig();
            assertEquals(12, ((Number) serialized.get("amount")).intValue());
            assertEquals("Custom", serialized.get("display"));
            assertEquals("some.route", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("Full round-trip with all fields")
    class FullRoundTrip {

        @Test
        @DisplayName("serialize then deserialize preserves all fields")
        public void fullRoundTrip_preservesAllFields() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10))
                    .withLocalizationRoute(Route.fromString("quest.rewards.display"))
                    .withInlineDisplayLabel("Quest Reward");

            Map<String, Object> serialized = configured.serializeConfig();
            RedeemableLevelsRewardType roundTripped = type.fromSerializedConfig(serialized);

            assertEquals(10L, roundTripped.getNumericAmount().getAsLong());
            Map<String, Object> reReserialized = roundTripped.serializeConfig();
            assertEquals("quest.rewards.display", reReserialized.get("localization-route"));
            assertEquals("Quest Reward", reReserialized.get("display"));
        }
    }
}
