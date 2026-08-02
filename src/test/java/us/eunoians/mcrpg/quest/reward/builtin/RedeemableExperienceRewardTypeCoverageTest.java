package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("RedeemableExperienceRewardType Coverage")
class RedeemableExperienceRewardTypeCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("isScalable")
    class IsScalable {

        @Test
        @DisplayName("returns true")
        void isScalable_returnsTrue() {
            assertTrue(new RedeemableExperienceRewardType().isScalable());
        }
    }

    @Nested
    @DisplayName("withExactAmount")
    class WithExactAmount {

        @Test
        @DisplayName("sets exact amount on returned instance")
        void withExactAmount_setsExactAmount() {
            RedeemableExperienceRewardType base = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 500));
            RedeemableExperienceRewardType exact = base.withExactAmount(42);
            assertEquals(42, exact.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("returns new instance")
        void withExactAmount_returnsNewInstance() {
            RedeemableExperienceRewardType base = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertNotSame(base, base.withExactAmount(100));
        }

        @Test
        @DisplayName("preserves localization route and display label")
        void withExactAmount_preservesRouteAndLabel() {
            RedeemableExperienceRewardType base = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 100,
                            "localization-route", "quests.test.rewards.xp",
                            "display", "Custom Label"
                    ));
            RedeemableExperienceRewardType exact = base.withExactAmount(75);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals("quests.test.rewards.xp", serialized.get("localization-route"));
            assertEquals("Custom Label", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("base instance returns 0")
        void getNumericAmount_baseInstance_returnsZero() {
            assertEquals(0L, new RedeemableExperienceRewardType().getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("configured instance returns configured amount")
        void getNumericAmount_configuredInstance_returnsAmount() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 999));
            assertEquals(999L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("is always present")
        void getNumericAmount_isPresent() {
            assertTrue(new RedeemableExperienceRewardType().getNumericAmount().isPresent());
        }
    }

    @Nested
    @DisplayName("describeForDisplay no-arg")
    class DescribeForDisplayNoArg {

        @Test
        @DisplayName("formats amount with Redeemable XP suffix")
        void describeForDisplay_formatsCorrectly() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 250));
            assertEquals("250 Redeemable XP", configured.describeForDisplay());
        }

        @Test
        @DisplayName("base instance shows 0")
        void describeForDisplay_baseInstance_showsZero() {
            assertEquals("0 Redeemable XP", new RedeemableExperienceRewardType().describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns new instance with route set")
        void withLocalizationRoute_returnsNewInstance() {
            RedeemableExperienceRewardType original = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.xp"));
            assertNotSame(original, withRoute);
            assertTrue(withRoute.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("preserves amount")
        void withLocalizationRoute_preservesAmount() {
            RedeemableExperienceRewardType original = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 350));
            RedeemableExperienceRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.label"));
            assertEquals(350, withRoute.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label set")
        void withInlineDisplayLabel_returnsNewInstance() {
            RedeemableExperienceRewardType original = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType withLabel = original.withInlineDisplayLabel("Custom XP Reward");
            assertNotSame(original, withLabel);
            assertEquals("Custom XP Reward", withLabel.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves amount and route")
        void withInlineDisplayLabel_preservesAmountAndRoute() {
            RedeemableExperienceRewardType original = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 200,
                            "localization-route", "quests.test.rewards.label"
                    ));
            RedeemableExperienceRewardType withLabel = original.withInlineDisplayLabel("My Label");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals(200, ((Number) serialized.get("amount")).intValue());
            assertEquals("quests.test.rewards.label", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void serializeConfig_omitsDisplayWhenEmpty() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.serializeConfig().containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        void serializeConfig_omitsLocalizationRouteWhenNull() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("includes display when non-empty")
        void serializeConfig_includesDisplayWhenNonEmpty() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100, "display", "XP Bonus"));
            assertEquals("XP Bonus", configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("includes localization-route when set")
        void serializeConfig_includesRouteWhenSet() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 100,
                            "localization-route", "quests.mcrpg.test.rewards.xp"
                    ));
            assertEquals("quests.mcrpg.test.rewards.xp", configured.serializeConfig().get("localization-route"));
        }

        @Test
        @DisplayName("round-trips through serialize and deserialize")
        void serializeConfig_roundTrip() {
            Map<String, Object> original = new HashMap<>();
            original.put("amount", 500);
            original.put("display", "Test Display");
            original.put("localization-route", "quests.ns.key.rewards.label");

            RedeemableExperienceRewardType first = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(original);
            Map<String, Object> serialized = first.serializeConfig();
            RedeemableExperienceRewardType second = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(serialized);
            Map<String, Object> reSerialized = second.serializeConfig();

            assertEquals(serialized.get("amount"), reSerialized.get("amount"));
            assertEquals(serialized.get("display"), reSerialized.get("display"));
            assertEquals(serialized.get("localization-route"), reSerialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig edge cases")
    class FromSerializedConfigEdgeCases {

        @Test
        @DisplayName("missing amount key deserializes to 0")
        void fromSerializedConfig_missingAmount_deserializesToZero() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of());
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("string number amount is parsed correctly")
        void fromSerializedConfig_stringAmount_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "123");
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(config);
            assertEquals(123, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("non-numeric amount deserializes to 0")
        void fromSerializedConfig_nonNumericAmount_deserializesToZero() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "not_a_number");
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(config);
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("Long type amount is handled correctly")
        void fromSerializedConfig_longAmount_handledCorrectly() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 42L));
            assertEquals(42, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("Double type amount truncates to int")
        void fromSerializedConfig_doubleAmount_truncatesToInt() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 99.7));
            assertEquals(99, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("missing display defaults to empty string")
        void fromSerializedConfig_missingDisplay_defaultsToEmpty() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.serializeConfig().containsKey("display"));
        }

        @Test
        @DisplayName("missing localization-route defaults to null")
        void fromSerializedConfig_missingRoute_defaultsToNull() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.serializeConfig().containsKey("localization-route"));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses amount from section")
        void parseConfig_parsesAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(750);
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType().parseConfig(section);
            assertEquals(750, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("zero amount produces instance with zero")
        void parseConfig_zeroAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(0);
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType().parseConfig(section);
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("negative amount produces instance with negative value")
        void parseConfig_negativeAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(-5);
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType().parseConfig(section);
            assertEquals(-5, configured.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("preserves localization route")
        void withAmountMultiplier_preservesRoute() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 200,
                            "localization-route", "quests.test.rewards.xp"
                    ));
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals("quests.test.rewards.xp", scaled.serializeConfig().get("localization-route"));
        }

        @Test
        @DisplayName("preserves display label")
        void withAmountMultiplier_preservesDisplayLabel() {
            RedeemableExperienceRewardType configured = new RedeemableExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 200,
                            "display", "XP Reward"
                    ));
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(2.0);
            assertEquals("XP Reward", scaled.serializeConfig().get("display"));
        }
    }
}
