package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedeemableLevelsRewardTypeTest extends McRPGBaseTest {

    private RedeemableLevelsRewardType type;

    @BeforeEach
    public void setup() {
        type = new RedeemableLevelsRewardType();
    }

    @DisplayName("getKey returns redeemable_levels key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(RedeemableLevelsRewardType.KEY, type.getKey());
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @DisplayName("fromSerializedConfig preserves amount")
    @Test
    public void fromSerializedConfig_preservesAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
        assertEquals(5, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig returns zero amount when key is missing")
    @Test
    public void fromSerializedConfig_returnsZeroWhenAmountMissing() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of());
        assertEquals(0, configured.getNumericAmount().orElse(-1));
    }

    @DisplayName("fromSerializedConfig handles Number subtype (Long)")
    @Test
    public void fromSerializedConfig_handlesLongAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 10L));
        assertEquals(10, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig handles Number subtype (Double)")
    @Test
    public void fromSerializedConfig_handlesDoubleAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 7.8));
        assertEquals(7, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig parses valid string amount")
    @Test
    public void fromSerializedConfig_parsesStringAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", "12"));
        assertEquals(12, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig returns zero for non-numeric string")
    @Test
    public void fromSerializedConfig_returnsZeroForNonNumericString() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", "xyz"));
        assertEquals(0, configured.getNumericAmount().orElse(-1));
    }

    @DisplayName("fromSerializedConfig preserves localization-route")
    @Test
    public void fromSerializedConfig_preservesLocalizationRoute() {
        Map<String, Object> config = new HashMap<>();
        config.put("amount", 5);
        config.put("localization-route", "quest.reward.levels");
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("quest.reward.levels", serialized.get("localization-route"));
    }

    @DisplayName("fromSerializedConfig preserves display label")
    @Test
    public void fromSerializedConfig_preservesDisplayLabel() {
        Map<String, Object> config = new HashMap<>();
        config.put("amount", 5);
        config.put("display", "Custom Levels");
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("Custom Levels", serialized.get("display"));
    }

    @DisplayName("serializeConfig round-trips amount")
    @Test
    public void serializeConfig_roundTripsAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(3, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("serializeConfig omits display and localization-route when not set")
    @Test
    public void serializeConfig_omitsOptionalFieldsWhenNotSet() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
        Map<String, Object> serialized = configured.serializeConfig();
        assertFalse(serialized.containsKey("display"));
        assertFalse(serialized.containsKey("localization-route"));
    }

    @DisplayName("withAmountMultiplier scales correctly")
    @Test
    public void withAmountMultiplier_scalesAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 4));
        RedeemableLevelsRewardType doubled = configured.withAmountMultiplier(2.0);
        assertEquals(8, doubled.getNumericAmount().orElse(0));
    }

    @DisplayName("withAmountMultiplier enforces minimum of 1")
    @Test
    public void withAmountMultiplier_enforcesMinimumOfOne() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
        RedeemableLevelsRewardType scaled = configured.withAmountMultiplier(0.001);
        assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
    }

    @DisplayName("withExactAmount sets the exact amount")
    @Test
    public void withExactAmount_setsExactAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
        RedeemableLevelsRewardType exact = configured.withExactAmount(20);
        assertEquals(20, exact.getNumericAmount().orElse(0));
    }

    @DisplayName("withLocalizationRoute returns new instance with route set")
    @Test
    public void withLocalizationRoute_returnsNewInstanceWithRoute() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
        var route = dev.dejvokep.boostedyaml.route.Route.fromString("custom.route");
        RedeemableLevelsRewardType withRoute = configured.withLocalizationRoute(route);
        Map<String, Object> serialized = withRoute.serializeConfig();
        assertEquals("custom.route", serialized.get("localization-route"));
        assertEquals(5, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("withInlineDisplayLabel returns new instance with label set")
    @Test
    public void withInlineDisplayLabel_returnsNewInstanceWithLabel() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 5));
        RedeemableLevelsRewardType withLabel = configured.withInlineDisplayLabel("My Label");
        Map<String, Object> serialized = withLabel.serializeConfig();
        assertEquals("My Label", serialized.get("display"));
        assertEquals(5, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("describeForDisplay returns formatted string with amount")
    @Test
    public void describeForDisplay_returnsFormattedString() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 3));
        String display = configured.describeForDisplay();
        assertTrue(display.contains("3"));
        assertTrue(display.contains("Redeemable Level"));
    }

    @DisplayName("getNumericAmount returns configured amount as OptionalLong")
    @Test
    public void getNumericAmount_returnsConfiguredAmount() {
        RedeemableLevelsRewardType configured = type.fromSerializedConfig(Map.of("amount", 8));
        assertEquals(8L, configured.getNumericAmount().getAsLong());
    }

    @DisplayName("isScalable returns true")
    @Test
    public void isScalable_returnsTrue() {
        assertTrue(type.isScalable());
    }
}
