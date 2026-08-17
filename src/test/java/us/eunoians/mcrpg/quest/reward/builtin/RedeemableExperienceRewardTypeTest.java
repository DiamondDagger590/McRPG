package us.eunoians.mcrpg.quest.reward.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedeemableExperienceRewardTypeTest extends McRPGBaseTest {

    private RedeemableExperienceRewardType type;

    @BeforeEach
    public void setup() {
        type = new RedeemableExperienceRewardType();
    }

    @DisplayName("getKey returns redeemable_experience key")
    @Test
    public void getKey_returnsExpectedKey() {
        assertEquals(RedeemableExperienceRewardType.KEY, type.getKey());
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
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 300));
        assertEquals(300, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig returns zero amount when key is missing")
    @Test
    public void fromSerializedConfig_returnsZeroWhenAmountMissing() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of());
        assertEquals(0, configured.getNumericAmount().orElse(-1));
    }

    @DisplayName("fromSerializedConfig handles Number subtype (Long)")
    @Test
    public void fromSerializedConfig_handlesLongAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 500L));
        assertEquals(500, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig handles Number subtype (Double)")
    @Test
    public void fromSerializedConfig_handlesDoubleAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 123.9));
        assertEquals(123, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig parses valid string amount")
    @Test
    public void fromSerializedConfig_parsesStringAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", "250"));
        assertEquals(250, configured.getNumericAmount().orElse(0));
    }

    @DisplayName("fromSerializedConfig returns zero for non-numeric string")
    @Test
    public void fromSerializedConfig_returnsZeroForNonNumericString() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", "abc"));
        assertEquals(0, configured.getNumericAmount().orElse(-1));
    }

    @DisplayName("fromSerializedConfig preserves localization-route")
    @Test
    public void fromSerializedConfig_preservesLocalizationRoute() {
        Map<String, Object> config = new HashMap<>();
        config.put("amount", 100);
        config.put("localization-route", "quest.reward.custom");
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("quest.reward.custom", serialized.get("localization-route"));
    }

    @DisplayName("fromSerializedConfig preserves display label")
    @Test
    public void fromSerializedConfig_preservesDisplayLabel() {
        Map<String, Object> config = new HashMap<>();
        config.put("amount", 100);
        config.put("display", "Custom Display");
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(config);
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals("Custom Display", serialized.get("display"));
    }

    @DisplayName("serializeConfig round-trips amount")
    @Test
    public void serializeConfig_roundTripsAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 150));
        Map<String, Object> serialized = configured.serializeConfig();
        assertEquals(150, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("serializeConfig omits display and localization-route when not set")
    @Test
    public void serializeConfig_omitsOptionalFieldsWhenNotSet() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        Map<String, Object> serialized = configured.serializeConfig();
        assertFalse(serialized.containsKey("display"));
        assertFalse(serialized.containsKey("localization-route"));
    }

    @DisplayName("withAmountMultiplier scales correctly")
    @Test
    public void withAmountMultiplier_scalesAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 200));
        RedeemableExperienceRewardType doubled = configured.withAmountMultiplier(1.5);
        assertEquals(300, doubled.getNumericAmount().orElse(0));
    }

    @DisplayName("withAmountMultiplier enforces minimum of 1")
    @Test
    public void withAmountMultiplier_enforcesMinimumOfOne() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 1));
        RedeemableExperienceRewardType scaled = configured.withAmountMultiplier(0.001);
        assertTrue(scaled.getNumericAmount().orElse(0) >= 1);
    }

    @DisplayName("withExactAmount sets the exact amount")
    @Test
    public void withExactAmount_setsExactAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        RedeemableExperienceRewardType exact = configured.withExactAmount(999);
        assertEquals(999, exact.getNumericAmount().orElse(0));
    }

    @DisplayName("withLocalizationRoute returns new instance with route set")
    @Test
    public void withLocalizationRoute_returnsNewInstanceWithRoute() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        var route = dev.dejvokep.boostedyaml.route.Route.fromString("custom.route");
        RedeemableExperienceRewardType withRoute = configured.withLocalizationRoute(route);
        Map<String, Object> serialized = withRoute.serializeConfig();
        assertEquals("custom.route", serialized.get("localization-route"));
        assertEquals(100, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("withInlineDisplayLabel returns new instance with label set")
    @Test
    public void withInlineDisplayLabel_returnsNewInstanceWithLabel() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 100));
        RedeemableExperienceRewardType withLabel = configured.withInlineDisplayLabel("My Label");
        Map<String, Object> serialized = withLabel.serializeConfig();
        assertEquals("My Label", serialized.get("display"));
        assertEquals(100, ((Number) serialized.get("amount")).intValue());
    }

    @DisplayName("describeForDisplay returns formatted string with amount")
    @Test
    public void describeForDisplay_returnsFormattedString() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 500));
        String display = configured.describeForDisplay();
        assertTrue(display.contains("500"));
        assertTrue(display.contains("Redeemable XP"));
    }

    @DisplayName("getNumericAmount returns configured amount as OptionalLong")
    @Test
    public void getNumericAmount_returnsConfiguredAmount() {
        RedeemableExperienceRewardType configured = type.fromSerializedConfig(Map.of("amount", 750));
        assertEquals(750L, configured.getNumericAmount().getAsLong());
    }

    @DisplayName("isScalable returns true")
    @Test
    public void isScalable_returnsTrue() {
        assertTrue(type.isScalable());
    }
}
