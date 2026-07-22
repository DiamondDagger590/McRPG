package us.eunoians.mcrpg.quest.reward.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedeemableExperienceRewardTypeCoverageTest extends McRPGBaseTest {

    private RedeemableExperienceRewardType type;

    @BeforeEach
    void setUp() {
        type = new RedeemableExperienceRewardType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns redeemable_experience key")
        void getKey_returnsExpectedKey() {
            assertEquals(RedeemableExperienceRewardType.KEY, type.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(us.eunoians.mcrpg.expansion.McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("parses amount from section")
        void parseConfig_parsesAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(500);

            RedeemableExperienceRewardType configured = type.parseConfig(section);

            assertEquals(500L, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("defaults to 0 when amount is missing")
        void parseConfig_defaultsToZero_whenAmountMissing() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(0);

            RedeemableExperienceRewardType configured = type.parseConfig(section);

            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("handles negative amount without throwing")
        void parseConfig_handlesNegativeAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(-10);

            RedeemableExperienceRewardType configured = type.parseConfig(section);

            assertEquals(-10L, configured.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("isScalable")
    class IsScalable {

        @Test
        @DisplayName("returns true")
        void isScalable_returnsTrue() {
            assertTrue(type.isScalable());
        }
    }

    @Nested
    @DisplayName("withExactAmount")
    class WithExactAmount {

        @Test
        @DisplayName("sets the exact amount")
        void withExactAmount_setsExactAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType exact = configured.withExactAmount(999);
            assertEquals(999L, exact.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("preserves display label")
        void withExactAmount_preservesDisplayLabel() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(
                    Map.of("amount", 100, "display", "My Label"));
            RedeemableExperienceRewardType exact = configured.withExactAmount(500);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals(500, ((Number) serialized.get("amount")).intValue());
            assertEquals("My Label", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("stores localization route in serialized config")
        void withLocalizationRoute_storesRoute() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 200));
            RedeemableExperienceRewardType withRoute = configured.withLocalizationRoute(
                    dev.dejvokep.boostedyaml.route.Route.fromString("quests.test.rewards.xp"));
            Map<String, Object> serialized = withRoute.serializeConfig();
            assertEquals("quests.test.rewards.xp", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("stores display label in serialized config")
        void withInlineDisplayLabel_storesLabel() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 300));
            RedeemableExperienceRewardType withLabel = configured.withInlineDisplayLabel("Custom Label");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals("Custom Label", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void serializeConfig_omitsDisplay_whenEmpty() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        void serializeConfig_omitsRoute_whenNull() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("localization-route"));
        }

        @Test
        @DisplayName("includes display when set")
        void serializeConfig_includesDisplay_whenSet() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(
                    Map.of("amount", 100, "display", "Test Display"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Test Display", serialized.get("display"));
        }

        @Test
        @DisplayName("includes localization-route when set")
        void serializeConfig_includesRoute_whenSet() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("localization-route", "quests.my.route");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("quests.my.route", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("round-trips amount correctly")
        void serializeConfig_roundTripsAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 250));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(250, ((Number) serialized.get("amount")).intValue());
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("handles missing amount key")
        void fromSerializedConfig_handlesNoAmountKey() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of());
            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("parses Number type for amount")
        void fromSerializedConfig_parsesNumber() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 750));
            assertEquals(750L, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("parses String number for amount")
        void fromSerializedConfig_parsesStringNumber() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", "123"));
            assertEquals(123L, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("returns 0 for non-numeric string amount")
        void fromSerializedConfig_returnsZero_whenNonNumericString() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", "not_a_number"));
            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("deserializes display label")
        void fromSerializedConfig_deserializesDisplayLabel() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("display", "Custom Display");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Custom Display", serialized.get("display"));
        }

        @Test
        @DisplayName("deserializes localization-route")
        void fromSerializedConfig_deserializesLocalizationRoute() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 100);
            config.put("localization-route", "path.to.label");
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("path.to.label", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withAmountMultiplier")
    class WithAmountMultiplier {

        @Test
        @DisplayName("scales amount correctly")
        void withAmountMultiplier_scalesAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType doubled = configured.withAmountMultiplier(2.0);
            assertEquals(200L, doubled.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("enforces minimum of 1")
        void withAmountMultiplier_enforcesMinimumOfOne() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(0.001);
            assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
        }

        @Test
        @DisplayName("preserves display label through scaling")
        void withAmountMultiplier_preservesDisplayLabel() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(
                    Map.of("amount", 100, "display", "My Label"));
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(2.0);
            Map<String, Object> serialized = scaled.serializeConfig();
            assertEquals("My Label", serialized.get("display"));
        }

        @Test
        @DisplayName("truncates fractional result to int")
        void withAmountMultiplier_truncatesFractionalResult() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(0.33);
            assertEquals(33L, scaled.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("describeForDisplay")
    class DescribeForDisplay {

        @Test
        @DisplayName("includes amount in no-arg description")
        void describeForDisplay_includesAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 500));
            assertEquals("500 Redeemable XP", configured.describeForDisplay());
        }

        @Test
        @DisplayName("zero amount returns '0 Redeemable XP'")
        void describeForDisplay_zeroAmount() {
            assertEquals("0 Redeemable XP", type.describeForDisplay());
        }

        @Test
        @DisplayName("returns non-null string")
        void describeForDisplay_returnsNonNull() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
            assertFalse(configured.describeForDisplay().isEmpty());
        }
    }

    @Nested
    @DisplayName("getNumericAmount")
    class GetNumericAmount {

        @Test
        @DisplayName("always returns present OptionalLong")
        void getNumericAmount_alwaysPresent() {
            assertTrue(type.getNumericAmount().isPresent());
        }

        @Test
        @DisplayName("unconfigured type returns 0")
        void getNumericAmount_returnsZero_whenUnconfigured() {
            assertEquals(0L, type.getNumericAmount().getAsLong());
        }

        @Test
        @DisplayName("configured type returns configured amount")
        void getNumericAmount_returnsConfiguredAmount() {
            RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 750));
            assertEquals(750L, configured.getNumericAmount().getAsLong());
        }
    }
}
