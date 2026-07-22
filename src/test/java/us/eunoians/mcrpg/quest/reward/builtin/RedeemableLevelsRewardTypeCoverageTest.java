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

class RedeemableLevelsRewardTypeCoverageTest extends McRPGBaseTest {

    private RedeemableLevelsRewardType type;

    @BeforeEach
    void setUp() {
        type = new RedeemableLevelsRewardType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @Test
        @DisplayName("getKey returns redeemable_levels key")
        void getKey_returnsExpectedKey() {
            assertEquals(RedeemableLevelsRewardType.KEY, type.getKey());
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
            when(section.getInt("amount", 0)).thenReturn(3);

            RedeemableLevelsRewardType configured = type.parseConfig(section);

            assertEquals(3L, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("defaults to 0 when amount is missing")
        void parseConfig_defaultsToZero_whenAmountMissing() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(0);

            RedeemableLevelsRewardType configured = type.parseConfig(section);

            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("handles negative amount without throwing")
        void parseConfig_handlesNegativeAmount() {
            Section section = mock(Section.class);
            when(section.getInt("amount", 0)).thenReturn(-2);

            RedeemableLevelsRewardType configured = type.parseConfig(section);

            assertEquals(-2L, configured.getNumericAmount().orElse(0));
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
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            RedeemableLevelsRewardType exact = configured.withExactAmount(20);
            assertEquals(20L, exact.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("preserves display label")
        void withExactAmount_preservesDisplayLabel() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(
                    Map.of("amount", 5, "display", "My Levels"));
            RedeemableLevelsRewardType exact = configured.withExactAmount(10);
            Map<String, Object> serialized = exact.serializeConfig();
            assertEquals(10, ((Number) serialized.get("amount")).intValue());
            assertEquals("My Levels", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("withLocalizationRoute")
    class WithLocalizationRoute {

        @Test
        @DisplayName("stores localization route in serialized config")
        void withLocalizationRoute_storesRoute() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            RedeemableLevelsRewardType withRoute = configured.withLocalizationRoute(
                    dev.dejvokep.boostedyaml.route.Route.fromString("quests.test.rewards.levels"));
            Map<String, Object> serialized = withRoute.serializeConfig();
            assertEquals("quests.test.rewards.levels", serialized.get("localization-route"));
        }
    }

    @Nested
    @DisplayName("withInlineDisplayLabel")
    class WithInlineDisplayLabel {

        @Test
        @DisplayName("stores display label in serialized config")
        void withInlineDisplayLabel_storesLabel() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10));
            RedeemableLevelsRewardType withLabel = configured.withInlineDisplayLabel("Custom Levels");
            Map<String, Object> serialized = withLabel.serializeConfig();
            assertEquals("Custom Levels", serialized.get("display"));
        }
    }

    @Nested
    @DisplayName("serializeConfig")
    class SerializeConfig {

        @Test
        @DisplayName("omits display when empty")
        void serializeConfig_omitsDisplay_whenEmpty() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("display"));
        }

        @Test
        @DisplayName("omits localization-route when null")
        void serializeConfig_omitsRoute_whenNull() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            Map<String, Object> serialized = configured.serializeConfig();
            assertFalse(serialized.containsKey("localization-route"));
        }

        @Test
        @DisplayName("includes display when set")
        void serializeConfig_includesDisplay_whenSet() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(
                    Map.of("amount", 5, "display", "Test Display"));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Test Display", serialized.get("display"));
        }

        @Test
        @DisplayName("includes localization-route when set")
        void serializeConfig_includesRoute_whenSet() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 5);
            config.put("localization-route", "quests.my.route");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("quests.my.route", serialized.get("localization-route"));
        }

        @Test
        @DisplayName("round-trips amount correctly")
        void serializeConfig_roundTripsAmount() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 8));
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals(8, ((Number) serialized.get("amount")).intValue());
        }
    }

    @Nested
    @DisplayName("fromSerializedConfig")
    class FromSerializedConfig {

        @Test
        @DisplayName("handles missing amount key")
        void fromSerializedConfig_handlesNoAmountKey() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of());
            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("parses Number type for amount")
        void fromSerializedConfig_parsesNumber() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 15));
            assertEquals(15L, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("parses String number for amount")
        void fromSerializedConfig_parsesStringNumber() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", "7"));
            assertEquals(7L, configured.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("returns 0 for non-numeric string amount")
        void fromSerializedConfig_returnsZero_whenNonNumericString() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", "not_a_number"));
            assertEquals(0L, configured.getNumericAmount().orElse(-1));
        }

        @Test
        @DisplayName("deserializes display label")
        void fromSerializedConfig_deserializesDisplayLabel() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 5);
            config.put("display", "Custom Display");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
            Map<String, Object> serialized = configured.serializeConfig();
            assertEquals("Custom Display", serialized.get("display"));
        }

        @Test
        @DisplayName("deserializes localization-route")
        void fromSerializedConfig_deserializesLocalizationRoute() {
            Map<String, Object> config = new HashMap<>();
            config.put("amount", 5);
            config.put("localization-route", "path.to.label");
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
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
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10));
            RedeemableLevelsRewardType tripled = configured.withAmountMultiplier(3.0);
            assertEquals(30L, tripled.getNumericAmount().orElse(0));
        }

        @Test
        @DisplayName("enforces minimum of 1")
        void withAmountMultiplier_enforcesMinimumOfOne() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(0.001);
            assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
        }

        @Test
        @DisplayName("preserves display label through scaling")
        void withAmountMultiplier_preservesDisplayLabel() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(
                    Map.of("amount", 10, "display", "My Levels"));
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(2.0);
            Map<String, Object> serialized = scaled.serializeConfig();
            assertEquals("My Levels", serialized.get("display"));
        }

        @Test
        @DisplayName("truncates fractional result to int")
        void withAmountMultiplier_truncatesFractionalResult() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10));
            RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(0.33);
            assertEquals(3L, scaled.getNumericAmount().orElse(0));
        }
    }

    @Nested
    @DisplayName("describeForDisplay")
    class DescribeForDisplay {

        @Test
        @DisplayName("includes amount in no-arg description")
        void describeForDisplay_includesAmount() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
            assertEquals("5 Redeemable Level(s)", configured.describeForDisplay());
        }

        @Test
        @DisplayName("zero amount returns '0 Redeemable Level(s)'")
        void describeForDisplay_zeroAmount() {
            assertEquals("0 Redeemable Level(s)", type.describeForDisplay());
        }

        @Test
        @DisplayName("returns non-null string")
        void describeForDisplay_returnsNonNull() {
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
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
            RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 15));
            assertEquals(15L, configured.getNumericAmount().getAsLong());
        }
    }
}
