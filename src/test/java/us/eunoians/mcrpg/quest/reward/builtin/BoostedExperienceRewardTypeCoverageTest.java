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

@DisplayName("BoostedExperienceRewardType Coverage")
class BoostedExperienceRewardTypeCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("isScalable")
    class IsScalable {

        @Test
        @DisplayName("returns true")
        void isScalable_returnsTrue() {
            assertTrue(new BoostedExperienceRewardType().isScalable());
        }
    }

    @Nested
    @DisplayName("withExactAmount")
    class WithExactAmount {

        @Test
        @DisplayName("sets exact amount on returned instance")
        void withExactAmount_setsExactAmount() {
            BoostedExperienceRewardType base = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 1000));
            BoostedExperienceRewardType exact = base.withExactAmount(250);
            assertEquals(250, exact.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("returns new instance")
        void withExactAmount_returnsNewInstance() {
            BoostedExperienceRewardType base = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 500));
            assertNotSame(base, base.withExactAmount(500));
        }

        @Test
        @DisplayName("preserves localization route and display label")
        void withExactAmount_preservesRouteAndLabel() {
            BoostedExperienceRewardType base = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 500,
                            "localization-route", "quests.test.rewards.boost",
                            "display", "Boost Reward"
                    ));
            BoostedExperienceRewardType exact = base.withExactAmount(200);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals("quests.test.rewards.boost", serialized.get("localization-route"));
            assertEquals("Boost Reward", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("describeForDisplay no-arg")
    class DescribeForDisplayNoArg {

        @Test
        @DisplayName("formats amount with Boosted XP suffix")
        void describeForDisplay_formatsCorrectly() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 300));
            assertEquals("300 Boosted XP", configured.describeForDisplay());
        }

        @Test
        @DisplayName("base instance shows 0")
        void describeForDisplay_baseInstance_showsZero() {
            assertEquals("0 Boosted XP", new BoostedExperienceRewardType().describeForDisplay());
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("returns new instance with route set")
        void withLocalizationRoute_returnsNewInstance() {
            BoostedExperienceRewardType original = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            BoostedExperienceRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.boost"));
            assertNotSame(original, withRoute);
            assertTrue(withRoute.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("preserves amount")
        void withLocalizationRoute_preservesAmount() {
            BoostedExperienceRewardType original = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 800));
            BoostedExperienceRewardType withRoute = original.withLocalizationRoute(
                    Route.fromString("quests.mcrpg.test.rewards.label"));
            assertEquals(800, withRoute.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("returns new instance with label set")
        void withInlineDisplayLabel_returnsNewInstance() {
            BoostedExperienceRewardType original = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            BoostedExperienceRewardType withLabel = original.withInlineDisplayLabel("XP Boost");
            assertNotSame(original, withLabel);
            assertEquals("XP Boost", withLabel.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("preserves amount and route")
        void withInlineDisplayLabel_preservesAmountAndRoute() {
            BoostedExperienceRewardType original = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 400,
                            "localization-route", "quests.test.rewards.label"
                    ));
            BoostedExperienceRewardType withLabel = original.withInlineDisplayLabel("My Label");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals(400, ((Number) serialized.get("amount")).intValue());
            assertEquals("quests.test.rewards.label", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void serializeConfig_omitsDisplayWhenEmpty() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.serializeConfig().containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        void serializeConfig_omitsLocalizationRouteWhenNull() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.serializeConfig().containsKey("localization-route"));
        }

        @Test
        @DisplayName("includes display when non-empty")
        void serializeConfig_includesDisplayWhenNonEmpty() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 100, "display", "Boost Bonus"));
            assertEquals("Boost Bonus", configured.serializeConfig().get("display"));
        }

        @Test
        @DisplayName("includes localization-route when set")
        void serializeConfig_includesRouteWhenSet() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 100,
                            "localization-route", "quests.mcrpg.test.rewards.boost"
                    ));
            assertEquals("quests.mcrpg.test.rewards.boost", configured.serializeConfig().get("localization-route"));
        }

        @Test
        @DisplayName("round-trips through serialize and deserialize")
        void serializeConfig_roundTrip() {
            Map<String, Object> original = new HashMap<>();
            original.put("amount", 750);
            original.put("display", "Boost Display");
            original.put("localization-route", "quests.ns.key.rewards.label");

            BoostedExperienceRewardType first = new BoostedExperienceRewardType()
                    .fromSerializedConfig(original);
            Map<String, Object> serialized = first.serializeConfig();
            BoostedExperienceRewardType second = new BoostedExperienceRewardType()
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
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of());
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("string number amount is parsed correctly")
        void fromSerializedConfig_stringAmount_parsedCorrectly() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "500");
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(config);
            assertEquals(500, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("non-numeric amount deserializes to 0")
        void fromSerializedConfig_nonNumericAmount_deserializesToZero() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", "invalid");
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(config);
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("Long type amount is handled correctly")
        void fromSerializedConfig_longAmount_handledCorrectly() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 300L));
            assertEquals(300, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("Double type amount truncates to int")
        void fromSerializedConfig_doubleAmount_truncatesToInt() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of("amount", 199.9));
            assertEquals(199, configured.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses amount from section")
        void parseConfig_parsesAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(1200);
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType().parseConfig(section);
            assertEquals(1200, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("zero amount produces instance with zero")
        void parseConfig_zeroAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(0);
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType().parseConfig(section);
            assertEquals(0, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("negative amount produces instance with negative value")
        void parseConfig_negativeAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(-10);
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType().parseConfig(section);
            assertEquals(-10, configured.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("preserves localization route")
        void withAmountMultiplier_preservesRoute() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 400,
                            "localization-route", "quests.test.rewards.boost"
                    ));
            BoostedExperienceRewardType scaled = configured.withAmountMultiplier(0.5);
            assertEquals("quests.test.rewards.boost", scaled.serializeConfig().get("localization-route"));
        }

        @Test
        @DisplayName("preserves display label")
        void withAmountMultiplier_preservesDisplayLabel() {
            BoostedExperienceRewardType configured = new BoostedExperienceRewardType()
                    .fromSerializedConfig(Map.of(
                            "amount", 400,
                            "display", "Boost Reward"
                    ));
            BoostedExperienceRewardType scaled = configured.withAmountMultiplier(2.0);
            assertEquals("Boost Reward", scaled.serializeConfig().get("display"));
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("base instance returns 0")
        void getNumericAmount_baseInstance_returnsZero() {
            assertEquals(0L, new BoostedExperienceRewardType().getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("is always present")
        void getNumericAmount_isPresent() {
            assertTrue(new BoostedExperienceRewardType().getNumericAmount().isPresent());
        }
    }
}
