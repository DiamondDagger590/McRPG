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

@DisplayName("RedeemableLevelsRewardType Coverage")
class RedeemableLevelsRewardTypeCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("isScalable")
    class IsScalable {

        @Test
        @DisplayName("returns true")
        void isScalable_returnsTrue() {
            assertTrue(new RedeemableLevelsRewardType().isScalable());
        }
    }

    @Nested
    @DisplayName("withExactAmount")
    class WithExactAmount {

        @Test
        @DisplayName("sets exact amount on returned instance")
        void withExactAmount_setsExactAmount() {
            RedeemableLevelsRewardType base = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 10));
            RedeemableLevelsRewardType exact = base.withExactAmount(3);
            assertEquals(3, exact.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("returns new instance")
        void withExactAmount_returnsNewInstance() {
            RedeemableLevelsRewardType base = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 5));
            assertNotSame(base, base.withExactAmount(5));
        }

        @Test
        @DisplayName("preserves localization route and display label")
        void withExactAmount_preservesRouteAndLabel() {
            RedeemableLevelsRewardType base = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 5,
                            "localization-route", "quests.test.rewards.levels",
                            "display", "Level Bonus"
                    ));
            RedeemableLevelsRewardType exact = base.withExactAmount(2);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals("quests.test.rewards.levels", serialized.get("localization-route"));
            assertEquals("Level Bonus", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("base instance returns 0")
        void getNumericAmount_baseInstance_returnsZero() {
            assertEquals(0L, new RedeemableLevelsRewardType().getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("configured instance returns configured amount")
        void getNumericAmount_configuredInstance_returnsAmount() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 7));
            assertEquals(7L, configured.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("is always present")
        void getNumericAmount_isPresent() {
            assertTrue(new RedeemableLevelsRewardType().getNumericAmount().isPresent());
        }
    }

    @Nested
    @DisplayName("describeForDisplay no-arg")
    class DescribeForDisplayNoArg {

        @Test
        @DisplayName("formats amount with Redeemable Level(s) suffix")
        void describeForDisplay_formatsCorrectly() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 3));
            assertEquals("3 Redeemable Level(s)", configured.describeForDisplay());
        }

        @Test
        @DisplayName("base instance shows 0")
        void describeForDisplay_baseInstance_showsZero() {
            assertEquals("0 Redeemable Level(s)", new RedeemableLevelsRewardType().describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns new instance with route set")
        void withLocalizationRoute_returnsNewInstance() {
            RedeemableLevelsRewardType original = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 5));
            RedeemableLevelsRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.levels"));
            assertNotSame(original, withRoute);
            assertTrue(withRoute.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("preserves amount")
        void withLocalizationRoute_preservesAmount() {
            RedeemableLevelsRewardType original = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 8));
            RedeemableLevelsRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.label"));
            assertEquals(8, withRoute.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label set")
        void withInlineDisplayLabel_returnsNewInstance() {
            RedeemableLevelsRewardType original = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 5));
            RedeemableLevelsRewardType withLabel = original.withInlineDisplayLabel("Custom Levels");
            assertNotSame(original, withLabel);
            assertEquals("Custom Levels", withLabel.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves amount and route")
        void withInlineDisplayLabel_preservesAmountAndRoute() {
            RedeemableLevelsRewardType original = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 4,
                            "localization-route", "quests.test.rewards.label"
                    ));
            RedeemableLevelsRewardType withLabel = original.withInlineDisplayLabel("My Label");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals(4, ((Number) serialized.get("amount")).intValue());
            assertEquals("quests.test.rewards.label", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void serializeConfig_omitsDisplayWhenEmpty() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 2));
            assertFalse(configured.serializeConfig().containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        void serializeConfig_omitsLocalizationRouteWhenNull() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 2));
            assertFalse(configured.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("includes display when non-empty")
        void serializeConfig_includesDisplayWhenNonEmpty() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 3, "display", "Level Bonus"));
            assertEquals("Level Bonus", configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("includes localization-route when set")
        void serializeConfig_includesRouteWhenSet() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 3,
                            "localization-route", "quests.mcrpg.test.rewards.levels"
                    ));
            assertEquals("quests.mcrpg.test.rewards.levels", configured.serializeConfig().get("localization-route"));
        }

        @Test
        @DisplayName("round-trips through serialize and deserialize")
        void serializeConfig_roundTrip() {
            Map<String, Object> original = new HashMap<>();
            original.put("amount", 10);
            original.put("display", "Test Display");
            original.put("localization-route", "quests.ns.key.rewards.label");

            RedeemableLevelsRewardType first = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(original);
            Map<String, Object> serialized = first.serializeConfig();
            RedeemableLevelsRewardType second = new RedeemableLevelsRewardType()
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
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of());
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("string number amount is parsed correctly")
        void fromSerializedConfig_stringAmount_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "7");
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(config);
            assertEquals(7, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("non-numeric amount deserializes to 0")
        void fromSerializedConfig_nonNumericAmount_deserializesToZero() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "not_a_number");
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(config);
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("Long type amount is handled correctly")
        void fromSerializedConfig_longAmount_handledCorrectly() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 5L));
            assertEquals(5, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("Double type amount truncates to int")
        void fromSerializedConfig_doubleAmount_truncatesToInt() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of("amount", 3.9));
            assertEquals(3, configured.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses amount from section")
        void parseConfig_parsesAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(15);
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType().parseConfig(section);
            assertEquals(15, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("zero amount produces instance with zero")
        void parseConfig_zeroAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(0);
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType().parseConfig(section);
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("negative amount produces instance with negative value")
        void parseConfig_negativeAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(-2);
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType().parseConfig(section);
            assertEquals(-2, configured.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("preserves localization route")
        void withAmountMultiplier_preservesRoute() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 6,
                            "localization-route", "quests.test.rewards.levels"
                    ));
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals("quests.test.rewards.levels", scaled.serializeConfig().get("localization-route"));
        }

        @Test
        @DisplayName("preserves display label")
        void withAmountMultiplier_preservesDisplayLabel() {
            RedeemableLevelsRewardType configured = new RedeemableLevelsRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 6,
                            "display", "Level Reward"
                    ));
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(2.0);
            assertEquals("Level Reward", scaled.serializeConfig().get("display"));
        }
    }
}
